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

Conventions (AGENTS.md §4.2, §4.3):
- `app/routers/` call `app/services/`, never SQL or models directly.
- `app/prompts/` holds AI prompts, versioned in git; every AI call has a deterministic fallback.
- CORS on from the first commit; `GET /api/health` is the first route.
- Every response uses the envelope in [docs/API_SPEC.md §1](../docs/API_SPEC.md#1-conventions).
- Commit `.env.example`; never `.env`.
