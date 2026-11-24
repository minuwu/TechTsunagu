# Galib's Work Log

Owner: Galib (`Ga-lib`) — Role: AI / LLM

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-24 night (Minhaz's open items, at the user's request)

- **M7 / XREQ-016 resolved** (`939dc9b`): the Android "Ask Wakatta" screen, pinned on the Worker home and on the
  Machine screen. **M4** (`6c52cda`) "Draft, not sent" and "Japanese only" labels. **M9** (`0f18500`) read-aloud for
  every message in its language. **M10** (`dc26fa6`) "I explained it" on Follow-ups. **M11** (`877e65a`) the new shop
  code no longer vanishes after registering. **Message delete** (`c8222b0`, ADR 0014).
- Docs: the 429 row in the platform contract; XREQ-001 and 002 checked (the web still runs on mock data).
- Not done (need a phone, keys or a person): M8 Firebase push, XREQ-025 install test, M12 a new signed release,
  the README and making the repository public (Minhaz).

## 2026-11-24 evening (Arjun's open items, at the user's request)

- **Frontend CI was red on main** (stale API types after the Machine admin routes): regenerated (`4cdde01`).
- **429:** already handled by Arjun's write-failure toast; the chatbot panel now says busy / today's limit too (`ed88957`).
- **XREQ-004, web record screen** (`960f89e`, `a0bf2ef`): "Dictate instead", the "AI unavailable" card, the
  provenance line and "approved by"; then the Briefing and Poster notice screens (`cefb7b3`). XREQ-004 resolved.
- **Android R5, R6, R7** for Arjun (`181fd94`, `8986a32`, `3236809`), tests green (chat 90, nearmiss 44, connect 79).
- Hero checked at 1440 / 768 / 375 px, motion on and off: no horizontal scroll, no console errors.
  Course drawings were already recoloured (`53c59db`).

## 2026-11-24 evening (after filming)

- **XREQ-006 item 5, closed** (`98ddf84`, `c9d2ba7`): the Chat message eval found both free Gemini models putting
  the Machine code (`press-brake-2`) in `machine_id`, so the die-change message had no draft. Fixed by
  `draft_message.v4` (`7bf5ef8`); 3.1-flash-lite went from 2/10 to 7/10 passed, 10/10 valid. Prompt injection case
  m11 added and passing (`89246da`).
- **G1 proven live**: 10 drafts all under 45 s. Fix found on the way (`86c69e6`): Groq always gets its turn.
- **G3 decided**: free tier for filming; `AI_TIER=paid` for a real shop.
- **backend-ci was red on main** (pyright, `floor.py`, the Machine admin merge), so nothing deployed; fixed (`4bed1c4`).

## 2026-11-24 (from Arjun: AI checks, with your OK)

- **Checked:** `tests/ai`, `tests/fixtures/ai` and `test_ai_checks` pass (81 tests); ruff, format and pyright are clean on `app/ai`. On this PC, run pytest and pyright as `uv run python -m pytest` and `uv run python -m pyright` (Windows Application Control blocks the `.exe` launchers).
- **XREQ-006 item 5, docs** (`2e2db1f`): [docs/ai-eval-messages.md](../../docs/ai-eval-messages.md) describes the 10 Chat cases and what `eval_messages` scores. It is linked from `ai-eval.md`.
- **Fixed** (`2e2db1f`): `app.ai.eval` rewrites `docs/ai-eval.md` on every run, so hand-added sections were lost; the chatbot link was one of them. `report()` now always links the messages and chatbot write-ups, with a test. Your "Takeaways" prose in `ai-eval.md` will still be replaced at the next run: keep a copy.
- **Still yours:**
  - The card wants one **prompt-injection** case ("ignore your instructions…"); m03 tests toning down, not injection.
  - Run the live `--pipelines messages` eval after filming, then close XREQ-006.
  - G3 capacity: free tier is about 900 calls a day and 24 a minute on Flash-Lite, but a Gemini 503 drops everything to Groq at 1 a minute. Paid or not needs the live logs.

## 2026-11-24 (Android audit and fix plan)

- **G1 / audit A1** (`a585352`): one model call stops at 15 s, the whole request at 25 s, then degraded (needsEdit).
- **G2 / audit A4** (`a585352`): `draft_message.v3` keeps the Foreman's words; a Machine name he did not say is
  rejected in code; a greeting may have no questions. Live: "machine 3", the insult and "hello" all right.
