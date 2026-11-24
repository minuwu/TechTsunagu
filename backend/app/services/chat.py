"""Chat (WS12, XREQ-005): Threads, the Foreman's message loop, Wakatta Status, Follow-ups, Re-explanation, Nudge,
Task Board. The loop: draft (AI, stage 3) → send (stage 4, Tasks created) → answers (stage 5, Follow-ups).

Scope: everything is the caller's shop; a Thread of another shop is a 404, a Thread of the same shop the caller is
not in is a 403. Workers never get `correctIndex`."""

import hashlib
import json
import uuid
from datetime import datetime, timedelta

from sqlalchemy.orm import Session

from app.ai.client import Part
from app.ai.prompts import render
from app.ai.schemas import AiQuestion, MessageDraft, Reexplanation, Translation
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, encode_cursor
from app.domain.ai_checks import GlossaryTerm as AiTerm
from app.domain.ai_checks import message_problems, reexplain_problems
from app.domain.chat import can_move, missed, wakatta_status
from app.domain.recurrence import TOKYO
from app.media import audio_mime
from app.models.chat import FollowUp, Message, MessageAnswer, Task, Thread
from app.models.work import Confusion
from app.repositories import accounts
from app.repositories import chat as repo
from app.repositories import work as work_repo
from app.schemas.chat import (
    AnswerResult,
    AnswersIn,
    AskForemanIn,
    DraftEdit,
    FollowUpOut,
    FollowUpUpdate,
    MessageForSender,
    MessageForWorker,
    MessagesPage,
    NudgeIn,
    ReexplainDraft,
    ReexplainIn,
    SendIn,
    TaskCreate,
    TaskDraft,
    TaskOut,
    TaskUpdate,
    ThreadOut,
)
from app.schemas.common import CheckQuestion
from app.services.ai_runner import AiRequest, AiRunner
from app.services.asks import LANGUAGE_NAMES
from app.services.auth import Principal
from app.services.notes import ai_languages

PROMPT_VERSION = 1  # translate
MESSAGE_PROMPT_VERSION = 2  # draft_message, reexplain: per-shop languages (XREQ-011)
NUDGE_WINDOW = timedelta(minutes=10)  # one Nudge per message and Worker in this window


def _me(who: Principal) -> tuple[int, int]:
    if who.shop_id is None or who.user_id is None:
        raise ApiError(403, "FORBIDDEN", "Not allowed")
    return who.shop_id, who.user_id


def _not_found() -> ApiError:
    return ApiError(404, "NOT_FOUND", "Not found")


def _thread(db: Session, who: Principal, thread_id: int) -> Thread:
    shop_id, user_id = _me(who)
    t = repo.thread(db, shop_id, thread_id)
    if t is None:
        raise _not_found()
    if repo.membership(db, shop_id, t.id, user_id) is None:
        raise ApiError(403, "FORBIDDEN", "You are not in this Thread")
    return t


def _poster(db: Session, who: Principal, thread_id: int) -> Thread:
    """A Thread the caller may post in now: a member, and an admin when it is announce-only (XREQ-019)."""
    shop_id, user_id = _me(who)
    t = _thread(db, who, thread_id)
    member = repo.membership(db, shop_id, t.id, user_id)
    if t.announce_only and (member is None or member.role != "admin"):
        raise ApiError(403, "FORBIDDEN", "Only the admins post in this Thread; you can still answer its Checks")
    return t


def _sent_message(db: Session, who: Principal, message_id: int, *, for_update: bool = False) -> Message:
    """A sent message of a Thread the caller is in (a Re-explanation only for its Worker or sender), from the
    caller's history start on: a member added later cannot reach older messages by id."""
    shop_id, user_id = _me(who)
    m = repo.message(db, shop_id, message_id, for_update=for_update)
    if m is None or m.sent_at is None:
        raise _not_found()
    _thread(db, who, m.thread_id)
    if m.kind == "reexplain" and user_id not in (m.to_user_id, m.from_user_id):
        raise _not_found()
    member = repo.membership(db, shop_id, m.thread_id, user_id)
    start = member.history_from if member else None
    if start is not None and m.sent_at < start and m.from_user_id != user_id:
        raise _not_found()
    return m


