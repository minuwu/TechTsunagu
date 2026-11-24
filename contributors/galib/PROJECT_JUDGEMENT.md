# Judgement of Wakatta? (web app + backend, Android not included)

**Date:** 23 Nov 2026 · **Judged against:** the BJIT *Hackathon Participant Guide* (22 Nov 2026), evaluation points and required items
**Method:** taken from the Hedera "submission validator" skill: evidence from the repository, a 1–5 score per criterion, a total, and improvements ranked by score impact. BJIT's own 5 criteria are used (equal weight, as the guide gives no weights). The Hedera skill itself was not used: 35% of its score is Hedera blockchain usage, which does not apply to this project.

## Scorecard

| BJIT criterion | Score | Key finding |
| :--- | :---: | :--- |
| Problem definition & fit with the theme | **4/5** | Strong, clear problem: foreign factory workers must *prove* they understood a safety instruction before the machine unlocks, and the same app connects them to their town. "Tsunagu" fits three ways: Foreman ↔ Worker, Worker ↔ town, Japanese ↔ own language. Missing: no real, cited numbers. |
| Technical skill | **5/5** | 82 API routes, contract-first (`docs/openapi.yaml`), live on Azure. ~360 backend tests and 98 frontend tests passing, CI on every push. AI with model failover, grounding checks, a RAG chatbot with page citations, measured evals (`docs/ai-eval.md`, `docs/ai-eval-chatbot.md`, `docs/ai-bakeoff.md`). |
| Completeness (what actually works) | **3/5** | The backend works live. The web app runs only on seeded demo data: it is not connected to the real API (Bearer login, XREQ-001) and is not hosted online (XREQ-013 pending). |
| Originality | **4/5** | "Understanding is checked, not assumed": the machine stays locked until the Worker passes a Check, wrong answers point to the exact shop word, and the Foreman reviews a back-translation. The chatbot is common as an idea (RAG bots); it supports the story but is not the headline. |
| Structure & clarity of the proposal | **2/5** (not done yet) | No proposal PDF or demo video yet. The README still says "Hackathon Repository Template… It holds no app code". |
| **Total** | **18/25 = 72%** | With the top fixes below: realistically **21–22/25 (84–88%)** |

## Fix before the deadline (these can cost points however good the app is)

1. **The GitHub repository is PRIVATE.** The guide requires it to be public (a required item). A scan of the current files and the full git history found **no API keys or passwords**. It does contain an Azure tenant ID and the Supabase project name (identifiers, not secrets); the owner should know before switching to public.
2. **Replace the README.** Reviewers "check the repository to confirm it matches your proposal", and today the README describes an empty template. It should say what Wakatta? is, show a screenshot, explain how to run it, link the live API, list what works vs what is planned, and say where AI tools were used.
3. **Code from before the hackathon.** The rules allow only open-source libraries, no code written before the hackathon. Commit `9cb9972` (00:31 on 22 Nov, 31 minutes after the start) added about 14,800 lines, including a frontend skeleton (configs, a health page, the UI-kit shell). A reviewer could read this as prepared code. Safest: one honest line in the proposal, e.g. "We started from our own empty team template (tooling, lint rules, a health-check page); all Wakatta? features were written 22–24 Nov." **Team decision.**
4. **State AI tool usage in the proposal** (a rule): the coding agents used (Claude Code and others) and that the app itself uses Gemini and Groq.
5. **Demo video:** at most 3 minutes, English, YouTube "Unlisted", showing the product actually working.

## Top 5 improvements, ranked by score impact

| # | Improvement | Raises | Effort |
| :---: | :--- | :--- | :--- |
| 1 | Make the repository public and write the new README | Proposal clarity; avoids a required-item deduction | 1 hour |
| 2 | Proposal PDF (max 3 pages, English, `[TeamName]_Proposal.pdf`) built around **one hero flow**: Foreman records → Worker reads → Check → machine unlocks → chatbot answers with the page | Proposal 2 → 4 | 3–4 hours |
| 3 | Host the web app online (XREQ-013, static build on Azure); a public link judges can open, even on demo data | Completeness 3 → 4 | 1–2 hours |
| 4 | Honest "Implementation status" table: *works now* (live backend with real AI; web app on seeded data) vs *next* (web app on the live API, Android, vector search) | Completeness (honesty is rewarded) | 30 min |
| 5 | Cite 2–3 real, sourced numbers, e.g. the government's count of foreign workers in Japan (厚生労働省「外国人雇用状況」); take the figures from the official source | Problem definition 4 → 5 | 30 min |

## Strongest points (lead with these)

- **The demo moment:** the Machine page stays **LOCKED** until the Worker passes the Check; a wrong answer tells the Foreman which shop word was misunderstood.
- **Evidence over claims:** measured AI eval (30/30 on the note and notice cases), a transcription bake-off with error rates, the chatbot's 0 made-up answers, grounding that rejects any uncited answer.
- **Safety-first AI:** the emergency card is never written by AI, "not covered" instead of a guess, a Foreman back-translation check on every AI draft.
- **Fit for these judges:** Daishin Giken (a manufacturer) fits the machine-safety story; Nagasaki Prefecture fits the district notices and the life guide (garbage, earthquakes, hospitals).

## Weaknesses a judge may raise

- **"Does the web app use the real AI?"** Not yet: it runs on seeded data. Say so openly and show the live backend.
- **Free AI quota:** Gemini returned "503 busy" many times on 23 Nov. Film only the warmed questions.
- **Very broad scope** (Course, Board, Senpai, Chat, Contacts, Talk Threads). In 3 minutes, show one flow well, not ten.
