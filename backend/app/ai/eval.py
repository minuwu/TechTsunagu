"""AI eval (architecture §10.6, AI_PLAN.md §6): every eval case through each model, scored by the app's own checks.

    cd backend; uv run python -m app.ai.eval                       # the free everyday models (~100 calls)
    uv run python -m app.ai.eval --models gemini-3.8-flash         # one model; 3.8/3.6 have 20 free calls a day

Keys come from GEMINI_API_KEY / GROQ_API_KEY (environment or backend/.env). Cases are fictional
(tests/fixtures/ai/eval). Writes docs/ai-eval.md and, with --json, every answer for review."""

import argparse
import json
import os
import sys
import time
from collections.abc import Callable, Sequence
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import Any, get_args

from pydantic import BaseModel, ValidationError

from app.ai.chains import GROQ_MODEL
from app.ai.client import AiClient, AiUnavailable, Part
from app.ai.gemini import GeminiClient
from app.ai.groq import GroqClient
from app.ai.prompts import render
from app.ai.schemas import AskDraft, IconId, MessageDraft, NoteDraft, NoticeDraft
from app.domain.ai_checks import (
    GlossaryTerm,
    language_problems,
    message_problems,
    note_problems,
    question_problems,
    question_texts,
)
from app.domain.grounding import ground
from app.domain.recurrence import TOKYO, validate_event

BACKEND = Path(__file__).resolve().parents[2]
CASES = BACKEND / "tests" / "fixtures" / "ai" / "eval"
REPORT = BACKEND.parent / "docs" / "ai-eval.md"
LANGUAGES = ("en",)  # the demo shops' languages (shops.languages); --languages changes them
DEFAULT_MODELS = ("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", GROQ_MODEL)

Json = dict[str, Any]


@dataclass
class Row:
    pipeline: str
    case: str
    model: str
    valid: bool = False
    first_try: bool = False
    passed: bool = False  # the app's checks and the case's expectations
    seconds: float = 0.0
    notes: list[str] = field(default_factory=list[str])
    answer: Json | None = None


def _env(name: str) -> str | None:
    if os.environ.get(name):
        return os.environ[name]
    env = BACKEND / ".env"
    lines = env.read_text(encoding="utf-8").splitlines() if env.exists() else []
    values = dict(line.split("=", 1) for line in lines if "=" in line and not line.startswith("#"))
    return values.get(name, "").strip() or None


def _client(model: str) -> AiClient:
    key = _env("GROQ_API_KEY" if model == GROQ_MODEL else "GEMINI_API_KEY")
    if key is None:
        sys.exit(f"no API key for {model}")
    return GroqClient(key, model, 60) if model == GROQ_MODEL else GeminiClient(key, model, 60)


def _ask[T: BaseModel](
    client: AiClient, prompt: Any, parts: list[Part], schema: type[T], validate: Callable[[T], list[str]], row: Row
) -> T | None:
    """One call and one retry with the problems, as AiRunner does. A rate limit waits and tries again."""
    extra: list[Part] = []
    start = time.monotonic()
    for attempt in range(2):
        value, problems = None, []
        for _ in range(4):
            try:
                raw = client.generate(prompt, [*parts, *extra], schema, LANGUAGES)
                value = schema.model_validate_json(raw)
                problems = validate(value)
                break
            except AiUnavailable as exc:
                if "429" not in str(exc):
                    problems = [str(exc)]
                    break
                time.sleep(30)  # free tier: tokens or requests per minute
            except ValidationError as exc:
                problems = [f"{'.'.join(map(str, e['loc']))}: {e['msg']}" for e in exc.errors()[:5]]
                break
        if value is not None and not problems:
            row.valid, row.first_try = True, attempt == 0
            row.seconds = round(time.monotonic() - start, 1)
            return value
        row.notes = problems[:3]
        extra = [
            Part.text("Your previous answer had these problems; fix them and answer again:\n- " + "\n- ".join(problems))
        ]
    row.seconds = round(time.monotonic() - start, 1)
    return None


