# Database on Supabase (runbook)

Owner: Anim (database). The API stays on Azure Container Apps (Asif); only the database moves.
No secrets in this file: passwords live in the Supabase dashboard and in Azure Container App secrets.

## The project

| | |
| :--- | :--- |
| Project | `wakatta`, ref `jpajxyejcbtfivrylcaf`, org "anim710's Org", **free plan** ($0) |
| Region | `ap-northeast-1` (Tokyo, next to Azure `japaneast`) |
| Postgres | 17.12 (CI and local run 16; the migrations were rehearsed on 17, see below) |
| Database | Supabase's own `postgres` database, schema `public` (the pooler serves `postgres`) |
| Pooler (use this) | `aws-0-ap-northeast-1.pooler.supabase.com:5432`, **session mode**, IPv4. `aws-1-…` does not know this project. |
| Direct host | `db.jpajxyejcbtfivrylcaf.supabase.co:5432` is IPv6-only on the free plan: Azure cannot reach it. Do not use it. |
| TLS | `sslmode=verify-full` against Supabase's root CA in `backend/certs/` (in the image; Asif's `e90c74e`); checked from here: the Tokyo pooler's chain and hostname validate |
| Limits | `max_connections` 60; the app pool is at most 10 (`app/db.py`) plus one owner connection at boot |

Free-plan facts to remember: a project with no traffic for 7 days is paused (restore it in the dashboard; the demo is
24–26 Nov); there is no point-in-time recovery, so the demo data is what `python -m app.seed --reset` makes.

## Roles

Same two roles as everywhere else (architecture §6, `jobs/dbinit.py`):

- `wakatta_owner`: owns every table, runs Alembic and the seed. On Supabase it gets `USAGE, CREATE` on `public`
  (Supabase keeps `public` owned by `pg_database_owner`, and its tooling expects that).
- `wakatta_app`: the API; DML only (the migration's grants), `statement_timeout 5s`,
  `idle_in_transaction_session_timeout 30s`, `TimeZone UTC`.
- Supabase's Data API roles `anon` and `authenticated` have **no** access to `public` (dbinit revokes it). The app
  never uses the Data API or the API keys. Recommended: also switch the Data API off in the dashboard
  (Project Settings → Data API).

Pooler usernames carry the project ref: `wakatta_app.jpajxyejcbtfivrylcaf`, `wakatta_owner.jpajxyejcbtfivrylcaf`,
`postgres.jpajxyejcbtfivrylcaf`.

## State now (2026-11-23, Anim's rehearsal)

Done on the project with throwaway passwords, then the passwords were set to NULL and the local copies deleted:

- roles created as `jobs/dbinit.py` does on Supabase; logins through the pooler work for both custom roles,
  and the role settings arrive (`TimeZone UTC`, `statement_timeout 5s`);
- `alembic upgrade head` → `downgrade base` → `upgrade head` clean on Postgres 17; head `c10c87c5bd34`;
- `python -m app.seed --reset` loaded (59 rows by key) with a **throwaway PIN pepper**: the demo PINs will not work until
  the cutover below reseeds with its own pepper;
- the API ran locally against the pooler as `wakatta_app`: `scripts/smoke.py` 13/13 PASS (health, demo sign-in, board,
  Skill Matrix, Notices without the answer key, Events, Chat poll 304, Task Board, Senpai link, Support Log CSV,
  Near-miss list, CORS preflight);
- checked in SQL: 35 tables all owned by `wakatta_owner`; `anon` / `authenticated` have no schema USAGE and SELECT on 0
  tables; `wakatta_app` cannot TRUNCATE anything or UPDATE `audit_events`;
- Supabase security advisor: no findings. Performance advisor: INFO only (composite foreign keys without their own
  index; unused indexes on a new database).

Right now `wakatta_owner` and `wakatta_app` have **no password** (they cannot log in) until `use-supabase.ps1` sets them.

## Cutover (Asif, Azure): `deploy/use-supabase.ps1`

Asif's script does it from his machine, through the session pooler, with nothing printed:
`jobs/dbinit.py` (fresh role passwords, `DB_NAME=postgres`) → `alembic upgrade head` → `python -m app.seed --reset`
(new PIN pepper) → Container App secrets `database-url`, `migration-database-url`, `pin-pepper` → new revision → a
roster call.

1. **Anim** resets the database password (Dashboard → Project Settings → Database → Reset database password; nobody
   knows the one generated at creation) and gives it to Asif privately. The Supabase API keys are not needed.
2. **Asif** puts the Session pooler string in his `backend/.env` as `SUPABASE_DB_URL`
   (`postgresql://postgres.jpajxyejcbtfivrylcaf:<db password>@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres`;
   `aws-0`, not `aws-1`) and runs `powershell -ExecutionPolicy Bypass -File backend\deploy\use-supabase.ps1`.
3. Check: `uv run python scripts/smoke.py https://<api-host> --origin https://<frontend-origin>` → all PASS.
4. When green: delete the Azure PostgreSQL server (decided 2026-11-23: Asif deletes it after cutover).

Rollback before step 4: set the two database secrets back to the Azure server's URLs and start a new revision.

## Checks to rerun any time (SQL editor or `execute_sql`)

```sql
with t as (select c.oid from pg_class c join pg_namespace n on n.oid = c.relnamespace
           where n.nspname = 'public' and c.relkind = 'r')
select has_schema_privilege('anon', 'public', 'USAGE')          as anon_usage,      -- false
       (select count(*) from t where has_table_privilege('anon', t.oid, 'SELECT')) as anon_tables, -- 0
       (select version_num from public.alembic_version)          as alembic_head;
```
