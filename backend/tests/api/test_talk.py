"""XREQ-018 (ADR 0014): Talk Threads. What someone said → plain Japanese, the meaning, dialect words and replies
(explain_utterance); the owner's understanding graded (grade_understanding); private to the owner."""

import uuid
from dataclasses import dataclass
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from tests import factories as f

Json = dict[str, Any]
SAID = "膝のいたかけん、今日は散歩はせんばい。"
EXPLAINED: Json = {
    "transcript": SAID,
    "plain_ja": "膝が痛いので、今日は散歩をしません。",
    "meaning": "My knee hurts, so I will not go for a walk today.",
    "terms": [
        {"dialect": "いたかけん", "plain": "痛いので", "meaning": "because it hurts"},
        {"dialect": "せんばい", "plain": "しません", "meaning": "will not do"},
    ],
    "reply_options": [
        {
            "ja": "では、今日は休みましょう。職員を呼びましょうか？",
            "meaning": "Then let's rest. Shall I call the staff?",
        },
        {"ja": "わかりました。お茶はいかがですか？", "meaning": "I see. Would you like some tea?"},
    ],
}


@dataclass
class People:
    rahim: dict[str, str]
    sadia: dict[str, str]  # another Worker of the same shop
    foreman: dict[str, str]


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


@pytest.fixture
def people(client: TestClient, db: Session) -> People:
    s = f.shop(db)
    rahim, sadia = (f.user(db, s, pin="2468", must_change=False) for _ in range(2))
    rahim.language = "en"
    db.flush()
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    return People(
        _login(client, rahim.id, "2468"), _login(client, sadia.id, "2468"), _login(client, foreman.id, "739215")
    )


def _thread(client: TestClient, who: dict[str, str], title: str = "Yamada-san (care home)") -> Json:
    res = client.post("/api/talk-threads", headers=who, json={"title": title})
    assert res.status_code == 201, res.text
    thread: Json = res.json()["data"]
    return thread


def _entry(client: TestClient, who: dict[str, str], thread_id: str, text: str = SAID) -> Json:
    res = client.post(f"/api/talk-threads/{thread_id}/entries", headers=who, data={"text": text})
    assert res.status_code == 201, res.text
    entry: Json = res.json()["data"]
    return entry


def test_an_entry_is_explained_graded_answered_and_confirmed(
    client: TestClient, people: People, fake_ai: FakeAiClient
) -> None:
    thread = _thread(client, people.rahim)
    fake_ai.queue("explain_utterance", EXPLAINED)
    entry = _entry(client, people.rahim, thread["id"])
    assert entry["transcript"] == SAID and entry["plainJa"] == EXPLAINED["plain_ja"]
    assert entry["meaning"].startswith("My knee hurts") and entry["needsEdit"] is False
    assert [t["dialect"] for t in entry["terms"]] == ["いたかけん", "せんばい"]
    assert entry["needsStaff"] is True  # pain: a fixed rule, not the model
    assert len(entry["replyOptions"]) == 2 and entry["grade"] is None

    url = f"/api/talk-threads/{thread['id']}/entries/{entry['id']}"
    fake_ai.queue("grade_understanding", {"grade": "orange"})
    graded = client.patch(url, headers=people.rahim, json={"ownText": "Her knee hurts"}).json()["data"]
    assert graded["ownText"] == "Her knee hurts" and graded["grade"] == "orange"

    reply = entry["replyOptions"][0]["ja"]
    replied = client.patch(url, headers=people.rahim, json={"replyJa": reply}).json()["data"]
    assert replied["replyJa"] == reply and replied["confirmed"] is None
    done = client.patch(url, headers=people.rahim, json={"confirmed": True}).json()["data"]
    assert done["confirmed"] is True

    [listed] = client.get("/api/talk-threads", headers=people.rahim).json()["data"]["items"]
    assert listed["entryCount"] == 1 and listed["title"] == "Yamada-san (care home)"
    [stored] = client.get(f"/api/talk-threads/{thread['id']}/entries", headers=people.rahim).json()["data"]["items"]
    assert stored["grade"] == "orange" and stored["confirmed"] is True


