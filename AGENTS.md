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
| *(none yet)* | | | | | | |

*(When adding a request: append a row above AND add a detailed card in 2.2. Never insert in the middle.)*

### 2.2 Detailed Request Cards

*(empty — add cards here using the template in 2.3)*

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
