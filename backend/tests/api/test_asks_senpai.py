"""WS11 Life flow (architecture §7.6, §9.3): grounded Asks → forward → Senpai link → reply; Meet-ups; Support Log."""

import uuid
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.ai.fake import FakeAiClient
from app.models.life import Notice
from app.models.scopes import Poster, User
from tests import factories as f

Json = dict[str, Any]
TODAY = datetime.now(UTC).date()


def _tri(word: str) -> dict[str, str]:
    return {"ja": word, "bn": f"{word}-bn", "en": f"{word}-en"}


@dataclass
class Shop:
    foreman: dict[str, str]
    worker: dict[str, str]
    worker_id: int
    notice_id: int


def _login(client: TestClient, user: User, pin: str) -> dict[str, str]:
    res = client.post("/api/auth/login", json={"userId": str(user.id), "pin": pin})
    assert res.status_code == 200, res.text
    return {"Authorization": f"Bearer {res.json()['data']['token']}"}


def _key() -> dict[str, str]:
    return {"Idempotency-Key": str(uuid.uuid4())}


def _shop(client: TestClient, db: Session) -> Shop:
    s = f.shop(db)
    foreman = f.user(db, s, role="foreman", pin="739215", must_change=False)
    worker = f.user(db, s, pin="2468", must_change=False)
    poster = Poster(district_id=s.default_district_id, name="自治会", kind="jichikai")
    db.add(poster)
    db.flush()
    author = User(
        role="poster",
        display_name=f"事務局-{poster.id}",
        language="ja",
        district_id=s.default_district_id,
        poster_id=poster.id,
        pin_hash=worker.pin_hash,
        pin_must_change=False,
    )
    db.add(author)
    db.flush()
    notice = Notice(
        district_id=s.default_district_id,
        poster_id=poster.id,
        created_by=author.id,
        category="garbage",
        source_kind="text",
        source_ja="燃やすごみは朝8時までに出してください。",
        title=_tri("ごみの出し方"),
        steps=[_tri("朝8時までに出す")],
        question={"id": "q1", "termId": None, "prompt": _tri("何時?"), "options": [_tri("8時")] * 3, "correctIndex": 0},
        status="published",
        idempotency_key=uuid.uuid4(),
        published_at=datetime.now(UTC),
    )
    db.add(notice)
    db.flush()
    return Shop(_login(client, foreman, "739215"), _login(client, worker, "2468"), worker.id, notice.id)


@pytest.fixture
def shop(client: TestClient, db: Session) -> Shop:
    return _shop(client, db)


def _ask(client: TestClient, shop: Shop, text: str = "ごみは何時までですか?") -> Json:
    res = client.post("/api/asks", headers=shop.worker | _key(), data={"text": text})
    assert res.status_code == 201, res.text
    data: Json = res.json()["data"]
    return data


def _pair(client: TestClient, shop: Shop) -> Json:
    body = {"workerId": str(shop.worker_id), "senpaiName": "田中 健", "kind": "coworker"}
    res = client.post("/api/senpai-pairs", headers=shop.foreman, json=body)
    assert res.status_code == 201, res.text
    data: Json = res.json()["data"]
    return data