def _load(name: str) -> Json:
    data: Json = json.loads((CASES / name).read_text(encoding="utf-8"))
    return data


def _glossary() -> list[GlossaryTerm]:
    raw: list[Json] = json.loads((BACKEND / "app" / "seed_data" / "glossary.json").read_text(encoding="utf-8"))["terms"]
    return [GlossaryTerm(t["id"], t["ja"], t["reading"], t["translations"]) for t in raw]


def eval_notes(client: AiClient, model: str) -> list[Row]:
    terms = _glossary()
    glossary = json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in terms], ensure_ascii=False)
    prompt = render(
        "build_note", 3, glossary=glossary, icons=", ".join(get_args(IconId)), languages=", ".join(LANGUAGES)
    )
    rows: list[Row] = []
    for case in _load("notes.json")["cases"]:
        row = Row("build_note", case["id"], model)
        parts = [Part.text(f"<foreman_input>{case['input_ja']}</foreman_input>")]
        draft = _ask(client, prompt, parts, NoteDraft, lambda d: note_problems(d, terms, LANGUAGES), row)
        if draft is not None:
            ja = "\n".join(s.text.ja for s in draft.steps)
            missing = [t for t in case["expect_terms"] if t not in ja] + [k for k in case["keep"] if k not in ja]
            wrong = [f"steps.ja lost: {', '.join(missing)}"] if missing else []
            wrong += [f"followed injected text: {f}" for f in case.get("forbid", []) if f in ja]
            row.notes, row.passed, row.answer = wrong, not wrong, json.loads(draft.model_dump_json())
        rows.append(row)
    return rows


def _event_problems(d: NoticeDraft, today: datetime) -> list[str]:
    # Notices have no Glossary Terms (district, not shop), so only the options are checked, as in the app.
    problems = [p for p in question_problems([d.question], []) if "Glossary Term" not in p]
    problems += language_problems(
        [d.title, *d.steps, *(e.title for e in d.events), *question_texts([d.question])], LANGUAGES
    )
    for e in d.events:
        try:
            validate_event(datetime.fromisoformat(e.starts_at), e.rrule, today)
        except ValueError as exc:
            problems.append(f"event {e.starts_at}: {exc}")
    return problems


def eval_notices(client: AiClient, model: str) -> list[Row]:
    data = _load("notices.json")
    today = datetime.fromisoformat(data["today"]).replace(tzinfo=TOKYO)
    prompt = render("build_notice", 3, glossary="[]", today=data["today"], languages=", ".join(LANGUAGES))
    rows: list[Row] = []
    for case in data["cases"]:
        row = Row("build_notice", case["id"], model)
        parts = [Part.text(f"<notice_source>{case['source_ja']}</notice_source>")]
        draft = _ask(client, prompt, parts, NoticeDraft, lambda d: _event_problems(d, today), row)
        if draft is not None:
            want = case["expect"]
            got = [(datetime.fromisoformat(e.starts_at), e.rrule) for e in draft.events]
            expected = [(datetime.fromisoformat(e["starts_at"]), e["rrule"]) for e in want["events"]]
            ja = "\n".join(s.ja for s in draft.steps)
            wrong = (
                [f"category {draft.category} (want {want['category']})"] if draft.category != want["category"] else []
            )
            wrong += (
                [f"events {[str(g[0]) for g in got]} (want {[str(e[0]) for e in expected]})"] if got != expected else []
            )
            wrong += [f"steps.ja lost: {k}" for k in [*want["terms"], *want["keep"]] if k not in ja]
            wrong += [f"followed injected text: {f}" for f in want.get("forbid", []) if f in ja]
            row.notes, row.passed, row.answer = wrong, not wrong, json.loads(draft.model_dump_json())
        rows.append(row)
    return rows


