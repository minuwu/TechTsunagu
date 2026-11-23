"""WS0 step 5: the AI fallback ladder (architecture §10.3, §10.4). Every case runs FakeAiClient; nothing hits Gemini."""

from datetime import UTC, datetime

import pytest
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.ai.client import AiUnavailable, Overloaded, Part, Prompt, QuotaExhausted
from app.ai.fake import FakeAiClient
from app.models.platform import AiUsage
from app.services.ai_runner import AiLimits, AiRequest, AiRunner, ChainLink, pacific_day


class Echo(BaseModel):
    word: str


PROMPT = Prompt(name="echo", version=1, text="Say a word.")
NOW = datetime(2026, 11, 22, 12, 0, tzinfo=UTC)


def _request(text: str = "hello") -> AiRequest[Echo]:
    return AiRequest(
        pipeline="echo", prompt=PROMPT, parts=[Part.text(text)], schema=Echo, scope="shop:1", cache_input=text.encode()
    )


def _no_problems(_: Echo) -> list[str]:
    return []


def _runner(db: Session, client: FakeAiClient, budget: int = 18) -> AiRunner:
    factory = db.info["factory"]
    return AiRunner(client, factory, AiLimits(max_concurrent=2, per_minute=4, daily_budget=budget, wait_seconds=1))


def test_a_valid_answer_is_returned_and_cached(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "ok"}]})
    runner = _runner(db, client)

    first = runner.run(_request(), _no_problems, NOW)
    second = runner.run(_request(), _no_problems, NOW)

    assert (first.value, first.outcome) == (Echo(word="ok"), "ok")
    assert (second.value, second.outcome) == (Echo(word="ok"), "cached")
    assert client.calls == 1


def test_an_invalid_answer_is_retried_once_with_the_problems(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "bad"}, {"word": "good"}]})

    result = _runner(db, client).run(_request(), lambda e: ["say good"] if e.word == "bad" else [], NOW)

    assert (result.value, result.outcome) == (Echo(word="good"), "retried")
    assert "say good" in client.last_prompt_text


def test_invalid_twice_degrades_without_an_error(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "bad"}, {"word": "bad"}]})

    result = _runner(db, client).run(_request(), lambda _: ["still bad"], NOW)

    assert (result.value, result.outcome) == (None, "degraded")


def test_a_schema_mismatch_counts_as_invalid(db: Session) -> None:
    client = FakeAiClient({"echo": [{"nope": 1}, {"word": "fixed"}]})

    result = _runner(db, client).run(_request(), _no_problems, NOW)

    assert (result.value, result.outcome) == (Echo(word="fixed"), "retried")


def test_an_unavailable_model_degrades(db: Session) -> None:
    client = FakeAiClient({"echo": [AiUnavailable("timeout")]})

    assert _runner(db, client).run(_request(), _no_problems, NOW).outcome == "degraded"


def test_the_daily_budget_stops_calls_before_they_happen(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "a"}, {"word": "b"}]})
    runner = _runner(db, client, budget=1)

    first = runner.run(_request("one"), _no_problems, NOW)
    second = runner.run(_request("two"), _no_problems, NOW)

    assert first.outcome == "ok"
    assert second.outcome == "degraded"
    assert client.calls == 1


def test_a_daily_quota_error_is_never_retried_and_blocks_the_rest_of_the_day(db: Session) -> None:
    client = FakeAiClient({"echo": [QuotaExhausted("per day"), {"word": "never"}]})
    runner = _runner(db, client)

    first = runner.run(_request("one"), _no_problems, NOW)
    second = runner.run(_request("two"), _no_problems, NOW)

    usage = db.get(AiUsage, (pacific_day(NOW), client.model))
    assert (first.outcome, second.outcome) == ("degraded", "degraded")
    assert client.calls == 1
    assert usage is not None and usage.exhausted


def test_the_cache_is_separate_per_scope(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "shop one"}, {"word": "shop two"}]})
    runner = _runner(db, client)
    other = AiRequest(
        pipeline="echo", prompt=PROMPT, parts=[Part.text("hello")], schema=Echo, scope="shop:2", cache_input=b"hello"
    )

    runner.run(_request(), _no_problems, NOW)
    result = runner.run(other, _no_problems, NOW)

    assert result.value == Echo(word="shop two")


@pytest.mark.parametrize(("utc_hour", "day"), [(7, 21), (8, 22)])
def test_the_budget_day_turns_over_at_midnight_pacific(utc_hour: int, day: int) -> None:
    assert pacific_day(datetime(2026, 11, 22, utc_hour, 30, tzinfo=UTC)).day == day


# The model chain (AI_PLAN.md §3): best model first, skip a used-up or busy one, the next model answers


