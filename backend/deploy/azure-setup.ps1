# One-time Azure setup for the Wakatta? API (safe to re-run: it skips what exists).
# Registry + managed identity (no expiring tokens), PostgreSQL B1ms, Container Apps, and two jobs that run INSIDE
# Azure: wakatta-dbinit (roles + database) and wakatta-seed (migrate + demo data), so no laptop ever needs port 5432.
# Secrets are generated here, stored only as Container App / Job secrets, and never printed.
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\azure-setup.ps1
. "$PSScriptRoot\common.ps1"

function Wait-Job($name) {
    $exec = Invoke-Az containerapp job start -n $name -g $Rg --query name -o tsv
    foreach ($i in 1..60) {
        Start-Sleep -Seconds 10
        $status = Invoke-Az containerapp job execution show -n $name -g $Rg --job-execution-name $exec `
            --query properties.status -o tsv
        if ($status -eq "Succeeded") { Step "$name succeeded"; return }
        if ($status -eq "Failed") { throw "$name failed. Logs: az containerapp job logs show -n $name -g $Rg --container $name" }
    }
    throw "$name still running after 10 min"
}

Step "providers and resource group"
Invoke-Az config set extension.use_dynamic_install=yes_without_prompt -o none
foreach ($p in "Microsoft.App", "Microsoft.OperationalInsights", "Microsoft.DBforPostgreSQL", "Microsoft.ContainerRegistry") {
    Invoke-Az provider register -n $p --wait -o none
}
Invoke-Az group create -n $Rg -l $Loc -o none

Step "managed identity $Identity"
Invoke-Az identity create -n $Identity -g $Rg -l $Loc -o none
$idRes = Invoke-Az identity show -n $Identity -g $Rg --query id -o tsv
$idPrincipal = Invoke-Az identity show -n $Identity -g $Rg --query principalId -o tsv

$acr = Get-Acr
if (-not $acr) {
    $acr = "wakattaacr" + (Get-Random -Minimum 10000 -Maximum 99999)
    Step "container registry $acr (Basic)"
    Invoke-Az acr create -n $acr -g $Rg -l $Loc --sku Basic --admin-enabled false -o none
}
$acrId = Invoke-Az acr show -n $acr --query id -o tsv
if (-not (Invoke-Az role assignment list --assignee $idPrincipal --scope $acrId --role AcrPull --query "[0].id" -o tsv)) {
    Step "AcrPull for $Identity"
    Invoke-Az role assignment create --assignee-object-id $idPrincipal --assignee-principal-type ServicePrincipal `
        --role AcrPull --scope $acrId -o none
}

$ref = (& "$PSScriptRoot\azure-deploy.ps1" | Select-Object -Last 1)
Step "image $ref"

$db = Get-DbServer
if (-not $db) {
    $db = "wakatta-db-" + (Get-Random -Minimum 10000 -Maximum 99999)
    Step "postgres $db (B1ms, PG16; 5-10 min)"
    Invoke-Az postgres flexible-server create -g $Rg -n $db -l $Loc --tier Burstable --sku-name Standard_B1ms `
        --storage-size 32 --version 16 --admin-user $DbAdmin --admin-password (New-Secret 32) --public-access None `
        --zonal-resiliency Disabled --backup-retention 7 --yes -o none
}
# 0.0.0.0 = only Azure services (the API and the jobs) may connect; no laptop IPs.
Invoke-Az postgres flexible-server firewall-rule create -g $Rg -s $db -n AllowAzureServices `
    --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 -o none
$dbHost = "$db.postgres.database.azure.com"

Step "container apps environment $EnvName"
if (-not (Invoke-Az containerapp env list -g $Rg --query "[?name=='$EnvName'].name" -o tsv)) {
    Invoke-Az containerapp env create -n $EnvName -g $Rg -l $Loc -o none
}

if (Invoke-Az containerapp list -g $Rg --query "[?name=='$App'].name" -o tsv) {
    Step "$App already exists: nothing else to set up (use azure-deploy.ps1 to ship code)"; return
}

# Fresh credentials for this setup. The admin password is reset each run and only the dbinit job holds it.
$adminPw = New-Secret 32; $ownerPw = New-Secret 32; $appPw = New-Secret 32; $pepper = New-Secret 48
Step "reset the database admin password"
Invoke-Az postgres flexible-server update -g $Rg -n $db --admin-password $adminPw -o none

$init = @"
set -e
psql -v ON_ERROR_STOP=1 -d postgres <<'SQL'
DO `$`$ BEGIN
  IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'wakatta_owner') THEN ALTER ROLE wakatta_owner LOGIN PASSWORD '$ownerPw';
  ELSE CREATE ROLE wakatta_owner LOGIN PASSWORD '$ownerPw'; END IF;
  IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'wakatta_app') THEN ALTER ROLE wakatta_app LOGIN PASSWORD '$appPw';
  ELSE CREATE ROLE wakatta_app LOGIN PASSWORD '$appPw'; END IF;
END `$`$;
ALTER ROLE wakatta_app SET statement_timeout = '5s';
ALTER ROLE wakatta_app SET idle_in_transaction_session_timeout = '30s';
GRANT wakatta_owner TO $DbAdmin;
SELECT 'CREATE DATABASE wakatta OWNER wakatta_owner' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'wakatta')\gexec
SQL
psql -v ON_ERROR_STOP=1 -d wakatta -c 'SET ROLE wakatta_owner; REVOKE ALL ON SCHEMA public FROM PUBLIC; GRANT USAGE ON SCHEMA public TO wakatta_app;'
"@ -replace "`r", ""
$initB64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($init))

