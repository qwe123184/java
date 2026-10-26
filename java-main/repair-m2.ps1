# repair-m2.ps1
# Remove corrupt / partial Maven artifacts from the local repository so that
# Maven re-downloads them cleanly on the next build.
# Called by start-all.bat (step 2). Safe: only deletes known-bad marker files
# and incomplete downloads; it never touches source code or settings.

$ErrorActionPreference = 'SilentlyContinue'

$repo = Join-Path $env:USERPROFILE '.m2\repository'
if (-not (Test-Path $repo)) {
    Write-Host "[repair-m2] No .m2 repository found at $repo - nothing to do."
    exit 0
}

Write-Host "[repair-m2] Scanning $repo for corrupt / stale markers ..."

$removed = 0

# ".lastUpdated" markers tell Maven a previous download failed. Leftover ones
# make Maven keep skipping the artifact. Removing them lets it retry.
Get-ChildItem -Path $repo -Recurse -File -Include '*.lastUpdated' | ForEach-Object {
    Remove-Item $_.FullName -Force
    $removed++
}

# Incomplete downloads left by interrupted builds.
Get-ChildItem -Path $repo -Recurse -File -Include '*.part', '*.tmp', '*.lock' | ForEach-Object {
    Remove-Item $_.FullName -Force
    $removed++
}

# Empty folders left behind after the cleanup above.
Get-ChildItem -Path $repo -Recurse -Directory |
    Where-Object { @(Get-ChildItem $_.FullName -Force).Count -eq 0 } |
    Remove-Item -Force -Recurse

Write-Host "[repair-m2] Removed $removed stale file(s). Maven will re-download on next build."
exit 0
