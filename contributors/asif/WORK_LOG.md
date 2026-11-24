# Asif's Work Log

Owner: Asif (`MdAsif-Hossain`) — Role: Backend (lead) / Services / Database

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-24 19:50 (from Arjun: S5 is on 1.0.3, after Anim's three steps)

- Minhaz released **1.0.3** (`80d58fc`, with the push fix `5425566`), not 1.0.2: test that build for S5. Its SHA-256 matches the committed `.sha256` (checked).
- Wait for Anim's three steps (my note in Anim's log): App Links fingerprints, publishing 1.0.3, the demo reset. The reset deletes the registered phones, so sign in on both phones after it, then run S5 with push.
- I could not run S5 myself: no phones here.

## 2026-11-24 — R4: a direct Thread has the other person's name (for Arjun)

### Changed
- `displayTitle` is mapped and used for every Thread name; the guess that replaced a Worker's own name with 職長 is gone from `ChatWire.kt` (`ChatsViewModel` still has its own).
- The empty bubble and `fromDisplayName` were already fixed by Minhaz; both now have a test.

### Verified (local; not run on a phone)
- The new title test failed first, then passed. `testDebugUnitTest checkModuleGraph :app:assembleDebug`: BUILD SUCCESSFUL, 896 tests, 0 failed.

### Waiting for Minhaz and Anim
- Open registration and the AI allowance (ADR 0017): my proposal is in both of your logs. I change nothing until you answer.

## 2026-11-24 — R3: the AI draft can wait for the Foreman (for Arjun)

### Changed
- `sendMessage` only posts: a Foreman's message comes back as a draft (`isDraft`, `needsEdit` in `ChatMessageUiModel`). New `ChatRepository.sendDraft(id)` and `ChatEvent.SendDraft(id)`; a double tap sends once; `ALREADY_SENT` counts as sent.
- A draft left from an earlier timeout arrives with the poll and can be sent or edited, with its questions and Tasks.
- **Nothing changes for the Foreman yet:** without a Send button on screen, the app still sends at once. M2 sets `reviewDrafts` in `ChatViewModel` to true; from then on no draft, good or `needsEdit`, goes out without `SendDraft`.

### Verified (local; not run on a phone)
- `testDebugUnitTest checkModuleGraph :app:assembleDebug`: BUILD SUCCESSFUL, 894 tests (85 in chat), 0 failed. 8 new tests; two of them fail when the review guard is removed.

## 2026-11-24 — Both mobile contract docs match the API again (for Minhaz)

- `MOBILE_API_CONTRACT.md`: 133 live operations on 104 paths (it said 76 on 64). 32 entries became live, 25 routes were missing (E-115 to E-139), three paths corrected.
- `MOBILE_PLATFORM_CONTRACT.md`: the 429 rows give the AI limit (10 a minute, 100 a day, 500 paid; `Retry-After` 60 or 3600).
- Verified with two separate scripts against `docs/openapi.yaml`: 0 missing, 0 extra. Not checked: the roles written on the Contacts and Talk rows (F W P) were left as they were.

## 2026-11-24 — Full Android check on `main`

### Verified (`789b9a4`, local, JDK 17; not run on a phone)
- `testDebugUnitTest checkModuleGraph :app:assembleDebug`: BUILD SUCCESSFUL, 886 tests, 0 failed, 0 skipped, debug APK built.
- This covers S4's missing app build, Arjun's R1 (`ReadTimeoutTest`) and App Links (`AppLinkHostTest`), and Minhaz's `444b71d`.

### Taken from Arjun
- R3 and R4 (marked in the plan). R2 was already done by Minhaz.

## 2026-11-24 — S4: dictation says what went wrong; Talk keeps words it could not save

### Changed (`eae8254`, on `main`)
- Dictation and Talk show a lost connection as a network error, and no longer report every failure as "no speech"; Talk no longer shows the read-aloud message for a dictation failure.
- Talk keeps words it could not save (offline, timeout, server error, busy, signed out, rate limit) and sends them again, in order, at the next Listen.
- The offline-recognition preference named in the plan is already gone from `DeviceDictation.kt`; no change needed.
- `eae8254` was pushed before its tests ran and with two Kotlin session files; the tests were run right after, and this commit untracks the files and ignores `.kotlin/`.

### Verified (local, JDK 17; not run on a phone)
- `:core:media:testDebugUnitTest` 26 passed, `:feature:connect:testDebugUnitTest` 78 passed, 0 failed, 0 skipped; `checkModuleGraph` passed.

### Known limits
- A save that times out after the server stored it can add the same Talk entry twice.
- Unsaved words are lost when the screen closes.

### Next
- S5 (final test on two phones) waits for Minhaz's 1.0.2 APK (M12).

## 2026-11-24 — S3: unanswered Checks carry the text and the Thread title

### Changed (`2f16153`, on `main`)
- `GET /api/messages/unanswered` items (`UnansweredCheck`) now also carry `threadTitle` (string, required, never null) and `text` (object of language code to string, required). Existing fields: `messageId`, `threadId`, `sentAt`, `deadline` (date-time or `null`). Additive only; listing does not mark the Thread as read.
- `docs/openapi.yaml` and `frontend/src/services/generated/types.gen.ts` updated in the same commit.

### Verified (on `main` after merging origin, 590 tests collected)
- `uv run ruff check .` clean; `ruff format --check .` 199 files already formatted; `pyright` 0 errors; `lint-imports` 4 kept, 0 broken.
- `uv run pytest --cov -q` exit 0, total coverage 90.36% (gate 80%), including `tests/test_contract.py` (OpenAPI matches the app).
- `npm run gen:api` in `frontend/`: no diff in `src/services/generated`.

