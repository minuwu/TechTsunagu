"""P0 demo path (architecture §7, §8, §9.3): note → approve → scan → Check → Confusion / Sign-off → Panel → board."""

import uuid
from dataclasses import dataclass

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.client import AiUnavailable
from app.ai.fake import FakeAiClient
from app.models.work import GlossaryTerm, Machine
from tests import factories as f

TRI = {"ja": "電源を切る", "bn": "পাওয়ার বন্ধ করুন", "en": "Turn off the power"}


def _option(word: str) -> dict[str, str]:
    return {"ja": word, "bn": word, "en": word}


def note_answer(transcript: str = "玉掛けの前に電源を切る") -> dict[str, object]:
    return {
        "transcript_ja": transcript,
        "steps": [
            {
                "text": {
                    "ja": "玉掛けの前に電源を切る",
                    "bn": "স্লিং-এর আগে পাওয়ার বন্ধ",
                    "en": "Before slinging, power off",
                },
                "icon": "lockout",
            }
        ],
        "back_translation": {"bn": "玉掛けの前に電源を切る", "en": "玉掛けの前に電源を切る"},
        "questions": [
            {
                "term_ja": "玉掛け",
                "prompt": _option("最初に何をする?"),
                "options": [_option("電源を切る"), _option("走る"), _option("待つ")],
                "correct_index": 0,
            },
            {
                "term_ja": None,
                "prompt": _option("どこで?"),
                "options": [_option("機械"), _option("家"), _option("駅")],
                "correct_index": 0,
            },
        ],
    }


@dataclass
class Shop:
    foreman: str
    worker: str
    worker_id: int
    machine: Machine
    term: GlossaryTerm


def _auth(token: str, key: str | None = None) -> dict[str, str]:
    headers = {"Authorization": f"Bearer {token}"}
    if key:
        headers["Idempotency-Key"] = key
    return headers


def _login(client: TestClient, user_id: int, pin: str) -> str:
    token: str = client.post("/api/auth/login", json={"userId": str(user_id), "pin": pin}).json()["data"]["token"]
    return token


@pytest.fixture
def shop(client: TestClient, db: Session) -> Shop:
    s = f.shop(db)
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    worker = f.user(db, s, name="Rahim", pin="2468", must_change=False)
    machine = f.machine(db, s, code="press-2")
    term = GlossaryTerm(shop_id=s.id, ja="玉掛け", reading="たまがけ", translations={"bn": "স্লিং", "en": "slinging"})
    db.add(term)
    db.flush()
    return Shop(_login(client, foreman.id, "739215"), _login(client, worker.id, "2468"), worker.id, machine, term)


def _record(client: TestClient, fake_ai: FakeAiClient, shop: Shop, key: str | None = None) -> dict[str, object]:
    fake_ai.queue("build_note", note_answer())
    res = client.post(
        f"/api/machines/{shop.machine.id}/notes",
        headers=_auth(shop.foreman, key or str(uuid.uuid4())),
        data={"text": "玉掛けの前に電源を切ること"},
    )
    assert res.status_code == 201, res.text
    body: dict[str, object] = res.json()["data"]
    return body


def _approve(client: TestClient, shop: Shop, note_id: object, kind: str = "safety") -> None:
    res = client.post(f"/api/note-versions/{note_id}/approve", headers=_auth(shop.foreman), json={"changeKind": kind})
    assert res.status_code == 200, res.text


def _check(client: TestClient, shop: Shop, note_id: object, answers: list[int]) -> dict[str, object]:
    res = client.post(
        "/api/checks",
        headers=_auth(shop.worker, str(uuid.uuid4())),
        json={"target": {"kind": "note", "id": str(note_id)}, "language": "bn", "answers": answers},
    )
    assert res.status_code == 200, res.text
    body: dict[str, object] = res.json()["data"]
    return body


