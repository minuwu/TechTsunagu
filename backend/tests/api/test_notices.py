"""WS10 Life flow (architecture §9.3): Poster draft → confirm Events → publish → Worker feed, receipt, Check, reach."""

import uuid
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from app.models.scopes import Poster, User
from tests import factories as f

SOON = (datetime.now(UTC) + timedelta(days=10)).replace(microsecond=0)
Json = dict[str, Any]


def _tri(word: str) -> dict[str, str]:
    return {"ja": word, "bn": f"{word}-bn", "en": f"{word}-en"}


def notice_answer(starts_at: datetime = SOON, rrule: str | None = None) -> Json:
    return {
        "source_ja": "防災訓練を行います。小学校の体育館に集合。",
        "category": "disaster",
        "title": _tri("防災訓練"),
        "steps": [_tri("体育館に集まる")],
        "events": [{"title": _tri("訓練"), "starts_at": starts_at.isoformat(), "rrule": rrule}],
        "question": {
            "term_ja": None,
            "prompt": _tri("どこに集まる?"),
            "options": [_tri("体育館"), _tri("駅"), _tri("会社")],
            "correct_index": 0,
        },
    }


@dataclass
class Town:
    poster: dict[str, str]
    worker: dict[str, str]


def _headers(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _key() -> dict[str, str]:
    return {"Idempotency-Key": str(uuid.uuid4())}


def _town(client: TestClient, db: Session) -> Town:
    d = f.district(db)
    shop = f.shop(db, d)
    poster = Poster(district_id=d.id, name="みなと町自治会", kind="jichikai")
    db.add(poster)
    db.flush()
    worker = f.user(db, shop, pin="2468", must_change=False)
    account = User(
        role="poster",
        display_name="事務局",
        language="ja",
        district_id=d.id,
        poster_id=poster.id,
        pin_hash=worker.pin_hash,  # same PIN as the Worker; argon2 is slow, so reuse the factory's hash
        pin_must_change=False,
    )
    db.add(account)
    db.flush()
    return Town(_headers(client, account.id, "2468"), _headers(client, worker.id, "2468"))


@pytest.fixture
def town(client: TestClient, db: Session) -> Town:
    return _town(client, db)


def _draft(client: TestClient, town: Town, text: str = "訓練") -> Json:
    res = client.post("/api/notices", headers=town.poster | _key(), data={"sourceKind": "text", "text": text})
    assert res.status_code == 201, res.text
    data: Json = res.json()["data"]
    return data


def _publish(client: TestClient, town: Town, notice: Json) -> None:
    for e in notice["events"]:
        url = f"/api/notices/{notice['id']}/events/{e['id']}"
        res = client.patch(url, headers=town.poster, json={"confirmed": True})
        assert res.status_code == 200, res.text
    res = client.post(f"/api/notices/{notice['id']}/publish", headers=town.poster)
    assert res.status_code == 200, res.text


def test_a_published_notice_reaches_the_worker_without_the_answer(
    client: TestClient, town: Town, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("build_notice", notice_answer(rrule="FREQ=WEEKLY;BYDAY=MO"))
    draft = _draft(client, town)
    assert draft["status"] == "draft" and draft["question"]["correctIndex"] == 0
    assert len(draft["events"][0]["nextDates"]) == 3

    assert client.get("/api/notices", headers=town.worker).json()["data"]["items"] == []  # drafts stay hidden
    _publish(client, town, draft)

    [item] = client.get("/api/notices", headers=town.worker).json()["data"]["items"]
    assert "correctIndex" not in item["question"] and item["opened"] is False
    target = {"kind": "notice", "id": item["id"]}
    assert client.post("/api/receipts", headers=town.worker, json={"target": target}).status_code == 200
    check = {"target": target, "language": "bn", "answers": [0]}
    res = client.post("/api/checks", headers=town.worker | _key(), json=check)
    assert res.status_code == 200, res.text
    assert res.json()["data"] == {"passed": True, "wrong": [], "signOff": None}

    [item] = client.get("/api/notices", headers=town.worker).json()["data"]["items"]
    assert item["opened"] is True and item["passed"] is True
    reach = client.get(f"/api/notices/{item['id']}/reach", headers=town.poster).json()["data"]
    assert reach == {"workers": 1, "opened": 1, "understood": 1, "notOpened": 0}


def test_a_wrong_answer_names_the_question(client: TestClient, town: Town, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("build_notice", notice_answer())
    draft = _draft(client, town)
    _publish(client, town, draft)

    check = {"target": {"kind": "notice", "id": draft["id"]}, "language": "bn", "answers": [2]}
    result = client.post("/api/checks", headers=town.worker | _key(), json=check).json()["data"]

    assert result["passed"] is False and [w["id"] for w in result["wrong"]] == ["q1"]


def test_publish_waits_for_every_date_to_be_confirmed(client: TestClient, town: Town, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("build_notice", notice_answer())
    draft = _draft(client, town)

    res = client.post(f"/api/notices/{draft['id']}/publish", headers=town.poster)

    assert res.status_code == 409 and res.json()["error"]["code"] == "EVENTS_NOT_CONFIRMED"


def test_a_past_date_cannot_be_confirmed(client: TestClient, town: Town, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("build_notice", notice_answer(starts_at=datetime(2020, 1, 1, 9, tzinfo=UTC)))
    draft = _draft(client, town)
    url = f"/api/notices/{draft['id']}/events/{draft['events'][0]['id']}"

    res = client.patch(url, headers=town.poster, json={"confirmed": True})

    assert res.status_code == 422 and res.json()["error"]["details"]["reason"] == "past"


def test_without_the_model_the_poster_gets_a_draft_to_finish(client: TestClient, town: Town) -> None:
    draft = _draft(client, town, text="ごみは朝8時までに出してください。")
    assert draft["needsEdit"] is True and draft["events"] == []

    res = client.post(f"/api/notices/{draft['id']}/publish", headers=town.poster)
    assert res.status_code == 409 and res.json()["error"]["code"] == "NEEDS_EDIT"

    question = {"id": "q1", "termId": None, "prompt": _tri("何時まで?"), "correctIndex": 0}
    fix = {
        "title": _tri("ごみ"),
        "steps": [_tri("朝8時までに出す")],
        "question": question | {"options": [_tri("8時"), _tri("10時"), _tri("夜")]},
    }
    assert client.patch(f"/api/notices/{draft['id']}", headers=town.poster, json=fix).status_code == 200
    assert client.post(f"/api/notices/{draft['id']}/publish", headers=town.poster).status_code == 200


def test_a_retried_draft_returns_the_first_one(client: TestClient, town: Town, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("build_notice", notice_answer())
    headers, body = town.poster | _key(), {"sourceKind": "text", "text": "訓練"}

    first = client.post("/api/notices", headers=headers, data=body).json()["data"]
    again = client.post("/api/notices", headers=headers, data=body).json()["data"]

    assert again["id"] == first["id"] and fake_ai.calls == 1


def test_a_flyer_photo_is_stored_without_its_exif(client: TestClient, town: Town, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("build_notice", notice_answer())
    payload = b"Exif\x00\x00GPS-SECRET"
    exif = b"\xff\xe1" + (len(payload) + 2).to_bytes(2) + payload  # a segment length counts its own 2 bytes
    jpeg = b"\xff\xd8" + exif + b"\xff\xdb\x00\x04\x00\x01" + b"\xff\xda\x00\x02pixels\xff\xd9"

    res = client.post(
        "/api/notices",
        headers=town.poster | _key(),
        data={"sourceKind": "photo"},
        files={"photo": ("f.jpg", jpeg, "image/jpeg")},
    )

    assert res.status_code == 201, res.text
    stored = client.get(res.json()["data"]["photo"], headers=town.worker)
    assert stored.status_code == 200 and stored.headers["content-type"] == "image/jpeg"
    assert b"GPS-SECRET" not in stored.content and stored.content.endswith(b"pixels\xff\xd9")


def test_a_photo_that_is_not_jpeg_is_refused(client: TestClient, town: Town) -> None:
    png = b"\x89PNG\r\n\x1a\n" + b"0" * 32

    res = client.post(
        "/api/notices",
        headers=town.poster | _key(),
        data={"sourceKind": "photo"},
        files={"photo": ("f.png", png, "image/png")},
    )

    assert res.status_code == 415


def test_workers_see_the_district_dates_in_a_range(client: TestClient, town: Town, fake_ai: FakeAiClient) -> None:
    fake_ai.queue("build_notice", notice_answer())
    _publish(client, town, _draft(client, town))
    # A day either side, so the Tokyo date of SOON is inside the range whatever zone the test runs in.
    params = {"from": str((SOON - timedelta(days=1)).date()), "to": str((SOON + timedelta(days=1)).date())}

    res = client.get("/api/events", headers=town.worker, params=params)
    too_long = client.get("/api/events", headers=town.worker, params={"from": "2026-01-01", "to": "2026-06-01"})

    assert res.status_code == 200, res.text
    assert len(res.json()["data"]) == 1
    assert too_long.status_code == 422


def test_another_district_cannot_see_or_change_a_notice(
    client: TestClient, db: Session, town: Town, fake_ai: FakeAiClient
) -> None:
    fake_ai.queue("build_notice", notice_answer())
    draft = _draft(client, town)
    _publish(client, town, draft)
    other, nid = _town(client, db), draft["id"]

    assert client.get("/api/notices", headers=other.worker).json()["data"]["items"] == []
    assert client.get("/api/notices", headers=other.poster).json()["data"]["items"] == []
    assert client.patch(f"/api/notices/{nid}", headers=other.poster, json={"category": "other"}).status_code == 404
    assert client.get(f"/api/notices/{nid}/reach", headers=other.poster).status_code == 404
    receipt = {"target": {"kind": "notice", "id": nid}}
    assert client.post("/api/receipts", headers=other.worker, json=receipt).status_code == 404


def test_workers_cannot_write_notices(client: TestClient, town: Town) -> None:
    res = client.post("/api/notices", headers=town.worker | _key(), data={"sourceKind": "text", "text": "x"})

    assert res.status_code == 403
