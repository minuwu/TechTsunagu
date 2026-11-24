"""Chat contract, WS12 (XREQ-005, GLOSSARY "Chat"): Threads, Messages, Wakatta Status, Follow-ups, Tasks.
Worker responses use CheckQuestionForWorker (no correctIndex), as everywhere else."""

from typing import Annotated, Literal

from pydantic import AwareDatetime, Field, StringConstraints

from app.domain.chat import FollowUpStatus, WakattaStatus
from app.schemas.common import CamelModel, CheckQuestion, CheckQuestionForWorker, IdIn, Translated

MessageKind = Literal["voice", "text", "ask", "reexplain", "system"]
SystemEventKind = Literal[
    "renamed", "member_added", "member_removed", "member_left", "role_changed", "settings_changed"
]


class SystemEvent(CamelModel):
    """What a `system` message records (XREQ-019); the app writes the sentence in the reader's language."""

    event: SystemEventKind
    actor_id: str
    subject_id: str | None


TaskColumn = Literal["todo", "doing", "done"]
TermJa = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=100)]


class ThreadOut(CamelModel):
    id: str
    title_ja: str
    kind: Literal["group", "direct"]
    member_ids: list[str]
    unread: int
    last_message_at: AwareDatetime | None
    announce_only: bool = False
    # The caller's own settings for this Thread (PUT /threads/{id}/me).
    muted: bool = False
    pinned: bool = False
    archived: bool = False


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
    event: SystemEvent | None = None
    """Set on `system` messages only."""


class WorkerResult(CamelModel):
    """One Worker's state on a sent message, for the Foreman's Wakatta card."""

    user_id: str
    status: WakattaStatus | None
    """null while the Worker has not answered."""
    answers: list[int] | None
    read: bool
    nudged: bool


class MessageForSender(_MessageOut):
    """The Foreman's view: the answer key, and the Task drafts of an unsent message (empty once it is sent)."""

    questions: list[CheckQuestion]
    task_drafts: list[TaskDraft] = []
    results: list[WorkerResult] = []
    """Every Worker of the Thread (a Re-explanation: only its Worker); empty for a message without questions."""


class MessageForWorker(_MessageOut):
    questions: list[CheckQuestionForWorker]
    my_status: WakattaStatus | None
    nudged: bool
    """The Foreman is waiting for this Worker's answer."""
    my_answers: list[int] | None = None
    answer_key: list[int] | None = None
    """The correct option of each question, only once this Worker has answered."""
    asked: bool = False
    """This Worker already asked the Foreman to explain this message again."""


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
    answers: list[int] = []
    answer_key: list[int] = []
    """The correct option of each question, revealed once the Worker has answered."""


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


class DraftEdit(CamelModel):
    """PATCH /messages/{id} (XREQ-022): the Foreman's corrected Japanese for an unsent draft. The AI translates it and
    makes the Check Questions again, as for a new draft."""

    text_ja: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=2000)]


class ReexplainIn(CamelModel):
    """POST /messages/{id}/reexplain. With `text` and `question` (the Foreman's edited draft from
    /reexplain/draft) it is sent as written and no model is called; without them the AI writes it (XREQ-022)."""

    user_id: IdIn
    term_ja: TermJa
    text: Translated | None = None
    question: CheckQuestion | None = None


class ReexplainDraft(CamelModel):
    """POST /messages/{id}/reexplain/draft (XREQ-022): the simpler version to read and edit before sending.
    `text.ja` is the easy Japanese; the other languages are the explanation. Nothing is stored."""

    text: Translated
    question: CheckQuestion


class AskForemanIn(CamelModel):
    """POST /messages/{id}/ask (XREQ-022): a Worker asks the Foreman to explain one term of a sent message again."""

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
