# How to install and update Wakatta? (direct APK): DRAFT

**Status** DRAFT, written 2026-11-23. For Workers and Foremen, and for whoever helps them. The Japanese, English and Bengali text must be reviewed by a native speaker and tested on each phone model before it is given to anyone (`docs/ANDROID_RELEASE.md` WB-15, WB-25). The Bengali has **not** been reviewed.

**Open points before use**

- The install path below (Settings > Apps > Special app access > Install unknown apps) comes from phone-maker and news pages, not from a Google page, and menu names differ by phone maker: `UNVERIFIED` (ANDROID_RELEASE section 6.1). Test on each phone model and replace with real screenshots.
- The download link `[DOWNLOAD LINK]` is decision D5 in ANDROID_RELEASE. The file is `wakatta-<version>.apk` in `android/release/` together with `wakatta-<version>.apk.sha256`.
- Support email `[support email]` is decision D2.
- The first APK is a **demo (mock mode)**: it has no server, and everything shown is sample data. The text says so.
- Certificate fingerprint (SHA-256) of the release key: `[paste from the release log after the key is generated]`. Publish it here, on the privacy page and on a card given by the Foreman.

---

## 日本語

### Wakatta?(デモ版)のインストール

このデモ版は、サーバーにつながっていません。表示されるデータはすべて見本です。入力した内容は、あなたの端末の外へ送られません。

1. 渡されたリンク [DOWNLOAD LINK] を開くか、受け取ったファイル `wakatta-[バージョン].apk` を探します。
2. ファイルを開こうとすると、「このアプリからのインストールを許可しますか」と表示されることがあります。「設定」を押し、「この提供元を許可」をオンにします。表示される場所は端末によって違います。たとえば「設定 > アプリ > 特別なアプリアクセス > 不明なアプリのインストール」です。見つからないときは、班長か [support email] に聞いてください。
3. もう一度ファイルを開き、「インストール」を押します。
4. インストールが終わったら、手順2の設定をオフに戻します(安全のため)。
5. 「Wakatta?」を開きます。最初の画面の説明を読んでください。

### 新しいバージョンに更新する

1. 班長または担当者から新しいファイルのリンクをもらいます。
2. 同じ手順で開いて「インストール」を押します。今のアプリの上に重ねて入れるので、端末の中のデータは残ります。
3. 「更新できません」と出たときは、アプリを削除せずに班長に聞いてください。削除すると、端末の中のデータが消えます。

### うまくいかないとき

- 「インストールがブロックされました」と出る: 手順2の許可をもう一度確認します。
- 「このアプリは使えません」「登録されていない開発者」などと出る: 端末の制限の可能性があります。班長か [support email] に連絡してください。自分で設定を変えないでください。
- 安全のため、知らない人から送られたファイルをインストールしないでください。

## English

### Installing Wakatta? (demo version)

This demo version is not connected to a server. All data shown is sample data. What you enter is not sent off your phone.

1. Open the link you were given, [DOWNLOAD LINK], or find the file you received: `wakatta-[version].apk`.
2. When you open the file, the phone may ask whether to allow installs from this source. Tap "Settings" and switch on "Allow from this source". Where this is differs by phone, for example "Settings > Apps > Special app access > Install unknown apps". If you cannot find it, ask your Foreman or write to [support email].
3. Open the file again and tap "Install".
4. When it is installed, switch the setting from step 2 off again (for safety).
5. Open "Wakatta?" and read the first screen.

### Updating to a new version

1. Get the link or file for the new version from your Foreman or the support person.
2. Open it the same way and tap "Install". It installs over the current app, so the data on your phone is kept.
3. If it says it cannot update, do not uninstall the app. Ask your Foreman first, because uninstalling removes the data on your phone.

### If it does not work

- "Install blocked": check the permission in step 2 again.
- "App not available" or "unregistered developer" or similar: your phone may restrict this. Tell your Foreman or [support email]. Do not change security settings yourself.
- For safety, never install a file sent by someone you do not know.

