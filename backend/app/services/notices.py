"""Notices, Events, Notice receipts and Notice Checks (architecture §7.5, §9.3 Life flow, §10.4).

A Poster's scope is their district and organisation; a Worker reads their district's published Notices. Every id from
another district is a 404."""

import hashlib
import uuid
from collections.abc import Mapping, Sequence
from datetime import datetime, time
from typing import Literal

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import NoticeDraft
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.domain.recurrence import (
    HORIZON,
    MAX_RRULE_LENGTH,
    TOKYO,
    InvalidEventError,
    occurrences_between,
    validate_event,
)
from app.domain.skills import grade
from app.media import audio_mime, jpeg_without_exif
from app.models.life import Event, Notice
from app.models.platform import Blob
from app.models.work import Check, Receipt
from app.repositories import accounts
from app.repositories import life as repo
from app.repositories import work as work_repo
from app.schemas.common import CheckQuestion, CheckQuestionForWorker, Translated
from app.schemas.notices import (
    EventOccurrence,
    EventOut,
    EventsQuery,
    EventUpdate,
    NoticeForPoster,
    NoticeForWorker,
    NoticeReach,
    NoticeUpdate,
    PosterOut,
)
from app.schemas.work import CheckIn, CheckResult, WrongQuestion
from app.services.ai_runner import AiRequest, AiRunner
from app.services.auth import Principal

NOTICE_PROMPT_VERSION = 2
LANGUAGES = ("ja", "bn", "en")  # shortcut: the AI schema is fixed to ja/bn/en (as notes.AI_LANGUAGES)
PLACEHOLDER = "（ここを入力してください）"  # a degraded draft's empty fields; publish refuses while one is left
NEXT_DATES = 3
MAX_TEXT = 1000  # Translated values are at most 1000 characters
SourceKind = Literal["text", "voice", "photo"]


def district_of(who: Principal) -> int:
    if who.district_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.district_id


def _poster_id(db: Session, who: Principal) -> int:
    poster = repo.poster_of(db, district_of(who), who.user_id or 0)
    if poster is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return poster.id


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _next_cursor(rows: Sequence[Notice], limit: int) -> str | None:
    """`rows` was fetched with limit + 1; one extra row means there is a next page."""
    return encode_cursor(rows[limit - 1].id) if len(rows) > limit else None


def _event_out(e: Event, now: datetime) -> EventOut:
    try:
        upcoming = occurrences_between(e.starts_at, e.rrule, now, now + HORIZON)[:NEXT_DATES]
    except InvalidEventError:
        upcoming = []  # the Poster sees no dates and must fix the rule before confirming
    return EventOut.model_validate(
        {
            "id": str(e.id),
            "title": e.title,
            "starts_at": e.starts_at,
            "rrule": e.rrule,
            "confirmed": e.confirmed,
            "next_dates": upcoming,
        }
    )


def _common(n: Notice, events: Sequence[Event], now: datetime) -> dict[str, object]:
    return {
        "id": str(n.id),
        "poster_id": str(n.poster_id),
        "category": n.category,
        "source_kind": n.source_kind,
        "source_ja": n.source_ja,
        "photo": f"/api/blobs/{n.source_blob_id}" if n.source_blob_id else None,
        "title": n.title,
        "steps": n.steps,
        "events": [_event_out(e, now) for e in events],
        "status": n.status,
        "needs_edit": n.needs_edit,
        "created_at": n.created_at,
        "published_at": n.published_at,
    }


def _for_poster(n: Notice, events: Sequence[Event], now: datetime) -> NoticeForPoster:
    return NoticeForPoster.model_validate({**_common(n, events, now), "question": n.question})


def _for_worker(n: Notice, events: Sequence[Event], receipt: Receipt | None, now: datetime) -> NoticeForWorker:
    question = {k: v for k, v in n.question.items() if k != "correctIndex"}  # Workers never get the answer
    return NoticeForWorker.model_validate(
        {
            **_common(n, events, now),
            "question": CheckQuestionForWorker.model_validate(question),
            "opened": receipt is not None,
            "passed": receipt.passed if receipt else None,
        }
    )


def _poster_view(db: Session, n: Notice, now: datetime) -> NoticeForPoster:
    return _for_poster(n, repo.events_of(db, n.district_id, [n.id])[n.id], now)


def posters(db: Session, who: Principal) -> Page[PosterOut]:
    poster = repo.poster_of(db, district_of(who), who.user_id or 0)
    if poster is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    out = PosterOut.model_validate({"id": str(poster.id), "name": poster.name, "kind": poster.kind})
    return Page(items=[out], next_cursor=None)


