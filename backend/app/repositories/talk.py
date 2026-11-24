"""Talk Threads (XREQ-018): every query is filtered by the owner, so another user's thread is simply not found."""

import uuid

from sqlalchemy import delete, func, select
from sqlalchemy.orm import Session

from app.models.personal import TalkEntry, TalkThread


def threads(db: Session, owner_id: int, after_id: int | None, limit: int) -> list[tuple[TalkThread, int]]:
    """Newest first; with the number of entries in each."""
    count = select(func.count()).where(TalkEntry.thread_id == TalkThread.id).correlate(TalkThread).scalar_subquery()
    stmt = select(TalkThread, count).where(TalkThread.owner_id == owner_id)
    if after_id is not None:
        stmt = stmt.where(TalkThread.id < after_id)
    return [(t, n) for t, n in db.execute(stmt.order_by(TalkThread.id.desc()).limit(limit)).all()]


def thread(db: Session, owner_id: int, thread_id: int, *, for_update: bool = False) -> TalkThread | None:
    stmt = select(TalkThread).where(TalkThread.owner_id == owner_id, TalkThread.id == thread_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def thread_by_key(db: Session, owner_id: int, key: uuid.UUID) -> TalkThread | None:
    return db.scalar(select(TalkThread).where(TalkThread.owner_id == owner_id, TalkThread.idempotency_key == key))


def entry_count(db: Session, thread_id: int) -> int:
    return db.scalar(select(func.count()).where(TalkEntry.thread_id == thread_id)) or 0


def delete_thread(db: Session, t: TalkThread) -> None:
    db.execute(delete(TalkEntry).where(TalkEntry.thread_id == t.id))
    db.delete(t)


def entries(db: Session, owner_id: int, thread_id: int, after_id: int | None, limit: int) -> list[TalkEntry]:
    """Oldest first, like a conversation."""
    stmt = select(TalkEntry).where(TalkEntry.owner_id == owner_id, TalkEntry.thread_id == thread_id)
    if after_id is not None:
        stmt = stmt.where(TalkEntry.id > after_id)
    return list(db.scalars(stmt.order_by(TalkEntry.id).limit(limit)))


def entry(db: Session, owner_id: int, thread_id: int, entry_id: int, *, for_update: bool = False) -> TalkEntry | None:
    stmt = select(TalkEntry).where(
        TalkEntry.owner_id == owner_id, TalkEntry.thread_id == thread_id, TalkEntry.id == entry_id
    )
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def entry_by_key(db: Session, owner_id: int, key: uuid.UUID) -> TalkEntry | None:
    return db.scalar(select(TalkEntry).where(TalkEntry.owner_id == owner_id, TalkEntry.idempotency_key == key))
