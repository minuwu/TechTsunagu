# Mobile API contract: Wakatta? Android app

**Status** draft v1 · **Date** 2026-11-23 · **Audience** the Kotlin / Jetpack Compose app in `android/` ([ADR 0015](adr/0015-native-android-app-in-android-folder.md)) and the backend team.

This file is the endpoint-by-endpoint contract for the whole prototype (Worker, Foreman and Community Poster roles). Push, deep links and QR, version gate and offline queue, and audio and photo upload are in the sibling [MOBILE_PLATFORM_CONTRACT.md](MOBILE_PLATFORM_CONTRACT.md); they are not repeated here. Terms follow [GLOSSARY.md](../GLOSSARY.md).

How the claims here were checked:

- `docs/openapi.yaml` (JSON despite the extension) was parsed with a script and compared with the `@router` decorators in `backend/app/routers/*.py`: both give the same 76 operations on 64 paths (check printed in section 1).
- Request and response models come from `backend/app/schemas/*.py`; what the web client sends comes from `frontend/src/services/wakatta.ts` and `frontend/src/domain/wakatta.ts`.
- Planned endpoints come from [BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md) section 9.3 and `AGENTS.md` XREQ-001..007. `new` endpoints come from a local backend-additions handoff note (not in the repo, `.agent-bridge/` is git-ignored; its content is restated in sections 4.8 to 4.11 below); they are proposals.
- Where a source is silent the text says `UNKNOWN` and the item is in section 7.2.
- Citations are `path:line`. `BA` below means `docs/BACKEND_ARCHITECTURE.md`; `AG` means `AGENTS.md`.

Contents: 1 Status legend and counts · 2 Conventions · 3 Common shapes · 4 Endpoints · 5 Mapping tables · 6 Kotlin notes · 7 Discrepancies and open questions · 8 Backend asks

---

## 1. Status legend and counts

| Status | Meaning | Count |
| :--- | :--- | :--- |
| `live` | Operation is in `docs/openapi.yaml` (generated from the FastAPI routes) and in the routers. Safe to code against. | 76 |
| `planned` | In `BA` 9.3 or an XREQ, not in `docs/openapi.yaml`. The XREQ id is named when there is one. Shapes are the spec's, not code. | 9 |
| `new` | From the handoff file only. No XREQ yet. Every path, field and rule can still change. | 29 |
| **Total** | | 114 |

`live` is counted in operations (method + path). The same operations are 64 paths in `docs/openapi.yaml`. `todayWord` on the live `GET /api/senpai/{token}` is a `new` field on a live endpoint; it is listed under 4.10 and does not change the counts. Revision 2 (2026-11-23, after the merge with `origin/main`): the live API grew from 52 operations on 43 paths to 76 on 64 (Chat, Briefings, Skill views, Panel Overrides, Knowledge Library, Sign-off list, effort metrics); 23 endpoints moved from `planned` to `live` and one live route (`POST /api/briefings`, E-114) was not in any plan.

Verification result (script run on 2026-11-23, revision 2): the 76 `live` operations equal the `docs/openapi.yaml` operations and the router decorators exactly (0 missing, 0 extra); the 9 `planned` operations equal (BA 9.3 plus the XREQ-003 and XREQ-005 routes) minus the live ones; the 29 `new` operations come from the handoff and overlap no live or planned one; the 34 screen ids, 27 Bus events and 49 `shortcut:` comments each appear exactly once in tables 5.1 to 5.3.

Each endpoint below starts with a heading of the form `#### E-nnn METHOD /path` and a line `Status: live|planned|new`. Role letters: **F** Foreman, **W** Worker, **P** Community Poster, **D** Machine Panel device session, **S** Senpai link token, **pub** no token. Role rules for live routes are from the `require(...)` guards in the routers; for planned ones from `BA` 3.2 and 9.3.

---

## 2. Conventions

### 2.1 Base URL per build type

| Build | Base URL | Source |
| :--- | :--- | :--- |
| debug, emulator | `http://10.0.2.2:8000` (the emulator's alias for the host machine's `localhost`; the API listens on port 8000) | `backend/Dockerfile:20`, `frontend/.env.example:4` |
| debug, physical phone | `http://<LAN address of the dev machine>:8000` | same |
| staging | `UNKNOWN` | no staging URL in the repo |
| release | `UNKNOWN` (BA 15 plans Azure Container Apps; no host is recorded anywhere) | `BA:657-671` |

Every path in this file starts with `/api`. The API serves its own docs at `/api/docs` and the schema at `/api/openapi.json` (`backend/app/main.py:52-53`). A debug build that uses `http://` needs a debug-only cleartext exception in the app's network security config; release builds use HTTPS only. CORS does not apply to a native client.

### 2.2 Envelope

Every JSON response is wrapped, success or error (`backend/app/api/envelope.py:8-12`, BA 9.1 at `BA:414-415`):

```json
{ "success": true, "data": { }, "message": "ok" }
```

```json
{ "success": false, "error": { "code": "VALIDATION_ERROR", "message": "Request is not valid", "details": { "fields": [ { "loc": ["body", "pin"], "msg": "..." } ] } } }
```

Not enveloped: `GET /api/support-log.csv` (CSV bytes), `GET /api/blobs/{id}` (image bytes), the planned `GET /api/workers/{id}/calendar.ics`, and every `304` (empty body). `details` is always an object (empty when unused). For a 422, `details.fields` is a list of `{loc, msg}`; the rejected input is never echoed back (`backend/app/api/errors.py:75-77`).

Every response carries `X-Request-ID` (a caller-sent one is kept if it matches `^[A-Za-z0-9._-]{1,64}$`; `backend/app/api/context.py:16,46`). Log it with every error so the backend can find the line.

### 2.3 Wire format

| Rule | Detail | Source |
| :--- | :--- | :--- |
| camelCase keys | `displayName`, `nextCursor`, `correctIndex` | ADR 0011, `backend/app/schemas/common.py:32` |
| ids are strings | `"12"`, never a JSON number. A non-numeric path id is refused before it reaches a query (see 7.1, D-02). Pattern `^[1-9][0-9]{0,18}$` | `backend/app/schemas/common.py:13` |
| time | UTC ISO-8601 with `Z`, for example `2026-12-22T10:00:00Z`. Some fields carry `+09:00` (Event `startsAt`, `at`); parse both as an offset date-time. Show in `Asia/Tokyo` | `BA:417`, AG XREQ-002 sample |
| dates | `YYYY-MM-DD` in the shop timezone (`shift-assignments`, `meetups`, `/events?from&to`) | `BA:417` |
| nullable fields | always present with `null` | `BA:417` |
| lists | cursor pages `{ "items": [...], "nextCursor": "opaque" \| null }`, query `?limit=` (default 50, max 200) and `?cursor=`. A bad cursor is 422. Exceptions that are bare arrays: see 7.1 D-03 | `backend/app/api/pagination.py:13-14,46-47` |
| unknown request fields | rejected with 422 (`extra="forbid"`) on every request model | `backend/app/schemas/common.py:32` |
| language codes | `ja bn en vi id ne my tl zh` | `backend/app/schemas/common.py:10` |

`Tri` (translated text): the web client's `Tri` is `{ja, bn, en}`. On the wire it is an object with key `ja` (always required) plus one key per language the shop uses (`BA:224`, `backend/app/schemas/common.py:20-30`). The keys are dynamic: a Vietnamese shop has `vi`. Model it as `Map<String, String>`, never as a class with three fixed fields. Reading rule: show `text[userLanguage]`, else fall back to `text["ja"]` (same as `pick()` in `frontend/src/domain/wakatta.ts:8`). Japanese text may carry furigana as `漢字{よみ}`; strip `{...}` for plain reading screens (same file, line 9).

### 2.4 Kotlin serialization settings

```kotlin
// responses and requests share one Json instance
val WakattaJson = Json {
    ignoreUnknownKeys = true   // the backend may add response fields
    explicitNulls = false      // absent and null both decode to null
    encodeDefaults = false     // never send a field the user did not set
    // enums: see 2.5
}
```

The client must never send a field that is not in the request model; the backend answers 422 for any extra key. Do not serialize UI-only state into a request DTO. Four requests need an explicit `null` and so must be encoded with a second `Json { explicitNulls = true }` instance (or a hand-built body):

| Request | Field | Why |
| :--- | :--- | :--- |
| `PUT /api/shift-assignments` | `workerId: null` | required field; `null` clears the slot (`backend/app/schemas/work.py:161-165`, `backend/app/routers/work.py:178`) |
| `PATCH /api/notices/{id}/events/{eventId}` | `rrule: null` | `null` makes the Event one-off; an omitted key keeps the old rule (the service uses `model_fields_set`, `backend/app/schemas/notices.py:52-60`, `backend/app/services/notices.py:325`) |
| `PATCH /api/tasks/{id}` | `dueAt: null` | `null` clears the due time; an omitted key keeps it (the service checks `model_fields_set`, `backend/app/services/chat.py:570`; `backend/app/schemas/chat.py:133-139`) |
| `POST /api/machines/{id}/overrides` | `workerId: null` | required field; `null` means anyone (`backend/app/schemas/work.py:116-122`) |

### 2.5 Tolerant enum decoding

The backend adds enum values over time (Lang already has 9, Notice category 5, message `kind` will grow). Every enum DTO must decode an unknown value to an `UNKNOWN` entry instead of failing the whole response, and the UI must render `UNKNOWN` as a neutral item. How to do it in kotlinx.serialization (`coerceInputValues` with a default, or a custom serializer) is `UNVERIFIED` against the version the app will pin; test it in the first sprint. Enums seen on the wire: section 6.

### 2.6 Auth flow

1. The Worker or Foreman scans the shop QR sticker or types the 8-character shop code `^[A-Z0-9]{8}$` (district code for Posters). Deep link and QR payloads are in [MOBILE_PLATFORM_CONTRACT.md](MOBILE_PLATFORM_CONTRACT.md). The prototype's onboarding code `PRESS-2F-4821` (`prototypes/wakatta/parts/E.js:246`) does not match that pattern; see D-17.
2. `GET /api/auth/roster?code=&role=` returns names and ids only (E-003). 20 per minute per IP (`backend/app/services/auth.py:28`).
3. The user picks a name and types the PIN: 4 digits for Workers, 6 for Foremen and Posters (ADR 0010; `BA:616`). `POST /api/auth/login {userId, pin}` returns `{token, expiresAt, mustChangePin, user}` (E-004). 10 attempts per minute per IP; 5 wrong PINs lock the account for 15 minutes (`backend/app/services/auth.py:24-27`).
4. Send `Authorization: Bearer <token>` on every call except E-001, E-002, E-003, E-004 and the `/api/senpai/{token}` family, which use the path token (BA 9.1 at `BA:419`).
5. If `mustChangePin` is `true`, show "choose your PIN" first. Until `PUT /api/auth/me/pin` succeeds every other route answers `403 PIN_CHANGE_REQUIRED` (`backend/app/api/auth.py:49-50`). Allowed while the flag is set: `GET /auth/me`, `PUT /auth/me/pin`, `POST /auth/logout` only (`backend/app/routers/auth.py:12`).
5a. A shop that has no code yet is opened with `POST /api/auth/register {shopName, displayName, pin}` (ADR 0017): `201` with the login answer plus `shopCode`, the new Foreman signed in. 3 per minute per IP; a guessable or non-6-digit PIN is `422`. Show the `shopCode` so the Foreman can hand it to Workers; Worker accounts are then made from the Me tab.
6. Session lifetime: Worker 30 days, Foreman and Poster 12 hours, Panel 180 days (`backend/app/services/auth.py:17-22`). A Worker's session is extended on the server when it is used more than an hour after its last use (`backend/app/services/auth.py:118-121`), but the client is not told the new expiry: `expiresAt` from login is only a hint. Treat the first `401` as the real expiry. A Foreman phone must expect a re-login about twice a day.
7. A PIN change revokes the user's other sessions but keeps the current one (`backend/app/services/auth.py:147`). A Foreman reset of a Worker's PIN revokes all of that Worker's sessions (ADR 0010); the Worker then gets 401 on the next call.
8. `POST /api/auth/logout` revokes the session; the Android client must also delete its stored token and, from the platform contract, unregister the push token.

### 2.7 Idempotency-Key

Header `Idempotency-Key: <UUID v4>`, one new UUID per user action, stored on disk before the first attempt and reused on every retry of that action (lifecycle in the platform contract). A repeat returns the first result and does not call the AI again (`BA:381-382`).

| Routes | Status | Missing key |
| :--- | :--- | :--- |
| `POST /machines/{id}/notes`, `POST /checks`, `POST /notices`, `POST /asks`, `POST /briefings/draft`, `POST /threads/{id}/messages` | live, required | `422 VALIDATION_ERROR` (FastAPI header validation; `docs/openapi.yaml` marks the parameter required) |
| `POST /near-misses` | planned, required (XREQ-001 item 4) | `422` expected |
| new creates: `POST /contacts`, `POST /talk-threads`, `POST /contacts/{id}/messages`, `POST /talk-threads/{id}/entries`, `POST /workers/{id}/interviews`, `POST /threads` | new, "Idempotency-Key on creates" (handoff section 2; interviews table has `idempotency_key`) | `UNKNOWN` which of these the backend will require |
| `POST /meetups`, `POST /senpai/{token}/meetups`, `POST /tasks`, `POST /receipts`, `POST /briefings` (publish), `POST /messages/{id}/reexplain` | no key in code or spec | the app must make these safe to retry itself (see 7.2, Q-10); a repeated `POST /briefings` or `/messages/{id}/send` answers `409` because it already happened |

Replay behaviour as coded: a replayed note draft returns the stored draft (`backend/app/services/notes.py:73-75`); the route is declared `status_code=201`, so a replay is also `201`, not the `200` that BA 8.2 describes. Reusing a key on a Check for a different target is `409 IDEMPOTENCY_KEY_REUSED` (`backend/app/services/floor.py:192`, `backend/app/services/notices.py:438`): never reuse a key for a different action.

### 2.8 ETag and polling

