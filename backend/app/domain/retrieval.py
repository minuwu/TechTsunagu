"""Find the source pieces a chatbot question is about (RAG_PLAN.md). Pure, in memory: the corpus is a few hundred
pieces (public guides, the shop's approved Note Versions, the district's Notices), so BM25 over words and Japanese
character pairs is enough and needs no vector store.
shortcut: keyword search only; add embeddings (pgvector on Supabase) when the corpus or paraphrased questions grow."""

import math
import re
import unicodedata
from collections import Counter
from collections.abc import Sequence
from dataclasses import dataclass

_WORD = re.compile(r"[a-z0-9]+")
_CJK = re.compile(r"[぀-ヿ㐀-鿿]+")
_STOP = frozenset(
    "a an and are as at be by can do does for from how i if in is it its my of on or should the to was what when "
    "where which who why will with you your me we our they this that there have has get".split()
)


@dataclass(frozen=True)
class Piece:
    id: str
    text: str
    boost: float = 1.0  # e.g. the Machine the Worker is standing at


def tokens(text: str) -> list[str]:
    """English words (lower case, no stop words, a plural s dropped) and Japanese character pairs."""
    text = unicodedata.normalize("NFKC", text).lower()
    words = [w[:-1] if len(w) > 3 and w.endswith("s") else w for w in _WORD.findall(text) if w not in _STOP]
    pairs = [run[i : i + 2] for run in _CJK.findall(text) for i in range(max(len(run) - 1, 1))]
    return words + pairs


def search(query: str, pieces: Sequence[Piece], k: int = 6, k1: float = 1.4, b: float = 0.75) -> list[Piece]:
    """The k best pieces for the query by BM25 (times each piece's boost); pieces that share no term are left out."""
    q = set(tokens(query))
    if not q or not pieces:
        return []
    docs = [Counter(tokens(p.text)) for p in pieces]
    avg = sum(sum(d.values()) for d in docs) / len(docs)
    df = Counter(t for d in docs for t in q if t in d)
    n = len(docs)
    scored: list[tuple[float, int]] = []
    for i, (piece, d) in enumerate(zip(pieces, docs, strict=True)):
        length = sum(d.values())
        score = 0.0
        for t in q & d.keys():
            idf = math.log(1 + (n - df[t] + 0.5) / (df[t] + 0.5))
            score += idf * d[t] * (k1 + 1) / (d[t] + k1 * (1 - b + b * length / avg))
        if score > 0:
            scored.append((score * piece.boost, i))
    return [pieces[i] for _, i in sorted(scored, reverse=True)[:k]]


# Questions about these are answered by a fixed card, never by the model (RAG_PLAN.md §3).
_EMERGENCY = re.compile(
    r"怪我|けが|ケガ|出血|血が|火事|火災|燃え|ガス漏れ|ガスのにおい|救急|意識がない|倒れ|挟まれ|閉じ込め|"
    r"\binjur|\bbleed|\bblood\b|\bfire\b|\bburning\b|\bsmoke\b|gas leak|smell gas|\bunconscious|collapsed|"
    r"\btrapped\b|\bstuck in\b|\bambulance\b|\b119\b|\bemergency\b"
)


def is_emergency(text: str) -> bool:
    return _EMERGENCY.search(unicodedata.normalize("NFKC", text).lower()) is not None
