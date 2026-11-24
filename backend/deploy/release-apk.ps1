# Publish a release APK (XREQ-025): upload it to public blob storage, then point GET /api/app/config at it.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\release-apk.ps1 <path\to\wakatta-1.2.0.apk> 1.2.0 [-MinVersion 1.1.0] [-SkipConfig]
#   -MinVersion   also raise the hard gate (phones below it must update); left out, the minimum stays as it is.
#   -SkipConfig   upload and verify only, without changing the API (a rehearsal).
# The storage account and its container are created on the first run. A version is never overwritten: a fixed
# build is a new version number, so the sha256 a phone already checked can never point at other bytes.
param(
    [Parameter(Mandatory = $true)][string]$Apk,
    [Parameter(Mandatory = $true)][string]$Version,
    [string]$MinVersion,
    [switch]$SkipConfig
)
. "$PSScriptRoot\common.ps1"
$Container = "apk"

if ($Version -notmatch '^\d+\.\d+\.\d+$') { throw "version must be MAJOR.MINOR.PATCH, got '$Version'" }
if ($MinVersion -and $MinVersion -notmatch '^\d+\.\d+\.\d+$') { throw "min version must be MAJOR.MINOR.PATCH" }
if (-not (Test-Path -LiteralPath $Apk -PathType Leaf)) { throw "APK not found: $Apk" }
$blob = "wakatta-$Version.apk"
$sha = (Get-FileHash -LiteralPath $Apk -Algorithm SHA256).Hash.ToLowerInvariant()

# One storage account for releases: public read on blobs of one container only (no listing), HTTPS, TLS 1.2.
$account = Invoke-Az storage account list -g $Rg --query "[?starts_with(name,'wakattaapk')].name | [0]" -o tsv
if (-not $account) {
    $account = "wakattaapk" + (Get-Random -Minimum 10000 -Maximum 99999)
    Step "storage account $account (Standard_LRS, public blob read allowed)"
    Invoke-Az storage account create -n $account -g $Rg -l $Loc --sku Standard_LRS --kind StorageV2 `
        --https-only true --min-tls-version TLS1_2 --allow-blob-public-access true -o none
}
# The key stays in this process's environment for the data-plane calls; it is never printed or passed as an argument.
$env:AZURE_STORAGE_KEY = Invoke-Az storage account keys list -n $account -g $Rg --query "[0].value" -o tsv
try {
    Invoke-Az storage container create -n $Container --account-name $account --public-access blob -o none
    $exists = Invoke-Az storage blob exists -c $Container -n $blob --account-name $account --query exists -o tsv
    if ($exists -eq "true") { throw "$blob is already published; give the new build a new version number" }
    Step "uploading $blob (sha256 $sha)"
    Invoke-Az storage blob upload -c $Container -n $blob -f $Apk --account-name $account `
        --content-type "application/vnd.android.package-archive" --overwrite false -o none
} finally {
    Remove-Item Env:\AZURE_STORAGE_KEY -ErrorAction SilentlyContinue
}

# What a phone will download, checked byte for byte over anonymous HTTPS.
$url = "https://$account.blob.core.windows.net/$Container/$blob"
$tmp = Join-Path ([IO.Path]::GetTempPath()) "$blob.check"
try {
    $res = Invoke-WebRequest $url -OutFile $tmp -PassThru -UseBasicParsing -TimeoutSec 120
    $got = (Get-FileHash -LiteralPath $tmp -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($got -ne $sha) { throw "downloaded bytes differ from the APK (sha256 $got)" }
    if ($res.Headers["Content-Type"] -ne "application/vnd.android.package-archive") {
        throw "wrong Content-Type: $($res.Headers['Content-Type'])"
    }
} finally {
    Remove-Item -LiteralPath $tmp -ErrorAction SilentlyContinue
}
Step "public and verified: $url"
if ($SkipConfig) { Step "-SkipConfig: GET /api/app/config unchanged"; return }

$vars = @("APP_LATEST_VERSION=$Version", "APP_DOWNLOAD_URL=$url", "APP_SHA256=$sha")
if ($MinVersion) { $vars += "APP_MIN_VERSION=$MinVersion" }
Wait-Idle
Step "app config: $($vars -join ' ')"
Invoke-Az containerapp update -n $App -g $Rg --set-env-vars @vars -o none
$fqdn = Wait-Ready
$config = (Invoke-RestMethod "https://$fqdn/api/app/config" -TimeoutSec 20).data
if ($config.latestVersion -ne $Version -or $config.sha256 -ne $sha -or $config.downloadUrl -ne $url) {
    throw "GET /api/app/config does not show the new release yet: $($config | ConvertTo-Json -Compress)"
}
Step "released ${Version}: GET /api/app/config now offers it (minVersion $($config.minVersion))"
