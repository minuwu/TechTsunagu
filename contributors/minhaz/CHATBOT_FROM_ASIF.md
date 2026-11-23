# The Wakatta? chatbot: what it is, what it does, and three decisions for you

**To:** Minhaz (web + Android) · **From:** Asif · **Date:** 2026-11-23
**Related:** XREQ-016 (screens, in `AGENTS.md`), Galib's plan [`contributors/galib/RAG_PLAN.md`](../galib/RAG_PLAN.md)

Galib built the chatbot's backend and it is live on the API. The screens are yours. This note explains the feature
from the user's side, then the API, then the three things **you decide**. Nothing here needs me; ask Galib about
answers and Anim about the API.

---

## 1. What it is, in one paragraph

A Worker types or speaks a question ("Why must I stop the machine before cleaning it?", "Which day is burnable
garbage?"). The bot answers **only** from documents we trust, **shows where the answer came from** (title and page),
and then asks **one practice question** to check the Worker understood. That last step is the same idea as the whole
app: not "was it said?" but "was it understood?". If no document covers the question, the bot **does not guess**:
it says so and offers to send the question to a real person, the Worker's Senpai.

So it is not a general chatbot. It is a way to look things up in the shop's own instructions and two official
guides, with proof of where each answer comes from.

## 2. Where its answers come from (in this order)

1. **The shop's approved machine instructions** (Note Versions the Foreman approved). On a Machine screen, that
   Machine's own instruction ranks first.
2. **The district's published Notices** (garbage days, drills, events from the community).
3. **Two official guides, English editions**, which allow reuse with attribution:
   - MHLW's manufacturing safety manual (work safety),
   - ISA's *Guidebook on Living and Working* (life in Japan: garbage, disasters, hospitals, procedures).

It answers in **Japanese and the Worker's language** (the demo shops are Japanese + English only: Bengali was dropped).

## 3. The four things a Worker can see (your screen states)

| State | When | What to show |
|---|---|---|
| **Answered** | `covered: true` | The answer in the Worker's language (and Japanese, smaller). Under it, **source chips**: title + "p. 15" when `page` is set; tapping a chip opens `url`. Under that, the **attribution line** when present (the licence requires it, so always show it). Then the **practice question**. |
| **Practice question** | `check` is present | Show it like a Check Question (prompt + 3 options). Grade it **on the phone** with `check.correctIndex`: green "Understood", or show the right option. It is **practice only**: it never calls `/api/checks` and never changes a Sign-off or unlocks a machine. |
| **Not covered** | `covered: false`, `emergency: false` | "No guide covers this yet." + a button **Ask my Senpai** (see §5). Also what you show when the AI is busy: the API never returns 500 for this; it says "not covered". |
| **Emergency** | `emergency: true` | A **red, fixed card**: stop work, tell the Foreman, call 119. The text is written by people, never by AI. Show it big, above everything; no practice question. Triggered by injury, fire, gas or "someone is trapped" questions. |

Out of scope on purpose: medical diagnosis, legal and visa questions come back "not covered" (the person they
should ask is the support organisation, not a bot).

## 4. The API (live now)

`POST /api/knowledge/asks` with the Worker's token (`Authorization: Bearer …`), **multipart form**:

| Field | |
|---|---|
| `text` | the question, up to 1000 characters, **or** |
| `audio` | a recording (same formats and size limit as voice notes) |
| `machineId` | optional: send it from a Machine screen so that Machine's instruction ranks first |

Response `data` (`KnowledgeAnswer` in `docs/openapi.yaml`; generate types as usual):

```json
{ "question": "Why must I stop the machine before cleaning it?",
  "covered": true, "emergency": false,
  "answer": { "ja": "機械を掃除するときは…", "en": "Be sure to completely stop the machine before cleaning it. …" },
  "sources": [ { "id": "mhlw-safety:p15:1", "kind": "safety", "title": "For Work Safety and Health: …",
                 "page": 15, "url": "https://www.mhlw.go.jp/…/0000148471.pdf",
                 "attribution": "出典：厚生労働省「…」… をもとに Wakatta? が抜粋・要約" } ],
  "check": { "id": "q1", "termId": null, "prompt": { "ja": "…", "en": "What must you do when cleaning the machine?" },
             "options": [ { "en": "Clean it after stopping the machine completely" }, { "en": "…" }, { "en": "…" } ],
             "correctIndex": 0 } }
```

- Errors: `403` (not a Worker), `413` / `415` (recording too long / wrong format), `422` (neither text nor audio).
- **Timing:** text answers take a couple of seconds; voice 3–8 s. Show a spinner and keep the question visible.
- `answer` is `null` when not covered. `sources` is empty then.
- Unlike real Checks, `correctIndex` **is** sent here, on purpose: it is practice and grants nothing.

Try it on the live API (`https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io`): sign in as
Worker **Rahim Uddin** (shop `SASEBO01`, PIN `2468`, see `docs/FRONTEND_API_HANDOVER.md`), then ask:

| Question | Expected |
|---|---|
| "Why must I stop the machine before cleaning it?" | answered, MHLW p. 15 |
| (on the plasma cutter's Machine screen) "How do I start safely?" | answered from the shop's own instruction |
| "Which day is burnable garbage?" | answered, ISA guide p. 142 or a district Notice |
| "What should I do in an earthquake?" (English or Japanese) | answered, ISA p. 128–129 |
| "Where can I eat good ramen?" | not covered → Ask my Senpai |
| "My hand is bleeding" | the red emergency card |

## 5. "Senpai" means two different things here: please do not mix them

1. **Ask my Senpai** (exists today): a **person**. When the bot cannot answer, the Worker taps this and the question
   goes to their human Senpai through the **existing** Asks feature: `POST /api/asks` with the question as `text`
   (send an `Idempotency-Key`), then `POST /api/asks/{id}/forward`. The Senpai answers in their private link, and the
   reply shows up in the Worker's Asks list as today. Nothing new on the backend.
2. **Safety Senpai** (Galib's plan, after the hackathon): the **name of the work-safety half of the bot**. Galib's plan
   splits the bot into two later:
   - **Safety Senpai** (Work tab): machine instructions + official safety manuals, asked from the Machine screen.
     Later it adds the MHLW safety *manga* booklets (text only, via OCR) for more topics.
   - **Life Guide** (Life tab): Notices + official living guides (garbage, disaster, hospitals). Later it adds city and
     national guides once their licences are checked.

   Today both halves already work as **one** bot; the split is about **how the screens present it** and **which half
   gets more sources first** after the hackathon.

## 6. Your three decisions

Please write your answers in XREQ-016 (`Status / Notes`) so Galib and Anim see them.

**Decision 1: where the ask box goes.** XREQ-016 (Galib) proposes **both**; you decide.

| Option | Good | Watch out |
|---|---|---|
| **Machine screen** | Sends `machineId`, so answers about *this* machine come from *this* shop's instruction first. Strongest demo moment: "I'm at the press, I ask, it cites our own instruction and checks I understood." | Life questions (garbage) feel out of place there. |
| **Worker home** | One obvious place for every question, work or life. | No `machineId`, so machine answers rely on the general manual more. |
| **Both** (proposal) | Each place sends the right context. | Two entry points to build and test, on web and Android. |

**Decision 2: one bot or two names on screen.** Show it as **one** "Ask" box everywhere, or as **Safety Senpai** on
the Machine / Work side and **Life Guide** on the Life side (same API either way; the name only changes the
wording and which screen it lives on).

**Decision 3: which half grows first after the hackathon** (Galib's question). Galib suggests **Life Guide first**:
lower risk (it extends the Asks screen you already have) and the licences are simpler. **Safety Senpai first** is
closer to the product's core (machine safety) and the demo story, but needs OCR of image-only booklets and more
checking, because a wrong safety answer is worse than a wrong garbage answer.

My only request, whatever you choose: in the pitch and the demo, present the bot as **supporting** the main story
(the Machine Panel locks until the Worker has passed the Check), not as the headline. Judges see many chatbots; the
"cited, checked, and handed to a person when it does not know" part is what makes ours different.

## 7. Where to ask

- What the bot answers, sources, wording, the emergency card: **Galib**.
- The API, errors, the Asks forward: **Anim** (he runs the backend and Azure now).
- Write decisions and questions in XREQ-016 in `AGENTS.md`.
