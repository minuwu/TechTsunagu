# Google Play store listing: draft (Wakatta? for Android)

> **DEFERRED.** The team decided not to upload to Google Play for now (`docs/ANDROID_RELEASE.md` decision R1). The first APK is distributed directly and runs in **mock mode** (R6): it has no backend, so the description below must not be used for it as written. Several paragraphs (chat between people, Talk Threads, notices from a community office, sending a question to a Senpai) describe a backend-connected build. Kept for a future Play listing; the text of the install guide for the direct APK is in [install-guide.draft.md](install-guide.draft.md).

**Status** DRAFT, written 2026-11-23. **Bengali text has not been reviewed by a native speaker and must be before submission.** Japanese and English need a native read on a real phone too (ANDROID_RELEASE WB-15).

## Rules this draft follows

- Describe only what [PRODUCT.md](../../PRODUCT.md) and [GLOSSARY.md](../../GLOSSARY.md) say. No user counts, awards, ratings, endorsements, "trusted by" lines, customer names, or performance numbers. There are none to claim (PRODUCT.md "Evidence on Hand").
- No safety guarantees. The Lock is software only: the Machine Panel shows Lock or Ready and no PLC or relay is connected (ADR 0003). The listing must never say the app prevents accidents or makes a machine safe. Every language version carries the one-line disclaimer below.
- Store-listing limits (https://support.google.com/googleplay/android-developer/answer/9859152, checked 2026-11-23): app name 30 characters, short description 80, full description 4,000 (full-width and half-width count the same); phone screenshots 2 to 8, PNG or JPEG, up to 8 MB each, each side between 960 px and 3,840 px; the page warns against repetitive or irrelevant keywords in the name and descriptions. Feature graphic and icon sizes: `UNVERIFIED` here, check the Play Console asset page.
- Screenshots use the seeded fictional demo data only (`frontend/src/services/mocks/seed.ts` names), never a real person, workplace or Notice.
- Languages in the app first release: Japanese, English, Bengali (the stated order in ANDROID_RELEASE). Do not list Indonesian or Vietnamese until the app has them and a native speaker reviewed them.

## App name

`Wakatta?` (8 characters). Optional suffix after review: ` - わかった?`. Do not add keywords.

## Short description (80 characters maximum)

| Language | Text | Length |
| :--- | :--- | :--- |
| en | Understood, at work and in town: instructions, checks and local notices. | 72 |
| ja | 仕事もまちも「わかった」に。作業の説明、確認、地域のお知らせ。 | 31 |
| bn | কাজে ও এলাকায় বুঝে নিন: নির্দেশনা, চেক ও স্থানীয় নোটিশ। | 54 |

Counts are by eye; recount in Play Console. "Understood, at work and in town" is the tagline in PRODUCT.md "Brand Commitments"; the Japanese and Bengali lines are translations for review, not approved brand text.

## Full description (4,000 characters maximum)

### English

Wakatta? helps people understand their work and their town.

AT WORK
A Foreman records know-how by voice or text for a Machine. The app turns it into plain Japanese (やさしい日本語), Bengali and English, and keeps the original. A Worker scans the QR sticker on the Machine, reads the instruction in their own language, and answers a short Check. When the Check is passed, the Machine Panel shows Ready for that Worker. Until then it shows Lock. When an answer is wrong, the Foreman sees which shop word caused it, so the next explanation can be simpler.

AT HOME
Local notices (garbage rules, drills, festivals) arrive as plain steps with dates, and a calendar file you can add to your phone. If you have a question about local life, the app answers only from posted notices and shows the source. If the notices do not cover it, it can send the question to your Senpai, a local Japanese helper.

CHAT AND PEOPLE
Chat with your Foreman, with messages translated for each person. Connect with other people by scanning their personal QR code. Talk Threads let you keep your own record of a conversation you had in daily life, such as with an elderly resident: what they said, what it means, and how you replied. Your employer cannot read Talk Threads.

LANGUAGES
Japanese, English and Bengali.

GOOD TO KNOW
- You need an account made by your workplace or community office. You sign in with your name and a PIN.
- The Lock in this app is a software status. It is not a safety device, and it does not control or stop any machine. Always follow your workplace's safety rules.
- Voice recordings are not kept by our server, but they are sent to outside services to make text. See the privacy policy: [privacy policy URL].
- Support: [support email]

### 日本語

Wakatta?(わかった?)は、仕事とまちの暮らしを「わかった」に変えるアプリです。

【仕事で】
班長が機械ごとに、声または文字で作業のコツを記録します。アプリはそれをやさしい日本語・ベンガル語・英語にして、元の内容も残します。作業者は機械のQRシールを読み取り、自分の言葉で説明を読み、短い確認(チェック)に答えます。合格すると、その作業者には機械パネルに「Ready」が表示されます。それまでは「Lock」です。間違えた答えは、どの現場用語でつまずいたかが班長に伝わるので、次の説明をやさしくできます。

【暮らしで】
ごみの出し方、防災訓練、お祭りなど地域のお知らせが、日付付きのやさしい手順で届きます。カレンダーに追加できるファイルもあります。地域の暮らしの質問には、掲載されたお知らせの内容だけで答え、出典を示します。お知らせにない質問は、日本人の先輩(Senpai)に送れます。

【チャットと人とのつながり】
班長とのチャットは、相手の言語に翻訳されます。相手のQRコードを読み取って、つながることもできます。「Talk Thread」では、日常で出会った方(たとえば高齢の方)との会話を、何と言われたか、どんな意味か、どう返したか、自分用に記録できます。雇用主はTalk Threadを読めません。

【対応言語】
日本語、英語、ベンガル語

【ご注意】
- 職場または地域の窓口が作成したアカウントが必要です。名前とPINでログインします。
- このアプリのLock(ロック)は、画面上の表示です。安全装置ではなく、機械を止めたり動かしたりするものではありません。必ず職場の安全ルールに従ってください。
- 録音は当社のサーバーには保存されませんが、文字にするために外部のサービスへ送られます。詳しくはプライバシーポリシー: [privacy policy URL]
- お問い合わせ: [support email]

### বাংলা (needs native review)

Wakatta? কাজ ও এলাকার জীবনে আপনাকে "বুঝেছি" বলতে সাহায্য করে।

কাজের জায়গায়
ফোরম্যান প্রতিটি মেশিনের কাজের কৌশল কথা বা লেখায় রেকর্ড করেন। অ্যাপটি তা সহজ জাপানি, বাংলা ও ইংরেজিতে রূপান্তর করে এবং মূল লেখাও রাখে। কর্মী মেশিনের QR স্টিকার স্ক্যান করে নিজের ভাষায় নির্দেশনা পড়েন এবং একটি ছোট চেকের উত্তর দেন। চেকে পাস করলে মেশিন প্যানেলে সেই কর্মীর জন্য "Ready" দেখায়। তার আগে "Lock" থাকে। উত্তর ভুল হলে ফোরম্যান দেখতে পান কোন কাজের শব্দে সমস্যা হয়েছে, তাই পরের ব্যাখ্যা আরও সহজ করা যায়।

বাসায় ও এলাকায়
আবর্জনা ফেলার নিয়ম, মহড়া, উৎসবের মতো এলাকার নোটিশ তারিখসহ সহজ ধাপে আসে, আর ক্যালেন্ডারে যোগ করার ফাইলও থাকে। এলাকার জীবন নিয়ে প্রশ্ন করলে অ্যাপ শুধু প্রকাশিত নোটিশ থেকে উত্তর দেয় এবং উৎস দেখায়। নোটিশে উত্তর না থাকলে প্রশ্নটি আপনার সেম্পাই (স্থানীয় জাপানি সাহায্যকারী) কে পাঠানো যায়।

চ্যাট ও মানুষের সঙ্গে যোগাযোগ
ফোরম্যানের সঙ্গে চ্যাট করুন; বার্তা প্রত্যেকের ভাষায় অনুবাদ হয়। অন্যের ব্যক্তিগত QR কোড স্ক্যান করে যোগাযোগ করতে পারেন। "Talk Thread"-এ প্রতিদিনের জীবনে কারও সঙ্গে (যেমন একজন বয়স্ক মানুষ) হওয়া কথোপকথন নিজের জন্য লিখে রাখতে পারেন: তিনি কী বলেছেন, তার অর্থ কী, আপনি কী উত্তর দিয়েছেন। আপনার নিয়োগকর্তা Talk Thread পড়তে পারেন না।

ভাষা
জাপানি, ইংরেজি ও বাংলা।

জেনে রাখুন
- আপনার কর্মস্থল বা এলাকার অফিস থেকে তৈরি করা অ্যাকাউন্ট লাগবে। নাম ও PIN দিয়ে সাইন ইন করতে হয়।
- এই অ্যাপের Lock একটি সফটওয়্যার অবস্থা। এটি নিরাপত্তা যন্ত্র নয় এবং কোনো মেশিন চালায় বা থামায় না। সবসময় কর্মস্থলের নিরাপত্তার নিয়ম মেনে চলুন।
- ভয়েস রেকর্ডিং আমাদের সার্ভারে রাখা হয় না, তবে লেখা তৈরির জন্য বাইরের সেবায় পাঠানো হয়। গোপনীয়তা নীতি: [privacy policy URL]
- সহায়তা: [support email]

## What the descriptions must stay true to

Before submission, check each sentence against a shipped feature. Remove any line whose feature is not in the release build. The "Talk Thread", "My QR" and "Periodic Interview" features depend on backend blockers B6 in [ANDROID_RELEASE.md](../ANDROID_RELEASE.md); if they are not live, delete those paragraphs.

## Release notes template (per release, per language, 500 characters maximum is `UNVERIFIED`: check the Play Console limit)

```
en: What's new in [versionName]
- [one plain sentence per change a user can see]
- Fixes: [one plain sentence]

ja: [versionName] の新機能
- [ユーザーに見える変更を1文ずつ]
- 修正: [1文]

bn: [versionName]-এ নতুন কী
- [ব্যবহারকারী যা দেখতে পাবে, প্রতিটি পরিবর্তনের জন্য এক বাক্য]
- সমস্যা সমাধান: [এক বাক্য]
```

Rules: no performance claims unless measured and recorded in the gate record; no promise of future features; mention a data or permission change explicitly and link the privacy policy.

## Screenshot plan

Phone, 2 to 8 images, PNG or JPEG, each side 960 to 3,840 px (limits above). Produce the same set in ja, en and bn, using seeded fictional names. Take them from a release-like build on a real phone.

| # | Screen | What it shows | Languages |
| :--- | :--- | :--- | :--- |
| 1 | Sign-in | Name list from the workplace roster, then PIN | ja, en, bn |
| 2 | Machine scan | Camera on a Machine QR sticker (fictional machine) | ja, en, bn |
| 3 | Note | The instruction in the user's language with icons | ja, en, bn (Bengali Note for the Bengali set) |
| 4 | Check | One Check Question | ja, en, bn |
| 5 | Ready | The Ready state for the Worker | ja, en, bn |
| 6 | Notice | A local notice as plain steps with dates | ja, en, bn |
| 7 | Chat | A translated Foreman message and the Wakatta Status | ja, en, bn |
| 8 | Talk Thread | A fictional entry; no real person | optional; only if B6 is live |

Do not show: real names, real workplaces, a real flyer, the demo PINs, a red "Lock" on a screen described as safe, any chart or number that implies usage.

Also needed (sizes `UNVERIFIED` here): app icon, feature graphic. Use the brand kit in `frontend/public/brand` once Design confirms it (whether those assets exist and are final was not verified in this task).

## Category and content-rating notes

- **Category:** not decided. Candidates are Productivity or Business; Education is not accurate for the main use. The category list was not checked: `UNVERIFIED`. Decide in Play Console and record why.
- **Content rating:** a questionnaire is required for each app, answers decide the rating shown, and it must be redone whenever content or features change (https://support.google.com/googleplay/android-developer/answer/188189, checked 2026-11-23). Answer truthfully. Facts to state: the app has chat between users (Foreman and Workers, and between Contacts connected by QR) with machine translation; users can block a Contact; no violence, gambling, ads or purchases; photos are uploaded by users. Whether the questionnaire treats user-to-user communication specially was not confirmed on the page: `UNVERIFIED`. Do not pre-announce the resulting rating.
- **Target audience:** adults. See the open child-policy question in ANDROID_RELEASE section 5 point 8.
- **Contact details on the listing:** support email `[support email]`, website `[https://HOST]`, privacy policy `[https://HOST/privacy]`.
- **Data safety:** [data-safety.md](data-safety.md). **Permissions:** [permissions.md](permissions.md).

## Review checklist

1. No claim without a source in the repository or a screenshot of a shipped feature.
2. Disclaimer about the software-only Lock present in all three languages.
3. Bengali read by a named native speaker; Japanese by a named native speaker.
4. Privacy policy link works without signing in.
5. Counts of characters checked in Play Console.
