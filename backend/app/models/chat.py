"""Chat data (WS12, XREQ-005), scoped by shop like Work: same-shop references are composite FKs on (shop_id, id)."""

import uuid

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    ForeignKey,
    Index,
    Integer,
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
from app.models.work import JsonList, Tri, shop_fk


class Thread(Base):
    __tablename__ = "threads"
    __table_args__ = (
        CheckConstraint("kind IN ('group','direct')", name="kind"),
        CheckConstraint("char_length(title_ja) BETWEEN 1 AND 100", name="title_length"),
        CheckConstraint("char_length(description) <= 500", name="description_length"),
        UniqueConstraint("shop_id", "id"),
        UniqueConstraint("shop_id", "created_by", "idempotency_key"),
        shop_fk("created_by", "users"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    title_ja: Mapped[str] = mapped_column(Text)
    kind: Mapped[str] = mapped_column(Text)
    created_by: Mapped[int] = mapped_column(BigInteger)
    created_at: Mapped[CreatedAt]
    description: Mapped[str] = mapped_column(Text, server_default="")
    # Only admins post; Workers still answer Checks (XREQ-019).
    announce_only: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    # A fixed Thread (e.g. Safety notices): no rename, member change or leaving.
    locked: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    idempotency_key: Mapped[uuid.UUID | None] = mapped_column(Uuid)


class ThreadMember(Base):
    __tablename__ = "thread_members"
    __table_args__ = (
        CheckConstraint("role IN ('admin','member')", name="role"),
        shop_fk("thread_id", "threads", ondelete="CASCADE"),
        shop_fk("user_id", "users"),
        Index("ix_thread_members_shop_user", "shop_id", "user_id"),
    )

    thread_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    user_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    joined_at: Mapped[CreatedAt]
    # Unread = messages sent after this; null means nothing read yet.
    last_read_at: Mapped[TimestampOrNone]
    role: Mapped[str] = mapped_column(Text, server_default="member")
    # Messages from here on; null = the whole history (founding members). Leaving deletes the row.
    history_from: Mapped[TimestampOrNone]
    muted: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))  # no chat_message push only
    pinned: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    archived: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))


