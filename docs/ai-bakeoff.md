# XREQ-007 bake-off

Run 2026-11-23 17:46 with `uv run python -m app.ai.bakeoff` (Galib). Clip texts:
`backend/tests/fixtures/ai/bakeoff/clips.json` (fictional). **Clips are Windows TTS voices** (Haruka ja-JP,
Zira en-US) plus 3 noisy copies (about 10 dB SNR and a 120 Hz hum), until real recordings exist: clean, read
speech, so error rates here are lower than on a Foreman's real voice. The order between models is the result.

Normalisation: NFKC, lower case, spaces and punctuation removed, single kanji digits 〇–九 → 0–9.

## Transcription

| Candidate | ja CER (clean) | ja CER (noisy) | en WER | Shop words | Numbers | Median s | Failures |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| groq whisper-large-v3-turbo | 5.6% | 23.1% | 1.4% | 22/29 | 19/20 | 0.4 | 0 |
| groq whisper-large-v3 | 8.1% | 19.9% | 1.4% | 22/29 | 19/20 | 0.4 | 0 |
| gemini-3.5-flash-lite (audio in) | 9.3% | 36.4% | 3.1% | 18/29 | 17/20 | 4.8 | 0 |

## Translation (the app's `translate` prompt, v1)

| Model | Direction | Valid | Shop words kept | Numbers kept | Median s |
| :--- | :--- | ---: | ---: | ---: | ---: |
| gemini-3.5-flash-lite | ja→en | 22/22 | 22/27 | 15/19 | 1.1 |
| gemini-3.5-flash-lite | en→ja | 10/10 | — | — | 1.0 |
| gemini-3.1-flash-lite | ja→en | 21/22 | 26/26 | 15/19 | 10.6 |
| gemini-3.1-flash-lite | en→ja | 10/10 | — | — | 2.0 |

## Choice (Galib, 2026-11-23)

| Task | Primary | Fallback | Why |
| :--- | :--- | :--- | :--- |
| **Transcribe** (ja Foreman, en Worker) | **Groq `whisper-large-v3-turbo`** | Groq `whisper-large-v3`, then Gemini audio in | Lowest error on clean Japanese (5.6% vs 9.3%) and English (1.4% vs 3.1%), most numbers right, 0.4 s. `large-v3` is better on noisy clips (19.9% vs 23.1%). |
| **Translate** ja → en | **gemini-3.1-flash-lite** | gemini-3.5-flash-lite | 3.1 kept every shop word (26/26); 3.5 lost 5 of 27 (寸法, 段取り, バリ) with the `translate` prompt, which has no glossary check today. |
| **Translate** en → ja | gemini-3.5-flash-lite | gemini-3.1-flash-lite | Both 10/10 valid; 3.5 is faster. Register and naturalness wait for the Japanese reader's sheet. |
| **Extract** (steps, Check Questions, Tasks) | gemini-3.5-flash-lite | gemini-3.1-flash-lite | The eval (`docs/ai-eval.md`): both 30/30 with the v2 prompts. |

- **Whisper does not keep shop words in their ja form** (22/29 for every candidate: it writes kana or other kanji). That
  is fine when its transcript goes into the Gemini text path, which writes Glossary Terms in their ja form and is
  checked by the glossary lock. So the voice path to build is: **Whisper transcript → Gemini text path** (XREQ-007
  item 3: transcription becomes its own step), instead of sending audio to Gemini directly.
- **`gemini-3.5-transcribe` did not answer on the free tier**: one call worked at 16:30 JST (4.8 s), then every call
  stalled (no answer in 90 s, with and without custom vocabulary), so it has no row. Its custom vocabulary is the
  feature we want for shop words; retry it on the paid tier.
- **`translate` should get the glossary check** the other pipelines have (`glossary_problems`), so a lost shop word
  triggers the retry. A small change in `asks.translate` (Anim).
- **Limits:** TTS clips are clean read speech, so real Foremen will be worse for every model; the order between models
  is the result. Japanese quality scores need a Japanese reader: `docs/ai-bakeoff-sheet.csv` (1–3 per sentence).

## Failures and lost shop words

- `gemini-3.5-flash-lite` · `n01-plasma`: lost 「バリ」→burr (bari)
- `gemini-3.5-flash-lite` · `n05-press-setup`: lost 「寸法」→dimensions (sunpō); lost 「段取り」→set-up (dandori)
- `gemini-3.5-flash-lite` · `n17-weld-burr`: lost 「バリ」→burr (bari)
- `gemini-3.5-flash-lite` · `n19-numbers`: lost 「寸法」→dimensions (sunpō)
- `gemini-3.1-flash-lite` · `n14-weld-vent`: AiUnavailable: gemini-3.1-flash-lite: HTTP 504

Not scored here: naturalness and politeness of the translations (a Japanese reader fills the `--sheet` CSV,
1–3 per sentence), real voices and accents, and 50-call robustness runs.
