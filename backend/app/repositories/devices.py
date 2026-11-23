"""Push device rows (XREQ-009). Each query takes the signed-in user's id; nothing here is unscoped."""

import uuid
from datetime import datetime

from sqlalchemy import delete
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.platform import Device


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
