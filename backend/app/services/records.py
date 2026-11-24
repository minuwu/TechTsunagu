"""Foreman records beside the floor flow: Panel Overrides (§7.1), the Sign-off list and the Knowledge Library (WS8)."""

import uuid
from datetime import datetime, timedelta

from sqlalchemy.orm import Session

from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, build_page
from app.models.work import PanelOverride
from app.repositories import accounts
from app.repositories import work as repo
from app.schemas.work import EffortOut, NoteVersionOut, OverrideIn, OverrideOut, SignOffOut
from app.services.auth import Principal
from app.services.notes import note_out, shop_of

REMIND_WINDOW = timedelta(minutes=10)


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _override_out(db: Session, o: PanelOverride) -> OverrideOut:
    return OverrideOut(
        id=str(o.id),
        machine_id=str(o.machine_id),
        worker_id=str(o.worker_id) if o.worker_id else None,
        by=repo.display_names(db, o.shop_id, [o.created_by]).get(o.created_by, ""),
        reason=o.reason,
        starts_at=o.starts_at,
        ends_at=o.ends_at,
        revoked_at=o.revoked_at,
    )


def start_override(
    db: Session, who: Principal, machine_id: int, body: OverrideIn, now: datetime, key: uuid.UUID | None = None
) -> OverrideOut:
    shop_id = shop_of(who)
    if key is not None and (done := repo.override_by_key(db, shop_id, key)) is not None:
        if done.created_by != who.user_id or done.machine_id != machine_id:
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another record")
        return _override_out(db, done)  # a retry returns the first
    if repo.machine(db, shop_id, machine_id) is None:
        raise _not_found()
    worker_id = int(body.worker_id) if body.worker_id else None
    if worker_id is not None and repo.worker(db, shop_id, worker_id) is None:
        raise _not_found()
    o = PanelOverride(
        shop_id=shop_id,
        machine_id=machine_id,
        worker_id=worker_id,
        reason=body.reason,
        created_by=who.user_id,
        starts_at=now,
        ends_at=now + timedelta(minutes=body.minutes),
        idempotency_key=key,
    )
    db.add(o)
    db.flush()
    accounts.audit(db, "panel_override_started", who.user_id, shop_id, subject_id=o.id)
    db.commit()
    return _override_out(db, o)


def remind(db: Session, who: Principal, machine_id: int, worker_id: int, now: datetime) -> None:
    """The Foreman asks a Worker to take the Machine's Check; the router sends the push. Once per 10 minutes."""
    shop_id = shop_of(who)
    if repo.machine(db, shop_id, machine_id) is None or repo.worker(db, shop_id, worker_id) is None:
        raise _not_found()
    if accounts.count_attempt(db, f"remind:{machine_id}:{worker_id}", now, REMIND_WINDOW) > 1:
        db.commit()
        raise ApiError(429, "RATE_LIMITED", "Already reminded; wait a few minutes", headers={"Retry-After": "600"})
    accounts.audit(db, f"check_reminder:{worker_id}", who.user_id, shop_id, subject_id=machine_id)
    db.commit()


def end_override(db: Session, who: Principal, machine_id: int, override_id: int, now: datetime) -> OverrideOut:
    """Idempotent: ending an override that already ended returns it unchanged."""
    shop_id = shop_of(who)
    o = repo.override(db, shop_id, machine_id, override_id)
    if o is None:
        raise _not_found()
    if o.revoked_at is None and o.ends_at > now:
        o.revoked_at = now
        accounts.audit(db, "panel_override_ended", who.user_id, shop_id, subject_id=o.id)
        db.commit()
    return _override_out(db, o)


def list_sign_offs(db: Session, who: Principal, page: PageParams) -> Page[SignOffOut]:
    """A Foreman sees the shop's Sign-offs; a Worker only their own (architecture §9.3)."""
    own = who.user_id if who.role == "worker" else None
    rows = repo.sign_offs_page(db, shop_of(who), own, page.after_id, page.limit)
    last = rows[page.limit - 1].id if len(rows) > page.limit else None
    items = [
        SignOffOut(
            id=str(s.id),
            worker_id=str(s.worker_id),
            machine_id=str(s.machine_id),
            note_version_id=str(s.note_version_id),
            passed_at=s.passed_at,
        )
        for s in rows
    ]
    return build_page(items, page.limit, last)


def list_versions(db: Session, who: Principal, machine_id: int, page: PageParams) -> Page[NoteVersionOut]:
    """Knowledge Library: every Note Version of a Machine, newest first, with who recorded and approved it."""
    shop_id = shop_of(who)
    if repo.machine(db, shop_id, machine_id) is None:
        raise _not_found()
    rows = repo.versions_page(db, shop_id, machine_id, page.after_id, page.limit)
    last = rows[page.limit - 1].id if len(rows) > page.limit else None
    # shortcut: note_out looks up names per row (one small query each, bounded by the page size); batch if pages grow.
    return build_page([note_out(db, n) for n in rows], page.limit, last)


def effort(db: Session, who: Principal) -> EffortOut:
    drafts, approved, seconds, edits, clean, first_try = repo.effort(db, shop_of(who))
    return EffortOut(
        drafts=drafts,
        approved=approved,
        median_seconds_to_approve=seconds,
        median_edits_per_draft=edits,
        clean_ai_share=clean,
        first_try_pass_share=first_try,
    )
