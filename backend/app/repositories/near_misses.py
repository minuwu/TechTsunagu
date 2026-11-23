"""Near-miss queries (WS7). Each takes the caller's shop id; nothing here is unscoped."""

import uuid

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.platform import Blob
from app.models.scopes import User
from app.models.work import NearMissReport


def report(db: Session, shop_id: int, report_id: int, *, for_update: bool = False) -> NearMissReport | None:
    stmt = select(NearMissReport).where(NearMissReport.shop_id == shop_id, NearMissReport.id == report_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def report_by_key(db: Session, shop_id: int, key: uuid.UUID) -> NearMissReport | None:
    stmt = select(NearMissReport).where(NearMissReport.shop_id == shop_id, NearMissReport.idempotency_key == key)
    return db.scalar(stmt)


def reports_page(db: Session, shop_id: int, open_only: bool, after_id: int | None, limit: int) -> list[NearMissReport]:
    """Newest first; `open_only` uses the partial index on unacknowledged reports."""
    stmt = select(NearMissReport).where(NearMissReport.shop_id == shop_id)
    if open_only:
        stmt = stmt.where(NearMissReport.acknowledged_at.is_(None))
    if after_id is not None:
        stmt = stmt.where(NearMissReport.id < after_id)
    return list(db.scalars(stmt.order_by(NearMissReport.id.desc()).limit(limit + 1)))


def blob(db: Session, shop_id: int, blob_id: int) -> Blob | None:
    return db.scalar(select(Blob).where(Blob.shop_id == shop_id, Blob.id == blob_id))


def language_of(db: Session, shop_id: int, user_id: int) -> str | None:
    return db.scalar(select(User.language).where(User.shop_id == shop_id, User.id == user_id))
