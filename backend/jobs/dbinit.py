"""Creates the least-privilege roles (and, on a server of our own, the database). Run as the server's admin:
by backend/deploy/use-supabase.ps1 for Supabase, or by jobs/start.py on first boot. Safe to re-run: roles are altered,
not duplicated, and passwords are rotated to the values given.

Environment (all secrets, none printed): ADMIN_DATABASE_URL (libpq URL to the `postgres` database), OWNER_PASSWORD,
APP_PASSWORD. Optional names: OWNER_ROLE, APP_ROLE, DB_NAME (default wakatta; `postgres` on Supabase, which shares the
project's database)."""

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
        created = not conn.execute("SELECT 1 FROM pg_database WHERE datname = %s", (DB,)).fetchone()
        if created:
            conn.execute(sql.SQL("CREATE DATABASE {} OWNER {}").format(_ident(DB), _ident(OWNER)))

    with psycopg.connect(make_conninfo(admin_url, dbname=DB), autocommit=True) as conn:
        # The owner creates the tables (managed servers do not always give `public` to the database owner); the app
        # role only uses them. Tables belong to the owner, so platform roles such as Supabase's anon get nothing.
        conn.execute(sql.SQL("GRANT USAGE, CREATE ON SCHEMA public TO {}").format(_ident(OWNER)))
        conn.execute(sql.SQL("GRANT USAGE ON SCHEMA public TO {}").format(_ident(APP)))
        # Nobody else needs the schema, also on a shared database: on Supabase, PUBLIC's USAGE would hand `public` to
        # the Data API roles, and a public anon key must not reach it even if a later table were created by a role
        # whose default privileges grant them access. Supabase's own services live in their own schemas; revoking
        # was rehearsed on the real project with the advisors clean (deploy/SUPABASE.md).
        conn.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC")
        for role in ("anon", "authenticated"):
            if conn.execute("SELECT 1 FROM pg_roles WHERE rolname = %s", (role,)).fetchone():
                conn.execute(sql.SQL("REVOKE ALL ON SCHEMA public FROM {}").format(_ident(role)))
        # The app's own UTC startup option may not pass a pooler, so the role carries it too (§6: timestamps are UTC).
        conn.execute(sql.SQL("ALTER ROLE {} SET TimeZone = 'UTC'").format(_ident(APP)))
    print(f"roles {OWNER} and {APP} and database {DB} are ready")  # noqa: T201  # a job's log line


if __name__ == "__main__":
    main()
