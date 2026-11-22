"""Scripted stand-in for Gemini (ADR 0008): tests queue answers per pipeline; development uses canned drafts."""

import json
from collections import deque
from collections.abc import Mapping, Sequence

from app.ai.client import AiUnavailable, Part, Prompt

Scripted = Mapping[str, object] | Exception


class FakeAiClient:
    model = "fake"

    def __init__(self, script: Mapping[str, Sequence[Scripted]] | None = None) -> None:
        self._script = {name: deque(answers) for name, answers in (script or {}).items()}
        self.calls = 0
        self.last_prompt_text = ""

    def generate(self, prompt: Prompt, parts: list[Part], schema: type[object]) -> str:
        self.calls += 1
        self.last_prompt_text = "\n".join([prompt.text, *(p.text_value or "" for p in parts)])
        queue = self._script.get(prompt.name)
        if not queue:
            raise AiUnavailable(f"fake: nothing scripted for {prompt.name}")
        answer = queue.popleft()
        if isinstance(answer, Exception):
            raise answer
        return json.dumps(answer, ensure_ascii=False)
