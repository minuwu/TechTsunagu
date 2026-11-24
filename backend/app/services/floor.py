"""The shop floor (architecture §6.6, §7.1–7.3, §8.1): machines, scan, Panel, Checks, board, shifts, glossary."""

import uuid
from datetime import date, datetime, timedelta
from zoneinfo import ZoneInfo

from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, build_page
from app.domain.skills import SignOffFact, current_shift, grade, panel_state, skill_status
from app.models.scopes import Session as SessionRow
from app.models.work import Check, Confusion, GlossaryTerm, Machine, SignOff
from app.repositories import accounts
from app.repositories import work as repo
from app.schemas.common import CheckQuestion, CheckQuestionForWorker
from app.schemas.work import (
    AssignmentIn,
    AssignmentOnBoard,
    AssignmentOut,
    BoardMachine,
    BoardOut,
    CheckIn,
    CheckResult,
    ConfusionOut,
    GlossaryIn,
    GlossaryOut,
    GlossaryUpdate,
    MachineDetail,
    MachineIn,
    MachineOut,
    MachineRename,
    NoteForWorker,
    PanelOut,
    PanelOverrideOut,
    PanelToken,
    PanelWorker,
    SignOffOut,
    Step,
    WrongQuestion,
)
from app.services.auth import SESSION_TTL, Principal
from app.services.credentials import new_token
from app.services.notes import shop_of

PRESENCE = timedelta(minutes=10)


def _shop_tz(db: Session, shop_id: int) -> str:
    shop = accounts.shop_by_id(db, shop_id)
    return shop.timezone if shop else "Asia/Tokyo"


def _today(now: datetime, tz: str) -> date:
    return now.astimezone(ZoneInfo(tz)).date()


def _machine(db: Session, who: Principal, machine_id: int) -> Machine:
    if who.role == "panel" and who.machine_id != machine_id:
        raise ApiError(404, "NOT_FOUND", "Machine not found")
    m = repo.machine(db, shop_of(who), machine_id)
    if m is None:
        raise ApiError(404, "NOT_FOUND", "Machine not found")
    return m


def _active(m: Machine) -> Machine:
    if m.archived_at is not None:
        raise ApiError(409, "MACHINE_ARCHIVED", "This machine is archived")
    return m


def _present(m: Machine, now: datetime) -> int | None:
    fresh = m.present_at is not None and now - m.present_at < PRESENCE
    return m.present_worker_id if fresh else None


def machine_out(m: Machine, now: datetime) -> MachineOut:
    present = _present(m, now)
    return MachineOut(
        id=str(m.id),
        code=m.code,
        name=m.name,
        current_version_id=str(m.current_version_id) if m.current_version_id else None,
        active_worker_id=str(present) if present else None,
        archived=m.archived_at is not None,
    )


def list_machines(db: Session, who: Principal, page: PageParams, now: datetime) -> Page[MachineOut]:
    rows = repo.machines_page(db, shop_of(who), page.after_id, page.limit)
    last = rows[page.limit - 1].id if len(rows) > page.limit else None
    return build_page([machine_out(m, now) for m in rows], page.limit, last)


def create_machine(db: Session, who: Principal, body: MachineIn, now: datetime) -> MachineOut:
    m = Machine(shop_id=shop_of(who), code=body.code, name=dict(body.name))
    db.add(m)
    try:
        with db.begin_nested():
            db.flush()
    except IntegrityError as exc:  # unique (shop_id, code)
        raise ApiError(409, "CONFLICT", "A machine with this code already exists") from exc
    db.commit()
    return machine_out(m, now)


def rename_machine(db: Session, who: Principal, machine_id: int, body: MachineRename, now: datetime) -> MachineOut:
    m = _active(_machine(db, who, machine_id))
    m.name = {str(k): v for k, v in body.name.items()}
    db.commit()
    return machine_out(m, now)


def archive_machine(db: Session, who: Principal, machine_id: int, now: datetime) -> None:
    """Soft delete; archiving twice is a no-op. The Panel and scans stop, history keeps the name."""
    m = _machine(db, who, machine_id)
    if m.archived_at is None:
        m.archived_at, m.present_worker_id, m.present_at = now, None, None
        accounts.audit(db, "machine_archived", who.user_id, m.shop_id, subject_id=m.id)
        db.commit()


