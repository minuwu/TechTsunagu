"""Chatbot eval (RAG_PLAN.md §7): every case in tests/fixtures/ai/eval/knowledge.json through the running API.

    cd backend; uv run python -m app.ai.eval_chatbot --base-url http://127.0.0.1:8000     # demo seed, real AI

Goes through the real route (search, the AI chain, ground(), the emergency rule), so it scores what a Worker gets.
Run it on a fresh ai_cache, or the answers replay from the cache. About 20 free AI calls; never on demo day.
Writes docs/ai-eval-chatbot.md."""

import argparse
import json
import sys
import time
from collections.abc import Sequence
from datetime import datetime
from pathlib import Path
from typing import Any

from app.ai.demo_warmup import Api
from app.domain.recurrence import TOKYO

BACKEND = Path(__file__).resolve().parents[2]
CASES = BACKEND / "tests" / "fixtures" / "ai" / "eval" / "knowledge.json"
REPORT = BACKEND.parent / "docs" / "ai-eval-chatbot.md"
Json = dict[str, Any]


def grade(case: Json, data: Json) -> list[str]:
    """What is wrong with one answer; empty means it passed."""
    got = "emergency" if data["emergency"] else "covered" if data["covered"] else "not_covered"
    if got != case["expect"]:
        return [f"{got} (want {case['expect']})"]
    if got != "covered":
        return []
    wrong: list[str] = []
    cited = [s["id"] for s in data["sources"]]
    if not any(c.startswith(tuple(case["expect_sources"])) for c in cited):
        wrong.append(f"cited {cited} (want one of {case['expect_sources']})")
    if data["check"] is None:
        wrong.append("no self-check question")
    if any(s["kind"] in ("safety", "life") and not s["attribution"] for s in data["sources"]):
        wrong.append("a public guide without its attribution")
    return wrong


def report(rows: list[tuple[Json, list[str], float, Json]], when: str) -> str:
    passed = sum(not w for _, w, _, _ in rows)
    lines = [
        "# Chatbot eval",
        "",
        f"Run {when} with `uv run python -m app.ai.eval_chatbot` (Galib) against the demo seed,",
        "through `POST /api/knowledge/asks` with real AI and an empty cache.",
        "Cases: `backend/tests/fixtures/ai/eval/knowledge.json`. Passed means: covered / not covered / emergency as",
        "expected, and a covered answer cites the expected source, has a self-check question and carries the guide's",
        "attribution.",
        "",
        f"**{passed}/{len(rows)} passed.**",
        "",
        "| Case | Lang | Question | Expected | Got | Cited | s | Result |",
        "| :--- | :--- | :--- | :--- | :--- | :--- | ---: | :--- |",
    ]
    for case, wrong, secs, data in rows:
        got = "emergency" if data["emergency"] else "covered" if data["covered"] else "not covered"
        cited = ", ".join(s["id"] for s in data["sources"]) or "—"
        result = "pass" if not wrong else "FAIL: " + "; ".join(wrong)
        lines.append(
            f"| {case['id']} | {case['lang']} | {case['question']} | {case['expect'].replace('_', ' ')} | {got} "
            f"| {cited} | {secs:.1f} | {result} |"
        )
    by_lang: dict[str, str] = {}
    for lang in ("en", "ja"):
        mine = [w for c, w, _, _ in rows if c["lang"] == lang]
        by_lang[lang] = f"{sum(not w for w in mine)}/{len(mine)}"
    lines += ["", f"By question language: en {by_lang['en']}, ja {by_lang['ja']}.", ""]
    return "\n".join(lines)


def main(argv: Sequence[str] | None = None) -> None:
    p = argparse.ArgumentParser(description="Chatbot eval through the running API.")
    p.add_argument("--base-url", default="http://127.0.0.1:8000")
    p.add_argument("--worker", default="Rahim Uddin")
    p.add_argument("--worker-pin", default="2468")
    args = p.parse_args(argv)
    api = Api(args.base_url)
    worker = api.login("SASEBO01", "worker", args.worker_pin, args.worker)
    machines = {m["code"]: m["id"] for m in api.get("/api/machines", worker)["items"]}
    rows: list[tuple[Json, list[str], float, Json]] = []
    for case in json.loads(CASES.read_text(encoding="utf-8"))["cases"]:
        form = {"text": case["question"]} | ({"machineId": machines[case["machine"]]} if "machine" in case else {})
        start = time.monotonic()
        data, _ = api.post_form("/api/knowledge/asks", worker, form)
        wrong = grade(case, data)
        rows.append((case, wrong, time.monotonic() - start, data))
        sys.stdout.write(f"{case['id']} {'pass' if not wrong else 'FAIL ' + '; '.join(wrong)}\n")
    when = datetime.now(TOKYO).strftime("%Y-%m-%d %H:%M JST")
    REPORT.write_bytes(report(rows, when).encode("utf-8"))
    sys.stdout.write(f"wrote {REPORT}\n")


if __name__ == "__main__":
    main()