$ownerUrl = "postgresql+psycopg://wakatta_owner:$ownerPw@${dbHost}:5432/wakatta?sslmode=require"
$appUrl = "postgresql+psycopg://wakatta_app:$appPw@${dbHost}:5432/wakatta?sslmode=require"

Step "job wakatta-dbinit (roles + database, runs inside Azure)"
if (Invoke-Az containerapp job list -g $Rg --query "[?name=='wakatta-dbinit'].name" -o tsv) {
    Invoke-Az containerapp job delete -n wakatta-dbinit -g $Rg --yes -o none
}
Invoke-Az containerapp job create -n wakatta-dbinit -g $Rg --environment $EnvName --trigger-type Manual `
    --replica-timeout 600 --replica-retry-limit 0 --parallelism 1 --replica-completion-count 1 `
    --image postgres:16-alpine --cpu 0.25 --memory 0.5Gi `
    --secrets "admin-pw=$adminPw" "init-script=$initB64" `
    --env-vars "PGHOST=$dbHost" "PGUSER=$DbAdmin" "PGSSLMODE=require" "PGPASSWORD=secretref:admin-pw" "INIT=secretref:init-script" `
    --command "/bin/sh" --args "-c" 'echo $INIT | base64 -d | sh' -o none
Wait-Job wakatta-dbinit
# The admin password only ever lived in this job; drop the job so nothing keeps it.
Invoke-Az containerapp job delete -n wakatta-dbinit -g $Rg --yes -o none

$common = @("PIN_PEPPER=secretref:pin-pepper", "APP_DB_ROLE=wakatta_app", "MIGRATION_DATABASE_URL=secretref:migration-database-url")

Step "job wakatta-seed (migrate + demo data; re-run later to reset the demo)"
Invoke-Az containerapp job create -n wakatta-seed -g $Rg --environment $EnvName --trigger-type Manual `
    --replica-timeout 900 --replica-retry-limit 0 --parallelism 1 --replica-completion-count 1 `
    --image $ref --registry-server "$acr.azurecr.io" --registry-identity $idRes --mi-user-assigned $idRes `
    --cpu 0.5 --memory 1.0Gi `
    --secrets "migration-database-url=$ownerUrl" "pin-pepper=$pepper" `
    --env-vars @common --command "/bin/sh" --args "-c" 'alembic upgrade head && python -m app.seed --reset' -o none
Wait-Job wakatta-seed

Step "container app $App (1 replica: migrations run at start-up)"
Invoke-Az containerapp create -n $App -g $Rg --environment $EnvName --image $ref `
    --registry-server "$acr.azurecr.io" --registry-identity $idRes --user-assigned $idRes `
    --target-port 8000 --ingress external --min-replicas 1 --max-replicas 1 --cpu 0.5 --memory 1.0Gi `
    --secrets "database-url=$appUrl" "migration-database-url=$ownerUrl" "pin-pepper=$pepper" `
    --env-vars "DATABASE_URL=secretref:database-url" @common "FRONTEND_ORIGIN=http://localhost:3000" `
    "DEMO_MODE=true" "LOG_LEVEL=INFO" "AI_MODE=fake" -o none

$fqdn = Invoke-Az containerapp show -n $App -g $Rg --query properties.configuration.ingress.fqdn -o tsv
foreach ($i in 1..30) {
    try { $null = Invoke-RestMethod "https://$fqdn/api/health/ready" -TimeoutSec 10; Step "ready: https://$fqdn"; return }
    catch { Start-Sleep -Seconds 10 }
}
throw "not ready after 5 min. Logs: az containerapp logs show -n $App -g $Rg --tail 50"
