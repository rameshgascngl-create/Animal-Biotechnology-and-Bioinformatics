# Animal Biotechnology v0.9.0-rc2 (release candidate)

Native (Compose) reader, English or Tamil, Units I to V. Offline: no internet permission. Android 8+.

## Known limitations
- **Tamil is a draft machine translation** in every chapter (every Tamil string is marked `draft` in the data; 9,754 strings, plus 255 marked bracketed) and has not been read against the State Board books chapter by chapter. Terminology has been applied to figures only where noted in docs/audit.
- **Units II to V**: all 34 topics (Unit I: 6, II: 8, III: 7, IV: 6, V: 7) open natively; none falls back to a web view. A chapter shows the "could not be loaded" screen only if its data file is damaged. Figures in Units II to V have had the same Phase 0 audit but only the figures listed in the CHANGELOG were re-drawn.
- **Revise, Flashcards, Glossary and Search** are English only: their content (key points, short answers, glossary) exists only in English. In Tamil mode these screens open fully in English.
- **Figures not yet fixed** (Phase 0 audit, 145 figures; 35 re-drawn so far): 0 major, 19 moderate (drawn, ship in the next candidate), 45 minor, 46 acceptable. No "severe" figure remains. The audit rows for dna_library d03 to d09 were written for old image ids and are not reliable.
- **Terms marked "descriptive, not State Board"** are listed in docs/audit/descriptive-terms-ledger.md (enhancer, ori, regulatory region, early/late region, minichromosome, cell transformation, stringency, chemiluminescence, autoradiograph, colony, and the explanatory sentences). Quiz result, feedback and Back/Save descriptions in Tamil were written by the developer, also not from the State Board books.
- **Tamil text still contains 335 English words** (baseline in tools/ci/latin_baseline.txt); the CI gate allows no new ones.
- Not tested on a physical device or with TalkBack.
- targetSdk is 35.
