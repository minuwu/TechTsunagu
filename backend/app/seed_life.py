"""Life demo seed (WS10/WS11): loads seed_data/life.json on top of `seed_work`, whose refs already hold the district
and the Worker keys. Called from `app.seed.main` in the same transaction."""

from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

from sqlalchemy.orm import Session

from app.models.life import Event, Meetup, Notice, SenpaiPair
from app.models.scopes import Poster, User
from app.models.work import Receipt
from app.seed import Refs, _key, _load  # pyright: ignore[reportPrivateUsage]  # one seed, split by flow
from app.services.credentials import hash_pin, token_digest

SENPAI_LINK_TTL = timedelta(days=180)  # architecture §3: Senpai links last until revoked or 180 days
PAIRED_BY = "f-yamaguchi"  # the Foreman who pairs Workers in the demo shop (work.json)


def seed_life(db: Session, pepper: str, refs: Refs, now: datetime) -> Refs:
    life = _load("life.json")
    district_id = refs[life["district"]["key"]]

    for p in life["posters"]:
        poster = Poster(district_id=district_id, name=p["name"], kind=p["kind"])
        db.add(poster)
        db.flush()
        refs[p["key"]] = poster.id

    author: dict[str, int] = {}  # poster key → the account that wrote its Notices
    for u in life["posterUsers"]:
        user = User(
            role="poster",
            display_name=u["displayName"],
            language=u["language"],
            district_id=district_id,
            poster_id=refs[u["posterKey"]],
            pin_hash=hash_pin(pepper, u["demoPin"]),
            pin_must_change=False,  # demo accounts; real accounts always start with a PIN change
        )
        db.add(user)
        db.flush()
        refs[u["key"]] = user.id
        author.setdefault(u["posterKey"], user.id)

    for n in life["notices"]:
        created_at = now - timedelta(days=n["createdDaysAgo"])
        notice = Notice(
            district_id=district_id,
            poster_id=refs[n["posterKey"]],
            created_by=author[n["posterKey"]],
            category=n["category"],
            source_kind=n["sourceKind"],
            source_ja=n["sourceJa"],
            title=n["title"],
            steps=n["steps"],
            question=n["question"],
            status=n["status"],
            idempotency_key=_key(n["key"]),
            created_at=created_at,
            published_at=created_at if n["status"] == "published" else None,
        )
        db.add(notice)
        db.flush()
        refs[n["key"]] = notice.id
        for e in n["events"]:
            event = Event(
                district_id=district_id,
                notice_id=notice.id,
                title=e["title"],
                starts_at=datetime.fromisoformat(e["startsAt"]),
                rrule=e["rrule"],
                confirmed=e["confirmed"],
            )
            db.add(event)
            db.flush()
            refs[e["key"]] = event.id

    def shop_of(worker_key: str) -> int:
        worker = db.get(User, refs[worker_key])
        if worker is None or worker.shop_id is None:
            raise ValueError(f"life.json points to {worker_key}, which seed_work did not create")
        return worker.shop_id

    for r in life["receipts"]:
        db.add(
            Receipt(
                shop_id=shop_of(r["workerKey"]),
                worker_id=refs[r["workerKey"]],
                notice_id=refs[r["noticeKey"]],
                opened_at=now - timedelta(days=r["openedDaysAgo"]),
                passed=r["passed"],
            )
        )

    pair_shops: dict[str, int] = {}
    for s in life["senpaiPairs"]:
        pair = SenpaiPair(
            shop_id=shop_of(s["workerKey"]),
            worker_id=refs[s["workerKey"]],
            senpai_name=s["senpaiName"],
            kind=s["kind"],
            token_hash=token_digest(s["demoToken"]),
            created_by=refs[PAIRED_BY],
            created_at=now,
            expires_at=now + SENPAI_LINK_TTL,
        )
        db.add(pair)
        db.flush()
        refs[s["key"]], pair_shops[s["key"]] = pair.id, pair.shop_id

    today = now.astimezone(ZoneInfo("Asia/Tokyo")).date()
    for m in life["meetups"]:
        db.add(
            Meetup(
                shop_id=pair_shops[m["pairKey"]],
                pair_id=refs[m["pairKey"]],
                kind=m["kind"],
                happened_on=today - timedelta(days=m["daysAgo"]),
                note=m["note"],
                logged_by=m["loggedBy"],
            )
        )
    db.flush()
    return refs
