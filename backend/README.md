# backend/

Plan (scope tiers, owners, timeline, guide checklist): [docs/BACKEND_PLAN.md](../docs/BACKEND_PLAN.md). Design (layout, data model, rules, API, AI pipeline, security, deploy): [docs/BACKEND_ARCHITECTURE.md](../docs/BACKEND_ARCHITECTURE.md). Code lands in WS0.

## Run (Python 3.12 + [uv](https://docs.astral.sh/uv/))

```bash
cd backend
cp .env.example .env            # then edit values; never commit .env
uv sync                         # installs the locked versions from uv.lock
uv run uvicorn app.main:create_app --factory --no-access-log --reload   # http://localhost:8000/api/health
uv run pytest                   # tests
uv run ruff check . && uv run ruff format --check . && uv run pyright   # lint, format, types (strict)
```

API docs while running: `http://localhost:8000/api/docs`.

### Local PostgreSQL (no Docker needed)

Any PostgreSQL 16 works. On Windows without Docker, the official [EDB binaries zip](https://www.enterprisedb.com/download-postgresql-binaries) unzipped to a folder is enough:

```powershell
$bin="E:\tools\pgsql\bin"; $data="E:\tools\pgdata-wakatta"
& "$bin\initdb.exe" -D $data -U postgres --auth=trust --encoding=UTF8 --locale=C   # local only
& "$bin\pg_ctl.exe" -D $data -l "$data.log" -o "-c listen_addresses=127.0.0.1" start
```

Then, as `postgres`, create the two roles and databases (tests use `wakatta_test`):

```sql
CREATE ROLE wakatta_owner LOGIN PASSWORD 'wakatta_owner_dev';
CREATE ROLE wakatta_app LOGIN PASSWORD 'wakatta_app_dev';
ALTER ROLE wakatta_app SET statement_timeout = '5s';
ALTER ROLE wakatta_app SET idle_in_transaction_session_timeout = '30s';
CREATE DATABASE wakatta OWNER wakatta_owner;
CREATE DATABASE wakatta_test OWNER wakatta_owner;
-- in each database:
REVOKE ALL ON SCHEMA public FROM PUBLIC; ALTER SCHEMA public OWNER TO wakatta_owner; GRANT USAGE ON SCHEMA public TO wakatta_app;
```

Migrations: `uv run alembic upgrade head` (uses `MIGRATION_DATABASE_URL`, the owner). The test suite rebuilds `wakatta_test` itself and runs the migration up, down and up again.

Conventions (AGENTS.md §4.2, §4.3):
- `app/routers/` call `app/services/`, never SQL or models directly.
- `app/prompts/` holds AI prompts, versioned in git; every AI call has a deterministic fallback.
- CORS on from the first commit; `GET /api/health` is the first route.
- Every response uses the envelope in [docs/API_SPEC.md §1](../docs/API_SPEC.md#1-conventions).
- Commit `.env.example`; never `.env`.
