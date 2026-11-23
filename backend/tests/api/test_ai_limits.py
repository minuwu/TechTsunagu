"""One user's AI allowance over HTTP (architecture §10.3): AI answers are refused with 429, but a message or a safety
report is still taken, untranslated."""

import uuid
from datetime import UTC, datetime, timedelta

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from app.models.scopes import Shop, User
from app.repositories import accounts
from app.services.ai_runner import USER_LIMITS
from tests import factories as f

WEBM = b"\x1a\x45\xdf\xa3" + b"0" * 64


@pytest.fixture
def worker(db: Session) -> User:
    """A Worker who has used up this minute's AI requests."""
    user = f.user(db, f.shop(db), pin="2468", must_change=False)
    for _ in range(USER_LIMITS["free"][0]):
        accounts.count_attempt(db, f"ai-minute:user:{user.id}", datetime.now(UTC), timedelta(minutes=1))
    db.commit()
    return user


@pytest.fixture
def headers(client: TestClient, worker: User) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(worker.id), "pin": "2468"})
    return {"Authorization": f"Bearer {res.json()['data']['token']}", "Idempotency-Key": str(uuid.uuid4())}


def test_an_ask_over_the_limit_is_rate_limited(
    client: TestClient, headers: dict[str, str], fake_ai: FakeAiClient
) -> None:
    res = client.post("/api/asks", headers=headers, data={"text": "When is garbage day?"})

    assert (res.status_code, res.json()["error"]["code"]) == (429, "RATE_LIMITED")
    assert res.headers["Retry-After"] == "60"
    assert fake_ai.calls == 0


@pytest.mark.parametrize("voice", [False, True])
def test_a_near_miss_report_is_never_refused(
    client: TestClient, db: Session, headers: dict[str, str], worker: User, fake_ai: FakeAiClient, voice: bool
) -> None:
    shop = db.get_one(Shop, worker.shop_id)
    form = {"machineId": str(f.machine(db, shop).id)}

    res = (
        client.post("/api/near-misses", headers=headers, data=form, files={"audio": ("a.webm", WEBM)})
        if voice
        else client.post("/api/near-misses", headers=headers, data=form | {"text": "The guard was off"})
    )

    assert res.status_code == 201, res.text
    assert fake_ai.calls == 0


def test_a_typed_chat_message_is_sent_untranslated_but_a_recording_is_refused(
    client: TestClient, db: Session, headers: dict[str, str], worker: User
) -> None:
    shop = db.get_one(Shop, worker.shop_id)
    url = f"/api/threads/{f.thread(db, shop, worker).id}/messages"

    typed = client.post(url, headers=headers, data={"text": "আমি বুঝেছি"})
    voice = headers | {"Idempotency-Key": str(uuid.uuid4())}
    spoken = client.post(url, headers=voice, files={"audio": ("a.webm", WEBM)})

    assert typed.status_code == 201, typed.text
    assert spoken.status_code == 429  # nothing is stored; the app keeps the recording and retries
