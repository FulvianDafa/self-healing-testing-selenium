# run_sisa_healing.ps1
# Menjalankan SISA 20 SUT Healing yang belum selesai.
# (10 SUT pertama: Amazon s/d Dicoding sudah selesai).

$suts = @(
    "TestEbayHealing",
    "TestEspnHealing",
    "TestEtsyHealing",
    "TestImdbHealing",
    "TestIndeedHealing",
    "TestKatadataHealing",
    "TestKompasHealing",
    "TestNiagahosterHealing",
    "TestOrangeHrmHealing",
    "TestRedditHealing",
    "TestRuangguruHealing",
    "TestRumah123Healing",
    "TestTargetHealing",
    "TestTiketHealing",
    "TestTokopediaHealing",
    "TestTravelokaHealing",
    "TestWalmartHealing",
    "TestWikipediaHealing",
    "TestYahooHealing",
    "TestYoutubeHealing"
)

$results = @()
$logDir  = "results\run_logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$total = $suts.Count
$idx = 0

foreach ($sut in $suts) {
    $idx++
    $logFile = "$logDir\$sut.txt"
    Write-Host ""
    Write-Host "[$idx/$total] Running $sut ..." -ForegroundColor Cyan

    $start = Get-Date
    mvn test "-Dtest=$sut" "-Dheadless=true" 2>&1 | Tee-Object -FilePath $logFile
    $code = $LASTEXITCODE
    $sec = [math]::Round(((Get-Date) - $start).TotalSeconds, 1)

    # Parse hasil
    $sumLine = (Select-String "Tests run:" $logFile | Select-Object -Last 1)
    $sum = if ($sumLine) { $sumLine.Line.Trim() } else { "N/A" }

    $r = if ($sum -match "Tests run: (\d+)") { $Matches[1] } else { "?" }
    $f = if ($sum -match "Failures: (\d+)") { $Matches[1] } else { "?" }
    $e = if ($sum -match "Errors: (\d+)") { $Matches[1] } else { "?" }

    $bld = if ($code -eq 0) { "PASS" } else { "FAIL" }

    $results += [PSCustomObject]@{ SUT=$sut; Build=$bld; Run=$r; Fail=$f; Err=$e; Sec=$sec }

    $color = if ($bld -eq "PASS") { "Green" } else { "Yellow" }
    Write-Host "  => $bld | run=$r fail=$f err=$e | ${sec}s" -ForegroundColor $color
}

Write-Host "`n=== SUMMARY ===" -ForegroundColor Yellow
$results | Format-Table -AutoSize
$results | Export-Csv -Path "results\healing_run_summary_part2.csv" -NoTypeInformation -Encoding UTF8
Write-Host "Summary Part 2 saved to results\healing_run_summary_part2.csv" -ForegroundColor Green
