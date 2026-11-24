"""Group controls on work Threads (XREQ-019). Changes need a Foreman who is an admin of that group Thread; a Thread
keeps at least one Foreman admin; a locked Thread does not change. Each change leaves a `system` message.
Removing or leaving deletes the member row: messages, answers and Follow-ups stay (they point at users)."""

import uuid
from datetime import datetime

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.models.chat import Message, Thread, ThreadMember
from app.repositories import chat as repo
from app.schemas.chat import SystemEventKind
from app.schemas.threads import MemberOut, MembersIn, MySettingsIn, RoleIn, ThreadCreate, ThreadDetail, ThreadUpdate
from app.services.auth import Principal


def _me(who: Principal) -> tuple[int, int]:
    if who.shop_id is None or who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id, who.user_id


def _member_of(db: Session, who: Principal, thread_id: int) -> tuple[Thread, ThreadMember]:
    shop_id, user_id = _me(who)
    t = repo.thread(db, shop_id, thread_id)
    if t is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    me = repo.membership(db, shop_id, t.id, user_id)
    if me is None:
        raise ApiError(403, "FORBIDDEN", "You are not in this Thread")
    return t, me


def _unlocked(t: Thread) -> None:
    if t.locked:
        raise ApiError(409, "THREAD_LOCKED", "This Thread is fixed and cannot be changed")


def _admin_of(db: Session, who: Principal, thread_id: int) -> Thread:
    t, me = _member_of(db, who, thread_id)
    if who.role != "foreman" or me.role != "admin":
        raise ApiError(403, "FORBIDDEN", "Only a Foreman admin of this Thread can change it")
    if t.kind != "group":
        raise ApiError(409, "CONFLICT", "A direct Thread has no group controls")
    _unlocked(t)
    return t


def _keep_a_foreman_admin(db: Session, t: Thread, user_id: int) -> None:
    """The Thread's only Foreman admin cannot step down, be removed or leave."""
    admins = [u.id for m, u in repo.roster(db, t.shop_id, t.id) if m.role == "admin" and u.role == "foreman"]
    if admins == [user_id]:
        raise ApiError(409, "CONFLICT", "A Thread keeps at least one Foreman admin; make another Foreman admin first")


def _event(db: Session, t: Thread, actor: int, event: SystemEventKind, subject: int | None, now: datetime) -> None:
    db.add(
        Message(
            shop_id=t.shop_id,
            thread_id=t.id,
            from_user_id=actor,
            kind="system",
            event={"event": event, "actorId": str(actor), "subjectId": str(subject) if subject else None},
            stage=4,
            sent_at=now,
        )
    )


def _people(db: Session, shop_id: int, ids: list[str]) -> list[int]:
    wanted = {int(i) for i in ids}
    found = {u.id for u in repo.shop_people(db, shop_id, list(wanted))}
    if found != wanted:
        raise ApiError(422, "VALIDATION_ERROR", "Every member must be an active Foreman or Worker of your shop")
    return sorted(found)


def _detail(db: Session, t: Thread, user_id: int) -> ThreadDetail:
    roster = repo.roster(db, t.shop_id, t.id)
    me = next(m for m, u in roster if u.id == user_id)
    members = [
        {"user_id": str(u.id), "display_name": u.display_name, "role": m.role, "joined_at": m.joined_at}
        for m, u in roster
    ]
    return ThreadDetail.model_validate(
        {
            "id": str(t.id),
            "title_ja": t.title_ja,
            "description": t.description,
            "kind": t.kind,
            "announce_only": t.announce_only,
            "locked": t.locked,
            "members": [MemberOut.model_validate(m) for m in members],
            "muted": me.muted,
            "pinned": me.pinned,
            "archived": me.archived,
        }
    )


