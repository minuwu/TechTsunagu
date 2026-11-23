"""My QR, Contacts, blocks and Contact Threads (XREQ-017 items 1-2, ADR 0014).

Personal data: the scope is the signed-in user, across shops; no Foreman, Poster or Support Log route reads it. A block
is never revealed: the blocked side's sends still answer 201 and are simply not delivered."""

import uuid
from datetime import datetime, timedelta

from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.models.personal import Contact, ContactMessage
from app.repositories import accounts
from app.repositories import contacts as repo
from app.schemas.contacts import (
    ContactCreate,
    ContactMessageIn,
    ContactMessageOut,
    ContactMessagesPage,
    ContactOut,
    ContactPreview,
    ContactUpdate,
    QrOut,
)
from app.services.ai_runner import AiRunner
from app.services.asks import translate
from app.services.auth import Principal
from app.services.credentials import new_token, token_digest

RESOLVES_PER_MINUTE = 20  # a scanned QR is resolved once; this stops guessing tokens
SENDS_PER_MINUTE = 30
MESSAGES_PER_PAGE = 100


def _me(who: Principal) -> int:
    if who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.user_id


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _limit(db: Session, key: str, per_minute: int, now: datetime) -> None:
    if accounts.count_attempt(db, key, now, timedelta(minutes=1)) > per_minute:
        db.commit()
        raise ApiError(429, "RATE_LIMITED", "Too many requests; try again in a minute", headers={"Retry-After": "60"})


def my_qr(db: Session, who: Principal, rotate: bool, now: datetime) -> QrOut:
    """Make the QR token on first use or when asked to rotate; it is shown only then (only its SHA-256 is kept)."""
    me = _me(who)
    current = repo.qr_of(db, me)
    if current is not None and not rotate:
        return QrOut(token=None, created_at=current.created_at)
    token, digest = new_token()
    repo.set_qr(db, me, digest, now)
    db.commit()
    return QrOut(token=token, created_at=now)


def resolve(db: Session, who: Principal, token: str, now: datetime) -> ContactPreview:
    me = _me(who)
    _limit(db, f"qr-resolve:{me}", RESOLVES_PER_MINUTE, now)
    owner = repo.user_by_qr(db, token_digest(token))
    if owner is None:
        raise _not_found()
    return ContactPreview.model_validate({"display_name": owner.display_name, "language": owner.language})


def _contact_out(c: Contact, peer_name: str, peer_lang: str, last: datetime | None, unread: int) -> ContactOut:
    return ContactOut.model_validate(
        {
            "id": str(c.id),
            "display_name": peer_name,
            "language": peer_lang,
            "muted": c.muted,
            "hidden": c.hidden,
            "last_message_at": last,
            "unread": unread,
            "created_at": c.created_at,
        }
    )


def add(db: Session, who: Principal, body: ContactCreate, now: datetime) -> ContactOut:
    me = _me(who)
    _limit(db, f"qr-resolve:{me}", RESOLVES_PER_MINUTE, now)
    peer = repo.user_by_qr(db, token_digest(body.token))
    if peer is None:
        raise _not_found()
    if peer.id == me:
        raise ApiError(403, "FORBIDDEN", "That is your own QR code")
    if repo.contact_with(db, me, peer.id) is not None:
        raise ApiError(409, "CONFLICT", "Already a Contact")
    try:
        repo.connect(db, me, peer.id)
        db.commit()
    except IntegrityError as exc:  # the same scan sent twice at once
        db.rollback()
        raise ApiError(409, "CONFLICT", "Already a Contact") from exc
    c = repo.contact_with(db, me, peer.id)
    if c is None:
        raise _not_found()
    return _contact_out(c, peer.display_name, peer.language, None, 0)


def list_contacts(db: Session, who: Principal, page: PageParams) -> Page[ContactOut]:
    rows = repo.contacts_page(db, _me(who), page.after_id, page.limit)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1][0].id) if len(rows) > page.limit else None
    items = [_contact_out(c, u.display_name, u.language, last, unread) for c, u, last, unread in shown]
    return Page(items=items, next_cursor=cursor)


def _own_contact(db: Session, me: int, contact_id: int) -> Contact:
    c = repo.contact(db, me, contact_id)
    if c is None:
        raise _not_found()
    return c


