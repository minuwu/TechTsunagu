# Wipe and reload the demo data on the deployed API (before filming, or after testers made a mess).
# One revision starts with SEED_ON_START=reset (backend/jobs/start.py), then the flag is removed again so a later
# restart can never wipe data by itself.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\reset-demo.ps1
. "$PSScriptRoot\common.ps1"

Wait-Idle
Step "starting a revision that resets the demo data"
Invoke-Az containerapp update -n $App -g $Rg --set-env-vars "SEED_ON_START=reset" -o none
$null = Wait-Ready
Step "removing the reset flag"
Invoke-Az containerapp update -n $App -g $Rg --remove-env-vars SEED_ON_START -o none
$fqdn = Wait-Ready
Step "demo data reset: https://$fqdn (shop SASEBO01, Foreman PIN 246810, Workers PIN 2468)"
