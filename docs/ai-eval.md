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

What each Chat message case checks: [ai-eval-messages.md](ai-eval-messages.md). The `draft_message` rows appear in
the table above after the next run.

## Chatbot

The chatbot (`POST /api/knowledge/asks`) has its own eval through the running API: see
[ai-eval-chatbot.md](ai-eval-chatbot.md) (`uv run python -m app.ai.eval_chatbot`).

Languages: since 2026-11-23 the demo shops are Japanese + English only (`shops.languages = {en}`); the team dropped
Bengali, so the AI writes no Bengali at all (this closes the "unchecked Bengali safety text" review point).
