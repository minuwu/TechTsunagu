"""Learning History streak and the understanding trend window (XREQ-020). Pure: days in, numbers out."""

from collections.abc import Set
from datetime import date, timedelta


def streak_days(good_days: Set[date], today: date) -> int:
    """Days in a row with a good result, ending today; a run that ends yesterday still counts until today is over."""
    day = today if today in good_days else today - timedelta(days=1)
    count = 0
    while day in good_days:
        count += 1
        day -= timedelta(days=1)
    return count


def day_window(today: date, days: int) -> list[date]:
    """The last `days` days, oldest first, ending today."""
    return [today - timedelta(days=n) for n in range(days - 1, -1, -1)]
