"""The Wakatta? chatbot (RAG_PLAN.md). Thin: parse → service → envelope. One route serves the Worker home and the
Machine screen (`machineId` ranks that Machine's instruction first)."""

from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, Request, UploadFile

from app.api.auth import DbSession, now_utc, require
from app.api.envelope import Envelope
from app.media import MAX_AUDIO_BYTES
from app.schemas.common import IdIn
from app.schemas.knowledge import KnowledgeAnswer
from app.services import knowledge
from app.services.ai_runner import AiRunner
from app.services.auth import Principal

router = APIRouter(prefix="/api", tags=["knowledge"])
Worker = Annotated[Principal, Depends(require("worker"))]


def _ai(request: Request) -> AiRunner:
    runner: AiRunner = request.app.state.ai
    return runner


@router.post(
    "/knowledge/asks",
    summary="Ask the chatbot (text or voice): answered only from cited sources, then one self-check; never a 500",
)
def ask_knowledge(
    who: Worker,
    db: DbSession,
    request: Request,
    text: Annotated[str | None, Form(max_length=1000)] = None,
    audio: Annotated[UploadFile | None, File()] = None,
    machine_id: Annotated[IdIn | None, Form(alias="machineId")] = None,
) -> Envelope[KnowledgeAnswer]:
    data = audio.file.read(MAX_AUDIO_BYTES + 1) if audio is not None else None
    machine = int(machine_id) if machine_id else None
    return Envelope(data=knowledge.ask(db, who, text, data or None, machine, _ai(request), now_utc()))
