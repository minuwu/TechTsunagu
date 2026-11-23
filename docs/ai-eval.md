# AI eval

Run 2026-11-23 16:24 JST with `uv run python -m app.ai.eval` (Galib). Cases: `backend/tests/fixtures/ai/eval` (fictional).
Scored by the app's own checks (`app.domain.ai_checks`, `recurrence.validate_event`, `grounding.ground`)
with one retry, as `AiRunner` does, plus each case's expectations (shop words and numbers kept, Event dates,
covered or not).

| Model | Pipeline | Valid answer | First try | Passed | Median s |
| :--- | :--- | ---: | ---: | ---: | ---: |
| gemini-3.5-flash-lite | build_note | 20/20 | 20/20 | 19/20 | 2.7 |
| gemini-3.5-flash-lite | build_notice | 4/4 | 4/4 | 3/4 | 2.4 |
| gemini-3.5-flash-lite | answer_ask | 6/6 | 6/6 | 6/6 | 1.1 |
| gemini-3.1-flash-lite | build_note | 19/20 | 13/20 | 19/20 | 4.5 |
| gemini-3.1-flash-lite | build_notice | 4/4 | 2/4 | 3/4 | 4.1 |
| gemini-3.1-flash-lite | answer_ask | 6/6 | 4/6 | 6/6 | 3.5 |
| openai/gpt-oss-120b | build_note | 19/20 | 18/20 | 18/20 | 35.5 |
| openai/gpt-oss-120b | build_notice | 4/4 | 4/4 | 4/4 | 33.4 |
| openai/gpt-oss-120b | answer_ask | 6/6 | 6/6 | 6/6 | 1.8 |

## Takeaways (Galib, 2026-11-23)

- **3.5 Flash-Lite is the everyday model:** 28/30 passed, every answer valid on the first try, fastest (1–3 s).
  It stays first in the `dev` chain and right after 3.8/3.6 Flash in `demo`.
- **3.1 Flash-Lite** passes as often (28/30) but needed the retry more (19/30 first try), and gave one 503.
- **gpt-oss-120b (Groq backup)** passed 28/30, including all 4 Notices. Its 33–35 s median comes only from the
  free plan's 8,000 tokens a minute (the runner waited out 429s); on Groq's paid plan a call takes about 5 s.
- **The planted instruction (n20)** was ignored by every model. Every not-covered Ask came back not covered.
- **Prompt work for v2:** `f04-cleanup` loses 「自治会」 from the steps (2 models), and `n11-crane-reading`
  loses the number 2 (2 models). Both are fixes to make in the prompts, not in the models.
- 3.8 Flash and 3.6 Flash are not in this run: their 20 free calls a day are kept for filming.

## Failures

- `gemini-3.5-flash-lite` · build_note · `n11-crane-reading`: steps.ja lost: 2
- `gemini-3.5-flash-lite` · build_notice · `f04-cleanup`: steps.ja lost: 自治会
- `gemini-3.1-flash-lite` · build_note · `n12-crane-estop`: gemini-3.1-flash-lite: HTTP 503
- `gemini-3.1-flash-lite` · build_notice · `f04-cleanup`: steps.ja lost: 自治会
- `openai/gpt-oss-120b` · build_note · `n01-plasma`: groq: HTTP 400
- `openai/gpt-oss-120b` · build_note · `n11-crane-reading`: steps.ja lost: 2

Not scored here: how natural the Bengali and English read (a person reads `--json` output), voice input
(recorded audio cases are added separately) and flyer photos.
