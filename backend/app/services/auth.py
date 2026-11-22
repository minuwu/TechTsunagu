"""Roster, login, lockout and sessions (architecture §3, §12). Every write commits its own short transaction, so a
failed PIN is counted even though the request then fails."""

from dataclasses import dataclass
from datetime import datetime, timedelta

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.domain.pins import pin_problem
from app.models.scopes import Session as SessionRow
from app.models.scopes import User
from app.repositories import accounts as repo
from app.schemas.auth import UserOut
from app.services.credentials import hash_pin, new_token, token_digest, verify_pin

SESSION_TTL = {
    "worker": timedelta(days=30),
    "foreman": timedelta(hours=12),
    "poster": timedelta(hours=12),
    "panel": timedelta(days=180),
}
SLIDE_AFTER = timedelta(hours=1)
MAX_FAILED_PINS = 5
LOCK_FOR = timedelta(minutes=15)
LIMIT_WINDOW = timedelta(minutes=1)
LOGINS_PER_IP = 10
ROSTERS_PER_IP = 20


@dataclass(frozen=True)
class Principal:
    """Who is calling. Built only from a session row; routes never read identity from the request body."""

    role: str
    user_id: int | None
    shop_id: int | None
    district_id: int | None
    machine_id: int | None
    must_change_pin: bool
    token_digest: bytes


def _unauthorized() -> ApiError:
    return ApiError(401, "UNAUTHORIZED", "Wrong name or PIN")


def _rate_limit(db: Session, key: str, limit: int, now: datetime) -> None:
    attempts = repo.count_attempt(db, key, now, LIMIT_WINDOW)
    db.commit()
    if attempts > limit:
        raise ApiError(429, "RATE_LIMITED", "Too many attempts, try again in a minute", headers={"Retry-After": "60"})


def user_out(user: User) -> UserOut:
    return UserOut(
        id=str(user.id),
        display_name=user.display_name,
        role=user.role,  # pyright: ignore[reportArgumentType]  # constrained by the users.role CHECK
        language=user.language,  # pyright: ignore[reportArgumentType]  # constrained by the users.language CHECK
        shop_id=str(user.shop_id) if user.shop_id else None,
        district_id=str(user.district_id) if user.district_id else None,
    )


def roster(db: Session, code: str, role: str, ip: str, now: datetime) -> list[User]:
    _rate_limit(db, f"roster:{ip}", ROSTERS_PER_IP, now)
    if role == "poster":
        district = repo.district_by_code(db, code)
        if district is None:
            raise ApiError(404, "NOT_FOUND", "Unknown code")
        return repo.roster_of_district(db, district.id)
    shop = repo.shop_by_code(db, code)
    if shop is None:
        raise ApiError(404, "NOT_FOUND", "Unknown code")
    return repo.roster_of_shop(db, shop.id, role)


def login(db: Session, user_id: int, pin: str, ip: str, pepper: str, now: datetime) -> tuple[str, datetime, User]:
    _rate_limit(db, f"login:{ip}", LOGINS_PER_IP, now)
    user = repo.user_by_id(db, user_id)
    if user is None or not user.active:
        raise _unauthorized()
    if user.pin_locked_until is not None and user.pin_locked_until > now:
        raise ApiError(423, "PIN_LOCKED", "Too many wrong PINs", {"until": user.pin_locked_until.isoformat()})

    if not verify_pin(pepper, user.pin_hash, pin):
        user.pin_failed_count += 1
        locked = user.pin_failed_count >= MAX_FAILED_PINS
        if locked:
            user.pin_failed_count = 0
            user.pin_locked_until = now + LOCK_FOR
        repo.audit(db, "login_failed", user.id, user.shop_id, user.district_id)
        db.commit()
        if locked:
            raise ApiError(423, "PIN_LOCKED", "Too many wrong PINs", {"until": (now + LOCK_FOR).isoformat()})
        raise _unauthorized()

    user.pin_failed_count = 0
    user.pin_locked_until = None
    token, digest = new_token()
    expires_at = now + SESSION_TTL[user.role]
    db.add(SessionRow(token_hash=digest, kind="user", user_id=user.id, expires_at=expires_at, last_seen_at=now))
    repo.audit(db, "login", user.id, user.shop_id, user.district_id)
    db.commit()
    return token, expires_at, user


def authenticate(db: Session, token: str, now: datetime) -> Principal:
    row = repo.session_by_digest(db, token_digest(token))
    if row is None or row.revoked_at is not None or row.expires_at <= now:
        raise ApiError(401, "UNAUTHORIZED", "Sign in again")
    if row.kind == "panel":
        return Principal("panel", None, row.shop_id, None, row.machine_id, False, row.token_hash)
    user = repo.user_by_id(db, row.user_id) if row.user_id is not None else None
    if user is None or not user.active:
        raise ApiError(401, "UNAUTHORIZED", "Sign in again")
    if now - row.last_seen_at > SLIDE_AFTER:
        row.last_seen_at = now
        if user.role == "worker":  # Workers keep their phone signed in while they keep using it
            row.expires_at = now + SESSION_TTL["worker"]
        db.commit()
    return Principal(user.role, user.id, user.shop_id, user.district_id, None, user.pin_must_change, row.token_hash)


def current_user(db: Session, who: Principal) -> User:
    user = repo.user_by_id(db, who.user_id) if who.user_id is not None else None
    if user is None:
        raise ApiError(401, "UNAUTHORIZED", "Sign in again")
    return user


def change_pin(db: Session, who: Principal, current: str, new: str, pepper: str, now: datetime) -> None:
    user = repo.user_by_id(db, who.user_id) if who.user_id is not None else None
    if user is None:
        raise ApiError(401, "UNAUTHORIZED", "Sign in again")
    if not verify_pin(pepper, user.pin_hash, current):
        raise ApiError(401, "UNAUTHORIZED", "Current PIN is wrong")
    problem = pin_problem(user.role, new)
    if problem is not None or new == current:
        reason = problem or "unchanged"
        raise ApiError(
            422, "VALIDATION_ERROR", "Choose a different PIN", {"fields": [{"loc": ["body", "newPin"], "msg": reason}]}
        )
    user.pin_hash = hash_pin(pepper, new)
    user.pin_must_change = False
    repo.revoke_sessions_of(db, user.id, now, keep=who.token_digest)
    db.commit()


def logout(db: Session, who: Principal, now: datetime) -> None:
    row = repo.session_by_digest(db, who.token_digest)
    if row is not None and row.revoked_at is None:
        row.revoked_at = now
        db.commit()