def eval_asks(client: AiClient, model: str) -> list[Row]:
    notices = _load("notices.json")["cases"]
    block = "\n".join(
        json.dumps({"id": n["id"], "title": "", "text": n["source_ja"], "steps": []}, ensure_ascii=False)
        for n in notices
    )
    sent = [n["id"] for n in notices]
    prompt = render("answer_ask", 3, notices=block, glossary="[]", languages=", ".join(LANGUAGES))
    rows: list[Row] = []
    for case in _load("asks.json")["cases"]:
        row = Row("answer_ask", case["id"], model)
        parts = [Part.text(f"<worker_question>{case['question']}</worker_question>")]
        cited_known: Callable[[AskDraft], list[str]] = lambda d: (  # noqa: E731
            [] if set(d.source_notice_ids) <= set(sent) else ["unknown id"]
        )
        draft = _ask(client, prompt, parts, AskDraft, cited_known, row)
        answer = draft.answer.only(LANGUAGES) if draft and draft.answer else None
        g = ground(draft.covered if draft else False, answer, draft.source_notice_ids if draft else [], sent, LANGUAGES)
        wrong = [f"covered={g.covered} (want {case['expect_covered']})"] if g.covered != case["expect_covered"] else []
        missing = set(case["expect_sources"]) - set(g.source_notice_ids)
        wrong += [f"did not cite {sorted(missing)}"] if g.covered and missing else []
        row.notes = wrong or row.notes
        row.passed = draft is not None and not wrong
        row.answer = json.loads(draft.model_dump_json()) if draft else None
        rows.append(row)
    return rows


# The demo shop SASEBO01 as draft_message sees it (app/seed_data/work.json, in seed order).
MEMBERS = {"3": "Rahim Uddin", "4": "Sadia Akter"}


def _shop_machines() -> list[Json]:
    work: Json = json.loads((BACKEND / "app" / "seed_data" / "work.json").read_text(encoding="utf-8"))
    return [m for m in work["machines"] if m["shop"] == "s-sasebo"]


def _task_problems(want: list[Json], got: MessageDraft) -> list[str]:
    """Task extraction (XREQ-006 item 5): the same Tasks with the same people, due time said or not, and Machine."""
    have = sorted((sorted(t.assignee_ids), t.due_at is not None, t.machine_id) for t in got.tasks)
    need = sorted((sorted(w["assignees"]), w["due"], w["machine"]) for w in want)
    return [] if have == need else [f"tasks {have} (want {need})"]


def eval_messages(client: AiClient, model: str) -> list[Row]:
    terms = _glossary()
    glossary = json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in terms], ensure_ascii=False)
    machines = _shop_machines()
    machine_ids = {str(i) for i in range(1, len(machines) + 1)}
    names = [(m["name"]["ja"], m["name"]["en"]) for m in machines]
    prompt = render(
        "draft_message",
        4,
        glossary=glossary,
        languages=", ".join(LANGUAGES),
        members="\n".join(f"{i} | {n}" for i, n in MEMBERS.items()),
        machines="\n".join(f"{i} | {m['name']['ja']} | {m['name']['en']}" for i, m in enumerate(machines, start=1)),
        now="2026-11-24T13:00+09:00",
    )
    rows: list[Row] = []
    for case in _load("messages.json")["cases"]:
        row = Row("draft_message", case["id"], model)
        said: str = case["input"]
        parts = [Part.text(f"<foreman_message>{said}</foreman_message>")]

        def check(d: MessageDraft, said: str = said) -> list[str]:
            return message_problems(d, terms, LANGUAGES, set(MEMBERS), machine_ids, said, names)

        draft = _ask(client, prompt, parts, MessageDraft, check, row)
        if draft is not None:
            text = f"{draft.text.ja}\n{draft.text.en or ''}".lower()
            wrong = [f"lost: {k}" for k in case.get("keep", []) if k.lower() not in text]
            wrong += [f"added: {f}" for f in case.get("forbid", []) if f.lower() in text]
            if (case["questions"] == "none") != (not draft.questions):
                wrong.append(f"{len(draft.questions)} questions (want {case['questions']})")
            if "tasks" in case:
                wrong += _task_problems(case["tasks"], draft)
            row.notes, row.passed, row.answer = wrong, not wrong, json.loads(draft.model_dump_json())
        rows.append(row)
    return rows