def machine_detail(db: Session, who: Principal, machine_id: int, now: datetime) -> MachineDetail:
    m = _machine(db, who, machine_id)
    current = None
    if m.current_version_id is not None:
        n = repo.note(db, m.shop_id, m.current_version_id)
        if n is not None:  # Worker-safe: the answer key never leaves the server (§12)
            current = NoteForWorker(
                id=str(n.id),
                version_no=n.version_no,
                steps=[Step.model_validate(s) for s in n.steps],
                questions=[
                    CheckQuestionForWorker.model_validate({k: v for k, v in q.items() if k != "correctIndex"})
                    for q in n.questions
                ],
            )
    return MachineDetail(**machine_out(m, now).model_dump(), current=current)


def scan(db: Session, who: Principal, machine_id: int, now: datetime) -> None:
    m = _active(_machine(db, who, machine_id))
    m.present_worker_id, m.present_at = who.user_id, now
    db.commit()


def panel(db: Session, who: Principal, machine_id: int, now: datetime) -> PanelOut:
    m = _machine(db, who, machine_id)
    tz = _shop_tz(db, m.shop_id)
    worker_id = _present(m, now)
    if worker_id is None:
        a = repo.assignment(db, m.shop_id, m.id, _today(now, tz), current_shift(now, tz))
        worker_id = a.worker_id if a else None
    current_ids = [m.current_version_id] if m.current_version_id is not None else []
    version_no = repo.version_numbers(db, m.shop_id, current_ids).get(m.current_version_id or 0)
    if worker_id is None:
        return PanelOut(state="idle", worker=None, reason=None, version_no=version_no, override=None)

    newest = repo.newest_sign_off(db, m.shop_id, worker_id, m.id)
    fact = SignOffFact(version_no=newest[0], passed_at=newest[1]) if newest else None
    status = skill_status(fact, m.recheck_version_no, now)
    ov = repo.active_override(db, m.shop_id, m.id, worker_id, now)
    state, reason = panel_state(m.current_version_id is not None, status, ov is not None)
    names = repo.display_names(db, m.shop_id, [worker_id, *([ov.created_by] if ov else [])])
    return PanelOut(
        state=state,
        worker=PanelWorker(id=str(worker_id), display_name=names.get(worker_id, "")),
        reason=reason,
        version_no=version_no,
        override=PanelOverrideOut(id=str(ov.id), by=names.get(ov.created_by, ""), until=ov.ends_at, reason=ov.reason)
        if ov
        else None,
    )


def issue_panel_token(db: Session, who: Principal, machine_id: int, now: datetime) -> PanelToken:
    m = _active(_machine(db, who, machine_id))
    token, digest = new_token()
    expires_at = now + SESSION_TTL["panel"]
    db.add(SessionRow(token_hash=digest, kind="panel", machine_id=m.id, shop_id=m.shop_id, expires_at=expires_at))
    accounts.audit(db, "panel_token_issued", who.user_id, m.shop_id, subject_id=m.id)
    db.commit()
    return PanelToken(token=token, expires_at=expires_at)


def _result(questions: list[dict[str, object]], wrong: list[int], sign_off: SignOff | None) -> CheckResult:
    picked = [CheckQuestion.model_validate(questions[i]) for i in wrong]
    return CheckResult(
        passed=not wrong,
        wrong=[WrongQuestion(id=q.id, term_id=q.term_id, prompt=q.prompt) for q in picked],
        sign_off=SignOffOut(
            id=str(sign_off.id),
            worker_id=str(sign_off.worker_id),
            machine_id=str(sign_off.machine_id),
            note_version_id=str(sign_off.note_version_id),
            passed_at=sign_off.passed_at,
        )
        if sign_off
        else None,
    )


