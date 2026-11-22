"""Foremen manage the Worker and Foreman accounts of their own shop (architecture §3.4)."""

from datetime import datetime

from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, build_page
from app.domain.pins import pin_problem
from app.models.scopes import User
from app.repositories import accounts as repo
from app.schemas.auth import AccountIn, AccountOut, AccountUpdate
from app.services.auth import Principal
from app.services.credentials import hash_pin


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
        user.pin_hash = hash_pin(pepper, body.pin)
        user.pin_must_change = True
        user.pin_failed_count = 0
        user.pin_locked_until = None
        repo.revoke_sessions_of(db, user.id, now)
        repo.audit(db, "pin_reset", who.user_id, shop_id, subject_id=user.id)
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
