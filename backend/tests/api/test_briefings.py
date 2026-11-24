"""WS6 Briefings (朝礼): record → AI draft → publish → Workers open and answer → the Foreman sees who understood."""

import uuid
from dataclasses import dataclass
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from tests import factories as f

Json = dict[str, Any]
PLACEHOLDER = "（ここを入力してください）"


def _tri(ja: str, en: str) -> dict[str, str]:
    return {"ja": ja, "en": en}


DRAFT: Json = {
    "transcript_ja": "今日15時から天井クレーンの点検です。クレーンは使えません。",
    "text": _tri("今日15時から16時まで、クレーンは使えません。", "No crane from 15:00 to 16:00 today."),
    "question": {
        "term_ja": None,
        "prompt": _tri("クレーンが使えないのは?", "When is the crane off?"),
        "options": [_tri("15時〜16時", "15:00–16:00"), _tri("9時〜10時", "9:00–10:00"), _tri("一日中", "All day")],
        "correct_index": 0,
    },
}


@dataclass
class Shop:
    foreman: dict[str, str]
    workers: list[dict[str, str]]
    worker_ids: list[int]
    outsider: dict[str, str]


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _key() -> dict[str, str]:
    return {"Idempotency-Key": str(uuid.uuid4())}


@pytest.fixture
def shop(client: TestClient, db: Session) -> Shop:
    s, other = f.shop(db), f.shop(db)
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    workers = [f.user(db, s, pin="2468", must_change=False) for _ in range(3)]
    outsider = f.user(db, other, pin="2468", must_change=False)
    return Shop(
        _login(client, foreman.id, "739215"),
        [_login(client, w.id, "2468") for w in workers],
        [w.id for w in workers],
        _login(client, outsider.id, "2468"),
    )


def _draft(client: TestClient, shop: Shop, fake_ai: FakeAiClient, key: dict[str, str] | None = None) -> Json:
    fake_ai.queue("build_briefing", DRAFT)
    res = client.post(
        "/api/briefings/draft", headers=shop.foreman | (key or _key()), data={"text": DRAFT["transcript_ja"]}
    )
    assert res.status_code == 201, res.text
    return res.json()["data"]


