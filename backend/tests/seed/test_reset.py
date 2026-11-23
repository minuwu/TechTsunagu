"""The demo reset (app.seed --reset, run at every container start) keeps the warmed AI answers and the day's calls."""

from app.seed import reset_tables


def test_reset_keeps_the_ai_cache_and_usage_but_clears_demo_data() -> None:
    tables = reset_tables()

    assert "ai_cache" not in tables
    assert "ai_usage" not in tables
    assert {"users", "note_versions", "messages", "briefings"} <= set(tables)
