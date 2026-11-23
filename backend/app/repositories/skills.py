"""Skill view queries (architecture §6.6): one aggregate query each, scoped by shop; never a query per Machine."""

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.work import Confusion


def confusion_counts(db: Session, shop_id: int) -> dict[tuple[int, int], int]:
    """(term, machine) → Confusions. Chat Confusions have no Machine and are left out, like on the Foreman Board."""
    stmt = (
        select(Confusion.term_id, Confusion.machine_id, func.count())
        .where(Confusion.shop_id == shop_id, Confusion.term_id.is_not(None), Confusion.machine_id.is_not(None))
        .group_by(Confusion.term_id, Confusion.machine_id)
    )
    return {(t, m): n for t, m, n in db.execute(stmt) if t is not None and m is not None}
