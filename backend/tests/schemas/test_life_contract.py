import json
from datetime import UTC, date, datetime
from typing import Any

import pytest
from fastapi import UploadFile
from pydantic import BaseModel, ValidationError

from app.schemas.asks import AskForm, AskOut
from app.schemas.common import CheckQuestion, DateRange, IdIn
from app.schemas.notices import EventsQuery, EventUpdate, NoticeDraftForm, NoticeForWorker, NoticeUpdate
from app.schemas.senpai import MeetupCreate, SenpaiPairCreate, SenpaiPairCreated, SenpaiPairOut, SenpaiView

TRI = {"ja": "燃えるごみ", "bn": "দাহ্য ময়লা", "en": "Burnable garbage"}
AT = datetime(2026, 11, 23, tzinfo=UTC)
QUESTION = {"id": "q1", "termId": None, "prompt": TRI, "options": [TRI, TRI, TRI]}


def worker_notice() -> dict[str, Any]:
    return {
        "id": "7",
        "posterId": "2",
        "category": "garbage",
        "sourceKind": "photo",
        "sourceJa": "燃えるごみは月・木",
        "photo": "/api/blobs/3",
        "title": TRI,
        "steps": [TRI],
        "events": [],
        "status": "published",
        "needsEdit": False,
        "createdAt": AT,
        "publishedAt": AT,
        "question": QUESTION,
        "opened": True,
        "passed": None,
    }


def upload() -> UploadFile:
    return UploadFile(file=None, filename="x")  # type: ignore[arg-type]  # only presence matters here


def test_responses_use_camel_case_and_keep_null_fields() -> None:
    dumped = NoticeForWorker.model_validate(worker_notice()).model_dump(by_alias=True)

    assert {"posterId", "sourceJa", "needsEdit", "publishedAt"} <= dumped.keys()
    assert dumped["passed"] is None
    assert not any("_" in key for key in dumped)


@pytest.mark.parametrize("model", [NoticeForWorker, AskOut, SenpaiView])
def test_worker_and_senpai_responses_never_carry_the_answer(model: type[BaseModel]) -> None:
    schema = json.dumps(model.model_json_schema(mode="serialization"))

    assert "correctIndex" not in schema


def test_worker_notice_rejects_a_question_with_the_answer() -> None:
    with pytest.raises(ValidationError):
        NoticeForWorker.model_validate({**worker_notice(), "question": {**QUESTION, "correctIndex": 0}})


@pytest.mark.parametrize(
    ("model", "body"),
    [
        (MeetupCreate, {"kind": "coffee", "date": "2026-11-23", "pairId": "1"}),
        (MeetupCreate, {"kind": "coffee", "date": "2026-11-23", "loggedBy": "worker"}),
        (NoticeUpdate, {"status": "published"}),
        (SenpaiPairCreate, {"workerId": "1", "senpaiName": "田中 健", "kind": "coworker", "token": "x"}),
    ],
)
def test_fields_the_backend_takes_from_the_session_are_rejected(model: type[BaseModel], body: dict[str, Any]) -> None:
    with pytest.raises(ValidationError, match="extra_forbidden|Extra inputs"):
        model.model_validate(body)


@pytest.mark.parametrize(
    "text",
    [{"bn": "x", "en": "y"}, {"ja": "x", "bn": " "}, {"ja": "x", "fr": "y"}, {"ja": "x" * 1001}],
)
def test_translated_text_needs_japanese_and_text_in_every_language(text: dict[str, str]) -> None:
    with pytest.raises(ValidationError):
        NoticeUpdate.model_validate({"title": text})


def test_check_question_has_exactly_three_options_and_a_valid_answer() -> None:
    CheckQuestion.model_validate({**QUESTION, "correctIndex": 2})
    for bad in [{**QUESTION, "correctIndex": 3}, {**QUESTION, "options": [TRI, TRI], "correctIndex": 0}]:
        with pytest.raises(ValidationError):
            CheckQuestion.model_validate(bad)


@pytest.mark.parametrize("value", ["abc", "0", "-1", "1" * 20, ""])
def test_ids_must_be_positive_integers(value: str) -> None:
    class Body(BaseModel):
        id: IdIn

    with pytest.raises(ValidationError):
        Body(id=value)


def test_date_ranges_use_from_and_to_and_must_be_ordered() -> None:
    assert DateRange.model_validate({"from": "2026-11-22", "to": "2026-11-30"}).from_ == date(2026, 11, 22)
    with pytest.raises(ValidationError):
        DateRange.model_validate({"from": "2026-11-30", "to": "2026-11-22"})


def test_events_query_allows_at_most_62_days() -> None:
    EventsQuery.model_validate({"from": "2026-11-01", "to": "2027-01-02"})
    with pytest.raises(ValidationError):
        EventsQuery.model_validate({"from": "2026-11-01", "to": "2027-01-03"})


def test_event_update_tells_a_missing_rrule_from_a_null_one() -> None:
    assert "rrule" not in EventUpdate.model_validate({"confirmed": True}).model_fields_set
    assert "rrule" in EventUpdate.model_validate({"confirmed": True, "rrule": None}).model_fields_set


def test_notice_draft_needs_exactly_the_source_its_kind_names() -> None:
    NoticeDraftForm(source_kind="text", text="燃えるごみは月・木")
    NoticeDraftForm(source_kind="photo", photo=upload())
    for bad in [
        {"source_kind": "text", "text": "  "},
        {"source_kind": "photo"},
        {"source_kind": "photo", "photo": upload(), "text": "also text"},
    ]:
        with pytest.raises(ValidationError):
            NoticeDraftForm.model_validate(bad)


def test_ask_needs_text_or_audio_but_not_both() -> None:
    AskForm(text="扇風機は？")
    AskForm(audio=upload())
    for bad in [{}, {"text": "x", "audio": upload()}]:
        with pytest.raises(ValidationError):
            AskForm.model_validate(bad)


@pytest.mark.parametrize("count", [0, 9])
def test_notice_has_one_to_eight_steps(count: int) -> None:
    with pytest.raises(ValidationError):
        NoticeUpdate.model_validate({"steps": [TRI] * count})


def test_meetup_takes_a_plain_date() -> None:
    assert MeetupCreate.model_validate({"kind": "drill", "date": "2026-12-06"}).date == date(2026, 12, 6)


def test_senpai_link_token_appears_only_when_the_pair_is_created() -> None:
    assert "token" not in SenpaiPairOut.model_fields
    assert "token" in SenpaiPairCreated.model_fields
