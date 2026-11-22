"""App factory. Run with: uv run uvicorn app.main:create_app --factory --no-access-log"""

from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from sqlalchemy.orm import Session, sessionmaker

from app.ai.client import AiClient
from app.ai.fake import FakeAiClient
from app.ai.gemini import GeminiClient
from app.api.context import RequestContextMiddleware, configure_logging
from app.api.errors import install_error_handlers
from app.api.limits import BodyLimitMiddleware
from app.config import Settings
from app.db import make_engine, make_session_factory
from app.routers import auth, health, life, users, work
from app.services.ai_runner import AiLimits, AiRunner


def _ai_client(settings: Settings) -> AiClient:
    if settings.ai_mode == "gemini" and settings.gemini_api_key is not None:
        key = settings.gemini_api_key.get_secret_value()
        return GeminiClient(key, settings.gemini_model, settings.ai_timeout_seconds)
    return FakeAiClient()  # development: nothing scripted, so every AI step takes the fallback path


def create_app(
    settings: Settings | None = None,
    session_factory: sessionmaker[Session] | None = None,
    ai_client: AiClient | None = None,
) -> FastAPI:
    """Tests inject a session factory bound to a rolled-back transaction and a scripted AI client."""
    settings = settings or Settings()  # pyright: ignore[reportCallIssue]  # values come from the environment
    configure_logging(settings.log_level)
    engine = None if session_factory else make_engine(settings.database_url)
    factory = session_factory or make_session_factory(engine)  # pyright: ignore[reportArgumentType]
    limits = AiLimits(settings.ai_max_concurrent, settings.ai_per_minute, settings.ai_daily_budget, wait_seconds=10)
    runner = AiRunner(ai_client or _ai_client(settings), factory, limits)

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
    app.state.ai = runner
    install_error_handlers(app)
    for module in (health, auth, users, work, life):
        app.include_router(module.router)

    # Added last = runs first: the request context wraps CORS, which wraps GZip.
    app.add_middleware(BodyLimitMiddleware)  # innermost of these: a 413 still gets CORS and request-id headers
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
