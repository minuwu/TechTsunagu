# One-shot onboarding (Windows). Run from repo root: powershell -ExecutionPolicy Bypass -File scripts\setup.ps1
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "[setup] enabling pre-push sync hook"
git config core.hooksPath .githooks

Write-Host "[setup] checking tools"
foreach ($t in @("git","node","npm","python")) {
  $c = Get-Command $t -ErrorAction SilentlyContinue
  if ($c) { Write-Host "  ok   $t" } else { Write-Host "  MISSING $t" }
}
python -m graphify --help *> $null
if ($LASTEXITCODE -eq 0) { Write-Host "  ok   graphify" } else { Write-Host "  installing graphify"; pip install -q graphifyy }

$name = git config user.name
Write-Host "[setup] git identity: $name <$(git config user.email)>"
if (-not $name) { Write-Host '  set it: git config user.name "Your Name"; git config user.email you@example.com' }

Write-Host ""
Write-Host "Next: read TEAM_INSTRUCTIONS.md, then AGENTS.md. Create your branch: git checkout -b <name>/<topic>"
