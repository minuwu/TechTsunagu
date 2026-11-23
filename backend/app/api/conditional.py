"""Conditional GET for routes the apps poll (XREQ-011 item 2, MOBILE_API_CONTRACT B-11): an `ETag` on every answer,
and `304 Not Modified` with no body when the client's `If-None-Match` still matches, so a poll costs almost nothing."""

import hashlib

from fastapi import Request, Response
from pydantic import BaseModel


def etag(body: str) -> str:
    """A strong validator for the exact JSON sent."""
    return '"' + hashlib.sha256(body.encode()).hexdigest()[:32] + '"'


def conditional_json(request: Request, envelope: BaseModel) -> Response:
    """The envelope as JSON with its ETag, or a bodyless 304 when If-None-Match matches (RFC 9110 §13.1.2)."""
    body = envelope.model_dump_json(by_alias=True)
    tag = etag(body)
    sent = request.headers.get("if-none-match", "")
    if sent.strip() == "*" or tag in (t.strip() for t in sent.split(",")):
        return Response(status_code=304, headers={"ETag": tag})
    return Response(body, media_type="application/json", headers={"ETag": tag, "Cache-Control": "no-cache"})
