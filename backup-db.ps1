$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$destino = Join-Path $PSScriptRoot "backups\booktruck_db_backup_$ts.sql"
New-Item -ItemType Directory -Force -Path (Join-Path $PSScriptRoot "backups") | Out-Null
C:\xampp\mysql\bin\mysqldump.exe -u root --routines --triggers --databases booktruck_db | Out-File -FilePath $destino -Encoding utf8
Write-Output "Backup guardado en $destino"
