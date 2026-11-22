"""Declarative base and column helpers for every table (architecture §6 conventions)."""

from datetime import datetime
from typing import Annotated

from sqlalchemy import BigInteger, DateTime, Identity, MetaData, func
from sqlalchemy.orm import DeclarativeBase, mapped_column

# Stable constraint names, so Alembic migrations stay reviewable and reversible.
NAMING = {
    "ix": "ix_%(column_0_N_label)s",
    "uq": "uq_%(table_name)s_%(column_0_N_name)s",
    "ck": "ck_%(table_name)s_%(constraint_name)s",
    "fk": "fk_%(table_name)s_%(column_0_N_name)s_%(referred_table_name)s",
    "pk": "pk_%(table_name)s",
}

JsonValue = dict[str, object] | list[object]


class Base(DeclarativeBase):
    metadata = MetaData(naming_convention=NAMING)


IdPk = Annotated[int, mapped_column(BigInteger, Identity(always=True), primary_key=True)]
CreatedAt = Annotated[datetime, mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)]
Timestamp = Annotated[datetime, mapped_column(DateTime(timezone=True), nullable=False)]
TimestampOrNone = Annotated[datetime | None, mapped_column(DateTime(timezone=True), nullable=True)]
