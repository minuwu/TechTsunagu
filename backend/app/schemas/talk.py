"""Talk Threads (XREQ-018, ADR 0014): the owner's notebook about real-life conversations. Personal: scoped by user."""

from typing import Annotated, Literal

from pydantic import AwareDatetime, StringConstraints, model_validator

from app.schemas.common import CamelModel

Title = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)]


class TalkThreadIn(CamelModel):
    title: Title


class TalkThreadOut(CamelModel):
    id: str
    title: str
    updated_at: AwareDatetime
    entry_count: int


class DialectTermOut(CamelModel):
    dialect: str
    plain: str
    meaning: str
    """In the owner's language."""


class ReplyOptionOut(CamelModel):
    """A reply the Worker can show or read aloud. Proposed `replyOptions: ["…"]` became objects: the Worker also needs
    to know what the Japanese reply says (`meaning`, in their language)."""

    ja: str
    meaning: str


class TalkEntryOut(CamelModel):
    id: str
    transcript: str
    plain_ja: str | None
    meaning: str | None
    """In the owner's language; null when the AI was unavailable (then needsEdit is true)."""
    terms: list[DialectTermOut]
    needs_staff: bool
    """A fixed rule, not the model: health or safety words, so tell a staff member or nurse."""
    needs_edit: bool
    own_text: str | None
    grade: Literal["green", "orange", "red"] | None
    reply_options: list[ReplyOptionOut]
    reply_ja: str | None
    confirmed: bool | None
    created_at: AwareDatetime


class TalkEntryUpdate(CamelModel):
    """PATCH an entry with exactly one of: ownText (graded again), replyJa (the reply shown), confirmed (it got
    through or not)."""

    own_text: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)] | None = None
    reply_ja: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=500)] | None = None
    confirmed: bool | None = None

    @model_validator(mode="after")
    def _exactly_one(self) -> "TalkEntryUpdate":
        if sum(v is not None for v in (self.own_text, self.reply_ja, self.confirmed)) != 1:
            raise ValueError("send exactly one of ownText, replyJa, confirmed")
        return self
