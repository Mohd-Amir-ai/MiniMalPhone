# MiniMalPhone — Automated Fetch, Unzip, Build Rotation & Flash Script
param (
    [switch]$NoFlash = $false
)

$repoOwner = "Mohd-Amir-ai"
$repoName = "MiniMalPhone"
$projectRoot = Split-Path -Parent $PSScriptRoot
$buildsDir = Join-Path $projectRoot "builds"
$adbPath = Join-Path $projectRoot "tools\platform-tools\adb.exe"

# 1. Ensure builds directory exists
if (-not (Test-Path $buildsDir)) {
    New-Item -ItemType Directory -Path $buildsDir -Force | Out-Null
}

# 2. Prepare timestamped folder for this build
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$currentBuildDir = Join-Path $buildsDir "build_$timestamp"
New-Item -ItemType Directory -Path $currentBuildDir -Force | Out-Null

$zipUrl = "https://github.com/$repoOwner/$repoName/releases/download/latest/MiniMalPhone-debug.zip"
$zipFile = Join-Path $currentBuildDir "MiniMalPhone-debug.zip"
$apkFile = Join-Path $currentBuildDir "MiniMalPhone-debug.apk"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  MiniMalPhone — Automated Fast Deployment Pipeline" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 3. Download the latest build zip from GitHub Release
Write-Host "`n[1/4] Downloading latest APK package from GitHub..." -ForegroundColor Yellow
try {
    curl.exe -L -o $zipFile $zipUrl
    if ((Test-Path $zipFile) -and (Get-Item $zipFile).Length -gt 1000) {
        Write-Host "  -> Successfully downloaded build package." -ForegroundColor Green
    } else {
        # Fallback: direct apk download if zip not ready yet
        $apkUrl = "https://github.com/$repoOwner/$repoName/releases/download/latest/MiniMalPhone-debug.apk"
        Write-Host "  -> Falling back to direct APK download..." -ForegroundColor Yellow
        curl.exe -L -o $apkFile $apkUrl
    }
} catch {
    Write-Error "Failed to download build asset from GitHub: $_"
}

# 4. Unzip if zip file was downloaded
if (Test-Path $zipFile) {
    Write-Host "`n[2/4] Unzipping application package..." -ForegroundColor Yellow
    try {
        Expand-Archive -Path $zipFile -DestinationPath $currentBuildDir -Force
        Write-Host "  -> Extracted to: $currentBuildDir" -ForegroundColor Green
    } catch {
        Write-Warning "Could not extract zip directly, checking for existing APK."
    }
}

# Locate APK
$foundApk = Get-ChildItem -Path $currentBuildDir -Filter "*.apk" -Recurse | Select-Object -First 1
if (-not $foundApk) {
    Write-Error "No APK file found in $currentBuildDir. Please check GitHub release status."
    exit 1
}
Write-Host "  -> Target APK ready: $($foundApk.FullName)" -ForegroundColor Green

# 5. Rotate Build Folders (Keep only last 3 folders, delete older ones)
Write-Host "`n[3/4] Maintaining build history (Retaining last 3 builds)..." -ForegroundColor Yellow
$allBuildDirs = Get-ChildItem -Path $buildsDir -Directory | Where-Object { $_.Name -like "build_*" } | Sort-Object CreationTime -Descending

if ($allBuildDirs.Count -gt 3) {
    $dirsToDelete = $allBuildDirs | Select-Object -Skip 3
    foreach ($dir in $dirsToDelete) {
        Write-Host "  -> Purging older build: $($dir.Name)" -ForegroundColor DarkGray
        Remove-Item -Path $dir.FullName -Recurse -Force
    }
}
Write-Host "  -> Build history clean. Active builds stored: $([Math]::Min($allBuildDirs.Count, 3))" -ForegroundColor Green

# 6. Flash directly to connected Android device (Samsung F23 / any phone)
Write-Host "`n[4/4] Checking connected phone via ADB..." -ForegroundColor Yellow

if (-not (Test-Path $adbPath)) {
    Write-Warning "ADB not found at $adbPath. Skipping flash step."
    exit 0
}

$devicesOutput = & $adbPath devices
$attachedDevices = $devicesOutput | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices" }

if ($attachedDevices) {
    Write-Host "  -> Device detected! Installing MiniMalPhone on your phone..." -ForegroundColor Cyan
    & $adbPath install -r $foundApk.FullName
    
    Write-Host "  -> Launching MiniMalPhone..." -ForegroundColor Cyan
    & $adbPath shell am start -n "com.minimalphone.launcher/.MainActivity"
    
    Write-Host "`n🎉 SUCCESS: MiniMalPhone is updated and running live on your phone!" -ForegroundColor Green
} else {
    Write-Host "`n  -> No authorized phone detected over USB." -ForegroundColor Yellow
    Write-Host "     1. Plug in your Samsung F23 via USB cable." -ForegroundColor White
    Write-Host "     2. Unlock your phone and accept 'Allow USB Debugging'." -ForegroundColor White
    Write-Host "     3. Run this script again: powershell -File scripts/fetch-and-flash.ps1" -ForegroundColor White
}
