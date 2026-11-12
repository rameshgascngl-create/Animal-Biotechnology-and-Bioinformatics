# Device checklist: release candidate (v0.9.0-rc1)

**APK:** the pre-release asset named in the GitHub Release `v0.9.0-rc1` (Releases page of this repository). If the file name contains `NOT-FOR-DISTRIBUTION`, it is debug-signed: install for testing only. Android 8 (API 26) or later.
**Nothing below has been run on a physical device or with TalkBack. CI proves install, upgrade, offline start, cold start and the language sample on an emulator only.**

Do every section **in English, then in Tamil** where it says so. Note: phone model, Android version, steps that failed, what TalkBack spoke.

## A. Install (clean)
1. Uninstall any earlier build. Install the RC APK (allow installs from your browser or file manager if asked). It opens to the home screen without a network prompt.
2. Open one chapter from each of Units I to V. Each opens a native screen (no blank page, no "chapter text could not be loaded").

## B. Upgrade over a previous install
1. On the *previous* build: switch to Tamil, open any chapter, star it (Saved), mark it studied, finish one module quiz, note the best score.
2. Install the RC over it without uninstalling.
3. Check: language is still Tamil; "Continue reading" (தொடர்ந்து படிக்க) points to the same chapter at the same position; Saved and studied marks are there; the quiz best score is still shown.

## C. Offline
1. Switch on aeroplane mode, wifi off. Force-stop the app and open it again.
2. Open a chapter with figures, open a figure, take a quiz. Nothing asks for the internet.

## D. Both languages
1. Switch English then Tamil on the home screen, a chapter, a quiz (question, feedback, result screen) and the About tab.
2. In Tamil mode no English sentence appears except symbols, gene and organism names, units and the institutional credit line. **Known:** Revise, Flashcards, Glossary and Search content is English only (see release notes).
3. Tamil is a draft machine translation for every chapter; read some chapters and note wrong terms.

## E. Figures: pinch, rotate, Back
1. Open *Screening and selection of recombinants* (batch figures) and *DNA library: genomic and cDNA*. Is each inline figure readable at arm's length?
2. Tap a figure: the viewer opens at fit-to-width. Pinch in and out, drag, double-tap, press Reset.
3. Rotate with the viewer open: it stays open with similar zoom and position.
4. Press system Back once: the viewer closes and the lesson is at the same place. Open again and use Close.

## F. TalkBack (Settings > Accessibility)
1. Swipe to a figure: it speaks the figure description in the current language. Double-tap: viewer opens; order is figure, Zoom in, Zoom out, Reset, Close.
2. Quiz result screen buttons and the Back button are spoken in the current language.
