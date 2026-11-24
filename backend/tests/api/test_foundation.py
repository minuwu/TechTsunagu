"""WS0 step 1: envelope, errors, request id, CORS, headers (architecture §9.1, §9.2, §12, §13)."""

import json
import logging

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.config import Settings
from app.main import create_app
from app.openapi import contract
from tests import factories as f
from tests.conftest import FRONTEND


class _Item(BaseModel):
    name: str


def _app_with_test_routes(settings: Settings) -> FastAPI:
    app = create_app(settings)

    @app.post("/api/_test/items")
    def create_item(item: _Item) -> dict[str, str]:  # pyright: ignore[reportUnusedFunction]
        return {"name": item.name}

    @app.get("/api/_test/conflict")
    def conflict() -> None:  # pyright: ignore[reportUnusedFunction]
        raise ApiError(409, "CONFLICT", "Already approved", {"noteVersionId": "7"})

    @app.get("/api/_test/crash")
    def crash() -> None:  # pyright: ignore[reportUnusedFunction]
        raise RuntimeError("database password is hunter2")

    return app


@pytest.fixture
def test_client(settings: Settings) -> TestClient:
    return TestClient(_app_with_test_routes(settings), raise_server_exceptions=False)


def test_health_returns_the_envelope(client: TestClient) -> None:
    res = client.get("/api/health")

    assert res.status_code == 200
    assert res.json() == {"success": True, "data": {"status": "ok"}, "message": "ok"}


def test_unknown_route_is_a_not_found_envelope(client: TestClient) -> None:
    res = client.get("/api/does-not-exist")

    assert res.status_code == 404
    assert res.json() == {"success": False, "error": {"code": "NOT_FOUND", "message": "Not Found", "details": {}}}


def test_wrong_method_is_a_method_not_allowed_envelope(client: TestClient) -> None:
    res = client.post("/api/health")

    assert res.status_code == 405
    assert res.json()["error"]["code"] == "METHOD_NOT_ALLOWED"


def test_validation_error_lists_fields_without_echoing_input(test_client: TestClient) -> None:
    res = test_client.post("/api/_test/items", json={"name": 5, "secret": "x"})

    body = res.json()
    assert res.status_code == 422
    assert body["success"] is False
    assert body["error"]["code"] == "VALIDATION_ERROR"
    fields = body["error"]["details"]["fields"]
    assert {"loc": ["body", "name"], "msg": "Input should be a valid string"} in fields
    assert "secret" not in res.text


def test_api_error_keeps_its_status_code_and_details(test_client: TestClient) -> None:
    res = test_client.get("/api/_test/conflict")

    assert res.status_code == 409
    assert res.json() == {
        "success": False,
        "error": {"code": "CONFLICT", "message": "Already approved", "details": {"noteVersionId": "7"}},
    }


def test_unhandled_error_is_generic_and_logged(test_client: TestClient, caplog: pytest.LogCaptureFixture) -> None:
    with caplog.at_level(logging.ERROR, logger="wakatta"):
        res = test_client.get("/api/_test/crash", headers={"X-Request-ID": "req-crash-1"})

    assert res.status_code == 500
    assert res.json()["error"] == {"code": "INTERNAL_ERROR", "message": "Internal server error", "details": {}}
    assert "hunter2" not in res.text
    assert any(getattr(r, "request_id", None) == "req-crash-1" for r in caplog.records)


def test_request_id_is_echoed_or_generated(client: TestClient) -> None:
    unsafe_id = "bad id with spaces" + "x" * 100
    echoed = client.get("/api/health", headers={"X-Request-ID": "abc-123"})
    generated = client.get("/api/health")
    unsafe = client.get("/api/health", headers={"X-Request-ID": unsafe_id})

    assert echoed.headers["X-Request-ID"] == "abc-123"
    assert len(generated.headers["X-Request-ID"]) == 32
    assert unsafe.headers["X-Request-ID"] != unsafe_id


def test_each_request_is_logged_with_its_route(client: TestClient, caplog: pytest.LogCaptureFixture) -> None:
    with caplog.at_level(logging.INFO, logger="wakatta.request"):
        client.get("/api/health", headers={"X-Request-ID": "req-log-1"})

    record = next(r for r in caplog.records if r.name == "wakatta.request")
    assert getattr(record, "route", None) == "/api/health"
    assert getattr(record, "status", None) == 200
    assert getattr(record, "request_id", None) == "req-log-1"


