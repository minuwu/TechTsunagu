"""Every Chat query (WS12, XREQ-005). Each takes the caller's shop id; nothing here is unscoped."""

import uuid
from datetime import datetime

from sqlalchemy import delete, func, select
from sqlalchemy.dialects.postgresql import distinct_on
from sqlalchemy.orm import Session, aliased

from app.models.chat import FollowUp, Message, MessageAnswer, MessageCheck, Task, TaskAssignee, Thread, ThreadMember
from app.models.scopes import AuditEvent, User
from app.models.work import Machine

MAX_MESSAGES = 100  # one poll returns at most this many messages after the cursor


def thread(db: Session, shop_id: int, thread_id: int) -> Thread | None:
    return db.scalar(select(Thread).where(Thread.shop_id == shop_id, Thread.id == thread_id))


def membership(db: Session, shop_id: int, thread_id: int, user_id: int) -> ThreadMember | None:
    stmt = select(ThreadMember).where(
        ThreadMember.shop_id == shop_id, ThreadMember.thread_id == thread_id, ThreadMember.user_id == user_id
    )
    return db.scalar(stmt)


def members(db: Session, shop_id: int, thread_id: int) -> list[User]:
    stmt = (
        select(User)
        .join(ThreadMember, (ThreadMember.user_id == User.id) & (ThreadMember.shop_id == User.shop_id))
        .where(ThreadMember.shop_id == shop_id, ThreadMember.thread_id == thread_id)
        .order_by(User.display_name)
    )
    return list(db.scalars(stmt))


def threads_of(db: Session, shop_id: int, user_id: int) -> list[tuple[Thread, int, datetime | None]]:
    """(Thread, unread, newest sent message) for every Thread the user is in, newest activity first."""
    unread = func.count(Message.id).filter(
        Message.from_user_id != user_id,
        (ThreadMember.last_read_at.is_(None)) | (Message.sent_at > ThreadMember.last_read_at),
    )
    newest = func.max(Message.sent_at)
    stmt = (
        select(Thread, unread, newest)
        .join(
            ThreadMember,
            (ThreadMember.thread_id == Thread.id)
            & (ThreadMember.shop_id == Thread.shop_id)
            & (ThreadMember.user_id == user_id),
        )
        .outerjoin(
            Message,
            (Message.shop_id == Thread.shop_id)
            & (Message.thread_id == Thread.id)
            & Message.sent_at.is_not(None)
            & ((Message.kind != "reexplain") | (Message.to_user_id == user_id) | (Message.from_user_id == user_id))
            # A member added later counts (and sees) messages from the join on, as in messages_after.
            & (ThreadMember.history_from.is_(None) | (Message.sent_at >= ThreadMember.history_from)),
        )
        .where(Thread.shop_id == shop_id)
        .group_by(Thread.id, ThreadMember.last_read_at)
        .order_by(newest.desc().nulls_last(), Thread.id)
    )
    return [(t, n, last) for t, n, last in db.execute(stmt)]


def last_messages(db: Session, shop_id: int, viewer_id: int, thread_ids: list[int]) -> dict[int, Message]:
    """Per Thread, the newest sent message the viewer may see (same rules as messages_after)."""
    if not thread_ids:
        return {}
    stmt = (
        select(Message)
        .join(
            ThreadMember,
            (ThreadMember.thread_id == Message.thread_id)
            & (ThreadMember.shop_id == Message.shop_id)
            & (ThreadMember.user_id == viewer_id),
        )
        .where(
            Message.shop_id == shop_id,
            Message.thread_id.in_(thread_ids),
            Message.sent_at.is_not(None),
            (Message.kind != "reexplain") | (Message.to_user_id == viewer_id) | (Message.from_user_id == viewer_id),
            ThreadMember.history_from.is_(None) | (Message.sent_at >= ThreadMember.history_from),
        )
        .order_by(Message.thread_id, Message.sent_at.desc(), Message.id.desc())
        .ext(distinct_on(Message.thread_id))
    )
    return {m.thread_id: m for m in db.scalars(stmt)}


