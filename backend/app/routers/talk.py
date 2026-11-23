"""Talk Threads (XREQ-018, ADR 0014). Thin: parse → service → envelope. Foremen and Workers (the Android roles);
every route acts only on the caller's own threads (another user's thread is a 404)."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Header, Request, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES
from app.schemas.common import IdIn
from app.schemas.talk import TalkEntryOut, TalkEntryUpdate, TalkThreadIn, TalkThreadOut
from app.services import talk
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["personal"])
Person = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
OptionalKey = Annotated[uuid.UUID | None, Header(alias="Idempotency-Key")]


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


@router.get("/talk-threads", summary="Your Talk Threads, newest first")
def list_threads(who: Person, db: DbSession, page: Paging) -> Envelope[Page[TalkThreadOut]]:
    return Envelope(data=talk.list_threads(db, who, page))


@router.post("/talk-threads", status_code=201, summary="Start a Talk Thread (title 1 to 40 characters)")
def create_thread(who: Person, body: TalkThreadIn, db: DbSession, key: OptionalKey = None) -> Envelope[TalkThreadOut]:
    return Envelope(data=talk.create_thread(db, who, body, key))


@router.patch("/talk-threads/{thread_id}", summary="Rename a Talk Thread")
def rename_thread(who: Person, thread_id: IdIn, body: TalkThreadIn, db: DbSession) -> Envelope[TalkThreadOut]:
    return Envelope(data=talk.rename_thread(db, who, int(thread_id), body, now_utc()))


@router.delete("/talk-threads/{thread_id}", summary="Delete a Talk Thread and every entry (hard delete)")
def delete_thread(who: Person, thread_id: IdIn, db: DbSession) -> Envelope[None]:
    talk.delete_thread(db, who, int(thread_id))
    return Envelope(data=None)


@router.get("/talk-threads/{thread_id}/entries", summary="A Talk Thread's entries, oldest first")
def list_entries(who: Person, thread_id: IdIn, db: DbSession, page: Paging) -> Envelope[Page[TalkEntryOut]]:
    return Envelope(data=talk.list_entries(db, who, int(thread_id), page))


@router.post(
    "/talk-threads/{thread_id}/entries",
    status_code=201,
    summary="What someone said (text or recording) → plain Japanese, the meaning, dialect words, replies (AI)",
)
def add_entry(
    who: Person,
    thread_id: IdIn,
    db: DbSession,
    request: Request,
    text: Annotated[str | None, Form(max_length=2000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
    key: OptionalKey = None,
) -> Envelope[TalkEntryOut]:
    data = audio.file.read(MAX_AUDIO_BYTES + 1) if audio is not None else None
    return Envelope(data=talk.add_entry(db, who, int(thread_id), text, data, key, _ai(request), now_utc()))


@router.patch(
    "/talk-threads/{thread_id}/entries/{entry_id}",
    summary="Exactly one of: ownText (graded by AI), replyJa (the reply shown), confirmed (it got through)",
)
def update_entry(
    who: Person, thread_id: IdIn, entry_id: IdIn, body: TalkEntryUpdate, db: DbSession, request: Request
) -> Envelope[TalkEntryOut]:
    return Envelope(data=talk.update_entry(db, who, int(thread_id), int(entry_id), body, _ai(request), now_utc()))


@router.delete("/talk-threads/{thread_id}/entries/{entry_id}", summary="Delete one entry")
def delete_entry(who: Person, thread_id: IdIn, entry_id: IdIn, db: DbSession) -> Envelope[None]:
    talk.delete_entry(db, who, int(thread_id), int(entry_id))
    return Envelope(data=None)