Two live routes support conditional requests: `GET /api/machines/{id}/panel` (`backend/app/routers/work.py:76-85`) and `GET /api/threads/{id}/messages?after=` (`backend/app/routers/chat.py:68-85`). Send `If-None-Match: <last ETag, with its quotes>`; a `304` has no body and an `ETag` header, and means "no change". The ETag is `"` + 32 hex characters + `"`. Panel and chat poll every 2 s (BA 1 at `BA:53`; AG XREQ-005 item 3, `AGENTS.md:228`). Planned: the Foreman Board every 5 s with ETag (BA 8.5 at `BA:401`, BA 17 item 15; the live `GET /api/board`, `GET /api/threads` and `GET /api/tasks` have no ETag, see D-07). No SSE or WebSocket exists or is planned. Foreground and background polling intervals and battery rules are in the platform contract.

### 2.9 What the app must DO for each error

Codes come from `BA` 9.2 (`BA:421-438`), the live code (`backend/app/api/errors.py:17-30`, `backend/app/services/*.py`), and XREQ-005 (`AGENTS.md:256`). Codes not in `BA` 9.2 are marked "extra".

| HTTP | `error.code` | When | App action |
| :--- | :--- | :--- | :--- |
| 400 | `BAD_REQUEST` | parseable input the schema cannot express | Show a generic "could not do that" message; do not retry; log `X-Request-ID`. |
| 401 | `UNAUTHORIZED` | no, expired or revoked token; wrong PIN | On any route except the two below: delete the stored token, keep the outbox, go to login for that user. On `POST /auth/login`: wrong name or PIN, stay on the PIN screen, never sign out. On `PUT /auth/me/pin`: "Current PIN is wrong" (`backend/app/services/auth.py:138`), stay on the screen, do not sign out. A global "401 means sign out" interceptor is wrong for these two routes. |
| 403 | `FORBIDDEN` | wrong role | Hide or disable the feature for this role; never retry. |
| 403 | `PIN_CHANGE_REQUIRED` | first-login PIN not changed | Navigate to the choose-your-PIN screen, then replay the blocked call. |
| 404 | `NOT_FOUND` | unknown id, id of another shop or district, wrong code on roster, wrong or revoked Senpai token | Treat the item as gone: refresh the list, drop any outbox entry for it. On roster: "unknown code". |
| 405 | `METHOD_NOT_ALLOWED` (extra, `errors.py:23`) | client bug | Log; no retry. |
| 409 | `CONFLICT` | state does not allow the action (example: only a draft can be edited; Worker already has a Senpai; name taken) | Re-read the resource and show the new state. |
| 409 | `VERSION_CHANGED` | the Note Version is no longer current (`backend/app/services/floor.py:205`) | Discard the in-progress Check, reload `GET /machines/{id}`, show the new Note, restart the Check. Never queue a Check offline. |
| 409 | `EVENTS_NOT_CONFIRMED` | publish with an unconfirmed or invalid Event (Poster) | Show which Event blocks publishing: `details` is `{eventId, reason}` with `reason` `not_confirmed` or a recurrence reason (`backend/app/services/notices.py:354-355`). |
| 409 | `NO_ACTIVE_PAIR` | forward an Ask without a Senpai | Tell the Worker to ask the Foreman for a Senpai. |
| 409 | `NEEDS_EDIT` (extra, `backend/app/services/notes.py:182`, `backend/app/services/notices.py:358`) | approve or publish a draft the AI could not finish | Open the editor; the user must fill the missing steps or questions. |
| 409 | `IDEMPOTENCY_KEY_REUSED` (extra) | same key, different Check target | Client bug: generate a new UUID per user action. |
| 409 | `ALREADY_SENT` (live, extra, `backend/app/services/chat.py:349`) | `POST /messages/{id}/send` twice | Treat as success, refresh the message. |
| 409 | `THREAD_LOCKED` (new, handoff section 1) | edit or leave a locked group (Safety notices) | Disable the action; explain the thread is managed by the shop. |
| 412 | `VERSION_CONFLICT` (live, extra, `backend/app/services/chat.py:567`) | Task moved by someone else (`If-Match` stale) | Re-fetch the Task Board and show the card where it now is. |
| 413 | `PAYLOAD_TOO_LARGE` | body over 10 MB, audio over 8 MB, photo over its limit | Shorten the recording or re-compress the photo; limits are in the platform contract. |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | magic bytes are not allowed | Re-encode (AAC in MP4, JPEG). |
| 422 | `VALIDATION_ERROR` | schema validation, missing `Idempotency-Key`, a bad cursor, a Check with the wrong number of answers | Show per-field messages from `details.fields[].loc`. For an Event date `details.reason` is `unsupported_rule`, `past` or `no_upcoming` (AG XREQ-002 item 2). |
| 423 | `PIN_LOCKED` | account locked; `details.until` is an ISO-8601 time | Show the unlock time; block the PIN field until then. |
| 429 | `RATE_LIMITED` | over a rate limit; `Retry-After` set | Wait `Retry-After` seconds (login and roster: 60), then retry once; never loop. |
| 500 | `INTERNAL_ERROR` | anything else | Generic message; retry a read once; never auto-retry a write without its `Idempotency-Key`. |
| 503 | `BUSY` | database pool exhausted; `Retry-After: 2` | Back off and retry with the same key. |
| 503 | `NOT_READY` (extra, `backend/app/routers/health.py:26`) | `/health/ready` and the DB is down; `Retry-After: 5` | Show "server unavailable". |
| 503 | `AI_UNAVAILABLE` (extra, `backend/app/services/chat.py:480`) | a Re-explanation could not be made now | Tell the Foreman to explain the term in person; no retry loop. |
| any | `HTTP_ERROR` (extra, `errors.py:71`) | any status without a mapped code | Use the HTTP status only. |

AI trouble is never a 5xx: a degraded AI result is a normal `201` with `needsEdit: true` and, for Asks, `covered: false` (`BA:438`, `BA:570-578`).

### 2.10 Worker responses never contain `correctIndex`

Every Worker-facing response uses `CheckQuestionForWorker` (`id`, `termId`, `prompt`, `options`); `correctIndex` exists only in Foreman and Poster models (`backend/app/schemas/common.py:35-45`, BA 12 at `BA:619`). Grading is on the server (`POST /api/checks`, `POST /api/messages/{id}/answers`). The Android Worker DTOs must not declare a `correctIndex` field at all, and the Foreman DTOs (`NoteVersionOut.questions`, `NoticeForPoster.question`) must live in separate classes.

---

## 3. Common shapes

Examples use made-up values. "(F)" marks Foreman-only models.

```json
// UserOut (login, /auth/me)
{ "id": "12", "displayName": "Rahim Uddin", "role": "worker", "language": "bn", "shopId": "1", "districtId": "1" }
// a Foreman has districtId possibly null; a Poster has shopId null (backend/app/schemas/auth.py UserOut)

// MachineOut (list item)
{ "id": "2", "code": "press-2", "name": { "ja": "2号プレス", "bn": "...", "en": "Press #2" },
  "currentVersionId": "41", "activeWorkerId": null }

// NoteForWorker (inside MachineDetail.current): no correctIndex
{ "id": "41", "versionNo": 3,
  "steps": [ { "text": { "ja": "電源を切る", "bn": "...", "en": "Switch off the power" }, "icon": "lockout" } ],
  "questions": [ { "id": "q1", "termId": "5", "prompt": { "ja": "...", "en": "..." },
                   "options": [ { "ja": "...", "en": "..." }, { "ja": "...", "en": "..." }, { "ja": "...", "en": "..." } ] } ] }

// NoteVersionOut (F): adds answers and provenance
{ "id": "57", "machineId": "2", "versionNo": 4, "status": "draft", "recordedBy": "山口 隆",
  "transcriptJa": "...", "steps": [ { "text": { "ja": "..." }, "icon": null } ],
  "backTranslation": { "bn": "...", "en": "..." },
  "questions": [ { "id": "q1", "termId": null, "prompt": { "ja": "..." }, "options": [ {}, {}, {} ], "correctIndex": 1 } ],
  "needsEdit": false, "changeKind": null, "editCount": 0, "aiOutcome": "ok",
  "createdAt": "2026-11-23T01:00:00Z", "approvedAt": null }

// CheckResult
{ "passed": false,
  "wrong": [ { "id": "q2", "termId": "5", "prompt": { "ja": "...", "bn": "..." } } ],
  "signOff": null }   // on a pass: { "id": "9", "workerId": "12", "machineId": "2", "noteVersionId": "41", "passedAt": "..." }

// PanelOut
{ "state": "ready", "worker": { "id": "12", "displayName": "Rahim Uddin" }, "reason": null, "versionNo": 3, "override": null }
// state: idle | locked | ready | override; override: { "by": "山口 隆", "until": "...", "reason": "..." }

// NoticeForWorker (GET /notices as a Worker); NoticeForPoster has `question` with correctIndex and no opened/passed
{ "id": "7", "posterId": "1", "category": "garbage", "sourceKind": "photo", "sourceJa": "...", "photo": "/api/blobs/3",
  "title": { "ja": "...", "en": "..." }, "steps": [ { "ja": "...", "en": "..." } ],
  "events": [ { "id": "11", "title": { "ja": "..." }, "startsAt": "2026-11-24T08:00:00+09:00", "rrule": "FREQ=WEEKLY;BYDAY=MO,TH",
                "confirmed": true, "nextDates": [ "2026-11-24T08:00:00+09:00" ] } ],
  "status": "published", "needsEdit": false, "createdAt": "...", "publishedAt": "...",
  "question": { "id": "q1", "termId": null, "prompt": {}, "options": [ {}, {}, {} ] },
  "opened": true, "passed": null }

// AskOut
{ "id": "31", "workerId": "12", "text": "...", "covered": false, "answer": null, "sourceNoticeIds": [],
  "forwarded": false, "senpaiReply": null, "createdAt": "..." }
```

Sources: `backend/app/schemas/{auth,work,notices,asks,senpai}.py`. Photo URLs are `/api/blobs/{id}` and need the Bearer header: load them with an authenticated image loader (AG XREQ-002 item 4).

---
## 4. Endpoints

Reading guide. Every authenticated route can also return `401 UNAUTHORIZED` and `403 PIN_CHANGE_REQUIRED` (2.9); they are not repeated. Every path param that is an id can return `422 VALIDATION_ERROR` when it is not numeric and `404 NOT_FOUND` for another scope's id. Every route with a body can return `422` for unknown fields. "Screens" are prototype screen ids; "Bus" are prototype Bus events (table 5.2). For `planned` and `new` endpoints the examples contain only fields a source names; anything else is `UNKNOWN`.

### 4.1 Health, auth and accounts

#### E-001 `GET /api/health`
Status: `live` · Roles: pub · Idempotency: none · Poll/ETag: none · Screens: none (use for a reachability check at startup)
Response 200: `{"success":true,"data":{"status":"ok"},"message":"ok"}`. Liveness only, no DB call (`backend/app/routers/health.py:19`).

#### E-002 `GET /api/health/ready`
Status: `live` · Roles: pub · Idempotency: none · Poll/ETag: none · Screens: none
Response 200: same body as E-001. Errors: `503 NOT_READY` with `Retry-After: 5` when the DB is down (`backend/app/routers/health.py:26`).

#### E-003 `GET /api/auth/roster`
Status: `live` · Roles: pub · Idempotency: none · Poll/ETag: none · Screens: `onboarding`
Request: query `code` (`^[A-Z0-9]{8}$`) and `role` (`worker|foreman|poster`).
Response 200: `{"success":true,"data":[{"id":"12","displayName":"Rahim Uddin"}],"message":"ok"}` (bare array, not paged; active users only).
Errors: `404 NOT_FOUND` unknown code; `422`; `429 RATE_LIMITED` (20 per minute per IP, `Retry-After: 60`).

#### E-004 `POST /api/auth/login`
Status: `live` · Roles: pub · Idempotency: none (a login is not retried blindly) · Poll/ETag: none · Screens: `onboarding`
Request: `{"userId":"12","pin":"1234"}` (`pin` 4 to 6 characters; length rule per role is checked on PIN creation).
Response 200: `{"success":true,"data":{"token":"opaque","expiresAt":"2026-12-22T10:00:00Z","mustChangePin":true,"user":<UserOut>},"message":"ok"}`.
Errors: `401 UNAUTHORIZED` wrong name or PIN (same message for an unknown or inactive user); `423 PIN_LOCKED` `details.until`; `429 RATE_LIMITED`; `422`. Source: `backend/app/services/auth.py:79-106`.

#### E-005 `GET /api/auth/me`
Status: `live` · Roles: F W P (also while `mustChangePin`) · Poll/ETag: none · Screens: `me`, session restore
Response 200: `<UserOut>`. Errors: `401`.

#### E-006 `PUT /api/auth/me/pin`
Status: `live` · Roles: F W P (allowed while `mustChangePin`) · Idempotency: none · Screens: `onboarding`, `me`
Request: `{"currentPin":"1234","newPin":"4821"}`. Response 200: `{"success":true,"data":null,"message":"ok"}`.
Errors: `401 UNAUTHORIZED` "Current PIN is wrong" (do not sign out); `422` when the new PIN breaks the rules (digits only, exact length per role, not all the same digit or a run) or equals the old one (`backend/app/services/auth.py:139-145`). Other sessions of the user are revoked.

#### E-007 `POST /api/auth/logout`
Status: `live` · Roles: F W P · Screens: `me` · Request: no body. Response 200: `data: null`. Revokes this session only.

#### E-008 `GET /api/workers`
Status: `live` · Roles: F · Poll/ETag: none · Screens: `skills`, `me` (team), `interview`, `supportlog` (worker pickers), `home`
Request: `?limit=&cursor=`. Response 200: `data` = page of `AccountOut`: `{"id":"12","displayName":"Rahim Uddin","role":"worker","language":"bn","active":true,"mustChangePin":false}`.

#### E-009 `POST /api/workers`
Status: `live` · Roles: F · Screens: none in the prototype · Idempotency: none in code
Request: `{"displayName":"Rahim Uddin","language":"bn","pin":"2468"}`. Response 201: `<AccountOut>` with `mustChangePin: true`.
Errors: `409 CONFLICT` name already used in the shop; `422` bad PIN or a language the shop does not use (`backend/app/services/users.py:36-48`).

