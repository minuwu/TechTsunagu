"""Support Log CSV (architecture §9.3, research.md §1): the shop's support events in a date range, as the support
record a 登録支援機関 keeps. The rows and file format are app.domain.support_log; this module only collects them."""

from datetime import datetime, time

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.domain.recurrence import TOKYO
from app.domain.support_log import SupportEvent, render_support_log
from app.repositories import life as repo
from app.schemas.senpai import SupportLogQuery
from app.services.auth import Principal

MEETUP_KINDS = {"coffee": "お茶・食事", "event": "地域の行事", "drill": "防災訓練", "other": "その他"}
PASSED = {True: "理解確認：合格", False: "理解確認：不合格", None: "理解確認：未回答"}


def export(db: Session, who: Principal, q: SupportLogQuery) -> bytes:
    if who.shop_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    shop_id = who.shop_id
    start, end = datetime.combine(q.from_, time.min, TOKYO), datetime.combine(q.to, time.max, TOKYO)
    events: list[SupportEvent] = []
    for a, worker, senpai in repo.forwarded_between(db, shop_id, start, end):
        handling = f"{senpai}：{a.senpai_reply_ja}" if a.senpai_reply_ja else f"{senpai}：未回答"
        when = a.forwarded_at or a.created_at
        events.append(SupportEvent("consultation", when, worker, a.text_ja or a.text_src, handling, "Senpai"))
    for m, worker, senpai in repo.meetups_between(db, shop_id, q.from_, q.to):
        content = f"{MEETUP_KINDS.get(m.kind, m.kind)}　{m.note}".strip()
        source = "Senpai" if m.logged_by == "senpai" else "Worker"
        events.append(SupportEvent("exchange", m.happened_on, worker, content, senpai, source))
    for r, worker, title in repo.notice_receipts_between(db, shop_id, start, end):
        events.append(SupportEvent("orientation", r.opened_at, worker, title.get("ja", ""), PASSED[r.passed], "Notice"))
    for o, machine, foreman, worker in repo.overrides_between(db, shop_id, start, end):
        events.append(
            SupportEvent("panel_override", o.starts_at, worker or "", f"{machine}：{o.reason}", foreman, "Panel")
        )
    return render_support_log(events, q.from_, q.to)
