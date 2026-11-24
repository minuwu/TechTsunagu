"""Chat routes, WS12 (XREQ-005): Threads, Messages, answers, Follow-ups, Re-explanation, Nudge, Task Board.
Thin: parse → service → envelope."""

import uuid
from typing import Annotated, Literal

from fastapi import APIRouter, BackgroundTasks, Depends, File, Form, Header, Query, Request, Response, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.conditional import conditional_json
from app.api.envelope import Envelope
from app.api.errors import ApiError
from app.api.pagination import Page, PageParams, page_params
from app.media import MAX_AUDIO_BYTES
from app.schemas.chat import (
    AnswerResult,
    AnswersIn,
    AskForemanIn,
    DraftEdit,
    FollowUpOut,
    FollowUpUpdate,
    MessageForSender,
    MessagesPage,
    NudgeIn,
    ReexplainDraft,
    ReexplainIn,
    SendIn,
    TaskCreate,
    TaskOut,
    TaskUpdate,
    ThreadOut,
    Transcription,
    UnansweredCheck,
)
from app.schemas.common import IdIn
from app.services import chat
from app.services.ai_runner import AiRunner
from app.services.auth import Principal
from app.services.push import Pusher

router = APIRouter(prefix="/api", tags=["chat"])
Foreman = Annotated[Principal, Depends(require("foreman"))]
Worker = Annotated[Principal, Depends(require("worker"))]
ShopMember = Annotated[Principal, Depends(require("foreman", "worker"))]
Paging = Annotated[PageParams, Depends(page_params)]
IdempotencyKey = Annotated[uuid.UUID, Header(alias="Idempotency-Key")]
OptionalKey = Annotated[uuid.UUID | None, Header(alias="Idempotency-Key")]  # optional: the web app sends none yet


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


def _pushed(
    request: Request, later: BackgroundTasks, db: DbSession, who: Principal, data: MessageForSender
) -> MessageForSender:
    """Push a sent message after the response (XREQ-014); a draft or a replay pushes nothing."""
    pusher: Pusher = request.app.state.push
    if who.shop_id is not None:
        # The request session would otherwise stay open (idle in transaction) until the push has finished.
        db.close()
        later.add_task(pusher.message_sent, who.shop_id, int(data.id))
    return data


@router.get(
    "/threads",
    summary="Your Threads with unread counts, newest activity first; If-None-Match gives 304",
    response_model=Envelope[list[ThreadOut]],
)
def list_threads(who: ShopMember, db: DbSession, request: Request) -> Response:
    return conditional_json(request, Envelope(data=chat.list_threads(db, who)))


