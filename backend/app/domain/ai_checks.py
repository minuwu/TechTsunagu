"""Checks on AI drafts before they are stored (architecture §7.4). Pure: each returns a list of problems in plain
English, empty when the draft passes, so a pipeline can append them to its one retry prompt."""

import re
import unicodedata
from collections.abc import Collection, Mapping, Sequence
from dataclasses import dataclass
from datetime import datetime

from app.ai.schemas import AiQuestion, AiTaskDraft, AiTri, MessageDraft, NoteDraft, Reexplanation

# Latin-script languages compare case-insensitively (§7.4); Bengali and Japanese have no case.
LATIN = frozenset({"en", "vi", "id", "tl"})


@dataclass(frozen=True)
class GlossaryTerm:
    id: str
    ja: str
    reading: str
    translations: Mapping[str, str]  # language -> fixed translation, one per shop language


def _nfkc(text: str) -> str:
    return unicodedata.normalize("NFKC", text)


def _contains(text: str, part: str, lang: str) -> bool:
    text, part = unicodedata.normalize("NFC", text), unicodedata.normalize("NFC", part)
    return part.casefold() in text.casefold() if lang in LATIN else part in text


def terms_in_scope(transcript_ja: str, glossary: Sequence[GlossaryTerm]) -> list[GlossaryTerm]:
    """A term is in scope when its ja or its reading appears in the transcript (Galib's review, point 4)."""
    transcript = _nfkc(transcript_ja)
    return [t for t in glossary if _nfkc(t.ja) in transcript or _nfkc(t.reading) in transcript]


def glossary_problems(
    transcript_ja: str, texts: Sequence[AiTri], glossary: Sequence[GlossaryTerm], languages: Collection[str]
) -> list[str]:
    """Every in-scope term keeps its ja form in the やさしい日本語 text and its fixed translation in every shop
    language."""
    easy_ja = _nfkc("\n".join(t.ja for t in texts))
    problems: list[str] = []
    for term in terms_in_scope(transcript_ja, glossary):
        if _nfkc(term.ja) not in easy_ja:
            problems.append(f"The Japanese steps must keep the shop word 「{term.ja}」 as written.")
        for lang in languages:
            joined = "\n".join(getattr(t, lang) or "" for t in texts)
            fixed = term.translations.get(lang, "")
            if fixed and not _contains(joined, fixed, lang):
                problems.append(f"The {lang} text must use the fixed translation 「{fixed}」 for 「{term.ja}」.")
    return problems


def _mentions(text: str, word: str, lang: str) -> bool:
    if lang in LATIN:  # whole words: "burr" is not in "burry"
        return re.search(rf"\b{re.escape(word)}\b", text, re.IGNORECASE) is not None
    return _contains(text, word, lang)


def translation_problems(source: str, ja: str, glossary: Sequence[GlossaryTerm], lang: str) -> list[str]:
    """A Worker's message translated into Japanese (XREQ-007): when the source names a shop word by its fixed
    translation or its romaji (「set-up (dandori)」 → "set-up" or "dandori"), the Japanese uses the shop's own word."""
    problems: list[str] = []
    for term in glossary:
        fixed = term.translations.get(lang, "")
        words = [w.strip(" )") for w in fixed.split(" (")] if fixed else []
        if any(w and _mentions(source, w, lang) for w in words) and _nfkc(term.ja) not in _nfkc(ja):
            problems.append(f"Translate 「{fixed}」 as the shop word 「{term.ja}」.")
    return problems


def language_problems(texts: Sequence[AiTri], languages: Collection[str]) -> list[str]:
    """Every text is written in ja and in each of the shop's languages (shops.languages, XREQ-011 item 5)."""
    missing = sorted({lang for t in texts for lang in languages if not (getattr(t, lang) or "").strip()})
    return [f"Write every text in {lang} as well (the shop's languages: {', '.join(languages)})." for lang in missing]


def question_texts(questions: Sequence[AiQuestion]) -> list[AiTri]:
    return [t for q in questions for t in (q.prompt, *q.options)]


