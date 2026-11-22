"""App factory. Run with: uv run uvicorn app.main:create_app --factory --no-access-log"""

from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from sqlalchemy.orm import Session, sessionmaker

from app.api.context import RequestContextMiddleware, configure_logging
from app.api.errors import install_error_handlers
from app.config import Settings
from app.db import make_engine, make_session_factory
from app.routers import health


def create_app(settings: Settings | None = None, session_factory: sessionmaker[Session] | None = None) -> FastAPI:
    """`session_factory` is injected by tests (bound to a rolled-back transaction); production builds its own pool."""
    settings = settings or Settings()  # pyright: ignore[reportCallIssue]  # values come from the environment
    configure_logging(settings.log_level)
    engine = None if session_factory else make_engine(settings.database_url)
    factory = session_factory or make_session_factory(engine)  # pyright: ignore[reportArgumentType]

    @asynccontextmanager
    async def lifespan(_: FastAPI) -> AsyncGenerator[None]:
        yield
        if engine is not None:
            engine.dispose()

    app = FastAPI(
        title="Wakatta? API",
        version="0.1.0",
        openapi_url="/api/openapi.json",
        docs_url="/api/docs",
        redoc_url=None,
        lifespan=lifespan,
    )
    app.state.settings = settings
    app.state.session_factory = factory
    install_error_handlers(app)
    app.include_router(health.router)

    # Added last = runs first: the request context wraps CORS, which wraps GZip.
    app.add_middleware(GZipMiddleware, minimum_size=1000)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[settings.frontend_origin],
        allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE"],
        allow_headers=["Authorization", "Content-Type", "Idempotency-Key", "If-None-Match", "X-Request-ID"],
        expose_headers=["ETag", "X-Request-ID", "Retry-After"],
        allow_credentials=False,
        max_age=600,
    )
    app.add_middleware(RequestContextMiddleware)
    return app
