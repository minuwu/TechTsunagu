"""Skill views (WS5, architecture §9.3): Skill Matrix, Heatmap, Skill Passport. Thin: parse → service → envelope."""

from typing import Annotated

from fastapi import APIRouter, Depends

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.schemas.common import IdIn
from app.schemas.skills import Heatmap, Passport, SkillMatrix
from app.services import skills
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["work"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]


@router.get("/skill-matrix", summary="Skill Matrix (力量表): every Worker × Machine with its Sign-off status")
def skill_matrix(who: Foreman, db: DbSession) -> Envelope[SkillMatrix]:
    return Envelope(data=skills.matrix(db, who, now_utc()))


@router.get("/heatmap", summary="Confusions per Glossary Term × Machine, most confused term first")
def heatmap(who: Foreman, db: DbSession) -> Envelope[Heatmap]:
    return Envelope(data=skills.heatmap(db, who))


@router.get("/workers/{worker_id}/passport", summary="Skill Passport: a Foreman sees any Worker, a Worker only self")
def passport(who: ShopMember, worker_id: IdIn, db: DbSession) -> Envelope[Passport]:
    return Envelope(data=skills.passport(db, who, int(worker_id), now_utc()))
