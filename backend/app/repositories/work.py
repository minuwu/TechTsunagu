"""Every Work query (architecture §6.2, §6.6). Each takes the caller's shop id; nothing here is unscoped."""

import uuid
from datetime import date, datetime

from sqlalchemy import delete, func, select
from sqlalchemy.dialects.postgresql import distinct_on, insert
from sqlalchemy.orm import Session

from app.models.scopes import User
from app.models.work import (
    Check,
    Confusion,
    GlossaryTerm,
    Machine,
    NoteVersion,
    PanelOverride,
    ShiftAssignment,
    SignOff,
)


def machine(db: Session, shop_id: int, machine_id: int, *, for_update: bool = False) -> Machine | None:
    stmt = select(Machine).where(Machine.shop_id == shop_id, Machine.id == machine_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def machines_page(db: Session, shop_id: int, after_id: int | None, limit: int) -> list[Machine]:
    stmt = select(Machine).where(Machine.shop_id == shop_id)
    if after_id is not None:
        stmt = stmt.where(Machine.id > after_id)
    return list(db.scalars(stmt.order_by(Machine.id).limit(limit + 1)))


def all_machines(db: Session, shop_id: int) -> list[Machine]:
    return list(db.scalars(select(Machine).where(Machine.shop_id == shop_id).order_by(Machine.code)))


def note(db: Session, shop_id: int, note_id: int, *, for_update: bool = False) -> NoteVersion | None:
    stmt = select(NoteVersion).where(NoteVersion.shop_id == shop_id, NoteVersion.id == note_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def note_by_key(db: Session, shop_id: int, key: uuid.UUID) -> NoteVersion | None:
    return db.scalar(select(NoteVersion).where(NoteVersion.shop_id == shop_id, NoteVersion.idempotency_key == key))


def next_version_no(db: Session, shop_id: int, machine_id: int) -> int:
    stmt = select(func.max(NoteVersion.version_no)).where(
        NoteVersion.shop_id == shop_id, NoteVersion.machine_id == machine_id
    )
    return (db.scalar(stmt) or 0) + 1


def version_numbers(db: Session, shop_id: int, ids: list[int]) -> dict[int, int]:
    if not ids:
        return {}
    stmt = select(NoteVersion.id, NoteVersion.version_no).where(NoteVersion.shop_id == shop_id, NoteVersion.id.in_(ids))
    return {row.id: row.version_no for row in db.execute(stmt)}


def glossary(db: Session, shop_id: int) -> list[GlossaryTerm]:
    return list(db.scalars(select(GlossaryTerm).where(GlossaryTerm.shop_id == shop_id).order_by(GlossaryTerm.ja)))


def display_names(db: Session, shop_id: int, ids: list[int]) -> dict[int, str]:
    if not ids:
        return {}
    stmt = select(User.id, User.display_name).where(User.shop_id == shop_id, User.id.in_(ids))
    return {row.id: row.display_name for row in db.execute(stmt)}


def worker(db: Session, shop_id: int, worker_id: int) -> User | None:
    return db.scalar(select(User).where(User.shop_id == shop_id, User.id == worker_id, User.role == "worker"))


def check_by_key(db: Session, shop_id: int, worker_id: int, key: uuid.UUID) -> Check | None:
    stmt = select(Check).where(Check.shop_id == shop_id, Check.worker_id == worker_id, Check.idempotency_key == key)
    return db.scalar(stmt)


def sign_off_of(db: Session, shop_id: int, check_id: int) -> SignOff | None:
    return db.scalar(select(SignOff).where(SignOff.shop_id == shop_id, SignOff.check_id == check_id))


def newest_sign_off(db: Session, shop_id: int, worker_id: int, machine_id: int) -> tuple[int, datetime] | None:
    """(version_no, passed_at) of the newest Sign-off for this Worker on this Machine."""
    stmt = (
        select(NoteVersion.version_no, SignOff.passed_at)
        .join(NoteVersion, (NoteVersion.shop_id == SignOff.shop_id) & (NoteVersion.id == SignOff.note_version_id))
        .where(SignOff.shop_id == shop_id, SignOff.worker_id == worker_id, SignOff.machine_id == machine_id)
        .order_by(SignOff.passed_at.desc())
        .limit(1)
    )
    row = db.execute(stmt).first()
    return (row.version_no, row.passed_at) if row else None


def newest_sign_offs(db: Session, shop_id: int) -> dict[tuple[int, int], tuple[int, datetime]]:
    """One query for the whole shop (board, Skill Matrix): (worker, machine) → (version_no, passed_at)."""
    stmt = (
        select(SignOff.worker_id, SignOff.machine_id, NoteVersion.version_no, SignOff.passed_at)
        .join(NoteVersion, (NoteVersion.shop_id == SignOff.shop_id) & (NoteVersion.id == SignOff.note_version_id))
        .where(SignOff.shop_id == shop_id)
        .order_by(SignOff.worker_id, SignOff.machine_id, SignOff.passed_at.desc())
        .ext(distinct_on(SignOff.worker_id, SignOff.machine_id))
    )
    return {(r.worker_id, r.machine_id): (r.version_no, r.passed_at) for r in db.execute(stmt)}


def active_override(
    db: Session, shop_id: int, machine_id: int, worker_id: int | None, now: datetime
) -> PanelOverride | None:
    stmt = select(PanelOverride).where(
        PanelOverride.shop_id == shop_id,
        PanelOverride.machine_id == machine_id,
        PanelOverride.revoked_at.is_(None),
        PanelOverride.starts_at <= now,
        PanelOverride.ends_at > now,
    )
    who = PanelOverride.worker_id.is_(None)
    if worker_id is not None:
        who = who | (PanelOverride.worker_id == worker_id)
    return db.scalar(stmt.where(who).order_by(PanelOverride.ends_at.desc()).limit(1))


def assignment(db: Session, shop_id: int, machine_id: int, day: date, shift: str) -> ShiftAssignment | None:
    stmt = select(ShiftAssignment).where(
        ShiftAssignment.shop_id == shop_id,
        ShiftAssignment.machine_id == machine_id,
        ShiftAssignment.shift_date == day,
        ShiftAssignment.shift == shift,
    )
    return db.scalar(stmt)


def assignments_on(db: Session, shop_id: int, day: date) -> list[ShiftAssignment]:
    stmt = select(ShiftAssignment).where(ShiftAssignment.shop_id == shop_id, ShiftAssignment.shift_date == day)
    return list(db.scalars(stmt.order_by(ShiftAssignment.machine_id, ShiftAssignment.shift)))


def set_assignment(
    db: Session, shop_id: int, machine_id: int, day: date, shift: str, worker_id: int, by: int
) -> ShiftAssignment:
    stmt = (
        insert(ShiftAssignment)
        .values(
            shop_id=shop_id, machine_id=machine_id, shift_date=day, shift=shift, worker_id=worker_id, assigned_by=by
        )
        .on_conflict_do_update(
            index_elements=["shop_id", "machine_id", "shift_date", "shift"],
            set_={"worker_id": worker_id, "assigned_by": by},
        )
        .returning(ShiftAssignment)
    )
    return db.scalars(stmt).one()


def clear_assignment(db: Session, shop_id: int, machine_id: int, day: date, shift: str) -> None:
    db.execute(
        delete(ShiftAssignment).where(
            ShiftAssignment.shop_id == shop_id,
            ShiftAssignment.machine_id == machine_id,
            ShiftAssignment.shift_date == day,
            ShiftAssignment.shift == shift,
        )
    )


def open_confusion_counts(db: Session, shop_id: int) -> dict[int, int]:
    stmt = (
        select(Confusion.machine_id, func.count())
        .where(Confusion.shop_id == shop_id, Confusion.acknowledged_at.is_(None))
        .group_by(Confusion.machine_id)
    )
    return {machine_id: count for machine_id, count in db.execute(stmt)}


def confusions_page(db: Session, shop_id: int, open_only: bool, before_id: int | None, limit: int) -> list[Confusion]:
    stmt = select(Confusion).where(Confusion.shop_id == shop_id)
    if open_only:
        stmt = stmt.where(Confusion.acknowledged_at.is_(None))
    if before_id is not None:
        stmt = stmt.where(Confusion.id < before_id)
    return list(db.scalars(stmt.order_by(Confusion.id.desc()).limit(limit + 1)))


def confusion(db: Session, shop_id: int, confusion_id: int) -> Confusion | None:
    return db.scalar(select(Confusion).where(Confusion.shop_id == shop_id, Confusion.id == confusion_id))
