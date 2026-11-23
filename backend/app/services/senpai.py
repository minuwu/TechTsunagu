"""Senpai pairs, the Senpai link, replies and Meet-ups (architecture §3, §9.3).

The Foreman pairs a Worker with a Senpai and sees the link token once; only its SHA-256 is stored. The Senpai acts
through `/senpai/{token}` with no account: a wrong, revoked or expired token is a 404."""

import uuid
from datetime import date, datetime, timedelta

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, build_page, encode_cursor
from app.domain.recurrence import TOKYO
from app.models.life import Ask, Meetup, SenpaiPair
from app.repositories import accounts
from app.repositories import life as repo
from app.schemas.senpai import (
    ForwardedAsk,
    MeetupCreate,
    MeetupOut,
    SenpaiPairCreate,
    SenpaiPairCreated,
    SenpaiPairOut,
    SenpaiReplyIn,
    SenpaiView,
    SenpaiWorker,
)
from app.services import notices
from app.services.ai_runner import AiRunner
from app.services.asks import translate, worker_of
from app.services.auth import Principal
from app.services.credentials import new_token, token_digest
from app.services.notes import ai_languages, shop_of

LINK_TTL = timedelta(days=180)  # architecture §3: until revoked or 180 days
EVENTS_AHEAD = timedelta(days=31)
SHOWN = 50  # forwarded Asks and Meet-ups on the Senpai page


def _shop(who: Principal) -> int:
    if who.shop_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _pair_fields(p: SenpaiPair) -> dict[str, object]:
    return {
        "id": str(p.id),
        "worker_id": str(p.worker_id),
        "senpai_name": p.senpai_name,
        "kind": p.kind,
        "created_at": p.created_at,
        "expires_at": p.expires_at,
    }


def _meetup_out(m: Meetup) -> MeetupOut:
    return MeetupOut.model_validate(
        {
            "id": str(m.id),
            "pair_id": str(m.pair_id),
            "kind": m.kind,
            "date": m.happened_on,
            "note": m.note,
            "logged_by": m.logged_by,
        }
    )


def _forwarded(a: Ask) -> ForwardedAsk:
    return ForwardedAsk(
        id=str(a.id),
        text=a.text_src,
        text_ja=a.text_ja,
        created_at=a.created_at,
        reply_ja=a.senpai_reply_ja,
    )


def list_pairs(db: Session, who: Principal, page: PageParams) -> Page[SenpaiPairOut]:
    rows = repo.pairs_page(db, _shop(who), page.after_id, page.limit)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[SenpaiPairOut.model_validate(_pair_fields(p)) for p in shown], next_cursor=cursor)


def create_pair(db: Session, who: Principal, body: SenpaiPairCreate, now: datetime) -> SenpaiPairCreated:
    shop_id = _shop(who)
    worker = repo.worker(db, shop_id, int(body.worker_id))
    if worker is None:
        raise _not_found()
    current = repo.active_pair(db, shop_id, worker.id, now)
    if current is not None:
        raise ApiError(409, "CONFLICT", "This Worker already has a Senpai; revoke the link first")
    stale = repo.unrevoked_pair(db, shop_id, worker.id)
    if stale is not None:
        stale.revoked_at = now  # expired but never revoked: it would block the one-active-pair index
        db.flush()
    token, digest = new_token()
    pair = SenpaiPair(
        shop_id=shop_id,
        worker_id=worker.id,
        senpai_name=body.senpai_name,
        kind=body.kind,
        token_hash=digest,
        created_by=who.user_id,
        created_at=now,
        expires_at=now + LINK_TTL,
    )
    db.add(pair)
    db.flush()
    accounts.audit(db, "senpai_paired", who.user_id, shop_id, subject_id=pair.id)
    db.commit()
    return SenpaiPairCreated.model_validate({**_pair_fields(pair), "token": token})


def revoke_pair(db: Session, who: Principal, pair_id: int, now: datetime) -> None:
    shop_id = _shop(who)
    pair = repo.pair(db, shop_id, pair_id, for_update=True)
    if pair is None:
        raise _not_found()
    if pair.revoked_at is None:
        pair.revoked_at = now
        accounts.audit(db, "senpai_revoked", who.user_id, shop_id, subject_id=pair.id)
    db.commit()


def _check_day(day: date, now: datetime) -> None:
    if day > now.astimezone(TOKYO).date():
        raise ApiError(
            422, "VALIDATION_ERROR", "A Meet-up is logged after it happened", {"fields": [{"loc": ["body", "date"]}]}
        )


