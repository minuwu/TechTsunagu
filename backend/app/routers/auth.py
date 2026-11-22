from typing import Annotated, Literal

from fastapi import APIRouter, Depends, Query, Request

from app.api.auth import AppSettings, DbSession, client_ip, now_utc, require
from app.api.envelope import Envelope
from app.schemas.auth import LoginIn, LoginOut, PinChangeIn, RosterEntry, ScopeCode, UserOut
from app.services import auth as service
from app.services.auth import Principal

router = APIRouter(prefix="/api/auth", tags=["auth"])
SignedIn = Annotated[Principal, Depends(require("foreman", "worker", "poster", allow_pin_change=True))]


@router.get("/roster", summary="Names to pick from, for a shop or district code (public, rate-limited)")
def roster(
    request: Request, db: DbSession, code: Annotated[ScopeCode, Query()], role: Literal["worker", "foreman", "poster"]
) -> Envelope[list[RosterEntry]]:
    users = service.roster(db, code, role, client_ip(request), now_utc())
    return Envelope(data=[RosterEntry(id=str(u.id), display_name=u.display_name) for u in users])


@router.post("/login", summary="Name + PIN → Bearer token")
def login(request: Request, body: LoginIn, db: DbSession, settings: AppSettings) -> Envelope[LoginOut]:
    pepper = settings.pin_pepper.get_secret_value()
    token, expires_at, user = service.login(db, int(body.user_id), body.pin, client_ip(request), pepper, now_utc())
    out = LoginOut(
        token=token, expires_at=expires_at, must_change_pin=user.pin_must_change, user=service.user_out(user)
    )
    return Envelope(data=out)


@router.get("/me", summary="The signed-in user")
def me(who: SignedIn, db: DbSession) -> Envelope[UserOut]:
    return Envelope(data=service.user_out(service.current_user(db, who)))


@router.put("/me/pin", summary="Change your own PIN; your other sessions are signed out")
def change_pin(who: SignedIn, body: PinChangeIn, db: DbSession, settings: AppSettings) -> Envelope[None]:
    pepper = settings.pin_pepper.get_secret_value()
    service.change_pin(db, who, body.current_pin, body.new_pin, pepper, now_utc())
    return Envelope(data=None)


@router.post("/logout", summary="Revoke this session")
def logout(who: SignedIn, db: DbSession) -> Envelope[None]:
    service.logout(db, who, now_utc())
    return Envelope(data=None)
