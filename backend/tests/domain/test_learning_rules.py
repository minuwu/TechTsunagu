"""Learning streak and the understanding trend (XREQ-020): pure rules, no database."""

from datetime import date

from app.domain.learning import day_window, streak_days

TODAY = date(2026, 11, 24)


def test_no_good_days_is_no_streak() -> None:
    assert streak_days(set(), TODAY) == 0


def test_the_streak_counts_back_from_today() -> None:
    days = {date(2026, 11, 24), date(2026, 11, 23), date(2026, 11, 22), date(2026, 11, 20)}
    assert streak_days(days, TODAY) == 3


def test_a_streak_ending_yesterday_still_counts_before_today_is_done() -> None:
    assert streak_days({date(2026, 11, 23), date(2026, 11, 22)}, TODAY) == 2


def test_a_gap_of_a_whole_day_ends_the_streak() -> None:
    assert streak_days({date(2026, 11, 22)}, TODAY) == 0


def test_the_window_lists_every_day_oldest_first_ending_today() -> None:
    assert day_window(TODAY, 3) == [date(2026, 11, 22), date(2026, 11, 23), date(2026, 11, 24)]
