"""Chat rules: Wakatta Status, missed questions, Follow-up transitions (XREQ-005)."""

import pytest

from app.domain.chat import can_move, missed, wakatta_status


@pytest.mark.parametrize(
    ("answers", "status"),
    [([1, 0, 2], "green"), ([1, 2, 2], "orange"), ([-1, 0, 2], "orange"), ([0, 1, 0], "red"), ([-1, -1, -1], "red")],
)
def test_status_follows_how_many_are_right(answers: list[int], status: str) -> None:
    assert wakatta_status([1, 0, 2], answers) == status


def test_not_sure_counts_as_missed() -> None:
    assert missed([1, 0], [-1, 0]) == [0]


@pytest.mark.parametrize("answers", [[1], [1, 0, 2, 0], [3, 0, 0], [-2, 0, 0]])
def test_a_malformed_answer_list_is_an_error(answers: list[int]) -> None:
    with pytest.raises(ValueError, match="one option index"):
        missed([1, 0, 2], answers)


def test_follow_ups_only_move_forward() -> None:
    assert can_move("open", "clarified") and can_move("open", "understood") and can_move("clarified", "understood")
    assert not can_move("clarified", "open") and not can_move("understood", "clarified")
    assert not can_move("open", "open")
