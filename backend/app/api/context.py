"""Per-request id, JSON logs and security headers (architecture §12, §13), as plain ASGI middleware."""

import json
import logging
import re
import sys
import uuid
from contextvars import ContextVar
from time import perf_counter
from typing import Any

from starlette.datastructures import Headers, MutableHeaders
from starlette.types import ASGIApp, Message, Receive, Scope, Send

request_id_var: ContextVar[str] = ContextVar("request_id", default="-")
_SAFE_ID = re.compile(r"^[A-Za-z0-9._-]{1,64}$")
_EXTRA_FIELDS = ("request_id", "method", "route", "status", "latency_ms")
_log = logging.getLogger("wakatta.request")


class JsonFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        line: dict[str, Any] = {
            "time": self.formatTime(record, "%Y-%m-%dT%H:%M:%S%z"),
            "level": record.levelname,
            "logger": record.name,
            "msg": record.getMessage(),
        }
        line.update({k: getattr(record, k) for k in _EXTRA_FIELDS if hasattr(record, k)})
        if record.exc_info:
            line["exc"] = self.formatException(record.exc_info)
        return json.dumps(line, ensure_ascii=False)


def configure_logging(level: str) -> None:
    root = logging.getLogger()
    if not any(isinstance(h.formatter, JsonFormatter) for h in root.handlers):
        handler = logging.StreamHandler(sys.stdout)
        handler.setFormatter(JsonFormatter())
        root.addHandler(handler)
    root.setLevel(level)


def _request_id(header: str | None) -> str:
    # A caller-supplied id is kept only if it is short and safe to put in logs and headers.
    return header if header and _SAFE_ID.match(header) else uuid.uuid4().hex


class RequestContextMiddleware:
    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return
        rid = _request_id(Headers(scope=scope).get("x-request-id"))
        # Not reset on exit: the 500 handler runs after this middleware unwinds and still needs the id.
        request_id_var.set(rid)
        status = 500
        start = perf_counter()

        async def send_with_headers(message: Message) -> None:
            nonlocal status
            if message["type"] == "http.response.start":
                status = message["status"]
                headers = MutableHeaders(scope=message)
                headers["X-Request-ID"] = rid
                headers["X-Content-Type-Options"] = "nosniff"
                headers["Referrer-Policy"] = "no-referrer"
            await send(message)

        try:
            await self.app(scope, receive, send_with_headers)
        finally:
            route = getattr(scope.get("route"), "path", "unmatched")
            _log.info(
                "request",
                extra={
                    "request_id": rid,
                    "method": scope["method"],
                    "route": route,
                    "status": status,
                    "latency_ms": round((perf_counter() - start) * 1000, 1),
                },
            )
