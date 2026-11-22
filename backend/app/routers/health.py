from typing import Literal

from fastapi import APIRouter

from app.api.auth import DbSession
from app.api.envelope import Envelope
from app.api.errors import ApiError
from app.schemas.common import CamelModel
from app.services import health as service

router = APIRouter(prefix="/api", tags=["health"])


class Health(CamelModel):
    status: Literal["ok"]


@router.get("/health", summary="Liveness: the process is up (no database call)")
def health() -> Envelope[Health]:
    return Envelope(data=Health(status="ok"))


@router.get("/health/ready", summary="Readiness: the database answers")
def ready(db: DbSession) -> Envelope[Health]:
    if not service.database_ready(db):
        raise ApiError(503, "NOT_READY", "Database unavailable", headers={"Retry-After": "5"})
    return Envelope(data=Health(status="ok"))
