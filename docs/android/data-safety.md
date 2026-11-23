# Play Data safety form: draft answers (Wakatta? for Android)

> **DEFERRED.** The team decided not to upload to Google Play for now (`docs/ANDROID_RELEASE.md` decision R1), and the first APK is distributed directly in **mock mode** with no backend (R6). This file is kept for when the team publishes on Play with a backend-connected build. Nothing here is submitted anywhere.
>
> **If the mock-mode APK were ever uploaded to Play,** the honest answer to "Does your app collect or share any of the required user data types?" would be **No**, but only while the zero-egress check (ANDROID_RELEASE WB-26) passes for that exact build and the build contains no SDK that sends data (Play counts data that leaves the device as "collected", and SDK data counts too; checked 2026-11-23 on https://support.google.com/googleplay/android-developer/answer/10787469). A privacy policy link would still be required (same page). The table below is for the **backend-connected** build and every row marked as server-related applies only when a backend is hosted.

**Status** DRAFT, written 2026-11-23. Not legal advice. The form answers must match the real app build, every SDK in it and the published privacy policy ([privacy-policy.draft.md](privacy-policy.draft.md)). Re-do this table when a dependency or a data flow changes.

## What is verified and what is not

- Verified on 2026-11-23 from Google's help page (https://support.google.com/googleplay/android-developer/answer/10787469): the form is required for apps on closed, open and production tracks, and not for apps only on internal testing tracks; a privacy policy link is required; data collected by SDKs and libraries must be declared; "collect" means transmitting data off the device; "share" means passing collected data to a party other than the first party or its service providers; data processed ephemerally must still be answered but may not be shown on the listing; transfers to a service provider that processes data only on the developer's behalf generally do not count as sharing; the data-type categories include personal info, health and fitness, messages, photos and videos, audio, app activity, app info and performance, and device or other IDs.
- Account deletion questions (https://support.google.com/googleplay/android-developer/answer/13327111, checked 2026-11-23): the form asks for a web link where users can request deletion; an in-app path is also required; deactivation does not count as deletion (https://support.google.com/googleplay/android-developer/answer/10144311, checked 2026-11-23).
- Firebase Cloud Messaging data (https://firebase.google.com/docs/android/play-data-disclosure, checked 2026-11-23): FCM collects app version and a Firebase user agent, and depends on Firebase Installations, which creates a per-installation identifier. That page does not tell you which Play data type to choose, so the team chooses; this draft chooses "Device or other IDs".
- **UNVERIFIED:** the exact current wording of every field, option name and sub-question in the Play Console form. The structure below follows the categories listed on the help page and the form as commonly shown; open the live form (Policy > App content > Data safety) and correct names before submitting.

## Assumptions behind the answers

1. The first release has no analytics, advertising or crash SDK (ANDROID_RELEASE WB-18). If Crashlytics or Analytics is added, add "App info and performance" rows and Firebase Installations ID.
2. AI providers receive audio and text. On the **paid** tier under a service-provider arrangement, the legal reviewer may conclude this is processing on the developer's behalf and not "sharing". On the **free** tier, `contributors/galib/AI_PLAN.md` section 5 says inputs are used to improve Google's products, so the conservative answer is **shared**. Column "Shared (free AI tier)" shows that case. The reviewer decides (ANDROID_RELEASE D7, D8).
3. Audio is sent to the server and on to AI providers; the backend does not store it (BACKEND_ARCHITECTURE section 11). It is still **collected** in Play's sense because it leaves the device.
4. Data is encrypted in transit (HTTPS only; cleartext off, WB-9).
5. The account-deletion path and web page exist before submission (backend blocker B7). Until then the "users can request deletion" answer would be false and must not be given.

## Top-level questions

| Form question | Draft answer | Justification |
| :--- | :--- | :--- |
| Does the app collect or share any of the required user data types? | Yes | Table below. |
| Is all of the user data collected by the app encrypted in transit? | Yes, after WB-9 passes | HTTPS to `[HOST]` only; network security config disallows cleartext. |
| Do you provide a way for users to request that their data is deleted? | Yes, **only when B7 is live** | In-app route and web link `[deletion request URL]`; deactivation alone does not count (policy S5/S6 above). |
| Privacy policy URL | `[https://HOST/privacy]` | Public, not a PDF (policy page above). |
| Follows Play Families policy? | Not applicable; app is not directed at children | `UNVERIFIED` against the Families declaration; legal reviewer. |

## Data types (one row per type the form lists that applies)

"Required" means the app does not work without it; "optional" means the user can use the app without it. Purposes use Play's names: App functionality, Analytics, Developer communications, Advertising or marketing, Fraud prevention security and compliance, Personalization, Account management (names UNVERIFIED against the live form).

| Category > type | Collected | Shared (paid AI tier) | Shared (free AI tier) | Ephemeral | Required | Purposes | Where it is collected |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Personal info > Name | Yes | No | No | No | Required | App functionality, Account management | Sign-in roster (display name, chosen from a list the Foreman made); profile |
| Personal info > User IDs | Yes | No | No | No | Required | App functionality, Account management | Account id and session token after sign-in |
| Personal info > Other info (language, workplace or district, role) | Yes | No | No | No | Required | App functionality, Personalization | Sign-in, onboarding language choice |
| Personal info > Other info (PIN) | Yes (sent at sign-in) | No | No | No | Required | Account management, Fraud prevention security and compliance | PIN keypad; backend stores only a hash, the phone stores nothing |
| Health and fitness | Review: probably "No" | n/a | n/a | n/a | n/a | n/a | Talk Thread entries may contain a third party's words about pain or appetite, and the backend derives a "tell staff" flag. This is free-text user content, not a health feature, but a reviewer must decide whether to declare it (also APPI 要配慮個人情報 point in ANDROID_RELEASE section 5). Do not answer "No" until the reviewer has looked. |
| Messages > Other in-app messages | Yes | Yes (translation) / No (if service provider) | Yes | No | Required for chat | App functionality | Chat screens, Ask screen, Contact Threads |
| Photos and videos > Photos | Yes | No | No | No | Optional | App functionality | Near-miss Report, flyer photo (camera or Photo Picker) |
| Audio files > Voice or sound recordings | Yes | Yes (AI) / No (if service provider) | Yes | Yes for audio not stored by the backend, if the reviewer accepts "ephemeral" | Optional (typing is possible) | App functionality | Record button in chat, Ask, Notice, Briefing, Near-miss, Talk Thread |
| App activity > Other user-generated content | Yes | AI providers receive transcripts and translations | Yes | No | Required | App functionality | Check answers, Asks, Notices, Talk Thread entries, Meet-ups, Tasks |
| App activity > App interactions | Review | No | No | No | n/a | n/a | Check results and Sign-offs are stored as product records; whether they count as "app interactions" for Play is a reviewer call. They are stored for the Foreman and Worker, not for analytics. |
| Contacts (address book) | No | n/a | n/a | n/a | n/a | n/a | The app never reads the phone's contacts. "Contacts" in the app are people connected by My QR, stored on the server as app data. |
| Location | No | n/a | n/a | n/a | n/a | n/a | No location permission. Photo location data is removed server-side (BACKEND_ARCHITECTURE section 11). |
| App info and performance > Crash logs, Diagnostics | No (first release) | n/a | n/a | n/a | n/a | n/a | No SDK. Play's own vitals collection is Google's, not declared by the developer here (UNVERIFIED; confirm in Play help). Becomes "Yes" if Crashlytics is added (WB-18). |
| Device or other IDs | Yes | No | No | No | Required for push | App functionality | FCM registration token (`PUT /devices/me`, backend B8) and the Firebase installation ID (FCM SDK). Push payloads for Contact Thread messages carry the sender's display name and no text; Talk Threads send no push (ANDROID_RELEASE R7). The sender name is also "Personal info > Name" passing through Google's push service. |
| Financial info, Calendar, Files and docs, Web browsing, SMS | No | n/a | n/a | n/a | n/a | n/a | Not used. The .ics "add to calendar" export hands a file to the user and does not read the calendar (confirm in the Android build). |

## Security practices block

| Question | Draft answer | Justification |
| :--- | :--- | :--- |
| Data encrypted in transit | Yes | See top-level. |
| Users can request data deletion | Yes when B7 is live | See top-level. |
| Committed to Play Families Policy | No | Not a children's app. |
| Independent security review | No | None done. Do not tick without a real review (invented claims are forbidden). |

## Consistency checklist before each submission

1. Every SDK in the release dependency list is covered above (list it in the PR).
2. The privacy policy names the same data, purposes, processors and retention.
3. The in-app first-use notices (Talk Thread notice, permission rationales in [permissions.md](permissions.md)) agree with the form.
4. The listing does not promise anything the form contradicts.
5. Date and person who submitted are recorded in the gate issue.
