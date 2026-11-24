"""Group controls on work Threads (XREQ-019). The title is `titleJa`, as in GET /threads."""

from typing import Annotated, Literal

from pydantic import AwareDatetime, Field, StringConstraints

from app.schemas.common import CamelModel, IdIn

Title = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=100)]
Description = Annotated[str, StringConstraints(strip_whitespace=True, max_length=500)]
MemberRole = Literal["admin", "member"]


class ThreadCreate(CamelModel):
    title_ja: Title
    description: Description = ""
    member_ids: Annotated[list[IdIn], Field(max_length=50)] = []
    announce_only: bool = False


class ThreadUpdate(CamelModel):
    title_ja: Title | None = None
    description: Description | None = None
    announce_only: bool | None = None


class MembersIn(CamelModel):
    user_ids: Annotated[list[IdIn], Field(min_length=1, max_length=50)]


class RoleIn(CamelModel):
    role: MemberRole


class MySettingsIn(CamelModel):
    muted: bool
    pinned: bool
    archived: bool


class MemberOut(CamelModel):
    user_id: str
    display_name: str
    role: MemberRole
    joined_at: AwareDatetime


class ThreadDetail(CamelModel):
    id: str
    title_ja: str
    description: str
    kind: Literal["group", "direct"]
    announce_only: bool
    locked: bool
    members: list[MemberOut]
    # The caller's own settings.
    muted: bool
    pinned: bool
    archived: bool