def _panel(client: TestClient, shop: Shop) -> dict[str, object]:
    body: dict[str, object] = client.get(f"/api/machines/{shop.machine.id}/panel", headers=_auth(shop.foreman)).json()[
        "data"
    ]
    return body


def test_the_demo_path_from_voice_note_to_ready(client: TestClient, fake_ai: FakeAiClient, shop: Shop) -> None:
    draft = _record(client, fake_ai, shop)
    assert draft["status"] == "draft"
    assert draft["versionNo"] == 1
    assert draft["questions"][0]["termId"] == str(shop.term.id)  # type: ignore[index]

    _approve(client, shop, draft["id"])
    assert client.post(f"/api/machines/{shop.machine.id}/scan", headers=_auth(shop.worker)).status_code == 200
    assert _panel(client, shop)["state"] == "locked"
    assert _panel(client, shop)["worker"] == {"id": str(shop.worker_id), "displayName": "Rahim"}

    page = client.get(f"/api/machines/{shop.machine.id}", headers=_auth(shop.worker)).json()["data"]
    assert "correctIndex" not in str(page)

    failed = _check(client, shop, draft["id"], [1, 0])
    assert failed["passed"] is False
    assert [w["termId"] for w in failed["wrong"]] == [str(shop.term.id)]  # type: ignore[union-attr]
    assert "correctIndex" not in str(failed)
    confusions = client.get("/api/confusions?open=true", headers=_auth(shop.foreman)).json()["data"]["items"]
    assert [c["termId"] for c in confusions] == [str(shop.term.id)]

    passed = _check(client, shop, draft["id"], [0, 0])
    assert passed["passed"] is True
    assert passed["signOff"] is not None
    assert _panel(client, shop)["state"] == "ready"


def test_the_panel_answers_304_when_nothing_changed(client: TestClient, fake_ai: FakeAiClient, shop: Shop) -> None:
    first = client.get(f"/api/machines/{shop.machine.id}/panel", headers=_auth(shop.foreman))

    again = client.get(
        f"/api/machines/{shop.machine.id}/panel",
        headers={**_auth(shop.foreman), "If-None-Match": first.headers["ETag"]},
    )

    assert again.status_code == 304
    assert again.content == b""


def test_a_safety_change_sends_everyone_back_but_a_wording_change_does_not(
    client: TestClient, fake_ai: FakeAiClient, shop: Shop
) -> None:
    v1 = _record(client, fake_ai, shop)
    _approve(client, shop, v1["id"])
    client.post(f"/api/machines/{shop.machine.id}/scan", headers=_auth(shop.worker))
    _check(client, shop, v1["id"], [0, 0])

    _approve(client, shop, _record(client, fake_ai, shop)["id"], kind="wording")
    assert _panel(client, shop)["state"] == "ready"

    _approve(client, shop, _record(client, fake_ai, shop)["id"], kind="safety")
    assert _panel(client, shop)["state"] == "locked"
    assert _panel(client, shop)["reason"] == "expired"


def test_a_check_on_a_version_that_is_no_longer_current_is_refused(
    client: TestClient, fake_ai: FakeAiClient, shop: Shop
) -> None:
    v1 = _record(client, fake_ai, shop)
    _approve(client, shop, v1["id"])
    _approve(client, shop, _record(client, fake_ai, shop)["id"])

    res = client.post(
        "/api/checks",
        headers=_auth(shop.worker, str(uuid.uuid4())),
        json={"target": {"kind": "note", "id": str(v1["id"])}, "language": "bn", "answers": [0, 0]},
    )

    assert res.status_code == 409
    assert res.json()["error"]["code"] == "VERSION_CHANGED"


def test_a_retried_recording_returns_the_same_draft_without_a_second_ai_call(
    client: TestClient, fake_ai: FakeAiClient, shop: Shop
) -> None:
    key = str(uuid.uuid4())
    first = _record(client, fake_ai, shop, key)
    calls = fake_ai.calls

    again = client.post(
        f"/api/machines/{shop.machine.id}/notes",
        headers=_auth(shop.foreman, key),
        data={"text": "玉掛けの前に電源を切ること"},
    )

    assert again.status_code == 201
    assert again.json()["data"]["id"] == first["id"]
    assert fake_ai.calls == calls


