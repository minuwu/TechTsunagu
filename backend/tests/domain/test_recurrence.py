from datetime import UTC, datetime, timedelta

import pytest

from app.domain.recurrence import TOKYO, InvalidEventError, occurrences_between, validate_event

# Monday 2026-11-23 08:00 in Japan, stored as UTC like a timestamptz column returns it.
MONDAY_8AM = datetime(2026, 11, 23, 8, 0, tzinfo=TOKYO).astimezone(UTC)
NOW = datetime(2026, 11, 22, 12, 0, tzinfo=UTC)


def jst(day: int, hour: int = 8) -> datetime:
    return datetime(2026, 11, day, hour, 0, tzinfo=TOKYO)


def test_weekly_rule_expands_on_japan_weekdays_at_japan_time() -> None:
    dates = occurrences_between(MONDAY_8AM, "FREQ=WEEKLY;BYDAY=MO,TH", NOW, NOW + timedelta(days=10))

    assert dates == [jst(23), jst(26), jst(30)]


def test_count_stops_the_rule() -> None:
    dates = occurrences_between(MONDAY_8AM, "FREQ=WEEKLY;BYDAY=MO,TH;COUNT=2", NOW, NOW + timedelta(days=30))

    assert dates == [jst(23), jst(26)]


def test_until_in_utc_stops_the_rule() -> None:
    dates = occurrences_between(
        MONDAY_8AM, "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261201T000000Z", NOW, NOW + timedelta(days=30)
    )

    assert dates == [jst(23), jst(30)]


def test_one_off_event_is_its_own_single_occurrence() -> None:
    assert occurrences_between(MONDAY_8AM, None, NOW, NOW + timedelta(days=2)) == [jst(23)]
    assert occurrences_between(MONDAY_8AM, None, NOW + timedelta(days=2), NOW + timedelta(days=9)) == []


@pytest.mark.parametrize(
    "rrule",
    [
        "",
        "garbage",
        "FREQ=DAILY",
        "FREQ=WEEKLY",
        "FREQ=WEEKLY;BYDAY=XX",
        "FREQ=WEEKLY;BYDAY=1MO",
        "FREQ=WEEKLY;BYDAY=mo",
        "RRULE:FREQ=WEEKLY;BYDAY=MO",
        "FREQ=WEEKLY;BYDAY=MO;INTERVAL=2",
        "FREQ=WEEKLY;BYDAY=MO;BYDAY=TU",
        "FREQ=WEEKLY;BYDAY=MO;COUNT=0",
        "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261231",
        "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261399T000000Z",
        "FREQ=WEEKLY;BYDAY=MO;COUNT=2;UNTIL=20261231T000000Z",
        "FREQ=WEEKLY;BYDAY=" + ",".join(["MO"] * 70),
    ],
)
def test_rules_the_frontend_cannot_expand_are_rejected(rrule: str) -> None:
    with pytest.raises(InvalidEventError) as error:
        validate_event(MONDAY_8AM, rrule, NOW)

    assert error.value.reason == "unsupported_rule"


def test_future_one_off_and_ongoing_weekly_rules_are_valid() -> None:
    validate_event(MONDAY_8AM, None, NOW)
    validate_event(MONDAY_8AM - timedelta(days=60), "FREQ=WEEKLY;BYDAY=MO,TH", NOW)


def test_past_one_off_event_is_rejected() -> None:
    with pytest.raises(InvalidEventError) as error:
        validate_event(NOW - timedelta(hours=1), None, NOW)

    assert error.value.reason == "past"


@pytest.mark.parametrize(
    ("starts_at", "rrule"),
    [
        (NOW + timedelta(days=367), None),
        (MONDAY_8AM - timedelta(days=60), "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261101T000000Z"),
        (NOW + timedelta(days=400), "FREQ=WEEKLY;BYDAY=MO"),
    ],
)
def test_event_with_no_date_in_the_next_366_days_is_rejected(starts_at: datetime, rrule: str | None) -> None:
    with pytest.raises(InvalidEventError) as error:
        validate_event(starts_at, rrule, NOW)

    assert error.value.reason == "no_upcoming"
