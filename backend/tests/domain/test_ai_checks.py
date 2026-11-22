import json
from pathlib import Path
from typing import Any

import pytest
from pydantic import ValidationError

from app.ai.schemas import AiQuestion, AiStep, AiTri, BackTranslation, NoteDraft
from app.domain.ai_checks import GlossaryTerm, glossary_problems, note_problems, question_problems, terms_in_scope

FIXTURES = Path(__file__).parents[1] / "fixtures" / "ai"
LANGS = ("bn", "en")


def load_glossary() -> list[GlossaryTerm]:
    raw: list[dict[str, Any]] = json.loads((FIXTURES / "glossary.json").read_text(encoding="utf-8"))["terms"]
    return [GlossaryTerm(t["id"], t["ja"], t["reading"], t["translations"]) for t in raw]


GLOSSARY = load_glossary()


def tri(ja: str, bn: str, en: str) -> AiTri:
    return AiTri(ja=ja, bn=bn, en=en)


def question(term_ja: str | None = "寸法", options: list[AiTri] | None = None) -> AiQuestion:
    return AiQuestion(
        term_ja=term_ja,
        prompt=tri("寸法は何か所測りますか？", "কত জায়গায় মাপ (সুনপো) নেবেন?", "How many points?"),
        options=options
        or [tri("1か所", "১ জায়গায়", "1 point"), tri("2か所", "২ জায়গায়", "2 points"), tri("測らない", "মাপব না", "None")],
        correct_index=1,
    )


# The demo plasma-cutter note from frontend/src/services/mocks/ai.ts, shortened to two steps.
TRANSCRIPT = "いいか、保護メガネ。寸法はプラスマイナス1ミリ、ノギスで2か所測れ。"
GOOD_STEPS = [
    AiStep(text=tri("保護メガネをかけます。", "সুরক্ষা চশমা পরুন।", "Put on safety glasses."), icon="ppe-goggles"),
    AiStep(
        text=tri(
            "ノギスで寸法を2か所測ります。",
            "ক্যালিপার দিয়ে ২ জায়গায় মাপ (সুনপো) নিন।",
            "Measure the Dimensions (sunpō) at 2 points.",
        ),
        icon="measure",
    ),
]


def note(steps: list[AiStep], questions: list[AiQuestion] | None = None) -> NoteDraft:
    return NoteDraft(
        transcript_ja=TRANSCRIPT,
        steps=steps,
        back_translation=BackTranslation(bn="保護メガネをかける。", en="保護メガネ着用。"),
        questions=questions or [question(), question(term_ja="保護メガネ")],
    )


def test_glossary_fixture_matches_the_seed() -> None:
    assert len(GLOSSARY) == 15
    assert all(t.translations["bn"] and t.translations["en"] for t in GLOSSARY)


def test_passing_note_has_no_problems() -> None:
    # "Dimensions" capitalised on purpose: English matches case-insensitively.
    assert note_problems(note(GOOD_STEPS), GLOSSARY, LANGS) == []


def test_missing_locked_term_is_reported() -> None:
    no_term = tri("ノギスで2か所測ります。", "২ জায়গায় মাপুন।", "Measure at 2 points.")
    steps = [GOOD_STEPS[0], AiStep(text=no_term, icon=None)]
    problems = note_problems(note(steps), GLOSSARY, LANGS)
    assert any("「寸法」" in p and "Japanese" in p for p in problems)
    assert any("মাপ (সুনপো)" in p for p in problems)
    assert any("dimensions (sunpō)" in p for p in problems)


def test_two_correct_options_are_reported() -> None:
    same = [tri("2か所", "২ জায়গায়", "2 points"), tri("2か所", "২ জায়গায়", "2 points"), tri("測らない", "মাপব না", "None")]
    problems = question_problems([question(options=same)], GLOSSARY)
    assert {p for p in problems if "same" in p} == {
        "Question 1: two options have the same ja text.",
        "Question 1: two options have the same bn text.",
        "Question 1: two options have the same en text.",
    }


def test_term_heard_only_as_reading_is_still_in_scope() -> None:
    scoped = terms_in_scope("たまかけの前に確認", GLOSSARY)
    assert [t.ja for t in scoped] == ["玉掛け"]
    problems = glossary_problems("たまかけの前に確認", [tri("確認します。", "যাচাই করুন।", "Check.")], GLOSSARY, LANGS)
    assert any("「玉掛け」" in p for p in problems)


def test_unknown_term_in_question_is_reported() -> None:
    assert question_problems([question(term_ja="溶接")], GLOSSARY) == [
        "Question 1: term_ja 「溶接」 is not a Glossary Term; use one or null."
    ]


def test_schema_rejects_unknown_icon_and_wrong_counts() -> None:
    with pytest.raises(ValidationError):
        AiStep.model_validate({"text": {"ja": "a", "bn": "b", "en": "c"}, "icon": "smile"})
    with pytest.raises(ValidationError):
        AiQuestion.model_validate({**question().model_dump(), "options": question().model_dump()["options"][:2]})
    with pytest.raises(ValidationError):
        note(GOOD_STEPS, questions=[question()])  # a Note Version needs 2–3 questions
