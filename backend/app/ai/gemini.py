"""Gemini adapter (ADR 0001, 0008): structured JSON output, inline media, hard timeout, vendor errors mapped."""

import logging
from collections.abc import Sequence

from google import genai
from google.genai import errors, types

from app.ai.client import AiUnavailable, Part, Prompt, QuotaExhausted
from app.ai.schemas import DEFAULT_LANGUAGES, response_schema

_log = logging.getLogger("wakatta.ai")
# Safari records audio/mp4, which Gemini does not list; the same AAC data is accepted as audio/m4a (Galib, point 1).
_MIME_ALIASES = {"audio/mp4": "audio/m4a"}


def _is_daily_quota(exc: errors.APIError) -> bool:
    return exc.code == 429 and "PerDay" in str(exc)  # the quota id, e.g. GenerateRequestsPerDayPerProject…


class GeminiClient:
    def __init__(self, api_key: str, model: str, timeout_seconds: int) -> None:
        self.model = model
        # One attempt: the SDK's own retries spent ~10 s on each busy (503) model before our chain could move on,
        # so a request walked 4 busy models in ~45 s (seen 2026-11-23). AiRunner already retries and fails over.
        options = types.HttpOptions(timeout=timeout_seconds * 1000, retry_options=types.HttpRetryOptions(attempts=1))
        self._client = genai.Client(api_key=api_key, http_options=options)

    def generate(
        self, prompt: Prompt, parts: list[Part], schema: type[object], languages: Sequence[str] = DEFAULT_LANGUAGES
    ) -> str:
        contents: list[str | types.Part] = [prompt.text]
        for part in parts:
            if part.data is not None and part.mime is not None:
                mime = _MIME_ALIASES.get(part.mime, part.mime)
                contents.append(types.Part.from_bytes(data=part.data, mime_type=mime))
            elif part.text_value is not None:
                contents.append(part.text_value)
        # JSON Schema, not the class: response_schema rejects additionalProperties, which every AiModel has
        # (extra="forbid"), so each real call got HTTP 400 (Galib's benchmark, 2026-11-23).
        json_schema = response_schema(schema, languages)
        config = types.GenerateContentConfig(response_mime_type="application/json", response_json_schema=json_schema)
        try:
            # The SDK's `contents` union names PIL.Image, which is not installed, so pyright sees it as Unknown.
            response = self._client.models.generate_content(  # pyright: ignore[reportUnknownMemberType]
                model=self.model, contents=contents, config=config
            )
        except errors.APIError as exc:
            if _is_daily_quota(exc):
                raise QuotaExhausted(f"{self.model}: daily quota used up") from exc
            raise AiUnavailable(f"{self.model}: HTTP {exc.code}") from exc
        except Exception as exc:  # network errors and timeouts surface as several types; all mean "fall back"
            _log.warning("gemini call failed", exc_info=exc)
            raise AiUnavailable(f"{self.model}: {type(exc).__name__}") from exc
        if not response.text:
            raise AiUnavailable(f"{self.model}: empty reply")
        return response.text
