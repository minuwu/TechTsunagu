"""Engine and per-request session (architecture §8.3, §8.4): small pool, pre-ping, fast failure when exhausted."""

from collections.abc import Iterator

from fastapi import Request
from sqlalchemy import Engine, create_engine
from sqlalchemy.orm import Session, sessionmaker


def make_engine(url: str) -> Engine:
    return create_engine(url, pool_size=5, max_overflow=5, pool_timeout=10, pool_pre_ping=True)


def make_session_factory(engine: Engine) -> sessionmaker[Session]:
    return sessionmaker(bind=engine, expire_on_commit=False)


def get_session(request: Request) -> Iterator[Session]:
    """One session per request. Services open short transactions with `session.begin()`; never across an AI call."""
    factory: sessionmaker[Session] = request.app.state.session_factory
    with factory() as session:
        yield session
