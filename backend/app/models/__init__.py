"""Importing this package registers every table on Base.metadata (Alembic and tests rely on it)."""

from app.models import life, platform, scopes, work
from app.models.base import Base

__all__ = ["Base", "life", "platform", "scopes", "work"]
