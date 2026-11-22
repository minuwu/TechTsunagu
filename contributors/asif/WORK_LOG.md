# Asif's Work Log

Owner: Asif (`MdAsif-Hossain`) — Role: Backend (lead) / Services / Database

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- Moved to the recreated repo (same URL, `minuwu/TechTsunagu`, fresh history from Minhaz's `init`). Dropped my old local history and merged my copy into the new `main`; the only change carried over is this log entry. Commit hashes in the 2026-11-21 entry refer to the old, deleted history.

- Reviewed the Wakatta? plan (backend focus). Team decision: keep every feature, 5 people, backend first, deploy on Azure after.
- Wrote the backend design: [docs/BACKEND_ARCHITECTURE.md](../../docs/BACKEND_ARCHITECTURE.md) and ADRs [0005](../../docs/adr/0005-bearer-session-tokens.md) (Bearer tokens), [0006](../../docs/adr/0006-code-first-openapi.md) (OpenAPI generated from FastAPI), [0007](../../docs/adr/0007-sync-fastapi-sqlmodel.md) (sync stack, inline AI), [0008](../../docs/adr/0008-ai-client-seam-and-fake.md) (AI seam + fake). Added **Presence** to GLOSSARY.md; updated the plan's auth, panel and data-model lines to match; ticked my Team review row.
- Plan review answers:
  1. I own **WS0** (foundation) and **WS2** (Notes, Checks, Sign-offs, Lock), and the Azure deploy in WS9.
  2. What makes a judge say no: a demo that breaks on a slow or rate-limited model (fixed: fallback ladder, fake AI by default, cache), a login cookie that fails on an iPhone (fixed: Bearer tokens), and a compliance claim we cannot source (the 特定技能 support wording stays in "Facts to verify").
  3. Nothing is cut. Risk control instead: WS0 lands all tables and typed route stubs first, so five people never edit the same migration or contract file.
  4. Fact I verify: Azure for Students covers PostgreSQL Flexible Server B1ms free for 12 months and Container Apps has a monthly free grant (Microsoft's Azure for Students and PostgreSQL pages); still to check: allowed regions for our subscription.

### Next
- Team reads BACKEND_ARCHITECTURE.md; then WS0 on `asif/ws0-foundation`: skeleton, all tables in the first migration, typed route stubs, generated openapi.yaml, AI seam with fake, seed, CI.
- Hello-world API on Azure Container Apps today.
- XREQ to Minhaz: Bearer token in `src/services/api.ts`, call `/machines/{id}/presence` after the name pick.

### Blockers / handoff
- For the team: re-clone the repo and run `scripts/setup.sh` (or `scripts\setup.ps1`) again; don't push from clones made before 2026-11-22.

## 2026-11-21

### Completed
- Cloned the repo, ran `scripts/setup.sh` (pre-push sync hook enabled via `core.hooksPath=.githooks`), started this work log (`7917a1b`).
- `.gitignore`: added local agent state (`.claude/settings.local.json`, `.claude/worktrees/`, `CLAUDE.local.md`, `.claude-mem/`) and Python tool caches (`.pyright/`, `.hypothesis/`, `.tox/`) (`34fbdb2`).
- Shared my backend docs for the team (`6a70f27`):
  - [CLAUDE.md](CLAUDE.md) — backend engineering standards (layering, API design, OWASP checks, migrations, testing) and the Definition of Done.
  - [claude-code-backend-setup.md](claude-code-backend-setup.md) — the dev tooling setup (review, security, TDD, Postgres best practices, Python LSP) and which command to use at each stage.
- Set up that tooling on my machine; verified every plugin shows enabled and pyright 1.1.414 runs.

### Next
- Hour-0: scaffold `backend/` (FastAPI expected) with CORS + `GET /api/health`, deploy hello-world to Render/Railway.
- Draft endpoints in `docs/API_SPEC.md` at kickoff using the §1 envelope.

### Blockers / handoff
- None. XREQ queue is empty.
- For Anim: if you want the same setup, follow Part A of [claude-code-backend-setup.md](claude-code-backend-setup.md); Part B runs once per project.

### Related issues / commits / PRs
- `7917a1b`, `34fbdb2`, `6a70f27`
