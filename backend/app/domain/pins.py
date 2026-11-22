"""PIN rules (architecture §3.4, §12): digits only, 4 for Workers and 6 for Foremen and Posters, not guessable."""

from typing import Literal

PinProblem = Literal["length", "digits", "guessable"]
PIN_LENGTH = {"worker": 4, "foreman": 6, "poster": 6}
_ASCII_DIGITS = frozenset("0123456789")


def _is_run(pin: str) -> bool:
    steps = {int(b) - int(a) for a, b in zip(pin, pin[1:], strict=False)}
    return steps in ({0}, {1}, {-1})


def pin_problem(role: str, pin: str) -> PinProblem | None:
    """None if the PIN is acceptable for this role, else why not."""
    if not set(pin) <= _ASCII_DIGITS:
        return "digits"
    if len(pin) != PIN_LENGTH[role]:
        return "length"
    if _is_run(pin):
        return "guessable"
    return None
