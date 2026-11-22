# WS1: how the AI pipelines are built and how everyone plugs in (proposal)

**Author** Galib (`Ga-lib`), AI / LLM · **Date** 2026-11-22 · **Reviewers** Asif, Anim (backend); Minhaz, Arjun (frontend, §6 only)
**Builds on** [AI_PLAN.md](AI_PLAN.md) (what we use), Asif's AI seam on `main` (`app/ai/client.py`, `app/services/ai_runner.py`) and his note service (`app/services/notes.py`), which is the pattern everything below copies.

> **Decision needed (Asif, Anim):** the ownership split in §1. If nobody objects by **Mon 03:00 BST**, I build to it. Everything else here follows from code already on `main`.

---

## 1. Who builds what

| Pipeline | AI part (prompt, schema, checks, fallback) | Service and route that call it | Status |
| :--- | :--- | :--- | :--- |
| `build_note` | Galib (prompt, schema, checks: done) | **Asif**, `services/notes.py` (done) | ✅ on `main` |
| `build_briefing` | Galib | **Galib**, WS6 `services/briefings.py` + routes | to build |
| `build_notice` | Galib | **Anim**, WS10 notices service | to build |
| `answer_ask` | Galib (includes Anim's `ground()`) | **Anim**, WS11 asks service | to build |
| `translate` | Galib | **Anim** (Senpai reply), WS7 owner (Near-miss, open) | to build |
| Model chain + Whisper step ([AI_PLAN.md](AI_PLAN.md) §3) | Galib, inside Asif's runner | — | waits for XREQ-003 |

Rule: **I own everything between "here is the input" and "here is a checked result"; the service owner owns reading the database, storing the result and the route.** Nobody calls Gemini or the runner directly except through these functions.

## 2. The pattern every pipeline follows (from `services/notes.py`)

1. The service reads what the AI needs (glossary, Notices) and **commits**, so no transaction stays open during the model call (§8.3).
2. It calls one function from `app/services/ai_pipelines.py` (§3).
3. That function renders the prompt (`app.ai.prompts.render`), wraps untrusted input in its delimited block, builds an `AiRequest` (pipeline name, prompt, parts, schema, scope `shop:<id>` or `district:<id>`, cache input) and calls `ai.run(request, validate, now)`.
4. `validate` returns the problems from `app.domain.ai_checks` (and Anim's rules where they apply); the runner retries once with them.
5. It returns a typed result: the checked draft, or `None` / a not-covered answer on fallback. **It never raises for an AI failure.**
6. The service stores it (`needs_edit=True` when the result is a fallback) and returns its API shape.

## 3. The functions (new file `app/services/ai_pipelines.py`, Galib)

It lives in `app/services/` because the layer rules forbid `app.ai` from importing services, and `AiRunner` is a service. All keyword-only; `now` is passed in, as in `notes.py`.

| Function | Input | Returns | Checks run inside |
| :--- | :--- | :--- | :--- |
| `glossary_block(terms)` | `list[GlossaryTerm]` | the JSON string the prompts take | — (moved from `notes.py`'s private `_glossary_block`; Asif may switch to it, optional) |
| `draft_briefing(ai, *, shop_id, terms, text, audio, now)` | text **or** audio bytes | `AiResult[BriefingDraft]` | `glossary_problems` on the text, `question_problems` |
| `draft_notice(ai, *, district_id, text, audio, photo, today, now)` | one of text / audio / photo (bytes + MIME) | `AiResult[NoticeDraft]` | `question_problems`; each Event through Anim's `recurrence.validate_event`, so the one retry can fix a bad date. No glossary: Notices belong to a district shared by several shops (XREQ-002 item 11) |
| `answer_ask(ai, *, district_id, text, audio, notices, languages, now)` | the question; `notices` = `[(notice_id, text)]`, at most 30 newest published | `GroundedAnswer` (Anim's type) | Anim's `ground()` with the ids that were sent; any fallback → `NOT_COVERED` |
| `translate(ai, *, scope, text, audio, source_lang, target_lang, now)` | text or audio | `str` or `None` (`None` = show the original with the "not translated" flag) | non-empty text |

Tests (offline, with `FakeAiClient`): for each function, one good scripted answer, one answer that fails a check then passes on retry, one `AiUnavailable` → the fallback shape. Anim's and Asif's service tests can script the same fake by pipeline name (`fake.queue("build_notice", {...})`).

## 4. What each teammate does with it

- **Anim (notices, asks, Senpai reply):** call `draft_notice`, `answer_ask`, `translate` from your services; map the result to your schemas (`app/schemas/notices.py`, `asks.py`, `senpai.py`). Store `needs_edit=True` when `AiResult.value` is `None`. For a voice Ask, store the transcript once the Whisper step exists (§5).
- **Asif (runner, notes):** nothing new until XREQ-003. When the chain is accepted, I change only the runner's model selection and budgets, and keep its `run()` signature, so no service changes. `notes.py` keeps working as is.
- **WS7 owner (Near-miss, open):** call `translate(... source_lang="bn" or "en", target_lang="ja")`.
- **Minhaz, Arjun:** the API shapes don't change. Drafts carry `needsEdit` (already in `NoteVersionOut`); XREQ-004 covers how to show it.

## 5. Order and deadlines (BST)

| When | Galib delivers | Unblocks |
| :--- | :--- | :--- |
| Mon 03:00 | ownership confirmed (or default) | everyone's parallel start |
| **Mon 10:00** | `ai_pipelines.py` with all four functions, fake fixtures, tests, merged to `main` | Anim's notices and asks services |
| Mon (when the key exists) | smoke test of the four Gemini models (AI_PLAN.md §6 step 1) | real calls in rehearsal |
| **Mon 18:00** | WS6 Briefing: service, routes (incl. the approved `GET /briefings/{id}/receipts`), tests | Minhaz's Briefing screens (P1) |
| after XREQ-003 is accepted | model chain + Whisper step in the runner | demo resilience |
| Tue 06:00 | P1 complete with Anim's Life services | — |
| Tue 10:00–14:00 | AI eval run, `docs/ai-eval.md`, cache filled for every filmed take | filming at 14:00 |

## 6. How we work in git (so our work merges cleanly)

- **One small branch and PR per piece:** `galib/ai-pipelines`, `galib/ws6-briefing`, `galib/ai-chain`. A PR is one reviewable slice.
- **Before every push:** `git pull origin main`, then in `backend/`: `uv run ruff check .`, `uv run ruff format --check app tests`, `uv run pyright`, `uv run lint-imports`, `uv run pytest`. All green, or no push.
- **Merge quickly after the check is green.** `main` moves every few minutes tonight, and "Verify Branch Sync" fails any PR that is behind. If it turns red only because `main` moved, pull and push again; don't leave a PR open for hours.
- **Stay in your own files:** I edit `app/ai/`, `app/prompts/`, `app/domain/ai_checks.py`, `app/services/ai_pipelines.py`, the briefing files and their tests. A change in someone else's file is their PR to review. Schema changes go through Asif (one Alembic head).
- **A signature change in `ai_pipelines.py` is a contract change:** tell Anim first (an XREQ, or a message plus the PR).
- **Review:** Asif reviews backend PRs (BACKEND_PLAN.md); for `ai_pipelines.py`, Anim reviews too, because his services depend on it.

## 7. Done means

- Works offline with `AI_MODE=fake`; a missing or failing model returns the fallback shape, never a 500.
- Tests for the good path, a retry path and the fallback path; ruff, format, pyright strict, import rules and pytest green on the PR.
- No real Worker data in any fixture or prompt; prompts versioned (`.v1.md`), and a change bumps the version.
- My work log updated with what changed and who it unblocks.

## 8. Questions

1. **Asif, Anim:** is the split in §1 right? Anim, do you want `answer_ask` to return your `GroundedAnswer` (as proposed), or the raw draft so your service calls `ground()` itself?
2. **Anim:** is Mon 10:00 early enough for `draft_notice` and `answer_ask`, given your cut order?
3. **Asif:** OK to move `_glossary_block` into `ai_pipelines.glossary_block` (your `notes.py` can keep its own copy)?
