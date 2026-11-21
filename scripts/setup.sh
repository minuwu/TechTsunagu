#!/bin/sh
# One-shot onboarding. Run from repo root: sh scripts/setup.sh
set -e
cd "$(dirname "$0")/.."

echo "[setup] enabling pre-push sync hook"
git config core.hooksPath .githooks
chmod +x .githooks/pre-push 2>/dev/null || true

echo "[setup] checking tools"
for t in git node npm python; do
  if command -v "$t" >/dev/null 2>&1; then echo "  ok   $t ($($t --version 2>&1 | head -1))"; else echo "  MISSING $t"; fi
done
if command -v graphify >/dev/null 2>&1 || python -m graphify --help >/dev/null 2>&1; then
  echo "  ok   graphify"
else
  echo "  installing graphify (knowledge graph CLI)"; pip install -q graphifyy || echo "  WARN graphify install failed; run: pip install graphifyy"
fi

name=$(git config user.name)
echo "[setup] git identity: ${name:-<unset>} <$(git config user.email)>"
[ -z "$name" ] && echo "  set it: git config user.name \"Your Name\"; git config user.email you@example.com"

echo
echo "Next: read TEAM_INSTRUCTIONS.md, then AGENTS.md. Create your branch: git checkout -b <name>/<topic>"
