# Spot illustrations manifest

Folder: `frontend/public/brand/images/spot/`. Track R, 800x800, one soft `#D8D7D3` rounded card per scene, transparent outside it, cobalt ledge at the bottom edge so the Nekko bust cut sits on the ledge. Nekko (scale 0.52, one scale in all scenes) is copied inline from `master/nekko-expressions`; props use stroke 18 (Nekko's 34 outline at that scale) for main lines and 10 for details, round caps and joins. Each stem has `<stem>.svg`, `<stem>-800.png` (transparent) and `<stem>-800.webp`. All SVGs are 16-19 KB. Colours: `#1351AA`, `#141414`, `#FFFFFF`, `#D8D7D3`, plus `#B3261E` (404, error, offline) and `#1E6B3A` (success). No text, no gradients, no filters. Generator: scratch `work/spot/build.py` (not shipped).

| File | Purpose | Expression | Notes |
| :--- | :--- | :--- | :--- |
| `role-worker` | Worker role card | happy | cobalt hard hat on Nekko, two gears |
| `role-foreman` | Foreman role card | neutral | clipboard with ticks, approve badge |
| `role-poster` | Community Poster role card | happy | pinned notice sheet with calendar grid, megaphone |
| `role-senpai` | Senpai role card | happy | steaming tea cup, small house |
| `step-1-scan-qr` | Onboarding step 1 | neutral | locked machine panel in jet, padlock, block QR sticker; 1 of 4 dots |
| `step-2-take-the-check` | Step 2 | neutral | check card with question and three answer pills, one chosen; small jet lock badge; 2 of 4 dots |
| `step-3-machine-unlocked` | Step 3 | happy | cobalt panel, open white padlock; 3 of 4 dots |
| `step-4-ready` | Step 4 | happy | Nekko with hard hat, cobalt panel with big tick; 4 of 4 dots |
| `state-empty` | empty list | sleepy | empty tray, rising dots |
| `state-no-results` | search with no hit | worried | magnifier with a minus |
| `state-offline` | no connection | worried | wifi icon with danger-red slash |
| `state-404` | page not found | worried | broken chain link, red cracks, no digits |
| `state-error` | something broke | error | danger-red warning triangle |
| `state-success` | done | happy | green tick disc, sparkles |

Steps read as a sequence: locked jet panel, then cobalt panels; the four ledge dots fill one per step.

## Known limits

- Props sit in the right third; Nekko fills the left two thirds, so the layout is the same across the set.
- Step-2 shows a small question mark drawn as a path (the brand symbol), not a font glyph.
- The hard hat is drawn flat over Nekko's head and covers part of the brow mark; the ears are hidden under it.
- Wifi and broken-link props are fairly small at 200 px but read in the contact sheet.
- Nekko surprised expression is not used (known master flaw).
