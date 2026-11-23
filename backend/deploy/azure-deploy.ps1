# Deploy the current backend: build the image inside Azure (no Docker needed), run the migrations in the
# wakatta-migrate job (the only place that holds the owner credential), then roll the API and wait until ready.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\azure-deploy.ps1
. "$PSScriptRoot\common.ps1"

$backend = Split-Path $PSScriptRoot -Parent
$acr = Get-Acr
if (-not $acr) { throw "no container registry in ${Rg}: run azure-setup.ps1 first" }

$tag = (git -C $backend rev-parse --short HEAD).Trim()
if (git -C $backend status --porcelain -- .) { $tag = "$tag-dirty" }
$ref = "$acr.azurecr.io/${Image}:$tag"

Step "building $ref in Azure (2-4 min)"
Invoke-Az acr build -r $acr -t "${Image}:$tag" -t "${Image}:latest" $backend --no-logs -o none

if (Test-Job wakatta-migrate) {
    Step "migrating with $tag"
    Invoke-Az containerapp job update -n wakatta-migrate -g $Rg --image $ref -o none
    Wait-Job wakatta-migrate
}

if (Invoke-Az containerapp list -g $Rg --query "[?name=='$App'].name" -o tsv) {
    Step "rolling $App to $tag"
    Invoke-Az containerapp update -n $App -g $Rg --image $ref -o none
    $fqdn = Invoke-Az containerapp show -n $App -g $Rg --query properties.configuration.ingress.fqdn -o tsv
    foreach ($i in 1..30) {
        # The old revision answers until the new one is ready, so wait for the new one before trusting /ready.
        $rev = Invoke-Az containerapp show -n $App -g $Rg `
            --query "[properties.latestRevisionName, properties.latestReadyRevisionName]" -o tsv
        if ($rev[0] -eq $rev[1]) {
            try {
                $r = Invoke-RestMethod "https://$fqdn/api/health/ready" -TimeoutSec 10
                Step "ready: https://$fqdn ($($r.data.status), revision $($rev[0]))"; return
            } catch { }
        }
        Start-Sleep -Seconds 10
    }
    throw "not ready after 5 min. Logs: az containerapp logs show -n $App -g $Rg --tail 50"
}
$ref
