"""Every query on users, sessions, audit events and login limits. Shop-owned lookups always take the shop id."""

from datetime import datetime, timedelta

from sqlalchemy import case, func, select, update
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.scopes import AuditEvent, District, LoginLimit, Shop, User
from app.models.scopes import Session as SessionRow


def shop_by_code(db: Session, code: str) -> Shop | None:
    return db.scalar(select(Shop).where(Shop.code == code))


def users_by_shop_and_name(db: Session, shop_name: str, display_name: str) -> list[User]:
    """Active Foremen and Workers named so in a shop named so (both ignoring case); a few at most."""
    stmt = (
        select(User)
        .join(Shop, Shop.id == User.shop_id)
        .where(
            func.lower(Shop.name) == shop_name.lower(),
            func.lower(User.display_name) == display_name.lower(),
            User.role.in_(("foreman", "worker")),
            User.active,
        )
        .order_by(User.id)
        .limit(5)
    )
    return list(db.scalars(stmt))


def district_by_code(db: Session, code: str) -> District | None:
    return db.scalar(select(District).where(District.code == code))


def shop_by_id(db: Session, shop_id: int) -> Shop | None:
    return db.get(Shop, shop_id)


def roster_of_shop(db: Session, shop_id: int, role: str) -> list[User]:
    stmt = select(User).where(User.shop_id == shop_id, User.role == role, User.active).order_by(User.display_name)
    return list(db.scalars(stmt))


def roster_of_district(db: Session, district_id: int) -> list[User]:
    stmt = (
        select(User)
        .where(User.district_id == district_id, User.role == "poster", User.active)
        .order_by(User.display_name)
    )
    return list(db.scalars(stmt))


def user_by_id(db: Session, user_id: int) -> User | None:
    return db.get(User, user_id)


def user_in_shop(db: Session, shop_id: int, user_id: int, role: str) -> User | None:
    return db.scalar(select(User).where(User.id == user_id, User.shop_id == shop_id, User.role == role))


def users_in_shop(db: Session, shop_id: int, role: str, after_id: int | None, limit: int) -> list[User]:
    stmt = select(User).where(User.shop_id == shop_id, User.role == role)
    if after_id is not None:
        stmt = stmt.where(User.id > after_id)
    return list(db.scalars(stmt.order_by(User.id).limit(limit + 1)))


def active_foremen_count(db: Session, shop_id: int) -> int:
    stmt = select(func.count()).select_from(User).where(User.shop_id == shop_id, User.role == "foreman", User.active)
    return db.scalar(stmt) or 0


def session_by_digest(db: Session, digest: bytes) -> SessionRow | None:
    return db.get(SessionRow, digest)


def revoke_sessions_of(db: Session, user_id: int, now: datetime, keep: bytes | None = None) -> None:
    stmt = update(SessionRow).where(SessionRow.user_id == user_id, SessionRow.revoked_at.is_(None))
    if keep is not None:
        stmt = stmt.where(SessionRow.token_hash != keep)
    db.execute(stmt.values(revoked_at=now))


def revoke_panel_sessions(db: Session, shop_id: int, machine_id: int, now: datetime) -> None:
    """An archived Machine's Panel device signs out (its session would otherwise live 180 days)."""
    stmt = update(SessionRow).where(
        SessionRow.kind == "panel",
        SessionRow.shop_id == shop_id,
        SessionRow.machine_id == machine_id,
        SessionRow.revoked_at.is_(None),
    )
    db.execute(stmt.values(revoked_at=now))


def audit(
    db: Session,
    action: str,
    actor_user_id: int | None,
    shop_id: int | None = None,
    district_id: int | None = None,
    subject_id: int | None = None,
) -> None:
    db.add(
        AuditEvent(
            action=action, actor_user_id=actor_user_id, shop_id=shop_id, district_id=district_id, subject_id=subject_id
        )
    )


def count_attempt(db: Session, key: str, now: datetime, window: timedelta) -> int:
    """Atomically counts one attempt in a fixed window and returns the count so far (survives restarts, §6.4)."""
    expired = LoginLimit.window_start < now - window
    stmt = (
        insert(LoginLimit)
        .values(key=key, window_start=now, attempts=1)
        .on_conflict_do_update(
            index_elements=[LoginLimit.key],
            set_={
                "attempts": case((expired, 1), else_=LoginLimit.attempts + 1),
                "window_start": case((expired, now), else_=LoginLimit.window_start),
            },
        )
        .returning(LoginLimit.attempts)
    )
    return db.execute(stmt).scalar_one()
