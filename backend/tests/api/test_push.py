"""XREQ-014: push on a sent Chat message and on a Nudge. Ids only, never the message text; one push per message;
dead tokens are removed; signed-out or muted phones get nothing."""

import json
import uuid
from collections.abc import Iterator
from datetime import UTC, datetime
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import select
from sqlalchemy.orm import Session, sessionmaker

from app.ai.fake import FakeAiClient
from app.config import Settings
from app.main import create_app
from app.models.platform import Device
from app.push import Outcome
from app.seed import Refs, seed_work
from app.seed_chat import seed_chat
from tests.factories import TEST_PEPPER


class Recorder:
    """Stands in for FCM: records what would be sent and answers with a scripted outcome per token."""

    def __init__(self) -> None:
        self.sent: list[tuple[str, dict[str, Any]]] = []
        self.answers: dict[str, Outcome] = {}

    def send(self, token: str, message: dict[str, Any]) -> Outcome:
        self.sent.append((token, message))
        return self.answers.get(token, "sent")


@pytest.fixture
def recorder() -> Recorder:
    return Recorder()


@pytest.fixture
def api(settings: Settings, db: Session, fake_ai: FakeAiClient, recorder: Recorder) -> Iterator[TestClient]:
    factory: sessionmaker[Session] = db.info["factory"]
    app = create_app(settings, session_factory=factory, ai_client=fake_ai, push_client=recorder)
    with TestClient(app, raise_server_exceptions=False) as c:
        yield c


@pytest.fixture
def refs(db: Session) -> Refs:
    now = datetime.now(UTC)
    return seed_chat(db, seed_work(db, TEST_PEPPER, now), now)


def _login(api: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = api.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _phone(api: TestClient, who: dict[str, str], token: str, *, enabled: bool = True) -> None:
    body = {
        "installId": str(uuid.uuid4()),
        "platform": "android",
        "fcmToken": token,
        "appVersion": "1.0.0",
        "notificationsEnabled": enabled,
    }
    assert api.put("/api/devices/me", headers=who, json=body).status_code == 200


def _say(api: TestClient, who: dict[str, str], thread_id: int, text: str, key: str | None = None) -> dict[str, Any]:
    headers = who | {"Idempotency-Key": key or str(uuid.uuid4())}
    res = api.post(f"/api/threads/{thread_id}/messages", headers=headers, data={"text": text})
    assert res.status_code == 201, res.text
    data: dict[str, Any] = res.json()["data"]
    return data


def test_a_worker_message_pushes_ids_only_to_the_other_members(api: TestClient, refs: Refs, recorder: Recorder) -> None:
    rahim, foreman = _login(api, refs["w-rahim"], "2468"), _login(api, refs["f-yamaguchi"], "246810")
    _phone(api, rahim, "tok-rahim")
    _phone(api, foreman, "tok-foreman")
    _phone(api, _login(api, refs["w-sadia"], "2468"), "tok-sadia", enabled=False)  # notifications off

    sent = _say(api, rahim, refs["t-line2"], "My glove is torn")

    assert [t for t, _ in recorder.sent] == ["tok-foreman"]  # not the sender, not a muted phone
    message = recorder.sent[0][1]
    assert message["data"] == {
        **message["data"],
        "v": "1",
        "type": "chat_message",
        "threadId": str(refs["t-line2"]),
        "messageId": sent["id"],
    }
    assert all(isinstance(v, str) for v in message["data"].values())
    assert "Rahim Uddin" in message["notification"]["title"] and "さん" in message["notification"]["title"]  # ja
    assert "glove" not in json.dumps(message)  # never the message text
    assert message["android"]["notification"]["channel_id"] == "messages"


def test_a_retried_post_does_not_push_twice(api: TestClient, refs: Refs, recorder: Recorder) -> None:
    rahim = _login(api, refs["w-rahim"], "2468")
    _phone(api, _login(api, refs["f-yamaguchi"], "246810"), "tok-foreman")
    key = str(uuid.uuid4())

    first, again = _say(api, rahim, refs["t-line2"], "hello", key), _say(api, rahim, refs["t-line2"], "hello", key)

    assert first["id"] == again["id"] and len(recorder.sent) == 1


def test_a_nudge_pushes_to_that_worker_only(api: TestClient, refs: Refs, recorder: Recorder) -> None:
    foreman = _login(api, refs["f-yamaguchi"], "246810")
    _phone(api, _login(api, refs["w-rahim"], "2468"), "tok-rahim")
    _phone(api, _login(api, refs["w-sadia"], "2468"), "tok-sadia")

    res = api.post(f"/api/messages/{refs['m1']}/nudge", headers=foreman, json={"userId": str(refs["w-rahim"])})

    assert res.status_code == 200
    [(target, message)] = recorder.sent
    assert target == "tok-rahim" and message["data"]["type"] == "nudge"
    assert message["data"]["messageId"] == str(refs["m1"]) and "山口 隆" in message["notification"]["body"]


def test_a_dead_token_is_removed(api: TestClient, db: Session, refs: Refs, recorder: Recorder) -> None:
    _phone(api, _login(api, refs["f-yamaguchi"], "246810"), "tok-dead")
    recorder.answers["tok-dead"] = "gone"

    _say(api, _login(api, refs["w-rahim"], "2468"), refs["t-line2"], "hi")

    assert db.scalar(select(Device).where(Device.fcm_token == "tok-dead")) is None  # noqa: S105  # a fake FCM token


def test_a_signed_out_phone_gets_nothing(api: TestClient, refs: Refs, recorder: Recorder) -> None:
    foreman = _login(api, refs["f-yamaguchi"], "246810")
    _phone(api, foreman, "tok-foreman")
    assert api.post("/api/auth/logout", headers=foreman).status_code == 200

    _say(api, _login(api, refs["w-rahim"], "2468"), refs["t-line2"], "hi")

    assert recorder.sent == []


def test_an_unsent_foreman_draft_pushes_nothing(api: TestClient, refs: Refs, recorder: Recorder) -> None:
    _phone(api, _login(api, refs["w-rahim"], "2468"), "tok-rahim")

    draft = _say(api, _login(api, refs["f-yamaguchi"], "246810"), refs["t-line2"], "明日の朝礼は8時")

    assert draft["stage"] < 4 and recorder.sent == []
