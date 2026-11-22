# backend/

Plan (scope tiers, owners, timeline, guide checklist): [docs/BACKEND_PLAN.md](../docs/BACKEND_PLAN.md). Design (layout, data model, rules, API, AI pipeline, security, deploy): [docs/BACKEND_ARCHITECTURE.md](../docs/BACKEND_ARCHITECTURE.md). Code lands in WS0.

Conventions (AGENTS.md §4.2, §4.3):
- `app/routers/` call `app/services/`, never SQL or models directly.
- `app/prompts/` holds AI prompts, versioned in git; every AI call has a deterministic fallback.
- CORS on from the first commit; `GET /api/health` is the first route.
- Every response uses the envelope in [docs/API_SPEC.md §1](../docs/API_SPEC.md#1-conventions).
- Commit `.env.example`; never `.env`.
