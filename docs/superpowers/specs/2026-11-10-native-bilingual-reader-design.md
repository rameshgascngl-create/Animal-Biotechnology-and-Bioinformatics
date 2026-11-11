# Native bilingual (EN/TA) chapter reader: design spec

Date: 2026-11-10 · Owner: Dr. R. Ramesh, Dept. of Zoology, GASC, Nagercoil
Status: APPROVED at v2 (commit 7f0674c) with two additions (v3, below). Plan: docs/superpowers/plans/2026-11-10-native-bilingual-reader.md

## 1. Goal and success criteria

Replace the WebView chapter reader of the Animal Biotechnology app with a 100% native Jetpack Compose reader, and make the whole app bilingual (English / Tamil), without regressing existing native features.

Success means:
1. No `android.webkit` import and no `assets/modules/*.html` in the shipped APK.
2. Unit I (exact scope in section 1.1) is readable natively with text, tables, callouts, key terms, glossary, and diagrams.
3. A language setting (English / தமிழ் / Both) applies to Unit I chapters, all 292 quiz questions, all 29 revision sets, syllabus titles, and UI strings.
4. **Amended (owner request: "completely android based"):** every one of the 29 chapters ships as a native bilingual reader; no chapter is left on the WebView or on a placeholder screen at release. Chapters whose Tamil is not yet authored show English with the visible "Tamil pending" marker. The "chapter text coming" screen of section 6.3 survives only as an internal safety net for a chapter whose JSON fails to parse, never as a planned end state.
5. Every Tamil string is flagged for owner review; none is presented as final.
6. Package id `com.gasczoology.animalbiotech`, minSdk 26, targetSdk 35, debug keystore and `build-apk.yml` stay unchanged, so installs update in place.

### 1.1 Unit I scope (exact)

Source of truth is `app/src/main/assets/syllabus.json`, which assigns files to units. Unit I contains topics t1 to t5 and `atlasI`:

| In pilot (Unit I) | Syllabus id |
|---|---|
| `biotechnology_fundamentals.html` | t1 |
| `enzymes_genetic_engineering.html` | t2 |
| `vectors_genetic_engineering.html` | t3 |
| `advanced_vectors_cosmid_yac_sv40_transposons.html` | t4 |
| `dna_library_genomic_cdna.html` | t5 |
| `atlas1_fundamentals_vectors.html` | atlasI |

Out of the pilot, and why: `gene_cloning_steps_selection.html` (t6) and `screening_selection_recombinants.html` (t8) are filed under **Unit II** (Techniques in Animal Biotechnology) in `syllabus.json`, alongside `gene_transfer_methods_rdna_host_cells` (t7). They are thematically close to Unit I (cloning), so this is a judgement call, not a technical limit. If the owner wants them in the pilot, the change is to add those two files to the list above and to the milestone M2 to M5 batches; Unit II would then be partly converted. Default here: follow the syllabus, six files.

Out of scope (pilot): Units II to V chapter conversion, Play Store release signing, new quiz questions, any network feature.

## 2. Current state (audited)

- `app/` is Kotlin + Compose (about 1,450 lines): `MainActivity` (navigation, state), `Screens`, `Study` (quiz), `Revise` (revision, flashcards, glossary), `Data` (JSON repos, `Store`).
- Only the chapter reader is non-native (WebView over `assets/modules/*.html`, 29 files, about 3 MB).
- Assets: `syllabus.json` (34 topics, 5 units), `quizbank.json` (29 files, 292 MCQs), `revision.json` (29 sets: key points, short answers, glossary), `headings.json`.
- No Tamil exists anywhere. The HTML has 145 inline SVG diagrams and no raster images. Six chapters have no diagrams.
- README states 230 MCQs; data holds 292. Fix the README.
- Chapter templates are not uniform (each file uses its own class names), so extraction cannot rely on fixed CSS classes.

## 3. Approaches considered

A. **Native renderer over bilingual JSON (chosen).** One schema, one renderer, extraction script plus authored Tamil. Pros: truly native, searchable, themable, small. Cons: highest effort.
B. Convert HTML to Compose `AnnotatedString` at runtime. Rejected: keeps HTML parsing on device, fragile with tables and SVG, does not solve Tamil.
C. Keep the WebView and inject Tamil. Rejected: not "pure Android".

## 4. Architecture

