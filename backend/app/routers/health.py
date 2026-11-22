from typing import Annotated, Literal

from fastapi import APIRouter, Depends
from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from app.api.envelope import Envelope
from app.api.errors import ApiError
from app.db import get_session
from app.schemas.common import CamelModel

router = APIRouter(prefix="/api", tags=["health"])


class Health(CamelModel):
    status: Literal["ok"]


@router.get("/health", summary="Liveness: the process is up (no database call)")
def health() -> Envelope[Health]:
    return Envelope(data=Health(status="ok"))


@router.get("/health/ready", summary="Readiness: the database answers")
def ready(session: Annotated[Session, Depends(get_session)]) -> Envelope[Health]:
    try:
        session.execute(text("SELECT 1"))
    except SQLAlchemyError as exc:
        raise ApiError(503, "NOT_READY", "Database unavailable", headers={"Retry-After": "5"}) from exc
    return Envelope(data=Health(status="ok"))
