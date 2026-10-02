$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Force artifacts | Out-Null
    & .\gradlew.bat build runGameTestServer --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Build or GameTest failed.' }
    $smokeWorld = Join-Path $projectRoot 'run/saves/NZGE-SmokeWorld'
    if (-not (Test-Path -LiteralPath $smokeWorld)) {
        New-Item -ItemType Directory -Force (Split-Path -Parent $smokeWorld) | Out-Null
        Copy-Item -LiteralPath (Join-Path $projectRoot 'run/world') -Destination $smokeWorld -Recurse
    }
    & .\gradlew.bat runClientSmoke -PsmokeWorld --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Client smoke test failed.' }
} finally {
    Pop-Location
}
