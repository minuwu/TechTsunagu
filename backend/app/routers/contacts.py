"""My QR, Contacts, blocks and Contact Threads (XREQ-017 items 1-2, ADR 0014). Thin: parse → service → envelope.
Foremen and Workers (the Android roles); every route acts only on the caller's own personal data."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, Header, Query, Request, Response

from app.api.auth import DbSession, now_utc, require
from app.api.conditional import conditional_json
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.schemas.common import IdIn
from app.schemas.contacts import (
    BlockIn,
    ContactCreate,
    ContactMessageIn,
    ContactMessageOut,
    ContactMessagesPage,
    ContactOut,
    ContactPreview,
    ContactUpdate,
    QrIn,
    QrOut,
    QrToken,
)
from app.services import contacts
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["personal"])
Person = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
OptionalKey = Annotated[uuid.UUID | None, Header(alias="Idempotency-Key")]


@router.post("/me/qr", summary="My QR: make it, or rotate it (the token is shown only then)")
def my_qr(who: Person, body: QrIn, db: DbSession) -> Envelope[QrOut]:
    return Envelope(data=contacts.my_qr(db, who, body.rotate, now_utc()))


@router.get("/contacts/resolve", summary="Who a scanned QR belongs to (name and language only; rate-limited)")
def resolve(who: Person, token: Annotated[QrToken, Query()], db: DbSession) -> Envelope[ContactPreview]:
    return Envelope(data=contacts.resolve(db, who, token, now_utc()))


@router.post("/contacts", status_code=201, summary="Add the Contact a scanned QR belongs to (both sides connect)")
def add_contact(who: Person, body: ContactCreate, db: DbSession) -> Envelope[ContactOut]:
    return Envelope(data=contacts.add(db, who, body, now_utc()))


@router.get("/contacts", summary="Your Contacts with unread counts")
def list_contacts(who: Person, db: DbSession, page: Paging) -> Envelope[Page[ContactOut]]:
    return Envelope(data=contacts.list_contacts(db, who, page))


@router.put("/contacts/{contact_id}", summary="Mute or hide a Contact")
def update_contact(who: Person, contact_id: IdIn, body: ContactUpdate, db: DbSession) -> Envelope[ContactOut]:
    return Envelope(data=contacts.update(db, who, int(contact_id), body))


@router.post("/blocks", summary="Block a Contact (they are not told)")
def block(who: Person, body: BlockIn, db: DbSession) -> Envelope[None]:
    contacts.block(db, who, int(body.contact_id))
    return Envelope(data=None)


@router.delete("/blocks/{contact_id}", summary="Unblock a Contact")
def unblock(who: Person, contact_id: IdIn, db: DbSession) -> Envelope[None]:
    contacts.unblock(db, who, int(contact_id))
    return Envelope(data=None)


@router.get(
    "/contacts/{contact_id}/messages",
    summary="Contact Thread: messages after a cursor, in your language; If-None-Match → 304",
    response_model=Envelope[ContactMessagesPage],
)
def read_messages(
    who: Person,
    contact_id: IdIn,
    db: DbSession,
    request: Request,
    after: Annotated[IdIn | None, Query()] = None,
) -> Response:
    page = contacts.read(db, who, int(contact_id), int(after) if after else None, now_utc())
    return conditional_json(request, Envelope(data=page))


@router.post("/contacts/{contact_id}/messages", status_code=201, summary="Send a Contact Thread message")
def send_message(
    who: Person, contact_id: IdIn, body: ContactMessageIn, db: DbSession, request: Request, key: OptionalKey = None
) -> Envelope[ContactMessageOut]:
    ai: AiRunner = request.app.state.ai
    return Envelope(data=contacts.send(db, who, int(contact_id), body, key, ai, now_utc()))


@router.delete(
    "/contacts/{contact_id}/messages/{message_id}",
    summary="Delete a message: one you sent goes for both of you, one you received only on your side",
)
def delete_message(who: Person, contact_id: IdIn, message_id: IdIn, db: DbSession) -> Envelope[None]:
    contacts.delete_message(db, who, int(contact_id), int(message_id))
    return Envelope(data=None)


@router.delete(
    "/contacts/{contact_id}/messages",
    summary="Delete the whole Contact Thread: all you sent, and your copy of what you received",
)
def delete_thread(who: Person, contact_id: IdIn, db: DbSession) -> Envelope[None]:
    contacts.delete_thread(db, who, int(contact_id))
    return Envelope(data=None)