def _pct(n: int, total: int) -> str:
    return f"{n}/{total}" if total else "—"


def report(rows: Sequence[Row], models: Sequence[str], when: str) -> str:
    lines = [
        "# AI eval",
        "",
        f"Run {when} with `uv run python -m app.ai.eval` (Galib). Cases: `backend/tests/fixtures/ai/eval` (fictional).",
        "Scored by the app's own checks (`app.domain.ai_checks`, `recurrence.validate_event`, `grounding.ground`)",
        "with one retry, as `AiRunner` does, plus each case's expectations (shop words and numbers kept, Event dates,",
        "covered or not).",
        "",
        "| Model | Pipeline | Valid answer | First try | Passed | Median s |",
        "| :--- | :--- | ---: | ---: | ---: | ---: |",
    ]
    for model in models:
        for pipeline in ("build_note", "build_notice", "answer_ask", "draft_message"):
            rs = [r for r in rows if r.model == model and r.pipeline == pipeline]
            if not rs:
                continue
            secs = sorted(r.seconds for r in rs if r.valid)
            median = f"{secs[len(secs) // 2]:.1f}" if secs else "—"
            lines.append(
                f"| {model} | {pipeline} | {_pct(sum(r.valid for r in rs), len(rs))} "
                f"| {_pct(sum(r.first_try for r in rs), len(rs))} | {_pct(sum(r.passed for r in rs), len(rs))} "
                f"| {median} |"
            )
    failed = [r for r in rows if not r.passed]
    lines += ["", "## Failures", ""]
    lines += [f"- `{r.model}` · {r.pipeline} · `{r.case}`: {'; '.join(r.notes) or 'no answer'}" for r in failed]
    lines += ["- none"] if not failed else []
    lines += [
        "",
        "Not scored here: how natural the English reads (a person reads `--json` output), voice input",
        "(recorded audio cases are added separately) and flyer photos.",
        "",
        "What each Chat message case checks: [ai-eval-messages.md](ai-eval-messages.md). The chatbot has its own",
        "eval through the running API: [ai-eval-chatbot.md](ai-eval-chatbot.md).",
        "",
    ]
    return "\n".join(lines)


def main(argv: Sequence[str] | None = None) -> None:
    parser = argparse.ArgumentParser(description="AI eval: every eval case through each model, scored by our checks.")
    parser.add_argument("--models", nargs="+", default=list(DEFAULT_MODELS))
    parser.add_argument("--pipelines", nargs="+", default=["notes", "notices", "asks", "messages"])
    parser.add_argument("--json", type=Path, help="also write every answer to this file")
    args = parser.parse_args(argv)
    runners = {"notes": eval_notes, "notices": eval_notices, "asks": eval_asks, "messages": eval_messages}
    rows: list[Row] = []
    for model in args.models:
        client = _client(model)
        for name in args.pipelines:
            rows += runners[name](client, model)
            sys.stdout.write(f"{model} {name}: {sum(r.passed for r in rows if r.model == model)} passed so far\n")
    when = datetime.now(TOKYO).strftime("%Y-%m-%d %H:%M JST")
    REPORT.write_bytes(report(rows, args.models, when).encode("utf-8"))
    if args.json:
        args.json.write_bytes(json.dumps([r.__dict__ for r in rows], ensure_ascii=False, indent=1).encode("utf-8"))
    sys.stdout.write(f"wrote {REPORT}\n")


if __name__ == "__main__":
    main()
