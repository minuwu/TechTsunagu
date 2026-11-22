"""Support Log CSV (architecture §9.3; research.md §1): the support records kept on site for 特定技能1号
Workers, grouped by the official support item each row evidences. Pure."""

import csv
import io
from collections.abc import Iterable
from dataclasses import dataclass
from datetime import date, datetime
from typing import Literal

from app.domain.recurrence import TOKYO

SupportItem = Literal["consultation", "exchange", "orientation", "panel_override"]

# Item numbers are the 義務的支援 items; the Panel Override is the shop's own safety record. Order = file order.
ITEM_LABELS: dict[SupportItem, str] = {
    "consultation": "7 相談・苦情への対応",
    "exchange": "8 日本人との交流促進",
    "orientation": "4 生活オリエンテーション（お知らせの確認）",
    "panel_override": "安全記録（パネル解除）",
}
HEADER = ["支援項目", "日時", "氏名", "内容", "対応・相手", "記録元"]
NOT_COVERED = "この記録に含まれないもの：定期的な面談（項目10、3か月に1回以上）、定期届出（参考様式第3-6号）。"
_FORMULA_START = ("=", "+", "-", "@", "\t", "\r")


@dataclass(frozen=True)
class SupportEvent:
    item: SupportItem
    when: date | datetime
    worker: str
    content: str
    handling: str
    source: str


def render_support_log(events: Iterable[SupportEvent], start: date, end: date) -> bytes:
    """CSV with a UTF-8 BOM, so Excel shows the Japanese, and one row per event."""
    order = list(ITEM_LABELS)
    out = io.StringIO()
    writer = csv.writer(out)  # CRLF line ends; quotes commas, quotes and line breaks
    writer.writerow([f"支援記録 {start.isoformat()}〜{end.isoformat()}"])
    writer.writerow([NOT_COVERED])
    writer.writerow([])
    writer.writerow(HEADER)
    for e in sorted(events, key=lambda e: (order.index(e.item), _when(e.when))):
        writer.writerow(
            [ITEM_LABELS[e.item], _when(e.when), *(_cell(text) for text in (e.worker, e.content, e.handling, e.source))]
        )
    return ("﻿" + out.getvalue()).encode("utf-8")


def _when(value: date | datetime) -> str:
    if isinstance(value, datetime):  # check first: a datetime is also a date
        return value.astimezone(TOKYO).strftime("%Y-%m-%d %H:%M")
    return value.isoformat()


def _cell(text: str) -> str:
    # Worker-typed text must not run as a formula when staff open the file in Excel (CSV injection).
    return "'" + text if text.startswith(_FORMULA_START) else text
