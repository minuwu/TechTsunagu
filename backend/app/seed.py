"""Demo seed (architecture §15.1): `uv run python -m app.seed --reset`.

Runs as the schema owner (MIGRATION_DATABASE_URL) and refuses to touch a database that already has shops unless
--reset is given. Work data comes from seed_data/work.json, the district from life.json, the terms from glossary.json.
`seed_work` returns key → id refs; the Life loader (WS10/WS11) builds on the same refs."""

import argparse
import json
import os
import uuid
from datetime import datetime, timedelta
from pathlib import Path
from typing import Any
from zoneinfo import ZoneInfo

from sqlalchemy import create_engine, func, select, text
from sqlalchemy.orm import Session

from app.models import Base
from app.models.scopes import District, Shop, User
from app.models.work import Check, GlossaryTerm, Machine, NoteVersion, ShiftAssignment, SignOff
from app.services.credentials import hash_pin

DATA = Path(__file__).resolve().parent / "seed_data"
_NS = uuid.UUID("6f1c2a3e-58a4-4f4b-9d55-1d4c0f0a2b10")  # stable idempotency keys for seeded rows
Refs = dict[str, int]


def _load(name: str) -> dict[str, Any]:
    data: dict[str, Any] = json.loads((DATA / name).read_text(encoding="utf-8"))
    return data


def _key(name: str) -> uuid.UUID:
    return uuid.uuid5(_NS, name)


def seed_work(db: Session, pepper: str, now: datetime) -> Refs:
    work, life, terms = _load("work.json"), _load("life.json"), _load("glossary.json")["terms"]
    refs: Refs = {}

    d = life["district"]
    district = District(code=d["code"], name=d["name"], prefecture=d["prefecture"], municipality=d["municipality"])
    db.add(district)
    db.flush()
    refs[d["key"]] = district.id

    shops: dict[str, Shop] = {}
    for s in work["shops"]:
        shop = Shop(code=s["code"], name=s["name"], default_district_id=refs[s["district"]], languages=s["languages"])
        db.add(shop)
        db.flush()
        refs[s["key"]], shops[s["key"]] = shop.id, shop

    hashes: dict[str, str] = {}
    for role, people in (("foreman", work["foremen"]), ("worker", work["workers"])):
        for p in people:
            hashes.setdefault(p["pin"], hash_pin(pepper, p["pin"]))
            shop = shops[p["shop"]]
            user = User(
                role=role,
                display_name=p["name"],
                language=p["language"],
                shop_id=shop.id,
                district_id=shop.default_district_id,
                pin_hash=hashes[p["pin"]],
                pin_must_change=False,  # demo accounts; real accounts always start with a PIN change
            )
            db.add(user)
            db.flush()
            refs[p["key"]] = user.id

    machines: dict[str, Machine] = {}
    for m in work["machines"]:
        machine = Machine(shop_id=refs[m["shop"]], code=m["code"], name=m["name"])
        db.add(machine)
        db.flush()
        refs[m["key"]], machines[m["key"]] = machine.id, machine

    home = refs["s-sasebo"]
    for t in terms:
        term = GlossaryTerm(shop_id=home, ja=t["ja"], reading=t["reading"], translations=t["translations"])
        db.add(term)
        db.flush()
        refs[t["id"]] = term.id

    notes: dict[str, NoteVersion] = {}
    for n in work["notes"]:
        machine = machines[n["machine"]]
        approved_at = now - timedelta(days=n["approvedDaysAgo"])
        questions = [
            {
                "id": f"q{i}",  # the API's CheckQuestion id shape (q1, q2…), as AI drafts number them
                "termId": str(refs[q["term"]]) if q["term"] else None,
                "prompt": q["prompt"],
                "options": q["options"],
                "correctIndex": q["correctIndex"],
            }
            for i, q in enumerate(n["questions"], start=1)
        ]
        note = NoteVersion(
            shop_id=machine.shop_id,
            machine_id=machine.id,
            version_no=n["versionNo"],
            status="approved",
            recorded_by=refs[n["recordedBy"]],
            approved_by=refs[n["recordedBy"]],
            source_kind="text",
            transcript_ja=n["transcriptJa"],
            steps=n["steps"],
            back_translation=n["backTranslation"],
            questions=questions,
            change_kind="safety",
            ai_outcome="typed",
            idempotency_key=_key(n["key"]),
            created_at=approved_at,
            approved_at=approved_at,
        )
        db.add(note)
        db.flush()
        machine.current_version_id, machine.recheck_version_no = note.id, note.version_no
        refs[n["key"]], notes[n["key"]] = note.id, note

    for i, s in enumerate(work["signOffs"]):
        note, worker_id = notes[s["note"]], refs[s["worker"]]
        passed_at = now - timedelta(days=s["daysAgo"])
        check = Check(
            shop_id=note.shop_id,
            worker_id=worker_id,
            note_version_id=note.id,
            language="bn",
            answers=[int(str(q["correctIndex"])) for q in note.questions],
            passed=True,
            idempotency_key=_key(f"check-{i}"),
            created_at=passed_at,
        )
        db.add(check)
        db.flush()
        db.add(
            SignOff(
                shop_id=note.shop_id,
                worker_id=worker_id,
                machine_id=note.machine_id,
                note_version_id=note.id,
                check_id=check.id,
                passed_at=passed_at,
            )
        )

    # The shop runs on Tokyo time but testers are in Bangladesh (3 h behind): both shifts, today and tomorrow in
    # Tokyo, so the board and Panels look the same at any hour a tester or judge opens them.
    today = now.astimezone(ZoneInfo("Asia/Tokyo")).date()
    for day in (today, today + timedelta(days=1)):
        for shift in ("day", "night"):
            for a in work["assignments"]:
                db.add(
                    ShiftAssignment(
                        shop_id=home,
                        worker_id=refs[a["worker"]],
                        machine_id=refs[a["machine"]],
                        shift_date=day,
                        shift=shift,
                        assigned_by=refs["f-yamaguchi"],
                    )
                )
    db.flush()
    return refs


def _reset(db: Session) -> None:
    tables = ", ".join(t.name for t in Base.metadata.sorted_tables)  # our own table names, never input
    db.execute(text(f"TRUNCATE {tables} RESTART IDENTITY CASCADE"))


def main() -> None:
    parser = argparse.ArgumentParser(description="Load the Wakatta? demo data")
    parser.add_argument("--reset", action="store_true", help="delete all data first (demo and local only)")
    args = parser.parse_args()
    url, pepper = os.environ.get("MIGRATION_DATABASE_URL"), os.environ.get("PIN_PEPPER", "")
    if not url or len(pepper) < 32:
        raise SystemExit("Set MIGRATION_DATABASE_URL and PIN_PEPPER (32+ chars) first.")
    engine = create_engine(url)
    with Session(engine) as db:
        if args.reset:
            _reset(db)
        elif db.scalar(select(func.count()).select_from(Shop)):
            raise SystemExit("The database already has shops; run with --reset to replace them.")
        from app.seed_life import seed_life  # here, not at the top: seed_life imports this module's helpers

        now = datetime.now(ZoneInfo("UTC"))
        refs = seed_life(db, pepper, seed_work(db, pepper, now), now)
        db.commit()
    engine.dispose()
    print(f"Seeded {len(refs)} rows by key. Demo: shop SASEBO01, Foreman PIN 246810, Worker PIN 2468.")  # noqa: T201


if __name__ == "__main__":
    main()
