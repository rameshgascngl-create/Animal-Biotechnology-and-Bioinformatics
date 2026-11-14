# Changelog

## 0.9.0-rc2 (release candidate; versionCode 9)
- Figures: all 17 figures graded "major" in the Phase 0 audit are now re-drawn as portrait figures in English and Tamil (batches 1 to 6), and 7 earlier figures were re-rendered with shaped-text (HarfBuzz/Raqm) measurement. 35 re-drawn figures ship in total; no major figure remains.
- Build checks restructured into three tiers (every push; per new figure batch on emulator; full suite nightly and before every RC). Tier 3 passed on main before this build.
- Not in this build: the 19 moderate figures (drawn, ship in rc3), the descriptive-term change for "vector", the interaction-wording sweep.

## 0.9.0-rc1 (release candidate; versionCode 8)
- Figures: pilot figure (screening-d02) and batches 1 and 2 re-drawn as portrait figures in English and Tamil: screening-d01, screening-d04, sanger-d01, vectors-d04, advanced_vectors-d03, dna_library-d05. Figure ids in dna_library shifted (see docs/audit/dna-library-id-remap.csv).
- Figure viewer: fixed the viewer closing itself after opening; state is held above the lazy list.
- Memory: inline figures are decoded at display width (scaled decode); the enlarged viewer keeps full detail.
- Language: quiz result screen, quiz header and several Back/Save/Studied descriptions now follow the chosen language (they were English-only in Tamil mode). Tamil quiz titles for mixed practice.
- Tamil terms in figures: primer = முதன்மை இழை, denaturation = இயல்பு திரிபு, probe = துலக்கி (ledger: docs/audit/descriptive-terms-ledger.md).
- Build: R8 and resource shrinking, targetSdk 35, minSdk 26, no manifest permissions. Release signing from CI secrets (docs/RELEASE.md).
- CI gates added (release-candidate workflow): install, upgrade over previous build, offline start, cold start, no WebView, language scan and sample.

## 2.0.0 (versionCode 7)
- Native Kotlin/Compose reader replacing the WebView build.
