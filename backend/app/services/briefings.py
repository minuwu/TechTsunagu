"""Briefings, WS6 (朝礼): the Foreman records → AI draft → edits and publishes → Workers open and answer one question
→ the Foreman sees who understood (WS6_BRIEFING_PLAN.md). The AI step never raises: a fallback is a draft to finish."""

import hashlib
import json
import uuid
from collections.abc import Mapping
from datetime import datetime

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import BriefingDraft
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.domain.ai_checks import GlossaryTerm as AiTerm
from app.domain.ai_checks import glossary_problems, language_problems, question_problems, question_texts
from app.domain.skills import grade
from app.media import audio_mime
from app.models.work import Briefing, Check, Receipt
from app.repositories import briefings as repo
from app.repositories import work as work_repo
from app.schemas.briefings import (
    BriefingForForeman,
    BriefingForWorker,
    BriefingPublishIn,
    BriefingReceipts,
    ReceiptWorker,
)
from app.schemas.common import CheckQuestion, Lang
from app.schemas.work import CheckIn, CheckResult, WrongQuestion
from app.services.ai_runner import AiRequest, AiRunner
from app.services.auth import Principal
from app.services.notes import ai_languages, shop_of

BRIEFING_PROMPT_VERSION = 2
PLACEHOLDER = "（ここを入力してください）"  # a degraded draft's empty fields; publish refuses while one is left


def glossary_block(terms: list[AiTerm]) -> str:
    return json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in terms], ensure_ascii=False)


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _worker_of(who: Principal) -> tuple[int, int]:
    if who.role != "worker" or who.shop_id is None or who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id, who.user_id


def _for_foreman(b: Briefing) -> BriefingForForeman:
    return BriefingForForeman.model_validate(
        {
            "id": str(b.id),
            "text": b.text,
            "status": b.status,
            "created_at": b.created_at,
            "published_at": b.published_at,
            "transcript_ja": b.transcript_ja,
            "question": b.question,
            "needs_edit": b.needs_edit,
        }
    )


def _for_worker(b: Briefing, receipt: Receipt | None) -> BriefingForWorker:
    question = {k: v for k, v in b.question.items() if k != "correctIndex"}  # Workers never get the answer
    return BriefingForWorker.model_validate(
        {
            "id": str(b.id),
            "text": b.text,
            "status": b.status,
            "created_at": b.created_at,
            "published_at": b.published_at,
            "question": question,
            "opened": receipt is not None,
            "passed": receipt.passed if receipt else None,
        }
    )


def _degraded(text: str | None, languages: tuple[str, ...]) -> tuple[dict[str, str], dict[str, object]]:
    """Keep what the Foreman gave us; every other field says what to fill in (architecture §10.4)."""
    blank = {lang: PLACEHOLDER for lang in ("ja", *languages)}
    tri = blank | ({"ja": text} if text else {})
    question: dict[str, object] = {
        "id": "q1",
        "termId": None,
        "prompt": blank,
        "options": [blank, blank, blank],
        "correctIndex": 0,
    }
    return tri, question


def create_draft(
    db: Session, who: Principal, text: str | None, audio: bytes | None, key: uuid.UUID, ai: AiRunner, now: datetime
) -> BriefingForForeman:
    shop_id = shop_of(who)
    existing = repo.by_key(db, shop_id, key)
    if existing is not None:
        return _for_foreman(existing)
    if not text and not audio:
        raise ApiError(422, "VALIDATION_ERROR", "Send a recording or text", {"fields": [{"loc": ["body", "text"]}]})
    mime = audio_mime(audio) if audio else None
    terms = [
        AiTerm(id=str(t.id), ja=t.ja, reading=t.reading, translations=t.translations)
        for t in work_repo.glossary(db, shop_id)
    ]
    languages = ai_languages(db, shop_id)
    db.commit()  # end the read transaction: nothing is held open during the model call (§8.3)

    glossary = glossary_block(terms)
    parts = [Part.blob(audio, mime)] if audio and mime else [Part.text(f"<foreman_input>{text}</foreman_input>")]
    request = AiRequest(
        pipeline="build_briefing",
        prompt=render("build_briefing", BRIEFING_PROMPT_VERSION, glossary=glossary, languages=", ".join(languages)),
        parts=parts,
        schema=BriefingDraft,
        scope=f"shop:{shop_id}",
        cache_input=(audio or (text or "").encode())
        + hashlib.sha256((glossary + ",".join(languages)).encode()).digest(),
        languages=languages,
    )
    result = ai.run(
        request,
        lambda d: (
            language_problems([d.text, *question_texts([d.question])], languages)
            + glossary_problems(d.transcript_ja, [d.text], terms, languages)
            + question_problems([d.question], terms)
        ),
        now,
    )

    if result.value is not None:
        d = result.value
        term_ids = {t.ja: t.id for t in terms}
        tri: dict[str, str] = d.text.only(languages)
        question: dict[str, object] = {
            "id": "q1",
            "termId": term_ids.get(d.question.term_ja or ""),
            "prompt": d.question.prompt.only(languages),
            "options": [o.only(languages) for o in d.question.options],
            "correctIndex": d.question.correct_index,
        }
        transcript, needs_edit = d.transcript_ja, False
    else:
        tri, question = _degraded(text, languages)
        transcript, needs_edit = text or "", True

    raced = repo.by_key(db, shop_id, key)
    if raced is not None:
        return _for_foreman(raced)
    row = Briefing(
        shop_id=shop_id,
        recorded_by=who.user_id,
        transcript_ja=transcript,
        text=tri,
        question=question,
        needs_edit=needs_edit,
        idempotency_key=key,
    )
    db.add(row)
    db.commit()
    return _for_foreman(row)