def _text(m: Message) -> dict[str, str]:
    text = {k: v for k, v in m.translations.items() if v}
    return {"ja": m.text_ja, **text} if m.text_ja else text


def _fields(m: Message) -> dict[str, object]:
    return {
        "id": str(m.id),
        "thread_id": str(m.thread_id),
        "from_user_id": str(m.from_user_id),
        "kind": m.kind,
        "language": m.language,
        "stage": m.stage,
        "needs_edit": m.needs_edit,
        "transcript_ja": m.transcript_ja,
        "text": _text(m),
        "to_user_id": str(m.to_user_id) if m.to_user_id else None,
        "ref_message_id": str(m.ref_message_id) if m.ref_message_id else None,
        "created_at": m.created_at,
        "sent_at": m.sent_at,
        "event": m.event,
    }


def _for_sender(
    m: Message, questions: list[dict[str, object]], results: list[dict[str, object]] | None = None
) -> MessageForSender:
    return MessageForSender.model_validate(
        {**_fields(m), "questions": questions, "task_drafts": m.task_drafts, "results": results or []}
    )


def _results(db: Session, shop_id: int, thread_id: int, rows: list[Message]) -> dict[int, list[dict[str, object]]]:
    """Per message, each Worker's answer, read state and nudge, for the Foreman's Wakatta card."""
    if not rows:
        return {}
    ids = [m.id for m in rows]
    answers = {(a.message_id, a.user_id): a for a in repo.answers_of(db, shop_id, ids)}
    nudges, workers = repo.nudges_of(db, shop_id, ids), repo.worker_members(db, shop_id, thread_id)
    out: dict[int, list[dict[str, object]]] = {}
    for m in rows:
        sent = m.sent_at
        out[m.id] = [
            {
                "user_id": str(u),
                "status": a.status if (a := answers.get((m.id, u))) else None,
                "answers": list(a.answers) if a else None,
                "read": a is not None or (read_at is not None and sent is not None and read_at >= sent),
                "nudged": (m.id, u) in nudges and a is None,
            }
            for u, read_at in workers
            if m.to_user_id is None or m.to_user_id == u
        ]
    return out


def _key(questions: list[dict[str, object]]) -> list[int]:
    return [int(str(q["correctIndex"])) for q in questions]


def _for_worker(
    m: Message, questions: list[dict[str, object]], mine: MessageAnswer | None, nudged: bool, asked: bool = False
) -> MessageForWorker:
    hidden = [{k: v for k, v in q.items() if k != "correctIndex"} for q in questions]  # the key only once answered
    return MessageForWorker.model_validate(
        {
            **_fields(m),
            "questions": hidden,
            "my_status": mine.status if mine else None,
            "nudged": nudged and mine is None,
            "my_answers": list(mine.answers) if mine else None,
            "answer_key": _key(questions) if mine else None,
            "asked": asked,
        }
    )


def _sender_view(db: Session, m: Message) -> MessageForSender:
    check = repo.checks_of(db, m.shop_id, [m.id]).get(m.id)
    return _for_sender(m, check.questions if check else [])


def list_threads(db: Session, who: Principal) -> list[ThreadOut]:
    shop_id, user_id = _me(who)
    rows = repo.threads_of(db, shop_id, user_id)
    ids = [t.id for t, _, _ in rows]
    members, mine = repo.member_ids(db, shop_id, ids), repo.my_memberships(db, shop_id, user_id, ids)
    return [
        ThreadOut.model_validate(
            {
                "id": str(t.id),
                "title_ja": t.title_ja,
                "kind": t.kind,
                "member_ids": [str(u) for u in members[t.id]],
                "unread": unread,
                "last_message_at": last,
                "announce_only": t.announce_only,
                "muted": mine[t.id].muted,
                "pinned": mine[t.id].pinned,
                "archived": mine[t.id].archived,
            }
        )
        for t, unread, last in rows
    ]


