"""Life contract, WS11: Senpai pairs, the Senpai link view, Meet-ups (architecture §9.3, §3.1, §6.3)."""

from datetime import date
from typing import Annotated, Literal

from pydantic import AwareDatetime, Field, StringConstraints

from app.schemas.common import CamelModel, DateRange, IdIn, Lang
from app.schemas.notices import EventOccurrence

SenpaiKind = Literal["coworker", "jichikai"]
MeetupKind = Literal["coffee", "event", "drill", "other"]


class SenpaiPairCreate(CamelModel):
    worker_id: IdIn
    senpai_name: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)]
    kind: SenpaiKind


class SenpaiPairOut(CamelModel):
    """No token: only its SHA-256 is stored, so the link can be shown once, at creation."""

    id: str
    worker_id: str
    senpai_name: str
    kind: SenpaiKind
    created_at: AwareDatetime
    expires_at: AwareDatetime


class SenpaiPairCreated(SenpaiPairOut):
    token: str
    """128-bit link token for `/senpai/{token}`, returned only in the POST /senpai-pairs response."""


class MeetupCreate(CamelModel):
    """POST /meetups (Worker) and POST /senpai/{token}/meetups. The pair and `loggedBy` come from the caller."""

    kind: MeetupKind
    date: date
    note: Annotated[str, Field(max_length=500)] = ""


class MeetupOut(CamelModel):
    id: str
    pair_id: str
    kind: MeetupKind
    date: date
    note: str
    logged_by: Literal["worker", "senpai"]


class SenpaiReplyIn(CamelModel):
    """POST /senpai/{token}/asks/{id}/reply; the backend translates it for the Worker."""

    reply_ja: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]


class ForwardedAsk(CamelModel):
    id: str
    text: str
    """The Worker's own words."""
    text_ja: str | None
    """Japanese translation for the Senpai; null when translation fell back (architecture §10.4)."""
    created_at: AwareDatetime
    reply_ja: str | None


class SenpaiWorker(CamelModel):
    display_name: str
    language: Lang


class SenpaiView(CamelModel):
    """GET /senpai/{token}: only this pair's Worker and data; a wrong token is 404."""

    senpai_name: str
    worker: SenpaiWorker
    asks: list[ForwardedAsk]
    events: list[EventOccurrence]
    meetups: list[MeetupOut]


class SupportLogQuery(DateRange):
    """GET /support-log.csv?from=&to= (Foreman)."""
