# Galib's Work Log

Owner: Galib (`Ga-lib`) — Role: AI / LLM

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
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
- Asif: open item in §15 answered above. The model split and review points 2–4 touch your §7 and §9; accept or change them in BACKEND_ARCHITECTURE.md.
- Minhaz: the Problem wording in the plan is corrected; use the new wording in the proposal and demo caption.

### Related issues / commits / PRs
- [docs/BACKEND_ARCHITECTURE.md §15](../../docs/BACKEND_ARCHITECTURE.md#15-open-items)