def messages(db: Session, who: Principal, thread_id: int, after_id: int | None, now: datetime) -> MessagesPage:
    """The poll (every 2 s, with ETag): messages after the cursor; reading them moves the unread marker."""
    shop_id, user_id = _me(who)
    t = _thread(db, who, thread_id)
    member = repo.membership(db, shop_id, t.id, user_id)
    rows = repo.messages_after(db, shop_id, t.id, user_id, after_id, member.history_from if member else None)
    ids = [m.id for m in rows]
    questions = {i: c.questions for i, c in repo.checks_of(db, shop_id, ids).items()}
    items: list[MessageForSender | MessageForWorker]
    if who.role == "foreman":
        results = _results(db, shop_id, t.id, [m for m in rows if questions.get(m.id) and m.sent_at is not None])
        items = [_for_sender(m, questions.get(m.id, []), results.get(m.id, [])) for m in rows]
    else:
        mine = {a.message_id: a for a in repo.answers_of(db, shop_id, ids, user_id)}
        nudged, asked = repo.nudged_messages(db, shop_id, user_id, ids), repo.asked_messages(db, shop_id, user_id, ids)
        items = [_for_worker(m, questions.get(m.id, []), mine.get(m.id), m.id in nudged, m.id in asked) for m in rows]
    newest = max((m.sent_at for m in rows if m.sent_at is not None and m.from_user_id != user_id), default=None)
    if member is not None and newest is not None and (member.last_read_at is None or member.last_read_at < newest):
        member.last_read_at = now
    db.commit()
    return MessagesPage(items=items)


def _glossary(db: Session, shop_id: int) -> list[AiTerm]:
    return [
        AiTerm(id=str(t.id), ja=t.ja, reading=t.reading, translations=t.translations)
        for t in work_repo.glossary(db, shop_id)
    ]


def _glossary_block(terms: list[AiTerm]) -> str:
    return json.dumps([{"ja": t.ja, "reading": t.reading, **t.translations} for t in terms], ensure_ascii=False)


def _question(q: AiQuestion, n: int, terms: list[AiTerm], languages: tuple[str, ...]) -> dict[str, object]:
    term_ids = {t.ja: t.id for t in terms}
    return {
        "id": f"q{n}",
        "termId": term_ids.get(q.term_ja) if q.term_ja else None,
        "prompt": q.prompt.only(languages),
        "options": [o.only(languages) for o in q.options],
        "correctIndex": q.correct_index,
    }


def to_japanese(ai: AiRunner, parts: list[Part], source: str, scope: str, key: bytes, now: datetime) -> str | None:
    request = AiRequest(
        pipeline="translate",
        prompt=render(
            "translate",
            PROMPT_VERSION,
            source_lang=LANGUAGE_NAMES.get(source, source),
            target_lang="Japanese",
            glossary="[]",
        ),
        parts=parts,
        schema=Translation,
        scope=scope,
        cache_input=key,
    )
    result = ai.run(request, lambda _: [], now)
    return result.value.text if result.value else None


def post_message(
    db: Session,
    who: Principal,
    thread_id: int,
    text: str | None,
    audio: bytes | None,
    key: uuid.UUID,
    ai: AiRunner,
    now: datetime,
) -> MessageForSender:
    """A Foreman's message becomes a draft (stage 3, or 0 with needsEdit when the AI is down); a Worker's message is
    translated to Japanese and sent at once."""
    shop_id, user_id = _me(who)
    t = _poster(db, who, thread_id)
    existing = repo.message_by_key(db, shop_id, user_id, key)
    if existing is not None:
        return _sender_view(db, existing)
    text = text.strip() if text else None
    if bool(text) == bool(audio):
        raise ApiError(422, "VALIDATION_ERROR", "Send text or a recording, not both", {"fields": [{"loc": ["body"]}]})
    mime = audio_mime(audio) if audio else None
    source = [Part.blob(audio, mime)] if audio and mime else []
    if who.role == "foreman":
        return _draft(db, who, t, text, source, audio, key, ai, now)

    language = next((u.language for u in repo.members(db, shop_id, t.id) if u.id == user_id), "en")
    db.commit()  # no transaction is held during the model call (§8.3)
    if language == "ja":
        text_ja = text
    else:
        parts = source or [Part.text(f"<source_text>{text}</source_text>")]
        text_ja = to_japanese(ai, parts, language, f"shop:{shop_id}", audio or (text or "").encode(), now)
    row = Message(
        shop_id=shop_id,
        thread_id=t.id,
        from_user_id=user_id,
        kind="voice" if audio else "text",
        language=language,
        text_ja=text_ja,  # None when the model is down: the Foreman sees the Worker's own words
        translations={language: text} if text and language != "ja" else {},
        stage=4,
        idempotency_key=key,
        sent_at=now,
    )
    db.add(row)
    db.commit()
    return _sender_view(db, row)


