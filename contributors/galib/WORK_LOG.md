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

### Proposal for the team (Asif to decide in the backend docs)
- **Split models by use, through `GEMINI_MODEL`:** prompt work on `gemini-3.5-flash-lite` (500/day); filmed takes and the live demo on `gemini-3.8-flash` (20/day), cached in `ai_cache` before demo day.
- 20 requests a day is the whole team's allowance on 3.8 Flash, which confirms ADR 0008: keep `AI_MODE=fake` as the default and never test against real Gemini day to day.

### Next
- Create a new Google Cloud project for Wakatta? and share the key with the team privately (never in git).
- One live test call per model: audio plus response schema, to confirm the docs.
- Team review row in the plan (Gemini pipeline), and take WS1 (AI pipeline).

### Blockers / handoff
- Asif: open item in §15 answered above; the model split is a proposal for you to accept or change.

### Related issues / commits / PRs
- [docs/BACKEND_ARCHITECTURE.md §15](../../docs/BACKEND_ARCHITECTURE.md#15-open-items)
