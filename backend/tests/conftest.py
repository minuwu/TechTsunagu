from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from app.config import Settings
from app.main import create_app

FRONTEND = "http://localhost:3000"


@pytest.fixture
def settings() -> Settings:
    return Settings(frontend_origin=FRONTEND, _env_file=None)  # pyright: ignore[reportCallIssue]


@pytest.fixture
def client(settings: Settings) -> Iterator[TestClient]:
    with TestClient(create_app(settings), raise_server_exceptions=False) as c:
        yield c
