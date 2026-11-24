"""machines: archived_at, a soft delete that keeps history

Revision ID: 9a4c6e2b8d13
Revises: 8e3b5d0a2f41
Create Date: 2026-11-24 18:00:00.000000

"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

# revision identifiers, used by Alembic.
revision: str = "9a4c6e2b8d13"
down_revision: str | Sequence[str] | None = "8e3b5d0a2f41"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column("machines", sa.Column("archived_at", sa.DateTime(timezone=True), nullable=True))


def downgrade() -> None:
    op.drop_column("machines", "archived_at")
