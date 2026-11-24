# Android fix plan: who does what

Made 2026-11-24 from the Android audit (code read plus live API calls at `0f7e60c`; not yet run on a phone).
Split between five people by file, so two people never edit the same file at the same time.
"Live" means seen on the hosted server; "code" means read in the source.

**All interface (screens) and Firebase work is Minhaz's.** The others do the logic, data, AI and server behind it.

**Order: every P0 first, then P1, then P2.** The XREQ queue in [AGENTS.md](../AGENTS.md) still comes first.

## The loop, for every task

One task, one commit, one push. Do not start the next task before the push is through.

1. `git pull --no-rebase origin main`
2. Do **one** task, with a test. A bug fix starts with a test that fails.
3. Tick your own box below and commit it with the fix (Conventional Commits, for example `fix(chat): stop dictation before sending`).
4. `git pull --no-rebase origin main` again.
5. **Test the merged code** (table below). Red: fix it, commit, back to step 4. Never push red.
6. `git push origin main`. Rejected: back to step 4.
7. Add the commit hash to your `WORK_LOG.md` with the next push.

| You changed | Run |
|---|---|
| `android/` | `cd android`, then `.\gradlew.bat testDebugUnitTest checkModuleGraph :app:assembleDebug` (macOS, Linux: `./gradlew`) |
| `backend/` | `cd backend`, then `uv run ruff check .`, `uv run ruff format --check .`, `uv run pyright`, `uv run lint-imports`, `uv run pytest --cov` |
| A backend route or schema | Also `uv run python -m app.openapi index`, regenerate `docs/openapi.yaml`, and the frontend types (`npm run gen:api` in `frontend/`) |
| Anything the user sees | Try it on a phone or emulator and write one line of what you saw in your log |

- Android needs **JDK 17 and the Android SDK** (compile SDK 37.1). Without them you cannot test, so install them before your first Android task.
- **Edit only the files in your section.** Need another one? Ask its owner in the chat first.
- **PR with one approval** (AGENTS.md §3.5) only for the tasks marked **PR**: API contract and CI. Everything else goes straight to `main` with the loop above.
- Only Minhaz edits `strings.xml`. Need a text? Ask Minhaz for the string name.

## Summary

| Person | Area | P0 | P1 | P2 |
|---|---|---|---|---|
| Minhaz | Interface (every screen), Firebase, release | M1 M2 M3 | M4 M5 M6 M7 | M8 M9 M10 M11 M12 |
| Arjun | App logic: chat, network, other ViewModels | R1 R2 R3 | R4 R5 | R6 R7 |
| Galib | AI speed, AI prompt, chatbot data | G1 G2 | G3 G4 | |
| Anim | Demo data, Home data, outbox, Android CI | N1 | N2 N3 | N4 N5 |
| Asif | Backend gaps, device voice, final test | S1 | S2 S3 | S4 S5 |

## Waits on

| Task | Waits for | Why |
|---|---|---|
| M2 review card, M4 draft label | R3 | R3 gives `isDraft`, `needsEdit` and the send and edit events |
| M7 chatbot screen | G4 | G4 gives the repository and the ViewModel |
| M10 clarified button | N5 | N5 gives the data call |
| N2 Home | S3 | Home needs the text and title in `GET /messages/unanswered` |
| R1 app timeout | nothing, but agree the numbers with G1 | The app must always wait longer than the server |
| N1 second reset | all backend tasks deployed | Otherwise test data comes back |
| M12 release | every P0 and P1 ticked | One release, not five |
| S5 final test | M12 | It tests the APK we film with |

---

## Minhaz: interface, Firebase, release

**Files:** every Compose screen and component, `strings.xml`, `android/app/**`, `android/core/design/**`, `android/feature/notifications/**`, `android/release/**`. In `feature/chat`: `ChatScreen.kt`, `ChatComposer.kt`, `ChatMessageBubble.kt`, `WakattaStatusBlock.kt`, `ChatErrors.kt`. Not the few files named in Arjun's and Asif's sections.

