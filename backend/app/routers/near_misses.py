"""Near-miss routes (WS7, architecture §9.3). Thin: parse → service → envelope. Paths match
frontend/src/services/wakatta.ts (`/api/near-misses`, `/api/near-misses/{id}/ack`)."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Header, Request, Response, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.conditional import conditional_json
from app.api.envelope import Envelope
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES, MAX_PHOTO_BYTES
from app.schemas.common import IdIn
from app.schemas.near_misses import Fraction, NearMissOut, NearMissTaskIn, Severity
from app.services import near_misses
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["work"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
Worker = Annotated[Principal, Depends(require("worker"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]


def _read(upload: UploadFile | None, limit: int) -> bytes | None:
    # One byte over the limit is enough for the size check to refuse it; never read an unbounded upload.
    return (upload.file.read(limit + 1) or None) if upload is not None else None


@router.post("/near-misses", status_code=201, summary="Report a near miss by text or voice, with an optional photo")
def report(
    who: Worker,
    key: IdempotencyKey,
    db: DbSession,
    request: Request,
    machine_id: Annotated[IdIn, Form(alias="machineId")],
    text: Annotated[str | None, Form(max_length=1000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
    photo: Annotated[UploadFile | None, File()] = None,
    severity: Annotated[Severity | None, Form()] = None,
    mark_x: Annotated[Fraction | None, Form(alias="photoMarkX")] = None,
    mark_y: Annotated[Fraction | None, Form(alias="photoMarkY")] = None,
) -> Envelope[NearMissOut]:
    ai: AiRunner = request.app.state.ai
    voice, image = _read(audio, MAX_AUDIO_BYTES), _read(photo, MAX_PHOTO_BYTES)
    out = near_misses.create(
        db, who, int(machine_id), text, voice, image, key, ai, now_utc(), severity, (mark_x, mark_y)
    )
    return Envelope(data=out)


@router.get(
    "/near-misses",
    summary="Near-miss Reports, newest first (a Worker: only their own); If-None-Match gives 304",
    response_model=Envelope[Page[NearMissOut]],
)
def list_reports(who: ShopMember, db: DbSession, page: Paging, request: Request, open: bool = False) -> Response:
    return conditional_json(request, Envelope(data=near_misses.list_reports(db, who, open, page)))


@router.get("/near-misses/{report_id}", summary="One Near-miss Report (a Worker: only their own)")
def get_report(who: ShopMember, report_id: IdIn, db: DbSession) -> Envelope[NearMissOut]:
    return Envelope(data=near_misses.read(db, who, int(report_id)))


@router.put("/near-misses/{report_id}/task", summary="Link the Task made from a Near-miss Report")
def link_task(who: Foreman, report_id: IdIn, body: NearMissTaskIn, db: DbSession) -> Envelope[NearMissOut]:
    return Envelope(data=near_misses.link_task(db, who, int(report_id), int(body.task_id)))


@router.post("/near-misses/{report_id}/ack", summary="Acknowledge a Near-miss Report")
def acknowledge(who: Foreman, report_id: IdIn, db: DbSession) -> Envelope[NearMissOut]:
    return Envelope(data=near_misses.acknowledge(db, who, int(report_id), now_utc()))


@router.get(
    "/near-misses/{report_id}/photo", summary="The report's photo (JPEG, EXIF removed)", response_class=Response
)
def get_photo(who: ShopMember, report_id: IdIn, db: DbSession) -> Response:
    data, mime = near_misses.photo(db, who, int(report_id))
    return Response(data, media_type=mime, headers={"Cache-Control": "private, max-age=3600"})
