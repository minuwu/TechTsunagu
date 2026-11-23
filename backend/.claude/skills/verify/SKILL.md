---
name: verify
description: Run the Wakatta? API locally against a throwaway seeded database and drive changed routes over HTTP.
---

# Verify the backend API (runtime, not tests)

Local PostgreSQL 16 from README ("Local PostgreSQL"); start it if port 5432 is closed:
`E:\tools\pgsql\bin\pg_ctl.exe -D E:\tools\pgdata-wakatta -l E:\tools\pgdata-wakatta.log -o "-c listen_addresses=127.0.0.1" -w start`

```bash
cd backend; P=/e/tools/pgsql/bin/psql.exe
$P -h 127.0.0.1 -U postgres -d postgres -q -c "DROP DATABASE IF EXISTS wakatta_verify" -c "CREATE DATABASE wakatta_verify OWNER wakatta_owner"
$P -h 127.0.0.1 -U postgres -d wakatta_verify -q -c "REVOKE ALL ON SCHEMA public FROM PUBLIC; ALTER SCHEMA public OWNER TO wakatta_owner; GRANT USAGE ON SCHEMA public TO wakatta_app;"
export MIGRATION_DATABASE_URL=postgresql+psycopg://wakatta_owner:wakatta_owner_dev@127.0.0.1:5432/wakatta_verify PIN_PEPPER=verify-pepper-0123456789abcdefghijklmnop
.venv/Scripts/alembic upgrade head && PYTHONIOENCODING=utf-8 .venv/Scripts/python -m app.seed
DATABASE_URL=postgresql+psycopg://wakatta_app:wakatta_app_dev@127.0.0.1:5432/wakatta_verify FRONTEND_ORIGIN=http://localhost:3000 AI_MODE=fake \
  .venv/Scripts/uvicorn app.main:create_app --factory --host 127.0.0.1 --port 8765 > /tmp/wakatta-verify.log 2>&1 &
```

Drive it:
- Roster: `GET /api/auth/roster?code=SASEBO01&role=foreman|worker` (needs both params).
- Login: `POST /api/auth/login {"userId":"1","pin":"246810"}` (Foreman 山口 隆); Worker Rahim is id 3, PIN 2468. Token is `data.token`, send as `Authorization: Bearer`.
- Machines 1–4 are SASEBO01; machine 5+ is ISAHAYA2 (use for cross-shop 404 probes).
- Contract check: compare live `data` keys with `components.schemas` in `../docs/openapi.yaml` (there is no live `/openapi.json`).

Gotchas:
- Windows Python cannot open `/tmp/...`; pass `$(cygpath -w /tmp)`.
- A 500's traceback is only in the uvicorn log, never in the response.
- Clean up: stop the process on port 8765 and `DROP DATABASE wakatta_verify WITH (FORCE)`.
