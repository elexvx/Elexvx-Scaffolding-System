param()
$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root 'backend'
$frontendDir = Join-Path $root 'frontend'
$pidDir = Join-Path $PSScriptRoot '.pids'
New-Item -ItemType Directory -Force -Path $pidDir | Out-Null

function Get-FreeTcpPort([int]$startPort, [int]$maxTries) {
  $start = [Math]::Max(1, $startPort)
  $tries = [Math]::Max(1, $maxTries)
  for ($p = $start; $p -lt ($start + $tries); $p++) {
    try {
      $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $p)
      $listener.Start()
      $listener.Stop()
      return $p
    } catch {
    }
  }
  return $startPort
}

$backendConfigPath = Join-Path $backendDir 'src\main\resources\application.yml'
$backendPort = 8080
$contextPath = '/api'
try {
  $yamlText = Get-Content -Path $backendConfigPath -Raw -ErrorAction SilentlyContinue
  if ($yamlText) {
    if ($yamlText -match 'server:\s*[\r\n]+(?:\s{2,}.*[\r\n]+)*?\s{2,}port:\s*(\d+)') {
      $backendPort = [int]$Matches[1]
    }
    if ($yamlText -match 'server:\s*[\r\n]+(?:\s{2,}.*[\r\n]+)*?\s{2,}context-path:\s*(\S+)') {
      $contextPath = $Matches[1]
    }
  }
} catch {}

$backendPort = Get-FreeTcpPort -startPort $backendPort -maxTries 20

$backendCmd = ('mvn -q -DskipTests -Dspring-boot.run.arguments=--server.port={0} spring-boot:run' -f $backendPort)
$backendProc = Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', $backendCmd -WorkingDirectory $backendDir -WindowStyle Normal -PassThru
Set-Content -Path (Join-Path $pidDir 'backend.pid') -Value $backendProc.Id

$frontendConfigPath = Join-Path $frontendDir 'vite.config.ts'
$frontendPort = 3002
try {
  $cfg = Get-Content -Path $frontendConfigPath -ErrorAction SilentlyContinue
  foreach ($line in $cfg) {
    if ($line -match 'port\s*:\s*(\d+)') {
      $frontendPort = [int]$Matches[1]
      break
    }
  }
} catch {}

$frontendPort = Get-FreeTcpPort -startPort $frontendPort -maxTries 20

$backendUrl = ('http://127.0.0.1:{0}' -f $backendPort)
$frontendCmd = ('set VITE_API_URL={0}&& npm run dev -- --port {1}' -f $backendUrl, $frontendPort)
$frontendProc = Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', $frontendCmd -WorkingDirectory $frontendDir -WindowStyle Normal -PassThru
Set-Content -Path (Join-Path $pidDir 'frontend.pid') -Value $frontendProc.Id

try {
  Start-Sleep -Milliseconds 300
} catch {}

$apiPrefix = '/api'

$localIps = @()
try {
  $localIps = (Get-NetIPAddress | Where-Object { $_.AddressFamily -eq 'IPv4' -and $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' }).IPAddress
} catch {
  try {
    $ipconfig = ipconfig | Select-String -Pattern 'IPv4 Address.*: (\d+\.\d+\.\d+\.\d+)' -AllMatches
    $localIps = @()
    foreach ($match in $ipconfig.Matches) {
      $val = $match.Groups[1].Value
      if ($val -notlike '169.254.*' -and $val -notlike '127.*') { $localIps += $val }
    }
  } catch {}
}
$localIps = $localIps | Sort-Object -Unique

Write-Output ""
Write-Output "Frontend:"
Write-Output ("  Local:   http://localhost:{0}/" -f $frontendPort)
foreach ($ip in $localIps) { Write-Output ("  Network: http://{0}:{1}/" -f $ip, $frontendPort) }
Write-Output ("  Proxy:   {0} -> http://localhost:{1}/" -f $apiPrefix, $backendPort)
Write-Output "Backend:"
Write-Output ("  API:     http://localhost:{0}{1}" -f $backendPort, $contextPath)
Write-Output ""

Write-Output "System starting; backend and frontend launched."
