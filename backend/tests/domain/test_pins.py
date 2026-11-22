import pytest

from app.domain.pins import PIN_LENGTH, pin_problem


@pytest.mark.parametrize(("role", "pin"), [("worker", "2468"), ("foreman", "739215"), ("poster", "481306")])
def test_a_pin_of_the_right_length_and_shape_is_accepted(role: str, pin: str) -> None:
    assert pin_problem(role, pin) is None


@pytest.mark.parametrize(
    ("role", "pin", "problem"),
    [
        ("worker", "123", "length"),
        ("worker", "24680", "length"),
        ("foreman", "2468", "length"),
        ("worker", "12a4", "digits"),
        ("worker", "７３９２", "digits"),
        ("worker", "0000", "guessable"),
        ("worker", "1234", "guessable"),
        ("foreman", "987654", "guessable"),
    ],
)
def test_a_weak_or_malformed_pin_is_refused(role: str, pin: str, problem: str) -> None:
    assert pin_problem(role, pin) == problem


def test_pin_lengths_follow_the_architecture() -> None:
    assert PIN_LENGTH == {"worker": 4, "foreman": 6, "poster": 6}