### For Anim
- N2 is unblocked: pull main and use `GET /messages/unanswered` in `HomeBoardSource.kt` instead of polling all messages; the live API has it after the next backend deploy.

## 2026-11-24 (from Arjun: task 2)

- Question on my task 2 (Task Board against the live API): `frontend/` has no Task Board; only the Android app (`feature/board`) has one. Which client failed on `If-Match`? If it was Android, it is Minhaz's.
- **Answer (Asif):** you are right, the web has no Task Board, and nothing in `frontend/` outside the generated types uses `If-Match`; only Android `feature/board` does, and CORS does not apply to it. My line "from the web" was wrong. The CORS fix (`4c0c559`) is for a future web board, so task 2 needs nothing from you: close it.

## 2026-11-24 — Android audit (code read + live API; no app code changed)

### Found (main at `0f7e60c`; not run on a phone)
- **AI drafts vs the app's 30 s read timeout:** 4 of 14 live drafts took 51–61 s. The app then shows "No connection" and the draft stays unsent; 12 unsent drafts are in the `SASEBO01` group Thread.
- **Chat voice:** the stop button becomes Send after the first partial words, and dictation keeps running, so a message is cut or sent twice. Audio is never uploaded; server Whisper is unused by the app.
- **Degraded drafts are sent untranslated** without a warning; `PATCH` draft edit has no screen.
- **Re-explain** fails for a wrong answer on a question without a Glossary Term (no Follow-up is created).
- **Live seed is old:** `shops.languages` and Rahim still have `bn`; needs the demo reset.
- **Home marks every Thread as read** (it polls messages); `GET /messages/unanswered` fixes it.

### Next
- Minhaz: interface and Firebase. Arjun: app logic. Galib: AI. Anim: demo data and CI. Asif: backend gaps and the final test.
- Work split for the five of us: [ANDROID_FIX_PLAN.md](../ANDROID_FIX_PLAN.md).
- Test data left in `ISAHAYA2`: Thread "Android audit" and one Briefing draft; the demo reset removes it.

## 2026-11-24 — Backend evaluation: every finding fixed

### Completed (all on `main`, `b155816`; 542 tests, 90% coverage)
- **CORS** allows `If-Match` (`4c0c559`); a test checks every header the contract declares.
- **Per-user AI allowance** (`6aecc80`): 10 a minute, 100 a day (500 paid), 429 with `Retry-After`; messages and Near-miss Reports are never refused.
- **CI** (`774c4f9`): gitleaks over the whole history on every PR and push (no real secret found), 80% coverage floor.
- **Route guard** (`2b7f1c7`): all 128 operations checked for 401, other roles, first PIN, another shop and a colleague; a new route fails CI until it is covered.
- **`python -m app.cli reset-pin`** (`454a652`), the documented last resort.
- **Message and thread deletion** (`3820598`, ADR 0014), migration `5b1e0c7d92aa`; personal texts are never cached.
- **Talk Threads**: Galib shipped XREQ-018 first, so I dropped mine.
- **`docs/API_SPEC.md`** filled in; its endpoint index is generated and checked (`a430bbc`).
- **XREQ-025 unblocked**: `Microsoft.Storage` registered. **XREQ-009 closed. XREQ-007 item 3** (seam) answered in the card.
- **`Cache-Control: no-store`** on every API response (sign-in and Senpai links too), as §12 promised; guard test also lists routes outside the contract; untrue claims in BACKEND_ARCHITECTURE corrected (SQLModel, Pillow, docker-compose, `/demo/reset`, load test).
- Notes for the team: `FROM_ASIF_2026-11-24.md` in the anim, galib, minhaz and arjun folders, each starting with that person's task list.

### Next
- XREQ-013 (static web demo): stays with Anim (decided 2026-11-24).
- Team: the AI allowance can be avoided by opening many shops (`POST /auth/register`); options in the notes.
- Minhaz: `MOBILE_API_CONTRACT.md` still says 76 operations; the exact list is in his note. Arjun or Minhaz: `npm run gen:api`.

## 2026-11-23 — HANDOVER: backend, Azure and database (Asif is off the project)

Read this section top to bottom; it is meant to be enough without asking me. **Anim owns deploy and operations from
now on** (XREQ-015). Everything below is on `main`.

### 1. What is live

| Piece | Where | Notes |
|---|---|---|
| API | `https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io` | health: `/api/health/ready`; contract: `docs/openapi.yaml` |
| Database | Supabase project `wakatta` (ref `jpajxyejcbtfivrylcaf`), Tokyo, free plan | runbook: `backend/deploy/SUPABASE.md` |
| AI | Gemini (`AI_MODE=gemini`, `AI_CHAIN=dev` = Flash-Lite), Groq as fallback | keys are Container App secrets |
| Azure | subscription **Azure for Students**, resource group **`wakatta-rg`**, region **Japan East** | the old Azure PostgreSQL server is **deleted** |

Azure resources in `wakatta-rg`: Container App `wakatta-api` (exactly 1 replica), environment `wakatta-env`,
registry `wakattaacr42338`, managed identities `wakatta-id` (pulls images; no tokens) and `wakatta-ci` (GitHub
deploys), custom role **Wakatta CI Deployer** (Container Apps only). Verified live: Anim's
`backend/scripts/smoke.py` **13/13** (health, sign-in for Foreman/Worker/Poster, 401 without a token, board, Skill
Matrix, Notices without answer keys, Events, Chat ETag 304, Task Board, Senpai, Support Log CSV, Near-miss, CORS).

### 2. How code ships (no manual deploy)

