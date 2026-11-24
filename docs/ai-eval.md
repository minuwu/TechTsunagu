# AI eval

Run 2026-11-23 17:11 JST with `uv run python -m app.ai.eval` (Galib). Cases: `backend/tests/fixtures/ai/eval` (fictional).
Scored by the app's own checks (`app.domain.ai_checks`, `recurrence.validate_event`, `grounding.ground`)
with one retry, as `AiRunner` does, plus each case's expectations (shop words and numbers kept, Event dates,
covered or not).

| Model | Pipeline | Valid answer | First try | Passed | Median s |
| :--- | :--- | ---: | ---: | ---: | ---: |
| gemini-3.5-flash-lite | build_note | 20/20 | 20/20 | 20/20 | 2.9 |
| gemini-3.5-flash-lite | build_notice | 4/4 | 4/4 | 4/4 | 2.6 |
| gemini-3.5-flash-lite | answer_ask | 6/6 | 6/6 | 6/6 | 1.3 |
| gemini-3.1-flash-lite | build_note | 20/20 | 19/20 | 20/20 | 3.8 |
| gemini-3.1-flash-lite | build_notice | 4/4 | 4/4 | 4/4 | 4.7 |
| gemini-3.1-flash-lite | answer_ask | 6/6 | 5/6 | 6/6 | 2.1 |

## Takeaways (Galib, 2026-11-23, prompts v2)

- **Both Flash-Lite models pass 30/30** with `build_note.v2` and `build_notice.v2` (v1: 28/30 each). v2 writes
  numbers as digits (二トン → 2トン) and leaves text aimed at the AI or the reader out of the steps.
- **Planted instructions** (n20, f04) were ignored by both models, and every not-covered Ask stayed not covered.
- **Watch:** in one earlier v2 run, 3.5 Flash-Lite dropped the step "±1 mm; outside it, stop and call me" (n01).
  That run is not in the table; the Foreman's review is the guard, and it is why every draft needs approval.
- **Groq gpt-oss-120b** (the backup) passed 28/30 with the v1 prompts this morning. It is not in this table:
  two eval runs used up its free daily allowance (200,000 tokens). **Never run the eval on Groq on demo day**, and
  use Groq's paid plan if the backup matters.
- **Gemini 3.8 Flash** returned 503 "high demand" at every try today (morning and afternoon), so it is not in the
  table either; the chain moves past it to the next model.

## Failures

- none

Not scored here: how natural the English reads (a person reads `--json` output), voice input
(recorded audio cases are added separately) and flyer photos.

## Chat messages

What each Chat message case checks: [ai-eval-messages.md](ai-eval-messages.md). Run 2026-11-24 ~17:00 JST with
`uv run python -m app.ai.eval --pipelines messages` (10 cases, Task extraction scored: people, due time said or not,
Machine).

| Model | Prompt | Valid answer | First try | Passed |
| :--- | :--- | ---: | ---: | ---: |
| gemini-3.5-flash-lite | draft_message v3 | 7/10 | 5/10 | 4/10 |
| gemini-3.1-flash-lite | draft_message v3 | 6/10 | 5/10 | 2/10 |
| gemini-3.5-flash-lite | **draft_message v4** | 6/10 | 3/10 | 5/10 |
| gemini-3.1-flash-lite | **draft_message v4** | **10/10** | 8/10 | **7/10** |

**What v3 got wrong, fixed in v4:** both models put the Machine **code** (`press-brake-2`) in `machine_id`, so a
message naming a Machine (m01, the die-change take; m09) failed its check twice and had no draft. v4 lists Machines
as `id | ja | en` only; m01 now gives one Task for Rahim and Sadia, Machine 2, with the due time, and 2 questions.
Also in v4: a change of plan ("tomorrow is a day off", m10) gets a question.

**Still failing on v4:**
- 3.5-flash-lite m01, m04, m05, m10: Gemini 503 / 504 (busy), not wrong answers.
- m03 (an insult meaning "clean up quickly"): both models give 0 questions. The text is right (no new sentence).
- m08 (3.1): "wear safety glasses before the plasma cutter" became a Task for every Worker although nobody is named.
- m09 (3.1): "lock out the power" became "turn off the power" in English; the Japanese keeps the meaning.

None of these sends a wrong instruction: the Foreman sees the draft before it goes out (XREQ-022).

## Chatbot

The chatbot (`POST /api/knowledge/asks`) has its own eval through the running API: see
[ai-eval-chatbot.md](ai-eval-chatbot.md) (`uv run python -m app.ai.eval_chatbot`).

Languages: since 2026-11-23 the demo shops are Japanese + English only (`shops.languages = {en}`); the team dropped
Bengali, so the AI writes no Bengali at all (this closes the "unchecked Bengali safety text" review point).
