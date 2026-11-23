# Frontend → real API: handover (XREQ-001)

For: Minhaz (frontend). From: Asif (backend). Everything below is live on Azure; the contract is
[`docs/openapi.yaml`](openapi.yaml) (generate types from it, as `frontend/src/services/generated/` already does).

## 1. Point the app at the API

`frontend/.env`:

```
VITE_USE_MOCK=false
VITE_API_URL=https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io
```

- Health: `GET https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io/api/health/ready` → `{"success":true,"data":{"status":"ok"}}`.
- Check the whole connection from the frontend side with `cd frontend && npm run check:api` (health, database, CORS for
  `http://localhost:3000`, deployed contract vs `docs/openapi.yaml`, and a demo sign-in round trip that signs out again).
- CORS allows `http://localhost:3000` (your `npm run dev` port). When the frontend is hosted, send Asif the exact
  origin (`https://host`, no trailing slash); one command adds it (`backend/deploy/set-cors.ps1`).
- Every response is `{"success":true,"data":…}` or `{"success":false,"error":{"code","message","details"}}`.

## 2. Sign-in (replaces the shared PINs and "pick who you are")

Everyone has a personal account and PIN ([ADR 0010](adr/0010-personal-accounts-and-pins.md)).

1. `GET /api/auth/roster?code=SASEBO01&role=worker` (role `foreman`, `worker`, or `poster` with the district code)
   → `[{id, displayName}]`. Public; show it as the "who are you?" list.
2. `POST /api/auth/login` `{"userId":"3","pin":"2468"}` →
   `{token, expiresAt, mustChangePin, user:{id, displayName, role, language, shopId, districtId}}`.
   Errors: `401 UNAUTHORIZED` (wrong PIN), `423 PIN_LOCKED` (`details.until`), `429 RATE_LIMITED`.
3. Store `token` (not a `'1'` flag) and send **`Authorization: Bearer <token>`** on every call. Drop
   `credentials: 'include'`; the API uses no cookies.
4. `mustChangePin: true` → show "choose your PIN" first: `PUT /api/auth/me/pin` `{"currentPin","newPin"}`.
5. `GET /api/auth/me` restores the session on reload; `POST /api/auth/logout` ends it. A `401` anywhere = sign in again.

Never send `workerId` (or any user id for "me") in a body: the API takes it from the token.

## 3. Request rules

- **Lists** return `{items, nextCursor}`; next page: `?cursor=<nextCursor>&limit=`. Your `list()` must unwrap `items`
  (and follow `nextCursor` when you need everything).
- **`Idempotency-Key: crypto.randomUUID()`** (one per user action, reused on retry) is **required** on:
  `POST /api/machines/{id}/notes`, `/api/checks`, `/api/asks`, `/api/near-misses`, `/api/notices`,
  `/api/briefings/draft`, `/api/threads/{id}/messages`.
- Ids are strings. Timestamps are ISO 8601 in UTC (`…Z`).
- Workers never receive `correctIndex`; Check answers are graded on the server (`POST /api/checks`).

## 4. Your calls that change path

| Your mock call | Real API |
|---|---|
| `POST /api/auth/pin` (shared role PIN) | §2: roster → login → token; `PUT /api/auth/me/pin` |
| `list('notes')` | `GET /api/machines/{id}/versions` (Knowledge Library, newest first) + `GET /api/note-versions/{id}` |
| `list('receipts')` | Poster: `GET /api/notices/{id}/reach`; Foreman: `GET /api/briefings/{id}/receipts`; a Worker's own read/passed state is inside each item of `GET /api/notices` |
| `list('checks')` | not needed by any screen; a Check's result comes back from `POST /api/checks` |
| `list('meetups')` | `GET /api/meetups` (Worker: own; Foreman: the shop's) |
| Panel on a tablet | `POST /api/machines/{id}/panel-token` (Foreman) → the tablet sends that token; `GET /api/machines/{id}/panel` answers `304` with `If-None-Match` |

Everything else in `frontend/src/services/wakatta.ts` already matches a real route (33 of 38 calls).

## 5. Demo accounts (seeded; fictional)

| Who | Sign in with | PIN |
|---|---|---|
| Foreman 山口 隆 | shop `SASEBO01`, role `foreman` | `246810` |
| Workers Rahim Uddin, Sadia Akter, Tanvir Hasan, 田中 健 | shop `SASEBO01`, role `worker` | `2468` |
| Poster 自治会 事務局 | district `MINATO26`, role `poster` | `482615` |
| Poster 国際交流 担当 | district `MINATO26`, role `poster` | `739204` |
| Other shop (isolation demo): Foreman 森田 健 | shop `ISAHAYA2`, role `foreman` | `135790` |
| Senpai (no login) | `GET /api/senpai/demo-tanaka-7f3a9c`, `/api/senpai/demo-mori-2b8e41` | — |

Asif can wipe and reload this data any time (`backend/deploy/reset-demo.ps1`), e.g. before filming.

## 6. AI

The server starts with AI off (`AI_MODE=fake`): voice notes, Chat drafts and translations come back with
`needsEdit: true` and the transcript only, never a 500. Show the "please check / type it" state for `needsEdit`
(XREQ-004). Asif switches real AI on with one command once the key is in place.

## 7. Questions

Open an XREQ in `AGENTS.md` §2 addressed to Backend (Asif), with the endpoint and the response you expected.
