"""Foremen manage the Worker and Foreman accounts of their own shop (architecture §3.4)."""

from datetime import datetime

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, build_page
from app.domain.pins import pin_problem
from app.models.chat import Thread, ThreadMember
from app.models.scopes import User
from app.repositories import accounts as repo
from app.schemas.auth import AccountIn, AccountOut, AccountUpdate
from app.services.auth import Principal
from app.services.credentials import hash_pin, new_pin


def account_out(user: User) -> AccountOut:
    return AccountOut(
        id=str(user.id),
        display_name=user.display_name,
        role=user.role,  # pyright: ignore[reportArgumentType]  # constrained by the users.role CHECK
        language=user.language,  # pyright: ignore[reportArgumentType]  # constrained by the users.language CHECK
        active=user.active,
        must_change_pin=user.pin_must_change,
    )


def _shop_id(who: Principal) -> int:
    if who.shop_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id


def _check_pin(role: str, pin: str) -> None:
    problem = pin_problem(role, pin)
    if problem is not None:
        raise ApiError(
            422, "VALIDATION_ERROR", "PIN is not valid", {"fields": [{"loc": ["body", "pin"], "msg": problem}]}
        )


def _check_language(db: Session, shop_id: int, language: str) -> None:
    shop = repo.shop_by_id(db, shop_id)
    if shop is None or (language != "ja" and language not in shop.languages):
        raise ApiError(
            422, "VALIDATION_ERROR", "Language not used in this shop", {"fields": [{"loc": ["body", "language"]}]}
        )


def _flush_unique(db: Session) -> None:
    try:
        with db.begin_nested():
            db.flush()
    except IntegrityError as exc:
        raise ApiError(409, "CONFLICT", "Someone in this shop already has that name") from exc


def _set_first_pin(db: Session, user: User, pin: str, pepper: str, actor_id: int | None, now: datetime) -> None:
    """The person must choose their own PIN next; a lockout is cleared and every session of theirs ends."""
    user.pin_hash = hash_pin(pepper, pin)
    user.pin_must_change = True
    user.pin_failed_count = 0
    user.pin_locked_until = None
    repo.revoke_sessions_of(db, user.id, now)
    district_id = user.district_id if user.shop_id is None else None
    repo.audit(db, "pin_reset", actor_id, user.shop_id, district_id, subject_id=user.id)


def reset_pin(db: Session, code: str, role: str, name: str, pepper: str, now: datetime) -> str:
    """Operator last resort (app.cli): a new first PIN for an active person found by scope code, role and roster name.
    The audit row has no actor: nobody is signed in."""
    if role == "poster":
        district = repo.district_by_code(db, code)
        people = repo.roster_of_district(db, district.id) if district else []
    else:
        shop = repo.shop_by_code(db, code)
        people = repo.roster_of_shop(db, shop.id, role) if shop else []
    user = next((u for u in people if u.display_name == name), None)
    if user is None:
        raise ApiError(404, "NOT_FOUND", f"No active {role} named {name!r} under code {code!r}")
    pin = new_pin(role)
    _set_first_pin(db, user, pin, pepper, None, now)
    db.commit()
    return pin


def list_accounts(db: Session, who: Principal, role: str, page: PageParams) -> Page[AccountOut]:
    rows = repo.users_in_shop(db, _shop_id(who), role, page.after_id, page.limit)
    last = rows[page.limit - 1].id if len(rows) > page.limit else None
    return build_page([account_out(u) for u in rows], page.limit, last)


def create_account(db: Session, who: Principal, role: str, body: AccountIn, pepper: str) -> AccountOut:
    shop_id = _shop_id(who)
    _check_pin(role, body.pin)
    _check_language(db, shop_id, body.language)
    shop = repo.shop_by_id(db, shop_id)
    if shop is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    user = User(
        role=role,
        display_name=body.display_name,
        language=body.language,
        shop_id=shop_id,
        district_id=shop.default_district_id,
        pin_hash=hash_pin(pepper, body.pin),
        pin_must_change=True,
    )
    db.add(user)
    _flush_unique(db)
    # A new account joins the shop's group Threads, so a Worker of a new shop has a chat from the start.
    groups = db.scalars(select(Thread.id).where(Thread.shop_id == shop_id, Thread.kind == "group"))
    db.add_all(ThreadMember(thread_id=g, user_id=user.id, shop_id=shop_id) for g in groups)
    db.commit()
    return account_out(user)


def update_account(
    db: Session, who: Principal, role: str, user_id: int, body: AccountUpdate, pepper: str, now: datetime
) -> AccountOut:
    shop_id = _shop_id(who)
    user = repo.user_in_shop(db, shop_id, user_id, role)
    if user is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    if body.display_name is not None:
        user.display_name = body.display_name
    if body.language is not None:
        _check_language(db, shop_id, body.language)
        user.language = body.language
    if body.pin is not None:
        _check_pin(role, body.pin)
        _set_first_pin(db, user, body.pin, pepper, who.user_id, now)
    if body.active is False and user.active:
        if role == "foreman" and user.id == who.user_id:
            raise ApiError(409, "CONFLICT", "You cannot deactivate yourself")
        if role == "foreman" and repo.active_foremen_count(db, shop_id) <= 1:
            raise ApiError(409, "CONFLICT", "A shop needs at least one active Foreman")
        user.active = False
        repo.revoke_sessions_of(db, user.id, now)
    elif body.active is True:
        user.active = True
    _flush_unique(db)
    db.commit()
    return account_out(user)
