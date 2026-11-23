"""`python -m app.cli reset-pin`: the operator way back in when nobody in the shop can (architecture §3.4)."""

from datetime import UTC, datetime, timedelta
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import select
from sqlalchemy.orm import Session

from app import cli
from app.config import Settings
from app.domain.pins import pin_problem
from app.models.scopes import AuditEvent, Poster, User
from tests import factories as f


@pytest.fixture(autouse=True)
def same_database(monkeypatch: pytest.MonkeyPatch, db: Session, settings: Settings) -> None:
    def factory(_: object) -> object:
        return db.info["factory"]

    monkeypatch.setattr(cli, "Settings", lambda: settings)
    monkeypatch.setattr(cli, "make_session_factory", factory)


def _run(capsys: pytest.CaptureFixture[str], code: str, role: str, name: str) -> str:
    cli.main(["reset-pin", "--code", code, "--role", role, "--name", name])
    return capsys.readouterr().out.strip().split()[-1]


def _login(client: TestClient, user: User, pin: str) -> dict[str, Any]:
    res = client.post("/api/auth/login", json={"userId": str(user.id), "pin": pin})
    assert res.status_code == 200, res.text
    data: dict[str, Any] = res.json()["data"]
    return data


def test_a_locked_out_foreman_gets_a_new_first_pin(
    client: TestClient, db: Session, capsys: pytest.CaptureFixture[str]
) -> None:
    s = f.shop(db)
    foreman = f.user(db, s, role="foreman", name="山口 隆", pin="739215", must_change=False)
    old = _login(client, foreman, "739215")["token"]
    foreman.pin_locked_until = datetime.now(UTC) + timedelta(minutes=10)
    db.commit()

    pin = _run(capsys, s.code, "foreman", "山口 隆")

    assert pin_problem("foreman", pin) is None
    db.expire_all()
    assert _login(client, foreman, pin)["mustChangePin"] is True  # the lockout is gone
    assert client.get("/api/auth/me", headers={"Authorization": f"Bearer {old}"}).status_code == 401
    audit = db.scalars(select(AuditEvent).where(AuditEvent.action == "pin_reset")).one()
    assert (audit.actor_user_id, audit.subject_id, audit.shop_id) == (None, foreman.id, s.id)


def test_a_poster_is_found_by_the_district_code(
    client: TestClient, db: Session, capsys: pytest.CaptureFixture[str]
) -> None:
    d = f.district(db)
    organisation = Poster(district_id=d.id, name="自治会", kind="jichikai")
    db.add(organisation)
    db.flush()
    poster = User(role="poster", display_name="事務局", language="ja", district_id=d.id, poster_id=organisation.id)
    poster.pin_hash = "unknown"
    db.add(poster)
    db.commit()

    pin = _run(capsys, d.code, "poster", "事務局")

    assert _login(client, poster, pin)["mustChangePin"] is True


@pytest.mark.parametrize(("code", "name"), [("NOSUCHXX", "Rahim"), (None, "Nobody")])
def test_an_unknown_person_changes_nothing(db: Session, code: str | None, name: str) -> None:
    s = f.shop(db)
    worker = f.user(db, s, name="Rahim")
    before = worker.pin_hash
    db.commit()

    with pytest.raises(SystemExit, match="No active worker"):
        cli.main(["reset-pin", "--code", code or s.code, "--role", "worker", "--name", name])

    db.refresh(worker)
    assert worker.pin_hash == before