def test_an_ask_is_answered_from_a_posted_notice(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    answer = {"covered": True, "answer": _tri("朝8時まで"), "source_notice_ids": [str(shop.notice_id)]}
    fake_ai.queue("answer_ask", answer)

    ask = _ask(client, shop)

    assert ask["covered"] is True and ask["sourceNoticeIds"] == [str(shop.notice_id)]
    assert ask["answer"]["bn"] == "朝8時まで-bn"
    assert "燃やすごみは朝8時まで" in fake_ai.last_prompt_text  # the Notice was sent to the model


def test_an_answer_citing_no_sent_notice_is_not_covered(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    invented = {"covered": True, "answer": _tri("火曜日"), "source_notice_ids": ["999999"]}
    fake_ai.queue("answer_ask", invented)
    fake_ai.queue("answer_ask", invented)  # the one retry says the same

    ask = _ask(client, shop)

    assert ask["covered"] is False and ask["answer"] is None and ask["sourceNoticeIds"] == []


def test_without_the_model_an_ask_is_not_covered(client: TestClient, shop: Shop) -> None:
    ask = _ask(client, shop)

    assert ask["covered"] is False and ask["answer"] is None


def test_the_senpai_answers_a_forwarded_ask(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    ask = _ask(client, shop, "粗大ごみはどうしますか?")
    res = client.post(f"/api/asks/{ask['id']}/forward", headers=shop.worker)
    assert res.status_code == 409 and res.json()["error"]["code"] == "NO_ACTIVE_PAIR"

    pair = _pair(client, shop)
    fake_ai.queue("translate", {"text": "粗大ごみはどうしますか?"})
    assert client.post(f"/api/asks/{ask['id']}/forward", headers=shop.worker).json()["data"]["forwarded"] is True

    page = client.get(f"/api/senpai/{pair['token']}").json()["data"]
    assert page["senpaiName"] == "田中 健" and [a["textJa"] for a in page["asks"]] == ["粗大ごみはどうしますか?"]

    fake_ai.queue("translate", {"text": "ক্লিন সেন্টারে বুক করুন"})
    fake_ai.queue("translate", {"text": "Book the clean center"})
    url = f"/api/senpai/{pair['token']}/asks/{ask['id']}/reply"
    assert client.post(url, json={"replyJa": "クリーンセンターに予約します"}).status_code == 200
    assert client.post(url, json={"replyJa": "もう一度"}).status_code == 409

    [mine] = client.get("/api/asks", headers=shop.worker).json()["data"]["items"]
    reply = {"ja": "クリーンセンターに予約します", "bn": "ক্লিন সেন্টারে বুক করুন", "en": "Book the clean center"}
    assert mine["senpaiReply"] == reply


def test_the_pair_token_is_shown_once_and_a_revoked_link_is_gone(client: TestClient, shop: Shop) -> None:
    pair = _pair(client, shop)

    [listed] = client.get("/api/senpai-pairs", headers=shop.foreman).json()["data"]["items"]
    assert "token" not in listed
    assert (
        client.post(
            "/api/senpai-pairs",
            headers=shop.foreman,
            json={"workerId": str(shop.worker_id), "senpaiName": "森", "kind": "jichikai"},
        ).status_code
        == 409
    )  # one active Senpai per Worker
    assert client.delete(f"/api/senpai-pairs/{pair['id']}", headers=shop.foreman).status_code == 200
    assert client.get(f"/api/senpai/{pair['token']}").status_code == 404
    assert client.get("/api/senpai/not-a-real-token").status_code == 404


def test_meetups_are_logged_by_both_sides_and_never_in_the_future(client: TestClient, shop: Shop) -> None:
    pair = _pair(client, shop)
    worker_side = {"kind": "coffee", "date": str(TODAY - timedelta(days=1)), "note": "駅前で"}
    senpai_side = {"kind": "drill", "date": str(TODAY - timedelta(days=2))}

    assert client.post("/api/meetups", headers=shop.worker, json=worker_side).status_code == 201
    assert client.post(f"/api/senpai/{pair['token']}/meetups", json=senpai_side).status_code == 201
    future = worker_side | {"date": str(TODAY + timedelta(days=3))}
    assert client.post("/api/meetups", headers=shop.worker, json=future).status_code == 422

    page = client.get(f"/api/senpai/{pair['token']}").json()["data"]
    assert sorted(m["loggedBy"] for m in page["meetups"]) == ["senpai", "worker"]


def test_the_support_log_lists_the_shops_support_events(client: TestClient, shop: Shop, fake_ai: FakeAiClient) -> None:
    _pair(client, shop)
    ask = _ask(client, shop, "ごみの日は?")
    fake_ai.queue("translate", {"text": "ごみの日は?"})
    client.post(f"/api/asks/{ask['id']}/forward", headers=shop.worker)
    client.post("/api/meetups", headers=shop.worker, json={"kind": "coffee", "date": str(TODAY), "note": "お茶"})
    client.post("/api/receipts", headers=shop.worker, json={"target": {"kind": "notice", "id": str(shop.notice_id)}})
    params = {"from": str(TODAY - timedelta(days=1)), "to": str(TODAY + timedelta(days=1))}

    res = client.get("/api/support-log.csv", headers=shop.foreman, params=params)

    assert res.status_code == 200 and res.headers["content-type"].startswith("text/csv")
    text = res.content.decode("utf-8-sig")
    assert "7 相談・苦情への対応" in text and "ごみの日は?" in text and "田中 健：未回答" in text
    assert "8 日本人との交流促進" in text and "お茶・食事" in text
    assert "ごみの出し方" in text and "理解確認：未回答" in text
    assert client.get("/api/support-log.csv", headers=shop.worker, params=params).status_code == 403


def test_another_shop_cannot_touch_these_records(client: TestClient, db: Session, shop: Shop) -> None:
    pair = _pair(client, shop)
    ask = _ask(client, shop)
    other = _shop(client, db)

    assert client.post(f"/api/asks/{ask['id']}/forward", headers=other.worker).status_code == 404
    assert client.delete(f"/api/senpai-pairs/{pair['id']}", headers=other.foreman).status_code == 404
    body = {"workerId": str(shop.worker_id), "senpaiName": "他人", "kind": "coworker"}
    assert client.post("/api/senpai-pairs", headers=other.foreman, json=body).status_code == 404
    assert client.get("/api/asks", headers=other.worker).json()["data"]["items"] == []
    params = {"from": str(TODAY - timedelta(days=1)), "to": str(TODAY + timedelta(days=1))}
    other_log = client.get("/api/support-log.csv", headers=other.foreman, params=params).content.decode("utf-8-sig")
    assert "田中 健" not in other_log
