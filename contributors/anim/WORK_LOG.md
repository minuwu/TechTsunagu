# Anim's Work Log

Owner: Anim (`anim710`) — Role: Backend (WS10 Notices + Events, WS11 Asks + Senpai + Support Log + Meet-ups)

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- **Decision (four-voice council: architect, skeptic, pragmatist, critic): no backend `.ics` now, no `icalendar` dependency.** The browser already builds the file (`toIcs`), so a backend endpoint nobody calls scores nothing; it moves to P2 (stdlib, with folding and a `VTIMEZONE`). The real calendar bug is in `toIcs` (no 75-octet folding, no `VTIMEZONE`): filed as XREQ-002 item 9. Rejected advice: writing UTC `…Z` start times to skip the `VTIMEZONE` breaks `BYDAY` (Mon 08:00 JST is Sun 23:00 UTC), as the dateutil probe showed. Strongest dissent: build the 40-line stdlib export now anyway (pragmatist).
- Life seed data: `backend/app/seed_data/life.json` (district みなと町, 2 Posters and their demo accounts, 3 published Notices with confirmed Events, receipts, 2 Senpai pairs, 2 Meet-ups), ported from `frontend/src/services/mocks/seed.ts` so the deployed demo matches the mock one. Keys, not ids, for `seed.py` to map. Verified: 6 tests in `backend/tests/seed/test_life_seed.py` (contract shapes in ja/bn/en, references resolve, Events still upcoming on 2026-11-26, PIN rules); 74 total pass; ruff and pyright strict clean.
- Support Log CSV (architecture §9.3, research.md §1): `backend/app/domain/support_log.py` `render_support_log(events, start, end)`. One row per event, grouped by the support item it evidences (7 相談・苦情への対応, 8 日本人との交流促進, 4 生活オリエンテーション, then Panel Overrides as the safety record), times in Japan time, UTF-8 BOM + CRLF for Excel. The preamble states what it does not cover (item 10 面談, 参考様式第3-6号). Cells starting with `= + - @`, tab or CR get a leading `'` so Worker-typed text cannot run as an Excel formula (CSV injection). Verified: 10 tests in `backend/tests/domain/test_support_log.py`; 68 total pass; ruff and pyright strict clean. Merged `anim/ws10-notices` into `main` (fast-forward) and now push to `main` after each task.
- Life API contract (WS10 + WS11): request and response models in `backend/app/schemas/` (`common.py`, `notices.py`, `asks.py`, `senpai.py`) for every Life row of architecture §9.3. camelCase, `extra="forbid"`, string ids, `Translated` = `{ja, <shop languages>}` with `ja` required; Worker and Senpai responses use `CheckQuestionForWorker` (no `correctIndex`). Found while probing FastAPI 0.165: a form model with `extra="forbid"` rejects the uploaded file as an extra field, so the multipart forms carry their file fields. `EventOut.nextDates` is backend-expanded. Verified: 28 contract tests in `backend/tests/schemas/` (58 total pass); ruff and pyright strict clean; a throwaway FastAPI app generated OpenAPI from all models (12 paths, 24 schemas). Frontend changes filed as **XREQ-002**.
- Ask grounding rule (architecture §7.6): `backend/app/domain/grounding.py` `ground()`. A covered answer is kept only if it cites at least one Notice, every cited Notice was among those sent to the model, and it has text in ja and every shop language (extra languages are dropped). Anything else becomes `NOT_COVERED` (`covered=false`, `answer=null`, no sources), the shape the frontend already renders with its own `ask_not_covered` text, so no fixed answer is stored (D4: wire format follows the frontend). Verified: 6 tests in `backend/tests/domain/test_grounding.py`; all 30 domain tests pass; ruff and pyright strict clean.
- Event recurrence rules (architecture §7.5): `backend/app/domain/recurrence.py` with `validate_event` and `occurrences_between`. Only `FREQ=WEEKLY;BYDAY=…` with optional `COUNT` or UTC `UNTIL` is accepted, in the exact shape the frontend can expand (dateutil alone also accepts `1MO`, lowercase days, an `RRULE:` prefix and `COUNT` with `UNTIL`). Rules always expand in Asia/Tokyo, so a UTC start keeps its Japan weekday. Rejects past one-off Events and Events with no date in the next 366 days. Verified: 24 tests in `backend/tests/domain/test_recurrence.py` pass; ruff (architecture rule set) and pyright strict clean. Run without a project file until WS0 lands: `cd backend; uv run --no-project --with pytest --with python-dateutil python -m pytest tests/domain -q`.

### Next
- WS10/WS11 routes, services and repositories once WS0 is merged on `main`, in `BACKEND_PLAN.md` §4 order (Notices first).

### Blockers / handoff
- **Asif (WS0), most urgent:** please push a partial `asif/ws0-foundation` branch with the models as soon as they exist, so Life services can be written against the real tables instead of guessed ones. `seed.py` should load `app/seed_data/life.json`, and the Work seed must use the worker keys `w-rahim`, `w-sadia`, `w-tanvir`, `w-tanaka` that the Life seed points to.
- Not verified: the flyer's garbage rules in the demo (burnable Mon/Thu before 8:00, recyclables Wed) come from the frontend mock, not from the official 佐世保市 page; days differ by district (plan, "Facts to verify").
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
- `65464cc` feat(domain): add support log CSV rendering
- `5415fc5` feat(seed): add Life demo seed data
- `b4c3882` chore(xreq): XREQ-002 items 9–11 (ics folding, backend support log, notice termId)