- [x] **M1 (P0, code) The voice stop button becomes Send.** *Done (Minhaz): while recording the button only stops; Cancel shows while recording.* After the first partial words `showSend` is true (`ChatComposer.kt:149`, `:356`). While recording, the button must stop (`StopRecording`). Show Cancel while recording too (`CancelCountdown` already ends dictation). The logic half is R2.
- [ ] **M2 (P0, live; after R3) Nobody sees the AI's draft before it goes out.** Review card: Japanese, English, questions, **Send** and **Edit**. With `needsEdit`, say "not translated" and let the Foreman choose.
- [x] **M3 (P0, code) Chat opens at the oldest message.** *Done (Minhaz): already opened at the newest; test `chatOpensAtTheNewestMessage` (50 messages).* The list in `ChatScreen.kt:147` never scrolls. Open at the newest; follow new messages only when the reader is already at the bottom.
  *Done when:* a Compose test with 50 messages shows the last one.
- [ ] **M4 (P1, live; after R3) An unsent draft looks sent.** Mark it "Draft, not sent"; mark `needsEdit` "not translated".
- [x] **M5 (P1, code) No Nudge button until somebody has answered.** *Done (Minhaz): Nudge shows from stage 4; test `foremanCanNudgeAWorkerOnceTheMessageIsSent`.* `done = stage >= 5` (`WakattaStatusBlock.kt:86`, `:347`, `:355`). Show Nudge from stage 4; the server allows it (checked live).
- [x] **M6 (P1, code) Wrong messages.** *Done (Minhaz): `ChatErrors.kt` splits Offline, Timeout, RateLimited (with seconds) and Busy; `503 AI_UNAVAILABLE` maps to `Busy`; no green check while transcribing; Briefing "Explain again" hidden.* `ChatErrors.kt`: `Timeout` is not "No connection"; `RateLimited` says when to try again; a server AI error says the AI is busy. The green check shows only after the send succeeds. Hide the Briefing "Re-explain" button: it says sent and does nothing (`RealBriefingRepository.kt:84`, no route exists).
- [ ] **M7 (P1; after G4) Android chatbot screen (XREQ-016),** with the placement you chose: pinned "Ask Wakatta" and the Machine-screen button.
- [ ] **M8 (P2) Firebase push (XREQ-014).** The release APK has no Firebase config, and the device is registered only when the Notifications screen opens. Add the config, register after sign-in, test on a real phone. The server key goes to Anim privately, who runs `set-firebase-key.ps1` (Azure access).
- [ ] **M9 (P2, code) Read-aloud for every chat message**, in the language shown. Today only voice messages have it, always in Japanese (`ChatMessageBubble.kt:99`).
- [ ] **M10 (P2; after N5) "Clarified" button** on the Follow-ups screen.
- [ ] **M11 (P2, code) The new shop code screen probably disappears after registering**, because the session opens first. Check it on a phone, then fix it.
- [ ] **M12 (P2, last) Release 1.0.2** when every P0 and P1 here is ticked; send the APK to Anim for hosting.

## Arjun: app logic behind the screens

**Files:** in `android/feature/chat`: `ChatViewModel.kt`, `data/*`, `model/*`. Also `android/core/network/**`, and the ViewModel parts of `NearMissNewScreen.kt` and `PChatScreen.kt` (no layout changes there).