def _draft_ai(
    db: Session,
    who: Principal,
    thread_id: int,
    text: str | None,
    source: list[Part],
    audio: bytes | None,
    ai: AiRunner,
    now: datetime,
) -> tuple[MessageDraft | None, list[AiTerm], tuple[str, ...]]:
    """The draft_message pipeline on a Foreman's text or recording. Commits first: no transaction during the call."""
    shop_id, _ = _me(who)
    terms = _glossary(db, shop_id)
    people = repo.members(db, shop_id, thread_id)
    machines = work_repo.all_machines(db, shop_id)
    languages = ai_languages(db, shop_id)
    db.commit()  # no transaction is held during the model call (§8.3)

    glossary = _glossary_block(terms)
    roster = "\n".join(f"{u.id} | {u.display_name}" for u in people)
    shop_machines = "\n".join(f"{m.id} | {m.code} | {m.name.get('ja', '')} | {m.name.get('en', '')}" for m in machines)
    # The Tokyo date is part of the key: 「明日の朝8時」 cached yesterday must not replay yesterday's due time.
    today = now.astimezone(TOKYO).date().isoformat()
    context = hashlib.sha256((glossary + roster + shop_machines + today + ",".join(languages)).encode()).digest()
    request = AiRequest(
        pipeline="draft_message",
        prompt=render(
            "draft_message",
            MESSAGE_PROMPT_VERSION,
            glossary=glossary,
            languages=", ".join(languages),
            members=roster,
            machines=shop_machines,
            now=now.astimezone(TOKYO).isoformat(timespec="minutes"),
        ),
        parts=source or [Part.text(f"<foreman_message>{text}</foreman_message>")],
        schema=MessageDraft,
        scope=f"shop:{shop_id}",
        cache_input=(audio or (text or "").encode()) + context,
        languages=languages,
    )
    member_ids, machine_ids = {str(u.id) for u in people}, {str(m.id) for m in machines}
    result = ai.run(request, lambda d: message_problems(d, terms, languages, member_ids, machine_ids), now)
    return result.value, terms, languages


def _apply_draft(
    db: Session, row: Message, d: MessageDraft | None, text: str | None, terms: list[AiTerm], languages: tuple[str, ...]
) -> MessageForSender:
    """Writes the AI's draft (or the degraded one: stage 0, needsEdit) on an unsent message and returns the view."""
    row.transcript_ja = d.transcript_ja if d else text
    row.text_ja = d.text.ja if d else text
    row.translations = {lang: getattr(d.text, lang) or "" for lang in languages} if d else {}
    row.stage = 3 if d else 0
    row.needs_edit = d is None  # degraded (§10.4): the Foreman finishes the message before sending
    drafts = [
        TaskDraft.model_validate(
            {
                "title": task.title.only(languages),
                "assignee_ids": task.assignee_ids,
                "due_at": task.due_at,
                "machine_id": task.machine_id,
            }
        )
        for task in (d.tasks if d else [])
    ]
    row.task_drafts = [t.model_dump(mode="json", by_alias=True) for t in drafts]
    db.flush()
    questions = [_question(q, n, terms, languages) for n, q in enumerate(d.questions, start=1)] if d else []
    repo.set_check(db, row.shop_id, row.id, questions)
    db.commit()
    return _for_sender(row, questions)


def _draft(
    db: Session,
    who: Principal,
    t: Thread,
    text: str | None,
    source: list[Part],
    audio: bytes | None,
    key: uuid.UUID,
    ai: AiRunner,
    now: datetime,
) -> MessageForSender:
    shop_id, user_id = _me(who)
    thread_id = t.id
    d, terms, languages = _draft_ai(db, who, thread_id, text, source, audio, ai, now)
    raced = repo.message_by_key(db, shop_id, user_id, key)
    if raced is not None:
        return _sender_view(db, raced)
    row = Message(
        shop_id=shop_id,
        thread_id=thread_id,
        from_user_id=user_id,
        kind="voice" if audio else "text",
        language="ja",
        stage=0,
        idempotency_key=key,
    )
    db.add(row)
    return _apply_draft(db, row, d, text, terms, languages)


