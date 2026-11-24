"""The FCM HTTP v1 client's pure parts: what a response means, and when push is switched on (XREQ-014)."""

import base64
import json

import pytest
from pydantic import SecretStr, ValidationError

from app.config import Settings
from app.push import classify, push_client_from

SETTINGS = {
    "frontend_origin": "http://localhost:3000",
    "database_url": "postgresql+psycopg://u:p@localhost/db",
    "pin_pepper": "x" * 32,
}


def _error(status: str, message: str, code: str | None = None) -> dict[str, object]:
    details = [{"@type": "type.googleapis.com/google.firebase.fcm.v1.FcmError", "errorCode": code}] if code else []
    return {"error": {"status": status, "message": message, "details": details}}


def test_a_200_is_sent() -> None:
    assert classify(200, {"name": "projects/p/messages/1"}) == "sent"


def test_an_unregistered_token_is_gone() -> None:
    assert classify(404, _error("NOT_FOUND", "Requested entity was not found.", "UNREGISTERED")) == "gone"


def test_a_malformed_token_is_gone_but_another_bad_request_is_our_bug() -> None:
    bad_token = _error("INVALID_ARGUMENT", "The registration token is not a valid FCM registration token")
    bad_payload = _error("INVALID_ARGUMENT", "Invalid value at 'message.data[0].value'")
    assert classify(400, bad_token) == "gone"
    assert classify(400, bad_payload) == "failed"


def test_quota_and_server_errors_can_be_retried() -> None:
    assert classify(429, _error("RESOURCE_EXHAUSTED", "quota")) == "retry"
    assert classify(503, {}) == "retry"
    assert classify(403, _error("PERMISSION_DENIED", "no")) == "failed"


def test_push_is_off_without_firebase_settings() -> None:
    assert push_client_from(Settings(**SETTINGS)) is None  # pyright: ignore[reportArgumentType]


def test_half_the_firebase_settings_fail_at_startup() -> None:
    with pytest.raises(ValidationError):
        Settings(**SETTINGS, firebase_project_id="daily-wizdom")  # pyright: ignore[reportArgumentType]


def test_a_key_for_another_project_fails_at_startup() -> None:
    key = base64.b64encode(json.dumps({"type": "service_account", "project_id": "other"}).encode()).decode()
    with pytest.raises(ValidationError):
        Settings(
            **SETTINGS,  # pyright: ignore[reportArgumentType]
            firebase_project_id="daily-wizdom",
            firebase_service_account_b64=SecretStr(key),
        )
