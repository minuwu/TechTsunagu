"""WS12 Chat loop (XREQ-005): draft → send (Tasks) → poll → answers (Wakatta Status, Follow-ups) → Re-explanation,
Nudge, Task Board; and the shop and Thread boundaries."""

import uuid
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from app.models.work import Confusion, GlossaryTerm
from tests import factories as f

Json = dict[str, Any]


def _tri(word: str) -> dict[str, str]:
    return {"ja": word, "bn": f"{word}-bn", "en": f"{word}-en"}


def _q(term: str | None, prompt: str, options: list[str]) -> Json:
    return {"term_ja": term, "prompt": _tri(prompt), "options": [_tri(o) for o in options], "correct_index": 0}


@dataclass
class Line:
    foreman: dict[str, str]
    rahim: dict[str, str]
    sadia: dict[str, str]
    outsider: dict[str, str]
    rahim_id: int
    sadia_id: int
    machine_id: int
    thread_id: int


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _key() -> dict[str, str]:
    return {"Idempotency-Key": str(uuid.uuid4())}


def _line(client: TestClient, db: Session) -> Line:
    s = f.shop(db)
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    rahim, sadia = (f.user(db, s, pin="2468", must_change=False) for _ in range(2))
    outsider = f.user(db, s, pin="2468", must_change=False)
    machine = f.machine(db, s, code="press-2")
    db.add(GlossaryTerm(shop_id=s.id, ja="金型", reading="かながた", translations={"bn": "ডাই", "en": "die"}))
    t = f.thread(db, s, foreman, [rahim, sadia])
    return Line(
        _login(client, foreman.id, "739215"),
        _login(client, rahim.id, "2468"),
        _login(client, sadia.id, "2468"),
        _login(client, outsider.id, "2468"),
        rahim.id,
        sadia.id,
        machine.id,
        t.id,
    )


@pytest.fixture
def line(client: TestClient, db: Session) -> Line:
    return _line(client, db)


def draft_answer(line: Line) -> Json:
    return {
        "transcript_ja": "明日の朝8時から、2号プレスの金型交換をします。",
        "text": _tri("明日8時に金型交換"),
        "questions": [
            _q("金型", "何を交換しますか?", ["金型", "電源", "手袋"]),
            _q(None, "何時?", ["8時", "9時", "10時"]),
        ],
        "tasks": [
            {
                "title": _tri("金型交換"),
                "assignee_ids": [str(line.rahim_id)],
                "due_at": "2026-11-24T08:00:00+09:00",
                "machine_id": str(line.machine_id),
            }
        ],
    }


def _sent(client: TestClient, line: Line, fake_ai: FakeAiClient) -> Json:
    fake_ai.queue("draft_message", draft_answer(line))
    res = client.post(
        f"/api/threads/{line.thread_id}/messages", headers=line.foreman | _key(), data={"text": "金型交換"}
    )
    assert res.status_code == 201, res.text
    draft: Json = res.json()["data"]
    body = {"text": draft["text"], "questions": draft["questions"], "tasks": draft["taskDrafts"]}
    res = client.post(f"/api/messages/{draft['id']}/send", headers=line.foreman, json=body)
    assert res.status_code == 200, res.text
    sent: Json = res.json()["data"]
    return sent


def _poll(client: TestClient, line: Line, who: dict[str, str]) -> list[Json]:
    res = client.get(f"/api/threads/{line.thread_id}/messages", headers=who)
    assert res.status_code == 200, res.text
    items: list[Json] = res.json()["data"]["items"]
    return items


def test_a_foreman_message_goes_from_draft_to_tasks_and_answers(
    client: TestClient, db: Session, line: Line, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("draft_message", draft_answer(line))
    res = client.post(
        f"/api/threads/{line.thread_id}/messages", headers=line.foreman | _key(), data={"text": "金型交換"}
    )
    draft = res.json()["data"]
    assert draft["stage"] == 3 and draft["needsEdit"] is False and draft["questions"][0]["correctIndex"] == 0
    assert draft["taskDrafts"][0]["assigneeIds"] == [str(line.rahim_id)]
    assert _poll(client, line, line.rahim) == []  # a draft stays with its sender

    body = {"text": draft["text"], "questions": draft["questions"], "tasks": draft["taskDrafts"]}
    sent = client.post(f"/api/messages/{draft['id']}/send", headers=line.foreman, json=body).json()["data"]
    assert sent["stage"] == 4
    assert client.post(f"/api/messages/{draft['id']}/send", headers=line.foreman, json=body).status_code == 409
    [task] = client.get("/api/tasks", headers=line.rahim).json()["data"]["items"]
    assert task["column"] == "todo" and task["assigneeIds"] == [str(line.rahim_id)]
    assert task["fromMessageId"] == draft["id"] and task["machineId"] == str(line.machine_id)

    [seen] = _poll(client, line, line.rahim)
    assert all("correctIndex" not in q for q in seen["questions"]) and seen["myStatus"] is None
    result = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [1, 0]}).json()
    assert result["data"]["status"] == "orange" and len(result["data"]["followUpIds"]) == 1
    again = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [0, 0]})
    assert again.json()["data"] == result["data"]  # the first answer counts

    [follow] = client.get("/api/follow-ups?status=open", headers=line.foreman).json()["data"]["items"]
    assert follow["termJa"] == "金型" and follow["userId"] == str(line.rahim_id)
    confusion = db.scalar(select(Confusion).where(Confusion.message_id == int(sent["id"])))
    assert confusion is not None and confusion.machine_id is None
    [seen] = _poll(client, line, line.rahim)
    assert seen["myStatus"] == "orange"


