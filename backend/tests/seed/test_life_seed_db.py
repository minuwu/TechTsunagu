"""The Life seed loads life.json into the database on top of the Work seed (plan "Seed story")."""

from datetime import UTC, datetime

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.life import Event, Meetup, Notice, SenpaiPair
from app.models.work import Receipt
from app.seed import Refs, seed_work
from app.seed_life import seed_life
from app.services.credentials import token_digest
from tests.factories import TEST_PEPPER

NOW = datetime(2026, 11, 24, 3, 0, tzinfo=UTC)


@pytest.fixture
def refs(db: Session) -> Refs:
    return seed_life(db, TEST_PEPPER, seed_work(db, TEST_PEPPER, NOW), NOW)


def test_posters_sign_in_by_district_code(client: TestClient, refs: Refs) -> None:
    roster = client.get("/api/auth/roster", params={"code": "MINATO26", "role": "poster"}).json()["data"]

    assert {p["displayName"] for p in roster} == {"自治会 事務局", "国際交流 担当"}
    res = client.post("/api/auth/login", json={"userId": str(refs["u-jichikai"]), "pin": "482615"})
    assert res.status_code == 200, res.text


def test_notices_are_published_with_their_events(db: Session, refs: Refs) -> None:
    notices = db.scalars(select(Notice).where(Notice.district_id == refs["d-minato"])).all()

    assert {n.status for n in notices} == {"published"}
    assert len(notices) == 3
    assert all(n.published_at is not None and n.question["correctIndex"] == 0 for n in notices)
    assert db.scalar(select(func.count()).select_from(Event).where(Event.confirmed)) == 3


def test_senpai_pairs_open_with_the_demo_tokens(db: Session, refs: Refs) -> None:
    pair = db.scalar(select(SenpaiPair).where(SenpaiPair.token_hash == token_digest("demo-tanaka-7f3a9c")))

    assert pair is not None and pair.worker_id == refs["w-rahim"] and pair.expires_at > NOW
    assert db.scalar(select(func.count()).select_from(Meetup)) == 2
    assert db.scalar(select(func.count()).select_from(Receipt).where(Receipt.notice_id.is_not(None))) == 3
