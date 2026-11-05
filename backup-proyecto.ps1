$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$destino = Join-Path $PSScriptRoot "backups\booktruck_proyecto_backup_$ts.zip"
New-Item -ItemType Directory -Force -Path (Join-Path $PSScriptRoot "backups") | Out-Null

$origen = Get-ChildItem $PSScriptRoot -Force | Where-Object { $_.Name -notin @("target", "backups") }
Compress-Archive -Path $origen.FullName -DestinationPath $destino -Force

Write-Output "Backup del proyecto guardado en $destino"
