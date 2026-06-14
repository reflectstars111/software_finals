[CmdletBinding()]
param(
  [string]$ProjectRoot = "D:\software\finals",
  [switch]$KeepDocker
)

$ErrorActionPreference = "Stop"

function Stop-FromPidFile {
  param([string]$Name, [string]$PidFile)

  if (-not (Test-Path -LiteralPath $PidFile)) {
    Write-Host "$Name PID file not found; skipping."
    return
  }

  $pidValue = (Get-Content -LiteralPath $PidFile -Raw).Trim()
  if ($pidValue -match '^\d+$') {
    $process = Get-Process -Id ([int]$pidValue) -ErrorAction SilentlyContinue
    if ($process) {
      Stop-Process -Id $process.Id -Force
      Write-Host "$Name stopped: PID $pidValue"
    } else {
      Write-Host "$Name process not found: PID $pidValue"
    }
  }
  Remove-Item -LiteralPath $PidFile -Force
}

$runtimeDir = Join-Path $ProjectRoot "deploy\runtime"
$infraDir = Join-Path $ProjectRoot "infra"

Stop-FromPidFile -Name "cloudflared tunnel" -PidFile (Join-Path $runtimeDir "cloudflared.pid")
Stop-FromPidFile -Name "Caddy" -PidFile (Join-Path $runtimeDir "caddy.pid")

if (-not $KeepDocker) {
  Push-Location $infraDir
  try {
    docker compose down
  } finally {
    Pop-Location
  }
  Write-Host "Docker Compose services stopped."
} else {
  Write-Host "Docker Compose services kept running."
}
