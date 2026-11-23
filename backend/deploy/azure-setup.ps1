# One-time Azure setup for the Wakatta? API. Safe to re-run: it skips what exists, and it stops cleanly where it needs
# the first CI build. Run it twice: once now (infrastructure + CI identity), once after the first image is in the registry.
#   - registry + two managed identities: wakatta-id (the apps pull images) and wakatta-ci (GitHub Actions deploys,
#     via OIDC: no stored passwords or tokens)
#   - the API app (Azure for Students only gets "express" environments: no Jobs, so every container start migrates
#     and the demo seed runs on request, backend/jobs/start.py). The database is Supabase: deploy/use-supabase.ps1
# Secrets are generated here, stored only as Container App secrets, and never printed.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\azure-setup.ps1
. "$PSScriptRoot\common.ps1"
$CiIdentity = "wakatta-ci"

Step "providers and resource group"
Invoke-Az config set extension.use_dynamic_install=yes_without_prompt -o none
foreach ($p in "Microsoft.App", "Microsoft.OperationalInsights", "Microsoft.ContainerRegistry", "Microsoft.ManagedIdentity") {
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
    Step "$App already exists: setup is complete (CI ships code; the database is Supabase: deploy/use-supabase.ps1)"; return
}

# Newest commit-tagged image (never the mutable "latest"). The repository does not exist until CI pushes one.
try { $ref = @(Invoke-Az acr repository show-tags -n $acr --repository $Image --orderby time_desc --top 5 -o tsv) | Where-Object { $_ -and $_ -ne "latest" } | Select-Object -First 1 }
catch { $ref = $null }
if (-not $ref) {
    Step "infrastructure ready. No image yet: push to main so CI builds it, then run this script again."; return
}
$ref = "$acr.azurecr.io/${Image}:$ref"

# The database lives on Supabase: the app starts with placeholder database settings, and use-supabase.ps1 (next)
# creates the roles, migrates, seeds and gives the app the real connection secrets.
Step "container app $App (image $ref)"
Invoke-Az containerapp create -n $App -g $Rg --environment $EnvName --image $ref `
    --registry-server "$acr.azurecr.io" --registry-identity $idRes --user-assigned $idRes `
    --target-port 8000 --ingress external --min-replicas 1 --max-replicas 1 --cpu 0.5 --memory 1.0Gi `
    --secrets "database-url=postgresql+psycopg://unset@unset/unset" "pin-pepper=$(New-Secret 48)" `
    --env-vars "DATABASE_URL=secretref:database-url" "PIN_PEPPER=secretref:pin-pepper" `
    "FRONTEND_ORIGIN=$FrontendOrigins" "LOG_LEVEL=INFO" "AI_MODE=fake" -o none
Step "next: put SUPABASE_DB_URL in backend/.env and run deploy/use-supabase.ps1; then deploy/set-ai-key.ps1"
