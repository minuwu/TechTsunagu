# Privacy policy: Wakatta? for Android (DRAFT)

> **DRAFT. Not legal advice.** This text has not been reviewed by a lawyer or by a native speaker. Do not publish it until the reviewer named in [ANDROID_RELEASE.md](../ANDROID_RELEASE.md) decision D7 has approved it. Items in [square brackets] are placeholders. Items marked `[LEGAL]` need a legal decision. The Japanese version is the one a Japanese court would read; the English version must say the same thing.
>
> Facts in this draft come from `docs/BACKEND_ARCHITECTURE.md` (sections 10, 11, 12), `docs/adr/0010`, `docs/adr/0014` and `contributors/galib/AI_PLAN.md`. Where the backend does not yet do what the text says (for example account deletion and the retention job), the text is marked `[BACKEND NOT YET BUILT]` and must not be published until it is true.
>
> Last updated: [date]. Version: [1].
>
> **Two parts.** **Part A** is for the first APK, which runs in **mock mode** (no backend baked in; `docs/ANDROID_RELEASE.md` decision R6). It says the app collects no data off the device. Publish only Part A with that APK, and only while the zero-egress check (ANDROID_RELEASE WB-26) passes for that exact APK. **Part B** (further down) describes the real-backend build and is **for when a backend is hosted**: every statement in Part B about servers, AI providers, push or retention is marked as such and must not be published before then.
> Google Play is deferred; the Play-specific wording of the earlier draft was removed.

---

# Part A: mock-mode APK (no backend)

## 日本語版 (Part A)

### A-1. 事業者の情報

- 事業者名: [事業者名(個人名または法人名)]
- 連絡先: [support email]
- 個人情報の取扱いに関する責任者: [氏名または役職]

### A-2. このバージョンについて

このバージョンの「Wakatta?(わかった?)」は**デモ版(モックモード)**です。サーバーにつながっていません。表示される機械、お知らせ、メッセージ、人の名前はすべて見本(架空のデータ)です。

### A-3. 取得する情報

**このアプリは、お使いの端末の外へ情報を送りません。** 通信機能を使わず、広告や解析、不具合報告のための外部サービスも組み込んでいません。カメラ、マイク、写真の選択は、画面の機能(QRコードの読み取り、録音、写真の添付)のために端末の中で使います。ここで扱う音声・写真・入力した文字は、このバージョンでは端末の外へ送られず、サーバーにも保存されません。`[Confirm each sentence against the exact APK: ANDROID_RELEASE WB-26. Also state what the mock does with a recording: for example "the sample app does not transcribe it".]`

端末の中に残るもの: 設定(言語など)と、入力した内容。アプリを削除すると、これらは端末から消えます。端末のバックアップ機能によって端末の外へ複製されることがないよう、アプリはバックアップの対象から外しています。`[Confirm: ANDROID_RELEASE WB-9(c).]`

### A-4. 外部の事業者への提供

ありません。このバージョンは、音声や文章を Google などの外部サービスへ送りません。

### A-5. お問い合わせと削除

サーバーにはあなたの情報がないため、削除の請求先はありません。端末のデータは、アプリを削除すると消えます。ご質問は [support email] へ。

### A-6. 今後の変更

サーバーにつながるバージョンを配布する前に、この方針を改訂し、何を送るかをアプリ内でお知らせします。そのバージョンのための方針は Part B の下書きです。

## English version (Part A)

### A-1. Who we are

- Operator: [operator name (individual or company)]
- Contact: [support email]
- Person responsible for personal information: [name or role]

### A-2. About this version

This version of "Wakatta?" is a **demo (mock mode)**. It is not connected to any server. Every machine, notice, message and person's name you see is sample (fictional) data.

### A-3. What we collect

**The app does not send any information off your device.** It does not use the network, and it contains no advertising, analytics or crash-reporting service. The camera, microphone and photo picker are used on the device only for the on-screen features (scanning a QR code, recording, attaching a photo). In this version, audio, photos and text you enter are not sent off the device and are not stored on any server. `[Confirm each sentence against the exact APK: ANDROID_RELEASE WB-26. Also state what the mock does with a recording.]`

What stays on the device: your settings (such as language) and what you enter. Uninstalling the app removes them. The app is excluded from Android's backup so that app data is not copied off the phone. `[Confirm: ANDROID_RELEASE WB-9(c).]`

### A-4. Sharing with other companies

None. This version does not send audio or text to Google or any other service.

### A-5. Contact and deletion

There is no data about you on any server, so there is nothing to ask us to delete. Your data on the phone is removed when you uninstall the app. Questions: [support email].

### A-6. Changes

Before we distribute a version that connects to a server, we will revise this policy and tell you in the app what is sent. The policy for that version is the draft in Part B.

---

