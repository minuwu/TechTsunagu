"""shops: an owner may choose a memorable shop code of 6 to 12 letters or digits

Revision ID: 8e3b5d0a2f41
Revises: 7d2f4a9c1e30
Create Date: 2026-11-24 15:00:00.000000

"""

from collections.abc import Sequence

from alembic import op

# revision identifiers, used by Alembic.
revision: str = "8e3b5d0a2f41"
down_revision: str | Sequence[str] | None = "7d2f4a9c1e30"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.drop_constraint("code_format", "shops", type_="check")
    op.create_check_constraint("code_format", "shops", "code ~ '^[A-Z0-9]{6,12}$'")


def downgrade() -> None:
    op.drop_constraint("code_format", "shops", type_="check")
    op.create_check_constraint("code_format", "shops", "code ~ '^[A-Z0-9]{8}$'")
