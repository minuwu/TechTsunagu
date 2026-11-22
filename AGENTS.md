# AGENTS.md — Cross-Functional Team Protocol & Coding Agent Instructions

> **CRITICAL DIRECTIVE FOR ALL CODING AGENTS (Claude Code, Cursor, Antigravity, Copilot, Gemini CLI, Codex):**
> Whenever you start or resume work in this repository:
> - **STEP 0 — XREQ queue.** Read Section 2. If a request assigned to your team or contributor is `[PENDING]`, **suspend all backlog work and fulfil it first**.
> - **STEP 1 — Align before coding.** Interview the user (your agent's interactive question tool, or a grill-me style round) to settle ambiguous requirements, constraints and edge cases before you lock in a plan.
> - **STEP 2 — Check standards on the web.** Look up official docs, current API signatures and library best practices instead of relying on unverified memory.
> - **STEP 3 — Knowledge graph first.** For any question about this codebase run `graphify query "<question>"` before grep/glob/read (Section 3.4).
> - **STEP 4 — Quality bar.** Every change must satisfy [docs/ENGINEERING_GUIDELINES.md](docs/ENGINEERING_GUIDELINES.md) (checklist `G-01`…`G-53`). Turn every correction into a deterministic check, keep the codebase free of slop, attach verification evidence (Section 3.6).
>
> Cross-functional dependencies ALWAYS take precedence over isolated feature work.

---

## Table of Contents
1. [Overview & Operating Philosophy](#1-overview--operating-philosophy)
2. [Active Cross-Functional Request Queue (XREQ)](#2-active-cross-functional-request-queue-xreq)
3. [Coding Agent Operational Protocol](#3-coding-agent-operational-protocol)
4. [Per-Team Instructions & Contracts](#4-per-team-instructions--contracts)
5. [Per-Contributor Registry & Instructions](#5-per-contributor-registry--instructions)
6. [File Management & Maintenance Manual](#6-file-management--maintenance-manual)
7. [Completed & Archived Requests Log](#7-completed--archived-requests-log)

---

## 1. Overview & Operating Philosophy

In hackathons, velocity dies from **unresolved cross-team dependencies**. When Frontend waits on a Backend endpoint, or Backend changes a payload without telling Frontend, both stall and integration fails.

This file is:
1. **The Machine-Readable Dispatch Board** — coding agents parse it to find urgent tasks requested by partner teams.
2. **The Bidirectional Contract Ledger** — one place to declare requested schemas, endpoints, mocks and response shapes.
3. **The Team Operating Standard** — per-team and per-contributor rules that keep branches synced and work unblocked.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       BIDIRECTIONAL WORKFLOW ENGINE                         │
│                                                                             │
│   Frontend Needs API ───► Creates [XREQ] ───► Backend Agent Discovers       │
│                                               │ (Maximum Priority)          │
│                                               ▼                             │
│   Frontend Unblocked ◄─── Marks [RESOLVED] ◄── Implements & Verifies        │
│   (Switches Mock)         with Contract Spec                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Active Cross-Functional Request Queue (XREQ)

*Coding Agents: read this table first. If any request targeting your team is `[PENDING]`, start on it immediately.*

### 2.1 Active Queue Summary

| Request ID | Priority | From | To | Target / Title | Status | Updated (UTC+6) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `XREQ-001` | P1 | Backend (Asif) | Frontend (Minhaz) | Auth, ids and list shape for the real API ([BACKEND_ARCHITECTURE.md §17](docs/BACKEND_ARCHITECTURE.md#17-frontend-contract-changes)) | `[PENDING]` | 2026-11-22 19:10 |
| `XREQ-002` | P1 | Backend (Anim) | Frontend (Minhaz) | Life contract: Notices, Events, Asks, Senpai, Meet-ups (`backend/app/schemas/`) | `[PENDING]` | 2026-11-22 20:06 |
| `XREQ-003` | P1 | AI (Galib) | Backend (Asif) | Review the free-AI plan: model chain, budgets, Groq Whisper, settings; Briefing receipts route ([AI_PLAN.md](contributors/galib/AI_PLAN.md)) | `[PENDING]` | 2026-11-22 22:40 |
| `XREQ-004` | P2 | AI (Galib) | Frontend (Minhaz, Arjun) | "Dictate instead" fallback, degraded-draft message, provenance line ([AI_PLAN.md](contributors/galib/AI_PLAN.md) §2, §8) | `[PENDING]` | 2026-11-22 22:40 |

*(When adding a request: append a row above AND add a detailed card in 2.2. Never insert in the middle.)*

### 2.2 Detailed Request Cards

#### `[XREQ-001]` Auth, ids and list shape for the real API
- **Priority**: `P1 (High)`
- **From**: Backend (Asif)
- **To**: Frontend (Minhaz)
- **Status**: `[PENDING]`
- **Created**: `2026-11-22 19:10 (UTC+6)`
- **Target Interface / Endpoint**: `frontend/src/services/wakatta.ts`, `frontend/src/services/api.ts`, `frontend/src/features/session/`

**Requirement Details:**
The backend supports many shops and personal accounts ([ADR 0009](docs/adr/0009-shop-and-district-scopes.md), [ADR 0010](docs/adr/0010-personal-accounts-and-pins.md)). The full list is in [BACKEND_ARCHITECTURE.md §17](docs/BACKEND_ARCHITECTURE.md#17-frontend-contract-changes); the ones that matter most:
1. Replace `credentials: 'include'` with `Authorization: Bearer <token>`. Store the token from login instead of `'1'` in `wakatta.pin.<role>` (the stored flag can be forged in dev tools; the backend refuses every call without a valid token anyway).
2. Login: `GET /api/auth/roster?code=&role=` → pick name → `POST /api/auth/login`. Workers and Foremen each have their own account; when the response has `mustChangePin: true`, show a "choose your PIN" screen (`PUT /api/auth/me/pin`) before anything else. Foreman screens to add: create Worker / Foreman, reset PIN, deactivate (`/api/workers`, `/api/foremen`).
3. Drop `workerId` from request bodies; the backend takes it from the token.
4. Lists return `{items, nextCursor}`; send `Idempotency-Key` (a `crypto.randomUUID()` per user action) on Checks, drafts, Asks and Near-miss Reports.
5. Briefings: draft → patch → publish by id. Senpai reply sends `{replyJa}`. Photos are multipart files, not data URLs.

**Expected Request (Payload / Query Params / Headers):**
```json
POST /api/auth/login
{ "userId": "12", "pin": "1234" }
```

**Expected Response (Status Code + Payload / Error Codes):**
```json
{ "success": true, "data": { "token": "opaque", "expiresAt": "2026-12-22T10:00:00Z",
  "user": { "id": "12", "displayName": "Rahim", "role": "worker", "language": "bn", "shopId": "1", "districtId": "1" } } }
```
Errors: `401 UNAUTHORIZED` (wrong PIN), `423 PIN_LOCKED` (`details.until`), `429 RATE_LIMITED`.

**Fulfillment & Proof (filled by recipient on completion):**
- **Branch / Commit**: 
- **Status / Notes**: 
- **Verification Proof**: 

#### `[XREQ-002]` Life contract: Notices, Events, Asks, Senpai, Meet-ups
- **Priority**: `P1 (High)`
- **From**: Backend (Anim)
- **To**: Frontend (Minhaz)
- **Status**: `[PENDING]`
- **Created**: `2026-11-22 20:06 (UTC+6)`
- **Target Interface / Endpoint**: `frontend/src/services/wakatta.ts` (Life functions), `frontend/src/features/{community,worker,senpai}/`

**Requirement Details:**
The Life request and response models are in `backend/app/schemas/notices.py`, `asks.py`, `senpai.py` (branch `anim/ws10-notices`, `e281beb`); `docs/openapi.yaml` is generated from them once WS0 lands. Changes beyond XREQ-001 (bodies reject unknown fields with 422):
1. `POST /api/notices` multipart: `sourceKind` plus exactly the one source it names (`text`, `audio` or `photo` file). No `posterId`: it comes from the session.
2. `PATCH /api/notices/{id}/events/{eventId}` returns the Event with `nextDates`, expanded by the backend with `COUNT` and `UNTIL` applied. Show these to the Poster: `nextOccurrences` in `domain/calendar.ts` ignores `COUNT` and `UNTIL`, so an ended rule still shows dates. A bad date is 422 with `details.reason` `unsupported_rule`, `past` or `no_upcoming`.
3. `POST /api/notices/{id}/publish` refuses with **409** `EVENTS_NOT_CONFIRMED` (the mock throws 422).
4. Notices add `needsEdit` (the AI fell back; the Poster must finish the draft) and `publishedAt`. `photo` is `/api/blobs/{id}`, which needs the Bearer header, so `<img src>` cannot load it directly: fetch it with the token and show `URL.createObjectURL(blob)`.
5. Worker `GET /api/notices` items carry `opened` and `passed` from their receipt, and the question has no `correctIndex` (grading is server-side, `POST /api/checks`).
6. `POST /api/meetups` and `POST /api/senpai/{token}/meetups` take `{kind, date, note}` only: no `pairId` or `loggedBy`.
7. The Senpai link token is returned only by `POST /api/senpai-pairs` (shown once; only its hash is stored). `GET /api/senpai-pairs` has no `token`, so the Senpai screen loads `GET /api/senpai/{token}` instead of finding the pair in the list.
8. Optional: `GET /api/events?from=&to=` (at most 62 days) returns `[{eventId, noticeId, title, at}]` for today / tomorrow, so the Worker screen need not expand rules itself.
9. `toIcs` in `domain/calendar.ts` (the "Add to calendar" file, built in the browser): RFC 5545 needs lines folded at **75 octets** (`\r\n` + a space), never splitting a UTF-8 character, and a `VTIMEZONE` block for `TZID=Asia/Tokyo` (one `STANDARD`, `TZOFFSETFROM`/`TZOFFSETTO` `+0900`). Without folding, a Japanese or Bengali `SUMMARY` passes 75 octets after about 25 characters and Google or Outlook can mangle it. Keep `DTSTART;TZID=Asia/Tokyo` local time: UTC `…Z` times shift `BYDAY` to the wrong weekday (Mon 08:00 JST is Sun 23:00 UTC). The backend `.ics` endpoint is deferred to P2, so this browser file is the one judges see.
10. Support Log: with the real API, download `GET /api/support-log.csv?from=&to=` instead of building the CSV in the browser. The backend file groups rows by the official support item, states what it does not cover, and escapes cells that start with `= + - @` so Worker-typed text cannot run as an Excel formula.
11. Notice questions have `termId: null`: Glossary Terms belong to one shop, Notices to a district shared by several employers (the mock's drill question points at `g-hinanjo`).
12. Demo flyer (`services/mocks/ai.ts` `noticeDraft`): the recyclables Event (`FREQ=WEEKLY;BYDAY=WE`, 「資源物…毎週 水」) is **not confirmed**. 佐世保市 sets recyclable collection days per area on its calendar (資源物収集日割表), and some 町内会 collect once a month (plan, "Facts to verify"). Make recyclables a step without an Event (「資源物（缶・びん・ペットボトル）：透明か半透明の袋に入れて、朝8時まで。日にちは地区のカレンダーで確認。」) and drop that Event. Verified on the city's 出し方 page: designated bag for burnable and non-burnable, see-through bags for recyclables, out by 8:00. Galib: the same fix in the seeded AI cache fixture for the filmed flyer.

**Expected Request (Payload / Query Params / Headers):**
```json
POST /api/meetups
{ "kind": "drill", "date": "2026-12-06", "note": "防災訓練に一緒に行った" }
```

**Expected Response (Status Code + Payload / Error Codes):**
```json
GET /api/senpai/{token} → 200
{ "success": true, "data": { "senpaiName": "田中 健",
  "worker": { "displayName": "Rahim Uddin", "language": "bn" },
  "asks": [{ "id": "31", "text": "আমার ফ্যান নষ্ট হয়ে গেছে।", "textJa": "扇風機が壊れました。", "createdAt": "2026-11-22T10:00:00Z", "replyJa": null }],
  "events": [{ "eventId": "5", "noticeId": "2", "title": { "ja": "防災訓練", "bn": "দুর্যোগ মহড়া", "en": "Disaster drill" }, "at": "2026-12-06T09:00:00+09:00" }],
  "meetups": [] } }
```
Errors: `404 NOT_FOUND` (wrong or revoked Senpai token), `409 NO_ACTIVE_PAIR` (forwarding an Ask without a Senpai), `422 VALIDATION_ERROR`.

**Fulfillment & Proof (filled by recipient on completion):**
- **Branch / Commit**: 
- **Status / Notes**: 
- **Verification Proof**: 

#### `[XREQ-003]` Review the free-AI plan (model chain, budgets, Whisper, settings)
- **Priority**: `P1 (High)`
- **From**: AI (Galib)
- **To**: Backend (Asif)
- **Status**: `[PENDING]`
- **Created**: `2026-11-22 22:40 (UTC+6)`
- **Target Interface / Endpoint**: `docs/BACKEND_ARCHITECTURE.md` §10.3, §10.4; `backend/.env.example`; `backend/pyproject.toml`; `app/ai/`

**Requirement Details:**
The team agreed to use every free AI API, so the demo does not hang on one model's 20-a-day quota. The full proposal is [contributors/galib/AI_PLAN.md](contributors/galib/AI_PLAN.md). This is a proposal: accept, change or reject each item, and I build WS1 to match.
1. Two model chains behind one setting `AI_CHAIN=dev|demo` (replaces `GEMINI_MODEL` and `GEMINI_MODEL_DEV`): `dev` = 3.5 Flash-Lite → 3.1 Flash-Lite; `demo` = 3.8 Flash → 3.6 Flash → 3.5 Flash-Lite → 3.1 Flash-Lite.
2. Fallback ladder (§10.4): cache looked up for each chain model → the chain in order (a model with `ai_usage.exhausted` or at budget is skipped) → one validation retry → seeded result → **Groq Whisper transcript for voice input** → degraded. Add `transcribed` to `note_versions.ai_outcome`.
3. Budgets per model in `ai_usage` (already keyed by `(day, model)`, so no schema change): 18/day for 3.8 and 3.6 Flash, 450 for both Flash-Lites, 1,800 for Whisper; per-minute buckets 4 / 12 / 15.
4. Settings: `GROQ_API_KEY` (optional; empty disables the Whisper step) and `AI_CHAIN` in `.env.example`; `httpx` as a runtime dependency for Groq.
5. From [WS6_BRIEFING_PLAN.md](contributors/galib/WS6_BRIEFING_PLAN.md) (branch `galib/ws1-ai`): OK `GET /briefings/{id}/receipts` for the Foreman's "understood / opened only / not opened" view, and seed shop A's Glossary from `backend/tests/fixtures/ai/glossary.json` (or move that file into `seed_data/`).
6. For XREQ-004's provenance line: include `approvedBy` (display name) and `needsEdit` in the Note Version response; both columns already exist (§6).
7. **Ownership of the remaining pipelines (Asif and Anim, by Mon 03:00 BST):** [WS1_INTEGRATION_PLAN.md](contributors/galib/WS1_INTEGRATION_PLAN.md) §1. Proposal: Galib builds `app/services/ai_pipelines.py` (`draft_briefing`, `draft_notice`, `answer_ask`, `translate`) by Mon 10:00; Anim's services call them; Asif keeps `notes.py`. If nobody objects by Mon 03:00, I build to it.

**Expected Request (Payload / Query Params / Headers):**
```json
GET /api/briefings/41/receipts
Authorization: Bearer <foreman token>
```

**Expected Response (Status Code + Payload / Error Codes):**
```json
{ "success": true, "data": {
  "understood": [{ "id": "12", "displayName": "Rahim Uddin" }],
  "openedOnly": [{ "id": "13", "displayName": "Sadia Akter" }],
  "notOpened": [{ "id": "14", "displayName": "Tanvir Hasan" }] } }
```
Errors: `403 FORBIDDEN` (not a Foreman), `404 NOT_FOUND` (another shop's Briefing), `409` if the Briefing is still a draft.

**Fulfillment & Proof (filled by recipient on completion):**
- **Branch / Commit**: 
- **Status / Notes**: 
- **Verification Proof**: 

#### `[XREQ-004]` "Dictate instead", degraded-draft message and provenance line
- **Priority**: `P2 (Normal)`
- **From**: AI (Galib)
- **To**: Frontend (Minhaz, Arjun)
- **Status**: `[PENDING]`
- **Created**: `2026-11-22 22:40 (UTC+6)`
- **Target Interface / Endpoint**: Foreman record and Briefing screens, Poster voice notice (`frontend/src/ui/v2/screens/foreman/ForemanRecordScreen.tsx`, `community/CommunityScreen.tsx`), shared draft view

**Requirement Details:**
Part of the free-AI plan ([AI_PLAN.md](contributors/galib/AI_PLAN.md) §2, §8). Proposal: accept, change or reject each item.
1. **"Dictate instead":** when a voice draft comes back with `needsEdit: true`, or the phone is offline, offer a button that uses the browser's speech recognition (`SpeechRecognition` / `webkitSpeechRecognition`, `lang = 'ja-JP'`) to fill the text box, then send it on the existing text path. Hide the button where the API is missing (Firefox). Support is partial in Chrome and Safari ([caniuse](https://caniuse.com/speech-recognition)), so please try it on the team's phones.
2. **Degraded drafts:** show `needsEdit` clearly, e.g. "AI unavailable: please check or type the steps" (ja / en / bn), instead of an empty form.
3. **Provenance line** on AI drafts and approved instructions: "AI draft · checked against back-translation · approved by 山口 · 11/22". `recordedBy` and `approvedAt` are already on `NoteVersion`; `approvedBy` and `needsEdit` are stored in the database (§6) and requested in the Note Version response under XREQ-003 item 6.

**Expected Request (Payload / Query Params / Headers):**
```json
POST /api/machines/3/notes   (multipart: text = what the browser heard, Idempotency-Key)
```

**Expected Response (Status Code + Payload / Error Codes):**
```json
{ "success": true, "data": { "id": "57", "status": "draft", "needsEdit": false, "recordedBy": "山口 隆" } }
```
No new error codes.

**Fulfillment & Proof (filled by recipient on completion):**
- **Branch / Commit**: 
- **Status / Notes**: 
- **Verification Proof**: 

### 2.3 Blank XREQ Template (copy & paste)

```markdown
#### `[XREQ-00X]` [Brief Actionable Title: e.g., POST /api/items Endpoint]
- **Priority**: `[P0 (Blocker) | P1 (High) | P2 (Normal)]`
- **From**: `[Frontend / Backend / AI] ([Contributor Name])`
- **To**: `[Backend / Frontend / AI] ([Contributor Name / Any])`
- **Status**: `[PENDING]`
- **Created**: `YYYY-MM-DD HH:MM (UTC+6)`
- **Target Interface / Endpoint**: `[e.g., POST /api/analyze or UI Error Toast]`

**Requirement Details:**
[Why this is needed, what feature it enables, expected behavior.]

**Expected Request (Payload / Query Params / Headers):**
```json
{ "field": "value" }
```

**Expected Response (Status Code + Payload / Error Codes):**
```json
{ "success": true, "data": {} }
```

**Fulfillment & Proof (filled by recipient on completion):**
- **Branch / Commit**: `[branch-name or commit-hash]`
- **Status / Notes**: `[Implemented route / schema / component]`
- **Verification Proof**: `[curl command, Swagger link, or test output]`
```

### 2.4 Worked Example (illustrative only — not a real request)

```markdown
#### `[XREQ-EXAMPLE]` POST /api/auth/login
- **Priority**: `P0 (Blocker)`
- **From**: Frontend (Minhaz)
- **To**: Backend (Asif)
- **Status**: `[RESOLVED]`
- **Created**: YYYY-MM-DD 18:00 (UTC+6)
- **Target Endpoint**: `POST /api/auth/login`

**Requirement Details:** Navbar + session state need a login endpoint returning a token and user object.

**Expected Request:** `{ "email": "user@example.com", "password": "secret" }`
**Expected Response (200):** `{ "success": true, "data": { "token": "opaque-token", "user": { "id": "usr_101", "name": "Jane", "email": "user@example.com", "role": "admin" } } }`

**Fulfillment & Proof:**
- **Branch / Commit**: `asif/auth-login` (`<commit-hash>`)
- **Status / Notes**: Route live; persistence stubbed.
- **Verification Proof**: `curl -X POST localhost:8000/api/auth/login -d '{...}'` returns 200 matching schema; visible at `http://localhost:8000/docs`.
```

---

## 3. Coding Agent Operational Protocol

```mermaid
flowchart TD
    Start(["Agent Initiated"]) --> Step0["Step 0: Read AGENTS.md Section 2"]
    Step0 --> CheckQueue{"Any PENDING request<br/>for my team?"}
    CheckQueue -- YES --> Claim["Mark IN_PROGRESS"]
    Claim --> Align1["Step 1-2: Align + check standards"]
    Align1 --> Implement["Implement endpoint / model / UI"]
    Implement --> Verify["Verify: test or curl"]
    Verify --> Document["Fill Fulfillment, mark RESOLVED"]
    Document --> Commit["Commit: fix: fulfill XREQ-XXX"]
    Commit --> Backlog["Resume backlog"]
    CheckQueue -- NO --> Align2["Step 1-2: Align + check standards"]
    Align2 --> Graph["Step 3: graphify query for context"]
    Graph --> ReadLog["Read contributor WORK_LOG.md + task"]
    ReadLog --> Work["Execute planned work"]
```

### 3.1 Step 0 — Check the XREQ Queue
1. Open Section 2.1. Filter rows where `To` matches your team or contributor.
2. Any `[PENDING]` row → change it to `[IN_PROGRESS]`, commit, then implement.
3. Only when the queue is clear for your team may you touch backlog work.

### 3.2 Rules for Recipient & Requester Agents

**Recipient (fulfilling):**
1. **Never ignore `[PENDING]`.** If Frontend logged an `XREQ` for an endpoint, that endpoint is Job #1.
2. **Speed over perfection.** If persistence isn't ready, **return mock data matching the contract immediately**, then wire real logic. Unblock the partner in minutes.
3. **Document proof.** Never mark `[RESOLVED]` without branch/commit + verification snippet.

**Requester (submitting):**
1. **Never guess field names.** Log an `XREQ` with the exact JSON you need.
2. **Provide a fallback mock immediately** behind the `USE_MOCK` toggle (Section 4.1) so your side keeps moving.
3. **Verify and close.** When `[RESOLVED]`, test the live endpoint, switch mock off, mark `[VERIFIED_AND_CLOSED]`.

### 3.3 Steps 1–2 — Align, Then Check Standards
1. **Align first.** Before a plan or design assumption, ask the user about ambiguous requirements, constraints and edge cases. Ask decisions interactively (your agent's question tool), with a recommended option first.
2. **Check standards.** Search official docs, current API signatures, RFCs and library best practices before writing code that depends on them.
3. **Team knowledge packs (optional).** If your team ships agent skills, list the routing (domain → skill) here and load the matching one before coding in that domain.

### 3.4 Step 3 — Query the Knowledge Graph First

`graphify-out/` is the local knowledge graph of this repo (untracked in git to prevent sync noise).
- Codebase question → `graphify query "<question>"` before any grep/glob/read.
- Relationship → `graphify path "A" "B"`. One concept → `graphify explain "X"`.
- Broad architecture review only → read `graphify-out/GRAPH_REPORT.md`.
- **After changing code** → `graphify update .` (local AST update, free). **After changing docs** → `/graphify . --update`.
- `graphify` missing → `pip install graphifyy`, or `python -m graphify`.

### 3.5 Git Rules for Agents
- **Branch**: `<name>/<short-topic>` (e.g. `minhaz/dashboard-ui`, `asif/auth-login`). Direct pushes to `main` are allowed **only after `git pull origin main`**; the pre-push hook rejects a push that is missing remote commits.
- **Merge**: small, safe changes may go straight to `main` after pulling. Anything that touches another team's area, the API contract or shared config goes through a PR with one teammate's approval. Keep PRs small (< 400 lines when possible).
- **Commit messages**: Conventional Commits (`feat:`, `fix:`, `docs:`, `chore(xreq):`). No AI/tool attribution trailers.
- **Sync**: the pre-push hook ([`.githooks/pre-push`](.githooks/pre-push)) fetches and merges `origin/main` before every push. It is enabled by `scripts/setup.sh` / `scripts/setup.ps1`. Commit or stash before pushing — the hook merges and will refuse a dirty tree.
- **CI**: [`.github/workflows/verify-branch-sync.yml`](.github/workflows/verify-branch-sync.yml) fails any PR that is behind `main`.
- **Secrets**: never commit `.env`. Keep `.env.example` current in `frontend/` and `backend/`.

### 3.6 Quality Bar — Engineering Guidelines

[docs/ENGINEERING_GUIDELINES.md](docs/ENGINEERING_GUIDELINES.md) is the project-wide quality checklist (items `G-01`…`G-53`). It applies to frontend, backend and AI code, and to docs and process.

1. **Before a PR or marking an `XREQ` `[RESOLVED]`:** walk the checklist for the areas you touched. Any box you cannot tick gets a one-line reason in the PR body or the XREQ proof block.
2. **Corrections become checks.** When you correct an agent or a teammate, add a lint rule, type, test or architecture rule (G-01). Edit prose rules only when no check can express it.
3. **Reviews start with evidence** (tests, screenshots, curl proof), then code (G-21).
4. **Found a recurring mistake?** Propose a new `G-xx` item with the check that enforces it, in the same PR (G-52).
5. Audit reports cite items by ID, `file:line`, priority (P0–P3, see the guide's priority table) and a fix.

---

## 4. Per-Team Instructions & Contracts

### 4.1 Frontend Team Directives
- **Mission**: responsive, intuitive, error-resilient UI for every target user journey.
- **Contract-First**:
  - API client in one place, e.g. `frontend/src/services/api.ts`.
  - Mock toggle driven by env, default ON until integration:
    ```ts
    const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true';
    ```
    (Next.js: `process.env.NEXT_PUBLIC_USE_MOCK`.)
  - Mocks mirror [docs/API_SPEC.md](docs/API_SPEC.md) or the open `XREQ` exactly.
- **Requesting from Backend**: file an `XREQ` with request + response JSON, including error states (400/401/404/500). Don't break existing components while waiting.
- **UI states**: always handle loading, empty and error (skeletons, toasts).
- **Replaceable UI**: the UI will change drastically and several UI versions may coexist. A redesign replaces presentation only: logic, data access, copy and routes are written once and never rewritten for a new look. Each UI version is a kit in `frontend/src/ui/<version>/` on top of one shared headless core; follow [docs/FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md) (items `A-01`…`A-09`).
- **Stack**: TanStack Start + React + TypeScript + Vite. Versions, data flow and state rules live in [docs/FRONTEND_STACK.md](docs/FRONTEND_STACK.md) (items `S-01`…`S-35`); layers in [docs/FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md).
- **Japan standards**: judges are Japanese. Every UI change must follow [docs/FRONTEND_REGULATIONS.md](docs/FRONTEND_REGULATIONS.md) (items `F-01`…`F-53`, pre-demo checklist).

### 4.2 Backend Team Directives
- **Mission**: fast, documented, reliable REST/SSE endpoints and business logic.
- **SLA**: acknowledge an `XREQ` and ship a mock or live response within 15–30 minutes.
- **CORS**: allow `*` or `localhost:<port>` in dev from the first commit.
- **Docs**: every endpoint visible in Swagger (`/docs`, `/openapi.json`) or equivalent.
- **Envelope**: every response uses the envelope in `docs/API_SPEC.md` §1.
- **No silent breaking changes**: never rename/remove a response key without a `P0` `XREQ` to Frontend.

### 4.3 AI / LLM Team Directives
- **Mission**: the model-powered feature that wins the demo. Ship a deterministic fallback for every AI call so the demo never hangs.
- **Streaming**: if using SSE, declare the chunk format (e.g. `data: {"text": "chunk"}\n\n`) in the `XREQ`.
- **Prompts** live in `backend/app/prompts/` (or equivalent), versioned in git, never inline in route handlers.
- **Keys**: `.env` only; `.env.example` lists every variable with a dummy value.
- **Shared types**: if TypeScript is used on both sides, keep shared interfaces in `shared/types/` or mirror them in `docs/API_SPEC.md`.

---

## 5. Per-Contributor Registry & Instructions

### 5.1 Contributor Roster

| Contributor | GitHub Handle | Primary Focus | Branch Prefix | Work Log |
| :--- | :--- | :--- | :--- | :--- |
| **Minhaz** | `minuwu` | Frontend / Full-Stack / Architecture | `minhaz/*` | `contributors/minhaz/WORK_LOG.md` |
| **Asif** | `MdAsif-Hossain` | Backend / Services / Database | `asif/*` | `contributors/asif/WORK_LOG.md` |
| **Anim** | `anim710` | Backend / Services / Database | `anim/*` | `contributors/anim/WORK_LOG.md` |
| **Galib** | `Ga-lib` | AI / LLM | `galib/*` | `contributors/galib/WORK_LOG.md` |
| *(Next Teammate)* | *TBD* | *Assigned Role* | `<name>/*` | `contributors/<name>/WORK_LOG.md` |

Roles are a starting point; confirm them at kickoff. Per-person setup checklists are in [TEAM_INSTRUCTIONS.md](TEAM_INSTRUCTIONS.md).

### 5.2 Minhaz
- **Role**: lead architecture, Frontend / Full-Stack integration, repo hygiene.
- **Agent instructions**: fulfil `XREQ`s where `To: Frontend` or `To: Minhaz`; follow Frontend directives (4.1); keep `AGENTS.md` current.

### 5.3 Asif
- **Role**: Backend engineering, data models, API integrations.
- **Agent instructions**: fulfil `XREQ`s where `To: Backend` or `To: Asif`; follow Backend directives (4.2); keep Swagger and `docs/API_SPEC.md` in sync before committing.

### 5.4 Anim
- **Role**: Backend engineering alongside Asif: services, data models, API integrations.
- **Agent instructions**: fulfil `XREQ`s where `To: Backend` or `To: Anim`; follow Backend directives (4.2); coordinate with Asif on shared `backend/` files.

### 5.5 Galib
- **Role**: AI / LLM engineering: model-powered features, prompts, deterministic fallbacks (Section 4.3).
- **Agent instructions**: fulfil `XREQ`s where `To: AI` or `To: Galib`; follow AI directives (4.3); coordinate with Asif and Anim on shared `backend/` files and declare any SSE chunk format in an `XREQ`.

### 5.6 Onboarding a New Contributor (2 minutes)
1. Add a row to 5.1 and a short 5.x section (role, agent instructions).
2. `cp -r contributors/_template contributors/<name>` and fill the header.
3. Run `scripts/setup.sh` (or `scripts/setup.ps1` on Windows).
4. Add a column to the per-person table in [TEAM_INSTRUCTIONS.md](TEAM_INSTRUCTIONS.md).

---

## 6. File Management & Maintenance Manual

### 6.1 Why This File Exists
Single source of truth for: humans coordinating asynchronously; agents discovering blockers without being prompted; Frontend/Backend/AI sharing one contract. Agent-specific entry files (`CLAUDE.md`, `GEMINI.md`, `.cursor/rules/agents.mdc`, `.github/copilot-instructions.md`) only point here; put rules in this file, never in them.

### 6.2 Creating a Request
1. Anyone (human or agent) can create one.
2. Next sequential ID: `XREQ-001`, `XREQ-002`, …
3. Fill the template (2.3), add the card to 2.2 and a row to 2.1.
4. Commit: `git commit -m "chore(xreq): add XREQ-00X for <endpoint/feature>"` and push.

### 6.3 Lifecycle
```
[PENDING] ───► [IN_PROGRESS] ───► [RESOLVED] ───► [VERIFIED_AND_CLOSED] ───► archived (Section 7)
```
- `[PENDING]` logged, waiting for recipient.
- `[IN_PROGRESS]` recipient claimed it.
- `[RESOLVED]` implemented, verified, committed; Fulfillment block complete.
- `[VERIFIED_AND_CLOSED]` requester tested integration and closed it.

### 6.4 Merge-Conflict Prevention
1. **Append only** at the bottom of the active list.
2. **Atomic edits**: change only the one card and row you own.
3. **Pre-push sync** hook stays enabled.

### 6.5 Archiving
Move `[VERIFIED_AND_CLOSED]` requests to Section 7 during cleanups. Keep the active queue under 10 items.

---

## 7. Completed & Archived Requests Log

*Historical log. Do not delete.*

| Request ID | Finished | From | To | Summary | Final Commit |
| :--- | :--- | :--- | :--- | :--- | :--- |
| *(none yet)* | | | | | |

---
*End of AGENTS.md. Keep this file current every sprint.*
