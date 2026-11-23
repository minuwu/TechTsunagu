"""Chat rules (WS12, XREQ-005, GLOSSARY "Chat"). Pure: no database, no framework."""

from collections.abc import Sequence
from typing import Literal

WakattaStatus = Literal["green", "orange", "red"]
FollowUpStatus = Literal["open", "clarified", "understood"]
NOT_SURE = -1

_NEXT: dict[FollowUpStatus, frozenset[FollowUpStatus]] = {
    "open": frozenset({"clarified", "understood"}),
    "clarified": frozenset({"understood"}),
    "understood": frozenset(),
}


def missed(correct: Sequence[int], answers: Sequence[int]) -> list[int]:
    """Positions answered wrong or "not sure" (-1). A malformed list is an error, never a wrong answer."""
    if len(answers) != len(correct) or any(a not in (NOT_SURE, 0, 1, 2) for a in answers):
        raise ValueError("answers must give one option index (0-2) or -1 per question")
    return [i for i, (want, got) in enumerate(zip(correct, answers, strict=True)) if want != got]


def wakatta_status(correct: Sequence[int], answers: Sequence[int]) -> WakattaStatus:
    """All right: green. None right: red. Anything between: orange."""
    wrong = len(missed(correct, answers))
    if wrong == 0:
        return "green"
    return "red" if wrong == len(correct) else "orange"


def can_move(current: FollowUpStatus, new: FollowUpStatus) -> bool:
    """Follow-ups only move forward: open → clarified → understood (or open → understood)."""
    return new in _NEXT[current]
