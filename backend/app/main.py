"""App factory. Run with: uv run uvicorn app.main:create_app --factory --no-access-log"""

from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from sqlalchemy.orm import Session, sessionmaker

from app.ai.chains import BUDGETS, GEMINI_CHAINS, GROQ_MODEL
from app.ai.client import AiClient
from app.ai.fake import FakeAiClient
from app.ai.gemini import GeminiClient
from app.ai.groq import GroqClient
from app.api.context import RequestContextMiddleware, configure_logging
from app.api.errors import install_error_handlers
from app.api.limits import BodyLimitMiddleware
from app.config import Settings
from app.db import make_engine, make_session_factory
from app.routers import (
    auth,
    briefings,
    chat,
    health,
    interviews,
    knowledge,
    life,
    near_misses,
    skills,
    support,
    users,
    work,
)
from app.services.ai_runner import AiLimits, AiRunner

# Every API router, in include order; app.openapi reads the same list to annotate the contract.
ROUTERS = tuple(
    m.router
    for m in (health, auth, users, work, life, support, skills, chat, briefings, near_misses, interviews, knowledge)
)


def _limits(settings: Settings, model: str) -> AiLimits:
    per_day, per_minute = BUDGETS[settings.ai_tier][model]
    return AiLimits(settings.ai_max_concurrent, per_minute, per_day, wait_seconds=10)


def ai_runner(settings: Settings, factory: sessionmaker[Session], ai_client: AiClient | None = None) -> AiRunner:
    """Tests pass one scripted client. AI_MODE=gemini builds the chain of AI_CHAIN, plus Groq when its key is set."""
    if ai_client is not None or settings.ai_mode != "gemini" or settings.gemini_api_key is None:
        # development: nothing scripted, so every AI step takes the fallback path
        return AiRunner(ai_client or FakeAiClient(), factory, AiLimits(settings.ai_max_concurrent, 4, 18, 10))
    timeout = settings.ai_timeout_seconds
    key = settings.gemini_api_key.get_secret_value()
    links: list[tuple[AiClient, AiLimits]] = [
        (GeminiClient(key, model, timeout), _limits(settings, model)) for model in GEMINI_CHAINS[settings.ai_chain]
    ]
    if settings.groq_api_key is not None:
        groq = GroqClient(settings.groq_api_key.get_secret_value(), GROQ_MODEL, timeout)
        links.append((groq, _limits(settings, GROQ_MODEL)))
    (first, first_limits), *backups = links
    return AiRunner(first, factory, first_limits, backups)


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
    runner = ai_runner(settings, factory, ai_client)

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
    for router in ROUTERS:
        app.include_router(router)

    # Added last = runs first: the request context wraps CORS, which wraps GZip.
    app.add_middleware(BodyLimitMiddleware)  # innermost of these: a 413 still gets CORS and request-id headers
    app.add_middleware(GZipMiddleware, minimum_size=1000)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.frontend_origin,
        allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE"],
        allow_headers=["Authorization", "Content-Type", "Idempotency-Key", "If-None-Match", "X-Request-ID"],
        expose_headers=["ETag", "X-Request-ID", "Retry-After"],
        allow_credentials=False,
        max_age=600,
    )
    app.add_middleware(RequestContextMiddleware)
    return app
