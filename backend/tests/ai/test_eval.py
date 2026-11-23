"""The eval report (app.ai.eval) counts each model and pipeline and lists every failure. No model is called."""

from app.ai.eval import Row, report


def test_report_counts_per_model_and_lists_failures() -> None:
    rows = [
        Row("build_note", "n01", "m", valid=True, first_try=True, passed=True, seconds=3.0),
        Row("build_note", "n02", "m", valid=True, first_try=False, passed=False, seconds=5.0, notes=["lost: 寸法"]),
        Row("answer_ask", "a01", "m", notes=["HTTP 503"]),
    ]

    text = report(rows, ["m"], "2026-11-23 16:00 JST")

    assert "| m | build_note | 2/2 | 1/2 | 1/2 | 5.0 |" in text
    assert "| m | answer_ask | 0/1 | 0/1 | 0/1 | — |" in text
    assert "build_notice" not in text.split("## Failures")[0]  # a pipeline that did not run gets no row
    assert "- `m` · build_note · `n02`: lost: 寸法" in text
    assert "- `m` · answer_ask · `a01`: HTTP 503" in text
