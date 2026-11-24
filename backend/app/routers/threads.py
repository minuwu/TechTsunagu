"""Group controls on work Threads (XREQ-019). Thin: parse → service → envelope."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, Header

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.schemas.common import IdIn
from app.schemas.threads import MembersIn, MySettingsIn, RoleIn, ThreadCreate, ThreadDetail, ThreadUpdate
from app.services import threads
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["chat"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]


@router.post("/threads", status_code=201, summary="Create a group Thread; you are its admin")
def create_thread(who: Foreman, body: ThreadCreate, key: IdempotencyKey, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.create(db, who, body, key))


@router.get("/threads/{thread_id}", summary="A Thread with its members and your own settings")
def get_thread(who: ShopMember, thread_id: IdIn, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.get(db, who, int(thread_id)))


@router.patch("/threads/{thread_id}", summary="Rename, describe or set announce-only (Foreman admin)")
def update_thread(who: Foreman, thread_id: IdIn, body: ThreadUpdate, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.update(db, who, int(thread_id), body, now_utc()))


@router.post("/threads/{thread_id}/members", summary="Add members; they see messages from now on (Foreman admin)")
def add_members(who: Foreman, thread_id: IdIn, body: MembersIn, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.add_members(db, who, int(thread_id), body, now_utc()))


@router.patch("/threads/{thread_id}/members/{user_id}", summary="Make a member admin or member (Foreman admin)")
def set_role(who: Foreman, thread_id: IdIn, user_id: IdIn, body: RoleIn, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.set_role(db, who, int(thread_id), int(user_id), body, now_utc()))


@router.delete("/threads/{thread_id}/members/{user_id}", summary="Remove a member; their messages stay")
def remove_member(who: Foreman, thread_id: IdIn, user_id: IdIn, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.remove_member(db, who, int(thread_id), int(user_id), now_utc()))


@router.put("/threads/{thread_id}/me", summary="Your own mute, pin and archive for this Thread")
def my_settings(who: ShopMember, thread_id: IdIn, body: MySettingsIn, db: DbSession) -> Envelope[ThreadDetail]:
    return Envelope(data=threads.my_settings(db, who, int(thread_id), body))


@router.post("/threads/{thread_id}/leave", summary="Leave a Thread (the last Foreman admin cannot)")
def leave(who: ShopMember, thread_id: IdIn, db: DbSession) -> Envelope[None]:
    threads.leave(db, who, int(thread_id), now_utc())
    return Envelope(data=None)