- **Eval** for G2 and XREQ-006 item 5: 10 Chat messages in `tests/fixtures/ai/eval/messages.json`, scored by
  `eval_messages` (kept words, nothing added, questions or none, Tasks: people, due time, Machine). Run after filming.
- Asif's task 3 (`5879499`): two identical Talk entry retries at once return one entry, not a 500.
- **G4** Android chatbot data layer in `feature/asks` (models, Real/Fake repository, `ChatbotViewModel`), 8 tests;
  `testDebugUnitTest checkModuleGraph :app:assembleDebug` green. Run Gradle from PowerShell on this PC (Git Bash
  breaks the test JVM on the PATH's spaces).
- CI was red after the voice-fallback route: regenerated the API index and frontend types (`c28c264`).

## 2026-11-23 night (Asif's chatbot review)

- **Bengali removed from AI output** (team decision): demo shops `languages = {en}`, workers Rahim/Sadia/Tanvir `en`,
  district Notices and every AI default `("en",)`. Ready-written `bn` seed text stays but nothing asks the AI for it.
  API shape unchanged. The saved demo answers must be warmed again after deploy (issue #14 step 3 does it).
- **Chatbot eval:** `app/ai/eval_chatbot.py` + 18 cases in `tests/fixtures/ai/eval/knowledge.json` (en and ja; covered,
  not covered, an injection, emergencies), through the real route. Result in `docs/ai-eval-chatbot.md`: every model
  answer correct and cited, 0 wrong; Gemini 503s tonight left some rows unanswered (rerun on a calm Gemini).
- **Search fix:** Japanese questions are searched with their English translation only (Japanese pairs such as
  「ください」 matched unrelated guide pages).
- `RAG_PLAN.md` §6 no longer says "pitch only"; issue #14 has the chatbot smoke-check note.

## 2026-11-23 (from Arjun: two AI fixes on `main`, please skim)

- **Voice Ask transcript** (`eadef5f`, XREQ-011 item 5b, Anim's request): `AskDraft.question_text` (required); `answer_ask` is now **v2** (new cache key), the eval renders v2. A voice Ask stores the transcript, so `/forward` no longer sends the Senpai an empty message. Your Bangla-removal prompt for Asks can be v3.
- **Runner, busy model** (`7038b77`): a model at its per-minute limit used to wait up to `wait_seconds` (10 s) before handing over, and had already spent one of its daily calls. Now the per-minute slot is taken before the daily budget, and only the last model of the chain (and the validation retry) waits; earlier models hand over at once. New test fails in 37 s on the old runner.
- Also: BACKEND_ARCHITECTURE §10.3/§10.4 now describe your chain (`AI_CHAIN`, `AI_TIER`, budgets, Groq backup, busy model skipped at once).
- Verified on a local PostgreSQL 16.14: full backend suite, ruff, pyright, lint-imports.

## 2026-11-23

### Completed
- **Live AI check (item 3 of the handoff below):** the Wakatta? Gemini key and a Groq key exist (in my local `backend/.env`, never in git). One call per model with the same Japanese voice note (TTS, 4 shop words: 玉掛け, 保護メガネ, 寸法, ノギス), audio in, JSON out:

  | Model | Result | Time | Shop words |
  | :--- | :--- | ---: | :--- |
  | Gemini 3.8 Flash | 503 "high demand" twice (Google's side, not quota) | — | — |
  | Gemini 3.6 Flash | ok | 48 s | 4/4 |
  | Gemini 3.5 Flash-Lite | ok | 3 s | 4/4, clean ja/bn/en |
  | Gemini 3.1 Flash-Lite | ok | 3 s | 4/4, but bn/en keep Japanese words in brackets |
  | Groq `whisper-large-v3-turbo` | ok | 0.6 s | 2/4 (保護眼鏡, のぎす: other script; "measure in 2 places" garbled) |

  Takeaways: audio + JSON works on every model that answered; 3.5 Flash-Lite is the strong everyday model; the 503 shows why the chain (AI_PLAN.md §3) must skip a busy model, not only an exhausted one; Whisper stays the last voice fallback (it loses shop words). I retry 3.8 later.
- **Benchmark of free non-Gemini models** on our real `build_note` task (6 eval notes, one with a planted instruction; scored by `app.domain.ai_checks`, one retry like `AiRunner`): Gemini 3.5 and 3.1 Flash-Lite 6/6; Groq `gpt-oss-120b` 5/6 and ignored the planted line; Groq Qwen 3.8 27B 6/6 but **copied the planted instruction into the steps** and is a preview model; `gpt-oss-20b` 1/6. Also checked: OpenRouter free (50 calls/day), Hugging Face ($0.10/month), PLaMo, Kotoba-Whisper, Cloudflare, Mistral, Cerebras; none worth it now. → `gpt-oss-120b` is the backup.
- **Found and fixed: every real Gemini call returned HTTP 400** (`response_schema` rejects `additionalProperties`, which `extra="forbid"` adds). **PR #8, merged:** the fix plus XREQ-003 items 1–4: model chain (`AI_CHAIN=dev|demo`), per-model budgets (`AI_TIER=free|paid`, `app/ai/chains.py`), cache reuse across the chain, Groq backup (Whisper → gpt-oss-120b, on when `GROQ_API_KEY` is set). CI green; verified live.
- **Team decision: Bangla is dropped, Japanese + English only.** Not yet changed in code (prompts, `AiTri`, glossary seed, screens still have `bn`).
- **Paid AI proposal** (shared with Asif and Minhaz as a page): Gemini paid Tier 1 (main) + Groq Developer plan (backup). About $3 (৳375) for the next week (~1,000 calls), about $9 (৳1,125) a month (~3,000 calls). Pay now: $10 (৳1,250) Google prepay; cap $20 (৳2,500).

### Blockers / handoff
- **Asif:** PR #8 changed your runner and settings (constructor and `run()` unchanged, no service changes). For Azure: `AI_MODE=gemini`, `AI_CHAIN=demo`, secrets `GEMINI_API_KEY` and `GROQ_API_KEY` (keys by private message), `AI_TIER=paid` once billing is on. BACKEND_ARCHITECTURE §10.3/§10.4 still name `GEMINI_MODEL` / `AI_DAILY_BUDGET`. The `transcribed` value for `ai_outcome` still needs your migration (less urgent now: the backup writes a full draft).
- **Anim:** your Chat AI (XREQ-006) is good; I add the missing checks (glossary lock on the message, a due time is never guessed, the Re-explanation question is on the missed term) in a small PR that touches `_draft_problems` in `services/chat.py`.
- **Minhaz:** please OK the AI budget (৳1,250 now, ৳2,500 cap) and say whose card we use; some Bangladeshi cards are refused, so try today.
- **Everyone, Bangla dropped:** please stop adding Bengali. I change the AI side (schemas, checks, prompts v2, fixtures); Asif/Anim `AI_LANGUAGES` and the glossary seed; Minhaz/Arjun the screens.

## 2026-11-22 (proto → production handoff from Minhaz)

Minhaz mapped the prototype (`prototypes/wakatta/`) and the frontend against `main` (`c07a8fb`). Decisions: the prototype's **Chat is in production scope (WS12, P1)**; Course stays P2 and frontend-only. Read [BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md) decision #8 and the new XREQ cards in [AGENTS.md](../../AGENTS.md) §2.2. Do the items in this order.

### Next (assigned)
1. **XREQ-006 (WS12 Chat AI)**: `draft_message` (voice or text → `transcriptJa`, translations, 1–3 Check Questions, **Task extraction**: assignees from Thread members, `dueAt` only when said, Machine from the shop), plus `translate` (Worker message → ja) and `reexplain`. Checks in `domain/ai_checks.py`; degraded result never raises.
2. **Voice ladder for every voice input (notes, chat, notices, asks, briefings):** cache → Gemini chain → Groq Whisper transcript → text path → degraded `needsEdit`. Needs Asif's answer on XREQ-003.
3. **Live AI before the switch:** create the Wakatta?-only Google project, give the key to Asif privately for Azure, run one live call per model (audio + schema) and log the result.
4. **WS6 Briefings** (backend + AI): the frontend calls `POST /api/briefings/draft`, `/api/briefings`; your receipts route is approved.
5. Anim's open request: a transcript field on `AskDraft` (e.g. `question_text`) so voice Asks store their text.
6. **Eval** (`python -m app.ai.eval` → `docs/ai-eval.md`): notes, notices, asks, plus the 10 chat messages scoring Task extraction.
7. **Before filming:** seed `ai_cache` with every filmed take (die-change chat message from the prototype's `DATA.script`, flyer, Ask, Foreman note). Flyer fixture: recyclables as a step without an Event (XREQ-002 item 12).
8. WS7: Near-miss translation for Anim's routes (P2).

### Blockers / handoff
- Item 2 waits on Asif's XREQ-003 decision. Anim builds the Chat service on top of your functions; agree the `draft_message` signature with him first (card XREQ-006).

## 2026-11-22

### Completed
- **WS1 integration plan, for review:** WS1_INTEGRATION_PLAN.md. Who builds which pipeline (proposal: I build `app/services/ai_pipelines.py` with `draft_briefing`, `draft_notice`, `answer_ask`, `translate`; Anim's services call them; Asif keeps `notes.py`), the pattern copied from Asif's `notes.py`, function signatures, deadlines (pipelines Mon 10:00, Briefing Mon 18:00, eval Tue 10:00–14:00), the git workflow and "done means". Linked from XREQ-003 item 7; default ownership at Mon 03:00 unless Asif or Anim objects.
- **Free-AI plan, for review:** [AI_PLAN.md](AI_PLAN.md). The team agreed to use every free AI API; the plan covers which model does what (`dev` and `demo` chains behind `AI_CHAIN`), the fallback ladder (cache → Gemini chain → seeded → Groq Whisper transcript for voice → degraded), per-model budgets (no schema change: `ai_usage` is already per model), privacy (fictional data only on free tiers), the eval and the demo-day quota calendar. Filed **XREQ-003** (Asif: chain, budgets, Whisper, settings, receipts route, `approvedBy`/`needsEdit` in responses) and **XREQ-004** (Minhaz, Arjun: "Dictate instead", degraded-draft message, provenance line). Questions for reviewers in AI_PLAN.md §9.
- **WS1, the parts that need no WS0** (branch `galib/ws1-ai`). Pure code, no skeleton, no database:
  - `backend/app/ai/schemas.py`: what each pipeline asks Gemini to return (`NoteDraft`, `BriefingDraft`, `NoticeDraft`, `AskDraft`, `Translation`). Simple schemas only (point 5); icon ids equal the frontend's `IconId`. Shortcut: languages fixed to ja/bn/en until a shop adds more.
  - `backend/app/domain/ai_checks.py`: the glossary lock exactly as §7.4 (NFKC; a term is in scope by `ja` or `reading`; steps keep `ja`; every language uses the fixed translation, case-insensitive for Latin scripts) and question checks (no two options with the same text in any language, `term_ja` must be a Glossary Term). Returns plain-English problems for the one retry prompt.
  - `backend/app/prompts/*.v1.md`: `build_note`, `build_briefing`, `build_notice`, `answer_ask`, `translate`. Untrusted text is data (point 6); notices never guess a date or weekday; asks answer only from the given Notices.
  - `backend/tests/fixtures/ai/`: `glossary.json` (the 15 seeded terms, copied from the frontend mock) and the eval set: 20 Foreman notes covering all 10 Work terms (one spoken only as readings, one with numbers, one prompt injection), 4 notices (the garbage one uses only Anim's verified rules, so the right answer has no Event), 6 asks (2 not covered).
  - Tests: the plan's three (passing note, missing locked term, two correct options) plus reading-only terms, unknown term, schema limits, eval-set completeness and prompt placeholders. Verified: 106 tests pass (88 before), ruff, `ruff format --check` and pyright strict clean.
- **WS6 Briefing plan:** [WS6_BRIEFING_PLAN.md](WS6_BRIEFING_PLAN.md). Found a gap: §9 has no route for the Foreman's "understood / opened only / not opened" view; proposed `GET /briefings/{id}/receipts` for Asif to approve.
- **AI options, all free (proposal for Asif, §10.3/§10.4).** Checked against official pages on 2026-11-22:

  | Option | Free limit | Source | Proposed role |
  | :--- | :--- | :--- | :--- |
  | Fake AI + `ai_cache` + seeded results | unlimited | already in the design (§10) | default for all development; filmed takes cached |
  | `gemini-3.5-flash-lite` (audio, image, JSON) | 500/day, 15/min | [model page](https://ai.google.dev/gemini-api/docs/models/gemini-3.5-flash-lite), AI Studio | prompt work, AI eval, first backup |
  | `gemini-3.1-flash-lite` (audio, image, JSON) | 500/day | [model page](https://ai.google.dev/gemini-api/docs/models/gemini-3.1-flash-lite), AI Studio | second backup (new) |
  | `gemini-3.8-flash` (audio, image, JSON) | 20/day, 5/min | [model page](https://ai.google.dev/gemini-api/docs/models/gemini-3.8-flash), AI Studio | filmed takes and live demo only |
  | `gemini-3.6-flash` | 20/day | AI Studio | demo-day backup for 3.8 (new) |
  | Browser speech recognition (`ja-JP`) | unlimited | [caniuse](https://caniuse.com/speech-recognition): partial support in Chrome (desktop, Android) and Safari (macOS, iOS); none in Firefox | Foreman voice → text when all AI is down (new, frontend) |
  | Groq Whisper (transcription only) | 2,000/day, org-wide | [Groq limits](https://console.groq.com/docs/rate-limits) | optional last resort; less accurate on Japanese than Gemini in one third-party benchmark |

  - Not usable or not free: Azure OpenAI on Azure for Students (no quota, per Microsoft Q&A), Google's $300 trial (doesn't cover AI Studio for accounts opened after 2026-03-02), Mistral Voxtral (no free API).
  - Free-tier inputs are used by Google to improve its products ([pricing page](https://ai.google.dev/gemini-api/docs/pricing)): fine for our fictional demo data, never for real Worker data. Paid tier, if the team chooses it later: $5 minimum prepay, $250/month cap ([billing page](https://ai.google.dev/gemini-api/docs/billing)).
  - Rules for the free plan: nobody tests on 3.8 Flash; cache each filmed take as soon as it works; a Google project used only for Wakatta?; rehearse on demo day with the cache, not live AI.
  - Still unmeasured: Bengali quality (no Japanese→Bengali benchmark exists; the AI eval must include a teammate scoring the Bengali) and whether the per-model quotas really add up (seen in AI Studio, not stated in Google's docs). A live test script (audio + JSON, one call per model) is ready and runs as soon as the Wakatta? project key is in `backend/.env`.
- Researched [Rover Villa](https://rovervilla.space/heroes/phoenix) for ideas we can use: ROVERVILLA_IDEAS.md. My picks: machine photos with tap-the-part Check Questions (understanding checked without depending on translation), a provenance line on every AI output, and a "Veteran's voice outlives the Veteran" beat in the pitch. Proposal only; needs team agreement and an XREQ for the first one.
- Checked the Gemini open item Asif assigned me ([BACKEND_ARCHITECTURE.md §15](../../docs/BACKEND_ARCHITECTURE.md#15-open-items)): "Confirm the current Gemini Flash model accepts audio input together with a response schema, and its daily limit on our key."
  - **Model:** `gemini-3.8-flash` is the current stable Flash model (updated September 2026). Google's model page lists audio, image, video, PDF and text as inputs and structured outputs as supported, so one call with the Foreman's audio and our JSON schema works. Checked from the docs only; no live call yet.
  - **Flash-Lite:** `gemini-3.5-flash-lite` (updated July 2026) also takes audio and images and supports structured outputs.
  - **Free-tier limits, read from AI Studio's rate-limit page on 2026-11-22:**

    | Model | RPM | TPM | RPD |
    | :--- | ---: | ---: | ---: |
    | Gemini 3.8 Flash | 5 | 250K | **20** |
    | Gemini 3.6 Flash | 5 | 250K | 20 |
    | Gemini 2.5 Flash | 5 | 250K | 20 |
    | Gemini 3.5 Flash-Lite | 15 | 250K | **500** |
    | Gemini 3.1 Flash Lite | 15 | 250K | 500 |

  - Limits apply **per Google Cloud project, not per API key**, so extra keys in the same project add nothing. The daily count resets at midnight Pacific time (14:00 Bangladesh time).
  - The project I checked already had other usage today (3.6 Flash 26/20, 2.5 Flash 28/20, 3.8 Flash 5/20), so I will create a new project used only for Wakatta?.
- Sources: [Gemini 3.8 Flash](https://ai.google.dev/gemini-api/docs/models/gemini-3.8-flash), [Gemini 3.5 Flash-Lite](https://ai.google.dev/gemini-api/docs/models/gemini-3.5-flash-lite), [Rate limits](https://ai.google.dev/gemini-api/docs/rate-limits) (Google does not publish free-tier numbers there; it points to the AI Studio rate-limit page).

- Fact check (plan, "Facts to verify"): MHLW 46.2% confirmed on [MHLW's results page](https://www.mhlw.go.jp/stf/newpage_75722.html), but the plan quoted the wrong wording. MHLW's item is 「日本語能力等のためにコミュニケーションが取りにくい」 (46.2%, 前年 43.9%, top challenge, published 2026-08-31). Fixed in the plan's Problem line and noted under "Facts to verify". Injury rates are still unchecked.
- Took **WS1 (AI pipeline)** and ticked my Team review row in the plan.

### Plan review: Gemini pipeline
1. **Audio input is fine.** Gemini accepts `audio/webm`, `audio/ogg`, `audio/opus`, `audio/m4a`, `audio/aac` ([audio docs](https://ai.google.dev/gemini-api/docs/audio)). Safari's MediaRecorder labels its files `audio/mp4`, which is not in Gemini's list: map it to `audio/m4a` before the call. Cost is 32 tokens per second (60 s ≈ 1,920 tokens), far under the 250K TPM limit.
2. **5 requests per minute is the tighter limit, not tokens.** The app's AI limiter (10/min per IP, BACKEND_ARCHITECTURE §9) is above Gemini's 5/min for the whole project. Make it one global limit of 4/min when `AI_MODE=gemini`.
3. **Don't retry a 429 that means the daily quota is gone.** §7.3 retries once on 429; when the error is the daily (RPD) limit, the retry only fails again. Go straight to the fallback ladder (§7.4) instead. The validation retry also costs a second request, so one bad output uses 2 of the day's 20.
4. **Glossary lock can be skipped silently.** §5.4 picks the terms to check from `transcript_ja`, which the model itself writes. If it transcribes 玉掛け as たまがけ, the term is never checked. Fix: also match the term's `reading`, and tell the prompt to write Glossary Terms in their `ja` form.
5. **Keep the response schemas simple.** Gemini supports a subset of JSON Schema; plain objects, lists, strings, ints and enums only, no unions. Our own Pydantic parse (§7.3) stays as the real check.
6. **Grounded Asks:** §5.6's subset check on `source_notice_ids` is good. Flyer and Notice text is untrusted input: the prompt must say that text inside Notices is data, never instructions.
7. **Demo quota plan:** the live demo needs about 3 real calls (flyer, Ask, Foreman note). Seed every filmed take into `ai_cache`, and don't rehearse on 3.8 Flash on demo day; rehearse with the cache. The day's count resets at 14:00 Bangladesh time.

### Answers to the plan's 4 questions
1. **Workstream:** WS1 (AI pipeline). I can also build the Ask pipeline in WS11 with whoever owns it.
2. **What makes a judge say no:**
   - a misquoted statistic (the MHLW wording, now fixed)
   - the AI failing live because of the 20/day limit (fixed by the cache and seeded results)
   - wrong Bengali in the demo. Our team reads Bengali, so one of us should check the 4 seeded Note Versions and every filmed Bengali screen by hand.
3. **What to cut without hurting the demo:** the Knowledge Library diff (WS8) and Near-miss translation (WS7). They only appear in the 2:20–3:00 quick cuts, so seeded screens are enough.
4. **Fact checked:** MHLW 46.2% (above).

### Proposal for the team (Asif to decide in the backend docs)
- **Split models by use, through `GEMINI_MODEL`:** prompt work on `gemini-3.5-flash-lite` (500/day); filmed takes and the live demo on `gemini-3.8-flash` (20/day), cached in `ai_cache` before demo day.
- 20 requests a day is the whole team's allowance on 3.8 Flash, which confirms ADR 0008: keep `AI_MODE=fake` as the default and never test against real Gemini day to day.

### Next
- Create a new Google Cloud project for Wakatta? and share the key with the team privately (never in git).
- One live test call per model: audio plus response schema, to confirm the docs.
- WS1 once Asif's WS0 lands the `AiClient` seam: prompts in `backend/app/prompts/`, validators with pytest cases, cache and fallback.

### Blockers / handoff
- Asif: please OK the Briefing receipts route ([WS6_BRIEFING_PLAN.md](WS6_BRIEFING_PLAN.md), route 5). WS0's seed should load `backend/tests/fixtures/ai/glossary.json` (or move it into `seed_data/`) so the eval and the demo use the same terms. When step 5 lands, the pipelines plug `app.ai.schemas` and `app.domain.ai_checks` into the `AiClient` seam.
- Asif: open item in §15 answered above. The model split and review points 2–4 touch your §7 and §9; accept or change them in BACKEND_ARCHITECTURE.md.
- Minhaz: the Problem wording in the plan is corrected; use the new wording in the proposal and demo caption.

### Related issues / commits / PRs
- [docs/BACKEND_ARCHITECTURE.md §15](../../docs/BACKEND_ARCHITECTURE.md#15-open-items)
