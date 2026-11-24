"""FCM HTTP v1 sender (XREQ-014, MOBILE_PLATFORM_CONTRACT §1.5). Outbound only: no database, no app layers.
Logs never carry the key or a device token."""

import logging
import random
import time
from typing import Any, Literal, Protocol, cast

import requests
from google.auth.exceptions import GoogleAuthError
from google.auth.transport.requests import AuthorizedSession
from google.oauth2 import service_account

from app.config import Settings, firebase_key

Outcome = Literal["sent", "gone", "failed"]
Verdict = Outcome | Literal["retry"]

SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
TIMEOUT_SECONDS = 10
RETRY_STATUSES = frozenset({429, 500, 502, 503, 504})
log = logging.getLogger("wakatta.push")


class PushClient(Protocol):
    def send(self, token: str, message: dict[str, Any]) -> Outcome: ...


def classify(status: int, body: dict[str, Any]) -> Verdict:
    """What an FCM answer means for the token: `gone` = delete the device row (manage-tokens guidance)."""
    if status == 200:
        return "sent"
    if status in RETRY_STATUSES:
        return "retry"
    error: dict[str, Any] = body.get("error") or {}
    raw: list[Any] = error.get("details") or []
    codes = {str(cast(dict[str, Any], d).get("errorCode")) for d in raw if isinstance(d, dict)}
    if status == 404 or "UNREGISTERED" in codes:
        return "gone"
    # A 400 is a dead token only when FCM says so; any other 400 is our payload, so keep the device.
    if status == 400 and "registration token" in str(error.get("message", "")).lower():
        return "gone"
    return "failed"


class FcmClient:
    def __init__(self, info: dict[str, Any], project_id: str) -> None:
        credentials = service_account.Credentials.from_service_account_info(  # pyright: ignore[reportUnknownMemberType]
            info, scopes=[SCOPE]
        )
        self._session = AuthorizedSession(credentials, refresh_timeout=TIMEOUT_SECONDS)
        self._url = f"https://fcm.googleapis.com/v1/projects/{project_id}/messages:send"

    def _post(self, token: str, message: dict[str, Any]) -> tuple[Verdict, int | None]:
        try:
            res = self._session.post(  # pyright: ignore[reportUnknownMemberType]
                self._url, json={"message": {**message, "token": token}}, timeout=TIMEOUT_SECONDS
            )
        except (requests.RequestException, GoogleAuthError) as exc:
            log.warning("push: FCM call failed (%s)", type(exc).__name__)
            return "retry", None
        try:
            body: object = res.json()
        except ValueError:
            body = {}
        return classify(res.status_code, body if isinstance(body, dict) else {}), res.status_code  # pyright: ignore[reportUnknownArgumentType]

    def send(self, token: str, message: dict[str, Any]) -> Outcome:
        """One retry with jitter on 429/5xx or a network error; a repeat is safe, the app drops it by eventId."""
        status: int | None = None
        for attempt in range(2):
            verdict, status = self._post(token, message)
            if verdict != "retry":
                if verdict == "failed":
                    log.warning("push: FCM refused a %s message (HTTP %s)", message["data"]["type"], status)
                return verdict
            if attempt == 0:
                time.sleep(0.5 + random.random())  # noqa: S311  # jitter, not crypto
        log.warning("push: FCM still unavailable after a retry (HTTP %s)", status)
        return "failed"


def push_client_from(settings: Settings) -> PushClient | None:
    """None when FIREBASE_* is unset (local, CI): nothing is sent."""
    if settings.firebase_service_account_b64 is None or settings.firebase_project_id is None:
        return None
    return FcmClient(firebase_key(settings.firebase_service_account_b64), settings.firebase_project_id)