def submit_check(db: Session, who: Principal, body: CheckIn, key: uuid.UUID, now: datetime) -> CheckResult:
    shop_id = shop_of(who)
    worker_id = who.user_id
    if worker_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    if body.target.kind != "note":
        raise ApiError(422, "VALIDATION_ERROR", "Briefing and Notice Checks use their own routes")

    done = repo.check_by_key(db, shop_id, worker_id, key)
    if done is not None:
        # A retried submit returns the first result (§8.2), but only for the same target: a key reused for another
        # note must not replay an old grade against a note that was never approved or is no longer current.
        if done.note_version_id is None or done.note_version_id != int(body.target.id):
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another Check")
        first = repo.note(db, shop_id, done.note_version_id)
        if first is None:
            raise ApiError(404, "NOT_FOUND", "Not found")
        stored = [int(q["correctIndex"]) for q in first.questions]  # pyright: ignore[reportArgumentType]
        return _result(first.questions, grade(stored, done.answers), repo.sign_off_of(db, shop_id, done.id))

    n = repo.note(db, shop_id, int(body.target.id))
    if n is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    correct = [int(q["correctIndex"]) for q in n.questions]  # pyright: ignore[reportArgumentType]
    m = repo.machine(db, shop_id, n.machine_id)
    if m is None or n.status != "approved" or m.current_version_id != n.id:
        raise ApiError(409, "VERSION_CHANGED", "This instruction was updated; read the new version")
    try:
        wrong = grade(correct, body.answers)
    except ValueError as exc:
        raise ApiError(422, "VALIDATION_ERROR", str(exc), {"fields": [{"loc": ["body", "answers"]}]}) from exc

    check = Check(
        shop_id=shop_id,
        worker_id=worker_id,
        note_version_id=n.id,
        language=body.language,
        answers=body.answers,
        passed=not wrong,
        idempotency_key=key,
    )
    db.add(check)
    db.flush()
    for i in wrong:
        q = n.questions[i]
        term = q.get("termId")
        db.add(
            Confusion(
                shop_id=shop_id,
                check_id=check.id,
                worker_id=worker_id,
                machine_id=m.id,
                question_id=str(q["id"]),
                term_id=int(str(term)) if term else None,
            )
        )
    sign_off = None
    if not wrong:
        sign_off = SignOff(
            shop_id=shop_id,
            worker_id=worker_id,
            machine_id=m.id,
            note_version_id=n.id,
            check_id=check.id,
            passed_at=now,
        )
        db.add(sign_off)
        db.flush()
    # Build the response before committing: if it fails, the Check rolls back and a retry is safe (no 500 after save).
    result = _result(n.questions, wrong, sign_off)
    db.commit()
    return result


def confusion_out(c: Confusion) -> ConfusionOut:
    return ConfusionOut(
        id=str(c.id),
        check_id=str(c.check_id) if c.check_id else None,
        message_id=str(c.message_id) if c.message_id else None,
        question_id=c.question_id,
        term_id=str(c.term_id) if c.term_id else None,
        machine_id=str(c.machine_id) if c.machine_id else None,
        worker_id=str(c.worker_id),
        created_at=c.created_at,
        acknowledged_at=c.acknowledged_at,
    )


def list_confusions(db: Session, who: Principal, open_only: bool, page: PageParams) -> Page[ConfusionOut]:
    rows = repo.confusions_page(db, shop_of(who), open_only, page.after_id, page.limit)
    last = rows[page.limit - 1].id if len(rows) > page.limit else None
    return build_page([confusion_out(c) for c in rows], page.limit, last)


def ack_confusion(db: Session, who: Principal, confusion_id: int, now: datetime) -> ConfusionOut:
    c = repo.confusion(db, shop_of(who), confusion_id)
    if c is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    if c.acknowledged_at is None:
        c.acknowledged_at, c.acknowledged_by = now, who.user_id
        db.commit()
    return confusion_out(c)


