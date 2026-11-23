"""Each prompt file fills in with exactly the placeholders its pipeline passes (string.Template)."""

from pathlib import Path
from string import Template

import pytest

PROMPTS = Path(__file__).parents[2] / "app" / "prompts"

PLACEHOLDERS = {
    "build_note.v1.md": {"glossary", "icons"},
    "build_note.v2.md": {"glossary", "icons"},
    "build_briefing.v1.md": {"glossary"},
    "build_notice.v1.md": {"glossary", "today"},
    "build_notice.v2.md": {"glossary", "today"},
    "answer_ask.v1.md": {"glossary", "notices"},
    "translate.v1.md": {"glossary", "source_lang", "target_lang"},
    "draft_message.v1.md": {"glossary", "members", "machines", "now"},
    "reexplain.v1.md": {"glossary", "term"},
}


def test_every_prompt_file_is_listed() -> None:
    assert {p.name for p in PROMPTS.glob("*.md")} == set(PLACEHOLDERS)


@pytest.mark.parametrize(("name", "keys"), PLACEHOLDERS.items())
def test_prompt_uses_exactly_its_placeholders(name: str, keys: set[str]) -> None:
    template = Template((PROMPTS / name).read_text(encoding="utf-8"))
    assert set(template.get_identifiers()) == keys
    filled = template.substitute(dict.fromkeys(keys, "X"))
    assert "$" not in filled
