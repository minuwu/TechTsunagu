"""Talk Threads (XREQ-018, ADR 0014): pure rules around the two AI pipelines.

`needs_staff` is a fixed rule, never the model: when what was said is about pain, illness, a fall, eating or
breathing, medicine or the toilet, the Worker is told to tell a staff member or nurse (the prototype's flag).
`explanation_problems` is the check the AI runner retries on, like the other pipelines' checks."""

import re
import unicodedata
from collections.abc import Sequence

from app.ai.schemas import UtteranceExplanation

# shortcut: a word list, Japanese (dialect and standard) and English; ask care staff to review it before real use.
_STAFF = re.compile(
    r"痛|いた[かいくけ]|苦し|くるし|息|胸|熱|寒気|吐|めまい|ふらふら|転|こけ|倒れ|血|薬|くすり|食欲|食べられ|"
    r"飲み込|むせ|トイレ|便|おしっこ|しびれ|かゆ|眠れ|"
    r"\bpain\b|\bhurts?\b|\bache|\bsore\b|\bsick\b|\bill\b|\bfever\b|\bbreath|\bchest\b|\bdizzy|\bvomit|\bfell\b|"
    r"\bfall\b|\bfallen\b|\bblood|\bbleed|\bmedicine|\bpills?\b|\bappetite\b|\bcan't eat\b|\bcannot eat\b|\bchok|"
    r"\btoilet\b|\bnumb"
)
_JAPANESE = re.compile(r"[぀-ヿ㐀-鿿]")


def needs_staff(texts: Sequence[str]) -> bool:
    """True when any text is about health or safety, so the Worker should tell a staff member or nurse."""
    return any(_STAFF.search(unicodedata.normalize("NFKC", t).lower()) for t in texts if t)


def explanation_problems(e: UtteranceExplanation, heard: str | None) -> list[str]:
    """What is wrong with an explanation; empty means accept it. `heard` is the typed text (None for audio)."""
    out: list[str] = []
    if not _JAPANESE.search(e.plain_ja):
        out.append("plain_ja must be Japanese.")
    if any(not _JAPANESE.search(r.ja) for r in e.reply_options):
        out.append("Every reply option's ja must be Japanese.")
    said = heard or e.transcript
    invented = [t.dialect for t in e.terms if t.dialect not in said]
    if invented:
        out.append(f"terms must be words from what was said; not found: {', '.join(invented)}")
    return out