def _own_draft(db: Session, who: Principal, message_id: int, *, for_update: bool = False) -> Message:
    shop_id, user_id = _me(who)
    m = repo.message(db, shop_id, message_id, for_update=for_update)
    if m is None or m.from_user_id != user_id:
        raise _not_found()
    if m.sent_at is not None:
        raise ApiError(409, "ALREADY_SENT", "This message was already sent")
    _poster(db, who, m.thread_id)  # still a member, and allowed to post, when the draft is edited
    return m


def edit_draft(
    db: Session, who: Principal, message_id: int, body: DraftEdit, ai: AiRunner, now: datetime
) -> MessageForSender:
    """XREQ-022: the Foreman corrects the Japanese of an unsent draft; translations and Check Questions are made again
    from it (a degraded result keeps the text with needsEdit, as for a new draft)."""
    thread_id = _own_draft(db, who, message_id).thread_id
    d, terms, languages = _draft_ai(db, who, thread_id, body.text_ja, [], None, ai, now)
    m = _own_draft(db, who, message_id, for_update=True)  # sent meanwhile → 409
    return _apply_draft(db, m, d, body.text_ja, terms, languages)


def _check_people(db: Session, shop_id: int, user_ids: list[int], machine_id: int | None) -> None:
    unknown = set(user_ids) - repo.active_users(db, shop_id, user_ids)
    if unknown or (machine_id is not None and not repo.machine_ids(db, shop_id, [machine_id])):
        raise ApiError(422, "VALIDATION_ERROR", "Assignees and the Machine must belong to this shop")


def send(db: Session, who: Principal, message_id: int, body: SendIn, now: datetime) -> MessageForSender:
    """The Foreman's edited draft goes to the Thread (stage 4) and the Tasks they kept go on the Task Board."""
    shop_id, user_id = _me(who)
    m = _own_draft(db, who, message_id, for_update=True)
    tasks: list[tuple[TaskDraft, list[int], int | None]] = []
    for draft in body.tasks:
        people, machine = [int(a) for a in draft.assignee_ids], int(draft.machine_id) if draft.machine_id else None
        _check_people(db, shop_id, people, machine)
        tasks.append((draft, people, machine))
    m.text_ja = body.text["ja"]
    m.translations = {str(k): v for k, v in body.text.items() if k != "ja"}
    questions: list[dict[str, object]] = [
        {**q.model_dump(by_alias=True), "id": f"q{n}"} for n, q in enumerate(body.questions, start=1)
    ]
    repo.set_check(db, shop_id, m.id, questions)
    for draft, people, machine in tasks:
        row = Task(
            shop_id=shop_id,
            title={str(k): v for k, v in draft.title.items()},
            due_at=draft.due_at,
            machine_id=machine,
            from_message_id=m.id,
            created_by=user_id,
        )
        db.add(row)
        db.flush()
        repo.set_assignees(db, shop_id, row.id, people)
    m.stage, m.sent_at, m.needs_edit, m.task_drafts = 4, now, False, []
    db.commit()
    return _for_sender(m, questions)


