"""Periodic Interview queries (support item 10). Each takes the caller's shop id; nothing here is unscoped."""

import uuid
from datetime import date

from sqlalchemy import func, select
from sqlalchemy.orm import Session, aliased

from app.models.life import PeriodicInterview
from app.models.scopes import Shop, User


def last_held(db: Session, shop_id: int) -> dict[int, date]:
    stmt = (
        select(PeriodicInterview.worker_id, func.max(PeriodicInterview.held_on))
        .where(PeriodicInterview.shop_id == shop_id)
        .group_by(PeriodicInterview.worker_id)
    )
    return {w: d for w, d in db.execute(stmt)}


def of_worker(db: Session, shop_id: int, worker_id: int, limit: int) -> list[tuple[PeriodicInterview, str]]:
    stmt = (
        select(PeriodicInterview, User.display_name)
        .join(User, (User.id == PeriodicInterview.recorded_by) & (User.shop_id == PeriodicInterview.shop_id))
        .where(PeriodicInterview.shop_id == shop_id, PeriodicInterview.worker_id == worker_id)
        .order_by(PeriodicInterview.held_on.desc(), PeriodicInterview.id.desc())
        .limit(limit)
    )
    return [(i, n) for i, n in db.execute(stmt)]


def by_key(db: Session, shop_id: int, key: uuid.UUID) -> PeriodicInterview | None:
    stmt = select(PeriodicInterview).where(
        PeriodicInterview.shop_id == shop_id, PeriodicInterview.idempotency_key == key
    )
    return db.scalar(stmt)


def between(db: Session, shop_id: int, start: date, end: date) -> list[tuple[PeriodicInterview, str, str]]:
    """Support Log item 10: (interview, Worker, who recorded it)."""
    worker, foreman = aliased(User), aliased(User)
    stmt = (
        select(PeriodicInterview, worker.display_name, foreman.display_name)
        .join(worker, (worker.id == PeriodicInterview.worker_id) & (worker.shop_id == PeriodicInterview.shop_id))
        .join(foreman, (foreman.id == PeriodicInterview.recorded_by) & (foreman.shop_id == PeriodicInterview.shop_id))
        .where(
            PeriodicInterview.shop_id == shop_id,
            PeriodicInterview.held_on >= start,
            PeriodicInterview.held_on <= end,
        )
    )
    return [(i, w, f) for i, w, f in db.execute(stmt)]


def shop_languages(db: Session, shop_id: int) -> list[str]:
    return list(db.scalar(select(Shop.languages).where(Shop.id == shop_id)) or [])
