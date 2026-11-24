"""personal: a replaced My QR answers "replaced", not "not found"

Revision ID: 7d2f4a9c1e30
Revises: 5b1e0c7d92aa
Create Date: 2026-11-24 14:10:00.000000

"""

import os
from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

# revision identifiers, used by Alembic.
revision: str = "7d2f4a9c1e30"
down_revision: str | Sequence[str] | None = "5b1e0c7d92aa"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Upgrade schema."""
    op.create_table(
        "retired_qr_tokens",
        sa.Column("token_hash", sa.LargeBinary(), nullable=False),
        sa.Column("user_id", sa.BigInteger(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.ForeignKeyConstraint(
            ["user_id"], ["users.id"], name=op.f("fk_retired_qr_tokens_user_id_users"), ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("token_hash", name=op.f("pk_retired_qr_tokens")),
    )
    op.create_index(op.f("ix_retired_qr_tokens_user_id"), "retired_qr_tokens", ["user_id"], unique=False)
    app_role = os.environ.get("APP_DB_ROLE", "wakatta_app")
    op.execute(f'GRANT SELECT, INSERT, UPDATE, DELETE ON retired_qr_tokens TO "{app_role}"')


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_index(op.f("ix_retired_qr_tokens_user_id"), table_name="retired_qr_tokens")
    op.drop_table("retired_qr_tokens")
