"""Settings from the environment (12-factor), validated at startup so a bad deploy fails before serving."""

from typing import Annotated, Literal

from pydantic import Field, SecretStr, StringConstraints
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
    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = "INFO"