Push to `main` → **`backend-ci`** (lint, types, import layers, tests on PostgreSQL 16) → on green,
**`backend-deploy`**: signs in to Azure with **OIDC** (no stored password; trusts only this repo's `main`), builds
the image into the registry tagged with the commit SHA, updates `wakatta-api`, waits until the new revision is
Healthy and `/api/health/ready` answers. On start the container (`backend/jobs/start.py`) runs
`alembic upgrade head`, so **a new migration ships with its code**.
- Watch it: GitHub → Actions → `backend-deploy`. If red: `az containerapp logs show -n wakatta-api -g wakatta-rg --tail 80`.
- Roll back: revert the commit and push; or point the app at an older image:
  `az containerapp update -n wakatta-api -g wakatta-rg --image wakattaacr42338.azurecr.io/techtsunagu-api:<older sha>`.

### 3. Operations (PowerShell, from the repo root)

Needs the Azure CLI signed in (`az login` in a browser; the device-code login is blocked by the tenant's security
defaults) and **Contributor on `wakatta-rg`** (sign in with `az login --tenant acf1fe93-0760-406b-9216-573b5fed5453`). If `az` is missing: the official installer, or
`uv tool install --prerelease=allow "azure-cli>=2.70"` (the scripts find either). Each command waits until the API is
healthy again.

| When | Command |
|---|---|
| Before filming / after testers made a mess | `powershell -ExecutionPolicy Bypass -File backend\deploy\reset-demo.ps1` |
| Filming / demo day: strongest model first | `...\set-ai-key.ps1 -Chain demo` (back to quota-saving: `-Chain dev`) |
| The frontend gets a hosted address | `...\set-cors.ps1 "http://localhost:3000,https://<frontend host>"` (exact origins, no trailing `/`) |
| Rotate the DB passwords / rebuild Supabase | `...\use-supabase.ps1` (needs `SUPABASE_DB_URL` in `backend/.env`; **reseeds**) |
| Logs | `az containerapp logs show -n wakatta-api -g wakatta-rg --tail 80` |
| After the hackathon (stop paying) | `az containerapp update -n wakatta-api -g wakatta-rg --min-replicas 0` |

`azure-setup.ps1` is the one-time infrastructure script (identities, registry, OIDC trust, roles). It needs **Owner**
rights (role assignments), so it is not needed or runnable with Contributor; everything it makes already exists.

Demo accounts (seeded, fictional): shop `SASEBO01` Foreman 山口 隆 PIN `246810`, Workers PIN `2468`; district
`MINATO26` Posters PIN `482615` / `739204`; other shop `ISAHAYA2` Foreman PIN `135790`; Senpai links
`/api/senpai/demo-tanaka-7f3a9c`, `/api/senpai/demo-mori-2b8e41`.

### 4. Secrets: where they live (never in git, never in chat)

- **Azure Container App secrets** (`az containerapp secret list -n wakatta-api -g wakatta-rg` shows names only):
  `database-url`, `migration-database-url`, `pin-pepper`, `gemini-api-key`, `groq-api-key`.
- **GitHub**: none. The workflow has only non-secret IDs (client, tenant, subscription).
- **Locally**: `backend/.env` (gitignored): `SUPABASE_DB_URL` (Session pooler URI with the DB password),
  `GEMINI_API_KEY`, `GROQ_API_KEY`. `set-ai-key.ps1` and `use-supabase.ps1` read them and never print them.
- **Demo PINs depend on `PIN_PEPPER`.** If the pepper changes, reseed (`use-supabase.ps1` does both), or no demo PIN works.

### 5. Why it is built this way (gotchas we hit; do not undo)

- **Azure for Students limits:** only 5 regions (japaneast, eastasia, koreacentral, indonesiacentral, malaysiawest);
  **ACR Tasks blocked** (so images are built in GitHub Actions, not in Azure); new Container Apps environments are
  **"express"**: **no Jobs**, one revision name (`wakatta-api--latest`), `latestReadyRevisionName` never filled.
  Hence migrations at container start (`jobs/start.py`) and health checks on the revision's own state.
- **Security tradeoff (documented):** without Jobs, the API container holds the DB owner credential to migrate;
  `start.py` drops it from the serving process's environment before Uvicorn starts. Upgrade path: a non-express
  environment and a migration Job.
- **OIDC subject:** this repo sends the ID-based subject `repo:minuwu@84508269/TechTsunagu@1466841300:ref:refs/heads/main`
  (not the plain name). The deploy job also runs only for a green **push** on this repo's own `main` (a fork PR
  named `main` cannot deploy).
- **Supabase:** connect through the **Session pooler** host `aws-0-ap-northeast-1.pooler.supabase.com:5432` (the
  direct connection is IPv6-only; the `aws-1` host does not know this project). Roles sign in as
  `<role>.jpajxyejcbtfivrylcaf`. TLS is `verify-full` against Supabase's own root CA in `backend/certs/` (valid to
  2031). Data API roles (`anon`, `authenticated`) are locked out of our tables. **Free plan pauses after 7 idle
  days**: open it the day before the demo.
- **PowerShell 5.1:** any CLI line on stderr became a fatal error under `ErrorActionPreference=Stop`; `Invoke-Az` and
  `use-supabase.ps1` relax it around the call and judge by exit code. Keep that if you edit the scripts.
- The scripts wait if another rollout is changing the app (`Wait-Idle`): Azure allows one change at a time.

### 6. Built and fixed this session (main commits)

- Chat tables + migration (`9ca7da7`); Panel Override, Sign-off list, Knowledge Library, `approvedBy` (`617e11e`);
  effort metrics (`ba69738`); `GET /api/meetups` (`3a02dce`); CORS origin list (`4d7e0e2`).
- Fixes: seeded question ids caused 500s (`46b312a`, with Anim); Check response built before commit (`113bff6`);
  UTC DB sessions (`71921a4`).
