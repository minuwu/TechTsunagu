import json
import re
from datetime import UTC, datetime
from pathlib import Path
from typing import Any

from pydantic import TypeAdapter

from app.domain.recurrence import validate_event
from app.schemas.common import CheckQuestion, Translated
from app.schemas.notices import NoticeCategory, NoticeSourceKind, PosterOut
from app.schemas.senpai import MeetupKind, SenpaiKind

SEED: dict[str, Any] = json.loads(
    (Path(__file__).parents[2] / "app" / "seed_data" / "life.json").read_text(encoding="utf-8")
)
# Worker keys of the Work seed; the same ids as frontend/src/services/mocks/seed.ts.
WORKERS = {"w-rahim", "w-sadia", "w-tanvir", "w-tanaka"}
DEMO_LANGUAGES = {"ja", "en"}
FINAL_PRESENTATION = datetime(2026, 11, 26, 10, 0, tzinfo=UTC)

translated = TypeAdapter(Translated)


def keys(section: str) -> list[str]:
    return [item["key"] for item in SEED[section]]


def test_keys_are_unique_and_every_reference_resolves() -> None:
    for section in ("posters", "posterUsers", "notices", "senpaiPairs"):
        assert len(keys(section)) == len(set(keys(section))), section
    assert {u["posterKey"] for u in SEED["posterUsers"]} <= set(keys("posters"))
    assert {n["posterKey"] for n in SEED["notices"]} <= set(keys("posters"))
    assert {r["noticeKey"] for r in SEED["receipts"]} <= set(keys("notices"))
    assert {m["pairKey"] for m in SEED["meetups"]} <= set(keys("senpaiPairs"))
    assert {r["workerKey"] for r in SEED["receipts"]} | {p["workerKey"] for p in SEED["senpaiPairs"]} <= WORKERS


def test_district_and_posters_match_the_schema() -> None:
    assert re.fullmatch(r"[A-Z0-9]{8}", SEED["district"]["code"])
    for poster in SEED["posters"]:
        PosterOut.model_validate({"id": "1", "name": poster["name"], "kind": poster["kind"]})


def test_notices_match_the_contract_in_the_demo_shop_languages() -> None:
    for notice in SEED["notices"]:
        TypeAdapter(NoticeCategory).validate_python(notice["category"])
        TypeAdapter(NoticeSourceKind).validate_python(notice["sourceKind"])
        texts = [notice["title"], *notice["steps"], *(event["title"] for event in notice["events"])]
        question = CheckQuestion.model_validate(notice["question"])
        texts += [question.prompt, *question.options]
        for text in texts:
            assert translated.validate_python(text).keys() == DEMO_LANGUAGES, notice["key"]
        assert 1 <= len(notice["steps"]) <= 8


def test_published_notices_have_confirmed_events_still_upcoming_on_presentation_day() -> None:
    for notice in SEED["notices"]:
        assert notice["status"] == "published"
        for event in notice["events"]:
            assert event["confirmed"], event["key"]
            validate_event(datetime.fromisoformat(event["startsAt"]), event["rrule"], FINAL_PRESENTATION)


def test_senpai_pairs_and_meetups_use_known_kinds_and_one_pair_per_worker() -> None:
    pairs = SEED["senpaiPairs"]
    assert len({p["workerKey"] for p in pairs}) == len(pairs)
    for pair in pairs:
        TypeAdapter(SenpaiKind).validate_python(pair["kind"])
    for meetup in SEED["meetups"]:
        TypeAdapter(MeetupKind).validate_python(meetup["kind"])
        assert meetup["loggedBy"] in {"worker", "senpai"}


def test_demo_poster_pins_follow_the_pin_rules() -> None:
    for user in SEED["posterUsers"]:
        pin = user["demoPin"]
        assert re.fullmatch(r"[0-9]{6}", pin), user["key"]
        assert len(set(pin)) > 1, "not all the same digit"
        assert pin not in "0123456789" and pin not in "9876543210", "not a straight run"
