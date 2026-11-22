from app.domain.grounding import NOT_COVERED, GroundedAnswer, ground

SENT = ["11", "12", "13"]
LANGUAGES = ["bn", "en"]
ANSWER = {"ja": "扇風機は粗大ごみです。", "bn": "ফ্যান বড় আকারের ময়লা।", "en": "A fan is oversized garbage."}


def test_answer_citing_notices_that_were_sent_is_kept() -> None:
    result = ground(True, {**ANSWER, "zh": "extra"}, ["12", "11", "12"], SENT, LANGUAGES)

    assert result == GroundedAnswer(covered=True, answer=ANSWER, source_notice_ids=("12", "11"))


def test_covered_answer_without_sources_is_not_covered() -> None:
    assert ground(True, ANSWER, [], SENT, LANGUAGES) == NOT_COVERED


def test_answer_citing_a_notice_that_was_not_sent_is_not_covered() -> None:
    assert ground(True, ANSWER, ["11", "999"], SENT, LANGUAGES) == NOT_COVERED


def test_answer_missing_a_language_is_not_covered() -> None:
    assert ground(True, {**ANSWER, "bn": "  "}, ["11"], SENT, LANGUAGES) == NOT_COVERED
    assert ground(True, {"ja": ANSWER["ja"], "en": ANSWER["en"]}, ["11"], SENT, LANGUAGES) == NOT_COVERED


def test_model_saying_not_covered_or_giving_no_answer_is_not_covered() -> None:
    assert ground(False, ANSWER, ["11"], SENT, LANGUAGES) == NOT_COVERED
    assert ground(True, None, ["11"], SENT, LANGUAGES) == NOT_COVERED


def test_not_covered_matches_the_frontend_shape() -> None:
    assert NOT_COVERED == GroundedAnswer(covered=False, answer=None, source_notice_ids=())
