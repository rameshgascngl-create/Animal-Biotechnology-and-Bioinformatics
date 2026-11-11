# Animal Biotechnology and Bioinformatics

- `html-suite/` : offline single-file HTML apps (open `index.html` by double-click; keep files together).
- `app/` and Gradle files : the Android app that bundles the same chapters.

## Android app

Offline study app for the five-unit Animal Biotechnology syllabus.
Package: `com.gasczoology.animalbiotech` · minSdk 26 · targetSdk 35 · Kotlin + Jetpack Compose.

## What is inside
- Native syllabus screen (34 topics, Units I-V, including five Diagram Atlases) with search, "studied" ticks, progress ring and starred topics.
- Native quiz screen: 292 MCQs across 29 chapters, in English and Tamil with instant feedback, explanations, final score, best-score memory,
  plus random mixed quizzes (whole syllabus / per unit).
- 29 native bilingual (English/Tamil) chapters, including five Diagram Atlases, with 145 inline figures, short-answer revision and a glossary.
  Chapters are JSON in `app/src/main/assets/content/`; there is no WebView, no HTML in the APK and no network permission.
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
Edit the source chapters in `html-suite/`, run `tools/extract/extract.py` to regenerate `assets/content/`, and update `assets/syllabus.json` and `assets/quizbank.json`. CI fails if the committed JSON differs from the extractor output.