def test_cors_allows_only_the_frontend_origin(client: TestClient) -> None:
    preflight = {"Access-Control-Request-Method": "POST", "Access-Control-Request-Headers": "Authorization"}

    allowed = client.options("/api/health", headers={"Origin": FRONTEND, **preflight})
    refused = client.options("/api/health", headers={"Origin": "https://evil.example", **preflight})

    assert allowed.headers["access-control-allow-origin"] == FRONTEND
    assert "access-control-allow-credentials" not in allowed.headers
    assert "access-control-allow-origin" not in refused.headers


def test_cors_allows_every_listed_origin_so_local_dev_and_the_hosted_frontend_both_work(settings: Settings) -> None:
    hosted = "https://wakatta.example"
    both = settings.model_copy(update={"frontend_origin": [FRONTEND, hosted]})
    preflight = {"Access-Control-Request-Method": "GET", "Access-Control-Request-Headers": "Authorization"}

    with TestClient(create_app(both)) as c:
        for origin in (FRONTEND, hosted):
            res = c.options("/api/health", headers={"Origin": origin, **preflight})
            assert res.headers["access-control-allow-origin"] == origin


def test_a_browser_may_send_every_header_the_contract_declares(client: TestClient) -> None:
    paths = json.loads(contract())["paths"].values()
    declared = {
        p["name"] for path in paths for op in path.values() for p in op.get("parameters", []) if p["in"] == "header"
    }
    assert {"If-Match", "Idempotency-Key"} <= declared  # the Task Board and the retried creates

    for header in declared:
        preflight = {"Origin": FRONTEND, "Access-Control-Request-Method": "PATCH"}
        res = client.options("/api/tasks/1", headers=preflight | {"Access-Control-Request-Headers": header})
        assert res.status_code == 200, f"CORS refuses {header}: add it to allow_headers in app/main.py"


def test_frontend_origin_reads_a_comma_separated_list() -> None:
    parsed = Settings(
        frontend_origin="http://localhost:3000, https://wakatta.example",  # pyright: ignore[reportArgumentType]
        database_url="postgresql+psycopg://u@h/db",
        pin_pepper="p" * 32,  # pyright: ignore[reportArgumentType]
        _env_file=None,  # pyright: ignore[reportCallIssue]
    )

    assert parsed.frontend_origin == ["http://localhost:3000", "https://wakatta.example"]


def test_security_headers_are_on_every_response(client: TestClient) -> None:
    res = client.get("/api/health")

    assert res.headers["X-Content-Type-Options"] == "nosniff"
    assert res.headers["Referrer-Policy"] == "no-referrer"


def test_api_answers_are_never_stored_by_a_cache(client: TestClient, db: Session) -> None:
    worker = f.user(db, f.shop(db), pin="2468", must_change=False)
    login = client.post("/api/auth/login", json={"userId": str(worker.id), "pin": "2468"})
    signed_in = {"Authorization": f"Bearer {login.json()['data']['token']}"}

    plain = client.get("/api/auth/me", headers=signed_in)
    polled = client.get("/api/tasks", headers=signed_in)
    unchanged = client.get("/api/tasks", headers=signed_in | {"If-None-Match": polled.headers["ETag"]})

    assert login.headers["Cache-Control"] == plain.headers["Cache-Control"] == "no-store"
    assert client.get(f"/api/senpai/{'x' * 22}").headers["Cache-Control"] == "no-store"  # the link is the credential
    assert polled.headers["Cache-Control"] == "no-cache"  # an ETag route keeps its own rule
    assert (unchanged.status_code, unchanged.headers.get("Cache-Control")) == (304, None)


def test_settings_fail_fast_without_frontend_origin(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.delenv("FRONTEND_ORIGIN", raising=False)

    with pytest.raises(ValueError, match="frontend_origin"):
        Settings(_env_file=None)  # pyright: ignore[reportCallIssue]


def test_settings_refuse_a_short_pin_pepper(settings: Settings) -> None:
    with pytest.raises(ValueError, match="pin_pepper"):
        Settings(
            frontend_origin=FRONTEND,
            database_url=settings.database_url,
            pin_pepper="too-short",  # pyright: ignore[reportArgumentType]
            _env_file=None,  # pyright: ignore[reportCallIssue]
        )


@pytest.mark.parametrize("origin", ["http://localhost:3000/", "*", "localhost:3000"])
def test_settings_reject_an_origin_that_is_not_exact(origin: str) -> None:
    with pytest.raises(ValueError, match="frontend_origin"):
        Settings(frontend_origin=origin, _env_file=None)  # pyright: ignore[reportCallIssue]
