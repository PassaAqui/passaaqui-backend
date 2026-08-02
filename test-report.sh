#!/usr/bin/env bash
set -euo pipefail

SEPARATOR="================================================================"

echo ""
echo "  $SEPARATOR"
echo "  Welcome to Passa Aqui - Backend Test Runner"
echo "  $SEPARATOR"
echo ""

START_TIME=$(date +%s)

if [ ! -f "./mvnw" ] && [ ! -f "./mvnw.cmd" ]; then
    echo "  ERROR: mvnw not found. Run this script from the project root."
    exit 1
fi

MVNW="./mvnw"
[ -f "./mvnw.cmd" ] && MVNW="./mvnw.cmd"

echo "  >> Running all tests (unit, integration, security)..."
echo ""

"$MVNW" clean test --no-transfer-progress 2>&1
BUILD_SUCCESS=$?

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo ""
echo "  Generating test report..."
echo ""

REPORT_DIR="target/surefire-reports"
if [ ! -d "$REPORT_DIR" ]; then
    echo "  ERROR: No test reports found at $REPORT_DIR"
    exit 1
fi

TOTAL_TESTS=0
TOTAL_FAILURES=0
TOTAL_ERRORS=0
TOTAL_SKIPPED=0
TOTAL_TIME=0.0

echo "  $SEPARATOR"
echo "  Test Results"
echo "  $SEPARATOR"
echo ""

for file in "$REPORT_DIR"/TEST-*.xml; do
    [ -f "$file" ] || continue

    CLASS=$(xmlstarlet sel -t -v "/testsuite/@name" "$file" 2>/dev/null || python3 -c "
import xml.etree.ElementTree as ET
root = ET.parse('$file').getroot()
print(root.get('name'))
" 2>/dev/null || true)

    [ -z "$CLASS" ] && continue

    SHORT="${CLASS#com.passaaqui.backend.}"

    TESTS=$(xmlstarlet sel -t -v "/testsuite/@tests" "$file" 2>/dev/null || python3 -c "
import xml.etree.ElementTree as ET
root = ET.parse('$file').getroot()
print(root.get('tests'))
" 2>/dev/null || echo 0)

    FAILURES=$(xmlstarlet sel -t -v "/testsuite/@failures" "$file" 2>/dev/null || python3 -c "
import xml.etree.ElementTree as ET
root = ET.parse('$file').getroot()
print(root.get('failures'))
" 2>/dev/null || echo 0)

    ERRORS=$(xmlstarlet sel -t -v "/testsuite/@errors" "$file" 2>/dev/null || python3 -c "
import xml.etree.ElementTree as ET
root = ET.parse('$file').getroot()
print(root.get('errors'))
" 2>/dev/null || echo 0)

    SKIPPED=$(xmlstarlet sel -t -v "/testsuite/@skipped" "$file" 2>/dev/null || python3 -c "
import xml.etree.ElementTree as ET
root = ET.parse('$file').getroot()
print(root.get('skipped'))
" 2>/dev/null || echo 0)

    TIME=$(xmlstarlet sel -t -v "/testsuite/@time" "$file" 2>/dev/null || python3 -c "
import xml.etree.ElementTree as ET
root = ET.parse('$file').getroot()
print(root.get('time'))
" 2>/dev/null || echo 0)

    TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
    TOTAL_FAILURES=$((TOTAL_FAILURES + FAILURES))
    TOTAL_ERRORS=$((TOTAL_ERRORS + ERRORS))
    TOTAL_SKIPPED=$((TOTAL_SKIPPED + SKIPPED))
    TOTAL_TIME=$(echo "$TOTAL_TIME + $TIME" | bc 2>/dev/null || echo "0.0")

    if [ "$FAILURES" -gt 0 ] || [ "$ERRORS" -gt 0 ]; then
        ICON="FAIL"
        COLOR="\033[0;31m"
    else
        ICON="PASS"
        COLOR="\033[0;32m"
    fi

    printf "  [%s]  %s\n" "$ICON" "$SHORT"
    printf "         tests: %-4s | failures: %-3s | errors: %-3s | skipped: %-3s | time: %6.2fs\n" "$TESTS" "$FAILURES" "$ERRORS" "$SKIPPED" "$TIME"
    echo ""
done

echo "  $SEPARATOR"
printf "  Total: %-5s | Failures: %-5s | Errors: %-5s | Skipped: %-5s | Time: %6.2fs\n" "$TOTAL_TESTS" "$TOTAL_FAILURES" "$TOTAL_ERRORS" "$TOTAL_SKIPPED" "$TOTAL_TIME"
echo ""

ALL_PASSED=false
if [ "$BUILD_SUCCESS" -eq 0 ] && [ "$TOTAL_FAILURES" -eq 0 ] && [ "$TOTAL_ERRORS" -eq 0 ]; then
    ALL_PASSED=true
fi

if $ALL_PASSED; then
    echo -e "  \033[0;32m[PASS]\033[0m All $TOTAL_TESTS tests completed successfully in ${ELAPSED}s"
else
    echo -e "  \033[0;31m[FAIL]\033[0m Some tests failed ($TOTAL_FAILURES failures, $TOTAL_ERRORS errors)"
fi

echo ""
[ "$ALL_PASSED" = true ]
