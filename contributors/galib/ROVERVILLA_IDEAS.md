# Ideas from Rover Villa for Wakatta?

**Author** Galib (`Ga-lib`) · **Date** 2026-11-22 · **Source** https://rovervilla.space/heroes/phoenix (read 2026-11-22)

Status: proposal for the team. Nothing here is built or agreed yet.

## What the site is

Rover Villa is a memorial site for spacecraft left on Mars and the Moon. Each "hero" page tells one machine's life story. The Phoenix lander page has:
- an interactive 3D model: tap a numbered part (1–9) to see what it did, or pull the parts apart
- a numbered timeline from launch to "last signal"
- archive photos, each with its own credit and source
- a "Written from" source list, and plain notes on what is simplified ("sizes are approximate", "NASA does not endorse this site")
- a heavy NASA 3D app that loads only when you ask
- a tribute wall labelled "saved on this device for now"

It is a learning site, not a factory app. What we can use is its approach, not its features.

## Recommendations (best first)

| # | Rover Villa | Wakatta? version | Cost before Nov 24 | Owner | Do it? |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | "Every part, up close": tap a numbered part | **Machine photo with numbered pins**: each pin is a step or a Glossary Term (非常停止, 保護メガネ…) with its Icon, in the Worker's language. New Check Question type: "Tap the emergency stop." | small: one `pins` field on Machines, one question type; the Foreman places pins, no AI needed | Minhaz (screens), Asif (schema) via XREQ; Galib: AI suggests the step for each pin | **yes** |
| 2 | "Written from" sources, per-photo credits, "simplified" notes | **Provenance line on every AI output**: "From 山口's voice note, 11/22 · AI draft · checked against back-translation · approved by 山口" | tiny: metadata the pipelines already produce | Galib (WS1), Minhaz (display) | **yes** |
| 3 | Download the model to 3D-print your own | **Printable machine card**: A4 with the QR, the Icon steps in ja/bn/en and the Note Version number, to laminate and hang on the Machine | small: extends `/foreman/qr` with a print view | Minhaz | if time |
| 4 | "Their mission ended. Their story didn't." / "Last signal" | **Pitch beat**: when the Veteran 山口 retires, his voice notes stay in the Knowledge Library as the record of how each Machine is really run | none: pitch and script only | whoever writes the script | **yes** |
| 5 | Heavy 3D view loads only when asked | Load Japanese audio, flyer photos and the Knowledge Library only on tap, so the Panel (2 s poll) and phones stay fast on factory wifi | small | Minhaz | if time |

### Why idea 1 matters most

A tap-the-part question checks understanding **without depending on translation**. That answers two founder-board risks ([contributors/asif/founder/board.md](../asif/founder/board.md) §2):
- "a wrong translation that a worker 'passes' is worse than no gate"
- "the AI is a commodity"

Even if Gemini mistranslates a sentence, the Worker must still find the right part on the real Machine. A Daishin Giken judge will recognise this as how training on a real shop floor works.

### Why idea 2 matters

Judges distrust overclaiming ([FRONTEND_REGULATIONS.md](../../docs/FRONTEND_REGULATIONS.md) F-47). Showing where each instruction came from and who approved it makes "the AI only drafts, a person approves" visible on every screen. Asks already quote their source Notice; this applies the same rule everywhere.

## Not worth taking

- **Real 3D models:** we have no 3D models of the Machines, and building them costs too much in two days. An annotated photo gives most of the effect.
- **An embedded external 3D app:** heavy, and off-topic for us.
- **A tribute wall:** device-only even on their site; the Senpai Meet-up log already covers the human side.

## Next steps (if the team agrees)

1. Asif and Minhaz: agree on idea 1 as an XREQ (`pins` on Machines, a `tap_point` Check Question type).
2. Galib: add the provenance fields to the WS1 pipeline output (idea 2).
3. Whoever writes the demo script: add the Veteran beat (idea 4).
