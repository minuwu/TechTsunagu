# :core:deeplink
Owner work package: WP-DL. Source of truth: `docs/MOBILE_PLATFORM_CONTRACT.md` section 2.

## What is here
| File | Role |
| :--- | :--- |
| `UriDeepLinkMatcher` | Pure Kotlin. `match(raw)` and `matchNotification(data)` return `DeepLinkResult.Matched(AppLink)` or `Rejected(DeepLinkRejection)`. `parseAppLink(raw, host)` is the contract 2.6 signature (null = not ours). |
| `AppLink` | Parsed link. `@Serializable`: store this as `pendingLink`, not the string. |
| `toDestination()` | `AppLink` to `DeepLinkDestination(tab, stack)`: the tab root plus the target key, a synthetic back stack. |

One parser serves App Link intents (`intent.dataString`), notification `data.link` and the QR scanner.

## Accepted shapes
Scheme `https` only, host equal to the configured host. No custom scheme. Ids are `[0-9]{1,18}`.

| URL | AppLink | Route key (tab) |
| :--- | :--- | :--- |
| `/m/{id}` and `/m/{id}?s={SHOPCODE}` (`[A-Z0-9]{8}`) | `Machine` | `MachineKey` (MACHINES) |
| `/c/{token}` (22 chars of `[A-Za-z0-9_-]`) | `Contact` | `ConnectKey` (ME) |
| `/t/{id}` and `?m={messageId}` | `Thread` | `ChatKey` (CHATS) |
| `/n/{id}` and `?e={eventId}` | `Notice` | `NoticeKey` (HOME) |
| `/b/{id}` | `Briefing` | `BriefingCardKey` (HOME) |
| `/nm/{id}` | `NearMiss` | `NearMissDetailKey` (HOME) |
| `/i/due` | `InterviewsDue` | `InterviewKey` (HOME) |

`/senpai/{token}` and every other path are rejected: they stay web pages.

## Strictness
- Length over 2048 characters is rejected.
- Any space, control character, DEL or non-ASCII character is rejected before parsing (homographs, full-width forms, zero-width and bidi characters). No trimming.
- Host must equal an allowed host, ignoring case. Userinfo, port (even 443), trailing dot, IP hosts and lookalike suffixes fail.
- The path is never percent-decoded: any `%` in an id fails. `..`, `.`, empty segments, trailing slash and extra segments fail. Path letters are case-sensitive.
- Known query keys (`s`, `m`, `e`) must be valid and appear once; otherwise the whole link is rejected. Values are not decoded. Unknown keys and the fragment are ignored.
- Nothing is fetched or redirected, so there is no open redirect. A link never carries a credential.
- Notification payload: `data.v` must be `"1"`; `data.link` is the only navigation input.
- The matcher throws `IllegalArgumentException` at construction if a host is not a bare lowercase host name, so an unreplaced `[HOST]` fails fast.

## Left to the app
Role guards, the Worker-only `POST /machines/{id}/scan`, the "another shop" screen, `pendingLink` storage and resume after sign-in (contract 2.4). Contact `token`, Machine `shopCode`, Thread `messageId` and Notice `eventId` stay on the `AppLink`: no route key carries them yet.

## Manifest intent filter (in `:app`)
Contract 2.3. Replace `[HOST]` with the decided host (open item O-12). The filter belongs on `MainActivity`. A library manifest merging into a same-name activity is UNVERIFIED (ANDROID_ARCHITECTURE WP-DL), so put it in `app/src/main/AndroidManifest.xml`.

```xml
<intent-filter android:autoVerify="true">
  <action android:name="android.intent.action.VIEW" />
  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
  <data android:scheme="https" android:host="[HOST]" />
  <data android:pathPrefix="/m/" />
  <data android:pathPrefix="/c/" />
  <data android:pathPrefix="/t/" />
  <data android:pathPrefix="/n/" />
  <data android:pathPrefix="/b/" />
  <data android:pathPrefix="/nm/" />
  <data android:pathPrefix="/i/" />
</intent-filter>
```

`MainActivity` should use `launchMode="singleTask"` (or handle `onNewIntent`) so a link opens in the running task. `/senpai/` is deliberately absent.

## Digital Asset Links
Served at `https://[HOST]/.well-known/assetlinks.json`: HTTPS, `Content-Type: application/json`, no redirect, on every host listed in the manifest. Package name is `com.techtsunagu.wakatta` (ANDROID_RELEASE R2).

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.techtsunagu.wakatta",
    "sha256_cert_fingerprints": ["[SHA256_FINGERPRINT]"]
  }
}]
```

- Fingerprint: `keytool -list -v -keystore <release.keystore>`, line `SHA256:`. The direct APK has no Play App Signing, so it is the release keystore. If Play is added, list the Play-signed fingerprint beside it. During a key rotation list both.
- A debug build needs its own fingerprint listed to verify.
- Android 14 and lower pick up a changed file only at install or update. Android 15 and higher re-verify periodically, up to 7 days.
- A staging host needs its own `assetlinks.json` and a build variant with that host in its manifest and in `UriDeepLinkMatcher(allowedHosts)`.
- Mock-mode APK (ANDROID_RELEASE R6): no backend, so verification cannot happen yet.

Check on a device:
```
adb shell pm verify-app-links --re-verify com.techtsunagu.wakatta
adb shell pm get-app-links com.techtsunagu.wakatta
adb shell am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d "https://[HOST]/m/12?s=ABCD1234"
adb shell am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d "https://[HOST]/senpai/abc"
curl -sI https://[HOST]/.well-known/assetlinks.json
```
Expected: `[HOST]: verified`; the first `am start` opens the app with no chooser; the `/senpai/` one opens the browser; curl shows 200, `application/json`, no `location` header.
