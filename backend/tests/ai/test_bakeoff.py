"""The bake-off's scoring (app.ai.bakeoff): one written-down normalisation, CER for Japanese, WER for English."""

from app.ai.bakeoff import cer, normalise, wer


def test_normalisation_drops_spaces_and_punctuation_and_unifies_digits() -> None:
    assert normalise("寸法は、２か所。") == normalise("寸法は 2か所") == "寸法は2か所"
    assert normalise("二トン") == "2トン"


def test_cer_counts_characters_and_wer_counts_words() -> None:
    assert cer("ノギスで2か所測れ", "ノギスで二か所測れ。") == 0
    assert cer("吊り荷", "釣り荷") == 1 / 3
    assert wer("I stopped using it.", "i stopped using it") == 0
    assert wer("The crane is making a noise", "The crane is making noise") == 1 / 6
