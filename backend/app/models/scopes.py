"""Scopes and identity (architecture §2, §3, §6.1)."""

from datetime import datetime

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    DateTime,
    ForeignKey,
    ForeignKeyConstraint,
    Index,
    Integer,
    LargeBinary,
    SmallInteger,
    Text,
    UniqueConstraint,
    func,
    text,
)
from sqlalchemy.dialects.postgresql import ARRAY
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, CreatedAt, IdPk, Timestamp, TimestampOrNone

SCOPE_CODE = "~ '^[A-Z0-9]{8}$'"
LANGS = "('ja','bn','en','vi','id','ne','my','tl','zh')"


class District(Base):
    __tablename__ = "districts"
    __table_args__ = (CheckConstraint(f"code {SCOPE_CODE}", name="code_format"),)

    id: Mapped[IdPk]
    code: Mapped[str] = mapped_column(Text, unique=True)
    name: Mapped[str] = mapped_column(Text)
    prefecture: Mapped[str] = mapped_column(Text)
    municipality: Mapped[str] = mapped_column(Text)
    created_at: Mapped[CreatedAt]


class Shop(Base):
    __tablename__ = "shops"
    __table_args__ = (
        CheckConstraint(f"code {SCOPE_CODE}", name="code_format"),
        CheckConstraint(
            "languages <@ ARRAY['bn','en','vi','id','ne','my','tl','zh']::text[] "
            "AND cardinality(languages) BETWEEN 1 AND 4",
            name="languages_supported",
        ),
    )

    id: Mapped[IdPk]
    code: Mapped[str] = mapped_column(Text, unique=True)
    name: Mapped[str] = mapped_column(Text)
    timezone: Mapped[str] = mapped_column(Text, server_default="Asia/Tokyo")
    default_district_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("districts.id"), index=True)
    languages: Mapped[list[str]] = mapped_column(ARRAY(Text), server_default=text("'{bn,en}'"))
    created_at: Mapped[CreatedAt]


class Poster(Base):
    __tablename__ = "posters"
    __table_args__ = (
        UniqueConstraint("district_id", "id"),
        CheckConstraint("kind IN ('jichikai','city','employer')", name="kind"),
    )

    id: Mapped[IdPk]
    district_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("districts.id"), index=True)
    name: Mapped[str] = mapped_column(Text)
    kind: Mapped[str] = mapped_column(Text)


class User(Base):
    __tablename__ = "users"
    __table_args__ = (
        CheckConstraint("role IN ('foreman','worker','poster')", name="role"),
        CheckConstraint("char_length(display_name) BETWEEN 1 AND 40", name="display_name_length"),
        CheckConstraint(f"language IN {LANGS}", name="language"),
        CheckConstraint(
            "(role = 'foreman' AND shop_id IS NOT NULL)"
            " OR (role = 'worker' AND shop_id IS NOT NULL AND district_id IS NOT NULL)"
            " OR (role = 'poster' AND shop_id IS NULL AND district_id IS NOT NULL AND poster_id IS NOT NULL)",
            name="scope_for_role",
        ),
        UniqueConstraint("shop_id", "id"),
        UniqueConstraint("district_id", "id"),
        ForeignKeyConstraint(["district_id", "poster_id"], ["posters.district_id", "posters.id"]),
        Index(
            "uq_users_shop_display_name",
            "shop_id",
            "display_name",
            unique=True,
            postgresql_where=text("active AND shop_id IS NOT NULL"),
        ),
        Index(
            "uq_users_poster_display_name",
            "district_id",
            "display_name",
            unique=True,
            postgresql_where=text("active AND role = 'poster'"),
        ),
    )

    id: Mapped[IdPk]
    role: Mapped[str] = mapped_column(Text)
    display_name: Mapped[str] = mapped_column(Text)
    language: Mapped[str] = mapped_column(Text)
    shop_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("shops.id"))
    district_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("districts.id"), index=True)
    poster_id: Mapped[int | None] = mapped_column(BigInteger)
    pin_hash: Mapped[str] = mapped_column(Text)
    pin_must_change: Mapped[bool] = mapped_column(Boolean, server_default=text("true"))
    pin_failed_count: Mapped[int] = mapped_column(SmallInteger, server_default=text("0"))
    pin_locked_until: Mapped[TimestampOrNone]
    active: Mapped[bool] = mapped_column(Boolean, server_default=text("true"))
    created_at: Mapped[CreatedAt]


class Session(Base):
    __tablename__ = "sessions"
    __table_args__ = (
        CheckConstraint("kind IN ('user','panel')", name="kind"),
        CheckConstraint("(kind = 'user') = (user_id IS NOT NULL)", name="user_session_has_user"),
        CheckConstraint(
            "(kind = 'panel') = (machine_id IS NOT NULL AND shop_id IS NOT NULL)", name="panel_session_has_machine"
        ),
        ForeignKeyConstraint(["shop_id", "machine_id"], ["machines.shop_id", "machines.id"]),
    )

    token_hash: Mapped[bytes] = mapped_column(LargeBinary, primary_key=True)
    kind: Mapped[str] = mapped_column(Text)
    user_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("users.id"), index=True)
    machine_id: Mapped[int | None] = mapped_column(BigInteger)
    shop_id: Mapped[int | None] = mapped_column(BigInteger)
    created_at: Mapped[CreatedAt]
    expires_at: Mapped[Timestamp] = mapped_column(index=True)
    last_seen_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    revoked_at: Mapped[TimestampOrNone]


class AuditEvent(Base):
    """Insert-only: the app role gets INSERT and SELECT, never UPDATE or DELETE (migration grants)."""

    __tablename__ = "audit_events"
    __table_args__ = (
        Index("ix_audit_events_shop_at", "shop_id", text("at DESC")),
        Index("ix_audit_events_district_at", "district_id", text("at DESC")),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int | None] = mapped_column(BigInteger)
    district_id: Mapped[int | None] = mapped_column(BigInteger)
    actor_user_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("users.id"), index=True)
    action: Mapped[str] = mapped_column(Text)
    subject_id: Mapped[int | None] = mapped_column(BigInteger)
    at: Mapped[CreatedAt]


class LoginLimit(Base):
    __tablename__ = "login_limits"

    key: Mapped[str] = mapped_column(Text, primary_key=True)
    window_start: Mapped[Timestamp]
    attempts: Mapped[int] = mapped_column(Integer, server_default=text("0"))
