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
from app.models.chat import Thread, ThreadMember
from app.models.scopes import Shop, User
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
        # the glossary lock (message_problems) needs 金型's fixed translations: ডাই, die
        "text": {"ja": "明日8時に金型交換", "bn": "কাল ৮টায় ডাই বদল", "en": "Die change tomorrow at 8"},
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
    text = {"ja": "金型は型です", "bn": "ডাই হলো ছাঁচ", "en": "The die is the mould"}  # fixed translations kept
    fake_ai.queue("reexplain", {"text": text, "question": _q("金型", "金型は?", ["型", "油", "靴"])})

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


def test_a_missed_question_without_a_term_can_be_explained_again(
    client: TestClient, db: Session, line: Line, fake_ai: FakeAiClient
) -> None:
    """Most questions are about a time or a place, not a Glossary Term: the Foreman follows up on the question."""
    sent = _sent(client, line, fake_ai)

    result = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [0, 2]}).json()

    [follow] = client.get("/api/follow-ups?status=open", headers=line.foreman).json()["data"]["items"]
    assert result["data"]["followUpIds"] == [follow["id"]]
    assert (follow["messageId"], follow["termJa"]) == (sent["id"], "何時?")
    assert db.scalar(select(Confusion).where(Confusion.message_id == int(sent["id"]))) is None  # needs a Term

    question = _q("何時?", "交換は何時?", ["8時", "9時", "10時"])
    fake_ai.queue("reexplain", {"text": _tri("8時です"), "question": question})
    res = client.post(
        f"/api/messages/{sent['id']}/reexplain/draft",
        headers=line.foreman,
        json={"userId": follow["userId"], "termJa": follow["termJa"]},
    )
    assert res.status_code == 200, res.text
    assert res.json()["data"]["text"]["ja"] == "8時です"


def test_the_follow_up_on_a_private_reexplanation_stays_with_its_sender(
    client: TestClient, db: Session, line: Line, fake_ai: FakeAiClient
) -> None:
    """A Follow-up can quote the question, so it is as visible as its message (poll: Re-explanation, history start)."""
    sent = _sent(client, line, fake_ai)
    thread = db.get_one(Thread, line.thread_id)
    other = f.user(db, db.get_one(Shop, thread.shop_id), role="foreman", pin="739215", must_change=False)
    member = ThreadMember(thread_id=thread.id, user_id=other.id, shop_id=thread.shop_id)
    db.add(member)
    db.flush()
    second = _login(client, other.id, "739215")
    options = [_tri("8時"), _tri("9時"), _tri("10時")]
    question = {"id": "q1", "termId": None, "prompt": _tri("秘密の質問?"), "options": options, "correctIndex": 0}
    body = {"userId": str(line.rahim_id), "termJa": "何時?", "text": _tri("8時です"), "question": question}
    re = client.post(f"/api/messages/{sent['id']}/reexplain", headers=line.foreman, json=body).json()["data"]
    [fid] = client.post(f"/api/messages/{re['id']}/answers", headers=line.rahim, json={"answers": [1]}).json()["data"][
        "followUpIds"
    ]

    def seen(who: dict[str, str]) -> list[str]:
        return [x["termJa"] for x in client.get("/api/follow-ups", headers=who).json()["data"]["items"]]

    assert seen(line.foreman) == ["秘密の質問?"] and seen(second) == []
    move = {"status": "clarified"}
    assert client.patch(f"/api/follow-ups/{fid}", headers=second, json=move).status_code == 404
    assert client.patch(f"/api/follow-ups/{fid}", headers=line.foreman, json=move).status_code == 200

    client.post(f"/api/messages/{sent['id']}/answers", headers=line.sadia, json={"answers": [1, 0]})
    assert seen(second) == ["金型"]
    member.history_from = datetime.now(UTC) + timedelta(hours=1)  # added after the message
    db.flush()
    assert seen(second) == []


def test_two_misses_on_one_term_make_one_follow_up(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    answer = draft_answer(line)
    answer["questions"] = [
        _q("金型", "何を交換?", ["金型", "電源", "靴"]),
        _q("金型", "どの型?", ["2号", "1号", "3号"]),
    ]
    fake_ai.queue("draft_message", answer)
    url = f"/api/threads/{line.thread_id}/messages"
    draft = client.post(url, headers=line.foreman | _key(), data={"text": "金型交換"}).json()["data"]
    body = {"text": draft["text"], "questions": draft["questions"]}
    assert client.post(f"/api/messages/{draft['id']}/send", headers=line.foreman, json=body).status_code == 200

    res = client.post(f"/api/messages/{draft['id']}/answers", headers=line.rahim, json={"answers": [1, -1]})

    assert res.status_code == 200, res.text
    [follow] = client.get("/api/follow-ups", headers=line.foreman).json()["data"]["items"]
    assert res.json()["data"]["followUpIds"] == [follow["id"]] and follow["termJa"] == "金型"


def test_asking_before_answering_keeps_one_follow_up(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    sent = _sent(client, line, fake_ai)
    client.post(f"/api/messages/{sent['id']}/ask", headers=line.rahim, json={"termJa": "金型"})

    res = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [1, 0]})

    assert res.status_code == 200, res.text
    [follow] = client.get("/api/follow-ups", headers=line.foreman).json()["data"]["items"]
    assert res.json()["data"]["followUpIds"] == [follow["id"]]


