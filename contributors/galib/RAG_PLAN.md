# RAG plan: Safety Senpai (Work) and Life Guide (Life)

**Author** Galib (`Ga-lib`), AI / LLM · **Date** 2026-11-23 · **Status** v1 shipped (§0); the rest is the plan for **after the hackathon**
**Reviewers** Anim (backend: tables, routes), Minhaz and Arjun (frontend, Android)
**Builds on** what is already on `main`: the Asks pipeline (`answer_ask` + `app.domain.grounding.ground()` + forward to
Senpai), approved Note Versions, the AI chain (`app/ai/chains.py`), `ai_cache`, per-shop languages (ja + en), the eval
runner (`app.ai.eval`) and the demo warm-up.

> **One line:** a Worker asks a question; the app answers **only** from trusted documents, **cites the page**, then
> asks **one check question** (the Wakatta loop). If no document covers it, it says so and sends the question to a
> person. It never guesses.

---

## 0. Shipped 2026-11-23 (team decision: **one** chatbot, before submission)

One bot for Work and Life: `POST /api/knowledge/asks` (Worker; text or voice; optional `machineId`). Screens: XREQ-016.
- **Sources:** the shop's approved Note Versions (the Machine's own ranks first), the district's published Notices,
  the MHLW manufacturing safety manual (English, 36 pieces) and the ISA *Guidebook on Living and Working* (English,
  7th ed., 281 pieces), both Public Data License 1.0, built by `backend/tools/ingest_knowledge.py` into
  `app/seed_data/knowledge.json`. Nagasaki City's guide needs permission for reuse, so it is **not** used.
- **No database change:** BM25 search in memory (`app/domain/retrieval.py`); pgvector is possible on Supabase later.
- **Answer:** `answer_knowledge.v1` through the normal AI chain, `ground()` (only cited, sent sources), language and
  glossary checks, a self-check question; Japanese or spoken questions are also searched in English.
- **Safety:** injury / fire / gas questions get a fixed card, never AI; medical, legal and visa questions are not covered.
- **Verified live** (real Gemini, demo seed): cleaning safety → MHLW p.15; plasma cutter → the shop's instruction;
  garbage → ISA p.142; earthquake (en and ja) → ISA p.128–129; drill → the seeded Notice; ramen → not covered;
  bleeding → the emergency card. Two chatbot takes are in `app/ai/demo_warmup.py`.
- Also fixed on the way: the Gemini SDK's own retries cost ~10 s per busy model, so `gemini.py` now makes one attempt
  and lets `AiRunner` fail over (a busy model fails in ~2 s).

The sections below are the full plan this v1 came from; phases 2–3 (pgvector, OCR of the manga materials, history)
remain for after the hackathon.

## 1. Two knowledge bases

| | **Safety Senpai** (Work tab) | **Life Guide** (Life tab) |
| :--- | :--- | :--- |
| Worker asks | "Why lock out the power before a die change?" | "Which day is burnable garbage?", "Where do I go in a typhoon?", "How do I see a doctor?" |
| Sources, in order | 1. The shop's **approved Note Versions** (our own data, newest version wins)<br>2. MHLW multilingual safety materials, **manufacturing** first | 1. **Published Notices** of the district (already used by Asks)<br>2. Nagasaki City's *Foreign Residents' Life Guide* (procedures, daily life, emergencies, disaster, transport)<br>3. National multilingual guides (CLAIR multilingual living information; JMA heavy-rain guidebook in easy Japanese and English) |
| Not covered → | the Worker's Foreman (a Chat message) | the Worker's Senpai (the existing forward) |
| Scope | shop (`shop:<id>`) + global documents | district (`district:<id>`) + global documents |

**Why these:** Wakatta? exists so that a Worker *understands* a safety instruction; Safety Senpai is that, on demand.
The Life Guide grows the Asks feature we already ship from "posted Notices" to "Notices plus the official guide".

## 2. Sources and licences (checked 2026-11-23)

