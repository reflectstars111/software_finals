[CmdletBinding()]
param(
  [string]$ProjectRoot = "",
  [string]$Container = "radar-mysql",
  [string]$Database = "personality_radar",
  [string]$User = "radar",
  [string]$Password = "radar123",
  [string]$BackendUrl = "http://127.0.0.1:8080",
  [string]$DemoPhone = "13900000001",
  [string]$DemoPassword = "User123456",
  [string]$MavenExe = "D:\allprogram\apache-maven-3.9.16\bin\mvn.cmd",
  [string]$JavaHome = "C:\Program Files\Android\openjdk\jdk-21.0.8",
  [string]$CaddyExe = "C:\Users\admin\Downloads\caddy_windows_amd64.exe",
  [string]$CloudflaredExe = "C:\Program Files (x86)\cloudflared\cloudflared.exe",
  [string]$TunnelName = "personality-radar",
  [switch]$SkipNpmInstall,
  [switch]$SkipFrontendBuild,
  [switch]$SkipDockerBuild,
  [switch]$DisableDockerFallback,
  [switch]$SkipApiVerification,
  [switch]$SkipPublicIngress,
  [switch]$SkipTunnel
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
  $ProjectRoot = Split-Path -Parent $PSScriptRoot
}

$FrontendDir = Join-Path $ProjectRoot "frontend"
$BackendDir = Join-Path $ProjectRoot "backend"
$InfraDir = Join-Path $ProjectRoot "infra"
$DeployDir = Join-Path $ProjectRoot "deploy"
$CaddyFile = Join-Path $DeployDir "Caddyfile"
$RuntimeDir = Join-Path $DeployDir "runtime"
$LogDir = Join-Path $RuntimeDir "logs"
$RuntimeDockerfile = Join-Path $InfraDir "backend-runtime.Dockerfile"

function Require-Path {
  param([string]$Path, [string]$Label)
  if (-not (Test-Path -LiteralPath $Path)) {
    throw "$Label not found: $Path"
  }
}

function Require-Command {
  param([string]$Name)
  if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
    throw "Command not found: $Name"
  }
}

function Assert-DockerDaemon {
  Write-Host ""
  Write-Host "==> Checking Docker daemon"
  $output = & docker version --format "{{.Server.Version}}" 2>&1
  if ($LASTEXITCODE -ne 0) {
    $details = ($output | Out-String).Trim()
    throw "Docker Desktop Linux engine is not running or not reachable. Start Docker Desktop, wait until it shows 'Docker Desktop is running', then rerun this script. Details: $details"
  }
  Write-Host "Docker daemon is reachable: $output"
}

function Invoke-Checked {
  param(
    [string]$Name,
    [scriptblock]$Script
  )
  Write-Host ""
  Write-Host "==> $Name"
  & $Script
  if ($LASTEXITCODE -ne 0) {
    throw "$Name failed with exit code $LASTEXITCODE"
  }
}

function Wait-Http {
  param(
    [string]$Url,
    [string]$Name,
    [int]$TimeoutSeconds = 120
  )
  Write-Host ""
  Write-Host "==> Waiting for $Name"
  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
      if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
        Write-Host "$Name is reachable: $Url"
        return
      }
    } catch {
      Start-Sleep -Seconds 2
    }
  }
  throw "$Name did not become reachable in $TimeoutSeconds seconds: $Url"
}

function Set-TemporaryEnv {
  param(
    [string]$Name,
    [string]$Value
  )
  $oldValue = [Environment]::GetEnvironmentVariable($Name, "Process")
  if ([string]::IsNullOrEmpty($Value)) {
    Remove-Item "Env:$Name" -ErrorAction SilentlyContinue
  } else {
    [Environment]::SetEnvironmentVariable($Name, $Value, "Process")
  }
  return $oldValue
}

function Restore-TemporaryEnv {
  param(
    [string]$Name,
    [AllowNull()][string]$Value
  )
  if ($null -eq $Value) {
    Remove-Item "Env:$Name" -ErrorAction SilentlyContinue
  } else {
    [Environment]::SetEnvironmentVariable($Name, $Value, "Process")
  }
}

function Assert-Equal {
  param(
    [string]$Name,
    [object]$Actual,
    [object]$Expected
  )
  if ($Actual -ne $Expected) {
    throw "$Name expected $Expected but got $Actual"
  }
  Write-Host "OK: $Name = $Actual"
}

