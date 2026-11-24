# Android finish plan (Minhaz, 2026-11-24)

"Done" = every screen on live data, plus dictation, text-to-speech, QR scan, photo, share, offline outbox and saved settings. Full build, unit tests, module check and an emulator run pass. Fonts, App Links, baseline profile, accessibility and translation review come after.

Status words: TODO, RUNNING, BUILT (a worker says so), VERIFIED (checked again by the owner).

## Batches (two workers at a time; no overlapping Gradle runs)

| Batch | Work | Files | Status |
|---|---|---|---|
| 1a | Device dictation in Chat | `core/media`, `feature/chat` | BUILT |
| 1b | Support, Contacts, My QR, Contact Threads on live data | `feature/support`, `feature/connect` | BUILT |
| 5a | First emulator run (Worker): sign-in, five tabs | all | DONE, faults below |
| H | Home, Me, sign-in, theme, language, saved settings, padding | `feature/home`, `feature/auth`, `app` | BUILT, unit tests pass |
| 2a | Chat: draft edit, Re-explanation draft, Ask the Foreman (XREQ-022) | `feature/chat` | BUILT, reviewed |
| 1c | Briefing and Near-miss on live data | `feature/briefing`, `feature/nearmiss` | BUILT, reviewed |
| 2b | Insights, Learning (XREQ-020) | `feature/insights` | RUNNING |
| 2c | Talk Threads (XREQ-018), Connect review findings | `feature/connect` | RUNNING |
| 3c | No demo data or stub: Board; Asks, Notices, Notifications, Machines | those modules | TODO |
| 3a | Device features: text-to-speech, QR camera, photo, share, recorder; outbox and cache on Room. Reuse branch `minhaz/android-media` | `core/media`, `core/database`, `app/di` | TODO |
| 4 | Merge `origin/main`; full build, all unit tests, module check, release build, smoke scripts | all | TODO |
| 5b | Emulator run of every screen as Foreman and Worker; compare with the prototype; fix | all | TODO |
| 6 | Review of the modules not reviewed yet | all | TODO |
| 7 | Land on `main`, push, update the XREQ fulfilment blocks | git | TODO |

## Faults found in the first emulator run (2026-11-24)

| Fault | Fix |
|---|---|
| Home showed a hard-coded Briefing, Check, Follow-up, tally and badge to every user | H: live routes |
| Skill Passport listed demo Machines, and one could look signed off without a Sign-off | H: live passport |
| Dark mode, large text and settings did nothing and were lost | H: DataStore, theme |
| API language stayed Japanese after a restart | H: follows the screen language |
| Empty band above every tab title (window insets counted twice) | H |
| "Screen: auth:signin" label and a Change PIN button on sign-in | H |
| Session store fell back to a demo store when the Keystore failed | H: reset and sign in again |
| Board shows user ids as avatars and demo Machine names | 3c |
| A Worker sees a Thread named after themself | 2a: shows "Foreman" |
| After sign-out the next user stayed on Sign-in (the Me tab kept the sign-in key) | H2: the session gate only |
| Screen state of the last user stayed in memory (no ViewModel scope in NavDisplay) | H2: one scope for each screen, reset at user change |
| Sign-out never reached the server, so pushes went on | H2: `POST /auth/logout` |
| No screen refreshed when shown again | H2: `RefreshOnReturn` (Home, Me, Chats); 3c for Board and Machines |
| Ask the Foreman sent a database id as the Term | H2 |
| Support Log listed demo Workers and called `/workers/u_rahim/interviews` (422) | H2: live Workers, due dates, and Meet-ups, Notice checks and interview facts parsed from `/support-log.csv` |
| Interviews counted from a fixed demo date | H2: today in Japan |

## Fakes still bound in live mode on 2026-11-24 (beyond batches 1 and 2)

| Fake | What the user sees | Batch |
|---|---|---|
| `FakeSpeaker` | The read-aloud button does nothing | 3a |
| `QrScanner`, `PhotoCapture`, `ShareFile` stubs in `MediaFakesModule` | No scan, no photo, no share | 3a |
| `FakeOutboxWriter` | An offline write is counted, then lost | 3b |
| `RoomCacheStore` never bound | No offline read cache | 3b |
| `FakeSettingsStore` | Settings are lost when the app closes | 3c |
| `LanguageManagerImpl("ja")` in `AppModule` | Language may return to Japanese at each start (to check) | 3c |
| `FakeEventSource` | Demo events on Home | 3c |
| `FakeSenderNames` | Every push says "Tanaka" | 3c |
| `FakeTodayWordSource` | Needs XREQ-024 | backend |

## Proof so far

Live loop on the hosted server, 2026-11-24 (script run, not through the app): the draft came back in three languages with one question and one Task draft; after Send the Worker could read it and never received `correctIndex`; a wrong answer gave red and an open Follow-up; after the Re-explanation a right answer gave green and Follow-up `understood`; Ask the Foreman returned 201; the Worker's Task count rose from 4 to 5.
