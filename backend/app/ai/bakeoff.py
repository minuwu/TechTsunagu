"""XREQ-007 bake-off: the same clips and sentences through every candidate, one table per task.

    cd backend; uv run python -m app.ai.bakeoff --clips <folder of <id>.wav>      # transcription + translation

Clip texts: tests/fixtures/ai/bakeoff/clips.json (fictional). Keys: GEMINI_API_KEY / GROQ_API_KEY (environment or
backend/.env). Writes docs/ai-bakeoff.md and, with --sheet, a CSV for a Japanese reader to score the translations.

Normalisation (written down once, as XREQ-007 asks): NFKC; lower case; every space and punctuation mark removed;
single kanji digits 〇一二…九 become 0–9 in both reference and hypothesis. Japanese is scored by character error
rate (CER), English by word error rate (WER)."""

import argparse
import csv
import json
import statistics
import sys
import threading
import time
import unicodedata
from collections.abc import Callable, Sequence
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import httpx2
from google import genai
from google.genai import types

from app.ai.client import Part
from app.ai.eval import BACKEND, _env  # pyright: ignore[reportPrivateUsage]  # same key lookup as the eval
from app.ai.gemini import GeminiClient
from app.ai.prompts import render
from app.ai.schemas import Translation
from app.domain.ai_checks import GlossaryTerm, terms_in_scope

FIXTURE = BACKEND / "tests" / "fixtures" / "ai" / "bakeoff" / "clips.json"
REPORT = BACKEND.parent / "docs" / "ai-bakeoff.md"
_KANJI_DIGITS = str.maketrans("〇一二三四五六七八九", "0123456789")
Json = dict[str, Any]
_Item = tuple[str, str, list[str]]  # (case id, text, numbers that must survive)


def normalise(text: str) -> str:
    text = unicodedata.normalize("NFKC", text).lower().translate(_KANJI_DIGITS)
    return "".join(ch for ch in text if not ch.isspace() and not unicodedata.category(ch).startswith("P"))


def _distance(a: Sequence[str], b: Sequence[str]) -> int:
    prev = list(range(len(b) + 1))
    for i, x in enumerate(a, start=1):
        cur = [i]
        for j, y in enumerate(b, start=1):
            cur.append(min(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + (x != y)))
        prev = cur
    return prev[-1]


def cer(reference: str, hypothesis: str) -> float:
    ref, hyp = normalise(reference), normalise(hypothesis)
    return _distance(ref, hyp) / max(len(ref), 1)


def wer(reference: str, hypothesis: str) -> float:
    def words(t: str) -> list[str]:
        cleaned = "".join(" " if unicodedata.category(ch).startswith("P") else ch for ch in t)
        return unicodedata.normalize("NFKC", cleaned).lower().split()

    ref = words(reference)
    return _distance(ref, words(hypothesis)) / max(len(ref), 1)


@dataclass
class Result:
    candidate: str
    clip: str
    lang: str
    text: str = ""
    error: str | None = None
    seconds: float = 0.0
    score: float = 0.0  # CER or WER
    terms: tuple[int, int] = (0, 0)
    numbers: tuple[int, int] = (0, 0)
    notes: list[str] = field(default_factory=list[str])


# --- transcription candidates: (clip path, language "ja" | "en") -> text --------------------------------------


def _groq(model: str) -> Callable[[Path, str], str]:
    key = _env("GROQ_API_KEY") or ""

    def run(path: Path, lang: str) -> str:
        files = {"file": (path.name, path.read_bytes(), "audio/wav")}
        data = {"model": model, "language": lang, "response_format": "text"}
        r = httpx2.post(
            "https://api.groq.com/openai/v1/audio/transcriptions",
            headers={"Authorization": f"Bearer {key}"},
            files=files,
            data=data,
            timeout=60,
        )
        if r.status_code >= 400:
            raise RuntimeError(f"HTTP {r.status_code}: {r.text[:120]}")
        return r.text.strip()

    return run