- Deploy: CI/CD with OIDC (`73aca9f`, `7386334`, `043401b`, `90aafd1`), start-up migrations (`4d7e0e2`, `393c026`),
  health-check fix (`8f891f8`), PowerShell fix (`e20c49c`), Supabase switch with `verify-full` (`cef15a7`,
  `e90c74e`, `15b75cf`).
- Frontend handover: [docs/FRONTEND_API_HANDOVER.md](../../docs/FRONTEND_API_HANDOVER.md) (linked from XREQ-001).

### 7. Open items and owners

| Item | Owner | State |
|---|---|---|
| XREQ-015: Azure access for Anim | Anim | **Done**: guest + Contributor on `wakatta-rg`. Accept the invitation email, then `az login --tenant acf1fe93-0760-406b-9216-573b5fed5453` and `az account set --subscription "Azure for Students"` |
| XREQ-013: host the web demo (Static Web Apps) | **Anim** (after XREQ-015) | Provider `Microsoft.Web` already registered (Contributor could not); East Asia is allowed |
| XREQ-001: frontend on the real API (sign-in, token, list shape) | Minhaz | Backend side done and verified; checklist in `docs/FRONTEND_API_HANDOVER.md` |
| XREQ-014: Firebase push | Minhaz → Anim | Pending |
| XREQ-011 item 1 (Worker's Follow-ups, history, analytics) | Anim | Not started |
| CORS for the hosted frontend | Anim | `set-cors.ps1` once the URL exists |
| Before filming | Anim | `reset-demo.ps1`, then `set-ai-key.ps1 -Chain demo`; check Supabase is awake |
| Not yet exercised live on Supabase | Anim | creating Notes, Checks, Chat messages with real data (reads and logins are verified) |
| Hardening (optional) | — | private networking for the DB; a non-express environment to move migrations to a Job |

Costs (from the $100 student credit): Container App always on (~US$15–25/month), registry Basic (~US$5/month).
Supabase is free. Scale to zero after the demo (command in §3).

### 8. First hour for the new owner (Anim)

1. `git pull`; read this section and `backend/deploy/SUPABASE.md`.
2. Accept the Azure invitation email; `az login --tenant acf1fe93-0760-406b-9216-573b5fed5453`; `az account set --subscription "Azure for Students"` → `az group show -n wakatta-rg` succeeds.
3. `cd backend && .venv\Scripts\python scripts\smoke.py https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io --origin http://localhost:3000` → 13/13.
4. Push any small backend change and watch `backend-ci` → `backend-deploy` go green in Actions.

## 2026-11-23

### Completed
- **XREQ-005 item 1 (Chat tables), Anim unblocked** (`9ca7da7`): migration `c10c87c5bd34` adds `threads`, `thread_members` (`last_read_at` for unread), `messages` (stage 0–5; `sent_at` set exactly at stage ≥ 4; a Re-explanation needs `to_user_id` + `ref_message_id`), `message_checks` (1–3 questions), `message_answers` (unique message + user, answers −1..2), `follow_ups` (unique message + user + term; `resolved_at` set exactly when not `open`), `tasks` (`board_column` because `column` is an SQL keyword, `version` for `If-Match`) and **`task_assignees`** (a join table, not an id array, so the database refuses an assignee from another shop). Every reference is a composite `(shop_id, id)` FK. Factories `thread`, `message`, `task` in `tests/factories.py`; 9 schema tests in `tests/db/test_chat_schema.py`.
- **Chat Confusions:** `confusions.message_id` added; `check_id` and `machine_id` are now nullable with `one_source` (exactly one of check / message) and `work_check_has_machine`. `ConfusionOut` gains `messageId`, and `checkId` / `machineId` can be null (additive; Work Confusions are unchanged). Foreman Board counts skip Chat Confusions (no machine). `docs/openapi.yaml` regenerated.
- AI keys from Galib moved into `backend/.env` (gitignored, `AI_MODE=fake` until the deploy); the keys file is deleted; `.env.example` documents `AI_MODE`, `GEMINI_API_KEY`, `GROQ_API_KEY`; `*keys*.txt` is gitignored (`3773b99`).

### Blockers / handoff
- **Anim:** pull, run `alembic upgrade head`, then `python -m app.seed --reset`. The tables are ready for XREQ-005 items 2–8.

## 2026-11-22 (proto → production handoff from Minhaz)

Minhaz mapped the prototype (`prototypes/wakatta/`) and the frontend against `main` (`c07a8fb`). Decisions: the prototype's **Chat is in production scope (WS12, P1)**; Course stays P2 and frontend-only. Read [BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md) decision #8 and the new XREQ cards in [AGENTS.md](../../AGENTS.md) §2.2. Do the items in this order.

### Next (assigned)
1. **XREQ-005 item 1 first (unblocks Anim):** Chat tables in one migration: `threads`, `thread_members`, `messages`, `message_checks`, `message_answers`, `follow_ups`, `tasks` (shop scope, composite FKs, `tasks.version` for `If-Match`). Factories in `tests/factories.py`. Push as soon as up → down → up passes.
2. **XREQ-003** (Galib, pending): accept or change the model chain, budgets, Groq Whisper step, settings; add `approvedBy` and `needsEdit` to the Note Version response. Chat voice depends on the Whisper step (XREQ-006 item 3).
3. **Routes the frontend already calls that the backend lacks:** `GET /api/sign-offs`, Panel Override, Skill Matrix / Heatmap / Skill Passport (WS5), Knowledge Library version timeline (WS8, now yours). Also note: the frontend calls `/api/auth/pin`; the backend has `PUT /api/auth/me/pin` (Minhaz changes the frontend under XREQ-001).
4. **WS9 production switch (Mon evening test deploy):** API on Azure Container Apps + PostgreSQL Flexible Server; `AI_MODE=gemini`, `GEMINI_API_KEY` as a secret (from Galib, never in git); CORS for the frontend's production origin; `alembic upgrade head` then `python -m app.seed --reset`; `/api/health/ready` green; min = max = 1 replica. Frontend hosting with `VITE_USE_MOCK=false` and the API base URL (agree the host with Minhaz).
5. P2: effort metrics (`GET /metrics/effort`).

### Blockers / handoff
- Anim waits on item 1 for WS12; Galib waits on item 2 for the Whisper step.

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

- Evaluated the alternative idea **Matte** (bank-transfer fraud circuit breaker): checked 18 claims of spec v2 against sources, judged it winnable, and wrote a system and team plan: [MATTE_EVALUATION_AND_PLAN.md](MATTE_EVALUATION_AND_PLAN.md).
- **Team decision: we stay with Wakatta?.** Matte is shelved; its plan stays in my folder for reference only.
- **Backend design v2, built for many shops and many users:** [docs/BACKEND_ARCHITECTURE.md](../../docs/BACKEND_ARCHITECTURE.md) rewritten (v1 replaced; nothing was migrated yet). Decided with a four-voice review (architect, skeptic, pragmatist, critic):
  - Two scopes: Work data per **shop**, Life data per **district** (a 自治会 serves Workers of several factories). Isolation in one repository layer + composite foreign keys; no RLS ([ADR 0009](../../docs/adr/0009-shop-and-district-scopes.md)).
  - Personal accounts: name + PIN → Bearer session; the Worker comes from the token, so nobody can forge a Sign-off; Panel device tokens; Senpai links hashed and revocable ([ADR 0010](../../docs/adr/0010-personal-accounts-and-pins.md), amends 0005).
  - Wire format matches Minhaz's frontend: camelCase, string ids, cursor lists ([ADR 0011](../../docs/adr/0011-camelcase-string-ids-cursor-lists.md)); `docs/API_SPEC.md` §1 updated.
  - Full data model (§6), transactions, idempotency and capacity (§8), every endpoint mapped to the frontend's paths (§9), build order with a check per step (§16).
  - Galib's 7 Gemini points accepted and folded into §7.4 and §10 (global 4/min limit, daily budget in Postgres, no retry on daily-quota 429, `reading` match for the Glossary lock, simple schemas, Notice text as data, model split).
- Opened **XREQ-001** for Minhaz in [AGENTS.md](../../AGENTS.md): Bearer token instead of cookie and the forgeable `'1'` PIN flag, roster + login, no `workerId` in bodies, cursor lists, `Idempotency-Key`.

- Completed the backend plan:
  - Account lifecycle for Workers and Foremen ([§3.4](../../docs/BACKEND_ARCHITECTURE.md#34-account-lifecycle-workers-and-foremen)): Foremen create accounts, first-login PIN change, reset revokes sessions (lost phone), deactivate keeps history, last Foreman can't be removed.
  - Added the plan's missing routes: Notice reach, expanded Events, Support Log date range.
  - Synced the team plan with v2: personal accounts, table map, route groups, WS0 owner and env vars (`8f2834c`).
  - XREQ-001 now includes the first-login PIN screen and the Foreman account screens.
- **Founder skill pack** ([founder-skill](https://github.com/Jakeschincariol/founder-skill) v1.0.0, MIT): read every file before installing (standard-library Python only, no network, no hooks), installed as a Claude Code plugin on the E drive, and its own 29 tests pass. It tests a *business* (board, competitors, buyer panel, pricing, CFO), not code, so the backend design itself was tested by the council above. Inputs used: free for employers and Workers, paid by 登録支援機関; buyer = factory owner / plant manager. Files: [founder/](founder/).
- **Founder board result** ([board.md](founder/board.md)): **3 × FUND IF, average 4.3/10** (Offers 4, Monopoly 4, Product 5).
  - Risks more than one lens raised: (1) nobody has shown the payer (登録支援機関) will pay, or that the Support Log matches what they must file; (2) the software LOCK may not survive a real factory (busy shifts, a worker walking past it); (3) the Foreman carries the effort and every re-approval voided all Sign-offs; (4) the AI is a commodity, its error rate on shop terms is unmeasured and the free tier can't run a factory; (5) incumbents (tebiki, Teachme Biz, カミナシ) could copy a machine gate.
  - Product lens: it is two products in one; the strongest version is a compliance-and-safety record a support organisation buys per Worker, filled daily by the machine gate.
  - Can't be met by Nov 24: letters of intent, a factory pilot. Can: a one-sentence pitch, honest payer/duty wording, a bypass answer, measured AI numbers.
- **Filled the board's gaps in the design** (`2771c6d`, BACKEND_ARCHITECTURE.md D5):
  - **Panel Override**: a Foreman lets a Worker run a Machine without a Sign-off for ≤ 8 h with a reason; amber on the Panel, on the board, audited, in the Support Log (answers "the lock stops production" and "what if someone walks past LOCK").
  - **Safety vs wording change**: only a Safety Change sends everyone back to the Check (`machines.recheck_version_no`); GLOSSARY.md and the plan rule updated.
  - **Per-shop languages** (`shops.languages`: bn, en, vi, id, ne, my, tl, zh); the demo stays bn/en and the wire shape stays `Tri`.
  - **Effort metrics** (`GET /metrics/effort`: time to approve, edits per draft, clean AI drafts, first-time passes) and an **AI eval** (`python -m app.ai.eval` → `docs/ai-eval.md`, Galib) plus a cost-per-factory formula.
  - **Performance budgets** (§8.5): Panel poll p95 50 ms with `ETag`/`304`, board 200 ms, `statement_timeout` 5 s, GZip, load test `tests/load/poll.py` (30 Panels + 5 boards + 2 AI drafts).
  - XREQ items 12–15 added to §17 (override state, approve choice, languages, `If-None-Match`).
- **Founder competitor map** ([competitors.md](founder/competitors.md), `c68a7ec`): 22 sourced competitors in Japan (7 direct, 10 indirect, 5 substitutes).
  - **Gap holds:** no product gates a specific machine on a passed quiz, alerts the foreman with the exact misunderstood term, or writes it into the 登録支援機関's record. No public review asks for it either, so demand is unproven.
  - **Corrections to our claim:** tebiki has translation and a skill map but no test feature found; カミナシ already has tests in 18 languages and Bengali in its worker app. Bengali and quizzes are not our difference; the gate, the term alert and the support record are. Plan line fixed.
  - **Biggest threat:** カミナシ (tests + per-learner results + shared tablets + equipment records + Bengali); linking them is a feature release for them.
  - Prices: most direct tools are quote-only; 3 public prices for factory manual SaaS, ¥34,000–¥89,800 per month.
- **Backend plan mapped to the hackathon guide** ([docs/BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md), `c30145d`), written from the Participant Guide:
  - The guide's rules turned into backend rules: claim only what runs ("what works now vs planned" is a required proposal item, and reviewers check the repo against it); code freeze Tue 22:30 BST because commits after 23:59 are not evaluated; an AI-use statement in the proposal; the API stays up for the 6-minute presentation on Nov 26.
  - 15 open questions closed in one table (accounts, Bearer, many shops, `/scan`, camelCase, WS7/WS8 kept as P2 instead of cut, "Chat" prototype out of backend scope, `argon2-cffi`, demo languages bn + en, AI model split, 1 replica, software lock, what is original (gate + term alert + support record, not Bengali or quizzes), who pays).
  - Backend work mapped to each of the 5 evaluation points; P0 / P1 / P2 tiers; proposed owners (Asif WS0/WS2/WS9, Anim WS10/WS11, Galib WS1 + AI parts, Minhaz XREQ-001, Member 5 WS5–WS8); BST timeline with checkpoints (WS0 by Mon 03:00, P0 on Azure with the real frontend by Mon 18:00, P1 by Tue 06:00, freeze Tue 10:00, submit 23:30); definition of done; proposal checklist.
  - Pulled Galib's Rover Villa UX ideas (proposal, not agreed): listed as an open item, no backend work before P0 is green.
- **Sourced research** ([research.md](founder/research.md), from official MOJ/ISA files, `c5a4893`):
  - **Nagasaki 特定技能1号: 4,276** (2号 86), mostly **Indonesia 1,385, Philippines 915, Vietnam 881, Myanmar 534; Bangladesh 31** (ISA, 2026-06-30). Nationally 425,961, Vietnam 38.3%. → Open team decision: add one Indonesian Worker to the demo (BACKEND_PLAN §10); the backend already supports per-shop languages.
  - **Support duties:** 10 mandatory items confirmed, each with an on-site record (e.g. consultation log 参考様式5-4号, meeting reports 5-5/5-6号), kept **≥ 1 year after the employment contract ends**. The periodic report moved from quarterly to **yearly** on 2025-04-01 (form 第3-6号, due 1 Apr–31 May) and asks for counts, not details. → Support Log now grouped by item 7 (consultation ← Asks/Senpai), item 8 (exchange ← Meet-ups), orientation (← Notices); item 10 (3-monthly meetings) stated as not covered; retention rule added.
  - **登録支援機関:** 11,876 nationwide, **79 in Nagasaki** (register 2026-11-19). **Average fee ¥28,386 per worker per month** (ISA, 2022 data; ~90% ≤ ¥30,000).
  - **Gemini paid prices:** 3.8 Flash $0.75 in / $3.75 out per 1M tokens (doubles 2027-01-01); 3.5 Flash-Lite $0.30 / $2.50; audio 32 tokens/s. Worked example ≈ $0.008 per note, under $2 per factory per month at 200 AI calls (to be replaced by measured tokens).
- **Backend kickoff prompt** for coworkers: [BACKEND_KICKOFF_PROMPT.md](BACKEND_KICKOFF_PROMPT.md). Paste it into Claude Code; it reads AGENTS.md (XREQ first), BACKEND_PLAN and the architecture, checks whether WS0 is merged (if not: only pure domain rules + tests, no skeleton), and runs every slice through plan → TDD → Postgres skill → context7 → ruff/pyright/import-linter/pytest → `/ponytail-review` → pull → push → WORK_LOG. Set for Anim (WS10/WS11); change two lines for Galib or Member 5.
- **WS0 step 1 — skeleton** (`33e0cdb`, on `main`): uv project (FastAPI 0.165, Starlette 1.10, pydantic-settings, python-dateutil 4); fail-fast settings; one error envelope (404/405/422 without echoing input/ApiError/500 generic + logged with request id); request id, JSON request logs and security headers as plain ASGI middleware; CORS for the frontend origin only, no credentials; GZip; `GET /api/health`. Test client needs `httpx2` (Starlette 1.10 deprecates `httpx`).
- **WS0 step 2 — database** (`b371aea`, merged to `main` as `4af31c1`):
  - Local PostgreSQL 16.16 from the official EDB zip in `E:\tools\pgsql` (no Docker, nothing on C); roles `wakatta_owner` (migrations) and `wakatta_app` (DML only, `statement_timeout` 5 s); databases `wakatta` and `wakatta_test`.
  - All **26 tables** of architecture §6 as SQLAlchemy 2 typed models (ADR 0007 amended: no SQLModel, it fails pyright strict). Composite `(shop_id, id)` foreign keys, so the database itself refuses cross-shop references; `asks.text_ja` included; `ai_usage.exhausted` added for the no-retry-on-daily-quota rule.
  - Found and fixed: Alembic silently drops `use_alter` foreign keys from `create_table`, so the Machine → current Note Version key was missing; it is now added explicitly (deferrable). Grants in the migration: app role DML only, `audit_events` insert-only.
  - Tests rebuild `wakatta_test`, run the migration up → down → up every run, and check isolation, uniqueness, role scope and grants as the app role; every test is rolled back. **95 pass**; ruff and pyright strict clean. `GET /api/health/ready` checks the database.
  - **Anim:** the models are on `main` (`backend/app/models/`), so you're unblocked. CI and the Life seed loader are yours as agreed; `tests/conftest.py` has the `db` and `client` fixtures and `tests/factories.py` the row builders.
- **WS0 step 3 — accounts and auth** (`43d7fb6`, on `main`): roster by shop/district code, name + PIN login (argon2id + pepper, `argon2-cffi`), 5 wrong PINs lock 15 min, per-IP limits in Postgres, opaque Bearer sessions (30 days sliding for Workers, 12 h otherwise), first-login PIN change enforced everywhere, logout/revocation, Foremen manage Workers and Foremen of their own shop only, last-Foreman guard, cursor-paged lists. `require(*roles)` is the only way a route learns who is calling.
- **WS0 step 5 — AI seam** (`141ec58`, on `main`): `app/ai/client.py` (`AiClient`, `Prompt`, `Part`, `AiUnavailable`, `QuotaExhausted`), `GeminiClient` (google-genai 1.x, structured JSON, inline media, timeout, `audio/mp4` → `audio/m4a`, daily-quota 429 mapped), `FakeAiClient`; `services/ai_runner.py` = the ladder: per-scope cache → daily budget (Pacific day, atomic) → limiter (2 at once, 4/min) → call → validate → one retry with the problems → degraded, own short DB sessions. **152 tests pass** (incl. Anim's and Galib's); ruff and pyright strict clean.
  - **Galib:** plug your pipelines in as `AiRunner.run(AiRequest(pipeline, prompt, parts, schema=NoteDraft, scope, cache_input), validate=lambda d: note_problems(d, glossary, langs), now)`; it returns `AiResult(value | None, outcome)`. Your `GET /briefings/{id}/receipts` is approved (replaces `/receipts?target=` for Briefings); the seed will load your `tests/fixtures/ai/glossary.json`.
- **P0 demo path (WS2)** (`4baee15`, on `main`): record note (voice/text, Idempotency-Key) → AI draft via the ladder + Galib's `note_problems` (degraded draft, never a 500) → edit step → approve as Safety or Wording Change under the machine row lock; Worker scan (Presence); Worker-safe machine view (no answer key); server-graded Checks → Confusions per Glossary Term + Sign-offs, `VERSION_CHANGED` on stale versions; Panel state with ETag/304; panel device tokens; Foreman board (fixed number of queries); Confusions list/ack; shift assignments; glossary. Found and fixed: the Worker-safe model correctly refused stored questions until `correctIndex` was stripped; "expiring" boundary at exactly 14 days left. **184 tests pass**; ruff and pyright strict clean.
- **Security review fixes** (`5dbbf6e`): a 10 MB body limit now runs *before* multipart parsing (ASGI middleware, Content-Length + streamed bytes), as §11 specified; a Check's Idempotency-Key reused for another note returns 409 `IDEMPOTENCY_KEY_REUSED` instead of replaying a grade. Regression tests first.
- **Work demo seed** (`af75e9a`): `python -m app.seed --reset` (owner role; refuses without `--reset` if data exists). District from Anim's `life.json`, shops `SASEBO01` + `ISAHAYA2` (isolation), Foreman 山口 隆 PIN `246810`, Workers PIN `2468`, 4 machines, Galib's glossary (moved to `app/seed_data/glossary.json`, his 2 tests repointed), the 4 approved notes exported from Minhaz's mock seed (live demo = mock demo), Sign-offs valid / expiring / expired relative to the run, today's shifts. Ran it on the local DB. Found and fixed: PowerShell read `seed.ts` as ANSI and garbled the Japanese/Bengali; re-exported as UTF-8. **189 tests pass.**
- **Contract and layer rules** (`e68e713`): `docs/openapi.yaml` is generated from the code (`cd backend; uv run python -m app.openapi > ../docs/openapi.yaml`); a test fails if it drifts, another proves no Worker schema has `correctIndex`. import-linter: 4 contracts kept (routers → services only, pure domain, AI seam out of data layers, repositories never call upward). **191 tests pass.** Minhaz can regenerate the frontend client from it now.
- **Anim: your work, start here** → see "For Anim: start here" under Blockers / handoff below.
- Azure: deploy after development is fine (everything is env + migration + seed); I suggest one test deploy on Mon evening to catch region or connection limits early.
- The 20-buyer panel is prepared (`founder/customer.json`, `founder/pitch.md`, all guesses) and waits for my check of the buyer profile.

### Next
- Read the competitor and research results; map the Support Log columns to the real support-record fields; put the Gemini prices into the cost model; then run the quick buyer panel.
- Team reads BACKEND_ARCHITECTURE.md v2 (§0 first). OK needed on the `argon2-cffi` dependency.
- WS0 on `asif/ws0-foundation`, steps 1–8 of §16 (target Nov 23 02:00).
- Team reads BACKEND_ARCHITECTURE.md; then WS0 on `asif/ws0-foundation`: skeleton, all tables in the first migration, typed route stubs, generated openapi.yaml, AI seam with fake, seed, CI.
- Hello-world API on Azure Container Apps today.
- XREQ to Minhaz: Bearer token in `src/services/api.ts`, call `/machines/{id}/presence` after the name pick.

### Blockers / handoff

#### For Anim: start here (2026-11-23 ~00:30)

WS0 is on `main` and you are unblocked. Everything below is yours (Asif keeps Azure, migrations/schema, and Work P1).

**0. Set up once (about 15 min)**
- `git pull`, then `cd backend; uv sync`.
- PostgreSQL 16 locally (no Docker needed): follow `backend/README.md` → "Local PostgreSQL". It creates roles `wakatta_owner` / `wakatta_app` and databases `wakatta` / `wakatta_test`.
- Copy `backend/.env.example` to `backend/.env` and set your own `PIN_PEPPER` (32+ chars).
- Check: `uv run pytest` → 196 pass; `uv run ruff check .`, `uv run ruff format --check .`, `uv run pyright`, `uv run lint-imports` → all clean. If not, tell me before you change anything.
- Demo data: `uv run python -m app.seed --reset` (shop `SASEBO01`, Foreman PIN `246810`, Worker PIN `2468`).

**1. Life seed loader (first, small)**
- New `app/seed_life.py` with `seed_life(db: Session, refs: Refs, now: datetime) -> Refs`, loading your `app/seed_data/life.json`.
- `refs` from `seed_work` already has `d-minato` (district), `s-sasebo`, `f-yamaguchi`, `w-rahim`, `w-sadia`, `w-tanvir`, `w-tanaka`, the machine and glossary keys. Do **not** create the district again (seed_work makes it from your life.json).
- Call it in `app/seed.py` `main()` right after `refs = seed_work(...)`. Posters' accounts: `User(role="poster", district_id=…, poster_id=…, pin_hash=hash_pin(pepper, pin), pin_must_change=False)`.
- Test like `tests/seed/test_work_seed.py`: Poster can log in by district code `MINATO26`, Notices are published, Senpai pairs exist.

**2. CI (`.github/workflows/backend-ci.yml`)**
- Postgres 16 service; create the two roles + `wakatta_test` with the SQL in `backend/README.md`; `uv sync`; then `ruff check`, `ruff format --check`, `pyright`, `lint-imports`, `pytest` with `TEST_DATABASE_URL` and `TEST_MIGRATION_DATABASE_URL` set (see `tests/conftest.py`).
- `tests/test_contract.py` already fails when `docs/openapi.yaml` is stale, so CI covers the contract.
- Image build is separate (`backend-image.yml`, mine); don't touch it.

**3. Life routes (WS10/WS11, P1), in your cut order: Notices → grounded Asks → Events → Support Log → Senpai → Meet-ups**
- Paths, roles and bodies: architecture §9.3 "Life flow"; your schemas in `app/schemas/{notices,asks,senpai}.py`; frontend calls in `frontend/src/services/wakatta.ts`.
- Copy the Work pattern exactly:
  - router (`app/routers/work.py`): `Annotated[Principal, Depends(require("poster"))]`, `DbSession`, `Paging`, `IdempotencyKey` header on creates, return `Envelope[...]`.
  - service: scope from the session (`who.district_id` for Posters, `who.shop_id`/`who.district_id` for Workers), `ApiError(404)` for another scope's id, `db.commit()` per write.
  - repository: every query takes the scope id; nothing unscoped.
- AI (Notices from text/voice/photo, Asks): end the read transaction (`db.commit()`) **before** the model call, then use the runner like `app/services/notes.py`:
  `request.app.state.ai.run(AiRequest(pipeline="build_notice", prompt=render("build_notice", 1, glossary=…, today=…), parts=[…], schema=NoticeDraft, scope=f"district:{id}", cache_input=…), validate=…, now)`. It returns `value | None` and an outcome; on `None` build the degraded draft (never a 500). Asks: pass the parsed draft through your `app.domain.grounding.ground()`.
- Your rules are already done: `app/domain/recurrence.py`, `grounding.py`, `support_log.py`.
- Tests: `client`, `db` and `fake_ai` fixtures (`fake_ai.queue("build_notice", {...})`); see `tests/api/test_work.py` for the flow style. Add a two-district isolation test.
- After adding routes: `uv run python -m app.openapi > ../docs/openapi.yaml` and commit it with the routes.

**4. Only if 1–3 are done:** Skill Matrix, Heatmap, Skill Passport (read-only; `repo.newest_sign_offs` already gives the shop-wide data). Tell me first so we don't both build them.

**Rules (same as the kickoff prompt):** schema changes go through me (ask in your log); never hand-edit `docs/openapi.yaml`; small Conventional Commits; `git pull` before every push; push after each finished part; no AI attribution; one line in your WORK_LOG per part.

- **Reply to Anim (2026-11-22 ~21:45):** WS0 starts now on `asif/ws0-foundation`; each step is pushed and merged to `main` as soon as its checks pass, **models + first migration first** so you are unblocked tonight. Accepted: `asks.text_ja text null` (in the architecture and the first migration); `.ics` backend → P2 (plan updated); `python-dateutil` + `types-python-dateutil` go in `pyproject.toml`; your `app/schemas/common.py` stays where it is (WS0 builds on it, no move). After the models land, **CI and the Life seed loader are yours** (OK given here). Your cut order is recorded in BACKEND_PLAN §5. `seed.py` will use your worker keys `w-rahim`, `w-sadia`, `w-tanvir`, `w-tanaka`.
- Owners updated: Member 5 is Arjun Saha (frontend design), so WS5 → Asif, WS6 → Galib, WS7/WS8 (P2) open.
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
