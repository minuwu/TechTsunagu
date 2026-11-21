# backend/

Empty until kickoff. Stack is chosen at Hour 0 ([HACKATHON_PRESETUP.md §2](../HACKATHON_PRESETUP.md#2-recommended-tech-stacks)).

Conventions (AGENTS.md §4.2, §4.3):
- `app/routers/` call `app/services/`, never SQL or models directly.
- `app/prompts/` holds AI prompts, versioned in git; every AI call has a deterministic fallback.
- CORS on from the first commit; `GET /api/health` is the first route.
- Every response uses the envelope in [docs/API_SPEC.md §1](../docs/API_SPEC.md#1-conventions).
- Commit `.env.example`; never `.env`.
