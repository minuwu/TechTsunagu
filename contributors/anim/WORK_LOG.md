# Anim's Work Log

Owner: Anim (`anim710`) — Role: Backend (WS10 Notices + Events, WS11 Asks + Senpai + Support Log + Meet-ups)

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-24 (from Asif: proposal for open registration, ADR 0017)

- Each new shop's Foreman gets a fresh 100 AI requests a day, and one IP may open 3 shops a minute, so the AI allowance can be got around.
- **Proposal:** at most 10 new shops a day per IP on `POST /api/auth/register`, next to the 3 a minute. It is one more rate-limit line and a test; no new table, and people in existing shops notice nothing.
- **Limit:** one venue or mobile network can share an IP, so 10 also counts everyone there. If the judges may register their own shops, choose a higher number.
- Anim: answer yes, no or another number in your log. I do the change when both of you agree.
- **Anim's answer: yes, 10.** Done: `REGISTERS_PER_IP_PER_DAY = 10` next to the 3 a minute, test `test_one_ip_opens_at_most_ten_shops_a_day`, ADR 0017 amended. Raise it if the judges register their own shops on site.
- **XREQ-027 review:** found 4 real problems in the deployed code and fixed them (sign-in by shop name could guess a namesake's PIN; an archived Machine kept its Panel and took writes). Details in the XREQ-027 card; 6 low items left there for their owners.

## 2026-11-24 (from Arjun: XREQ-013)

- Question: what is the hosted address for the static web demo (XREQ-013)? Once it is up, I will give you the final `VITE_PUBLIC_URL` and check every page.
- **Anim's answer:** https://mango-beach-001af4300.4.azurestaticapps.net (Static Web App `wakatta-web`, Free, East Asia). Already built with that `VITE_PUBLIC_URL` and deployed; details in the XREQ-013 card. Please check every page and tell me what to fix.
- **Demo data reset (N1) is on hold:** it truncates every table but the AI cache, so it signs every tester out, deletes registered phones (push), shops made by registration, Contacts and chats. It runs once, announced, just before filming.

## 2026-11-24 (from Arjun: small help, nothing to do unless you want)

- **XREQ-013 is ready to host:** on today's `main`, `npm run build:static` in `frontend/` is green: 39 pages pre-rendered into `frontend/.output/public`, 19 MB (the card says about 15 MB; the 3D Journey assets came since). Only `VITE_PUBLIC_URL` is still needed, once the Static Web App has its address.
- **Frontend CI was red on `main`:** `docs/openapi.yaml` changed in `c74d4c6` (Wakatta cards: `WorkerResult`, answer keys, `asked`) without `npm run gen:api`, and `frontend-ci` fails on stale `src/services/generated`. Regenerated; typecheck, lint and 104 frontend tests pass.
- **Your Landing.tsx typecheck FYI** (`lp_all_courses`, `lp_label_access`): already fixed on `main`.
- Windows tip: if `vitest` says "failed to find the runner" for every file, the shell is in `d:\` (lowercase drive). Run it from `D:\techtsunagu\frontend`.

## 2026-11-24

### Completed
- **M2** `08b4d19` (Minhaz's task, with his knowledge): review card for the AI draft, no draft is sent without the Foreman's Send; full Android check green on the merged `main`. Not tried on a phone.
- **XREQ-020 done** (reply to Minhaz): `GET /api/workers/me/learning` (own Follow-ups + `streakDays`, paged) and `GET /api/analytics/understanding?days=` (trend, per Worker, top terms). Shapes in the XREQ-020 Fulfillment block. No migration.
- **Push sender (XREQ-014)**: FCM HTTP v1, `chat_message` and `nudge`, ids only, after the response. What the phone gets and how Minhaz tests it: the XREQ-014 Fulfillment block. `google-auth[requests]` is now a direct dependency (it was already in the lock through google-genai).
- Checked after the latest pushes: live API on `d7f6744`, smoke 13/13, Supabase at migration head `21d9f849b5c9` with no drift, security advisor clean, Firebase secret and project id on the Container App. No phone has registered for push yet (`devices` is empty).
- **XREQ-019 done** (reply to Minhaz): group controls on work Threads, migration `3c7e9a1f5b20` (additive). The title field is `titleJa`, not `title`; every rule and difference is in the XREQ-019 Fulfillment block. After the security review: sending or editing a draft re-checks membership and announce-only; a member added later cannot reach older messages by id, and their unread count starts at the join.
- **XREQ-025 done**: Asif registered `Microsoft.Storage`; storage `wakattaapk71923` rehearsed (upload, byte-for-byte HTTPS check, no listing), then released **1.0.0** and **1.0.1** (Minhaz's builds, SHA-256 checked against the committed `.sha256` first). `GET /api/app/config` offers 1.0.1 (`minVersion` 1.0.0) at `https://wakattaapk71923.blob.core.windows.net/apk/wakatta-1.0.1.apk`.
- **Asif's list** (`FROM_ASIF_2026-11-24.md`): #1 done (above). #3 checked: the running image `ae48470` has `5b1e0c7d92aa` as head and its start-up `alembic upgrade head` changed nothing, so Supabase is at `5b1e0c7d92aa`; live smoke 13/13; `secret-scan` not checked (the repo is private and `gh` is not installed here). #4 already done by Minhaz in `de6b82a` (a failed PATCH keeps the text as `needsEdit` and the same PATCH repairs it; `test_an_edit_that_lost_the_model_is_repaired_by_the_same_edit`). #8 done (`2ff2b51`): `fromDisplayName` on polled messages, `assignees` with names and `fromThreadId` on Tasks, `displayTitle` on Threads, `GET /api/messages/unanswered`; Minhaz to confirm.
- **XREQ-007 seam answered**: one chain for all tasks today, model stored on cached results; Whisper-first not built (card item 4: keep AI_PLAN §2 while the real recordings are open). Fixed: Whisper heard every recording as Japanese (now the speaker's language), `translate` got an empty glossary (now the shop glossary plus a one-retry shop-word check), and a Japanese-speaking Worker's voice message was stored empty.
- **XREQ-026 backend side done**: `/.well-known/assetlinks.json` and the "open in the app" page on the API host (the App Links host). Minhaz: send the debug and release certificate SHA-256 fingerprints; I set `ANDROID_CERT_SHA256`.
- Closed XREQ-010, 011 and 017 (every item live or done under a newer card). Fixed a pyright strict error in `tests/api/test_auth.py` that would have failed CI. XREQ-019 is live (`c683fe8`, migration ran on Supabase, smoke 13/13).
- **Android fix plan** (`contributors/ANDROID_FIX_PLAN.md`): N3 `172c890` (Android CI workflow; `gradlew` is committed without the executable bit, so it runs as `bash gradlew`), N4 and N5 `74bb8e3`. Verified on this laptop (JDK 17 installed): the new tests pass, the N4 test fails on the old code, and the full `testDebugUnitTest checkModuleGraph :app:assembleDebug` passes on the merged `main`. N2 waits on Asif's S3; N1 (reset) waits on the team's OK.

### Next
- Each new APK: `backend\deploy\release-apk.ps1 android\release\wakatta-<v>.apk <v>`. Set `ANDROID_CERT_SHA256` when the fingerprints arrive (XREQ-026).
- #6: decide with Minhaz (daily cap per IP on `POST /auth/register`, or a smaller AI allowance for new accounts), ADR 0017, then the change.
- #2 XREQ-013: Static Web App `wakatta-web` (Free, eastasia) not created yet: the create was held for approval on my side.
- #5 before filming: `reset-demo.ps1`, `set-ai-key.ps1 -Chain demo`, warmup, logs with Galib.

## 2026-11-23 (evening)

### Completed
- **XREQ-017 items 1-2 live** (reply to Minhaz): My QR, Contacts across shops, blocks, Contact Threads translated per side. Migration `21d9f849b5c9`. Details and the one change (`token` only on create or rotate) are in the XREQ-017 Fulfillment block. 6 API tests incl. silent block and "the employer cannot read personal threads"; 359 tests pass; contract and frontend types regenerated.
- Azure CLI 2.93 installed (uv tool) and signed in (Azure for Students, `wakatta-rg`).
- **Firebase key (XREQ-014)** stored as the Container App secret `firebase-service-account` with the new `deploy/set-firebase-key.ps1` (never printed; refuses a key inside the repo); env `FIREBASE_PROJECT_ID=daily-wizdom`. New revision healthy, live smoke 12/12.
- `deploy/common.ps1` `Wait-Ready`: a failed read while the revision is being replaced now means "not ready yet" (it aborted the Firebase script after the secret was already set).
- FYI Arjun: `npm run typecheck` on `main` fails in `src/ui/v4/screens/home/Landing.tsx` (`lp_all_courses`, `lp_label_access` missing from the messages) with or without my change.

### Next
- XREQ-013: host the web demo on Azure Static Web Apps (after sign-in), then CORS for its origin.
- XREQ-017 items 3 (Talk Threads, with Galib) and 4 (group controls); XREQ-011 item 1.

## From Minhaz (Android app), 2026-11-23 23:56: nine separate requests, XREQ-018 to XREQ-026

I checked the live `docs/openapi.yaml` (82 paths) against what the Android app still fakes. Thank you for My QR, Contacts, Contact messages and blocks: the app wires them next. What the backend still lacks, one card each in `AGENTS.md` section 2.2 so you can answer them one by one (reply in the card's Fulfillment block, regenerate `docs/openapi.yaml`, one line here):

- **XREQ-018** (P1, with Galib): Talk Threads and entries, plus `explain_utterance` and `grade_understanding`.
- **XREQ-020** (P1): Worker-readable Follow-ups and Learning History; analytics trend.
- **XREQ-022** (P1, with Galib): edit a Chat draft before Send; Re-explanation draft; "ask the Foreman" message.
- **XREQ-025** (P1, Azure): host the release APK and the release routine behind `GET /app/config`.
- **XREQ-019** (P2): group controls on work Threads.
- **XREQ-021** (P2): Remind a Worker, Idempotency-Key on overrides, Tasks by Machine.
- **XREQ-023** (P2): Near-miss for the Worker's own list, severity, translation for the Foreman.
- **XREQ-024** (P2): Foreman reads Asks, Today's Word setter, Support Log JSON summary, voice Ask transcript.
- **XREQ-026** (P2, Azure): App Links host, `assetlinks.json`, web fallback.

Order I suggest: 020, 022, 025 first (they block the real Chat, Learning and update flows), then 018, then the rest. "Later" is a fine answer for any card; the Android app keeps a Fake for that part and shows nothing broken. Firebase (XREQ-014): the project and the app config exist on my side; I will send you the service-account key privately, not through the repo.

## From Minhaz (Android app), 2026-11-23 19:50: please build XREQ-017, then reply in your log

The Android app is now wired to the live API through one file (`ApiConfig.kt`), and your routes from XREQ-008, 009, 010 (items 4 to 7) and 011 are used as shipped. Thank you. What is still missing on the backend, so the app still shows a Fake:

1. **Contacts and My QR** (no AI, smallest): `POST /me/qr`, `GET /contacts/resolve`, `POST|GET /contacts`, `PUT /contacts/{id}`, `POST|DELETE /blocks`.
2. **Contact Threads**: `GET|POST /contacts/{id}/messages` (translated per side, ETag, generic push with no text).
3. **Talk Threads** (needs Galib: `explain_utterance`, `grade_understanding`): threads CRUD plus `/entries` CRUD.
4. **Group controls** on work Threads: create, members add/role/remove, mute/pin/archive, leave, announce-only, `THREAD_LOCKED`.

Everything (proposed paths and JSON, errors, rules, order) is in **XREQ-017** in `AGENTS.md` section 2.2: the shapes are proposals, change a name if your code prefers another and write the change in the Fulfillment block. **Please reply** (a) in the XREQ-017 Fulfillment block after each item, with the real path, shape and a curl proof, (b) with one line here in your WORK_LOG, and (c) regenerate `docs/openapi.yaml`. "Later" for an item is a fine answer; say which, so the Android Fake stays. The notification list needs nothing (push-only, as you decided). Also still open from my side: XREQ-014 (Firebase): Minhaz creates the project and sends you the service-account key privately; I will reply there when it is done. For the film on 2026-11-24 the app works with the Fakes for the missing areas, so nothing here blocks you before then.

## 2026-11-23 (Asif is off the project: backend lead, database and deploy are mine)

### Completed
- **XREQ-008 (P0) `[DONE]`** (`df1606f`): `docs/openapi.yaml` declares `HTTPBearer` on every protected operation and `x-roles` per route (75 operations), from `require()` itself; `BACKEND_ARCHITECTURE` §8.2 replay status and §9.2 every error code the code returns; the card answers the base URL, demo shop, accounts and reset.
- **XREQ-012 `[DONE]`**: live API on Supabase, smoke 12/12, SQL checks; XREQ-003 items 5-6 noted done.
- **Migration `99f33c3ca2c7`** (schema is mine now): `periodic_interviews`, `devices`, optional `idempotency_key` on `meetups` and `tasks`; up → down → up clean; app role gets DML only.
- **XREQ-010 (agreed subset)**: `GET /interviews/due`, `GET|POST /workers/{id}/interviews` (face/online with consent, no future date, optional Idempotency-Key, audited), Support Log **item 10** (the file now covers it; the "not covered" note keeps only 参考様式第3-6号), `GET /today-word` (`seed_data/phrasebook.json`, bn + en), `GET /senpai-pairs/me`. Contacts / QR / Talk Threads / group controls are "planned".
- **XREQ-009**: `GET /app/config` (public, env-driven), `PUT|DELETE /devices/me` (upsert per install, shared token moves, 6/min, tied to the session); push only, no inbox; photos JPEG ≤ 2 MB (doc now matches code).
- **XREQ-011**: optional Idempotency-Key on `POST /meetups`, `/tasks`; Notices: Posters only (decided).
- **XREQ-011 item 4**: `PATCH /glossary/{id}` (ja, reading, translations merged per language; 409 on a duplicate term; 404 for another shop); `GlossaryOut.translations` added (bn/en kept). Frontend API types regenerated (`npm run gen:api`; typecheck passes).
- **Requests out**: XREQ-014 (Minhaz: Firebase, step-by-step), XREQ-015 (Asif: Contributor on the resource group for Anim).
- Verified: 342 tests pass; ruff, format, pyright strict, lint-imports clean; contract regenerated.

### Blocked / next
- **Azure (XREQ-015)**: until Asif grants Contributor, I cannot host the web demo (XREQ-013), set CORS for it, reset the demo or switch the AI chain before filming, or delete the old Azure PostgreSQL server. Code still deploys through `backend-deploy.yml`.
- **XREQ-011 still open**: Worker's own Follow-ups / Learning History / analytics (item 1).
- **Firebase sender**: after Minhaz sends the service-account key privately (XREQ-014).

## 2026-11-23 (database on Supabase; I own the database now)

### Completed
- **Supabase project** `wakatta` (ref `jpajxyejcbtfivrylcaf`, Tokyo, free plan, $0), created with your OK. Runbook: [backend/deploy/SUPABASE.md](../../backend/deploy/SUPABASE.md).
- **`jobs/dbinit.py` on Supabase** (merged with Asif's `3f2b7f6`, which grants the owner `USAGE, CREATE` on `public` instead of taking it): removes `public` from PUBLIC and from Supabase's Data API roles `anon` / `authenticated`, and sets `TimeZone UTC` on the app role (a pooler may drop startup options). Regression test `tests/db/test_dbinit.py` builds a Supabase-shaped server locally (fails on the old script, passes on the new); it runs when `TEST_ADMIN_DATABASE_URL` (a superuser) is set.
- **Rehearsal on the real project** (throwaway role passwords, only SCRAM verifiers sent; then set to NULL, local copies deleted): pooler logins as `wakatta_owner` / `wakatta_app` work on `aws-0-ap-northeast-1` (not `aws-1`); Alembic up → down → up clean on Postgres 17.12; seed 59 rows; the API on my laptop against the pooler passed `scripts/smoke.py` 13/13; SQL proof: 35 tables owned by `wakatta_owner`, `anon` / `authenticated` no USAGE and SELECT on 0 tables, `wakatta_app` cannot TRUNCATE or UPDATE `audit_events`; Supabase security advisor: no findings.
- **`backend/scripts/smoke.py`**: end-to-end smoke against any running API (read-only apart from sign-in, safe on the live demo), PASS/FAIL per check, exit 1 on failure. Stdlib only.
- `.env.example` shows the Supabase URL shape; README links the runbook.
- Verified: 321 tests pass (incl. the dbinit test with `TEST_ADMIN_DATABASE_URL`); ruff, ruff format, pyright strict (also `jobs/`, `scripts/`), lint-imports clean; smoke 13/13 against Supabase and against local.

### Handoff
- **Asif, XREQ-012 (P1):** run your `deploy/use-supabase.ps1` with `SUPABASE_DB_URL` on the **`aws-0-ap-northeast-1`** pooler (`aws-1` does not know this project), then `scripts/smoke.py` against the Azure URL, then delete Azure PostgreSQL. `jobs/dbinit.py` is merged with your `3f2b7f6`: your structure, plus it revokes `public` from PUBLIC on a shared database too (on Supabase PUBLIC's USAGE hands the schema to the Data API roles; revoking was rehearsed on the real project with the advisors clean) and from `anon` / `authenticated`, and sets `TimeZone UTC` on the app role.
- **Anim (me) before the cutover:** reset the Supabase database password in the dashboard and give it to Asif privately; optionally switch the Data API off (Project Settings → Data API).

## 2026-11-23

### Completed
- **WS12 Chat, XREQ-005 `[DONE]`** (`e702961`, `729118f`): `GET /threads` (unread, newest first); `POST /threads/{id}/messages` (Foreman: voice/text → AI draft at stage 3 with Check Questions and Task drafts, or `needsEdit` at stage 0 when the model is down; Worker: translated to Japanese and sent); poll `GET /threads/{id}/messages?after=` with ETag/304, no `correctIndex` for Workers; `POST /messages/{id}/send` (stage 4, Tasks created, 409 `ALREADY_SENT`); `POST /messages/{id}/answers` (`-1` = not sure → green/orange/red; a missed Glossary Term → Confusion + Follow-up; idempotent); `GET/PATCH /follow-ups` (forward only: open → clarified → understood); `POST /messages/{id}/reexplain` (one Worker, one new question; passing it closes the Follow-up); `POST /messages/{id}/nudge` (10-minute limit, audited, shows as `nudged`); Task Board `GET/POST /tasks`, `PATCH /tasks/{id}` with `If-Match` (412 `VERSION_CONFLICT`). Clarified Follow-ups go to the Support Log as item 7. Rules in `app/domain/chat.py`. Thread of another shop: 404; same shop, not a member: 403.
- **Chat seed** (`729118f`): `app/seed_chat.py` + `seed_data/chat.json`: the prototype's Line 2 Thread (m1–m2c), a direct Thread with Rahim, Tasks k1–k3; Tanvir and Sadia have seeded answers, Rahim answers live. `python -m app.seed --reset` (and the deploy seed job) runs it.
- **Security review fixes** (`729118f`): Follow-ups are listed and changed only by Foremen in that Thread; Workers move Tasks but only the Foreman changes assignees and due dates.
- **WS7 Near-miss Reports** (`54d5e0e`): `POST /near-misses` (text or voice, optional JPEG photo with EXIF removed, `Idempotency-Key`; translated to Japanese, the Worker's words kept; a voice report is stored even when the model is down), `GET /near-misses?open=`, `POST /near-misses/{id}/ack`, `GET /near-misses/{id}/photo` (shop Foremen and the reporter only).
- **WS5 skill views** (`3b33eb2`): `GET /skill-matrix`, `GET /heatmap` (one grouped query), `GET /workers/{id}/passport` (Worker: self only).
- **Seed fix** (`3b33eb2`): seeded Check Questions were `q-crane-1`, which fails the API's `q1` pattern; a wrong answer on a seeded note was a 500. Now `q1`, `q2`…
- Verified: **312 tests pass** on PostgreSQL 16.14; ruff, ruff format, pyright strict, lint-imports clean; `docs/openapi.yaml` regenerated; the real seed loads (59 rows by key).

### Handoff
- **Galib (XREQ-006):** so WS12 runs today I added `MessageDraft`, `AiTaskDraft`, `Reexplanation` (`app/ai/schemas.py`) and `draft_message.v1.md`, `reexplain.v1.md` (registered in `tests/ai/test_prompts.py`), following your house style. They are yours from here: tune, eval, change the shape if you need (tell me, `services/chat.py` builds on them). Task checks (assignees = Thread members, Machine of the shop, `dueAt` with an offset) run in `_draft_problems`.
- **Minhaz:** the Chat, Task Board, Near-miss, Skill Matrix / Heatmap / Passport routes are live; shapes in `docs/openapi.yaml`. Near-miss photo is a multipart JPEG file, not a data URL (same as flyers).
- **Asif:** no schema change needed. Known shortcut: Task drafts are not stored (returned once by the POST), so a retried draft POST has no drafts; a `task_drafts jsonb` column on `messages` would fix it if the demo needs it.

## 2026-11-23 (from Arjun: done for you on `main`)

- **XREQ-011 item 2, ETag part** (`351a27a`): `GET /board`, `/threads` and `/tasks` send an `ETag` and answer **304** on a matching `If-None-Match`. One helper, `app/api/conditional.py`, now serves those and the existing Panel and message poll (the copies in `work.py` / `chat.py` and `floor.etag` are gone). Response schemas unchanged; `openapi.yaml` differs only in three summaries. `/near-misses` is not covered: the route does not exist yet (WS7). The `Idempotency-Key` half of item 2 is still open (needs columns, so Asif's schema).
- **XREQ-011 item 1 (B-19) and item 3 (B-18):** `GET /tasks?machineId=` returns only that Machine's Tasks (another shop's id finds nothing); `GET /notices/{id}` reads one Notice with the list's rules: a Poster's own (drafts too), a published one for a Worker without `correctIndex` and with `opened` / `passed`, 404 for other districts and for a Worker on a draft. Tests in `test_chat.py` and `test_notices.py`; `openapi.yaml` and the frontend types regenerated. The Poster-or-Foreman decision in item 3 is still yours.
- **XREQ-011 item 5b / your voice-Ask request** (`eadef5f`): voice Asks keep their transcript (`AskDraft.question_text`), so forwarding sends the Senpai the question.
- Verified on a local PostgreSQL 16.14: full backend suite, ruff, pyright, lint-imports.

## 2026-11-22 (proto → production handoff from Minhaz)

Minhaz mapped the prototype (`prototypes/wakatta/`) and the frontend against `main` (`c07a8fb`). Decisions: the prototype's **Chat is in production scope (WS12, P1)**; Course stays P2 and frontend-only. Read [BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md) decision #8 and the new XREQ cards in [AGENTS.md](../../AGENTS.md) §2.2. Do the items in this order.

### Next (assigned)
1. **XREQ-005 (WS12 Chat routes)**, after Asif's Chat migration. Before it lands, only pure domain code + tests: answers → Wakatta Status (all right green, none right red, else orange; `-1` = not sure), Follow-up transitions (open → clarified → understood), Task column moves with version check.
2. Routes in card order: send (voice/text draft → `/send` creates Tasks) → Worker poll with ETag/304 (no `correctIndex`) → answers (Confusion + Follow-up on a wrong term) → Follow-ups → Re-explanation → Nudge → Task Board (`If-Match`, 412). Two-shop isolation test, as in Life.
3. Call Galib's `draft_message`, `translate`, `reexplain` (XREQ-006) through the runner; until they exist, queue fake outputs with `fake_ai.queue(...)`.
4. Chat seed: the prototype's `t_line2` thread, messages and tasks `k1`–`k3` on the seeded Workers, with seeded answers for Workers not on a phone in the demo.
5. Support Log: clarified Follow-ups → item 7. Regenerate `docs/openapi.yaml`.
6. Then **WS7 Near-miss routes** (P2, now yours; Galib does the translation). The frontend already calls `/api/near-misses` and `/api/near-misses/{id}/ack`.

### Blockers / handoff
- Needs Asif's Chat migration (XREQ-005 item 1) and Galib's `draft_message` (XREQ-006). Your earlier asks (voice Ask transcript field) are in Galib's list below.

## 2026-11-22

### Completed
- **WS11 routes** (`d03d90f`): grounded Asks (`POST/GET /asks`; the newest 20 published district Notices go to the model, then `ground()`; a cited Notice that was never sent means not covered), `POST /asks/{id}/forward` (409 `NO_ACTIVE_PAIR`; Japanese translation for the Senpai), Senpai pairs (`GET/POST/DELETE /senpai-pairs`, token returned once, SHA-256 stored, one active pair per Worker), the Senpai link (`GET /senpai/{token}`, reply translated to bn/en or Japanese only if the model is down, Meet-ups), `POST /meetups` (no future dates), `GET /support-log.csv` (forwarded Asks → item 7, Meet-ups → item 8, Notice receipts → 生活オリエンテーション, Panel Overrides). Verified: 8 API tests incl. another-shop isolation.
- **WS10 routes** (`03584d2`): `GET /posters`, `POST /notices` (text / voice / flyer photo → AI draft with Events; degraded draft with `needsEdit` when the model is down, never a 500), `PATCH /notices/{id}`, `PATCH /notices/{id}/events/{eventId}` (422 with the recurrence reason), `POST /notices/{id}/publish` (409 `EVENTS_NOT_CONFIRMED` / `NEEDS_EDIT`), `GET /notices` (Poster: own; Worker: published, no `correctIndex`, with `opened`/`passed`), `GET /notices/{id}/reach` (counts only), `GET /events?from=&to=`, `POST /receipts` (Notices), Notice Checks through `POST /checks` (`kind: notice`, one-line dispatch in `routers/work.py`), `GET /blobs/{id}` (JPEG only, EXIF/APP1 removed; a draft's photo only for its own organisation, after the automated security review). Verified: 11 API tests incl. two-district isolation.
- **CI** (`597873b`): `.github/workflows/backend-ci.yml`: Postgres 16.14 service, roles + `wakatta_test` as in the README, `uv sync --locked`, ruff, format check, pyright, lint-imports, pytest. Actions pinned to SHAs. SQL dry-run locally.
- **Life seed loader** (`8f3b2df`): `app/seed_life.py` `seed_life(db, pepper, refs, now)`, called in `app/seed.py` after `seed_work`. Posters + demo accounts (PIN 482615 / 739204), published Notices with Events, receipts, Senpai pairs with the demo tokens (`demo-tanaka-7f3a9c`, `demo-mori-2b8e41`, 180 days), Meet-ups. Verified: 3 DB tests; `python -m app.seed --reset` on the local DB, then every Life route on the seeded data (Worker feed, events, Poster, Senpai page, CSV).
- All checks after each part: **218 tests pass**; ruff, ruff format, pyright strict, lint-imports clean; `docs/openapi.yaml` regenerated (Life paths included).
- **Decision (council, WS0 blocker):** do not build a second skeleton (team rule; two migration chains would collide before code freeze). Ask Asif tonight for a partial WS0 push and offer to take named WS0 pieces he signs off (request below). No repositories against guessed models; only pure service logic if anything. Meanwhile do the unowned scored work: the proposal's Life section and its "works now vs planned" table. Strongest dissent: write services behind small interfaces now (pragmatist) vs "that is rework" (skeptic).
- Pitch deck (team draft, artifact v4): slide 5 now matches the sourced competitor check (カミナシ already has quizzes and Bengali; our differences are the machine lock, the term-level alert and the support record); slide 4 says 登録支援機関 pay (free for employers and workers, price not set) and claims only verified garbage rules (city bag, by 8:00).
- **Decision (four-voice council: architect, skeptic, pragmatist, critic): no backend `.ics` now, no `icalendar` dependency.** The browser already builds the file (`toIcs`), so a backend endpoint nobody calls scores nothing; it moves to P2 (stdlib, with folding and a `VTIMEZONE`). The real calendar bug is in `toIcs` (no 75-octet folding, no `VTIMEZONE`): filed as XREQ-002 item 9. Rejected advice: writing UTC `…Z` start times to skip the `VTIMEZONE` breaks `BYDAY` (Mon 08:00 JST is Sun 23:00 UTC), as the dateutil probe showed. Strongest dissent: build the 40-line stdlib export now anyway (pragmatist).
- Life seed data: `backend/app/seed_data/life.json` (district みなと町, 2 Posters and their demo accounts, 3 published Notices with confirmed Events, receipts, 2 Senpai pairs, 2 Meet-ups), ported from `frontend/src/services/mocks/seed.ts` so the deployed demo matches the mock one. Keys, not ids, for `seed.py` to map. Verified: 6 tests in `backend/tests/seed/test_life_seed.py` (contract shapes in ja/bn/en, references resolve, Events still upcoming on 2026-11-26, PIN rules); 74 total pass; ruff and pyright strict clean.
- Support Log CSV (architecture §9.3, research.md §1): `backend/app/domain/support_log.py` `render_support_log(events, start, end)`. One row per event, grouped by the support item it evidences (7 相談・苦情への対応, 8 日本人との交流促進, 4 生活オリエンテーション, then Panel Overrides as the safety record), times in Japan time, UTF-8 BOM + CRLF for Excel. The preamble states what it does not cover (item 10 面談, 参考様式第3-6号). Cells starting with `= + - @`, tab or CR get a leading `'` so Worker-typed text cannot run as an Excel formula (CSV injection). Verified: 10 tests in `backend/tests/domain/test_support_log.py`; 68 total pass; ruff and pyright strict clean. Merged `anim/ws10-notices` into `main` (fast-forward) and now push to `main` after each task.
- Life API contract (WS10 + WS11): request and response models in `backend/app/schemas/` (`common.py`, `notices.py`, `asks.py`, `senpai.py`) for every Life row of architecture §9.3. camelCase, `extra="forbid"`, string ids, `Translated` = `{ja, <shop languages>}` with `ja` required; Worker and Senpai responses use `CheckQuestionForWorker` (no `correctIndex`). Found while probing FastAPI 0.165: a form model with `extra="forbid"` rejects the uploaded file as an extra field, so the multipart forms carry their file fields. `EventOut.nextDates` is backend-expanded. Verified: 28 contract tests in `backend/tests/schemas/` (58 total pass); ruff and pyright strict clean; a throwaway FastAPI app generated OpenAPI from all models (12 paths, 24 schemas). Frontend changes filed as **XREQ-002**.
- Ask grounding rule (architecture §7.6): `backend/app/domain/grounding.py` `ground()`. A covered answer is kept only if it cites at least one Notice, every cited Notice was among those sent to the model, and it has text in ja and every shop language (extra languages are dropped). Anything else becomes `NOT_COVERED` (`covered=false`, `answer=null`, no sources), the shape the frontend already renders with its own `ask_not_covered` text, so no fixed answer is stored (D4: wire format follows the frontend). Verified: 6 tests in `backend/tests/domain/test_grounding.py`; all 30 domain tests pass; ruff and pyright strict clean.
- Event recurrence rules (architecture §7.5): `backend/app/domain/recurrence.py` with `validate_event` and `occurrences_between`. Only `FREQ=WEEKLY;BYDAY=…` with optional `COUNT` or UTC `UNTIL` is accepted, in the exact shape the frontend can expand (dateutil alone also accepts `1MO`, lowercase days, an `RRULE:` prefix and `COUNT` with `UNTIL`). Rules always expand in Asia/Tokyo, so a UTC start keeps its Japan weekday. Rejects past one-off Events and Events with no date in the next 366 days. Verified: 24 tests in `backend/tests/domain/test_recurrence.py` pass; ruff (architecture rule set) and pyright strict clean. Run without a project file until WS0 lands: `cd backend; uv run --no-project --with pytest --with python-dateutil python -m pytest tests/domain -q`.

### Next
- Asif: Life P1 is done (seed, CI, routes). Do you want me on Skill Matrix / Heatmap / Skill Passport (your handoff item 4)? I will not start until you say so.
- Draft the proposal's Life section and the "works now vs planned" table.

### Blockers / handoff
- **Asif:** everything is on `main`; deploy when you are ready (`alembic upgrade head`, then `python -m app.seed --reset`, which now loads Life too). No schema change was needed. I touched two of your files on purpose: `routers/work.py` (`/checks` with `kind: notice` goes to `services/notices.py`) and `media.py` (`jpeg_without_exif`).
- **Minhaz (XREQ-002, now testable against the real API):** `POST /notices` takes the flyer as a multipart **file** `photo` (JPEG), not a data URL, and no `posterId`; the confirm/publish/edit calls return the Notice; `POST /receipts` body is `{target: {kind: "notice", id}}`; Notice Checks go to `POST /checks` with `target.kind = "notice"`; photos at `/api/blobs/{id}` need the Bearer header.
- **Galib (WS1):** a voice Ask stores no transcript (`AskOut.text` is empty), because `AskDraft` has no transcript field; please add one (e.g. `question_text`) to the schema and `answer_ask` prompt, and I will store it. Life prompts get an empty glossary (Posters have no shop).
- **WS6 (Briefings):** `POST /receipts` with `kind: briefing` returns 422 until Briefings exist; extend `routers/life.py` `open_receipt`.
- ~~Asif (WS0) blocker~~: resolved, WS0 is on `main` (2026-11-22 ~22:47) and Life is built on it.
- Minhaz and Galib: the demo flyer's recyclables Event (every Wednesday) is not confirmed; 佐世保市 sets recyclable days per area, and some 町内会 collect once a month. Verified only: designated bag for burnable and non-burnable, see-through bags for recyclables, out by 8:00; 粗大ごみ via クリーンセンター or paid home collection. Collection frequencies are unconfirmed. XREQ-002 item 12; sources in the plan's "Facts to verify". (Correction: `fbea045` stated "twice a month" and "twice a week" from a search summary, not from the city's pages.)
- **Schema request for Asif (needs a migration):** `asks` has no column for the Japanese translation of the Worker's question, but the plan forwards the Ask "translated to Japanese" to the Senpai (a 自治会 volunteer like 森 早苗 cannot read Bengali). Please add `asks.text_ja text null`, filled when the Ask is forwarded; the contract already has `ForwardedAsk.textJa`.
- Asif (WS0): `app/schemas/common.py` holds `CamelModel`, `Translated`, `IdIn`, `CheckQuestion`, `DateRange`; move them into `app/api/` if you prefer, and the Life schemas follow. They need `fastapi`, `pydantic` and `python-multipart` in `pyproject.toml`.
- Galib (WS1): `answer_ask` should pass its parsed output through `app.domain.grounding.ground()` (with the ids of the Notices it sent) before the service writes the Ask.
- WS0 (Asif): the domain files use implicit namespace packages (no `__init__.py`), so they merge cleanly into your skeleton. Please add `python-dateutil` and `types-python-dateutil` (dev) to `pyproject.toml`.
- Minhaz (FYI, no XREQ needed yet): `frontend/src/domain/calendar.ts` `nextOccurrences` ignores `COUNT` and `UNTIL`, so a rule that has ended still shows dates in the preview. The backend accepts both, as the architecture specifies.

### Related issues / commits / PRs
- `c6939ba` feat(domain): add event recurrence rules (branch `anim/ws10-notices`)
- `47509f6` feat(domain): add ask grounding rule
- `e281beb` feat(schemas): add the Life API contract
- `6d429c8` chore(xreq): add XREQ-002 for the Life contract
- `65464cc` feat(domain): add support log CSV rendering
- `5415fc5` feat(seed): add Life demo seed data
- `b4c3882` chore(xreq): XREQ-002 items 9–11 (ics folding, backend support log, notice termId)
- `fbea045` docs(plan): checked Sasebo garbage rules; XREQ-002 item 12
- `8f3b2df` feat(seed): Life seed loader · `597873b` ci: backend CI · `03584d2` feat(life): Notices/Events/receipts · `d03d90f` feat(life): Asks/Senpai/Meet-ups/Support Log
