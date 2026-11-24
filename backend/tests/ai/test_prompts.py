"""Each prompt file fills in with exactly the placeholders its pipeline passes (string.Template)."""

from pathlib import Path
from string import Template

import pytest

PROMPTS = Path(__file__).parents[2] / "app" / "prompts"

PLACEHOLDERS = {
    "build_note.v3.md": {"glossary", "icons", "languages"},
    "build_briefing.v2.md": {"glossary", "languages"},
    "build_notice.v3.md": {"glossary", "today", "languages"},
    "answer_ask.v3.md": {"glossary", "notices", "languages"},
    "translate.v1.md": {"glossary", "source_lang", "target_lang"},
    "answer_knowledge.v1.md": {"sources", "glossary", "languages"},
    "draft_message.v4.md": {"glossary", "members", "machines", "now", "languages"},
    "reexplain.v2.md": {"glossary", "term", "languages"},
    "explain_utterance.v1.md": {"language"},
    "grade_understanding.v1.md": {"meaning", "plain_ja"},
}


def test_every_prompt_file_is_listed() -> None:
    assert {p.name for p in PROMPTS.glob("*.md")} == set(PLACEHOLDERS)


@pytest.mark.parametrize(("name", "keys"), PLACEHOLDERS.items())
def test_prompt_uses_exactly_its_placeholders(name: str, keys: set[str]) -> None:
    template = Template((PROMPTS / name).read_text(encoding="utf-8"))
    assert set(template.get_identifiers()) == keys
    filled = template.substitute(dict.fromkeys(keys, "X"))
    assert "$" not in filled


def test_the_model_gets_a_schema_with_only_the_shop_languages() -> None:
    from app.ai.schemas import NoteDraft, response_schema

    defs = response_schema(NoteDraft, ["en"])["$defs"]

    assert list(defs["AiTri"]["properties"]) == defs["AiTri"]["required"] == ["ja", "en"]
    assert defs["BackTranslation"]["required"] == ["en"]
    assert defs["AiTri"]["properties"]["en"]["type"] == "string"  # required, not nullable
