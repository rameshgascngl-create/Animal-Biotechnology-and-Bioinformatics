#!/usr/bin/env bash
# Runs every emulator test; any failure fails the job, but screenshots are always pulled first so a failure can be inspected.
set -u
rc=0
mkdir -p qa-captures
adb logcat -G 16M || true; adb logcat -c || true
adb shell wm size 720x1280; adb shell wm density 320
adb install -r app/build/outputs/apk/debug/app-debug.apk || rc=1
adb shell am force-stop com.gasczoology.animalbiotech
adb shell am start -W -n com.gasczoology.animalbiotech/.MainActivity | tee qa-captures/cold-start.txt
stat -c '%s' app/build/outputs/apk/debug/app-debug.apk | tee qa-captures/apk-bytes.txt
gradle :app:connectedDebugAndroidTest --stacktrace || { echo "FAILED: base suite" | tee -a qa-captures/failures.txt; rc=1; }
if [ "${RUN_PILOT:-0}" = "1" ]; then
  for d in 480 420; do
    adb shell wm size 1080x2400; adb shell wm density $d
    gradle :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.gasczoology.animalbiotech.ReaderCaptureTest#capturePilotFigure --stacktrace \
      || { echo "FAILED: pilot at density $d" | tee -a qa-captures/failures.txt; rc=1; }
    gradle :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.gasczoology.animalbiotech.BatchFigureCaptureTest --stacktrace \
      || { echo "FAILED: batch figures at density $d" | tee -a qa-captures/failures.txt; rc=1; }
  done
fi
adb shell wm size reset; adb shell wm density reset
adb pull /sdcard/Pictures qa-captures/screenshots || true
adb logcat -d -s CITEST:E CITEST-MEM:E > qa-captures/citest.txt || true
adb logcat -d -s CITEST:I CITEST:E CITEST-MEM:I CITEST-MEM:E > qa-captures/citest-all.txt || true
adb logcat -d | grep -E 'AndroidRuntime|FATAL|animalbiotech|ActivityTaskManager|WindowManager' | tail -400 > qa-captures/logcat.txt || true
exit $rc