def last_checked(db: Session, shop_id: int, thread_ids: list[int]) -> dict[int, Message]:
    """Per Thread, the newest sent message that carries a Check."""
    if not thread_ids:
        return {}
    stmt = (
        select(Message)
        .join(MessageCheck, (MessageCheck.shop_id == Message.shop_id) & (MessageCheck.message_id == Message.id))
        .where(Message.shop_id == shop_id, Message.thread_id.in_(thread_ids), Message.sent_at.is_not(None))
        .order_by(Message.thread_id, Message.sent_at.desc(), Message.id.desc())
        .ext(distinct_on(Message.thread_id))
    )
    return {m.thread_id: m for m in db.scalars(stmt)}


def my_memberships(db: Session, shop_id: int, user_id: int, thread_ids: list[int]) -> dict[int, ThreadMember]:
    if not thread_ids:
        return {}
    stmt = select(ThreadMember).where(
        ThreadMember.shop_id == shop_id, ThreadMember.user_id == user_id, ThreadMember.thread_id.in_(thread_ids)
    )
    return {m.thread_id: m for m in db.scalars(stmt)}


def roster(db: Session, shop_id: int, thread_id: int) -> list[tuple[ThreadMember, User]]:
    """Members with their accounts, Foremen first, then by name."""
    stmt = (
        select(ThreadMember, User)
        .join(User, (ThreadMember.user_id == User.id) & (ThreadMember.shop_id == User.shop_id))
        .where(ThreadMember.shop_id == shop_id, ThreadMember.thread_id == thread_id)
        .order_by(User.role, User.display_name)
    )
    return [(m, u) for m, u in db.execute(stmt)]


def muted_ids(db: Session, shop_id: int, thread_id: int) -> set[int]:
    stmt = select(ThreadMember.user_id).where(
        ThreadMember.shop_id == shop_id, ThreadMember.thread_id == thread_id, ThreadMember.muted
    )
    return set(db.scalars(stmt))


def thread_by_key(db: Session, shop_id: int, created_by: int, key: uuid.UUID) -> Thread | None:
    stmt = select(Thread).where(
        Thread.shop_id == shop_id, Thread.created_by == created_by, Thread.idempotency_key == key
    )
    return db.scalar(stmt)


def shop_people(db: Session, shop_id: int, user_ids: list[int]) -> list[User]:
    """Active Foremen and Workers of the shop among these ids (Posters are not in work Threads)."""
    stmt = select(User).where(
        User.shop_id == shop_id, User.id.in_(user_ids), User.active, User.role.in_(("foreman", "worker"))
    )
    return list(db.scalars(stmt))


def member_ids(db: Session, shop_id: int, thread_ids: list[int]) -> dict[int, list[int]]:
    out: dict[int, list[int]] = {i: [] for i in thread_ids}
    if thread_ids:
        stmt = select(ThreadMember.thread_id, ThreadMember.user_id).where(
            ThreadMember.shop_id == shop_id, ThreadMember.thread_id.in_(thread_ids)
        )
        for t, u in db.execute(stmt.order_by(ThreadMember.user_id)):
            out[t].append(u)
    return out


