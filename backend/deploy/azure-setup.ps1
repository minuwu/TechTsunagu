# One-time Azure setup for the Wakatta? API. Safe to re-run: it skips what exists, and it stops cleanly where it needs
# the first CI build. Run it twice: once now (infrastructure + CI identity), once after the first image is in the registry.
#   - registry + two managed identities: wakatta-id (the apps pull images) and wakatta-ci (GitHub Actions deploys,
#     via OIDC: no stored passwords or tokens)
#   - PostgreSQL B1ms; roles/database created by a job INSIDE Azure, so no laptop ever needs port 5432
#   - jobs wakatta-migrate (alembic) and wakatta-seed (demo data; re-run to reset the demo), and the API app
# The API only gets the least-privilege wakatta_app credential. Secrets are generated here, stored only as
# Container App / Job secrets, and never printed.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\azure-setup.ps1
. "$PSScriptRoot\common.ps1"
$CiIdentity = "wakatta-ci"

Step "providers and resource group"
Invoke-Az config set extension.use_dynamic_install=yes_without_prompt -o none
foreach ($p in "Microsoft.App", "Microsoft.OperationalInsights", "Microsoft.DBforPostgreSQL", "Microsoft.ContainerRegistry", "Microsoft.ManagedIdentity") {
    Invoke-Az provider register -n $p --wait -o none
}
Invoke-Az group create -n $Rg -l $Loc -o none
$rgId = Invoke-Az group show -n $Rg --query id -o tsv

