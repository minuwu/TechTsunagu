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
    for name in (
        "CheckQuestionForWorker",
        "NoteForWorker",
        "WrongQuestion",
        "CheckResult",
        "MachineDetail",
        "BriefingForWorker",
    ):
        assert "correctIndex" not in json.dumps(schemas[name]), name


def test_protected_routes_declare_bearer_and_their_roles() -> None:
    spec = json.loads(contract())
    assert spec["components"]["securitySchemes"]["HTTPBearer"] == {"type": "http", "scheme": "bearer"}
    board = spec["paths"]["/api/board"]["get"]
    assert board["security"] == [{"HTTPBearer": []}] and board["x-roles"] == ["foreman"]
    assert spec["paths"]["/api/notices"]["get"]["x-roles"] == ["poster", "worker"]
    assert "security" not in spec["paths"]["/api/auth/roster"]["get"]  # public
    assert "security" not in spec["paths"]["/api/senpai/{token}"]["get"]  # the link token is the credential
