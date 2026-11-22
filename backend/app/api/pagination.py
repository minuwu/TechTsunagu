"""Cursor pagination (ADR 0011): `?limit=&cursor=` → `{items, nextCursor}`. The cursor is an opaque base64 of the
last row's id; it carries no secret, and an id from another scope is filtered out by the scoped query anyway."""

import base64
import binascii
from typing import Annotated

from fastapi import Query

from app.api.errors import ApiError
from app.schemas.common import CamelModel

DEFAULT_LIMIT = 50
MAX_LIMIT = 200


class Page[T](CamelModel):
    items: list[T]
    next_cursor: str | None


class PageParams(CamelModel):
    limit: int
    after_id: int | None


def encode_cursor(last_id: int) -> str:
    return base64.urlsafe_b64encode(str(last_id).encode()).decode().rstrip("=")


def _bad_cursor() -> ApiError:
    return ApiError(422, "VALIDATION_ERROR", "Invalid cursor", {"fields": [{"loc": ["query", "cursor"]}]})


def _decode_cursor(cursor: str) -> int:
    try:
        value = int(base64.urlsafe_b64decode(cursor + "=" * (-len(cursor) % 4)).decode())
    except (binascii.Error, UnicodeDecodeError, ValueError) as exc:
        raise _bad_cursor() from exc
    if value < 1:
        raise _bad_cursor()
    return value


def page_params(
    limit: Annotated[int, Query(ge=1, le=MAX_LIMIT)] = DEFAULT_LIMIT,
    cursor: Annotated[str | None, Query(max_length=40)] = None,
) -> PageParams:
    return PageParams(limit=limit, after_id=_decode_cursor(cursor) if cursor else None)


def build_page[T](items: list[T], limit: int, last_id: int | None) -> Page[T]:
    """`items` was fetched with limit + 1, so one extra row means there is a next page."""
    has_more = len(items) > limit
    return Page(items=items[:limit], next_cursor=encode_cursor(last_id) if has_more and last_id is not None else None)
