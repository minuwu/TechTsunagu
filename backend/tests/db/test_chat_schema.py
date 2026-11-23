"""WS12 Chat tables: the database itself keeps Chat rows inside one shop and consistent (XREQ-005 item 1)."""

from datetime import UTC, datetime

import pytest
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.models.chat import FollowUp, MessageAnswer, MessageCheck, TaskAssignee
from app.models.work import Confusion
from tests import factories as f


def test_a_thread_cannot_have_a_member_from_another_shop(db: Session) -> None:
    shop_a, shop_b = f.shop(db), f.shop(db)
    foreman_a = f.user(db, shop_a, role="foreman")
    worker_b = f.user(db, shop_b)

    with pytest.raises(IntegrityError, match="fk_thread_members_shop_id_user_id_users"), db.begin_nested():
        f.thread(db, shop_a, foreman_a, members=[worker_b])


def test_a_task_cannot_be_assigned_to_another_shops_worker(db: Session) -> None:
    shop_a, shop_b = f.shop(db), f.shop(db)
    foreman_a = f.user(db, shop_a, role="foreman")
    worker_b = f.user(db, shop_b)
    task = f.task(db, shop_a, foreman_a)

    with pytest.raises(IntegrityError, match="fk_task_assignees_shop_id_user_id_users"), db.begin_nested():
        db.add(TaskAssignee(task_id=task.id, user_id=worker_b.id, shop_id=shop_a.id))
        db.flush()


def test_a_message_is_sent_exactly_when_it_reaches_stage_4(db: Session) -> None:
    shop = f.shop(db)
    foreman = f.user(db, shop, role="foreman")
    thread = f.thread(db, shop, foreman)

    with pytest.raises(IntegrityError, match="sent_is_stage_4"), db.begin_nested():
        f.message(db, shop, thread, foreman, stage=4)
    sent = f.message(db, shop, thread, foreman, stage=4, sent_at=datetime.now(UTC))

    assert sent.sent_at is not None


def test_a_reexplanation_needs_a_worker_and_the_original_message(db: Session) -> None:
    shop = f.shop(db)
    foreman = f.user(db, shop, role="foreman")
    thread = f.thread(db, shop, foreman)

    with pytest.raises(IntegrityError, match="reexplain_has_target"), db.begin_nested():
        f.message(db, shop, thread, foreman, kind="reexplain")


def test_a_worker_answers_a_message_once(db: Session) -> None:
    shop = f.shop(db)
    foreman, worker = f.user(db, shop, role="foreman"), f.user(db, shop)
    thread = f.thread(db, shop, foreman, members=[worker])
    msg = f.message(db, shop, thread, foreman, stage=4, sent_at=datetime.now(UTC))
    db.add(MessageAnswer(shop_id=shop.id, message_id=msg.id, user_id=worker.id, answers=[1, -1], status="orange"))
    db.flush()

    with pytest.raises(IntegrityError, match="uq_message_answers_message_id_user_id"), db.begin_nested():
        db.add(MessageAnswer(shop_id=shop.id, message_id=msg.id, user_id=worker.id, answers=[1, 1], status="green"))
        db.flush()


def test_an_answer_outside_minus_one_to_two_is_refused(db: Session) -> None:
    shop = f.shop(db)
    foreman, worker = f.user(db, shop, role="foreman"), f.user(db, shop)
    thread = f.thread(db, shop, foreman, members=[worker])
    msg = f.message(db, shop, thread, foreman)

    with pytest.raises(IntegrityError, match="ck_message_answers_answers"), db.begin_nested():
        db.add(MessageAnswer(shop_id=shop.id, message_id=msg.id, user_id=worker.id, answers=[3], status="red"))
        db.flush()


def test_a_message_check_holds_one_to_three_questions(db: Session) -> None:
    shop = f.shop(db)
    foreman = f.user(db, shop, role="foreman")
    msg = f.message(db, shop, f.thread(db, shop, foreman), foreman)

    with pytest.raises(IntegrityError, match="one_to_three_questions"), db.begin_nested():
        db.add(MessageCheck(shop_id=shop.id, message_id=msg.id, questions=[]))
        db.flush()


def test_a_closed_follow_up_records_when_it_was_resolved(db: Session) -> None:
    shop = f.shop(db)
    foreman, worker = f.user(db, shop, role="foreman"), f.user(db, shop)
    msg = f.message(db, shop, f.thread(db, shop, foreman, members=[worker]), foreman)

    with pytest.raises(IntegrityError, match="resolved_when_closed"), db.begin_nested():
        db.add(FollowUp(shop_id=shop.id, message_id=msg.id, user_id=worker.id, term_ja="金型", status="clarified"))
        db.flush()


def test_a_chat_confusion_needs_no_check_or_machine_but_exactly_one_source(db: Session) -> None:
    shop = f.shop(db)
    foreman, worker = f.user(db, shop, role="foreman"), f.user(db, shop)
    msg = f.message(db, shop, f.thread(db, shop, foreman, members=[worker]), foreman)

    db.add(Confusion(shop_id=shop.id, message_id=msg.id, worker_id=worker.id, question_id="q1"))
    db.flush()
    with pytest.raises(IntegrityError, match="one_source"), db.begin_nested():
        db.add(Confusion(shop_id=shop.id, worker_id=worker.id, question_id="q1"))
        db.flush()
