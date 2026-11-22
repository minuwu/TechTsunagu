"""Photos, AI cache and the AI daily budget (architecture §6.4)."""

from datetime import date

from sqlalchemy import BigInteger, CheckConstraint, Date, ForeignKey, Integer, LargeBinary, Text
from sqlalchemy import text as sql_text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, CreatedAt, IdPk

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
