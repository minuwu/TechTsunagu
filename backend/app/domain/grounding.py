"""Ask grounding (architecture §7.6): an answer counts only when the Notices it cites back it. Pure."""

from collections.abc import Collection, Mapping, Sequence
from dataclasses import dataclass


@dataclass(frozen=True)
class GroundedAnswer:
    covered: bool
    answer: Mapping[str, str] | None
    source_notice_ids: tuple[str, ...]


# The frontend shows its own "no posted notice covers this" text when answer is null.
NOT_COVERED = GroundedAnswer(covered=False, answer=None, source_notice_ids=())


def ground(
    covered: bool,
    answer: Mapping[str, str] | None,
    source_notice_ids: Sequence[str],
    sent_notice_ids: Collection[str],
    languages: Collection[str],
) -> GroundedAnswer:
    """Keep the model's answer only if it cites Notices that were sent to it and has text in ja and every
    shop language; anything else is not covered, so an Ask never shows a guessed rule."""
    sources = tuple(dict.fromkeys(source_notice_ids))
    if not covered or answer is None or not sources or not set(sources) <= set(sent_notice_ids):
        return NOT_COVERED
    text = {lang: answer.get(lang, "").strip() for lang in ("ja", *languages)}
    if not all(text.values()):
        return NOT_COVERED
    return GroundedAnswer(covered=True, answer=text, source_notice_ids=sources)
