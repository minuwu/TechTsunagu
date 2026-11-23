"""Gemini errors map to what the chain does next (AI_PLAN.md §3): skip for the day, retry once, or move on."""

import pytest
from google.genai import errors

from app.ai.client import AiUnavailable, Overloaded, QuotaExhausted
from app.ai.gemini import map_error


def _error(code: int, status: str, quota_id: str = "") -> errors.APIError:
    details = [{"@type": "type.googleapis.com/google.rpc.QuotaFailure", "violations": [{"quotaId": quota_id}]}]
    return errors.APIError(code, {"error": {"code": code, "status": status, "message": "x", "details": details}})


def test_a_daily_quota_429_skips_the_model_for_the_day() -> None:
    exc = _error(429, "RESOURCE_EXHAUSTED", "GenerateRequestsPerDayPerProjectPerModel-FreeTier")
    assert type(map_error("gemini-3.8-flash", exc)) is QuotaExhausted


@pytest.mark.parametrize(
    ("code", "status", "quota_id"),
    [
        (503, "UNAVAILABLE", ""),  # "the model is experiencing high demand" (Galib's live check)
        (500, "INTERNAL", ""),
        (429, "RESOURCE_EXHAUSTED", "GenerateRequestsPerMinutePerProjectPerModel-FreeTier"),
    ],
)
def test_busy_errors_are_worth_one_retry(code: int, status: str, quota_id: str) -> None:
    assert type(map_error("gemini-3.8-flash", _error(code, status, quota_id))) is Overloaded


@pytest.mark.parametrize(
    ("code", "status"), [(400, "INVALID_ARGUMENT"), (403, "PERMISSION_DENIED"), (404, "NOT_FOUND")]
)
def test_other_errors_move_on_without_a_retry(code: int, status: str) -> None:
    assert type(map_error("gemini-3.8-flash", _error(code, status))) is AiUnavailable
