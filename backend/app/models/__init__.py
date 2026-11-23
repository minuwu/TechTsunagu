"""Importing this package registers every table on Base.metadata (Alembic and tests rely on it)."""

from app.models import chat, life, personal, platform, scopes, work
from app.models.base import Base

__all__ = ["Base", "chat", "life", "personal", "platform", "scopes", "work"]
