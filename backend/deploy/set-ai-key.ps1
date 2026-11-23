# Turn on real AI on the deployed API: copies GEMINI_API_KEY (and GROQ_API_KEY when present) from backend/.env into
# Container App secrets, never printing them, and sets AI_MODE=gemini.
# AI_CHAIN: dev = Flash-Lite only (saves the small daily quota while testing); demo = 3.8 Flash first (filming).
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\set-ai-key.ps1 [-Chain demo]
param([ValidateSet("dev", "demo")][string]$Chain = "dev")
. "$PSScriptRoot\common.ps1"

$vals = @{}
Get-Content (Join-Path (Split-Path $PSScriptRoot -Parent) ".env") -Encoding UTF8 | ForEach-Object {
    if ($_ -match '^\s*(GEMINI_API_KEY|GROQ_API_KEY)\s*=\s*(.+?)\s*$') { $vals[$Matches[1]] = $Matches[2] }
}
if (-not $vals.GEMINI_API_KEY) { throw "GEMINI_API_KEY is missing or empty in backend/.env" }

$secrets = @("gemini-api-key=$($vals.GEMINI_API_KEY)")
$envVars = @("GEMINI_API_KEY=secretref:gemini-api-key", "AI_MODE=gemini", "AI_CHAIN=$Chain")
if ($vals.GROQ_API_KEY) {
    $secrets += "groq-api-key=$($vals.GROQ_API_KEY)"
    $envVars += "GROQ_API_KEY=secretref:groq-api-key"
}
Step "storing $($secrets.Count) AI key(s) as secrets; AI_MODE=gemini, AI_CHAIN=$Chain"
Invoke-Az containerapp secret set -n $App -g $Rg --secrets @secrets -o none
Invoke-Az containerapp update -n $App -g $Rg --set-env-vars @envVars -o none
$null = Wait-Ready