def _draft_problems(d: NoticeDraft) -> list[str]:
    problems: list[str] = []
    for e in d.events:
        try:
            starts = datetime.fromisoformat(e.starts_at)
        except ValueError:
            problems.append(f"event starts_at is not ISO 8601: {e.starts_at}")
            continue
        if starts.tzinfo is None:
            problems.append(f"event starts_at needs +09:00: {e.starts_at}")
        if e.rrule is not None and len(e.rrule) > MAX_RRULE_LENGTH:
            problems.append("event rrule is too long")
    return problems


def _source_parts(kind: SourceKind, text: str | None, audio: bytes | None, image: bytes | None) -> list[Part]:
    if kind == "voice" and audio:
        return [Part.blob(audio, audio_mime(audio))]
    if kind == "photo" and image:
        return [Part.blob(image, "image/jpeg")]
    return [Part.text(f"<notice_source>{text}</notice_source>")]


def _replay(db: Session, n: Notice, poster_id: int, now: datetime) -> NoticeForPoster:
    """A retry returns the first draft, but only to its organisation; the key is unique per district."""
    if n.poster_id != poster_id:
        raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another Notice")
    return _poster_view(db, n, now)


def create_draft(
    db: Session,
    who: Principal,
    kind: SourceKind,
    text: str | None,
    audio: bytes | None,
    photo: bytes | None,
    key: uuid.UUID,
    ai: AiRunner,
    now: datetime,
) -> NoticeForPoster:
    district_id, poster_id = district_of(who), _poster_id(db, who)
    existing = repo.notice_by_key(db, district_id, key)
    if existing is not None:
        return _replay(db, existing, poster_id, now)
    text = text.strip() if text else None
    given = {"text": bool(text), "voice": bool(audio), "photo": bool(photo)}
    if not given[kind] or sum(given.values()) != 1:
        raise ApiError(422, "VALIDATION_ERROR", "Send exactly the one source sourceKind names")
    if audio:
        audio_mime(audio)  # size and type are refused before any model call
    image = jpeg_without_exif(photo) if photo else None
    db.commit()  # end the read transaction: nothing is held open during the model call (§8.3)

    today = now.astimezone(TOKYO).date().isoformat()
    request = AiRequest(
        pipeline="build_notice",
        # shortcut: no Glossary Terms; they belong to a shop and a Poster has none. Add district terms if needed.
        prompt=render("build_notice", NOTICE_PROMPT_VERSION, glossary="[]", today=today),
        parts=_source_parts(kind, text, audio, image),
        schema=NoticeDraft,
        scope=f"district:{district_id}",
        # Without today: a flyer states absolute dates, so the same flyer gives the same draft (demo cache).
        cache_input=image or audio or (text or "").encode(),
    )
    result = ai.run(request, _draft_problems, now)

    raced = repo.notice_by_key(db, district_id, key)
    if raced is not None:
        return _replay(db, raced, poster_id, now)
    blob_id = None
    if image is not None:
        blob = Blob(
            district_id=district_id,
            created_by=who.user_id,
            mime="image/jpeg",
            size_bytes=len(image),
            sha256=hashlib.sha256(image).digest(),
            bytes_=image,
        )
        db.add(blob)
        db.flush()
        blob_id = blob.id

    row = Notice(
        district_id=district_id,
        poster_id=poster_id,
        created_by=who.user_id,
        source_kind=kind,
        source_blob_id=blob_id,
        idempotency_key=key,
        **_draft_fields(result.value, text),
    )
    db.add(row)
    db.flush()
    for e in result.value.events if result.value else []:
        db.add(
            Event(
                district_id=district_id,
                notice_id=row.id,
                title=e.title.model_dump(),
                starts_at=datetime.fromisoformat(e.starts_at),
                rrule=e.rrule,
            )
        )
    db.commit()
    return _poster_view(db, row, now)


def _draft_fields(d: NoticeDraft | None, text: str | None) -> dict[str, object]:
    if d is not None:
        q = d.question
        question = {
            "id": "q1",
            "termId": None,
            "prompt": q.prompt.model_dump(),
            "options": [o.model_dump() for o in q.options],
            "correctIndex": q.correct_index,
        }
        return {
            "source_ja": d.source_ja,
            "category": d.category,
            "title": d.title.model_dump(),
            "steps": [s.model_dump() for s in d.steps],
            "question": question,
            "needs_edit": False,
        }
    # Degraded (§10.4): keep what the Poster gave us; they finish the draft before anyone sees it.
    blank = {"ja": PLACEHOLDER}
    return {
        "source_ja": text or "",
        "category": "other",
        "title": {"ja": (text or PLACEHOLDER)[:40]},
        "steps": [{"ja": (text or PLACEHOLDER)[:MAX_TEXT]}],
        "question": {"id": "q1", "termId": None, "prompt": blank, "options": [blank] * 3, "correctIndex": 0},
        "needs_edit": True,
    }


