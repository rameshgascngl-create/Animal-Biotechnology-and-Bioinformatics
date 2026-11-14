#!/usr/bin/env bash
# Tier 2 emulator step: install the prebuilt APKs, set the screen, run ONLY the batch's new figures. Fatal on any failure; captures are always pulled.
# Usage: tier2_capture.sh <comma-separated figure ids> <wm size> <density> <label>
set -u
IDS=$1; SIZE=$2; DENSITY=$3; LABEL=$4; rc=0
PKG=com.gasczoology.animalbiotech; mkdir -p qa-captures
adb shell wm size "$SIZE"; adb shell wm density "$DENSITY"
adb install -r app-debug.apk || { echo "FAILED: install app" | tee -a qa-captures/failures.txt; exit 1; }
adb install -r -t app-debug-androidTest.apk || { echo "FAILED: install tests" | tee -a qa-captures/failures.txt; exit 1; }
adb shell mkdir -p /sdcard/Pictures; adb logcat -c || true
out=$(adb shell am instrument -w -e class $PKG.BatchFigureCaptureTest -e figures "$IDS" $PKG.test/androidx.test.runner.AndroidJUnitRunner 2>&1); echo "$out" | tee qa-captures/instrument-$LABEL.txt
echo "$out" | grep -q '^OK (1 test)' || { echo "FAILED: batch capture at $LABEL" | tee -a qa-captures/failures.txt; rc=1; }
adb pull /sdcard/Pictures qa-captures/screenshots >/dev/null 2>&1 || true
adb logcat -d -s CITEST:I CITEST:E > qa-captures/citest-$LABEL.txt || true
adb shell wm size reset; adb shell wm density reset
exit $rc
