"""Accounts and login contract (architecture §3, §9.3)."""

from datetime import datetime
from typing import Annotated, Literal

from pydantic import StringConstraints

from app.schemas.common import CamelModel, IdIn, Lang

Role = Literal["foreman", "worker", "poster"]
Pin = Annotated[str, StringConstraints(min_length=4, max_length=6)]
ScopeCode = Annotated[str, StringConstraints(pattern=r"^[A-Z0-9]{8}$")]
DisplayName = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=40)]


class RosterEntry(CamelModel):
    id: str
    display_name: str


class LoginIn(CamelModel):
    user_id: IdIn
    pin: Pin


class UserOut(CamelModel):
    id: str
    display_name: str
    role: Role
    language: Lang
    shop_id: str | None
    district_id: str | None
    # The code Workers sign in with, so a Foreman can share it (ADR 0017); None for Posters.
    shop_code: str | None = None


class LoginOut(CamelModel):
    token: str
    expires_at: datetime
    must_change_pin: bool
    user: UserOut


class PinChangeIn(CamelModel):
    current_pin: Pin
    new_pin: Pin


class AccountIn(CamelModel):
    display_name: DisplayName
    language: Lang
    pin: Pin


class AccountUpdate(CamelModel):
    display_name: DisplayName | None = None
    language: Lang | None = None
    pin: Pin | None = None
    active: bool | None = None


class AccountOut(CamelModel):
    id: str
    display_name: str
    role: Role
    language: Lang
    active: bool
    must_change_pin: bool


class RegisterIn(CamelModel):
    """POST /api/auth/register (ADR 0017): a new shop and its first Foreman."""

    shop_name: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=60)]
    display_name: DisplayName
    pin: Pin


class RegisterOut(LoginOut):
    shop_code: str
