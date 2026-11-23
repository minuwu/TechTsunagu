"""The guard test (architecture §3.3): every route of the contract refuses the wrong person. A new route fails here
until it declares its roles, and a new route with an id in its path fails until it has a row in CALLS."""

import json
import uuid
from dataclasses import dataclass, field
from datetime import UTC, datetime, timedelta
from typing import Any

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.models.chat import FollowUp, MessageCheck
from app.models.life import Ask, Event, Notice, SenpaiPair
from app.models.personal import Contact, ContactMessage
from app.models.platform import Blob
from app.models.scopes import Poster, Shop, User
from app.models.scopes import Session as SessionRow
from app.models.work import Briefing, Confusion, GlossaryTerm, Machine, NearMissReport, PanelOverride
from app.openapi import contract
from app.services.credentials import new_token
from tests import factories as f

Json = dict[str, Any]
ROLES = ("foreman", "worker", "poster", "panel")

# No session: the version gate, health, the sign-in itself, and the Senpai link, whose token is the credential.
PUBLIC = {
    "GET /api/health",
    "GET /api/health/ready",
    "GET /api/app/config",
    "GET /api/auth/roster",
    "POST /api/auth/login",
    "GET /api/senpai/{token}",
    "POST /api/senpai/{token}/asks/{ask_id}/reply",
    "POST /api/senpai/{token}/meetups",
}
BEFORE_PIN_CHANGE = {"GET /api/auth/me", "PUT /api/auth/me/pin", "POST /api/auth/logout"}
OPERATIONS: dict[str, list[str]] = {
    f"{method.upper()} {path}": op.get("x-roles", [])
    for path, ops in json.loads(contract())["paths"].items()
    for method, op in ops.items()
}

TRI = {"ja": "日本語", "bn": "বাংলা", "en": "English"}
QUESTION: Json = {"id": "q1", "termId": None, "prompt": TRI, "options": [TRI, TRI, TRI], "correctIndex": 0}


@dataclass(frozen=True)
class Call:
    """A request its rightful user can make. `rows` renames a path id when the default row does not fit the route."""

    json: Json | None = None
    data: Json | None = None
    headers: dict[str, str] = field(default_factory=dict[str, str])
    rows: dict[str, str] = field(default_factory=dict[str, str])


KEY = {"Idempotency-Key": str(uuid.uuid4())}
CALLS: dict[str, Call] = {
    "PATCH /api/workers/{user_id}": Call(json={}, rows={"user_id": "worker"}),
    "PATCH /api/foremen/{user_id}": Call(json={}, rows={"user_id": "foreman"}),
    "GET /api/machines/{machine_id}": Call(),
    "POST /api/machines/{machine_id}/scan": Call(),
    "GET /api/machines/{machine_id}/panel": Call(),
    "POST /api/machines/{machine_id}/panel-token": Call(),
    "POST /api/machines/{machine_id}/notes": Call(data={"text": "刃を交換する"}, headers=KEY),
    "GET /api/note-versions/{note_id}": Call(),
    "PATCH /api/note-versions/{note_id}": Call(json={"index": 0, "ja": "電源を切ります"}),
    "POST /api/note-versions/{note_id}/approve": Call(json={}),
    "POST /api/machines/{machine_id}/overrides": Call(json={"workerId": None, "minutes": 5, "reason": "training"}),
    "DELETE /api/machines/{machine_id}/overrides/{override_id}": Call(),
    "GET /api/machines/{machine_id}/versions": Call(),
    "POST /api/confusions/{confusion_id}/ack": Call(),
    "PATCH /api/glossary/{term_id}": Call(json={"reading": "かながた"}),
    "GET /api/notices/{notice_id}": Call(rows={"notice_id": "published_notice"}),
    "PATCH /api/notices/{notice_id}": Call(json={}),
    "PATCH /api/notices/{notice_id}/events/{event_id}": Call(json={"confirmed": True}),
    "POST /api/notices/{notice_id}/publish": Call(),
    "GET /api/notices/{notice_id}/reach": Call(rows={"notice_id": "published_notice"}),
    "GET /api/blobs/{blob_id}": Call(),
    "POST /api/asks/{ask_id}/forward": Call(),
    "DELETE /api/senpai-pairs/{pair_id}": Call(),
    "GET /api/workers/{worker_id}/passport": Call(),
    "POST /api/threads/{thread_id}/messages": Call(data={"text": "わかりました"}, headers=KEY),
    "GET /api/threads/{thread_id}/messages": Call(),
    "POST /api/messages/{message_id}/send": Call(json={"text": TRI}, rows={"message_id": "draft"}),
    "POST /api/messages/{message_id}/answers": Call(json={"answers": [0]}),
    "PATCH /api/follow-ups/{follow_up_id}": Call(json={"status": "clarified"}),
    "POST /api/messages/{message_id}/reexplain": Call(json={"userId": "{worker}", "termJa": "金型"}),
    "POST /api/messages/{message_id}/nudge": Call(json={"userId": "{worker}"}),
    "PATCH /api/tasks/{task_id}": Call(json={"column": "doing"}, headers={"If-Match": "1"}),
    "GET /api/briefings/{briefing_id}/receipts": Call(),
    "POST /api/near-misses/{report_id}/ack": Call(),
    "GET /api/near-misses/{report_id}/photo": Call(),
    "GET /api/workers/{worker_id}/interviews": Call(),
    "POST /api/workers/{worker_id}/interviews": Call(json={"heldOn": "2026-11-01", "method": "face"}),
    "PUT /api/contacts/{contact_id}": Call(json={"muted": True}),
    "DELETE /api/blocks/{contact_id}": Call(),
    "GET /api/contacts/{contact_id}/messages": Call(),
    "POST /api/contacts/{contact_id}/messages": Call(json={"text": "hi", "lang": "bn"}),
    "DELETE /api/contacts/{contact_id}/messages": Call(),
    "DELETE /api/contacts/{contact_id}/messages/{message_id}": Call(rows={"message_id": "contact_message"}),
}

