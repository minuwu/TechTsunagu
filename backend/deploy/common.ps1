# Shared settings for the Azure deploy scripts (Windows PowerShell 5.1+). Names, not secrets.
$ErrorActionPreference = "Stop"
$Rg = "wakatta-rg"; $Loc = "japaneast"; $EnvName = "wakatta-env"; $App = "wakatta-api"; $Identity = "wakatta-id"
$Image = "techtsunagu-api"; $DbAdmin = "wakattaadmin"

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
# Start a manual Container Apps Job and wait for its execution to finish.
function Wait-Job($name) {
    $exec = Invoke-Az containerapp job start -n $name -g $Rg --query name -o tsv
    foreach ($i in 1..90) {
        Start-Sleep -Seconds 10
        $status = Invoke-Az containerapp job execution show -n $name -g $Rg --job-execution-name $exec `
            --query properties.status -o tsv
        if ($status -eq "Succeeded") { Step "$name succeeded"; return }
        if ($status -eq "Failed") { throw "$name failed. Logs: az containerapp job logs show -n $name -g $Rg --container $name" }
    }
    throw "$name still running after 15 min"
}
function Test-Job($name) { [bool](Invoke-Az containerapp job list -g $Rg --query "[?name=='$name'].name" -o tsv) }
function Get-Acr { Invoke-Az acr list -g $Rg --query "[0].name" -o tsv }
function Get-DbServer { Invoke-Az postgres flexible-server list -g $Rg --query "[0].name" -o tsv }
