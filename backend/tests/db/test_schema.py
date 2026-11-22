"""WS0 step 2: the database enforces isolation and least privilege by itself (architecture §2, §6, §12)."""

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError, ProgrammingError
from sqlalchemy.orm import Session

from app.models.scopes import AuditEvent
from tests import factories as f


def test_a_note_version_cannot_point_at_another_shops_machine(db: Session) -> None:
    shop_a, shop_b = f.shop(db), f.shop(db)
    foreman_a = f.user(db, shop_a, role="foreman")
    machine_b = f.machine(db, shop_b)

    with pytest.raises(IntegrityError, match="fk_note_versions_shop_id_machine_id_machines"), db.begin_nested():
        f.note_draft(db, shop_a, machine_b, foreman_a)


def test_a_machine_can_point_at_its_current_version_within_one_transaction(db: Session) -> None:
    shop = f.shop(db)
    foreman = f.user(db, shop, role="foreman")
    machine = f.machine(db, shop)
    draft = f.note_draft(db, shop, machine, foreman)

    machine.current_version_id = draft.id
    db.flush()

    assert machine.current_version_id == draft.id


def test_two_active_users_of_one_shop_cannot_share_a_display_name(db: Session) -> None:
    shop = f.shop(db)
    f.user(db, shop, name="Rahim")

    with pytest.raises(IntegrityError, match="uq_users_shop_display_name"), db.begin_nested():
        f.user(db, shop, name="Rahim")


def test_a_worker_must_have_a_shop_and_a_district(db: Session) -> None:
    shop = f.shop(db)
    worker = f.user(db, shop)

    with pytest.raises(IntegrityError, match="scope_for_role"), db.begin_nested():
        worker.district_id = None
        db.flush()


def test_the_app_role_cannot_change_audit_events(db: Session) -> None:
    event = AuditEvent(action="login")
    db.add(event)
    db.flush()

    with pytest.raises(ProgrammingError, match="permission denied"), db.begin_nested():
        db.execute(text("UPDATE audit_events SET action = 'x' WHERE id = :id"), {"id": event.id})
    with pytest.raises(ProgrammingError, match="permission denied"), db.begin_nested():
        db.execute(text("DELETE FROM audit_events WHERE id = :id"), {"id": event.id})


def test_the_app_role_cannot_create_tables(db: Session) -> None:
    with pytest.raises(ProgrammingError, match="permission denied"), db.begin_nested():
        db.execute(text("CREATE TABLE intruder (id int)"))


def test_readiness_checks_the_database(client: TestClient) -> None:
    res = client.get("/api/health/ready")

    assert res.status_code == 200
    assert res.json()["data"] == {"status": "ok"}