def test_without_the_model_the_foreman_finishes_the_draft(client: TestClient, line: Line) -> None:
    res = client.post(f"/api/threads/{line.thread_id}/messages", headers=line.foreman | _key(), data={"text": "清掃"})
    draft = res.json()["data"]

    assert res.status_code == 201 and draft["needsEdit"] is True and draft["stage"] == 0
    assert draft["questions"] == [] and draft["taskDrafts"] == []
    sent = client.post(f"/api/messages/{draft['id']}/send", headers=line.foreman, json={"text": _tri("清掃")})
    assert sent.status_code == 200 and sent.json()["data"]["needsEdit"] is False


def test_a_reexplanation_reaches_one_worker_and_closes_the_follow_up(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)
    client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [2, 0]})
    fake_ai.queue("reexplain", {"text": _tri("金型は型です"), "question": _q("金型", "金型は?", ["型", "油", "靴"])})

    body = {"userId": str(line.rahim_id), "termJa": "金型"}
    res = client.post(f"/api/messages/{sent['id']}/reexplain", headers=line.foreman, json=body)
    assert res.status_code == 201, res.text
    re_id = res.json()["data"]["id"]

    assert [m["id"] for m in _poll(client, line, line.sadia)] == [sent["id"]]  # not for Sadia
    assert client.post(f"/api/messages/{re_id}/answers", headers=line.sadia, json={"answers": [0]}).status_code == 404
    passed = client.post(f"/api/messages/{re_id}/answers", headers=line.rahim, json={"answers": [0]}).json()["data"]
    assert passed["status"] == "green"
    [follow] = client.get("/api/follow-ups", headers=line.foreman).json()["data"]["items"]
    assert follow["status"] == "understood"


def test_follow_ups_only_move_forward_and_reach_the_support_log(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)
    [fid] = client.post(f"/api/messages/{sent['id']}/answers", headers=line.sadia, json={"answers": [-1, 0]}).json()[
        "data"
    ]["followUpIds"]

    done = client.patch(
        f"/api/follow-ups/{fid}", headers=line.foreman, json={"status": "clarified", "note": "図で説明"}
    )
    back = client.patch(f"/api/follow-ups/{fid}", headers=line.foreman, json={"status": "clarified"})

    assert done.status_code == 200 and done.json()["data"]["status"] == "clarified"
    assert back.status_code == 409
    today = datetime.now(UTC).date()
    params = {"from": str(today - timedelta(days=1)), "to": str(today + timedelta(days=1))}
    log = client.get("/api/support-log.csv", headers=line.foreman, params=params).content.decode("utf-8-sig")
    assert "金型" in log and "図で説明" in log


def test_a_nudge_shows_and_is_rate_limited(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    sent = _sent(client, line, fake_ai)
    body = {"userId": str(line.sadia_id)}

    assert client.post(f"/api/messages/{sent['id']}/nudge", headers=line.foreman, json=body).status_code == 200
    assert client.post(f"/api/messages/{sent['id']}/nudge", headers=line.foreman, json=body).status_code == 429
    assert _poll(client, line, line.sadia)[0]["nudged"] is True
    assert _poll(client, line, line.rahim)[0]["nudged"] is False


def test_a_stale_task_move_is_refused(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    _sent(client, line, fake_ai)
    [task] = client.get("/api/tasks", headers=line.foreman).json()["data"]["items"]
    url = f"/api/tasks/{task['id']}"

    moved = client.patch(url, headers=line.rahim | {"If-Match": "1"}, json={"column": "doing"})
    stale = client.patch(url, headers=line.foreman | {"If-Match": "1"}, json={"column": "done"})

    assert moved.status_code == 200 and moved.json()["data"]["version"] == 2
    assert stale.status_code == 412 and stale.json()["error"]["code"] == "VERSION_CONFLICT"
    assert client.patch(url, headers=line.foreman, json={"column": "done"}).status_code == 422  # If-Match required


def test_a_worker_message_is_translated_for_the_foreman(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("translate", {"text": "機械3のオイルが少ないです。"})

    res = client.post(f"/api/threads/{line.thread_id}/messages", headers=line.rahim | _key(), data={"text": "oil low"})

    assert res.status_code == 201 and res.json()["data"]["stage"] == 4
    [seen] = _poll(client, line, line.foreman)
    assert seen["text"]["ja"] == "機械3のオイルが少ないです。"
    threads = client.get("/api/threads", headers=line.sadia).json()["data"]
    assert threads[0]["unread"] == 1


def test_an_unchanged_poll_is_a_304(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    _sent(client, line, fake_ai)
    first = client.get(f"/api/threads/{line.thread_id}/messages", headers=line.rahim)

    again = client.get(
        f"/api/threads/{line.thread_id}/messages", headers=line.rahim | {"If-None-Match": first.headers["ETag"]}
    )

    assert again.status_code == 304


def test_outsiders_and_other_shops_are_kept_out(
    client: TestClient, db: Session, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)
    other = _line(client, db)
    url = f"/api/threads/{line.thread_id}/messages"

    assert client.get(url, headers=other.foreman).status_code == 404
    assert client.get(url, headers=line.outsider).status_code == 403  # same shop, not in the Thread
    assert (
        client.post(f"/api/messages/{sent['id']}/answers", headers=other.rahim, json={"answers": [0, 0]}).status_code
        == 404
    )
    assert (
        client.post(f"/api/messages/{sent['id']}/send", headers=line.rahim, json={"text": _tri("x")}).status_code == 403
    )
    assert client.get("/api/tasks", headers=other.foreman).json()["data"]["items"] == []
    assert client.get("/api/follow-ups", headers=other.foreman).json()["data"]["items"] == []
