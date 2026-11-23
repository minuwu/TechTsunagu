"""The fallback ladder (architecture §10.3, §10.4; AI_PLAN.md §3): cache of every model → then for each model of the
chain: budget → limiter → call → validate → one retry → next model → degrade.

It opens its own short database sessions, so no request transaction is held across a model call (§8.3).
It never raises for AI trouble: the caller gets a value or `None` with the outcome, and builds its degraded draft."""

import hashlib
import json
import logging
import threading
import time
from collections import deque
from collections.abc import Callable, Sequence
from dataclasses import dataclass
from datetime import date, datetime
from typing import Literal
from zoneinfo import ZoneInfo

from pydantic import BaseModel, ValidationError
from sqlalchemy.orm import Session, sessionmaker

from app.ai.client import AiClient, AiUnavailable, Part, Prompt, QuotaExhausted
from app.ai.schemas import DEFAULT_LANGUAGES
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
    languages: tuple[str, ...] = DEFAULT_LANGUAGES  # the shop's languages besides ja (XREQ-011 item 5)


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

    def acquire(self, wait_seconds: float) -> bool:
        if not self._slots.acquire(timeout=wait_seconds):
            return False
        deadline = time.monotonic() + wait_seconds
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


@dataclass(frozen=True)
class _Link:
    """One model of the chain with its own budget and limiter (AI_PLAN.md §3–§4)."""

    client: AiClient
    limits: AiLimits
    limiter: _Limiter


class AiRunner:
    def __init__(
        self,
        client: AiClient,
        sessions: sessionmaker[Session],
        limits: AiLimits,
        backups: Sequence[tuple[AiClient, AiLimits]] = (),
    ) -> None:
        """`client` answers first; each backup is tried in order when the one before it gives no valid answer."""
        self._sessions = sessions
        self._chain = [_Link(c, lim, _Limiter(lim)) for c, lim in [(client, limits), *backups]]

    @property
    def model(self) -> str:
        return self._chain[0].client.model

    @property
    def models(self) -> list[str]:
        return [link.client.model for link in self._chain]

    def _key[T: BaseModel](self, req: AiRequest[T], model: str) -> str:
        head = f"{req.scope}|{req.pipeline}|v{req.prompt.version}|{model}|".encode()
        return hashlib.sha256(head + req.cache_input).hexdigest()

    def _call[T: BaseModel](
        self, link: _Link, req: AiRequest[T], extra: list[Part], now: datetime, *, wait: bool
    ) -> str | None:
        """One call to one model. `wait`: queue for its per-minute slot (the last model of the chain, and the
        validation retry); otherwise a busy model is skipped at once, because the next model answers sooner.
        The per-minute slot is taken before the day's budget, so a skipped call never spends a daily call."""
        day, model = pacific_day(now), link.client.model
        if not link.limiter.acquire(link.limits.wait_seconds if wait else 0):
            _log.info("ai limiter busy: %s", model, extra={"route": req.pipeline})
            return None
        try:
            with self._sessions() as db:
                allowed = repo.take_budget(db, day, model, link.limits.daily_budget)
                db.commit()
            if not allowed:
                _log.info("ai budget exhausted: %s", model, extra={"route": req.pipeline})
                return None
            return link.client.generate(req.prompt, [*req.parts, *extra], req.schema, req.languages)
        except QuotaExhausted:
            with self._sessions() as db:
                repo.mark_exhausted(db, day, model)
                db.commit()
            return None
        except AiUnavailable as exc:
            _log.warning("ai unavailable: %s", exc)
            return None
        finally:
            link.limiter.release()

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
        # A cached answer from any model of the chain wins, best first: a filmed 3.8 Flash take is reused
        # even when 3.8 Flash is out of quota (AI_PLAN.md §3 step 1).
        with self._sessions() as db:
            for link in self._chain:
                hit = repo.cached(db, self._key(req, link.client.model))
                if hit is not None:
                    return AiResult(req.schema.model_validate(hit.response), "cached")

        for link in self._chain:
            raw = self._call(link, req, [], now, wait=link is self._chain[-1])
            if raw is None:
                continue  # unavailable, out of budget or busy: the next model, at once
            value, problems = self._parse(req, raw, validate)
            outcome: Outcome = "ok"
            if value is None:
                note = "Your previous answer had these problems; fix them and answer again:\n- " + "\n- ".join(problems)
                raw = self._call(link, req, [Part.text(note)], now, wait=True)
                value, _ = self._parse(req, raw, validate) if raw is not None else (None, [])
                outcome = "retried"
            if value is None:
                continue
            model = link.client.model
            with self._sessions() as db:
                response = json.loads(value.model_dump_json())
                repo.store(db, self._key(req, model), req.scope, req.pipeline, req.prompt.version, model, response)
                db.commit()
            if link is not self._chain[0]:
                _log.info("ai answered by backup: %s", model, extra={"route": req.pipeline})
            return AiResult(value, outcome)

        _log.info("ai degraded", extra={"route": req.pipeline})
        return AiResult(None, "degraded")
