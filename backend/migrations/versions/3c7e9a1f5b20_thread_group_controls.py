"""thread group controls (XREQ-019): description, announce-only, locked, create key; member role, history start,
personal flags; system messages

Revision ID: 3c7e9a1f5b20
Revises: a445952485bc
Create Date: 2026-11-24 09:00:00.000000

"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision: str = "3c7e9a1f5b20"
down_revision: str | Sequence[str] | None = "a445952485bc"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

OLD_KINDS = "kind IN ('voice','text','ask','reexplain')"
NEW_KINDS = "kind IN ('voice','text','ask','reexplain','system')"


def upgrade() -> None:
    """Additive only, with defaults: rows and code from before keep working (expand)."""
    op.add_column("threads", sa.Column("description", sa.Text(), server_default="", nullable=False))
    op.add_column("threads", sa.Column("announce_only", sa.Boolean(), server_default=sa.false(), nullable=False))
    op.add_column("threads", sa.Column("locked", sa.Boolean(), server_default=sa.false(), nullable=False))
    op.add_column("threads", sa.Column("idempotency_key", sa.Uuid(), nullable=True))
    op.create_unique_constraint(
        op.f("uq_threads_shop_id_created_by_idempotency_key"), "threads", ["shop_id", "created_by", "idempotency_key"]
    )
    op.create_check_constraint(op.f("ck_threads_description_length"), "threads", "char_length(description) <= 500")

    op.add_column("thread_members", sa.Column("role", sa.Text(), server_default="member", nullable=False))
    # Null = sees the whole history (founding members and every row from before); set when added later.
    op.add_column("thread_members", sa.Column("history_from", sa.DateTime(timezone=True), nullable=True))
    for flag in ("muted", "pinned", "archived"):
        op.add_column("thread_members", sa.Column(flag, sa.Boolean(), server_default=sa.false(), nullable=False))
    op.create_check_constraint(op.f("ck_thread_members_role"), "thread_members", "role IN ('admin','member')")
    # Every Thread so far is run by its Foremen: they become its admins.
    op.execute(
        "UPDATE thread_members SET role = 'admin' FROM users"
        " WHERE users.id = thread_members.user_id AND users.role = 'foreman'"
    )

    op.add_column("messages", sa.Column("event", postgresql.JSONB(astext_type=sa.Text()), nullable=True))
    op.drop_constraint(op.f("ck_messages_kind"), "messages", type_="check")
    op.create_check_constraint(op.f("ck_messages_kind"), "messages", NEW_KINDS)
    op.create_check_constraint(
        op.f("ck_messages_system_has_event"), "messages", "(kind = 'system') = (event IS NOT NULL)"
    )


def downgrade() -> None:
    op.execute("DELETE FROM messages WHERE kind = 'system'")
    op.drop_constraint(op.f("ck_messages_system_has_event"), "messages", type_="check")
    op.drop_constraint(op.f("ck_messages_kind"), "messages", type_="check")
    op.create_check_constraint(op.f("ck_messages_kind"), "messages", OLD_KINDS)
    op.drop_column("messages", "event")

    op.drop_constraint(op.f("ck_thread_members_role"), "thread_members", type_="check")
    for column in ("archived", "pinned", "muted", "history_from", "role"):
        op.drop_column("thread_members", column)

    op.drop_constraint(op.f("ck_threads_description_length"), "threads", type_="check")
    op.drop_constraint(op.f("uq_threads_shop_id_created_by_idempotency_key"), "threads", type_="unique")
    for column in ("idempotency_key", "locked", "announce_only", "description"):
        op.drop_column("threads", column)
