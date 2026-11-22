# Anim's Work Log

Owner: Anim (`anim710`) — Role: Backend (WS10 Notices + Events, WS11 Asks + Senpai + Support Log + Meet-ups)

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- Life API contract (WS10 + WS11): request and response models in `backend/app/schemas/` (`common.py`, `notices.py`, `asks.py`, `senpai.py`) for every Life row of architecture §9.3. camelCase, `extra="forbid"`, string ids, `Translated` = `{ja, <shop languages>}` with `ja` required; Worker and Senpai responses use `CheckQuestionForWorker` (no `correctIndex`). Found while probing FastAPI 0.165: a form model with `extra="forbid"` rejects the uploaded file as an extra field, so the multipart forms carry their file fields. `EventOut.nextDates` is backend-expanded. Verified: 28 contract tests in `backend/tests/schemas/` (58 total pass); ruff and pyright strict clean; a throwaway FastAPI app generated OpenAPI from all models (12 paths, 24 schemas). Frontend changes filed as **XREQ-002**.
- Ask grounding rule (architecture §7.6): `backend/app/domain/grounding.py` `ground()`. A covered answer is kept only if it cites at least one Notice, every cited Notice was among those sent to the model, and it has text in ja and every shop language (extra languages are dropped). Anything else becomes `NOT_COVERED` (`covered=false`, `answer=null`, no sources), the shape the frontend already renders with its own `ask_not_covered` text, so no fixed answer is stored (D4: wire format follows the frontend). Verified: 6 tests in `backend/tests/domain/test_grounding.py`; all 30 domain tests pass; ruff and pyright strict clean.
- Event recurrence rules (architecture §7.5): `backend/app/domain/recurrence.py` with `validate_event` and `occurrences_between`. Only `FREQ=WEEKLY;BYDAY=…` with optional `COUNT` or UTC `UNTIL` is accepted, in the exact shape the frontend can expand (dateutil alone also accepts `1MO`, lowercase days, an `RRULE:` prefix and `COUNT` with `UNTIL`). Rules always expand in Asia/Tokyo, so a UTC start keeps its Japan weekday. Rejects past one-off Events and Events with no date in the next 366 days. Verified: 24 tests in `backend/tests/domain/test_recurrence.py` pass; ruff (architecture rule set) and pyright strict clean. Run without a project file until WS0 lands: `cd backend; uv run --no-project --with pytest --with python-dateutil python -m pytest tests/domain -q`.

### Next
- WS10/WS11 routes, services and repositories once WS0 is merged on `main`, in `BACKEND_PLAN.md` §4 order (Notices first).

### Blockers / handoff
- **Schema request for Asif (needs a migration):** `asks` has no column for the Japanese translation of the Worker's question, but the plan forwards the Ask "translated to Japanese" to the Senpai (a 自治会 volunteer like 森 早苗 cannot read Bengali). Please add `asks.text_ja text null`, filled when the Ask is forwarded; the contract already has `ForwardedAsk.textJa`.
- Asif (WS0): `app/schemas/common.py` holds `CamelModel`, `Translated`, `IdIn`, `CheckQuestion`, `DateRange`; move them into `app/api/` if you prefer, and the Life schemas follow. They need `fastapi`, `pydantic` and `python-multipart` in `pyproject.toml`.
- Galib (WS1): `answer_ask` should pass its parsed output through `app.domain.grounding.ground()` (with the ids of the Notices it sent) before the service writes the Ask.
- WS0 (Asif): the domain files use implicit namespace packages (no `__init__.py`), so they merge cleanly into your skeleton. Please add `python-dateutil` and `types-python-dateutil` (dev) to `pyproject.toml`.
- Minhaz (FYI, no XREQ needed yet): `frontend/src/domain/calendar.ts` `nextOccurrences` ignores `COUNT` and `UNTIL`, so a rule that has ended still shows dates in the preview. The backend accepts both, as the architecture specifies.

### Related issues / commits / PRs
- `c6939ba` feat(domain): add event recurrence rules (branch `anim/ws10-notices`)
- `47509f6` feat(domain): add ask grounding rule
- `e281beb` feat(schemas): add the Life API contract
- `6d429c8` chore(xreq): add XREQ-002 for the Life contract
