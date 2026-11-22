# Galib's Work Log

Owner: Galib (`Ga-lib`) — Role: AI / LLM

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- **WS1 integration plan, for review:** [WS1_INTEGRATION_PLAN.md](WS1_INTEGRATION_PLAN.md). Who builds which pipeline (proposal: I build `app/services/ai_pipelines.py` with `draft_briefing`, `draft_notice`, `answer_ask`, `translate`; Anim's services call them; Asif keeps `notes.py`), the pattern copied from Asif's `notes.py`, function signatures, deadlines (pipelines Mon 10:00, Briefing Mon 18:00, eval Tue 10:00–14:00), the git workflow and "done means". Linked from XREQ-003 item 7; default ownership at Mon 03:00 unless Asif or Anim objects.
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
- Researched [Rover Villa](https://rovervilla.space/heroes/phoenix) for ideas we can use: [ROVERVILLA_IDEAS.md](ROVERVILLA_IDEAS.md). My picks: machine photos with tap-the-part Check Questions (understanding checked without depending on translation), a provenance line on every AI output, and a "Veteran's voice outlives the Veteran" beat in the pitch. Proposal only; needs team agreement and an XREQ for the first one.
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
