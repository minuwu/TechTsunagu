"""Talk Threads (XREQ-018, ADR 0014): a Worker's private notebook about real-life conversations (often an elderly
person in Kyushu dialect). An entry is what was said (typed or recorded), explained by explain_utterance; the owner's
own understanding is graded by grade_understanding; they pick a reply and note whether it got through.

Personal data: the scope is the signed-in user; no Foreman, Poster or Support Log route reads it, nothing notifies, and
deleting is a hard delete. Never a 500 for the AI: a failed explanation keeps the transcript with needsEdit, a failed
grade leaves grade null."""

import hashlib
import uuid
from datetime import datetime, timedelta

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import UnderstandingGrade, UtteranceExplanation
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.domain.talk import explanation_problems, needs_staff
from app.media import audio_mime
from app.models.personal import TalkEntry, TalkThread
from app.repositories import accounts
from app.repositories import contacts as people
from app.repositories import talk as repo
from app.schemas.talk import TalkEntryOut, TalkEntryUpdate, TalkThreadIn, TalkThreadOut
from app.services.ai_runner import AiRequest, AiRunner
from app.services.asks import LANGUAGE_NAMES
from app.services.auth import Principal

EXPLAIN_PROMPT_VERSION = 1
GRADE_PROMPT_VERSION = 1
ENTRIES_PER_MINUTE = 10  # each entry is one or two model calls


def _me(who: Principal) -> int:
    if who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.user_id


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _thread_out(t: TalkThread, entries: int) -> TalkThreadOut:
    return TalkThreadOut.model_validate(
        {"id": str(t.id), "title": t.title, "updated_at": t.updated_at, "entry_count": entries}
    )


def _entry_out(e: TalkEntry) -> TalkEntryOut:
    return TalkEntryOut.model_validate(
        {
            "id": str(e.id),
            "transcript": e.transcript,
            "plain_ja": e.plain_ja,
            "meaning": e.meaning,
            "terms": e.terms,
            "needs_staff": e.needs_staff,
            "needs_edit": e.needs_edit,
            "own_text": e.own_text,
            "grade": e.grade,
            "reply_options": e.reply_options,
            "reply_ja": e.reply_ja,
            "confirmed": e.confirmed,
            "created_at": e.created_at,
        }
    )


def _own_thread(db: Session, me: int, thread_id: int, *, for_update: bool = False) -> TalkThread:
    t = repo.thread(db, me, thread_id, for_update=for_update)
    if t is None:
        raise _not_found()
    return t


def list_threads(db: Session, who: Principal, page: PageParams) -> Page[TalkThreadOut]:
    me = _me(who)
    rows = repo.threads(db, me, page.after_id, page.limit + 1)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1][0].id) if len(rows) > page.limit else None
    return Page(items=[_thread_out(t, n) for t, n in shown], next_cursor=cursor)


def create_thread(db: Session, who: Principal, body: TalkThreadIn, key: uuid.UUID | None) -> TalkThreadOut:
    me = _me(who)
    if key is not None and (done := repo.thread_by_key(db, me, key)) is not None:
        return _thread_out(done, repo.entry_count(db, done.id))
    t = TalkThread(owner_id=me, title=body.title, idempotency_key=key)
    db.add(t)
    db.commit()
    return _thread_out(t, 0)


def rename_thread(db: Session, who: Principal, thread_id: int, body: TalkThreadIn, now: datetime) -> TalkThreadOut:
    me = _me(who)
    t = _own_thread(db, me, thread_id, for_update=True)
    t.title, t.updated_at = body.title, now
    db.commit()
    return _thread_out(t, repo.entry_count(db, t.id))


def delete_thread(db: Session, who: Principal, thread_id: int) -> None:
    """A hard delete with every entry (ADR 0014)."""
    me = _me(who)
    repo.delete_thread(db, _own_thread(db, me, thread_id, for_update=True))
    db.commit()


def list_entries(db: Session, who: Principal, thread_id: int, page: PageParams) -> Page[TalkEntryOut]:
    me = _me(who)
    _own_thread(db, me, thread_id)
    rows = repo.entries(db, me, thread_id, page.after_id, page.limit + 1)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[_entry_out(e) for e in shown], next_cursor=cursor)


def _limit(db: Session, me: int, now: datetime) -> None:
    if accounts.count_attempt(db, f"talk-entry:{me}", now, timedelta(minutes=1)) > ENTRIES_PER_MINUTE:
        db.commit()
        raise ApiError(429, "RATE_LIMITED", "Too many requests; try again in a minute", headers={"Retry-After": "60"})


