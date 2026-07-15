param(
    [string]$BackupDir = $env:BACKUP_DIR,
    [int]$KeepDb = 7,
    [int]$KeepUploads = 3
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($BackupDir)) {
    $BackupDir = Join-Path $PSScriptRoot '..\..\backups'
}

if (-not $env:MYSQL_ROOT_PASSWORD -or -not $env:MYSQL_DATABASE) {
    throw 'Set MYSQL_ROOT_PASSWORD and MYSQL_DATABASE before running the backup.'
}

$resolvedBackupDir = [System.IO.Path]::GetFullPath($BackupDir)
[System.IO.Directory]::CreateDirectory($resolvedBackupDir) | Out-Null
$timestamp = [System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId(
    [DateTimeOffset]::UtcNow, 'China Standard Time').ToString('yyyyMMdd-HHmmss')
$databaseArchive = Join-Path $resolvedBackupDir "blogsys-db-$timestamp.sql.gz"
$uploadsArchive = Join-Path $resolvedBackupDir "blogsys-uploads-$timestamp.tar.gz"

function Invoke-DockerArchive {
    param([string[]]$Arguments, [string]$Destination)

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = 'docker'
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.Arguments = (($Arguments | ForEach-Object {
        '"' + $_.Replace('"', '\"') + '"'
    }) -join ' ')

    $process = [System.Diagnostics.Process]::Start($startInfo)
    $file = [System.IO.File]::Open($Destination, [System.IO.FileMode]::CreateNew)
    try {
        $process.StandardOutput.BaseStream.CopyTo($file)
        $file.Flush()
        $errorText = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            throw "Docker backup command failed: $errorText"
        }
    } finally {
        $file.Dispose()
        $process.Dispose()
    }
    if ((Get-Item -LiteralPath $Destination).Length -eq 0) {
        throw "Backup archive is empty: $Destination"
    }
}

try {
    Invoke-DockerArchive -Destination $databaseArchive -Arguments @(
        'compose', 'exec', '-T', '-e', "MYSQL_PWD=$($env:MYSQL_ROOT_PASSWORD)", 'mysql',
        'sh', '-c', "mysqldump --single-transaction --routines --triggers -uroot '$($env:MYSQL_DATABASE)' | gzip -c"
    )
    Invoke-DockerArchive -Destination $uploadsArchive -Arguments @(
        'compose', 'exec', '-T', 'backend', 'tar', '-C', '/app/uploads', '-czf', '-', '.'
    )
} catch {
    Remove-Item -LiteralPath $databaseArchive, $uploadsArchive -Force -ErrorAction SilentlyContinue
    throw
}

Get-ChildItem -LiteralPath $resolvedBackupDir -Filter 'blogsys-db-*.sql.gz' |
    Sort-Object LastWriteTime -Descending | Select-Object -Skip $KeepDb |
    Remove-Item -Force
Get-ChildItem -LiteralPath $resolvedBackupDir -Filter 'blogsys-uploads-*.tar.gz' |
    Sort-Object LastWriteTime -Descending | Select-Object -Skip $KeepUploads |
    Remove-Item -Force

Write-Output "Created $databaseArchive and $uploadsArchive"
