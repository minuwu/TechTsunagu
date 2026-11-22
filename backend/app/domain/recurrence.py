"""Event recurrence rules (architecture §7.5). Pure: stdlib and dateutil only."""

import re
from datetime import datetime, timedelta
from typing import Literal
from zoneinfo import ZoneInfo

from dateutil.rrule import rrule, rrulestr

# Districts are in Japan; Japan has no daylight saving time, so weekly expansion is exact.
TOKYO = ZoneInfo("Asia/Tokyo")
HORIZON = timedelta(days=366)
MAX_RRULE_LENGTH = 200  # matches the events.rrule column check

_KEYS = frozenset({"FREQ", "BYDAY", "UNTIL", "COUNT"})
_DAYS = frozenset({"MO", "TU", "WE", "TH", "FR", "SA", "SU"})
_UNTIL = re.compile(r"\d{8}T\d{6}Z")
_COUNT = re.compile(r"[1-9]\d{0,3}")

Reason = Literal["unsupported_rule", "past", "no_upcoming"]


class InvalidEventError(ValueError):
    def __init__(self, reason: Reason) -> None:
        super().__init__(reason)
        self.reason: Reason = reason


def occurrences_between(starts_at: datetime, rule: str | None, start: datetime, end: datetime) -> list[datetime]:
    """Occurrences in [start, end] as Japan-time datetimes; a one-off Event is its own single occurrence."""
    if rule is None:
        local = starts_at.astimezone(TOKYO)
        return [local] if start <= local <= end else []
    return list(_parse(rule, starts_at).between(start, end, inc=True))


def validate_event(starts_at: datetime, rule: str | None, now: datetime) -> None:
    """Raise InvalidEventError unless the Event can be published: a supported rule with a date within 366 days."""
    if rule is None and starts_at < now:
        raise InvalidEventError("past")
    if not occurrences_between(starts_at, rule, now, now + HORIZON):
        raise InvalidEventError("no_upcoming")


def _parse(rule: str, starts_at: datetime) -> rrule:
    # dateutil also accepts forms the frontend cannot expand (1MO, lowercase days, an RRULE: prefix,
    # COUNT with UNTIL), so the shape is checked here first.
    pairs = [part.partition("=") for part in rule.split(";")]
    fields = {key: value for key, _, value in pairs}
    supported = (
        len(rule) <= MAX_RRULE_LENGTH
        and len(fields) == len(pairs)
        and fields.keys() <= _KEYS
        and fields.get("FREQ") == "WEEKLY"
        and set(fields.get("BYDAY", "").split(",")) <= _DAYS
        and not ("UNTIL" in fields and "COUNT" in fields)
        and _COUNT.fullmatch(fields.get("COUNT", "1")) is not None
        and _UNTIL.fullmatch(fields.get("UNTIL", "20000101T000000Z")) is not None
    )
    if not supported:
        raise InvalidEventError("unsupported_rule")
    try:
        parsed = rrulestr(rule, dtstart=starts_at.astimezone(TOKYO))
    except ValueError as error:  # e.g. UNTIL=20261399T000000Z has the right shape but no such date
        raise InvalidEventError("unsupported_rule") from error
    if not isinstance(parsed, rrule):  # a single RRULE line never parses to an rruleset
        raise InvalidEventError("unsupported_rule")
    return parsed
