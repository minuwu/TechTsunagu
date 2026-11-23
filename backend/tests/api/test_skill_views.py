"""Skill Matrix, Heatmap and Skill Passport on the Work demo seed (architecture §6.6, §9.3)."""

import uuid
from datetime import UTC, datetime

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.seed import Refs, seed_work
from tests.factories import TEST_PEPPER


@pytest.fixture
def refs(db: Session) -> Refs:
    return seed_work(db, TEST_PEPPER, datetime.now(UTC))


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def test_the_matrix_shows_every_worker_on_every_machine(client: TestClient, refs: Refs) -> None:
    data = client.get("/api/skill-matrix", headers=_login(client, refs["f-yamaguchi"], "246810")).json()["data"]

    codes = {m["id"]: m["code"] for m in data["machines"]}
    cells = {(r["displayName"], codes[c["machineId"]]): c["status"] for r in data["rows"] for c in r["cells"]}
    assert len(data["rows"]) == 4 and len(cells) == 16
    assert cells[("Rahim Uddin", "plasma-1")] == "valid"
    assert cells[("Sadia Akter", "plasma-1")] == "expiring"  # 80 of 90 days
    assert cells[("Tanvir Hasan", "press-brake-2")] == "expired"  # 100 days
    assert cells[("Sadia Akter", "crane-3")] == "none"


def test_the_heatmap_counts_confusions_per_term_and_machine(client: TestClient, refs: Refs) -> None:
    worker = _login(client, refs["w-sadia"], "2468")
    check = {"target": {"kind": "note", "id": str(refs["n-crane-1"])}, "language": "bn", "answers": [0, 0]}
    res = client.post("/api/checks", headers=worker | {"Idempotency-Key": str(uuid.uuid4())}, json=check)
    assert res.status_code == 200 and res.json()["data"]["passed"] is False

    data = client.get("/api/heatmap", headers=_login(client, refs["f-yamaguchi"], "246810")).json()["data"]

    crane = str(refs["m-crane"])
    counts = {r["ja"]: next(c["count"] for c in r["cells"] if c["machineId"] == crane) for r in data["rows"]}
    assert len(counts) == 2 and set(counts.values()) == {1}  # both crane terms missed once; no other rows


def test_a_worker_sees_only_their_own_passport(client: TestClient, refs: Refs) -> None:
    rahim = _login(client, refs["w-rahim"], "2468")

    mine = client.get(f"/api/workers/{refs['w-rahim']}/passport", headers=rahim)
    theirs = client.get(f"/api/workers/{refs['w-sadia']}/passport", headers=rahim)

    assert mine.status_code == 200 and len(mine.json()["data"]["cells"]) == 4
    assert theirs.status_code == 403


def test_another_shop_sees_none_of_this(client: TestClient, refs: Refs) -> None:
    other = _login(client, refs["f-isahaya"], "135790")

    matrix = client.get("/api/skill-matrix", headers=other).json()["data"]

    assert all(r["displayName"] not in {"Rahim Uddin", "Sadia Akter"} for r in matrix["rows"])
    assert client.get(f"/api/workers/{refs['w-rahim']}/passport", headers=other).status_code == 404
    assert client.get("/api/heatmap", headers=other).json()["data"]["rows"] == []
