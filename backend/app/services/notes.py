"""Note Versions (architecture §7.1, §7.4, §8.1–8.3, §10): record → AI draft → edit → approve."""

import hashlib
import json
import uuid
from datetime import datetime
from typing import get_args

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import IconId, NoteDraft
from app.api.errors import ApiError
from app.domain.ai_checks import GlossaryTerm as AiTerm
from app.domain.ai_checks import note_problems
from app.media import audio_mime
from app.models.work import NoteVersion
from app.repositories import accounts
from app.repositories import work as repo
from app.schemas.common import CheckQuestion
from app.schemas.work import NoteVersionOut, Step
from app.services.ai_runner import AiRequest, AiRunner
from app.services.auth import Principal

NOTE_PROMPT_VERSION = 1
AI_LANGUAGES = ("bn", "en")  # shortcut: the AI schema is fixed to ja/bn/en (Galib); per-shop languages after the demo


def shop_of(who: Principal) -> int:
    if who.shop_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id


def note_out(db: Session, n: NoteVersion) -> NoteVersionOut:
    names = repo.display_names(db, n.shop_id, [n.recorded_by])
    return NoteVersionOut(
        id=str(n.id),
        machine_id=str(n.machine_id),
        version_no=n.version_no,
        status=n.status,  # pyright: ignore[reportArgumentType]  # CHECK constraint
        recorded_by=names.get(n.recorded_by, ""),
        transcript_ja=n.transcript_ja,
        steps=[Step.model_validate(s) for s in n.steps],
        back_translation=n.back_translation,
        questions=[CheckQuestion.model_validate(q) for q in n.questions],
        needs_edit=n.needs_edit,
        change_kind=n.change_kind,  # pyright: ignore[reportArgumentType]  # CHECK constraint
        edit_count=n.edit_count,
        ai_outcome=n.ai_outcome,
        created_at=n.created_at,
        approved_at=n.approved_at,
    )


def _glossary_block(terms: list[AiTerm]) -> str:
    return json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in terms], ensure_ascii=False)


def create_draft(
    db: Session,
    who: Principal,
    machine_id: int,
    text: str | None,
    audio: bytes | None,
    key: uuid.UUID,
    ai: AiRunner,
    now: datetime,
) -> NoteVersionOut:
    shop_id = shop_of(who)
    existing = repo.note_by_key(db, shop_id, key)
    if existing is not None:
        return note_out(db, existing)
    if repo.machine(db, shop_id, machine_id) is None:
        raise ApiError(404, "NOT_FOUND", "Machine not found")
    if not text and not audio:
        raise ApiError(422, "VALIDATION_ERROR", "Send a recording or text", {"fields": [{"loc": ["body", "text"]}]})
    mime = audio_mime(audio) if audio else None
    terms = [
        AiTerm(id=str(t.id), ja=t.ja, reading=t.reading, translations=t.translations)
        for t in repo.glossary(db, shop_id)
    ]
    db.commit()  # end the read transaction: nothing is held open during the model call (§8.3)

    glossary = _glossary_block(terms)
    parts = [Part.blob(audio, mime)] if audio and mime else [Part.text(f"<foreman_input>{text}</foreman_input>")]
    request = AiRequest(
        pipeline="build_note",
        prompt=render("build_note", NOTE_PROMPT_VERSION, glossary=glossary, icons=", ".join(get_args(IconId))),
        parts=parts,
        schema=NoteDraft,
        scope=f"shop:{shop_id}",
        cache_input=(audio or (text or "").encode()) + hashlib.sha256(glossary.encode()).digest(),
    )
    result = ai.run(request, lambda d: note_problems(d, terms, AI_LANGUAGES), now)
    term_ids = {t.ja: int(t.id) for t in terms}

    if result.value is not None:
        draft = result.value
        transcript = draft.transcript_ja
        steps = [{"text": s.text.model_dump(), "icon": s.icon} for s in draft.steps]
        back = draft.back_translation.model_dump()
        questions = [
            {
                "id": f"q{i}",
                "termId": str(term_ids[q.term_ja]) if q.term_ja in term_ids else None,
                "prompt": q.prompt.model_dump(),
                "options": [o.model_dump() for o in q.options],
                "correctIndex": q.correct_index,
            }
            for i, q in enumerate(draft.questions, start=1)
        ]
        outcome, needs_edit = ("retried" if result.outcome == "retried" else "ok"), False
    else:
        # Degraded (§10.4): keep what the Foreman gave us; they edit it before anyone sees it.
        transcript = text or ""
        steps = [{"text": {"ja": text, "bn": "", "en": ""}, "icon": None}] if text else []
        back, questions, outcome, needs_edit = {"bn": "", "en": ""}, [], "degraded", True

    machine = repo.machine(db, shop_id, machine_id, for_update=True)  # serialises version numbers (§8.1)
    if machine is None:
        raise ApiError(404, "NOT_FOUND", "Machine not found")
    raced = repo.note_by_key(db, shop_id, key)
    if raced is not None:
        return note_out(db, raced)
    row = NoteVersion(
        shop_id=shop_id,
        machine_id=machine_id,
        version_no=repo.next_version_no(db, shop_id, machine_id),
        recorded_by=who.user_id,
        source_kind="voice" if audio else "text",
        audio_sha256=hashlib.sha256(audio).digest() if audio else None,
        transcript_ja=transcript,
        steps=steps,
        back_translation=back,
        questions=questions,
        needs_edit=needs_edit,
        ai_outcome=outcome,
        idempotency_key=key,
    )
    db.add(row)
    db.commit()
    return note_out(db, row)


