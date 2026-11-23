"""The Work seed produces the demo story (plan "Seed story", BACKEND_PLAN §8.2)."""

from datetime import UTC, datetime

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.seed import Refs, seed_work
from app.services import floor
from app.services.auth import Principal
from tests.factories import TEST_PEPPER

NOW = datetime.now(UTC)


@pytest.fixture
def refs(db: Session) -> Refs:
    return seed_work(db, TEST_PEPPER, NOW)


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def test_the_demo_accounts_can_sign_in_by_shop_code(client: TestClient, refs: Refs) -> None:
    roster = client.get("/api/auth/roster", params={"code": "SASEBO01", "role": "worker"}).json()["data"]

    assert {w["displayName"] for w in roster} == {"Rahim Uddin", "Sadia Akter", "Tanvir Hasan", "田中 健"}
    _login(client, refs["w-rahim"], "2468")
    _login(client, refs["f-yamaguchi"], "246810")


def test_the_board_shows_every_case_the_demo_needs(client: TestClient, refs: Refs) -> None:
    board = client.get("/api/board", headers=_login(client, refs["f-yamaguchi"], "246810")).json()["data"]

    by_code = {m["code"]: m for m in board["machines"]}
    assert set(by_code) == {"plasma-1", "press-brake-2", "crane-3", "weld-4"}
    assert all(m["versionNo"] == 1 for m in board["machines"])
    seen = {code: [(a["displayName"], a["status"], a["flag"]) for a in m["assignments"]] for code, m in by_code.items()}
    assert ("Sadia Akter", "none", True) in seen["crane-3"]  # assigned without a Sign-off: flagged
    assert ("Tanvir Hasan", "expired", True) in seen["press-brake-2"]  # 100 days old
    assert ("Rahim Uddin", "valid", False) in seen["plasma-1"]


@pytest.mark.parametrize("bangladesh_hour", [10, 16, 20, 22, 23])
def test_the_board_is_filled_at_any_hour_testers_use_in_bangladesh(
    client: TestClient, db: Session, bangladesh_hour: int
) -> None:
    # Seeded at 09:00 Bangladesh time; testers click later the same evening, after Tokyo's midnight too (21:00 BST).
    seeded_at = datetime(2026, 11, 24, 3, 0, tzinfo=UTC)
    refs = seed_work(db, TEST_PEPPER, seeded_at)
    later = datetime(2026, 11, 24, bangladesh_hour - 6, 30, tzinfo=UTC)

    who = Principal("foreman", refs["f-yamaguchi"], refs["s-sasebo"], refs["d-minato"], None, False, b"")
    board = floor.board(db, who, later)

    assert all(m.assignments for m in board.machines)


def test_the_other_shop_is_isolated(client: TestClient, refs: Refs) -> None:
    other = _login(client, refs["f-isahaya"], "135790")

    machines = client.get("/api/machines", headers=other).json()["data"]["items"]

    assert [m["code"] for m in machines] == ["lathe-1"]
    assert client.get(f"/api/machines/{refs['m-plasma']}", headers=other).status_code == 404


def test_every_seeded_note_reads_back_through_the_api(client: TestClient, refs: Refs) -> None:
    foreman, worker = _login(client, refs["f-yamaguchi"], "246810"), _login(client, refs["w-rahim"], "2468")
    machines = client.get("/api/machines", headers=foreman).json()["data"]["items"]
    assert machines

    for m in machines:
        versions = client.get(f"/api/machines/{m['id']}/versions", headers=foreman)
        page = client.get(f"/api/machines/{m['id']}", headers=worker)
        assert versions.status_code == 200, versions.text
        assert page.status_code == 200, page.text
