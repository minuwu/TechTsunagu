"""XREQ-019: group controls on work Threads: create, read, rename, members and roles, personal flags, leave,
announce-only, locked Threads, system messages, and the shop boundary."""

import uuid
from datetime import UTC, datetime
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.models.chat import Thread
from app.models.scopes import Shop
from app.seed import Refs, seed_work
from app.seed_chat import seed_chat
from tests import factories as f
from tests.factories import TEST_PEPPER


@pytest.fixture
def refs(db: Session) -> Refs:
    now = datetime.now(UTC)
    return seed_chat(db, seed_work(db, TEST_PEPPER, now), now)


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _key() -> dict[str, str]:
    return {"Idempotency-Key": str(uuid.uuid4())}


def _foreman(client: TestClient, refs: Refs) -> dict[str, str]:
    return _login(client, refs["f-yamaguchi"], "246810")


def _worker(client: TestClient, refs: Refs, key: str) -> dict[str, str]:
    return _login(client, refs[key], "2468")


def _poll(client: TestClient, who: dict[str, str], thread_id: int | str) -> list[dict[str, Any]]:
    res = client.get(f"/api/threads/{thread_id}/messages", headers=who)
    assert res.status_code == 200, res.text
    items: list[dict[str, Any]] = res.json()["data"]["items"]
    return items


def _create(client: TestClient, who: dict[str, str], refs: Refs, **extra: Any) -> dict[str, Any]:
    body = {"titleJa": "夜勤チーム", "memberIds": [str(refs["w-rahim"])], **extra}
    res = client.post("/api/threads", headers=who | _key(), json=body)
    assert res.status_code == 201, res.text
    data: dict[str, Any] = res.json()["data"]
    return data


def test_a_foreman_creates_a_group_thread_once(client: TestClient, refs: Refs) -> None:
    foreman, key = _foreman(client, refs), _key()
    body = {"titleJa": "夜勤チーム", "description": "夜勤の連絡", "memberIds": [str(refs["w-rahim"])]}

    first = client.post("/api/threads", headers=foreman | key, json=body)
    again = client.post("/api/threads", headers=foreman | key, json=body)

    assert first.status_code == 201 and again.json()["data"]["id"] == first.json()["data"]["id"]
    data = first.json()["data"]
    roles = {m["displayName"]: m["role"] for m in data["members"]}
    assert roles == {"山口 隆": "admin", "Rahim Uddin": "member"}
    assert (data["kind"], data["announceOnly"], data["locked"]) == ("group", False, False)
    assert client.get(f"/api/threads/{data['id']}", headers=_worker(client, refs, "w-rahim")).status_code == 200


def test_workers_cannot_create_or_change_threads(client: TestClient, refs: Refs) -> None:
    rahim = _worker(client, refs, "w-rahim")
    line = refs["t-line2"]

    assert client.post("/api/threads", headers=rahim | _key(), json={"titleJa": "x"}).status_code == 403
    assert client.patch(f"/api/threads/{line}", headers=rahim, json={"titleJa": "x"}).status_code == 403
    res = client.post(f"/api/threads/{line}/members", headers=rahim, json={"userIds": [str(refs["w-tanaka"])]})
    assert res.status_code == 403


def test_members_must_be_in_the_shop(client: TestClient, refs: Refs) -> None:
    body = {"titleJa": "x", "memberIds": [str(refs["w-isahaya"])]}
    assert client.post("/api/threads", headers=_foreman(client, refs) | _key(), json=body).status_code == 422


def test_a_rename_is_a_system_message(client: TestClient, refs: Refs) -> None:
    foreman, line = _foreman(client, refs), refs["t-line2"]

    res = client.patch(f"/api/threads/{line}", headers=foreman, json={"titleJa": "2号ライン（夜）"})

    assert res.status_code == 200 and res.json()["data"]["titleJa"] == "2号ライン（夜）"
    last = _poll(client, _worker(client, refs, "w-rahim"), line)[-1]
    assert last["kind"] == "system"
    assert last["event"] == {"event": "renamed", "actorId": str(refs["f-yamaguchi"]), "subjectId": None}


def test_a_new_member_sees_messages_from_the_join_and_a_removed_one_none(client: TestClient, refs: Refs) -> None:
    foreman, line = _foreman(client, refs), refs["t-line2"]

    added = client.post(f"/api/threads/{line}/members", headers=foreman, json={"userIds": [str(refs["w-tanaka"])]})
    removed = client.delete(f"/api/threads/{line}/members/{refs['w-sadia']}", headers=foreman)

    assert added.status_code == 200 and removed.status_code == 200
    names = {m["displayName"] for m in removed.json()["data"]["members"]}
    assert "田中 健" in names and "Sadia Akter" not in names
    seen = _poll(client, _worker(client, refs, "w-tanaka"), line)
    assert str(refs["m1"]) not in {m["id"] for m in seen}  # history before the join stays hidden
    assert [m["event"]["event"] for m in seen] == ["member_added", "member_removed"]
    assert client.get(f"/api/threads/{line}/messages", headers=_worker(client, refs, "w-sadia")).status_code == 403