def _own_draft(db: Session, who: Principal, notice_id: int) -> Notice:
    n = repo.notice(db, district_of(who), notice_id, for_update=True)
    if n is None or n.poster_id != _poster_id(db, who):
        raise _not_found()
    if n.status != "draft":
        raise ApiError(409, "CONFLICT", "Only a draft can be changed")
    return n


def list_for_poster(db: Session, who: Principal, page: PageParams, now: datetime) -> Page[NoticeForPoster]:
    district_id = district_of(who)
    rows = repo.notices_page(db, district_id, _poster_id(db, who), page.after_id, page.limit)
    shown = rows[: page.limit]
    events = repo.events_of(db, district_id, [n.id for n in shown])
    return Page(items=[_for_poster(n, events[n.id], now) for n in shown], next_cursor=_next_cursor(rows, page.limit))


def list_for_worker(db: Session, who: Principal, page: PageParams, now: datetime) -> Page[NoticeForWorker]:
    district_id = district_of(who)
    rows = repo.notices_page(db, district_id, None, page.after_id, page.limit)
    shown = rows[: page.limit]
    ids = [n.id for n in shown]
    events, receipts = repo.events_of(db, district_id, ids), repo.receipts_of(db, who.user_id or 0, ids)
    items = [_for_worker(n, events[n.id], receipts.get(n.id), now) for n in shown]
    return Page(items=items, next_cursor=_next_cursor(rows, page.limit))


def get_for_poster(db: Session, who: Principal, notice_id: int, now: datetime) -> NoticeForPoster:
    """One of the Poster's own Notices, drafts too; anyone else's is 404, as in the list."""
    n = repo.notice(db, district_of(who), notice_id)
    if n is None or n.poster_id != _poster_id(db, who):
        raise _not_found()
    return _poster_view(db, n, now)


def get_for_worker(db: Session, who: Principal, notice_id: int, now: datetime) -> NoticeForWorker:
    """One published Notice of the Worker's district, without the answer key; a draft is 404, as in the list."""
    district_id = district_of(who)
    n = repo.notice(db, district_id, notice_id)
    if n is None or n.status != "published":
        raise _not_found()
    events = repo.events_of(db, district_id, [n.id])[n.id]
    receipt = repo.receipts_of(db, who.user_id or 0, [n.id]).get(n.id)
    return _for_worker(n, events, receipt, now)


def update(db: Session, who: Principal, notice_id: int, body: NoticeUpdate, now: datetime) -> NoticeForPoster:
    n = _own_draft(db, who, notice_id)
    if body.category is not None:
        n.category = body.category
    if body.title is not None:
        n.title = _plain(body.title)
    if body.steps is not None:
        n.steps = [_plain(s) for s in body.steps]
    if body.question is not None:
        n.question = body.question.model_dump(by_alias=True)
    db.commit()
    return _poster_view(db, n, now)


def update_event(
    db: Session, who: Principal, notice_id: int, event_id: int, body: EventUpdate, now: datetime
) -> NoticeForPoster:
    n = _own_draft(db, who, notice_id)
    e = repo.event(db, n.district_id, n.id, event_id)
    if e is None:
        raise _not_found()
    starts_at = body.starts_at or e.starts_at
    rule = body.rrule if "rrule" in body.model_fields_set else e.rrule
    if body.confirmed:
        try:
            validate_event(starts_at, rule, now)
        except InvalidEventError as exc:
            raise ApiError(422, "VALIDATION_ERROR", "This date cannot be published", {"reason": exc.reason}) from exc
    e.starts_at, e.rrule, e.confirmed = starts_at, rule, body.confirmed
    db.commit()
    return _poster_view(db, n, now)


def _plain(text: Translated) -> dict[str, str]:
    return {str(lang): value for lang, value in text.items()}


def _missing_text(values: Sequence[Mapping[str, str]]) -> bool:
    return any(not v.get(lang, "").strip() or v.get(lang) == PLACEHOLDER for v in values for lang in LANGUAGES)


