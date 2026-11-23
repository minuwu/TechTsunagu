"""App platform (XREQ-009; MOBILE_PLATFORM_CONTRACT 1.3, 3.2): the version gate and push device registration."""

import uuid
from typing import Annotated, Literal

from pydantic import AwareDatetime, Field, StringConstraints

from app.schemas.common import CamelModel

Version = Annotated[str, StringConstraints(pattern=r"^\d+\.\d+\.\d+$")]


class AppConfig(CamelModel):
    """GET /app/config (public). The app blocks below `minVersion` and offers `downloadUrl` (check `sha256`)."""

    min_version: Version
    latest_version: Version
    download_url: str | None
    sha256: str | None
    server_time: AwareDatetime


class DeviceIn(CamelModel):
    install_id: uuid.UUID
    platform: Literal["android"]
    fcm_token: Annotated[str, StringConstraints(min_length=1, max_length=4096)]
    app_version: Version
    os_api_level: Annotated[int | None, Field(ge=1, le=100)] = None
    locale: Annotated[str | None, StringConstraints(max_length=35)] = None
    notifications_enabled: bool


class DeviceOut(CamelModel):
    registered: bool
    last_seen_at: AwareDatetime
