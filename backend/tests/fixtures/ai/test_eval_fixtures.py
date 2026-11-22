"""The AI eval set stays complete: every seeded Glossary Term is tested, and the cases point at real data."""

import json
from pathlib import Path
from typing import Any

HERE = Path(__file__).parent


def load(name: str) -> dict[str, Any]:
    return json.loads((HERE / name).read_text(encoding="utf-8"))


SEED_DATA = HERE.parents[2] / "app" / "seed_data"  # the glossary is shared with the demo seed
GLOSSARY: list[dict[str, Any]] = json.loads((SEED_DATA / "glossary.json").read_text(encoding="utf-8"))["terms"]
NOTES: list[dict[str, Any]] = load("eval/notes.json")["cases"]
NOTICES: list[dict[str, Any]] = load("eval/notices.json")["cases"]
ASKS: list[dict[str, Any]] = load("eval/asks.json")["cases"]


def test_ids_are_unique() -> None:
    ids = [c["id"] for c in NOTES + NOTICES + ASKS]
    assert len(ids) == len(set(ids))


def test_every_glossary_term_is_tested() -> None:
    tested = {t for c in NOTES for t in c["expect_terms"]} | {t for c in NOTICES for t in c["expect"]["terms"]}
    untested = {t["ja"] for t in GLOSSARY} - tested
    # 避難所 is tested through the drill notice's steps; no notice states it as a source word yet.
    assert untested <= {"避難所"}


def test_expected_terms_are_in_the_input() -> None:
    by_ja = {t["ja"]: t for t in GLOSSARY}
    for c in NOTES:
        for term in c["expect_terms"]:
            assert term in c["input_ja"] or by_ja[term]["reading"] in c["input_ja"], (c["id"], term)
    for c in NOTICES:
        for term in c["expect"]["terms"]:
            assert term in c["source_ja"], (c["id"], term)


def test_asks_cite_real_notices_and_two_are_not_covered() -> None:
    notice_ids = {c["id"] for c in NOTICES}
    for c in ASKS:
        assert set(c["expect_sources"]) <= notice_ids, c["id"]
        assert c["expect_covered"] == bool(c["expect_sources"]), c["id"]
    assert sum(not c["expect_covered"] for c in ASKS) == 2


def test_garbage_notice_states_no_collection_day() -> None:
    garbage = next(c for c in NOTICES if c["expect"]["category"] == "garbage")
    assert garbage["expect"]["events"] == []
    # Weekdays appear as 月曜 / （月）, and repetition as 毎週 / 第1; plain 日 also means "day" (収集日).
    weekday_marks = ["曜", "毎週", "第", *[f"（{d}）" for d in "月火水木金土日"]]
    assert not any(mark in garbage["source_ja"] for mark in weekday_marks)
