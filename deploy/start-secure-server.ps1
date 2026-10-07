$ErrorActionPreference = 'Stop'

$deployDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $deployDir

$envFile = Join-Path $deployDir 'secure-server.env.ps1'
$jarFile = Join-Path $projectDir 'target\secure-server-0.0.1-SNAPSHOT.jar'
$logDir = Join-Path $deployDir 'logs'
$pidFile = Join-Path $deployDir 'secure-server.pid'

if (Test-Path $pidFile) {
    $existingPid = Get-Content $pidFile -ErrorAction SilentlyContinue

    if ($existingPid -and
        (Get-Process -Id $existingPid -ErrorAction SilentlyContinue)) {
        Write-Host "Secure Server가 이미 실행 중입니다. PID: $existingPid"
        exit
    }

    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
}

. $envFile

$javaExe = Join-Path $env:JAVA_HOME 'bin\java.exe'

if (-not (Test-Path $javaExe)) {
    throw "Java 실행 파일을 찾을 수 없습니다: $javaExe"
}

if (-not (Test-Path $jarFile)) {
    throw "JAR 파일을 찾을 수 없습니다: $jarFile"
}

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw "DB_PASSWORD가 설정되지 않았습니다."
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null

$outLog = Join-Path $logDir 'secure-server.out.log'
$errorLog = Join-Path $logDir 'secure-server.error.log'

$startOptions = @{
    FilePath               = $javaExe
    ArgumentList           = @('-jar', "`"$jarFile`"")
    WorkingDirectory       = $projectDir
    RedirectStandardOutput = $outLog
    RedirectStandardError  = $errorLog
    WindowStyle            = 'Hidden'
    PassThru               = $true
}

$serverProcess = Start-Process @startOptions
$serverProcess.Id | Set-Content -Path $pidFile -Encoding ascii

Write-Host "Secure Server를 백그라운드에서 시작했습니다."
Write-Host "PID: $($serverProcess.Id)"
Write-Host "접속 주소: http://localhost:8081"
Write-Host "로그 폴더: $logDir"