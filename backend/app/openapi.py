"""Writes the API contract (ADR 0006): `uv run python -m app.openapi > ../docs/openapi.yaml`, and the endpoint index of
docs/API_SPEC.md: `uv run python -m app.openapi index`.

The output is JSON, which is valid YAML, so no YAML dependency is needed. tests/test_contract.py fails if a file
and the code disagree, so a route change always ships with its contract."""

import json
import re
import sys
from pathlib import Path
from typing import Any

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


API_SPEC = Path(__file__).resolve().parents[2] / "docs" / "API_SPEC.md"
_INDEX = re.compile(r"(<!-- endpoint index: generated -->\n).*?(<!-- end of endpoint index -->)", re.DOTALL)


def with_endpoint_index(api_spec: str) -> str:
    """docs/API_SPEC.md with the endpoint table of section 3 rebuilt from the contract."""
    paths: dict[str, dict[str, dict[str, Any]]] = json.loads(contract())["paths"]
    rows = [
        f"| `{method.upper()}` | `{path}` | {_access(op)} | {op.get('summary', '')} |"
        for path, operations in paths.items()
        for method, op in operations.items()
    ]
    head = [f"{len(rows)} operations on {len(paths)} paths, all live.", ""]
    table = ["| Method | Path | Who | What |", "| :--- | :--- | :--- | :--- |", *rows]
    return _INDEX.sub(lambda m: m[1] + "\n".join([*head, *table, ""]) + m[2], api_spec)


def _access(operation: dict[str, Any]) -> str:
    roles: list[str] | None = operation.get("x-roles")
    return ", ".join(roles) if roles else "**public**"


if __name__ == "__main__":
    if sys.argv[1:] == ["index"]:
        API_SPEC.write_text(with_endpoint_index(API_SPEC.read_text(encoding="utf-8")), encoding="utf-8", newline="\n")
    else:
        print(contract(), end="")  # noqa: T201
