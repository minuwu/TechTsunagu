"""The chatbot (POST /api/knowledge/asks): answers only from cited sources, then one self-check; never a 500."""

from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from tests import factories as f

GARBAGE_PAGE = "isa-guide:p142:1"  # the guide's garbage rules, which the search finds for this question
QUESTION = "How do I throw away garbage?"


def _tri(ja: str, en: str) -> dict[str, str]:
    return {"ja": ja, "bn": f"{en} (bn)", "en": en}


ANSWER: dict[str, Any] = {
    "covered": True,
    "answer": _tri("きめられた日と場所に、分けて出します。", "Sort it and put it out at the set place on the set day."),
    "source_ids": [GARBAGE_PAGE],
    "check": {
        "term_ja": None,
        "prompt": _tri("ごみはいつ出しますか?", "When do you put out garbage?"),
        "options": [
            _tri("きめられた日", "On the set day"),
            _tri("いつでも", "Any time"),
            _tri("夜だけ", "Only at night"),
        ],
        "correct_index": 0,
    },
}


@pytest.fixture
def worker(client: TestClient, db: Session) -> dict[str, str]:
    s = f.shop(db)
    user = f.user(db, s, pin="2468", must_change=False)
    res = client.post("/api/auth/login", json={"userId": str(user.id), "pin": "2468"})
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _ask(client: TestClient, headers: dict[str, str], text: str) -> Any:
    res = client.post("/api/knowledge/asks", headers=headers, data={"text": text})
    assert res.status_code == 200, res.text
    return res.json()["data"]


def test_a_covered_answer_cites_the_guide_and_ends_with_a_self_check(
    client: TestClient, worker: dict[str, str], fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("answer_knowledge", ANSWER)

    data = _ask(client, worker, QUESTION)

    assert (data["covered"], data["emergency"]) == (True, False)
    assert data["answer"]["en"].startswith("Sort it")
    assert [s["id"] for s in data["sources"]] == [GARBAGE_PAGE]
    assert data["sources"][0]["kind"] == "life" and data["sources"][0]["page"] == 142
    assert "出入国在留管理庁" in data["sources"][0]["attribution"]
    assert data["check"]["correctIndex"] == 0


def test_a_source_that_was_not_sent_is_retried_then_not_covered(
    client: TestClient, worker: dict[str, str], fake_ai: FakeAiClient
) -> None:
    invented = ANSWER | {"source_ids": ["made-up:p1:1"]}
    fake_ai.queue("answer_knowledge", invented)
    fake_ai.queue("answer_knowledge", invented)

    data = _ask(client, worker, QUESTION)

    assert (data["covered"], data["answer"], data["sources"]) == (False, None, [])
    assert "source_ids must be ids from the sources list" in fake_ai.last_prompt_text


def test_an_emergency_gets_the_fixed_card_without_any_model_call(
    client: TestClient, worker: dict[str, str], fake_ai: FakeAiClient
) -> None:
    data = _ask(client, worker, "My friend is bleeding, what do I do?")

    assert data["emergency"] is True
    assert "119" in data["answer"]["en"]
    assert fake_ai.calls == 0


def test_no_source_and_no_model_means_not_covered_never_a_500(
    client: TestClient, worker: dict[str, str], fake_ai: FakeAiClient
) -> None:
    assert _ask(client, worker, "zzzz qqqq")["covered"] is False  # nothing found: no model call
    assert _ask(client, worker, QUESTION)["covered"] is False  # nothing scripted: the model is "down"
    assert fake_ai.calls == 1
