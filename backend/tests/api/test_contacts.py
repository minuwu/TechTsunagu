"""XREQ-017 items 1-2 (ADR 0014): My QR, Contacts across shops, blocks, Contact Threads translated per side."""

from dataclasses import dataclass

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from tests import factories as f


@dataclass
class People:
    rahim: dict[str, str]  # a Worker in shop 1 (language bn)
    nguyen: dict[str, str]  # a Worker in another shop (language en)
    foreman: dict[str, str]  # Rahim's Foreman


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


@pytest.fixture
def people(client: TestClient, db: Session) -> People:
    home, other = f.shop(db), f.shop(db)
    rahim = f.user(db, home, pin="2468", must_change=False)
    nguyen = f.user(db, other, pin="2468", must_change=False)
    nguyen.language = "en"
    db.flush()
    foreman = f.user(db, home, role="foreman", pin="739215", must_change=False)
    return People(
        _login(client, rahim.id, "2468"), _login(client, nguyen.id, "2468"), _login(client, foreman.id, "739215")
    )


def _qr(client: TestClient, who: dict[str, str], rotate: bool = False) -> str | None:
    token: str | None = client.post("/api/me/qr", headers=who, json={"rotate": rotate}).json()["data"]["token"]
    return token


def _connect(client: TestClient, people: People) -> tuple[str, str]:
    token = _qr(client, people.nguyen)
    assert token is not None
    added = client.post("/api/contacts", headers=people.rahim, json={"token": token})
    assert added.status_code == 201, added.text
    [back] = client.get("/api/contacts", headers=people.nguyen).json()["data"]["items"]
    return added.json()["data"]["id"], back["id"]


def test_the_qr_token_is_shown_once_and_rotating_kills_the_old_one(client: TestClient, people: People) -> None:
    first = _qr(client, people.nguyen)
    assert first is not None and _qr(client, people.nguyen) is None  # kept on the phone, not shown again
    preview = client.get(f"/api/contacts/resolve?token={first}", headers=people.rahim).json()["data"]
    assert set(preview) == {"displayName", "language"}  # never an id or a shop

    new = _qr(client, people.nguyen, rotate=True)

    assert new is not None and new != first
    assert client.get(f"/api/contacts/resolve?token={first}", headers=people.rahim).status_code == 404


def test_scanning_connects_both_sides_across_shops(client: TestClient, people: People) -> None:
    mine, theirs = _connect(client, people)
    token = _qr(client, people.nguyen, rotate=True)

    again = client.post("/api/contacts", headers=people.rahim, json={"token": token})
    own = client.post("/api/contacts", headers=people.nguyen, json={"token": token})

    assert mine and theirs and again.status_code == 409 and own.status_code == 403


def test_contact_messages_are_translated_for_the_reader(
    client: TestClient, people: People, fake_ai: FakeAiClient
) -> None:
    mine, theirs = _connect(client, people)
    fake_ai.queue("translate", {"text": "Are you coming to the festival?"})
    body = {"text": "উৎসবে আসবে?", "lang": "bn"}

    sent = client.post(f"/api/contacts/{mine}/messages", headers=people.rahim, json=body)

    assert sent.status_code == 201 and sent.json()["data"]["fromMe"] is True
    [listed] = client.get("/api/contacts", headers=people.nguyen).json()["data"]["items"]
    assert listed["unread"] == 1
    first = client.get(f"/api/contacts/{theirs}/messages", headers=people.nguyen)
    [msg] = first.json()["data"]["items"]
    assert msg["text"] == "Are you coming to the festival?" and msg["textSrc"] == "উৎসবে আসবে?"
    again = client.get(
        f"/api/contacts/{theirs}/messages", headers=people.nguyen | {"If-None-Match": first.headers["ETag"]}
    )
    assert again.status_code == 304
    [listed] = client.get("/api/contacts", headers=people.nguyen).json()["data"]["items"]
    assert listed["unread"] == 0


def test_without_the_model_the_reader_gets_the_original(client: TestClient, people: People) -> None:
    mine, theirs = _connect(client, people)
    client.post(f"/api/contacts/{mine}/messages", headers=people.rahim, json={"text": "ধন্যবাদ", "lang": "bn"})

    [msg] = client.get(f"/api/contacts/{theirs}/messages", headers=people.nguyen).json()["data"]["items"]

    assert msg["text"] == "ধন্যবাদ"


def test_a_block_is_silent(client: TestClient, people: People, fake_ai: FakeAiClient) -> None:
    mine, theirs = _connect(client, people)
    assert client.post("/api/blocks", headers=people.nguyen, json={"contactId": theirs}).status_code == 200

    sent = client.post(f"/api/contacts/{mine}/messages", headers=people.rahim, json={"text": "hi", "lang": "en"})

    assert sent.status_code == 201  # the blocked side is not told
    assert fake_ai.calls == 0  # nothing is translated for nobody
    assert client.get("/api/contacts", headers=people.nguyen).json()["data"]["items"] == []
    assert client.get(f"/api/contacts/{theirs}/messages", headers=people.nguyen).status_code == 404
    client.delete(f"/api/blocks/{theirs}", headers=people.nguyen)
    assert client.get(f"/api/contacts/{theirs}/messages", headers=people.nguyen).json()["data"]["items"] == []


def test_the_employer_cannot_read_personal_threads(client: TestClient, people: People) -> None:
    mine, _ = _connect(client, people)

    assert client.get(f"/api/contacts/{mine}/messages", headers=people.foreman).status_code == 404
    assert client.get("/api/contacts", headers=people.foreman).json()["data"]["items"] == []
