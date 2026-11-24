# Set the browser origins the API accepts (CORS), e.g. local dev plus the hosted frontend.
# Usage: add the site to $FrontendOrigins in common.ps1, then: powershell -ExecutionPolicy Bypass -File backend\deploy\set-cors.ps1
# An argument (comma-separated list) replaces the whole list for this run.
# Exact origins only: scheme://host[:port], no path, no trailing slash, no wildcard (the API refuses to start otherwise).
param([string]$Origins)
. "$PSScriptRoot\common.ps1"
if (-not $Origins) { $Origins = $FrontendOrigins }

Wait-Idle
Step "FRONTEND_ORIGIN=$Origins"
Invoke-Az containerapp update -n $App -g $Rg --set-env-vars "FRONTEND_ORIGIN=$Origins" -o none
$null = Wait-Ready
