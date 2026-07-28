param(
    [string]$ReleaseDir
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrWhiteSpace($ReleaseDir)) {
    $ReleaseDir = Join-Path $projectRoot 'releases'
}
$expectedVersions = @(
    '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4', '1.21.5',
    '1.21.6', '1.21.7', '1.21.8', '1.21.9', '1.21.10', '1.21.11'
)
$modVersion = '0.1.1-alpha'

Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($version in $expectedVersions) {
    $jarName = "e-hud-$version-$modVersion.jar"
    $jarPath = Join-Path $ReleaseDir $jarName
    if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
        throw "Missing release: $jarName"
    }
    $archive = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
    try {
        $required = @(
            'fabric.mod.json',
            'assets/ehud/icon.png',
            'dev/oolist/ehud/EHud.class',
            'dev/oolist/ehud/client/EHudClient.class',
            'dev/oolist/ehud/client/compat/EHudModMenu.class'
        )
        $names = $archive.Entries.FullName
        foreach ($entry in $required) {
            if ($names -notcontains $entry) {
                throw "$jarName is missing $entry"
            }
        }
        $metadataEntry = $archive.Entries | Where-Object FullName -eq 'fabric.mod.json'
        $reader = New-Object System.IO.StreamReader($metadataEntry.Open())
        try {
            $metadata = $reader.ReadToEnd() | ConvertFrom-Json
        }
        finally {
            $reader.Dispose()
        }
        if ($metadata.id -ne 'ehud') {
            throw "$jarName has the wrong mod id."
        }
        if ($metadata.version -ne $modVersion) {
            throw "$jarName declares mod version '$($metadata.version)' instead of '$modVersion'."
        }
        if ($metadata.depends.minecraft -ne "~$version") {
            throw "$jarName declares Minecraft '$($metadata.depends.minecraft)' instead of '~$version'."
        }
    }
    finally {
        $archive.Dispose()
    }
}

$checksumFile = Join-Path $ReleaseDir 'SHA256SUMS.txt'
if (-not (Test-Path -LiteralPath $checksumFile -PathType Leaf)) {
    throw 'Missing SHA256SUMS.txt'
}
$checksumLines = Get-Content -LiteralPath $checksumFile
foreach ($version in $expectedVersions) {
    $jarName = "e-hud-$version-$modVersion.jar"
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $ReleaseDir $jarName)).Hash.ToLowerInvariant()
    if ($checksumLines -notcontains "$actual  $jarName") {
        throw "Checksum mismatch for $jarName"
    }
}

Write-Host "Verified $($expectedVersions.Count) E HUD release jars and checksums."
