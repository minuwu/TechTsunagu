"""WS0 step 5: the AI fallback ladder (architecture §10.3, §10.4). Every case runs FakeAiClient; nothing hits Gemini."""

import time
from datetime import UTC, datetime

import pytest
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.ai.client import AiUnavailable, Part, Prompt, QuotaExhausted
from app.ai.fake import FakeAiClient
from app.models.platform import AiUsage
from app.services.ai_runner import AiLimits, AiRequest, AiRunner, pacific_day


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


def _named(name: str, script: dict[str, list[object]]) -> FakeAiClient:
    client = FakeAiClient(script)  # pyright: ignore[reportArgumentType]
    client.model = name
    return client


def _chain(db: Session, *clients: FakeAiClient, budget: int = 18) -> AiRunner:
    limits = AiLimits(max_concurrent=2, per_minute=4, daily_budget=budget, wait_seconds=1)
    first, *rest = clients
    return AiRunner(first, db.info["factory"], limits, [(c, limits) for c in rest])


def test_an_unavailable_model_hands_over_to_the_next_in_the_chain(db: Session) -> None:
    best = _named("best", {"echo": [AiUnavailable("503 high demand")]})
    backup = _named("backup", {"echo": [{"word": "from backup"}]})

    result = _chain(db, best, backup).run(_request(), _no_problems, NOW)

    assert (result.value, result.outcome) == (Echo(word="from backup"), "ok")
    assert (best.calls, backup.calls) == (1, 1)


def test_a_model_that_fails_its_retry_hands_over_too(db: Session) -> None:
    best = _named("best", {"echo": [{"word": "bad"}, {"word": "bad"}]})
    backup = _named("backup", {"echo": [{"word": "good"}]})

    result = _chain(db, best, backup).run(_request(), lambda e: ["say good"] if e.word == "bad" else [], NOW)

    assert result.value == Echo(word="good")
    assert (best.calls, backup.calls) == (2, 1)


def test_a_model_out_of_budget_is_skipped_without_a_call(db: Session) -> None:
    best = _named("best", {"echo": [{"word": "a"}, {"word": "b"}]})
    backup = _named("backup", {"echo": [{"word": "c"}]})
    runner = _chain(db, best, backup, budget=1)

    runner.run(_request("one"), _no_problems, NOW)
    second = runner.run(_request("two"), _no_problems, NOW)

    assert second.value == Echo(word="c")
    assert (best.calls, backup.calls) == (1, 1)


def test_a_model_at_its_per_minute_limit_hands_over_at_once_without_spending_its_day(db: Session) -> None:
    best = _named("best", {"echo": [{"word": "a"}]})
    backup = _named("backup", {"echo": [{"word": "b"}]})
    limits = AiLimits(max_concurrent=2, per_minute=1, daily_budget=18, wait_seconds=30)
    runner = AiRunner(best, db.info["factory"], limits, [(backup, limits)])
    runner.run(_request("one"), _no_problems, NOW)  # best uses its one call this minute

    started = time.monotonic()
    second = runner.run(_request("two"), _no_problems, NOW)

    assert second.value == Echo(word="b")
    assert time.monotonic() - started < 5  # no 30 s wait for best's per-minute slot
    usage = db.get(AiUsage, (pacific_day(NOW), "best"))
    assert usage is not None and usage.calls == 1  # the skip did not spend one of best's daily calls


def test_a_cached_answer_from_any_model_of_the_chain_is_reused(db: Session) -> None:
    backup = _named("backup", {"echo": [{"word": "filmed"}]})
    _chain(db, _named("best", {"echo": [AiUnavailable("down")]}), backup).run(_request(), _no_problems, NOW)
    best_again = _named("best", {"echo": [{"word": "never asked"}]})

    result = _chain(db, best_again, _named("backup", {})).run(_request(), _no_problems, NOW)

    assert (result.value, result.outcome) == (Echo(word="filmed"), "cached")
    assert best_again.calls == 0


def test_every_model_failing_still_degrades_without_an_error(db: Session) -> None:
    down = [_named(n, {"echo": [AiUnavailable("down")]}) for n in ("a", "b", "c")]

    assert _chain(db, *down).run(_request(), _no_problems, NOW).outcome == "degraded"