def _transcribe_model(vocabulary: list[str] | None) -> Callable[[Path, str], str]:
    # A hard timeout: without one, a stalled upload or interaction hangs the whole run (seen 2026-11-23).
    client: Any = genai.Client(
        api_key=_env("GEMINI_API_KEY"), http_options={"api_version": "v1beta", "timeout": 60_000}
    )

    def run(path: Path, lang: str) -> str:
        f = client.files.upload(file=str(path))
        config: Json = {"language_codes": ["ja-JP" if lang == "ja" else "en-US"]}
        if vocabulary and lang == "ja":
            config["custom_vocabulary"] = vocabulary
        try:
            r = client.interactions.create(
                model="gemini-3.5-transcribe",
                input=[{"type": "audio", "uri": f.uri, "mime_type": f.mime_type}],
                generation_config={"transcription_config": config},
            )
            return str(r.output_text or "")
        finally:
            client.files.delete(name=f.name)

    return run


def _chat_model(model: str) -> Callable[[Path, str], str]:
    client: Any = genai.Client(api_key=_env("GEMINI_API_KEY"), http_options={"timeout": 60_000})
    ask = "Transcribe this recording exactly, word for word, in the language spoken. Return only the transcript."

    def run(path: Path, lang: str) -> str:
        audio = types.Part.from_bytes(data=path.read_bytes(), mime_type="audio/wav")
        r = client.models.generate_content(model=model, contents=[ask, audio])
        return str(r.text or "").strip()

    return run


def _within(seconds: float, fn: Callable[[], str]) -> str:
    """Run fn, but give up after `seconds`: the SDK's own timeout did not stop a stalled transcribe call."""
    box: dict[str, str | BaseException] = {}

    def target() -> None:
        try:
            box["value"] = fn()
        except BaseException as exc:  # handed back to the caller below
            box["error"] = exc

    t = threading.Thread(target=target, daemon=True)
    t.start()
    t.join(seconds)
    if t.is_alive():
        raise TimeoutError(f"no answer in {seconds:.0f} s")
    error = box.get("error")
    if isinstance(error, BaseException):
        raise error
    return str(box["value"])


def _transcription(clips_dir: Path, data: Json, terms: list[str], candidates: Sequence[str]) -> list[Result]:
    makers: dict[str, Callable[[], Callable[[Path, str], str]]] = {
        "gemini-3.5-transcribe": lambda: _transcribe_model(None),
        "gemini-3.5-transcribe + shop words": lambda: _transcribe_model(terms),
        "gemini-3.5-flash-lite (audio in)": lambda: _chat_model("gemini-3.5-flash-lite"),
        "groq whisper-large-v3": lambda: _groq("whisper-large-v3"),
        "groq whisper-large-v3-turbo": lambda: _groq("whisper-large-v3-turbo"),
    }
    clips = [(c, "ja", c["id"]) for c in data["ja"]]
    clips += [(c, "ja", f"{cid}_noisy") for cid in data["noisy"] for c in data["ja"] if c["id"] == cid]
    clips += [(c, "en", c["id"]) for c in data["en"]]
    results: list[Result] = []
    for name in candidates:
        run = makers[name]()
        timeouts = 0
        for clip, lang, file_id in clips:
            row = Result(name, file_id, lang)
            if timeouts >= 3:  # a model that stalls three times in a row is out; its other clips count as failed
                row.error = "skipped: 3 timeouts in a row"
                results.append(row)
                continue
            start = time.monotonic()
            path = clips_dir / f"{file_id}.wav"
            for attempt in range(3):
                try:
                    row.text, row.error = _within(90, lambda path=path, lang=lang, run=run: run(path, lang)), None
                    timeouts = 0
                    break
                except TimeoutError as exc:
                    row.error, timeouts = str(exc), timeouts + 1
                    break
                except Exception as exc:  # 429 waits; anything else is a counted failure
                    row.error = f"{type(exc).__name__}: {str(exc)[:120]}"
                    if "429" not in str(exc) or attempt == 2:
                        break
                    time.sleep(20)
            row.seconds = round(time.monotonic() - start, 1)
            if row.error is None and not row.text:
                row.error = "empty"
            if row.error is None:
                ref = clip["text"]
                row.score = cer(ref, row.text) if lang == "ja" else wer(ref, row.text)
                hyp = normalise(row.text)
                want_terms = clip.get("terms", [])
                row.terms = (sum(t in row.text for t in want_terms), len(want_terms))
                nums = clip.get("numbers", [])
                row.numbers = (sum(n in hyp for n in nums), len(nums))
            results.append(row)
            sys.stdout.write(f"  {name} · {file_id}: {row.error or round(row.score, 2)} ({row.seconds} s)\n")
            sys.stdout.flush()
        sys.stdout.write(f"transcribe · {name}: done\n")
    return results


