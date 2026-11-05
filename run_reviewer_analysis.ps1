$ErrorActionPreference = "Continue"

# The 23 research SUTs are the test classes whose per-SUT healing logs contained
# data in the pre-rerun snapshot archived on 2026-10-02.
$suts = @(
    @{ Sut = "amazon";      Class = "TestAmazonHealing" },
    @{ Sut = "bhinneka";    Class = "TestBhinnekaHealing" },
    @{ Sut = "bing";        Class = "TestBingHealing" },
    @{ Sut = "bisnis";      Class = "TestBisnisHealing" },
    @{ Sut = "cekresi";     Class = "TestCekresiHealing" },
    @{ Sut = "cnnindo";     Class = "TestCnnIndoHealing" },
    @{ Sut = "dicoding";    Class = "TestDicodingHealing" },
    @{ Sut = "ebay";        Class = "TestEbayHealing" },
    @{ Sut = "indeed";      Class = "TestIndeedHealing" },
    @{ Sut = "katadata";    Class = "TestKatadataHealing" },
    @{ Sut = "kompas";      Class = "TestKompasHealing" },
    @{ Sut = "niagahoster"; Class = "TestNiagahosterHealing" },
    @{ Sut = "orangehrm";   Class = "TestOrangeHrmHealing" },
    @{ Sut = "rumah123";    Class = "TestRumah123Healing" },
    @{ Sut = "saucedemo";   Class = "TestSauceDemoHealing" },
    @{ Sut = "target";      Class = "TestTargetHealing" },
    @{ Sut = "tiket";       Class = "TestTiketHealing" },
    @{ Sut = "tokopedia";   Class = "TestTokopediaHealing" },
    @{ Sut = "traveloka";   Class = "TestTravelokaHealing" },
    @{ Sut = "walmart";     Class = "TestWalmartHealing" },
    @{ Sut = "wikipedia";   Class = "TestWikipediaHealing" },
    @{ Sut = "yahoo";       Class = "TestYahooHealing" },
    @{ Sut = "youtube";     Class = "TestYoutubeHealing" }
)

$workspace = (Resolve-Path -LiteralPath ".").Path
$resultsDir = (Resolve-Path -LiteralPath ".\results").Path
$runLogDir = Join-Path $resultsDir "reviewer_run_logs"
New-Item -ItemType Directory -Force -Path $runLogDir | Out-Null

$summary = @()
$index = 0

