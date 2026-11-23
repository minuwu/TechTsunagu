# Shared settings for the Azure deploy scripts (Windows PowerShell 5.1+). Names, not secrets.
$ErrorActionPreference = "Stop"
$Rg = "wakatta-rg"; $Loc = "japaneast"; $EnvName = "wakatta-env"; $App = "wakatta-api"; $Identity = "wakatta-id"
$Image = "techtsunagu-api"
# Browser origins allowed by CORS (comma-separated). Add the hosted frontend here and run deploy/set-cors.ps1.
$FrontendOrigins = "http://localhost:3000"

# Use `az` from PATH; otherwise the uv-installed CLI (uv tool install --prerelease=allow "azure-cli>=2.70").
$UvAz = Join-Path $env:APPDATA "uv\tools\azure-cli\Scripts\python.exe"
function Invoke-Az {
    # Windows PowerShell 5.1 turns any stderr line (even a blank progress line) into a fatal error under "Stop" when
    # 2>&1 is used, so relax it for the call and judge success by the CLI's exit code below.
    $ErrorActionPreference = "Continue"
    if (Get-Command az.cmd -ErrorAction SilentlyContinue) { $out = & az.cmd @args --only-show-errors 2>&1 }
    else { $out = & $UvAz -m azure.cli @args --only-show-errors 2>&1 }
    $code = $LASTEXITCODE
    $ErrorActionPreference = "Stop"
    $out | ForEach-Object { "$_" } | Where-Object { $_ -notmatch 'pkg_resources|SyntaxWarning|^\s+import |excludedActions|^\s*$' }
    if ($code) { throw "az $($args[0..2] -join ' ') failed (exit $code)" }
}
function Step($m) { Write-Output "[$(Get-Date -Format HH:mm:ss)] $m" }
# 64 URL-safe characters: 256 is a multiple of 64, so byte % 64 has no bias.
function New-Secret([int]$n) {
    $chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_'
    $bytes = New-Object byte[] $n
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    -join ($bytes | ForEach-Object { $chars[$_ % 64] })
}
# Wait until the API's newest revision is Healthy and /api/health/ready answers; returns the host name.
# Express environments keep one revision name ("--latest") and never fill latestReadyRevisionName, so check the
# revision's own health; the first pause lets an update replace the old reading.
function Wait-Ready {
    $fqdn = Invoke-Az containerapp show -n $App -g $Rg --query properties.configuration.ingress.fqdn -o tsv
    Start-Sleep -Seconds 30
    foreach ($i in 1..40) {
        # Right after an update the revision is being replaced and these reads can fail once: that is "not ready
        # yet", not an error (the 10-minute limit below still ends a real failure).
        try {
            $latest = Invoke-Az containerapp show -n $App -g $Rg --query properties.latestRevisionName -o tsv
            $health = Invoke-Az containerapp revision show -n $App -g $Rg --revision $latest --query properties.healthState -o tsv
        } catch { $health = $null }
        if ($health -eq "Healthy") {
            try {
                $null = Invoke-RestMethod "https://$fqdn/api/health/ready" -TimeoutSec 20
                Write-Host "[$(Get-Date -Format HH:mm:ss)] ready: https://$fqdn"
                return $fqdn
            } catch { }
        }
        Start-Sleep -Seconds 15
    }
    throw "not ready after 10 min. Logs: az containerapp logs show -n $App -g $Rg --tail 80"
}
# Azure allows one change at a time per app (a CI rollout may be running): wait until it is idle before changing it.
function Wait-Idle {
    foreach ($i in 1..60) {
        $state = Invoke-Az containerapp show -n $App -g $Rg --query properties.provisioningState -o tsv
        if ($state -notin @("InProgress", "Updating", "Provisioning")) { return }
        Start-Sleep -Seconds 10
    }
    throw "$App stayed busy for 10 min (provisioningState $state)"
}
# Reads the named keys from backend/.env into a hashtable (values are never printed).
function Read-DotEnv([string[]]$Names) {
    $vals = @{}
    $pattern = '^\s*(' + ($Names -join '|') + ')\s*=\s*(.+?)\s*$'
    Get-Content (Join-Path (Split-Path $PSScriptRoot -Parent) ".env") -Encoding UTF8 | ForEach-Object {
        if ($_ -match $pattern) { $vals[$Matches[1]] = $Matches[2] }
    }
    $vals
}
function Get-Acr { Invoke-Az acr list -g $Rg --query "[0].name" -o tsv }