# --- translation: ja → en and en → ja with the app's translate prompt -------------------------------------------


def _translation(data: Json, glossary: list[GlossaryTerm], models: Sequence[str]) -> list[Result]:
    block = json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in glossary], ensure_ascii=False)
    eval_dir = BACKEND / "tests" / "fixtures" / "ai" / "eval"
    notices = json.loads((eval_dir / "notices.json").read_text(encoding="utf-8"))["cases"]
    ja: list[_Item] = [(c["id"], c["text"], c.get("numbers", [])) for c in data["ja"]]
    ja += [(n["id"], n["source_ja"], n["expect"]["keep"]) for n in notices]
    en: list[_Item] = [(c["id"], c["text"], []) for c in data["en"]]
    results: list[Result] = []
    for model in models:
        client = GeminiClient(_env("GEMINI_API_KEY") or "", model, 60)
        for source, target, items in (("Japanese", "English", ja), ("English", "Japanese", en)):
            prompt = render("translate", 1, source_lang=source, target_lang=target, glossary=block)
            for cid, text, nums in items:
                row = Result(model, cid, f"{source[:2].lower()}→{target[:2].lower()}")
                start = time.monotonic()
                for attempt in range(4):
                    try:
                        raw = client.generate(prompt, [Part.text(f"<source_text>{text}</source_text>")], Translation)
                        row.text, row.error = Translation.model_validate_json(raw).text, None
                        break
                    except Exception as exc:  # a per-minute 429 or a 503 waits and tries again
                        row.error = f"{type(exc).__name__}: {str(exc)[:120]}"
                        if not any(code in str(exc) for code in ("429", "503")) or attempt == 3:
                            break
                        time.sleep(20)
                row.seconds = round(time.monotonic() - start, 1)
                if row.error is None and target == "English":
                    in_scope = terms_in_scope(text, glossary)
                    kept = [t for t in in_scope if t.translations["en"].casefold() in row.text.casefold()]
                    row.terms = (len(kept), len(in_scope))
                    row.notes = [f"lost 「{t.ja}」→{t.translations['en']}" for t in in_scope if t not in kept]
                if row.error is None:
                    hyp = normalise(row.text)
                    row.numbers = (sum(n in hyp for n in nums), len(nums))
                results.append(row)
        sys.stdout.write(f"translate · {model}: done\n")
    return results


# --- report ------------------------------------------------------------------------------------------------------


def _frac(pairs: Sequence[tuple[int, int]]) -> str:
    got, total = sum(p[0] for p in pairs), sum(p[1] for p in pairs)
    return f"{got}/{total}" if total else "—"


def _median(values: Sequence[float]) -> str:
    return f"{statistics.median(values):.1f}" if values else "—"


