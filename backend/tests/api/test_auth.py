"""WS0 step 3: accounts, login, lockout, sessions and the role guard (architecture §3, §3.4, §9.3, §12)."""

from datetime import UTC, datetime, timedelta

from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.models.scopes import Shop, User
from tests import factories as f


def _login(client: TestClient, who: User, pin: str) -> str:
    res = client.post("/api/auth/login", json={"userId": str(who.id), "pin": pin})
    assert res.status_code == 200, res.text
    token: str = res.json()["data"]["token"]
    return token


def _bearer(token: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {token}"}


def _ready_foreman(client: TestClient, db: Session, shop: Shop) -> tuple[User, str]:
    foreman = f.user(db, shop, role="foreman", pin="739215", must_change=False)
    return foreman, _login(client, foreman, "739215")


def test_the_roster_lists_active_workers_of_the_shop_by_code(client: TestClient, db: Session) -> None:
    shop = f.shop(db)
    rahim = f.user(db, shop, name="Rahim")
    gone = f.user(db, shop, name="Gone")
    gone.active = False
    f.user(db, f.shop(db), name="Other shop")
    db.flush()

    res = client.get("/api/auth/roster", params={"code": shop.code, "role": "worker"})

    assert res.status_code == 200
    assert res.json()["data"] == [{"id": str(rahim.id), "displayName": "Rahim"}]


def test_the_roster_of_an_unknown_code_is_not_found(client: TestClient) -> None:
    assert client.get("/api/auth/roster", params={"code": "ZZZZZZZZ", "role": "worker"}).status_code == 404


def test_login_returns_a_token_and_the_user(client: TestClient, db: Session) -> None:
    shop = f.shop(db)
    rahim = f.user(db, shop, name="Rahim", pin="2468")

    res = client.post("/api/auth/login", json={"userId": str(rahim.id), "pin": "2468"})

    data = res.json()["data"]
    assert res.status_code == 200
    assert len(data["token"]) >= 40
    assert data["mustChangePin"] is True
    assert data["user"] == {
        "id": str(rahim.id),
        "displayName": "Rahim",
        "role": "worker",
        "language": "bn",
        "shopId": str(shop.id),
        "districtId": str(shop.default_district_id),
        "shopCode": shop.code,
    }
    expires = datetime.fromisoformat(data["expiresAt"])
    assert timedelta(days=29) < expires - datetime.now(UTC) <= timedelta(days=30)


def test_a_wrong_pin_is_unauthorized_and_five_lock_the_account(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468")
    body = {"userId": str(rahim.id), "pin": "1357"}

    statuses = [client.post("/api/auth/login", json=body).status_code for _ in range(5)]
    locked = client.post("/api/auth/login", json={"userId": str(rahim.id), "pin": "2468"})

    assert statuses == [401, 401, 401, 401, 423]
    assert locked.status_code == 423
    assert locked.json()["error"]["code"] == "PIN_LOCKED"
    assert "until" in locked.json()["error"]["details"]


def test_an_expired_lock_lets_the_right_pin_in_again(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468")
    rahim.pin_failed_count = 5
    rahim.pin_locked_until = datetime.now(UTC) - timedelta(seconds=1)
    db.flush()

    assert client.post("/api/auth/login", json={"userId": str(rahim.id), "pin": "2468"}).status_code == 200


def test_a_deactivated_user_cannot_log_in(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468")
    rahim.active = False
    db.flush()

    assert client.post("/api/auth/login", json={"userId": str(rahim.id), "pin": "2468"}).status_code == 401


def test_requests_without_a_valid_token_are_unauthorized(client: TestClient) -> None:
    assert client.get("/api/auth/me").status_code == 401
    assert client.get("/api/auth/me", headers=_bearer("not-a-real-token")).status_code == 401
    assert client.get("/api/auth/me", headers={"Authorization": "Basic abc"}).status_code == 401


def test_first_login_must_change_the_pin_before_anything_else(client: TestClient, db: Session) -> None:
    shop = f.shop(db)
    foreman = f.user(db, shop, role="foreman", pin="739215")
    token = _login(client, foreman, "739215")

    blocked = client.get("/api/workers", headers=_bearer(token))
    me = client.get("/api/auth/me", headers=_bearer(token))
    weak = client.put("/api/auth/me/pin", headers=_bearer(token), json={"currentPin": "739215", "newPin": "123456"})
    changed = client.put("/api/auth/me/pin", headers=_bearer(token), json={"currentPin": "739215", "newPin": "604817"})
    after = client.get("/api/workers", headers=_bearer(token))

    assert blocked.status_code == 403
    assert blocked.json()["error"]["code"] == "PIN_CHANGE_REQUIRED"
    assert me.status_code == 200
    assert weak.status_code == 422
    assert changed.status_code == 200
    assert after.status_code == 200


def test_changing_the_pin_revokes_the_users_other_sessions(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468", must_change=False)
    phone, tablet = _login(client, rahim, "2468"), _login(client, rahim, "2468")

    client.put("/api/auth/me/pin", headers=_bearer(phone), json={"currentPin": "2468", "newPin": "9061"})

    assert client.get("/api/auth/me", headers=_bearer(phone)).status_code == 200
    assert client.get("/api/auth/me", headers=_bearer(tablet)).status_code == 401


def test_logout_revokes_the_token(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468", must_change=False)
    token = _login(client, rahim, "2468")

    assert client.post("/api/auth/logout", headers=_bearer(token)).status_code == 200
    assert client.get("/api/auth/me", headers=_bearer(token)).status_code == 401


def test_a_foreman_creates_a_worker_who_must_choose_a_pin(client: TestClient, db: Session) -> None:
    shop = f.shop(db)
    _, token = _ready_foreman(client, db, shop)

    res = client.post(
        "/api/workers", headers=_bearer(token), json={"displayName": "Sadia", "language": "bn", "pin": "5820"}
    )
    login = client.post("/api/auth/login", json={"userId": res.json()["data"]["id"], "pin": "5820"})

    assert res.status_code == 201
    assert res.json()["data"]["displayName"] == "Sadia"
    assert login.json()["data"]["mustChangePin"] is True


def test_a_worker_cannot_manage_accounts(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468", must_change=False)
    token = _login(client, rahim, "2468")

    res = client.post(
        "/api/workers", headers=_bearer(token), json={"displayName": "X", "language": "bn", "pin": "5820"}
    )

    assert res.status_code == 403
    assert res.json()["error"]["code"] == "FORBIDDEN"


def test_a_foreman_cannot_touch_another_shops_worker(client: TestClient, db: Session) -> None:
    _, token = _ready_foreman(client, db, f.shop(db))
    stranger = f.user(db, f.shop(db))

    res = client.patch(f"/api/workers/{stranger.id}", headers=_bearer(token), json={"active": False})

    assert res.status_code == 404


def test_a_pin_reset_signs_the_worker_out_everywhere(client: TestClient, db: Session) -> None:
    shop = f.shop(db)
    _, foreman_token = _ready_foreman(client, db, shop)
    rahim = f.user(db, shop, pin="2468", must_change=False)
    rahim_token = _login(client, rahim, "2468")

    res = client.patch(f"/api/workers/{rahim.id}", headers=_bearer(foreman_token), json={"pin": "7193"})
    relogin = client.post("/api/auth/login", json={"userId": str(rahim.id), "pin": "7193"})

    assert res.status_code == 200
    assert client.get("/api/auth/me", headers=_bearer(rahim_token)).status_code == 401
    assert relogin.json()["data"]["mustChangePin"] is True


def test_the_last_active_foreman_cannot_be_deactivated(client: TestClient, db: Session) -> None:
    shop = f.shop(db)
    foreman, token = _ready_foreman(client, db, shop)
    second = f.user(db, shop, role="foreman")

    first_try = client.patch(f"/api/foremen/{foreman.id}", headers=_bearer(token), json={"active": False})
    other = client.patch(f"/api/foremen/{second.id}", headers=_bearer(token), json={"active": False})
    last = client.patch(f"/api/foremen/{foreman.id}", headers=_bearer(token), json={"active": False})

    assert first_try.status_code == 409  # not on yourself
    assert other.status_code == 200
    assert last.status_code == 409


def test_unknown_fields_are_rejected(client: TestClient, db: Session) -> None:
    rahim = f.user(db, f.shop(db), pin="2468")

    res = client.post("/api/auth/login", json={"userId": str(rahim.id), "pin": "2468", "role": "foreman"})

    assert res.status_code == 422


def test_anyone_can_open_a_shop_and_becomes_its_foreman(client: TestClient, db: Session) -> None:
    f.district(db)

    res = client.post(
        "/api/auth/register", json={"shopName": "Minato Works", "displayName": "森 太郎", "pin": "739215"}
    )

    assert res.status_code == 201, res.text
    data = res.json()["data"]
    assert data["user"]["role"] == "foreman" and data["mustChangePin"] is False
    assert data["user"]["shopCode"] == data["shopCode"]
    roster = client.get("/api/auth/roster", params={"code": data["shopCode"], "role": "foreman"})
    assert roster.json()["data"] == [{"id": data["user"]["id"], "displayName": "森 太郎"}]
    threads = client.get("/api/threads", headers=_bearer(data["token"])).json()["data"]
    assert [t["titleJa"] for t in (threads if isinstance(threads, list) else threads["items"])] == ["全員"]

    worker = client.post(
        "/api/workers", headers=_bearer(data["token"]), json={"displayName": "Rahim", "language": "en", "pin": "5820"}
    )
    worker_token = client.post("/api/auth/login", json={"userId": worker.json()["data"]["id"], "pin": "5820"})
    assert worker_token.status_code == 200


def test_opening_a_shop_needs_a_safe_six_digit_pin(client: TestClient, db: Session) -> None:
    f.district(db)
    res = client.post("/api/auth/register", json={"shopName": "X", "displayName": "Y", "pin": "123456"})
    assert res.status_code == 422
