param(
    [Parameter(Mandatory = $true)]
    [string]$JavaExe
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$wrapperJar = Join-Path $projectRoot 'gradle\wrapper\gradle-wrapper.jar'
$releaseDir = Join-Path $projectRoot 'releases'
New-Item -ItemType Directory -Force -Path $releaseDir | Out-Null

$matrix = @(
    @{ Minecraft = '26.1'; Fabric = '0.145.1+26.1'; ModMenu = '18.0.0' },
    @{ Minecraft = '26.2'; Fabric = '0.155.2+26.2'; ModMenu = '20.0.1' }
)

Push-Location $projectRoot
try {
    foreach ($entry in $matrix) {
        Write-Host "Building E HUD for Minecraft $($entry.Minecraft)..."
        & $JavaExe '-Dorg.gradle.appname=gradlew' '-classpath' $wrapperJar `
            'org.gradle.wrapper.GradleWrapperMain' 'clean' 'build' `
            "-Pminecraft_version=$($entry.Minecraft)" `
            "-Pfabric_version=$($entry.Fabric)" `
            "-Pmodmenu_version=$($entry.ModMenu)" '--no-daemon'
        if ($LASTEXITCODE -ne 0) {
            throw "Build failed for Minecraft $($entry.Minecraft)."
        }
        $jarName = "e-hud-$($entry.Minecraft)-0.1.0.jar"
        Copy-Item -LiteralPath (Join-Path $projectRoot "build\libs\$jarName") `
            -Destination (Join-Path $releaseDir $jarName) -Force
    }

    $checksums = Get-ChildItem -LiteralPath $releaseDir -Filter 'e-hud-*.jar' |
        Sort-Object Name |
        ForEach-Object {
            $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName
            "$($hash.Hash.ToLowerInvariant())  $($_.Name)"
        }
    Set-Content -LiteralPath (Join-Path $releaseDir 'SHA256SUMS.txt') -Value $checksums -Encoding utf8
    Write-Host "Minecraft 26.x builds completed."
}
finally {
    Pop-Location
}
