"""Which Gemini models the app calls, best first (Galib's AI_PLAN.md §2, §4). Changing a model is one line here.

Budgets stay just under each model's free tier (AI Studio, 2026-11-22): Flash 20 requests a day and 5 a minute,
Flash-Lite 500 a day and 15 a minute. Limits are per Google Cloud project, so every key in one project shares them."""

from dataclasses import dataclass
from typing import Literal

ChainName = Literal["dev", "demo"]


@dataclass(frozen=True)
class ModelSpec:
    name: str
    daily_budget: int
    per_minute: int


def _flash(name: str) -> ModelSpec:
    return ModelSpec(name, daily_budget=18, per_minute=4)


def _lite(name: str) -> ModelSpec:
    return ModelSpec(name, daily_budget=450, per_minute=12)


CHAINS: dict[ChainName, tuple[ModelSpec, ...]] = {
    # Prompt work, the eval, anyone testing real AI: the cheap models, 3.5 Flash-Lite was the best of the live check
    "dev": (_lite("gemini-3.5-flash-lite"), _lite("gemini-3.1-flash-lite")),
    # Filmed takes and the live demo: the strongest model first, the Flash-Lites keep the demo answering
    "demo": (
        _flash("gemini-3.8-flash"),
        _flash("gemini-3.6-flash"),
        _lite("gemini-3.5-flash-lite"),
        _lite("gemini-3.1-flash-lite"),
    ),
}
