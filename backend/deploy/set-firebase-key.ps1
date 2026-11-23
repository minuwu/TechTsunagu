# Store the Firebase service-account key (push, XREQ-014) as a Container App secret, never printing it.
# The key file stays outside the repo; delete it after this script succeeds.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\set-firebase-key.ps1 <path-to-key.json> [-ProjectId daily-wizdom]
param(
    [Parameter(Mandatory = $true)][string]$KeyFile,
    [string]$ProjectId = "daily-wizdom"
)
. "$PSScriptRoot\common.ps1"

if (-not (Test-Path -LiteralPath $KeyFile)) { throw "key file not found: $KeyFile" }
$repo = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$full = (Resolve-Path -LiteralPath $KeyFile).Path
if ($full.StartsWith($repo, [StringComparison]::OrdinalIgnoreCase)) {
    throw "the key file is inside the repository; move it outside first so it can never be committed"
}
$raw = [IO.File]::ReadAllText($full)
$key = $raw | ConvertFrom-Json
# Checked without printing any value: the right kind of file for the right Firebase project.
if ($key.type -ne "service_account" -or -not $key.private_key -or -not $key.client_email) {
    throw "not a service-account key (expected type, private_key and client_email)"
}
if ($key.project_id -ne $ProjectId) { throw "the key belongs to another Firebase project than $ProjectId" }
$b64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($raw))

Wait-Idle
Step "storing the Firebase key as secret firebase-service-account; FIREBASE_PROJECT_ID=$ProjectId"
Invoke-Az containerapp secret set -n $App -g $Rg --secrets "firebase-service-account=$b64" -o none
Invoke-Az containerapp update -n $App -g $Rg -o none --set-env-vars `
    "FIREBASE_SERVICE_ACCOUNT_B64=secretref:firebase-service-account" "FIREBASE_PROJECT_ID=$ProjectId"
$null = Wait-Ready
Step "done: delete the local key file now ($KeyFile)"
