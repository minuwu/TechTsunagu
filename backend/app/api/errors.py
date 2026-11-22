"""One error format for every failure (architecture §9.2); internals never reach the client."""

import logging
from collections.abc import Mapping
from http import HTTPStatus

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.api.context import request_id_var
from app.api.envelope import ErrorBody, ErrorEnvelope

_log = logging.getLogger("wakatta.errors")

_CODES: dict[int, str] = {
    400: "BAD_REQUEST",
    401: "UNAUTHORIZED",
    403: "FORBIDDEN",
    404: "NOT_FOUND",
    405: "METHOD_NOT_ALLOWED",
    409: "CONFLICT",
    413: "PAYLOAD_TOO_LARGE",
    415: "UNSUPPORTED_MEDIA_TYPE",
    422: "VALIDATION_ERROR",
    423: "PIN_LOCKED",
    429: "RATE_LIMITED",
    503: "BUSY",
}


class ApiError(Exception):
    """Raised by services for an expected failure the client should see."""

    def __init__(
        self,
        status: int,
        code: str,
        message: str,
        details: Mapping[str, object] | None = None,
        headers: Mapping[str, str] | None = None,
    ) -> None:
        super().__init__(message)
        self.status = status
        self.code = code
        self.message = message
        self.details = dict(details or {})
        self.headers = dict(headers or {})


def error_response(
    status: int,
    code: str,
    message: str,
    details: Mapping[str, object] | None = None,
    headers: Mapping[str, str] | None = None,
) -> JSONResponse:
    body = ErrorEnvelope(error=ErrorBody(code=code, message=message, details=dict(details or {})))
    return JSONResponse(body.model_dump(), status_code=status, headers=dict(headers or {}))


def install_error_handlers(app: FastAPI) -> None:
    @app.exception_handler(ApiError)
    async def _api_error(_: Request, exc: ApiError) -> JSONResponse:  # pyright: ignore[reportUnusedFunction]
        return error_response(exc.status, exc.code, exc.message, exc.details, exc.headers)

    @app.exception_handler(StarletteHTTPException)
    async def _http_error(_: Request, exc: StarletteHTTPException) -> JSONResponse:  # pyright: ignore[reportUnusedFunction]
        message = exc.detail or HTTPStatus(exc.status_code).phrase
        return error_response(exc.status_code, _CODES.get(exc.status_code, "HTTP_ERROR"), message, headers=exc.headers)

    @app.exception_handler(RequestValidationError)
    async def _validation_error(_: Request, exc: RequestValidationError) -> JSONResponse:  # pyright: ignore[reportUnusedFunction]
        # loc and msg only: the rejected input is never echoed back (it may hold a PIN or personal text).
        fields = [{"loc": list(e["loc"]), "msg": e["msg"]} for e in exc.errors()]
        return error_response(422, "VALIDATION_ERROR", "Request is not valid", {"fields": fields})

    @app.exception_handler(Exception)
    async def _unhandled(_: Request, exc: Exception) -> JSONResponse:  # pyright: ignore[reportUnusedFunction]
        _log.error("unhandled error", exc_info=exc, extra={"request_id": request_id_var.get()})
        return error_response(500, "INTERNAL_ERROR", "Internal server error")