```
app/src/main/
  assets/
    content/u1/<chapter>.json     bilingual chapter (new)
    syllabus.json quizbank.json revision.json   (extended with "ta" fields)
  java/com/gasczoology/animalbiotech/
    content/Model.kt              Block sealed hierarchy, Bi(en, ta, reviewed)
    content/ChapterRepo.kt        loads + caches chapter JSON (org.json, no new deps)
    ui/LessonScreen.kt            LazyColumn renderer of blocks
    ui/blocks/*.kt                one composable per block type
    ui/diagram/*.kt               Compose Canvas diagrams, bilingual labels
    i18n/Lang.kt                  Lang { EN, TA, BOTH }, UI string table
  res/font/notosanstamil_*.ttf    bundled font (SIL OFL)
```
Language is stored in the existing `Store` and exposed as a Compose state read by every screen.

### 4.1 Content schema (v1)
`Bi = { "en": str, "ta": str, "ta_status": "draft|reviewed|bracketed" }`

Chapter = `{ id, unit, title: Bi, sections: [ { id, title: Bi, blocks: [Block] } ] }`

Block types: `p` (paragraph with inline `**bold**` and `_italic_`), `bullets`, `numbered`, `table` (header Bi[], rows Bi[][]), `callout` (kind: info|warn|tip|exam, text), `keyterm` (term, definition), `diagram` (ref to a native diagram id, caption Bi), `steps` (flow list), `reveal` (question/answer pair), `summary` (bullets), `glossary` (term, def), `unparsed` (text, source file, selector; see 6.4).

### 4.2 Extraction pipeline (`tools/`, Python, not shipped)
1. Parse each chapter with BeautifulSoup. Walk `body` by tag semantics: `h2` starts a section, `h3` a sub-heading, `p`, `ul`/`ol`, `table`, `svg` and `figure`. Class-name hints map to callout, key term, reveal and glossary through a per-chapter profile file, because templates differ.
2. Drop chrome (sidebar, nav, progress, footer, scripts). Keep the credit line as a footer constant.
3. Emit English JSON. A validator reports any text node not captured, so nothing is silently lost.
4. Tamil is added into the same JSON (`ta`, `ta_status=draft`). Terms follow Tamil Nadu State Board Biology/Zoology textbook usage; where no standard word is known, `ta` shows Tamil followed by the English term in brackets and `ta_status=bracketed`.

### 4.3 Diagrams
- Each Unit I diagram is audited. A first regex count of inline `<svg>` elements in the six pilot files gives 32 (vectors 4, advanced vectors 4, DNA library 9, Atlas I 15; Fundamentals and Enzymes 0); the M2 inventory is authoritative and replaces this figure. Each is audited against the source science, then rebuilt as a Compose `Canvas` composable with bilingual labels (labels come from the schema, not hard-coded).
- Audit findings and any corrections are logged in `docs/audit/unit1-diagrams.md` with before/after notes.
- Chapters with no diagrams (Enzymes, Fundamentals) get new original diagrams (e.g. restriction cut types, biotechnology branches). No third-party artwork is copied. If a photograph is genuinely needed it must be Wikimedia Commons CC0/CC-BY with attribution recorded in `docs/audit/credits.md`; none is assumed.
- Rendering is vector, theme-aware, and respects the text-size setting.

### 4.4 Screens and behaviour
- Reader: top bar with language toggle (EN | தமிழ் | Both), section index drawer, progress marking into existing "studied" state, text-size control (reuses existing `zoom`).
- "Both" shows English then Tamil per block, visually distinguished.
- Quiz, Revision, Flashcards, Glossary, Syllabus, Search all read `Lang`. Search matches both scripts.
- Dark mode support is added to the colour scheme.
- Accessibility: content descriptions on diagrams, minimum 48 dp touch targets, Tamil line height tuned for glyph stacking.

## 5. Error handling
- Missing `ta` falls back to `en` and shows a small "Tamil pending" marker, never blank.
- Malformed chapter JSON is caught in `ChapterRepo`, logged, and the screen shows a recoverable error instead of crashing.
- Unknown block type is skipped and logged.

## 6. Testing and verification
1. Python (run here): schema validation, 100% of `en` text accounted for versus source, Tamil coverage report, quiz answer index in range, JSON well-formedness, Tamil Unicode normalisation check.
2. Kotlin unit tests (JVM) for `Model`/`ChapterRepo` parsing and `Lang` fallback, included in the repo.
3. **Limit:** this environment has no Android SDK and Google Maven is blocked, so the APK cannot be compiled or run here. Kotlin is written conservatively against the same dependency set the project already builds with, and the first GitHub Actions run is the real compile check. Anything uncertain is listed in the hand-over notes.

