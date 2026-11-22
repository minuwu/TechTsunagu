"""The response envelope every JSON route returns (API_SPEC.md §1.1)."""

from typing import Literal

from pydantic import BaseModel, Field


class Envelope[T](BaseModel):
    success: Literal[True] = True
    data: T
    message: str = "ok"


class ErrorBody(BaseModel):
    code: str
    message: str
    details: dict[str, object] = Field(default_factory=dict[str, object])


class ErrorEnvelope(BaseModel):
    success: Literal[False] = False
    error: ErrorBody
