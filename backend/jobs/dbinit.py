"""Creates the database roles and the database. Run once as the server admin, inside Azure, by the wakatta-dbinit job
(`python /app/jobs/dbinit.py`), so no laptop needs to reach the database. Safe to re-run: roles are altered, not
duplicated, and passwords are rotated to the values given.

Environment (all secrets, none printed): ADMIN_DATABASE_URL (libpq URL to the `postgres` database), ADMIN_USER,
OWNER_PASSWORD, APP_PASSWORD. Optional names: OWNER_ROLE, APP_ROLE, DB_NAME (defaults match the app and the README)."""

import os

import psycopg
from psycopg import sql
from psycopg.conninfo import make_conninfo

OWNER = os.environ.get("OWNER_ROLE", "wakatta_owner")
APP = os.environ.get("APP_ROLE", "wakatta_app")
DB = os.environ.get("DB_NAME", "wakatta")


def _ident(name: str) -> sql.Identifier:
    return sql.Identifier(name)


def main() -> None:
    admin_url = os.environ["ADMIN_DATABASE_URL"]
    admin = os.environ["ADMIN_USER"]
    passwords = {OWNER: os.environ["OWNER_PASSWORD"], APP: os.environ["APP_PASSWORD"]}

    with psycopg.connect(admin_url, autocommit=True) as conn:
        for role, password in passwords.items():
            exists = conn.execute("SELECT 1 FROM pg_roles WHERE rolname = %s", (role,)).fetchone()
            verb = "ALTER" if exists else "CREATE"
            conn.execute(
                sql.SQL("{} ROLE {} LOGIN PASSWORD {}").format(sql.SQL(verb), _ident(role), sql.Literal(password))
            )
        # The API role has short limits so a runaway query cannot hold the small database (architecture §8).
        conn.execute(sql.SQL("ALTER ROLE {} SET statement_timeout = '5s'").format(_ident(APP)))
        conn.execute(sql.SQL("ALTER ROLE {} SET idle_in_transaction_session_timeout = '30s'").format(_ident(APP)))
        # Lets the admin act as the owner below (the managed server's admin is not a superuser).
        conn.execute(sql.SQL("GRANT {} TO {}").format(_ident(OWNER), _ident(admin)))
        if not conn.execute("SELECT 1 FROM pg_database WHERE datname = %s", (DB,)).fetchone():
            conn.execute(sql.SQL("CREATE DATABASE {} OWNER {}").format(_ident(DB), _ident(OWNER)))

    with psycopg.connect(make_conninfo(admin_url, dbname=DB), autocommit=True) as conn:
        prepare_schema(conn)
    print(f"roles {OWNER} and {APP} and database {DB} are ready")  # noqa: T201  # a job's log line


def prepare_schema(conn: psycopg.Connection[tuple[object, ...]]) -> None:
    """Let the owner create tables in `public` and give everyone else nothing but the app role's USAGE.

    Three hosts, three starting points:
    - stock PostgreSQL 16+: the database was created OWNER wakatta_owner, so it already owns `public`;
    - Azure Flexible Server: `public` belongs to the admin, so the owner is given the schema;
    - Supabase: the database is Supabase's own `postgres` (its pooler serves that one), `public` stays with
      pg_database_owner as Supabase's tooling expects, and the owner gets USAGE + CREATE instead. Supabase also grants
      `public` to its Data API roles (anon, authenticated): those grants are removed, because this API never uses the
      Data API and a public anon key must not reach our tables.
    """
    db_owner = _one(conn.execute("SELECT datdba::regrole::text FROM pg_database WHERE datname = current_database()"))
    schema_owner = _one(conn.execute("SELECT nspowner::regrole::text FROM pg_namespace WHERE nspname = 'public'"))
    if schema_owner == "pg_database_owner" and db_owner != OWNER:
        conn.execute(sql.SQL("GRANT USAGE, CREATE ON SCHEMA public TO {}").format(_ident(OWNER)))
    elif schema_owner != "pg_database_owner":
        conn.execute(sql.SQL("ALTER SCHEMA public OWNER TO {}").format(_ident(OWNER)))
    conn.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC")
    for role in ("anon", "authenticated"):
        if conn.execute("SELECT 1 FROM pg_roles WHERE rolname = %s", (role,)).fetchone():
            conn.execute(sql.SQL("REVOKE ALL ON SCHEMA public FROM {}").format(_ident(role)))
    conn.execute(sql.SQL("GRANT USAGE ON SCHEMA public TO {}").format(_ident(APP)))
    # The app's own UTC startup option may not pass a pooler, so the role carries it too (§6: timestamps are UTC).
    conn.execute(sql.SQL("ALTER ROLE {} SET TimeZone = 'UTC'").format(_ident(APP)))


def _one(cursor: psycopg.Cursor[tuple[object, ...]]) -> str:
    row = cursor.fetchone()
    if row is None:
        raise RuntimeError("expected one row")
    return str(row[0])


if __name__ == "__main__":
    main()
