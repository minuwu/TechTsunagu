# Frontend Regulations — Building for Japanese Judges

Requirements every frontend change must meet. Judges are Japanese; they read the UI with Japanese expectations for trust, density, typography, wording and flow, and score the project on Japanese hackathon axes (§8). Items are numbered `F-xx` so reviews and PRs can cite them, the same way `G-xx` items in [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md) are cited.

**Requirement levels**
- **MUST** — required for the demo build. A MUST you cannot meet needs a one-line reason in the PR body.
- **SHOULD** — do it unless time runs out. Skip only consciously.
- Items without a tag are MUST.

**Reference standards** (read when in doubt):
- Digital Agency Design System (DADS): https://design.digital.go.jp/dads/ — Japan's official government UI system. Closest thing to a national style guide.
- JIS X 8341-3:2016 (= ISO/IEC 40500:2012 = WCAG 2.0), conformance level AA. WAIC started drafting a revision in October 2025 to match ISO/IEC 40500:2025 (= WCAG 2.2); MLIT expects it from April 2026 onward, not yet published. Design to WCAG 2.2 AA so you are covered either way.
- Since April 2024 the amended 障害者差別解消法 makes 合理的配慮 (reasonable accommodation) mandatory for private businesses too. Web accessibility itself stays an effort obligation, but judges know accessibility is a live topic in Japan.
- Benchmarks judges know: SmartHR, freee, Mercari, LINE, Cookpad, note, Money Forward, Yahoo! JAPAN, Rakuten (for density), Apple Japan / Muji (for calm minimalism).

---

## 0. What Japanese judges reward (the mindset)

| Value | Japanese term | What it means in the UI |
| :--- | :--- | :--- |
| Trust and safety | 安心・安全 (anshin) | Nothing surprising. Every state explained. Clear who runs it, what happens to data. |
| Care for the user | おもてなし (omotenashi) | Anticipate the next question. Helper text, examples, confirmations, polite error messages. |
| Polish of details | こだわり (kodawari) | Aligned spacing, correct line breaks, consistent icons, no broken Japanese. Judges notice small flaws. |
| Respect for all users | 誰一人取り残されない | Readable for elderly users, accessible, works on a phone. |
| Practical value | 実用性 | A real person's problem solved end to end. Not a flashy toy. |
| Social purpose | 社会的意義 | The team can say *why this problem matters*, not only what tech it uses. |

Western "move fast, bold, minimal text" pages can read as *unexplained* or *untrustworthy* to Japanese users. Japanese users accept **more information, clearly organised**, over less information.

---

## 1. Language and copy

- **F-01** Japanese and English both ship, complete. The language is chosen in this order: the user's saved choice (cookie), then the browser language (`Accept-Language`, so Japanese judges get `ja` with no click), then `en`. Every string goes through i18n files (`ja.json`, `en.json`); no hard-coded text. A `日本語 / EN` toggle sits top right.
- **F-02** Japanese copy is written or checked by a fluent speaker. Machine-translated Japanese is an instant credibility loss. If no native checker is available, keep copy short and use the standard phrases in F-04.
- **F-03** Polite form (です・ます調) everywhere. No casual (だ・である) in UI. Not overly humble (謙譲語) either; DADS style is plain polite.
- **F-04** Use standard UI vocabulary:

| Action | Use | Avoid |
| :--- | :--- | :--- |
| Submit / Send | 送信する | サブミット |
| Save | 保存する | セーブ |
| Cancel | キャンセル | 取り消す (only for undo) |
| Next / Back | 次へ / 戻る | — |
| Confirm | 確認する / 確認画面へ | — |
| Log in / Sign up | ログイン / 新規登録 | サインイン (acceptable but less common) |
| Required / Optional | 必須 / 任意 | — |
| Loading | 読み込み中… | — |
| Search | 検索 | — |
| Delete | 削除する | — |

- **F-05** Error messages say what happened, why, and what to do, politely. Example: `メールアドレスの形式が正しくありません。例：taro@example.com` — never "Invalid input" or a raw error code.
- **F-06** Full-width punctuation in Japanese text (`、。「」（）`). Half-width for numbers and Latin (`2026年11月21日`, `ID: 123`). Pick one rule for spacing between Japanese and Latin/numbers (no space is the common choice) and apply it everywhere.
- **F-07** Dates `2026年11月21日（土）` or `2026/11/21`; time 24-hour `14:30`. Currency `¥1,200` or `1,200円`, with `（税込）` where relevant. Names in family-name-first order with separate 姓 / 名 fields; add フリガナ fields when collecting names.
- **F-08** Addresses (when collected): 郵便番号 first (`〒123-4567`, auto-fill prefecture/city), then 都道府県, 市区町村, 番地, 建物名. Phone `090-1234-5678`.

