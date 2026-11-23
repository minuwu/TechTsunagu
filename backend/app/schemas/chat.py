"""Chat contract, WS12 (XREQ-005, GLOSSARY "Chat"): Threads, Messages, Wakatta Status, Follow-ups, Tasks.
Worker responses use CheckQuestionForWorker (no correctIndex), as everywhere else."""

from typing import Annotated, Literal

from pydantic import AwareDatetime, Field, StringConstraints

from app.domain.chat import FollowUpStatus, WakattaStatus
from app.schemas.common import CamelModel, CheckQuestion, CheckQuestionForWorker, IdIn, Translated

MessageKind = Literal["voice", "text", "ask", "reexplain"]
TaskColumn = Literal["todo", "doing", "done"]
TermJa = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=100)]


class ThreadOut(CamelModel):
    id: str
    title_ja: str
    kind: Literal["group", "direct"]
    member_ids: list[str]
    unread: int
    last_message_at: AwareDatetime | None


class TaskDraft(CamelModel):
    """A Task the AI found in a message, or one the Foreman keeps when sending."""

    title: Translated
    assignee_ids: Annotated[list[IdIn], Field(max_length=10)] = []
    due_at: AwareDatetime | None = None
    machine_id: IdIn | None = None


class _MessageOut(CamelModel):
    id: str
    thread_id: str
    from_user_id: str
    kind: MessageKind
    language: str
    stage: int
    """0–5: Transcribed, Translated, Check Questions made, Tasks extracted, Sent to Workers, Answers in."""
    needs_edit: bool
    transcript_ja: str | None
    text: dict[str, str]
    """ja plus the translations; empty for a degraded draft."""
    to_user_id: str | None
    ref_message_id: str | None
    created_at: AwareDatetime
    sent_at: AwareDatetime | None


class MessageForSender(_MessageOut):
    """The Foreman's view: the answer key, and the Task drafts on the response to POST (they are not stored)."""

    questions: list[CheckQuestion]
    task_drafts: list[TaskDraft] = []


class MessageForWorker(_MessageOut):
    questions: list[CheckQuestionForWorker]
    my_status: WakattaStatus | None
    nudged: bool
    """The Foreman is waiting for this Worker's answer."""


class MessagesPage(CamelModel):
    items: list[MessageForSender | MessageForWorker]


class SendIn(CamelModel):
    """POST /messages/{id}/send: the edited message; the questions and Tasks the Foreman kept."""

    text: Translated
    questions: Annotated[list[CheckQuestion], Field(max_length=3)] = []
    tasks: Annotated[list[TaskDraft], Field(max_length=10)] = []


class AnswersIn(CamelModel):
    answers: Annotated[list[Annotated[int, Field(ge=-1, le=2)]], Field(min_length=1, max_length=3)]
    """One per question: the option index, or -1 for "not sure"."""


class AnswerResult(CamelModel):
    status: WakattaStatus
    follow_up_ids: list[str]


class FollowUpOut(CamelModel):
    id: str
    message_id: str
    user_id: str
    display_name: str
    term_ja: str
    status: FollowUpStatus
    note: str
    created_at: AwareDatetime
    resolved_at: AwareDatetime | None


class FollowUpUpdate(CamelModel):
    status: Literal["clarified", "understood"]
    note: Annotated[str, Field(max_length=500)] = ""


class ReexplainIn(CamelModel):
    user_id: IdIn
    term_ja: TermJa


class NudgeIn(CamelModel):
    user_id: IdIn


class TaskCreate(CamelModel):
    title: Translated
    assignee_ids: Annotated[list[IdIn], Field(max_length=10)] = []
    due_at: AwareDatetime | None = None
    machine_id: IdIn | None = None


class TaskOut(CamelModel):
    id: str
    title: Translated
    column: TaskColumn
    assignee_ids: list[str]
    due_at: AwareDatetime | None
    machine_id: str | None
    from_message_id: str | None
    version: int
    updated_at: AwareDatetime


class TaskUpdate(CamelModel):
    """PATCH /tasks/{id} with `If-Match: <version>`; only the fields sent change (`dueAt: null` clears it)."""

    column: TaskColumn | None = None
    assignee_ids: Annotated[list[IdIn] | None, Field(max_length=10)] = None
    due_at: AwareDatetime | None = None
