"""Every Life query (architecture §6.3). Notices, Events and photos take the caller's district id; receipts, Asks,
Senpai pairs and Meet-ups take the shop id (or the hashed Senpai token). Nothing here is unscoped."""

import uuid
from datetime import datetime

from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.life import Ask, Event, Meetup, Notice, SenpaiPair
from app.models.platform import Blob
from app.models.scopes import Poster, User
from app.models.work import Receipt


def poster_of(db: Session, district_id: int, user_id: int) -> Poster | None:
    stmt = select(Poster).join(User, (User.poster_id == Poster.id) & (User.district_id == Poster.district_id))
    return db.scalar(stmt.where(Poster.district_id == district_id, User.id == user_id))


def notice(db: Session, district_id: int, notice_id: int, *, for_update: bool = False) -> Notice | None:
    stmt = select(Notice).where(Notice.district_id == district_id, Notice.id == notice_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def notice_by_key(db: Session, district_id: int, key: uuid.UUID) -> Notice | None:
    return db.scalar(select(Notice).where(Notice.district_id == district_id, Notice.idempotency_key == key))


def notices_page(
    db: Session, district_id: int, poster_id: int | None, after_id: int | None, limit: int
) -> list[Notice]:
    """Newest first. `poster_id` set: that organisation's Notices (drafts too); None: published Notices only."""
    stmt = select(Notice).where(Notice.district_id == district_id)
    mine = Notice.poster_id == poster_id if poster_id is not None else Notice.status == "published"
    stmt = stmt.where(mine)
    if after_id is not None:
        stmt = stmt.where(Notice.id < after_id)
    return list(db.scalars(stmt.order_by(Notice.id.desc()).limit(limit + 1)))


def published_notices(db: Session, district_id: int, limit: int) -> list[Notice]:
    stmt = select(Notice).where(Notice.district_id == district_id, Notice.status == "published")
    return list(db.scalars(stmt.order_by(Notice.published_at.desc()).limit(limit)))


def events_of(db: Session, district_id: int, notice_ids: list[int]) -> dict[int, list[Event]]:
    out: dict[int, list[Event]] = {i: [] for i in notice_ids}
    if not notice_ids:
        return out
    stmt = select(Event).where(Event.district_id == district_id, Event.notice_id.in_(notice_ids)).order_by(Event.id)
    for e in db.scalars(stmt):
        out[e.notice_id].append(e)
    return out


def event(db: Session, district_id: int, notice_id: int, event_id: int) -> Event | None:
    stmt = select(Event).where(Event.district_id == district_id, Event.notice_id == notice_id, Event.id == event_id)
    return db.scalar(stmt)


def published_events(db: Session, district_id: int) -> list[Event]:
    """Confirmed Events of published Notices; expanded into dates by app.domain.recurrence."""
    stmt = (
        select(Event)
        .join(Notice, (Notice.id == Event.notice_id) & (Notice.district_id == Event.district_id))
        .where(Event.district_id == district_id, Event.confirmed, Notice.status == "published")
    )
    return list(db.scalars(stmt))


def blob(db: Session, district_id: int, blob_id: int) -> Blob | None:
    return db.scalar(select(Blob).where(Blob.district_id == district_id, Blob.id == blob_id))


def receipts_of(db: Session, worker_id: int, notice_ids: list[int]) -> dict[int, Receipt]:
    if not notice_ids:
        return {}
    stmt = select(Receipt).where(Receipt.worker_id == worker_id, Receipt.notice_id.in_(notice_ids))
    return {r.notice_id: r for r in db.scalars(stmt) if r.notice_id is not None}


def open_receipt(db: Session, shop_id: int, worker_id: int, notice_id: int, passed: bool | None = None) -> None:
    """Upsert: the first open is kept; a Check result (`passed`) overwrites the previous one."""
    stmt = insert(Receipt).values(shop_id=shop_id, worker_id=worker_id, notice_id=notice_id, passed=passed)
    keys, partial = ["worker_id", "notice_id"], Receipt.notice_id.is_not(None)  # uq_receipts_worker_notice
    if passed is None:
        db.execute(stmt.on_conflict_do_nothing(index_elements=keys, index_where=partial))
    else:
        db.execute(stmt.on_conflict_do_update(index_elements=keys, index_where=partial, set_={"passed": passed}))


def reach(db: Session, district_id: int, notice_id: int) -> tuple[int, int, int]:
    """(Workers of the district, how many opened, how many passed), across every shop of the district."""
    workers = select(User.id).where(User.district_id == district_id, User.role == "worker", User.active)
    total = db.scalar(select(func.count()).select_from(workers.subquery())) or 0
    stmt = select(func.count(), func.count().filter(Receipt.passed.is_(True))).where(
        Receipt.notice_id == notice_id, Receipt.worker_id.in_(workers)
    )
    opened, understood = db.execute(stmt).one()
    return total, opened, understood


def ask_by_key(db: Session, shop_id: int, worker_id: int, key: uuid.UUID) -> Ask | None:
    stmt = select(Ask).where(Ask.shop_id == shop_id, Ask.worker_id == worker_id, Ask.idempotency_key == key)
    return db.scalar(stmt)


def ask(db: Session, shop_id: int, worker_id: int, ask_id: int, *, for_update: bool = False) -> Ask | None:
    stmt = select(Ask).where(Ask.shop_id == shop_id, Ask.worker_id == worker_id, Ask.id == ask_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def asks_page(db: Session, shop_id: int, worker_id: int, after_id: int | None, limit: int) -> list[Ask]:
    stmt = select(Ask).where(Ask.shop_id == shop_id, Ask.worker_id == worker_id)
    if after_id is not None:
        stmt = stmt.where(Ask.id < after_id)
    return list(db.scalars(stmt.order_by(Ask.id.desc()).limit(limit + 1)))


def forwarded_ask(db: Session, pair: SenpaiPair, ask_id: int, *, for_update: bool = False) -> Ask | None:
    stmt = select(Ask).where(Ask.shop_id == pair.shop_id, Ask.pair_id == pair.id, Ask.id == ask_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def forwarded_asks(db: Session, pair: SenpaiPair, limit: int) -> list[Ask]:
    stmt = select(Ask).where(Ask.shop_id == pair.shop_id, Ask.pair_id == pair.id, Ask.forwarded_at.is_not(None))
    return list(db.scalars(stmt.order_by(Ask.forwarded_at.desc()).limit(limit)))


def active_pair(db: Session, shop_id: int, worker_id: int, now: datetime) -> SenpaiPair | None:
    stmt = select(SenpaiPair).where(
        SenpaiPair.shop_id == shop_id,
        SenpaiPair.worker_id == worker_id,
        SenpaiPair.revoked_at.is_(None),
        SenpaiPair.expires_at > now,
    )
    return db.scalar(stmt)


def pair_by_token(db: Session, token_hash: bytes, now: datetime) -> SenpaiPair | None:
    stmt = select(SenpaiPair).where(
        SenpaiPair.token_hash == token_hash, SenpaiPair.revoked_at.is_(None), SenpaiPair.expires_at > now
    )
    return db.scalar(stmt)


def pair(db: Session, shop_id: int, pair_id: int, *, for_update: bool = False) -> SenpaiPair | None:
    stmt = select(SenpaiPair).where(SenpaiPair.shop_id == shop_id, SenpaiPair.id == pair_id)
    return db.scalar(stmt.with_for_update() if for_update else stmt)


def pairs_page(db: Session, shop_id: int, after_id: int | None, limit: int) -> list[SenpaiPair]:
    stmt = select(SenpaiPair).where(SenpaiPair.shop_id == shop_id, SenpaiPair.revoked_at.is_(None))
    if after_id is not None:
        stmt = stmt.where(SenpaiPair.id > after_id)
    return list(db.scalars(stmt.order_by(SenpaiPair.id).limit(limit + 1)))


def worker(db: Session, shop_id: int, worker_id: int) -> User | None:
    stmt = select(User).where(User.shop_id == shop_id, User.id == worker_id, User.role == "worker", User.active)
    return db.scalar(stmt)


def meetups_of(db: Session, pair: SenpaiPair, limit: int) -> list[Meetup]:
    stmt = select(Meetup).where(Meetup.shop_id == pair.shop_id, Meetup.pair_id == pair.id)
    return list(db.scalars(stmt.order_by(Meetup.happened_on.desc(), Meetup.id.desc()).limit(limit)))