# The same shop, but not this Worker's own record.
COLLEAGUE_REFUSED: dict[str, int] = {
    "POST /api/asks/{ask_id}/forward": 404,
    "GET /api/workers/{worker_id}/passport": 403,
    "POST /api/threads/{thread_id}/messages": 403,
    "GET /api/threads/{thread_id}/messages": 403,
    "POST /api/messages/{message_id}/answers": 403,
    "GET /api/near-misses/{report_id}/photo": 404,
    "PUT /api/contacts/{contact_id}": 404,
    "DELETE /api/blocks/{contact_id}": 404,
    "GET /api/contacts/{contact_id}/messages": 404,
    "POST /api/contacts/{contact_id}/messages": 404,
    "DELETE /api/contacts/{contact_id}/messages": 404,
    "DELETE /api/contacts/{contact_id}/messages/{message_id}": 404,
}


@dataclass
class Site:
    """One shop and its district: a signed-in person per role, and one row for every id a path can name."""

    headers: dict[str, dict[str, str]]
    colleague: dict[str, str]
    newcomer: dict[str, str]  # has not chosen their own PIN yet
    rows: dict[str, int]


def _session(db: Session, user: User | None, machine: Machine | None = None) -> dict[str, str]:
    token, digest = new_token()
    row = SessionRow(token_hash=digest, kind="user", expires_at=datetime.now(UTC) + timedelta(hours=1))
    if user is not None:
        row.user_id = user.id
    if machine is not None:
        row.kind, row.machine_id, row.shop_id = "panel", machine.id, machine.shop_id
    db.add(row)
    db.flush()
    return {"Authorization": f"Bearer {token}"}


def _save[T](db: Session, row: T) -> T:
    db.add(row)
    db.flush()
    return row


