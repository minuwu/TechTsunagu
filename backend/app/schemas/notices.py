"""Life contract, WS10: Posters, Notices, Events (architecture §9.3 Life flow, §6.3).
Shapes follow frontend/src/domain/wakatta.ts `Notice`, `NoticeEvent`, `Poster`."""

from typing import Annotated, Literal, Self

from fastapi import UploadFile
from pydantic import AwareDatetime, Field, NonNegativeInt, model_validator

from app.schemas.common import CamelModel, CheckQuestion, CheckQuestionForWorker, DateRange, Translated

NoticeCategory = Literal["garbage", "disaster", "event", "duty", "other"]
NoticeSourceKind = Literal["text", "voice", "photo"]
MAX_EVENTS_RANGE_DAYS = 62


class PosterOut(CamelModel):
    id: str
    name: str
    kind: Literal["jichikai", "city", "employer"]


class NoticeDraftForm(CamelModel):
    """POST /notices, multipart, with an `Idempotency-Key` header. Upload limits and magic bytes: architecture §11."""

    source_kind: NoticeSourceKind
    text: Annotated[str | None, Field(max_length=4000)] = None
    audio: UploadFile | None = None
    photo: UploadFile | None = None

    @model_validator(mode="after")
    def _source_matches_kind(self) -> Self:
        given = {
            "text": bool(self.text and self.text.strip()),
            "voice": self.audio is not None,
            "photo": self.photo is not None,
        }
        if not given[self.source_kind] or sum(given.values()) != 1:
            raise ValueError("send exactly the one source that sourceKind names")
        return self


class EventOut(CamelModel):
    id: str
    title: Translated
    starts_at: AwareDatetime
    rrule: str | None
    confirmed: bool
    next_dates: list[AwareDatetime]
    """The next 3 dates the backend expands (COUNT and UNTIL included), shown to the Poster before confirming."""


class EventUpdate(CamelModel):
    """PATCH /notices/{id}/events/{eventId}. A field left out keeps its value; `rrule: null` makes the Event
    one-off (the service tells them apart with `model_fields_set`). Invalid dates are a 422 with the reason
    from app.domain.recurrence."""

    confirmed: bool
    starts_at: AwareDatetime | None = None
    rrule: Annotated[str | None, Field(max_length=200)] = None


class _NoticeOut(CamelModel):
    id: str
    poster_id: str
    category: NoticeCategory
    source_kind: NoticeSourceKind
    source_ja: str
    photo: str | None
    """`/api/blobs/{id}` of the flyer photo, or null."""
    title: Translated
    steps: Annotated[list[Translated], Field(min_length=1, max_length=8)]
    events: list[EventOut]
    status: Literal["draft", "published"]
    needs_edit: bool
    """True when the AI fell back to a draft the Poster must finish (architecture §10.4)."""
    created_at: AwareDatetime
    published_at: AwareDatetime | None


class NoticeForPoster(_NoticeOut):
    question: CheckQuestion


class NoticeForWorker(_NoticeOut):
    """GET /notices for a Worker: published Notices of their district, with their own receipt."""

    question: CheckQuestionForWorker
    opened: bool
    passed: bool | None


class NoticeUpdate(CamelModel):
    """PATCH /notices/{id} on a draft; only the fields sent change."""

    category: NoticeCategory | None = None
    title: Translated | None = None
    steps: Annotated[list[Translated] | None, Field(min_length=1, max_length=8)] = None
    question: CheckQuestion | None = None


class NoticeReach(CamelModel):
    """GET /notices/{id}/reach: counts over all Workers of the district, never names."""

    workers: NonNegativeInt
    opened: NonNegativeInt
    understood: NonNegativeInt
    not_opened: NonNegativeInt


class EventsQuery(DateRange):
    """GET /events?from=&to=, at most 62 days."""

    @model_validator(mode="after")
    def _short_enough(self) -> Self:
        if self.days.days > MAX_EVENTS_RANGE_DAYS:
            raise ValueError(f"at most {MAX_EVENTS_RANGE_DAYS} days")
        return self


class EventOccurrence(CamelModel):
    event_id: str
    notice_id: str
    title: Translated
    at: AwareDatetime