def report(transcripts: Sequence[Result], translations: Sequence[Result], when: str) -> str:
    lines = [
        "# XREQ-007 bake-off",
        "",
        f"Run {when} with `uv run python -m app.ai.bakeoff` (Galib). Clip texts:",
        "`backend/tests/fixtures/ai/bakeoff/clips.json` (fictional). **Clips are Windows TTS voices** (Haruka ja-JP,",
        "Zira en-US) plus 3 noisy copies (about 10 dB SNR and a 120 Hz hum), until real recordings exist: clean, read",
        "speech, so error rates here are lower than on a Foreman's real voice. The order between models is the result.",
        "",
        "Normalisation: NFKC, lower case, spaces and punctuation removed, single kanji digits 〇–九 → 0–9.",
        "",
        "## Transcription",
        "",
        "| Candidate | ja CER (clean) | ja CER (noisy) | en WER | Shop words | Numbers | Median s | Failures |",
        "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |",
    ]
    for name in dict.fromkeys(r.candidate for r in transcripts):
        rs = [r for r in transcripts if r.candidate == name]
        ok = [r for r in rs if r.error is None]

        def mean(sel: Callable[[Result], bool], ok: list[Result] = ok) -> str:
            vals = [r.score for r in ok if sel(r)]
            return f"{statistics.mean(vals) * 100:.1f}%" if vals else "—"

        lines.append(
            f"| {name} | {mean(lambda r: r.lang == 'ja' and not r.clip.endswith('_noisy'))} "
            f"| {mean(lambda r: r.clip.endswith('_noisy'))} | {mean(lambda r: r.lang == 'en')} "
            f"| {_frac([r.terms for r in ok])} | {_frac([r.numbers for r in ok])} "
            f"| {_median([r.seconds for r in ok])} | {len(rs) - len(ok)} |"
        )
    lines += [
        "",
        "## Translation (the app's `translate` prompt, v1)",
        "",
        "| Model | Direction | Valid | Shop words kept | Numbers kept | Median s |",
        "| :--- | :--- | ---: | ---: | ---: | ---: |",
    ]
    for model in dict.fromkeys(r.candidate for r in translations):
        for direction in ("ja→en", "en→ja"):
            rs = [r for r in translations if r.candidate == model and r.lang == direction]
            ok = [r for r in rs if r.error is None]
            lines.append(
                f"| {model} | {direction} | {len(ok)}/{len(rs)} | {_frac([r.terms for r in ok])} "
                f"| {_frac([r.numbers for r in ok])} | {_median([r.seconds for r in ok])} |"
            )
    misses = [r for r in [*transcripts, *translations] if r.error or r.notes]
    lines += ["", "## Failures and lost shop words", ""]
    lines += [f"- `{r.candidate}` · `{r.clip}`: {r.error or '; '.join(r.notes)}" for r in misses] or ["- none"]
    lines += [
        "",
        "Not scored here: naturalness and politeness of the translations (a Japanese reader fills the `--sheet` CSV,",
        "1–3 per sentence), real voices and accents, and 50-call robustness runs.",
        "",
    ]
    return "\n".join(lines)


def main(argv: Sequence[str] | None = None) -> None:
    parser = argparse.ArgumentParser(description="XREQ-007 bake-off: transcription and translation candidates.")
    parser.add_argument("--clips", type=Path, required=True, help="folder with <clip id>.wav files")
    parser.add_argument("--transcribe", nargs="*", default=None, help="candidates (default: all)")
    parser.add_argument("--translate", nargs="*", default=["gemini-3.5-flash-lite", "gemini-3.1-flash-lite"])
    parser.add_argument("--sheet", type=Path, help="write the translations as CSV for a Japanese reader")
    parser.add_argument("--json", type=Path, help="write every result")
    args = parser.parse_args(argv)
    data: Json = json.loads(FIXTURE.read_text(encoding="utf-8"))
    raw: list[Json] = json.loads((BACKEND / "app" / "seed_data" / "glossary.json").read_text(encoding="utf-8"))["terms"]
    glossary = [GlossaryTerm(t["id"], t["ja"], t["reading"], t["translations"]) for t in raw]
    candidates = (
        args.transcribe
        if args.transcribe is not None
        else [  # fast ones first, so a stalling model cannot hold back the others' results
            "groq whisper-large-v3-turbo",
            "groq whisper-large-v3",
            "gemini-3.5-flash-lite (audio in)",
            "gemini-3.5-transcribe",
            "gemini-3.5-transcribe + shop words",
        ]
    )
    transcripts = _transcription(args.clips, data, [t.ja for t in glossary], candidates)
    translations = _translation(data, glossary, args.translate)
    when = time.strftime("%Y-%m-%d %H:%M")
    REPORT.write_bytes(report(transcripts, translations, when).encode("utf-8"))
    if args.sheet:
        with args.sheet.open("w", newline="", encoding="utf-8-sig") as f:
            w = csv.writer(f)
            w.writerow(
                ["model", "id", "direction", "translation", "meaning kept 1-3", "polite register 1-3", "natural 1-3"]
            )
            w.writerows([r.candidate, r.clip, r.lang, r.text, "", "", ""] for r in translations if r.error is None)
    if args.json:
        args.json.write_bytes(
            json.dumps([r.__dict__ for r in [*transcripts, *translations]], ensure_ascii=False).encode()
        )
    sys.stdout.write(f"wrote {REPORT}\n")


if __name__ == "__main__":
    main()
