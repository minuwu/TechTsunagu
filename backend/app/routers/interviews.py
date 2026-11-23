"""Periodic Interviews, Today's Word (XREQ-010) and the app platform routes (XREQ-009).
Thin: parse → service → envelope."""

import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, Header, Query

from app.api.auth import AppSettings, DbSession, now_utc, require
from app.api.envelope import Envelope
from app.schemas.common import IdIn
from app.schemas.interviews import InterviewDue, InterviewIn, InterviewOut, TodayWord
from app.schemas.platform import AppConfig, DeviceIn, DeviceOut
from app.services import interviews, platform
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["life"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
OptionalKey = Annotated[uuid.UUID | None, Header(alias="Idempotency-Key")]


@router.get("/interviews/due", summary="Periodic Interviews (support item 10): next due date per Worker")
def interviews_due(who: Foreman, db: DbSession) -> Envelope[list[InterviewDue]]:
    return Envelope(data=interviews.due(db, who, now_utc()))


@router.get("/workers/{worker_id}/interviews", summary="A Worker's past Periodic Interviews, newest first")
def worker_interviews(who: Foreman, worker_id: IdIn, db: DbSession) -> Envelope[list[InterviewOut]]:
    return Envelope(data=interviews.history(db, who, int(worker_id)))


@router.post("/workers/{worker_id}/interviews", status_code=201, summary="Record a Periodic Interview")
def record_interview(
    who: Foreman, worker_id: IdIn, body: InterviewIn, db: DbSession, key: OptionalKey = None
) -> Envelope[InterviewOut]:
    return Envelope(data=interviews.record(db, who, int(worker_id), body, key, now_utc()))


@router.get("/today-word", summary="Today's Word: one word of the Workers' language for the Japanese side")
def today_word(who: Foreman, db: DbSession) -> Envelope[TodayWord]:
    return Envelope(data=interviews.today_word(db, who, now_utc()))


@router.get("/app/config", summary="Android version gate (public): minimum and latest version, download link")
def app_config(settings: AppSettings) -> Envelope[AppConfig]:
    return Envelope(data=platform.app_config(settings, now_utc()))


@router.put("/devices/me", summary="Register this app install for push (upsert; 6 per minute)")
def register_device(who: ShopMember, body: DeviceIn, db: DbSession) -> Envelope[DeviceOut]:
    return Envelope(data=platform.register(db, who, body, now_utc()))


@router.delete("/devices/me", summary="Forget this app install before signing out (idempotent)")
def unregister_device(
    who: ShopMember, db: DbSession, install_id: Annotated[uuid.UUID, Query(alias="installId")]
) -> Envelope[None]:
    platform.unregister(db, who, install_id)
    return Envelope(data=None)
