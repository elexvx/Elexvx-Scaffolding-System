param()
$pidDir = Join-Path $PSScriptRoot '.pids'
$backendFile = Join-Path $pidDir 'backend.pid'
$frontendFile = Join-Path $pidDir 'frontend.pid'

if (Test-Path $backendFile) {
  try {
    $pid = Get-Content $backendFile | Select-Object -First 1
    if ($pid) { Stop-Process -Id [int]$pid -Force -ErrorAction SilentlyContinue }
  } catch {}
  Remove-Item $backendFile -Force -ErrorAction SilentlyContinue
}

if (Test-Path $frontendFile) {
  try {
    $pid = Get-Content $frontendFile | Select-Object -First 1
    if ($pid) { Stop-Process -Id [int]$pid -Force -ErrorAction SilentlyContinue }
  } catch {}
  Remove-Item $frontendFile -Force -ErrorAction SilentlyContinue
}

Write-Output "System stopped. If residual processes exist, please end manually."
