"""Personal data (ADR 0014, XREQ-017, XREQ-018): My QR, Contacts, blocks, Contact Threads and Talk Threads.
Scoped by user id, never by shop: a Foreman, a Poster and the Support Log have no route that reads these tables."""

import uuid

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    ForeignKey,
    Index,
    LargeBinary,
    Text,
    UniqueConstraint,
    Uuid,
)
from sqlalchemy import text as sql_text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, CreatedAt, IdPk, TimestampOrNone
from app.models.scopes import LANGS


class QrToken(Base):
    """A user's My QR: an opaque token, stored only as its SHA-256. Rotating replaces it; old QR codes stop working."""

    __tablename__ = "qr_tokens"

    user_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    token_hash: Mapped[bytes] = mapped_column(LargeBinary, unique=True)
    created_at: Mapped[CreatedAt]


class RetiredQrToken(Base):
    """A My QR replaced by a new one, kept as its SHA-256 only so a scan can say "replaced", not "not found"."""

    __tablename__ = "retired_qr_tokens"

    token_hash: Mapped[bytes] = mapped_column(LargeBinary, primary_key=True)
    user_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), index=True)
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
    """A Talk Thread (XREQ-018): the owner's own notebook about real-life conversations. Private to the owner (no shop,
    no Foreman, no Support Log), never notifies; deleting it deletes every entry."""

    __tablename__ = "talk_threads"
    __table_args__ = (
        CheckConstraint("char_length(title) BETWEEN 1 AND 40", name="title_length"),
        UniqueConstraint("owner_id", "idempotency_key"),
        Index("ix_talk_threads_owner_id", "owner_id", "id"),
    )

    id: Mapped[IdPk]
    owner_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    title: Mapped[str] = mapped_column(Text)
    idempotency_key: Mapped[uuid.UUID | None] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    updated_at: Mapped[CreatedAt]


class TalkEntry(Base):
    """What someone said, explained (explain_utterance), the owner's own understanding graded (grade_understanding),
    the reply they chose and whether it got through. On AI failure only the transcript is kept, with needs_edit."""

    __tablename__ = "talk_entries"
    __table_args__ = (
        CheckConstraint("grade IS NULL OR grade IN ('green','orange','red')", name="grade"),
        CheckConstraint(f"meaning_lang IN {LANGS}", name="meaning_lang"),
        CheckConstraint("char_length(transcript) <= 2000", name="transcript_length"),
        CheckConstraint("own_text IS NULL OR char_length(own_text) <= 1000", name="own_text_length"),
        CheckConstraint("reply_ja IS NULL OR char_length(reply_ja) <= 500", name="reply_ja_length"),
        UniqueConstraint("owner_id", "idempotency_key"),
        Index("ix_talk_entries_thread_id", "thread_id", "id"),
    )

    id: Mapped[IdPk]
    thread_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("talk_threads.id", ondelete="CASCADE"))
    owner_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"))
    transcript: Mapped[str] = mapped_column(Text)
    plain_ja: Mapped[str | None] = mapped_column(Text)
    meaning: Mapped[str | None] = mapped_column(Text)
    meaning_lang: Mapped[str] = mapped_column(Text)
    terms: Mapped[list[dict[str, str]]] = mapped_column(JSONB, server_default=sql_text("'[]'::jsonb"))
    reply_options: Mapped[list[dict[str, str]]] = mapped_column(JSONB, server_default=sql_text("'[]'::jsonb"))
    needs_staff: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    needs_edit: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    own_text: Mapped[str | None] = mapped_column(Text)
    grade: Mapped[str | None] = mapped_column(Text)
    reply_ja: Mapped[str | None] = mapped_column(Text)
    confirmed: Mapped[bool | None] = mapped_column(Boolean)
    idempotency_key: Mapped[uuid.UUID | None] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
