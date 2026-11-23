"""Who gets a push and what it says (XREQ-014, MOBILE_PLATFORM_CONTRACT §1.4-1.5). Runs after the response (a
FastAPI background task) in its own session. The payload carries ids only, never a message's text."""

import logging
import uuid
from datetime import UTC, datetime, timedelta
from typing import Any, Literal

from sqlalchemy.orm import Session, sessionmaker

from app.push import PushClient
from app.repositories import accounts, devices
from app.repositories import chat as chat_repo
from app.repositories import work as work_repo

Kind = Literal["chat_message", "nudge"]

# Visible text per type: (title, body), {name} = the sender. ja and en only until a shop language is native-reviewed.
TEMPLATES: dict[Kind, dict[str, tuple[str, str]]] = {
    "chat_message": {
        "ja": ("{name}さんからメッセージ", "Wakatta?を開いて確認してください"),
        "en": ("{name} sent a message", "Open Wakatta? to read it"),
    },
    "nudge": {
        "ja": ("確認に答えてください", "{name}さんが待っています"),
        "en": ("Please answer the Check", "{name} is waiting"),
    },
}
CHANNEL: dict[Kind, str] = {"chat_message": "messages", "nudge": "safety"}
TTL: dict[Kind, str] = {"chat_message": "86400s", "nudge": "7200s"}
# A replayed POST (same Idempotency-Key) returns the same message: push it once.
# shortcut: one login_limits row per message; add a cleanup job if that table grows large.
PUSH_ONCE = timedelta(days=1)
log = logging.getLogger("wakatta.push")


def _message(kind: Kind, language: str, name: str, ids: dict[str, str], now: datetime) -> dict[str, Any]:
    title, body = TEMPLATES[kind].get(language) or TEMPLATES[kind]["en"]
    return {
        "notification": {"title": title.format(name=name), "body": body.format(name=name)},
        "android": {"priority": "HIGH", "ttl": TTL[kind], "notification": {"channel_id": CHANNEL[kind]}},
        "data": {
            "v": "1",
            "type": kind,
            "eventId": str(uuid.uuid4()),
            "sentAt": now.astimezone(UTC).isoformat().replace("+00:00", "Z"),
            **ids,
        },
    }


def _deliver(
    client: PushClient, db: Session, user_ids: list[int], kind: Kind, name: str, ids: dict[str, str], now: datetime
) -> None:
    for device, language in devices.reachable(db, user_ids, now):
        if client.send(device.fcm_token, _message(kind, language, name, ids, now)) == "gone":
            devices.forget(db, device.id)


class Pusher:
    def __init__(self, client: PushClient | None, factory: sessionmaker[Session]) -> None:
        self._client, self._factory = client, factory

    def message_sent(self, shop_id: int, message_id: int) -> None:
        """A sent message: its addressee (Re-explanation), else every Thread member but the sender. Drafts: nothing."""
        client = self._client
        if client is None:
            return
        now = datetime.now(UTC)
        try:
            with self._factory() as db:
                m = chat_repo.message(db, shop_id, message_id)
                if m is None or m.sent_at is None:
                    return
                if accounts.count_attempt(db, f"push:message:{m.id}", now, PUSH_ONCE) > 1:
                    db.commit()
                    return
                members = [u.id for u in chat_repo.members(db, shop_id, m.thread_id) if u.id != m.from_user_id]
                to = [m.to_user_id] if m.to_user_id is not None else members
                name = work_repo.display_names(db, shop_id, [m.from_user_id]).get(m.from_user_id, "")
                ids = {"threadId": str(m.thread_id), "messageId": str(m.id)}
                _deliver(client, db, to, "chat_message", name, ids, now)
                db.commit()
        except Exception:  # a push must never fail the request that already succeeded; on-call reads this log
            log.exception("push: chat_message for message %s failed", message_id)

    def nudged(self, shop_id: int, message_id: int, worker_id: int, foreman_id: int) -> None:
        client = self._client
        if client is None:
            return
        now = datetime.now(UTC)
        try:
            with self._factory() as db:
                m = chat_repo.message(db, shop_id, message_id)
                if m is None:
                    return
                name = work_repo.display_names(db, shop_id, [foreman_id]).get(foreman_id, "")
                ids = {"threadId": str(m.thread_id), "messageId": str(m.id)}
                _deliver(client, db, [worker_id], "nudge", name, ids, now)
                db.commit()
        except Exception:  # see message_sent
            log.exception("push: nudge for message %s failed", message_id)
