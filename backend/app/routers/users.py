from typing import Annotated

from fastapi import APIRouter, Depends

from app.api.auth import AppSettings, DbSession, now_utc, require
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.schemas.auth import AccountIn, AccountOut, AccountUpdate
from app.schemas.common import IdIn
from app.services import users as service
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["accounts"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
Paging = Annotated[PageParams, Depends(page_params)]


def _routes(role: str, path: str) -> None:
    @router.get(f"/{path}", summary=f"{role.title()}s of your shop", name=f"list_{path}")
    def list_accounts(who: Foreman, db: DbSession, page: Paging) -> Envelope[Page[AccountOut]]:
        return Envelope(data=service.list_accounts(db, who, role, page))

    @router.post(f"/{path}", status_code=201, summary=f"Create a {role} account", name=f"create_{path}")
    def create_account(who: Foreman, body: AccountIn, db: DbSession, settings: AppSettings) -> Envelope[AccountOut]:
        pepper = settings.pin_pepper.get_secret_value()
        return Envelope(data=service.create_account(db, who, role, body, pepper))

    @router.patch(f"/{path}/{{user_id}}", summary=f"Rename, reset PIN or deactivate a {role}", name=f"update_{path}")
    def update_account(
        who: Foreman, user_id: IdIn, body: AccountUpdate, db: DbSession, settings: AppSettings
    ) -> Envelope[AccountOut]:
        pepper = settings.pin_pepper.get_secret_value()
        return Envelope(data=service.update_account(db, who, role, int(user_id), body, pepper, now_utc()))

    _ = (list_accounts, create_account, update_account)


_routes("worker", "workers")
_routes("foreman", "foremen")
