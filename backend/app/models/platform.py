"""Photos, AI cache and the AI daily budget (architecture §6.4)."""

import uuid
from datetime import date

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    Date,
    ForeignKey,
    Integer,
    LargeBinary,
    SmallInteger,
    Text,
    UniqueConstraint,
    Uuid,
)
from sqlalchemy import text as sql_text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, CreatedAt, IdPk, Timestamp

MAX_BLOB_BYTES = 2 * 1024 * 1024


class Blob(Base):
    __tablename__ = "blobs"
    __table_args__ = (
        CheckConstraint("num_nonnulls(shop_id, district_id) = 1", name="one_scope"),
        CheckConstraint(f"size_bytes > 0 AND size_bytes <= {MAX_BLOB_BYTES}", name="size_limit"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("shops.id"), index=True)
    district_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("districts.id"), index=True)
    created_by: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id"), index=True)
    mime: Mapped[str] = mapped_column(Text)
    size_bytes: Mapped[int] = mapped_column(Integer)
    sha256: Mapped[bytes] = mapped_column(LargeBinary)
    bytes_: Mapped[bytes] = mapped_column("bytes", LargeBinary)
    created_at: Mapped[CreatedAt]


class AiCache(Base):
    __tablename__ = "ai_cache"

    key: Mapped[str] = mapped_column(Text, primary_key=True)
    scope: Mapped[str] = mapped_column(Text)
    pipeline: Mapped[str] = mapped_column(Text)
    prompt_version: Mapped[int] = mapped_column(Integer)
    model: Mapped[str] = mapped_column(Text)
    response: Mapped[dict[str, object]] = mapped_column(JSONB)
    created_at: Mapped[CreatedAt]


class AiUsage(Base):
    """Calls per Pacific-time day and model; the free-tier quota resets at midnight Pacific."""

    __tablename__ = "ai_usage"

    day: Mapped[date] = mapped_column(Date, primary_key=True)
    model: Mapped[str] = mapped_column(Text, primary_key=True)
    calls: Mapped[int] = mapped_column(Integer, server_default=sql_text("0"))
    exhausted: Mapped[bool] = mapped_column(server_default=sql_text("false"))


class Device(Base):
    """An app install that can receive push (XREQ-009, MOBILE_PLATFORM_CONTRACT §1.3). Tied to the session that
    registered it: when that session row goes, so does the device; a sender also skips revoked or expired sessions."""

    __tablename__ = "devices"
    __table_args__ = (
        CheckConstraint("platform IN ('android')", name="platform"),
        CheckConstraint("char_length(fcm_token) BETWEEN 1 AND 4096", name="fcm_token_length"),
        UniqueConstraint("user_id", "install_id"),
        UniqueConstraint("fcm_token"),  # a token belongs to one user at a time (shared phones)
    )

    id: Mapped[IdPk]
    user_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), index=True)
    install_id: Mapped[uuid.UUID] = mapped_column(Uuid)
    platform: Mapped[str] = mapped_column(Text)
    fcm_token: Mapped[str] = mapped_column(Text)
    app_version: Mapped[str] = mapped_column(Text)
    os_api_level: Mapped[int | None] = mapped_column(SmallInteger)
    locale: Mapped[str | None] = mapped_column(Text)
    notifications_enabled: Mapped[bool] = mapped_column(Boolean)
    session_token_hash: Mapped[bytes] = mapped_column(
        LargeBinary, ForeignKey("sessions.token_hash", ondelete="CASCADE"), index=True
    )
    last_seen_at: Mapped[Timestamp]
    created_at: Mapped[CreatedAt]
