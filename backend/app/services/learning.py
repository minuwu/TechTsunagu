"""A Worker's Learning History and the Foreman's understanding analytics (XREQ-020). Days are Tokyo days, as in
the Chat seed and Task due times."""

from collections import Counter
from datetime import date, datetime, time, timedelta

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import PageParams, encode_cursor
from app.domain.learning import day_window, streak_days
from app.domain.recurrence import TOKYO
from app.models.chat import FollowUp
from app.repositories import accounts
from app.repositories import learning as repo
from app.schemas.learning import DayCounts, Learning, LearningItem, TermCount, Understanding, WorkerCounts
from app.services.auth import Principal
from app.services.notes import shop_of

STREAK_LOOKBACK_DAYS = 366  # shortcut: a streak longer than a year shows as 366; widen if anyone gets there
TOP_TERMS = 10


def _item(f: FollowUp) -> LearningItem:
    return LearningItem(
        follow_up_id=str(f.id),
        term_ja=f.term_ja,
        status=f.status,  # pyright: ignore[reportArgumentType]  # CHECK constraint
        missed_at=f.created_at,
        clarified_at=f.resolved_at if f.status == "clarified" else None,
        understood_at=f.resolved_at if f.status == "understood" else None,
    )


def _start_of(day_offset: int, now: datetime) -> datetime:
    today = now.astimezone(TOKYO).date()
    return datetime.combine(today - timedelta(days=day_offset), time.min, TOKYO)


def learning(db: Session, who: Principal, page: PageParams, now: datetime) -> Learning:
    shop_id, user_id = shop_of(who), who.user_id
    if user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    rows = repo.own_follow_ups(db, shop_id, user_id, page.after_id, page.limit)
    shown = rows[: page.limit]
    good = repo.good_days(db, shop_id, user_id, _start_of(STREAK_LOOKBACK_DAYS, now), TOKYO.key)
    return Learning(
        items=[_item(f) for f in shown],
        next_cursor=encode_cursor(shown[-1].id) if len(rows) > page.limit else None,
        streak_days=streak_days(good, now.astimezone(TOKYO).date()),
    )


def understanding(db: Session, who: Principal, days: int, now: datetime) -> Understanding:
    shop_id = shop_of(who)
    since = _start_of(days - 1, now)
    per_day: Counter[tuple[date, str]] = Counter()
    per_user: Counter[tuple[int, str]] = Counter()
    for day, user_id, status, n in repo.answer_counts(db, shop_id, since, TOKYO.key):
        per_day[day, status] += n
        per_user[user_id, status] += n
    trend = [
        DayCounts(day=d, green=per_day[d, "green"], orange=per_day[d, "orange"], red=per_day[d, "red"])
        for d in day_window(now.astimezone(TOKYO).date(), days)
    ]
    workers = [
        WorkerCounts(
            worker_id=str(w.id),
            display_name=w.display_name,
            green=per_user[w.id, "green"],
            orange=per_user[w.id, "orange"],
            red=per_user[w.id, "red"],
        )
        for w in accounts.roster_of_shop(db, shop_id, "worker")
    ]
    terms = [TermCount(term_ja=t, count=c) for t, c in repo.top_terms(db, shop_id, since, TOP_TERMS)]
    return Understanding(days=days, trend=trend, per_worker=workers, top_terms=terms)
