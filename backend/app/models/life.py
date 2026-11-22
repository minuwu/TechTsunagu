"""Life data (architecture §6.3): Notices and Events belong to a district; Senpai pairs, Asks and Meet-ups to a shop."""

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
    LargeBinary,
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


class Notice(Base):
    __tablename__ = "notices"
    __table_args__ = (
        CheckConstraint("category IN ('garbage','disaster','event','duty','other')", name="category"),
        CheckConstraint("source_kind IN ('text','voice','photo')", name="source_kind"),
        CheckConstraint("status IN ('draft','published')", name="status"),
        CheckConstraint("(status = 'published') = (published_at IS NOT NULL)", name="published_is_complete"),
        UniqueConstraint("district_id", "idempotency_key"),
        UniqueConstraint("district_id", "id"),
        ForeignKeyConstraint(["district_id", "poster_id"], ["posters.district_id", "posters.id"]),
        ForeignKeyConstraint(["district_id", "created_by"], ["users.district_id", "users.id"]),
        Index(
            "ix_notices_published",
            "district_id",
            sql_text("published_at DESC"),
            postgresql_where=sql_text("status = 'published'"),
        ),
        Index("ix_notices_poster_created", "district_id", "poster_id", sql_text("created_at DESC")),
    )

    id: Mapped[IdPk]
    district_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("districts.id"))
    poster_id: Mapped[int] = mapped_column(BigInteger)
    created_by: Mapped[int] = mapped_column(BigInteger)
    category: Mapped[str] = mapped_column(Text)
    source_kind: Mapped[str] = mapped_column(Text)
    source_ja: Mapped[str] = mapped_column(Text)
    source_blob_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("blobs.id"), index=True)
    title: Mapped[Tri] = mapped_column(JSONB)
    steps: Mapped[list[Tri]] = mapped_column(JSONB)
    question: Mapped[dict[str, object]] = mapped_column(JSONB)
    status: Mapped[str] = mapped_column(Text, server_default="draft")
    needs_edit: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    published_at: Mapped[TimestampOrNone]


class Event(Base):
    __tablename__ = "events"
    __table_args__ = (
        CheckConstraint("rrule IS NULL OR char_length(rrule) <= 200", name="rrule_length"),
        ForeignKeyConstraint(["district_id", "notice_id"], ["notices.district_id", "notices.id"], ondelete="CASCADE"),
        Index("ix_events_district_notice", "district_id", "notice_id"),
    )

    id: Mapped[IdPk]
    district_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("districts.id"))
    notice_id: Mapped[int] = mapped_column(BigInteger)
    title: Mapped[Tri] = mapped_column(JSONB)
    starts_at: Mapped[Timestamp]
    ends_at: Mapped[TimestampOrNone]
    rrule: Mapped[str | None] = mapped_column(Text)
    confirmed: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))


class SenpaiPair(Base):
    __tablename__ = "senpai_pairs"
    __table_args__ = (
        CheckConstraint("kind IN ('coworker','jichikai')", name="kind"),
        UniqueConstraint("shop_id", "id"),
        ForeignKeyConstraint(["shop_id", "worker_id"], ["users.shop_id", "users.id"]),
        ForeignKeyConstraint(["shop_id", "created_by"], ["users.shop_id", "users.id"]),
        Index(
            "uq_senpai_pairs_active_worker",
            "shop_id",
            "worker_id",
            unique=True,
            postgresql_where=sql_text("revoked_at IS NULL"),
        ),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    senpai_name: Mapped[str] = mapped_column(Text)
    kind: Mapped[str] = mapped_column(Text)
    token_hash: Mapped[bytes] = mapped_column(LargeBinary, unique=True)
    created_by: Mapped[int] = mapped_column(BigInteger)
    created_at: Mapped[CreatedAt]
    expires_at: Mapped[Timestamp]
    revoked_at: Mapped[TimestampOrNone]


class Ask(Base):
    __tablename__ = "asks"
    __table_args__ = (
        CheckConstraint("source_kind IN ('text','voice')", name="source_kind"),
        CheckConstraint("char_length(text_src) <= 1000", name="text_src_length"),
        CheckConstraint(f"language IN {LANGS}", name="language"),
        CheckConstraint(
            "covered = (answer IS NOT NULL AND cardinality(source_notice_ids) > 0)", name="covered_is_grounded"
        ),
        UniqueConstraint("shop_id", "idempotency_key"),
        ForeignKeyConstraint(["shop_id", "worker_id"], ["users.shop_id", "users.id"]),
        ForeignKeyConstraint(["shop_id", "pair_id"], ["senpai_pairs.shop_id", "senpai_pairs.id"]),
        Index("ix_asks_shop_worker_created", "shop_id", "worker_id", sql_text("created_at DESC")),
        Index(
            "ix_asks_waiting_for_senpai",
            "pair_id",
            postgresql_where=sql_text("forwarded_at IS NOT NULL AND replied_at IS NULL"),
        ),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    worker_id: Mapped[int] = mapped_column(BigInteger)
    source_kind: Mapped[str] = mapped_column(Text)
    text_src: Mapped[str] = mapped_column(Text)
    text_ja: Mapped[str | None] = mapped_column(Text)
    language: Mapped[str] = mapped_column(Text)
    covered: Mapped[bool] = mapped_column(Boolean)
    answer: Mapped[Tri | None] = mapped_column(JSONB)
    source_notice_ids: Mapped[list[int]] = mapped_column(ARRAY(BigInteger), server_default=sql_text("'{}'"))
    pair_id: Mapped[int | None] = mapped_column(BigInteger)
    forwarded_at: Mapped[TimestampOrNone]
    senpai_reply_ja: Mapped[str | None] = mapped_column(Text)
    senpai_reply: Mapped[Tri | None] = mapped_column(JSONB)
    replied_at: Mapped[TimestampOrNone]
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]


class Meetup(Base):
    __tablename__ = "meetups"
    __table_args__ = (
        CheckConstraint("kind IN ('coffee','event','drill','other')", name="kind"),
        CheckConstraint("char_length(note) <= 500", name="note_length"),
        CheckConstraint("logged_by IN ('worker','senpai')", name="logged_by"),
        ForeignKeyConstraint(["shop_id", "pair_id"], ["senpai_pairs.shop_id", "senpai_pairs.id"]),
        Index("ix_meetups_shop_pair_day", "shop_id", "pair_id", sql_text("happened_on DESC")),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    pair_id: Mapped[int] = mapped_column(BigInteger)
    kind: Mapped[str] = mapped_column(Text)
    happened_on: Mapped[date] = mapped_column(Date)
    note: Mapped[str] = mapped_column(Text, server_default="")
    logged_by: Mapped[str] = mapped_column(Text)
    created_at: Mapped[CreatedAt]
