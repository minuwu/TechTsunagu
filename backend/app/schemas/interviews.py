"""Periodic Interviews (support item 10) and Today's Word (XREQ-010 items 4-5; MOBILE_API_CONTRACT 4.11)."""

from datetime import date
from typing import Annotated, Literal, Self

from pydantic import AwareDatetime, Field, model_validator

from app.domain.interviews import DueStatus
from app.schemas.common import CamelModel, Lang


class InterviewIn(CamelModel):
    held_on: date
    method: Literal["face", "online"]
    online_consent_at: AwareDatetime | None = None
    """When the Worker agreed to an online interview; required for `online`."""
    note_ja: Annotated[str, Field(max_length=1000)] = ""

    @model_validator(mode="after")
    def _online_needs_consent(self) -> Self:
        if self.method == "online" and self.online_consent_at is None:
            raise ValueError("an online interview needs onlineConsentAt (the Worker's consent)")
        return self


class InterviewOut(CamelModel):
    id: str
    worker_id: str
    held_on: date
    method: Literal["face", "online"]
    online_consent_at: AwareDatetime | None
    note_ja: str
    recorded_by: str
    """Display name of the Foreman who recorded it."""
    created_at: AwareDatetime


class InterviewDue(CamelModel):
    worker_id: str
    display_name: str
    last_held_on: date | None
    due_on: date
    status: DueStatus


class TodayWord(CamelModel):
    date: date
    language: Lang
    word: str
    reading: str
    meaning_ja: str
