"""jobs/dbinit.py on a Supabase-shaped server: the admin is not a superuser, the database is the admin's own (as
Supabase's `postgres` is), `public` stays with pg_database_owner and the Data API roles have USAGE on it.

Needs a superuser to build that server, so it runs only when TEST_ADMIN_DATABASE_URL is set (a libpq URL to the
`postgres` database as a superuser, e.g. postgresql://postgres@127.0.0.1:5432/postgres locally or in CI)."""

import os
import runpy
from collections.abc import Iterator
from pathlib import Path

import psycopg
import pytest
from psycopg.conninfo import conninfo_to_dict, make_conninfo

SUPER_URL = os.environ.get("TEST_ADMIN_DATABASE_URL")
DBINIT = Path(__file__).resolve().parents[2] / "jobs" / "dbinit.py"
DB, ADMIN, OWNER, APP = "dbinit_supabase_sim", "dbinit_admin", "dbinit_owner", "dbinit_app"
DATA_API_ROLES = ("anon", "authenticated")
TEST_PW = "dbinit-test-only"  # a throwaway role in a throwaway database, dropped after the test

pytestmark = pytest.mark.skipif(SUPER_URL is None, reason="TEST_ADMIN_DATABASE_URL (a superuser) is not set")


def _drop(su: psycopg.Connection[tuple[object, ...]]) -> None:
    su.execute(f"DROP DATABASE IF EXISTS {DB} WITH (FORCE)")
    for role in (APP, OWNER, ADMIN):
        if su.execute("SELECT 1 FROM pg_roles WHERE rolname = %s", (role,)).fetchone():
            su.execute(f"DROP OWNED BY {role}")
            su.execute(f"DROP ROLE {role}")


@pytest.fixture
def supabase_like(monkeypatch: pytest.MonkeyPatch) -> Iterator[str]:
    assert SUPER_URL is not None
    with psycopg.connect(SUPER_URL, autocommit=True) as su:
        _drop(su)
        su.execute(f"CREATE ROLE {ADMIN} LOGIN CREATEROLE PASSWORD '{TEST_PW}'")
        su.execute(f"CREATE DATABASE {DB} OWNER {ADMIN}")
        for role in DATA_API_ROLES:
            if not su.execute("SELECT 1 FROM pg_roles WHERE rolname = %s", (role,)).fetchone():
                su.execute(f"CREATE ROLE {role} NOLOGIN")
    with psycopg.connect(make_conninfo(SUPER_URL, dbname=DB), autocommit=True) as su:
        su.execute("GRANT USAGE ON SCHEMA public TO anon, authenticated")  # what Supabase starts with
    host = conninfo_to_dict(SUPER_URL).get("host", "127.0.0.1")
    admin_url = make_conninfo(SUPER_URL, user=ADMIN, password=TEST_PW, dbname=DB, host=host)
    for key, value in {
        "ADMIN_DATABASE_URL": admin_url,
        "ADMIN_USER": ADMIN,
        "OWNER_PASSWORD": TEST_PW,
        "APP_PASSWORD": TEST_PW,
        "OWNER_ROLE": OWNER,
        "APP_ROLE": APP,
        "DB_NAME": DB,
    }.items():
        monkeypatch.setenv(key, value)
    yield admin_url
    with psycopg.connect(SUPER_URL, autocommit=True) as su:
        _drop(su)


def test_on_supabase_the_owner_can_build_tables_and_the_data_api_roles_cannot_see_them(supabase_like: str) -> None:
    runpy.run_path(str(DBINIT), run_name="__main__")
    runpy.run_path(str(DBINIT), run_name="__main__")  # safe to re-run

    owner_url = make_conninfo(supabase_like, user=OWNER, password=TEST_PW)
    with psycopg.connect(owner_url, autocommit=True) as owner:
        owner.execute("CREATE TABLE probe (id int)")
        owner.execute(f"GRANT SELECT ON probe TO {APP}")
    with psycopg.connect(make_conninfo(supabase_like, user=APP, password=TEST_PW)) as app:
        zone = app.execute("SHOW TimeZone").fetchone()
        assert zone is not None and zone[0] == "UTC"
        assert app.execute("SELECT count(*) FROM probe").fetchone() == (0,)
    with psycopg.connect(supabase_like) as admin:
        schema_owner = admin.execute("SELECT nspowner::regrole::text FROM pg_namespace WHERE nspname = 'public'")
        assert schema_owner.fetchone() == ("pg_database_owner",)  # left as Supabase expects
        for role in (*DATA_API_ROLES, "public"):
            usage = admin.execute("SELECT has_schema_privilege(%s, 'public', 'USAGE')", (role,)).fetchone()
            assert usage == (False,), role