def test_asking_again_after_understanding_still_reaches_the_foreman(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)
    [fid] = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [1, 0]}).json()[
        "data"
    ]["followUpIds"]
    client.patch(f"/api/follow-ups/{fid}", headers=line.foreman, json={"status": "understood"})

    res = client.post(f"/api/messages/{sent['id']}/ask", headers=line.rahim, json={"termJa": "金型"})

    assert res.status_code == 201, res.text
    assert "ask" in [m["kind"] for m in _poll(client, line, line.foreman)]
    [follow] = client.get("/api/follow-ups", headers=line.foreman).json()["data"]["items"]
    assert follow["status"] == "understood"  # Follow-ups only move forward


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


def test_the_task_board_filters_by_machine(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    _sent(client, line, fake_ai)  # one Task on the line's Machine
    loose = {"title": {"ja": "床の清掃", "bn": "মেঝে পরিষ্কার", "en": "Clean the floor"}}
    assert client.post("/api/tasks", headers=line.foreman, json=loose).status_code == 201  # no Machine

    every = client.get("/api/tasks", headers=line.foreman).json()["data"]["items"]
    mine = client.get("/api/tasks", headers=line.foreman, params={"machineId": str(line.machine_id)}).json()["data"]
    none = client.get("/api/tasks", headers=line.foreman, params={"machineId": "999999"}).json()["data"]

    assert len(every) == 2
    assert [t["machineId"] for t in mine["items"]] == [str(line.machine_id)]
    assert none["items"] == []


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


def test_a_long_thread_opens_at_its_newest_messages(client: TestClient, db: Session, line: Line) -> None:
    """Without a cursor a poll returns the newest page, oldest first; a cursor still walks forward from it."""
    thread = db.get_one(Thread, line.thread_id)
    shop, foreman = db.get_one(Shop, thread.shop_id), db.get_one(User, thread.created_by)
    sent = [str(f.message(db, shop, thread, foreman, stage=4, sent_at=datetime.now(UTC)).id) for _ in range(103)]

    ids = [m["id"] for m in _poll(client, line, line.rahim)]
    url = f"/api/threads/{line.thread_id}/messages"
    page = client.get(url, params={"after": sent[0]}, headers=line.rahim).json()["data"]["items"]

    assert ids == sent[3:]
    assert [m["id"] for m in page] == sent[1:101]


@pytest.mark.parametrize("path", ["/api/threads", "/api/tasks"])
def test_unchanged_threads_and_tasks_are_a_304(
    client: TestClient, line: Line, fake_ai: FakeAiClient, path: str
) -> None:
    _sent(client, line, fake_ai)
    first = client.get(path, headers=line.rahim)
    assert first.status_code == 200 and first.headers["ETag"]

    again = client.get(path, headers=line.rahim | {"If-None-Match": first.headers["ETag"]})
    stale = client.get(path, headers=line.rahim | {"If-None-Match": '"not-the-tag"'})

    assert again.status_code == 304 and again.content == b""
    assert stale.status_code == 200 and stale.json() == first.json()


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


def test_workers_move_tasks_but_only_the_foreman_reassigns(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    _sent(client, line, fake_ai)
    [task] = client.get("/api/tasks", headers=line.foreman).json()["data"]["items"]

    reassign = {"assigneeIds": [str(line.sadia_id)]}
    res = client.patch(f"/api/tasks/{task['id']}", headers=line.rahim | {"If-Match": "1"}, json=reassign)

    assert res.status_code == 403
    ok = client.patch(f"/api/tasks/{task['id']}", headers=line.foreman | {"If-Match": "1"}, json=reassign)
    assert ok.status_code == 200 and ok.json()["data"]["assigneeIds"] == [str(line.sadia_id)]


def test_a_foreman_outside_the_thread_cannot_see_or_close_its_follow_ups(
    client: TestClient, db: Session, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)
    [fid] = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [1, 0]}).json()[
        "data"
    ]["followUpIds"]
    shop = db.get(User, line.rahim_id)
    assert shop is not None and shop.shop_id is not None
    second = f.user(db, db.get(Shop, shop.shop_id), role="foreman", pin="739215", must_change=False)  # type: ignore[arg-type]
    other_foreman = _login(client, second.id, "739215")

    assert client.get("/api/follow-ups", headers=other_foreman).json()["data"]["items"] == []
    res = client.patch(f"/api/follow-ups/{fid}", headers=other_foreman, json={"status": "clarified"})
    assert res.status_code == 403


