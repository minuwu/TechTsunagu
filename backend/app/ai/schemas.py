"""What each AI pipeline asks Gemini to return (architecture §10.2, §10.5).

Kept simple on purpose (Galib's review, point 5): objects, lists, strings, ints, enums; no unions, no free-form maps,
because Gemini's response_schema supports only a subset of JSON Schema. These models are parsed again on our side
and then checked by app.domain.ai_checks before a service writes anything.
"""

from collections.abc import Sequence
from typing import Annotated, Any, Literal

from pydantic import BaseModel, ConfigDict, Field, StringConstraints, TypeAdapter

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

DEFAULT_LANGUAGES = ("en",)  # the demo shops' languages; the team dropped Bengali (2026-11-23)

NoticeCategory = Literal["garbage", "disaster", "event", "duty", "other"]

Text = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]


class AiModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class AiTri(AiModel):
    """Japanese plus the shop's languages (shops.languages, XREQ-011 item 5). Every other language stays null;
    app.domain.ai_checks.language_problems makes sure each shop language is filled. One fixed schema, so Gemini's
    response schema and the Groq prompt never change per shop."""

    ja: Text
    bn: Text | None = None
    en: Text | None = None
    vi: Text | None = None
    id: Text | None = None
    ne: Text | None = None
    my: Text | None = None
    tl: Text | None = None
    zh: Text | None = None

    def only(self, languages: Sequence[str]) -> dict[str, str]:
        """{ja, <each shop language>} for storage; a language the checks let through empty stays ""."""
        return {"ja": self.ja} | {lang: getattr(self, lang) or "" for lang in languages}


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
    """Each shop language's text translated back into Japanese, so a Foreman can check its meaning."""

    bn: Text | None = None
    en: Text | None = None
    vi: Text | None = None
    id: Text | None = None
    ne: Text | None = None
    my: Text | None = None
    tl: Text | None = None
    zh: Text | None = None


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

    question_text: Text
    """The Worker's question in their own language: for a voice Ask the transcript, which is what a Senpai receives."""
    covered: bool
    answer: AiTri | None
    source_notice_ids: list[str]


class Translation(AiModel):
    text: Text


class KnowledgeDraft(AiModel):
    """answer_knowledge (the chatbot, RAG_PLAN.md): an answer only from the given sources, then one self-check.
    Passed through app.domain.grounding.ground() before it is shown."""

    covered: bool
    answer: AiTri | None
    source_ids: Annotated[list[str], Field(max_length=6)]
    check: AiQuestion | None


class AiTaskDraft(AiModel):
    """A Task the Foreman's message asks for; the service checks every id against the Thread and the shop."""

    title: AiTri
    assignee_ids: Annotated[list[str], Field(max_length=10)]
    due_at: Annotated[str | None, Field(description="ISO 8601 with +09:00 only when a time was said, else null")]
    machine_id: str | None


class MessageDraft(AiModel):
    """draft_message (XREQ-006 item 1). Proposed by Anim so WS12 can run on the fake client; Galib owns the prompt."""

    transcript_ja: Text
    text: AiTri
    questions: Annotated[list[AiQuestion], Field(min_length=1, max_length=3)]
    tasks: Annotated[list[AiTaskDraft], Field(max_length=5)]


class Reexplanation(AiModel):
    """reexplain (XREQ-006 item 4): a simpler sentence about one missed term, and one new question on it."""

    text: AiTri
    question: AiQuestion


_LANGUAGE_MODELS = ("AiTri", "BackTranslation")


def response_schema(schema: type[object], languages: Sequence[str]) -> dict[str, Any]:
    """The JSON Schema sent to the model, with only ja and the shop's languages, all required.

    Gemini refuses the full schema (every optional language in every step and option: "too many states"), and a
    smaller schema is also a clearer instruction. The pydantic model stays the same, so parsing does not change."""
    js: dict[str, Any] = TypeAdapter(schema).json_schema()
    for name in _LANGUAGE_MODELS:
        definition: dict[str, Any] | None = js.get("$defs", {}).get(name)
        if definition is None:
            continue
        keep = [*(["ja"] if name == "AiTri" else []), *languages]
        props: dict[str, Any] = definition["properties"]
        definition["properties"] = {k: props[k].get("anyOf", [props[k]])[0] for k in keep if k in props}
        definition["required"] = [k for k in keep if k in props]
    return js
