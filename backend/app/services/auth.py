"""Roster, login, lockout and sessions (architecture §3, §12). Every write commits its own short transaction, so a
failed PIN is counted even though the request then fails."""

import functools
import json
import secrets
from dataclasses import dataclass
from datetime import datetime, timedelta
from pathlib import Path

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.domain.pins import pin_problem
from app.models.chat import Thread, ThreadMember
from app.models.scopes import District, Shop, User
from app.models.scopes import Session as SessionRow
from app.models.work import GlossaryTerm
from app.repositories import accounts as repo
from app.schemas.auth import RegisterIn, UserOut
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
REGISTERS_PER_IP = 3
# ADR 0017: every new shop's Foreman gets a fresh AI allowance; one venue or mobile network shares an IP, so raise
# this if the judges register their own shops on site.
REGISTERS_PER_IP_PER_DAY = 10
GLOSSARY = Path(__file__).resolve().parent.parent / "seed_data" / "glossary.json"
CODE_LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"  # no 0/O or 1/I: the code is read off a sticker


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


def _rate_limit(db: Session, key: str, limit: int, now: datetime, window: timedelta = LIMIT_WINDOW) -> None:
    attempts = repo.count_attempt(db, key, now, window)
    db.commit()
    if attempts > limit:
        retry = str(int(window.total_seconds()))
        raise ApiError(429, "RATE_LIMITED", "Too many attempts, try again later", headers={"Retry-After": retry})


def user_out(db: Session, user: User) -> UserOut:
    shop = repo.shop_by_id(db, user.shop_id) if user.shop_id else None
    return UserOut(
        id=str(user.id),
        display_name=user.display_name,
        role=user.role,  # pyright: ignore[reportArgumentType]  # constrained by the users.role CHECK
        language=user.language,  # pyright: ignore[reportArgumentType]  # constrained by the users.language CHECK
        shop_id=str(user.shop_id) if user.shop_id else None,
        district_id=str(user.district_id) if user.district_id else None,
        shop_code=shop.code if shop else None,
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
    return _sign_in(db, user, pin, pepper, now)


def _sign_in(db: Session, user: User, pin: str, pepper: str, now: datetime) -> tuple[str, datetime, User]:
    """The PIN check with its lockout, after the caller's rate limit."""
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
    return _open_session(db, user, "login", now)


def find(
    db: Session, shop_name: str, display_name: str, pin: str, ip: str, pepper: str, now: datetime
) -> tuple[str, datetime, User]:
    """Forgot the shop code: shop name + own name + PIN signs in, and the session carries the code. Nothing is
    returned without the right PIN, so a shop name never reveals its code or its roster."""
    _rate_limit(db, f"login:{ip}", LOGINS_PER_IP, now)  # before any PIN hash: a limited request costs nothing
    users = repo.users_by_shop_and_name(db, shop_name.strip(), display_name.strip())
    if len(users) != 1:
        # Nobody, or namesakes (two shops of one name): never guess between people; they sign in with the shop
        # code. One dummy check keeps the reply as slow as a real one, so timing does not tell who exists.
        verify_pin(pepper, _dummy_hash(pepper), pin)
        raise _unauthorized()
    user = users[0]
    if user.pin_locked_until is not None and user.pin_locked_until > now:
        # A lock would confirm that this shop and person exist (the shop-code route shows it), so answer as for
        # nobody, after the same hash: a locked person must not answer faster either.
        verify_pin(pepper, _dummy_hash(pepper), pin)
        raise _unauthorized()
    try:
        return _sign_in(db, user, pin, pepper, now)
    except ApiError as e:
        if e.status == 423:  # this wrong PIN locked the account just now; it already did its hash
            raise _unauthorized() from e
        raise


@functools.cache
def _dummy_hash(pepper: str) -> str:
    return hash_pin(pepper, "000000")


def _open_session(db: Session, user: User, event: str, now: datetime) -> tuple[str, datetime, User]:
    token, digest = new_token()
    expires_at = now + SESSION_TTL[user.role]
    db.add(SessionRow(token_hash=digest, kind="user", user_id=user.id, expires_at=expires_at, last_seen_at=now))
    repo.audit(db, event, user.id, user.shop_id, user.district_id)
    db.commit()
    return token, expires_at, user


def register(db: Session, body: RegisterIn, ip: str, pepper: str, now: datetime) -> tuple[str, datetime, User, Shop]:
    """ADR 0017: anyone can open a new shop and becomes its first Foreman. The shop starts with one group Thread and a
    copy of the standard Glossary; Workers still get their accounts from a Foreman (ADR 0010)."""
    _rate_limit(db, f"register:{ip}", REGISTERS_PER_IP, now)
    _rate_limit(db, f"register-day:{ip}", REGISTERS_PER_IP_PER_DAY, now, timedelta(days=1))
    if (problem := pin_problem("foreman", body.pin)) is not None:
        raise ApiError(
            422, "VALIDATION_ERROR", "Choose a different PIN", {"fields": [{"loc": ["body", "pin"], "msg": problem}]}
        )
    # shortcut: a new shop joins the first district; a district picker comes when there is more than one.
    district_id = db.scalar(select(District.id).order_by(District.id).limit(1))
    if district_id is None:
        raise ApiError(503, "UNAVAILABLE", "No district to join yet")
    code = body.shop_code.upper() if body.shop_code else _new_shop_code(db)
    if body.shop_code and repo.shop_by_code(db, code) is not None:
        raise ApiError(409, "SHOP_CODE_TAKEN", "That shop code is taken; choose another")
    shop = Shop(code=code, name=body.shop_name, default_district_id=district_id, languages=["en"])
    db.add(shop)
    db.flush()
    user = User(
        role="foreman",
        display_name=body.display_name,
        language="ja",
        shop_id=shop.id,
        district_id=district_id,
        pin_hash=hash_pin(pepper, body.pin),
        pin_must_change=False,  # they chose it themselves; nobody else knows it
    )
    db.add(user)
    db.flush()
    thread = Thread(shop_id=shop.id, title_ja="全員", kind="group", created_by=user.id)
    db.add(thread)
    db.flush()
    db.add(ThreadMember(thread_id=thread.id, user_id=user.id, shop_id=shop.id, last_read_at=now))
    for t in json.loads(GLOSSARY.read_text(encoding="utf-8"))["terms"]:
        db.add(GlossaryTerm(shop_id=shop.id, ja=t["ja"], reading=t["reading"], translations=t["translations"]))
    token, expires_at, user = _open_session(db, user, "register", now)
    return token, expires_at, user, shop


def _new_shop_code(db: Session) -> str:
    while True:
        code = "".join(secrets.choice(CODE_LETTERS) for _ in range(8))
        if repo.shop_by_code(db, code) is None:
            return code


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
