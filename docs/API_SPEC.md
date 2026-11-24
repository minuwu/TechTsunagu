# API Specification (Shared Contract)

**Version** 0.2.0 · **Updated** 2026-11-24 · **Owner** Backend

This is the **single source of truth** for the API between Frontend, Backend and AI. Build against it now using mocks. Any change goes through an `XREQ` in [AGENTS.md](../AGENTS.md). Never rename a field silently.

A machine-readable copy lives in [openapi.yaml](openapi.yaml) (OpenAPI 3.1). Change both in the same PR.

**Product in one paragraph:** Wakatta? helps foreign Workers in Japanese factories understand what their Foreman said. A Foreman speaks an instruction in Japanese; each Worker gets it in their language and answers short Check Questions; the Machine Panel turns green only for a Worker who understood. The Life side does the same for district Notices (garbage, disaster drills) and connects each Worker with a Senpai.

## 1. Conventions

| Item | Rule |
| :--- | :--- |
| Base URL (local) | `http://localhost:8000` |
| Contract file | [openapi.yaml](openapi.yaml), validated with `npx -y @redocly/cli lint docs/openapi.yaml` |
| Mock API (no backend) | `npx -y @stoplight/prism-cli mock docs/openapi.yaml` → `http://localhost:4010` |
| Format | JSON, UTF-8, `camelCase` keys, ids as strings ([ADR 0011](adr/0011-camelcase-string-ids-cursor-lists.md)) |
| Auth | `Authorization: Bearer <token>` from `POST /api/auth/login` on every endpoint not marked **public** ([ADR 0010](adr/0010-personal-accounts-and-pins.md)) |
| Versioning | No version in the path during the hackathon. Breaking changes only through an `XREQ`. |
| Timestamps | ISO 8601 in UTC, e.g. `2026-01-01T04:30:00Z` |
| Nulls | A nullable field is always present and set to `null`. |
| Lists | Query `limit` (default 50, max 200) and `cursor`; `data` is `{ "items": [...], "nextCursor": "opaque" \| null }` |
| Retried creates | `Idempotency-Key: <uuid>` header; a repeat returns the first result |
| Endpoints (Wakatta?) | [Section 3](#3-endpoint-index), generated from the code; rules: [BACKEND_ARCHITECTURE.md §9](BACKEND_ARCHITECTURE.md#9-api) |

### 1.1 Response envelope

Every JSON response, success or error, uses this envelope.

```json
{ "success": true, "data": {}, "message": "ok" }
```

```json
{
  "success": false,
  "error": { "code": "RESOURCE_NOT_FOUND", "message": "Item not found", "details": {} }
}
```

### 1.2 Error codes

Every code the API returns, with when: [BACKEND_ARCHITECTURE.md §9.2](BACKEND_ARCHITECTURE.md#9-api). Field errors are 422 with `error.details.fields` (`loc`, `msg`); the rejected input is never echoed. `429` carries `Retry-After` in seconds.

## 2. Roles and permissions

| Role | Signs in with | Can |
| :--- | :--- | :--- |
| Foreman (職長) | shop code, name, 6-digit PIN | Runs their shop: accounts, Machines, Note Versions, Briefings, Chat, Tasks, Panels and Overrides, Near-miss reviews, Senpai pairs, interviews, Support Log |
| Worker | shop code, name, 4-digit PIN | Their own work in their shop (Checks, Chat, Tasks, Near-miss Reports) and life in their district (Notices, Events, Asks, Senpai, Meet-ups) |
| Poster | district code, name, 6-digit PIN | Their organisation's Notices and Events in one district; counts of who understood, never names |
| Panel | a token a Foreman issues for one Machine | Reads that Machine's state |
| Senpai | a link token (no account) | Their Worker's forwarded Asks, replies and Meet-ups |

Foremen and Workers also own personal data that no employer route reads ([ADR 0014](adr/0014-personal-threads-outside-shop-scope-and-private-from-the-employer.md)): My QR, Contacts, Contact Threads and Talk Threads. A wrong role is 403; another shop's, district's or person's id is 404. `backend/tests/api/test_route_guard.py` checks both for every route.

## 3. Endpoint index

Generated from the code: `cd backend; uv run python -m app.openapi index` (CI fails when it is stale).

<!-- endpoint index: generated -->
133 operations on 104 paths, all live.

| Method | Path | Who | What |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | **public** | Liveness: the process is up (no database call) |
| `GET` | `/api/health/ready` | **public** | Readiness: the database answers |
| `GET` | `/api/auth/roster` | **public** | Names to pick from, for a shop or district code (public, rate-limited) |
| `POST` | `/api/auth/login` | **public** | Name + PIN → Bearer token |
| `POST` | `/api/auth/find` | **public** | Forgot the shop code: shop name + your name + PIN → Bearer token (rate-limited) |
| `POST` | `/api/auth/register` | **public** | Open a new shop; you become its first Foreman (rate-limited) |
| `GET` | `/api/auth/me` | foreman, worker, poster | The signed-in user |
| `PUT` | `/api/auth/me/pin` | foreman, worker, poster | Change your own PIN; your other sessions are signed out |
| `POST` | `/api/auth/logout` | foreman, worker, poster | Revoke this session |
| `GET` | `/api/workers` | foreman | Workers of your shop |
| `POST` | `/api/workers` | foreman | Create a worker account |
| `PATCH` | `/api/workers/{user_id}` | foreman | Rename, reset PIN or deactivate a worker |
| `GET` | `/api/foremen` | foreman | Foremans of your shop |
| `POST` | `/api/foremen` | foreman | Create a foreman account |
| `PATCH` | `/api/foremen/{user_id}` | foreman | Rename, reset PIN or deactivate a foreman |
| `GET` | `/api/machines` | foreman, worker | Machines of your shop |
| `POST` | `/api/machines` | foreman | Add a Machine |
| `PATCH` | `/api/machines/{machine_id}` | foreman | Rename a Machine (the code stays) |
| `DELETE` | `/api/machines/{machine_id}` | foreman | Archive a Machine: off the lists, still named in history |
| `GET` | `/api/machines/{machine_id}` | foreman, worker | A Machine and its current Note Version (no answers) |
| `POST` | `/api/machines/{machine_id}/scan` | worker | The signed-in Worker is at this Machine (Presence) |
| `GET` | `/api/machines/{machine_id}/panel` | panel, foreman | Machine Panel state; send If-None-Match to get 304 when unchanged |
| `POST` | `/api/machines/{machine_id}/panel-token` | foreman | Use this device as the Machine's Panel |
| `POST` | `/api/machines/{machine_id}/notes` | foreman | Record a note → AI draft (never a 500) |
| `GET` | `/api/note-versions/{note_id}` | foreman | A Note Version with its answer key (Foreman) |
| `PATCH` | `/api/note-versions/{note_id}` | foreman | Edit one step of a draft |
| `POST` | `/api/note-versions/{note_id}/approve` | foreman | Approve: safety change (re-check) or wording change |
| `POST` | `/api/checks` | worker | Answer a Note Version's Check; graded on the server |
| `POST` | `/api/machines/{machine_id}/overrides` | foreman | Panel Override: allow without a Sign-off |
| `POST` | `/api/machines/{machine_id}/remind` | foreman | Remind a Worker to take the Machine's Check (rate-limited) |
| `DELETE` | `/api/machines/{machine_id}/overrides/{override_id}` | foreman | End a Panel Override early |
| `GET` | `/api/machines/{machine_id}/versions` | foreman | Knowledge Library: a Machine's Note Versions, newest first |
| `GET` | `/api/metrics/effort` | foreman | Effort metrics: time to approve, edits, clean AI drafts, first-try Checks |
| `GET` | `/api/sign-offs` | foreman, worker | Sign-offs, newest first (Workers see their own) |
| `GET` | `/api/confusions` | foreman | Confusions, newest first |
| `POST` | `/api/confusions/{confusion_id}/ack` | foreman | Acknowledge a Confusion |
| `GET` | `/api/board` | foreman | Foreman board: every Machine, today's assignments and their Sign-off status; If-None-Match gives 304 |
| `GET` | `/api/shift-assignments` | foreman, worker | Shift Assignments of a day (Workers see their own) |
| `PUT` | `/api/shift-assignments` | foreman | Assign a Worker to a Machine and shift, or clear it with workerId null |
| `GET` | `/api/glossary` | foreman, worker | Glossary Terms of your shop |
| `POST` | `/api/glossary` | foreman | Add a Glossary Term |
| `PATCH` | `/api/glossary/{term_id}` | foreman | Edit a Glossary Term or add a translation for a shop language |
| `GET` | `/api/posters` | poster | The signed-in Poster's organisation |
| `POST` | `/api/notices` | poster | Text, voice or flyer photo → AI draft with Events (never a 500) |
| `GET` | `/api/notices` | poster, worker | Posters: their own Notices; Workers: the district's published Notices |
| `GET` | `/api/notices/{notice_id}` | poster, worker | One Notice: a Poster's own (drafts too), or a published one for a Worker |
| `PATCH` | `/api/notices/{notice_id}` | poster | Edit a draft Notice |
| `PATCH` | `/api/notices/{notice_id}/events/{event_id}` | poster | Confirm or correct an Event of a draft |
| `POST` | `/api/notices/{notice_id}/publish` | poster | Publish; 409 EVENTS_NOT_CONFIRMED until every date is confirmed |
| `GET` | `/api/notices/{notice_id}/reach` | poster | How many district Workers opened and understood it (counts only) |
| `GET` | `/api/events` | worker | Dates of confirmed district Events in a range (at most 62 days) |
| `POST` | `/api/receipts` | worker | The Worker opened a Notice or a Briefing (idempotent) |
| `GET` | `/api/blobs/{blob_id}` | poster, worker | A flyer photo of your district (JPEG, EXIF removed) |
| `POST` | `/api/asks` | worker | Ask by text or voice; answered only from published Notices |
| `GET` | `/api/asks` | foreman, worker | Asks, newest first: a Worker's own, or the shop's for a Foreman |
| `POST` | `/api/asks/{ask_id}/forward` | worker | Send an Ask to your Senpai; 409 NO_ACTIVE_PAIR without one |
| `GET` | `/api/senpai-pairs` | foreman | Senpai pairs of your shop |
| `POST` | `/api/senpai-pairs` | foreman | Pair a Worker with a Senpai; the link token is shown once |
| `DELETE` | `/api/senpai-pairs/{pair_id}` | foreman | Revoke a Senpai link |
| `GET` | `/api/meetups` | foreman, worker | Meet-ups, newest first (Workers: their own; Foremen: the shop's) |
| `POST` | `/api/meetups` | worker | Log a Meet-up with your Senpai |
| `GET` | `/api/senpai-pairs/me` | worker | Your own Senpai pair, or null (no link token) |
| `GET` | `/api/senpai/{token}` | **public** | The Senpai page: their Worker, forwarded Asks, district dates, Meet-ups |
| `POST` | `/api/senpai/{token}/asks/{ask_id}/reply` | **public** | Answer a forwarded Ask in Japanese |
| `POST` | `/api/senpai/{token}/meetups` | **public** | Log a Meet-up as the Senpai |
| `GET` | `/api/support-log.csv` | foreman | Support Log for the 登録支援機関 (CSV, UTF-8 with BOM, not enveloped) |
| `GET` | `/api/skill-matrix` | foreman | Skill Matrix (力量表): every Worker × Machine with its Sign-off status |
| `GET` | `/api/heatmap` | foreman | Confusions per Glossary Term × Machine, most confused term first |
| `GET` | `/api/workers/me/learning` | worker | Your Learning History: missed terms, newest first, and your streak |
| `GET` | `/api/analytics/understanding` | foreman | Wakatta Status trend per day and per Worker, and the top missed terms |
| `GET` | `/api/workers/{worker_id}/passport` | foreman, worker | Skill Passport: a Foreman sees any Worker, a Worker only self |
| `GET` | `/api/threads` | foreman, worker | Your Threads with unread counts, newest activity first; If-None-Match gives 304 |
| `POST` | `/api/threads` | foreman | Create a group Thread; you are its admin |
| `POST` | `/api/threads/{thread_id}/messages` | foreman, worker | Foreman: voice/text → AI draft. Worker: sent |
| `GET` | `/api/threads/{thread_id}/messages` | foreman, worker | Poll messages after a cursor; send If-None-Match to get 304 when nothing changed |
| `POST` | `/api/transcriptions` | foreman, worker | Dictation fallback: a recording in, its text out (personal, never stored) |
| `PATCH` | `/api/messages/{message_id}` | foreman | Edit an unsent draft's Japanese; the AI translates it again (XREQ-022) |
| `POST` | `/api/messages/{message_id}/send` | foreman | Send the edited draft; the Tasks kept go on the Task Board |
| `GET` | `/api/messages/unanswered` | worker | Your unanswered Checks across your Threads, newest first (at most 100) |
| `POST` | `/api/messages/{message_id}/answers` | worker | Answer a message's questions (-1 = not sure) → Wakatta Status |
| `GET` | `/api/follow-ups` | foreman | Follow-ups of your shop, newest first |
| `PATCH` | `/api/follow-ups/{follow_up_id}` | foreman | Mark a Follow-up clarified or understood |
| `POST` | `/api/messages/{message_id}/reexplain` | foreman | Re-explain one term to one Worker (AI) |
| `POST` | `/api/messages/{message_id}/reexplain/draft` | foreman | See the Re-explanation before sending it; nothing is stored |
| `POST` | `/api/messages/{message_id}/ask` | worker | Worker: ask the Foreman to explain a term again (no AI) |
| `POST` | `/api/messages/{message_id}/nudge` | foreman | Show a Worker you are waiting for their answer (rate-limited) |
| `GET` | `/api/tasks` | foreman, worker | The Task Board of your shop; If-None-Match gives 304 |
| `POST` | `/api/tasks` | foreman, worker | Add a Task by hand (a Worker's own Task is always theirs alone) |
| `PATCH` | `/api/tasks/{task_id}` | foreman, worker | Move or edit a Task; If-Match: <version>, 412 when it changed meanwhile |
| `GET` | `/api/threads/{thread_id}` | foreman, worker | A Thread with its members and your own settings |
| `PATCH` | `/api/threads/{thread_id}` | foreman | Rename, describe or set announce-only (Foreman admin) |
| `POST` | `/api/threads/{thread_id}/members` | foreman | Add members; they see messages from now on (Foreman admin) |
| `PATCH` | `/api/threads/{thread_id}/members/{user_id}` | foreman | Make a member admin or member (Foreman admin) |
| `DELETE` | `/api/threads/{thread_id}/members/{user_id}` | foreman | Remove a member; their messages stay |
| `PUT` | `/api/threads/{thread_id}/me` | foreman, worker | Your own mute, pin and archive for this Thread |
| `POST` | `/api/threads/{thread_id}/leave` | foreman, worker | Leave a Thread (the last Foreman admin cannot) |
| `POST` | `/api/briefings/draft` | foreman | Record a Briefing (voice or text) → AI draft (never a 500) |
| `POST` | `/api/briefings` | foreman | Publish the reviewed draft (its edited text and question) |
| `GET` | `/api/briefings` | foreman, worker | Foremen: every Briefing with answers; Workers: published ones with their receipt |
| `GET` | `/api/briefings/{briefing_id}/receipts` | foreman | Who understood, who only opened it, who has not opened it |
| `POST` | `/api/near-misses` | worker | Report a near miss by text or voice, with an optional photo |
| `GET` | `/api/near-misses` | foreman, worker | Near-miss Reports, newest first (a Worker: only their own); If-None-Match gives 304 |
| `GET` | `/api/near-misses/{report_id}` | foreman, worker | One Near-miss Report (a Worker: only their own) |
| `PUT` | `/api/near-misses/{report_id}/task` | foreman | Link the Task made from a Near-miss Report |
| `POST` | `/api/near-misses/{report_id}/ack` | foreman | Acknowledge a Near-miss Report |
| `GET` | `/api/near-misses/{report_id}/photo` | foreman, worker | The report's photo (JPEG, EXIF removed) |
| `GET` | `/api/interviews/due` | foreman | Periodic Interviews (support item 10): next due date per Worker |
| `GET` | `/api/workers/{worker_id}/interviews` | foreman | A Worker's past Periodic Interviews, newest first |
| `POST` | `/api/workers/{worker_id}/interviews` | foreman | Record a Periodic Interview |
| `GET` | `/api/today-word` | foreman | Today's Word: one word of the Workers' language for the Japanese side |
| `GET` | `/api/app/config` | **public** | Android version gate (public): minimum and latest version, download link |
| `PUT` | `/api/devices/me` | foreman, worker | Register this app install for push (upsert; 6 per minute) |
| `DELETE` | `/api/devices/me` | foreman, worker | Forget this app install before signing out (idempotent) |
| `POST` | `/api/knowledge/asks` | worker | Ask the chatbot (text or voice): answered only from cited sources, then one self-check; never a 500 |
| `POST` | `/api/me/qr` | foreman, worker | My QR: make it, or rotate it (the token is shown only then) |
| `GET` | `/api/contacts/resolve` | foreman, worker | Who a scanned QR belongs to (name and language only; rate-limited) |
| `POST` | `/api/contacts` | foreman, worker | Add the Contact a scanned QR belongs to (both sides connect) |
| `GET` | `/api/contacts` | foreman, worker | Your Contacts with unread counts |
| `PUT` | `/api/contacts/{contact_id}` | foreman, worker | Mute or hide a Contact |
| `POST` | `/api/blocks` | foreman, worker | Block a Contact (they are not told) |
| `DELETE` | `/api/blocks/{contact_id}` | foreman, worker | Unblock a Contact |
| `GET` | `/api/contacts/{contact_id}/messages` | foreman, worker | Contact Thread: messages after a cursor, in your language; If-None-Match → 304 |
| `POST` | `/api/contacts/{contact_id}/messages` | foreman, worker | Send a Contact Thread message |
| `DELETE` | `/api/contacts/{contact_id}/messages` | foreman, worker | Delete the whole Contact Thread: all you sent, and your copy of what you received |
| `DELETE` | `/api/contacts/{contact_id}/messages/{message_id}` | foreman, worker | Delete a message: one you sent goes for both of you, one you received only on your side |
| `GET` | `/api/talk-threads` | foreman, worker | Your Talk Threads, newest first |
| `POST` | `/api/talk-threads` | foreman, worker | Start a Talk Thread (title 1 to 40 characters) |
| `PATCH` | `/api/talk-threads/{thread_id}` | foreman, worker | Rename a Talk Thread |
| `DELETE` | `/api/talk-threads/{thread_id}` | foreman, worker | Delete a Talk Thread and every entry (hard delete) |
| `GET` | `/api/talk-threads/{thread_id}/entries` | foreman, worker | A Talk Thread's entries, oldest first |
| `POST` | `/api/talk-threads/{thread_id}/entries` | foreman, worker | What someone said (text or recording) → plain Japanese, the meaning, dialect words, replies (AI) |
| `PATCH` | `/api/talk-threads/{thread_id}/entries/{entry_id}` | foreman, worker | Exactly one of: ownText (graded by AI), replyJa (the reply shown), confirmed (it got through) |
| `DELETE` | `/api/talk-threads/{thread_id}/entries/{entry_id}` | foreman, worker | Delete one entry |
<!-- end of endpoint index -->

## 4. Endpoint details

Request and response schemas, examples and required headers are in [openapi.yaml](openapi.yaml) and, on a running API, at `/api/docs`. Rules per flow: [BACKEND_ARCHITECTURE.md §9.3](BACKEND_ARCHITECTURE.md#9-api).

## 5. How to update this file

1. Need a new endpoint or field? **File an `XREQ`** in [AGENTS.md](../AGENTS.md) with the exact JSON.
2. Backend implements it, regenerates [openapi.yaml](openapi.yaml) and this index (commands in section 3 and the README), and marks the `XREQ` `[RESOLVED]`.
3. Frontend regenerates its types (`npm run gen:api`), checks the integration and closes the `XREQ`.

### Changelog

| Version | Date | Change |
| :--- | :--- | :--- |
| 0.1.0 | 2026-11-22 | First contract. |
| 0.2.0 | 2026-11-24 | Filled in: roles, error codes, the generated endpoint index. |
