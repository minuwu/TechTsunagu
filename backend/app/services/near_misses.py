"""Near-miss Reports (WS7): a Worker reports by text or voice, with an optional photo; the Foreman reads it in
Japanese and acknowledges it. A report is a safety record: it is never lost because the AI is down."""

import hashlib
import uuid
from datetime import datetime

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.media import audio_mime, jpeg_without_exif
from app.models.platform import Blob
from app.models.work import NearMissReport
from app.repositories import near_misses as repo
from app.repositories import work as work_repo
from app.schemas.near_misses import NearMissOut
from app.services.ai_runner import AiRunner
from app.services.auth import Principal
from app.services.chat import to_japanese

# The Foreman still sees that a report came in and goes to the Worker; the recording is never stored (§11).
UNTRANSLATED_VOICE = "（音声の報告：いま翻訳できません。本人に確認してください）"


def _me(who: Principal) -> tuple[int, int]:
    if who.shop_id is None or who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id, who.user_id


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def report_out(r: NearMissReport) -> NearMissOut:
    return NearMissOut.model_validate(
        {
            "id": str(r.id),
            "worker_id": str(r.worker_id),
            "machine_id": str(r.machine_id),
            "text": r.text,
            "photo": f"/api/near-misses/{r.id}/photo" if r.photo_blob_id else None,
            "created_at": r.created_at,
            "acknowledged_at": r.acknowledged_at,
        }
    )


def _replay(r: NearMissReport, worker_id: int) -> NearMissOut:
    """A retry returns the first report, but only to the Worker who made it; the key is unique per shop."""
    if r.worker_id != worker_id:
        raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another report")
    return report_out(r)


def create(
    db: Session,
    who: Principal,
    machine_id: int,
    text: str | None,
    audio: bytes | None,
    photo: bytes | None,
    key: uuid.UUID,
    ai: AiRunner,
    now: datetime,
) -> NearMissOut:
    shop_id, worker_id = _me(who)
    existing = repo.report_by_key(db, shop_id, key)
    if existing is not None:
        return _replay(existing, worker_id)
    text = text.strip() if text else None
    if bool(text) == bool(audio):
        raise ApiError(422, "VALIDATION_ERROR", "Send text or a recording, not both", {"fields": [{"loc": ["body"]}]})
    if work_repo.machine(db, shop_id, machine_id) is None:
        raise _not_found()
    mime = audio_mime(audio) if audio else None
    image = jpeg_without_exif(photo) if photo else None
    language = repo.language_of(db, shop_id, worker_id) or "en"
    db.commit()  # no transaction is held during the model call (§8.3)

    if language == "ja" and text:
        text_ja: str | None = text
    else:
        parts = [Part.blob(audio, mime)] if audio and mime else [Part.text(f"<source_text>{text}</source_text>")]
        text_ja = to_japanese(ai, parts, language, f"shop:{shop_id}", audio or (text or "").encode(), now)
    translated = {"ja": text_ja or (UNTRANSLATED_VOICE if audio else text or "")}
    if text and language != "ja":
        translated[language] = text

    raced = repo.report_by_key(db, shop_id, key)
    if raced is not None:
        return _replay(raced, worker_id)
    blob_id = None
    if image is not None:
        b = Blob(
            shop_id=shop_id,
            created_by=worker_id,
            mime="image/jpeg",
            size_bytes=len(image),
            sha256=hashlib.sha256(image).digest(),
            bytes_=image,
        )
        db.add(b)
        db.flush()
        blob_id = b.id
    row = NearMissReport(
        shop_id=shop_id,
        worker_id=worker_id,
        machine_id=machine_id,
        source_kind="voice" if audio else "text",
        text_src=text or "",
        text=translated,
        photo_blob_id=blob_id,
        idempotency_key=key,
    )
    db.add(row)
    db.commit()
    return report_out(row)


def list_reports(db: Session, who: Principal, open_only: bool, page: PageParams) -> Page[NearMissOut]:
    shop_id, _ = _me(who)
    rows = repo.reports_page(db, shop_id, open_only, page.after_id, page.limit)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[report_out(r) for r in shown], next_cursor=cursor)


def acknowledge(db: Session, who: Principal, report_id: int, now: datetime) -> NearMissOut:
    shop_id, user_id = _me(who)
    r = repo.report(db, shop_id, report_id, for_update=True)
    if r is None:
        raise _not_found()
    if r.acknowledged_at is None:  # acknowledging twice keeps the first
        r.acknowledged_at, r.acknowledged_by = now, user_id
    db.commit()
    return report_out(r)


def photo(db: Session, who: Principal, report_id: int) -> tuple[bytes, str]:
    """The Foremen of the shop and the Worker who reported it."""
    shop_id, user_id = _me(who)
    r = repo.report(db, shop_id, report_id)
    if r is None or r.photo_blob_id is None or (who.role == "worker" and r.worker_id != user_id):
        raise _not_found()
    b = repo.blob(db, shop_id, r.photo_blob_id)
    if b is None:
        raise _not_found()
    return b.bytes_, b.mime
