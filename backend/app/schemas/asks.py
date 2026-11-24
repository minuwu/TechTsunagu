"""Life contract, WS11: Asks (architecture §9.3, §7.6). Shape follows frontend/src/domain/wakatta.ts `Ask`."""

from typing import Annotated, Self

from fastapi import UploadFile
from pydantic import AwareDatetime, Field, model_validator

from app.schemas.common import CamelModel, Translated


class AskForm(CamelModel):
    """POST /asks, multipart, with an `Idempotency-Key` header. The Worker comes from the session."""

    text: Annotated[str | None, Field(max_length=1000)] = None
    audio: UploadFile | None = None

    @model_validator(mode="after")
    def _one_source(self) -> Self:
        if bool(self.text and self.text.strip()) == (self.audio is not None):
            raise ValueError("send text or audio, not both")
        return self


class AskOut(CamelModel):
    id: str
    worker_id: str
    worker_name: str
    """The Worker's display name."""
    text: str
    """What the Worker asked: typed text, or the transcript of their voice question."""
    text_ja: str | None
    """The Japanese of a forwarded Ask; null when it was not forwarded or the translation fell back."""
    covered: bool
    answer: Translated | None
    """Null when not covered; the frontend shows its own "no posted notice covers this" text."""
    source_notice_ids: list[str]
    forwarded: bool
    senpai_reply: Translated | None
    replied_at: AwareDatetime | None
    created_at: AwareDatetime
