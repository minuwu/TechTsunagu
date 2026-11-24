# Wakatta? release folder (direct APK, no Google Play)

This folder holds the latest signed release APK and the files next to it. Only the latest release is kept; the build script deletes the previous one.

| File | What it is |
| :--- | :--- |
| `wakatta-<versionName>.apk` | The signed release APK. |
| `wakatta-<versionName>.apk.sha256` | Checksum in `sha256sum` format: `<64 hex>  wakatta-<versionName>.apk`. |
| `wakatta-<versionName>-mapping.txt` | R8 `mapping.txt`. Without it a release crash stack cannot be read. Keep it with the APK. |
| `latest.json` | The update manifest the app reads: `versionName`, `versionCode`, `apkFile`, `sha256`, `minSupportedVersionCode`. |

The release APK runs in mock mode for now (no backend baked in).

## Rules for the Release Owner

One named person creates the keystore, keeps two offline backups and builds every release APK. The keystore is the identity of the app: with a lost key nobody can update an installed copy, and with a leaked key someone else could sign an "update". Never commit it, never paste its passwords into an issue, a chat or a file in this repository.

## 1. Create the keystore (once)

Run this on the Release Owner's machine. Replace the path and pick long, different passwords; store them in a password manager.

```
keytool -genkeypair -v -keystore <path-outside-the-repository>/wakatta-release.jks \
  -storetype PKCS12 -alias wakatta -keyalg RSA -keysize 4096 -validity 10000
```

Then:

1. Copy the `.jks` file to two offline backups (for example two encrypted USB drives kept in different places). Not on the build machine, not in the repository, not in cloud storage that other people can read.
2. Write down the SHA-256 fingerprint of the certificate (`keytool -list -v -keystore <file>`). It is needed for Android developer verification of `com.techtsunagu.wakatta`.
3. Create `android/keystore.properties` (ignored by git; `git check-ignore android/keystore.properties` must print the path):

```
storeFile=C:/path/outside/the/repository/wakatta-release.jks
storePassword=<store password>
keyAlias=wakatta
keyPassword=<key password>
```

CI can use the environment variables `WAKATTA_KEYSTORE_FILE`, `WAKATTA_KEYSTORE_PASSWORD`, `WAKATTA_KEY_ALIAS` and `WAKATTA_KEY_PASSWORD` instead.

## 2. Build a release

1. Raise `versionCode` by one in `android/version.properties` (and set `versionName`). Optional: `minSupportedVersionCode` in the same file; apps below it get the blocking update screen. Default is 1.
2. From the repository root: `cd android && bash tools/release/build-release.sh`.
3. The script runs `./gradlew :app:assembleRelease`, copies the APK, the checksum, the mapping and `latest.json` into this folder, deletes the previous release's files, and checks the result with `apksigner verify --print-certs` and `sha256sum -c`. It needs `ANDROID_HOME` (or `sdk.dir` in `android/local.properties`) with build-tools installed, and `keytool` only for `--throwaway-key`.
4. Check that `apksigner` printed the fingerprint you wrote down in step 1.2. A different fingerprint means the wrong key signed it: do not publish.
5. Commit the four files (plain git, no Git LFS). Old APKs stay in git history.

`bash tools/release/build-release.sh --throwaway-key` signs with a temporary key made outside the repository. It is for testing the tooling only. Delete its output from this folder afterwards; never share or commit a throwaway-signed APK.

The manual GitHub workflow `android-release.yml` does the same build from CI secrets. Its artifacts are signed with the real key, so only the Release Owner starts it.

## 3. Install on a phone

1. Get `wakatta-<versionName>.apk` onto the phone (download, USB or a messaging app).
2. Open it. Android asks to allow installs from this source ("Install unknown apps"): allow it for the app you opened the file with (for example the browser or Files).
3. Confirm the install. To check the file first, compare its SHA-256 with the `.sha256` file.
4. An update installs over the old version only if it has the same package name, the same signing certificate and a higher `versionCode`. If the certificate differs, Android refuses; the old version must be uninstalled first, which deletes its local data.

The in-app update screen does steps 1 to 3 itself: it downloads the APK named in `latest.json` (or the backend config), checks the SHA-256 and hands it to the system installer.

## 4. What to do if the key leaks or is lost

Android uses the signing certificate to verify that an update comes from the same author as the installed app. Because this app is distributed directly (no Google Play App Signing), certificate changes require special handling:

### If the key is lost
1. Check both offline backup locations (the two encrypted drives created during step 1).
2. If all copies and backups are permanently lost, **you cannot release an update to existing installations**. Android will refuse to install any APK signed with a new certificate over an app signed with the lost key.
3. Generate a new keystore following step 1.
4. Notify all users and shop administrators that they must manually uninstall the existing Wakatta? app and install the new APK. Note: uninstalling removes local SQLite databases and saved preferences, so ensure workers and foremen understand that un-synced local data will be reset.

### If the key leaks or is compromised
1. **Immediate revocation of secrets:** If the compromise involved CI secrets or developer workstations, immediately delete/rotate the secrets in GitHub repository settings (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`).
2. Generate a new keystore with fresh passwords and store two offline backups outside the repository.
3. Build and publish an urgent new APK signed with the new keystore, incrementing `versionCode`.
4. Issue an immediate security bulletin advising all users and shops that the previous signing key was compromised. Instruct all users to uninstall the old app and install the newly signed APK.
5. If backend API services or FCM push tokens were linked to the compromised build/identity, rotate backend secrets and invalidate old sessions/device registrations.

## Not in this folder

Real keystores (`*.jks`, `*.keystore`), `keystore.properties`, passwords and the Firebase `google-services.json`.
