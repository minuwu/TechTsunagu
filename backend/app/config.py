"""Settings from the environment (12-factor), validated at startup so a bad deploy fails before serving."""

from typing import Annotated, Literal, Self

from pydantic import Field, SecretStr, StringConstraints, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

# An exact origin for CORS: scheme and host[:port], no path, no wildcard.
Origin = Annotated[str, StringConstraints(pattern=r"^https?://[^/\s*]+$")]


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore", frozen=True)

    frontend_origin: Origin
    # The app role (DML only). Migrations use MIGRATION_DATABASE_URL (the owner), read by migrations/env.py only.
    database_url: Annotated[str, StringConstraints(pattern=r"^postgresql\+psycopg://")]
    # HMAC key mixed into every PIN hash before argon2id; at least 32 characters, never in git (architecture §12).
    pin_pepper: Annotated[SecretStr, Field(min_length=32)]
    demo_mode: bool = False

    # AI (architecture §10). `fake` by default: real calls only when someone sets AI_MODE=gemini and a key.
    ai_mode: Literal["fake", "gemini"] = "fake"
    gemini_api_key: SecretStr | None = None
    gemini_model: str = "gemini-3.8-flash"
    ai_timeout_seconds: int = 45
    ai_max_concurrent: int = 2
    ai_per_minute: int = 4
    ai_daily_budget: int = 18

    @model_validator(mode="after")
    def _gemini_needs_a_key(self) -> Self:
        if self.ai_mode == "gemini" and self.gemini_api_key is None:
            raise ValueError("gemini_api_key is required when ai_mode is gemini")
        return self

    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = "INFO"
