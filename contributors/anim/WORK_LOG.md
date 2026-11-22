# Anim's Work Log

Owner: Anim (`anim710`) — Role: Backend (WS10 Notices + Events, WS11 Asks + Senpai + Support Log + Meet-ups)

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- **Decision (council, WS0 blocker):** do not build a second skeleton (team rule; two migration chains would collide before code freeze). Ask Asif tonight for a partial WS0 push and offer to take named WS0 pieces he signs off (request below). No repositories against guessed models; only pure service logic if anything. Meanwhile do the unowned scored work: the proposal's Life section and its "works now vs planned" table. Strongest dissent: write services behind small interfaces now (pragmatist) vs "that is rework" (skeptic).
- Pitch deck (team draft, artifact v4): slide 5 now matches the sourced competitor check (カミナシ already has quizzes and Bengali; our differences are the machine lock, the term-level alert and the support record); slide 4 says 登録支援機関 pay (free for employers and workers, price not set) and claims only verified garbage rules (city bag, by 8:00).
- **Decision (four-voice council: architect, skeptic, pragmatist, critic): no backend `.ics` now, no `icalendar` dependency.** The browser already builds the file (`toIcs`), so a backend endpoint nobody calls scores nothing; it moves to P2 (stdlib, with folding and a `VTIMEZONE`). The real calendar bug is in `toIcs` (no 75-octet folding, no `VTIMEZONE`): filed as XREQ-002 item 9. Rejected advice: writing UTC `…Z` start times to skip the `VTIMEZONE` breaks `BYDAY` (Mon 08:00 JST is Sun 23:00 UTC), as the dateutil probe showed. Strongest dissent: build the 40-line stdlib export now anyway (pragmatist).
- Life seed data: `backend/app/seed_data/life.json` (district みなと町, 2 Posters and their demo accounts, 3 published Notices with confirmed Events, receipts, 2 Senpai pairs, 2 Meet-ups), ported from `frontend/src/services/mocks/seed.ts` so the deployed demo matches the mock one. Keys, not ids, for `seed.py` to map. Verified: 6 tests in `backend/tests/seed/test_life_seed.py` (contract shapes in ja/bn/en, references resolve, Events still upcoming on 2026-11-26, PIN rules); 74 total pass; ruff and pyright strict clean.
- Support Log CSV (architecture §9.3, research.md §1): `backend/app/domain/support_log.py` `render_support_log(events, start, end)`. One row per event, grouped by the support item it evidences (7 相談・苦情への対応, 8 日本人との交流促進, 4 生活オリエンテーション, then Panel Overrides as the safety record), times in Japan time, UTF-8 BOM + CRLF for Excel. The preamble states what it does not cover (item 10 面談, 参考様式第3-6号). Cells starting with `= + - @`, tab or CR get a leading `'` so Worker-typed text cannot run as an Excel formula (CSV injection). Verified: 10 tests in `backend/tests/domain/test_support_log.py`; 68 total pass; ruff and pyright strict clean. Merged `anim/ws10-notices` into `main` (fast-forward) and now push to `main` after each task.
- Life API contract (WS10 + WS11): request and response models in `backend/app/schemas/` (`common.py`, `notices.py`, `asks.py`, `senpai.py`) for every Life row of architecture §9.3. camelCase, `extra="forbid"`, string ids, `Translated` = `{ja, <shop languages>}` with `ja` required; Worker and Senpai responses use `CheckQuestionForWorker` (no `correctIndex`). Found while probing FastAPI 0.165: a form model with `extra="forbid"` rejects the uploaded file as an extra field, so the multipart forms carry their file fields. `EventOut.nextDates` is backend-expanded. Verified: 28 contract tests in `backend/tests/schemas/` (58 total pass); ruff and pyright strict clean; a throwaway FastAPI app generated OpenAPI from all models (12 paths, 24 schemas). Frontend changes filed as **XREQ-002**.
- Ask grounding rule (architecture §7.6): `backend/app/domain/grounding.py` `ground()`. A covered answer is kept only if it cites at least one Notice, every cited Notice was among those sent to the model, and it has text in ja and every shop language (extra languages are dropped). Anything else becomes `NOT_COVERED` (`covered=false`, `answer=null`, no sources), the shape the frontend already renders with its own `ask_not_covered` text, so no fixed answer is stored (D4: wire format follows the frontend). Verified: 6 tests in `backend/tests/domain/test_grounding.py`; all 30 domain tests pass; ruff and pyright strict clean.
- Event recurrence rules (architecture §7.5): `backend/app/domain/recurrence.py` with `validate_event` and `occurrences_between`. Only `FREQ=WEEKLY;BYDAY=…` with optional `COUNT` or UTC `UNTIL` is accepted, in the exact shape the frontend can expand (dateutil alone also accepts `1MO`, lowercase days, an `RRULE:` prefix and `COUNT` with `UNTIL`). Rules always expand in Asia/Tokyo, so a UTC start keeps its Japan weekday. Rejects past one-off Events and Events with no date in the next 366 days. Verified: 24 tests in `backend/tests/domain/test_recurrence.py` pass; ruff (architecture rule set) and pyright strict clean. Run without a project file until WS0 lands: `cd backend; uv run --no-project --with pytest --with python-dateutil python -m pytest tests/domain -q`.

### Next
- Draft the proposal's Life section and the "works now vs planned" table.
- When WS0 lands: Notices first, then Asks, Events, Support Log, Senpai, in the cut order above (`BACKEND_PLAN.md` §4).

### Blockers / handoff
- **Asif (WS0), most urgent: I am blocked.** Life is done up to the database (rules, API schemas, seed data, XREQ-002, all on `main`); nothing of WS0 is pushed yet.
  1. Please push whatever WS0 you have **tonight**, even partial (the models alone are enough), to `asif/ws0-foundation`.
  2. If you are short on time, hand me named WS0 pieces and review them: the **Life table models**, the **seed loader**, or **CI**. Tell me which here or in chat; I will not start any of them without your OK (team rule: schema and skeleton are yours).
  3. If WS0 is not usable by **Mon 06:00**, my cut order for Life: keep **Notices** and **Asks grounded in Notices**; **Senpai** moves to "planned" first, then **Meet-ups** and **Notice reach**. Not yet decided: the Support Log CSV endpoint (keep for the demo or cut early).
  4. Schema request: `asks.text_ja text null` (see below).
  5. `seed.py` should load `app/seed_data/life.json`; the Work seed must use the worker keys `w-rahim`, `w-sadia`, `w-tanvir`, `w-tanaka` that the Life seed points to.
- Minhaz and Galib: the demo flyer's recyclables Event (every Wednesday) is not confirmed; 佐世保市 sets recyclable days per area, and some 町内会 collect once a month. Verified only: designated bag for burnable and non-burnable, see-through bags for recyclables, out by 8:00; 粗大ごみ via クリーンセンター or paid home collection. Collection frequencies are unconfirmed. XREQ-002 item 12; sources in the plan's "Facts to verify". (Correction: `fbea045` stated "twice a month" and "twice a week" from a search summary, not from the city's pages.)
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
- `fbea045` docs(plan): checked Sasebo garbage rules; XREQ-002 item 12