def message(db: Session, shop_id: int, message_id: int, *, for_update: bool = False) -> Message | None:
    stmt = select(Message).where(Message.shop_id == shop_id, Message.id == message_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def message_by_key(db: Session, shop_id: int, user_id: int, key: uuid.UUID) -> Message | None:
    stmt = select(Message).where(
        Message.shop_id == shop_id, Message.from_user_id == user_id, Message.idempotency_key == key
    )
    return db.scalar(stmt)


def messages_after(
    db: Session, shop_id: int, thread_id: int, viewer_id: int, after_id: int | None, since: datetime | None = None
) -> list[Message]:
    """Sent messages the viewer may see (a Re-explanation only to its Worker and sender), plus the viewer's drafts;
    `since` = the viewer's history start (a member added later sees messages from the join on)."""
    for_everyone = (Message.kind != "reexplain") | (Message.to_user_id == viewer_id)
    visible = (Message.sent_at.is_not(None) & for_everyone) | (Message.from_user_id == viewer_id)
    stmt = select(Message).where(Message.shop_id == shop_id, Message.thread_id == thread_id, visible)
    if after_id is not None:
        stmt = stmt.where(Message.id > after_id)
    if since is not None:
        # sent_at, not created_at: created_at is the transaction's start and can precede the join it records.
        stmt = stmt.where((Message.sent_at >= since) | (Message.from_user_id == viewer_id))
    return list(db.scalars(stmt.order_by(Message.id).limit(MAX_MESSAGES)))


def checks_of(db: Session, shop_id: int, message_ids: list[int]) -> dict[int, MessageCheck]:
    if not message_ids:
        return {}
    stmt = select(MessageCheck).where(MessageCheck.shop_id == shop_id, MessageCheck.message_id.in_(message_ids))
    return {c.message_id: c for c in db.scalars(stmt)}


def set_check(db: Session, shop_id: int, message_id: int, questions: list[dict[str, object]]) -> None:
    db.execute(delete(MessageCheck).where(MessageCheck.shop_id == shop_id, MessageCheck.message_id == message_id))
    if questions:
        db.add(MessageCheck(shop_id=shop_id, message_id=message_id, questions=questions))


def answer_of(db: Session, shop_id: int, message_id: int, user_id: int) -> MessageAnswer | None:
    stmt = select(MessageAnswer).where(
        MessageAnswer.shop_id == shop_id, MessageAnswer.message_id == message_id, MessageAnswer.user_id == user_id
    )
    return db.scalar(stmt)


def answers_of(db: Session, shop_id: int, message_ids: list[int], user_id: int | None = None) -> list[MessageAnswer]:
    """Every Worker's answers to these messages, or only one Worker's."""
    if not message_ids:
        return []
    stmt = select(MessageAnswer).where(MessageAnswer.shop_id == shop_id, MessageAnswer.message_id.in_(message_ids))
    if user_id is not None:
        stmt = stmt.where(MessageAnswer.user_id == user_id)
    return list(db.scalars(stmt))


def worker_members(db: Session, shop_id: int, thread_id: int) -> list[tuple[int, datetime | None]]:
    """(Worker id, last read) for the Workers of a Thread, by id."""
    stmt = (
        select(ThreadMember.user_id, ThreadMember.last_read_at)
        .join(User, (ThreadMember.user_id == User.id) & (ThreadMember.shop_id == User.shop_id))
        .where(ThreadMember.shop_id == shop_id, ThreadMember.thread_id == thread_id, User.role == "worker")
    )
    return [(u, r) for u, r in db.execute(stmt.order_by(ThreadMember.user_id))]


def nudges_of(db: Session, shop_id: int, message_ids: list[int]) -> set[tuple[int, int]]:
    """(message id, Worker id) pairs the Foreman nudged (audit action `nudge:<user id>`)."""
    if not message_ids:
        return set()
    stmt = select(AuditEvent.subject_id, AuditEvent.action).where(
        AuditEvent.shop_id == shop_id, AuditEvent.action.like("nudge:%"), AuditEvent.subject_id.in_(message_ids)
    )
    return {(s, int(a.split(":", 1)[1])) for s, a in db.execute(stmt) if s is not None}


def asked_messages(db: Session, shop_id: int, user_id: int, message_ids: list[int]) -> set[int]:
    """Messages this Worker already sent an `ask` about."""
    if not message_ids:
        return set()
    stmt = select(Message.ref_message_id).where(
        Message.shop_id == shop_id,
        Message.from_user_id == user_id,
        Message.kind == "ask",
        Message.ref_message_id.in_(message_ids),
    )
    return {r for r in db.scalars(stmt) if r is not None}


def ask_of(db: Session, shop_id: int, user_id: int, message_id: int, text_ja: str) -> Message | None:
    """This Worker's earlier `ask` with the same text about a message."""
    stmt = select(Message).where(
        Message.shop_id == shop_id,
        Message.from_user_id == user_id,
        Message.kind == "ask",
        Message.ref_message_id == message_id,
        Message.text_ja == text_ja,
    )
    return db.scalar(stmt.limit(1))


def nudged_messages(db: Session, shop_id: int, user_id: int, message_ids: list[int]) -> set[int]:
    """Messages the Foreman nudged this Worker about (audit action `nudge:<user id>`)."""
    if not message_ids:
        return set()
    stmt = select(AuditEvent.subject_id).where(
        AuditEvent.shop_id == shop_id, AuditEvent.action == f"nudge:{user_id}", AuditEvent.subject_id.in_(message_ids)
    )
    return {s for s in db.scalars(stmt) if s is not None}


def follow_ups_of(db: Session, shop_id: int, message_id: int, user_id: int) -> list[FollowUp]:
    stmt = select(FollowUp).where(
        FollowUp.shop_id == shop_id, FollowUp.message_id == message_id, FollowUp.user_id == user_id
    )
    return list(db.scalars(stmt.order_by(FollowUp.id)))


def follow_up(db: Session, shop_id: int, follow_up_id: int, *, for_update: bool = False) -> FollowUp | None:
    stmt = select(FollowUp).where(FollowUp.shop_id == shop_id, FollowUp.id == follow_up_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def follow_ups_page(
    db: Session, shop_id: int, viewer_id: int, status: str | None, after_id: int | None, limit: int
) -> list[tuple[FollowUp, str]]:
    """Follow-ups from Threads the viewer is in."""
    stmt = (
        select(FollowUp, User.display_name)
        .join(User, (User.id == FollowUp.user_id) & (User.shop_id == FollowUp.shop_id))
        .join(Message, (Message.id == FollowUp.message_id) & (Message.shop_id == FollowUp.shop_id))
        .join(
            ThreadMember,
            (ThreadMember.thread_id == Message.thread_id)
            & (ThreadMember.shop_id == Message.shop_id)
            & (ThreadMember.user_id == viewer_id),
        )
        .where(FollowUp.shop_id == shop_id)
    )
    if status is not None:
        stmt = stmt.where(FollowUp.status == status)
    if after_id is not None:
        stmt = stmt.where(FollowUp.id < after_id)
    return [(f, n) for f, n in db.execute(stmt.order_by(FollowUp.id.desc()).limit(limit + 1))]


def clarified_between(
    db: Session, shop_id: int, start: datetime, end: datetime
) -> list[tuple[FollowUp, str, str, str | None]]:
    """Support Log item 7: (Follow-up, Worker, who clarified it, the message it was about)."""
    resolver = aliased(User)
    stmt = (
        select(FollowUp, User.display_name, resolver.display_name, Message.text_ja)
        .join(User, (User.id == FollowUp.user_id) & (User.shop_id == FollowUp.shop_id))
        .join(resolver, resolver.id == FollowUp.resolved_by)
        .join(Message, (Message.id == FollowUp.message_id) & (Message.shop_id == FollowUp.shop_id))
        .where(
            FollowUp.shop_id == shop_id,
            FollowUp.status != "open",
            FollowUp.resolved_at >= start,
            FollowUp.resolved_at <= end,
        )
    )
    return [(f, w, r, t) for f, w, r, t in db.execute(stmt)]


def task(db: Session, shop_id: int, task_id: int, *, for_update: bool = False) -> Task | None:
    stmt = select(Task).where(Task.shop_id == shop_id, Task.id == task_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def tasks_page(
    db: Session, shop_id: int, after_id: int | None, limit: int, machine_id: int | None = None
) -> list[Task]:
    stmt = select(Task).where(Task.shop_id == shop_id)
    if machine_id is not None:
        stmt = stmt.where(Task.machine_id == machine_id)
    if after_id is not None:
        stmt = stmt.where(Task.id > after_id)
    return list(db.scalars(stmt.order_by(Task.id).limit(limit + 1)))


def assignees_of(db: Session, shop_id: int, task_ids: list[int]) -> dict[int, list[int]]:
    out: dict[int, list[int]] = {i: [] for i in task_ids}
    if task_ids:
        stmt = select(TaskAssignee.task_id, TaskAssignee.user_id).where(
            TaskAssignee.shop_id == shop_id, TaskAssignee.task_id.in_(task_ids)
        )
        for t, u in db.execute(stmt.order_by(TaskAssignee.user_id)):
            out[t].append(u)
    return out


def set_assignees(db: Session, shop_id: int, task_id: int, user_ids: list[int]) -> None:
    db.execute(delete(TaskAssignee).where(TaskAssignee.shop_id == shop_id, TaskAssignee.task_id == task_id))
    db.add_all(TaskAssignee(shop_id=shop_id, task_id=task_id, user_id=u) for u in dict.fromkeys(user_ids))


def active_users(db: Session, shop_id: int, user_ids: list[int]) -> set[int]:
    if not user_ids:
        return set()
    stmt = select(User.id).where(User.shop_id == shop_id, User.active, User.id.in_(user_ids))
    return set(db.scalars(stmt))


def machine_ids(db: Session, shop_id: int, ids: list[int]) -> set[int]:
    if not ids:
        return set()
    return set(db.scalars(select(Machine.id).where(Machine.shop_id == shop_id, Machine.id.in_(ids))))


def task_by_key(db: Session, shop_id: int, key: uuid.UUID) -> Task | None:
    return db.scalar(select(Task).where(Task.shop_id == shop_id, Task.idempotency_key == key))