def _explain(
    ai: AiRunner, me: int, text: str | None, audio: bytes | None, mime: str | None, lang: str, now: datetime
) -> UtteranceExplanation | None:
    part = Part.blob(audio, mime, lang) if audio and mime else Part.text(f"<what_was_said>{text}</what_was_said>")
    request = AiRequest(
        pipeline="explain_utterance",
        prompt=render("explain_utterance", EXPLAIN_PROMPT_VERSION, language=LANGUAGE_NAMES.get(lang, "English")),
        parts=[part],
        schema=UtteranceExplanation,
        scope=f"user:{me}",
        cache_input=(audio or (text or "").encode()) + f"|{lang}".encode(),
    )
    return ai.run(request, lambda e: explanation_problems(e, text), now).value


def add_entry(
    db: Session,
    who: Principal,
    thread_id: int,
    text: str | None,
    audio: bytes | None,
    key: uuid.UUID | None,
    ai: AiRunner,
    now: datetime,
) -> TalkEntryOut:
    """What was said, typed or recorded, explained in the owner's language. The AI down → the transcript (the typed
    text; empty for a recording) with needsEdit, never a 500."""
    me = _me(who)
    if key is not None and (done := repo.entry_by_key(db, me, key)) is not None:
        return _entry_out(done)
    text = text.strip() if text else None
    if bool(text) == bool(audio):
        raise ApiError(422, "VALIDATION_ERROR", "Send text or a recording, not both", {"fields": [{"loc": ["body"]}]})
    if text and len(text) > 2000:
        raise ApiError(422, "VALIDATION_ERROR", "Text is too long", {"fields": [{"loc": ["body", "text"]}]})
    mime = audio_mime(audio) if audio else None
    _own_thread(db, me, thread_id)
    _limit(db, me, now)
    user = people.user(db, me)
    lang = user.language if user else "en"
    db.commit()  # no transaction is held during the model call (§8.3)

    e = _explain(ai, me, text, audio, mime, lang, now)
    t = _own_thread(db, me, thread_id, for_update=True)  # deleted meanwhile → 404
    transcript = text or (e.transcript if e else "")
    row = TalkEntry(
        thread_id=t.id,
        owner_id=me,
        transcript=transcript[:2000],
        plain_ja=e.plain_ja if e else None,
        meaning=e.meaning if e else None,
        meaning_lang=lang,
        terms=[term.model_dump() for term in e.terms] if e else [],
        reply_options=[r.model_dump() for r in e.reply_options] if e else [],
        needs_staff=needs_staff([transcript, *([e.plain_ja, e.meaning] if e else [])]),
        needs_edit=e is None,
        idempotency_key=key,
    )
    db.add(row)
    t.updated_at = now
    db.commit()
    return _entry_out(row)


def _grade(ai: AiRunner, me: int, e: TalkEntry, own_text: str, now: datetime) -> str | None:
    if e.meaning is None:
        return None  # nothing to compare with (the explanation failed)
    request = AiRequest(
        pipeline="grade_understanding",
        prompt=render("grade_understanding", GRADE_PROMPT_VERSION, meaning=e.meaning, plain_ja=e.plain_ja or ""),
        parts=[Part.text(f"<worker_understanding>{own_text}</worker_understanding>")],
        schema=UnderstandingGrade,
        scope=f"user:{me}",
        cache_input=hashlib.sha256(f"{e.meaning}|{e.plain_ja}|{own_text}".encode()).digest(),
    )
    result = ai.run(request, lambda _: [], now).value
    return result.grade if result else None


def update_entry(
    db: Session,
    who: Principal,
    thread_id: int,
    entry_id: int,
    body: TalkEntryUpdate,
    ai: AiRunner,
    now: datetime,
) -> TalkEntryOut:
    """ownText is graded again (the AI down → grade null); replyJa is the reply shown; confirmed is whether it got
    through. A new reply clears the earlier confirmation."""
    me = _me(who)
    e = repo.entry(db, me, thread_id, entry_id)
    if e is None:
        raise _not_found()
    grade: str | None = None
    if body.own_text is not None:
        _limit(db, me, now)
        db.commit()  # no transaction is held during the model call (§8.3)
        grade = _grade(ai, me, e, body.own_text, now)
    e = repo.entry(db, me, thread_id, entry_id, for_update=True)
    if e is None:
        raise _not_found()
    if body.own_text is not None:
        e.own_text, e.grade = body.own_text, grade
    if body.reply_ja is not None:
        e.reply_ja, e.confirmed = body.reply_ja, None
    if body.confirmed is not None:
        e.confirmed = body.confirmed
    db.commit()
    return _entry_out(e)


def delete_entry(db: Session, who: Principal, thread_id: int, entry_id: int) -> None:
    me = _me(who)
    e = repo.entry(db, me, thread_id, entry_id, for_update=True)
    if e is None:
        raise _not_found()
    db.delete(e)
    db.commit()