function Assert-AtLeast {
  param(
    [string]$Name,
    [int]$Actual,
    [int]$Minimum
  )
  if ($Actual -lt $Minimum) {
    throw "$Name expected at least $Minimum but got $Actual"
  }
  Write-Host "OK: $Name = $Actual"
}

function Get-DatabaseCount {
  param(
    [string]$Sql
  )

  $raw = & docker exec -e "MYSQL_PWD=$Password" $Container mysql `
    --default-character-set=utf8mb4 `
    --batch `
    --skip-column-names `
    "-u$User" `
    $Database `
    -e $Sql
  if ($LASTEXITCODE -ne 0) {
    throw "Database count query failed: $Sql"
  }

  $value = ($raw | Select-Object -Last 1).ToString().Trim()
  $count = 0
  if (-not [int]::TryParse($value, [ref]$count)) {
    throw "Database count query returned an invalid value '$value': $Sql"
  }
  return $count
}

function Start-ManagedProcess {
  param(
    [string]$Name,
    [string]$FilePath,
    [string[]]$ArgumentList,
    [string]$WorkingDirectory,
    [string]$PidFile,
    [string]$StdoutFile,
    [string]$StderrFile
  )

  if (Test-Path -LiteralPath $PidFile) {
    $oldPid = (Get-Content -LiteralPath $PidFile -Raw).Trim()
    if ($oldPid -match '^\d+$') {
      $existing = Get-Process -Id ([int]$oldPid) -ErrorAction SilentlyContinue
      if ($existing) {
        $expectedPath = (Resolve-Path -LiteralPath $FilePath).Path
        if ($existing.Path -and $existing.Path.Equals($expectedPath, [System.StringComparison]::OrdinalIgnoreCase)) {
          Write-Host "$Name already running with PID $oldPid"
          return $existing
        }
        Write-Warning "Ignoring stale $Name PID file. PID $oldPid belongs to another process."
      }
    }
    Remove-Item -LiteralPath $PidFile -Force
  }

  $process = Start-Process `
    -FilePath $FilePath `
    -ArgumentList $ArgumentList `
    -WorkingDirectory $WorkingDirectory `
    -RedirectStandardOutput $StdoutFile `
    -RedirectStandardError $StderrFile `
    -WindowStyle Hidden `
    -PassThru
  Set-Content -LiteralPath $PidFile -Value $process.Id -Encoding ASCII
  Write-Host "$Name started with PID $($process.Id)"
  return $process
}

function Assert-ProcessAlive {
  param(
    [string]$Name,
    [System.Diagnostics.Process]$Process,
    [string]$StderrFile
  )

  Start-Sleep -Seconds 2
  $running = Get-Process -Id $Process.Id -ErrorAction SilentlyContinue
  if (-not $running) {
    $tail = ""
    if (Test-Path -LiteralPath $StderrFile) {
      $tail = (Get-Content -LiteralPath $StderrFile -Tail 20) -join [Environment]::NewLine
    }
    throw "$Name exited immediately. Recent stderr: $tail"
  }
}

function Wait-TunnelActive {
  param(
    [int]$TimeoutSeconds = 60
  )

  Write-Host ""
  Write-Host "==> Waiting for Cloudflare Tunnel connector"
  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  $lastOutput = ""
  while ((Get-Date) -lt $deadline) {
    $previousErrorActionPreference = $ErrorActionPreference
    try {
      # cloudflared writes update notices to stderr even when the command succeeds.
      $ErrorActionPreference = "Continue"
      $output = & $CloudflaredExe tunnel info $TunnelName 2>&1
      $cloudflaredExitCode = $LASTEXITCODE
    } finally {
      $ErrorActionPreference = $previousErrorActionPreference
    }
    $lastOutput = ($output | Out-String).Trim()
    if ($cloudflaredExitCode -eq 0 -and $lastOutput -notmatch "does not have any active connection") {
      Write-Host "Cloudflare Tunnel connector is active."
      return
    }
    Start-Sleep -Seconds 2
  }

  throw "Cloudflare Tunnel did not become active in $TimeoutSeconds seconds. Last status: $lastOutput"
}

function Start-PublicIngress {
  Require-Path $DeployDir "Deploy directory"
  Require-Path $CaddyFile "Caddyfile"
  Require-Path $CaddyExe "Caddy executable"
  New-Item -ItemType Directory -Path $RuntimeDir -Force | Out-Null
  New-Item -ItemType Directory -Path $LogDir -Force | Out-Null

  Invoke-Checked "Validate Caddy config" {
    & $CaddyExe validate --config $CaddyFile
  }

  $caddyErr = Join-Path $LogDir "caddy.err.log"
  Start-ManagedProcess `
    -Name "Caddy" `
    -FilePath $CaddyExe `
    -ArgumentList @("run", "--config", $CaddyFile) `
    -WorkingDirectory $ProjectRoot `
    -PidFile (Join-Path $RuntimeDir "caddy.pid") `
    -StdoutFile (Join-Path $LogDir "caddy.out.log") `
    -StderrFile $caddyErr | Out-Null

  Wait-Http -Url "http://127.0.0.1:8090" -Name "Caddy frontend"

  if (-not $SkipTunnel) {
    Require-Path $CloudflaredExe "cloudflared executable"
    $cloudflaredErr = Join-Path $LogDir "cloudflared.err.log"
    $cloudflared = Start-ManagedProcess `
      -Name "cloudflared tunnel" `
      -FilePath $CloudflaredExe `
      -ArgumentList @("tunnel", "run", $TunnelName) `
      -WorkingDirectory $ProjectRoot `
      -PidFile (Join-Path $RuntimeDir "cloudflared.pid") `
      -StdoutFile (Join-Path $LogDir "cloudflared.out.log") `
      -StderrFile $cloudflaredErr
    Assert-ProcessAlive -Name "cloudflared tunnel" -Process $cloudflared -StderrFile $cloudflaredErr
    Wait-TunnelActive
  }
}

function Invoke-DockerComposeRefresh {
  Push-Location $InfraDir
  try {
    Write-Host ""
    Write-Host "==> Rebuild and recreate Docker services"
    docker compose up -d --build --force-recreate
    if ($LASTEXITCODE -eq 0) {
      return
    }

    if ($DisableDockerFallback) {
      throw "Rebuild and recreate Docker services failed with exit code $LASTEXITCODE"
    }

    Assert-DockerDaemon
    Write-Warning "Docker compose build failed, likely because the remote registry or mirror is unavailable."
    Write-Warning "Fallback: build backend jar locally, build a runtime-only backend image, then recreate services without compose build."
  } finally {
    Pop-Location
  }

  Require-Path $MavenExe "Maven executable"
  Require-Path $RuntimeDockerfile "Runtime backend Dockerfile"
  Require-Path $JavaHome "JAVA_HOME"

  $oldJavaHome = Set-TemporaryEnv -Name "JAVA_HOME" -Value $JavaHome
  $oldPath = [Environment]::GetEnvironmentVariable("Path", "Process")
  [Environment]::SetEnvironmentVariable("Path", "$JavaHome\bin;$oldPath", "Process")
  try {
    Push-Location $BackendDir
    try {
      Invoke-Checked "Build backend jar with local Maven" {
        & $MavenExe "-DskipTests" "clean" "package"
      }
    } finally {
      Pop-Location
    }
  } finally {
    Restore-TemporaryEnv -Name "JAVA_HOME" -Value $oldJavaHome
    Restore-TemporaryEnv -Name "Path" -Value $oldPath
  }

  $oldBuildKit = Set-TemporaryEnv -Name "DOCKER_BUILDKIT" -Value "0"
  try {
    Invoke-Checked "Build backend runtime image from local jar" {
      docker build --pull=false -t radar-backend-runtime -f $RuntimeDockerfile $BackendDir
    }
  } finally {
    Restore-TemporaryEnv -Name "DOCKER_BUILDKIT" -Value $oldBuildKit
  }

  Push-Location $InfraDir
  try {
    Invoke-Checked "Recreate Docker services from local images" {
      docker compose up -d --no-build --force-recreate
    }
  } finally {
    Pop-Location
  }
}

Require-Path $ProjectRoot "Project root"
Require-Path $FrontendDir "Frontend directory"
Require-Path $BackendDir "Backend directory"
Require-Path $InfraDir "Infra directory"
Require-Command docker
Assert-DockerDaemon

# Auto-generate JWT_SECRET if not already set (cryptographically secure)
if (-not (Test-Path env:JWT_SECRET) -or [string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
  $bytes = New-Object byte[] 48
  $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
  $rng.GetBytes($bytes)
  $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
  Write-Host "JWT_SECRET auto-generated (48 bytes, Base64)"
}

if (-not $SkipFrontendBuild) {
  Require-Command npm
  Push-Location $FrontendDir
  try {
    if (-not $SkipNpmInstall -and -not (Test-Path -LiteralPath (Join-Path $FrontendDir "node_modules"))) {
      Invoke-Checked "Install frontend dependencies" { npm install }
    }
    Invoke-Checked "Build latest frontend dist" { npm run build }
  } finally {
    Pop-Location
  }
}

if (-not $SkipDockerBuild) {
  Invoke-DockerComposeRefresh
}

Wait-Http -Url "$BackendUrl/actuator/health" -Name "Backend health"

$reactivateSql = @"
UPDATE users
SET active = 1,
    failed_login_attempts = 0,
    locked_until = NULL
WHERE phone IN ('13800000000', '13900000001', '13900000002');

SELECT phone, display_name, role, active, failed_login_attempts, locked_until
FROM users
WHERE phone IN ('13800000000', '13900000001', '13900000002')
ORDER BY phone;
"@

Invoke-Checked "Reactivate default users" {
  docker exec -e "MYSQL_PWD=$Password" $Container mysql --default-character-set=utf8mb4 "-u$User" $Database -e $reactivateSql
}

$seedSql = @"
SELECT 'active_questions' AS item, COUNT(*) AS count FROM question WHERE active = 1
UNION ALL SELECT 'active_recommendations', COUNT(*) FROM recommendation_item WHERE active = 1
UNION ALL SELECT 'active_rules', COUNT(*) FROM recommendation_rule WHERE active = 1;

SELECT type, COUNT(*) AS count
FROM question
WHERE active = 1
GROUP BY type
ORDER BY type;

SELECT scene, COUNT(*) AS count
FROM recommendation_item
WHERE active = 1
GROUP BY scene
ORDER BY scene;

SELECT tag, label, weight, active
FROM recommendation_rule
ORDER BY tag;
"@

Invoke-Checked "Show seed data status" {
  docker exec -e "MYSQL_PWD=$Password" $Container mysql --default-character-set=utf8mb4 "-u$User" $Database -e $seedSql
}

if (-not $SkipApiVerification) {
  Write-Host ""
  Write-Host "==> Verify latest API data"
  $loginBody = @{
    phone = $DemoPhone
    password = $DemoPassword
  } | ConvertTo-Json -Compress
  $login = Invoke-RestMethod -Method Post -Uri "$BackendUrl/api/auth/login" -ContentType "application/json" -Body $loginBody -TimeoutSec 15
  $headers = @{ Authorization = "Bearer $($login.data.token)" }

  $questionTypes = @("personality", "food", "travel", "social")
  foreach ($type in $questionTypes) {
    $expectedCount = Get-DatabaseCount "SELECT COUNT(*) FROM question WHERE active = 1 AND type = UPPER('$type');"
    Assert-AtLeast "database.questions.$type count" $expectedCount 1
    $response = Invoke-RestMethod -Uri "$BackendUrl/api/questions?type=$type" -Headers $headers -TimeoutSec 15
    $items = @($response.data)
    Assert-Equal "questions.$type count" $items.Count $expectedCount
    if ($type -eq "personality") {
      $firstQuestionId = [long]$items[0].id
      $expectedOptionCount = Get-DatabaseCount "SELECT COUNT(*) FROM question_option WHERE question_id = $firstQuestionId;"
      Assert-AtLeast "database.questions.personality first option count" $expectedOptionCount 2
      Assert-Equal "questions.personality first option count" @($items[0].options).Count $expectedOptionCount
      $weightCount = @($items[0].options[0].weights.PSObject.Properties).Count
      Assert-AtLeast "questions.personality first option weight dimensions" $weightCount 1
    }
  }

  $recommendationScenes = @("food", "travel", "outfit", "career", "social")
  foreach ($scene in $recommendationScenes) {
    $expectedCount = Get-DatabaseCount "SELECT COUNT(*) FROM recommendation_item WHERE active = 1 AND scene = UPPER('$scene');"
    Assert-AtLeast "database.recommendations.$scene count" $expectedCount 1
    $response = Invoke-RestMethod -Uri "$BackendUrl/api/recommendations?scene=$scene" -Headers $headers -TimeoutSec 15
    $items = @($response.data)
    Assert-AtLeast "recommendations.$scene response count" $items.Count 1
  }
}

if (-not $SkipPublicIngress) {
  Start-PublicIngress
}

Write-Host ""
Write-Host "Refresh complete. Local code, frontend dist, backend container, default users, seed data, API checks, Caddy, and Cloudflare Tunnel are up to date."
Write-Host "Local URL:  http://127.0.0.1:8090"
Write-Host "Public URL: https://app.reflectstars.dev"
Write-Host "Logs:       $LogDir"
