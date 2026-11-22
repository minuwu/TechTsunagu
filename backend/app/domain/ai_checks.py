"""Checks on AI drafts before they are stored (architecture §7.4). Pure: each returns a list of problems in plain
English, empty when the draft passes, so a pipeline can append them to its one retry prompt."""

import unicodedata
from collections.abc import Collection, Mapping, Sequence
from dataclasses import dataclass

from app.ai.schemas import AiQuestion, AiTri, NoteDraft

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
            joined = "\n".join(getattr(t, lang) for t in texts)
            fixed = term.translations.get(lang, "")
            if fixed and not _contains(joined, fixed, lang):
                problems.append(f"The {lang} text must use the fixed translation 「{fixed}」 for 「{term.ja}」.")
    return problems


def question_problems(questions: Sequence[AiQuestion], glossary: Sequence[GlossaryTerm]) -> list[str]:
    """Option count and correct_index are enforced by the schema; this catches what a schema cannot."""
    known = {_nfkc(t.ja) for t in glossary}
    problems: list[str] = []
    for n, q in enumerate(questions, start=1):
        # Two options with the same text would both be "correct"; check every language, not just ja.
        for lang in ("ja", "bn", "en"):
            texts = [_nfkc(getattr(o, lang)).strip() for o in q.options]
            if len(set(texts)) < len(texts):
                problems.append(f"Question {n}: two options have the same {lang} text.")
        if q.term_ja is not None and _nfkc(q.term_ja) not in known:
            problems.append(f"Question {n}: term_ja 「{q.term_ja}」 is not a Glossary Term; use one or null.")
    return problems


def note_problems(draft: NoteDraft, glossary: Sequence[GlossaryTerm], languages: Collection[str]) -> list[str]:
    texts = [s.text for s in draft.steps]
    return glossary_problems(draft.transcript_ja, texts, glossary, languages) + question_problems(
        draft.questions, glossary
    )
