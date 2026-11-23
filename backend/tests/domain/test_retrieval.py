"""The chatbot's search (app.domain.retrieval) over the shipped public guides, and the emergency rule."""

import json
from pathlib import Path

from app.domain.retrieval import Piece, is_emergency, search, tokens

KNOWLEDGE = Path(__file__).parents[2] / "app" / "seed_data" / "knowledge.json"
CHUNKS = json.loads(KNOWLEDGE.read_text(encoding="utf-8"))["chunks"]
PIECES = [Piece(c["id"], c["text"]) for c in CHUNKS]


def test_life_and_safety_questions_find_the_right_guide_pages() -> None:
    assert search("How do I throw away garbage?", PIECES, 3)[0].id.startswith("isa-guide:p142")
    assert search("What should I do in an earthquake?", PIECES, 3)[0].id.startswith("isa-guide:p12")
    assert search("Can I wear gloves near a drilling machine?", PIECES, 3)[0].id.startswith("mhlw-safety:")


def test_the_shop_instruction_for_this_machine_ranks_first() -> None:
    note = Piece("note:1", "Press brake: wear safety glasses before bending", boost=3.0)
    assert search("safety glasses press brake", [*PIECES, note], 3)[0].id == "note:1"


def test_japanese_is_searched_by_character_pairs_and_nothing_matches_nothing() -> None:
    assert "玉掛" in tokens("玉掛けの前に")
    assert search("zzzz qqqq", PIECES) == []


def test_injury_fire_and_gas_questions_are_emergencies() -> None:
    assert is_emergency("My friend is bleeding from the hand")
    assert is_emergency("火事です")
    assert not is_emergency("How do I clean the press brake?")
