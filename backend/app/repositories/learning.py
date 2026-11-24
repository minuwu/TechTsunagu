"""Learning History and understanding analytics queries (XREQ-020). Each takes the caller's shop id."""

from datetime import date, datetime

from sqlalchemy import Date, cast, func, select
from sqlalchemy.orm import Session

from app.models.chat import FollowUp, MessageAnswer
from app.models.work import Check


def own_follow_ups(db: Session, shop_id: int, user_id: int, after_id: int | None, limit: int) -> list[FollowUp]:
    stmt = select(FollowUp).where(FollowUp.shop_id == shop_id, FollowUp.user_id == user_id)
    if after_id is not None:
        stmt = stmt.where(FollowUp.id < after_id)
    return list(db.scalars(stmt.order_by(FollowUp.id.desc()).limit(limit + 1)))


def good_days(db: Session, shop_id: int, user_id: int, since: datetime, tz: str) -> set[date]:
    """Local days with a passed Check or an all-right (green) Chat answer."""
    answer_day = cast(func.timezone(tz, MessageAnswer.created_at), Date)
    check_day = cast(func.timezone(tz, Check.created_at), Date)
    answers = select(answer_day).where(
        MessageAnswer.shop_id == shop_id,
        MessageAnswer.user_id == user_id,
        MessageAnswer.status == "green",
        MessageAnswer.created_at >= since,
    )
    checks = select(check_day).where(
        Check.shop_id == shop_id, Check.worker_id == user_id, Check.passed, Check.created_at >= since
    )
    return set(db.scalars(answers.union(checks)))


def answer_counts(db: Session, shop_id: int, since: datetime, tz: str) -> list[tuple[date, int, str, int]]:
    """(local day, user id, Wakatta Status, count) of Chat answers since `since`."""
    day = cast(func.timezone(tz, MessageAnswer.created_at), Date)
    stmt = (
        select(day, MessageAnswer.user_id, MessageAnswer.status, func.count())
        .where(MessageAnswer.shop_id == shop_id, MessageAnswer.created_at >= since)
        .group_by(day, MessageAnswer.user_id, MessageAnswer.status)
    )
    return [(d, u, s, n) for d, u, s, n in db.execute(stmt)]


def top_terms(db: Session, shop_id: int, since: datetime, limit: int) -> list[tuple[str, int]]:
    """Terms behind the most Follow-ups since `since`, most first."""
    n = func.count()
    stmt = (
        select(FollowUp.term_ja, n)
        .where(FollowUp.shop_id == shop_id, FollowUp.created_at >= since)
        .group_by(FollowUp.term_ja)
        .order_by(n.desc(), FollowUp.term_ja)
        .limit(limit)
    )
    return [(t, c) for t, c in db.execute(stmt)]