def test_a_thread_keeps_one_foreman_admin(client: TestClient, refs: Refs) -> None:
    foreman, line, me = _foreman(client, refs), refs["t-line2"], refs["f-yamaguchi"]

    demote = client.patch(f"/api/threads/{line}/members/{me}", headers=foreman, json={"role": "member"})
    remove = client.delete(f"/api/threads/{line}/members/{me}", headers=foreman)
    leave = client.post(f"/api/threads/{line}/leave", headers=foreman)

    assert (demote.status_code, remove.status_code, leave.status_code) == (409, 409, 409)


def test_a_worker_can_leave(client: TestClient, refs: Refs) -> None:
    rahim, line = _worker(client, refs, "w-rahim"), refs["t-line2"]

    assert client.post(f"/api/threads/{line}/leave", headers=rahim).status_code == 200

    assert client.get(f"/api/threads/{line}", headers=rahim).status_code == 403
    assert _poll(client, _foreman(client, refs), line)[-1]["event"]["event"] == "member_left"


def test_announce_only_stops_worker_posts(client: TestClient, refs: Refs) -> None:
    foreman, rahim = _foreman(client, refs), _worker(client, refs, "w-rahim")
    thread = _create(client, foreman, refs, announceOnly=True)

    blocked = client.post(f"/api/threads/{thread['id']}/messages", headers=rahim | _key(), data={"text": "hi"})
    client.patch(f"/api/threads/{thread['id']}", headers=foreman, json={"announceOnly": False})
    allowed = client.post(f"/api/threads/{thread['id']}/messages", headers=rahim | _key(), data={"text": "hi"})

    assert blocked.status_code == 403 and allowed.status_code == 201


def test_a_locked_thread_cannot_be_changed(client: TestClient, db: Session, refs: Refs) -> None:
    line = refs["t-line2"]
    thread = db.get(Thread, line)
    assert thread is not None
    thread.locked = True
    db.flush()

    res = client.patch(f"/api/threads/{line}", headers=_foreman(client, refs), json={"titleJa": "x"})

    assert res.status_code == 409 and res.json()["error"]["code"] == "THREAD_LOCKED"


def test_personal_flags_show_in_the_thread_list(client: TestClient, refs: Refs) -> None:
    rahim, line = _worker(client, refs, "w-rahim"), refs["t-line2"]

    res = client.put(f"/api/threads/{line}/me", headers=rahim, json={"muted": True, "pinned": True, "archived": False})

    assert res.status_code == 200
    mine = next(t for t in client.get("/api/threads", headers=rahim).json()["data"] if t["id"] == str(line))
    assert (mine["muted"], mine["pinned"], mine["archived"]) == (True, True, False)


def test_another_shop_sees_no_thread(client: TestClient, refs: Refs) -> None:
    other = _login(client, refs["f-isahaya"], "135790")
    assert client.get(f"/api/threads/{refs['t-line2']}", headers=other).status_code == 404


def test_a_removed_foreman_cannot_send_an_old_draft(client: TestClient, db: Session, refs: Refs) -> None:
    shop = db.get(Shop, refs["s-sasebo"])
    assert shop is not None
    other = f.user(db, shop, role="foreman", pin="739215", must_change=False)
    foreman, other_login = _foreman(client, refs), _login(client, other.id, "739215")
    thread = _create(client, foreman, refs, memberIds=[str(other.id)])
    res = client.post(f"/api/threads/{thread['id']}/messages", headers=other_login | _key(), data={"text": "明日"})
    assert res.status_code == 201, res.text
    draft = res.json()["data"]
    assert client.delete(f"/api/threads/{thread['id']}/members/{other.id}", headers=foreman).status_code == 200

    sent = client.post(f"/api/messages/{draft['id']}/send", headers=other_login, json={"text": {"ja": "明日"}})

    assert sent.status_code == 403


def test_a_new_member_cannot_reach_older_messages_by_id(client: TestClient, refs: Refs) -> None:
    foreman, line = _foreman(client, refs), refs["t-line2"]
    client.post(f"/api/threads/{line}/members", headers=foreman, json={"userIds": [str(refs["w-tanaka"])]})
    tanaka = _worker(client, refs, "w-tanaka")

    answer = client.post(f"/api/messages/{refs['m1']}/answers", headers=tanaka, json={"answers": [0]})
    mine = next(t for t in client.get("/api/threads", headers=tanaka).json()["data"] if t["id"] == str(line))

    assert answer.status_code == 404
    assert mine["unread"] == 1  # only the member_added system message, not the history before the join
