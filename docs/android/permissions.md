# Android permissions: why, rationale text, and what happens when denied

**Status** DRAFT, written 2026-11-23. The Bengali (bn) text has **not** been reviewed by a native speaker and must be before release (ANDROID_RELEASE WB-15). The Japanese and English text should also be read by a native reader on a real phone.

**Distribution and mock mode** (`docs/ANDROID_RELEASE.md` R1, R6, R7): the app is distributed as a direct APK, not through Google Play, and the first APK runs in **mock mode** with no backend. In mock mode the manifest should carry **no** `INTERNET` and **no** `POST_NOTIFICATIONS` permission, and CAMERA and RECORD_AUDIO are used on the device only (a recording is not transcribed by a server and is not sent anywhere; the rationale below says so only for the backend build, see the mock-mode text under each permission). Notifications and the network permissions come back with a backend-connected build. The Play statements below are kept because they are still the least-collecting practice; Play rules themselves are deferred.

Rules this file follows:

- Ask in context, never at launch: show the app's own explanation first, then the system dialog. Play requires an in-app disclosure before collecting data when the use may not be expected, and requires a clear affirmative action; pressing back is not consent (https://support.google.com/googleplay/android-developer/answer/10144311, checked 2026-11-23).
- For notifications on Android 13 and higher, the permission is `POST_NOTIFICATIONS`, notifications are off by default for new installs, and an app that targets 13 or higher controls when the dialog appears (https://developer.android.com/develop/ui/views/notifications/notification-permission, checked 2026-11-23).
- Photos use the system Photo Picker, so the app does **not** declare `READ_MEDIA_IMAGES`. Play allows that permission only when system pickers are insufficient and then requires a declaration (https://support.google.com/googleplay/android-developer/answer/16558241, checked 2026-11-23).
- Every denied state keeps the app usable. A denied permission is never a dead end and never shown as an error in red (red means Lock or Confusion in this product, PRODUCT.md).

## Summary

| Permission | Type | Used for | Asked when |
| :--- | :--- | :--- | :--- |
| `CAMERA` | runtime | Scan a Machine QR sticker or a My QR; take a Near-miss or flyer photo | The first time the user taps Scan or Take photo |
| `RECORD_AUDIO` | runtime | Record voice for chat, Asks, Notices, Briefings, Near-miss Reports and Talk Threads | The first time the user taps the microphone |
| `POST_NOTIFICATIONS` | runtime (Android 13+) | New messages, Check due, Nudge, new Notice, Event reminder, other types in MOBILE_PLATFORM_CONTRACT.md | After sign-in, from a button the user taps (for example "Turn on reminders"), not at launch |
| `INTERNET`, `ACCESS_NETWORK_STATE` | normal (no prompt) | API calls; deciding when to queue offline | Install time |
| Photos | none (Photo Picker) | Choose an existing photo | Picker opens; no permission |

Not requested: location, contacts, calendar, SMS, storage, `READ_MEDIA_*`, exact alarms. If a later feature needs one, update this file, the Data safety form and the privacy policy first.

## CAMERA

**Why.** Machines carry a QR sticker; a Worker scans it to open that Machine's Note and Check. The same screen scans a My QR to add a Contact. The camera also takes a Near-miss or flyer photo.

**In-app rationale (shown before the system dialog), backend build**

| ja | en | bn (needs native review) |
| :--- | :--- | :--- |
| 機械のQRコードを読み取るために、カメラを使います。写真は、あなたが送信したときだけ送られます。 | Wakatta? uses the camera to scan the QR code on a machine. A photo is sent only when you choose to send it. | মেশিনের QR কোড স্ক্যান করার জন্য ক্যামেরা লাগবে। আপনি পাঠালে তবেই ছবি পাঠানো হবে। |

**Mock-mode text (first APK, nothing leaves the device)**

| ja | en | bn (needs native review) |
| :--- | :--- | :--- |
| 機械のQRコードを読み取るために、カメラを使います。このデモ版では、写真は端末の外へ送られません。 | Wakatta? uses the camera to scan the QR code on a machine. In this demo version nothing is sent off your phone. | মেশিনের QR কোড স্ক্যান করার জন্য ক্যামেরা লাগবে। এই ডেমো সংস্করণে কোনো কিছু আপনার ফোনের বাইরে পাঠানো হয় না। |

**If denied**

- The scan screen shows the explanation again and a button "Open settings" (ja: 設定を開く, en: Open settings, bn: সেটিংস খুলুন). If the user has chosen "don't ask again", the system dialog will not appear, so this button is the only way back.
- Proposal for the Android owner: a typed Machine code as a fallback so a Worker without camera permission can still reach a Machine. Not in any current contract: mark as a decision in ANDROID_ARCHITECTURE.md before building.
- Photo reports can still use the Photo Picker (no camera needed).

## RECORD_AUDIO

**Why.** Foremen record spoken know-how and messages; Workers record Asks, replies and Near-miss reports in their own language; Talk Threads record what another person said. The audio is turned into text and translated.

**Mock-mode text (first APK, nothing leaves the device)**

| ja | en | bn (needs native review) |
| :--- | :--- | :--- |
| このデモ版では、録音は端末の外へ送られず、保存もされません。`[Say what the demo does with a recording, for example "it shows a sample result".]` | In this demo version a recording is not sent off your phone and is not saved. `[Say what the demo does with a recording.]` | এই ডেমো সংস্করণে রেকর্ডিং আপনার ফোনের বাইরে পাঠানো হয় না এবং সংরক্ষণও করা হয় না। |

**In-app rationale, backend build**

| ja | en | bn (needs native review) |
| :--- | :--- | :--- |
| 話した内容を文字にして翻訳するために、マイクを使います。録音はこのアプリのサーバーには保存されませんが、文字にするために外部のサービス(Google など)へ送られます。 | The microphone turns what you say into text and translates it. The server does not keep the recording, but it is sent to outside services (such as Google) to make the text. | আপনার কথা লেখায় রূপান্তর ও অনুবাদ করার জন্য মাইক্রোফোন লাগবে। রেকর্ডিং আমাদের সার্ভারে রাখা হয় না, তবে লেখা তৈরির জন্য বাইরের সেবায় (যেমন Google) পাঠানো হয়। |

For Talk Threads the one-time notice (privacy policy section 5) is shown before this screen: the content goes to the server and outside providers, the employer cannot see it, and the user can delete a message or thread. See [privacy-policy.draft.md](privacy-policy.draft.md).

**If denied**

- The microphone button changes to a keyboard: the user can type the message. Typing uses the same text path as voice (XREQ-005 sends `text` or `audio`).
- "Dictate instead" with Android `SpeechRecognizer` also needs the microphone, which is general Android behaviour not checked here: `UNVERIFIED`. If microphone permission is denied, hide that button.
- Keep recording in the foreground only. The sensitive-permissions page does not mention a microphone declaration (checked 2026-11-23); foreground-service types were not checked.

## POST_NOTIFICATIONS (Android 13 and higher)

**Backend build only; not in the mock-mode APK.**

**Why.** Push tells a Worker that a message needs an answer, a Check is due, a Notice was published or an Event is near. Push is best-effort: the app polls and shows everything in-app, so a missed push is never the only way to learn something (MOBILE_PLATFORM_CONTRACT.md). Rule R7 (ANDROID_RELEASE): a Contact Thread message sends only a generic line, "New message from [name]", with **no message text**; **Talk Threads never send a push**; no Check answers or care content in any payload. The sender's name does pass through Google's push service.

**In-app rationale**

| ja | en | bn (needs native review) |
| :--- | :--- | :--- |
| 新しいメッセージ、確認の期限、地域のお知らせを通知でお知らせします。コンタクトからのメッセージは「〇〇さんから新しいメッセージ」とだけ表示され、本文は表示されません。チェックの答えは表示されません。Talk Thread は通知しません。 | We can notify you about new messages, check deadlines and local notices. A message from a Contact shows only "New message from [name]", never the text. Check answers are never shown. Talk Threads never send notifications. | নতুন বার্তা, চেকের সময়সীমা ও এলাকার নোটিশ আমরা নোটিফিকেশনে জানাতে পারি। কন্ট্যাক্টের বার্তায় শুধু "[নাম]-এর কাছ থেকে নতুন বার্তা" দেখানো হয়, বার্তার লেখা নয়। চেকের উত্তর কখনো দেখানো হয় না। Talk Thread কখনো নোটিফিকেশন পাঠায় না। |

**If denied**

- No push arrives. The app still shows unread counts, open Checks and deadlines on its home screen, and refreshes by polling when open.
- Show a quiet banner (not red) with "Turn on notifications" that opens the app's notification settings, at most once per session.
- Do not ask again at launch. If the user swiped the dialog away, the permission state does not change (checked in the page above), so the app may offer the button again later.
- A Check that decides Lock or Ready never depends on a notification (BACKEND_ARCHITECTURE section 7.3; Check deadlines ignore mute).

## Photo Picker (no permission)

Used for "attach a photo". The app receives only the photo the user picks. Nothing to declare in Play for `READ_MEDIA_IMAGES` as long as the app never requests it.

## Checklist for the release reviewer

1. The merged manifest lists only the permissions in the summary table (check the release bundle, including permissions added by libraries).
2. Each runtime request is preceded by the matching rationale above, in the user's language, and is triggered by a user tap.
3. Every denied path is tested with TalkBack on and at the largest font size.
4. The rationale text matches the Data safety form and the privacy policy.
5. A native reader of each language approved the text on a device.
