<#
Runs the Gephi accessibility audit agent on Windows and zips the results, for the accessibility report.

Usage, from a PowerShell window in this folder:
    powershell -ExecutionPolicy Bypass -File .\measure-windows.ps1
    powershell -ExecutionPolicy Bypass -File .\measure-windows.ps1 -Gephi "D:\Apps\Gephi-0.11.4"

Gephi opens and must stay the active window for about 3 minutes: close NVDA first and don't touch the keyboard or
mouse until the script says it's done. The agent presses some keys itself (Tab, Ctrl+2...) in Gephi's graph view. The result is windows-measurements.zip in this folder.
#>
param([string]$Gephi = "")
$ErrorActionPreference = "Stop"
$kit = $PSScriptRoot
$kitVersion = Join-Path $kit "kit-version.txt"
if (Test-Path $kitVersion) { Write-Host (Get-Content $kitVersion) } else { Write-Warning "No kit-version.txt: old kit?" }
# The Gephi version this kit was made for (filled in by make-windows-kit.sh)
$Version = "{version}"

if (-not $Gephi) {
    # Installed Gephi folders that have a launcher; the one named after this kit's version, else ask
    $roots = @($env:ProgramFiles, ${env:ProgramFiles(x86)}) | Where-Object { $_ }
    $installs = Get-ChildItem -Path $roots -Directory -Filter "Gephi*" -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\gephi64.exe") }
    $match = $installs | Where-Object { $_.Name -match ("(^|[^0-9.])" + [regex]::Escape($Version) + "$") } |
        Select-Object -First 1
    if (-not $match) {
        $list = ($installs | ForEach-Object { $_.FullName }) -join ", "
        throw "Gephi $Version not found in Program Files (found: $list). Pass -Gephi <Gephi $Version folder>"
    }
    $Gephi = $match.FullName
}
$exe = Join-Path $Gephi "bin\gephi64.exe"
if (-not (Test-Path $exe)) { throw "No Gephi launcher at $exe" }
Write-Host "Using $Gephi"

$run = Join-Path $env:TEMP "gephi-a11y-audit"
$out = Join-Path $kit "windows"
Remove-Item -Recurse -Force $run, $out -ErrorAction SilentlyContinue
$prefs = Join-Path $run "userdir\config\Preferences\org\gephi"
New-Item -ItemType Directory -Force (Join-Path $prefs "desktop"), (Join-Path $prefs "welcome"), $out | Out-Null
# A fresh Gephi profile without first-run prompts (analytics consent, update check, welcome screen)
Set-Content -Encoding ASCII (Join-Path $prefs "desktop\branding.properties") `
    "disable_all_tracking=true`r`nanalytics_consent_shown=true`r`ncheck_latest_version=false"
Set-Content -Encoding ASCII (Join-Path $prefs "welcome\screen.properties") "WelcomeScreen_Open_Startup=false"

function Quote([string]$s) { '"' + $s + '"' }
$arguments = @(
    "--userdir", (Quote (Join-Path $run "userdir")),
    "--cachedir", (Quote (Join-Path $run "cachedir")),
    (Quote ("-J-javaagent:" + (Join-Path $kit "a11y-audit-agent.jar"))),
    (Quote ("-J-Da11yaudit.out=" + $out)),
    "-J-Da11yaudit.variant=windows",
    "-J-Da11yaudit.walks=true",
    # Real key presses (java.awt.Robot) in the graph view, a native window that Java key events don't reach;
    # only sent while the graph view has the focus
    "-J-Da11yaudit.robot=true",
    (Quote ("-J-Da11yaudit.graph=" + (Join-Path $kit "les-miserables.gexf"))),
    (Quote ("-J-Da11yaudit.broken=" + (Join-Path $kit "import-issues.gexf"))),
    "-J-Duser.language=en", "-J-Duser.country=US",
    "-J-Dorg.gephi.crashReporter.enabled=false",
    "-J-Dorg.gephi.settingsUpgrader.enabled=false",
    "-J-Dplugin.manager.check.interval=NEVER",
    "-J-Dnetbeans.exception.report.min.level=99999",
    "-J-Dnetbeans.exception.alert.min.level=99999"
)

Write-Host "Starting $exe with the audit agent."
Write-Host "Don't touch the keyboard or mouse until this script says it's done (about 3 minutes)."
Start-Process -FilePath $exe -ArgumentList $arguments | Out-Null
# The launcher can return before Gephi exits: wait for the agent's results file instead (20 minutes at most)
$results = Join-Path $out "measurements.json"
$deadline = (Get-Date).AddMinutes(20)
while (-not (Test-Path $results) -and (Get-Date) -lt $deadline) { Start-Sleep -Seconds 5 }
Start-Sleep -Seconds 5
if (-not (Test-Path $results)) { Write-Warning "No results after 20 minutes; zipping what there is." }

$zip = Join-Path $kit "windows-measurements.zip"
Compress-Archive -Force -Path $out -DestinationPath $zip
Write-Host "Done. Send back $zip"
