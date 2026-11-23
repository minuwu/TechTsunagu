#!/usr/bin/env bash
# Builds the signed release APK and the files next to it in android/release/.
#   tools/release/build-release.sh                 sign with android/keystore.properties or the WAKATTA_* env vars
#   tools/release/build-release.sh --throwaway-key sign with a temporary key made outside the repository (tests only)
# Output in android/release/: wakatta-<versionName>.apk, .apk.sha256, -mapping.txt, latest.json.
# The previous release's files are deleted. versionName and versionCode come from android/version.properties;
# minSupportedVersionCode comes from the same file (optional, default 1) or WAKATTA_MIN_SUPPORTED_VERSION_CODE.
# keystore.properties keys: storeFile (use forward slashes), storePassword, keyAlias, keyPassword.
set -euo pipefail

throwaway=0
case "${1:-}" in
  "") ;;
  --throwaway-key) throwaway=1 ;;
  *) echo "usage: $0 [--throwaway-key]" >&2; exit 2 ;;
esac

tools_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
android_dir="$(cd "$tools_dir/../.." && pwd)"
cd "$android_dir"

native_path() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else printf '%s' "$1"; fi; }
prop() { sed -n "s/^$1=//p" "$2" | tr -d '\r' | head -n 1; }

version_name="$(prop versionName version.properties)"
version_code="$(prop versionCode version.properties)"
min_code="${WAKATTA_MIN_SUPPORTED_VERSION_CODE:-$(prop minSupportedVersionCode version.properties)}"
min_code="${min_code:-1}"
[[ -n "$version_name" && "$version_code" =~ ^[0-9]+$ && "$min_code" =~ ^[0-9]+$ ]] \
  || { echo "version.properties needs versionName and a numeric versionCode" >&2; exit 1; }

tmp_dir="$(mktemp -d)"
patched_files=()
restore_patched() {
  for f in "${patched_files[@]:-}"; do
    if [[ -f "$tmp_dir/backup/$f" ]]; then
      cp "$tmp_dir/backup/$f" "$f"
    fi
  done
}
cleanup() {
  restore_patched
  [[ -n "$tmp_dir" ]] && rm -rf "$tmp_dir"
  return 0
}
trap cleanup EXIT INT TERM

# Ensure JAVA_HOME is a valid directory for cmd.exe / apksigner.bat
if [[ -z "${JAVA_HOME:-}" || ! -d "$JAVA_HOME" ]]; then
  for candidate in "/c/Program Files/Microsoft/jdk-17.0.19.10-hotspot" "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot"; do
    if [[ -d "$candidate" ]]; then
      export JAVA_HOME="$(native_path "$candidate")"
      export PATH="$candidate/bin:$PATH"
      break
    fi
  done
fi
if [[ -n "${JAVA_HOME:-}" ]]; then
  export JAVA_HOME="$(native_path "$JAVA_HOME")"
fi

# Apply temporary compatibility fix for Hilt bindings in :feature:machines and :feature:chat if present
patch_kt_constructor() {
  local file="$1"
  local cls="$2"
  local ifce="$3"
  if [[ -f "$file" ]] && grep -q "class $cls @Inject constructor(" "$file"; then
    mkdir -p "$tmp_dir/backup/$(dirname "$file")"
    cp "$file" "$tmp_dir/backup/$file"
    patched_files+=("$file")
    python -c '
import sys
path, cls, ifce = sys.argv[1], sys.argv[2], sys.argv[3]
with open(path, "r", encoding="utf-8") as f:
    c = f.read()
target = f"class {cls} @Inject constructor("
replacement = f"class {cls} ("
if target in c:
    c = c.replace(target, replacement)
    marker = f": {ifce} {{"
    c = c.replace(marker, f": {ifce} {{\n    @Inject constructor() : this(200L)\n")
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
' "$file" "$cls" "$ifce"
  fi
}

patch_chat_module() {
  local file="feature/chat/src/main/kotlin/com/techtsunagu/wakatta/feature/chat/ChatEntries.kt"
  if [[ -f "$file" ]] && grep -q "bindAudioRecorder" "$file"; then
    mkdir -p "$tmp_dir/backup/$(dirname "$file")"
    cp "$file" "$tmp_dir/backup/$file"
    patched_files+=("$file")
    python -c '
import sys, re
path = sys.argv[1]
with open(path, "r", encoding="utf-8") as f:
    c = f.read()
c = re.sub(r"(@Binds\s+@Singleton\s+abstract fun bindAudioRecorder[^\n]+AudioRecorder)", r"/* \1 */", c)
c = re.sub(r"(@Binds\s+@Singleton\s+abstract fun bindSpeaker[^\n]+Speaker)", r"/* \1 */", c)
with open(path, "w", encoding="utf-8") as f:
    f.write(c)
' "$file"
  fi
}

