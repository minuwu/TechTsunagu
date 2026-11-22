# WS6 Briefing: build plan

**Owner** Galib (`Ga-lib`) · **Date** 2026-11-22 · **Status** plan; code starts when WS0 steps 2–5 are on `main` (tables, auth, route stubs, AI seam).

Sources: [BACKEND_ARCHITECTURE.md](../../docs/BACKEND_ARCHITECTURE.md) §6 (`briefings`, `receipts`, `checks`), §7.3 (Check grading), §8.2 (idempotency), §9 (routes), §10.2 (`build_briefing`); [BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md) (WS6 is mine, backend + AI).

## What it does (the demo path)

1. A Foreman records a spoken shift briefing (朝礼) or types it.
2. `build_briefing` turns it into やさしい日本語 + Bengali + English with one Check Question. The Foreman edits and publishes.
3. Workers see it on `/me`, open it (receipt) and answer the question (Check).
4. The Foreman sees who understood, who only opened it, and who has not opened it.

## Routes (all from §9, nothing new except item 5)

| # | Route | Who | My part |
| :--- | :--- | :--- | :--- |
| 1 | `POST /briefings/draft` (multipart `audio` or `text`, `Idempotency-Key`) | Foreman | service: idempotency lookup → `build_briefing` → glossary and question checks → store `draft` (`needs_edit` on fallback) |
| 2 | `PATCH /briefings/{id}` `{text?, question?}` | Foreman | only while `draft`; 409 otherwise |
| 3 | `POST /briefings/{id}/publish` | Foreman | sets `published_at`; 409 if already published |
| 4 | `GET /briefings` | Foreman, Worker | published, paged (`{items, nextCursor}`); Workers get the question without `correctIndex` |
| 5 | **Receipts summary for the Foreman** | Foreman | **missing from §9**; the plan's WS6 needs "understood / opened only / not opened". Proposal: `GET /briefings/{id}/receipts` → `{understood: [worker], openedOnly: [...], notOpened: [...]}`, computed from `receipts` + the shop's active Workers. Needs Asif's OK. |

Shared with others (not mine to build, I only call them):
- `POST /receipts` (opened) and `POST /checks` with `briefingId`: the receipts and checks services are shared with Anim (Notices) and Asif (Note Versions). A Briefing Check upserts the receipt's `passed` (§7.3).

## AI part

- Prompt: `backend/app/prompts/build_briefing.v1.md` (done, on `galib/ws1-ai`).
- Output: `app.ai.schemas.BriefingDraft`; checked with `app.domain.ai_checks` (`glossary_problems` on `text`, `question_problems` on the one question).
- Fallback: on a quota or validation failure, store a `needs_edit` draft holding the transcript or typed text, so the Foreman can still publish by hand. Never a 500.

## Tests I will write

- Draft → edit → publish happy path; edit after publish is 409.
- Same `Idempotency-Key` twice returns the same draft and calls the fake AI once.
- A Worker cannot create, edit or publish (403); a Worker of shop B cannot see shop A's Briefing (404).
- Worker response has no `correctIndex`.
- Receipts summary puts each active Worker in exactly one group.

## Needs from others

| From | What | When |
| :--- | :--- | :--- |
| Asif | WS0 steps 2–5: `briefings`/`receipts`/`checks` tables, auth + `require()`, route stubs, `AiClient` + fake | Mon 03:00 (his target) |
| Asif | OK for route 5 (receipts summary), or tell me where it belongs | before I build it |
| Minhaz / Arjun | Foreman receipts screen calls route 5; Worker Briefing card already exists in the mock | after route 5 is agreed |
