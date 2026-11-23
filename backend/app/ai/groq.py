"""Groq adapter, the backup at the end of every chain (XREQ-003, AI_PLAN.md §3).

Groq's text models take no audio, so a recording goes through Whisper first and the model gets the transcript.
Photos cannot be read here: a flyer photo raises AiUnavailable and the pipeline degrades as before."""

import json
import logging

import httpx2
from pydantic import TypeAdapter

from app.ai.client import AiUnavailable, Part, Prompt, QuotaExhausted

_log = logging.getLogger("wakatta.ai")
_BASE_URL = "https://api.groq.com/openai/v1"
_WHISPER = "whisper-large-v3-turbo"
_AUDIO_FILES = {"audio/webm": "note.webm", "audio/ogg": "note.ogg", "audio/mp4": "note.m4a", "audio/m4a": "note.m4a"}
_TRANSCRIPT_NOTE = (
    "The recording was transcribed to text for you. Treat <audio_transcript> as the recorded input, and copy it "
    "as the transcript where one is asked for."
)


class GroqClient:
    def __init__(self, api_key: str, model: str, timeout_seconds: int, transport: httpx2.BaseTransport | None = None):
        self.model = model
        self._http = httpx2.Client(
            base_url=_BASE_URL,
            headers={"Authorization": f"Bearer {api_key}"},
            timeout=timeout_seconds,
            transport=transport,
        )

    def _post(self, path: str, **kwargs: object) -> httpx2.Response:
        try:
            response = self._http.post(path, **kwargs)  # pyright: ignore[reportArgumentType]
        except httpx2.HTTPError as exc:  # timeouts and network errors: fall back
            raise AiUnavailable(f"groq: {type(exc).__name__}") from exc
        if response.status_code == 429 and "per day" in response.text:
            raise QuotaExhausted(f"{self.model}: daily limit used up")
        if response.status_code >= 400:
            _log.warning("groq HTTP %s: %s", response.status_code, response.text[:200])
            raise AiUnavailable(f"groq: HTTP {response.status_code}")
        return response

    def _transcribe(self, data: bytes, mime: str) -> str:
        name = _AUDIO_FILES.get(mime, "note.webm")
        response = self._post(
            "/audio/transcriptions",
            files={"file": (name, data, mime)},
            data={"model": _WHISPER, "language": "ja", "response_format": "text"},
        )
        return response.text.strip()

    def generate(self, prompt: Prompt, parts: list[Part], schema: type[object]) -> str:
        texts: list[str] = []
        for part in parts:
            if part.data is not None and part.mime is not None:
                if not part.mime.startswith("audio/"):
                    raise AiUnavailable(f"groq: cannot read {part.mime}")
                texts.append(f"<audio_transcript>{self._transcribe(part.data, part.mime)}</audio_transcript>")
                texts.append(_TRANSCRIPT_NOTE)
            elif part.text_value is not None:
                texts.append(part.text_value)
        json_schema = json.dumps(TypeAdapter(schema).json_schema())
        system = f"{prompt.text}\n\nReturn only JSON that matches this JSON Schema:\n{json_schema}"
        body = {
            "model": self.model,
            "messages": [{"role": "system", "content": system}, {"role": "user", "content": "\n".join(texts)}],
            "response_format": {"type": "json_object"},
            "temperature": 0.2,
        }
        reply = self._post("/chat/completions", json=body).json()
        content = reply["choices"][0]["message"]["content"] if reply.get("choices") else None
        if not content:
            raise AiUnavailable(f"{self.model}: empty reply")
        return content
