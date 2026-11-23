"""The Wakatta? chatbot (RAG_PLAN.md, one bot for Work and Life): a Worker asks by text or voice; the answer comes only
from the shop's approved Note Versions, the district's published Notices and two public guides (MHLW manufacturing
safety, ISA living and working), cites them, and ends with one self-check question. Nothing is stored.
Never a 500: no source, a model failure or an ungrounded answer is `covered: false`, and the app offers to forward
the question to the Senpai (POST /asks, then /forward). Injury, fire and gas questions get a fixed card, never AI."""

import hashlib
import json
from datetime import datetime
from functools import cache
from pathlib import Path
from typing import Any

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import KnowledgeDraft, Translation
from app.api.errors import ApiError
from app.domain.ai_checks import GlossaryTerm as AiTerm
from app.domain.ai_checks import glossary_problems, language_problems, question_problems, question_texts
from app.domain.grounding import ground
from app.domain.retrieval import Piece, is_emergency, search, tokens
from app.media import audio_mime
from app.repositories import life as life_repo
from app.repositories import work as work_repo
from app.schemas.knowledge import KnowledgeAnswer, KnowledgeSource
from app.schemas.work import Step
from app.services.ai_runner import AiRequest, AiRunner
from app.services.asks import LANGUAGE_NAMES, worker_of
from app.services.auth import Principal
from app.services.notes import ai_languages

PROMPT_VERSION = 1
TRANSLATE_PROMPT_VERSION = 1
NOTICES_SEARCHED = 20
KNOWLEDGE = Path(__file__).resolve().parents[1] / "seed_data" / "knowledge.json"
EMERGENCY_CARD = {
    "ja": "すぐに作業を止めて、まわりの人とフォアマンに大きな声で知らせてください。"
    "けが・火事・ガスのときは 119 に電話してください。",
    "en": "Stop work now and shout to the people around you and your Foreman. For an injury, fire or gas, call 119.",
}
Json = dict[str, Any]


@cache
def _guides() -> tuple[dict[str, Json], tuple[Piece, ...], dict[str, Json]]:
    """(sources by id, pieces, chunks by id) of the public guides, read once."""
    data: Json = json.loads(KNOWLEDGE.read_text(encoding="utf-8"))
    sources = {s["id"]: s for s in data["sources"]}
    chunks = {c["id"]: c for c in data["chunks"]}
    return sources, tuple(Piece(c["id"], c["text"]) for c in chunks.values()), chunks


def _glossary_block(terms: list[AiTerm]) -> str:
    return json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in terms], ensure_ascii=False)


def _english(ai: AiRunner, shop_id: int, part: Part, source_lang: str, key: bytes, now: datetime) -> str | None:
    """The question in English, for searching the English guides (a voice question is transcribed on the way)."""
    request = AiRequest(
        pipeline="translate",
        prompt=render(
            "translate", TRANSLATE_PROMPT_VERSION, source_lang=source_lang, target_lang="English", glossary="[]"
        ),
        parts=[part],
        schema=Translation,
        scope=f"shop:{shop_id}",
        cache_input=b"knowledge-q:" + key,
    )
    result = ai.run(request, lambda _: [], now)
    return result.value.text if result.value else None


def _card(question: str) -> KnowledgeAnswer:
    """The fixed emergency card: written by people, shown before any model is asked."""
    return KnowledgeAnswer.model_validate(
        {
            "question": question,
            "covered": True,
            "emergency": True,
            "answer": EMERGENCY_CARD,
            "sources": [],
            "check": None,
        }
    )


def _out(
    question: str, covered: bool, answer: Json | None, sources: list[KnowledgeSource], check: Json | None
) -> KnowledgeAnswer:
    return KnowledgeAnswer.model_validate(
        {"question": question, "covered": covered, "emergency": False, "answer": answer, "sources": sources}
        | {"check": check}
    )


