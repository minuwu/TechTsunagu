"""The Wakatta? chatbot (RAG_PLAN.md): a Worker's question answered only from cited sources, with one self-check."""

from typing import Literal

from app.schemas.common import CamelModel, CheckQuestion, Translated


class KnowledgeSource(CamelModel):
    id: str
    kind: Literal["safety", "life", "note", "notice"]
    title: str
    page: int | None
    """Page of the guide's PDF; null for a Note Version or a Notice."""
    url: str | None
    attribution: str | None
    """Shown under the answer when a public guide was used (Public Data License 1.0 asks for it)."""


class KnowledgeAnswer(CamelModel):
    question: str
    covered: bool
    emergency: bool
    """True for an injury, fire or gas question: `answer` is the fixed emergency card, never written by AI."""
    answer: Translated | None
    sources: list[KnowledgeSource]
    check: CheckQuestion | None
    """A self-check for the Worker, graded on the phone: it is practice, not a Check, so it never changes a Sign-off
    or the Wakatta Status, which is why it carries correctIndex."""
