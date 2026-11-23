"""Briefing routes, WS6 (朝礼). Thin: parse → service → envelope. Paths match frontend/src/services/wakatta.ts
(`createBriefingDraft`, `publishBriefing`, the `briefings` collection). Opening one is POST /receipts and answering
its question is POST /checks, both with a `briefing` target."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Header, Request, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES
from app.schemas.briefings import BriefingForForeman, BriefingForWorker, BriefingPublishIn, BriefingReceipts
from app.schemas.common import IdIn
from app.services import briefings
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["briefings"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


@router.post("/briefings/draft", status_code=201, summary="Record a Briefing (voice or text) → AI draft (never a 500)")
def draft_briefing(
    who: Foreman,
    key: IdempotencyKey,
    db: DbSession,
    request: Request,
    text: Annotated[str | None, Form(max_length=2000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
) -> Envelope[BriefingForForeman]:
    data = audio.file.read(MAX_AUDIO_BYTES + 1) if audio is not None else None
    return Envelope(data=briefings.create_draft(db, who, text, data or None, key, _ai(request), now_utc()))


@router.post("/briefings", summary="Publish the reviewed draft (its edited text and question)")
def publish_briefing(who: Foreman, body: BriefingPublishIn, db: DbSession) -> Envelope[BriefingForForeman]:
    return Envelope(data=briefings.publish(db, who, body, now_utc()))


@router.get("/briefings", summary="Foremen: every Briefing with answers; Workers: published ones with their receipt")
def list_briefings(
    who: ShopMember, db: DbSession, page: Paging
) -> Envelope[Page[BriefingForForeman] | Page[BriefingForWorker]]:
    if who.role == "foreman":
        return Envelope(data=briefings.list_for_foreman(db, who, page))
    return Envelope(data=briefings.list_for_worker(db, who, page))


@router.get("/briefings/{briefing_id}/receipts", summary="Who understood, who only opened it, who has not opened it")
def briefing_receipts(who: Foreman, briefing_id: IdIn, db: DbSession) -> Envelope[BriefingReceipts]:
    return Envelope(data=briefings.receipts(db, who, int(briefing_id)))
