"""Life routes, WS11 (architecture §9.3 Life flow): Asks, Senpai pairs, the Senpai link, Meet-ups, Support Log.
Thin: parse → service → envelope. Paths match frontend/src/services/wakatta.ts."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Header, Path, Query, Request, Response, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES
from app.schemas.asks import AskOut
from app.schemas.common import IdIn
from app.schemas.senpai import (
    ForwardedAsk,
    MeetupCreate,
    MeetupOut,
    SenpaiPairCreate,
    SenpaiPairCreated,
    SenpaiPairOut,
    SenpaiReplyIn,
    SenpaiView,
    SupportLogQuery,
)
from app.services import asks, senpai, support_log
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["life"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
Worker = Annotated[Principal, Depends(require("worker"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]
OptionalKey = Annotated[uuid.UUID | None, Header(alias="Idempotency-Key")]  # optional: the web app sends none yet
# The Senpai link token is 43 url-safe characters (secrets.token_urlsafe(32)); demo tokens are shorter.
SenpaiToken = Annotated[str, Path(min_length=8, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")]


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


@router.post("/asks", status_code=201, summary="Ask by text or voice; answered only from published Notices")
def create_ask(
    who: Worker,
    key: IdempotencyKey,
    db: DbSession,
    request: Request,
    text: Annotated[str | None, Form(max_length=1000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
) -> Envelope[AskOut]:
    voice = (audio.file.read(MAX_AUDIO_BYTES + 1) or None) if audio is not None else None
    return Envelope(data=asks.create(db, who, text, voice, key, _ai(request), now_utc()))


@router.get("/asks", summary="Your own Asks, newest first")
def list_asks(who: Worker, db: DbSession, page: Paging) -> Envelope[Page[AskOut]]:
    return Envelope(data=asks.list_own(db, who, page))


@router.post("/asks/{ask_id}/forward", summary="Send an Ask to your Senpai; 409 NO_ACTIVE_PAIR without one")
def forward_ask(who: Worker, ask_id: IdIn, db: DbSession, request: Request) -> Envelope[AskOut]:
    return Envelope(data=asks.forward(db, who, int(ask_id), _ai(request), now_utc()))


@router.get("/senpai-pairs", summary="Senpai pairs of your shop")
def list_pairs(who: Foreman, db: DbSession, page: Paging) -> Envelope[Page[SenpaiPairOut]]:
    return Envelope(data=senpai.list_pairs(db, who, page))


@router.post("/senpai-pairs", status_code=201, summary="Pair a Worker with a Senpai; the link token is shown once")
def create_pair(who: Foreman, body: SenpaiPairCreate, db: DbSession) -> Envelope[SenpaiPairCreated]:
    return Envelope(data=senpai.create_pair(db, who, body, now_utc()))


@router.delete("/senpai-pairs/{pair_id}", summary="Revoke a Senpai link")
def revoke_pair(who: Foreman, pair_id: IdIn, db: DbSession) -> Envelope[None]:
    senpai.revoke_pair(db, who, int(pair_id), now_utc())
    return Envelope(data=None)


@router.get("/meetups", summary="Meet-ups, newest first (Workers: their own; Foremen: the shop's)")
def list_meetups(who: ShopMember, db: DbSession, page: Paging) -> Envelope[Page[MeetupOut]]:
    return Envelope(data=senpai.list_meetups(db, who, page))


@router.post("/meetups", status_code=201, summary="Log a Meet-up with your Senpai")
def worker_meetup(who: Worker, body: MeetupCreate, db: DbSession, key: OptionalKey = None) -> Envelope[MeetupOut]:
    return Envelope(data=senpai.worker_meetup(db, who, body, now_utc(), key))


@router.get("/senpai-pairs/me", summary="Your own Senpai pair, or null (no link token)")
def my_senpai_pair(who: Worker, db: DbSession) -> Envelope[SenpaiPairOut | None]:
    return Envelope(data=senpai.my_pair(db, who, now_utc()))


@router.get("/senpai/{token}", summary="The Senpai page: their Worker, forwarded Asks, district dates, Meet-ups")
def senpai_view(token: SenpaiToken, db: DbSession) -> Envelope[SenpaiView]:
    return Envelope(data=senpai.view(db, token, now_utc()))


@router.post("/senpai/{token}/asks/{ask_id}/reply", summary="Answer a forwarded Ask in Japanese")
def senpai_reply(
    token: SenpaiToken, ask_id: IdIn, body: SenpaiReplyIn, db: DbSession, request: Request
) -> Envelope[ForwardedAsk]:
    return Envelope(data=senpai.reply(db, token, int(ask_id), body, _ai(request), now_utc()))


@router.post("/senpai/{token}/meetups", status_code=201, summary="Log a Meet-up as the Senpai")
def senpai_meetup(token: SenpaiToken, body: MeetupCreate, db: DbSession) -> Envelope[MeetupOut]:
    return Envelope(data=senpai.senpai_meetup(db, token, body, now_utc()))


@router.get(
    "/support-log.csv",
    summary="Support Log for the 登録支援機関 (CSV, UTF-8 with BOM, not enveloped)",
    response_class=Response,
)
def support_log_csv(who: Foreman, db: DbSession, q: Annotated[SupportLogQuery, Query()]) -> Response:
    data = support_log.export(db, who, q)
    filename = f"support-log-{q.from_.isoformat()}-{q.to.isoformat()}.csv"
    headers = {"Content-Disposition": f'attachment; filename="{filename}"', "Cache-Control": "no-store"}
    return Response(data, media_type="text/csv; charset=utf-8", headers=headers)