## বাংলা (not reviewed by a native speaker)

### Wakatta? (ডেমো সংস্করণ) ইনস্টল করা

এই ডেমো সংস্করণ কোনো সার্ভারের সঙ্গে যুক্ত নয়। এখানে দেখানো সব তথ্য নমুনা। আপনি যা লেখেন তা আপনার ফোনের বাইরে পাঠানো হয় না।

1. আপনাকে দেওয়া লিংক [DOWNLOAD LINK] খুলুন, অথবা পাওয়া ফাইলটি `wakatta-[সংস্করণ].apk` খুঁজে নিন।
2. ফাইল খুললে ফোন জিজ্ঞেস করতে পারে এই উৎস থেকে ইনস্টল করার অনুমতি দেবেন কি না। "সেটিংস" চাপুন এবং "এই উৎস থেকে অনুমতি দিন" চালু করুন। ফোনভেদে এটি আলাদা জায়গায় থাকে, যেমন "সেটিংস > অ্যাপ > বিশেষ অ্যাপ অ্যাক্সেস > অজানা অ্যাপ ইনস্টল"। খুঁজে না পেলে আপনার ফোরম্যান বা [support email]-কে জিজ্ঞেস করুন।
3. ফাইলটি আবার খুলে "ইনস্টল" চাপুন।
4. ইনস্টল শেষ হলে ধাপ ২-এর সেটিং আবার বন্ধ করে দিন (নিরাপত্তার জন্য)।
5. "Wakatta?" খুলে প্রথম পর্দার লেখা পড়ুন।

### নতুন সংস্করণে আপডেট করা

1. ফোরম্যান বা সহায়তাকারীর কাছ থেকে নতুন সংস্করণের লিংক বা ফাইল নিন।
2. একইভাবে খুলে "ইনস্টল" চাপুন। এটি বর্তমান অ্যাপের ওপর বসে, তাই ফোনের তথ্য থেকে যায়।
3. "আপডেট করা যাচ্ছে না" দেখালে অ্যাপ মুছবেন না। আগে ফোরম্যানকে জিজ্ঞেস করুন, কারণ মুছলে ফোনের তথ্য চলে যায়।

### কাজ না করলে

- "ইনস্টল ব্লক করা হয়েছে": ধাপ ২-এর অনুমতি আবার দেখুন।
- "অ্যাপ পাওয়া যাচ্ছে না" বা "নিবন্ধিত নয় এমন ডেভেলপার" ইত্যাদি দেখালে আপনার ফোনে বিধিনিষেধ থাকতে পারে। ফোরম্যান বা [support email]-কে জানান। নিজে নিরাপত্তা সেটিং বদলাবেন না।
- নিরাপত্তার জন্য অচেনা কারও পাঠানো ফাইল কখনো ইনস্টল করবেন না।

---

## For the person who hands out the file (Foreman or team member)

Not for Workers. Do this before giving anyone the file.

1. Get the file and the checksum from `android/release/` on the main branch: `wakatta-<version>.apk` and `wakatta-<version>.apk.sha256`.
2. Run `sha256sum -c wakatta-<version>.apk.sha256` in that folder. It must print `OK`. Source of the command's use: standard Linux tool, not checked against a source here.
3. Check the signer with `apksigner verify --print-certs wakatta-<version>.apk`. The SHA-256 certificate fingerprint it prints must equal the fingerprint written in the open points above and in the release log. If it does not, **do not hand the file out** and tell the release owner (ANDROID_RELEASE section 6.4). `apksigner` options were not verified here: `UNVERIFIED`.
4. Hand the file over in person or send the link `[DOWNLOAD LINK]`. Help with the first install. Note the phone model and anything that went wrong; send it to the support mailbox so this guide can be fixed.
5. Android developer verification may make unregistered apps harder to install in the future. If a phone refuses the app with a message about developers or registration, do not try to bypass it; tell the release owner (ANDROID_RELEASE section 5).
