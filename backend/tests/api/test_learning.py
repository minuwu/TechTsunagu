"""XREQ-020: a Worker reads their own Learning History; the Foreman reads the understanding analytics."""

from datetime import UTC, datetime, timedelta

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.models.chat import FollowUp
from app.seed import Refs, seed_work
from app.seed_chat import seed_chat
from tests.factories import TEST_PEPPER


@pytest.fixture
def refs(db: Session) -> Refs:
    now = datetime.now(UTC)
    return seed_chat(db, seed_work(db, TEST_PEPPER, now), now)


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _follow_up(db: Session, refs: Refs, worker: str, term: str, status: str, resolved: datetime | None) -> None:
    db.add(
        FollowUp(
            shop_id=refs["s-sasebo"],
            message_id=refs["m1"],
            user_id=refs[worker],
            term_ja=term,
            status=status,
            resolved_at=resolved,
            resolved_by=refs["f-yamaguchi"] if status == "clarified" else None,
        )
    )
    db.flush()


def test_a_worker_sees_only_their_own_learning_history(client: TestClient, db: Session, refs: Refs) -> None:
    now = datetime.now(UTC)
    _follow_up(db, refs, "w-rahim", "金型", "open", None)
    _follow_up(db, refs, "w-rahim", "段取り", "clarified", now)
    _follow_up(db, refs, "w-rahim", "保護具", "understood", now + timedelta(minutes=1))
    _follow_up(db, refs, "w-sadia", "油圧", "open", None)
    rahim = _login(client, refs["w-rahim"], "2468")

    data = client.get("/api/workers/me/learning", headers=rahim).json()["data"]

    by_term = {i["termJa"]: i for i in data["items"]}
    assert set(by_term) == {"金型", "段取り", "保護具"}  # never Sadia's
    assert by_term["金型"]["status"] == "open" and by_term["金型"]["clarifiedAt"] is None
    assert by_term["段取り"]["clarifiedAt"] is not None and by_term["段取り"]["understoodAt"] is None
    assert by_term["保護具"]["understoodAt"] is not None and by_term["保護具"]["missedAt"]
    assert data["streakDays"] == 0 and data["nextCursor"] is None


def test_a_green_answer_today_starts_a_streak(client: TestClient, refs: Refs) -> None:
    rahim = _login(client, refs["w-rahim"], "2468")
    res = client.post(f"/api/messages/{refs['m1']}/answers", headers=rahim, json={"answers": [0]})
    assert res.json()["data"]["status"] == "green"

    assert client.get("/api/workers/me/learning", headers=rahim).json()["data"]["streakDays"] == 1


def test_learning_history_pages_by_cursor(client: TestClient, db: Session, refs: Refs) -> None:
    for term in ("一", "二", "三"):
        _follow_up(db, refs, "w-rahim", term, "open", None)
    rahim = _login(client, refs["w-rahim"], "2468")

    first = client.get("/api/workers/me/learning?limit=2", headers=rahim).json()["data"]
    rest = client.get(f"/api/workers/me/learning?limit=2&cursor={first['nextCursor']}", headers=rahim).json()["data"]

    assert [i["termJa"] for i in first["items"] + rest["items"]] == ["三", "二", "一"]
    assert rest["nextCursor"] is None


def test_the_foreman_reads_the_trend_per_worker_and_top_terms(client: TestClient, db: Session, refs: Refs) -> None:
    _follow_up(db, refs, "w-sadia", "金型", "open", None)
    _follow_up(db, refs, "w-rahim", "金型", "open", None)
    _follow_up(db, refs, "w-rahim", "段取り", "open", None)
    foreman = _login(client, refs["f-yamaguchi"], "246810")

    data = client.get("/api/analytics/understanding?days=7", headers=foreman).json()["data"]

    assert data["days"] == 7 and len(data["trend"]) == 7
    totals = {k: sum(d[k] for d in data["trend"]) for k in ("green", "orange", "red")}
    assert totals == {"green": 1, "orange": 0, "red": 1}  # seeded m1: Tanvir right, Sadia wrong
    workers = {w["displayName"]: w for w in data["perWorker"]}
    assert workers["Tanvir Hasan"]["green"] == 1 and workers["Sadia Akter"]["red"] == 1
    assert (workers["Rahim Uddin"]["green"], workers["Rahim Uddin"]["red"]) == (0, 0)
    assert data["topTerms"][0] == {"termJa": "金型", "count": 2}


def test_analytics_is_for_the_foreman_and_stays_in_the_shop(client: TestClient, refs: Refs) -> None:
    rahim = _login(client, refs["w-rahim"], "2468")
    other = _login(client, refs["f-isahaya"], "135790")

    assert client.get("/api/analytics/understanding", headers=rahim).status_code == 403
    assert client.get("/api/analytics/understanding?days=91", headers=other).status_code == 422
    data = client.get("/api/analytics/understanding", headers=other).json()["data"]
    assert data["days"] == 30 and data["topTerms"] == [] and sum(d["red"] for d in data["trend"]) == 0


def test_the_learning_route_is_for_workers(client: TestClient, refs: Refs) -> None:
    foreman = _login(client, refs["f-yamaguchi"], "246810")
    assert client.get("/api/workers/me/learning", headers=foreman).status_code == 403
