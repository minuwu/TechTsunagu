"""personal: message deletion, no personal texts in the AI cache

Revision ID: 5b1e0c7d92aa
Revises: a445952485bc
Create Date: 2026-11-24 00:40:00.000000

"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

# revision identifiers, used by Alembic.
revision: str = "5b1e0c7d92aa"
down_revision: str | Sequence[str] | None = "a445952485bc"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Upgrade schema."""
    op.add_column(
        "contact_messages",
        sa.Column("recipient_deleted", sa.Boolean(), server_default=sa.text("false"), nullable=False),
    )
    # Personal texts are no longer cached (a deleted message must not survive in ai_cache); nothing reads these rows.
    ai_cache = sa.table("ai_cache", sa.column("scope", sa.Text()))
    op.execute(ai_cache.delete().where(ai_cache.c.scope.like("user:%")))


def downgrade() -> None:
    """Downgrade schema. The purged cache rows were derived data and are not restored."""
    op.drop_column("contact_messages", "recipient_deleted")