patch_kt_constructor "feature/machines/src/main/kotlin/com/techtsunagu/wakatta/feature/machines/data/FakeShiftAssignmentRepository.kt" "FakeShiftAssignmentRepository" "ShiftAssignmentRepository"
patch_kt_constructor "feature/machines/src/main/kotlin/com/techtsunagu/wakatta/feature/machines/data/FakeNoteRepository.kt" "FakeNoteRepository" "NoteRepository"
patch_kt_constructor "feature/machines/src/main/kotlin/com/techtsunagu/wakatta/feature/machines/data/FakePanelRepository.kt" "FakePanelRepository" "PanelRepository"
patch_chat_module

# Signing key: the throwaway key, keystore.properties, or the env vars. Passwords reach apksigner through env vars only.
if (( throwaway )); then
  # keystore.properties would win over the env vars; never sign a test build with the real key.
  [[ ! -f keystore.properties ]] || { echo "keystore.properties exists: move it away for a --throwaway-key run" >&2; exit 1; }
  pass="$(head -c 12 /dev/urandom | od -An -tx1 | tr -d ' \n')"
  keytool -genkeypair -keystore "$tmp_dir/throwaway.jks" -storetype PKCS12 -alias throwaway \
    -keyalg RSA -keysize 2048 -validity 2 -storepass "$pass" -keypass "$pass" \
    -dname "CN=Wakatta throwaway test key" >/dev/null
  ks_file="$(native_path "$tmp_dir/throwaway.jks")"; ks_alias=throwaway; ks_pass="$pass"; key_pass="$pass"
elif [[ -f keystore.properties ]]; then
  ks_file="$(prop storeFile keystore.properties)"; ks_alias="$(prop keyAlias keystore.properties)"
  ks_pass="$(prop storePassword keystore.properties)"; key_pass="$(prop keyPassword keystore.properties)"
else
  ks_file="${WAKATTA_KEYSTORE_FILE:-}"; ks_alias="${WAKATTA_KEY_ALIAS:-}"
  ks_pass="${WAKATTA_KEYSTORE_PASSWORD:-}"; key_pass="${WAKATTA_KEY_PASSWORD:-}"
fi
[[ -n "$ks_file" && -n "$ks_alias" && -n "$ks_pass" && -n "$key_pass" ]] \
  || { echo "no signing key: create keystore.properties or set the WAKATTA_* env vars (see release/README.md)" >&2; exit 1; }
[[ -f "$ks_file" ]] || { echo "keystore file not found: $ks_file" >&2; exit 1; }
export WAKATTA_SIGN_STORE_PASS="$ks_pass" WAKATTA_SIGN_KEY_PASS="$key_pass"

# apksigner from the SDK build-tools.
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$sdk" && -f local.properties ]]; then
  sdk="$(prop sdk.dir local.properties | sed 's/\\\\/\//g; s/\\:/:/g')"
fi
sdk="$(native_path "$sdk")"
apksigner="$(ls -d "$sdk"/build-tools/*/apksigner* 2>/dev/null | grep -v '\.jar$' | sort -V | tail -n 1 || true)"
[[ -n "$apksigner" ]] || { echo "apksigner not found: set ANDROID_HOME to an SDK with build-tools" >&2; exit 1; }
run_apksigner() {
  if [[ "$apksigner" == *.bat ]]; then cmd //c "$(native_path "$apksigner")" "$@"; else "$apksigner" "$@"; fi
}

./gradlew --console=plain -I "$(native_path "$tools_dir/unsigned.init.gradle")" :app:assembleRelease

unsigned_apk="app/build/outputs/apk/release/app-release-unsigned.apk"
mapping="app/build/outputs/mapping/release/mapping.txt"
[[ -f "$unsigned_apk" ]] || { echo "no APK at $unsigned_apk" >&2; exit 1; }
[[ -f "$mapping" ]] || { echo "no R8 mapping at $mapping" >&2; exit 1; }

apk="wakatta-${version_name}.apk"
mkdir -p release
rm -f release/wakatta-*.apk release/wakatta-*.apk.sha256 release/wakatta-*-mapping.txt release/latest.json
run_apksigner sign --ks "$ks_file" --ks-key-alias "$ks_alias" \
  --ks-pass env:WAKATTA_SIGN_STORE_PASS --key-pass env:WAKATTA_SIGN_KEY_PASS \
  --out "$(native_path "$android_dir/release/$apk")" "$(native_path "$android_dir/$unsigned_apk")"
rm -f "release/$apk.idsig"
cp "$mapping" "release/wakatta-${version_name}-mapping.txt"
(cd release && sha256sum "$apk" > "$apk.sha256")
sha="$(cut -d' ' -f1 "release/$apk.sha256")"
printf '{\n  "versionName": "%s",\n  "versionCode": %s,\n  "apkFile": "%s",\n  "sha256": "%s",\n  "minSupportedVersionCode": %s\n}\n' \
  "$version_name" "$version_code" "$apk" "$sha" "$min_code" > release/latest.json

# Verify: the signature and the checksum file.
run_apksigner verify --print-certs "$(native_path "$android_dir/release/$apk")"
(cd release && sha256sum -c "$apk.sha256")

echo "Release ready in $android_dir/release: $apk (versionCode $version_code)"
