"""The Chat seed loads the prototype's Line 2 Thread, its messages and Tasks k1-k3 onto the Work seed."""

from datetime import UTC, datetime

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.seed import Refs, seed_work
from app.seed_chat import seed_chat
from tests.factories import TEST_PEPPER


@pytest.fixture
def refs(db: Session) -> Refs:
    return seed_chat(db, seed_work(db, TEST_PEPPER, datetime.now(UTC)), datetime.now(UTC))


def _login(client: TestClient, user_id: int, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def test_rahim_sees_the_line_thread_and_can_answer_the_oil_check(client: TestClient, refs: Refs) -> None:
    rahim = _login(client, refs["w-rahim"], "2468")

    threads = client.get("/api/threads", headers=rahim).json()["data"]
    items = client.get(f"/api/threads/{refs['t-line2']}/messages", headers=rahim).json()["data"]["items"]
    first = next(m for m in items if m["id"] == str(refs["m1"]))

    assert {t["id"] for t in threads} == {str(refs["t-line2"]), str(refs["t-rahim"])}
    assert len(items) == 7 and first["myStatus"] is None and "correctIndex" not in first["questions"][0]
    res = client.post(f"/api/messages/{refs['m1']}/answers", headers=rahim, json={"answers": [0]})
    assert res.status_code == 200 and res.json()["data"]["status"] == "green"


def test_the_board_has_the_three_tasks_and_seeded_answers(client: TestClient, refs: Refs) -> None:
    foreman = _login(client, refs["f-yamaguchi"], "246810")

    tasks = client.get("/api/tasks", headers=foreman).json()["data"]["items"]
    sadia = _login(client, refs["w-sadia"], "2468")
    items = client.get(f"/api/threads/{refs['t-line2']}/messages", headers=sadia).json()["data"]["items"]

    assert sorted(t["column"] for t in tasks) == ["doing", "done", "todo"]
    assert next(m for m in items if m["id"] == str(refs["m1"]))["myStatus"] == "red"