def test_without_the_model_the_entry_keeps_the_transcript(client: TestClient, people: People) -> None:
    thread = _thread(client, people.rahim)
    entry = _entry(client, people.rahim, thread["id"], "お茶ばもう一杯ちょうだい。")
    assert entry["needsEdit"] is True and entry["transcript"] == "お茶ばもう一杯ちょうだい。"
    assert entry["meaning"] is None and entry["terms"] == [] and entry["needsStaff"] is False
    url = f"/api/talk-threads/{thread['id']}/entries/{entry['id']}"
    graded = client.patch(url, headers=people.rahim, json={"ownText": "More tea"})
    assert graded.status_code == 200 and graded.json()["data"]["grade"] is None  # nothing to compare with


def test_an_invented_dialect_word_is_retried_then_degraded(
    client: TestClient, people: People, fake_ai: FakeAiClient
) -> None:
    thread = _thread(client, people.rahim)
    bad = EXPLAINED | {"terms": [{"dialect": "ばってん", "plain": "でも", "meaning": "but"}]}  # not in what was said
    fake_ai.queue("explain_utterance", bad)
    fake_ai.queue("explain_utterance", EXPLAINED)
    entry = _entry(client, people.rahim, thread["id"])
    assert entry["needsEdit"] is False and [t["dialect"] for t in entry["terms"]] == ["いたかけん", "せんばい"]


def test_talk_threads_are_private_to_their_owner(client: TestClient, people: People, fake_ai: FakeAiClient) -> None:
    thread = _thread(client, people.rahim)
    fake_ai.queue("explain_utterance", EXPLAINED)
    entry = _entry(client, people.rahim, thread["id"])
    for other in (people.sadia, people.foreman):
        assert client.get("/api/talk-threads", headers=other).json()["data"]["items"] == []
        assert client.get(f"/api/talk-threads/{thread['id']}/entries", headers=other).status_code == 404
        assert client.patch(f"/api/talk-threads/{thread['id']}", headers=other, json={"title": "x"}).status_code == 404
        url = f"/api/talk-threads/{thread['id']}/entries/{entry['id']}"
        assert client.patch(url, headers=other, json={"confirmed": True}).status_code == 404
        assert client.delete(url, headers=other).status_code == 404


def test_rename_delete_and_validation(client: TestClient, people: People, fake_ai: FakeAiClient) -> None:
    key = {"Idempotency-Key": str(uuid.uuid4())}
    first = client.post("/api/talk-threads", headers=people.rahim | key, json={"title": "Elderly 1"}).json()["data"]
    again = client.post("/api/talk-threads", headers=people.rahim | key, json={"title": "Elderly 1"}).json()["data"]
    assert again["id"] == first["id"]  # a retried create makes one thread
    assert client.post("/api/talk-threads", headers=people.rahim, json={"title": "x" * 41}).status_code == 422

    renamed = client.patch(f"/api/talk-threads/{first['id']}", headers=people.rahim, json={"title": "Mori-san"})
    assert renamed.json()["data"]["title"] == "Mori-san"

    fake_ai.queue("explain_utterance", EXPLAINED)
    entry = _entry(client, people.rahim, first["id"])
    url = f"/api/talk-threads/{first['id']}/entries/{entry['id']}"
    assert client.patch(url, headers=people.rahim, json={}).status_code == 422
    two = client.patch(url, headers=people.rahim, json={"replyJa": "はい", "confirmed": True})
    assert two.status_code == 422  # exactly one change per PATCH
    both = client.post(f"/api/talk-threads/{first['id']}/entries", headers=people.rahim, data={"text": " "}, files={})
    assert both.status_code == 422  # neither text nor a recording

    assert client.delete(f"/api/talk-threads/{first['id']}", headers=people.rahim).status_code == 200
    assert client.get(f"/api/talk-threads/{first['id']}/entries", headers=people.rahim).status_code == 404
