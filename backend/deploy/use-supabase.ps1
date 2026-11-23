# Point the API at the Supabase database. Needs SUPABASE_DB_URL in backend/.env: the Session pooler string
# (Supabase -> Connect -> Session pooler, port 5432), e.g.
#   postgresql://postgres.<project-ref>:<password>@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres
# The direct connection is IPv6-only on the free plan and Azure Container Apps is IPv4, so the pooler is required.
# 1. From this machine: create the least-privilege roles (fresh passwords), migrate, load the demo data.
# 2. Store the new connection secrets on the API, restart it on them, and check it answers.
# Secrets live only in memory and in Azure; nothing is printed. Safe to re-run (it rotates the role passwords).
# Usage: powershell -ExecutionPolicy Bypass -File backend\deploy\use-supabase.ps1
. "$PSScriptRoot\common.ps1"
$backend = Split-Path $PSScriptRoot -Parent
$py = Join-Path $backend ".venv\Scripts\python.exe"

$admin = (Read-DotEnv @("SUPABASE_DB_URL")).SUPABASE_DB_URL
if (-not $admin) { throw "SUPABASE_DB_URL is missing in backend/.env" }
$u = [Uri]$admin
$user = $u.UserInfo.Split(":")[0]
if ($user -notmatch '^postgres\.([a-z0-9]+)$' -or $u.Port -ne 5432) {
    throw "use the Session pooler string (user postgres.<project-ref>, port 5432), not the direct connection"
}
$ref = $Matches[1]
$dbHost = $u.Host
if ($u.Query -notmatch 'sslmode=') { $admin += $(if ($u.Query) { "&" } else { "?" }) + "sslmode=require" }

$ownerPw = New-Secret 32; $appPw = New-Secret 32; $pepper = New-Secret 48
# Through the pooler a role signs in as <role>.<project-ref>.
$ownerUrl = "postgresql+psycopg://wakatta_owner.${ref}:$ownerPw@${dbHost}:5432/postgres?sslmode=require"
$appUrl = "postgresql+psycopg://wakatta_app.${ref}:$appPw@${dbHost}:5432/postgres?sslmode=require"

function Invoke-Py([hashtable]$Env, [string[]]$PyArgs) {
    foreach ($k in $Env.Keys) { Set-Item "Env:$k" $Env[$k] }
    try {
        Push-Location $backend
        & $py @PyArgs
        if ($LASTEXITCODE) { throw "python $($PyArgs -join ' ') failed (exit $LASTEXITCODE)" }
    } finally {
        Pop-Location
        foreach ($k in $Env.Keys) { Remove-Item "Env:$k" -ErrorAction SilentlyContinue }
    }
}

Step "roles in Supabase (wakatta_owner creates tables, wakatta_app uses them)"
Invoke-Py @{ ADMIN_DATABASE_URL = $admin; OWNER_PASSWORD = $ownerPw; APP_PASSWORD = $appPw; DB_NAME = "postgres" } @("jobs/dbinit.py")
Step "migrate"
Invoke-Py @{ MIGRATION_DATABASE_URL = $ownerUrl; APP_DB_ROLE = "wakatta_app" } @("-m", "alembic", "upgrade", "head")
Step "load the demo data"
Invoke-Py @{ MIGRATION_DATABASE_URL = $ownerUrl; PIN_PEPPER = $pepper; PYTHONIOENCODING = "utf-8" } @("-m", "app.seed", "--reset")

Step "point the API at Supabase"
Wait-Idle
Invoke-Az containerapp secret set -n $App -g $Rg `
    --secrets "database-url=$appUrl" "migration-database-url=$ownerUrl" "pin-pepper=$pepper" -o none
# Changing a plain setting starts a new revision, which reads the new secrets.
Invoke-Az containerapp update -n $App -g $Rg --set-env-vars "DATABASE_HOST=$dbHost" -o none
$fqdn = Wait-Ready
$roster = Invoke-RestMethod "https://$fqdn/api/auth/roster?code=SASEBO01&role=foreman" -TimeoutSec 20
Step "live on Supabase: roster has $(@($roster.data).Count) Foreman(s) at https://$fqdn"
