"""Types shared by request and response models (architecture §9.1, ADR 0011): camelCase JSON, string ids,
translated text keyed by language. Shared with the Work schemas; WS0 may move these into app/api/."""

from datetime import date, timedelta
from typing import Annotated, Literal, Self

from pydantic import AfterValidator, BaseModel, ConfigDict, Field, StringConstraints, model_validator
from pydantic.alias_generators import to_camel

Lang = Literal["ja", "en", "vi", "id", "ne", "my", "tl", "zh"]  # no Bengali (ADR 0016)

# Ids are bigint in Postgres and strings on the wire; a non-numeric id is refused before it reaches a query.
IdIn = Annotated[str, StringConstraints(pattern=r"^[1-9][0-9]{0,18}$")]


def _text_in_every_language(text: dict[Lang, str]) -> dict[Lang, str]:
    if "ja" not in text:
        raise ValueError("ja is required")
    if not all(value.strip() for value in text.values()):
        raise ValueError("every language needs text")
    return text


# {ja, <each shop language>}; with the demo shop's languages this is exactly the frontend's Tri.
# Which languages a shop needs is checked in the service, because it differs per shop.
Translated = Annotated[
    dict[Lang, Annotated[str, StringConstraints(max_length=1000)]], AfterValidator(_text_in_every_language)
]


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, validate_by_name=True, validate_by_alias=True, extra="forbid")


class CheckQuestionForWorker(CamelModel):
    id: Annotated[str, StringConstraints(pattern=r"^q[0-9]{1,2}$")]
    term_id: str | None
    prompt: Translated
    options: Annotated[list[Translated], Field(min_length=3, max_length=3)]


class CheckQuestion(CheckQuestionForWorker):
    """Only Foremen and Posters see the answer; Worker responses use CheckQuestionForWorker."""

    correct_index: Annotated[int, Field(ge=0, le=2)]


class DateRange(CamelModel):
    """`?from=&to=` in shop-timezone dates, both inclusive."""

    from_: date = Field(alias="from")
    to: date

    @model_validator(mode="after")
    def _ordered(self) -> Self:
        if self.to < self.from_:
            raise ValueError("to must not be before from")
        return self

    @property
    def days(self) -> timedelta:
        return self.to - self.from_
