"""XREQ-010 (Periodic Interviews, Today's Word, own Senpai pair), XREQ-009 (app config, push devices) and the
optional Idempotency-Key on Meet-ups and Tasks (XREQ-011 item 2)."""

import uuid
from dataclasses import dataclass
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.platform import Device
from tests import factories as f

TODAY = datetime.now(ZoneInfo("Asia/Tokyo")).date()


@dataclass
class Shop:
    foreman: dict[str, str]
    worker: dict[str, str]
    other_worker: dict[str, str]
    worker_id: int


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _shop(client: TestClient, db: Session) -> Shop:
    s = f.shop(db)
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    worker, other = (f.user(db, s, pin="2468", must_change=False) for _ in range(2))
    return Shop(
        _login(client, foreman.id, "739215"),
        _login(client, worker.id, "2468"),
        _login(client, other.id, "2468"),
        worker.id,
    )


@pytest.fixture
def shop(client: TestClient, db: Session) -> Shop:
    return _shop(client, db)


def _due_of(client: TestClient, shop: Shop) -> dict[str, object]:
    rows = client.get("/api/interviews/due", headers=shop.foreman).json()["data"]
    row: dict[str, object] = next(d for d in rows if d["workerId"] == str(shop.worker_id))
    return row


def test_an_interview_moves_the_due_date_and_reaches_the_support_log(client: TestClient, shop: Shop) -> None:
    before = _due_of(client, shop)
    assert before["lastHeldOn"] is None and before["status"] == "ok"  # due 3 months after the account was made

    body = {"heldOn": str(TODAY), "method": "face", "noteJa": "生活の困りごとなし"}
    res = client.post(f"/api/workers/{shop.worker_id}/interviews", headers=shop.foreman, json=body)

    assert res.status_code == 201, res.text
    assert _due_of(client, shop)["lastHeldOn"] == str(TODAY)
    [past] = client.get(f"/api/workers/{shop.worker_id}/interviews", headers=shop.foreman).json()["data"]
    assert past["noteJa"] == "生活の困りごとなし" and past["recordedBy"]
    params = {"from": str(TODAY - timedelta(days=1)), "to": str(TODAY + timedelta(days=1))}
    log = client.get("/api/support-log.csv", headers=shop.foreman, params=params).content.decode("utf-8-sig")
    assert "10 定期的な面談" in log and "生活の困りごとなし" in log


def test_interview_rules(client: TestClient, shop: Shop) -> None:
    url = f"/api/workers/{shop.worker_id}/interviews"
    online = client.post(url, headers=shop.foreman, json={"heldOn": str(TODAY), "method": "online"})
    future = client.post(url, headers=shop.foreman, json={"heldOn": str(TODAY + timedelta(days=2)), "method": "face"})
    by_worker = client.post(url, headers=shop.worker, json={"heldOn": str(TODAY), "method": "face"})

    assert online.status_code == 422  # online needs the Worker's consent time
    assert future.status_code == 422
    assert by_worker.status_code == 403


def test_a_retried_interview_is_stored_once(client: TestClient, shop: Shop) -> None:
    url, key = f"/api/workers/{shop.worker_id}/interviews", {"Idempotency-Key": str(uuid.uuid4())}
    body = {"heldOn": str(TODAY), "method": "face"}

    first = client.post(url, headers=shop.foreman | key, json=body).json()["data"]
    again = client.post(url, headers=shop.foreman | key, json=body).json()["data"]

    assert again["id"] == first["id"]
    assert len(client.get(url, headers=shop.foreman).json()["data"]) == 1


def test_another_shop_cannot_see_or_record(client: TestClient, db: Session, shop: Shop) -> None:
    other = _shop(client, db)
    url = f"/api/workers/{shop.worker_id}/interviews"

    assert client.get(url, headers=other.foreman).status_code == 404
    assert client.post(url, headers=other.foreman, json={"heldOn": str(TODAY), "method": "face"}).status_code == 404
    workers = {d["workerId"] for d in client.get("/api/interviews/due", headers=other.foreman).json()["data"]}
    assert str(shop.worker_id) not in workers


def test_todays_word_is_one_word_of_the_workers_language(client: TestClient, shop: Shop) -> None:
    word = client.get("/api/today-word", headers=shop.foreman).json()["data"]

    assert word["language"] == "en" and word["word"] and word["reading"] and word["meaningJa"]
    assert word["date"] == str(TODAY)


def test_app_config_is_public(client: TestClient) -> None:
    data = client.get("/api/app/config").json()["data"]

    assert data["minVersion"] == "1.0.0" and data["latestVersion"] == "1.0.0" and "serverTime" in data


def test_a_device_registers_once_and_a_shared_token_moves(client: TestClient, db: Session, shop: Shop) -> None:
    install = str(uuid.uuid4())
    body = {
        "installId": install,
        "platform": "android",
        "fcmToken": "tok-1",
        "appVersion": "1.0.0",
        "notificationsEnabled": True,
    }

    assert client.put("/api/devices/me", headers=shop.worker, json=body).json()["data"]["registered"] is True
    assert client.put("/api/devices/me", headers=shop.worker, json=body | {"appVersion": "1.0.1"}).status_code == 200
    shared_phone = body | {"installId": str(uuid.uuid4())}
    assert client.put("/api/devices/me", headers=shop.other_worker, json=shared_phone).status_code == 200

    assert db.scalar(select(func.count()).select_from(Device).where(Device.fcm_token == body["fcmToken"])) == 1
    assert client.delete(f"/api/devices/me?installId={install}", headers=shop.worker).status_code == 200
    assert client.put("/api/devices/me", json=body).status_code == 401


def test_a_worker_reads_their_own_senpai_pair(client: TestClient, shop: Shop) -> None:
    assert client.get("/api/senpai-pairs/me", headers=shop.worker).json()["data"] is None
    pair = {"workerId": str(shop.worker_id), "senpaiName": "田中 健", "kind": "coworker"}
    client.post("/api/senpai-pairs", headers=shop.foreman, json=pair)

    mine = client.get("/api/senpai-pairs/me", headers=shop.worker).json()["data"]

    assert mine["senpaiName"] == "田中 健" and "token" not in mine


def test_retried_meetups_and_tasks_are_stored_once(client: TestClient, shop: Shop) -> None:
    pair = {"workerId": str(shop.worker_id), "senpaiName": "森", "kind": "jichikai"}
    client.post("/api/senpai-pairs", headers=shop.foreman, json=pair)
    key = {"Idempotency-Key": str(uuid.uuid4())}
    meetup = {"kind": "coffee", "date": str(TODAY), "note": "駅前"}
    first = client.post("/api/meetups", headers=shop.worker | key, json=meetup).json()["data"]
    again = client.post("/api/meetups", headers=shop.worker | key, json=meetup).json()["data"]
    assert again["id"] == first["id"]

    task_key = {"Idempotency-Key": str(uuid.uuid4())}
    task = {"title": {"ja": "手袋 発注", "bn": "গ্লাভস", "en": "Order gloves"}}
    t1 = client.post("/api/tasks", headers=shop.foreman | task_key, json=task).json()["data"]
    t2 = client.post("/api/tasks", headers=shop.foreman | task_key, json=task).json()["data"]
    assert t2["id"] == t1["id"]
    assert len(client.get("/api/tasks", headers=shop.foreman).json()["data"]["items"]) == 1