## 2. Typography (the biggest quality signal)

- **F-09** Font stack:
  ```css
  font-family: "Noto Sans JP", "Hiragino Sans", "Hiragino Kaku Gothic ProN",
               "Yu Gothic UI", "Meiryo", system-ui, sans-serif;
  ```
  Never let Japanese fall back to a Chinese glyph font (e.g. PingFang SC, Microsoft YaHei). Set `<html lang="ja">`; it selects Japanese glyph forms. Switch `lang` when the user switches language.
  Latin letters and digits come from **Manrope**, listed first; it has no kana or kanji, so Japanese still renders in Noto Sans JP. UI and content languages are Japanese and English only (Bengali dropped 2026-11-23).
- **F-10** Body text 16px minimum (team floor). Never body text below 14px. Japanese characters are dense; small text looks cramped.
- **F-11** Line height 1.7–1.8 for Japanese body text, 1.4–1.5 for headings. DADS sets a floor of 150% for body text; Western default 1.4 is too tight for kanji.
- **F-12** **SHOULD:** slight letter spacing for body (`letter-spacing: 0.04em`–`0.05em`). Headings may use `font-feature-settings: "palt"` for tighter, polished kerning.
- **F-13** Weights: 400 body, 700 headings. No ultra-thin (100–200) weights for Japanese; strokes disappear.
- **F-14** **SHOULD:** line length 35–45 Japanese characters max for reading text (`max-width: 40em`).
- **F-15** Headings do not break mid-word: `word-break: auto-phrase` (Chrome 119+, needs `lang="ja"`), with BudouX or `<wbr>` for other browsers. Browsers already keep `。` and `、` off line starts for `lang="ja"`; add `line-break: strict` to also keep small kana (`ゃ`, `ッ`) and `ー` off line starts. No single orphan character on a heading's last line.
- **F-16** No italics for Japanese (no true Japanese italic; it renders as a fake slant). Emphasise with weight or colour.
- **F-17** Japanese and English differ in length per meaning. Do not size buttons or cards to one language; if the `en` toggle ships, test both `ja` and `en`.

## 3. Layout and visual style

- **F-18** Clean, calm, trustworthy: white or off-white background, one brand colour, one accent, neutral greys. Avoid neon gradients, glassmorphism overload, dark-only "hacker" style unless the product needs it.
- **F-19** Information is grouped, labelled and dense-but-orderly. Clear section headings, cards with titles, tables for comparisons. Japanese users accept more text on screen if it is organised.
- **F-20** **SHOULD:** 8px spacing grid, consistent radius (4–8px), one icon library only (e.g. Lucide or Material Symbols).
- **F-21** Colour meanings: red = error/warning, blue = link/primary, green = success. Do not use red as the brand primary for normal actions.
- **F-22** **SHOULD:** illustrations soft, friendly and flat (in the style of Japanese public/service sites). A small mascot or friendly empty-state illustration is welcome. No stock photos of unrelated people.
- **F-23** Always show who made it and how to reach them: footer with team name, お問い合わせ (contact), プライバシーポリシー, 利用規約. Placeholder pages are fine. This signals trust.
- **F-24** Mobile first. Many judges open the demo on a phone (QR). Test at 375px wide. Touch targets ≥ 44×44px.

## 4. Flow and interaction (UX)

- **F-25** Predictable, step-by-step flows. Multi-step tasks show a step indicator (`1 入力 → 2 確認 → 3 完了`).
- **F-26** **Confirmation screen (確認画面) before any important submit** (money, personal data, bookings, anything that cannot be undone; not for light actions such as a comment), then a completion screen (完了画面) that says what happened and what comes next. This is a Japanese web convention; skipping it feels unsafe.
- **F-27** Forms: label above the field, `必須` badge (red) on required fields, `任意` on optional ones, placeholder shows an example (`例：山田 太郎`), helper text under the field. Validate inline on blur, not only on submit. Never clear the form on error.
- **F-28** Accept both full-width and half-width input for numbers, phone and postcode; normalise silently (`str.normalize('NFKC')`). Japanese IME users often type full-width digits.
- **F-29** IME safety: never submit or validate on `Enter` while composing. Check both, in every key handler:
  ```ts
  if (e.isComposing || e.keyCode === 229) return;
  ```
  `isComposing` alone fails in Safari: the Enter that confirms a conversion arrives after `compositionend`, so `isComposing` is already `false`; only `keyCode === 229` catches it. Test with a Japanese IME on iOS Safari and on Chrome. Submitting mid-conversion is a classic bug Japanese judges hit immediately.
