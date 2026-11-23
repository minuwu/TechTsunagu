"""Chat demo seed (WS12, XREQ-005 item 7): loads seed_data/chat.json on top of `seed_work`, whose refs hold the
Foreman, Workers and Machines. Called from `app.seed.main` in the same transaction."""

from datetime import datetime, time, timedelta

from sqlalchemy.orm import Session

from app.domain.chat import wakatta_status
from app.domain.recurrence import TOKYO
from app.models.chat import Message, MessageAnswer, MessageCheck, Task, TaskAssignee, Thread, ThreadMember
from app.seed import Refs, _key, _load  # pyright: ignore[reportPrivateUsage]  # one seed, split by flow


def seed_chat(db: Session, refs: Refs, now: datetime) -> Refs:
    chat = _load("chat.json")
    shop_id = refs["s-sasebo"]

    for t in chat["threads"]:
        thread = Thread(shop_id=shop_id, title_ja=t["titleJa"], kind=t["kind"], created_by=refs[t["createdBy"]])
        db.add(thread)
        db.flush()
        refs[t["key"]] = thread.id
        db.add_all(
            ThreadMember(thread_id=thread.id, user_id=refs[u], shop_id=shop_id, last_read_at=now) for u in t["members"]
        )

    for m in chat["messages"]:
        at = now - timedelta(minutes=m["minutesAgo"])
        text = m["text"]
        row = Message(
            shop_id=shop_id,
            thread_id=refs[m["thread"]],
            from_user_id=refs[m["from"]],
            kind=m["kind"],
            language=m["language"],
            transcript_ja=m.get("transcriptJa"),
            text_ja=text["ja"],
            translations={k: v for k, v in text.items() if k != "ja"},
            ref_message_id=refs[m["ref"]] if m.get("ref") else None,
            stage=5 if m.get("answers") else 4,
            idempotency_key=_key(m["key"]),
            created_at=at,
            sent_at=at,
        )
        db.add(row)
        db.flush()
        refs[m["key"]] = row.id
        if m.get("questions"):
            db.add(MessageCheck(shop_id=shop_id, message_id=row.id, questions=m["questions"], created_at=at))
        correct = [q["correctIndex"] for q in m.get("questions", [])]
        for a in m.get("answers", []):
            status = wakatta_status(correct, a["answers"])
            db.add(
                MessageAnswer(
                    shop_id=shop_id, message_id=row.id, user_id=refs[a["worker"]], answers=a["answers"], status=status
                )
            )

    today = now.astimezone(TOKYO).date()
    for k in chat["tasks"]:
        hour, minute = (int(x) for x in k["dueTokyo"].split(":"))
        task = Task(
            shop_id=shop_id,
            title=k["title"],
            board_column=k["column"],
            due_at=datetime.combine(today + timedelta(days=k["dueDays"]), time(hour, minute), TOKYO),
            machine_id=refs[k["machine"]] if k["machine"] else None,
            from_message_id=refs[k["from"]] if k["from"] else None,
            created_by=refs["f-yamaguchi"],
        )
        db.add(task)
        db.flush()
        refs[k["key"]] = task.id
        db.add_all(TaskAssignee(task_id=task.id, user_id=refs[u], shop_id=shop_id) for u in k["assignees"])
    db.flush()
    return refs
