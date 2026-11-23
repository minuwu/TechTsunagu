"""AI_CHAIN, AI_TIER and GROQ_API_KEY build the chain the runner walks (XREQ-003)."""

import pytest
from sqlalchemy.orm import sessionmaker

from app.ai.chains import BUDGETS, GEMINI_CHAINS, GROQ_MODEL
from app.config import Settings
from app.main import ai_runner

BASE = {
    "frontend_origin": "http://localhost:3000",
    "database_url": "postgresql+psycopg://u:p@h/d",
    "pin_pepper": "x" * 32,
}


def _settings(**values: object) -> Settings:
    return Settings(**BASE, **values, _env_file=None)  # pyright: ignore[reportCallIssue, reportArgumentType]


def test_every_chain_model_has_a_budget_in_both_tiers() -> None:
    models = {m for chain in GEMINI_CHAINS.values() for m in chain} | {GROQ_MODEL}
    for tier in BUDGETS.values():
        assert models <= tier.keys()


@pytest.mark.parametrize(
    ("values", "models"),
    [
        ({}, ["fake"]),  # AI_MODE=fake by default: no network
        ({"ai_mode": "gemini", "gemini_api_key": "k"}, ["gemini-3.5-flash-lite", "gemini-3.1-flash-lite"]),
        (
            {"ai_mode": "gemini", "gemini_api_key": "k", "ai_chain": "demo", "groq_api_key": "g"},
            [*GEMINI_CHAINS["demo"], GROQ_MODEL],
        ),
    ],
)
def test_settings_pick_the_chain(values: dict[str, object], models: list[str]) -> None:
    assert ai_runner(_settings(**values), sessionmaker()).models == models
