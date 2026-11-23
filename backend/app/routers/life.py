"""Life routes, WS10 (architecture §9.3 Life flow): Notices, Events, Notice receipts and photos.
Thin: parse → service → envelope. Paths match frontend/src/services/wakatta.ts."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Header, Query, Request, Response, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES, MAX_PHOTO_BYTES
from app.schemas.common import IdIn
from app.schemas.notices import (
    EventOccurrence,
    EventsQuery,
    EventUpdate,
    NoticeForPoster,
    NoticeForWorker,
    NoticeReach,
    NoticeSourceKind,
    NoticeUpdate,
    PosterOut,
    ReceiptIn,
)
from app.services import briefings, notices
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["life"])
PosterUser = Annotated[Principal, Depends(require("poster"))]
Worker = Annotated[Principal, Depends(require("worker"))]
DistrictMember = Annotated[Principal, Depends(require("poster", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


def _read(upload: UploadFile | None, limit: int) -> bytes | None:
    # One byte over the limit is enough for the size check to refuse it; never read an unbounded upload.
    return (upload.file.read(limit + 1) or None) if upload is not None else None


@router.get("/posters", summary="The signed-in Poster's organisation")
def list_posters(who: PosterUser, db: DbSession) -> Envelope[Page[PosterOut]]:
    return Envelope(data=notices.posters(db, who))


@router.post("/notices", status_code=201, summary="Text, voice or flyer photo → AI draft with Events (never a 500)")
def create_notice(
    who: PosterUser,
    key: IdempotencyKey,
    db: DbSession,
    request: Request,
    source_kind: Annotated[NoticeSourceKind, Form(alias="sourceKind")],
    text: Annotated[str | None, Form(max_length=4000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
    photo: Annotated[UploadFile | None, File()] = None,
) -> Envelope[NoticeForPoster]:
    voice, image = _read(audio, MAX_AUDIO_BYTES), _read(photo, MAX_PHOTO_BYTES)
    draft = notices.create_draft(db, who, source_kind, text, voice, image, key, _ai(request), now_utc())
    return Envelope(data=draft)


@router.get("/notices", summary="Posters: their own Notices; Workers: the district's published Notices")
def list_notices(
    who: DistrictMember, db: DbSession, page: Paging
) -> Envelope[Page[NoticeForPoster] | Page[NoticeForWorker]]:
    if who.role == "poster":
        return Envelope(data=notices.list_for_poster(db, who, page, now_utc()))
    return Envelope(data=notices.list_for_worker(db, who, page, now_utc()))


@router.get("/notices/{notice_id}", summary="One Notice: a Poster's own (drafts too), or a published one for a Worker")
def get_notice(who: DistrictMember, notice_id: IdIn, db: DbSession) -> Envelope[NoticeForPoster | NoticeForWorker]:
    if who.role == "poster":
        return Envelope(data=notices.get_for_poster(db, who, int(notice_id), now_utc()))
    return Envelope(data=notices.get_for_worker(db, who, int(notice_id), now_utc()))


@router.patch("/notices/{notice_id}", summary="Edit a draft Notice")
def update_notice(who: PosterUser, notice_id: IdIn, body: NoticeUpdate, db: DbSession) -> Envelope[NoticeForPoster]:
    return Envelope(data=notices.update(db, who, int(notice_id), body, now_utc()))


@router.patch("/notices/{notice_id}/events/{event_id}", summary="Confirm or correct an Event of a draft")
def update_event(
    who: PosterUser, notice_id: IdIn, event_id: IdIn, body: EventUpdate, db: DbSession
) -> Envelope[NoticeForPoster]:
    return Envelope(data=notices.update_event(db, who, int(notice_id), int(event_id), body, now_utc()))


@router.post("/notices/{notice_id}/publish", summary="Publish; 409 EVENTS_NOT_CONFIRMED until every date is confirmed")
def publish_notice(who: PosterUser, notice_id: IdIn, db: DbSession) -> Envelope[NoticeForPoster]:
    return Envelope(data=notices.publish(db, who, int(notice_id), now_utc()))


@router.get("/notices/{notice_id}/reach", summary="How many district Workers opened and understood it (counts only)")
def notice_reach(who: PosterUser, notice_id: IdIn, db: DbSession) -> Envelope[NoticeReach]:
    return Envelope(data=notices.reach(db, who, int(notice_id)))


@router.get("/events", summary="Dates of confirmed district Events in a range (at most 62 days)")
def list_events(who: Worker, db: DbSession, q: Annotated[EventsQuery, Query()]) -> Envelope[list[EventOccurrence]]:
    return Envelope(data=notices.events_between(db, who, q))


@router.post("/receipts", summary="The Worker opened a Notice or a Briefing (idempotent)")
def open_receipt(who: Worker, body: ReceiptIn, db: DbSession) -> Envelope[None]:
    if body.target.kind == "briefing":
        briefings.open_briefing(db, who, int(body.target.id))
    else:
        notices.open_notice(db, who, int(body.target.id))
    return Envelope(data=None)


@router.get("/blobs/{blob_id}", summary="A flyer photo of your district (JPEG, EXIF removed)", response_class=Response)
def get_blob(who: DistrictMember, blob_id: IdIn, db: DbSession) -> Response:
    data, mime = notices.photo(db, who, int(blob_id))
    return Response(data, media_type=mime, headers={"Cache-Control": "private, max-age=3600"})