- [x] **R1 (P0, live) The app gives up before the AI answers.** *Done (Arjun): `ApiClientFactory.READ_TIMEOUT_SECONDS = 60`, test `ReadTimeoutTest`; not run locally (no JDK on Arjun's PC), Minhaz's Gradle run checks it.* Read timeout is 30 s (`core/network/.../ApiClientFactory.kt:21`); 4 of 14 live AI drafts took 51–61 s. Set it to 60 s (above the server's 45 s from G1).
  *Done when:* a MockWebServer answer after 40 s arrives in the app.
- [x] **R2 (P0, code) A voice message is cut short or sent twice.** *Done (Minhaz, with M1): `doSend` calls `endDictation()`; test `sendingWhileDictatingSendsOnce_evenWhenALateFinalArrives`.* `doSend` (`ChatViewModel.kt:409`) does not stop dictation, so a late result refills the box and sends again. Cancel `dictationJob` before sending. The button half is M1.
  *Done when:* a test sends during recording, then emits a late `Final`, and exactly one message goes out.
- [x] **R3 (P0, live) Drafts are sent without a look, also untranslated ones.** *Logic done (Asif, for Arjun); the app still sends at once until M2 turns `reviewDrafts` on. M2 and M4 can start.* `RealChatRepository.kt:161-163` posts and sends at once. Split it: post returns the draft; a new event sends it; `EditDraft` (`PATCH /messages/{id}`) exists already. `needsEdit = true` is never sent by itself. Put `isDraft` and `needsEdit` in the UI model. **Unblocks M2 and M4.** Push it together with Minhaz's M2, so no version on `main` is without a Send.
  *Done when:* tests cover a good draft, a `needsEdit` draft, and a draft left from an earlier timeout that can still be sent.
- [ ] **R4 (P1, code) Names and empty bubbles.** *Asif is doing this (2026-11-24); Arjun, please do not start it.* Map `fromDisplayName` and `displayTitle` (the server sends both) in `ChatApi.kt` and `ChatWire.kt`. When a Worker's message has no Japanese, fall back to the Worker's own words in `ChatWire.kt` `base()`; today the Foreman sees an empty bubble.
- [ ] **R5 (P1, code) Dictation language in chat.** Use the screen language, not the account language; let dictation add to text already in the box.
- [ ] **R6 (P2, code) Near-miss: changing the Machine erases the text and photo** (`NearMissNewScreen.kt:163-167`). Keep them.
- [ ] **R7 (P2, code) Contact chat does not refresh by itself.** Poll while the screen is open (`PChatScreen.kt`, ViewModel part).

## Galib: AI

**Files:** `backend/app/ai/**`, `backend/app/prompts/**`, `backend/app/domain/ai_checks.py`, `backend/app/services/ai_runner.py`, the AI settings in `backend/app/config.py`, and new data and ViewModel files for the chatbot in `android/feature/asks`.

- [x] **G1 (P0, live) AI answers are too slow.** *Done `a585352` (Galib): 15 s a model (`AI_TIMEOUT_SECONDS`), 25 s for the whole request (`AI_CHAIN_SECONDS`), then degraded; tests in `tests/ai/test_runner.py`. The server now always answers within ~25 s, so R1's 60 s app timeout is safe. Still to do after filming: the 10 live drafts.* Healthy calls take 2–6 s; 4 of 14 took 51–61 s. Probable cause: each model may take 45 s (`config.py:48`) before the next is tried; check the logs with Anim first. Target: about 12 s a model and 45 s for the whole request, then degrade.
  *Done when:* a test with a hanging first model answers from the second, and 10 live drafts are all under 45 s.
- [x] **G2 (P0, live) `draft_message` changes what the Foreman said.** *Done `a585352` (Galib): `draft_message.v3`, a code check rejects a Machine name the Foreman did not say, greetings may have no question; live on Gemini and Groq the insult, "machine 3" and "hello" cases were right. Eval: 10 cases in `tests/fixtures/ai/eval/messages.json` (`uv run python -m app.ai.eval --pipelines messages`), to run after filming.* An insult became "Please provide work-related instructions", sent as the Foreman's words; 「機能あめでしたぁ」 became "it was raining yesterday"; "machine 3" became the crane's full name (it is Machine 3 in that shop, but the Foreman did not say crane). Translate faithfully, never answer or moderate, keep the Machine as said, and allow **no** question for a greeting ("hello" got a quiz). New prompt version, plus eval cases for these.
- [ ] **G3 (P1) AI capacity for filming** (task 1 of your 11-24 note): still open, and it decides whether G1 is enough.
- [x] **G4 (P1) Chatbot data layer for Android** (XREQ-016): *Done (Galib). `feature/asks`: `model/KnowledgeDtos.kt` (answer, sources, practice question, `kind`), `data/ChatbotRepository.kt` (`ChatbotRepository`; Real = `POST /api/knowledge/asks` with `text` or `audio` and `machineId`; Fake = the filmed answers offline; wired in `AsksDataModule`), `ChatbotViewModel.kt` (events `SetMachine`, `InputChanged`, `Send`, `SendRecording`, `PickOption`, `AskSenpai`, `OpenSource`, `DismissError`; effect `OpenUrl`; per turn COVERED / NOT_COVERED / EMERGENCY, `sending`, `error`). Tests: `ChatbotViewModelTest` (6), `RealChatbotRepositoryTest` (2); `testDebugUnitTest checkModuleGraph :app:assembleDebug` green. **Minhaz (M7):** send `SetMachine(id)` from the Machine screen.* API call, repository, ViewModel with the four states, tests. No screen: that is M7, which this unblocks.

## Anim: demo data, operations, CI

**Files:** `backend/deploy/**`, `.github/workflows/**`, `android/core/sync/**`, and the `data/` folders of `android/feature/home` and `android/feature/insights`.

- [ ] **N1 (P0, live) Reset the demo data.** The live shops and Rahim still have Bengali, so his English message is stored as `bn` and shows back to him in Japanese; `SASEBO01` has 12 unsent drafts. Run `backend/deploy/reset-demo.ps1` (**it wipes the test data: ask the team first**), once now and once more after the last backend deploy. Then `set-ai-key.ps1 -Chain demo` and the warmup.
  *Done when:* Rahim's language is `en`, no message has `bn`, smoke is green.
- [x] **N2 (P1, live; after S3) Opening Home marks every chat as read.** `getPendingChecks` polls all messages (`HomeBoardSource.kt:190`). Use `GET /messages/unanswered`. *S3 is on `main`: ready to start.* *Anim: Home reads `GET /messages/unanswered` in one call (no Thread poll, nothing marked read); `RealHomeBoardSourceTest` checks the single request.*
- [x] **N3 (P1) Android tests in CI. PR.** Nothing runs them today. A workflow on `android/**`: unit tests, `checkModuleGraph`, `:app:assembleDebug`. *Anim, `172c890`: `.github/workflows/android-ci.yml`, merged to `main` on Anim's call (no PR approval yet; please review it). Its first run on GitHub is the proof.*
- [x] **N4 (P2, code) The outbox may send nothing when the app is closed.** `OutboxDrain.kt:31` reads the session before it is loaded and reports success. Wait for the session to load. *Anim, `74bb8e3`: waits up to 5 s for the session; the new test fails on the old code.*
- [x] **N5 (P2, code) Follow-up "clarified" data call:** `PATCH /follow-ups/{id}` in `feature/insights/data`. Unblocks M10. *Anim, `74bb8e3`: `FollowUpRepository.clarify(id, note)` (409 → `AppError.Conflict`); the demo Fake keeps it for the run. Minhaz: `domain/InsightsRepositories.kt` gained that one method.*

## Asif: backend gaps, device voice, final test

**Files:** `backend/app/services/chat.py`, `backend/app/repositories/chat.py`, `backend/app/schemas/chat.py`, `backend/app/routers/chat.py`, `android/core/media/**`, and the ViewModel part of `TalkScreen.kt`.

- [x] **S1 (P0, live) Re-explain fails after a wrong answer.** A missed question without a Glossary Term makes no Follow-up (`answer()` in `services/chat.py`), and the app needs one. Make the Follow-up with the question's Japanese prompt, cut to 100 characters (`term_ja` limit); no Confusion, it needs a Term. Test that `/reexplain/draft` then works.
- [x] **S2 (P1, code) Past 100 messages the newest never show.** `messages_after` returns the oldest 100. Without a cursor, return the newest 100 in order.
- [x] **S3 (P1) `GET /messages/unanswered` with the text and the Thread title. PR.** Additive fields only; unblocks N2. *Asif, `2f16153`, on `main`.*
- [x] **S4 (P2, code) Device voice.** `DeviceDictation.kt:101` prefers offline recognition, so a phone without the Japanese pack may fail: prefer offline only without a network. Show network errors as network errors. Talk: keep the spoken words when saving fails, so they can be sent again. *Asif, `eae8254`: tests green.*
- [ ] **S5 (P2, last) Final test on the 1.0.2 APK**, two phones, Foreman and Worker: sign in, typed message, voice message, answer wrong, re-explain, nudge, Near-miss offline, Briefing, Talk, chatbot, language switch, push. Write pass or fail for each in `contributors/asif/WORK_LOG.md`.

---

## Not before filming

- Recording and approving a machine note on Android.
- Uploading chat audio to the server. It works (a test recording came back translated in 2.5 s), but the app uses the phone's own recognizer today.
- Chat for a Worker who speaks another language than Japanese or English.

## On filming day

Use the exact texts of `backend/app/ai/demo_warmup.py`, typed. No live voice in chat until **M1** and **R2** are released.