### 6.3 Units II to V: WebView removed, not kept
The release contains no WebView and no `assets/modules`. For a topic without native chapter JSON, tapping it opens a native screen that:
- shows a clear banner: "Chapter text for this topic is coming in a later update" (English and Tamil),
- offers three prominent buttons: Take the quiz, Revision notes, Glossary (all already native and bilingual),
- never dead-ends: back returns to the syllabus, and the topic can still be starred or marked.

The HTML suite in `html-suite/` remains in the repo for desktop use. A topic switches from fallback to native chapter automatically when its `content/<unit>/<chapter>.json` exists, so each later unit is a pure content addition.

### 6.4 Extractor failure behaviour (never silent)
Content that matches no known template is never dropped without trace:
1. The extractor emits an `unparsed` block that preserves the node's plain text, its source file and CSS selector.
2. The extractor writes `tools/out/extract-report.md` listing every unparsed node and exits non-zero if any exist in a chapter not yet allow-listed, so CI or a local run fails loudly.
3. In the app, an `unparsed` block renders as a visible warning card ("This part could not be formatted; showing plain text") with the preserved text, so nothing disappears from the student's view.
4. Clarification for owner review: the instruction was "log-and-skip"; this spec logs and skips the structured rendering but keeps the text, because skipping the text itself is the silent-loss failure to avoid.

## 7. Deliverables
1. Patch (git format) and zip of the updated project, ready for the existing CI.
2. `docs/audit/` reports: content/extraction audit, diagram audit, Tamil review sheet (CSV: id, en, ta, status).
3. README corrections and build notes.

### 7.1 Delivery sequence (incremental, always compilable)

Each milestone is one commit (or small PR) that must build green in GitHub Actions before the next starts. Because compilation can only be verified there, no milestone depends on an unmerged later one, and the WebView stays working until the very end.

| # | Milestone | Touches | Build state |
|---|---|---|---|
| M1 | Schema and types: `Model.kt`, `Lang.kt`, `ChapterRepo.kt` plus JVM unit tests. Not wired into the UI. | new files only | app behaves exactly as today |
| M2 | Extractor (`tools/`, Python) with its own unit tests; generates English JSON for the six Unit I files; extract report **including a diagram inventory**: every `<svg>` and `<figure>` in the six pilot files counted and listed (file, index, caption/id, rough element count), so M5 scope is verified against real data. JSON added to assets but unused. | tools/, assets | app unchanged |
| M2b | **Added.** Extractor extended to all 29 chapters (145 diagrams: 32/33/34/24/22 by unit); reproducibility check in CI. Delivered. | tools/, assets | app unchanged |
| M3 | Tamil for the six **chapter** JSON files, then Units II to V per unit (same review CSV process), plus their review CSV; schema and coverage validator (Python). | assets, docs | app unchanged |
| M3b | Tamil for **all 292 quiz questions** (question, four options, explanation), **all 29 revision sets** (title, key points, short answers, glossary terms and definitions) and **syllabus titles/notes**, authored inline as `*_ta` fields that the current parsers ignore, plus their review CSV (`docs/audit/tamil-review-quiz-revision.csv`). Data only. | assets, docs | app unchanged |
| M4 | Reader UI (`LessonScreen` and block composables), language toggle. **All 29 chapters open native** (Units II to V in English with "Tamil pending" until M3 reaches them). | ui/, MainActivity | both paths work |
| M5 | Native diagrams for all **145** (one generic Canvas renderer over per-diagram draw specs, bilingual labels by id), audit log. | ui/diagram, docs | both paths work |
| M6 | Screens consume the M3b `*_ta` fields: bilingual quiz, revision, glossary, syllabus, search; fallback screen for topics without JSON. No Tamil authoring happens in M6. | existing screens | both paths work |
| M7 | WebView removal, last: delete `android.webkit` use and `assets/modules`, update proguard/README, version bump. | MainActivity, assets | final |

The current workflow publishes a `latest` GitHub release on every push to any branch. Before M1, change `build-apk.yml` so the release step runs only on `main` (one-line `if:` condition), so milestone branches do not overwrite the released APK. This is part of M1.

## 8. Risks
| Risk | Mitigation |
|---|---|
| Cannot compile here | Conservative Kotlin, unit tests, hand-over list, CI is the check |
| Tamil terminology errors | Draft flag on every string, review CSV, State Board terms, bracketed English fallback |
| Extractor misses content | Validator accounts for every source text node |
| Diagram redraw changes meaning | Per-diagram audit log with before/after |
| Units II to V chapter text unreadable in-app until converted | Visible banner, one-tap quiz/revision/glossary, follow-up units are content-only additions |
| Branch pushes overwrite the `latest` release | `if: github.ref == 'refs/heads/main'` on the release step (M1) |
