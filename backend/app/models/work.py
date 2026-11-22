"""Work data, scoped by shop (architecture §6.2). Every same-shop reference is a composite FK on (shop_id, id)."""

import uuid
from datetime import date

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    Date,
    ForeignKey,
    ForeignKeyConstraint,
    Index,
    Integer,
    LargeBinary,
    SmallInteger,
    Text,
    UniqueConstraint,
    Uuid,
)
from sqlalchemy import (
    text as sql_text,
)
from sqlalchemy.dialects.postgresql import ARRAY, JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, CreatedAt, IdPk, Timestamp, TimestampOrNone
from app.models.scopes import LANGS

Tri = dict[str, str]
JsonList = list[dict[str, object]]


def _fk(local: str, table: str, **kw: object) -> ForeignKeyConstraint:
    """(shop_id, <local>) -> <table>(shop_id, id): the database refuses a reference into another shop."""
    return ForeignKeyConstraint(["shop_id", local], [f"{table}.shop_id", f"{table}.id"], **kw)  # pyright: ignore[reportArgumentType]


class Machine(Base):
    __tablename__ = "machines"
    __table_args__ = (
        CheckConstraint("code ~ '^[a-z0-9-]{1,32}$'", name="code_format"),
        UniqueConstraint("shop_id", "code"),
        UniqueConstraint("shop_id", "id"),
        _fk("current_version_id", "note_versions", use_alter=True, deferrable=True, initially="DEFERRED"),
        _fk("present_worker_id", "users"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    code: Mapped[str] = mapped_column(Text)
    name: Mapped[Tri] = mapped_column(JSONB)
    current_version_id: Mapped[int | None] = mapped_column(BigInteger)
    recheck_version_no: Mapped[int | None] = mapped_column(Integer)
    present_worker_id: Mapped[int | None] = mapped_column(BigInteger)
    present_at: Mapped[TimestampOrNone]
    created_at: Mapped[CreatedAt]


class GlossaryTerm(Base):
    __tablename__ = "glossary_terms"
    __table_args__ = (UniqueConstraint("shop_id", "ja"), UniqueConstraint("shop_id", "id"))

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    ja: Mapped[str] = mapped_column(Text)
    reading: Mapped[str] = mapped_column(Text)
    translations: Mapped[dict[str, str]] = mapped_column(JSONB)
    note: Mapped[str | None] = mapped_column(Text)


class NoteVersion(Base):
    __tablename__ = "note_versions"
    __table_args__ = (
        CheckConstraint("status IN ('draft','approved')", name="status"),
        CheckConstraint("source_kind IN ('voice','text')", name="source_kind"),
        CheckConstraint("change_kind IS NULL OR change_kind IN ('safety','wording')", name="change_kind"),
        CheckConstraint("ai_outcome IN ('ok','retried','fallback_seed','degraded','typed')", name="ai_outcome"),
        CheckConstraint(
            "(status = 'approved') = (approved_at IS NOT NULL AND approved_by IS NOT NULL AND change_kind IS NOT NULL)",
            name="approved_is_complete",
        ),
        UniqueConstraint("shop_id", "machine_id", "version_no"),
        UniqueConstraint("shop_id", "idempotency_key"),
        UniqueConstraint("shop_id", "id"),
        _fk("machine_id", "machines"),
        _fk("recorded_by", "users"),
        _fk("approved_by", "users"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    machine_id: Mapped[int] = mapped_column(BigInteger)
    version_no: Mapped[int] = mapped_column(Integer)
    status: Mapped[str] = mapped_column(Text, server_default="draft")
    needs_edit: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    recorded_by: Mapped[int] = mapped_column(BigInteger)
    approved_by: Mapped[int | None] = mapped_column(BigInteger)
    source_kind: Mapped[str] = mapped_column(Text)
    audio_sha256: Mapped[bytes | None] = mapped_column(LargeBinary)
    transcript_ja: Mapped[str] = mapped_column(Text)
    steps: Mapped[JsonList] = mapped_column(JSONB)
    back_translation: Mapped[dict[str, str]] = mapped_column(JSONB)
    questions: Mapped[JsonList] = mapped_column(JSONB)
    change_kind: Mapped[str | None] = mapped_column(Text)
    edit_count: Mapped[int] = mapped_column(Integer, server_default=sql_text("0"))
    ai_outcome: Mapped[str] = mapped_column(Text)
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    approved_at: Mapped[TimestampOrNone]


class Briefing(Base):
    __tablename__ = "briefings"
    __table_args__ = (
        CheckConstraint("status IN ('draft','published')", name="status"),
        CheckConstraint("(status = 'published') = (published_at IS NOT NULL)", name="published_is_complete"),
        UniqueConstraint("shop_id", "idempotency_key"),
        UniqueConstraint("shop_id", "id"),
        _fk("recorded_by", "users"),
        Index(
            "ix_briefings_published",
            "shop_id",
            sql_text("published_at DESC"),
            postgresql_where=sql_text("status = 'published'"),
        ),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    status: Mapped[str] = mapped_column(Text, server_default="draft")
    recorded_by: Mapped[int] = mapped_column(BigInteger)
    transcript_ja: Mapped[str] = mapped_column(Text)
    text: Mapped[Tri] = mapped_column(JSONB)
    question: Mapped[dict[str, object]] = mapped_column(JSONB)
    needs_edit: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    published_at: Mapped[TimestampOrNone]


class Check(Base):
    __tablename__ = "checks"
    __table_args__ = (
        CheckConstraint("num_nonnulls(note_version_id, briefing_id, notice_id) = 1", name="one_target"),
        CheckConstraint(f"language IN {LANGS}", name="language"),
        UniqueConstraint("shop_id", "worker_id", "idempotency_key"),
        UniqueConstraint("shop_id", "id"),
        _fk("worker_id", "users"),
        _fk("note_version_id", "note_versions"),
        _fk("briefing_id", "briefings"),
        Index("ix_checks_shop_worker_created", "shop_id", "worker_id", sql_text("created_at DESC")),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    note_version_id: Mapped[int | None] = mapped_column(BigInteger, index=True)
    briefing_id: Mapped[int | None] = mapped_column(BigInteger, index=True)
    notice_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("notices.id"), index=True)
    language: Mapped[str] = mapped_column(Text)
    answers: Mapped[list[int]] = mapped_column(ARRAY(SmallInteger))
    passed: Mapped[bool] = mapped_column(Boolean)
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]


class Confusion(Base):
    __tablename__ = "confusions"
    __table_args__ = (
        _fk("check_id", "checks"),
        _fk("worker_id", "users"),
        _fk("machine_id", "machines"),
        _fk("term_id", "glossary_terms"),
        _fk("acknowledged_by", "users"),
        Index(
            "ix_confusions_open",
            "shop_id",
            sql_text("created_at DESC"),
            postgresql_where=sql_text("acknowledged_at IS NULL"),
        ),
        Index("ix_confusions_shop_term", "shop_id", "term_id"),
        Index("ix_confusions_shop_machine", "shop_id", "machine_id"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    check_id: Mapped[int] = mapped_column(BigInteger)
    worker_id: Mapped[int] = mapped_column(BigInteger)
    machine_id: Mapped[int] = mapped_column(BigInteger)
    question_id: Mapped[str] = mapped_column(Text)
    term_id: Mapped[int | None] = mapped_column(BigInteger)
    created_at: Mapped[CreatedAt]
    acknowledged_at: Mapped[TimestampOrNone]
    acknowledged_by: Mapped[int | None] = mapped_column(BigInteger)


class SignOff(Base):
    __tablename__ = "sign_offs"
    __table_args__ = (
        UniqueConstraint("check_id"),
        _fk("worker_id", "users"),
        _fk("machine_id", "machines"),
        _fk("note_version_id", "note_versions"),
        _fk("check_id", "checks"),
        Index("ix_sign_offs_lookup", "shop_id", "worker_id", "machine_id", sql_text("passed_at DESC")),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    machine_id: Mapped[int] = mapped_column(BigInteger)
    note_version_id: Mapped[int] = mapped_column(BigInteger)
    check_id: Mapped[int] = mapped_column(BigInteger)
    passed_at: Mapped[Timestamp]


class ShiftAssignment(Base):
    __tablename__ = "shift_assignments"
    __table_args__ = (
        CheckConstraint("shift IN ('day','night')", name="shift"),
        UniqueConstraint("shop_id", "machine_id", "shift_date", "shift"),
        _fk("worker_id", "users"),
        _fk("machine_id", "machines"),
        _fk("assigned_by", "users"),
        Index("ix_shift_assignments_shop_date", "shop_id", "shift_date"),
        Index("ix_shift_assignments_shop_worker_date", "shop_id", "worker_id", "shift_date"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    machine_id: Mapped[int] = mapped_column(BigInteger)
    shift_date: Mapped[date] = mapped_column(Date)
    shift: Mapped[str] = mapped_column(Text)
    assigned_by: Mapped[int] = mapped_column(BigInteger)


class PanelOverride(Base):
    __tablename__ = "panel_overrides"
    __table_args__ = (
        CheckConstraint("char_length(reason) BETWEEN 3 AND 200", name="reason_length"),
        CheckConstraint("ends_at > starts_at AND ends_at <= starts_at + interval '8 hours'", name="at_most_8_hours"),
        _fk("machine_id", "machines"),
        _fk("worker_id", "users"),
        _fk("created_by", "users"),
        Index("ix_panel_overrides_machine_ends", "shop_id", "machine_id", sql_text("ends_at DESC")),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    machine_id: Mapped[int] = mapped_column(BigInteger)
    worker_id: Mapped[int | None] = mapped_column(BigInteger)
    reason: Mapped[str] = mapped_column(Text)
    created_by: Mapped[int] = mapped_column(BigInteger)
    starts_at: Mapped[CreatedAt]
    ends_at: Mapped[Timestamp]
    revoked_at: Mapped[TimestampOrNone]


class Receipt(Base):
    __tablename__ = "receipts"
    __table_args__ = (
        CheckConstraint("num_nonnulls(briefing_id, notice_id) = 1", name="one_target"),
        _fk("worker_id", "users"),
        _fk("briefing_id", "briefings"),
        Index(
            "uq_receipts_worker_briefing",
            "worker_id",
            "briefing_id",
            unique=True,
            postgresql_where=sql_text("briefing_id IS NOT NULL"),
        ),
        Index(
            "uq_receipts_worker_notice",
            "worker_id",
            "notice_id",
            unique=True,
            postgresql_where=sql_text("notice_id IS NOT NULL"),
        ),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    briefing_id: Mapped[int | None] = mapped_column(BigInteger, index=True)
    notice_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("notices.id"), index=True)
    opened_at: Mapped[CreatedAt]
    passed: Mapped[bool | None] = mapped_column(Boolean)


class NearMissReport(Base):
    __tablename__ = "near_miss_reports"
    __table_args__ = (
        CheckConstraint("source_kind IN ('voice','text')", name="source_kind"),
        UniqueConstraint("shop_id", "idempotency_key"),
        _fk("worker_id", "users"),
        _fk("machine_id", "machines"),
        _fk("acknowledged_by", "users"),
        Index(
            "ix_near_miss_reports_open",
            "shop_id",
            sql_text("created_at DESC"),
            postgresql_where=sql_text("acknowledged_at IS NULL"),
        ),
        Index("ix_near_miss_reports_shop_machine", "shop_id", "machine_id"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    machine_id: Mapped[int] = mapped_column(BigInteger)
    source_kind: Mapped[str] = mapped_column(Text)
    text_src: Mapped[str] = mapped_column(Text)
    text: Mapped[Tri] = mapped_column(JSONB)
    photo_blob_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("blobs.id"), index=True)
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    acknowledged_at: Mapped[TimestampOrNone]
    acknowledged_by: Mapped[int | None] = mapped_column(BigInteger)
