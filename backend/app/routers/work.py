"""Work routes (architecture §9.3). Thin: parse → service → envelope. Paths match frontend/src/services/wakatta.ts."""

import uuid
from datetime import date
from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Header, Request, Response, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.conditional import conditional_json
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES
from app.schemas.common import IdIn
from app.schemas.work import (
    ApproveIn,
    AssignmentIn,
    AssignmentOut,
    BoardOut,
    CheckIn,
    CheckResult,
    ConfusionOut,
    EffortOut,
    GlossaryIn,
    GlossaryOut,
    MachineDetail,
    MachineIn,
    MachineOut,
    NoteVersionOut,
    OverrideIn,
    OverrideOut,
    PanelOut,
    PanelToken,
    SignOffOut,
    StepEdit,
)
from app.services import briefings, floor, notes, notices, records
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["work"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
Worker = Annotated[Principal, Depends(require("worker"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
PanelViewer = Annotated[Principal, Depends(require("panel", "foreman"))]
Paging = Annotated[PageParams, Depends(page_params)]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


@router.get("/machines", summary="Machines of your shop")
def list_machines(who: ShopMember, db: DbSession, page: Paging) -> Envelope[Page[MachineOut]]:
    return Envelope(data=floor.list_machines(db, who, page, now_utc()))


@router.post("/machines", status_code=201, summary="Add a Machine")
def create_machine(who: Foreman, body: MachineIn, db: DbSession) -> Envelope[MachineOut]:
    return Envelope(data=floor.create_machine(db, who, body, now_utc()))


@router.get("/machines/{machine_id}", summary="A Machine and its current Note Version (no answers)")
def machine_detail(who: ShopMember, machine_id: IdIn, db: DbSession) -> Envelope[MachineDetail]:
    return Envelope(data=floor.machine_detail(db, who, int(machine_id), now_utc()))


@router.post("/machines/{machine_id}/scan", summary="The signed-in Worker is at this Machine (Presence)")
def scan(who: Worker, machine_id: IdIn, db: DbSession) -> Envelope[None]:
    floor.scan(db, who, int(machine_id), now_utc())
    return Envelope(data=None)


@router.get(
    "/machines/{machine_id}/panel",
    summary="Machine Panel state; send If-None-Match to get 304 when unchanged",
    response_model=Envelope[PanelOut],
)
def panel(who: PanelViewer, machine_id: IdIn, db: DbSession, request: Request) -> Response:
    return conditional_json(request, Envelope(data=floor.panel(db, who, int(machine_id), now_utc())))


@router.post("/machines/{machine_id}/panel-token", summary="Use this device as the Machine's Panel")
def panel_token(who: Foreman, machine_id: IdIn, db: DbSession) -> Envelope[PanelToken]:
    return Envelope(data=floor.issue_panel_token(db, who, int(machine_id), now_utc()))


@router.post("/machines/{machine_id}/notes", status_code=201, summary="Record a note → AI draft (never a 500)")
def record_note(
    who: Foreman,
    machine_id: IdIn,
    key: IdempotencyKey,
    db: DbSession,
    request: Request,
    text: Annotated[str | None, Form(max_length=2000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
) -> Envelope[NoteVersionOut]:
    data = audio.file.read(MAX_AUDIO_BYTES + 1) if audio is not None else None
    draft = notes.create_draft(db, who, int(machine_id), text, data or None, key, _ai(request), now_utc())
    return Envelope(data=draft)


@router.get("/note-versions/{note_id}", summary="A Note Version with its answer key (Foreman)")
def get_note(who: Foreman, note_id: IdIn, db: DbSession) -> Envelope[NoteVersionOut]:
    return Envelope(data=notes.get_note(db, who, int(note_id)))


@router.patch("/note-versions/{note_id}", summary="Edit one step of a draft")
def edit_note(who: Foreman, note_id: IdIn, body: StepEdit, db: DbSession) -> Envelope[NoteVersionOut]:
    return Envelope(data=notes.edit_step(db, who, int(note_id), body.index, body.ja))


@router.post("/note-versions/{note_id}/approve", summary="Approve: safety change (re-check) or wording change")
def approve_note(who: Foreman, note_id: IdIn, body: ApproveIn, db: DbSession) -> Envelope[NoteVersionOut]:
    return Envelope(data=notes.approve(db, who, int(note_id), body.change_kind, now_utc()))


@router.post("/checks", summary="Answer a Note Version's Check; graded on the server")
def submit_check(who: Worker, body: CheckIn, key: IdempotencyKey, db: DbSession) -> Envelope[CheckResult]:
    if body.target.kind == "notice":
        return Envelope(data=notices.submit_check(db, who, body, key))
    if body.target.kind == "briefing":
        return Envelope(data=briefings.submit_check(db, who, body, key))
    return Envelope(data=floor.submit_check(db, who, body, key, now_utc()))


@router.post("/machines/{machine_id}/overrides", status_code=201, summary="Panel Override: allow without a Sign-off")
def start_override(who: Foreman, machine_id: IdIn, body: OverrideIn, db: DbSession) -> Envelope[OverrideOut]:
    return Envelope(data=records.start_override(db, who, int(machine_id), body, now_utc()))


@router.delete("/machines/{machine_id}/overrides/{override_id}", summary="End a Panel Override early")
def end_override(who: Foreman, machine_id: IdIn, override_id: IdIn, db: DbSession) -> Envelope[OverrideOut]:
    return Envelope(data=records.end_override(db, who, int(machine_id), int(override_id), now_utc()))


@router.get("/machines/{machine_id}/versions", summary="Knowledge Library: a Machine's Note Versions, newest first")
def list_versions(who: Foreman, machine_id: IdIn, db: DbSession, page: Paging) -> Envelope[Page[NoteVersionOut]]:
    return Envelope(data=records.list_versions(db, who, int(machine_id), page))


@router.get("/metrics/effort", summary="Effort metrics: time to approve, edits, clean AI drafts, first-try Checks")
def effort(who: Foreman, db: DbSession) -> Envelope[EffortOut]:
    return Envelope(data=records.effort(db, who))


@router.get("/sign-offs", summary="Sign-offs, newest first (Workers see their own)")
def list_sign_offs(who: ShopMember, db: DbSession, page: Paging) -> Envelope[Page[SignOffOut]]:
    return Envelope(data=records.list_sign_offs(db, who, page))


@router.get("/confusions", summary="Confusions, newest first")
def list_confusions(who: Foreman, db: DbSession, page: Paging, open: bool = False) -> Envelope[Page[ConfusionOut]]:
    return Envelope(data=floor.list_confusions(db, who, open, page))


@router.post("/confusions/{confusion_id}/ack", summary="Acknowledge a Confusion")
def ack_confusion(who: Foreman, confusion_id: IdIn, db: DbSession) -> Envelope[ConfusionOut]:
    return Envelope(data=floor.ack_confusion(db, who, int(confusion_id), now_utc()))


@router.get(
    "/board",
    summary="Foreman board: every Machine, today's assignments and their Sign-off status; If-None-Match gives 304",
    response_model=Envelope[BoardOut],
)
def board(who: Foreman, db: DbSession, request: Request) -> Response:
    return conditional_json(request, Envelope(data=floor.board(db, who, now_utc())))


@router.get("/shift-assignments", summary="Shift Assignments of a day (Workers see their own)")
def list_assignments(who: ShopMember, db: DbSession, day: date | None = None) -> Envelope[list[AssignmentOut]]:
    return Envelope(data=floor.list_assignments(db, who, day, now_utc()))


@router.put("/shift-assignments", summary="Assign a Worker to a Machine and shift, or clear it with workerId null")
def set_assignment(who: Foreman, body: AssignmentIn, db: DbSession) -> Envelope[AssignmentOut | None]:
    return Envelope(data=floor.set_assignment(db, who, body, now_utc()))


@router.get("/glossary", summary="Glossary Terms of your shop")
def list_glossary(who: ShopMember, db: DbSession) -> Envelope[list[GlossaryOut]]:
    return Envelope(data=floor.list_glossary(db, who))


@router.post("/glossary", status_code=201, summary="Add a Glossary Term")
def add_glossary(who: Foreman, body: GlossaryIn, db: DbSession) -> Envelope[GlossaryOut]:
    return Envelope(data=floor.add_glossary(db, who, body))