def _log_meetup(
    db: Session, pair: SenpaiPair, body: MeetupCreate, by: str, now: datetime, key: uuid.UUID | None = None
) -> MeetupOut:
    if key is not None and (done := repo.meetup_by_key(db, pair.shop_id, key)) is not None:
        if done.pair_id != pair.id:
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another record")
        return _meetup_out(done)  # a retried offline create returns the first row
    _check_day(body.date, now)
    row = Meetup(
        shop_id=pair.shop_id,
        pair_id=pair.id,
        kind=body.kind,
        happened_on=body.date,
        note=body.note,
        logged_by=by,
        idempotency_key=key,
    )
    db.add(row)
    db.commit()
    return _meetup_out(row)


def worker_meetup(
    db: Session, who: Principal, body: MeetupCreate, now: datetime, key: uuid.UUID | None = None
) -> MeetupOut:
    shop_id, _, worker_id = worker_of(who)
    pair = repo.active_pair(db, shop_id, worker_id, now)
    if pair is None:
        raise ApiError(409, "NO_ACTIVE_PAIR", "You have no Senpai yet; ask your Foreman")
    return _log_meetup(db, pair, body, "worker", now, key)


def my_pair(db: Session, who: Principal, now: datetime) -> SenpaiPairOut | None:
    """The signed-in Worker's active Senpai pair, or null (XREQ-010 item 7). Never the link token."""
    shop_id, _, worker_id = worker_of(who)
    pair = repo.active_pair(db, shop_id, worker_id, now)
    return SenpaiPairOut.model_validate(_pair_fields(pair)) if pair else None


def _pair_of(db: Session, token: str, now: datetime) -> SenpaiPair:
    pair = repo.pair_by_token(db, token_digest(token), now)
    if pair is None:
        raise _not_found()
    return pair


def view(db: Session, token: str, now: datetime) -> SenpaiView:
    pair = _pair_of(db, token, now)
    worker = repo.worker(db, pair.shop_id, pair.worker_id)
    if worker is None or worker.district_id is None:
        raise _not_found()
    return SenpaiView.model_validate(
        {
            "senpai_name": pair.senpai_name,
            "worker": SenpaiWorker.model_validate({"display_name": worker.display_name, "language": worker.language}),
            "asks": [_forwarded(a) for a in repo.forwarded_asks(db, pair, SHOWN)],
            "events": notices.occurrences(db, worker.district_id, now, now + EVENTS_AHEAD),
            "meetups": [_meetup_out(m) for m in repo.meetups_of(db, pair, SHOWN)],
        }
    )


def reply(db: Session, token: str, ask_id: int, body: SenpaiReplyIn, ai: AiRunner, now: datetime) -> ForwardedAsk:
    """The Senpai answers in Japanese; the Worker gets it in their languages (Japanese only if the model is down)."""
    pair = _pair_of(db, token, now)
    a = repo.forwarded_ask(db, pair, ask_id)
    if a is None or a.forwarded_at is None:
        raise _not_found()
    if a.replied_at is not None:
        raise ApiError(409, "CONFLICT", "This question is already answered")
    shop_id = pair.shop_id
    languages = ai_languages(db, shop_id)
    db.commit()  # no lock is held during the model calls (§8.3)

    translated = {"ja": body.reply_ja}
    for lang in languages:
        text = translate(ai, body.reply_ja, "ja", lang, f"shop:{shop_id}", now)
        if text:
            translated[lang] = text

    a = repo.forwarded_ask(db, _pair_of(db, token, now), ask_id, for_update=True)
    if a is None:
        raise _not_found()
    if a.replied_at is not None:
        raise ApiError(409, "CONFLICT", "This question is already answered")
    a.senpai_reply_ja, a.senpai_reply, a.replied_at = body.reply_ja, translated, now
    db.commit()
    return _forwarded(a)


def senpai_meetup(db: Session, token: str, body: MeetupCreate, now: datetime) -> MeetupOut:
    return _log_meetup(db, _pair_of(db, token, now), body, "senpai", now)


def list_meetups(db: Session, who: Principal, page: PageParams) -> Page[MeetupOut]:
    """GET /meetups: a Worker's own Meet-ups, or every Meet-up of the Foreman's shop."""
    own = who.user_id if who.role == "worker" else None
    rows = repo.meetups_page(db, shop_of(who), own, page.after_id, page.limit)
    last = rows[page.limit - 1].id if len(rows) > page.limit else None
    return build_page([_meetup_out(m) for m in rows], page.limit, last)
