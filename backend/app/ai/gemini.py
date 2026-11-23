"""Gemini adapter (ADR 0001, 0008): structured JSON output, inline media, hard timeout, vendor errors mapped."""

import logging

from google import genai
from google.genai import errors, types

from app.ai.client import AiUnavailable, Overloaded, Part, Prompt, QuotaExhausted

_log = logging.getLogger("wakatta.ai")
# Safari records audio/mp4, which Gemini does not list; the same AAC data is accepted as audio/m4a (Galib, point 1).
_MIME_ALIASES = {"audio/mp4": "audio/m4a"}


def map_error(model: str, exc: errors.APIError) -> AiUnavailable:
    """Daily 429 → QuotaExhausted (skip the model for the day); per-minute 429 or 5xx → Overloaded (one retry);
    anything else (400, 404 unknown model, 403 key) → AiUnavailable (next model at once)."""
    if exc.code == 429 and "PerDay" in str(exc):  # the quota id, e.g. GenerateRequestsPerDayPerProject…
        return QuotaExhausted(f"{model}: daily quota used up")
    if exc.code == 429 or exc.code >= 500:  # Galib's live check: 3.8 Flash answered 503 "high demand" twice
        return Overloaded(f"{model}: HTTP {exc.code}")
    return AiUnavailable(f"{model}: HTTP {exc.code}")


class GeminiClient:
    def __init__(self, api_key: str, model: str, timeout_seconds: int) -> None:
        self.model = model
        self._client = genai.Client(api_key=api_key, http_options=types.HttpOptions(timeout=timeout_seconds * 1000))

    def generate(self, prompt: Prompt, parts: list[Part], schema: type[object]) -> str:
        contents: list[str | types.Part] = [prompt.text]
        for part in parts:
            if part.data is not None and part.mime is not None:
                mime = _MIME_ALIASES.get(part.mime, part.mime)
                contents.append(types.Part.from_bytes(data=part.data, mime_type=mime))
            elif part.text_value is not None:
                contents.append(part.text_value)
        config = types.GenerateContentConfig(response_mime_type="application/json", response_schema=schema)
        try:
            # The SDK's `contents` union names PIL.Image, which is not installed, so pyright sees it as Unknown.
            response = self._client.models.generate_content(  # pyright: ignore[reportUnknownMemberType]
                model=self.model, contents=contents, config=config
            )
        except errors.APIError as exc:
            raise map_error(self.model, exc) from exc
        except Exception as exc:  # network errors and timeouts surface as several types; all mean "fall back"
            _log.warning("gemini call failed", exc_info=exc)
            raise AiUnavailable(f"{self.model}: {type(exc).__name__}") from exc
        if not response.text:
            raise AiUnavailable(f"{self.model}: empty reply")
        return response.text
