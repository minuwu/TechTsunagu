"""Briefing contract, WS6 (朝礼). Shapes follow frontend/src/domain/wakatta.ts `Briefing` ({id, text, question,
createdAt}); the extra fields are for the Foreman's review and the Worker's own receipt."""

from typing import Literal

from pydantic import AwareDatetime

from app.schemas.common import CamelModel, CheckQuestion, CheckQuestionForWorker, IdIn, Translated


class _BriefingOut(CamelModel):
    id: str
    text: Translated
    status: Literal["draft", "published"]
    created_at: AwareDatetime
    published_at: AwareDatetime | None


class BriefingForForeman(_BriefingOut):
    transcript_ja: str
    question: CheckQuestion
    needs_edit: bool
    """True when the AI fell back: the Foreman fills every 「（ここを入力してください）」 field before publishing."""


class BriefingForWorker(_BriefingOut):
    question: CheckQuestionForWorker
    opened: bool
    passed: bool | None


class BriefingPublishIn(CamelModel):
    """POST /briefings: the reviewed draft, edited text and question included (the frontend sends its Briefing)."""

    id: IdIn
    text: Translated
    question: CheckQuestion
    created_at: AwareDatetime | None = None  # sent back by the frontend; the server keeps its own


class ReceiptWorker(CamelModel):
    id: str
    name: str


class BriefingReceipts(CamelModel):
    """GET /briefings/{id}/receipts: every active Worker of the shop in exactly one group."""

    understood: list[ReceiptWorker]
    opened_only: list[ReceiptWorker]
    """Opened it, but has not passed its question (not answered yet, or answered wrongly)."""
    not_opened: list[ReceiptWorker]
