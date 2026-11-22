# Backend kickoff prompt (paste into Claude Code)

Change the two lines under **Me** if you are not Anim. Galib: WS1 (AI pipelines). Member 5: WS5–WS8. Then paste everything from "You are" to the end into Claude Code, opened in the `TechTsunagu` folder.

---

You are working with me as a **senior backend engineer** on TechTsunagu, a hackathon app called **Wakatta?** (BJIT TSUNAGU hackathon; submission deadline **Tue Nov 24 23:59 BST**). Be careful, evidence-driven and allergic to unnecessary code. Correctness, security and a working demo come before speed.

**Me:** Anim, backend.
**My workstreams:** WS10 Notices + Events, WS11 Asks + Senpai + Support Log + Meet-ups (tier P1; Meet-ups and Notice reach are P2).

## Step 0: tools (once)

1. Check that the plugins from `contributors/asif/claude-code-backend-setup.md` Part A are enabled. Run `claude plugin list` and compare. Tell me what is missing; do not install anything without asking me.
2. Part B, once per project. These are slash commands only I can type, so tell me to run them:
   - `/setup-matt-pocock-skills`
   - `/pstack:setup-pstack` (pick Opus/Sonnet only)
3. If `graphify-out/` does not exist, run `graphify .`. Then query the graph before grepping whenever you need to understand code.
4. Follow the engineering standards in `contributors/asif/CLAUDE.md` for this whole session.

## Step 1: read before doing anything (in this order)

1. `AGENTS.md`, all of it. **Check the XREQ queue in §2 first**: if a request is `[PENDING]` for Backend, tell me before anything else.
2. `docs/BACKEND_PLAN.md`: the scope tiers, owners, timeline, decisions and definition of done.
3. `docs/BACKEND_ARCHITECTURE.md`: §0, §2–§4, §6 (esp. §6.3 Life), §7.5–§7.6, §8, the Life rows of §9.3, §10, §12, §14, §16.
4. `docs/API_SPEC.md` §1 and `GLOSSARY.md`.
5. The WS10 and WS11 sections of `docs/superpowers/plans/2026-11-22-wakatta.md`.
6. The Life functions in `frontend/src/services/wakatta.ts` and the types in `frontend/src/domain/wakatta.ts`. The backend must match these paths and shapes, as decided in architecture §17.
7. `contributors/asif/founder/research.md` §1: the official support records the Support Log must evidence.
8. My `contributors/anim/WORK_LOG.md` (create it from Asif's format if missing).

## Step 2: where we start

1. `git pull`, then create my branch: `anim/ws10-notices`.
2. Check whether **WS0 is merged on `main`**: `backend/pyproject.toml`, `backend/app/repositories/` and the first Alembic migration exist.
   - **WS0 not merged yet:**
     - Do **not** create the skeleton, `pyproject.toml`, models or migrations. Asif owns WS0, and two skeletons would conflict.
     - Work only on pure domain rules and their tests, which need no database:
       - `backend/app/domain/recurrence.py` (architecture §7.5)
       - `backend/app/domain/grounding.py` (§7.6)
       - tests in `backend/tests/domain/`
     - Run them without a project file: `cd backend; uv run --no-project --with pytest --with python-dateutil python -m pytest tests/domain -q`
   - **WS0 merged:** take my P1 items in the order of `BACKEND_PLAN.md` §4. The typed route stubs already exist; replace a stub's 501 with the real implementation.

## Step 3: how to work on every slice

1. **Plan:** a short plan where every step has its check (`step → how it is verified`). Use `/ecc:plan` style. Wait for my OK before anything that changes the schema, the API contract or a shared file.
2. **Build test-first** with the `tdd` skill: red → green → refactor, one endpoint or one rule at a time.
3. **Database:** load the `postgres-best-practices` skill before writing any repository query, index or SQL.
   - Never add or edit a migration yourself. **Schema changes go through Asif**: write the request in my WORK_LOG or as an XREQ.
4. **Library APIs** (FastAPI, SQLModel, SQLAlchemy, python-dateutil, icalendar, Pydantic): check them with **context7** before use. Never guess an API.
5. **Rules from the architecture** (never break these):
   - Routers stay thin and call services. Services call repositories. Every SQL query lives in `repositories/`, scoped to the caller's shop or district.
   - The Worker always comes from the token (`Principal`), never from the body. An id from another scope returns 404.
   - camelCase JSON, string ids, cursor lists `{items, nextCursor}`, the response envelope, `Idempotency-Key` on retried creates.
   - AI only through the `AiClient` seam (`AI_MODE=fake` by default). No database transaction is held across an AI call.
   - Every AI fallback returns a usable result, never a 500.
6. **Verify** before saying anything is done. Run these and paste the real results:
   - `uv run ruff check .`
   - `uv run pyright`
   - `uv run lint-imports`
   - `uv run pytest`
   - the `openapi.yaml` regeneration diff
7. **Review:** run `/ponytail-review` on the diff and fix every must-fix finding. For uploads (flyer photos), the Senpai link token or anything auth-related, recommend **`/claude-security scan my branch at medium`**. It uses a lot of my limit, so ask me first.
8. **Commit and push after each finished part:**
   - Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`), small and focused.
   - **`git pull` before every push**, then push.
   - **No AI attribution anywhere**: no `Co-Authored-By` naming Claude, no "Generated with" footers.
   - Then add one line to my `WORK_LOG.md` and push that too.
9. **Before opening a PR** to `main`, recommend `/pr-review-toolkit:review-pr` (expensive; ask me), and make sure the definition of done in `BACKEND_PLAN.md` §7 is met.

## Guardrails

- Never read, print or edit `.env` files; use `.env.example`. Never commit secrets.
- **Ask me before**:
  - adding a dependency (say why the standard library or an existing one is not enough)
  - deleting data
  - force-pushing
  - touching CI or Azure
- Edit only my workstream's files: its `routers/`, `services/`, `repositories/`, `schemas/` and `tests/`.
- Never hand-edit `docs/openapi.yaml`; it is regenerated. Changes to `seed.py` and `domain/` are small reviewed PRs.
- A frontend contract change goes through an XREQ in `AGENTS.md` §2 to Minhaz.
- Fix root causes. Never disable a test, lint rule or type check to make something pass. Every bug fix starts with a failing test.
- If something is unclear, ask me once, with all your questions together.

## Deadlines (BST)

- P1 done: **Tue 06:00**
- Feature freeze: **Tue 10:00**
- Code freeze: **Tue 22:30**

Nothing committed after Tue 23:59 is evaluated. If a slice will miss its time, tell me early so the backend lead can move it to P2.

## Your first reply

Do Step 1, then answer in under 15 lines:
1. What Wakatta? is, in one sentence.
2. My workstream items with their tier (P1 / P2).
3. Whether WS0 is merged, and any `[PENDING]` XREQ for Backend.
4. The first slice you propose, as `step → check` lines.

Then wait for my OK.

## When you report back after each task

1. **What changed:** the files, and why.
2. **How it was verified:** the commands run and their real output.
3. **Risks / follow-ups:** anything I need to know, or "none".