def _work_rows(db: Session, s: Shop, foreman: User, worker: User, machine: Machine) -> dict[str, int]:
    now = datetime.now(UTC)
    shop: Json = {"shop_id": s.id}
    thread = f.thread(db, s, foreman, [worker])
    message = f.message(db, s, thread, foreman, stage=4, sent_at=now)
    db.add(MessageCheck(message_id=message.id, questions=[QUESTION], **shop))
    db.flush()
    photo = Blob(created_by=worker.id, mime="image/jpeg", size_bytes=2, sha256=b"x", bytes_=b"\xff\xd8", **shop)
    report = NearMissReport(
        worker_id=worker.id,
        machine_id=machine.id,
        source_kind="text",
        text_src="guard off",
        text={"ja": "カバーが外れていた"},
        photo_blob_id=_save(db, photo).id,
        idempotency_key=uuid.uuid4(),
        **shop,
    )
    override = PanelOverride(
        machine_id=machine.id, reason="training", created_by=foreman.id, ends_at=now + timedelta(hours=1), **shop
    )
    briefing = Briefing(
        status="published",
        recorded_by=foreman.id,
        transcript_ja="点検",
        text=TRI,
        question=QUESTION,
        idempotency_key=uuid.uuid4(),
        published_at=now,
        **shop,
    )
    confusion = Confusion(message_id=message.id, worker_id=worker.id, question_id="q1", **shop)
    term = GlossaryTerm(ja="金型", reading="かながた", translations={"en": "die"}, **shop)
    follow_up = FollowUp(message_id=message.id, user_id=worker.id, term_ja="金型", **shop)
    return {
        "note_id": f.note_draft(db, s, machine, foreman).id,
        "override_id": _save(db, override).id,
        "confusion_id": _save(db, confusion).id,
        "term_id": _save(db, term).id,
        "thread_id": thread.id,
        "message_id": message.id,
        "draft": f.message(db, s, thread, foreman).id,
        "follow_up_id": _save(db, follow_up).id,
        "task_id": f.task(db, s, foreman).id,
        "briefing_id": _save(db, briefing).id,
        "report_id": _save(db, report).id,
    }


def _life_rows(db: Session, s: Shop, worker: User, foreman: User, poster: User) -> dict[str, int]:
    now, district = datetime.now(UTC), s.default_district_id

    def notice(**extra: Any) -> Notice:
        row = Notice(
            district_id=district,
            poster_id=poster.poster_id,
            created_by=poster.id,
            category="garbage",
            source_kind="text",
            source_ja="ごみ",
            title=TRI,
            steps=[TRI],
            question=QUESTION,
            idempotency_key=uuid.uuid4(),
            **extra,
        )
        return _save(db, row)

    blob = Blob(
        district_id=district, created_by=poster.id, mime="image/jpeg", size_bytes=2, sha256=b"x", bytes_=b"\xff\xd8"
    )
    blob_id, draft = _save(db, blob).id, notice()
    event = Event(district_id=district, notice_id=draft.id, title=TRI, starts_at=now + timedelta(days=3))
    ask = Ask(
        shop_id=s.id,
        worker_id=worker.id,
        source_kind="text",
        text_src="?",
        language="bn",
        covered=False,
        idempotency_key=uuid.uuid4(),
    )
    pair = SenpaiPair(
        shop_id=s.id,
        worker_id=worker.id,
        senpai_name="田中",
        kind="coworker",
        token_hash=new_token()[1],
        created_by=foreman.id,
        expires_at=now + timedelta(days=30),
    )
    return {
        "notice_id": draft.id,
        "published_notice": notice(status="published", published_at=now, source_blob_id=blob_id).id,
        "event_id": _save(db, event).id,
        "blob_id": blob_id,
        "ask_id": _save(db, ask).id,
        "pair_id": _save(db, pair).id,
    }


def _personal_rows(db: Session, worker: User, peer: User) -> dict[str, int]:
    message = ContactMessage(sender_id=worker.id, recipient_id=peer.id, text_src="hi", src_lang="bn")
    return {
        "contact_id": _save(db, Contact(owner_id=worker.id, peer_id=peer.id)).id,
        "contact_message": _save(db, message).id,
    }


def _site(db: Session) -> Site:
    s = f.shop(db)
    foreman, worker, colleague = (f.user(db, s, role=r, must_change=False) for r in ("foreman", "worker", "worker"))
    organisation = _save(db, Poster(district_id=s.default_district_id, name="自治会", kind="jichikai"))
    poster = User(
        role="poster",
        display_name=f"poster-{organisation.id}",
        language="ja",
        district_id=s.default_district_id,
        poster_id=organisation.id,
        pin_hash=worker.pin_hash,
        pin_must_change=False,
    )
    _save(db, poster)
    machine = f.machine(db, s)
    people = {"foreman": foreman, "worker": worker, "poster": poster}
    rows = {"foreman": foreman.id, "worker": worker.id, "worker_id": worker.id, "machine_id": machine.id}
    rows |= _work_rows(db, s, foreman, worker, machine) | _life_rows(db, s, worker, foreman, poster)
    rows |= _personal_rows(db, worker, foreman)
    headers = {role: _session(db, user) for role, user in people.items()} | {"panel": _session(db, None, machine)}
    return Site(headers, _session(db, colleague), _session(db, f.user(db, s)), rows)