def test_when_the_ai_fails_the_foreman_still_gets_an_editable_draft(
    client: TestClient, fake_ai: FakeAiClient, shop: Shop
) -> None:
    fake_ai.queue("build_note", AiUnavailable("down"))

    res = client.post(
        f"/api/machines/{shop.machine.id}/notes",
        headers=_auth(shop.foreman, str(uuid.uuid4())),
        data={"text": "刃を交換する"},
    )
    draft = res.json()["data"]
    approve = client.post(f"/api/note-versions/{draft['id']}/approve", headers=_auth(shop.foreman), json={})

    assert res.status_code == 201
    assert (draft["needsEdit"], draft["aiOutcome"], draft["transcriptJa"]) == (True, "degraded", "刃を交換する")
    assert approve.status_code == 409  # no Check Questions yet, so it cannot be approved


def test_a_foreman_of_another_shop_sees_nothing(client: TestClient, db: Session, shop: Shop) -> None:
    other = f.shop(db)
    stranger = f.user(db, other, role="foreman", pin="739215", must_change=False)
    token = _login(client, stranger.id, "739215")

    assert client.get(f"/api/machines/{shop.machine.id}", headers=_auth(token)).status_code == 404
    assert client.get(f"/api/machines/{shop.machine.id}/panel", headers=_auth(token)).status_code == 404


def test_the_board_flags_an_assigned_worker_without_a_sign_off(client: TestClient, shop: Shop) -> None:
    put = client.put(
        "/api/shift-assignments",
        headers=_auth(shop.foreman),
        json={"machineId": str(shop.machine.id), "shift": "day", "workerId": str(shop.worker_id)},
    )
    board = client.get("/api/board", headers=_auth(shop.foreman)).json()["data"]

    assert put.status_code == 200
    row = next(m for m in board["machines"] if m["id"] == str(shop.machine.id))
    assert row["assignments"][0]["status"] == "none"
    assert row["assignments"][0]["flag"] is True


def test_reusing_a_check_key_for_another_note_is_refused(client: TestClient, fake_ai: FakeAiClient, shop: Shop) -> None:
    v1 = _record(client, fake_ai, shop)
    _approve(client, shop, v1["id"])
    draft = _record(client, fake_ai, shop)  # never approved: must not be gradeable through a replayed key
    key = str(uuid.uuid4())
    body = {"language": "bn", "answers": [0, 0]}

    first = client.post(
        "/api/checks", headers=_auth(shop.worker, key), json={**body, "target": {"kind": "note", "id": str(v1["id"])}}
    )
    replay = client.post(
        "/api/checks",
        headers=_auth(shop.worker, key),
        json={**body, "target": {"kind": "note", "id": str(draft["id"])}},
    )

    assert first.status_code == 200
    assert replay.status_code == 409
    assert replay.json()["error"]["code"] == "IDEMPOTENCY_KEY_REUSED"


def test_an_oversized_body_is_refused_before_it_is_parsed(client: TestClient) -> None:
    big = b'{"userId": "1", "pin": "' + b"1" * (11 * 1024 * 1024) + b'"}'

    res = client.post("/api/auth/login", content=big, headers={"Content-Type": "application/json"})

    assert res.status_code == 413
    assert res.json()["error"]["code"] == "PAYLOAD_TOO_LARGE"


def test_a_worker_cannot_record_or_approve(client: TestClient, shop: Shop) -> None:
    res = client.post(
        f"/api/machines/{shop.machine.id}/notes", headers=_auth(shop.worker, str(uuid.uuid4())), data={"text": "x"}
    )

    assert res.status_code == 403
