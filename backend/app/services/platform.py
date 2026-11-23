"""App platform (XREQ-009): the version gate and push device registration. Sending is XREQ-014 (Firebase)."""

import uuid
from datetime import datetime, timedelta

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.config import Settings
from app.repositories import accounts
from app.repositories import devices as repo
from app.schemas.platform import AppConfig, DeviceIn, DeviceOut
from app.services.auth import Principal

REGISTRATIONS_PER_MINUTE = 6  # MOBILE_PLATFORM_CONTRACT 1.3 rule 4


def app_config(settings: Settings, now: datetime) -> AppConfig:
    return AppConfig.model_validate(
        {
            "min_version": settings.app_min_version,
            "latest_version": settings.app_latest_version,
            "download_url": settings.app_download_url,
            "sha256": settings.app_sha256,
            "server_time": now,
        }
    )


def register(db: Session, who: Principal, body: DeviceIn, now: datetime) -> DeviceOut:
    """Upsert this install for the signed-in user; the row lives as long as the session that registered it."""
    if who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    if accounts.count_attempt(db, f"device:{who.user_id}", now, timedelta(minutes=1)) > REGISTRATIONS_PER_MINUTE:
        db.commit()
        raise ApiError(
            429, "RATE_LIMITED", "Too many device updates; try again in a minute", headers={"Retry-After": "60"}
        )
    values: dict[str, object] = {
        "platform": body.platform,
        "fcm_token": body.fcm_token,
        "app_version": body.app_version,
        "os_api_level": body.os_api_level,
        "locale": body.locale,
        "notifications_enabled": body.notifications_enabled,
    }
    repo.register(db, who.user_id, who.token_digest, values, body.install_id, now)
    db.commit()
    return DeviceOut(registered=True, last_seen_at=now)


def unregister(db: Session, who: Principal, install_id: uuid.UUID) -> None:
    """Best effort before logout; an unknown install is fine (idempotent)."""
    if who.user_id is not None:
        repo.unregister(db, who.user_id, install_id)
        db.commit()
