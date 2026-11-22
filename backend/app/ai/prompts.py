"""Loads `app/prompts/<name>.v<N>.md` and fills its placeholders (string.Template, as Galib's prompt tests check)."""

from functools import cache
from pathlib import Path
from string import Template

from app.ai.client import Prompt

_DIR = Path(__file__).resolve().parent.parent / "prompts"


@cache
def _template(name: str, version: int) -> Template:
    return Template((_DIR / f"{name}.v{version}.md").read_text(encoding="utf-8"))


def render(name: str, version: int, **values: str) -> Prompt:
    return Prompt(name=name, version=version, text=_template(name, version).substitute(values))
