"""Writes the API contract (ADR 0006): `uv run python -m app.openapi > ../docs/openapi.yaml`.

The output is JSON, which is valid YAML, so no YAML dependency is needed. tests/test_contract.py fails if the file
and the code disagree, so a route change always ships with its contract."""

import json

from fastapi.routing import APIRoute

from app.config import Settings
from app.main import ROUTERS, create_app

# Placeholder settings: building the schema never connects to a database or a model.
_SETTINGS = Settings(
    frontend_origin="http://localhost:3000",
    database_url="postgresql+psycopg://contract@localhost/contract",
    pin_pepper="contract-generation-only-not-a-secret-000",  # pyright: ignore[reportArgumentType]
    _env_file=None,  # pyright: ignore[reportCallIssue]
)


def contract() -> str:
    spec = create_app(_SETTINGS).openapi()
    # The roles each route allows (contract D-23, B-15), read from its require(...) dependency's name.
    for route in (r for router in ROUTERS for r in router.routes if isinstance(r, APIRoute)):
        guards = [getattr(d.call, "__name__", "") for d in route.dependant.dependencies]
        roles = next((name.removeprefix("require_").split("_") for name in guards if name.startswith("require_")), None)
        methods: set[str] = set(route.methods or ()) if roles else set()
        for method in methods:
            spec["paths"][route.path_format][method.lower()]["x-roles"] = roles
    return json.dumps(spec, indent=2, ensure_ascii=False) + "\n"


if __name__ == "__main__":
    print(contract(), end="")  # noqa: T201
