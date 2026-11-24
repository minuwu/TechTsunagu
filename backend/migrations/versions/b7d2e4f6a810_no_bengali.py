"""no Bengali (ADR 0016): languages lose 'bn'; a new shop defaults to English

Rows still in Bengali are moved to English first, so the stricter checks can be added on live data.

Revision ID: b7d2e4f6a810
Revises: 9a4c6e2b8d13
Create Date: 2026-11-24 22:00:00.000000

"""

from collections.abc import Sequence

from alembic import op

# revision identifiers, used by Alembic.
revision: str = "b7d2e4f6a810"
down_revision: str | Sequence[str] | None = "9a4c6e2b8d13"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

OLD = "('ja','bn','en','vi','id','ne','my','tl','zh')"
NEW = "('ja','en','vi','id','ne','my','tl','zh')"
# (table, column, constraint name) of every language column
COLUMNS = [
    ("users", "language", "ck_users_language"),
    ("messages", "language", "ck_messages_language"),
    ("asks", "language", "ck_asks_language"),
    ("checks", "language", "ck_checks_language"),
    ("contact_messages", "src_lang", "ck_contact_messages_src_lang"),
    ("talk_entries", "meaning_lang", "ck_talk_entries_meaning_lang"),
]
SHOP_CHECK = "ck_shops_languages_supported"


def _shop_check(langs: str) -> str:
    return f"languages <@ ARRAY[{langs}]::text[] AND cardinality(languages) BETWEEN 1 AND 4"


def upgrade() -> None:
    for table, column, name in COLUMNS:
        # table and column names are constants in this file, no user input
        op.execute(f"UPDATE {table} SET {column} = 'en' WHERE {column} = 'bn'")  # noqa: S608
        op.drop_constraint(op.f(name), table, type_="check")
        op.create_check_constraint(op.f(name), table, f"{column} IN {NEW}")
    op.execute("UPDATE shops SET languages = array_remove(languages, 'bn') WHERE 'bn' = ANY(languages)")
    op.execute("UPDATE shops SET languages = '{en}' WHERE cardinality(languages) = 0")
    op.drop_constraint(op.f(SHOP_CHECK), "shops", type_="check")
    op.create_check_constraint(op.f(SHOP_CHECK), "shops", _shop_check("'en','vi','id','ne','my','tl','zh'"))
    op.alter_column("shops", "languages", server_default="{en}")


def downgrade() -> None:
    op.alter_column("shops", "languages", server_default="{bn,en}")
    op.drop_constraint(op.f(SHOP_CHECK), "shops", type_="check")
    op.create_check_constraint(op.f(SHOP_CHECK), "shops", _shop_check("'bn','en','vi','id','ne','my','tl','zh'"))
    for table, column, name in COLUMNS:
        op.drop_constraint(op.f(name), table, type_="check")
        op.create_check_constraint(op.f(name), table, f"{column} IN {OLD}")
