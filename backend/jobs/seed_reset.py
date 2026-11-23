"""Resets the demo: truncates every table and loads the demo data (`python /app/jobs/seed_reset.py`, the wakatta-seed job).
Needs MIGRATION_DATABASE_URL (the owner) and PIN_PEPPER. Run it again whenever the demo should start from scratch."""

import runpy
import sys

if __name__ == "__main__":
    sys.argv = ["app.seed", "--reset"]
    runpy.run_module("app.seed", run_name="__main__")
