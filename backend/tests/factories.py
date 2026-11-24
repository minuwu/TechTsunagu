"""Small builders for test rows. Each returns the flushed ORM object with its id."""

import uuid
from itertools import count

from sqlalchemy.orm import Session

from app.models.chat import Message, Task, Thread, ThreadMember
from app.models.scopes import District, Shop, User
from app.models.work import Machine, NoteVersion
from app.services.credentials import hash_pin

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


TEST_PEPPER = "test-pepper-0123456789abcdef0123456789"
_hashes: dict[str, str] = {}  # argon2 is deliberately slow; reuse one hash per PIN across tests


def user(
    db: Session, at: Shop, role: str = "worker", name: str | None = None, pin: str = "2468", must_change: bool = True
) -> User:
    if pin not in _hashes:
        _hashes[pin] = hash_pin(TEST_PEPPER, pin)
    row = User(
        role=role,
        display_name=name or f"user-{next(_seq)}",
        language="bn" if role == "worker" else "ja",
        shop_id=at.id,
        district_id=at.default_district_id,
        pin_hash=_hashes[pin],
        pin_must_change=must_change,
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


def thread(db: Session, at: Shop, by: User, members: list[User] | None = None, kind: str = "group") -> Thread:
    row = Thread(shop_id=at.id, title_ja="2号ライン", kind=kind, created_by=by.id)
    db.add(row)
    db.flush()
    for m in [by, *(members or [])]:
        db.add(ThreadMember(thread_id=row.id, user_id=m.id, shop_id=at.id, role="admin" if m is by else "member"))
    db.flush()
    return row


def message(db: Session, at: Shop, on: Thread, by: User, **overrides: object) -> Message:
    row = Message(
        shop_id=at.id,
        thread_id=on.id,
        from_user_id=by.id,
        kind="text",
        text_ja="明日の朝8時から金型交換をします。",
        translations={"bn": "আগামীকাল সকাল ৮টা থেকে ডাই পরিবর্তন।", "en": "Die change from 8:00 tomorrow."},
        stage=3,
        idempotency_key=uuid.uuid4(),
    )
    for key, value in overrides.items():
        setattr(row, key, value)
    db.add(row)
    db.flush()
    return row


def task(db: Session, at: Shop, by: User, **overrides: object) -> Task:
    row = Task(shop_id=at.id, title={"ja": "金型交換", "bn": "ডাই পরিবর্তন", "en": "Die change"}, created_by=by.id)
    for key, value in overrides.items():
        setattr(row, key, value)
    db.add(row)
    db.flush()
    return row
