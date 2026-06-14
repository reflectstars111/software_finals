[CmdletBinding()]
param(
  [string]$ProjectRoot = "D:\software\finals",
  [string]$CaddyExe = "C:\Users\admin\Downloads\caddy_windows_amd64.exe",
  [string]$CloudflaredExe = "C:\Program Files (x86)\cloudflared\cloudflared.exe",
  [string]$TunnelName = "personality-radar",
  [string]$JwtSecret = $env:JWT_SECRET,
  [switch]$SkipNpmInstall,
  [switch]$SkipFrontendBuild,
  [switch]$SkipTunnel
)

$ErrorActionPreference = "Stop"

function Require-Path {
  param([string]$Path, [string]$Label)
  if (-not (Test-Path -LiteralPath $Path)) {
    throw "$Label not found: $Path"
  }
}

function Wait-Http {
  param(
    [string]$Url,
    [string]$Name,
    [int]$TimeoutSeconds = 90
  )
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

function New-Secret {
  $bytes = New-Object byte[] 48
  $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
  try {
    $rng.GetBytes($bytes)
  } finally {
    $rng.Dispose()
  }
  [Convert]::ToBase64String($bytes)
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
        Write-Host "$Name already running with PID $oldPid"
        return $existing
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

$frontendDir = Join-Path $ProjectRoot "frontend"
$infraDir = Join-Path $ProjectRoot "infra"
$deployDir = Join-Path $ProjectRoot "deploy"
$caddyFile = Join-Path $deployDir "Caddyfile"
$runtimeDir = Join-Path $deployDir "runtime"
$logDir = Join-Path $runtimeDir "logs"
$secretFile = Join-Path $runtimeDir "jwt-secret.txt"

Require-Path $ProjectRoot "Project root"
Require-Path $frontendDir "Frontend directory"
Require-Path $infraDir "Infra directory"
Require-Path $caddyFile "Caddyfile"
Require-Path $CaddyExe "Caddy executable"

New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

if ([string]::IsNullOrWhiteSpace($JwtSecret)) {
  if (Test-Path -LiteralPath $secretFile) {
    $JwtSecret = (Get-Content -LiteralPath $secretFile -Raw).Trim()
  } else {
    $JwtSecret = New-Secret
    Set-Content -LiteralPath $secretFile -Value $JwtSecret -Encoding ASCII
  }
}
$env:JWT_SECRET = $JwtSecret

if (-not $SkipFrontendBuild) {
  Push-Location $frontendDir
  try {
    if (-not $SkipNpmInstall -and -not (Test-Path -LiteralPath (Join-Path $frontendDir "node_modules"))) {
      npm install
    }
    npm run build
  } finally {
    Pop-Location
  }
}

Push-Location $infraDir
try {
  docker compose up -d --build
} finally {
  Pop-Location
}

Wait-Http -Url "http://127.0.0.1:8080/actuator/health" -Name "Backend health"

& $CaddyExe validate --config $caddyFile
Start-ManagedProcess `
  -Name "Caddy" `
  -FilePath $CaddyExe `
  -ArgumentList @("run", "--config", $caddyFile) `
  -WorkingDirectory $ProjectRoot `
  -PidFile (Join-Path $runtimeDir "caddy.pid") `
  -StdoutFile (Join-Path $logDir "caddy.out.log") `
  -StderrFile (Join-Path $logDir "caddy.err.log") | Out-Null

Wait-Http -Url "http://127.0.0.1:8090" -Name "Caddy frontend"

if (-not $SkipTunnel) {
  Require-Path $CloudflaredExe "cloudflared executable"
  Start-ManagedProcess `
    -Name "cloudflared tunnel" `
    -FilePath $CloudflaredExe `
    -ArgumentList @("tunnel", "run", $TunnelName) `
    -WorkingDirectory $ProjectRoot `
    -PidFile (Join-Path $runtimeDir "cloudflared.pid") `
    -StdoutFile (Join-Path $logDir "cloudflared.out.log") `
    -StderrFile (Join-Path $logDir "cloudflared.err.log") | Out-Null
}

Write-Host ""
Write-Host "Local temporary server is ready."
Write-Host "Local URL:  http://127.0.0.1:8090"
Write-Host "Public URL: https://app.reflectstars.dev"
Write-Host "Logs:       $logDir"
