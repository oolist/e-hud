param(
    [Parameter(Mandatory = $true)]
    [string]$JavaExe
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$wrapperJar = Join-Path $projectRoot 'gradle\wrapper\gradle-wrapper.jar'
$releaseDir = Join-Path $projectRoot 'releases'
$modVersion = '0.1.2-alpha'
New-Item -ItemType Directory -Force -Path $releaseDir | Out-Null

$matrix = @(
    @{ Minecraft = '1.21';    Fabric = '0.102.0+1.21';    ModMenu = '11.0.4' },
    @{ Minecraft = '1.21.1';  Fabric = '0.116.14+1.21.1'; ModMenu = '11.0.4' },
    @{ Minecraft = '1.21.2';  Fabric = '0.106.1+1.21.2';  ModMenu = '12.0.1' },
    @{ Minecraft = '1.21.3';  Fabric = '0.114.1+1.21.3';  ModMenu = '12.0.1' },
    @{ Minecraft = '1.21.4';  Fabric = '0.119.4+1.21.4';  ModMenu = '13.0.4' },
    @{ Minecraft = '1.21.5';  Fabric = '0.128.2+1.21.5';  ModMenu = '14.0.2' },
    @{ Minecraft = '1.21.6';  Fabric = '0.128.2+1.21.6';  ModMenu = '15.0.2' },
    @{ Minecraft = '1.21.7';  Fabric = '0.129.0+1.21.7';  ModMenu = '15.0.2' },
    @{ Minecraft = '1.21.8';  Fabric = '0.136.1+1.21.8';  ModMenu = '15.0.2' },
    @{ Minecraft = '1.21.9';  Fabric = '0.134.1+1.21.9';  ModMenu = '16.0.1' },
    @{ Minecraft = '1.21.10'; Fabric = '0.138.4+1.21.10'; ModMenu = '16.0.1' },
    @{ Minecraft = '1.21.11'; Fabric = '0.141.5+1.21.11'; ModMenu = '17.0.0' }
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
        $jarName = "e-hud-$($entry.Minecraft)-$modVersion.jar"
        Copy-Item -LiteralPath (Join-Path $projectRoot "build\libs\$jarName") `
            -Destination (Join-Path $releaseDir $jarName) -Force
    }

    $checksums = Get-ChildItem -LiteralPath $releaseDir -Filter "e-hud-*-$modVersion.jar" |
        Sort-Object Name |
        ForEach-Object {
            $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName
            "$($hash.Hash.ToLowerInvariant())  $($_.Name)"
        }
    Set-Content -LiteralPath (Join-Path $releaseDir 'SHA256SUMS.txt') -Value $checksums -Encoding utf8
    Write-Host "All E HUD builds completed."
}
finally {
    Pop-Location
}
