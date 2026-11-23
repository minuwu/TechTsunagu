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
        conn.execute(sql.SQL("SET ROLE {}").format(_ident(OWNER)))
        conn.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC")
        conn.execute(sql.SQL("GRANT USAGE ON SCHEMA public TO {}").format(_ident(APP)))
    print(f"roles {OWNER} and {APP} and database {DB} are ready")


if __name__ == "__main__":
    main()