def board(db: Session, who: Principal, now: datetime) -> BoardOut:
    shop_id = shop_of(who)
    day = _today(now, _shop_tz(db, shop_id))
    machines = repo.all_machines(db, shop_id)
    versions = repo.version_numbers(db, shop_id, [m.current_version_id for m in machines if m.current_version_id])
    assignments = repo.assignments_on(db, shop_id, day)
    names = repo.display_names(db, shop_id, [a.worker_id for a in assignments])
    newest = repo.newest_sign_offs(db, shop_id)
    open_counts = repo.open_confusion_counts(db, shop_id)

    rows: list[BoardMachine] = []
    for m in machines:
        on_board: list[AssignmentOnBoard] = []
        for a in (a for a in assignments if a.machine_id == m.id):
            last = newest.get((a.worker_id, m.id))
            status = skill_status(SignOffFact(*last) if last else None, m.recheck_version_no, now)
            on_board.append(
                AssignmentOnBoard(
                    shift=a.shift,  # pyright: ignore[reportArgumentType]  # CHECK constraint
                    worker_id=str(a.worker_id),
                    display_name=names.get(a.worker_id, ""),
                    status=status,
                    flag=status in ("none", "expired"),
                )
            )
        rows.append(
            BoardMachine(
                id=str(m.id),
                code=m.code,
                name=m.name,
                version_no=versions.get(m.current_version_id or 0),
                open_confusions=open_counts.get(m.id, 0),
                assignments=on_board,
            )
        )
    return BoardOut(date=day, machines=rows, open_confusions=sum(open_counts.values()))


def set_assignment(db: Session, who: Principal, body: AssignmentIn, now: datetime) -> AssignmentOut | None:
    shop_id = shop_of(who)
    m = repo.machine(db, shop_id, int(body.machine_id))
    if m is None:
        raise ApiError(404, "NOT_FOUND", "Machine not found")
    _active(m)
    day = body.date or _today(now, _shop_tz(db, shop_id))
    if body.worker_id is None:
        repo.clear_assignment(db, shop_id, m.id, day, body.shift)
        db.commit()
        return None
    if repo.worker(db, shop_id, int(body.worker_id)) is None:
        raise ApiError(404, "NOT_FOUND", "Worker not found")
    a = repo.set_assignment(db, shop_id, m.id, day, body.shift, int(body.worker_id), who.user_id or 0)
    db.commit()
    return AssignmentOut(
        id=str(a.id), worker_id=str(a.worker_id), machine_id=str(a.machine_id), date=a.shift_date, shift=body.shift
    )


def list_assignments(db: Session, who: Principal, day: date | None, now: datetime) -> list[AssignmentOut]:
    shop_id = shop_of(who)
    rows = repo.assignments_on(db, shop_id, day or _today(now, _shop_tz(db, shop_id)))
    if who.role == "worker":
        rows = [a for a in rows if a.worker_id == who.user_id]
    return [
        AssignmentOut(
            id=str(a.id),
            worker_id=str(a.worker_id),
            machine_id=str(a.machine_id),
            date=a.shift_date,
            shift=a.shift,  # pyright: ignore[reportArgumentType]  # CHECK constraint
        )
        for a in rows
    ]


def glossary_out(t: GlossaryTerm) -> GlossaryOut:
    return GlossaryOut(
        id=str(t.id),
        ja=t.ja,
        reading=t.reading,
        bn=t.translations.get("bn", ""),
        en=t.translations.get("en", ""),
        translations=dict(t.translations),
    )


def list_glossary(db: Session, who: Principal) -> list[GlossaryOut]:
    return [glossary_out(t) for t in repo.glossary(db, shop_of(who))]


def update_glossary(db: Session, who: Principal, term_id: int, body: GlossaryUpdate) -> GlossaryOut:
    t = repo.glossary_term(db, shop_of(who), term_id, for_update=True)
    if t is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    if body.ja is not None:
        t.ja = body.ja
    if body.reading is not None:
        t.reading = body.reading
    if body.translations:
        t.translations = {**t.translations, **{str(k): v for k, v in body.translations.items()}}  # new dict: jsonb
    try:
        with db.begin_nested():
            db.flush()
    except IntegrityError as exc:  # unique (shop_id, ja)
        raise ApiError(409, "CONFLICT", "This term is already in the glossary") from exc
    db.commit()
    return glossary_out(t)


def add_glossary(db: Session, who: Principal, body: GlossaryIn) -> GlossaryOut:
    t = GlossaryTerm(
        shop_id=shop_of(who), ja=body.ja, reading=body.reading, translations={"bn": body.bn, "en": body.en}
    )
    db.add(t)
    try:
        with db.begin_nested():
            db.flush()
    except IntegrityError as exc:  # unique (shop_id, ja)
        raise ApiError(409, "CONFLICT", "This term is already in the glossary") from exc
    db.commit()
    return glossary_out(t)