def _published(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> Json:
    draft = _draft(client, shop, fake_ai)
    body = {"id": draft["id"], "text": draft["text"], "question": draft["question"], "createdAt": draft["createdAt"]}
    res = client.post("/api/briefings", headers=shop.foreman, json=body)
    assert res.status_code == 200, res.text
    return res.json()["data"]


def test_the_ai_draft_comes_back_for_review(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    draft = _draft(client, shop, fake_ai)

    assert draft["status"] == "draft"
    assert draft["needsEdit"] is False
    assert draft["text"]["en"] == "No crane from 15:00 to 16:00 today."
    assert draft["question"]["id"] == "q1"
    assert draft["question"]["correctIndex"] == 0


def test_the_same_key_returns_the_same_draft_with_one_ai_call(
    client: TestClient, shop: Shop, fake_ai: FakeAiClient
) -> None:
    key = _key()
    first = _draft(client, shop, fake_ai, key)
    again = client.post("/api/briefings/draft", headers=shop.foreman | key, data={"text": "x"})

    assert again.json()["data"]["id"] == first["id"]
    assert fake_ai.calls == 1


def test_without_ai_the_draft_must_be_finished_before_publishing(
    client: TestClient, shop: Shop, fake_ai: FakeAiClient
) -> None:
    res = client.post("/api/briefings/draft", headers=shop.foreman | _key(), data={"text": "15時から点検"})
    draft = res.json()["data"]
    assert res.status_code == 201
    assert draft["needsEdit"] is True
    assert draft["text"]["ja"] == "15時から点検"
    assert draft["text"]["en"] == PLACEHOLDER

    body = {"id": draft["id"], "text": draft["text"], "question": draft["question"]}
    refused = client.post("/api/briefings", headers=shop.foreman, json=body)
    assert (refused.status_code, refused.json()["error"]["code"]) == (409, "NEEDS_EDIT")

    body |= {"text": DRAFT["text"], "question": _published_question()}
    assert client.post("/api/briefings", headers=shop.foreman, json=body).status_code == 200


def _published_question() -> Json:
    q = DRAFT["question"]
    return {"id": "q1", "termId": None, "prompt": q["prompt"], "options": q["options"], "correctIndex": 0}


def test_the_foreman_edits_are_what_is_published_and_only_once(
    client: TestClient, shop: Shop, fake_ai: FakeAiClient
) -> None:
    draft = _draft(client, shop, fake_ai)
    edited = draft["text"] | {"en": "Crane inspection: no crane 15:00–16:00."}
    body = {"id": draft["id"], "text": edited, "question": draft["question"]}

    res = client.post("/api/briefings", headers=shop.foreman, json=body)
    again = client.post("/api/briefings", headers=shop.foreman, json=body)

    assert res.json()["data"]["text"]["en"] == "Crane inspection: no crane 15:00–16:00."
    assert res.json()["data"]["status"] == "published"
    assert again.status_code == 409


def test_workers_see_published_briefings_without_the_answer(
    client: TestClient, shop: Shop, fake_ai: FakeAiClient
) -> None:
    _draft(client, shop, fake_ai)  # a draft Workers must not see
    published = _published(client, shop, fake_ai)

    items = client.get("/api/briefings", headers=shop.workers[0]).json()["data"]["items"]

    assert [b["id"] for b in items] == [published["id"]]
    assert "correctIndex" not in items[0]["question"]
    assert (items[0]["opened"], items[0]["passed"]) == (False, None)


def test_receipts_put_every_worker_in_one_group(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    b = _published(client, shop, fake_ai)
    target = {"kind": "briefing", "id": b["id"]}
    passed, opened = shop.workers[0], shop.workers[1]
    for w in (passed, opened):
        assert client.post("/api/receipts", headers=w, json={"target": target}).status_code == 200
    check = {"target": target, "language": "en", "answers": [0]}
    graded = client.post("/api/checks", headers=passed | _key(), json=check)
    assert graded.json()["data"]["passed"] is True

    groups = client.get(f"/api/briefings/{b['id']}/receipts", headers=shop.foreman).json()["data"]

    ids = [str(i) for i in shop.worker_ids]
    assert [w["id"] for w in groups["understood"]] == [ids[0]]
    assert [w["id"] for w in groups["openedOnly"]] == [ids[1]]
    assert [w["id"] for w in groups["notOpened"]] == [ids[2]]


def test_a_wrong_answer_keeps_the_worker_in_opened_only(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    b = _published(client, shop, fake_ai)
    check = {"target": {"kind": "briefing", "id": b["id"]}, "language": "en", "answers": [2]}

    res = client.post("/api/checks", headers=shop.workers[0] | _key(), json=check)
    groups = client.get(f"/api/briefings/{b['id']}/receipts", headers=shop.foreman).json()["data"]

    assert res.json()["data"]["passed"] is False
    assert [w["id"] for w in groups["openedOnly"]] == [str(shop.worker_ids[0])]


def test_only_the_foreman_drafts_and_other_shops_see_nothing(
    client: TestClient, shop: Shop, fake_ai: FakeAiClient
) -> None:
    b = _published(client, shop, fake_ai)
    target = {"kind": "briefing", "id": b["id"]}

    worker_draft = client.post("/api/briefings/draft", headers=shop.workers[0] | _key(), data={"text": "x"})
    outsider_open = client.post("/api/receipts", headers=shop.outsider, json={"target": target})
    outsider_list = client.get("/api/briefings", headers=shop.outsider).json()["data"]["items"]

    assert worker_draft.status_code == 403
    assert outsider_open.status_code == 404
    assert outsider_list == []


def test_an_english_only_shop_gets_briefings_without_bengali(
    client: TestClient, db: Session, fake_ai: FakeAiClient
) -> None:
    s = f.shop(db)
    s.languages = ["en"]  # XREQ-011 item 5: the AI writes ja and the shop's languages only
    db.flush()
    foreman = _login(client, f.user(db, s, role="foreman", pin="739215", must_change=False).id, "739215")
    question = DRAFT["question"] | {
        "prompt": {"ja": "クレーンが使えないのは?", "en": "When is the crane off?"},
        "options": [
            {"ja": a, "en": b} for a, b in (("15時〜16時", "15:00–16:00"), ("9時", "9:00"), ("一日中", "All day"))
        ],
    }
    fake_ai.queue("build_briefing", DRAFT | {"text": {"ja": DRAFT["text"]["ja"]}, "question": question})  # en missing
    fake_ai.queue("build_briefing", DRAFT | {"text": DRAFT["text"], "question": question})  # the retry adds it

    res = client.post("/api/briefings/draft", headers=foreman | _key(), data={"text": "15時から点検"})
    draft = res.json()["data"]

    assert res.status_code == 201, res.text
    assert draft["needsEdit"] is False
    assert draft["text"] == DRAFT["text"]
    assert set(draft["question"]["prompt"]) == {"ja", "en"}
    assert "Write every text in en" in fake_ai.last_prompt_text
