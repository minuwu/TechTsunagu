"""Periodic Interview rule (support item 10, research.md §1): the 登録支援機関 meets each 特定技能1号 Worker at least
once every 3 months. Pure: stdlib and dateutil only."""

from datetime import date, timedelta
from typing import Literal

from dateutil.relativedelta import relativedelta

DueStatus = Literal["ok", "soon", "overdue"]
EVERY = relativedelta(months=3)
SOON = timedelta(days=14)  # the Foreman sees "soon" two weeks before the date


def next_due(last_held: date | None, since: date) -> date:
    """3 months after the last interview, or after `since` (the Worker's start) when there was none."""
    return (last_held or since) + EVERY


def due_status(due_on: date, today: date) -> DueStatus:
    if today > due_on:
        return "overdue"
    return "soon" if due_on - today <= SOON else "ok"