def ask(
    db: Session,
    who: Principal,
    text: str | None,
    audio: bytes | None,
    machine_id: int | None,
    ai: AiRunner,
    now: datetime,
) -> KnowledgeAnswer:
    shop_id, district_id, worker_id = worker_of(who)
    text = text.strip() if text else None
    if not text and not audio:
        raise ApiError(422, "VALIDATION_ERROR", "Send a question or a recording", {"fields": [{"loc": ["body"]}]})
    if text and is_emergency(text):
        return _card(text)
    mime = audio_mime(audio) if audio else None

    languages = ai_languages(db, shop_id)
    me = work_repo.worker(db, shop_id, worker_id)
    spoken = LANGUAGE_NAMES.get(me.language if me else "en", "English")
    terms = [
        AiTerm(id=str(t.id), ja=t.ja, reading=t.reading, translations=t.translations)
        for t in work_repo.glossary(db, shop_id)
    ]
    own: list[Piece] = []
    meta: dict[str, KnowledgeSource] = {}
    for m in work_repo.all_machines(db, shop_id):
        if m.current_version_id is None:
            continue
        note = work_repo.note(db, shop_id, m.current_version_id)
        if note is None:
            continue
        name = m.name.get("en") or m.name.get("ja", m.code)
        texts = [Step.model_validate(s).text for s in note.steps]
        steps = " ".join(f"{x.get('ja', '')} {x.get('en', '')}" for x in texts)
        pid = f"note:{note.id}"
        own.append(Piece(pid, f"{name} {m.name.get('ja', '')} {steps}", boost=3.0 if m.id == machine_id else 1.5))
        meta[pid] = KnowledgeSource(
            id=pid,
            kind="note",
            title=f"{name}: work instruction v{note.version_no}",
            page=None,
            url=None,
            attribution=None,
        )
    for n in life_repo.published_notices(db, district_id, NOTICES_SEARCHED):
        steps = " ".join(f"{s.get('ja', '')} {s.get('en', '')}" for s in n.steps)
        pid = f"notice:{n.id}"
        own.append(Piece(pid, f"{n.title.get('en', '')} {n.title.get('ja', '')} {n.source_ja} {steps}", boost=1.2))
        meta[pid] = KnowledgeSource(
            id=pid,
            kind="notice",
            title=n.title.get("en") or n.title.get("ja", "Notice"),
            page=None,
            url=None,
            attribution=None,
        )
    db.commit()  # nothing is held open during the model calls (§8.3)

    # Japanese or spoken questions are searched in English too: the public guides are the English editions.
    query = text or ""
    if audio and mime:
        english = _english(ai, shop_id, Part.blob(audio, mime), spoken, hashlib.sha256(audio).digest(), now)
        if english is None:
            return _out("", False, None, [], None)
        query = english
    elif any(not ch.isascii() for ch in query) and tokens(query):
        english = _english(ai, shop_id, Part.text(f"<source_text>{query}</source_text>"), spoken, query.encode(), now)
        query = f"{query} {english or ''}"
    if is_emergency(query):
        return _card(query)

    sources, guide_pieces, chunks = _guides()
    found = search(query, [*own, *guide_pieces], k=6)
    if not found:
        return _out(query, False, None, [], None)
    for p in found:
        if p.id in chunks:
            c = chunks[p.id]
            s = sources[c["source"]]
            meta[p.id] = KnowledgeSource(
                id=p.id, kind=s["kind"], title=s["title"], page=c["page"], url=s["url"], attribution=s["attribution"]
            )
    sent = [p.id for p in found]
    block = "\n".join(
        json.dumps({"id": p.id, "title": meta[p.id].title, "text": p.text[:1500]}, ensure_ascii=False) for p in found
    )
    glossary = _glossary_block(terms)

    def problems(d: KnowledgeDraft) -> list[str]:
        out = [] if set(d.source_ids) <= set(sent) else ["source_ids must be ids from the sources list"]
        if d.covered and d.answer is None:
            out.append("A covered answer needs the answer text.")
        if d.answer is not None:
            out += language_problems([d.answer], languages)
            out += glossary_problems(d.answer.ja, [d.answer], terms, languages)
        if d.check is not None:
            out += language_problems(question_texts([d.check]), languages) + question_problems([d.check], terms)
        return out

    request = AiRequest(
        pipeline="answer_knowledge",
        prompt=render(
            "answer_knowledge", PROMPT_VERSION, sources=block, glossary=glossary, languages=", ".join(languages)
        ),
        parts=[Part.text(f"<worker_question>{query}</worker_question>")],
        schema=KnowledgeDraft,
        scope=f"shop:{shop_id}",
        cache_input=f"{query}|{','.join(sent)}".encode()
        + hashlib.sha256((glossary + ",".join(languages)).encode()).digest(),
        languages=languages,
    )
    d = ai.run(request, problems, now).value
    answer = d.answer.only(languages) if d and d.answer else None
    grounded = ground(d.covered if d else False, answer, d.source_ids if d else [], sent, languages)
    if not grounded.covered:
        return _out(query, False, None, [], None)
    check = None
    if d and d.check is not None:
        term_ids = {t.ja: t.id for t in terms}
        check = {
            "id": "q1",
            "termId": term_ids.get(d.check.term_ja or ""),
            "prompt": d.check.prompt.only(languages),
            "options": [o.only(languages) for o in d.check.options],
            "correctIndex": d.check.correct_index,
        }
    cited = [meta[i] for i in grounded.source_notice_ids if i in meta]
    return _out(query, True, dict(grounded.answer or {}), cited, check)