def _has_placeholder(values: Mapping[Lang, str]) -> bool:
    return any(v.strip() == PLACEHOLDER for v in values.values())


def publish(db: Session, who: Principal, body: BriefingPublishIn, now: datetime) -> BriefingForForeman:
    """The Foreman's reviewed draft goes out: their edits to the text and question are what Workers see."""
    shop_id = shop_of(who)
    b = repo.briefing(db, shop_id, int(body.id), for_update=True)
    if b is None:
        raise _not_found()
    if b.status != "draft":
        raise ApiError(409, "CONFLICT", "This Briefing is already published")
    q = body.question
    if _has_placeholder(body.text) or _has_placeholder(q.prompt) or any(_has_placeholder(o) for o in q.options):
        raise ApiError(409, "NEEDS_EDIT", "Fill in every field marked （ここを入力してください） before publishing")
    b.text = {str(lang): value for lang, value in body.text.items()}
    b.question = q.model_dump(by_alias=True)
    b.status, b.published_at, b.needs_edit = "published", now, False
    db.commit()
    return _for_foreman(b)


def list_for_foreman(db: Session, who: Principal, page: PageParams) -> Page[BriefingForForeman]:
    rows = repo.page(db, shop_of(who), published_only=False, after_id=page.after_id, limit=page.limit)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[_for_foreman(b) for b in shown], next_cursor=cursor)


def list_for_worker(db: Session, who: Principal, page: PageParams) -> Page[BriefingForWorker]:
    shop_id, worker_id = _worker_of(who)
    rows = repo.page(db, shop_id, published_only=True, after_id=page.after_id, limit=page.limit)
    shown = rows[: page.limit]
    receipts = repo.receipts_of(db, shop_id, worker_id, [b.id for b in shown])
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[_for_worker(b, receipts.get(b.id)) for b in shown], next_cursor=cursor)


def _published(db: Session, shop_id: int, briefing_id: int) -> Briefing:
    b = repo.briefing(db, shop_id, briefing_id)
    if b is None or b.status != "published":
        raise _not_found()
    return b


def open_briefing(db: Session, who: Principal, briefing_id: int) -> None:
    shop_id, worker_id = _worker_of(who)
    b = _published(db, shop_id, briefing_id)
    repo.open_receipt(db, shop_id, worker_id, b.id)
    db.commit()


def _check_result(question: Mapping[str, object], wrong: list[int]) -> CheckResult:
    q = CheckQuestion.model_validate(question)
    shown = [WrongQuestion(id=q.id, term_id=q.term_id, prompt=q.prompt)] if wrong else []
    return CheckResult(passed=not wrong, wrong=shown, sign_off=None)


def _answer_key(b: Briefing) -> list[int]:
    return [int(str(b.question["correctIndex"]))]


def submit_check(db: Session, who: Principal, body: CheckIn, key: uuid.UUID) -> CheckResult:
    """A Briefing's one question: graded on the server, stored as a Check, and the receipt gets `passed` (§7.3)."""
    shop_id, worker_id = _worker_of(who)
    done = work_repo.check_by_key(db, shop_id, worker_id, key)
    if done is not None:
        if done.briefing_id is None or done.briefing_id != int(body.target.id):
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another Check")
        first = _published(db, shop_id, done.briefing_id)
        return _check_result(first.question, grade(_answer_key(first), done.answers))

    b = _published(db, shop_id, int(body.target.id))
    try:
        wrong = grade(_answer_key(b), body.answers)
    except ValueError as exc:
        raise ApiError(422, "VALIDATION_ERROR", str(exc), {"fields": [{"loc": ["body", "answers"]}]}) from exc
    db.add(
        Check(
            shop_id=shop_id,
            worker_id=worker_id,
            briefing_id=b.id,
            language=body.language,
            answers=body.answers,
            passed=not wrong,
            idempotency_key=key,
        )
    )
    repo.open_receipt(db, shop_id, worker_id, b.id, passed=not wrong)
    result = _check_result(b.question, wrong)  # built before the commit, as notices.submit_check does
    db.commit()
    return result


def receipts(db: Session, who: Principal, briefing_id: int) -> BriefingReceipts:
    """Who understood (passed), who only opened it, and who has not opened it: every active Worker in one group."""
    shop_id = shop_of(who)
    if repo.briefing(db, shop_id, briefing_id) is None:
        raise _not_found()
    by_worker = {r.worker_id: r for r in repo.receipts_for(db, shop_id, briefing_id)}
    groups: dict[str, list[ReceiptWorker]] = {"understood": [], "opened_only": [], "not_opened": []}
    for w in repo.active_workers(db, shop_id):
        r = by_worker.get(w.id)
        group = "not_opened" if r is None else "understood" if r.passed else "opened_only"
        groups[group].append(ReceiptWorker(id=str(w.id), name=w.display_name))
    return BriefingReceipts.model_validate(groups)
