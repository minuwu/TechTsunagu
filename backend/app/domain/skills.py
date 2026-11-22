"""Sign-off validity, Lock state, grading and shifts (architecture §7.1–7.3). Pure: no I/O, no clock of its own."""

from collections.abc import Sequence
from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Literal
from zoneinfo import ZoneInfo

SIGN_OFF_DAYS = 90
EXPIRING_DAYS = 14
Status = Literal["valid", "expiring", "expired", "none"]
PanelState = Literal["idle", "locked", "ready", "override"]
Reason = Literal["no_note", "no_sign_off", "expired", "expiring", "override"] | None


@dataclass(frozen=True)
class SignOffFact:
    version_no: int
    passed_at: datetime


def skill_status(newest: SignOffFact | None, recheck_version_no: int | None, now: datetime) -> Status:
    """Valid while it covers the newest Safety Change and is under 90 days old; 'expiring' in its last 14 days."""
    if newest is None:
        return "none"
    age = now - newest.passed_at
    if (recheck_version_no is not None and newest.version_no < recheck_version_no) or age >= timedelta(SIGN_OFF_DAYS):
        return "expired"
    if age >= timedelta(SIGN_OFF_DAYS - EXPIRING_DAYS):  # 14 days or less left
        return "expiring"
    return "valid"


def panel_state(has_note: bool, status: Status, override: bool) -> tuple[PanelState, Reason]:
    if override:
        return "override", "override"
    if not has_note:
        return "locked", "no_note"
    if status == "valid":
        return "ready", None
    if status == "expiring":
        return "ready", "expiring"
    return "locked", "no_sign_off" if status == "none" else "expired"


def grade(correct: Sequence[int], answers: Sequence[int]) -> list[int]:
    """Positions of the wrong answers. A malformed answer list is an error, never a wrong answer (§7.3)."""
    if len(answers) != len(correct) or any(a not in (0, 1, 2) for a in answers):
        raise ValueError("answers must give one option index (0-2) per question")
    return [i for i, (want, got) in enumerate(zip(correct, answers, strict=True)) if want != got]


def current_shift(now: datetime, timezone: str) -> Literal["day", "night"]:
    """Day shift 06:00–17:59 shop time, night otherwise."""
    hour = now.astimezone(ZoneInfo(timezone)).hour
    return "day" if 6 <= hour < 18 else "night"
