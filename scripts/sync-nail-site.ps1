param(
    [switch]$Clean
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$source = Join-Path $projectRoot 'frontend\nail-site'
$target = Join-Path $projectRoot 'public\nail-site'

if (-not (Test-Path -LiteralPath $source)) {
    throw "Front-end source directory was not found: $source"
}

if ($Clean -and (Test-Path -LiteralPath $target)) {
    Remove-Item -LiteralPath $target -Recurse -Force
}

New-Item -ItemType Directory -Path $target -Force | Out-Null
$copy = robocopy $source $target /E /XD .impeccable .trae '%TEMP%' Save-Animals-main /XF README.md sync.ps1 package-lock.json .codex-preview-server.js new_file.html /NFL /NDL /NJH /NJS /NC /NS
if ($LASTEXITCODE -gt 7) {
    throw "Front-end publish failed with robocopy exit code $LASTEXITCODE"
}

Write-Output "Published nail site: $target"
