"""Test fixtures. Tests run against a real PostgreSQL (architecture §14): the schema is rebuilt once per session by
the migration (as the owner), and every test runs as the app role inside a transaction that is rolled back."""

import os
from collections.abc import Iterator
from pathlib import Path

import pytest
from alembic import command
from alembic.config import Config
from fastapi.testclient import TestClient
from sqlalchemy import Connection, Engine, create_engine, text
from sqlalchemy.orm import Session, sessionmaker

from app.config import Settings
from app.main import create_app

FRONTEND = "http://localhost:3000"
APP_URL = os.environ.get(
    "TEST_DATABASE_URL", "postgresql+psycopg://wakatta_app:wakatta_app_dev@127.0.0.1:5432/wakatta_test"
)
OWNER_URL = os.environ.get(
    "TEST_MIGRATION_DATABASE_URL", "postgresql+psycopg://wakatta_owner:wakatta_owner_dev@127.0.0.1:5432/wakatta_test"
)
APP_ROLE = os.environ.get("APP_DB_ROLE", "wakatta_app")
BACKEND = Path(__file__).resolve().parent.parent


def _alembic(connection: Connection) -> Config:
    cfg = Config(str(BACKEND / "alembic.ini"))
    cfg.set_main_option("script_location", str(BACKEND / "migrations"))
    cfg.attributes["connection"] = connection
    cfg.attributes["configure_logger"] = False
    return cfg


@pytest.fixture(scope="session")
def migrated_schema() -> None:
    """Fresh schema, then upgrade → downgrade → upgrade, so every run also proves the migration is reversible."""
    owner = create_engine(OWNER_URL)
    with owner.begin() as conn:
        conn.execute(text("DROP SCHEMA IF EXISTS public CASCADE"))
        conn.execute(text("CREATE SCHEMA public"))
        conn.execute(text("REVOKE ALL ON SCHEMA public FROM PUBLIC"))
        conn.execute(text(f'GRANT USAGE ON SCHEMA public TO "{APP_ROLE}"'))
    for step in ("upgrade", "downgrade", "upgrade"):
        with owner.begin() as conn:
            cfg = _alembic(conn)
            if step == "upgrade":
                command.upgrade(cfg, "head")
            else:
                command.downgrade(cfg, "base")
    owner.dispose()


@pytest.fixture(scope="session")
def app_engine(migrated_schema: None) -> Iterator[Engine]:
    engine = create_engine(APP_URL)
    yield engine
    engine.dispose()


@pytest.fixture
def db(app_engine: Engine) -> Iterator[Session]:
    """A session as the app role; everything it (and the app under test) writes is rolled back."""
    with app_engine.connect() as conn:
        outer = conn.begin()
        factory = sessionmaker(bind=conn, join_transaction_mode="create_savepoint", expire_on_commit=False)
        with factory() as session:
            session.info["factory"] = factory
            yield session
        outer.rollback()


@pytest.fixture
def settings() -> Settings:
    return Settings(frontend_origin=FRONTEND, database_url=APP_URL, _env_file=None)  # pyright: ignore[reportCallIssue]


@pytest.fixture
def client(settings: Settings, db: Session) -> Iterator[TestClient]:
    factory: sessionmaker[Session] = db.info["factory"]
    with TestClient(create_app(settings, session_factory=factory), raise_server_exceptions=False) as c:
        yield c
