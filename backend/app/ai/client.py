"""The AI seam (ADR 0008): services and the runner see only this interface, never a vendor type."""

from collections.abc import Sequence
from dataclasses import dataclass
from typing import Protocol, Self


class AiUnavailable(Exception):
    """Timeout, rate limit, server error or unparseable reply: the caller falls back."""


class QuotaExhausted(AiUnavailable):
    """The model's daily quota is used up: never retried, and the rest of the day goes straight to the fallback."""


@dataclass(frozen=True)
class Prompt:
    name: str
    version: int
    text: str


@dataclass(frozen=True)
class Part:
    """Either text, or bytes with a MIME type (audio, image), sent inline."""

    text_value: str | None = None
    data: bytes | None = None
    mime: str | None = None

    @classmethod
    def text(cls, value: str) -> Self:
        return cls(text_value=value)

    @classmethod
    def blob(cls, data: bytes, mime: str) -> Self:
        return cls(data=data, mime=mime)


class AiClient(Protocol):
    model: str

    def generate(
        self, prompt: Prompt, parts: list[Part], schema: type[object], languages: Sequence[str] = ("bn", "en")
    ) -> str:
        """Returns the model's raw JSON text for `schema`; raises AiUnavailable or QuotaExhausted."""
        ...