#### E-010 `PATCH /api/workers/{id}`
Status: `live` · Roles: F · Screens: none in the prototype
Request: any of `{"displayName":"..","language":"..","pin":"..","active":false}`. Response 200: `<AccountOut>`. A PIN reset revokes that Worker's sessions and sets `mustChangePin`.
Errors: `404`; `409 CONFLICT` "cannot deactivate yourself" or "a shop needs at least one active Foreman" (`backend/app/services/users.py:109-111`); `422`.

#### E-011 `GET /api/foremen`
Status: `live` · Roles: F · Request: `?limit=&cursor=`. Response 200: page of `AccountOut`. Screens: none in the prototype.

#### E-012 `POST /api/foremen`
Status: `live` · Roles: F · Request and response as E-009 (PIN must be 6 digits). Screens: none in the prototype.

#### E-013 `PATCH /api/foremen/{id}`
Status: `live` · Roles: F · Request and response as E-010. Screens: none in the prototype.

### 4.2 Machines, Scan, Panel

#### E-014 `GET /api/machines`
Status: `live` · Roles: F W · Poll/ETag: none · Screens: `machines`, `home`, `scan`, `machine`
Request: `?limit=&cursor=`. Response 200: page of `<MachineOut>`. `activeWorkerId` is the Worker present at the Machine now (Presence).
Note: there is no per-Worker Lock or Ready field here; a Worker reads their own from the Skill Passport (E-043).

#### E-015 `POST /api/machines`
Status: `live` · Roles: F · Screens: none in the prototype
Request: `{"code":"press-2","name":{"ja":"2号プレス","bn":"..","en":".."}}` (`code` `^[a-z0-9-]{1,32}$`). Response 201: `<MachineOut>`. Errors: `409 CONFLICT` code exists; `422`.

#### E-016 `GET /api/machines/{id}`
Status: `live` · Roles: F W · Screens: `scan` (after QR), `note`, `machine`
Response 200: `<MachineOut>` plus `"current": <NoteForWorker> | null`. No `correctIndex`, even for a Foreman (the answer key is `GET /note-versions/{id}`). Errors: `404`.