- **F-30** Every action gives feedback within 100ms: pressed state, spinner, toast `保存しました`. Destructive actions ask for confirmation (`削除してもよろしいですか？`) in an in-page modal, never `window.confirm`.
- **F-31** Always handle loading (skeletons), empty (explain and give the next action) and error (polite message + retry) states. Matches AGENTS.md §4.1.
- **F-32** Navigation: persistent header with logo (links to top page), clear menu labels, breadcrumbs on deep pages. The browser back button always works; never trap users.
- **F-33** Judges can try the product without signing up: `ゲストで試す` (try as guest) or a demo-data mode, plus a short first-time explanation (3 steps max).
- **F-34** **SHOULD:** motion subtle and purposeful (150–250ms ease). No autoplay sound, no aggressive parallax. (`prefers-reduced-motion` is a MUST; see F-38.)

## 5. Accessibility (JIS X 8341-3 level AA)

The baseline (WCAG 2.2 AA: keyboard, focus, labels, contrast, phone width) is [G-43](ENGINEERING_GUIDELINES.md), enforced by `jsx-a11y` and axe. Do not restate it here; the items below are the additions.

- **F-35** Never rely on colour alone; add an icon or text. Buttons, input borders and focus rings need ≥ 3:1 contrast (WCAG 1.4.11), not only text.
- **F-36** Skip link to main content; logical tab order through the 入力 → 確認 → 完了 flow.
- **F-37** Semantic HTML: one `<h1>` per page, ordered headings, `alt` on meaningful images (in the current language), `aria-live` for toasts. Japanese screen readers (PC-Talker, NVDA-ja) read `lang` and `alt` aloud, so both must match the UI language.
- **F-38** Respect `prefers-reduced-motion`. **SHOULD:** layout survives 200% text zoom without overlap or horizontal scroll.
- **F-39** **SHOULD:** mention accessibility in the pitch ("JIS X 8341-3 AA を意識して設計"). Public-sector-minded judges value it.

## 6. Performance and reliability

- **F-40** Japanese web fonts are large. Load Noto Sans JP via Google Fonts with `display=swap` (subsetted automatically) or self-host subsets. Never ship a full multi-MB font file.
- **F-41** **SHOULD:** first meaningful paint < 2s on a mid-range phone; Lighthouse Performance and Accessibility ≥ 90 before demo.
- **F-42** The demo never breaks: mock mode (`USE_MOCK`) as fallback, seeded realistic Japanese sample data (Japanese names, places, ¥ prices), no lorem ipsum, no `undefined` / `NaN` on screen.

## 7. Demo and pitch presentation

- **F-43** Show a real persona and a story: "田中さん（68歳）が…". Walk one complete journey from problem to completion screen.
- **F-44** First screen (ファーストビュー) states in one line what the product does and for whom, with one primary button. Judges decide in about 10 seconds.
- **F-45** **SHOULD:** slides and UI share the same colours and font so the product looks finished.
- **F-46** QR code to the live demo URL on the first and last slide.
- **F-47** Humble, specific claims. No hype words ("revolutionary", "world's first", "革命的", "世界初") unless proven; Japanese judges distrust overclaiming. Show numbers and evidence.

## 8. What Japanese hackathon judges score

Always read the event's published 審査基準 first and re-weight this section to match it. Without one, expect these axes; they recur across Japanese events (e.g. Engineer Guild Hackathon scored 実用性・創造性・ユーザビリティ・完成度・プレゼンテーション), and Japanese judges also look for the social reason behind the project.

| Axis | Japanese term | What judges check | How the frontend earns it |
| :--- | :--- | :--- | :--- |
| Practicality | 実用性 | Does it solve a real person's problem? | F-43, F-48 |
| Originality | 独創性・創造性 | A new angle, not a cosmetic copy of an existing app | F-51 |
| Usability | ユーザビリティ | Can a stranger use it without help? | F-25–F-33, F-50 |
| Completeness | 完成度 | Does the core flow work end to end, polished? | F-42, F-49 |
| Technical skill | 技術力 | Is there real engineering behind the UI? | F-51 |
| Presentation | プレゼンテーション | Clear story, live demo, no overclaiming | F-43–F-47, F-52 |
| Social purpose | 社会性・社会実装 | Why this problem, who benefits, could it really be run | F-48, F-53 |

- **F-48** The "why" is visible. The top page or an about section says in one or two sentences which problem this solves and for whom (課題 → 解決). The pitch opens with the problem, not the tech stack.
- **F-49** A working product beats slides. Every button on the demo path works. Unfinished features are hidden, not shown as dead links or "Coming soon" buttons on the main path.
- **F-50** Hallway test: someone outside the team completes the core journey on a phone, without explanation, in under 60 seconds. Fix whatever they trip on before the demo.
- **F-51** Show what is new and what is under the hood. One "仕組み" (how it works) view or slide explains the original idea and the architecture in plain terms. For AI features, show the basis of the answer (source, reason or confidence) so the result feels trustworthy, not magic.
- **F-52** A demo backup exists: a 1–2 minute screen recording of the core journey and screenshots, ready if the network or the live service fails.
- **F-53** **SHOULD:** a short 今後の展望 (next steps) that is realistic: who would use it, who would run or pay for it, the next feature. Concrete beats grand.

