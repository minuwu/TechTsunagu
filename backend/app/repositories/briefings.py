"""Briefing queries (WS6, architecture §6.2). Every query takes the caller's shop id; nothing here is unscoped."""

import uuid

from sqlalchemy import select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.scopes import User
from app.models.work import Briefing, Receipt


def by_key(db: Session, shop_id: int, key: uuid.UUID) -> Briefing | None:
    return db.scalar(select(Briefing).where(Briefing.shop_id == shop_id, Briefing.idempotency_key == key))


def briefing(db: Session, shop_id: int, briefing_id: int, *, for_update: bool = False) -> Briefing | None:
    stmt = select(Briefing).where(Briefing.shop_id == shop_id, Briefing.id == briefing_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def page(db: Session, shop_id: int, *, published_only: bool, after_id: int | None, limit: int) -> list[Briefing]:
    """Newest first; one extra row tells the caller whether a next page exists."""
    stmt = select(Briefing).where(Briefing.shop_id == shop_id)
    if published_only:
        stmt = stmt.where(Briefing.status == "published")
    if after_id is not None:
        stmt = stmt.where(Briefing.id < after_id)
    return list(db.scalars(stmt.order_by(Briefing.id.desc()).limit(limit + 1)))


def receipts_of(db: Session, shop_id: int, worker_id: int, briefing_ids: list[int]) -> dict[int, Receipt]:
    if not briefing_ids:
        return {}
    stmt = select(Receipt).where(
        Receipt.shop_id == shop_id, Receipt.worker_id == worker_id, Receipt.briefing_id.in_(briefing_ids)
    )
    return {r.briefing_id: r for r in db.scalars(stmt) if r.briefing_id is not None}


def receipts_for(db: Session, shop_id: int, briefing_id: int) -> list[Receipt]:
    return list(db.scalars(select(Receipt).where(Receipt.shop_id == shop_id, Receipt.briefing_id == briefing_id)))


def open_receipt(db: Session, shop_id: int, worker_id: int, briefing_id: int, passed: bool | None = None) -> None:
    """Upsert: the first open is kept; a Check result (`passed`) overwrites the previous one."""
    stmt = insert(Receipt).values(shop_id=shop_id, worker_id=worker_id, briefing_id=briefing_id, passed=passed)
    keys, partial = ["worker_id", "briefing_id"], Receipt.briefing_id.is_not(None)  # uq_receipts_worker_briefing
    if passed is None:
        db.execute(stmt.on_conflict_do_nothing(index_elements=keys, index_where=partial))
    else:
        db.execute(stmt.on_conflict_do_update(index_elements=keys, index_where=partial, set_={"passed": passed}))


def active_workers(db: Session, shop_id: int) -> list[User]:
    stmt = select(User).where(User.shop_id == shop_id, User.role == "worker", User.active)
    return list(db.scalars(stmt.order_by(User.display_name)))
