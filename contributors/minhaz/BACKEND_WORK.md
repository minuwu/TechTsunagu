# Backend work list (from the Android app, 2026-11-24)

For whoever writes the backend code: Anim, Galib, or Minhaz after the Android work. Full requirements are in the XREQ cards in [AGENTS.md](../../AGENTS.md); this file gives the order, the code that is involved, and what is new. Update the Status column as you work.

## Before you start

1. Tell the team which row you take (XREQ status `[IN_PROGRESS]`), so two people do not build the same thing.
2. **A push to `main` that passes `backend-ci` deploys by itself** (`backend-deploy.yml`), and the API migrates the live database on start. Push only tested code.
3. Tests need PostgreSQL 16 and Python 3.12 (`backend/README.md`, "Local PostgreSQL"). The checks are `uv run ruff check .`, `ruff format --check .`, `pyright`, `lint-imports`, `pytest`. A pull request runs all of them without deploying.
4. After a route change, regenerate `docs/openapi.yaml` and the frontend types in the same commit. The Android test `OpenApiContractTest` fails until `ApiConfig.ApiPaths` has every path: tell Minhaz.
5. Keep the rules: ids from the token, never `correctIndex` to a Worker, every query limited to the caller's shop, optional `Idempotency-Key` on creates.

## Order

| # | Work | Where | Status |
|---|---|---|---|
| 1 | **AI capacity (new, core loop).** See below | `app/ai/chains.py`, server settings | TODO |
| 2 | **PATCH draft must not lose a good draft (new).** See below | `app/services/chat.py` `edit_draft`, `_apply_draft` | TODO |
| 3 | **Task drafts lost on a retried POST (new).** See below | `app/services/chat.py`, migration | TODO |
| 4 | XREQ-023 Near-miss: Worker's own list, get by id, severity, `textJa`, photo mark, `taskId`, reporter name | `routers/near_misses.py`, `services/near_misses.py`, migration | TODO |
| 5 | XREQ-024 Foreman reads Asks, `PUT /today-word`, Support Log summary, voice Ask transcript | `services/asks.py`, `services/support_log.py` | TODO |
| 6 | XREQ-021 Remind, override `Idempotency-Key`, `GET /tasks?machineId=` | `routers/work.py`, `services/floor.py`, `services/chat.py` | TODO |
| 7 | XREQ-019 Group controls | `services/chat.py`, migration | TODO |
| 8 | XREQ-025 APK hosting (Azure access needed) | `backend/deploy/` | TODO |
| 9 | XREQ-026 App Links host and `assetlinks.json` (Azure access needed) | `app/main.py` or static host | TODO |
| 10 | XREQ-014 Service-account key as an Azure secret (Minhaz sends the key to Anim privately) | server settings | TODO |

## 1. AI capacity

Seen on the hosted server on 2026-11-24, with drafts sent back to back: most came back `needsEdit: true` with no translation and no question after 14 to 35 s. Failing sentences had a clock time or none, so it is not a content rule. With 100 s between the AI calls, the full loop passed at the first try.

Likely cause (the server logs were not visible): Gemini answered 503 (see the XREQ-018 proof), so every draft reached `openai/gpt-oss-120b`, which `BUDGETS["free"]` limits to 1 call a minute and 40 a day for everyone. A validation retry uses a second call. When the model is down, a message has no Check Question, so the Worker cannot answer it.

Please do:
- Confirm in the logs: `ai limiter busy`, `ai budget exhausted`, `ai unavailable`.
- Before the demo, give the core loop more capacity: a paid tier (`BUDGETS["paid"]`) or another model in the chain. This is a cost decision for the team.
- Tell the app why a draft has no questions (busy, out of budget, or bad answer), so it can offer Retry.

Acceptance: ten different drafts sent back to back return at least nine with questions, each in under 10 s.

## 2. PATCH draft keeps the last good draft

`PATCH /messages/{id}` with the model down replaced a good draft (three languages, one question) with Japanese only and no question. The old translation does not match the new text, so it must not be sent. But the Foreman should be able to go back: either keep the old draft and answer 503 `AI_UNAVAILABLE`, or store the new text as `needsEdit` and allow the same PATCH again as a retry. Add a test for the choice.

## 3. Task drafts on a retried POST

The comment in `_apply_draft` says Task drafts live only in the response. On a phone with a weak network, the app repeats the POST with the same `Idempotency-Key` and gets no Task drafts, so the Task is silently missing. Store them with the message (JSON column, migration) and return them in `MessageForSender` until Send.

## Small gaps found in the emulator run

- A Worker cannot read the names of the other assignees of a Task or of the sender of a message (`GET /workers` is Foreman only). The app shows the Thread title and "+N". A `displayName` next to `fromUserId` and `assigneeIds` would fix it (add to XREQ-021).
- `ThreadOut` has only `titleJa`. A direct Thread is titled with the Worker's own name, so the Worker sees their own name. A title for the caller, or `kind` plus the other member's name, would fix it (add to XREQ-021).
- The Worker Home reads the messages of each Thread to find unanswered Checks (one call per Thread). One route with the caller's unanswered Checks would replace them.
- `TaskOut` gives `fromMessageId` without the Thread id, so the app cannot open the source message.

## Waiting on the app

| Delivered | App status |
|---|---|
| XREQ-018 Talk Threads | batch 2c |
| XREQ-020 Learning and analytics | batch 2b |
| XREQ-022 Chat routes | batch 2a |
| XREQ-014 FCM sender | app side built, not tested on a device |

## Test data

The live check left messages that start with 【smoke test】 in the demo shop `SASEBO01`, some unsent drafts, Follow-ups and two new Tasks (one titled 【smoke test】始業前点検). `backend/deploy/reset-demo.ps1` removes them.
