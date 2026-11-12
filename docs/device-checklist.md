# Device test: pilot figure on a real phone (5 steps)

**APK** (debug-signed CI build of `pilot/screening-d02`, a pre-release, not the "latest" app):
https://github.com/rameshgascngl-create/Animal-Biotechnology-and-Bioinformatics/releases/download/pilot-screening-d02/AnimalBiotechnology-pilot.apk
The release notes name the commit it was built from. Android 8 or later. You may need to allow installs from your browser; installing replaces the current app only if the signing key matches (both are CI debug-signed), otherwise uninstall first (reading position and saved items are lost).

Open the chapter *Screening and selection of recombinants* and scroll to the figure with the pBR322 plasmid (its caption begins "pBR322 plasmid with ampicillin- and tetracycline-resistance genes"). Do each step in **English, then Tamil** (Tamil: switch the app language to Tamil first).

1. **Look.** Is the inline figure readable at normal arm's length without zooming? Note any label you cannot read or any word that is not in the chosen language (Latin names like AmpR, TetS, BamHI, LB are expected).
2. **Open and pinch.** Tap the figure; the full-screen viewer opens at fit-to-width. Pinch-zoom in and out, drag to the bottom of the figure, double-tap, press Reset. Note when text stops getting sharper (it is capped at the bitmap's pixel size).
3. **Rotate.** With the viewer open, turn the phone sideways and back. The viewer must stay open and keep roughly the same zoom and position. (The app no longer forces landscape.)
4. **Back.** Press the system Back button/gesture once: the viewer must close and the lesson must still be at the same place. Then open it again and use Close.
5. **TalkBack on** (Settings > Accessibility). Swipe to the figure: it should speak the figure's description in the current language. Double-tap to open; swipe through: figure first, then Zoom in, Zoom out, Reset, Close. Try the "Zoom in" action on the figure (actions menu). Note anything silent, in the wrong language, or out of order.

Report: phone model, Android version, the steps that failed, and the wording TalkBack spoke. **Nothing here has ever been run on a device or with TalkBack; the emulator CI cannot test TalkBack speech.**
