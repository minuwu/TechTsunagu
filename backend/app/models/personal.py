"""Personal data (ADR 0014, XREQ-017, XREQ-018): My QR, Contacts, blocks, Contact Threads and Talk Threads. Scoped by
user id, never by shop: a Foreman, a Poster and the Support Log have no route that reads these tables."""

import uuid

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    ForeignKey,
    ForeignKeyConstraint,
    Index,
    LargeBinary,
    Text,
    UniqueConstraint,
    Uuid,
)
from sqlalchemy import text as sql_text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, CreatedAt, IdPk, Timestamp, TimestampOrNone
from app.models.scopes import LANGS


class QrToken(Base):
    """A user's My QR: an opaque token, stored only as its SHA-256. Rotating replaces it; old QR codes stop working."""

    __tablename__ = "qr_tokens"

    user_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    token_hash: Mapped[bytes] = mapped_column(LargeBinary, unique=True)
    created_at: Mapped[CreatedAt]


class Contact(Base):
    """One side of a connection: `owner` sees `peer` in their list. Scanning a QR makes both sides."""

    __tablename__ = "contacts"
    __table_args__ = (
        CheckConstraint("owner_id <> peer_id", name="not_self"),
        UniqueConstraint("owner_id", "peer_id"),
        Index("ix_contacts_peer_owner", "peer_id", "owner_id"),
    )

    id: Mapped[IdPk]
    owner_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    peer_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    muted: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    hidden: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    last_read_at: Mapped[TimestampOrNone]
    created_at: Mapped[CreatedAt]


class Block(Base):
    """`blocker` gets nothing from `blocked`; the blocked side is never told (its sends still answer 201)."""

    __tablename__ = "blocks"

    blocker_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    blocked_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    created_at: Mapped[CreatedAt]


class ContactMessage(Base):
    """A Contact Thread message. Translated once, at send time, into the recipient's language."""

    __tablename__ = "contact_messages"
    __table_args__ = (
        CheckConstraint("char_length(text_src) BETWEEN 1 AND 2000", name="text_length"),
        CheckConstraint(f"src_lang IN {LANGS}", name="src_lang"),
        UniqueConstraint("sender_id", "idempotency_key"),
        Index("ix_contact_messages_pair_id", "sender_id", "recipient_id", "id"),
        Index("ix_contact_messages_recipient_pair_id", "recipient_id", "sender_id", "id"),
    )

    id: Mapped[IdPk]
    sender_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    recipient_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    text_src: Mapped[str] = mapped_column(Text)
    src_lang: Mapped[str] = mapped_column(Text)
    # language -> text; empty when the model was unavailable (the reader then sees text_src).
    translations: Mapped[dict[str, str]] = mapped_column(JSONB, server_default=sql_text("'{}'::jsonb"))
    # False when the sender was blocked: kept for the sender's own view, never shown to the recipient.
    delivered: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("true"))
    # The recipient deleted their copy. A sender deleting theirs removes the row: nothing of it is kept (ADR 0014).
    recipient_deleted: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    idempotency_key: Mapped[uuid.UUID | None] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]


class TalkThread(Base):
    """A Talk Thread: its owner's notes about real-life conversations with one person who is not in the app."""

    __tablename__ = "talk_threads"
    __table_args__ = (
        CheckConstraint("char_length(title) BETWEEN 1 AND 40", name="title_length"),
        UniqueConstraint("owner_id", "idempotency_key"),
        UniqueConstraint("owner_id", "id"),
    )

    id: Mapped[IdPk]
    owner_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    title: Mapped[str] = mapped_column(Text)
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    updated_at: Mapped[Timestamp]


class TalkEntry(Base):
    """One exchange: what they said and what it means (AI), the owner's own understanding and reply. The recording
    is never stored."""

    __tablename__ = "talk_entries"
    __table_args__ = (
        CheckConstraint("char_length(transcript) <= 2000", name="transcript_length"),
        CheckConstraint("char_length(own_text) <= 1000", name="own_text_length"),
        CheckConstraint("char_length(reply_ja) <= 500", name="reply_ja_length"),
        CheckConstraint("grade IN ('green','orange','red')", name="grade"),
        CheckConstraint("confirmed IS NULL OR reply_ja IS NOT NULL", name="confirmed_has_reply"),
        UniqueConstraint("owner_id", "idempotency_key"),
        ForeignKeyConstraint(
            ["owner_id", "thread_id"], ["talk_threads.owner_id", "talk_threads.id"], ondelete="CASCADE"
        ),
        Index("ix_talk_entries_owner_thread_id", "owner_id", "thread_id", "id"),
    )

    id: Mapped[IdPk]
    owner_id: Mapped[int] = mapped_column(BigInteger)
    thread_id: Mapped[int] = mapped_column(BigInteger)
    transcript: Mapped[str] = mapped_column(Text)
    plain_ja: Mapped[str | None] = mapped_column(Text)
    meaning: Mapped[str | None] = mapped_column(Text)
    terms: Mapped[list[dict[str, str]]] = mapped_column(JSONB, server_default=sql_text("'[]'::jsonb"))
    reply_options: Mapped[list[dict[str, str]]] = mapped_column(JSONB, server_default=sql_text("'[]'::jsonb"))
    own_text: Mapped[str | None] = mapped_column(Text)
    grade: Mapped[str | None] = mapped_column(Text)
    reply_ja: Mapped[str | None] = mapped_column(Text)
    confirmed: Mapped[bool | None] = mapped_column(Boolean)
    idempotency_key: Mapped[uuid.UUID] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
