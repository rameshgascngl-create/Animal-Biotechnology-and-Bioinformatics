# Animal Biotechnology and Bioinformatics

- `html-suite/` : offline single-file HTML apps (open `index.html` by double-click; keep files together).
- `app/` and Gradle files : the Android app that bundles the same chapters.

## Android app

Offline study app for the five-unit Animal Biotechnology syllabus.
Package: `com.gasczoology.animalbiotech` · minSdk 26 · targetSdk 35 · Kotlin + Jetpack Compose.

## What is inside
- Native syllabus screen (34 topics, Units I-V, including five Diagram Atlases) with search, "studied" ticks, progress ring and starred topics.
- Native quiz screen: 230 MCQs (23 modules x 10) with instant feedback, explanations, final score, best-score memory,
  plus random mixed quizzes (whole syllabus / per unit).
- 29 interactive chapters (incl. 5 Diagram Atlases with 73 diagrams and flow charts) (diagrams, flow charts, short-answer revision, glossary) bundled in `app/src/main/assets/modules/`
  and shown in a locked-down WebView (no network permission; navigation limited to bundled files).
- Footer credit: Department of Zoology, GASC, Nagercoil / Created by R. Ramesh.

## Build the APK
**GitHub Actions (no local setup):** push this folder to a repository; the workflow `.github/workflows/build-apk.yml`
builds `app-debug.apk` and attaches it as the artefact *AnimalBiotechnology-debug-apk*.

**Android Studio:** open this folder, let Gradle sync, then Build > Build APK(s).

**Command line:** `gradle :app:assembleDebug` (Gradle 8.9, JDK 17, Android SDK 35).

## Before Google Play
Create a release keystore, add a `signingConfig`, and build `bundleRelease` (AAB). Note that Play's
"minimum functionality" policy favours apps with native features; this app has native syllabus, progress, bookmarks and quizzes
around the bundled chapters.

## Updating content
Regenerate the modules/hub, copy the HTML files into `assets/modules/`, and update `assets/syllabus.json` and `assets/quizbank.json`.