def answer(db: Session, who: Principal, message_id: int, body: AnswersIn, now: datetime) -> AnswerResult:
    """Graded on the server → Wakatta Status. A missed question on a Glossary Term makes a Confusion and a Follow-up.
    Answering twice returns the first result."""
    shop_id, user_id = _me(who)
    m = _sent_message(db, who, message_id, for_update=True)
    done = repo.answer_of(db, shop_id, m.id, user_id)
    check = repo.checks_of(db, shop_id, [m.id]).get(m.id)
    if done is not None:
        follow_ups = repo.follow_ups_of(db, shop_id, m.id, user_id)
        return AnswerResult.model_validate(
            {
                "status": done.status,
                "follow_up_ids": [str(f.id) for f in follow_ups],
                "answers": list(done.answers),
                "answer_key": _key(check.questions) if check else [],
            }
        )
    if check is None:
        raise ApiError(422, "VALIDATION_ERROR", "This message has no questions")
    questions = [CheckQuestion.model_validate(q) for q in check.questions]
    correct = [q.correct_index for q in questions]
    try:
        wrong, status = missed(correct, body.answers), wakatta_status(correct, body.answers)
    except ValueError as exc:
        raise ApiError(422, "VALIDATION_ERROR", str(exc), {"fields": [{"loc": ["body", "answers"]}]}) from exc
    db.add(MessageAnswer(shop_id=shop_id, message_id=m.id, user_id=user_id, answers=body.answers, status=status))
    terms = {t.id: t.ja for t in work_repo.glossary(db, shop_id)}
    follow_ups: list[FollowUp] = []
    for i in wrong:
        q = questions[i]
        term_id = int(q.term_id) if q.term_id else None
        if term_id is None or term_id not in terms:
            continue
        db.add(Confusion(shop_id=shop_id, message_id=m.id, worker_id=user_id, question_id=q.id, term_id=term_id))
        follow = FollowUp(shop_id=shop_id, message_id=m.id, user_id=user_id, term_ja=terms[term_id])
        db.add(follow)
        follow_ups.append(follow)
    if m.kind == "reexplain" and status == "green" and m.ref_message_id is not None:
        for f in repo.follow_ups_of(db, shop_id, m.ref_message_id, user_id):
            if f.status != "understood":  # passing the Re-explanation closes the Follow-up; no person did
                f.status, f.resolved_at = "understood", now
    m.stage = 5
    db.flush()
    db.commit()
    return AnswerResult.model_validate(
        {
            "status": status,
            "follow_up_ids": [str(f.id) for f in follow_ups],
            "answers": body.answers,
            "answer_key": correct,
        }
    )


def _follow_up_out(f: FollowUp, name: str) -> FollowUpOut:
    return FollowUpOut.model_validate(
        {
            "id": str(f.id),
            "message_id": str(f.message_id),
            "user_id": str(f.user_id),
            "display_name": name,
            "term_ja": f.term_ja,
            "status": f.status,
            "note": f.note,
            "created_at": f.created_at,
            "resolved_at": f.resolved_at,
        }
    )


def list_follow_ups(db: Session, who: Principal, status: str | None, page: PageParams) -> Page[FollowUpOut]:
    shop_id, user_id = _me(who)
    rows = repo.follow_ups_page(db, shop_id, user_id, status, page.after_id, page.limit)
    shown = rows[: page.limit]
    cursor = encode_cursor(shown[-1][0].id) if len(rows) > page.limit else None
    return Page(items=[_follow_up_out(f, n) for f, n in shown], next_cursor=cursor)


def update_follow_up(
    db: Session, who: Principal, follow_up_id: int, body: FollowUpUpdate, now: datetime
) -> FollowUpOut:
    shop_id, user_id = _me(who)
    f = repo.follow_up(db, shop_id, follow_up_id, for_update=True)
    m = repo.message(db, shop_id, f.message_id) if f is not None else None
    if f is None or m is None:
        raise _not_found()
    _thread(db, who, m.thread_id)  # 403 for a Foreman who is not in that Thread
    if not can_move(f.status, body.status):  # pyright: ignore[reportArgumentType]  # CHECK constraint
        raise ApiError(409, "CONFLICT", f"A Follow-up that is {f.status} cannot become {body.status}")
    f.status, f.note, f.resolved_by, f.resolved_at = body.status, body.note, user_id, now
    db.commit()
    names = work_repo.display_names(db, shop_id, [f.user_id])
    return _follow_up_out(f, names.get(f.user_id, ""))


def _reexplain_target(db: Session, who: Principal, message_id: int, body: ReexplainIn) -> Message:
    shop_id, _ = _me(who)
    m = _sent_message(db, who, message_id)
    if repo.membership(db, shop_id, m.thread_id, int(body.user_id)) is None:
        raise ApiError(422, "VALIDATION_ERROR", "That Worker is not in this Thread")
    return m


def _reexplain_ai(
    db: Session, who: Principal, m: Message, term_ja: str, ai: AiRunner, now: datetime
) -> tuple[Reexplanation, list[AiTerm], tuple[str, ...]]:
    shop_id, _ = _me(who)
    terms = _glossary(db, shop_id)
    languages = ai_languages(db, shop_id)
    original, message_id = m.text_ja or "", m.id
    db.commit()  # no transaction is held during the model call (§8.3)
    request = AiRequest(
        pipeline="reexplain",
        prompt=render(
            "reexplain",
            MESSAGE_PROMPT_VERSION,
            glossary=_glossary_block(terms),
            term=term_ja,
            languages=", ".join(languages),
        ),
        parts=[Part.text(f"<original_message>{original}</original_message>")],
        schema=Reexplanation,
        scope=f"shop:{shop_id}",
        cache_input=f"{message_id}:{term_ja}:{original}".encode(),
        languages=languages,
    )
    result = ai.run(request, lambda r: reexplain_problems(r, term_ja, terms, languages), now)
    if result.value is None:
        raise ApiError(503, "AI_UNAVAILABLE", "The re-explanation could not be made now; explain it in person")
    return result.value, terms, languages


