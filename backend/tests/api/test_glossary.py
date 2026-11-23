"""PATCH /glossary/{id} (XREQ-011 item 4): fix a term's reading or add a translation per shop language."""

from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from tests import factories as f


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def test_a_foreman_edits_a_term_and_adds_a_language(client: TestClient, db: Session) -> None:
    s = f.shop(db)
    foreman = _login(client, f.user(db, s, role="foreman", pin="739215", must_change=False).id, "739215")
    worker = _login(client, f.user(db, s, pin="2468", must_change=False).id, "2468")
    body = {"ja": "玉掛け", "reading": "たまかけ", "bn": "স্লিং", "en": "slinging"}
    term = client.post("/api/glossary", headers=foreman, json=body).json()["data"]
    client.post("/api/glossary", headers=foreman, json=body | {"ja": "金型", "reading": "かながた"})

    res = client.patch(
        f"/api/glossary/{term['id']}", headers=foreman, json={"reading": "たまがけ", "translations": {"vi": "móc cáp"}}
    )

    assert res.status_code == 200, res.text
    data = res.json()["data"]
    assert data["reading"] == "たまがけ" and data["translations"] == {"bn": "স্লিং", "en": "slinging", "vi": "móc cáp"}
    assert data["bn"] == "স্লিং"  # the existing fields stay for current clients
    url = f"/api/glossary/{term['id']}"
    assert client.patch(url, headers=foreman, json={"ja": "金型"}).status_code == 409  # already a term
    assert client.patch(url, headers=foreman, json={"translations": {"en": " "}}).status_code == 422
    assert client.patch(url, headers=worker, json={"reading": "x"}).status_code == 403


def test_another_shop_cannot_edit_the_term(client: TestClient, db: Session) -> None:
    s, other = f.shop(db), f.shop(db)
    foreman = _login(client, f.user(db, s, role="foreman", pin="739215", must_change=False).id, "739215")
    stranger = _login(client, f.user(db, other, role="foreman", pin="135790", must_change=False).id, "135790")
    body = {"ja": "玉掛け", "reading": "たまかけ", "bn": "স্লিং", "en": "slinging"}
    term = client.post("/api/glossary", headers=foreman, json=body).json()["data"]

    assert client.patch(f"/api/glossary/{term['id']}", headers=stranger, json={"reading": "x"}).status_code == 404
