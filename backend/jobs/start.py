"""Container entry point (Dockerfile CMD). Azure for Students only offers "express" Container Apps environments,
which have no Jobs, so the one-off steps run here, before the API starts:

1. ADMIN_DATABASE_URL set (first boot only; azure-setup.ps1 removes it right after) -> roles + database (dbinit.py).
2. MIGRATION_DATABASE_URL set -> `alembic upgrade head` (safe because the app runs exactly one replica).
3. SEED_ON_START=reset -> wipe and reload the demo data (set for one revision only: deploy/reset-demo.ps1).

Tradeoff: the API container holds the owner credential for step 2. Upgrade path once Jobs are available (a non-express
environment): move steps 1-3 back into Container Apps Jobs and give the API only DATABASE_URL.
Then the process replaces itself with uvicorn, so platform signals reach the server directly."""

import os
import runpy
import sys
from pathlib import Path

from alembic.config import main as alembic

HOST = "0.0.0.0"  # noqa: S104  # every interface, as a container must; only Azure's ingress can reach it
UVICORN = [
    "uvicorn", "app.main:create_app", "--factory", "--host", HOST, "--port", "8000",
    "--no-access-log", "--proxy-headers", "--forwarded-allow-ips=*",
]  # fmt: skip


def main() -> None:
    if os.environ.get("ADMIN_DATABASE_URL"):
        runpy.run_path(str(Path(__file__).with_name("dbinit.py")), run_name="__main__")
    if os.environ.get("MIGRATION_DATABASE_URL"):
        alembic(argv=["upgrade", "head"])
    if os.environ.get("SEED_ON_START") == "reset":
        sys.argv = ["app.seed", "--reset"]
        runpy.run_module("app.seed", run_name="__main__")
    os.execvp(UVICORN[0], UVICORN)  # noqa: S606  # fixed argv, no user input


if __name__ == "__main__":
    main()