def reexplain_draft(
    db: Session, who: Principal, message_id: int, body: ReexplainIn, ai: AiRunner, now: datetime
) -> ReexplainDraft:
    """XREQ-022: the simpler version for the Foreman to read and edit first; nothing is stored or sent."""
    m = _reexplain_target(db, who, message_id, body)
    r, terms, languages = _reexplain_ai(db, who, m, body.term_ja, ai, now)
    return ReexplainDraft.model_validate(
        {"text": r.text.only(languages), "question": _question(r.question, 1, terms, languages)}
    )


def reexplain(
    db: Session, who: Principal, message_id: int, body: ReexplainIn, ai: AiRunner, now: datetime
) -> MessageForSender:
    """A simpler sentence on one missed term, sent to that Worker only, with one new question on the term. With the
    Foreman's edited `text` and `question` (XREQ-022) it is sent as written; otherwise the AI writes it."""
    shop_id, user_id = _me(who)
    if (body.text is None) != (body.question is None):
        raise ApiError(
            422, "VALIDATION_ERROR", "Send both text and question, or neither", {"fields": [{"loc": ["body"]}]}
        )
    m = _reexplain_target(db, who, message_id, body)
    target, thread_id = int(body.user_id), m.thread_id
    if body.text is not None and body.question is not None:
        text = {str(k): v for k, v in body.text.items()}
        question: dict[str, object] = {**body.question.model_dump(by_alias=True), "id": "q1"}
    else:
        r, terms, languages = _reexplain_ai(db, who, m, body.term_ja, ai, now)
        text = {"ja": r.text.ja, **{lang: getattr(r.text, lang) or "" for lang in languages}}
        question = _question(r.question, 1, terms, languages)
    row = Message(
        shop_id=shop_id,
        thread_id=thread_id,
        from_user_id=user_id,
        kind="reexplain",
        language="ja",
        text_ja=text["ja"],
        translations={k: v for k, v in text.items() if k != "ja"},
        ref_message_id=message_id,
        to_user_id=target,
        stage=4,
        sent_at=now,
    )
    db.add(row)
    db.flush()
    repo.set_check(db, shop_id, row.id, [question])
    db.commit()
    return _for_sender(row, [question])


ASK_TEXT = {
    "ja": "「{term}」がわかりません。もう一度教えてください。",
    "en": "I don't understand \u201c{term}\u201d. Please explain it again.",
}


def ask_foreman(db: Session, who: Principal, message_id: int, body: AskForemanIn, now: datetime) -> MessageForSender:
    """XREQ-022: a Worker taps "ask again" on a term of a sent message. A fixed sentence (no AI) goes to the Thread as
    an `ask` message, and the Foreman gets a Follow-up on the term unless one is already open."""
    shop_id, user_id = _me(who)
    m = _sent_message(db, who, message_id)
    language = next((u.language for u in repo.members(db, shop_id, m.thread_id) if u.id == user_id), "en")
    texts = {lang: t.format(term=body.term_ja) for lang, t in ASK_TEXT.items()}
    if (same := repo.ask_of(db, shop_id, user_id, m.id, texts["ja"])) is not None:
        return _for_sender(same, [])  # a second tap on the same term sends nothing new
    row = Message(
        shop_id=shop_id,
        thread_id=m.thread_id,
        from_user_id=user_id,
        kind="ask",
        language=language,
        text_ja=texts["ja"],
        translations={k: v for k, v in texts.items() if k != "ja"},
        ref_message_id=m.id,
        stage=4,
        sent_at=now,
    )
    db.add(row)
    open_terms = {f.term_ja for f in repo.follow_ups_of(db, shop_id, m.id, user_id) if f.status != "understood"}
    if body.term_ja not in open_terms:
        db.add(FollowUp(shop_id=shop_id, message_id=m.id, user_id=user_id, term_ja=body.term_ja))
    db.commit()
    return _for_sender(row, [])


