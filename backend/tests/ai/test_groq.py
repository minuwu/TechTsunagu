"""The Groq backup adapter, offline: httpx2.MockTransport stands in for api.groq.com."""

import json
from collections.abc import Callable

import httpx2
import pytest
from pydantic import BaseModel

from app.ai.client import AiUnavailable, Part, Prompt, QuotaExhausted
from app.ai.groq import GroqClient

PROMPT = Prompt(name="echo", version=1, text="Turn the input into JSON.")


class Echo(BaseModel):
    word: str


def _client(handler: Callable[[httpx2.Request], httpx2.Response]) -> tuple[GroqClient, list[httpx2.Request]]:
    seen: list[httpx2.Request] = []

    def record(request: httpx2.Request) -> httpx2.Response:
        seen.append(request)
        return handler(request)

    return GroqClient("test-key", "openai/gpt-oss-120b", 5, transport=httpx2.MockTransport(record)), seen


def _chat(content: str) -> httpx2.Response:
    return httpx2.Response(200, json={"choices": [{"message": {"content": content}}]})


def test_audio_is_transcribed_first_and_the_model_gets_the_transcript() -> None:
    def handler(request: httpx2.Request) -> httpx2.Response:
        if request.url.path.endswith("/audio/transcriptions"):
            return httpx2.Response(200, text="保護メガネをかけろ")
        return _chat('{"word": "ok"}')

    client, seen = _client(handler)
    answer = client.generate(PROMPT, [Part.blob(b"OggS...", "audio/ogg")], Echo)

    assert json.loads(answer) == {"word": "ok"}
    assert [r.url.path for r in seen] == ["/openai/v1/audio/transcriptions", "/openai/v1/chat/completions"]
    body = json.loads(seen[1].content)
    assert "<audio_transcript>保護メガネをかけろ</audio_transcript>" in body["messages"][1]["content"]
    assert '"word"' in body["messages"][0]["content"]  # the JSON Schema is in the system prompt
    assert seen[0].headers["Authorization"] == "Bearer test-key"


@pytest.mark.parametrize(("language", "sent"), [("en", b'name="language"\r\n\r\nen'), (None, None)])
def test_whisper_hears_the_speakers_language_or_detects_it(language: str | None, sent: bytes | None) -> None:
    """A Worker's English recording must not be transcribed as Japanese (XREQ-007 seam)."""

    def handler(request: httpx2.Request) -> httpx2.Response:
        if request.url.path.endswith("/audio/transcriptions"):
            return httpx2.Response(200, text="my fan is broken")
        return _chat('{"word": "ok"}')

    client, seen = _client(handler)
    client.generate(PROMPT, [Part.blob(b"OggS...", "audio/ogg", language)], Echo)

    form = seen[0].content
    assert (sent in form) if sent else (b'name="language"' not in form)


def test_text_goes_straight_to_the_model() -> None:
    client, seen = _client(lambda _: _chat('{"word": "hi"}'))

    client.generate(PROMPT, [Part.text("<foreman_input>こんにちは</foreman_input>")], Echo)

    assert len(seen) == 1


def test_a_photo_cannot_be_read_here() -> None:
    client, seen = _client(lambda _: _chat("{}"))

    with pytest.raises(AiUnavailable):
        client.generate(PROMPT, [Part.blob(b"\xff\xd8", "image/jpeg")], Echo)
    assert seen == []


def test_the_daily_limit_is_quota_exhausted_and_other_errors_are_unavailable() -> None:
    daily, _ = _client(lambda _: httpx2.Response(429, text="Rate limit reached on tokens per day (TPD)"))
    minute, _ = _client(lambda _: httpx2.Response(429, text="Rate limit reached on tokens per minute (TPM)"))
    broken, _ = _client(lambda _: httpx2.Response(400, json={"error": {"code": "json_validate_failed"}}))

    with pytest.raises(QuotaExhausted):
        daily.generate(PROMPT, [Part.text("x")], Echo)
    for client in (minute, broken):
        with pytest.raises(AiUnavailable) as raised:
            client.generate(PROMPT, [Part.text("x")], Echo)
        assert not isinstance(raised.value, QuotaExhausted)
