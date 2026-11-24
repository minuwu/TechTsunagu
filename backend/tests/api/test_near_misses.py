"""WS7 Near-miss Reports (architecture §9.3): report (text/voice, photo) → Foreman reads Japanese → acknowledge."""

import uuid
from dataclasses import dataclass

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from app.services.near_misses import UNTRANSLATED_VOICE
from tests import factories as f

WEBM = b"\x1a\x45\xdf\xa3" + b"0" * 64


@dataclass
class Floor:
    foreman: dict[str, str]
    worker: dict[str, str]
    other_worker: dict[str, str]
    machine_id: int


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _key() -> dict[str, str]:
    return {"Idempotency-Key": str(uuid.uuid4())}


def _floor(client: TestClient, db: Session) -> Floor:
    s = f.shop(db)
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    worker, other = (f.user(db, s, pin="2468", must_change=False) for _ in range(2))
    machine = f.machine(db, s, code="press-2")
    return Floor(
        _login(client, foreman.id, "739215"),
        _login(client, worker.id, "2468"),
        _login(client, other.id, "2468"),
        machine.id,
    )


@pytest.fixture
def floor(client: TestClient, db: Session) -> Floor:
    return _floor(client, db)


def test_a_report_is_translated_and_acknowledged(client: TestClient, floor: Floor, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("translate", {"text": "プレスの安全カバーが外れていました。"})
    form = {"machineId": str(floor.machine_id), "text": "Press guard was off"}

    res = client.post("/api/near-misses", headers=floor.worker | _key(), data=form)

    assert res.status_code == 201, res.text
    report = res.json()["data"]
    assert report["text"] == {"ja": "プレスの安全カバーが外れていました。", "bn": "Press guard was off"}
    [listed] = client.get("/api/near-misses?open=true", headers=floor.foreman).json()["data"]["items"]
    assert listed["id"] == report["id"]
    acked = client.post(f"/api/near-misses/{report['id']}/ack", headers=floor.foreman).json()["data"]
    assert acked["acknowledgedAt"] is not None
    assert client.get("/api/near-misses?open=true", headers=floor.foreman).json()["data"]["items"] == []


def test_an_unchanged_list_is_a_304_and_a_new_report_is_not(
    client: TestClient, floor: Floor, fake_ai: FakeAiClient
) -> None:
    first = client.get("/api/near-misses", headers=floor.foreman)
    tag = first.headers["ETag"]
    same = client.get("/api/near-misses", headers=floor.foreman | {"If-None-Match": tag})
    fake_ai.queue("translate", {"text": "床が滑りやすい。"})
    form = {"machineId": str(floor.machine_id), "text": "Floor is slippery"}
    assert client.post("/api/near-misses", headers=floor.worker | _key(), data=form).status_code == 201

    changed = client.get("/api/near-misses", headers=floor.foreman | {"If-None-Match": tag})

    assert same.status_code == 304
    assert changed.status_code == 200 and len(changed.json()["data"]["items"]) == 1


def test_a_voice_report_is_kept_when_the_model_is_down(client: TestClient, floor: Floor) -> None:
    res = client.post(
        "/api/near-misses",
        headers=floor.worker | _key(),
        data={"machineId": str(floor.machine_id)},
        files={"audio": ("r.webm", WEBM, "audio/webm")},
    )

    assert res.status_code == 201 and res.json()["data"]["text"] == {"ja": UNTRANSLATED_VOICE}


def test_a_retried_report_is_stored_once(client: TestClient, floor: Floor, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("translate", {"text": "床が濡れています。"})
    headers, form = floor.worker | _key(), {"machineId": str(floor.machine_id), "text": "Wet floor"}

    first = client.post("/api/near-misses", headers=headers, data=form).json()["data"]
    again = client.post("/api/near-misses", headers=headers, data=form).json()["data"]

    assert again["id"] == first["id"] and fake_ai.calls == 1


def test_the_photo_is_for_the_foreman_and_the_reporter_only(
    client: TestClient, floor: Floor, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("translate", {"text": "油漏れ"})
    payload = b"Exif\x00\x00GPS"
    jpeg = b"\xff\xd8\xff\xe1" + (len(payload) + 2).to_bytes(2) + payload + b"\xff\xda\x00\x02img\xff\xd9"

    res = client.post(
        "/api/near-misses",
        headers=floor.worker | _key(),
        data={"machineId": str(floor.machine_id), "text": "oil leak"},
        files={"photo": ("p.jpg", jpeg, "image/jpeg")},
    )
    url = res.json()["data"]["photo"]

    foreman_view = client.get(url, headers=floor.foreman)
    assert foreman_view.status_code == 200 and b"GPS" not in foreman_view.content
    assert client.get(url, headers=floor.worker).status_code == 200
    assert client.get(url, headers=floor.other_worker).status_code == 404


def test_another_shop_sees_none_of_it(client: TestClient, db: Session, floor: Floor, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("translate", {"text": "危ない"})
    report = client.post(
        "/api/near-misses", headers=floor.worker | _key(), data={"machineId": str(floor.machine_id), "text": "danger"}
    ).json()["data"]
    other = _floor(client, db)

    assert client.get("/api/near-misses", headers=other.foreman).json()["data"]["items"] == []
    assert client.post(f"/api/near-misses/{report['id']}/ack", headers=other.foreman).status_code == 404
    form = {"machineId": str(floor.machine_id), "text": "x"}
    assert client.post("/api/near-misses", headers=other.worker | _key(), data=form).status_code == 404
    assert client.get(f"/api/near-misses/{report['id']}", headers=other.foreman).status_code == 404
    body = {"taskId": "1"}
    assert client.put(f"/api/near-misses/{report['id']}/task", headers=other.foreman, json=body).status_code == 404


def test_another_workers_key_does_not_return_their_report(
    client: TestClient, floor: Floor, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("translate", {"text": "床が濡れています。"})
    key, form = _key(), {"machineId": str(floor.machine_id), "text": "Wet floor"}
    assert client.post("/api/near-misses", headers=floor.worker | key, data=form).status_code == 201

    res = client.post("/api/near-misses", headers=floor.other_worker | key, data=form)

    assert res.status_code == 409 and res.json()["error"]["code"] == "IDEMPOTENCY_KEY_REUSED"


JPEG = b"\xff\xd8\xff\xda\x00\x02img\xff\xd9"


def test_a_worker_lists_and_opens_only_their_own_reports(
    client: TestClient, floor: Floor, fake_ai: FakeAiClient
) -> None:
    """XREQ-023: My reports, with severity, the photo mark and the reporter's name."""
    fake_ai.queue("translate", {"text": "油漏れ"})
    res = client.post(
        "/api/near-misses",
        headers=floor.worker | _key(),
        data={
            "machineId": str(floor.machine_id),
            "text": "oil leak",
            "severity": "high",
            "photoMarkX": "0.5",
            "photoMarkY": "0.25",
        },
        files={"photo": ("p.jpg", JPEG, "image/jpeg")},
    )
    assert res.status_code == 201, res.text
    mine = res.json()["data"]
    assert mine["severity"] == "high" and mine["photoMark"] == {"x": 0.5, "y": 0.25}
    assert mine["textJa"] == "油漏れ" and mine["lang"] == "bn" and mine["workerName"] and mine["taskId"] is None

    assert [r["id"] for r in client.get("/api/near-misses", headers=floor.worker).json()["data"]["items"]] == [
        mine["id"]
    ]
    assert client.get("/api/near-misses", headers=floor.other_worker).json()["data"]["items"] == []
    path = f"/api/near-misses/{mine['id']}"
    assert client.get(path, headers=floor.worker).json()["data"] == mine
    assert client.get(path, headers=floor.foreman).json()["data"] == mine
    assert client.get(path, headers=floor.other_worker).status_code == 404


@pytest.mark.parametrize(
    "extra",
    [
        {"severity": "huge"},
        {"photoMarkX": "0.5", "photoMarkY": "0.5"},
        {"photoMarkX": "nan", "photoMarkY": "0.5"},
        {"photoMarkX": "1.5", "photoMarkY": "0.5"},
    ],
)
def test_a_bad_severity_or_a_mark_without_a_photo_is_refused(
    client: TestClient, floor: Floor, extra: dict[str, str]
) -> None:
    form = {"machineId": str(floor.machine_id), "text": "x"} | extra
    assert client.post("/api/near-misses", headers=floor.worker | _key(), data=form).status_code == 422


def test_the_foreman_links_one_task_to_a_report(client: TestClient, floor: Floor, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("translate", {"text": "油漏れ"})
    form = {"machineId": str(floor.machine_id), "text": "oil leak"}
    report = client.post("/api/near-misses", headers=floor.worker | _key(), data=form).json()["data"]
    title = {"ja": "点検", "bn": "পরীক্ষা", "en": "Check"}
    first, second = (
        client.post("/api/tasks", headers=floor.foreman | _key(), json={"title": title}).json()["data"]["id"]
        for _ in range(2)
    )
    path = f"/api/near-misses/{report['id']}/task"

    assert client.put(path, headers=floor.worker, json={"taskId": first}).status_code == 403
    for _ in range(2):  # a retry changes nothing
        linked = client.put(path, headers=floor.foreman, json={"taskId": first})
        assert linked.status_code == 200 and linked.json()["data"]["taskId"] == first
    assert client.put(path, headers=floor.foreman, json={"taskId": second}).status_code == 409
    assert client.put(path, headers=floor.foreman, json={"taskId": "999999"}).status_code == 404
