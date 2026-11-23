"""Skill views (WS5, architecture §9.3): Skill Matrix, Heatmap, Skill Passport. Thin: parse → service → envelope."""

from typing import Annotated

from fastapi import APIRouter, Depends, Query

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.api.pagination import PageParams, page_params
from app.schemas.common import IdIn
from app.schemas.learning import Learning, Understanding
from app.schemas.skills import Heatmap, Passport, SkillMatrix
from app.services import learning, skills
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["work"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
Worker = Annotated[Principal, Depends(require("worker"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]


@router.get("/skill-matrix", summary="Skill Matrix (力量表): every Worker × Machine with its Sign-off status")
def skill_matrix(who: Foreman, db: DbSession) -> Envelope[SkillMatrix]:
    return Envelope(data=skills.matrix(db, who, now_utc()))


@router.get("/heatmap", summary="Confusions per Glossary Term × Machine, most confused term first")
def heatmap(who: Foreman, db: DbSession) -> Envelope[Heatmap]:
    return Envelope(data=skills.heatmap(db, who))


@router.get("/workers/me/learning", summary="Your Learning History: missed terms, newest first, and your streak")
def my_learning(who: Worker, db: DbSession, page: Paging) -> Envelope[Learning]:
    return Envelope(data=learning.learning(db, who, page, now_utc()))


@router.get("/analytics/understanding", summary="Wakatta Status trend per day and per Worker, and the top missed terms")
def understanding(
    who: Foreman, db: DbSession, days: Annotated[int, Query(ge=1, le=90)] = 30
) -> Envelope[Understanding]:
    return Envelope(data=learning.understanding(db, who, days, now_utc()))


@router.get("/workers/{worker_id}/passport", summary="Skill Passport: a Foreman sees any Worker, a Worker only self")
def passport(who: ShopMember, worker_id: IdIn, db: DbSession) -> Envelope[Passport]:
    return Envelope(data=skills.passport(db, who, int(worker_id), now_utc()))
