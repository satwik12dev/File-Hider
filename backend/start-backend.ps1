# CypherVault Spring Boot Backend Launcher
$backendDir = $PSScriptRoot
Set-Location $backendDir

# Automatically release port 8080 if already in use
$conn = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($conn) {
    $pids = $conn | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($p in $pids) {
        if ($p -gt 0) {
            Write-Host "Releasing port 8080 (Terminating existing PID $p)..." -ForegroundColor Yellow
            Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
        }
    }
    Start-Sleep -Seconds 1
}

Write-Host "Compiling and assembling Spring Boot 3 dependencies..." -ForegroundColor Cyan

$allJars = Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Recurse -Filter "*.jar" | Where-Object { 
  $_.FullName -notmatch "\\org\\springframework\\.*\\3\.1\." -and 
  $_.FullName -notmatch "\\org\\springframework\\.*\\3\.4\." -and 
  $_.FullName -notmatch "\\org\\springframework\\.*\\3\.5\." -and 
  $_.FullName -notmatch "\\org\\springframework\\.*\\4\." -and 
  $_.FullName -notmatch "\\org\\springframework\\.*\\6\.0\." -and 
  $_.FullName -notmatch "\\org\\springframework\\.*\\6\.2\." -and 
  $_.FullName -notmatch "\\org\\springframework\\.*\\7\." -and 
  $_.FullName -notmatch "\\org\\apache\\tomcat\\.*\\11\." -and 
  $_.FullName -notmatch "3\.2\.18" -and
  $_.FullName -notmatch "commons-logging" -and
  $_.FullName -notmatch "slf4j-api-1\." -and
  $_.FullName -notmatch "slf4j-simple" -and
  $_.FullName -notmatch "slf4j-jdk14"
}

$cpString = "target/classes;" + (($allJars | Select-Object -ExpandProperty FullName) -join ";")
if (!(Test-Path "target")) { New-Item -ItemType Directory -Path "target" | Out-Null }
[System.IO.File]::WriteAllText("target\boot-cp.txt", "-cp`n$cpString`n")

$srcFiles = Get-ChildItem -Recurse -Path "src/main/java/com/filehider" -Filter "*.java" | Select-Object -ExpandProperty FullName
[System.IO.File]::WriteAllLines("target\sources.txt", $srcFiles)

javac --release 21 "@target\boot-cp.txt" -d target/classes "@target\sources.txt"
Copy-Item -Path "src/main/resources/application.properties" -Destination "target/classes/application.properties" -Force

Write-Host "Launching Spring Boot Server on http://localhost:8080 ..." -ForegroundColor Green
java "@target\boot-cp.txt" com.filehider.FileHiderApplication
