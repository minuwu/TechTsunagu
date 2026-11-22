import csv
import io
from datetime import UTC, date, datetime

import pytest

from app.domain.support_log import SupportEvent, render_support_log

START, END = date(2026, 11, 1), date(2026, 11, 30)


def rows(data: bytes) -> list[list[str]]:
    return list(csv.reader(io.StringIO(data.decode("utf-8-sig"), newline="")))


def event(**changes: object) -> SupportEvent:
    base: dict[str, object] = {
        "item": "consultation",
        "when": datetime(2026, 11, 22, 10, 5, tzinfo=UTC),
        "worker": "Rahim Uddin",
        "content": "扇風機の捨て方",
        "handling": "田中 健が回答",
        "source": "Ask 31",
    }
    return SupportEvent(**{**base, **changes})  # type: ignore[arg-type]


def test_file_opens_in_excel_as_utf8_with_crlf_lines() -> None:
    data = render_support_log([event()], START, END)

    assert data.startswith(b"\xef\xbb\xbf")
    assert b"\r\n" in data


def test_header_says_what_the_export_does_not_cover() -> None:
    table = rows(render_support_log([], START, END))
    preamble = " ".join(cell for row in table[:3] for cell in row)

    assert "2026-11-01" in preamble and "2026-11-30" in preamble
    assert "定期的な面談" in preamble and "参考様式第3-6号" in preamble
    assert table[-1] == ["支援項目", "日時", "氏名", "内容", "対応・相手", "記録元"]


def test_rows_are_grouped_by_support_item_then_ordered_by_time() -> None:
    data = render_support_log(
        [
            event(item="panel_override", source="Override 4"),
            event(item="exchange", when=date(2026, 11, 20), source="Meet-up 2"),
            event(when=datetime(2026, 11, 23, 1, 0, tzinfo=UTC), source="Ask 40"),
            event(item="orientation", source="Notice 2"),
            event(source="Ask 31"),
        ],
        START,
        END,
    )

    sources = [row[5] for row in rows(data)[4:]]
    assert sources == ["Ask 31", "Ask 40", "Meet-up 2", "Notice 2", "Override 4"]


def test_times_are_shown_in_japan_time_and_dates_as_dates() -> None:
    data = render_support_log([event(), event(item="exchange", when=date(2026, 11, 20))], START, END)

    assert [row[1] for row in rows(data)[4:]] == ["2026-11-22 19:05", "2026-11-20"]


def test_commas_quotes_and_line_breaks_survive() -> None:
    text = 'He said "ok", then\nleft'

    assert rows(render_support_log([event(content=text)], START, END))[4][3] == text


@pytest.mark.parametrize("text", ['=HYPERLINK("http://x")', "+1+1", "-2+3", "@SUM(A1)", "\tcmd"])
def test_cells_that_excel_would_run_as_formulas_are_escaped(text: str) -> None:
    row = rows(render_support_log([event(content=text, worker=text)], START, END))[4]

    assert row[3] == "'" + text
    assert row[2] == "'" + text