def update(db: Session, who: Principal, contact_id: int, body: ContactUpdate) -> ContactOut:
    me = _me(who)
    c = _own_contact(db, me, contact_id)
    if body.muted is not None:
        c.muted = body.muted
    if body.hidden is not None:
        c.hidden = body.hidden
    db.commit()
    peer = repo.user(db, c.peer_id)
    if peer is None:
        raise _not_found()
    return _contact_out(c, peer.display_name, peer.language, None, 0)


def block(db: Session, who: Principal, contact_id: int) -> None:
    me = _me(who)
    repo.block(db, me, _own_contact(db, me, contact_id).peer_id)
    db.commit()


def unblock(db: Session, who: Principal, contact_id: int) -> None:
    me = _me(who)
    repo.unblock(db, me, _own_contact(db, me, contact_id).peer_id)
    db.commit()


def _message_out(m: ContactMessage, me: int, my_lang: str) -> ContactMessageOut:
    from_me = m.sender_id == me
    text = m.text_src if from_me else m.translations.get(my_lang, m.text_src)
    return ContactMessageOut.model_validate(
        {
            "id": str(m.id),
            "from_me": from_me,
            "text_src": m.text_src,
            "src_lang": m.src_lang,
            "text": text,
            "created_at": m.created_at,
        }
    )


def _thread_peer(db: Session, me: int, contact_id: int) -> int:
    """The peer of a Contact the caller can read: not removed, not blocked by the caller."""
    c = _own_contact(db, me, contact_id)
    if repo.is_blocked(db, me, c.peer_id):
        raise _not_found()
    return c.peer_id


def read(db: Session, who: Principal, contact_id: int, after_id: int | None, now: datetime) -> ContactMessagesPage:
    """Messages after the cursor; reading moves the caller's unread marker."""
    me = _me(who)
    peer = _thread_peer(db, me, contact_id)
    me_user = repo.user(db, me)
    rows = repo.messages(db, me, peer, after_id, MESSAGES_PER_PAGE)
    shown = rows[:MESSAGES_PER_PAGE]
    items = [_message_out(m, me, me_user.language if me_user else "en") for m in shown]
    if any(m.sender_id == peer for m in shown):
        _own_contact(db, me, contact_id).last_read_at = now
    db.commit()
    cursor = encode_cursor(shown[-1].id) if len(rows) > MESSAGES_PER_PAGE else None
    return ContactMessagesPage(items=items, next_cursor=cursor)


def delete_message(db: Session, who: Principal, contact_id: int, message_id: int) -> None:
    """ADR 0014. Blocking someone does not stop you deleting what is between you."""
    me = _me(who)
    if not repo.delete_messages(db, me, _own_contact(db, me, contact_id).peer_id, message_id):
        raise _not_found()
    db.commit()


def delete_thread(db: Session, who: Principal, contact_id: int) -> None:
    me = _me(who)
    repo.delete_messages(db, me, _own_contact(db, me, contact_id).peer_id)
    db.commit()


def send(
    db: Session,
    who: Principal,
    contact_id: int,
    body: ContactMessageIn,
    key: uuid.UUID | None,
    ai: AiRunner,
    now: datetime,
) -> ContactMessageOut:
    """Stored with the recipient's translation. A blocked sender still gets 201; the message is not delivered."""
    me = _me(who)
    if key is not None and (done := repo.message_by_key(db, me, key)) is not None:
        return _message_out(done, me, body.lang)
    peer = _thread_peer(db, me, contact_id)
    _limit(db, f"contact-send:{me}", SENDS_PER_MINUTE, now)
    recipient = repo.user(db, peer)
    if recipient is None:
        raise _not_found()
    delivered, target = not repo.is_blocked(db, peer, me), recipient.language
    db.commit()  # no transaction is held during the model call (§8.3)

    translations: dict[str, str] = {}
    if delivered and target != body.lang:
        text = translate(ai, body.text, body.lang, target, None, f"user:{me}", now)
        if text:  # at the AI cap the reader sees the original, never an error
            translations[target] = text
    row = ContactMessage(
        sender_id=me,
        recipient_id=peer,
        text_src=body.text,
        src_lang=body.lang,
        translations=translations,
        delivered=delivered,
        idempotency_key=key,
    )
    db.add(row)
    db.commit()
    return _message_out(row, me, body.lang)
