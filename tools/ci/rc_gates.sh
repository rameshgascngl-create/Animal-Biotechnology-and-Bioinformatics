#!/usr/bin/env bash
# Release-candidate gates that run on an emulator. Every failure is fatal (exit 1) but all gates run so one report lists everything.
# Usage: rc_gates.sh <gate.apk> <previous.apk> <cold-start-limit-ms>
set -u
GATE_APK=$1; PREV_APK=$2; LIMIT=${3:-4000}
PKG=com.gasczoology.animalbiotech; OUT=qa-captures/rc; mkdir -p "$OUT"; rc=0
fail() { echo "RC GATE FAILED: $1" | tee -a "$OUT/failures.txt"; rc=1; }
ui_text() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb shell cat /sdcard/ui.xml; }
wait_text() { for _ in $(seq 1 20); do ui_text | grep -q "$1" && return 0; sleep 1; done; return 1; }
adb root >/dev/null 2>&1; sleep 3; adb wait-for-device
adb shell getprop ro.build.version.sdk | tee "$OUT/sdk.txt"
adb shell wm size 1080x2400; adb shell wm density 420

echo "== 1. clean install"
adb uninstall $PKG >/dev/null 2>&1
adb install -r "$GATE_APK" || fail "clean install"
adb shell dumpsys package $PKG | grep -E "versionCode|versionName|requested permissions|android.permission" | tee "$OUT/package.txt"
grep -q "android.permission.INTERNET" "$OUT/package.txt" && fail "app requests INTERNET"

echo "== 2. cold start (3 runs, median recorded; limit ${LIMIT} ms)"
: > "$OUT/cold-start.txt"
for i in 1 2 3; do
  adb shell am force-stop $PKG; sleep 2
  adb shell am start -W -n $PKG/.MainActivity | grep -E "TotalTime" | sed 's/[^0-9]//g' >> "$OUT/cold-start.txt"
done
med=$(sort -n "$OUT/cold-start.txt" | sed -n 2p); echo "cold start ms (3 runs): $(tr '\n' ' ' < "$OUT/cold-start.txt") median=$med" | tee "$OUT/cold-start-summary.txt"
[ -n "$med" ] && [ "$med" -le "$LIMIT" ] || fail "cold start median ${med:-none} ms exceeds $LIMIT ms"

echo "== 3. offline: radios off, app still opens and shows its home screen"
adb shell svc wifi disable; adb shell svc data disable; sleep 3
{ adb shell ping -c 1 -W 2 8.8.8.8 || true; } 2>&1 | tee "$OUT/offline-ping.txt"
adb shell am force-stop $PKG; adb shell am start -n $PKG/.MainActivity >/dev/null
wait_text "Animal Biotechnology" || fail "offline: home screen did not appear"
ui_text > "$OUT/offline-home.xml"
adb shell svc wifi enable; adb shell svc data enable

echo "== 4. upgrade over the previous build keeps progress, bookmarks, quiz history, language"
adb uninstall $PKG >/dev/null 2>&1
adb install -r "$PREV_APK" || fail "install previous build"
adb shell am start -n $PKG/.MainActivity >/dev/null; sleep 4; adb shell am force-stop $PKG
TOPIC=$(python3 - <<'P'
import json; s=json.load(open('app/src/main/assets/syllabus.json',encoding='utf8')); t=s['units'][0]['topics'][0]; print(t['id'] + ' ' + t['file'])
P
)
TID=${TOPIC%% *}; TFILE=${TOPIC#* }
printf '<?xml version="1.0" encoding="utf-8" standalone="yes" ?>\n<map>\n<string name="lang">TA</string>\n<string name="last">%s</string>\n<int name="pos_%s" value="3" />\n<int name="zoom" value="115" />\n<int name="best_%s" value="7" />\n<set name="saved"><string>%s</string></set>\n<set name="studied"><string>%s</string></set>\n<set name="wrong"><string>q-seed</string></set>\n</map>\n' "$TID" "$TID" "$TFILE" "$TID" "$TID" > seed.xml
adb push seed.xml /data/local/tmp/seed.xml >/dev/null
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs && cp /data/local/tmp/seed.xml shared_prefs/animal_biotech.xml'" || fail "could not seed preferences in previous build"
adb install -r "$GATE_APK" || fail "upgrade install (-r over previous)"
adb shell dumpsys package $PKG | grep versionCode | tee "$OUT/upgraded-version.txt"
PREFS=/data/data/$PKG/shared_prefs/animal_biotech.xml
adb shell cat $PREFS > "$OUT/prefs-after-upgrade.xml"
for k in "name=\"lang\">TA" "name=\"last\">$TID" "pos_$TID" "best_$TFILE\" value=\"7\"" "name=\"saved\"" "name=\"studied\"" "name=\"wrong\"" "name=\"zoom\" value=\"115\""; do
  grep -q "$k" "$OUT/prefs-after-upgrade.xml" || fail "upgrade lost preference: $k"
done
adb shell am start -n $PKG/.MainActivity >/dev/null
wait_text "தொடர்ந்து படிக்க" || fail "upgrade: Tamil language / continue-reading card not restored on launch"
adb shell cat $PREFS > "$OUT/prefs-after-launch.xml"
grep -q "best_$TFILE\" value=\"7\"" "$OUT/prefs-after-launch.xml" || fail "quiz history lost after first launch"
ui_text > "$OUT/upgrade-home.xml"
adb shell screencap -p /sdcard/rc-upgrade.png; adb pull /sdcard/rc-upgrade.png "$OUT/" >/dev/null
adb shell wm size reset; adb shell wm density reset
exit $rc
