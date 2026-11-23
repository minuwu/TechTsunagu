"""The fallback ladder (architecture §10.3, §10.4; Galib's AI_PLAN.md §3): cache → model chain → validate → one retry
→ degrade.

The chain is the models of `app/ai/chain.py`, best first. A model is skipped when its day's budget is used or its
quota is exhausted; a busy model (503, per-minute 429) gets one short jittered retry, then the next model answers.
It opens its own short database sessions, so no request transaction is held across a model call (§8.3).
It never raises for AI trouble: the caller gets a value or `None` with the outcome, and builds its degraded draft."""

import hashlib
import json
import logging
import random
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

from app.ai.client import AiClient, AiUnavailable, Overloaded, Part, Prompt, QuotaExhausted
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
    per_minute: int  # for a single client; a chain link carries its own
    daily_budget: int  # for a single client; a chain link carries its own
    wait_seconds: float
    retry_backoff: tuple[float, float] = (0.5, 1.5)  # seconds before retrying a busy model, picked at random


@dataclass(frozen=True)
class ChainLink:
    """One model of the chain with its own budget and per-minute bucket (AI_PLAN.md §4)."""

    client: AiClient
    daily_budget: int
    per_minute: int


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
    model: str | None = None  # which model answered (or whose cached answer was used); None when degraded


class _Window:
    """At most N calls per rolling minute for one model, across all threads of this one replica (§15.3)."""

    def __init__(self, per_minute: int) -> None:
        self._lock = threading.Lock()
        self._recent: deque[float] = deque()
        self._per_minute = per_minute

    def acquire(self, wait_seconds: float) -> bool:
        deadline = time.monotonic() + wait_seconds
        while True:
            with self._lock:
                now = time.monotonic()
                while self._recent and now - self._recent[0] > 60:
                    self._recent.popleft()
                if len(self._recent) < self._per_minute:
                    self._recent.append(now)
                    return True
            if time.monotonic() >= deadline:
                return False
            time.sleep(0.2)


@dataclass(frozen=True)
class _Link:
    client: AiClient
    daily_budget: int
    window: _Window

    @property
    def model(self) -> str:
        return self.client.model


class AiRunner:
    def __init__(
        self, client: AiClient | Sequence[ChainLink], sessions: sessionmaker[Session], limits: AiLimits
    ) -> None:
        chain = (
            [ChainLink(client, limits.daily_budget, limits.per_minute)] if not isinstance(client, Sequence) else client
        )
        if not chain:
            raise ValueError("the AI chain needs at least one model")
        self._links = [_Link(c.client, c.daily_budget, _Window(c.per_minute)) for c in chain]
        self._sessions = sessions
        self._limits = limits
        self._slots = threading.BoundedSemaphore(limits.max_concurrent)  # shared: one replica, all models

    @property
    def model(self) -> str:
        """The first (best) model of the chain."""
        return self._links[0].model

    @property
    def models(self) -> list[str]:
        return [link.model for link in self._links]

    def _key[T: BaseModel](self, req: AiRequest[T], model: str) -> str:
        head = f"{req.scope}|{req.pipeline}|v{req.prompt.version}|{model}|".encode()
        return hashlib.sha256(head + req.cache_input).hexdigest()

    def _attempt[T: BaseModel](
        self, link: _Link, req: AiRequest[T], extra: list[Part], day: date, wait_seconds: float
    ) -> str | Literal["busy", "skip"]:
        """One call to one model: its raw reply, "busy" (worth one retry) or "skip" (go to the next model).
        The per-minute bucket is checked before the daily budget, so a skipped call never spends a Flash day call."""
        if not self._slots.acquire(timeout=self._limits.wait_seconds):
            _log.info("ai slots busy", extra={"route": req.pipeline})
            return "skip"
        try:
            if not link.window.acquire(wait_seconds):
                _log.info("ai per-minute limit reached for %s", link.model, extra={"route": req.pipeline})
                return "skip"
            with self._sessions() as db:
                allowed = repo.take_budget(db, day, link.model, link.daily_budget)
                db.commit()
            if not allowed:
                _log.info("ai budget used up for %s", link.model, extra={"route": req.pipeline})
                return "skip"
            return link.client.generate(req.prompt, [*req.parts, *extra], req.schema)
        except QuotaExhausted:
            with self._sessions() as db:
                repo.mark_exhausted(db, day, link.model)
                db.commit()
            return "skip"
        except Overloaded as exc:
            _log.warning("ai busy: %s", exc)
            return "busy"
        except AiUnavailable as exc:
            _log.warning("ai unavailable: %s", exc)
            return "skip"
        finally:
            self._slots.release()

    def _call_link[T: BaseModel](
        self, link: _Link, req: AiRequest[T], extra: list[Part], now: datetime, last: bool
    ) -> str | None:
        # Only the last model waits for its per-minute bucket; before it, moving on beats making the user wait.
        wait = self._limits.wait_seconds if last else 0.0
        day = pacific_day(now)
        result = self._attempt(link, req, extra, day, wait)
        if result == "busy":
            time.sleep(random.uniform(*self._limits.retry_backoff))  # noqa: S311  # jitter, not cryptography
            result = self._attempt(link, req, extra, day, wait)
        return None if result in ("busy", "skip") else result

    def _call_chain[T: BaseModel](self, req: AiRequest[T], now: datetime) -> tuple[str, _Link] | None:
        for i, link in enumerate(self._links):
            raw = self._call_link(link, req, [], now, last=i == len(self._links) - 1)
            if raw is not None:
                return raw, link
        return None

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
        # Cache for every model of the chain, best first: a filmed take is reused even when its model is out
        with self._sessions() as db:
            for link in self._links:
                hit = repo.cached(db, self._key(req, link.model))
                if hit is not None:
                    return AiResult(req.schema.model_validate(hit.response), "cached", link.model)

        answered = self._call_chain(req, now)
        if answered is None:
            _log.info("ai degraded: no model answered", extra={"route": req.pipeline})
            return AiResult(None, "degraded")
        raw, link = answered
        value, problems = self._parse(req, raw, validate)
        outcome: Outcome = "ok"
        if value is None:
            # One validation retry, on the model that answered (AI_PLAN.md §3 step 3)
            note = "Your previous answer had these problems; fix them and answer again:\n- " + "\n- ".join(problems)
            raw = self._call_link(link, req, [Part.text(note)], now, last=True)
            value, _ = self._parse(req, raw, validate) if raw is not None else (None, [])
            outcome = "retried"
        if value is None:
            _log.info("ai degraded", extra={"route": req.pipeline})
            return AiResult(None, "degraded")

        with self._sessions() as db:
            response = json.loads(value.model_dump_json())
            key = self._key(req, link.model)
            repo.store(db, key, req.scope, req.pipeline, req.prompt.version, link.model, response)
            db.commit()
        return AiResult(value, outcome, link.model)
