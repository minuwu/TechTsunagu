"""What each AI pipeline asks Gemini to return (architecture §10.2, §10.5).

Kept simple on purpose (Galib's review, point 5): objects, lists, strings, ints, enums; no unions, no free-form maps,
because Gemini's response_schema supports only a subset of JSON Schema. These models are parsed again on our side
and then checked by app.domain.ai_checks before a service writes anything.
"""

from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field, StringConstraints

# The frontend's IconId (frontend/src/domain/wakatta.ts); keep the two lists equal.
IconId = Literal[
    "ppe-goggles",
    "ppe-gloves",
    "hot-surface",
    "lockout",
    "measure",
    "crane-sling",
    "emergency-stop",
    "hands-clear",
    "ventilation",
    "check-list",
    "garbage",
    "calendar",
]

NoticeCategory = Literal["garbage", "disaster", "event", "duty", "other"]

Text = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]


class AiModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class AiTri(AiModel):
    """Japanese plus the demo shop's languages.
    shortcut: fixed to ja/bn/en, build the schema from shops.languages when a shop adds vi, id, ne, my, tl or zh."""

    ja: Text
    bn: Text
    en: Text


class AiStep(AiModel):
    text: AiTri
    icon: IconId | None


class AiQuestion(AiModel):
    # The model names the Glossary Term by its Japanese form; the service maps it to the term's id.
    term_ja: str | None
    prompt: AiTri
    options: Annotated[list[AiTri], Field(min_length=3, max_length=3)]
    correct_index: Annotated[int, Field(ge=0, le=2)]


class BackTranslation(AiModel):
    """The Bengali and English texts translated back into Japanese, so a Foreman can check their meaning."""

    bn: Text
    en: Text


class NoteDraft(AiModel):
    transcript_ja: Text
    steps: Annotated[list[AiStep], Field(min_length=1, max_length=12)]
    back_translation: BackTranslation
    questions: Annotated[list[AiQuestion], Field(min_length=2, max_length=3)]


class BriefingDraft(AiModel):
    transcript_ja: Text
    text: AiTri
    question: AiQuestion


class AiEvent(AiModel):
    title: AiTri
    starts_at: Annotated[str, Field(description="ISO 8601 with +09:00, e.g. 2026-12-06T09:00:00+09:00")]
    rrule: Annotated[str | None, Field(description="FREQ=WEEKLY;BYDAY=… only; null for a one-off date")]


class NoticeDraft(AiModel):
    source_ja: Text
    category: NoticeCategory
    title: AiTri
    steps: Annotated[list[AiTri], Field(min_length=1, max_length=8)]
    events: Annotated[list[AiEvent], Field(max_length=8)]
    question: AiQuestion


class AskDraft(AiModel):
    """Passed through app.domain.grounding.ground() before a service stores it."""

    covered: bool
    answer: AiTri | None
    source_notice_ids: list[str]


class Translation(AiModel):
    text: Text
