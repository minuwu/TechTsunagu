from typing import Literal

from fastapi import APIRouter

from app.api.envelope import Envelope
from app.schemas.common import CamelModel

router = APIRouter(prefix="/api", tags=["health"])


class Health(CamelModel):
    status: Literal["ok"]


@router.get("/health", summary="Liveness: the process is up (no database call)")
def health() -> Envelope[Health]:
    return Envelope(data=Health(status="ok"))
