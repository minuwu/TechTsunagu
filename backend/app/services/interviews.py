"""Periodic Interviews (support item 10) and Today's Word (XREQ-010). Foremen only; scope is the Foreman's shop."""

import json
import uuid
from datetime import datetime
from functools import cache
from pathlib import Path
from typing import Any

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.domain.interviews import due_status, next_due
from app.domain.recurrence import TOKYO
from app.models.life import PeriodicInterview
from app.repositories import accounts
from app.repositories import interviews as repo
from app.repositories import work as work_repo
from app.schemas.interviews import InterviewDue, InterviewIn, InterviewOut, TodayWord
from app.services.auth import Principal
from app.services.notes import shop_of

PHRASEBOOK = Path(__file__).resolve().parent.parent / "seed_data" / "phrasebook.json"
HISTORY = 50  # past interviews shown per Worker


@cache
def _phrasebook() -> dict[str, list[dict[str, str]]]:
    data: dict[str, Any] = json.loads(PHRASEBOOK.read_text(encoding="utf-8"))
    return {k: v for k, v in data.items() if not k.startswith("_")}


def _worker_id(db: Session, shop_id: int, worker_id: int) -> int:
    if not any(u.id == worker_id for u in accounts.roster_of_shop(db, shop_id, "worker")):
        raise ApiError(404, "NOT_FOUND", "Not found")
    return worker_id


def due(db: Session, who: Principal, now: datetime) -> list[InterviewDue]:
    """Every active Worker: the last interview, the next due date (3 months), and ok / soon / overdue."""
    shop_id = shop_of(who)
    today, held = now.astimezone(TOKYO).date(), repo.last_held(db, shop_id)
    out: list[InterviewDue] = []
    for w in accounts.roster_of_shop(db, shop_id, "worker"):
        last = held.get(w.id)
        due_on = next_due(last, w.created_at.astimezone(TOKYO).date())
        out.append(
            InterviewDue.model_validate(
                {
                    "worker_id": str(w.id),
                    "display_name": w.display_name,
                    "last_held_on": last,
                    "due_on": due_on,
                    "status": due_status(due_on, today),
                }
            )
        )
    return sorted(out, key=lambda d: d.due_on)


def _out(i: PeriodicInterview, recorder: str) -> InterviewOut:
    return InterviewOut.model_validate(
        {
            "id": str(i.id),
            "worker_id": str(i.worker_id),
            "held_on": i.held_on,
            "method": i.method,
            "online_consent_at": i.online_consent_at,
            "note_ja": i.note_ja,
            "recorded_by": recorder,
            "created_at": i.created_at,
        }
    )


def history(db: Session, who: Principal, worker_id: int) -> list[InterviewOut]:
    shop_id = shop_of(who)
    wid = _worker_id(db, shop_id, worker_id)
    return [_out(i, name) for i, name in repo.of_worker(db, shop_id, wid, HISTORY)]


def record(
    db: Session, who: Principal, worker_id: int, body: InterviewIn, key: uuid.UUID | None, now: datetime
) -> InterviewOut:
    shop_id = shop_of(who)
    if key is not None and (done := repo.by_key(db, shop_id, key)) is not None:
        if done.worker_id != worker_id or done.recorded_by != who.user_id:
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another record")
        return _out(done, work_repo.display_names(db, shop_id, [done.recorded_by]).get(done.recorded_by, ""))
    wid = _worker_id(db, shop_id, worker_id)
    if body.held_on > now.astimezone(TOKYO).date():
        details = {"fields": [{"loc": ["body", "heldOn"]}]}
        raise ApiError(422, "VALIDATION_ERROR", "An interview is recorded after it was held", details)
    row = PeriodicInterview(
        shop_id=shop_id,
        worker_id=wid,
        held_on=body.held_on,
        method=body.method,
        online_consent_at=body.online_consent_at,
        note_ja=body.note_ja,
        recorded_by=who.user_id,
        idempotency_key=key,
    )
    db.add(row)
    db.flush()
    accounts.audit(db, "interview_recorded", who.user_id, shop_id, subject_id=row.id)
    db.commit()
    return _out(row, work_repo.display_names(db, shop_id, [row.recorded_by]).get(row.recorded_by, ""))


def today_word(db: Session, who: Principal, now: datetime) -> TodayWord:
    """One word of the shop's first Worker language, the same for everyone in the shop on a Tokyo day."""
    book, today = _phrasebook(), now.astimezone(TOKYO).date()
    language = next((lang for lang in repo.shop_languages(db, shop_of(who)) if lang in book), "en")
    entry = book[language][today.toordinal() % len(book[language])]
    return TodayWord.model_validate(
        {
            "date": today,
            "language": language,
            "word": entry["word"],
            "reading": entry["reading"],
            "meaning_ja": entry["meaningJa"],
        }
    )
