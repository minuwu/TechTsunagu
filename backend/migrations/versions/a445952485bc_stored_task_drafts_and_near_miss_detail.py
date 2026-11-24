"""stored task drafts, near miss detail, override key

Revision ID: a445952485bc
Revises: f440ca5739ea
Create Date: 2026-11-24 06:37:20.736836

"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision: str = "a445952485bc"
down_revision: str | Sequence[str] | None = "f440ca5739ea"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Upgrade schema."""
    op.add_column(
        "messages",
        sa.Column(
            "task_drafts",
            postgresql.JSONB(astext_type=sa.Text()),
            server_default=sa.text("'[]'::jsonb"),
            nullable=False,
        ),
    )
    op.add_column("near_miss_reports", sa.Column("photo_mark_x", sa.Double(), nullable=True))
    op.add_column("near_miss_reports", sa.Column("photo_mark_y", sa.Double(), nullable=True))
    op.add_column("near_miss_reports", sa.Column("severity", sa.Text(), nullable=True))
    op.add_column("near_miss_reports", sa.Column("task_id", sa.BigInteger(), nullable=True))
    op.create_foreign_key(
        op.f("fk_near_miss_reports_shop_id_task_id_tasks"),
        "near_miss_reports",
        "tasks",
        ["shop_id", "task_id"],
        ["shop_id", "id"],
    )
    op.add_column("panel_overrides", sa.Column("idempotency_key", sa.Uuid(), nullable=True))
    op.create_unique_constraint(
        op.f("uq_panel_overrides_shop_id_idempotency_key"), "panel_overrides", ["shop_id", "idempotency_key"]
    )
    # Autogenerate does not see CHECK constraints.
    op.create_check_constraint(
        op.f("ck_near_miss_reports_severity"), "near_miss_reports", "severity IN ('low','mid','high')"
    )
    op.create_check_constraint(
        op.f("ck_near_miss_reports_photo_mark"),
        "near_miss_reports",
        "(photo_mark_x IS NULL) = (photo_mark_y IS NULL)"
        " AND photo_mark_x BETWEEN 0 AND 1 AND photo_mark_y BETWEEN 0 AND 1",
    )


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_constraint(op.f("uq_panel_overrides_shop_id_idempotency_key"), "panel_overrides", type_="unique")
    op.drop_column("panel_overrides", "idempotency_key")
    op.drop_constraint(op.f("ck_near_miss_reports_photo_mark"), "near_miss_reports", type_="check")
    op.drop_constraint(op.f("ck_near_miss_reports_severity"), "near_miss_reports", type_="check")
    op.drop_constraint(op.f("fk_near_miss_reports_shop_id_task_id_tasks"), "near_miss_reports", type_="foreignkey")
    op.drop_column("near_miss_reports", "task_id")
    op.drop_column("near_miss_reports", "severity")
    op.drop_column("near_miss_reports", "photo_mark_y")
    op.drop_column("near_miss_reports", "photo_mark_x")
    op.drop_column("messages", "task_drafts")
