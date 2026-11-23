# Shared settings for the Azure deploy scripts (Windows PowerShell 5.1+). Names, not secrets.
$ErrorActionPreference = "Stop"
$Rg = "wakatta-rg"; $Loc = "japaneast"; $EnvName = "wakatta-env"; $App = "wakatta-api"; $Identity = "wakatta-id"
$Image = "techtsunagu-api"; $DbAdmin = "wakattaadmin"
# Browser origins allowed by CORS (comma-separated). Add the hosted frontend here and run deploy/set-cors.ps1.
$FrontendOrigins = "http://localhost:3000"

# Use `az` from PATH; otherwise the uv-installed CLI (uv tool install --prerelease=allow "azure-cli>=2.70").
$UvAz = Join-Path $env:APPDATA "uv\tools\azure-cli\Scripts\python.exe"
function Invoke-Az {
    if (Get-Command az.cmd -ErrorAction SilentlyContinue) { $out = & az.cmd @args --only-show-errors 2>&1 }
    else { $out = & $UvAz -m azure.cli @args --only-show-errors 2>&1 }
    $code = $LASTEXITCODE
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
# Wait until the newest revision of the API is the ready one and /api/health/ready answers; returns the host name.
# (Right after an update the old revision still answers, so its health alone proves nothing.)
function Wait-Ready {
    $fqdn = Invoke-Az containerapp show -n $App -g $Rg --query properties.configuration.ingress.fqdn -o tsv
    foreach ($i in 1..40) {
        $rev = @(Invoke-Az containerapp show -n $App -g $Rg `
                --query "[properties.latestRevisionName, properties.latestReadyRevisionName]" -o tsv)
        if ($rev[0] -and $rev[0] -eq $rev[1]) {
            try {
                $null = Invoke-RestMethod "https://$fqdn/api/health/ready" -TimeoutSec 10
                Write-Host "[$(Get-Date -Format HH:mm:ss)] ready: https://$fqdn (revision $($rev[0]))"
                return $fqdn
            } catch { }
        }
        Start-Sleep -Seconds 15
    }
    throw "not ready after 10 min. Logs: az containerapp logs show -n $App -g $Rg --tail 80"
}
function Get-Acr { Invoke-Az acr list -g $Rg --query "[0].name" -o tsv }
function Get-DbServer { Invoke-Az postgres flexible-server list -g $Rg --query "[0].name" -o tsv }