@router.post("/threads/{thread_id}/messages", status_code=201, summary="Foreman: voice/text → AI draft. Worker: sent")
def post_message(
    who: ShopMember,
    thread_id: IdIn,
    key: IdempotencyKey,
    db: DbSession,
    request: Request,
    later: BackgroundTasks,
    text: Annotated[str | None, Form(max_length=2000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
) -> Envelope[MessageForSender]:
    voice = (audio.file.read(MAX_AUDIO_BYTES + 1) or None) if audio is not None else None
    data = chat.post_message(db, who, int(thread_id), text, voice, key, _ai(request), now_utc())
    return Envelope(data=_pushed(request, later, db, who, data))


@router.post("/transcriptions", summary="Dictation fallback: a recording in, its text out (personal, never stored)")
def transcribe(
    who: ShopMember,
    db: DbSession,
    request: Request,
    language: Annotated[str, Form(max_length=8)],
    audio: Annotated[UploadFile | None, File()] = None,
) -> Envelope[Transcription]:
    data = audio.file.read(MAX_AUDIO_BYTES + 1) if audio is not None else None
    return Envelope(data=Transcription(text=chat.transcribe(db, who, data, language, _ai(request), now_utc())))


@router.get(
    "/threads/{thread_id}/messages",
    summary="Poll messages after a cursor; send If-None-Match to get 304 when nothing changed",
    response_model=Envelope[MessagesPage],
)
def poll_messages(
    who: ShopMember,
    thread_id: IdIn,
    db: DbSession,
    request: Request,
    after: Annotated[IdIn | None, Query()] = None,
) -> Response:
    page = chat.messages(db, who, int(thread_id), int(after) if after else None, now_utc())
    return conditional_json(request, Envelope(data=page))


@router.patch(
    "/messages/{message_id}", summary="Edit an unsent draft's Japanese; the AI translates it again (XREQ-022)"
)
def edit_draft(
    who: Foreman, message_id: IdIn, body: DraftEdit, db: DbSession, request: Request
) -> Envelope[MessageForSender]:
    return Envelope(data=chat.edit_draft(db, who, int(message_id), body, _ai(request), now_utc()))


@router.post("/messages/{message_id}/send", summary="Send the edited draft; the Tasks kept go on the Task Board")
def send_message(
    who: Foreman, message_id: IdIn, body: SendIn, db: DbSession, request: Request, later: BackgroundTasks
) -> Envelope[MessageForSender]:
    return Envelope(data=_pushed(request, later, db, who, chat.send(db, who, int(message_id), body, now_utc())))


@router.get("/messages/unanswered", summary="Your unanswered Checks across your Threads, newest first (at most 100)")
def unanswered(who: Worker, db: DbSession) -> Envelope[list[UnansweredCheck]]:
    return Envelope(data=chat.unanswered(db, who))


@router.post("/messages/{message_id}/answers", summary="Answer a message's questions (-1 = not sure) → Wakatta Status")
def answer_message(who: Worker, message_id: IdIn, body: AnswersIn, db: DbSession) -> Envelope[AnswerResult]:
    return Envelope(data=chat.answer(db, who, int(message_id), body, now_utc()))


@router.get("/follow-ups", summary="Follow-ups of your shop, newest first")
def list_follow_ups(
    who: Foreman,
    db: DbSession,
    page: Paging,
    status: Literal["open", "clarified", "understood"] | None = None,
) -> Envelope[Page[FollowUpOut]]:
    return Envelope(data=chat.list_follow_ups(db, who, status, page))


@router.patch("/follow-ups/{follow_up_id}", summary="Mark a Follow-up clarified or understood")
def update_follow_up(who: Foreman, follow_up_id: IdIn, body: FollowUpUpdate, db: DbSession) -> Envelope[FollowUpOut]:
    return Envelope(data=chat.update_follow_up(db, who, int(follow_up_id), body, now_utc()))


@router.post("/messages/{message_id}/reexplain", status_code=201, summary="Re-explain one term to one Worker (AI)")
def reexplain(
    who: Foreman, message_id: IdIn, body: ReexplainIn, db: DbSession, request: Request, later: BackgroundTasks
) -> Envelope[MessageForSender]:
    data = chat.reexplain(db, who, int(message_id), body, _ai(request), now_utc())
    return Envelope(data=_pushed(request, later, db, who, data))


@router.post(
    "/messages/{message_id}/reexplain/draft", summary="See the Re-explanation before sending it; nothing is stored"
)
def reexplain_draft(
    who: Foreman, message_id: IdIn, body: ReexplainIn, db: DbSession, request: Request
) -> Envelope[ReexplainDraft]:
    return Envelope(data=chat.reexplain_draft(db, who, int(message_id), body, _ai(request), now_utc()))


@router.post(
    "/messages/{message_id}/ask", status_code=201, summary="Worker: ask the Foreman to explain a term again (no AI)"
)
def ask_foreman(
    who: Worker, message_id: IdIn, body: AskForemanIn, db: DbSession, request: Request, later: BackgroundTasks
) -> Envelope[MessageForSender]:
    data = chat.ask_foreman(db, who, int(message_id), body, now_utc())
    return Envelope(data=_pushed(request, later, db, who, data))


@router.post("/messages/{message_id}/nudge", summary="Show a Worker you are waiting for their answer (rate-limited)")
def nudge(
    who: Foreman, message_id: IdIn, body: NudgeIn, db: DbSession, request: Request, later: BackgroundTasks
) -> Envelope[None]:
    chat.nudge(db, who, int(message_id), body, now_utc())
    pusher: Pusher = request.app.state.push
    if who.shop_id is not None and who.user_id is not None:
        db.close()  # as in _pushed
        later.add_task(pusher.nudged, who.shop_id, int(message_id), int(body.user_id), who.user_id)
    return Envelope(data=None)


@router.get(
    "/tasks",
    summary="The Task Board of your shop; If-None-Match gives 304",
    response_model=Envelope[Page[TaskOut]],
)
def list_tasks(
    who: ShopMember,
    db: DbSession,
    page: Paging,
    request: Request,
    machine_id: Annotated[IdIn | None, Query(alias="machineId", description="Only this Machine's Tasks")] = None,
) -> Response:
    tasks = chat.list_tasks(db, who, page, int(machine_id) if machine_id else None)
    return conditional_json(request, Envelope(data=tasks))


@router.post("/tasks", status_code=201, summary="Add a Task by hand")
def create_task(who: Foreman, body: TaskCreate, db: DbSession, key: OptionalKey = None) -> Envelope[TaskOut]:
    return Envelope(data=chat.create_task(db, who, body, key))


@router.patch("/tasks/{task_id}", summary="Move or edit a Task; If-Match: <version>, 412 when it changed meanwhile")
def update_task(
    who: ShopMember,
    task_id: IdIn,
    body: TaskUpdate,
    db: DbSession,
    if_match: Annotated[str, Header(alias="If-Match")],
) -> Envelope[TaskOut]:
    version = if_match.strip().strip('"')
    if not version.isdigit():
        raise ApiError(
            422, "VALIDATION_ERROR", "If-Match must be the Task's version", {"fields": [{"loc": ["header"]}]}
        )
    return Envelope(data=chat.update_task(db, who, int(task_id), body, int(version), now_utc()))
