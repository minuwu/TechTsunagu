"""Request body limit before any parsing (architecture §11): refuses on Content-Length and counts streamed bytes,
so an oversized upload never reaches the multipart parser or memory."""

from starlette.datastructures import Headers
from starlette.types import ASGIApp, Message, Receive, Scope, Send

from app.api.errors import error_response

MAX_BODY_BYTES = 10 * 1024 * 1024


class _TooLarge(Exception):
    pass


class BodyLimitMiddleware:
    def __init__(self, app: ASGIApp, limit: int = MAX_BODY_BYTES) -> None:
        self.app = app
        self.limit = limit

    async def _refuse(self, scope: Scope, receive: Receive, send: Send) -> None:
        response = error_response(413, "PAYLOAD_TOO_LARGE", f"Request body is over {self.limit // (1024 * 1024)} MB")
        await response(scope, receive, send)

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return
        declared = Headers(scope=scope).get("content-length")
        if declared is not None and declared.isdigit() and int(declared) > self.limit:
            await self._refuse(scope, receive, send)
            return

        received = 0
        started = False

        async def counted_receive() -> Message:
            nonlocal received
            message = await receive()
            if message["type"] == "http.request":
                received += len(message.get("body", b""))
                if received > self.limit:
                    raise _TooLarge
            return message

        async def tracked_send(message: Message) -> None:
            nonlocal started
            started = started or message["type"] == "http.response.start"
            await send(message)

        try:
            await self.app(scope, counted_receive, tracked_send)
        except _TooLarge:
            if not started:
                await self._refuse(scope, receive, send)
