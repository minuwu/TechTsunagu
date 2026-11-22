from datetime import UTC, datetime, timedelta

import pytest

from app.domain.skills import SignOffFact, current_shift, grade, panel_state, skill_status

NOW = datetime(2026, 11, 24, 3, 0, tzinfo=UTC)


def _passed(days_ago: int, version_no: int = 1) -> SignOffFact:
    return SignOffFact(version_no=version_no, passed_at=NOW - timedelta(days=days_ago))


@pytest.mark.parametrize(
    ("days_ago", "status"), [(0, "valid"), (75, "valid"), (76, "expiring"), (89, "expiring"), (90, "expired")]
)
def test_a_sign_off_is_valid_for_90_days_and_expiring_in_its_last_14(days_ago: int, status: str) -> None:
    assert skill_status(_passed(days_ago), recheck_version_no=1, now=NOW) == status


def test_no_sign_off_is_none() -> None:
    assert skill_status(None, recheck_version_no=1, now=NOW) == "none"


def test_a_newer_safety_change_expires_older_sign_offs_but_a_wording_change_does_not() -> None:
    passed_v1 = _passed(1, version_no=1)

    assert skill_status(passed_v1, recheck_version_no=2, now=NOW) == "expired"  # v2 was a safety change
    assert skill_status(passed_v1, recheck_version_no=1, now=NOW) == "valid"  # v2 was wording only
    assert skill_status(_passed(1, version_no=3), recheck_version_no=2, now=NOW) == "valid"


@pytest.mark.parametrize(
    ("has_note", "status", "override", "expected"),
    [
        (False, "valid", False, ("locked", "no_note")),
        (True, "none", False, ("locked", "no_sign_off")),
        (True, "expired", False, ("locked", "expired")),
        (True, "expiring", False, ("ready", "expiring")),
        (True, "valid", False, ("ready", None)),
        (True, "none", True, ("override", "override")),
    ],
)
def test_the_panel_state_follows_the_sign_off(
    has_note: bool, status: str, override: bool, expected: tuple[str, str | None]
) -> None:
    assert panel_state(has_note, status, override) == expected  # pyright: ignore[reportArgumentType]


def test_grading_returns_the_wrong_question_positions() -> None:
    assert grade([0, 2, 1], [0, 2, 1]) == []
    assert grade([0, 2, 1], [1, 2, 0]) == [0, 2]


@pytest.mark.parametrize("answers", [[0, 2], [0, 2, 1, 0], [0, 3, 1], [0, -1, 1]])
def test_grading_refuses_answers_of_the_wrong_length_or_range(answers: list[int]) -> None:
    with pytest.raises(ValueError, match="answers"):
        grade([0, 2, 1], answers)


@pytest.mark.parametrize(("hour", "shift"), [(5, "night"), (6, "day"), (17, "day"), (18, "night"), (23, "night")])
def test_the_shift_is_day_from_6_to_18_tokyo_time(hour: int, shift: str) -> None:
    tokyo_hour_in_utc = (hour - 9) % 24
    assert current_shift(datetime(2026, 11, 24, tokyo_hour_in_utc, 0, tzinfo=UTC), "Asia/Tokyo") == shift
