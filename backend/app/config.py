"""Settings from the environment (12-factor), validated at startup so a bad deploy fails before serving."""

from typing import Annotated, Literal

from pydantic import StringConstraints
from pydantic_settings import BaseSettings, SettingsConfigDict

# An exact origin for CORS: scheme and host[:port], no path, no wildcard.
Origin = Annotated[str, StringConstraints(pattern=r"^https?://[^/\s*]+$")]


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore", frozen=True)

    frontend_origin: Origin
    demo_mode: bool = False
    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = "INFO"
