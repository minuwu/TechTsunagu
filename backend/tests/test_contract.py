"""docs/openapi.yaml is generated from the code (ADR 0006). Regenerate it with:
cd backend; uv run python -m app.openapi > ../docs/openapi.yaml"""

import json
from pathlib import Path

from app.openapi import contract

CONTRACT = Path(__file__).resolve().parents[2] / "docs" / "openapi.yaml"


def test_the_committed_contract_matches_the_code() -> None:
    assert CONTRACT.read_text(encoding="utf-8") == contract(), (
        "run: uv run python -m app.openapi > ../docs/openapi.yaml"
    )


def test_no_worker_facing_schema_carries_the_answer_key() -> None:
    schemas: dict[str, dict[str, object]] = json.loads(contract())["components"]["schemas"]
    for name in ("CheckQuestionForWorker", "NoteForWorker", "WrongQuestion", "CheckResult", "MachineDetail"):
        assert "correctIndex" not in json.dumps(schemas[name]), name