def question_problems(questions: Sequence[AiQuestion], glossary: Sequence[GlossaryTerm]) -> list[str]:
    """Option count and correct_index are enforced by the schema; this catches what a schema cannot."""
    known = {_nfkc(t.ja) for t in glossary}
    problems: list[str] = []
    for n, q in enumerate(questions, start=1):
        # Two options with the same text would both be "correct"; check every language they are written in.
        for lang in AiTri.model_fields:
            values: list[str | None] = [getattr(o, lang) for o in q.options]
            if None in values:
                continue
            texts = [_nfkc(v or "").strip() for v in values]
            if len(set(texts)) < len(texts):
                problems.append(f"Question {n}: two options have the same {lang} text.")
        if q.term_ja is not None and _nfkc(q.term_ja) not in known:
            problems.append(f"Question {n}: term_ja 「{q.term_ja}」 is not a Glossary Term; use one or null.")
    return problems


# A due time needs words like these in the transcript; with none, the model invented it (XREQ-006 item 2).
# shortcut: a word check, not a date parser; it catches a time made up from nothing, not a wrong hour.
_TIME_WORDS = re.compile(
    r"[0-9一二三四五六七八九十]+\s*(時|分|日)|[月火水木金土日]曜|明日|あした|明後日|今日|きょう|今夜|今晩|午前|午後|朝|昼|夕方|夜|まで|来週|今週"
)


def task_problems(
    tasks: Sequence[AiTaskDraft], transcript_ja: str, member_ids: Collection[str], machine_ids: Collection[str]
) -> list[str]:
    problems: list[str] = []
    said_a_time = _TIME_WORDS.search(_nfkc(transcript_ja)) is not None
    for n, task in enumerate(tasks, start=1):
        problems += [f"Task {n}: {a} is not a Thread member." for a in task.assignee_ids if a not in member_ids]
        if task.machine_id is not None and task.machine_id not in machine_ids:
            problems.append(f"Task {n}: machine {task.machine_id} is not a Machine of this shop; use one or null.")
        if task.due_at is None:
            continue
        if not said_a_time:
            problems.append(f"Task {n}: the Foreman said no day or time, so due_at must be null.")
            continue
        try:
            if datetime.fromisoformat(task.due_at).tzinfo is None:
                problems.append(f"Task {n}: due_at needs the +09:00 offset.")
        except ValueError:
            problems.append(f"Task {n}: due_at must be ISO 8601, like 2026-11-24T08:00:00+09:00.")
    return problems


def message_problems(
    draft: MessageDraft,
    glossary: Sequence[GlossaryTerm],
    languages: Collection[str],
    member_ids: Collection[str],
    machine_ids: Collection[str],
) -> list[str]:
    """A Chat message: shop words locked in every language, valid questions, Tasks only for real people and
    Machines, and a due time only when one was said."""
    texts = [draft.text, *question_texts(draft.questions), *(t.title for t in draft.tasks)]
    return (
        language_problems(texts, languages)
        + glossary_problems(draft.transcript_ja, [draft.text], glossary, languages)
        + question_problems(draft.questions, glossary)
        + task_problems(draft.tasks, draft.transcript_ja, member_ids, machine_ids)
    )


def reexplain_problems(
    draft: Reexplanation, term_ja: str, glossary: Sequence[GlossaryTerm], languages: Collection[str]
) -> list[str]:
    """The new explanation keeps the missed term and its fixed translations, and the question is about that term."""
    term = next((t for t in glossary if t.ja == term_ja), GlossaryTerm(id="", ja=term_ja, reading="", translations={}))
    problems = language_problems([draft.text, *question_texts([draft.question])], languages)
    # A term outside the Glossary (a word, or the whole message when the Worker named none) is not kept verbatim, and
    # the question may name it: otherwise "term_ja must be the term" and "term_ja must be a Glossary Term" never agree.
    problems += glossary_problems(term.ja, [draft.text], [term] if term.id else [], languages)
    problems += question_problems([draft.question], [*glossary, term])
    if draft.question.term_ja is None or _nfkc(draft.question.term_ja) != _nfkc(term_ja):
        problems.append(f"The question must be about 「{term_ja}」: set term_ja to it.")
    return problems


def note_problems(draft: NoteDraft, glossary: Sequence[GlossaryTerm], languages: Collection[str]) -> list[str]:
    texts = [s.text for s in draft.steps]
    back = [f"The back_translation needs {lang}." for lang in languages if not getattr(draft.back_translation, lang)]
    return (
        language_problems([*texts, *question_texts(draft.questions)], languages)
        + back
        + glossary_problems(draft.transcript_ja, texts, glossary, languages)
        + question_problems(draft.questions, glossary)
    )
