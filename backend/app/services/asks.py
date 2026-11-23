"""Asks (architecture §7.6, §9.3): a Worker's question is answered only from their district's published Notices, and
can be forwarded to their Senpai, translated to Japanese."""

import hashlib
import json
import uuid
from datetime import datetime

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import AskDraft, Translation
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.domain.grounding import ground
from app.media import audio_mime
from app.models.life import Ask
from app.repositories import life as repo
from app.schemas.asks import AskOut
from app.services.ai_runner import AiRequest, AiRunner
from app.services.auth import Principal
from app.services.notes import ai_languages

ASK_PROMPT_VERSION = 3  # v2 adds question_text (voice Asks keep a transcript); v3 per-shop languages
TRANSLATE_PROMPT_VERSION = 1
NOTICES_SENT = 20  # the newest published Notices of the district go into the prompt
LANGUAGE_NAMES = {
    "ja": "Japanese",
    "bn": "Bengali",
    "en": "English",
    "vi": "Vietnamese",
    "id": "Indonesian",
    "ne": "Nepali",
    "my": "Burmese",
    "tl": "Tagalog",
    "zh": "Chinese",
}


def worker_of(who: Principal) -> tuple[int, int, int]:
    """(shop, district, worker) of the signed-in Worker."""
    if who.shop_id is None or who.district_id is None or who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id, who.district_id, who.user_id


def ask_out(a: Ask) -> AskOut:
    return AskOut.model_validate(
        {
            "id": str(a.id),
            "worker_id": str(a.worker_id),
            "text": a.text_src,
            "covered": a.covered,
            "answer": a.answer,
            "source_notice_ids": [str(i) for i in a.source_notice_ids],
            "forwarded": a.forwarded_at is not None,
            "senpai_reply": a.senpai_reply,
            "created_at": a.created_at,
        }
    )


def translate(ai: AiRunner, text: str, source: str, target: str, scope: str, caller: str, now: datetime) -> str | None:
    """One faithful translation, or None when the model is unavailable or the caller is over their AI allowance
    (either way the original is kept, never an error)."""
    request = AiRequest(
        pipeline="translate",
        prompt=render(
            "translate",
            TRANSLATE_PROMPT_VERSION,
            source_lang=LANGUAGE_NAMES.get(source, source),
            target_lang=LANGUAGE_NAMES.get(target, target),
            glossary="[]",
        ),
        parts=[Part.text(f"<source_text>{text}</source_text>")],
        schema=Translation,
        scope=scope,
        cache_input=f"{source}>{target}:{text}".encode(),
        caller=caller,
        over_limit="degrade",
    )
    result = ai.run(request, lambda _: [], now)
    return result.value.text if result.value else None


def create(
    db: Session,
    who: Principal,
    text: str | None,
    audio: bytes | None,
    key: uuid.UUID,
    ai: AiRunner,
    now: datetime,
) -> AskOut:
    shop_id, district_id, worker_id = worker_of(who)
    existing = repo.ask_by_key(db, shop_id, worker_id, key)
    if existing is not None:
        return ask_out(existing)
    text = text.strip() if text else None
    if bool(text) == bool(audio):
        raise ApiError(422, "VALIDATION_ERROR", "Send text or a recording, not both", {"fields": [{"loc": ["body"]}]})
    mime = audio_mime(audio) if audio else None
    me = repo.worker(db, shop_id, worker_id)
    if me is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    language = me.language
    sent = repo.published_notices(db, district_id, NOTICES_SENT)
    block = "\n".join(
        json.dumps(
            {"id": str(n.id), "title": n.title.get("ja", ""), "text": n.source_ja, "steps": [s["ja"] for s in n.steps]},
            ensure_ascii=False,
        )
        for n in sent
    )
    sent_ids = [str(n.id) for n in sent]
    languages = ai_languages(db, shop_id)
    db.commit()  # end the read transaction: nothing is held open during the model call (§8.3)

    request = AiRequest(
        pipeline="answer_ask",
        # shortcut: no Glossary Terms; the shop's terms are workplace words, Asks are about life in the district.
        prompt=render(
            "answer_ask", ASK_PROMPT_VERSION, notices=block or "(none)", glossary="[]", languages=", ".join(languages)
        ),
        parts=[Part.blob(audio, mime)] if audio and mime else [Part.text(f"<worker_question>{text}</worker_question>")],
        schema=AskDraft,
        scope=f"shop:{shop_id}",
        cache_input=(audio or (text or "").encode()) + hashlib.sha256((block + ",".join(languages)).encode()).digest(),
        caller=f"user:{worker_id}",
        languages=languages,
    )
    # A cited Notice that was never sent is worth one retry; ground() below is the rule either way.
    result = ai.run(request, lambda d: [] if set(d.source_notice_ids) <= set(sent_ids) else ["unknown id"], now)
    draft = result.value
    answer = draft.answer.only(languages) if draft and draft.answer else None
    grounded = ground(
        draft.covered if draft else False,
        answer,
        draft.source_notice_ids if draft else [],
        sent_ids,
        languages,
    )

    raced = repo.ask_by_key(db, shop_id, worker_id, key)
    if raced is not None:
        return ask_out(raced)
    row = Ask(
        shop_id=shop_id,
        worker_id=worker_id,
        source_kind="voice" if audio else "text",
        # A typed Ask keeps the Worker's own words; a voice Ask keeps the model's transcript, so forwarding
        # it to the Senpai sends the question instead of an empty message ("" only when the AI was unavailable).
        text_src=text or (draft.question_text if draft else ""),
        language=language,
        covered=grounded.covered,
        answer=dict(grounded.answer) if grounded.answer else None,
        source_notice_ids=[int(i) for i in grounded.source_notice_ids],
        idempotency_key=key,
    )
    db.add(row)
    db.commit()
    return ask_out(row)


def list_own(db: Session, who: Principal, page: PageParams) -> Page[AskOut]:
    shop_id, _, worker_id = worker_of(who)
    rows = repo.asks_page(db, shop_id, worker_id, page.after_id, page.limit)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[ask_out(a) for a in shown], next_cursor=cursor)


def forward(db: Session, who: Principal, ask_id: int, ai: AiRunner, now: datetime) -> AskOut:
    """To the Worker's Senpai, with a Japanese translation; forwarding twice changes nothing."""
    shop_id, _, worker_id = worker_of(who)
    a = repo.ask(db, shop_id, worker_id, ask_id)
    if a is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    if a.forwarded_at is not None:
        return ask_out(a)
    pair = repo.active_pair(db, shop_id, worker_id, now)
    if pair is None:
        raise ApiError(409, "NO_ACTIVE_PAIR", "You have no Senpai yet; ask your Foreman")
    pair_id, text, language = pair.id, a.text_src, a.language
    db.commit()  # no lock is held during the model call (§8.3)

    if language == "ja" or not text:
        text_ja = text or None
    else:
        # None: the Senpai sees the original
        text_ja = translate(ai, text, language, "ja", f"shop:{shop_id}", f"user:{worker_id}", now)

    a = repo.ask(db, shop_id, worker_id, ask_id, for_update=True)
    if a is None:
        raise ApiError(404, "NOT_FOUND", "Not found")
    if a.forwarded_at is None:
        a.pair_id, a.forwarded_at, a.text_ja = pair_id, now, text_ja
        db.commit()
    return ask_out(a)