#### E-017 `POST /api/machines/{id}/scan`
Status: `live` · Roles: W · Idempotency: none (idempotent by nature: last scan wins) · Screens: `scan` (Presence)
Request: no body (the Worker is taken from the token; the web client's `{workerId}` body is wrong, D-13). Response 200: `data: null`. Presence lasts 10 minutes on the Panel (`BA:315`).

#### E-018 `GET /api/machines/{id}/panel`
Status: `live` · Roles: **D, F only** (a Worker gets `403 FORBIDDEN`, `backend/app/routers/work.py:44`) · Poll/ETag: poll every 2 s with `If-None-Match`, `304` when unchanged · Screens: `panel` (Foreman view), the Panel device mode
Response 200: `<PanelOut>` with headers `ETag: "<32 hex>"` and `Cache-Control: no-cache`. Response 304: empty body.
Errors: `404`. The Worker's own Lock or Ready is NOT available from this route; use E-043 (D-09).

#### E-019 `POST /api/machines/{id}/panel-token`
Status: `live` · Roles: F · Screens: none in the prototype ("Use this device as the Panel")
Request: no body. Response 200: `{"success":true,"data":{"token":"opaque","expiresAt":"..."},"message":"ok"}`. The token is a read-only session for one Machine, 180 days (`backend/app/services/auth.py:21`).

#### E-020 `POST /api/machines/{id}/overrides`
Status: `live` · Roles: F · Screens: `panel` (Foreman "allow without Sign-off") · Idempotency: none (each call starts an override)
Request: `{"workerId":"12","minutes":60,"reason":"Line is stopped otherwise"}`; `workerId` is required and `null` means anyone (send an explicit `null`, 2.4); `minutes` 1 to 480; `reason` 3 to 200 characters (`backend/app/schemas/work.py:116-122`).
Response 201: `{"id":"6","machineId":"2","workerId":"12","by":"山口 隆","reason":"..","startsAt":"..","endsAt":"..","revokedAt":null}`. The Panel then shows `state: "override"`. Errors: `404` unknown Machine or Worker; `422`.

#### E-021 `DELETE /api/machines/{id}/overrides/{overrideId}`
Status: `live` · Roles: F · Screens: `panel` · Ends an override early; idempotent (ending an ended override returns it unchanged). Response 200: the override as in E-020 with `revokedAt` set. Errors: `404`.

#### E-022 `GET /api/machines/{id}/versions`
Status: `live` · Roles: F · Screens: none in the prototype (Knowledge Library) · Request `?limit=&cursor=`. Response 200: page of `<NoteVersionOut>`, newest first. Errors: `404`.

### 4.3 Notes, Checks, Sign-offs

#### E-023 `POST /api/machines/{id}/notes`
Status: `live` · Roles: F · Idempotency: `Idempotency-Key` required · Polling: none, the AI runs inline (15 s typical, 45 s cap; BA 8.5) · Screens: none in the prototype (see Q-05)
Request: multipart; field `text` (max 2000 characters) or file `audio`; one of the two.
Response 201: `<NoteVersionOut>` (status `draft`). A degraded AI result is still `201` with `needsEdit: true` and `aiOutcome: "degraded"` holding the typed text only (`backend/app/services/notes.py:117-120`).
Errors: `404` machine; `413`, `415` (audio over 8 MB or a format other than webm, ogg or mp4); `422` neither text nor audio; `422` missing key.

#### E-024 `GET /api/note-versions/{id}`
Status: `live` · Roles: F · Screens: `note` (Foreman view) · Response 200: `<NoteVersionOut>` with `correctIndex` and `approvedBy` (display name of the approving Foreman, `null` until approved; XREQ-004 provenance line).

#### E-025 `PATCH /api/note-versions/{id}`
Status: `live` · Roles: F · Request: `{"index":0,"ja":"..."}` (`index` 0 to 11, `ja` 1 to 1000 characters). Response 200: `<NoteVersionOut>`. Errors: `409 CONFLICT` only a draft can be edited; `422` no such step.

#### E-026 `POST /api/note-versions/{id}/approve`
Status: `live` · Roles: F · Request: `{"changeKind":"safety"}` or `"wording"` (default `safety`; the first version of a Machine is always `safety`). Response 200: `<NoteVersionOut>` (status `approved`). Errors: `409 CONFLICT` only a draft; `409 NEEDS_EDIT` steps or questions missing (`backend/app/services/notes.py:179-182`).

#### E-027 `POST /api/checks`
Status: `live` · Roles: W · Idempotency: required · Polling: none · Screens: `note` (kind `note`), `notice` (kind `notice`), `briefing-card` (kind `briefing`), `panel`/`machines` (result changes Lock to Ready)
Request: `{"target":{"kind":"note","id":"41"},"language":"bn","answers":[1,0,2]}`. `kind` is `note`, `briefing` or `notice`; `answers` has 1 to 3 values, each 0 to 2, in question order; `language` must be one the shop uses (or `ja`). The Worker comes from the token; the web client's `workerId` field is wrong (D-13).
Response 200: `<CheckResult>`. A pass on a `note` creates the Sign-off; each wrong answer on a `note` creates a Confusion for the Foreman. The response lists wrong questions without `correctIndex`. The route dispatches on `kind`: `notice` to the Notice service, `briefing` to the Briefing service, `note` to the Work service (`backend/app/routers/work.py:123-130`).
Errors: `409 VERSION_CHANGED` (a new Note Version was approved mid-Check: reload and redo); `409 IDEMPOTENCY_KEY_REUSED`; `404`; `422` wrong answer count or range. For `kind: "briefing"` and `"notice"` the result has `signOff: null` and `wrong` holds the single question (`backend/app/services/briefings.py:220-223`).
Offline rule: never queue or apply optimistically (the verdict decides Lock/Ready; BA 7.3 at `BA:317-323`).

#### E-028 `GET /api/sign-offs`
Status: `live` · Roles: F (shop) and W (own only) · Screens: `me`, `skills` (passport dates) · Request `?limit=&cursor=`. Response 200: page of `{"id":"9","workerId":"12","machineId":"2","noteVersionId":"41","passedAt":"..."}`, newest first.

#### E-029 `GET /api/metrics/effort`
Status: `live` · Roles: F · Screens: `analytics` (partly) · Response 200: `{"drafts":12,"approved":9,"medianSecondsToApprove":84.0,"medianEditsPerDraft":1.0,"cleanAiShare":0.75,"firstTryPassShare":0.8}`. The four last fields are `null` when there is no data; shares are 0 to 1 (`backend/app/schemas/work.py:EffortOut`).

### 4.4 Briefings and receipts

The Briefing routes now exist (`backend/app/routers/briefings.py`, `docs/openapi.yaml`). The publish step is `POST /api/briefings` with the reviewed draft in the body; the `PATCH /api/briefings/{id}` and `POST /api/briefings/{id}/publish` routes of BA 9.3 are not built (E-031, E-032 stay `planned` and may never be).

#### E-030 `POST /api/briefings/draft`
Status: `live` (BA 9.3 at `BA:489`; XREQ-001 item 5, `AGENTS.md:83`) · Roles: F · Idempotency: required · Polling: none, AI inline · Screens: `briefing`
Request: multipart; `text` (max 2000) or file `audio`; one of the two. Response 201: `<BriefingForForeman>`:
```json
{ "success": true, "data": { "id": "41", "text": { "ja": "..", "en": ".." }, "status": "draft", "createdAt": "..", "publishedAt": null,
  "transcriptJa": "..", "question": { "id": "q1", "termId": null, "prompt": { "ja": ".." }, "options": [ {}, {}, {} ], "correctIndex": 1 }, "needsEdit": false }, "message": "ok" }
```
A degraded AI result is still `201` with `needsEdit: true`; the Foreman must fill every field marked 「（ここを入力してください）」 before publishing. Errors: `422` neither text nor audio, or missing key; `413`, `415`.

#### E-031 `PATCH /api/briefings/{id}`
Status: `planned` (`BA:490`; XREQ-001 item 5, `AGENTS.md:83`) · Roles: F · Not built: edits are sent in the E-114 body instead. Screens: `briefing`. Request and response `UNKNOWN`.

#### E-032 `POST /api/briefings/{id}/publish`
Status: `planned` (`BA:491`; XREQ-001 item 5, `AGENTS.md:83`) · Roles: F · Not built: use E-114. Screens: `briefing`.

#### E-033 `GET /api/briefings`
Status: `live` · Roles: F W · Poll: none stated (on screen open and with push) · Screens: `briefing-card`, `home`, `briefing-board` · Bus: `briefing:new`
Request: `?limit=&cursor=`. Response 200: a page; for a Foreman every Briefing as `<BriefingForForeman>` (with `correctIndex`), for a Worker the published ones as `BriefingForWorker`: `id`, `text`, `status`, `createdAt`, `publishedAt`, `question` (no `correctIndex`), `opened`, `passed`. Two DTO classes by role (D-19).

#### E-034 `GET /api/receipts`
Status: `planned` (`BA:494`) · Roles: F · Request `?target=` (format `UNKNOWN`) · Superseded by E-035 in code; probably never built (D-15).

#### E-035 `GET /api/briefings/{id}/receipts`
Status: `live` (XREQ-003 item 5, `AGENTS.md:162`) · Roles: F · Poll: none stated · Screens: `briefing-board` · Bus: `briefing:seen`, `briefing:answer`
Response 200: `{"understood":[{"id":"12","name":"Rahim Uddin"}],"openedOnly":[...],"notOpened":[...]}`; every active Worker of the shop is in exactly one group; `openedOnly` also holds Workers who answered wrongly. The key is `name`, not `displayName` as in the XREQ-003 sample (D-15). Errors: `403`; `404`.

#### E-036 `POST /api/receipts`
Status: `live` · Roles: W · Idempotency: none, idempotent by design · Screens: `notice`, `briefing-card` · Bus: `briefing:seen`
Request: `{"target":{"kind":"briefing","id":"41"}}` (`kind` `briefing` or `notice`). Response 200: `data: null`. Errors: `404` (a draft Briefing or another shop's).

#### E-114 `POST /api/briefings`
Status: `live` · Roles: F · Idempotency: none; a repeat after success is `409 CONFLICT` "already published", so treat that 409 as success after re-reading · Screens: `briefing` · Bus: `briefing:new`
Request: the reviewed draft: `{"id":"41","text":{"ja":"..","en":".."},"question":{<CheckQuestion with correctIndex>},"createdAt":"..."}` (`createdAt` optional and ignored; `backend/app/schemas/briefings.py:BriefingPublishIn`). Response 200: `<BriefingForForeman>` with `status: "published"`.
Errors: `409 NEEDS_EDIT` placeholder text still present; `409 CONFLICT` already published; `404`; `422`.

### 4.5 Near-miss Reports

All three are `planned` in BA 9.3 and have no XREQ. A Worker's own list and detail view are not specified (Q-09).

#### E-037 `POST /api/near-misses`
Status: `planned` (`BA:495`; the key is XREQ-001 item 4, `AGENTS.md:82`) · Roles: W · Idempotency: required · Screens: `nearmiss` · Bus: `nearmiss:new`
Request: multipart `machineId`, `text` or `audio`, optional `photo` file (JPEG), `Idempotency-Key`. Response `UNKNOWN`; the report holds `text` (Tri), `photoBlobId`, `acknowledgedAt` (`BA:255`). The prototype also asks for a severity and a photo mark position (`prototypes/wakatta/parts/C.js:337` `NM.sev`, `NM.mark`); the backend plan has neither (Q-08).

#### E-038 `GET /api/near-misses`
Status: `planned` (`BA:496`) · Roles: F (W own: `UNKNOWN`) · Request `?open=true` · Screens: `nearmiss-list`, `nearmiss-detail`, `home` (badge) · Bus: `nearmiss:new`. Paged, newest first. No get-by-id route is listed.

#### E-039 `POST /api/near-misses/{id}/ack`
Status: `planned` (`BA:497`) · Roles: F · Screens: `nearmiss-detail` · Bus: `nearmiss:ack`. Response `UNKNOWN`.

### 4.6 Board, Skill Matrix, Confusions, Glossary, Shift Assignments

The word "Board" is ambiguous. `GET /api/board` is the **Foreman Board** (Machines and Sign-offs). The prototype's `board` screen is the **Task Board** (`GET /api/tasks`, E-060). Do not mix them (GLOSSARY.md "Foreman Board", "Task Board").

#### E-040 `GET /api/board`
Status: `live` · Roles: F · Poll: every 5 s planned (BA 8.5), no ETag in code (D-07) · Screens: `machine` (Foreman), `home` (locked Machines), `machines` (Foreman)
Response 200: `{"date":"2026-11-23","openConfusions":2,"machines":[{"id":"2","code":"press-2","name":{...},"versionNo":3,"openConfusions":1,"assignments":[{"shift":"day","workerId":"12","displayName":"Rahim Uddin","status":"valid","flag":false}]}]}`. `status` is `valid|expiring|expired|none`; `flag` is true when assigned without a valid Sign-off.

#### E-041 `GET /api/skill-matrix`
Status: `live` · Roles: F · Screens: `skills` · Response 200: `{"machines":[{"id":"2","code":"press-2","name":{...}}],"rows":[{"workerId":"12","displayName":"Rahim Uddin","cells":[{"machineId":"2","status":"valid","passedAt":"..."}]}]}`. `status` is `valid|expiring|expired|none`; `passedAt` is `null` for `none`. Every active Worker and every Machine of the shop.

#### E-042 `GET /api/heatmap`
Status: `live` · Roles: F · Screens: `analytics` · Response 200: `{"machines":[...],"rows":[{"termId":"5","ja":"玉掛け","reading":"たまがけ","translations":{"bn":"..","en":".."},"cells":[{"machineId":"2","count":3}],"total":3}]}`. Only terms with at least one Confusion, most confused first.

#### E-043 `GET /api/workers/{id}/passport`
Status: `live` · Roles: F (any Worker), W (self only; another Worker's id is `403 FORBIDDEN`) · Screens: `me`, `machines`, `machine`, `panel` (Worker), `home` · Response 200: `{"workerId":"12","displayName":"Rahim Uddin","machines":[{"id":"2","code":"press-2","name":{...}}],"cells":[{"machineId":"2","status":"valid","passedAt":"..."}]}`. This is the Worker's own Ready (valid or expiring) or Locked (expired or none) per Machine. It does not include an active Panel Override; only the Panel route (E-018, not open to Workers) shows `override`.

#### E-044 `GET /api/confusions`
Status: `live` · Roles: F · Poll: `UNKNOWN` · Screens: `analytics`, `home`, `learning` (Foreman side) · Bus: `confusion:new`
Request: `?open=true&limit=&cursor=`. Response 200: page of `{"id":"3","checkId":"9","messageId":null,"questionId":"q2","termId":"5","machineId":"2","workerId":"12","createdAt":"...","acknowledgedAt":null}`. A Confusion comes from a Work Check (`checkId` and `machineId` set, `messageId` null) or from a Chat message's Check (`messageId` set, `checkId` and `machineId` null); the three fields are nullable (`backend/app/schemas/work.py:149-160`).

#### E-045 `POST /api/confusions/{id}/ack`
Status: `live` · Roles: F · Response 200: `<ConfusionOut>` (shape above). Screens: `analytics`. Errors: `404`.

#### E-046 `GET /api/glossary`
Status: `live` · Roles: F W · Screens: `chat` (glossary lock), `analytics`, `learning`
Response 200: bare array (not paged) of `{"id":"5","ja":"玉掛け","reading":"たまがけ","bn":"..","en":".."}`. The model has fixed `bn` and `en` fields, not a Tri (D-05).

#### E-047 `POST /api/glossary`
Status: `live` · Roles: F · Screens: `chat`, `analytics` ("add to glossary") · Bus: `glossary:add`
Request: `{"ja":"玉掛け","reading":"たまがけ","bn":"..","en":".."}` (`ja`, `reading` up to 40; `bn`, `en` up to 80). Response 201: the term. Errors: `409 CONFLICT` already in the glossary; `422`.

#### E-048 `PATCH /api/glossary/{id}`
Status: `planned` (`BA:466`) · Roles: F · Request and response `UNKNOWN`.

#### E-049 `GET /api/shift-assignments`
Status: `live` · Roles: F W (W sees own) · Request: query **`day`** (`YYYY-MM-DD`, optional; BA 9.3 says `date`, D-01) · Screens: `home`, `machine`, `panel`
Response 200: bare array of `{"id":"4","workerId":"12","machineId":"2","date":"2026-11-23","shift":"day"}`.

#### E-050 `PUT /api/shift-assignments`
Status: `live` · Roles: F · Screens: `machine`, `skills` (assign)
Request: `{"machineId":"2","shift":"day","date":"2026-11-23","workerId":"12"}`; `date` optional (defaults to today); `workerId` is required and `null` clears the slot (send an explicit `null`, 2.4). Response 200: `<AssignmentOut>` or `null` when cleared.

---
### 4.7 Chat (XREQ-005, live)

Built as WS12 (`backend/app/routers/chat.py`, `backend/app/schemas/chat.py`) and in `docs/openapi.yaml`. The XREQ-005 row in `AGENTS.md` still reads `[PENDING]` (`AGENTS.md:61`) although the routes exist; the XREQ-006 AI half also reads `[PENDING]` there (`AGENTS.md:62`). The six stages are Transcribed, Translated, Check Questions made, Tasks extracted, Sent to Workers, Answers in (`prototypes/wakatta/parts/skeleton.html:279`; `stage` 0 to 5). The Auto-send Countdown is client-side: the app calls `/send` unless the Foreman cancels. `POST /api/messages/{id}/ask` (XREQ-022) creates the `kind: "ask"` message; a second ask on the same term returns the first and sends nothing new.

Shared message fields (`MessageForSender`, `MessageForWorker`): `id`, `threadId`, `fromUserId`, `kind` (`voice|text|ask|reexplain`), `language`, `stage`, `needsEdit`, `transcriptJa` (nullable), `text` (a map: `ja` plus translations; empty for a degraded draft), `toUserId` and `refMessageId` (nullable), `createdAt`, `sentAt` (nullable). The Foreman's view adds `questions` (with `correctIndex`) and `taskDrafts` (only on the response to `POST`; they are not stored). The Foreman's view also carries `results` on a sent message with questions: one row per Worker of the Thread (a Re-explanation: only its Worker), `{userId, status (green|orange|red|null while waiting), answers (null while waiting), read, nudged}`, for the Wakatta card. The Worker's view adds `questions` (without `correctIndex`), `myStatus` (`green|orange|red|null`), `nudged`, `myAnswers` and `answerKey` (both null until this Worker answers; then the per-question picks and the correct option of each, so the card can show right and wrong after the last answer), and `asked` (this Worker already sent an `ask` about the message).

#### E-051 `GET /api/threads`
Status: `live` · Roles: F W members · Poll: none stated, no ETag (B-11) · Screens: `chats`, `home`, tab badge · Bus: `msg:new`
Response 200: **bare array**, newest activity first: `[{"id":"7","titleJa":"Line 2 · プレス班","kind":"group","memberIds":["12","13"],"unread":2,"lastMessageAt":"..."}]` (`kind` `group|direct`; `lastMessageAt` nullable).

#### E-052 `GET /api/threads/{id}/messages`
Status: `live` · Roles: F W members only · Poll: **every 2 s with `If-None-Match`, `304` when unchanged** · Screens: `chat` · Bus: `msg:new`, `wk:stage`, `check:answer`, `wk:read`, `wk:reexplain`
Request: `?after=<message id>` (a string id; omit for the first page). Response 200: `{"items":[<message>...]}` (no `nextCursor`; the client keeps the highest message id it has seen) with `ETag` and `Cache-Control: no-cache`. Reading moves the member's unread marker.
Errors: `403 FORBIDDEN` not in this Thread; `404`.

#### E-053 `POST /api/threads/{id}/messages`
Status: `live` · Roles: F W members (`require("foreman","worker")`) · Idempotency: required (a repeat returns the stored message) · Polling: none, AI inline · Screens: `chat` · Bus: `msg:new`
Request: multipart; `text` (max 2000) or file `audio`, exactly one. Response 201: `<MessageForSender>`.
- Foreman: becomes a draft at stage 3 (or stage 0 with `needsEdit: true` when the AI is down) with `transcriptJa`, `text`, `questions` and `taskDrafts`; nothing is sent until E-054.
- Worker: the message is translated to Japanese and sent at once (stage 4); `transcriptJa` is `null` when the model is down.
Errors: `403` not a member; `404`; `422` both or neither source, or missing key; `413`, `415`.

#### E-054 `POST /api/messages/{id}/send`
Status: `live` · Roles: F (the author) · Idempotency: none; a repeat is `409 ALREADY_SENT` (treat as success) · Screens: `chat` (Auto-send Countdown end) · Bus: `msg:new`, `task:update`, `wk:stage`
Request: `{"text":{"ja":"..","en":".."},"questions":[<CheckQuestion with correctIndex>],"tasks":[{"title":{"ja":".."},"assigneeIds":["12"],"dueAt":"2026-11-23T08:00:00+09:00","machineId":"2"}]}` (up to 3 questions and 10 tasks; `tasks` items are `TaskDraft`: `assigneeIds` default empty, `dueAt` and `machineId` nullable). Response 200: `<MessageForSender>` at stage 4; the kept Tasks are on the Task Board.
Errors: `409 ALREADY_SENT`; `404` not the author's; `422` assignees or Machine not of this shop. Note: `questions` here are full `CheckQuestion` objects, not strings as in the XREQ-005 sample (D-18).

#### E-055 `POST /api/messages/{id}/answers`
Status: `live` · Roles: W · Idempotency: one answer per message and user (a repeat is idempotent) · Screens: `chat`, `home` (Checks waiting) · Bus: `check:answer`, `followup:new`
Request: `{"answers":[1,1,-1]}` (1 to 3 values; each 0 to 2, or `-1` for "not sure"; this route accepts `-1`, `POST /checks` does not). Response 200: `{"status":"orange","followUpIds":["4"],"answers":[1,1,-1],"answerKey":[1,0,2]}`; `status` `green|orange|red`. The answer key is revealed only after answering; a repeat returns the first answers. A wrong or "not sure" answer on a question with a term creates a Confusion and a Follow-up.
Errors: `403`; `404`; `422` the message has no questions, or a wrong answer count.

#### E-056 `GET /api/follow-ups`
Status: `live` · Roles: F only (a Worker gets 403; Q-07) · Poll: none stated · Screens: `followups`, `home` · Bus: `followup:new`, `followup:resolve`
Request: `?status=open|clarified|understood&limit=&cursor=`. Response 200: page of `{"id":"4","messageId":"91","userId":"12","displayName":"Rahim Uddin","termJa":"金型交換","status":"open","note":"","createdAt":"..","resolvedAt":null}`, newest first.

#### E-057 `PATCH /api/follow-ups/{id}`
Status: `live` · Roles: F · Screens: `followups` · Bus: `followup:resolve`
Request: `{"status":"clarified","note":"explained in person"}` (`status` `clarified|understood`; `note` up to 500, default empty). Response 200: `<FollowUpOut>`. Errors: `409 CONFLICT` an impossible move (for example from `understood`); `404`.

#### E-058 `POST /api/messages/{id}/reexplain`
Status: `live` (AI side: XREQ-006) · Roles: F · Idempotency: none · Polling: none, AI inline · Screens: `followups`, `chat` · Bus: `wk:reexplain`
Request: `{"userId":"12","termJa":"金型交換"}` (`termJa` 1 to 100). Response 201: `<MessageForSender>` of `kind: "reexplain"`, sent to that Worker only with one new question. Errors: `422` that Worker is not in the Thread; `503 AI_UNAVAILABLE` "explain it in person" (a normal outcome, not a bug); `404`.

#### E-059 `POST /api/messages/{id}/nudge`
Status: `live` · Roles: F · Screens: `chat` · Bus: `wk:nudge`
Request: `{"userId":"12"}`. Response 200: `data: null`. Errors: `429 RATE_LIMITED` with `Retry-After: 600` (one Nudge per message and Worker in 10 minutes); `422` not in the Thread.

#### E-060 `GET /api/tasks`
Status: `live` · Roles: F W · Poll: none stated, no ETag (B-11) · Screens: `board`, `home`, `task`, `machine` · Bus: `task:update`
Request: `?limit=&cursor=` (no filter by Machine; Q-12). Response 200: page of `{"id":"3","title":{"ja":"..","en":".."},"column":"todo","assigneeIds":["12"],"dueAt":null,"machineId":"2","fromMessageId":"91","version":1,"updatedAt":".."}`; `column` `todo|doing|done`.

#### E-061 `POST /api/tasks`
Status: `live` · Roles: **F only** · Idempotency: none (a retry can add a card twice, D-20) · Screens: `board` (new task), `nearmiss-detail` (task from a Near-miss) · Bus: `task:update`
Request: `{"title":{"ja":".."},"assigneeIds":["12"],"dueAt":null,"machineId":null}` (`title` needs `ja`). Response 201: `<TaskOut>`. Errors: `422` assignees or Machine not of this shop.

#### E-062 `PATCH /api/tasks/{id}`
Status: `live` · Roles: F W (every Worker may move cards) · Screens: `board`, `task` · Bus: `task:update`
Request: header **`If-Match: <version>`** (required; the number from `TaskOut.version`, quotes allowed, else `422`) and body with any of `{"column":"doing","assigneeIds":["12"],"dueAt":"..."}`. `"dueAt": null` clears the due time (explicit null, 2.4). Response 200: `<TaskOut>` with `version` plus one.
Errors: `412 VERSION_CONFLICT` someone moved it first (reload the board); `404`; `422`.

### 4.8 Group controls (new)

Source: handoff section 1 (`.agent-bridge/specs/android/handoff-backend-additions.md:7-14`). It extends XREQ-005 in shop scope. All `new`; no response shape is given.

Rules from the handoff: members are active users of the same shop; a thread keeps at least one Foreman admin (409); a locked thread (Safety notices; membership follows the roster) refuses edits and leave with `409 THREAD_LOCKED`; Workers get `403` on member changes; announce-only: Workers cannot post but can still answer Checks; removal is soft (history stays); new members see messages from their join time (handoff section 5); system messages have `kind: "system"` with `{event, actorId, subjectId}` and the client renders the text per language. Mute is notification-only: a muted Worker still shows grey on the board, and Nudge and Check deadlines ignore mute.

#### E-063 `POST /api/threads`
Status: `new` (handoff section 1) · Roles: F (Foreman admin; Worker creation `UNKNOWN`) · Idempotency: "on creates" (handoff section 2 wording; applies here `UNKNOWN`) · Screens: `chats` (group), `chat` · Request and response `UNKNOWN` (fields named: title, `description`, members, `locked`, `announce_only`).

#### E-064 `GET /api/threads/{id}`
Status: `new` · Roles: members · Screens: `chat` (header, members). Response `UNKNOWN`.

#### E-065 `PATCH /api/threads/{id}`
Status: `new` · Roles: F admin · Screens: `chat` (rename, description, `announce_only`). Errors: `409 THREAD_LOCKED`. Audit `thread_renamed`. Request `UNKNOWN`.

#### E-066 `POST /api/threads/{id}/members`
Status: `new` · Roles: F admin; `403` for a Worker · Screens: `chat`. Errors: `409`, `409 THREAD_LOCKED`. Audit `thread_member_added`. Request `UNKNOWN`.

#### E-067 `PATCH /api/threads/{id}/members/{userId}`
Status: `new` · Roles: F admin · Screens: `chat`. Changes `role` (`admin|member`). Audit `thread_role_changed`. A thread keeps at least one Foreman admin (`409`). Request `UNKNOWN`.

#### E-068 `DELETE /api/threads/{id}/members/{userId}`
Status: `new` · Roles: F admin · Screens: `chat`. Soft removal (`removed_at`, `removed_by`). Audit `thread_member_removed`. Errors: `409 THREAD_LOCKED`.

#### E-069 `PUT /api/threads/{id}/me`
Status: `new` · Roles: any member · Screens: `chats`, `chat`. Per-member `muted`, `pinned`, `archived`. Request body names the three flags; response `UNKNOWN`.

#### E-070 `POST /api/threads/{id}/leave`
Status: `new` · Roles: any member · Screens: `chat`. Errors: `409 THREAD_LOCKED` for locked threads.

### 4.9 Contacts, My QR and Talk Threads (new)

Source: handoff section 2 (`handoff-backend-additions.md:16-22`) and ADR 0014. These are user-scoped, not shop-scoped: the scope is `me.user_id`; the Foreman and the employer cannot read them; they never reach the Support Log or Learning History. All roles with an account may use them (Foreman, Worker, Poster); not a Panel or a Senpai link. A blocked sender is dropped silently (no `403` reveals a block). At the per-user daily AI cap the original text is returned untranslated, never a 5xx. Audio is never stored but is sent to Google and Groq (handoff section 2 "Privacy"). The QR payload format and the scan flow are in the platform contract.

#### E-071 `POST /api/me/qr`
Status: `new` · Roles: F W P · Idempotency: n/a · Screens: `connect`
Get or rotate My QR. Returns an opaque 128-bit token (SHA-256 at rest, revocable, rotatable), never a user id or PIN. Request and response shape `UNKNOWN`.

#### E-072 `GET /api/contacts/resolve`
Status: `new` · Roles: F W P · Request `?token=` · Screens: `connect-scan` · Rate-limited. Returns the other person's name and language only, never an id or shop. Response shape `UNKNOWN`.

#### E-073 `POST /api/contacts`
Status: `new` · Roles: F W P · Idempotency: yes · Screens: `connect-scan`. The scanner confirms after seeing the name. Request (token) and response `UNKNOWN`.

#### E-074 `GET /api/contacts`
Status: `new` · Roles: F W P · Screens: `chats`. Response `UNKNOWN`; per-side `hidden_at` and `muted` exist.

#### E-075 `PUT /api/contacts/{id}`
Status: `new` · Roles: F W P · Screens: `pchat` (menu). Mute or hide. Request and response `UNKNOWN`.

#### E-076 `GET /api/contacts/{id}/messages`
Status: `new` · Roles: F W P · Poll: `UNKNOWN` (no ETag stated) · Screens: `pchat`. Response `UNKNOWN`; a message has `text_src`, `src_lang`, translated text.

#### E-077 `POST /api/contacts/{id}/messages`
Status: `new` · Roles: F W P · Idempotency: yes · Rate-limited · Screens: `pchat`. Request `UNKNOWN` (`text_src` and the language). Translated for the other person's language.

#### E-078 `POST /api/blocks`
Status: `new` · Roles: F W P · Screens: `pchat`, `connect-scan`. Request names `blocked_id`; the wire field name `UNKNOWN`.

#### E-079 `DELETE /api/blocks/{userId}`
Status: `new` · Roles: F W P · Screens: `pchat` (unblock).

#### E-080 `GET /api/talk-threads`
Status: `new` · Roles: F W P · Screens: `chats`, `pchat`. Response `UNKNOWN`. No route to read the entries of one Talk Thread is listed in the handoff (Q-06).

#### E-081 `POST /api/talk-threads`
Status: `new` · Roles: F W P · Idempotency: yes · Screens: `chats` (new talk). Title 1 to 40 characters (handoff). Request and response otherwise `UNKNOWN`.

#### E-082 `PATCH /api/talk-threads/{id}`
Status: `new` · Roles: owner · Screens: `pchat` (rename). Request `UNKNOWN`.

#### E-083 `DELETE /api/talk-threads/{id}`
Status: `new` · Roles: owner · Screens: `pchat` (menu). Hard delete of the thread and its entries.

#### E-084 `POST /api/talk-threads/{id}/entries`
Status: `new` · Roles: owner · Idempotency: yes · Screens: `pchat` (listen) · Poll: none, AI inline
Request: audio or text (field names `UNKNOWN`). One call returns an entry holding: transcript, plain Japanese, meaning in the owner's language, dialect term pairs, `needs_staff` flag (a deterministic rule, not the model), `own_text`, `grade` (`green|orange|red`), reply options, `reply_ja`, `confirmed` (bool or null). AI pipelines: `explain_utterance`, `grade_understanding` (handoff section 2). Wire names `UNKNOWN` except those listed.

#### E-085 `PATCH /api/talk-threads/{id}/entries/{UNKNOWN}`
Status: `new` · Roles: owner · Screens: `pchat`. The handoff says only "PATCH and DELETE an entry (attempt, reply, confirm)" and names no path; the last segment `{UNKNOWN}` is a placeholder (Q-06). Request `UNKNOWN`.

#### E-086 `DELETE /api/talk-threads/{id}/entries/{UNKNOWN}`
Status: `new` · Roles: owner · Screens: `pchat`. Same placeholder path. Hard delete.

### 4.10 Life: Notices, Events, Asks, Senpai, Meet-ups

These are `live`; the XREQ-002 card is still `[PENDING]` for the web client (`AGENTS.md:58`), but the routes exist in code and in `docs/openapi.yaml`. Posters write Notices; Workers read them. The Senpai link routes are a web page with no app and no login (platform contract), so the Android app does not call E-102 to E-104 (the stated decision is in `.agent-bridge/specs/android/contract-platform.md`, section 2); they are listed because they are live.

#### E-087 `GET /api/posters`
Status: `live` · Roles: P · Screens: `notice-new` (posting as) · Response 200: page with one item `{"id":"1","name":"第3自治会","kind":"jichikai"}` (`kind` `jichikai|city|employer`).

#### E-088 `POST /api/notices`
Status: `live` · Roles: P · Idempotency: required · Polling: none, AI inline (15 s typical) · Screens: `notice-new` · Bus: `notice:new`
Request: multipart; `sourceKind` (`text|voice|photo`) plus exactly the one source it names: `text` (max 4000), `audio`, or `photo` (JPEG). No `posterId` (taken from the session). Response 201: `<NoticeForPoster>` (status `draft`, with `events[].nextDates`, `needsEdit`). A degraded AI result is still `201` with `needsEdit: true`.
Errors: `422` source does not match `sourceKind`; `413`; `415` (photo must be JPEG, `backend/app/media.py:25`).

#### E-089 `GET /api/notices`
Status: `live` · Roles: P (own Notices) and W (published Notices of the district) · Poll: none · Screens: `life`, `notice`, `home`
Request: `?limit=&cursor=`. Response 200: page of `NoticeForPoster` (Poster) or `NoticeForWorker` (Worker); the two have different `question` types, so use two DTO classes chosen by the signed-in role (7.1, D-19). A Worker item adds `opened` and `passed`.
No get-by-id route exists; the `notice` screen reads from the list.

#### E-090 `PATCH /api/notices/{id}`
Status: `live` · Roles: P · Screens: `notice-new`
Request: any of `category`, `title` (Tri), `steps` (1 to 8 Tri), `question` (with `correctIndex`). Response 200: `<NoticeForPoster>`. Errors: `409 CONFLICT` only a draft can be changed.

#### E-091 `PATCH /api/notices/{id}/events/{eventId}`
Status: `live` · Roles: P · Screens: `notice-new`
Request: `{"confirmed":true,"startsAt":"2026-11-24T08:00:00+09:00","rrule":"FREQ=WEEKLY;BYDAY=MO,TH"}`; `confirmed` is required; omit `startsAt` or `rrule` to keep them; `"rrule": null` makes the Event one-off (explicit null, 2.4). Only `FREQ=WEEKLY` with `BYDAY` is accepted (BA 7.5).
Response 200: `<NoticeForPoster>` with `nextDates`. Errors: `422` with `details.reason` `unsupported_rule|past|no_upcoming`.

#### E-092 `POST /api/notices/{id}/publish`
Status: `live` · Roles: P · Screens: `notice-new` · Response 200: `<NoticeForPoster>` (status `published`). Errors: `409 EVENTS_NOT_CONFIRMED`; `409 NEEDS_EDIT` (title, steps or question missing in a language).

#### E-093 `GET /api/notices/{id}/reach`
Status: `live` · Roles: P · Screens: `life` (Poster view) · Response 200: `{"workers":8,"opened":5,"understood":3,"notOpened":3}` (counts over all Workers of the district, never names).

#### E-094 `GET /api/events`
Status: `live` · Roles: W · Request: `?from=2026-11-23&to=2026-11-24` (dates, at most 62 days apart; `to` not before `from`) · Screens: `life` (today and tomorrow), `senpai`
Response 200: bare array `[{"eventId":"11","noticeId":"7","title":{"ja":"..","en":".."},"at":"2026-11-24T08:00:00+09:00"}]`.

#### E-095 `POST /api/asks`
Status: `live` · Roles: W · Idempotency: required · Polling: none, AI inline · Screens: `ask` · Bus: `ask:new`
Request: multipart; `text` (max 1000) or `audio`, not both. Response 201: `<AskOut>`. A voice Ask comes back with `text: ""` (no transcript is kept, `backend/app/services/asks.py:134`, D-11). `covered` is true only when `sourceNoticeIds` is non-empty and all are published Notices of the district; otherwise `covered: false`, `answer: null` and the app shows its own "no posted notice covers this" text.
Errors: `422` both or neither source; `413`, `415`.

#### E-096 `GET /api/asks`
Status: `live` · Roles: W (own) · Poll: none stated; poll on screen open for `senpaiReply` · Screens: `ask`, `senpai`, `life` · Bus: `ask:reply` · Response 200: page of `<AskOut>`.

#### E-097 `POST /api/asks/{id}/forward`
Status: `live` · Roles: W · Idempotency: forwarding twice changes nothing · Screens: `ask` · Response 200: `<AskOut>` with `forwarded: true`. Errors: `409 NO_ACTIVE_PAIR`; `404`.

#### E-098 `GET /api/senpai-pairs`
Status: `live` · Roles: F (not Worker) · Screens: `senpai` (Foreman) · Response 200: page of `{"id":"3","workerId":"12","senpaiName":"田中 健","kind":"coworker","createdAt":"..","expiresAt":".."}`. No token. A Worker cannot read their own pair (B-04).

#### E-099 `POST /api/senpai-pairs`
Status: `live` · Roles: F · Screens: `senpai` (Foreman) · Request: `{"workerId":"12","senpaiName":"田中 健","kind":"coworker"}` (`kind` `coworker|jichikai`). Response 201: the pair plus `"token"`, shown once. Errors: `409 CONFLICT` the Worker already has a Senpai (revoke first).

#### E-100 `DELETE /api/senpai-pairs/{id}`
Status: `live` · Roles: F · Screens: `senpai` (Foreman) · Response 200: `data: null`. Revoking twice changes nothing.

#### E-101 `POST /api/meetups`
Status: `live` · Roles: W · Idempotency: none (D-20) · Screens: `senpai`, `life` · Bus: `meetup:new`
Request: `{"kind":"drill","date":"2026-12-06","note":"防災訓練に一緒に行った"}` (`kind` `coffee|event|drill|other`; `note` up to 500, default empty). No `pairId` or `loggedBy`. Response 201: `{"id":"5","pairId":"3","kind":"drill","date":"2026-12-06","note":"..","loggedBy":"worker"}`. Errors: `409 NO_ACTIVE_PAIR`; `422` future date ("A Meet-up is logged after it happened", `backend/app/services/senpai.py:130-134`).
There is no `GET /meetups` (B-04).

#### E-102 `GET /api/senpai/{token}`
Status: `live` · Roles: S (path token, no Bearer) · Screens: web Senpai page · Response 200: `{"senpaiName":"田中 健","worker":{"displayName":"Rahim Uddin","language":"bn"},"asks":[{"id":"31","text":"..","textJa":"..","createdAt":"..","replyJa":null}],"events":[<EventOccurrence>],"meetups":[<MeetupOut>]}`. A `todayWord` field is `new` (4.11, E-110). Errors: `404` wrong or revoked token.

#### E-103 `POST /api/senpai/{token}/asks/{askId}/reply`
Status: `live` · Roles: S · Request `{"replyJa":"..."}` (1 to 1000). Response 200: the forwarded Ask. Errors: `409 CONFLICT` already answered; `404`.

#### E-104 `POST /api/senpai/{token}/meetups`
Status: `live` · Roles: S · Request as E-101. Response 201: `<MeetupOut>` with `loggedBy: "senpai"`.

#### E-105 `GET /api/workers/{id}/calendar.ics`
Status: `planned` (`BA:512`, P2) · Roles: W (self) · Screens: `life` ("Add to calendar") · Not enveloped, not built; the web app builds the file in the browser. On Android use `CalendarContract` insert intents with the `GET /api/events` data instead (platform decision, Q-13).

### 4.11 Periodic Interview and Today's Word (new)

Source: handoff sections 3 and 4 (`handoff-backend-additions.md:23-32`). Shop scope, Foreman only. Workers cannot read these records. The due date is derived, never stored: last `held_on` plus 3 months; status `ok`, `soon` (14 days or fewer) or `overdue`.

#### E-106 `GET /api/interviews/due`
Status: `new` · Roles: F · Screens: `supportlog`, `interview`, `home` · Response `UNKNOWN` (per Worker: last held, due date, status `ok|soon|overdue`).

#### E-107 `GET /api/workers/{id}/interview-prep`
Status: `new` · Roles: F · Screens: `interview` · Facts since the last interview (Meet-ups, Notice receipts, Confusion terms, forwarded Asks) plus a fixed question list. Response `UNKNOWN`.

#### E-108 `GET /api/workers/{id}/interviews`
Status: `new` · Roles: F · Screens: `interview` ("past interviews"). Response `UNKNOWN`.

#### E-109 `POST /api/workers/{id}/interviews`
Status: `new` · Roles: F · Idempotency: the table has `idempotency_key` (header requirement `UNKNOWN`) · Screens: `interview` · Bus: `interview:new`
Request fields named by the handoff table: `held_on`, `method` (`face|online`), `online_consent_at` (required when `online`), `note_ja` (up to 1000). Wire names `UNKNOWN`. Audit `interview_recorded`.

#### E-110 `GET /api/today-word`
Status: `new` · Roles: F · Screens: `life` (Foreman) · One word of a Worker's language shown to the Japanese side; from `seed_data/phrasebook.json`, chosen by date from `shops.languages`. Also exposed as a `todayWord` field on `GET /api/senpai/{token}`. Response `UNKNOWN`.

### 4.12 Support Log

#### E-111 `GET /api/support-log.csv`
Status: `live` · Roles: F · Request: `?from=2026-11-01&to=2026-11-30` (dates, inclusive, both required; no range limit stated) · Screens: `supportlog` (export button) · Bus: none
Response 200: **not enveloped**, `Content-Type: text/csv; charset=utf-8`, UTF-8 with BOM, `Content-Disposition: attachment; filename="support-log-<from>-<to>.csv"`, `Cache-Control: no-store` (`backend/app/routers/support.py:110-114`). Rows: forwarded Asks and Senpai replies, Meet-ups, Notice receipts, Panel Overrides. Cells that start with `= + - @` are escaped. Download with the Bearer header into app storage and hand the file to a share intent; do not parse it for the on-screen summary (B-06). Item 10 (Periodic Interviews) is not in the file today; the handoff adds it (`handoff-backend-additions.md:27`).
Errors: `403`, `422` (`to` before `from`).

### 4.13 Blobs

#### E-112 `GET /api/blobs/{id}`
Status: `live` · Roles: F W P (district members only; a draft's photo only for the Poster who wrote it) · Screens: `notice`, `notice-new` (`photo` field of a Notice is `/api/blobs/{id}`) · Not enveloped: JPEG bytes, `Cache-Control: private, max-age=3600`. Needs the Bearer header, so use an authenticated image loader. Errors: `404`. Limits and format: 2 MB stored, JPEG only (D-16).

### 4.14 Demo

#### E-113 `POST /api/demo/reset`
Status: `planned` (`BA:528`) · Roles: F, only when the server runs with `DEMO_MODE=true` · Screens: none in the prototype · Re-runs the seed for the caller's shop only. Not for the release build; use only in a debug menu.

---
## 5. Mapping tables

### 5.1 Screen id to endpoints (every prototype screen id, once)

Screen ids are the keys of `SCREENS` in `prototypes/wakatta/parts/*.js` (34 ids; the merged app lists the same ones in `parts/*.screens.html`). "R" = reads, "W" = writes. "Pending" marks an endpoint that is `planned` or `new` (not callable today). `UNKNOWN` points to section 7.2 or an Ask in section 8.

| Screen id | Role | R / W (endpoints) | Notes |
| :--- | :--- | :--- | :--- |
| `chat` | F W | R: E-052 messages (poll 2 s, ETag), E-046 glossary, E-056 follow-ups (F). W: E-053 post (Foreman draft or Worker message), E-054 send, E-055 answers, E-058 reexplain, E-059 nudge, E-047 add term; group controls E-063..E-070 (Pending) | W also: `POST /messages/{id}/ask` (XREQ-022); stages 0 to 5 in the message |
| `home` | F W | F: E-040 board, E-056 follow-ups, E-038 near-misses (open count, Pending), E-060 tasks, E-052 (tally of Wakatta Status). W: E-033 briefings, E-051 threads (Checks waiting), E-060 tasks, E-043 passport, E-089 notices (pending count) | |
| `chats` | F W | R: E-051 threads, E-074 contacts, E-080 talk-threads. W: E-081 new talk | personal threads are `new` |
| `board` | F W | R: E-060 tasks. W: E-061, E-062 (`If-Match`) | this is the **Task Board**, not `GET /api/board` (E-040) |
| `machines` | F W | R: E-014 machines; F: E-040 board; W: E-043 passport (Ready or Locked) | |
| `me` | F W | R: E-005 me; W: E-043 passport; F: E-008 workers, E-041 skill-matrix. W: E-007 logout, E-006 change PIN | settings (dark, large text, offline) are local |
| `task` | F W | R: E-060 (no single-task route). W: E-062 | |
| `machine` | F W | R: E-016 detail; F: E-040 board, E-049 assignments; W: E-043 passport; E-060 tasks of the Machine (filter UNKNOWN, Q-12) | opens the Machine Panel screen |
| `scan` | W (F) | W: E-017 scan; R: E-016 detail | QR decode and payload: platform contract |
| `panel` | F; W view | F: E-018 panel (poll 2 s, ETag), E-020 override, E-021 end override, E-019 panel-token. W: Ready or Locked from E-043 | a Worker gets 403 on E-018 and so never sees an override. The Remind button has no endpoint (B-07) |
| `note` | W (F read) | W (Worker): R E-016 (current Note), W E-027 (Check, kind "note"); F: R E-024 | Foreman recording and approving (E-023, E-025, E-026) has no prototype screen (Q-05) |
| `nearmiss` | W | W: E-037 | Pending |
| `nearmiss-list` | F (W own) | R: E-038 | Worker's own list UNKNOWN (Q-09) |
| `nearmiss-detail` | F (W own) | R: E-038 (no get-by-id route). W: E-039 ack, E-061 task from a report | |
| `briefing` | F | W: E-030 draft, E-114 publish (E-031, E-032 are not built) | |
| `briefing-board` | F | R: E-035 receipts, E-033 | |
| `briefing-card` | W | R: E-033. W: E-036 receipt, E-027 Check kind "briefing" | |
| `life` | W (F, P) | W: R E-089 notices, E-094 events, E-096 asks. F: R E-110 today-word, E-098 pairs. P: R E-089 own, E-093 reach | reminders on Events are local |
| `notice` | W | R: E-089 (item from the list). W: E-036 receipt, E-027 Check kind "notice" | no get-by-id route |
| `notice-new` | P (prototype: F) | R: E-087 posters. W: E-088, E-090, E-091, E-092 | the prototype gates it on Foreman (D-21) |
| `ask` | W (F) | R: E-096. W: E-095, E-097. F view "質問（佐藤さん宛）": no Foreman route to read Asks (B-04) | |
| `senpai` | W (F) | Worker: R none (no route to read own pair or Meet-ups, B-04), W E-101. Foreman: R E-098, W E-099, E-100 | |
| `supportlog` | F | R: E-111 (CSV), E-106 due. | on-screen summary and PDF: B-06, Q-14 |
| `interview` | F | R: E-106, E-107, E-108. W: E-109 | all `new` |
| `connect` | F W P | W: E-071 My QR | |
| `connect-scan` | F W P | R: E-072 resolve. W: E-073 connect, E-078 block | |
| `pchat` | F W P | R: E-076 messages, E-080 talk-threads. W: E-077, E-075, E-078, E-079, E-081..E-086 | all `new` |
| `notifications` | F W | no list endpoint (B-07); push in the platform contract | |
| `onboarding` | F W | R: E-003 roster. W: E-004 login, E-006 PIN | scan and permissions: platform contract |
| `analytics` | F | R: E-042 heatmap, E-029 effort (partial); trend and per-Worker bars have no endpoint (B-05). W: E-047 add term | |
| `skills` | F | R: E-041 skill-matrix, E-040 board, E-008 workers. W: E-050 assign | |
| `followups` | F | R: E-056. W: E-057, E-058 | |
| `learning` | W (F) | no endpoint (B-08): Learning History is built from Confusions and Follow-ups, neither readable by a Worker | |
| `course` | W | no backend (static): content and progress are local (`prototypes/wakatta/parts/F.js:1-4`) | |

### 5.2 Bus events (27) to the replacement

The prototype's `Bus` fans an event out to both phones. In production there is no push channel for state: every consumer polls (2 s chat and Panel, 5 s board) and push is a hint (platform contract). Column "Carried by" says which call the receiving phone makes.

| Bus event | Prototype source | Replaced by | Carried by (receiver) |
| :--- | :--- | :--- | :--- |
| `ask:foreman` | `A.js:781` | `POST /api/messages/{id}/ask` (XREQ-022); `asked` on the Worker's message disables a second ask | E-052 poll |
| `ask:foremanReply` | `A.js:787` | a Foreman message with E-053 then E-054 (the draft and send flow) | E-052 poll |
| `ask:new` | `D.js:477` | E-095 (the response carries the Ask) | none (own Worker); Foreman view none (B-04) |
| `ask:reply` | `D.js:481,882` | E-103 (Senpai web page) | E-096 poll (`senpaiReply`) |
| `briefing:answer` | `C.js:620` | E-027 kind `briefing` | E-035 poll (F) |
| `briefing:new` | `C.js:548` | E-114 publish | E-033 poll (W) + push |
| `briefing:seen` | `C.js:633,662` | E-036 | E-035 poll (F) |
| `check:answer` | `A.js:360,617` | E-055 | E-052 poll (F) |
| `confusion:new` | `C.js:308` | server side effect of E-027 (kind `note`) and E-055 | E-044 poll (F) |
| `followup:new` | `A.js:363` | server side effect of E-055 | E-056 poll (F) |
| `followup:resolve` | `A.js:368` | E-057, E-058 | E-056 poll |
| `glossary:add` | `A.js:813`, `E.js:399` | E-047 | E-046 on screen open |
| `interview:new` | `D.js:623` | E-109 | E-106, E-108 |
| `meetup:new` | `D.js:541` | E-101 | none (no read route, B-04); E-111 CSV for the Foreman |
| `msg:new` | `A.js:495` | E-053 (+ E-054 to send) | E-051 and E-052 poll, push |
| `nearmiss:ack` | `C.js:481` | E-039 | E-038 poll, push to the reporter |
| `nearmiss:new` | `C.js:422` | E-037 | E-038 poll (F), push |
| `notice:answer` | `D.js:428,872` | E-027 kind `notice` | E-093 reach (P) |
| `notice:new` | `D.js:425` | E-088 then E-092 publish | E-089 poll (W) + push |
| `notify` | `skeleton.html:353` | no endpoint; push (platform contract) | none (B-07) |
| `notify:read` | `E.js:148,231` | local state; server read state UNKNOWN | none (B-07) |
| `signoff` | `C.js:320` | server side effect of E-027 (kind `note`) | E-043, E-040, E-018 |
| `task:update` | `A.js:955`, `B.js:273`, `B.init.js:29`, `C.js:474` | E-061 (Foreman), E-062 (Foreman or Worker); Tasks from a message are created by E-054 | E-060 poll |
| `wk:nudge` | `A.js:599` | E-059 | push (the Worker is reminded) |
| `wk:read` | `A.js:684,961` | no read-receipt route; reading with E-052 only moves the member's own unread marker, and a Foreman sees each Worker's `read` (unread marker at or after `sentAt`, or answered) in `results` on E-052 | none |
| `wk:reexplain` | `A.js:727` | E-058 | E-052 poll (that Worker) |
| `wk:stage` | `A.js:954` | the `stage` field of the message (E-053, E-054) | E-052 poll |

Event names were extracted by script from `Bus.emit('...')` and `Bus.on('...')` calls, including names inside the array at `B.js:459` and `E.js:550`.

### 5.3 Every `// shortcut:` comment (49) to what the real API does

49 lines contain `shortcut:` in `prototypes/wakatta/parts/*.js` (A 10, C 7, D 22, E 9, F 1; none in `skeleton.html` or the HTML fragments).

| Line | The prototype fakes | The real app and API |
| :--- | :--- | :--- |
| `A.js:73` | AI re-explanation per topic | E-058 (AI `reexplain`, XREQ-006 item 4) |
| `A.js:101` | simulated English dictation and translation | audio sent in E-053; backend transcribes and translates to Japanese (XREQ-005 item 3) |
| `A.js:108` | simulated Japanese dictation of a reply | audio in E-053; the backend translates for the Worker |
| `A.js:115` | AI suggestion for a glossary term | no endpoint (B-09); E-047 takes typed `bn` and `en` |
| `A.js:424` | timed reveal of the dictation | record audio and send in E-053; the draft returns `transcriptJa`. Live preview, if wanted, uses Android `SpeechRecognizer` ("Dictate instead", platform contract) |
| `A.js:491` | no machine translation of typed text | server translation on E-053 |
| `A.js:510` | browser speech synthesis | Android `TextToSpeech` on the device; no backend call; cloud TTS UNKNOWN |
| `A.js:694` | AI templates for re-explain | E-058 |
| `A.js:796` | AI suggestion in the glossary sheet | same as `A.js:115` |
| `A.js:921` | simulated Workers pass the re-explain Check | real Workers answer on their phones through E-055 |
| `C.js:109` | canned briefing dictation, translation, question | E-030 (audio, AI draft) |
| `C.js:147` | fake camera, chips as a QR read | CameraX + ML Kit (platform contract), then E-016 and E-017 |
| `C.js:329` | canned Near-miss dictation and translation | E-037 (audio or text; backend `translate`) |
| `C.js:335` | fake machine translation | E-037, backend `translate(text, source, target)` (`BA:552`) |
| `C.js:531` | canned Japanese dictation (briefing) | E-030 audio |
| `C.js:614` | browser speech synthesis | `TextToSpeech` on the device |
| `C.js:629` | scripted briefing answers for absent Workers | real answers through E-027 kind `briefing` |
| `D.js:14` | fixed demo date | device time shown in `Asia/Tokyo`; server dates from E-040 `date` |
| `D.js:159` | fake OCR of three flyers | E-088 with `sourceKind: photo` (AI reads the flyer) |
| `D.js:228` | Ask date has no year | `createdAt` is a full timestamp (E-096), so a date filter works |
| `D.js:231` | fixed interview questions in ja and en | the fixed question list comes from E-107; translation per language UNKNOWN |
| `D.js:241` | seeded Today's Word | E-110 (`todayWord`) |
| `D.js:363` | canned flyer results | E-088 (text, voice or photo) |
| `D.js:427` | simulated Workers answer a Notice | real answers through E-027 kind `notice`; Poster sees E-093 |
| `D.js:480` | fake Senpai reply | the Senpai answers on the web page (E-103); the Worker polls E-096 |
| `D.js:488` | fake speech for an Ask | audio in E-095, or typed text from `SpeechRecognizer` |
| `D.js:539` | Meet-up note not translated | same as the backend: `note` is stored as typed (E-101) |
| `D.js:681` | personal thread state local to the phone | user-scoped tables behind E-071..E-086 (ADR 0014) |
| `D.js:685` | seeded languages | `language` of `UserOut` (E-004) and the resolve answer (E-072) |
| `D.js:688` | canned dialect utterances | E-084 (transcription and `explain_utterance`) |
| `D.js:721` | counts matching ideas | E-084 `grade_understanding` (`green\|orange\|red` plus a hint) |
| `D.js:726` | four-phrase lookup instead of translation | E-077 and the `translate` pipeline |
| `D.js:802` | fake camera for contacts | CameraX + ML Kit, then E-072 and E-073 |
| `D.js:867` | play button animation only | no backend: audio is never stored (`BA:604`); local recording or TTS only |
| `D.js:892` | fake speech for a Notice | E-088 with `sourceKind: voice`, or typed text |
| `D.js:944` | placeholder share link | E-071 returns the real token; link shape in the platform contract |
| `D.js:956` | the contact answers with a canned line | E-077 then the other phone polls E-076 |
| `D.js:969` | fake speech for a Talk entry | audio in E-084; the transcript is saved, the audio is not |
| `D.js:996` | no real PDF | the server gives CSV (E-111); a PDF would be built on the device (Q-14) |
| `E.js:88` | local fallback confusion seed | E-044 |
| `E.js:212` | offline mode is visual only | offline outbox in the platform contract |
| `E.js:279` | fake QR scan to join the team | scan the shop QR, then E-003 and E-004 |
| `E.js:286` | fake microphone permission | Android `RECORD_AUDIO` runtime permission (platform contract) |
| `E.js:308` | seeded analytics | no analytics endpoint (B-05); E-042 and E-029 cover part |
| `E.js:407` | seeded Sign-off ages | real `passedAt` from E-043 or E-028 |
| `E.js:415` | one seeded expiring cell | `status: expiring` from E-040 or E-041 |
| `E.js:447` | only Rahim has a phone, so the Check is a toast | real Workers answer E-055; Foreman nudges with E-059 |
| `E.js:472` | seeded streak | no endpoint (B-08); would derive from the Worker's Check history, which no route returns |
| `F.js:24` | synthesized chime and thud | local sound resources; no backend |

---

## 6. Kotlin notes

### 6.1 DTO names per resource

| Resource | Class | Notes |
| :--- | :--- | :--- |
| Envelope | `ApiEnvelope<T>`, `ApiErrorEnvelope`, `ApiErrorBody(code, message, details: JsonObject)` | decode errors first by HTTP status, then the envelope; a `304` and a CSV or image have no envelope |
| Page | `Page<T>(items, nextCursor: String?)` | |
| User | `UserDto`, `LoginDto`, `RosterEntryDto`, `AccountDto`, `PinChangeRequest` | |
| Machine | `MachineDto`, `MachineDetailDto(current: NoteForWorkerDto?)`, `PanelDto`, `PanelOverrideDto`, `PanelTokenDto` | |
| Note | `NoteForWorkerDto` (no answer key), `NoteVersionDto` (Foreman, with `correctIndex`), `StepDto`, `StepEditRequest`, `ApproveRequest` | keep the two question classes separate (2.10) |
| Check | `CheckRequest(target, language, answers)`, `CheckResultDto`, `WrongQuestionDto`, `SignOffDto` | |
| Board | `BoardDto`, `BoardMachineDto`, `AssignmentOnBoardDto`, `AssignmentDto`, `AssignmentRequest` | `AssignmentRequest.workerId` needs explicit null |
| Confusion, Glossary | `ConfusionDto`, `GlossaryTermDto(ja, reading, bn, en)` | glossary has fixed fields (D-05) |
| Notice | `NoticeForPosterDto`, `NoticeForWorkerDto`, `EventDto`, `EventUpdateRequest`, `EventOccurrenceDto`, `NoticeReachDto`, `PosterDto` | two classes for `GET /notices` |
| Ask, Senpai | `AskDto`, `SenpaiPairDto`, `SenpaiPairCreatedDto`, `MeetupDto`, `MeetupRequest` | |
| Chat | `ThreadDto`, `MessageForSenderDto`, `MessageForWorkerDto`, `MessagesPageDto(items)`, `TaskDraftDto`, `SendRequest`, `AnswersRequest`, `AnswerResultDto`, `FollowUpDto`, `FollowUpUpdateRequest`, `ReexplainRequest`, `NudgeRequest`, `TaskDto`, `TaskCreateRequest`, `TaskUpdateRequest` | live; `GET /threads/{id}/messages` items are the sender or the worker class by role |
| Briefing | `BriefingForForemanDto`, `BriefingForWorkerDto`, `BriefingPublishRequest`, `BriefingReceiptsDto`, `ReceiptWorkerDto(id, name)` | two classes for `GET /briefings` |
| Skills, records | `SkillMatrixDto`, `SkillCellDto`, `HeatmapDto`, `HeatRowDto`, `PassportDto`, `OverrideRequest`, `OverrideDto`, `EffortDto`, `SignOffDto` | `OverrideRequest.workerId` needs explicit null |
| New | `ContactDto`, `TalkThreadDto`, `TalkEntryDto`, `InterviewDto`, `TodayWordDto`, `GroupMemberDto` | fields `UNKNOWN` |

### 6.2 Enums (all with an `UNKNOWN` fallback, 2.5)

| Enum | Values | Source |
| :--- | :--- | :--- |
| `Role` | `foreman`, `worker`, `poster` (the Panel session has no user) | `backend/app/schemas/auth.py:10` |
| `Lang` | `ja bn en vi id ne my tl zh` | `backend/app/schemas/common.py:10` |
| `Shift` | `day`, `night` | `backend/app/schemas/work.py:12` |
| `SkillStatus` | `valid`, `expiring`, `expired`, `none` | `backend/app/schemas/work.py:13` |
| `PanelState` | `idle`, `locked`, `ready`, `override` | `backend/app/schemas/work.py` `PanelOut` |
| `NoteStatus` | `draft`, `approved` | same |
| `ChangeKind` | `safety`, `wording` | same |
| `CheckTargetKind` | `note`, `briefing`, `notice` | same |
| `AiOutcome` (string today) | `ok`, `retried`, `fallback_seed`, `degraded`, `typed`; XREQ-003 adds `transcribed` | `BA:247`, `AGENTS.md:160` |
| `NoticeCategory` | `garbage`, `disaster`, `event`, `duty`, `other` | `backend/app/schemas/notices.py:11-12` |
| `NoticeSourceKind` | `text`, `voice`, `photo` | same |
| `NoticeStatus` | `draft`, `published` | same |
| `PosterKind` | `jichikai`, `city`, `employer` | same |
| `SenpaiKind` | `coworker`, `jichikai` | `backend/app/schemas/senpai.py:11` |
| `MeetupKind` | `coffee`, `event`, `drill`, `other` | same |
| `MeetupLoggedBy` | `worker`, `senpai` | same |
| `WakattaStatus` | `green`, `orange`, `red` on the wire; grey (waiting) is `myStatus: null` on `MessageForWorker` and `{status: "wait"}` is not sent | `backend/app/domain/chat.py:6`, `backend/app/schemas/chat.py` |
| `MessageKind` | `voice`, `text`, `ask`, `reexplain` live; `system` is new (handoff section 1) | `backend/app/schemas/chat.py:11`, handoff section 1 |
| `FollowUpStatus` | `open`, `clarified`, `understood` | `backend/app/domain/chat.py:7` |
| `TaskColumn` | `todo`, `doing`, `done` | `backend/app/schemas/chat.py:12` |
| `ThreadKind` | `group`, `direct` | `backend/app/schemas/chat.py:ThreadOut` |
| `ThreadMemberRole` | `admin`, `member` | handoff section 1 |
| `TalkGrade` | `green`, `orange`, `red` | handoff section 2 |
| `InterviewMethod`, `InterviewStatus` | `face`, `online`; `ok`, `soon`, `overdue` | handoff section 3 |
| `IconId` | 12 ids in the web client (`ppe-goggles` ... `calendar`); the backend types it as a plain string | `frontend/src/domain/wakatta.ts:11-23`, `backend/app/schemas/work.py:11` |

### 6.3 Fields that are `Tri` (`Map<String, String>`)

`MachineDto.name`, `BoardMachineDto.name`, `StepDto.text`, `CheckQuestion.prompt`, every item of `CheckQuestion.options`, `WrongQuestionDto.prompt`, `NoticeDto.title`, every item of `NoticeDto.steps`, `EventDto.title`, `EventOccurrenceDto.title`, `AskDto.answer`, `AskDto.senpaiReply`, `NoteVersionDto.backTranslation` (a map without `ja`, keys are the shop languages). Chat: `Task.title` and `TaskDraft.title` are Tri; message `text` is a map `ja` plus translations (empty for a degraded draft). Briefing: `BriefingDto.text`. Heatmap `translations` is a map without `ja`. Not Tri: `GlossaryTermDto` (fixed `bn` and `en`), `AskDto.text` (the Worker's own words, one string), `ForwardedAsk.text` and `textJa`.

### 6.4 Ids

Every `id` and every `...Id` field is a `String`: `id`, `userId`, `workerId`, `machineId`, `noteVersionId`, `checkId`, `shopId`, `districtId`, `posterId`, `noticeId`, `eventId`, `pairId`, `termId`, `currentVersionId`, `activeWorkerId`, and each element of `sourceNoticeIds`, `followUpIds`, `assigneeIds`. `questionId` is also a string but not numeric (`q1`, `q2`, `q3`). Never parse an id as a number; pass it back untouched in paths and bodies (a number in a body is a 422 for `IdIn` fields).

---
## 7. Discrepancies and open questions

### 7.1 Discrepancies

Sources compared: `BA` (docs/BACKEND_ARCHITECTURE.md), `openapi` (docs/openapi.yaml), `routers` (backend/app/routers and schemas), `web` (frontend/src/services and domain), `proto` (prototypes/wakatta/parts). Where the live code and a document disagree, this contract follows the code and says so. Ids are stable across revisions: D-12 (`approvedBy`) and D-14 (Briefing checks and receipts) were resolved by the merge on 2026-11-23 and removed; D-09 and D-15 were narrowed.

| Id | What differs | Evidence | What the app does |
| :--- | :--- | :--- | :--- |
| D-01 | Shift Assignments query param: BA says `?date=`, the live route and openapi use **`day`** | `BA:467`, `docs/openapi.yaml` (parameter `day` of `/api/shift-assignments`), `backend/app/routers/work.py:173` | send `day` |
| D-02 | A non-numeric id: BA 9.1 says 404; the live routes validate the id pattern and answer **422** (openapi lists 422 on every id route) | `BA:415`, `backend/app/schemas/common.py:13` | ids from the server are always numeric strings; treat a 422 on an id as a client bug |
| D-03 | ADR 0011 says every list is a cursor page; live bare arrays: `/auth/roster`, `/glossary`, `/shift-assignments`, `/events`, `/threads`. `GET /threads/{id}/messages` returns `{items}` with no `nextCursor` | `BA:418`, `backend/app/routers/auth.py:16`, `backend/app/routers/work.py:173,183`, `backend/app/routers/life.py:101`, `backend/app/routers/chat.py:49,73-76` | decode those as `List<T>` or `{items}`; do not add paging |
| D-04 | BA 17 item 11 drops `Machine.activeWorkerId`; the live `MachineOut` still has `activeWorkerId` | `BA:723`, `backend/app/schemas/work.py:31` | read it if present; do not rely on it for the Panel |
| D-05 | Glossary Terms are not a Tri: live `GlossaryOut` has fixed `bn` and `en`; BA 6.2 stores one translation per shop language. The AI is fixed to `bn`, `en` in notes, asks and chat drafts (the model chain and the glossary lock on chat changed, the language set did not) | `backend/app/schemas/work.py:208-213`, `BA:246`, `backend/app/services/notes.py:27`, `backend/app/services/chat.py:52`, `backend/app/services/asks.py:26` | for a shop with `vi` the glossary and drafts have no `vi` yet (Ask B-16) |
| D-06 | BA 9.2 lacks codes the code uses: `IDEMPOTENCY_KEY_REUSED`, `NEEDS_EDIT`, `NOT_READY`, `METHOD_NOT_ALLOWED`, `HTTP_ERROR`, `ALREADY_SENT`, `VERSION_CONFLICT` (412), `AI_UNAVAILABLE` (503); the handoff adds `THREAD_LOCKED` | `BA:421-438`, `backend/app/api/errors.py:17-30,71`, `backend/app/services/chat.py:349,480,567` | handle all of them (2.9) |
| D-07 | BA 8.5 and BA 17 item 15 say the Foreman board has an `ETag` and `304`; only `GET /machines/{id}/panel` and `GET /threads/{id}/messages` do. `GET /board`, `/threads` and `/tasks` have none | `BA:401,727`, `backend/app/routers/work.py:76-85,167-168`, `backend/app/routers/chat.py:68-85` | poll those three without `If-None-Match` until the backend adds it (B-11) |
| D-08 | BA 8.2 says a replayed create returns `200`; the live create routes are declared `201`, so a replay is also `201` | `BA:382`, `backend/app/routers/work.py:93-94`, `backend/app/services/notes.py:73-75` | treat `200` and `201` alike on creates |
| D-09 | A Worker cannot call `GET /machines/{id}/panel` (Panel and Foreman only), so a Worker never sees an active Panel Override; their Ready or Locked comes from the Skill Passport (E-043), which has no override field. Resolved for Lock and Ready, open for the amber override state | `backend/app/routers/work.py:44`, `backend/app/services/skills.py:62-75`, `BA:312` | use E-043; show "override" only on Foreman and Panel devices |
| D-10 | The web client is still on the old contract: `credentials: 'include'`, `POST /api/auth/pin`, Senpai reply `{reply}` (the API wants `{replyJa}`), `pairId` and `loggedBy` sent to `/meetups`, `Idempotency-Key` never sent | `frontend/src/services/wakatta.ts:54,411,423-427,463`; XREQ-001 and XREQ-002 are still pending (`AGENTS.md:57-58`) | follow this contract, not the web client |
| D-11 | A voice Ask is saved with an empty `text`: `AskDto.text` is `""` for voice Asks, though the model docstring says "or the transcript of their voice question" | `backend/app/services/asks.py:134`, `backend/app/schemas/asks.py:27-28` | show the audio or a "voice question" label when `text` is empty |
| D-13 | Web `scanMachine` and `submitCheck` send `workerId`; the API takes the Worker from the token and rejects unknown fields (422) | `frontend/src/services/wakatta.ts:80-85,153-159`, `BA:715` | never send `workerId` |
| D-15 | BA 9.3 still lists `GET /receipts?target=` (planned); the code has `GET /briefings/{id}/receipts` instead, and its worker entries use the key `name`, not `displayName` as in the XREQ-003 sample. BA 9.3 `PATCH /briefings/{id}` and `POST /briefings/{id}/publish` are replaced by `POST /briefings` | `BA:490-494`, `AGENTS.md:168-179`, `backend/app/schemas/briefings.py:41`, `backend/app/routers/briefings.py:45,50,59` | code against the live routes (E-035, E-114) |
| D-16 | Photo limits: BA 11 says jpeg, png or webp up to 5 MB in; the code accepts JPEG only up to 2 MB. Audio limit 8 MB matches. HEIC is not accepted | `BA:605`, `backend/app/media.py:25,28`, `backend/app/models/platform.py:12` | compress to JPEG at or under 2 MB (details: platform contract) |
| D-17 | The prototype's team code `PRESS-2F-4821` does not match the backend shop code `^[A-Z0-9]{8}$` (BA 6.1 and `ScopeCode`) | `prototypes/wakatta/parts/E.js:246`, `backend/app/schemas/auth.py:12` | use real 8-character codes only |
| D-18 | The XREQ-005 samples differ from the built schemas: `POST /messages/{id}/send` takes full `questions` objects (not strings) and `tasks`; a message `text` is a map and `questions` use `prompt`/`options`/`termId`, not `q`/`termJa`; `taskDrafts` appear only on the POST response | `AGENTS.md:237-251`, `backend/app/schemas/chat.py:70-76` | follow the schemas |
| D-19 | `GET /notices` and `GET /briefings` each return two different item shapes on one path by role (Foreman or Poster: `question` with `correctIndex`; Worker: without it, plus `opened`, `passed`); the openapi response is a union | `backend/app/routers/life.py:69-73`, `backend/app/routers/briefings.py:50-56`, `backend/app/schemas/notices.py` | two DTO classes chosen by role |
| D-20 | `POST /meetups`, `POST /tasks`, `POST /receipts` and `POST /briefings` (publish) have no `Idempotency-Key`; a retried `POST /meetups` or `POST /tasks` creates a duplicate, a retried publish answers `409` | `backend/app/routers/support.py:84`, `backend/app/routers/chat.py:132`, `backend/app/services/briefings.py:179`, BA 6.3 `meetups` row at `BA:265` | queue carefully; see Q-10 |
| D-21 | The prototype lets the **Foreman** post Notices (`isPoster = () => FM`); the backend lets only the **Poster** role post (`PosterUser`) and refuses a Foreman token with 403 | `prototypes/wakatta/parts/D.js:213,638`, `backend/app/routers/life.py:31` | product decision: a Poster login in the app, or the backend allows Foremen (Q-02) |
| D-22 | BA 12 says authenticated JSON gets `Cache-Control: no-store`; the code sets cache headers only on the Panel, chat poll, blob and CSV responses | `BA:622`, `backend/app/routers/work.py:85`, `backend/app/routers/chat.py:85`, `backend/app/routers/life.py:117`, `backend/app/routers/support.py:113` | the app must not rely on server cache headers |
| D-23 | `docs/openapi.yaml` declares no security scheme and no roles, so Bearer and the role list cannot be generated from it; roles in this file come from the `require(...)` guards | `docs/openapi.yaml` (no `securitySchemes`), `backend/app/api/auth.py:42` | set the Bearer header in one OkHttp interceptor |
| D-24 | `kind: "ask"` is in the message enum and the prototype's "ask the Foreman about a term" (`ask:foreman`) needs it, but no route creates one: `POST /threads/{id}/messages` makes only `voice` or `text` (Worker) or a draft (Foreman) | `backend/app/schemas/chat.py:11`, `backend/app/services/chat.py:245,307` | a Worker can send a plain message; the term link is lost (Ask B-22) |
| D-25 | `AGENTS.md` still shows XREQ-005 and XREQ-006 as `[PENDING]` (and XREQ-001, 002, 004, 007), XREQ-003 as `[IN_PROGRESS]`, although the Chat routes (XREQ-005), Briefing receipts and `approvedBy` (XREQ-003 items 5 and 6) are built | `AGENTS.md:57-63` | status in `AGENTS.md` is not a guide to what is live; use `docs/openapi.yaml` (Ask B-26) |

### 7.2 Open questions

| Id | Question | Needed for |
| :--- | :--- | :--- |
| Q-01 | Staging and release base URLs; HTTPS host. Nothing in the repo records them (BA 15 plans Azure Container Apps) | section 2.1, App Links |
| Q-02 | Does the Android app serve the Poster role? The prototype has no Poster user (D-21); the backend has one | scope of `notice-new`, E-087..E-093 |
| Q-04 | What is the term-linked "ask the Foreman" (`ask:foreman`) for Workers (D-24)? Message kind `ask` exists but no route sets it | `chat`, E-053 |
| Q-05 | The prototype has no screen to record, edit or approve a Note Version. Is the Foreman's recording flow in the Android scope? E-023..E-026 are live | Foreman Machine screens |
| Q-06 | Talk entries: the path to edit or delete one entry, and the route to read a Talk Thread's entries, are not named in the handoff | E-080, E-085, E-086 |
| Q-07 | May a Worker read their own Follow-ups and Confusions (needed for `learning`)? XREQ-005 lists Follow-up routes as Foreman tools | `learning`, B-08 |
| Q-08 | The Near-miss prototype records a severity (low, mid, high) and a photo mark position; the backend plan has neither field | E-037 |
| Q-09 | May a Worker list and open their own Near-miss Reports? BA 9.3 gives `GET /near-misses` to the Foreman only; the prototype shows "My reports" | `nearmiss`, `nearmiss-detail` |
| Q-10 | Duplicate protection for `POST /meetups`, `/tasks`, `/receipts` (D-20) | offline outbox |
| Q-11 | Is there a notification list and server-side read state, or push only? | `notifications` |
| Q-12 | Can `GET /tasks` be filtered by Machine (`machine` screen lists a Machine's Tasks)? | `machine` |
| Q-13 | Calendar export: Android intents from `GET /events` data, or the planned `.ics` route (P2)? | `life` |
| Q-14 | Support Log PDF: the server gives CSV only. Build a PDF on the device, or share the CSV? The on-screen counts need data the CSV does not give in JSON | `supportlog` |
| Q-15 | Page size and ordering for `GET /api/threads/{id}/messages?after=` (no `limit` parameter; an id after which all newer messages are returned) and how a deletion would show up (none exists) | `chat` |
| Q-16 | Which of the `new` creates will require `Idempotency-Key` (handoff says "on creates")? | 2.7 |
| Q-17 | Every other `UNKNOWN` in section 4: request and response shapes for E-031, E-032, E-034, E-037..E-039, E-048 and E-063..E-086, E-106..E-110; the `?target=` format of E-034; the wire value for a grey (waiting) Wakatta Status on the Foreman side (a Worker sees `myStatus: null`); whether the interview question list is translated per language; who may create groups (E-063); whether cloud TTS exists (shortcut `A.js:510`) | all of section 4 |

---

## 8. Backend asks

One row per ask. Owners are the suggested ones from the handoff (`handoff-backend-additions.md:35`: Asif tables and migration, Anim routes, services and tests, Galib AI pipelines) and `AGENTS.md` sections 5.3 to 5.5 (`AGENTS.md:531-541`). Ids are stable across revisions: B-01 (Worker Ready or Locked: the Passport is live), B-02 (Briefing routes) and B-10 (`approvedBy`) were resolved by the merge on 2026-11-23 and removed; B-15 was narrowed to the OpenAPI security scheme; B-05, B-08, B-12 and B-22 were narrowed. Q-03 was resolved in the same way.

| Id | Item | Endpoint or field | Suggested owner |
| :--- | :--- | :--- | :--- |
| B-03 | Build the Near-miss routes; add a get-by-id and a Worker's own list; decide severity and photo mark (Q-08, Q-09) | E-037..E-039 | Anim, Asif (table) |
| B-04 | Let a Worker read their own Senpai pair (name, link status) and their Meet-ups; let a Foreman read Asks (the prototype's Foreman Ask screen) | new `GET /senpai-pairs/me` (name `UNKNOWN`), `GET /meetups` | Anim |
| B-05 | An analytics read for the `analytics` screen: understanding trend, per-Worker green, orange, red counts over time. The heatmap (E-042) and effort metrics (E-029) now exist; the trend and per-Worker bars do not | new route `UNKNOWN` | Anim |
| B-06 | A JSON summary of the Support Log (counts of Periodic Interviews, Meet-ups, Notice checks per Worker and month) so the screen need not parse the CSV | new route `UNKNOWN` next to E-111 | Anim |
| B-07 | Decide notifications: a list with read state, or push only; a "remind this Worker" action for the Panel | E-051 area, new `GET /notifications` `UNKNOWN`; platform contract for push | Asif |
| B-08 | Worker-readable Follow-ups, Learning History, read receipts and a streak source (`GET /follow-ups` is Foreman only) | `wk:read`, E-056, new `GET /workers/{id}/learning` `UNKNOWN` | Anim |
| B-09 | Glossary: `PATCH /glossary/{id}`, a Tri-shaped term (`translations` per shop language) and an AI suggestion endpoint for the glossary sheet (D-05) | E-048, E-046, E-047 | Anim, Galib |
| B-11 | ETag and `304` on `GET /board`, `GET /tasks`, `GET /threads`, `GET /near-misses` (D-07) | E-040, E-060, E-051, E-038 | Anim |
| B-12 | `Idempotency-Key` on `POST /meetups`, `/tasks`, `/receipts`, `/briefings` (publish) (D-20) | E-101, E-061, E-036, E-114 | Anim |
| B-13 | State the replay status of creates (200 or 201) in BA 8.2 or change the routes (D-08) | all `POST` creates | Asif |
| B-14 | Update BA 9.2 with the codes in D-06 and 422 for a non-numeric id (D-02) | `docs/BACKEND_ARCHITECTURE.md` | Asif |
| B-15 | Regenerate `docs/openapi.yaml` with a Bearer security scheme and per-route roles so a client can be generated (D-23); the Chat and Briefing schemas now exist | `docs/openapi.yaml` | Asif |
| B-16 | Per-shop languages in the AI pipelines (`AI_LANGUAGES`), so a `vi` shop gets `vi` text (D-05) | `backend/app/services/notes.py:27` | Galib |
| B-17 | Keep a transcript for voice Asks (add `text` to `AskDraft`) (D-11) | E-095 | Galib, Anim |
| B-18 | A get-one route for a Notice (the `notice` screen reads the whole list today) | new `GET /notices/{id}` | Anim |
| B-19 | A Machine filter for Tasks (Q-12) | E-060 | Anim |
| B-20 | Reconcile photo limits and formats between BA 11 and the code, and say what HEIC from Android should become (D-16) | `backend/app/media.py` | Asif |
| B-21 | Publish staging and release base URLs and an HTTPS host (Q-01) | deployment | Asif |
| B-22 | Add the term-linked "ask the Foreman" message (kind `ask`) or drop that kind from the enum (D-24, Q-04) | E-053 | Anim |
| B-23 | Fill the handoff gaps before building: wire names and response shapes for E-063..E-086 and E-106..E-110, the Talk entry paths and a read route for entries (Q-06), and which creates need `Idempotency-Key` (Q-16) | E-063..E-110 | Anim, Asif |
| B-24 | Decide who may post Notices: Poster only (as coded) or also Foreman, as in the prototype (D-21) | E-088 | product owner, Anim |
| B-25 | Push device registration and the version gate (`GET /app/config`) are specified in [MOBILE_PLATFORM_CONTRACT.md](MOBILE_PLATFORM_CONTRACT.md); they are not repeated here | platform contract | Asif |
| B-26 | Update the XREQ statuses and fulfilment blocks in `AGENTS.md` (XREQ-005, XREQ-006, XREQ-003 items 5 and 6) to match what is built (D-25) | `AGENTS.md` | Anim, Asif |
