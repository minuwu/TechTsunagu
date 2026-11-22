"""The fallback ladder (architecture §10.3, §10.4): cache → budget → limiter → call → validate → one retry → degrade.

It opens its own short database sessions, so no request transaction is held across a model call (§8.3).
It never raises for AI trouble: the caller gets a value or `None` with the outcome, and builds its degraded draft."""

import hashlib
import json
import logging
import threading
import time
from collections import deque
from collections.abc import Callable
from dataclasses import dataclass
from datetime import date, datetime
from typing import Literal
from zoneinfo import ZoneInfo

from pydantic import BaseModel, ValidationError
from sqlalchemy.orm import Session, sessionmaker

from app.ai.client import AiClient, AiUnavailable, Part, Prompt, QuotaExhausted
from app.repositories import ai as repo

_log = logging.getLogger("wakatta.ai")
_PACIFIC = ZoneInfo("America/Los_Angeles")
Outcome = Literal["ok", "cached", "retried", "degraded"]


def pacific_day(now: datetime) -> date:
    """Gemini's free-tier quota resets at midnight Pacific (14:00 Bangladesh, 17:00 Tokyo)."""
    return now.astimezone(_PACIFIC).date()


@dataclass(frozen=True)
class AiLimits:
    max_concurrent: int
    per_minute: int
    daily_budget: int
    wait_seconds: float


@dataclass(frozen=True)
class AiRequest[T: BaseModel]:
    pipeline: str
    prompt: Prompt
    parts: list[Part]
    schema: type[T]
    scope: str  # "shop:<id>" or "district:<id>": one scope never reads another's cache
    cache_input: bytes  # the input bytes plus anything that changes the answer (e.g. the glossary)


@dataclass(frozen=True)
class AiResult[T: BaseModel]:
    value: T | None
    outcome: Outcome


class _Limiter:
    """At most N calls at once and M per rolling minute, across all threads of this one replica (§15.3)."""

    def __init__(self, limits: AiLimits) -> None:
        self._slots = threading.BoundedSemaphore(limits.max_concurrent)
        self._lock = threading.Lock()
        self._recent: deque[float] = deque()
        self._limits = limits

    def acquire(self) -> bool:
        if not self._slots.acquire(timeout=self._limits.wait_seconds):
            return False
        deadline = time.monotonic() + self._limits.wait_seconds
        while True:
            with self._lock:
                now = time.monotonic()
                while self._recent and now - self._recent[0] > 60:
                    self._recent.popleft()
                if len(self._recent) < self._limits.per_minute:
                    self._recent.append(now)
                    return True
            if time.monotonic() >= deadline:
                self._slots.release()
                return False
            time.sleep(0.2)

    def release(self) -> None:
        self._slots.release()


class AiRunner:
    def __init__(self, client: AiClient, sessions: sessionmaker[Session], limits: AiLimits) -> None:
        self._client = client
        self._sessions = sessions
        self._limits = limits
        self._limiter = _Limiter(limits)

    @property
    def model(self) -> str:
        return self._client.model

    def _key[T: BaseModel](self, req: AiRequest[T]) -> str:
        head = f"{req.scope}|{req.pipeline}|v{req.prompt.version}|{self._client.model}|".encode()
        return hashlib.sha256(head + req.cache_input).hexdigest()

    def _call[T: BaseModel](self, req: AiRequest[T], extra: list[Part], now: datetime) -> str | None:
        day = pacific_day(now)
        with self._sessions() as db:
            allowed = repo.take_budget(db, day, self._client.model, self._limits.daily_budget)
            db.commit()
        if not allowed:
            _log.info("ai budget exhausted", extra={"route": req.pipeline})
            return None
        if not self._limiter.acquire():
            _log.info("ai limiter busy", extra={"route": req.pipeline})
            return None
        try:
            return self._client.generate(req.prompt, [*req.parts, *extra], req.schema)
        except QuotaExhausted:
            with self._sessions() as db:
                repo.mark_exhausted(db, day, self._client.model)
                db.commit()
            return None
        except AiUnavailable as exc:
            _log.warning("ai unavailable: %s", exc)
            return None
        finally:
            self._limiter.release()

    def _parse[T: BaseModel](
        self, req: AiRequest[T], raw: str, validate: Callable[[T], list[str]]
    ) -> tuple[T | None, list[str]]:
        try:
            value = req.schema.model_validate_json(raw)
        except ValidationError as exc:
            return None, [f"{'.'.join(map(str, e['loc']))}: {e['msg']}" for e in exc.errors()[:10]]
        problems = validate(value)
        return (value, []) if not problems else (None, problems)

    def run[T: BaseModel](self, req: AiRequest[T], validate: Callable[[T], list[str]], now: datetime) -> AiResult[T]:
        key = self._key(req)
        with self._sessions() as db:
            hit = repo.cached(db, key)
            if hit is not None:
                return AiResult(req.schema.model_validate(hit.response), "cached")

        raw = self._call(req, [], now)
        if raw is None:
            return AiResult(None, "degraded")
        value, problems = self._parse(req, raw, validate)
        outcome: Outcome = "ok"
        if value is None:
            note = "Your previous answer had these problems; fix them and answer again:\n- " + "\n- ".join(problems)
            raw = self._call(req, [Part.text(note)], now)
            value, _ = self._parse(req, raw, validate) if raw is not None else (None, [])
            outcome = "retried"
        if value is None:
            _log.info("ai degraded", extra={"route": req.pipeline})
            return AiResult(None, "degraded")

        with self._sessions() as db:
            response = json.loads(value.model_dump_json())
            repo.store(db, key, req.scope, req.pipeline, req.prompt.version, self._client.model, response)
            db.commit()
        return AiResult(value, outcome)
