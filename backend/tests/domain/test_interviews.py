"""Periodic Interview due rule: every 3 months, "soon" in the last 14 days, "overdue" after the date."""

from datetime import date

import pytest

from app.domain.interviews import due_status, next_due


def test_due_three_months_after_the_last_interview_or_the_start() -> None:
    assert next_due(date(2026, 8, 31), date(2026, 1, 1)) == date(2026, 11, 30)  # month end stays valid
    assert next_due(None, date(2026, 9, 10)) == date(2026, 12, 10)


@pytest.mark.parametrize(
    ("today", "status"),
    [
        (date(2026, 11, 1), "ok"),
        (date(2026, 11, 16), "soon"),
        (date(2026, 11, 30), "soon"),
        (date(2026, 12, 1), "overdue"),
    ],
)
def test_status_turns_soon_two_weeks_before_and_overdue_after(today: date, status: str) -> None:
    assert due_status(date(2026, 11, 30), today) == status