def publish(db: Session, who: Principal, notice_id: int, now: datetime) -> NoticeForPoster:
    n = _own_draft(db, who, notice_id)
    for e in repo.events_of(db, n.district_id, [n.id])[n.id]:
        reason = None if e.confirmed else "not_confirmed"
        if reason is None:
            try:
                validate_event(e.starts_at, e.rrule, now)
            except InvalidEventError as exc:
                reason = exc.reason
        if reason is not None:
            details = {"eventId": str(e.id), "reason": reason}
            raise ApiError(409, "EVENTS_NOT_CONFIRMED", "Confirm every date first", details)
    q = CheckQuestion.model_validate(n.question)
    if _missing_text([n.title, *n.steps, _plain(q.prompt), *map(_plain, q.options)]):
        raise ApiError(409, "NEEDS_EDIT", "Fill in the title, steps and question in every language first")
    n.status, n.published_at, n.needs_edit = "published", now, False
    accounts.audit(db, "notice_published", who.user_id, district_id=n.district_id, subject_id=n.id)
    db.commit()
    return _poster_view(db, n, now)


def reach(db: Session, who: Principal, notice_id: int) -> NoticeReach:
    n = repo.notice(db, district_of(who), notice_id)
    if n is None or n.poster_id != _poster_id(db, who):
        raise _not_found()
    workers, opened, understood = repo.reach(db, n.district_id, n.id)
    return NoticeReach(workers=workers, opened=opened, understood=understood, not_opened=workers - opened)


def occurrences(db: Session, district_id: int, start: datetime, end: datetime) -> list[EventOccurrence]:
    out: list[EventOccurrence] = []
    for e in repo.published_events(db, district_id):
        try:
            dates = occurrences_between(e.starts_at, e.rrule, start, end)
        except InvalidEventError:
            continue  # publish refuses invalid rules; an old row must not break the whole list
        base = {"event_id": str(e.id), "notice_id": str(e.notice_id), "title": e.title}
        out += [EventOccurrence.model_validate({**base, "at": d}) for d in dates]
    return sorted(out, key=lambda o: o.at)


def events_between(db: Session, who: Principal, q: EventsQuery) -> list[EventOccurrence]:
    start = datetime.combine(q.from_, time.min, TOKYO)
    end = datetime.combine(q.to, time.max, TOKYO)
    return occurrences(db, district_of(who), start, end)


def photo(db: Session, who: Principal, blob_id: int) -> tuple[bytes, str]:
    """Published Notices' photos for the district; a draft's photo only for the organisation that wrote it."""
    found = repo.notice_photo(db, district_of(who), blob_id)
    if found is None:
        raise _not_found()
    b, n = found
    if n.status != "published" and (who.role != "poster" or n.poster_id != _poster_id(db, who)):
        raise _not_found()
    return b.bytes_, b.mime


def _published(db: Session, who: Principal, notice_id: int) -> Notice:
    n = repo.notice(db, district_of(who), notice_id)
    if n is None or n.status != "published":
        raise _not_found()
    return n


def _shop_worker(who: Principal) -> tuple[int, int]:
    if who.shop_id is None or who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id, who.user_id


def open_notice(db: Session, who: Principal, notice_id: int) -> None:
    shop_id, worker_id = _shop_worker(who)
    n = _published(db, who, notice_id)
    repo.open_receipt(db, shop_id, worker_id, n.id)
    db.commit()


def _check_result(question: Mapping[str, object], wrong: list[int]) -> CheckResult:
    q = CheckQuestion.model_validate(question)
    shown = [WrongQuestion(id=q.id, term_id=q.term_id, prompt=q.prompt)] if wrong else []
    return CheckResult(passed=not wrong, wrong=shown, sign_off=None)


def _answer_key(n: Notice) -> list[int]:
    return [int(str(n.question["correctIndex"]))]


def submit_check(db: Session, who: Principal, body: CheckIn, key: uuid.UUID) -> CheckResult:
    """A Notice's one question: graded on the server, stored as a Check, and the receipt gets `passed`."""
    shop_id, worker_id = _shop_worker(who)
    done = work_repo.check_by_key(db, shop_id, worker_id, key)
    if done is not None:
        if done.notice_id is None or done.notice_id != int(body.target.id):
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another Check")
        first = _published(db, who, done.notice_id)
        return _check_result(first.question, grade(_answer_key(first), done.answers))

    n = _published(db, who, int(body.target.id))
    try:
        wrong = grade(_answer_key(n), body.answers)
    except ValueError as exc:
        raise ApiError(422, "VALIDATION_ERROR", str(exc), {"fields": [{"loc": ["body", "answers"]}]}) from exc
    db.add(
        Check(
            shop_id=shop_id,
            worker_id=worker_id,
            notice_id=n.id,
            language=body.language,
            answers=body.answers,
            passed=not wrong,
            idempotency_key=key,
        )
    )
    repo.open_receipt(db, shop_id, worker_id, n.id, passed=not wrong)
    # Build the response before committing, as floor.submit_check does: a failure here must not leave a saved Check.
    result = _check_result(n.question, wrong)
    db.commit()
    return result
