param(
  [string]$Container = "radar-mysql",
  [string]$Database = "personality_radar",
  [string]$User = "radar",
  [string]$Password = "radar123",
  [string]$OutputDir = ".\backups"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $OutputDir)) {
  New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$target = Join-Path $OutputDir "$Database-$timestamp.sql"

docker exec $Container mysqldump "-u$User" "-p$Password" --databases $Database | Set-Content -LiteralPath $target -Encoding UTF8

Write-Host "Backup written to $target"