def nudge(db: Session, who: Principal, message_id: int, body: NudgeIn, now: datetime) -> None:
    """Shows the Worker that the Foreman is waiting for their answer. Rate-limited and audited."""
    shop_id, user_id = _me(who)
    m = _sent_message(db, who, message_id)
    target = int(body.user_id)
    if repo.membership(db, shop_id, m.thread_id, target) is None:
        raise ApiError(422, "VALIDATION_ERROR", "That Worker is not in this Thread")
    if accounts.count_attempt(db, f"nudge:{m.id}:{target}", now, NUDGE_WINDOW) > 1:
        db.commit()
        raise ApiError(429, "RATE_LIMITED", "Already nudged; wait a few minutes", headers={"Retry-After": "600"})
    accounts.audit(db, f"nudge:{target}", user_id, shop_id, subject_id=m.id)
    db.commit()


def _task_out(t: Task, assignees: list[int]) -> TaskOut:
    return TaskOut.model_validate(
        {
            "id": str(t.id),
            "title": t.title,
            "column": t.board_column,
            "assignee_ids": [str(a) for a in assignees],
            "due_at": t.due_at,
            "machine_id": str(t.machine_id) if t.machine_id else None,
            "from_message_id": str(t.from_message_id) if t.from_message_id else None,
            "version": t.version,
            "updated_at": t.updated_at,
        }
    )


def list_tasks(db: Session, who: Principal, page: PageParams, machine_id: int | None = None) -> Page[TaskOut]:
    """The shop's Task Board, optionally only one Machine's Tasks (another shop's Machine id finds nothing)."""
    shop_id, _ = _me(who)
    rows = repo.tasks_page(db, shop_id, page.after_id, page.limit, machine_id)
    shown = rows[: page.limit]
    assignees = repo.assignees_of(db, shop_id, [t.id for t in shown])
    cursor = encode_cursor(shown[-1].id) if len(rows) > page.limit else None
    return Page(items=[_task_out(t, assignees[t.id]) for t in shown], next_cursor=cursor)


def create_task(db: Session, who: Principal, body: TaskCreate, key: uuid.UUID | None = None) -> TaskOut:
    shop_id, user_id = _me(who)
    if key is not None and (done := repo.task_by_key(db, shop_id, key)) is not None:
        if done.created_by != user_id:
            raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for another record")
        return _task_out(done, repo.assignees_of(db, shop_id, [done.id])[done.id])  # a retry returns the first
    people, machine = [int(a) for a in body.assignee_ids], int(body.machine_id) if body.machine_id else None
    _check_people(db, shop_id, people, machine)
    row = Task(
        shop_id=shop_id,
        title={str(k): v for k, v in body.title.items()},
        due_at=body.due_at,
        machine_id=machine,
        created_by=user_id,
        idempotency_key=key,
    )
    db.add(row)
    db.flush()
    repo.set_assignees(db, shop_id, row.id, people)
    db.commit()
    return _task_out(row, list(dict.fromkeys(people)))


def update_task(db: Session, who: Principal, task_id: int, body: TaskUpdate, version: int, now: datetime) -> TaskOut:
    """A move or edit with `If-Match: <version>`: 412 when someone else changed the card first."""
    shop_id, _ = _me(who)
    t = repo.task(db, shop_id, task_id, for_update=True)
    if t is None:
        raise _not_found()
    if t.version != version:
        raise ApiError(412, "VERSION_CONFLICT", "Someone moved this Task first; reload the board")
    if who.role != "foreman" and ("due_at" in body.model_fields_set or body.assignee_ids is not None):
        raise ApiError(403, "FORBIDDEN", "Workers can move a Task; the Foreman changes who and when")
    if body.column is not None:
        t.board_column = body.column
    if "due_at" in body.model_fields_set:
        t.due_at = body.due_at
    if body.assignee_ids is not None:
        people = [int(a) for a in body.assignee_ids]
        _check_people(db, shop_id, people, None)
        repo.set_assignees(db, shop_id, t.id, people)
    t.version, t.updated_at = t.version + 1, now
    db.commit()
    return _task_out(t, repo.assignees_of(db, shop_id, [t.id])[t.id])
