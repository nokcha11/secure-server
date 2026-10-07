$ErrorActionPreference = 'Stop'

$deployDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pidFile = Join-Path $deployDir 'secure-server.pid'

if (-not (Test-Path $pidFile)) {
    Write-Host "[INFO] Server is not running."
    exit
}

$serverPid = (Get-Content $pidFile).Trim()
$serverProcess = Get-Process -Id $serverPid -ErrorAction SilentlyContinue

if (-not $serverProcess) {
    Remove-Item $pidFile -Force
    Write-Host "[INFO] Server is already stopped."
    exit
}

if ($serverProcess.ProcessName -notin @('java', 'javaw')) {
    throw "The saved PID is not a Java process: $serverPid"
}

Stop-Process -Id $serverPid -Force
Remove-Item $pidFile -Force

Write-Host "[OK] Secure Server stopped. PID: $serverPid"