import logging

from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from app.repositories import health as repo

_log = logging.getLogger("wakatta.health")


def database_ready(db: Session) -> bool:
    try:
        repo.ping(db)
    except SQLAlchemyError as exc:
        _log.warning("database not ready", exc_info=exc)
        return False
    return True
