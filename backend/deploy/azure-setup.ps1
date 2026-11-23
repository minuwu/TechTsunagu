# One-time Azure setup for the Wakatta? API. Safe to re-run: it skips what exists, and it stops cleanly where it needs
# the first CI build. Run it twice: once now (infrastructure + CI identity), once after the first image is in the registry.
#   - registry + two managed identities: wakatta-id (the apps pull images) and wakatta-ci (GitHub Actions deploys,
#     via OIDC: no stored passwords or tokens)
#   - PostgreSQL B1ms, reachable only from Azure services, so no laptop ever needs port 5432
#   - the API app. Azure for Students only gets "express" environments (no Jobs), so the first container start
#     creates the roles and database, every start migrates, and the demo seed runs on request (backend/jobs/start.py)
# Secrets are generated here, stored only as Container App secrets, and never printed.
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
# Least privilege for CI: a custom role limited to Container Apps in this resource group (plus attaching
# the existing pull identity). No rights over PostgreSQL, the registry, other identities or role assignments.
# listSecrets stays inside Microsoft.App/containerApps/*: `az containerapp update` reads the secrets to preserve them.
$roleName = "Wakatta CI Deployer"
$roleFile = Join-Path ([IO.Path]::GetTempPath()) "wakatta-ci-role.json"
@{
    Name = $roleName
    Description = "Roll the Wakatta API: Container Apps and the pull identity, in one resource group."
    Actions = @(
        "Microsoft.App/containerApps/*", "Microsoft.App/locations/*/read",
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
# 0.0.0.0 = Azure-hosted clients only (the API), still behind TLS and 32-char random passwords.
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

# Removes the first-boot credential and the demo-wipe flag from the app, then rotates the admin password so any
# copy left in an old revision is useless. Safe to call when they are already gone.
function Remove-Bootstrap {
    Step "removing the bootstrap credential and seed flag; rotating the admin password"
    if (-not (Invoke-Az containerapp list -g $Rg --query "[?name=='$App'].name" -o tsv)) { return }  # create failed: nothing to clean
    $present = @(Invoke-Az containerapp show -n $App -g $Rg --query "properties.template.containers[0].env[].name" -o tsv)
    $leftover = @("ADMIN_DATABASE_URL", "ADMIN_USER", "OWNER_PASSWORD", "APP_PASSWORD", "SEED_ON_START") | Where-Object { $present -contains $_ }
    if ($leftover) { Invoke-Az containerapp update -n $App -g $Rg --remove-env-vars @leftover -o none }
    $secrets = @(Invoke-Az containerapp secret list -n $App -g $Rg --query "[].name" -o tsv)
    $leftSecrets = @("admin-url", "owner-pw", "app-pw") | Where-Object { $secrets -contains $_ }
    if ($leftSecrets) { Invoke-Az containerapp secret remove -n $App -g $Rg --secret-names @leftSecrets -o none }
    Invoke-Az postgres flexible-server update -g $Rg -n $db --admin-password (New-Secret 32) -o none
}

if (Invoke-Az containerapp list -g $Rg --query "[?name=='$App'].name" -o tsv) {
    Remove-Bootstrap; Step "$App already exists: setup is complete (CI ships code from now on)"; return
}

# Newest commit-tagged image (never the mutable "latest"). The repository does not exist until CI pushes one.
try { $ref = @(Invoke-Az acr repository show-tags -n $acr --repository $Image --orderby time_desc --top 5 -o tsv) | Where-Object { $_ -and $_ -ne "latest" } | Select-Object -First 1 }
catch { $ref = $null }
if (-not $ref) {
    Step "infrastructure ready. No image yet: push to main so CI builds it, then run this script again."; return
}
$ref = "$acr.azurecr.io/${Image}:$ref"
Step "using image $ref"

# Fresh credentials. Express environments have no Jobs, so the first container start does the one-off work
# (backend/jobs/start.py): roles + database with the admin credential, migrations, demo seed. The admin credential
# and the seed flag are removed right after, and the admin password is rotated so the old value is useless.
$adminPw = New-Secret 32; $ownerPw = New-Secret 32; $appPw = New-Secret 32; $pepper = New-Secret 48
Step "reset the database admin password"
Invoke-Az postgres flexible-server update -g $Rg -n $db --admin-password $adminPw -o none

$ownerUrl = "postgresql+psycopg://wakatta_owner:$ownerPw@${dbHost}:5432/wakatta?sslmode=require"
$appUrl = "postgresql+psycopg://wakatta_app:$appPw@${dbHost}:5432/wakatta?sslmode=require"
$adminUrl = "postgresql://${DbAdmin}:$adminPw@${dbHost}:5432/postgres?sslmode=require"

Step "container app ${App}: first boot creates the database, migrates and seeds"
try {
    Invoke-Az containerapp create -n $App -g $Rg --environment $EnvName --image $ref `
        --registry-server "$acr.azurecr.io" --registry-identity $idRes --user-assigned $idRes `
        --target-port 8000 --ingress external --min-replicas 1 --max-replicas 1 --cpu 0.5 --memory 1.0Gi `
        --secrets "database-url=$appUrl" "migration-database-url=$ownerUrl" "pin-pepper=$pepper" `
        "admin-url=$adminUrl" "owner-pw=$ownerPw" "app-pw=$appPw" `
        --env-vars "DATABASE_URL=secretref:database-url" "MIGRATION_DATABASE_URL=secretref:migration-database-url" `
        "PIN_PEPPER=secretref:pin-pepper" "APP_DB_ROLE=wakatta_app" "FRONTEND_ORIGIN=$FrontendOrigins" `
        "LOG_LEVEL=INFO" "AI_MODE=fake" `
        "ADMIN_DATABASE_URL=secretref:admin-url" "ADMIN_USER=$DbAdmin" "OWNER_PASSWORD=secretref:owner-pw" `
        "APP_PASSWORD=secretref:app-pw" "SEED_ON_START=reset" -o none
    $null = Wait-Ready
} finally {
    # Runs even if the first boot failed: never leave the admin credential or the wipe flag on the app.
    Remove-Bootstrap
}
$fqdn = Wait-Ready
Step "done: https://$fqdn/api  (demo: shop SASEBO01, Foreman PIN 246810, Workers PIN 2468)"