def test_an_edited_draft_is_translated_again_before_sending(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    """XREQ-022: the Foreman fixes the Japanese; translations and questions are made again from the new text."""
    fake_ai.queue("draft_message", draft_answer(line))
    res = client.post(
        f"/api/threads/{line.thread_id}/messages", headers=line.foreman | _key(), data={"text": "金型交換"}
    )
    draft = res.json()["data"]
    edited = draft_answer(line) | {"text": {"ja": "明日9時に金型交換", "bn": "কাল ৯টায় ডাই বদল", "en": "Die change at 9"}}
    fake_ai.queue("draft_message", edited)

    res = client.patch(f"/api/messages/{draft['id']}", headers=line.foreman, json={"textJa": "明日9時に金型交換"})
    assert res.status_code == 200, res.text
    again = res.json()["data"]
    assert again["id"] == draft["id"] and again["text"]["en"] == "Die change at 9" and again["stage"] == 3
    assert again["questions"] and again["taskDrafts"]
    assert client.patch(f"/api/messages/{draft['id']}", headers=line.rahim, json={"textJa": "x"}).status_code == 403

    body = {"text": again["text"], "questions": again["questions"]}
    client.post(f"/api/messages/{draft['id']}/send", headers=line.foreman, json=body)
    late = client.patch(f"/api/messages/{draft['id']}", headers=line.foreman, json={"textJa": "遅い"})
    assert late.status_code == 409  # a sent message is not edited


def test_an_edit_without_the_model_keeps_the_foremans_text(client: TestClient, line: Line) -> None:
    res = client.post(f"/api/threads/{line.thread_id}/messages", headers=line.foreman | _key(), data={"text": "清掃"})
    draft_id = res.json()["data"]["id"]
    res = client.patch(f"/api/messages/{draft_id}", headers=line.foreman, json={"textJa": "清掃は10時"})
    data = res.json()["data"]
    assert res.status_code == 200 and data["needsEdit"] is True and data["text"] == {"ja": "清掃は10時"}


def test_an_edit_that_lost_the_model_is_repaired_by_the_same_edit(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("draft_message", draft_answer(line))
    res = client.post(
        f"/api/threads/{line.thread_id}/messages", headers=line.foreman | _key(), data={"text": "金型交換"}
    )
    path, edit = f"/api/messages/{res.json()['data']['id']}", {"textJa": "明日9時に金型交換"}
    degraded = client.patch(path, headers=line.foreman, json=edit).json()["data"]
    assert degraded["needsEdit"] is True and degraded["taskDrafts"] == []  # not the old text's Tasks

    fake_ai.queue("draft_message", draft_answer(line))
    again = client.patch(path, headers=line.foreman, json=edit).json()["data"]

    assert again["needsEdit"] is False and again["questions"] and again["taskDrafts"]


def test_a_retried_post_returns_the_task_drafts_until_send(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("draft_message", draft_answer(line))
    path, headers, form = f"/api/threads/{line.thread_id}/messages", line.foreman | _key(), {"text": "金型交換"}
    first = client.post(path, headers=headers, data=form).json()["data"]
    again = client.post(path, headers=headers, data=form).json()["data"]
    assert first["taskDrafts"] and again == first

    body = {"text": first["text"], "questions": first["questions"]}
    client.post(f"/api/messages/{first['id']}/send", headers=line.foreman, json=body)
    assert client.post(path, headers=headers, data=form).json()["data"]["taskDrafts"] == []


def test_the_foreman_reads_and_edits_a_reexplanation_before_it_is_sent(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    """XREQ-022: /reexplain/draft stores nothing; /reexplain with the edited text and question sends it as written."""
    sent = _sent(client, line, fake_ai)
    text = {"ja": "金型は型です", "bn": "ডাই হলো ছাঁচ", "en": "The die is the mould"}
    fake_ai.queue("reexplain", {"text": text, "question": _q("金型", "金型は?", ["型", "油", "靴"])})
    ask = {"userId": str(line.rahim_id), "termJa": "金型"}

    res = client.post(f"/api/messages/{sent['id']}/reexplain/draft", headers=line.foreman, json=ask)
    assert res.status_code == 200, res.text
    draft = res.json()["data"]
    assert draft["text"]["en"] == "The die is the mould" and draft["question"]["correctIndex"] == 0
    assert len(_poll(client, line, line.rahim)) == 1  # nothing sent yet

    edited = draft["text"] | {"en": "A die is the metal mould"}
    res = client.post(
        f"/api/messages/{sent['id']}/reexplain",
        headers=line.foreman,
        json=ask | {"text": edited, "question": draft["question"]},
    )
    assert res.status_code == 201, res.text
    assert res.json()["data"]["text"]["en"] == "A die is the metal mould"
    half = client.post(f"/api/messages/{sent['id']}/reexplain", headers=line.foreman, json=ask | {"text": edited})
    assert half.status_code == 422


def test_a_worker_asks_the_foreman_about_a_term(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    """XREQ-022: a fixed sentence (no AI) in the Thread, and one open Follow-up for the Foreman."""
    sent = _sent(client, line, fake_ai)
    res = client.post(f"/api/messages/{sent['id']}/ask", headers=line.rahim, json={"termJa": "金型"})
    assert res.status_code == 201, res.text
    asked = res.json()["data"]
    assert asked["kind"] == "ask" and asked["refMessageId"] == sent["id"] and "金型" in asked["text"]["ja"]
    client.post(f"/api/messages/{sent['id']}/ask", headers=line.rahim, json={"termJa": "金型"})

    follow_ups = client.get("/api/follow-ups?status=open", headers=line.foreman).json()["data"]["items"]
    assert [f["termJa"] for f in follow_ups] == ["金型"]  # asking twice opens one Follow-up
    assert [m["kind"] for m in _poll(client, line, line.foreman)].count("ask") == 1  # a second tap sends nothing
    assert next(m for m in _poll(client, line, line.rahim) if m["id"] == sent["id"])["asked"] is True
    assert (
        client.post(f"/api/messages/{sent['id']}/ask", headers=line.foreman, json={"termJa": "金型"}).status_code == 403
    )
    outsider = client.post(f"/api/messages/{sent['id']}/ask", headers=line.outsider, json={"termJa": "金型"})
    assert outsider.status_code in (403, 404)


def test_the_wakatta_cards_show_each_workers_answers(client: TestClient, line: Line, fake_ai: FakeAiClient) -> None:
    """The Foreman's card lists every Worker; a Worker sees the answer key only after answering."""
    sent = _sent(client, line, fake_ai)
    [card] = _poll(client, line, line.foreman)
    assert {r["userId"] for r in card["results"]} == {str(line.rahim_id), str(line.sadia_id)}
    assert all(r["status"] is None and r["answers"] is None for r in card["results"])

    [before] = _poll(client, line, line.rahim)
    assert before["answerKey"] is None and before["myAnswers"] is None and before["asked"] is False
    done = client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [1, 0]})
    assert done.json()["data"]["answers"] == [1, 0] and done.json()["data"]["answerKey"] == [0, 0]
    [after] = _poll(client, line, line.rahim)
    assert after["myAnswers"] == [1, 0] and after["answerKey"] == [0, 0]

    client.post(f"/api/messages/{sent['id']}/nudge", headers=line.foreman, json={"userId": str(line.sadia_id)})
    rows = {r["userId"]: r for r in _poll(client, line, line.foreman)[0]["results"]}
    assert rows[str(line.rahim_id)] == {
        "userId": str(line.rahim_id),
        "status": "orange",
        "answers": [1, 0],
        "read": True,
        "nudged": False,
    }
    assert rows[str(line.sadia_id)]["status"] is None and rows[str(line.sadia_id)]["nudged"] is True


def test_a_japanese_speaking_workers_voice_message_is_transcribed(
    client: TestClient, db: Session, line: Line, fake_ai: FakeAiClient
) -> None:
    worker = db.get(User, line.rahim_id)
    assert worker is not None
    worker.language = "ja"
    db.flush()
    fake_ai.queue("translate", {"text": "手袋が破れました"})
    ogg = ("v.ogg", b"OggS" + b"\x00" * 32, "audio/ogg")

    res = client.post(f"/api/threads/{line.thread_id}/messages", headers=line.rahim | _key(), files={"audio": ogg})

    assert res.status_code == 201, res.text
    assert res.json()["data"]["text"] == {"ja": "手袋が破れました"}  # not an empty message


def test_a_workers_message_keeps_the_shop_word_in_japanese(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("translate", {"text": "型の交換が終わりました"})  # 「金型」 lost: one retry
    fake_ai.queue("translate", {"text": "金型の交換が終わりました"})

    res = client.post(
        f"/api/threads/{line.thread_id}/messages", headers=line.rahim | _key(), data={"text": "ডাই বদল শেষ"}
    )

    assert res.status_code == 201, res.text
    assert res.json()["data"]["text"]["ja"] == "金型の交換が終わりました"


def test_the_thread_list_shows_the_last_message_and_the_worst_status(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    """Chats rows: a preview of the newest message for everyone; the worst Wakatta Status for the Foreman only."""

    def row(who: dict[str, str]) -> Json:
        threads: list[Json] = client.get("/api/threads", headers=who).json()["data"]
        return next(t for t in threads if t["id"] == str(line.thread_id))

    assert row(line.foreman)["lastMessage"] is None and row(line.foreman)["wkStatus"] is None
    sent = _sent(client, line, fake_ai)

    mine = row(line.foreman)
    assert mine["lastMessage"]["fromUserId"] == sent["fromUserId"] and mine["lastMessage"]["text"]["ja"]
    assert mine["wkStatus"] == "wait"
    client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [1, 0]})
    assert row(line.foreman)["wkStatus"] == "orange"
    assert row(line.rahim)["wkStatus"] is None and row(line.rahim)["lastMessage"]["kind"] == sent["kind"]


def test_workers_see_names_next_to_ids_and_the_tasks_thread(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)
    names = {
        m["userId"]: m["displayName"]
        for m in client.get(f"/api/threads/{line.thread_id}", headers=line.rahim).json()["data"]["members"]
    }

    polled = next(m for m in _poll(client, line, line.rahim) if m["id"] == sent["id"])
    [task] = client.get("/api/tasks", headers=line.rahim).json()["data"]["items"]

    assert polled["fromDisplayName"] == names[polled["fromUserId"]]
    assert polled["fromRole"] == "foreman"
    assert task["assignees"] == [{"userId": str(line.rahim_id), "displayName": names[str(line.rahim_id)]}]
    assert task["fromThreadId"] == str(line.thread_id)


def test_a_direct_thread_is_titled_with_the_other_person(client: TestClient, db: Session, line: Line) -> None:
    rahim = db.get(User, line.rahim_id)
    shop = db.get(Shop, rahim.shop_id) if rahim else None
    assert rahim is not None and shop is not None
    foreman = db.scalar(select(User).where(User.shop_id == shop.id, User.role == "foreman"))
    assert foreman is not None
    direct = f.thread(db, shop, foreman, [rahim], kind="direct")

    threads = {t["id"]: t for t in client.get("/api/threads", headers=line.rahim).json()["data"]}

    assert threads[str(direct.id)]["displayTitle"] == foreman.display_name
    group = threads[str(line.thread_id)]
    assert group["displayTitle"] == group["titleJa"]


def test_a_worker_lists_their_unanswered_checks_in_one_call(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    sent = _sent(client, line, fake_ai)

    before = client.get("/api/messages/unanswered", headers=line.rahim)
    client.post(f"/api/messages/{sent['id']}/answers", headers=line.rahim, json={"answers": [0, 0]})
    after = client.get("/api/messages/unanswered", headers=line.rahim).json()["data"]

    assert before.status_code == 200
    [item] = before.json()["data"]
    assert (item["messageId"], item["threadId"]) == (sent["id"], str(line.thread_id))
    assert after == []
    assert client.get("/api/messages/unanswered", headers=line.foreman).status_code == 403


def test_a_recording_is_transcribed_for_the_composer_and_nothing_is_sent(
    client: TestClient, line: Line, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("translate", {"text": "3号機の油を点検してください"})
    m4a = ("d.m4a", b"\x00\x00\x00\x18ftypM4A " + b"\x00" * 32, "audio/mp4")

    res = client.post("/api/transcriptions", headers=line.foreman, data={"language": "ja"}, files={"audio": m4a})
    empty = client.post("/api/transcriptions", headers=line.foreman, data={"language": "ja"})
    odd = client.post("/api/transcriptions", headers=line.foreman, data={"language": "xx"}, files={"audio": m4a})
    unavailable = client.post(
        "/api/transcriptions", headers=line.foreman, data={"language": "ja"}, files={"audio": m4a}
    )

    assert res.status_code == 200, res.text
    assert res.json()["data"] == {"text": "3号機の油を点検してください"}
    assert (empty.status_code, odd.status_code, unavailable.status_code) == (422, 422, 503)
    msgs = client.get(f"/api/threads/{line.thread_id}/messages", headers=line.foreman).json()["data"]["items"]
    assert msgs == []
