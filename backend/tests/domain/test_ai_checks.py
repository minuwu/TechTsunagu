import json
from pathlib import Path
from typing import Any

import pytest
from pydantic import ValidationError

from app.ai.schemas import (
    AiQuestion,
    AiStep,
    AiTaskDraft,
    AiTri,
    BackTranslation,
    MessageDraft,
    NoteDraft,
    Reexplanation,
)
from app.domain.ai_checks import (
    GlossaryTerm,
    added_machine_names,
    glossary_problems,
    language_problems,
    message_problems,
    note_problems,
    question_problems,
    reexplain_problems,
    task_problems,
    terms_in_scope,
    translation_problems,
)

FIXTURES = Path(__file__).parents[1] / "fixtures" / "ai"
SEED_DATA = Path(__file__).parents[2] / "app" / "seed_data"  # the glossary is shared with the demo seed
LANGS = ("en",)


def load_glossary() -> list[GlossaryTerm]:
    raw: list[dict[str, Any]] = json.loads((SEED_DATA / "glossary.json").read_text(encoding="utf-8"))["terms"]
    return [GlossaryTerm(t["id"], t["ja"], t["reading"], t["translations"]) for t in raw]


GLOSSARY = load_glossary()


def tri(ja: str, en: str) -> AiTri:
    return AiTri(ja=ja, en=en)


def question(term_ja: str | None = "寸法", options: list[AiTri] | None = None) -> AiQuestion:
    return AiQuestion(
        term_ja=term_ja,
        prompt=tri("寸法は何か所測りますか？", "How many points?"),
        options=options or [tri("1か所", "1 point"), tri("2か所", "2 points"), tri("測らない", "None")],
        correct_index=1,
    )


# The demo plasma-cutter note from frontend/src/services/mocks/ai.ts, shortened to two steps.
TRANSCRIPT = "いいか、保護メガネ。寸法はプラスマイナス1ミリ、ノギスで2か所測れ。"
GOOD_STEPS = [
    AiStep(text=tri("保護メガネをかけます。", "Put on safety glasses."), icon="ppe-goggles"),
    AiStep(
        text=tri(
            "ノギスで寸法を2か所測ります。",
            "Measure the Dimensions (sunpō) at 2 points.",
        ),
        icon="measure",
    ),
]


def note(steps: list[AiStep], questions: list[AiQuestion] | None = None) -> NoteDraft:
    return NoteDraft(
        transcript_ja=TRANSCRIPT,
        steps=steps,
        back_translation=BackTranslation(en="保護メガネ着用。"),
        questions=questions or [question(), question(term_ja="保護メガネ")],
    )


def test_glossary_fixture_matches_the_seed() -> None:
    assert len(GLOSSARY) == 15
    assert all(t.translations["en"] and "bn" not in t.translations for t in GLOSSARY)  # no Bangla (ADR 0016)


def test_passing_note_has_no_problems() -> None:
    # "Dimensions" capitalised on purpose: English matches case-insensitively.
    assert note_problems(note(GOOD_STEPS), GLOSSARY, LANGS) == []


def test_missing_locked_term_is_reported() -> None:
    no_term = tri("ノギスで2か所測ります。", "Measure at 2 points.")
    steps = [GOOD_STEPS[0], AiStep(text=no_term, icon=None)]
    problems = note_problems(note(steps), GLOSSARY, LANGS)
    assert any("「寸法」" in p and "Japanese" in p for p in problems)
    assert any("dimensions (sunpō)" in p for p in problems)


def test_two_correct_options_are_reported() -> None:
    same = [tri("2か所", "2 points"), tri("2か所", "2 points"), tri("測らない", "None")]
    problems = question_problems([question(options=same)], GLOSSARY)
    assert {p for p in problems if "same" in p} == {
        "Question 1: two options have the same ja text.",
        "Question 1: two options have the same en text.",
    }


def test_term_heard_only_as_reading_is_still_in_scope() -> None:
    scoped = terms_in_scope("たまかけの前に確認", GLOSSARY)
    assert [t.ja for t in scoped] == ["玉掛け"]
    problems = glossary_problems("たまかけの前に確認", [tri("確認します。", "Check.")], GLOSSARY, LANGS)
    assert any("「玉掛け」" in p for p in problems)


def test_unknown_term_in_question_is_reported() -> None:
    assert question_problems([question(term_ja="溶接")], GLOSSARY) == [
        "Question 1: term_ja 「溶接」 is not a Glossary Term; use one or null."
    ]


def test_schema_rejects_unknown_icon_and_wrong_counts() -> None:
    with pytest.raises(ValidationError):
        AiStep.model_validate({"text": {"ja": "a", "en": "c"}, "icon": "smile"})
    with pytest.raises(ValidationError):
        AiQuestion.model_validate({**question().model_dump(), "options": question().model_dump()["options"][:2]})
    with pytest.raises(ValidationError):
        note(GOOD_STEPS, questions=[question()])  # a Note Version needs 2–3 questions


# Chat (XREQ-006): a die-change message like the prototype's, with seed-like ids.
MSG = "明日の朝8時から、2号プレスで段取りをします。保護メガネを忘れずに。"
MEMBERS, MACHINES = {"12", "13"}, {"2"}
MSG_TEXT = tri(
    "明日の朝8時から、2号プレスで段取りをします。保護メガネをかけます。",
    "Tomorrow from 8:00, set-up (dandori) on Press #2. Wear safety glasses.",
)


def task(
    assignees: list[str] | None = None, due: str | None = "2026-11-24T08:00:00+09:00", machine: str | None = "2"
) -> AiTaskDraft:
    return AiTaskDraft(
        title=tri("2号プレス 段取り", "Set-up · Press #2"),
        assignee_ids=["12"] if assignees is None else assignees,
        due_at=due,
        machine_id=machine,
    )