---

## 9. Turning rules into checks (G-01)

Prose is the last resort. Once `frontend/` has code, wire these and add `Enforced by:` to the item:

| Item | Check |
| :--- | :--- |
| F-01 | `eslint-plugin-i18next` rule `no-literal-string` in CI |
| F-09–F-13 | `src/theme/base.css` owns font stack, sizes, line heights for every UI kit ([FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md) A-07); `no-restricted-syntax` bans inline `font-family` |
| F-29 | Unit test: key handler ignores Enter when `isComposing` or `keyCode === 229` |
| F-35–F-38 | axe in e2e (G-43) |
| F-42 | e2e asserts page text has no `undefined`, `NaN`, `lorem` |
| F-47 | CI grep of `ja.json` / `en.json` for 革命的, 世界初, revolutionary, world's first |

---

## Pre-demo checklist

Tick before every demo build. Cite the `F-xx` in the PR for any MUST box you cannot tick; boxes marked SHOULD may be skipped consciously.

- [ ] `lang="ja"`, Japanese font stack, body ≥16px, line-height ≥1.7 (F-09–F-11)
- [ ] All copy native-checked, polite form, standard vocabulary (F-01–F-05)
- [ ] Headings break cleanly, no stray `。` at line start (F-15)
- [ ] Confirmation and completion screens on key flows (F-26)
- [ ] Forms: 必須/任意 badges, examples, inline validation, full/half-width accepted (F-27, F-28)
- [ ] IME Enter does not submit (F-29)
- [ ] Loading, empty, error states everywhere (F-31)
- [ ] Footer with team, contact, privacy, terms (F-23)
- [ ] Works at 375px, touch targets ≥44px (F-24)
- [ ] G-43 passes (axe, `jsx-a11y`); no colour-only meaning, UI contrast 3:1, reduced motion (F-35, F-38); 200% zoom ok (F-38, SHOULD)
- [ ] No `undefined`/lorem ipsum (F-42); Lighthouse ≥90 Perf + A11y (F-41, SHOULD)
- [ ] Guest/demo mode, realistic Japanese sample data (F-33, F-42)
- [ ] `ja` / `en` toggle tested both ways (F-01, F-17, SHOULD)
- [ ] Problem and target user stated on the top page (F-44, F-48)
- [ ] Every button on the demo path works; unfinished features hidden (F-49)
- [ ] Hallway test passed on a phone (F-50)
- [ ] "仕組み" view or slide ready; AI answers show their basis (F-51)
- [ ] Backup demo video and screenshots saved (F-52)
- [ ] QR code on first and last slide, no hype words (F-46, F-47)

---

## Sources

- Digital Agency Design System (DADS): https://design.digital.go.jp/dads/ ; "How to use DADS" deck (本文テキストの行高はフォントサイズの150%以上): https://www.digital.go.jp/assets/contents/node/basic_page/field_ref_resources/79b783f4-5265-4044-a374-f8f6ccd6e5da/d3abed7f/20260806_meeting_How-to-use-DADS_outline_04.pdf
- 総務省 みんなの公共サイト運用ガイドライン (JIS X 8341-3 AA): https://www.soumu.go.jp/main_content/000945249.pdf
- 国土交通省 資料 (JIS X 8341-3 revision toward WCAG 2.2): https://www.mlit.go.jp/sogoseisaku/barrierfree/content/001964853.pdf
- JIS X 8341-3 revision committee (WAIC, October 2025): https://www.issoh.co.jp/column/details/3022/ , https://gihyo.jp/article/2025/01/web-accessibility-prospect
- 障害者差別解消法 改正 (2024) and web accessibility: https://www.issoh.co.jp/column/details/17566/
- Chrome `word-break: auto-phrase` (BudouX): https://developer.chrome.com/blog/css-i18n-features
- Japanese web typography: https://medium.com/@masaharuhayataki/japanese-web-typography-anatomy-and-best-practices-185449b7be65 , https://aqworks.com/blog/perfect-japanese-typography
- Japanese information density: https://bootcamp.uxdesign.cc/common-elements-in-japanese-web-design-93cdec731dde , https://www.utsubo.com/blog/japanese-web-design-style-guide
- Japanese hackathon judging (実用性・創造性・ユーザビリティ・完成度・プレゼン): https://gaishishukatsu.com/archives/057edcf35209458483e8e62a9484988e ; social viewpoint in pitches: https://www.lifehacker.jp/article/2502-techgala-hackathon/
