#!/usr/bin/env bash
# Tier 3 emulator suite (nightly and before every RC). Every failure is fatal, but screenshots are always pulled first so a failure can be inspected.
# Runs: install + cold start, the whole androidTest base suite (every class except the Tier 2 per-figure capture and the representative-figure test),
# then the viewer lifecycle + memory budget on the three representative figures (tall, wide, Tamil) at two screen sizes.
set -u
rc=0
mkdir -p qa-captures
PKG=com.gasczoology.animalbiotech
adb logcat -G 16M || true; adb logcat -c || true
adb shell wm size 720x1280; adb shell wm density 320
adb install -r app/build/outputs/apk/debug/app-debug.apk || rc=1
adb shell am force-stop $PKG
adb shell am start -W -n $PKG/.MainActivity | tee qa-captures/cold-start.txt
stat -c '%s' app/build/outputs/apk/debug/app-debug.apk | tee qa-captures/apk-bytes.txt
NOT="$PKG.BatchFigureCaptureTest,$PKG.ViewerLifecycleRepsTest"
gradle :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notClass=$NOT --stacktrace || { echo "FAILED: base suite" | tee -a qa-captures/failures.txt; rc=1; }
REPS=$(python3 tools/ci/representatives.py); echo "representative figures: $REPS" | tee qa-captures/representatives.txt
for cfg in "1080x2400 480" "1080x2400 420"; do
  set -- $cfg; adb shell wm size $1; adb shell wm density $2
  gradle :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=$PKG.ViewerLifecycleRepsTest -Pandroid.testInstrumentationRunnerArguments.reps="$REPS" --stacktrace \
    || { echo "FAILED: viewer lifecycle and memory budget at density $2" | tee -a qa-captures/failures.txt; rc=1; }
done
adb shell wm size reset; adb shell wm density reset
adb pull /sdcard/Pictures qa-captures/screenshots || true
adb logcat -d -s CITEST:E CITEST-MEM:E > qa-captures/citest.txt || true
adb logcat -d -s CITEST:I CITEST:E CITEST-MEM:I CITEST-MEM:E > qa-captures/citest-all.txt || true
adb logcat -d | grep -E 'AndroidRuntime|FATAL|animalbiotech|ActivityTaskManager|WindowManager' | tail -400 > qa-captures/logcat.txt || true
exit $rc
