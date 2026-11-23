"""Personal queries (XREQ-017, ADR 0014). Each takes the signed-in user's id; nothing here is shop-scoped or open to
another user, and no shop route imports this module."""

import uuid
from datetime import datetime

from sqlalchemy import and_, delete, func, or_, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.models.personal import Block, Contact, ContactMessage, QrToken
from app.models.scopes import User


def qr_of(db: Session, user_id: int) -> QrToken | None:
    return db.get(QrToken, user_id)


def set_qr(db: Session, user_id: int, token_hash: bytes, now: datetime) -> None:
    stmt = insert(QrToken).values(user_id=user_id, token_hash=token_hash, created_at=now)
    db.execute(
        stmt.on_conflict_do_update(index_elements=["user_id"], set_={"token_hash": token_hash, "created_at": now})
    )


def user_by_qr(db: Session, token_hash: bytes) -> User | None:
    stmt = select(User).join(QrToken, QrToken.user_id == User.id).where(QrToken.token_hash == token_hash, User.active)
    return db.scalar(stmt)


def user(db: Session, user_id: int) -> User | None:
    return db.scalar(select(User).where(User.id == user_id, User.active))


def contact(db: Session, owner_id: int, contact_id: int) -> Contact | None:
    return db.scalar(select(Contact).where(Contact.owner_id == owner_id, Contact.id == contact_id))


def contact_with(db: Session, owner_id: int, peer_id: int) -> Contact | None:
    return db.scalar(select(Contact).where(Contact.owner_id == owner_id, Contact.peer_id == peer_id))


def connect(db: Session, a: int, b: int) -> None:
    """Both sides of a connection; an existing side is kept as it is."""
    for owner, peer in ((a, b), (b, a)):
        db.execute(insert(Contact).values(owner_id=owner, peer_id=peer).on_conflict_do_nothing())


def contacts_page(
    db: Session, owner_id: int, after_id: int | None, limit: int
) -> list[tuple[Contact, User, datetime | None, int]]:
    """(contact, peer, newest visible message, unread from the peer), hidden and blocked contacts left out."""
    visible = or_(
        ContactMessage.sender_id == owner_id,
        and_(ContactMessage.sender_id == Contact.peer_id, ContactMessage.delivered),
    )
    pair = or_(
        and_(ContactMessage.sender_id == owner_id, ContactMessage.recipient_id == Contact.peer_id),
        and_(ContactMessage.sender_id == Contact.peer_id, ContactMessage.recipient_id == owner_id),
    )
    newest = select(func.max(ContactMessage.created_at)).where(pair, visible).correlate(Contact).scalar_subquery()
    unread = (
        select(func.count())
        .where(
            ContactMessage.sender_id == Contact.peer_id,
            ContactMessage.recipient_id == owner_id,
            ContactMessage.delivered,
            or_(Contact.last_read_at.is_(None), ContactMessage.created_at > Contact.last_read_at),
        )
        .correlate(Contact)
        .scalar_subquery()
    )
    blocked = select(Block.blocked_id).where(Block.blocker_id == owner_id)
    stmt = (
        select(Contact, User, newest, unread)
        .join(User, User.id == Contact.peer_id)
        .where(Contact.owner_id == owner_id, Contact.hidden.is_(False), Contact.peer_id.not_in(blocked), User.active)
    )
    if after_id is not None:
        stmt = stmt.where(Contact.id > after_id)
    return [(c, u, n, k) for c, u, n, k in db.execute(stmt.order_by(Contact.id).limit(limit + 1))]


def is_blocked(db: Session, blocker_id: int, blocked_id: int) -> bool:
    stmt = select(Block).where(Block.blocker_id == blocker_id, Block.blocked_id == blocked_id)
    return db.scalar(stmt) is not None


def block(db: Session, blocker_id: int, blocked_id: int) -> None:
    db.execute(insert(Block).values(blocker_id=blocker_id, blocked_id=blocked_id).on_conflict_do_nothing())


def unblock(db: Session, blocker_id: int, blocked_id: int) -> None:
    db.execute(delete(Block).where(Block.blocker_id == blocker_id, Block.blocked_id == blocked_id))


def message_by_key(db: Session, sender_id: int, key: uuid.UUID) -> ContactMessage | None:
    stmt = select(ContactMessage).where(ContactMessage.sender_id == sender_id, ContactMessage.idempotency_key == key)
    return db.scalar(stmt)


def messages(db: Session, me: int, peer: int, after_id: int | None, limit: int) -> list[ContactMessage]:
    """Oldest first after the cursor: mine to the peer, and the peer's that were delivered to me."""
    mine = and_(ContactMessage.sender_id == me, ContactMessage.recipient_id == peer)
    theirs = and_(ContactMessage.sender_id == peer, ContactMessage.recipient_id == me, ContactMessage.delivered)
    stmt = select(ContactMessage).where(or_(mine, theirs))
    if after_id is not None:
        stmt = stmt.where(ContactMessage.id > after_id)
    return list(db.scalars(stmt.order_by(ContactMessage.id).limit(limit + 1)))
