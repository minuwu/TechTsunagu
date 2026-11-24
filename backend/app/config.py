"""Settings from the environment (12-factor), validated at startup so a bad deploy fails before serving."""

import base64
import binascii
import json
from typing import Annotated, Any, Literal, Self, cast

from pydantic import Field, SecretStr, StringConstraints, field_validator, model_validator
from pydantic_settings import BaseSettings, NoDecode, SettingsConfigDict

from app.ai.chains import Chain, Tier


def firebase_key(b64: SecretStr) -> dict[str, Any]:
    """The service-account JSON from its base64 form; a ValueError (never the key itself) when it is not one."""
    try:
        info: object = json.loads(base64.b64decode(b64.get_secret_value(), validate=True))
    except (binascii.Error, UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise ValueError("FIREBASE_SERVICE_ACCOUNT_B64 is not base64 of a JSON key") from exc
    if not isinstance(info, dict) or info.get("type") != "service_account":  # pyright: ignore[reportUnknownMemberType]
        raise ValueError("FIREBASE_SERVICE_ACCOUNT_B64 is not a service-account key")
    return cast(dict[str, Any], info)


# An exact origin for CORS: scheme and host[:port], no path, no wildcard.
Origin = Annotated[str, StringConstraints(pattern=r"^https?://[^/\s*]+$")]
CertFingerprint = Annotated[str, StringConstraints(pattern=r"^([0-9A-F]{2}:){31}[0-9A-F]{2}$")]


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore", frozen=True)

    # FRONTEND_ORIGIN: one exact origin, or several separated by commas (local dev and the hosted frontend).
    frontend_origin: Annotated[list[Origin], NoDecode, Field(min_length=1)]
    # The app role (DML only). Migrations use MIGRATION_DATABASE_URL (the owner), read by migrations/env.py only.
    database_url: Annotated[str, StringConstraints(pattern=r"^postgresql\+psycopg://")]
    # HMAC key mixed into every PIN hash before argon2id; at least 32 characters, never in git (architecture §12).
    pin_pepper: Annotated[SecretStr, Field(min_length=32)]
    demo_mode: bool = False

    # AI (architecture §10). `fake` by default: real calls only when someone sets AI_MODE=gemini and a key.
    ai_mode: Literal["fake", "gemini"] = "fake"
    gemini_api_key: SecretStr | None = None
    # Models and per-model budgets live in app/ai/chains.py (XREQ-003): dev = Flash-Lite only, demo = 3.8 Flash first.
    ai_chain: Chain = "dev"
    ai_tier: Tier = "free"  # paid: the budgets become a cost cap instead of the free quota
    groq_api_key: SecretStr | None = None  # optional backup at the end of the chain; empty turns it off
    # One model call, and the whole chain (audit A1: drafts took 51-61 s; the Android app gives up at 30 s).
    ai_timeout_seconds: int = 15
    ai_chain_seconds: int = 20
    ai_max_concurrent: int = 2

    @field_validator("frontend_origin", "android_cert_sha256", mode="before")
    @classmethod
    def _split_commas(cls, value: object) -> object:
        return [o.strip() for o in value.split(",") if o.strip()] if isinstance(value, str) else value

    @model_validator(mode="after")
    def _gemini_needs_a_key(self) -> Self:
        if self.ai_mode == "gemini" and self.gemini_api_key is None:
            raise ValueError("gemini_api_key is required when ai_mode is gemini")
        return self

    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = "INFO"

    # Push (XREQ-014): both or neither. The key is the service-account JSON, base64 (deploy/set-firebase-key.ps1).
    firebase_project_id: str | None = None
    firebase_service_account_b64: SecretStr | None = None

    @model_validator(mode="after")
    def _firebase_is_complete(self) -> Self:
        key, project = self.firebase_service_account_b64, self.firebase_project_id
        if (key is None) != (project is None):
            raise ValueError("set both FIREBASE_PROJECT_ID and FIREBASE_SERVICE_ACCOUNT_B64, or neither")
        if key is not None and firebase_key(key).get("project_id") != project:
            raise ValueError("the Firebase key belongs to another project than FIREBASE_PROJECT_ID")
        return self

    # Android app gate (XREQ-009, GET /api/app/config): the app blocks below the minimum and offers the download.
    app_min_version: Annotated[str, StringConstraints(pattern=r"^\d+\.\d+\.\d+$")] = "1.0.0"
    app_latest_version: Annotated[str, StringConstraints(pattern=r"^\d+\.\d+\.\d+$")] = "1.0.0"
    app_download_url: str | None = None  # the release APK (direct distribution, no Play Store)
    app_sha256: Annotated[str | None, StringConstraints(pattern=r"^[0-9a-f]{64}$")] = None
    # App Links (XREQ-026): signing-certificate SHA-256 fingerprints (AB:CD:…), comma-separated: release, debug, …
    android_cert_sha256: Annotated[list[CertFingerprint], NoDecode] = []
