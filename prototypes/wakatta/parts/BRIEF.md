# Wakatta prototype: rules for every part agent

## Product
Wakatta? ("understood?"): a workplace app for a Japanese factory. The Foreman, 田中 (ROLE 'foreman'), uses the UI in Japanese. Foreign Workers use it in English; Rahim (ROLE 'worker') is the live one, and Nguyen and Siti are simulated. Core loop: the foreman speaks, the message is translated, Check Questions are made, and each worker's understanding shows as green, orange or red. Tasks are taken from the chat onto a shared Board. Terms: see ../../../GLOSSARY.md (Foreman, Worker, Machine, Note, Check, Sign-off, Confusion, Lock/Ready, Machine Panel, Briefing, Near-miss Report, Notice, Event, Ask, Senpai, Meet-up, Support Log, Skill Matrix, Skill Passport, Confusion Heatmap, Wakatta Status, Task, Board). Use those words in code and copy.

## Files
- One single-file app is merged from fragments: `parts/skeleton.html` holds the runtime, CSS tokens and components, DATA, Bus, ROLE, ME and helpers. `parts/<P>.css|.screens.html|.tabs.html|.overlays.html|.js|.init.js` are the fragments. `python parts/merge.py` writes app.html.
- READ, never edit: skeleton.html, merge.py, index.html, BRIEF.md, and other agents' part files (unless your prompt gives you ownership).
- Runtime API: C:\Users\minha\.claude\skills\mobile-html-prototype\SKILL.md. Use data-push/data-back/data-open/data-toast, App.push/back/tab/open/close/toast, `.screen`, `.appbar`, `.content`, `.list-item`, `.card`, `.btn`, `.chip`, `.sheet`, `.dialog`, `.stagger` and `.skeleton`.
- Preview your work: `python parts/merge.py --out parts/preview-<P>.html`. This merges ALL parts that exist right now, so other agents' work in progress may appear. That is fine; never "fix" their files.
  - Single phone: open `file:///C:/Users/minha/Desktop/TechTsunagu/prototypes/wakatta/parts/preview-<P>.html?role=foreman` (or `?role=worker`). There, Bus.paired is false.
  - Two phones (same-origin): open `http://127.0.0.1:8766/index.html?app=parts/preview-<P>.html`. A server is already running; do not start or stop servers.
  - You can drive iframes by script: `document.getElementById('foreman').contentWindow.eval('App')`.
- Chrome DevTools MCP: always use new_page with your own isolatedContext ("part<P>") and pass your own pageId. Other agents share the browser.
  - CSS zoom on index.html breaks the tool's click coordinates. Drive by script, or resize the page to at least 1000x1000 first.
- Do not add AI/Claude attribution anywhere. No git commits. Delete your preview file when done.

## Code rules
- Vanilla HTML/CSS/JS. No frameworks or CDN scripts. One-line `// shortcut: <limit>, <when to upgrade>` comments for faked parts, such as fake AI, fake QR camera or fake speech.
- Prefix every CSS class and every top-level JS name with your part's prefix. Wrap your .js and .init.js in an IIFE. Expose only what other parts need, as `window.<prefix>X`.
- `<P>.js` runs BEFORE `App` exists, and SCREENS hooks can run during App's boot. Never touch App synchronously at top level or in a hook's synchronous path; use event handlers or setTimeout. `<P>.init.js` runs after App exists.
- Add screens with `Object.assign(SCREENS, {id(el, params){...}})`. Add data with `Object.assign(DATA, {...})` or new keys, and never replace existing keys.
- Change shared state only inside Bus.on handlers. Bus.emit also fires locally, and on the other phone when paired. Handlers must be idempotent: replace by id, never duplicate.
- Notifications: call `notify({to:'foreman'|'worker'|'all', title, body, push:['screenId', {params}]})` (skeleton helper). Part E renders them.
- Colors: only CSS custom properties (`--primary`, `--surface-1..3`, `--on`, `--on-2`, `--outline`, `--ok/--warn/--bad` with their `-c` variants, `--wait`), so dark mode (part E) works. No hard-coded hex in new UI, except avatar colors.
- Copy: Japanese on the foreman side and English on the worker side. A local `t(ja, en)` is fine (`ROLE === 'foreman' ? ja : en`).
- Android Material 3 look. Touch targets of at least 48px, 16px body text, inline SVG Material icons, no emoji for UI icons. Animate transform/opacity only, 150-500ms. Include loading/empty/error states where the idea needs them.
- Every tappable thing does something. Use a toast "Not in prototype" only as a last resort.

## Done means
Merged preview works for both roles, two-phone sync works where your feature crosses roles, zero console errors (favicon 404 ok), screenshots checked for clipping/overlap with tab bar, FAB, composer. Report: files, screen ids + events + window exports you added, what you verified, what is not done.

## Contract between parts (screen ids, events, data owners)
Other parts may link to these screen ids before they exist. If the screen is missing in your preview, guard with `document.querySelector('.screen[data-screen="id"]') ? App.push(...) : App.toast(...)`.

| Part | Owner files | Screens it adds | Bus events it emits | DATA keys it owns (others read only, guard with `|| []`) |
|---|---|---|---|---|
| A: chat | A.* | `chat` (exists) | msg:new, wk:stage, check:answer, task:update (exist); wk:reexplain, wk:read, wk:nudge, ask:foreman, ask:foremanReply, glossary:add | messages, threads.unread, glossary `[{ja, en, reading}]`, confusions `[{user, term, msgId, at}]` (seed ~12 historic rows across the 3 workers and 5 terms) |
| B+E: shell, finish | B.*, E.* | home, chats, board, machines, me, task, machine (exist); `notifications`, `analytics`, `skills`, `onboarding` | task:update (exists) | notifications rendering, settings `{dark, largeText, offline}` |
| C: safety | C.* | `scan`, `panel` (param {machine}), `note` (param {machine}), `nearmiss`, `nearmiss-list`, `nearmiss-detail` (param {id}), `briefing`, `briefing-board` (param {id}) | signoff `{user, machine, at}`, nearmiss:new, nearmiss:ack, briefing:new, briefing:answer | notes `{machineId: {version, stepsJa, stepsEn, icons, questions}}`, signoffs `[{user, machine, at, version}]`, nearmisses, briefings. On signoff also set `DATA.machines[i].ready[user] = true` |
| D: life | D.* | `life`, `notice` (param {id}), `notice-new`, `ask`, `senpai`, `supportlog` | notice:new, notice:answer, ask:new, ask:reply, meetup:new | notices, events, asks, senpai `{u_rahim: 'u_sato'}` (add a Senpai user to DATA.users), meetups |

Entry points: part B/E adds the links to every C and D screen in home, machines and me, and to `scan` and `panel` from the machines tab and machine detail. Parts C and D also deep-link from inside their own screens.