# Retries because a just-created identity or custom role takes a short while to become visible to role assignment.
function Grant($principal, $role, $scope) {
    foreach ($attempt in 1..12) {
        try {
            if (-not (Invoke-Az role assignment list --assignee $principal --scope $scope --role $role --query "[0].id" -o tsv)) {
                Invoke-Az role assignment create --assignee-object-id $principal --assignee-principal-type ServicePrincipal `
                    --role $role --scope $scope -o none
            }
            return
        } catch {
            if ($attempt -eq 12) { throw }
            Start-Sleep -Seconds 10
        }
    }
}

Step "managed identities $Identity (apps pull images) and $CiIdentity (CI deploys)"
Invoke-Az identity create -n $Identity -g $Rg -l $Loc -o none
Invoke-Az identity create -n $CiIdentity -g $Rg -l $Loc -o none
$idRes = Invoke-Az identity show -n $Identity -g $Rg --query id -o tsv
$idPrincipal = Invoke-Az identity show -n $Identity -g $Rg --query principalId -o tsv
$ciPrincipal = Invoke-Az identity show -n $CiIdentity -g $Rg --query principalId -o tsv
$ciClient = Invoke-Az identity show -n $CiIdentity -g $Rg --query clientId -o tsv

$acr = Get-Acr
if (-not $acr) {
    $acr = "wakattaacr" + (Get-Random -Minimum 10000 -Maximum 99999)
    Step "container registry $acr (Basic)"
    Invoke-Az acr create -n $acr -g $Rg -l $Loc --sku Basic --admin-enabled false -o none
}
$acrId = Invoke-Az acr show -n $acr --query id -o tsv
Grant $idPrincipal AcrPull $acrId
Grant $ciPrincipal AcrPush $acrId
# Least privilege for CI: a custom role limited to Container Apps and their jobs in this resource group (plus attaching
# the existing pull identity). No rights over PostgreSQL, the registry, other identities or role assignments.
# listSecrets stays inside Microsoft.App/containerApps/*: `az containerapp update` reads the secrets to preserve them.
$roleName = "Wakatta CI Deployer"
$roleFile = Join-Path ([IO.Path]::GetTempPath()) "wakatta-ci-role.json"
@{
    Name = $roleName
    Description = "Roll and migrate the Wakatta API: Container Apps, jobs and the pull identity, in one resource group."
    Actions = @(
        "Microsoft.App/containerApps/*", "Microsoft.App/jobs/*", "Microsoft.App/locations/*/read",
        "Microsoft.App/managedEnvironments/read", "Microsoft.App/managedEnvironments/join/action",
        "Microsoft.ManagedIdentity/userAssignedIdentities/read",
        "Microsoft.ManagedIdentity/userAssignedIdentities/*/assign/action",
        "Microsoft.Resources/subscriptions/resourceGroups/read"
    )
    NotActions = @(); DataActions = @(); NotDataActions = @()
    AssignableScopes = @($rgId)
} | ConvertTo-Json -Depth 4 | Set-Content -Path $roleFile -Encoding UTF8
if (Invoke-Az role definition list --name $roleName --query "[0].name" -o tsv) {
    Invoke-Az role definition update --role-definition "@$roleFile" -o none
} else {
    Invoke-Az role definition create --role-definition "@$roleFile" -o none
}
Remove-Item $roleFile
Grant $ciPrincipal $roleName $rgId
# An earlier run granted the broad Contributor role: take it away.
foreach ($a in (Invoke-Az role assignment list --assignee $ciPrincipal --scope $rgId --role Contributor --query "[].id" -o tsv)) {
    if ($a) { Invoke-Az role assignment delete --ids $a -o none }
}

# Only a workflow running on the main branch of this repository can sign in as wakatta-ci.
# This repository presents its ID-based subject (owner and repo numeric IDs), not the plain repo name: use the form
# shown in the azure/login log ("subject claim"), or Azure answers AADSTS700213.
$subject = "repo:minuwu@84508269/TechTsunagu@1466841300:ref:refs/heads/main"
if (Invoke-Az identity federated-credential list --identity-name $CiIdentity -g $Rg --query "[?name=='github-main'].name" -o tsv) {
    Invoke-Az identity federated-credential update --identity-name $CiIdentity -g $Rg --name github-main --subject $subject -o none
} else {
    Invoke-Az identity federated-credential create --identity-name $CiIdentity -g $Rg --name github-main `
        --issuer "https://token.actions.githubusercontent.com" --subject $subject --audiences "api://AzureADTokenExchange" -o none
}

$db = Get-DbServer
if (-not $db) {
    $db = "wakatta-db-" + (Get-Random -Minimum 10000 -Maximum 99999)
    Step "postgres $db (B1ms, PG16; 5-10 min)"
    Invoke-Az postgres flexible-server create -g $Rg -n $db -l $Loc --tier Burstable --sku-name Standard_B1ms `
        --storage-size 32 --version 16 --admin-user $DbAdmin --admin-password (New-Secret 32) --public-access None `
        --zonal-resiliency Disabled --backup-retention 7 --yes -o none
}
# Remove any laptop IP rule left by an earlier attempt; only the Azure-services rule stays.
foreach ($rule in (Invoke-Az postgres flexible-server firewall-rule list -g $Rg -s $db --query "[?starts_with(name, 'FirewallIPAddress')].name" -o tsv)) {
    Invoke-Az postgres flexible-server firewall-rule delete -g $Rg -s $db -n $rule --yes -o none
}
# 0.0.0.0 = Azure-hosted clients only (the API and the jobs), still behind TLS and 32-char random passwords.
# Follow-up: VNet-integrate the environment and give the server private access, then drop this rule.
Invoke-Az postgres flexible-server firewall-rule create -g $Rg -s $db -n AllowAzureServices `
    --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 -o none
$dbHost = "$db.postgres.database.azure.com"

Step "container apps environment $EnvName"
if (-not (Invoke-Az containerapp env list -g $Rg --query "[?name=='$EnvName'].name" -o tsv)) {
    Invoke-Az containerapp env create -n $EnvName -g $Rg -l $Loc -o none
}

$tenant = Invoke-Az account show --query tenantId -o tsv
$sub = Invoke-Az account show --query id -o tsv
Write-Output ""
Write-Output "CI identity (not secrets; they go in .github/workflows/backend-deploy.yml):"
Write-Output "  AZURE_CLIENT_ID=$ciClient  AZURE_TENANT_ID=$tenant  AZURE_SUBSCRIPTION_ID=$sub  ACR=$acr"

if (Invoke-Az containerapp list -g $Rg --query "[?name=='$App'].name" -o tsv) {
    Step "$App already exists: setup is complete (CI ships code from now on)"; return
}

# Newest commit-tagged image (never the mutable "latest"). The repository does not exist until CI pushes one.
try { $ref = @(Invoke-Az acr repository show-tags -n $acr --repository $Image --orderby time_desc --top 5 -o tsv) | Where-Object { $_ -and $_ -ne "latest" } | Select-Object -First 1 }
catch { $ref = $null }
if (-not $ref) {
    Step "infrastructure ready. No image yet: push to main so CI builds it, then run this script again."; return
}
$ref = "$acr.azurecr.io/${Image}:$ref"
Step "using image $ref"

# Fresh credentials. The admin password is reset here and only the dbinit job ever holds it.
$adminPw = New-Secret 32; $ownerPw = New-Secret 32; $appPw = New-Secret 32; $pepper = New-Secret 48
Step "reset the database admin password"
Invoke-Az postgres flexible-server update -g $Rg -n $db --admin-password $adminPw -o none

$ownerUrl = "postgresql+psycopg://wakatta_owner:$ownerPw@${dbHost}:5432/wakatta?sslmode=require"
$appUrl = "postgresql+psycopg://wakatta_app:$appPw@${dbHost}:5432/wakatta?sslmode=require"
$adminUrl = "postgresql://${DbAdmin}:$adminPw@${dbHost}:5432/postgres?sslmode=require"

# Jobs run scripts that ship in the image (backend/jobs), because the CLI cannot pass shell flags such as -c.
$registry = @("--registry-server", "$acr.azurecr.io", "--registry-identity", $idRes)
$jobBase = @("--environment", $EnvName, "--trigger-type", "Manual", "--replica-retry-limit", "0", "--parallelism", "1",
    "--replica-completion-count", "1", "--image", $ref) + $registry + @("--mi-user-assigned", $idRes)
$ownerEnv = @("MIGRATION_DATABASE_URL=secretref:migration-database-url", "APP_DB_ROLE=wakatta_app", "PIN_PEPPER=secretref:pin-pepper")

Step "job wakatta-dbinit (roles + database)"
if (Test-Job wakatta-dbinit) { Invoke-Az containerapp job delete -n wakatta-dbinit -g $Rg --yes -o none }
Invoke-Az containerapp job create -n wakatta-dbinit -g $Rg @jobBase --replica-timeout 600 --cpu 0.25 --memory 0.5Gi `
    --secrets "admin-url=$adminUrl" "owner-pw=$ownerPw" "app-pw=$appPw" `
    --env-vars "ADMIN_DATABASE_URL=secretref:admin-url" "OWNER_PASSWORD=secretref:owner-pw" "APP_PASSWORD=secretref:app-pw" "ADMIN_USER=$DbAdmin" `
    --command "python" --args "/app/jobs/dbinit.py" -o none
Wait-Job wakatta-dbinit
# The admin password only ever lived in this job; drop the job so nothing keeps it.
Invoke-Az containerapp job delete -n wakatta-dbinit -g $Rg --yes -o none

foreach ($job in @(
        @{ name = "wakatta-migrate"; command = "alembic"; args = @("upgrade", "head") },
        @{ name = "wakatta-seed"; command = "python"; args = @("/app/jobs/seed_reset.py") })) {
    Step "job $($job.name)"
    if (Test-Job $job.name) { Invoke-Az containerapp job delete -n $job.name -g $Rg --yes -o none }
    Invoke-Az containerapp job create -n $job.name -g $Rg @jobBase --replica-timeout 900 --cpu 0.5 --memory 1.0Gi `
        --secrets "migration-database-url=$ownerUrl" "pin-pepper=$pepper" --env-vars @ownerEnv `
        --command $job.command --args @($job.args) -o none
    Wait-Job $job.name
}

Step "container app $App (app role only)"
Invoke-Az containerapp create -n $App -g $Rg --environment $EnvName --image $ref @registry --user-assigned $idRes `
    --target-port 8000 --ingress external --min-replicas 1 --max-replicas 1 --cpu 0.5 --memory 1.0Gi `
    --secrets "database-url=$appUrl" "pin-pepper=$pepper" `
    --env-vars "DATABASE_URL=secretref:database-url" "PIN_PEPPER=secretref:pin-pepper" `
    "FRONTEND_ORIGIN=http://localhost:3000" "DEMO_MODE=true" "LOG_LEVEL=INFO" "AI_MODE=fake" -o none

$fqdn = Invoke-Az containerapp show -n $App -g $Rg --query properties.configuration.ingress.fqdn -o tsv
foreach ($i in 1..30) {
    try { $null = Invoke-RestMethod "https://$fqdn/api/health/ready" -TimeoutSec 10; Step "ready: https://$fqdn"; return }
    catch { Start-Sleep -Seconds 10 }
}
throw "not ready after 5 min. Logs: az containerapp logs show -n $App -g $Rg --tail 50"