# Part B: backend-connected build (FOR WHEN A BACKEND IS HOSTED, not for the mock-mode APK)

> Everything below describes a build that talks to a hosted backend. Do not publish it with the mock-mode APK.

## 日本語版 (Part B)

### 1. 事業者の情報

- 事業者名: [事業者名(個人名または法人名)]
- 住所: [所在地]
- 連絡先(個人情報に関するお問い合わせ窓口): [support email]
- 個人情報の取扱いに関する責任者: [氏名または役職]

### 2. このアプリについて

「Wakatta?(わかった?)」は、工場などで働く外国人の従業員(以下「作業者」)と、その職場の班長(以下「班長」)、地域の自治会や市の担当者などが使うアプリです。班長の説明を日本語・ベンガル語・英語などで読み、確認問題(チェック)に答えることができます。地域のお知らせ、質問、チャット、QRコードによる連絡先の交換、そして介護の現場などで聞き取った話を記録する「Talk Thread」も含まれます。

### 3. 取得する情報と利用目的

| 情報 | 内容 | 利用目的 |
| :--- | :--- | :--- |
| アカウント情報 | 表示名、役割(班長・作業者・自治会担当)、言語、所属する職場または地区、PIN(暗証番号) | ログイン、本人確認、表示言語の切り替え |
| 理解確認の記録 | チェックの回答、結果、サインオフ(機械ごとの確認記録)、分からなかった用語 | 作業者の理解の確認、班長への共有、安全に関する記録の保管 |
| メッセージ | チャットや質問(Ask)の文章、翻訳結果 | 班長と作業者の連絡、翻訳 |
| 音声 | 話した内容(チャット、質問、お知らせの作成、ヒヤリハット報告、Talk Thread) | 文字起こしと翻訳のためにのみ使います。**当社のサーバーは音声そのものを保存しません**が、文字起こしのために下記の外部事業者へ送信します。 |
| 音声の文字起こし | 音声から作られた文章 | メッセージとして保存し、翻訳や確認問題の作成に使います。 |
| 写真 | ヒヤリハット報告の写真、お知らせのチラシの写真 | 報告・お知らせの作成。サーバーで位置情報などのメタデータを取り除き、縮小して保存します。 |
| Talk Thread の内容 | 本人以外の方(例:高齢の方)が話した内容の文字起こし、その意味、本人の理解、返答 | 本人が会話を振り返るため。**班長や雇用主は読めません**(後述)。 |
| 連絡先(コンタクト)の情報 | 自分のQRコードに含まれる取り消し可能な識別トークン、コンタクトとのチャット、ブロックの設定 | コンタクト同士の連絡。QRコードには利用者のIDやPINは含まれません。 |
| 通知用の情報 | 端末の通知トークン(FCM)、アプリのバージョン、端末の言語 | プッシュ通知の送信 `[BACKEND NOT YET BUILT]`。コンタクトからメッセージが届いたときの通知は「[相手の名前]から新しいメッセージ」という固定の文だけで、メッセージの本文は含みません。Talk Thread は通知を送りません。相手の名前は Google の通知サービスを通ります。 |
| 技術的な情報 | アクセス日時、リクエストの種類、エラー | 不具合の調査。ログに氏名・文章・回答・音声の内容は記録しません。 |

アプリは広告のための情報を取得せず、行動を分析する外部サービス(解析SDK)を組み込んでいません。`[Confirm before publishing: true only if WB-18 stays "vitals only".]`

### 4. 外部の事業者への提供(委託・海外への送信)

文字起こし・翻訳・文章の作成のために、次の事業者のサービスへ音声または文章を送信します。通知の配信にも事業者を使います。

| 事業者 | 目的 | 送る情報 | 所在国 |
| :--- | :--- | :--- | :--- |
| Google(Gemini API) | 文字起こし、翻訳、確認問題の作成 | 音声、文章 | [国・地域を確認] |
| Groq | 音声の文字起こし(Google のサービスが使えない場合の代替。使用する場合のみ) | 音声 | 米国(`contributors/galib/AI_PLAN.md` の記載) |
| Google(Firebase Cloud Messaging) | プッシュ通知の配信 | 通知トークン、通知の見出し | [国・地域を確認] |
| [クラウド事業者: Microsoft Azure] | サーバーとデータベースの運用 | 上記のすべての保存データ | [リージョンを確認] |

`[LEGAL]` 個人情報保護法第27条第5項第1号(委託)と第28条(外国にある第三者への提供)のどちらに当たるか、また同意の取得方法(第28条第2項の情報提供を含む)は、契約内容と設定によります。弁護士の確認が必要です。
`[LEGAL]` 無料の Google AI API の入力は Google の製品改善に使われることがあります(`contributors/galib/AI_PLAN.md` 第5節)。実際の利用者のデータを送る前に、有料プランへ切り替えるか、実データを送らない運用にします。確定するまでこの段落を公開しないでください。

