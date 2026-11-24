"""WS0 step 5: the AI fallback ladder (architecture §10.3, §10.4). Every case runs FakeAiClient; nothing hits Gemini."""

import time
from collections.abc import Sequence
from dataclasses import replace
from datetime import UTC, datetime, timedelta
from typing import Literal

import pytest
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.ai.chains import BUDGETS, Tier
from app.ai.client import AiUnavailable, Part, Prompt, QuotaExhausted
from app.ai.fake import FakeAiClient
from app.api.errors import ApiError
from app.models.platform import AiCache, AiUsage
from app.services.ai_runner import USER_LIMITS, AiLimits, AiRequest, AiRunner, pacific_day


class Echo(BaseModel):
    word: str


PROMPT = Prompt(name="echo", version=1, text="Say a word.")
NOW = datetime(2026, 11, 22, 12, 0, tzinfo=UTC)


def _request(
    text: str = "hello", caller: str = "user:1", over_limit: Literal["refuse", "degrade"] = "refuse"
) -> AiRequest[Echo]:
    return AiRequest(
        pipeline="echo",
        prompt=PROMPT,
        parts=[Part.text(text)],
        schema=Echo,
        scope="shop:1",
        cache_input=text.encode(),
        caller=caller,
        over_limit=over_limit,
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
    other = replace(_request(), scope="shop:2")

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


def _limited(db: Session, client: FakeAiClient, per_minute: int, per_day: int) -> AiRunner:
    limits = AiLimits(max_concurrent=2, per_minute=100, daily_budget=100, wait_seconds=1)
    return AiRunner(client, db.info["factory"], limits, user_limits=(per_minute, per_day))


def test_one_user_over_the_minute_limit_is_refused_and_others_still_answered(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": w} for w in "abc"]})
    runner = _limited(db, client, per_minute=2, per_day=50)
    for text in ("one", "two"):
        runner.run(_request(text), _no_problems, NOW)

    with pytest.raises(ApiError) as refused:
        runner.run(_request("three"), _no_problems, NOW)
    other = runner.run(_request("three", caller="user:2"), _no_problems, NOW)

    assert (refused.value.status, refused.value.code) == (429, "RATE_LIMITED")
    assert refused.value.headers == {"Retry-After": "60"}
    assert other.value == Echo(word="c") and client.calls == 3


def test_a_cached_answer_does_not_spend_the_users_allowance(db: Session) -> None:
    runner = _limited(db, FakeAiClient({"echo": [{"word": "a"}]}), per_minute=1, per_day=1)
    runner.run(_request(), _no_problems, NOW)

    assert [runner.run(_request(), _no_problems, NOW).outcome for _ in range(3)] == ["cached"] * 3


def test_the_user_is_answered_again_after_the_minute(db: Session) -> None:
    runner = _limited(db, FakeAiClient({"echo": [{"word": "a"}, {"word": "b"}]}), per_minute=1, per_day=50)
    runner.run(_request("one"), _no_problems, NOW)

    later = runner.run(_request("two"), _no_problems, NOW + timedelta(seconds=61))

    assert later.value == Echo(word="b")


def test_one_user_cannot_spend_the_whole_day_for_everyone(db: Session) -> None:
    runner = _limited(db, FakeAiClient({"echo": [{"word": "a"}]}), per_minute=10, per_day=1)
    runner.run(_request("one"), _no_problems, NOW)

    with pytest.raises(ApiError) as refused:
        runner.run(_request("two"), _no_problems, NOW + timedelta(minutes=5))

    assert refused.value.status == 429 and refused.value.headers == {"Retry-After": "3600"}


def test_a_message_that_must_not_be_lost_degrades_instead_of_being_refused(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "a"}]})
    runner = _limited(db, client, per_minute=1, per_day=50)
    runner.run(_request("one"), _no_problems, NOW)

    assert runner.run(_request("two", over_limit="degrade"), _no_problems, NOW).outcome == "degraded"
    assert client.calls == 1


@pytest.mark.parametrize("tier", ["free", "paid"])
def test_no_user_can_spend_a_models_daily_budget_even_if_every_answer_is_retried(tier: Tier) -> None:
    flash_lite = BUDGETS[tier]["gemini-3.5-flash-lite"][0]

    assert USER_LIMITS[tier][1] * 2 < flash_lite


def test_a_shop_request_must_name_its_caller() -> None:
    with pytest.raises(ValueError, match="must name its caller"):
        _request(caller="")


def test_a_personal_request_is_never_cached_and_spends_its_owners_allowance(db: Session) -> None:
    client = FakeAiClient({"echo": [{"word": "one"}, {"word": "two"}]})
    runner = _limited(db, client, per_minute=2, per_day=50)
    personal = replace(_request(), scope="user:7", caller="")

    words = [runner.run(personal, _no_problems, NOW).value for _ in range(2)]

    assert words == [Echo(word="one"), Echo(word="two")]
    assert db.scalars(select(AiCache)).all() == []
    with pytest.raises(ApiError):
        runner.run(replace(_request(), caller="user:7"), _no_problems, NOW)


class _Slow(FakeAiClient):
    """A model that answers only after `seconds` (audit A1: Gemini took 51-61 s on some drafts)."""

    def __init__(self, name: str, seconds: float, script: dict[str, list[object]]) -> None:
        super().__init__(script)  # pyright: ignore[reportArgumentType]
        self.model, self._seconds = name, seconds

    def generate(
        self, prompt: Prompt, parts: list[Part], schema: type[object], languages: Sequence[str] = ("en",)
    ) -> str:
        time.sleep(self._seconds)
        return super().generate(prompt, parts, schema, languages)


def test_no_new_model_starts_after_the_chain_time_is_used(db: Session) -> None:
    """The request ends degraded instead of running past the Android app's 30 s timeout."""
    slow = _Slow("slow", 0.4, {"echo": [AiUnavailable("504 after a long wait")]})
    backup = _named("backup", {"echo": [{"word": "too late"}]})
    limits = AiLimits(max_concurrent=2, per_minute=4, daily_budget=18, wait_seconds=1)
    runner = AiRunner(slow, db.info["factory"], limits, [(backup, limits)], chain_seconds=0.2)

    started = time.monotonic()
    result = runner.run(_request(), _no_problems, NOW)

    assert result.outcome == "degraded" and backup.calls == 0
    assert time.monotonic() - started < 1


def test_a_backup_still_answers_within_the_chain_time(db: Session) -> None:
    slow = _Slow("slow", 0.05, {"echo": [AiUnavailable("503")]})
    backup = _named("backup", {"echo": [{"word": "in time"}]})
    limits = AiLimits(max_concurrent=2, per_minute=4, daily_budget=18, wait_seconds=1)
    runner = AiRunner(slow, db.info["factory"], limits, [(backup, limits)], chain_seconds=5)

    assert runner.run(_request(), _no_problems, NOW).value == Echo(word="in time")
