"""Near-miss Reports (WS7, architecture §9.3). Shape follows frontend/src/domain/wakatta.ts `NearMissReport`."""

from pydantic import AwareDatetime

from app.schemas.common import CamelModel, Translated


class NearMissOut(CamelModel):
    id: str
    worker_id: str
    machine_id: str
    text: Translated
    """Japanese for the Foreman, plus the Worker's own words in their language."""
    photo: str | None
    """`/api/near-misses/{id}/photo` (Bearer), or null."""
    created_at: AwareDatetime
    acknowledged_at: AwareDatetime | None