def message(tasks: list[AiTaskDraft], text: AiTri = MSG_TEXT) -> MessageDraft:
    return MessageDraft(transcript_ja=MSG, text=text, questions=[question(term_ja="段取り")], tasks=tasks)


def test_passing_message_has_no_problems_and_zero_tasks_is_fine() -> None:
    assert message_problems(message([task()]), GLOSSARY, LANGS, MEMBERS, MACHINES) == []
    assert message_problems(message([]), GLOSSARY, LANGS, MEMBERS, MACHINES) == []


def test_message_translation_must_keep_the_shop_words() -> None:
    loose = tri(MSG_TEXT.ja, "Tomorrow, work on Press #2.")
    problems = message_problems(message([], text=loose), GLOSSARY, LANGS, MEMBERS, MACHINES)
    assert any("set-up (dandori)" in p for p in problems)
    assert any("safety glasses" in p for p in problems)


def test_task_must_use_listed_members_and_machines() -> None:
    assert task_problems([task(assignees=["12", "99"], machine="7")], MSG, MEMBERS, MACHINES) == [
        "Task 1: 99 is not a Thread member.",
        "Task 1: machine 7 is not a Machine of this shop; use one or null.",
    ]


def test_a_due_time_is_never_guessed() -> None:
    no_time = "2号プレスで段取りをします。"
    assert task_problems([task()], no_time, MEMBERS, MACHINES) == [
        "Task 1: the Foreman said no day or time, so due_at must be null."
    ]
    assert task_problems([task(due=None)], no_time, MEMBERS, MACHINES) == []
    assert task_problems([task(due="2026-11-24T08:00:00")], MSG, MEMBERS, MACHINES) == [
        "Task 1: due_at needs the +09:00 offset."
    ]


def test_reexplanation_question_must_be_on_the_missed_term() -> None:
    text = tri(
        "段取りは、機械の準備です。",
        "Set-up (dandori) means getting the machine ready.",
    )
    assert (
        reexplain_problems(Reexplanation(text=text, question=question(term_ja="段取り")), "段取り", GLOSSARY, LANGS)
        == []
    )
    off = Reexplanation(text=text, question=question(term_ja=None))
    assert reexplain_problems(off, "段取り", GLOSSARY, LANGS) == [
        "The question must be about 「段取り」: set term_ja to it."
    ]


def test_a_term_outside_the_glossary_can_be_re_explained() -> None:
    sentence = "今日は3号機の油の点検を先にしてください。"
    text = tri("まず、3号機の油を見てください。", "First, check the oil of machine 3.")
    for term in ("バリ取り", sentence):
        assert (
            reexplain_problems(Reexplanation(text=text, question=question(term_ja=term)), term, GLOSSARY, LANGS) == []
        )


# Per-shop languages (XREQ-011 item 5): a shop that uses only English must get English.
def test_an_english_only_shop_needs_english() -> None:
    en_only = AiTri(ja="保護メガネをかけます。", en="Put on safety glasses.")
    no_en = AiTri(ja="保護メガネをかけます。")

    assert language_problems([en_only], ("en",)) == []
    assert language_problems([no_en], ("en",)) == ["Write every text in en as well (the shop's languages: en)."]
    assert en_only.only(("en",)) == {"ja": "保護メガネをかけます。", "en": "Put on safety glasses."}


def test_question_options_are_compared_only_in_the_languages_they_are_written_in() -> None:
    en_only = [AiTri(ja=j, en=e) for j, e in (("1か所", "1 point"), ("2か所", "2 points"), ("測らない", "None"))]
    q = AiQuestion(term_ja=None, prompt=AiTri(ja="何か所?", en="How many?"), options=en_only, correct_index=1)

    assert question_problems([q], GLOSSARY) == []


@pytest.mark.parametrize(
    ("source", "ja", "ok"),
    [
        ("The set-up of press 2 is done", "2号プレスの準備が終わりました", False),  # 「段取り」 lost
        ("The set-up of press 2 is done", "2号プレスの段取りが終わりました", True),
        ("dandori finished", "終わりました", False),  # the romaji counts too
        ("My burry glove", "手袋", True),  # whole words only in Latin script
    ],
)
def test_a_translation_into_japanese_keeps_the_shop_word(source: str, ja: str, ok: bool) -> None:
    assert (translation_problems(source, ja, GLOSSARY, "en") == []) is ok


def test_a_machine_name_the_foreman_never_said_is_rejected() -> None:
    """Audit A4: "Check the oil on machine 3" came back as "overhead crane (slinging) #3"."""
    names = [("天井クレーン（玉掛け）3号", "Overhead crane (slinging) #3"), ("2号プレス", "Press #2")]
    swapped = tri("天井クレーン（玉掛け）3号の油を点検してください。", "Check the oil on overhead crane (slinging) #3.")
    kept = tri("3号機の油を点検してください。", "Check the oil on machine 3.")

    assert added_machine_names("Check the oil on machine 3", swapped, names) == ["天井クレーン（玉掛け）3号"]
    assert added_machine_names("Check the oil on machine 3", kept, names) == []
    assert added_machine_names("2号プレスの段取り", tri("2号プレスの段取り", "Set-up on Press #2"), names) == []


def test_a_greeting_may_have_no_questions() -> None:
    hello = MessageDraft(transcript_ja="こんにちは", text=tri("こんにちは", "Hello"), questions=[], tasks=[])
    assert message_problems(hello, GLOSSARY, LANGS, MEMBERS, MACHINES) == []
