"""Skill Matrix, Heatmap and Skill Passport (WS5, architecture §6.6). Statuses come from app.domain.skills, the same
rule the Foreman Board and the Panel use."""

from datetime import datetime

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.domain.skills import SignOffFact, skill_status
from app.models.scopes import User
from app.models.work import Machine
from app.repositories import accounts
from app.repositories import skills as repo
from app.repositories import work as work_repo
from app.schemas.skills import HeatCell, Heatmap, HeatRow, Passport, SkillCell, SkillMachine, SkillMatrix, SkillRow
from app.services.auth import Principal
from app.services.notes import shop_of


def _machine(m: Machine) -> SkillMachine:
    return SkillMachine(id=str(m.id), code=m.code, name=m.name)


def _cells(
    worker_id: int, machines: list[Machine], newest: dict[tuple[int, int], tuple[int, datetime]], now: datetime
) -> list[SkillCell]:
    out: list[SkillCell] = []
    for m in machines:
        last = newest.get((worker_id, m.id))
        status = skill_status(SignOffFact(*last) if last else None, m.recheck_version_no, now)
        out.append(SkillCell(machine_id=str(m.id), status=status, passed_at=last[1] if last else None))
    return out


def matrix(db: Session, who: Principal, now: datetime) -> SkillMatrix:
    shop_id = shop_of(who)
    machines, newest = work_repo.all_machines(db, shop_id), work_repo.newest_sign_offs(db, shop_id)
    rows = [
        SkillRow(worker_id=str(w.id), display_name=w.display_name, cells=_cells(w.id, machines, newest, now))
        for w in accounts.roster_of_shop(db, shop_id, "worker")
    ]
    return SkillMatrix(machines=[_machine(m) for m in machines], rows=rows)


def heatmap(db: Session, who: Principal) -> Heatmap:
    shop_id = shop_of(who)
    machines, counts = work_repo.all_machines(db, shop_id), repo.confusion_counts(db, shop_id)
    rows: list[HeatRow] = []
    for t in work_repo.glossary(db, shop_id):
        cells = [HeatCell(machine_id=str(m.id), count=counts.get((t.id, m.id), 0)) for m in machines]
        total = sum(c.count for c in cells)
        if total:
            rows.append(
                HeatRow(
                    term_id=str(t.id), ja=t.ja, reading=t.reading, translations=t.translations, cells=cells, total=total
                )
            )
    rows.sort(key=lambda r: r.total, reverse=True)
    return Heatmap(machines=[_machine(m) for m in machines], rows=rows)


def passport(db: Session, who: Principal, worker_id: int, now: datetime) -> Passport:
    shop_id = shop_of(who)
    if who.role == "worker" and who.user_id != worker_id:
        raise ApiError(403, "FORBIDDEN", "You can only see your own Skill Passport")
    worker: User | None = next((w for w in accounts.roster_of_shop(db, shop_id, "worker") if w.id == worker_id), None)
    if worker is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    machines, newest = work_repo.all_machines(db, shop_id), work_repo.newest_sign_offs(db, shop_id)
    return Passport(
        worker_id=str(worker.id),
        display_name=worker.display_name,
        machines=[_machine(m) for m in machines],
        cells=_cells(worker.id, machines, newest, now),
    )