def create(db: Session, who: Principal, body: ThreadCreate, key: uuid.UUID) -> ThreadDetail:
    shop_id, user_id = _me(who)
    existing = repo.thread_by_key(db, shop_id, user_id, key)
    if existing is not None:
        return _detail(db, existing, user_id)
    people = [p for p in _people(db, shop_id, body.member_ids) if p != user_id]
    t = Thread(
        shop_id=shop_id,
        title_ja=body.title_ja,
        description=body.description,
        kind="group",
        announce_only=body.announce_only,
        created_by=user_id,
        idempotency_key=key,
    )
    db.add(t)
    db.flush()
    db.add(ThreadMember(thread_id=t.id, user_id=user_id, shop_id=shop_id, role="admin"))
    db.add_all(ThreadMember(thread_id=t.id, user_id=p, shop_id=shop_id) for p in people)
    db.commit()
    return _detail(db, t, user_id)


def get(db: Session, who: Principal, thread_id: int) -> ThreadDetail:
    t, me = _member_of(db, who, thread_id)
    return _detail(db, t, me.user_id)


def update(db: Session, who: Principal, thread_id: int, body: ThreadUpdate, now: datetime) -> ThreadDetail:
    t = _admin_of(db, who, thread_id)
    actor = _me(who)[1]
    if body.title_ja is not None and body.title_ja != t.title_ja:
        t.title_ja = body.title_ja
        _event(db, t, actor, "renamed", None, now)
    changed = False
    if body.description is not None and body.description != t.description:
        t.description, changed = body.description, True
    if body.announce_only is not None and body.announce_only != t.announce_only:
        t.announce_only, changed = body.announce_only, True
    if changed:
        _event(db, t, actor, "settings_changed", None, now)
    db.commit()
    return _detail(db, t, actor)


def add_members(db: Session, who: Principal, thread_id: int, body: MembersIn, now: datetime) -> ThreadDetail:
    t = _admin_of(db, who, thread_id)
    actor = _me(who)[1]
    present = {u.id for _, u in repo.roster(db, t.shop_id, t.id)}
    for p in _people(db, t.shop_id, body.user_ids):
        if p not in present:
            db.add(ThreadMember(thread_id=t.id, user_id=p, shop_id=t.shop_id, history_from=now))
            _event(db, t, actor, "member_added", p, now)
    db.commit()
    return _detail(db, t, actor)


def _target(db: Session, t: Thread, user_id: int) -> ThreadMember:
    m = repo.membership(db, t.shop_id, t.id, user_id)
    if m is None:
        raise ApiError(404, "NOT_FOUND", "That person is not in this Thread")
    return m


def set_role(db: Session, who: Principal, thread_id: int, user_id: int, body: RoleIn, now: datetime) -> ThreadDetail:
    t = _admin_of(db, who, thread_id)
    actor = _me(who)[1]
    m = _target(db, t, user_id)
    if m.role != body.role:
        if body.role == "member":
            _keep_a_foreman_admin(db, t, user_id)
        m.role = body.role
        _event(db, t, actor, "role_changed", user_id, now)
    db.commit()
    return _detail(db, t, actor)


def remove_member(db: Session, who: Principal, thread_id: int, user_id: int, now: datetime) -> ThreadDetail:
    t = _admin_of(db, who, thread_id)
    actor = _me(who)[1]
    m = _target(db, t, user_id)
    _keep_a_foreman_admin(db, t, user_id)
    db.delete(m)
    _event(db, t, actor, "member_removed", user_id, now)
    db.commit()
    return _detail(db, t, actor)


def my_settings(db: Session, who: Principal, thread_id: int, body: MySettingsIn) -> ThreadDetail:
    t, me = _member_of(db, who, thread_id)
    me.muted, me.pinned, me.archived = body.muted, body.pinned, body.archived
    db.commit()
    return _detail(db, t, me.user_id)


def leave(db: Session, who: Principal, thread_id: int, now: datetime) -> None:
    t, me = _member_of(db, who, thread_id)
    _unlocked(t)
    _keep_a_foreman_admin(db, t, me.user_id)
    db.delete(me)
    _event(db, t, me.user_id, "member_left", me.user_id, now)
    db.commit()
