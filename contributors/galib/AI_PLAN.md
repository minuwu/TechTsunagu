# AI plan: every free API (proposal for review)

**Author** Galib (`Ga-lib`), AI / LLM · **Date** 2026-11-22 · **Reviewers** Asif (backend), Minhaz and Arjun (frontend)
**Status** proposal. Nothing here changes `docs/BACKEND_ARCHITECTURE.md` until Asif accepts it. Requests: **XREQ-003** (Asif), **XREQ-004** (Minhaz, Arjun) in [AGENTS.md §2](../../AGENTS.md#2-active-cross-functional-request-queue-xreq).

**Please review:** comment on the PR or in your work log, or edit this file directly. Section 9 lists the questions I need answered.

---

## 1. The decision

The team agreed to use every free AI API from my check ([WORK_LOG.md](WORK_LOG.md), 2026-11-22), so the demo never depends on one model's 20-a-day quota and costs nothing:

| API | Free limit (AI Studio / provider, 2026-11-22) | Accepts |
| :--- | :--- | :--- |
| Gemini `gemini-3.8-flash` | 20/day, 5/min | text, audio, image, JSON output |
| Gemini `gemini-3.6-flash` | 20/day, 5/min | same |
| Gemini `gemini-3.5-flash-lite` | 500/day, 15/min | same |
| Gemini `gemini-3.1-flash-lite` | 500/day, 15/min | same ([model page](https://ai.google.dev/gemini-api/docs/models/gemini-3.1-flash-lite)) |
| Groq `whisper-large-v3-turbo` | 2,000/day, 20/min, 8 h audio/day ([Groq limits](https://console.groq.com/docs/rate-limits)) | audio → text only |
| Browser speech recognition (`ja-JP`) | unlimited, on the phone | voice → text; partial support in Chrome and Safari, none in Firefox ([caniuse](https://caniuse.com/speech-recognition)) |
| Fake AI + `ai_cache` + seeded results | unlimited, offline | already in the design (§10) |

Gemini limits are per Google project, not per key, and reset at 14:00 Bangladesh time. Each model has its own daily counter (seen in AI Studio; Google's docs don't state it, so the live test in §6 confirms it).

## 2. Which model does what

Two chains, switched by one setting (`AI_CHAIN`, §4):

| Chain | Models, in order | Used for |
| :--- | :--- | :--- |
| `dev` (default with `AI_MODE=gemini`) | 3.5 Flash-Lite → 3.1 Flash-Lite | prompt work, the AI eval, anyone testing real AI |
| `demo` | 3.8 Flash → 3.6 Flash → 3.5 Flash-Lite → 3.1 Flash-Lite | filmed takes, rehearsal with cache, the live demo |

Day-to-day development stays on `AI_MODE=fake` (no API calls), as today.

| Pipeline | Input | Last resort when every Gemini model is out |
| :--- | :--- | :--- |
| `build_note` (Foreman note) | voice or text | voice: Groq Whisper transcript → a `needs_edit` draft holding the transcript, so the Foreman edits instead of retyping |
| `build_briefing` (朝礼) | voice or text | same as notes |
| `build_notice` (Poster) | text, voice or flyer photo | voice: Whisper transcript in a `needs_edit` draft; photo: empty `needs_edit` draft, the Poster types the steps |
| `answer_ask` (Worker question) | text or voice | `covered = false` and the forward-to-Senpai offer (as today); a voice Ask is forwarded with its Whisper transcript |
| `translate` (Near-miss, Senpai reply) | text or voice | voice: Whisper transcript, shown with the existing "not translated" flag |

Browser speech recognition is the frontend's fallback (XREQ-004): when the backend returns a degraded draft, or the phone is offline, the record screen offers **"Dictate instead"**, which fills the text box with what the browser heard; the text path already works with no AI.

## 3. The fallback ladder (replaces §10.4 steps 2–5)

1. **Cache.** The key includes the model, so look up the cache for each model of the chain, best first. A filmed 3.8 Flash take is reused even when 3.8 is out of quota.
2. **Chain.** Call the chain's models in order. Skip a model when its `ai_usage` row for today is `exhausted` or at its budget. A daily-quota 429 marks it `exhausted` and moves to the next model at once (no retry); a per-minute 429 or a 5xx gets the one jittered retry, then the next model.
3. **One validation retry** on the model that answered, with the problems from `app.domain.ai_checks`, if its budget allows.
4. **Seeded result** for the demo Machines and the demo flyer.
5. **Voice only: Whisper transcript** (Groq), if `GROQ_API_KEY` is set.
6. **Degraded result**, 200, never a 500 (as today).

`note_versions.ai_outcome` already records which step produced the draft (`ok`, `retried`, `fallback_seed`, `degraded`, `typed`); I propose adding `transcribed` for step 5 (one value in a check constraint).

## 4. Limits and settings (replaces the §10.3 table)

| Model | Daily budget in `ai_usage` | Per-minute bucket |
| :--- | ---: | ---: |
| 3.8 Flash, 3.6 Flash | 18 | 4 |
| 3.5 Flash-Lite, 3.1 Flash-Lite | 450 | 12 |
| Groq Whisper | 1,800 | 15 |

- Budgets stay just under each free limit, as Asif's §10.3 already does for 3.8 Flash. `ai_usage` is already keyed by `(day, model)` with an `exhausted` flag, so **no schema change** is needed for the budgets.
- Unchanged: `AI_MAX_CONCURRENT=2`, `AI_TIMEOUT_SECONDS=45`, 10 AI requests per minute per user, one API replica.
- Settings: `AI_MODE=fake|gemini` (unchanged), **`AI_CHAIN=dev|demo`** (replaces `GEMINI_MODEL` and `GEMINI_MODEL_DEV`), `GEMINI_API_KEY`, **`GROQ_API_KEY`** (optional; empty turns step 5 off). The model lists and budgets live in one table in `app/ai/`, so changing a model is one line.
- Groq needs an HTTP client at runtime. Proposal: `httpx` (the client FastAPI's test client is built on). Asif decides dependencies.

## 5. Data and privacy

- **Free Gemini:** Google uses free-tier inputs to improve its products ([pricing page](https://ai.google.dev/gemini-api/docs/pricing): "Used to improve our products: Yes"). Our demo data is fictional, so this is fine for the hackathon. **Never send real Worker data on the free tier.**
- **Groq:** says it keeps no inference data by default, stores in the US, and offers zero retention on pay-as-you-go ([Groq: your data](https://console.groq.com/docs/your-data)). I could not confirm its training terms, so the same rule applies: fictional audio only.
- Audio is never stored by us (§11, unchanged).
- **Pitch line:** "In production we use the paid tier: no training on your data, a $250 monthly cap, about $0.01 per instruction" ([pricing](https://ai.google.dev/gemini-api/docs/pricing), [billing](https://ai.google.dev/gemini-api/docs/billing)).

## 6. Quality: how we choose, with numbers

The eval set is on branch `galib/ws1-ai`: 20 Foreman notes (all 10 Work shop words, one said only as readings, one prompt injection), 4 notices (the garbage one uses only Anim's verified rules), 6 Asks (2 not covered).

1. **Smoke test (needs the Wakatta? key):** one audio + JSON call to each Gemini model. Confirms audio support and separate quotas.
2. **Eval run** (Mon, after WS0 step 5): the full set on both Flash-Lite models; 10 notes each on 3.8 and 3.6 Flash (fits their budget over two days); 5 recorded notes through Whisper, comparing shop words heard.
3. **Bengali:** a teammate scores each Bengali output 1–3 (meaning kept, shop word correct, reads naturally). No benchmark covers Japanese → Bengali, so this score decides.
4. `docs/ai-eval.md` gets one row per model: valid JSON %, glossary-lock pass %, question validity, not-covered Asks correct, Bengali score, median seconds, tokens per call.
5. If a Flash-Lite model scores as well as 3.8 Flash on a pipeline, it moves to the front of the `demo` chain for that pipeline (more quota, same quality).

## 7. Demo-day rules

1. Nobody tests on 3.8 or 3.6 Flash. They are for filming and the live demo only.
2. Each filmed take goes into the cache the moment it works; a retake then costs 0 calls.
3. One Google project used only for Wakatta?, so other apps don't eat its quota.
4. Rehearse with the cache, not live AI.

**Quota calendar** (resets at 14:00 Bangladesh time):
- Tue Nov 24 **10:00–14:00**: eval and cache filling use the previous window.
- **14:00–18:00**: filming starts on fresh quotas.
- The live demo runs on the next day's fresh quotas.

## 8. What changes, and who does it

| Who | What | Where |
| :--- | :--- | :--- |
| **Asif** | Accept or change §2–§4; update §10.3/§10.4; `AI_CHAIN` and `GROQ_API_KEY` in `.env.example`; `httpx` runtime dependency; `transcribed` in `ai_outcome`; `approvedBy` and `needsEdit` in the Note Version response (for the provenance line). Also from my WS6 plan: OK `GET /briefings/{id}/receipts`, and seed the glossary from `tests/fixtures/ai/glossary.json` | XREQ-003 |
| **Minhaz, Arjun** | "Dictate instead" (browser speech, `ja-JP`) on the Foreman record, Briefing and Poster voice screens; show `needsEdit` drafts clearly ("AI unavailable: please check or type"); a provenance line on AI drafts ("AI draft · checked against back-translation · approved by …") | XREQ-004 |
| **Galib** | The chain, budgets and cache lookup in `app/ai/` (on Asif's seam), the Whisper adapter, the eval runner and `docs/ai-eval.md`, cache seeding before filming, the smoke test | WS1 |
| **Anim** | Nothing new: `answer_ask` still goes through `ground()`; voice Asks get a transcript to forward | — |

## 9. Questions for the reviewers

1. **Asif:** one `AI_CHAIN` switch instead of `GEMINI_MODEL` / `GEMINI_MODEL_DEV`. OK?
2. **Asif:** cache lookup across the chain's models (§3 step 1). OK, or should the cache key drop the model?
3. **Asif:** `httpx` as a runtime dependency for Groq, and `transcribed` in `ai_outcome`. OK?
4. **Minhaz, Arjun:** which screens get "Dictate instead"? Browser speech support on iPhone Safari is partial, so please try it on the team's phones.
5. **Everyone:** is anyone against sending our fictional demo audio to Groq (US)?
6. **Everyone:** who reads Bengali best and can score the eval outputs on Monday?