### 5. 要配慮個人情報について

Talk Thread には、話した方の体調(痛みや食欲など)に関する言葉が含まれることがあります。`[LEGAL]` これが要配慮個人情報に当たるか、当たる場合に必要な同意や取扱いは、弁護士の判断を受けて記載します。介護分野の情報の取扱いについては、厚生労働省の指針が外部クラウドの利用を慎重に扱うことが ADR 0014 に記録されています。そのため、Talk Thread を最初に使う前に一度だけ、次のことを画面で説明します: (1) 内容は端末の外のサーバーと外部事業者に送られること、(2) 班長や雇用主には見えないこと、(3) メッセージやスレッド全体を削除できること。

### 6. 班長・雇用主と見える範囲

- 班長は、職場のチャット、チェックの結果、サインオフ、確認問題の分からなかった用語を見ることができます。
- Talk Thread とコンタクトとのチャットは、班長にも雇用主にも見えません。これらの内容がサポート記録や学習履歴に入ることはありません。
- 自治会・市の担当者は、お知らせを作成できますが、職場のデータは見えません。

### 7. 保存期間

- 作業者の特定技能の雇用契約が終わってから少なくとも1年間は、支援に関する記録(転送した質問、先輩の返信、ミートアップ、お知らせの受領記録)を削除しません。これは法令(特定技能基準省令)に基づく保管のためです(`docs/BACKEND_ARCHITECTURE.md` 第12節)。`[LEGAL: 条文と期間を確認]`
- サインオフ(安全に関する確認記録)は、アカウントを無効にしても保管します。
- 音声は保存しません。
- 写真は縮小し、位置情報を取り除いて保存します。
- 利用者のアカウントを無効にした場合、Talk Thread は30日後に削除します(予定値。`handoff-backend-additions.md` の既定)。`[BACKEND NOT YET BUILT]`
- 上記の期間が過ぎた後の自動削除は、現在まだ実装されていません。`[BACKEND NOT YET BUILT. Remove this sentence only when the job exists.]`

### 8. 削除の請求とアカウント削除

アプリの[設定 > アカウントの削除]から、またはウェブページ [削除請求のURL] から、アカウントとデータの削除を請求できます `[BACKEND NOT YET BUILT]`。削除しても、法令に基づき保管が必要な記録(第7節)は、その期間が終わるまで残る場合があります `[LEGAL: 残す項目を具体的に列挙する]`。個人情報保護法に基づくご本人の権利(利用停止・消去の請求など)については [LEGAL: 根拠条文と手続を確認して記載]。

### 9. 安全管理

通信は暗号化(HTTPS)します。PINは暗号学的ハッシュで保管し、5回連続で間違えると15分間ロックします。ログインの日時は記録しますが、PINは記録しません。あるデータが他の職場から見えないよう、データベースと API の両方で分離しています。

### 10. 子どもについて

このアプリは、働く成人と地域の担当者のためのものです。13歳未満の方を対象にしていません。`[LEGAL: Google Play の対象年齢の申告と合わせて確認]`

### 11. 変更

この方針を変更する場合は、アプリ内と本ページでお知らせします。

### 12. お問い合わせ

[support email] / [郵送先]

---

## English version (Part B)

### 1. Who we are

- Operator: [operator name (individual or company)]
- Address: [address]
- Contact for privacy questions: [support email]
- Person responsible for handling personal information: [name or role]

### 2. About the app

"Wakatta?" is used by foreign employees who work in factories and similar workplaces ("Workers"), their Foremen (班長), and local officials such as a neighbourhood association (自治会) or a city office. Workers read a Foreman's instructions in Japanese, Bengali, English and other languages, and answer check questions ("Checks"). The app also carries local notices, questions, chat, a QR-code way to connect with other people, and "Talk Threads", where a user records what someone else said to them (for example an elderly person in a care setting).

### 3. What we collect, and why