| Source | Languages | Licence | Notes |
| :--- | :--- | :--- | :--- |
| MHLW 「マンガでわかる働く人の安全と健康」: manufacturing (素形材, 産業機械製造, 電気・電子) | 11 incl. ja, en | **Public Data License 1.0** ([MHLW terms](https://www.mhlw.go.jp/chosakuken/index.html)): reuse and edit allowed with attribution | Logos and **character designs are excluded**: we store and show **text only**, never the manga images. Edited text must say so: 「厚生労働省『…』(URL) を加工して作成」. The PDFs are mostly images (~33 MB), so the text needs OCR (Gemini vision, per page). |
| MHLW safety training texts for foreign workers ([index](https://www.mhlw.go.jp/stf/seisakunitsuite/bunya/0000186714.html)) | 11 | PDL 1.0 | Text PDFs: easier to extract. |
| Shop's approved Note Versions | ja + shop languages | ours | Already reviewed by the Foreman, so the highest trust. |
| Nagasaki City *Foreign Residents' Life Guide* | multilingual (city site) | **to check** (city site terms) | Garbage, emergencies, disaster, transport, public facilities. |
| CLAIR multilingual living information; JMA heavy-rain guidebook | many / ja (easy) + en | **to check** | Linked from Nagasaki Prefecture's foreign residents' desk. |
| District Notices | ja + en | ours | Already in Asks. |

Rule: **no source goes into the index until its licence is written in the `knowledge_sources` row.**

## 3. How it works (fits the current pipeline)

```
question (text or voice)
  → [voice] transcript (XREQ-007: Whisper turbo, then the text path)
  → retrieve: top 6 chunks from the allowed sources (scope + language), plus the shop's Note Versions
  → answer pipeline (new prompt, same runner and chain as today):
        {covered, answer {ja, <shop languages>}, source_ids, check_question}
  → checks: ground() (every cited id was retrieved), glossary lock, language_problems, question_problems
  → covered: answer + "Source: <title>, p. <n>" chips + one check question
    not covered: "No guide covers this; sent to your Senpai / Foreman" (existing forward)
```

- **Retrieval:** our corpus is small (hundreds of pages), so: Postgres **pgvector** (Supabase has it) with Gemini
  embeddings, plus keyword match on shop words (Glossary Terms) so 「玉掛け」 always finds 玉掛け pages.
  No separate vector service.
- **Chunks:** one page or section, 300–600 Japanese characters, with `source_id`, `page`, `heading`, `text_ja`
  (and the English text when the source has it, to save a translation).
- **Fixed answers, never AI:** an injury, fire, gas or "someone is trapped" question shows the shop's emergency card
  (stop, call the Foreman, 119) first. Medical: where to go and how (from the guide), never what the illness is.
  Visa and labour law: out of scope, "ask your support organisation".
- **Cache:** the same question with the same sources hits `ai_cache`, as every pipeline does today.

## 4. Data model (Anim: one migration)

| Table | Columns |
| :--- | :--- |
| `knowledge_sources` | id, kind (`safety_guide`, `life_guide`), scope (`global`, `district:<id>`, `shop:<id>`), title, url, publisher, licence, language, version, fetched_at, active |
| `knowledge_chunks` | id, source_id, page, heading, text_ja, text_en (nullable), embedding (vector), created_at |
| `knowledge_asks` | like `asks`: who, question, transcript, covered, answer, source ids, check result, forwarded_at |

Note Versions are not copied into chunks: the shop's current approved versions are read directly (few, small), so a
new approval is searchable at once.

## 5. Who does what

| Who | What |
| :--- | :--- |
| **Galib** | `python -m app.ai.ingest` (download, OCR per page with Gemini, chunk, embed, store, with attribution); the answer prompt (`answer_knowledge.v1.md`) and checks; retrieval function; emergency/out-of-scope rules; eval set (20 questions per base: 4 not covered, 2 prompt injections, 4 shop-word questions) in `docs/ai-eval.md` |
| **Anim** | migration (pgvector, the 3 tables); routes `POST /api/knowledge/asks` (Worker), `GET /api/knowledge/asks` (own history), `GET /api/knowledge/sources` (Foreman: what is indexed); forward reuses the Senpai / Chat paths |
| **Minhaz, Arjun** | web (v4 kit) and Android: an ask box on the Machine screen (Safety) and in Life; source chips with page number and an "open source" link; the check question; the "sent to your Senpai" state |

Requests to file when we start: **XREQ-016** (Anim: tables and routes) and **XREQ-017** (Minhaz/Arjun: screens).

## 6. Order and time (phase 0 shipped; phases 1–3 after submission on 24 Nov)

| Phase | What | Time |
| :--- | :--- | :--- |
| 0 (hackathon) | ~~Pitch only~~ **Shipped instead** (team decision, 2026-11-23): one bot for Work and Life, see §0 | done |
| 1 | Life Guide first (lowest risk: extends Asks, same screen): ingest Nagasaki guide + CLAIR, retrieval, prompt, eval | 2–3 days |
| 2 | Safety Senpai: MHLW manufacturing materials (OCR) + Note Versions, Machine-screen ask box, emergency card | 3–4 days |
| 3 | Re-ingest job when a source changes; Foreman view of what is indexed; more languages per shop | 2 days |

## 7. Quality bar (done means)

- Every answer cites at least one retrieved chunk, and `ground()` rejects any other answer.
- Eval: ≥ 90% correct covered / not covered, 100% of not-covered questions forwarded, 0 answers from outside the
  sources, both prompt injections ignored, every shop-word question keeps the fixed term.
- Attribution line on every answer that uses a public source; no manga images stored or shown.
- Works offline with `AI_MODE=fake`; a model failure falls back to "sent to your Senpai", never a 500.

## 8. Risks

| Risk | Mitigation |
| :--- | :--- |
| A wrong safety answer | Sources only, cited pages, check question, Foreman's Note Versions outrank the general guide, emergency card is fixed text |
| Out-of-date guides | `version`, `fetched_at`, re-ingest job; answers show the source date |
| Manga PDFs are images | OCR with Gemini vision, a person spot-checks 10 pages per source |
| Licence of city guides | not indexed until checked (§2 rule) |
| Free-tier quota | embeddings once at ingest; answers cached; paid tier for production |

## 9. Questions for the team

1. **Anim:** pgvector on our Supabase project: OK to enable the extension?
2. **Minhaz:** Safety Senpai on the Machine screen, the Worker home, or both?
3. **Everyone:** Life Guide first (phase 1) or Safety Senpai first? I suggest Life first (lower risk, same screen as Asks).

**Answers (2026-11-23, Asif):** 1. pgvector on Supabase: **OK**, Anim enables it when phase 1 starts. 2 and 3: **Minhaz decides** (placement, one bot or two names, Life Guide or Safety Senpai first); the background he needs is in [contributors/minhaz/CHATBOT_FROM_ASIF.md](../minhaz/CHATBOT_FROM_ASIF.md), and his answers go in XREQ-016.
