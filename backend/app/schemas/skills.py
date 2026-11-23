"""Skill views (WS5, architecture §6.6, §9.3): Skill Matrix, Confusion Heatmap, Skill Passport. Read-only aggregates."""

from datetime import datetime

from app.domain.skills import Status
from app.schemas.common import CamelModel


class SkillMachine(CamelModel):
    id: str
    code: str
    name: dict[str, str]


class SkillCell(CamelModel):
    machine_id: str
    status: Status
    passed_at: datetime | None


class SkillRow(CamelModel):
    worker_id: str
    display_name: str
    cells: list[SkillCell]


class SkillMatrix(CamelModel):
    """GET /skill-matrix: every active Worker × every Machine of the shop (力量表)."""

    machines: list[SkillMachine]
    rows: list[SkillRow]


class HeatCell(CamelModel):
    machine_id: str
    count: int


class HeatRow(CamelModel):
    term_id: str
    ja: str
    reading: str
    translations: dict[str, str]
    cells: list[HeatCell]
    total: int


class Heatmap(CamelModel):
    """GET /heatmap: Confusions per Glossary Term × Machine; only terms with at least one, most confused first."""

    machines: list[SkillMachine]
    rows: list[HeatRow]


class Passport(CamelModel):
    """GET /workers/{id}/passport: one Worker's Sign-off status on every Machine of the shop."""

    worker_id: str
    display_name: str
    machines: list[SkillMachine]
    cells: list[SkillCell]
