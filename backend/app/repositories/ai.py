"""AI cache and daily budget rows (architecture §6.4, §10.3)."""

from datetime import date

from sqlalchemy import update
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.platform import AiCache, AiUsage


def cached(db: Session, key: str) -> AiCache | None:
    return db.get(AiCache, key)


def store(
    db: Session, key: str, scope: str, pipeline: str, version: int, model: str, response: dict[str, object]
) -> None:
    stmt = insert(AiCache).values(
        key=key, scope=scope, pipeline=pipeline, prompt_version=version, model=model, response=response
    )
    db.execute(stmt.on_conflict_do_nothing(index_elements=[AiCache.key]))


def take_budget(db: Session, day: date, model: str, budget: int) -> bool:
    """Atomically counts one call if the day still has budget and the quota is not exhausted."""
    db.execute(insert(AiUsage).values(day=day, model=model, calls=0).on_conflict_do_nothing())
    stmt = (
        update(AiUsage)
        .where(AiUsage.day == day, AiUsage.model == model, AiUsage.calls < budget, AiUsage.exhausted.is_(False))
        .values(calls=AiUsage.calls + 1)
        .returning(AiUsage.calls)
    )
    return db.execute(stmt).scalar_one_or_none() is not None


def mark_exhausted(db: Session, day: date, model: str) -> None:
    db.execute(update(AiUsage).where(AiUsage.day == day, AiUsage.model == model).values(exhausted=True))
