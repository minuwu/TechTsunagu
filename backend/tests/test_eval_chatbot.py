"""The chatbot eval's grading (app.ai.eval_chatbot) and its cases."""

import json
from typing import Any

from app.ai.eval_chatbot import CASES, grade

SAFETY: dict[str, Any] = {"id": "mhlw-safety:p15:1", "kind": "safety", "attribution": "出典：厚生労働省"}
CASE: dict[str, Any] = {"expect": "covered", "expect_sources": ["mhlw-safety"]}


def answer(**over: Any) -> dict[str, Any]:
    return {"covered": True, "emergency": False, "sources": [SAFETY], "check": {"id": "q1"}} | over


def test_a_cited_covered_answer_passes() -> None:
    assert grade(CASE, answer()) == []


def test_wrong_outcome_source_check_and_attribution_fail() -> None:
    assert grade(CASE, answer(covered=False)) == ["not_covered (want covered)"]
    assert grade({"expect": "not_covered"}, answer(emergency=True)) == ["emergency (want not_covered)"]
    note = {"id": "note:3", "kind": "note", "attribution": None}
    assert grade(CASE, answer(sources=[note]))[0].startswith("cited ['note:3']")
    assert grade(CASE, answer(check=None)) == ["no self-check question"]
    assert grade(CASE, answer(sources=[SAFETY | {"attribution": None}])) == ["a public guide without its attribution"]


def test_cases_are_consistent() -> None:
    cases: list[dict[str, Any]] = json.loads(CASES.read_text(encoding="utf-8"))["cases"]
    assert len({c["id"] for c in cases}) == len(cases)
    for c in cases:
        assert c["expect"] in ("covered", "not_covered", "emergency"), c["id"]
        assert bool(c["expect_sources"]) == (c["expect"] == "covered"), c["id"]
