"""The role guard (architecture §3.3). Every protected route declares its roles with `require(...)`."""

from collections.abc import Callable
from datetime import UTC, datetime
from typing import Annotated

from fastapi import Depends, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.config import Settings
from app.db import get_session
from app.services import auth as auth_service
from app.services.auth import Principal

DbSession = Annotated[Session, Depends(get_session)]


def now_utc() -> datetime:
    return datetime.now(UTC)


def get_settings(request: Request) -> Settings:
    settings: Settings = request.app.state.settings
    return settings


AppSettings = Annotated[Settings, Depends(get_settings)]


def client_ip(request: Request) -> str:
    return request.client.host if request.client else "unknown"


def _bearer(request: Request) -> str:
    scheme, _, token = request.headers.get("authorization", "").partition(" ")
    if scheme.lower() != "bearer" or not token:
        raise ApiError(401, "UNAUTHORIZED", "Sign in first")
    return token.strip()


# Declares the Bearer scheme in the OpenAPI contract (XREQ-008); auto_error off so our own 401 envelope still answers.
_BEARER_SCHEME = HTTPBearer(auto_error=False)
BearerDeclared = Annotated[HTTPAuthorizationCredentials | None, Depends(_BEARER_SCHEME)]


def require(*roles: str, allow_pin_change: bool = False) -> Callable[..., Principal]:
    """Dependency: 401 without a valid session, 403 for another role or before the first-login PIN change."""

    def dependency(request: Request, db: DbSession, _: BearerDeclared) -> Principal:
        who = auth_service.authenticate(db, _bearer(request), now_utc())
        if who.role not in roles:
            raise ApiError(403, "FORBIDDEN", "Not allowed for your role")
        if who.must_change_pin and not allow_pin_change:
            raise ApiError(403, "PIN_CHANGE_REQUIRED", "Choose your own PIN first")
        return who

    dependency.__name__ = f"require_{'_'.join(roles)}"
    return dependency
