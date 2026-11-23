"""Push device rows (XREQ-009). Each query takes the signed-in user's id; nothing here is unscoped."""

import uuid
from datetime import datetime

from sqlalchemy import delete, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.platform import Device
from app.models.scopes import Session as AuthSession
from app.models.scopes import User


def register(
    db: Session, user_id: int, session_hash: bytes, values: dict[str, object], install_id: uuid.UUID, now: datetime
) -> None:
    """Upsert on (user, install); a token held by another row moves here (shared phone: one user per token)."""
    others = (Device.user_id != user_id) | (Device.install_id != install_id)
    db.execute(delete(Device).where(Device.fcm_token == values["fcm_token"], others))
    row = {
        **values,
        "user_id": user_id,
        "install_id": install_id,
        "session_token_hash": session_hash,
        "last_seen_at": now,
    }
    stmt = insert(Device).values(**row)
    update = {k: stmt.excluded[k] for k in row if k not in ("user_id", "install_id")}
    db.execute(stmt.on_conflict_do_update(index_elements=["user_id", "install_id"], set_=update))


def unregister(db: Session, user_id: int, install_id: uuid.UUID) -> None:
    db.execute(delete(Device).where(Device.user_id == user_id, Device.install_id == install_id))


def reachable(db: Session, user_ids: list[int], now: datetime) -> list[tuple[Device, str]]:
    """(device, account language) for these users' phones with notifications on and a live session (§1.3 rule 3)."""
    if not user_ids:
        return []
    stmt = (
        select(Device, User.language)
        .join(User, User.id == Device.user_id)
        .join(AuthSession, AuthSession.token_hash == Device.session_token_hash)
        .where(
            Device.user_id.in_(user_ids),
            Device.notifications_enabled,
            User.active,
            AuthSession.revoked_at.is_(None),
            AuthSession.expires_at > now,
        )
    )
    return [(d, lang) for d, lang in db.execute(stmt)]


def forget(db: Session, device_id: int) -> None:
    """FCM said the token is dead (UNREGISTERED, or an invalid token)."""
    db.execute(delete(Device).where(Device.id == device_id))
