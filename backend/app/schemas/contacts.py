"""My QR, Contacts, blocks and Contact Threads (XREQ-017 items 1-2, ADR 0014). Personal: scoped by user, not shop."""

from typing import Annotated

from pydantic import AwareDatetime, StringConstraints

from app.schemas.common import CamelModel, IdIn, Lang

QrToken = Annotated[str, StringConstraints(min_length=16, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")]


class QrIn(CamelModel):
    rotate: bool = False


class QrOut(CamelModel):
    token: str | None
    """The token only when it was just created or rotated (stored as SHA-256 only); otherwise null: keep it."""
    created_at: AwareDatetime


class ContactPreview(CamelModel):
    """GET /contacts/resolve: who a scanned QR belongs to. Never an id or a shop."""

    display_name: str
    language: Lang


class ContactCreate(CamelModel):
    token: QrToken


class ContactOut(CamelModel):
    id: str
    display_name: str
    language: Lang
    muted: bool
    hidden: bool
    last_message_at: AwareDatetime | None
    unread: int
    created_at: AwareDatetime


class ContactUpdate(CamelModel):
    muted: bool | None = None
    hidden: bool | None = None


class BlockIn(CamelModel):
    contact_id: IdIn


class ContactMessageIn(CamelModel):
    text: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=2000)]
    lang: Lang


class ContactMessageOut(CamelModel):
    id: str
    from_me: bool
    text_src: str
    src_lang: Lang
    text: str
    """In the reader's language; the original when it could not be translated."""
    created_at: AwareDatetime


class ContactMessagesPage(CamelModel):
    items: list[ContactMessageOut]
    next_cursor: str | None
