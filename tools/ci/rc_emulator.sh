#!/usr/bin/env bash
# Emulator side of the RC: install/offline/cold-start/upgrade gates, then the language sample on the real activity. Any failure is fatal.
set -u; rc=0; mkdir -p qa-captures
bash tools/ci/rc_gates.sh "$1" "$2" "$3" || rc=1
adb shell wm size 1080x2400; adb shell wm density 420
gradle :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.gasczoology.animalbiotech.LanguageSampleTest --stacktrace \
  || { echo "RC GATE FAILED: language sample ($4)" | tee -a qa-captures/rc/failures.txt; rc=1; }
adb logcat -d -s CITEST:E > qa-captures/rc/citest-$4.txt || true
exit $rc