foreach ($entry in $suts) {
    $index++
    $sut = $entry.Sut
    $class = $entry.Class
    $sourceFile = Join-Path $workspace "src\test\java\com\fulvian\tests\$class.java"
    $healingFile = Join-Path $resultsDir "${sut}_healing_log.csv"
    $candidateFile = Join-Path $resultsDir "${sut}_candidates_log.csv"
    $runLog = Join-Path $runLogDir "${class}.txt"

    if (-not $sourceFile.StartsWith($workspace, [System.StringComparison]::OrdinalIgnoreCase) -or
        -not $healingFile.StartsWith($resultsDir, [System.StringComparison]::OrdinalIgnoreCase) -or
        -not $candidateFile.StartsWith($resultsDir, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Resolved path escaped the intended workspace/results directory."
    }

    $source = Get-Content -Raw -LiteralPath $sourceFile
    $designed = ([regex]::Matches($source, '(?m)^\s*@Test\b')).Count

    # Old data was already copied to archive/pre_reviewer_rerun_20261002.
    # Remove only the exact current-run outputs so stale candidate rows cannot mix.
    if (Test-Path -LiteralPath $healingFile) {
        Remove-Item -LiteralPath $healingFile -Force
    }
    if (Test-Path -LiteralPath $candidateFile) {
        Remove-Item -LiteralPath $candidateFile -Force
    }

    Write-Host ("[{0}/{1}] {2} ({3} designed scenarios)" -f $index, $suts.Count, $class, $designed) -ForegroundColor Cyan
    $started = Get-Date
    & mvn -q test "-Dtest=$class" "-Dheadless=true" 2>&1 | Out-File -LiteralPath $runLog -Encoding utf8
    $exitCode = $LASTEXITCODE
    $duration = [math]::Round(((Get-Date) - $started).TotalSeconds, 1)

    $runText = if (Test-Path -LiteralPath $runLog) {
        Get-Content -Raw -LiteralPath $runLog
    } else {
        ""
    }
    $summaryMatches = [regex]::Matches(
        $runText,
        'Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)'
    )
    if ($summaryMatches.Count -gt 0) {
        $last = $summaryMatches[$summaryMatches.Count - 1]
        $executed = [int]$last.Groups[1].Value
        $failures = [int]$last.Groups[2].Value
        $errors = [int]$last.Groups[3].Value
        $skipped = [int]$last.Groups[4].Value
    } else {
        $executed = 0
        $failures = 0
        $errors = 0
        $skipped = 0

        # Maven -q does not print a textual summary for successful classes.
        # Surefire XML is authoritative in that case.
        $xmlReport = Join-Path $workspace "target\surefire-reports\TEST-com.fulvian.tests.$class.xml"
        if (Test-Path -LiteralPath $xmlReport) {
            try {
                [xml]$surefire = Get-Content -Raw -LiteralPath $xmlReport
                $executed = [int]$surefire.testsuite.tests
                $failures = [int]$surefire.testsuite.failures
                $errors = [int]$surefire.testsuite.errors
                $skipped = [int]$surefire.testsuite.skipped
            } catch {
                # Leave zero values; reviewer_analysis.py performs the same fallback.
            }
        }
    }

    $evidence = @()
    if ($runText -match 'CAPTCHA|captcha') { $evidence += "CAPTCHA" }
    if ($runText -match 'Access Denied|ACCESS DENIED|403 Forbidden') { $evidence += "access_denied" }
    if ($runText -match 'ERR_CONNECTION|ERR_NAME|ERR_TIMED_OUT|UnknownHost|ConnectException') { $evidence += "network_or_site" }
    if ($runText -match 'TimeoutException|Expected condition failed|timed out') { $evidence += "element_or_page_timeout" }
    if ($runText -match 'Precondition|precondition|Elemen target asli tidak ditemukan') { $evidence += "precondition" }
    if ($runText -match 'NoSuchElementException|no such element') { $evidence += "element_not_rendered" }
    if ($evidence.Count -eq 0 -and $exitCode -ne 0) { $evidence += "test_failure_review_log" }
    if ($evidence.Count -eq 0) { $evidence += "none" }

    $summary += [PSCustomObject]@{
        sut = $sut
        test_class = $class
        scenario_designed = $designed
        scenario_executed = $executed
        maven_failures = $failures
        maven_errors = $errors
        maven_skipped = $skipped
        exit_code = $exitCode
        duration_seconds = $duration
        healing_log = "results/${sut}_healing_log.csv"
        candidate_log = "results/${sut}_candidates_log.csv"
        run_log = "results/reviewer_run_logs/${class}.txt"
        evidence = ($evidence | Select-Object -Unique) -join ";"
    }

    $color = if ($exitCode -eq 0) { "Green" } else { "Yellow" }
    Write-Host ("  exit={0} tests={1} failures={2} errors={3} time={4}s evidence={5}" -f
        $exitCode, $executed, $failures, $errors, $duration, (($evidence | Select-Object -Unique) -join ";")) -ForegroundColor $color
}

$summaryPath = Join-Path $resultsDir "reviewer_run_summary.csv"
$summary | Export-Csv -LiteralPath $summaryPath -NoTypeInformation -Encoding utf8
Write-Host "Execution summary: $summaryPath" -ForegroundColor Green

python reviewer_analysis.py
exit $LASTEXITCODE
