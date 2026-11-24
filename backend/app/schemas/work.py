"""Work contract (architecture §9.3), shaped like frontend/src/domain/wakatta.ts (camelCase, string ids)."""

from datetime import date as Day
from datetime import datetime
from typing import Annotated, Literal

from pydantic import Field, StringConstraints

from app.schemas.common import CamelModel, CheckQuestion, CheckQuestionForWorker, IdIn, Lang, Translated

IconId = str
Shift = Literal["day", "night"]
SkillStatus = Literal["valid", "expiring", "expired", "none"]


class Step(CamelModel):
    text: dict[str, str]
    icon: IconId | None


class MachineIn(CamelModel):
    code: Annotated[str, StringConstraints(pattern=r"^[a-z0-9-]{1,32}$")]
    name: Translated


class MachineOut(CamelModel):
    id: str
    code: str
    name: dict[str, str]
    current_version_id: str | None
    active_worker_id: str | None


class NoteForWorker(CamelModel):
    id: str
    version_no: int
    steps: list[Step]
    questions: list[CheckQuestionForWorker]


class MachineDetail(MachineOut):
    current: NoteForWorker | None


class NoteVersionOut(CamelModel):
    id: str
    machine_id: str
    version_no: int
    status: Literal["draft", "approved"]
    recorded_by: str
    # Display name for the provenance line (XREQ-004); null until approved.
    approved_by: str | None
    transcript_ja: str
    steps: list[Step]
    back_translation: dict[str, str]
    questions: list[CheckQuestion]
    needs_edit: bool
    change_kind: Literal["safety", "wording"] | None
    edit_count: int
    ai_outcome: str
    created_at: datetime
    approved_at: datetime | None


class StepEdit(CamelModel):
    index: Annotated[int, Field(ge=0, le=11)]
    ja: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]


class ApproveIn(CamelModel):
    change_kind: Literal["safety", "wording"] = "safety"


class CheckTarget(CamelModel):
    kind: Literal["note", "briefing", "notice"]
    id: IdIn


class CheckIn(CamelModel):
    target: CheckTarget
    language: Lang
    answers: Annotated[list[int], Field(min_length=1, max_length=3)]


class WrongQuestion(CamelModel):
    id: str
    term_id: str | None
    prompt: Translated


class SignOffOut(CamelModel):
    id: str
    worker_id: str
    machine_id: str
    note_version_id: str
    passed_at: datetime


class CheckResult(CamelModel):
    passed: bool
    wrong: list[WrongQuestion]
    sign_off: SignOffOut | None


class PanelWorker(CamelModel):
    id: str
    display_name: str


class PanelOverrideOut(CamelModel):
    id: str
    by: str
    until: datetime
    reason: str


class OverrideIn(CamelModel):
    """Panel Override (architecture §7.1): a named Foreman lets a Worker (or anyone, workerId null) run the machine
    without a Sign-off for up to 8 hours, with a reason that goes into the Support Log."""

    worker_id: IdIn | None
    minutes: Annotated[int, Field(ge=1, le=480)]
    reason: Annotated[str, StringConstraints(strip_whitespace=True, min_length=3, max_length=200)]


class RemindIn(CamelModel):
    """POST /machines/{id}/remind: the Worker who should take the Machine's Check."""

    worker_id: IdIn


class OverrideOut(CamelModel):
    id: str
    machine_id: str
    worker_id: str | None
    by: str
    reason: str
    starts_at: datetime
    ends_at: datetime
    revoked_at: datetime | None


class PanelOut(CamelModel):
    state: Literal["idle", "locked", "ready", "override"]
    worker: PanelWorker | None
    reason: str | None
    version_no: int | None
    override: PanelOverrideOut | None


class PanelToken(CamelModel):
    token: str
    expires_at: datetime


class ConfusionOut(CamelModel):
    """From a Work Check (checkId and machineId set) or from a Chat message's Check (messageId set)."""

    id: str
    check_id: str | None
    message_id: str | None
    question_id: str
    term_id: str | None
    machine_id: str | None
    worker_id: str
    created_at: datetime
    acknowledged_at: datetime | None


class AssignmentOnBoard(CamelModel):
    shift: Shift
    worker_id: str
    display_name: str
    status: SkillStatus
    flag: bool  # assigned without a valid Sign-off


class BoardMachine(CamelModel):
    id: str
    code: str
    name: dict[str, str]
    version_no: int | None
    open_confusions: int
    assignments: list[AssignmentOnBoard]


class BoardOut(CamelModel):
    date: Day
    machines: list[BoardMachine]
    open_confusions: int


class AssignmentIn(CamelModel):
    machine_id: IdIn
    shift: Shift
    date: Day | None = None
    worker_id: IdIn | None


class AssignmentOut(CamelModel):
    id: str
    worker_id: str
    machine_id: str
    date: Day
    shift: Shift


class GlossaryIn(CamelModel):
    ja: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)]
    reading: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)]
    bn: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=80)]
    en: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=80)]


class GlossaryUpdate(CamelModel):
    """PATCH /glossary/{id}: only the fields sent change; `translations` adds or replaces languages."""

    ja: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)] | None = None
    reading: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)] | None = None
    translations: (
        dict[Lang, Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=80)]] | None
    ) = None


class GlossaryOut(CamelModel):
    id: str
    ja: str
    reading: str
    bn: str
    en: str
    translations: dict[str, str]
    """Every language the term has (one per shop language, XREQ-011 item 4); `bn` / `en` stay for current clients."""


class EffortOut(CamelModel):
    """How much work the AI saves a Foreman (architecture §9.3); every share is 0..1, null when there is no data."""

    drafts: int
    approved: int
    median_seconds_to_approve: float | None
    median_edits_per_draft: float | None
    clean_ai_share: float | None
    first_try_pass_share: float | None
