"""Learning History (Worker) and understanding analytics (Foreman), XREQ-020."""

from datetime import date

from pydantic import AwareDatetime

from app.domain.chat import FollowUpStatus
from app.schemas.common import CamelModel


class LearningItem(CamelModel):
    follow_up_id: str
    term_ja: str
    status: FollowUpStatus
    missed_at: AwareDatetime
    # One resolved_at is stored: a Follow-up that went clarified → understood shows only understoodAt.
    clarified_at: AwareDatetime | None
    understood_at: AwareDatetime | None


class Learning(CamelModel):
    items: list[LearningItem]
    next_cursor: str | None
    streak_days: int


class DayCounts(CamelModel):
    day: date
    green: int
    orange: int
    red: int


class WorkerCounts(CamelModel):
    worker_id: str
    display_name: str
    green: int
    orange: int
    red: int


class TermCount(CamelModel):
    term_ja: str
    count: int


class Understanding(CamelModel):
    days: int
    trend: list[DayCounts]
    per_worker: list[WorkerCounts]
    top_terms: list[TermCount]