| Data | What it is | Why |
| :--- | :--- | :--- |
| Account | Display name, role (Foreman, Worker, community poster), language, workplace or district, PIN | Sign-in, identity, showing the app in your language |
| Understanding records | Check answers and results, Sign-offs (a per-Machine record that a Worker passed the Check), terms the Worker did not understand | Confirming understanding, sharing it with the Foreman, keeping the safety record |
| Messages | Chat and Ask text, translations | Communication between Foreman and Worker, translation |
| Voice | What you say when you record a chat message, an Ask, a notice, a near-miss report or a Talk Thread entry | Used only to transcribe and translate. **Our server does not store the audio**, but we send it to the outside providers listed below for transcription. |
| Transcripts | Text made from your audio | Stored as messages; used for translation and for making check questions |
| Photos | Near-miss report photos, flyer photos | Making reports and notices. The server removes location and other metadata, shrinks the image and stores it. |
| Talk Thread content | A transcript of what a third party said, its meaning, your own understanding and your reply | So you can review a conversation. **Your Foreman and employer cannot read it** (section 6). |
| Contacts | A revocable token inside your own QR code, chats with Contacts, blocks | Letting people you choose message you. Your QR code never contains your user id or PIN. |
| Notification data | Device notification token (FCM), app version, device language | Sending push notifications `[BACKEND NOT YET BUILT]`. A push for a Contact message is a fixed line, "New message from [name]", with no message text. Talk Threads never send a push. The sender's name passes through Google's push service. |
| Technical data | Time and type of request, errors | Fixing problems. Logs do not contain names, message text, answers or audio. |

The app does not collect data for advertising and contains no analytics or crash-reporting SDK; if something goes wrong you can choose "Share diagnostic report", which shows you the log before you send it to [support email]. `[Confirm before publishing: true only if ANDROID_RELEASE WB-18 stays "Android vitals only".]` We distribute the app directly, not through Google Play.

### 4. Who else receives data (processors and overseas transfer)

| Provider | Purpose | What we send | Country |
| :--- | :--- | :--- | :--- |
| Google (Gemini API) | Transcription, translation, drafting check questions | Audio, text | [confirm] |
| Groq | Speech transcription as a fallback (only if enabled) | Audio | United States (per `contributors/galib/AI_PLAN.md`) |
| Google (Firebase Cloud Messaging) | Delivering push notifications | Notification token, notification title | [confirm] |
| [Cloud provider: Microsoft Azure] | Running the server and database | All stored data | [region to confirm] |

`[LEGAL]` Whether each transfer is "entrustment" (APPI Art. 27(5)(i)) or "provision to a third party in a foreign country" requiring consent (APPI Art. 28(1) and the information duty in Art. 28(2)) depends on the contracts and settings. A lawyer must decide and the consent flow must follow.
`[LEGAL]` The repository's AI plan says that inputs on Google's free AI tier may be used to improve Google's products. Before any real user's data is sent, either move to the paid tier or send no real data. Do not publish this section until that is decided.

### 5. Sensitive information

Talk Threads can contain words about a person's health, such as pain or appetite. `[LEGAL]` Whether this is "special care-required personal information" (要配慮個人情報) under the APPI, and what consent that requires, must be decided by a lawyer before publication. ADR 0014 records that the Japanese health ministry's information-security guide for care providers is cautious about external cloud AI. For that reason the app shows a one-time notice before first use of Talk Threads that says: (1) the content is sent to our server and outside providers, (2) your Foreman and employer cannot see it, and (3) you can delete a message or a whole thread.

### 6. What your Foreman and employer can see

- A Foreman can see workplace chat, Check results, Sign-offs, and the terms a Worker did not understand.
- Talk Threads and chats with Contacts are not visible to the Foreman or the employer. Nothing from them enters the Support Log or the Learning History.
- A community poster can publish notices but cannot see workplace data.

### 7. How long we keep data

- Support records (forwarded Asks, Senpai replies, Meet-ups, Notice receipts) are kept for at least one year after the end of the Worker's 特定技能 employment contract, because the law requires it (`docs/BACKEND_ARCHITECTURE.md` section 12). `[LEGAL: confirm the provision and period]`
- Sign-offs (the safety record) are kept even when an account is deactivated.
- Audio is not stored.
- Photos are shrunk and stripped of location data before storage.
- If an account is deactivated, its Talk Threads are deleted after 30 days (planned default from `handoff-backend-additions.md`). `[BACKEND NOT YET BUILT]`
- Automatic deletion after the periods above is not built yet. `[BACKEND NOT YET BUILT. Remove only when the job exists.]`

### 8. Deleting your account and data

You can request deletion of your account and data in the app at [Settings > Delete account], or on the web at [deletion request URL] `[BACKEND NOT YET BUILT]`. Records that the law requires us to keep (section 7) may remain until their retention period ends. `[LEGAL: list exactly which categories remain.]` Your rights under the APPI (for example requests to stop using or to delete data) are described here: `[LEGAL: confirm the provisions and the procedure; this draft does not state them.]`

### 9. Security

Traffic is encrypted (HTTPS). PINs are stored as cryptographic hashes, and five wrong PINs lock the account for 15 minutes. Sign-ins are logged but never the PIN. Each workplace's data is separated in both the database and the API.

### 10. Children

The app is for working adults and community staff. It is not directed at children under 13. `[LEGAL: confirm together with the target-audience declaration in Google Play.]`

### 11. Changes

If we change this policy we will tell you in the app and on this page.

### 12. Contact

[support email] / [postal address]
