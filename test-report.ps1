param(
    [switch]$NoBuild,
    [switch]$Quiet
)

$passaAqui = "Passa Aqui"
$separator = "=" * 70

Write-Host ""
Write-Host "  $separator" -ForegroundColor Cyan
Write-Host "  Welcome to $passaAqui - Backend Test Runner" -ForegroundColor Cyan
Write-Host "  $separator" -ForegroundColor Cyan
Write-Host ""

$startTime = Get-Date

$mvnArgs = @("clean", "test", "--no-transfer-progress")
if ($Quiet) {
    $mvnArgs += "--quiet"
}

if (-not (Test-Path ".\mvnw.cmd")) {
    Write-Host "  ERROR: mvnw.cmd not found. Run this script from the project root." -ForegroundColor Red
    exit 1
}

Write-Host "  >> Running all tests (unit, integration, security)..." -ForegroundColor Yellow
Write-Host ""

& ".\mvnw.cmd" $mvnArgs
$buildSuccess = $LASTEXITCODE -eq 0
$elapsed = (Get-Date) - $startTime

Write-Host ""
Write-Host "  Generating test report..." -ForegroundColor Yellow

$reportDir = "target/surefire-reports"
if (-not (Test-Path $reportDir)) {
    Write-Host "  ERROR: No test reports found at $reportDir" -ForegroundColor Red
    exit 1
}

$xmlFiles = Get-ChildItem -Path $reportDir -Filter "*.xml" | Where-Object { $_.Name -like "TEST-*.xml" }

$results = @()
$grandTotal = @{ tests = 0; failures = 0; errors = 0; skipped = 0; time = 0.0 }

foreach ($file in $xmlFiles) {
    [xml]$xml = Get-Content $file.FullName
    $ts = $xml.testsuite
    $className = $ts.name
    $shortName = $className -replace '^com\.passaaqui\.backend\.', ''
    $tests = [int]$ts.tests
    $failures = [int]$ts.failures
    $errors = [int]$ts.errors
    $skipped = [int]$ts.skipped
    $time = [double]$ts.time

    $passed = ($failures -eq 0 -and $errors -eq 0) -or ($tests -eq 0 -and $failures -eq 0 -and $errors -eq 0)

    $results += [PSCustomObject]@{
        Class    = $shortName
        Tests    = $tests
        Failures = $failures
        Errors   = $errors
        Skipped  = $skipped
        Time     = $time
        Passed   = $passed
    }

    $grandTotal.tests    += $tests
    $grandTotal.failures += $failures
    $grandTotal.errors   += $errors
    $grandTotal.skipped  += $skipped
    $grandTotal.time     += $time
}

Write-Host ""
Write-Host "  $separator" -ForegroundColor Cyan
Write-Host "  Test Results" -ForegroundColor Cyan
Write-Host "  $separator" -ForegroundColor Cyan
Write-Host ""

foreach ($r in $results) {
    $icon = if ($r.Passed) { "PASS" } else { "FAIL" }
    $color = if ($r.Passed) { "Green" } else { "Red" }

    Write-Host ("  {0,-6}  {1}" -f "[$icon]", $r.Class) -ForegroundColor $color
    Write-Host ("         tests: {0,-4} | failures: {1,-3} | errors: {2,-3} | skipped: {3,-3} | time: {4,6:F2}s" -f $r.Tests, $r.Failures, $r.Errors, $r.Skipped, $r.Time)
    Write-Host ""
}

Write-Host "  $separator" -ForegroundColor DarkGray
Write-Host ("  Total: {0,-5} | Failures: {1,-5} | Errors: {2,-5} | Skipped: {3,-5} | Time: {4,6:F2}s" -f $grandTotal.tests, $grandTotal.failures, $grandTotal.errors, $grandTotal.skipped, $grandTotal.time)
Write-Host ""

$allPassed = $buildSuccess -and $grandTotal.failures -eq 0 -and $grandTotal.errors -eq 0

if ($allPassed) {
    Write-Host "  [PASS] All $($grandTotal.tests) tests completed successfully in $($elapsed.TotalSeconds.ToString('F2'))s" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Some tests failed or build failed ($($grandTotal.failures) failures, $($grandTotal.errors) errors)" -ForegroundColor Red
}

Write-Host ""
exit $(if ($allPassed) { 0 } else { 1 })