class Message(Base):
    __tablename__ = "messages"
    __table_args__ = (
        CheckConstraint("kind IN ('voice','text','ask','reexplain','system')", name="kind"),
        CheckConstraint("(kind = 'system') = (event IS NOT NULL)", name="system_has_event"),
        CheckConstraint(f"language IN {LANGS}", name="language"),
        CheckConstraint("stage BETWEEN 0 AND 5", name="stage"),
        # Stages 4 (Sent to Workers) and 5 (Answers in) are exactly the sent ones.
        CheckConstraint("(stage >= 4) = (sent_at IS NOT NULL)", name="sent_is_stage_4"),
        CheckConstraint(
            "kind <> 'reexplain' OR (to_user_id IS NOT NULL AND ref_message_id IS NOT NULL)",
            name="reexplain_has_target",
        ),
        CheckConstraint("text_ja IS NULL OR char_length(text_ja) <= 2000", name="text_ja_length"),
        UniqueConstraint("shop_id", "id"),
        UniqueConstraint("shop_id", "from_user_id", "idempotency_key"),
        shop_fk("thread_id", "threads", ondelete="CASCADE"),
        shop_fk("from_user_id", "users"),
        shop_fk("to_user_id", "users"),
        shop_fk("ref_message_id", "messages"),
        # Worker polling: `?after=<id>` within one Thread.
        Index("ix_messages_shop_thread_id", "shop_id", "thread_id", "id"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    thread_id: Mapped[int] = mapped_column(BigInteger)
    from_user_id: Mapped[int] = mapped_column(BigInteger)
    kind: Mapped[str] = mapped_column(Text)
    # The language the sender spoke or typed: Foremen Japanese, Workers often their own.
    language: Mapped[str] = mapped_column(Text, server_default="ja")
    transcript_ja: Mapped[str | None] = mapped_column(Text)
    text_ja: Mapped[str | None] = mapped_column(Text)
    translations: Mapped[Tri] = mapped_column(JSONB, server_default=sql_text("'{}'::jsonb"))
    audio_blob_id: Mapped[int | None] = mapped_column(BigInteger, ForeignKey("blobs.id"), index=True)
    ref_message_id: Mapped[int | None] = mapped_column(BigInteger)
    to_user_id: Mapped[int | None] = mapped_column(BigInteger)
    stage: Mapped[int] = mapped_column(SmallInteger, server_default=sql_text("0"))
    needs_edit: Mapped[bool] = mapped_column(Boolean, server_default=sql_text("false"))
    # The AI's Task drafts of an unsent message (API shape), so a retried POST returns them; Send clears them.
    task_drafts: Mapped[list[dict[str, object]]] = mapped_column(JSONB, server_default=sql_text("'[]'::jsonb"))
    idempotency_key: Mapped[uuid.UUID | None] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    sent_at: Mapped[TimestampOrNone]
    # A system message (XREQ-019): {event, actorId, subjectId}; the app writes the text per language.
    event: Mapped[dict[str, str | None] | None] = mapped_column(JSONB)


class MessageCheck(Base):
    __tablename__ = "message_checks"
    __table_args__ = (
        CheckConstraint("jsonb_array_length(questions) BETWEEN 1 AND 3", name="one_to_three_questions"),
        UniqueConstraint("message_id"),
        shop_fk("message_id", "messages", ondelete="CASCADE"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    message_id: Mapped[int] = mapped_column(BigInteger)
    # The existing CheckQuestion shape (app/schemas/common.py), correctIndex included: never sent to Workers.
    questions: Mapped[JsonList] = mapped_column(JSONB)
    deadline: Mapped[TimestampOrNone]
    created_at: Mapped[CreatedAt]


class MessageAnswer(Base):
    __tablename__ = "message_answers"
    __table_args__ = (
        CheckConstraint("status IN ('green','orange','red')", name="status"),
        # One answer per question: 0..2, or -1 for "not sure".
        CheckConstraint(
            "cardinality(answers) BETWEEN 1 AND 3 AND -1 <= ALL(answers) AND 2 >= ALL(answers)", name="answers"
        ),
        UniqueConstraint("message_id", "user_id"),
        shop_fk("message_id", "messages", ondelete="CASCADE"),
        shop_fk("user_id", "users"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    message_id: Mapped[int] = mapped_column(BigInteger)
    user_id: Mapped[int] = mapped_column(BigInteger)
    answers: Mapped[list[int]] = mapped_column(ARRAY(SmallInteger))
    status: Mapped[str] = mapped_column(Text)
    created_at: Mapped[CreatedAt]


class FollowUp(Base):
    __tablename__ = "follow_ups"
    __table_args__ = (
        CheckConstraint("status IN ('open','clarified','understood')", name="status"),
        CheckConstraint("(status = 'open') = (resolved_at IS NULL)", name="resolved_when_closed"),
        CheckConstraint("char_length(note) <= 500", name="note_length"),
        CheckConstraint("char_length(term_ja) BETWEEN 1 AND 100", name="term_ja_length"),
        # Answers are idempotent per message + user, so a term is followed up at most once.
        UniqueConstraint("message_id", "user_id", "term_ja"),
        UniqueConstraint("shop_id", "id"),
        shop_fk("message_id", "messages", ondelete="CASCADE"),
        shop_fk("user_id", "users"),
        shop_fk("resolved_by", "users"),
        Index("ix_follow_ups_shop_status_created", "shop_id", "status", sql_text("created_at DESC")),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    message_id: Mapped[int] = mapped_column(BigInteger)
    user_id: Mapped[int] = mapped_column(BigInteger)
    term_ja: Mapped[str] = mapped_column(Text)
    status: Mapped[str] = mapped_column(Text, server_default="open")
    note: Mapped[str] = mapped_column(Text, server_default="")
    created_at: Mapped[CreatedAt]
    # Null with a resolved_at when the Worker passed the Re-explanation (no person closed it).
    resolved_by: Mapped[int | None] = mapped_column(BigInteger)
    resolved_at: Mapped[TimestampOrNone]


class Task(Base):
    __tablename__ = "tasks"
    __table_args__ = (
        CheckConstraint("board_column IN ('todo','doing','done')", name="board_column"),
        CheckConstraint("version >= 1", name="version"),
        UniqueConstraint("shop_id", "id"),
        shop_fk("machine_id", "machines"),
        shop_fk("from_message_id", "messages"),
        shop_fk("created_by", "users"),
        Index("ix_tasks_shop_column", "shop_id", "board_column"),
        UniqueConstraint("shop_id", "idempotency_key"),
    )

    id: Mapped[IdPk]
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
    title: Mapped[Tri] = mapped_column(JSONB)
    # `column` is an SQL keyword; the API field is still `column`.
    board_column: Mapped[str] = mapped_column(Text, server_default="todo")
    due_at: Mapped[TimestampOrNone]
    machine_id: Mapped[int | None] = mapped_column(BigInteger)
    from_message_id: Mapped[int | None] = mapped_column(BigInteger)
    created_by: Mapped[int] = mapped_column(BigInteger)
    # Optimistic lock for `If-Match` (412 on a stale move): bump on every change.
    version: Mapped[int] = mapped_column(Integer, server_default=sql_text("1"))
    # A retried offline create must not make two rows (XREQ-011 item 2); optional so the web app needs no change.
    idempotency_key: Mapped[uuid.UUID | None] = mapped_column(Uuid)
    created_at: Mapped[CreatedAt]
    updated_at: Mapped[Timestamp] = mapped_column(server_default=sql_text("now()"))


class TaskAssignee(Base):
    """A join table rather than an id array, so the database refuses an assignee from another shop."""

    __tablename__ = "task_assignees"
    __table_args__ = (
        shop_fk("task_id", "tasks", ondelete="CASCADE"),
        shop_fk("user_id", "users"),
        Index("ix_task_assignees_shop_user", "shop_id", "user_id"),
    )

    task_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    user_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    shop_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("shops.id"))