def _send(client: TestClient, op: str, headers: dict[str, str], rows: dict[str, int]) -> int:
    call = CALLS[op]
    method, path = op.split(" ")
    for name in [p[1:-1] for p in path.split("/") if p.startswith("{")]:
        path = path.replace(f"{{{name}}}", str(rows[call.rows.get(name, name)]))
    body = call.json
    if body:
        body = {k: str(rows["worker"]) if v == "{worker}" else v for k, v in body.items()}
    return client.request(method, path, headers=headers | call.headers, json=body, data=call.data).status_code


def _knock(client: TestClient, op: str, headers: dict[str, str]) -> tuple[int, str]:
    """The route with made-up ids and no body: the guard answers before either is looked at."""
    method, path = op.split(" ")
    path = "/".join("1" if p.startswith("{") else p for p in path.split("/"))
    res = client.request(method, path, headers=headers)
    return res.status_code, res.json().get("error", {}).get("code", "")


def test_only_the_listed_routes_are_public() -> None:
    assert {op for op, roles in OPERATIONS.items() if not roles} == PUBLIC


@pytest.mark.parametrize("headers", [{}, {"Authorization": "Bearer not-a-token"}])
def test_every_protected_route_refuses_a_request_without_a_valid_token(
    client: TestClient, headers: dict[str, str]
) -> None:
    protected = [op for op in OPERATIONS if op not in PUBLIC]

    answers = {op: _knock(client, op, headers) for op in protected}

    assert answers == dict.fromkeys(protected, (401, "UNAUTHORIZED"))


def test_every_protected_route_refuses_every_other_role(client: TestClient, db: Session) -> None:
    site = _site(db)
    outsiders = [(op, r) for op, roles in OPERATIONS.items() if op not in PUBLIC for r in ROLES if r not in roles]

    answers = {(op, role): _knock(client, op, site.headers[role]) for op, role in outsiders}

    assert answers == dict.fromkeys(outsiders, (403, "FORBIDDEN"))


def test_every_protected_route_waits_for_the_first_pin_change(client: TestClient, db: Session) -> None:
    newcomer = _site(db).newcomer
    worker_routes = [op for op, roles in OPERATIONS.items() if "worker" in roles and op not in BEFORE_PIN_CHANGE]

    answers = {op: _knock(client, op, newcomer) for op in worker_routes}

    assert answers == dict.fromkeys(worker_routes, (403, "PIN_CHANGE_REQUIRED"))


def test_every_route_with_an_id_has_a_wrong_shop_case() -> None:
    assert set(CALLS) == {op for op in OPERATIONS if "{" in op and op not in PUBLIC}


@pytest.mark.parametrize("op", CALLS)
def test_an_id_of_another_shop_is_not_found(client: TestClient, db: Session, op: str) -> None:
    home, other = _site(db), _site(db)
    db.commit()

    for role in OPERATIONS[op]:
        own_record_only = op == "GET /api/workers/{worker_id}/passport" and role == "worker"
        expected = 403 if own_record_only else 404  # 403 whatever the id is, so it reveals nothing either
        assert _send(client, op, other.headers[role], home.rows) == expected, role

    # The same call by its rightful user gets through, so the refusals above are about who asked.
    rightful = "worker" if "contact_id" in op else OPERATIONS[op][0]
    assert _send(client, op, home.headers[rightful], home.rows) not in (401, 403, 404, 405, 422)


@pytest.mark.parametrize("op", COLLEAGUE_REFUSED)
def test_a_colleague_cannot_reach_another_workers_own_record(client: TestClient, db: Session, op: str) -> None:
    site = _site(db)
    db.commit()

    assert _send(client, op, site.colleague, site.rows) == COLLEAGUE_REFUSED[op]