def get_note(db: Session, who: Principal, note_id: int) -> NoteVersionOut:
    n = repo.note(db, shop_of(who), note_id)
    if n is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    return note_out(db, n)


def edit_step(db: Session, who: Principal, note_id: int, index: int, ja: str) -> NoteVersionOut:
    n = repo.note(db, shop_of(who), note_id, for_update=True)
    if n is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    if n.status != "draft":
        raise ApiError(409, "CONFLICT", "Only a draft can be edited")
    if index >= len(n.steps):
        raise ApiError(422, "VALIDATION_ERROR", "No such step", {"fields": [{"loc": ["body", "index"]}]})
    steps = [Step.model_validate(s) for s in n.steps]
    steps[index].text["ja"] = ja
    n.steps = [s.model_dump() for s in steps]  # a new list, so SQLAlchemy sees the jsonb change
    n.edit_count += 1
    db.commit()
    return note_out(db, n)


def approve(db: Session, who: Principal, note_id: int, change_kind: str, now: datetime) -> NoteVersionOut:
    shop_id = shop_of(who)
    n = repo.note(db, shop_id, note_id)
    if n is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    machine = repo.machine(db, shop_id, n.machine_id, for_update=True)  # two approvals cannot interleave (§8.1)
    db.refresh(n)
    if machine is None or n.status != "draft":
        raise ApiError(409, "CONFLICT", "Only a draft can be approved")
    steps = [Step.model_validate(s) for s in n.steps]
    if not steps or not 2 <= len(n.questions) <= 3 or any(not s.text.get("ja") for s in steps):
        raise ApiError(409, "NEEDS_EDIT", "Add the steps and 2–3 Check Questions before approving")
    kind = "safety" if machine.current_version_id is None else change_kind  # a Machine's first version is safety
    n.status, n.approved_at, n.approved_by, n.change_kind = "approved", now, who.user_id, kind
    machine.current_version_id = n.id
    if kind == "safety":
        machine.recheck_version_no = n.version_no
    accounts.audit(db, "note_approved", who.user_id, shop_id, subject_id=n.id)
    db.commit()
    return note_out(db, n)
