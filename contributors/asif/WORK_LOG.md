# Asif's Work Log

Owner: Asif (`MdAsif-Hossain`) — Role: Backend (lead) / Services / Database

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- Moved to the recreated repo (same URL, `minuwu/TechTsunagu`, fresh history from Minhaz's `init`). Dropped my old local history and merged my copy into the new `main`; the only change carried over is this log entry. Commit hashes in the 2026-11-21 entry refer to the old, deleted history.

### Next
- Hour-0 backend scaffold (see the 2026-11-21 "Next" list).

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
