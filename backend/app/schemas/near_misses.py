"""Near-miss Reports (WS7, architecture §9.3). Shape follows frontend/src/domain/wakatta.ts `NearMissReport`."""

from typing import Annotated, Literal

from pydantic import AwareDatetime, Field

from app.schemas.common import CamelModel, IdIn, Translated

Severity = Literal["low", "mid", "high"]
Fraction = Annotated[float, Field(ge=0, le=1, allow_inf_nan=False)]


class PhotoMark(CamelModel):
    """Where the danger is on the photo: fractions of its width and height (0 to 1)."""

    x: Fraction
    y: Fraction


class NearMissOut(CamelModel):
    id: str
    worker_id: str
    worker_name: str
    """The reporter's display name."""
    machine_id: str
    text: Translated
    """Japanese for the Foreman, plus the Worker's own words in their language."""
    text_ja: str
    """The same Japanese as `text.ja`."""
    lang: str | None
    """The language of the Worker's own words, or null when only the Japanese is stored (a recording)."""
    severity: Severity | None
    photo: str | None
    """`/api/near-misses/{id}/photo` (Bearer), or null."""
    photo_mark: PhotoMark | None
    task_id: str | None
    """The Task made from this report."""
    created_at: AwareDatetime
    acknowledged_at: AwareDatetime | None


class NearMissTaskIn(CamelModel):
    """PUT /near-misses/{id}/task: the Task the Foreman made from the report."""

    task_id: IdIn
