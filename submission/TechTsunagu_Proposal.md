# Wakatta? — Turning "understood" into understanding

**Team TechTsunagu** · Theme: TSUNAGU, update the way we connect · Android app + web app

---

## 1. The problem: the most dangerous word on the shop floor

*A typical day (scenario).* Nagasaki is reinventing itself. Beside the new Nagasaki Stadium City, Mr. Tanaka runs a small metal factory. To keep up with orders he hired skilled, hardworking people from Vietnam, Nepal and Bangladesh. Most of them hold JLPT N3 or N2 certificates.

One morning he asks Rahim-san to change the die on Press #2. Rahim nods: *"Wakarimashita."* By the afternoon, Rahim is injured. He had misread a single word, and neither of them knew until it was too late.

Nobody here was careless. A worker new to a country and a language can feel hesitant to ask again: he does not want to slow the team down, or be embarrassed in front of colleagues. So "understood" feels like the easiest answer. Mr. Tanaka heard it and, reasonably, trusted it. A JLPT certificate proves someone can pass a test. It cannot prove that one specific instruction, given in fast factory Japanese full of shop words, landed correctly. **The failure is in the connection between the two people, so the fix belongs in that connection too.**

## 2. The evidence: a growing, measurable gap

- **2,571,037** foreign workers in Japan (October 2025), a record high (MHLW).
- **12,807** in Nagasaki Prefecture, up **15.4%** in a single year. **Manufacturing** is the largest employer, with 3,481 workers across 2,338 workplaces (Nagasaki Labour Bureau).
- **6,244** foreign workers killed or injured at work in 2024, more than **four times** the 1,443 recorded in 2008 (Sompo Risk Management, from MHLW data).
- In manufacturing, foreign workers are injured at **6.5 per 1,000**, against **2.6** for all workers: **over twice the risk** (2021 rates, same report).
- The same report examines a technical intern killed at a press machine whose work instructions existed **only in Japanese**, and warns that managers too often assume "the Japanese instruction got through."

The government's own 14th Accident Prevention Plan sets a target to bring the foreign-worker injury rate down to the national average by 2027. For a small factory, a single misunderstood instruction can cost an injured colleague, medical bills, lost workdays, an insurance claim and a stopped line.

## 3. Today's workaround: translation without confirmation

Today a worker copies the message, switches to a translation app, pastes it, reads the result and switches back. Every step adds friction, and at the end there is still **no proof**: the translation may be wrong, the key word may still be missed, and the employer never finds out. The mistake surfaces only when the work is already wrong, or someone is already hurt.

## 4. Our answer: Wakatta closes the loop

Wakatta carries the conversation back and forth between both sides until understanding is confirmed, so nobody has to say "I don't understand" out loud.

> **Foreman speaks → AI transcribes → translates into each worker's language → writes short Check Questions → extracts Tasks → workers answer → the foreman sees, at a glance, who truly understood.**

- **Understanding is confirmed.** Each worker answers 1–3 quick questions. Green means understood. Orange or red opens a **Follow-up**, and the AI sends that one worker a simpler **Re-explanation** of the exact word they missed.
- **Shop words never drift.** A shop Glossary locks terms such as 段取り or 金型交換, so the AI cannot paraphrase them away.
- **People stay in control.** The AI prepares a draft; nothing is sent until the foreman approves it.
- **Conversation becomes work.** Tasks found in a message go straight to a shared **Task Board**, with assignees, deadline and machine.
- **Safety is built in.** At a machine, the worker scans its QR code, reads the instructions and passes a Check. Only then does the machine show **Ready**; until then it stays **Locked**.
- **The whole day is covered.** A **Daily Briefing** shows who understood, who only opened it and who has not seen it. **Push notifications** alert workers to new messages. Workers report **Near-misses** with a photo, and **Ask Wakatta** answers questions from the shop's own notes and flags emergencies.

The name is the promise: *"Wakatta?"* means *"Understood?"*, and the app makes sure the answer is true.

## 5. What we built, and how far it is

- **Android app:** Kotlin and Jetpack Compose, 15 feature modules, signed APK 1.0.2.
- **Web app:** React and TypeScript, for the foreman's PC and our landing page.
- **Backend:** Python, FastAPI and PostgreSQL, live on Microsoft Azure.
- **AI:** Groq Whisper turns real speech into text; Gemini handles translation, questions and task extraction. Every AI step has a fallback, so the app keeps working when a model is busy.

**Working now:** voice and text chat with translation; Check Questions, Wakatta status, Follow-ups and Re-explanations; Task Board; Daily Briefing with read receipts; machine Lock/Ready by QR; push notifications; Near-miss reports; the Ask Wakatta assistant; Japanese, English and Bengali.

**Measured quality:** on our evaluation set, the AI passed **30 of 30** cases for machine notes, notices and worker questions. For chat messages it produced a valid draft in **10 of 10** cases and a fully correct one in **7 of 10**. That is exactly why the foreman reviews every draft before it is sent.

**Next:** more worker languages, a larger test set recorded on real shop floors, and a dedicated AI model for each task.

## 6. What makes Wakatta different

1. **It confirms, then follows up.** Translation tools stop at translating. Wakatta checks the answer and repairs the gap.
2. **It ties understanding to safety.** A machine unlocks only after its Check is passed.
3. **It turns speech into action.** One spoken message becomes clear text, questions and tasks.
4. **It protects dignity.** Workers never have to admit confusion in front of others; the app asks on their behalf, privately.

## 7. The team: TechTsunagu

| Member | Role | Email | GitHub |
| :--- | :--- | :--- | :--- |
| Mohammad Minhazul Abedin | Architecture, Android app | minhazulab3din@gmail.com | [minuwu](https://github.com/minuwu) |
| Md. Asif Hossain | Backend, database | asifhossain8612@gmail.com | [MdAsif-Hossain](https://github.com/MdAsif-Hossain) |
| Md Zahidul Hoque Anim | Backend services, cloud deploy | anim3654@gmail.com | [anim710](https://github.com/anim710) |
| Wakib Jahan Galib | AI and prompts, evaluation | xahangalib19@gmail.com | [Ga-lib](https://github.com/Ga-lib) |
| Arjun Saha | Web app, design and brand | arjun144948@gmail.com | [sillyfellow21](https://github.com/sillyfellow21) |

In three days, five of us made **474 commits**. We worked in parallel through a shared board of **27 cross-team requests**, so frontend, backend and AI moved together instead of waiting on one another.

## 8. Links

- **Demo video (YouTube, unlisted):** [YOUTUBE_UNLISTED_LINK]
- **GitHub repository:** https://github.com/minuwu/TechTsunagu
- **Web app / landing page:** https://mango-beach-001af4300.4.azurestaticapps.net
- **Android app (APK 1.0.2):** https://wakattaapk71923.blob.core.windows.net/apk/wakatta-1.0.2.apk

**Sources:** MHLW, 「外国人雇用状況」の届出状況まとめ（令和7年10月末時点）, 2026-01-30 · Nagasaki Labour Bureau, 長崎県における「外国人雇用状況」の集計結果まとめ（令和7年10月末時点）, 2026-01-30 · Sompo Risk Management, Report 277, 外国人労働者の労働災害の現状とその対策, 2025-09-17 · MHLW, 第14次労働災害防止計画.
