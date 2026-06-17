[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)]
  [string]$BackupFile,
  [string]$Container = "radar-mysql",
  [string]$Database = "personality_radar",
  [string]$User = "radar",
  [string]$Password = "radar123"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $BackupFile)) {
  throw "Backup file not found: $BackupFile"
}

Write-Host "==> Restoring database $Database from $BackupFile"

Get-Content -LiteralPath $BackupFile -Raw | docker exec -i -e "MYSQL_PWD=$Password" $Container mysql --default-character-set=utf8mb4 "-u$User" $Database

if ($LASTEXITCODE -ne 0) {
  throw "Restore failed with exit code $LASTEXITCODE"
}

Write-Host "Restore complete. Verify with: docker exec $Container mysql -u$User -p$Password $Database -e 'SHOW TABLES;'"
