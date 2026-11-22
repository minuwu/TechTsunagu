"""Small builders for test rows. Each returns the flushed ORM object with its id."""

import uuid
from itertools import count

from sqlalchemy.orm import Session

from app.models.scopes import District, Shop, User
from app.models.work import Machine, NoteVersion

_seq = count(1)


def _code() -> str:
    return f"T{next(_seq):07d}"


def district(db: Session) -> District:
    row = District(code=_code(), name="みなと町", prefecture="長崎県", municipality="佐世保市")
    db.add(row)
    db.flush()
    return row


def shop(db: Session, home: District | None = None) -> Shop:
    home = home or district(db)
    row = Shop(code=_code(), name="Test shop", default_district_id=home.id, languages=["bn", "en"])
    db.add(row)
    db.flush()
    return row


def user(db: Session, at: Shop, role: str = "worker", name: str | None = None, pin_hash: str = "x") -> User:
    row = User(
        role=role,
        display_name=name or f"user-{next(_seq)}",
        language="bn" if role == "worker" else "ja",
        shop_id=at.id,
        district_id=at.default_district_id,
        pin_hash=pin_hash,
    )
    db.add(row)
    db.flush()
    return row


def machine(db: Session, at: Shop, code: str | None = None) -> Machine:
    row = Machine(shop_id=at.id, code=code or f"m-{next(_seq)}", name={"ja": "プレス", "bn": "প্রেস", "en": "Press"})
    db.add(row)
    db.flush()
    return row


def note_draft(db: Session, at: Shop, on: Machine, by: User, version_no: int = 1) -> NoteVersion:
    row = NoteVersion(
        shop_id=at.id,
        machine_id=on.id,
        version_no=version_no,
        recorded_by=by.id,
        source_kind="text",
        transcript_ja="刃を交換する",
        steps=[{"text": {"ja": "電源を切る", "bn": "পাওয়ার বন্ধ করুন", "en": "Turn off the power"}, "icon": "lockout"}],
        back_translation={"bn": "電源を切る", "en": "電源を切る"},
        questions=[],
        ai_outcome="typed",
        idempotency_key=uuid.uuid4(),
    )
    db.add(row)
    db.flush()
    return row
