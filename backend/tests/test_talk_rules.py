"""Talk Threads' fixed rules (app.domain.talk): never the model."""

from app.ai.schemas import UtteranceExplanation
from app.domain.talk import explanation_problems, needs_staff


def test_health_words_need_staff_in_dialect_japanese_and_english() -> None:
    assert needs_staff(["膝のいたかけん"])
    assert needs_staff(["今日はあんまり食欲のなかと"])
    assert needs_staff(["", "She says her chest hurts"])
    assert not needs_staff(["お茶ばもう一杯ちょうだい。", "Please give me one more cup of tea."])


def _explanation(**over: object) -> UtteranceExplanation:
    data: dict[str, object] = {
        "transcript": "お茶ばもう一杯ちょうだい。",
        "plain_ja": "お茶をもう一杯ください。",
        "meaning": "One more cup of tea, please.",
        "terms": [{"dialect": "お茶ば", "plain": "お茶を", "meaning": "tea (object)"}],
        "reply_options": [
            {"ja": "はい、すぐに。", "meaning": "Yes, right away."},
            {"ja": "温かいお茶ですか？", "meaning": "Hot tea?"},
        ],
    }
    return UtteranceExplanation.model_validate(data | over)


def test_an_explanation_must_be_japanese_and_quote_real_words() -> None:
    assert explanation_problems(_explanation(), "お茶ばもう一杯ちょうだい。") == []
    assert explanation_problems(_explanation(plain_ja="One more tea"), None) == ["plain_ja must be Japanese."]
    invented = _explanation(terms=[{"dialect": "ばってん", "plain": "でも", "meaning": "but"}])
    assert explanation_problems(invented, None)[0].startswith("terms must be words from what was said")
