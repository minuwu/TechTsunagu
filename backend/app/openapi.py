"""Writes the API contract (ADR 0006): `uv run python -m app.openapi > ../docs/openapi.yaml`.

The output is JSON, which is valid YAML, so no YAML dependency is needed. tests/test_contract.py fails if the file
and the code disagree, so a route change always ships with its contract."""

import json

from app.config import Settings
from app.main import create_app

# Placeholder settings: building the schema never connects to a database or a model.
_SETTINGS = Settings(
    frontend_origin="http://localhost:3000",
    database_url="postgresql+psycopg://contract@localhost/contract",
    pin_pepper="contract-generation-only-not-a-secret-000",  # pyright: ignore[reportArgumentType]
    _env_file=None,  # pyright: ignore[reportCallIssue]
)


def contract() -> str:
    return json.dumps(create_app(_SETTINGS).openapi(), indent=2, ensure_ascii=False) + "\n"


if __name__ == "__main__":
    print(contract(), end="")  # noqa: T201