def _chain(db: Session, *links: tuple[FakeAiClient, int]) -> AiRunner:
    factory = db.info["factory"]
    limits = AiLimits(max_concurrent=2, per_minute=4, daily_budget=18, wait_seconds=1, retry_backoff=(0, 0))
    return AiRunner([ChainLink(client, budget, per_minute=4) for client, budget in links], factory, limits)


def test_a_busy_model_gets_one_retry_then_the_next_model_answers(db: Session) -> None:
    flash = FakeAiClient({"echo": [Overloaded("503 high demand"), Overloaded("503 high demand")]}, model="flash")
    lite = FakeAiClient({"echo": [{"word": "lite"}]}, model="lite")

    result = _chain(db, (flash, 18), (lite, 450)).run(_request(), _no_problems, NOW)

    assert (result.value, result.outcome, result.model) == (Echo(word="lite"), "ok", "lite")
    assert (flash.calls, lite.calls) == (2, 1)


def test_a_busy_model_that_recovers_on_the_retry_answers_itself(db: Session) -> None:
    flash = FakeAiClient({"echo": [Overloaded("503"), {"word": "flash"}]}, model="flash")
    lite = FakeAiClient(model="lite")

    result = _chain(db, (flash, 18), (lite, 450)).run(_request(), _no_problems, NOW)

    assert (result.value, result.model) == (Echo(word="flash"), "flash")
    assert lite.calls == 0


def test_a_timeout_moves_to_the_next_model_without_a_retry(db: Session) -> None:
    flash = FakeAiClient({"echo": [AiUnavailable("timeout")]}, model="flash")
    lite = FakeAiClient({"echo": [{"word": "lite"}]}, model="lite")

    result = _chain(db, (flash, 18), (lite, 450)).run(_request(), _no_problems, NOW)

    assert result.model == "lite"
    assert flash.calls == 1


def test_an_exhausted_model_is_skipped_for_the_rest_of_the_day(db: Session) -> None:
    flash = FakeAiClient({"echo": [QuotaExhausted("per day")]}, model="flash")
    lite = FakeAiClient({"echo": [{"word": "a"}, {"word": "b"}]}, model="lite")
    runner = _chain(db, (flash, 18), (lite, 450))

    first = runner.run(_request("one"), _no_problems, NOW)
    second = runner.run(_request("two"), _no_problems, NOW)

    usage = db.get(AiUsage, (pacific_day(NOW), "flash"))
    assert (first.model, second.model) == ("lite", "lite")
    assert flash.calls == 1
    assert usage is not None and usage.exhausted


def test_each_model_has_its_own_daily_budget(db: Session) -> None:
    flash = FakeAiClient({"echo": [{"word": "flash"}, {"word": "never"}]}, model="flash")
    lite = FakeAiClient({"echo": [{"word": "lite"}]}, model="lite")
    runner = _chain(db, (flash, 1), (lite, 450))

    first = runner.run(_request("one"), _no_problems, NOW)
    second = runner.run(_request("two"), _no_problems, NOW)

    assert (first.model, second.model) == ("flash", "lite")
    assert flash.calls == 1


def test_a_cached_answer_from_a_better_model_is_reused_when_that_model_is_out(db: Session) -> None:
    flash = FakeAiClient({"echo": [{"word": "filmed take"}, QuotaExhausted("per day")]}, model="flash")
    lite = FakeAiClient({"echo": [{"word": "other"}]}, model="lite")
    runner = _chain(db, (flash, 18), (lite, 450))
    runner.run(_request(), _no_problems, NOW)  # the filmed take, stored under flash
    runner.run(_request("something else"), _no_problems, NOW)  # flash runs out of quota here

    again = runner.run(_request(), _no_problems, NOW)

    assert (again.value, again.outcome, again.model) == (Echo(word="filmed take"), "cached", "flash")


def test_the_validation_retry_goes_to_the_model_that_answered(db: Session) -> None:
    flash = FakeAiClient({"echo": [AiUnavailable("timeout")]}, model="flash")
    lite = FakeAiClient({"echo": [{"word": "bad"}, {"word": "good"}]}, model="lite")

    result = _chain(db, (flash, 18), (lite, 450)).run(
        _request(), lambda e: ["say good"] if e.word == "bad" else [], NOW
    )

    assert (result.value, result.outcome, result.model) == (Echo(word="good"), "retried", "lite")
    assert flash.calls == 1


def test_every_model_out_degrades_without_an_error(db: Session) -> None:
    flash = FakeAiClient({"echo": [QuotaExhausted("per day")]}, model="flash")
    lite = FakeAiClient({"echo": [AiUnavailable("timeout")]}, model="lite")

    result = _chain(db, (flash, 18), (lite, 450)).run(_request(), _no_problems, NOW)

    assert (result.value, result.outcome, result.model) == (None, "degraded", None)
