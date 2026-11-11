# Native Bilingual (EN/TA) Chapter Reader Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the WebView chapter reader with a native Compose reader for the six Unit I chapters, make the whole app bilingual (English / Tamil / Both), and ship no WebView or HTML.

**Architecture:** Chapter content becomes bilingual JSON blocks extracted from the HTML by a Python tool, rendered by a Compose `LessonScreen`. Quiz/revision/syllabus Tamil is added inline as `*_ta` fields. Seven milestones, each of which must build green in GitHub Actions before the next starts; the WebView is removed last.

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2024.10.01), `org.json`, JUnit4 (JVM tests), Python 3 + BeautifulSoup + pytest for tools.

**Spec:** `docs/superpowers/specs/2026-11-10-native-bilingual-reader-design.md` (approved, v3)

**Execution:** native, in this session (owner said "start with M1"). Branch `feat/native-bilingual-reader`, cut from `spec/native-bilingual-reader`.

## Global Constraints

- Package `com.gasczoology.animalbiotech`; minSdk 26; targetSdk 35; compileSdk 35; Compose BOM `2024.10.01`; JDK 17.
- Keep `app/debug.keystore` and its `signingConfigs.debug`; installs must update in place.
- No `INTERNET` permission, no new runtime dependencies (test-only deps allowed; font is an asset, not a dependency).
- Final release: no `android.webkit` import, no `assets/modules`.
- Unit I = exactly: `biotechnology_fundamentals`, `enzymes_genetic_engineering`, `vectors_genetic_engineering`, `advanced_vectors_cosmid_yac_sv40_transposons`, `dna_library_genomic_cdna`, `atlas1_fundamentals_vectors` (syllabus ids t1 to t5, atlasI).
- British English. Credit line verbatim: "Department of Zoology, GASC, Nagercoil / Created by R. Ramesh".
- Tamil follows Tamil Nadu State Board Biology/Zoology textbook terms; where no standard word is known, write the Tamil followed by the English term in brackets and mark `bracketed`. Every Tamil string is `draft` until the owner marks it reviewed.
- Chapter JSON path: `app/src/main/assets/content/u1/<stem>.json`. Quiz/revision/syllabus Tamil keys end in `_ta`.
- Extractor never drops content silently (spec 6.4). Missing Tamil falls back to English with a visible marker (spec 5).
- Never skip CI: after each milestone push, confirm the Actions run is green before starting the next.

## Review Focus

1. Table row with fewer cells than the header: renders without crash (Task 3, Task 8).
2. Stored language value that is not `EN`/`TA`/`BOTH` (corrupt prefs): defaults to `EN` (Task 2).
3. Tamil text in NFD vs NFC form: search matches both (Task 11).
4. Syllabus topic whose chapter JSON does not exist (all of Units II to V): fallback screen with quiz/revision/glossary buttons, no crash (Task 9).
5. Quiz `answer` index outside the options range, or a quiz/revision `ta` array whose length differs from English: validator fails the check (Task 6).

## File Structure

```
.github/workflows/build-apk.yml            modify: unit-test step, release only on main
app/build.gradle.kts                       modify: testImplementation junit + org.json
app/src/main/java/com/gasczoology/animalbiotech/
  content/Model.kt                         Bi, TaStatus, Block hierarchy, Chapter
  content/ChapterParser.kt                 pure JSON string -> ParseResult
  content/ChapterRepo.kt                   asset loading + path rule
  i18n/Lang.kt                             Lang, parseLang, Bi.shown, UiText
  ui/LessonScreen.kt                       LazyColumn renderer, toolbar, section index
  ui/blocks/Blocks.kt                      one composable per Block type
  ui/diagram/Diagrams.kt                   DiagramRegistry + Canvas diagrams
  ui/FallbackChapterScreen.kt              "chapter text coming" screen
app/src/test/java/com/gasczoology/animalbiotech/   JVM tests
app/src/main/assets/content/u1/*.json      six chapter files
tools/extract/extract.py  tools/validate_content.py  tools/tamil_review_csv.py
tools/tests/*              pytest
docs/audit/                reports and review CSVs
```

---

## Milestone M1: schema, types, plumbing (app behaviour unchanged)

### Task 1: CI plumbing and test dependencies

**Files:** Modify `.github/workflows/build-apk.yml`, `app/build.gradle.kts`

**Interfaces:** Produces: `gradle :app:testDebugUnitTest` runs JUnit4 tests that can use `org.json`.

- [ ] **Step 1:** In `app/build.gradle.kts` dependencies add `testImplementation("junit:junit:4.13.2")` and `testImplementation("org.json:json:20240303")` (the Android stub `org.json` throws on the JVM).
- [ ] **Step 2:** In the workflow, add a step before "Build debug APK": `gradle :app:testDebugUnitTest --stacktrace`.
- [ ] **Step 3:** Add `if: github.ref == 'refs/heads/main'` to the "Publish APK to the 'latest' release" step only.
- [ ] **Step 4:** Commit `ci: run unit tests; publish latest release only from main`. Push; Expected: Actions run green (no tests yet; task passes with "NO-SOURCE").

### Task 2: `Bi`, `Lang` and display rules

**Files:** Create `content/Model.kt` (the `Bi`/`TaStatus` part), `i18n/Lang.kt`; Test `i18n/LangTest.kt`

**Interfaces:** Produces
```kotlin
enum class TaStatus { DRAFT, REVIEWED, BRACKETED }
data class Bi(val en: String, val ta: String = "", val taStatus: TaStatus = TaStatus.DRAFT)
enum class Lang { EN, TA, BOTH }
fun parseLang(raw: String?): Lang                      // anything unknown -> EN
data class Shown(val text: String, val isTamil: Boolean, val pending: Boolean)
fun Bi.shown(lang: Lang): List<Shown>
```

- [ ] **Step 1: Failing tests** in `LangTest`: `parseLang("TA")==TA`; `parseLang("garbage")==EN`; `parseLang(null)==EN`; `Bi("Vector","காவி").shown(EN)==[Shown("Vector",false,false)]`; `.shown(TA)==[Shown("காவி",true,false)]`; `.shown(BOTH)` has two entries English first; `Bi("Vector").shown(TA)==[Shown("Vector",false,true)]` (English fallback, `pending=true`); `Bi("Vector").shown(BOTH)==[Shown("Vector",false,false), Shown("",true,true)]`.
- [ ] **Step 2:** Run `gradle :app:testDebugUnitTest`; Expected: FAIL (compile error, types missing).
- [ ] **Step 3:** Implement per the rules in the tests.
- [ ] **Step 4:** Run tests; Expected: PASS.
- [ ] **Step 5:** Commit `feat(content): Bi, Lang and display fallback rules`.

### Task 3: Chapter model and parser

**Files:** Complete `content/Model.kt`; Create `content/ChapterParser.kt`, `content/ChapterRepo.kt`; Test `content/ChapterParserTest.kt`

**Interfaces:** Consumes `Bi`. Produces
```kotlin
sealed interface Block
data class Paragraph(val text: Bi): Block
data class Bullets(val items: List<Bi>, val ordered: Boolean): Block
data class Table(val header: List<Bi>, val rows: List<List<Bi>>): Block
enum class CalloutKind { INFO, WARN, TIP, EXAM }
data class Callout(val kind: CalloutKind, val text: Bi): Block
data class KeyTerm(val term: Bi, val definition: Bi): Block
data class Diagram(val id: String, val caption: Bi): Block
data class Steps(val items: List<Bi>): Block
data class Reveal(val question: Bi, val answer: Bi): Block
data class Summary(val items: List<Bi>): Block
data class GlossaryEntry(val term: Bi, val definition: Bi): Block
data class Unparsed(val text: String, val source: String, val selector: String): Block
data class Section(val id: String, val title: Bi, val blocks: List<Block>)
data class Chapter(val id: String, val unit: String, val title: Bi, val sections: List<Section>)
data class ParseResult(val chapter: Chapter?, val warnings: List<String>)
fun parseChapter(json: String): ParseResult
fun chapterAssetPath(file: String, unitRoman: String): String   // ("enzymes_genetic_engineering.html","I") -> "content/u1/enzymes_genetic_engineering.json"
object ChapterRepo { fun load(ctx: Context, file: String, unitRoman: String): ParseResult? }  // null when asset absent
```

- [ ] **Step 1: Failing tests:** valid minimal chapter parses with one section and a `Paragraph`; table whose second row has one cell where the header has two parses and the missing cell is `Bi("")`; unknown block type `"hologram"` yields an `Unparsed` block with `selector=="type:hologram"` plus one warning; malformed JSON (`"{"`) returns `chapter==null` and one warning; `chapterAssetPath("atlas1_fundamentals_vectors.html","I")=="content/u1/atlas1_fundamentals_vectors.json"` and unit `"IV"` maps to `u4`.
- [ ] **Step 2:** Run; Expected: FAIL.
- [ ] **Step 3:** Implement with `org.json`, never throwing; every Bi reads `{en,ta,ta_status}` with defaults `""`/`draft`.
- [ ] **Step 4:** Run tests; Expected: PASS.
- [ ] **Step 5:** Commit `feat(content): chapter model and tolerant parser`. Push branch; wait for green CI. **M1 done.**

---

## Milestone M2: extractor and diagram inventory (app unchanged)

### Task 4: Extractor core

**Files:** Create `tools/extract/extract.py`, `tools/extract/profiles/*.json`, `tools/tests/test_extract.py`, `tools/tests/fixtures/*.html`; `tools/requirements.txt` (beautifulsoup4, lxml, pytest)

**Interfaces:** Produces
```python
def extract_chapter(html: str, source: str, profile: dict) -> dict   # chapter dict per spec 4.1, English only
def text_coverage(html: str, chapter: dict) -> list[str]             # source text nodes absent from the chapter
def diagram_inventory(html: str, source: str) -> list[dict]          # {"file","index","kind":"svg|figure","id","caption","elements"}
```
CLI: `python3 -I tools/extract/extract.py --unit I --out app/src/main/assets/content/u1 --report tools/out/extract-report.md`; exit code 1 if any `unparsed` block exists.

- [ ] **Step 1: Failing tests:** (a) fixture with `h2`,`p`,`ul`,`table` maps to a section with `p`, `bullets`, `table` blocks; (b) sidebar/nav/footer/script text never appears; (c) a node of unknown structure (`<marquee>Hello</marquee>`) becomes `{"type":"unparsed","text":"Hello","selector":"marquee"}` and CLI exit code is 1; (d) `text_coverage` returns `[]` for the fixture and returns the dropped sentence when one block is deleted; (e) `diagram_inventory` on a fixture with 2 svgs and 1 figure returns 3 records whose `caption` comes from `figcaption`.
- [ ] **Step 2:** Run `python3 -I -m pytest tools/tests -q`; Expected: FAIL.
- [ ] **Step 3:** Implement by walking `body` by tag semantics (`h2` opens a section, `h3` is a sub-heading paragraph with `bold`, `p`, `ul`/`ol`, `table`, `svg`/`figure` become `diagram` blocks with `id` `<stem>-dNN`). Profile JSON maps class-name substrings to `callout`, `keyterm`, `reveal`, `glossary`, and to the chrome selectors to drop; one profile per template family (the six files use four different class vocabularies).
- [ ] **Step 4:** Run tests; Expected: PASS.
- [ ] **Step 5:** Commit `feat(tools): chapter extractor with unparsed and coverage checks`.

### Task 5: Generate Unit I English JSON and the inventory

**Files:** Create `app/src/main/assets/content/u1/*.json` (6), `docs/audit/extract-report.md`, `docs/audit/diagrams-unit1.json`

- [ ] **Step 1:** Run the CLI on `html-suite/` for the six files. Expected: exit 0, `text_coverage` empty for all six. If any `unparsed` appear, extend profiles (not the code path that tolerates them) until none remain or each is listed and accepted in the report.
- [ ] **Step 2:** The report must contain a table per file (SVG count, figure count) and a total, listing every diagram's id and caption. State in the report whether the total differs from the spec's regex figure of 32.
- [ ] **Step 3:** Parse every output file with the Kotlin parser logic (run `ChapterParserTest` extended with a loop over the six asset files, expecting zero warnings).
- [ ] **Step 4:** Commit `feat(content): extract Unit I chapters (English) and diagram inventory`. Push; wait for green CI. **M2 done.**

---

## Milestone M3: Tamil for the six chapters (data only)

### Task 6: Validator, then Tamil authoring

**Files:** Create `tools/validate_content.py`, `tools/tamil_review_csv.py`, `tools/tests/test_validate.py`; Modify the six chapter JSON; Create `docs/audit/tamil-review-chapters.csv`

**Interfaces:** Produces `validate_content.py [--chapters] [--quiz-revision]` exits 1 on any failure.

- [ ] **Step 1: Failing tests:** chapter with an empty `ta` is reported with its path; invalid `ta_status` is rejected; a quiz item with `a` outside `range(len(o))` fails; a quiz/revision `*_ta` list whose length differs from the English list fails; Tamil strings must be NFC-normalised and contain at least one character in U+0B80 to U+0BFF unless `bracketed`/identical (symbols, formulae).
- [ ] **Step 2:** Run pytest; Expected: FAIL. Implement; Expected: PASS.
- [ ] **Step 3:** Author `ta` for every `Bi` in the six chapters, one chapter per commit, status `draft` (or `bracketed` when the English term follows in brackets). Chemical symbols, enzyme names (EcoRI, Taq), organism names and units stay in Latin script.
- [ ] **Step 4:** Run `python3 -I tools/validate_content.py --chapters`; Expected: exit 0 and 100% Tamil coverage. Generate `tamil-review-chapters.csv` (columns: file, path, en, ta, status).
- [ ] **Step 5:** Push; green CI. **M3 done.**

## Milestone M3b: Tamil for quizzes, revision sets and syllabus (data only)

### Task 7: Author `*_ta` fields in existing JSON

**Files:** Modify `app/src/main/assets/quizbank.json`, `revision.json`, `syllabus.json`; Create `docs/audit/tamil-review-quiz-revision.csv`

**Interfaces:** Produces keys (all optional to the current parsers, which ignore unknown keys): quiz item `q_ta`, `o_ta` (list, same length as `o`), `e_ta`; revision set `title_ta`, `kp_ta` (same length as `kp`), `sa[].q_ta`, `sa[].a_ta`, `gl[].t_ta`, `gl[].d_ta`; syllabus unit and topic `title_ta`, topic `note_ta`.

- [ ] **Step 1:** Extend `validate_content.py --quiz-revision` coverage rules (tests first: missing `o_ta` entry, length mismatch, untranslated item all fail).
- [ ] **Step 2:** Author in batches of one module per commit (29 modules; 292 MCQs, 29 revision sets, 34 topics). Answer indexes and English fields are never altered.
- [ ] **Step 3:** Validate: expected exit 0 with 292/292 questions, 29/29 revision sets, 34/34 topics covered. Generate the review CSV.
- [ ] **Step 4:** Confirm the app still builds and the existing parsers are untouched (`git diff --stat` shows only assets and docs). Push; green CI. **M3b done.**

---

## Milestone M4: native reader, both paths working

### Task 8: Language state and block renderers

**Files:** Modify `Data.kt` (`Store`), `MainActivity.kt`; Create `ui/blocks/Blocks.kt`, `ui/LessonScreen.kt`, `app/src/main/res/font/` (Noto Sans Tamil, OFL); Test `ui/LessonLogicTest.kt`

**Interfaces:** Produces
```kotlin
fun Store.getLang(): Lang                 // parseLang(getString("lang"))
fun Store.putLang(l: Lang)
@Composable fun LessonScreen(chapter: Chapter, lang: Lang, onLang: (Lang)->Unit, zoom: Int,
    studied: Boolean, onStudied: () -> Unit, onBack: () -> Unit, onQuiz: () -> Unit)
fun normaliseTable(header: List<Bi>, rows: List<List<Bi>>): List<List<Bi>>   // pads ragged rows
```

- [ ] **Step 1: Failing tests:** `normaliseTable` pads a one-cell row to the header width with `Bi("")` and never drops cells; `Store`-independent `parseLang` already covered.
- [ ] **Step 2:** Implement; the renderer shows each `Shown` via `Bi.shown(lang)`, Tamil in the Tamil font with 1.35 line-height, `pending` as a small "தமிழ் விரைவில் / Tamil pending" tag; `Unparsed` as a warning card with its text; tables in a horizontal scroll container; credit line footer. Get the font TTF from npm package `@expo-google-fonts/noto-sans-tamil`; if unavailable, rely on the system Tamil font and record this in the PR.
- [ ] **Step 3:** In `MainActivity`, when `ChapterRepo.load(ctx, topic.file, topic.unitRoman)` returns a chapter, open `LessonScreen`; otherwise keep the existing WebView `ReaderScreen` (still present in M4).
- [ ] **Step 4:** Run tests; push; green CI; **M4 done.**

## Milestone M5: native diagrams

### Task 9: Diagram registry and per-chapter diagrams

**Files:** Create `ui/diagram/Diagrams.kt` (plus one file per chapter if it exceeds about 400 lines), `docs/audit/unit1-diagrams.md`

**Interfaces:** Produces `@Composable fun DiagramView(id: String, lang: Lang)`; `DiagramRegistry.ids: Set<String>`; unknown `id` renders a visible "diagram unavailable" placeholder, never crashes.

- [ ] **Step 1: Failing test:** every `diagram` id found in the six chapter JSON files (read from assets in a JVM test) is present in `DiagramRegistry.ids`.
- [ ] **Step 2:** Work from `diagrams-unit1.json` (M2 inventory). One task commit per chapter with diagrams (vectors, advanced vectors, DNA library, Atlas I), then new original diagrams for Fundamentals and Enzymes if they have none. Labels come from a `Bi` map, not hard-coded strings.
- [ ] **Step 3:** For every diagram write an audit entry (source claim, finding, correction, before/after). Do not copy third-party artwork; any outside photo needs an entry in `docs/audit/credits.md` with licence and URL.
- [ ] **Step 4:** Run tests; push; green CI. **M5 done.**

## Milestone M6: bilingual screens and fallback

### Task 10: Quiz, revision, glossary, syllabus read `*_ta`

**Files:** Modify `Data.kt` (add `qTa`, `optionsTa`, `explanationTa`, etc. as optional fields; keep existing constructors source-compatible via defaults), `Study.kt`, `Screens.kt`, `Revise.kt`, `MainActivity.kt`; Test `DataBilingualTest.kt`

- [ ] **Step 1: Failing tests** (JVM, `org.json`): parsing a quiz JSON string with `q_ta` yields `Mcq.qTa`; missing `_ta` yields empty strings; a `o_ta` of wrong length is ignored (falls back to English).
- [ ] **Step 2:** Implement parsing and render all list/detail text through a shared `pick(en, ta, lang)` helper built on `Bi.shown`. Add the language toggle to the About tab.
- [ ] **Step 3:** Run tests; push; green CI.

> **Amendment (completely native):** `FallbackChapterScreen` is now only a parse-failure safety net. Every topic routes to `LessonScreen` (Task 9) because all 29 chapter JSONs exist (M2b). Tasks 8 and 9 (diagrams, reader) cover all 145 diagrams, not 32.

### Task 11: Search and fallback screen

**Files:** Modify `Study.kt` (`buildIndex`, `SearchScreen`); Create `ui/FallbackChapterScreen.kt`; Test `SearchTest.kt`

**Interfaces:** Produces `fun normaliseForSearch(s: String): String` (NFC, lower-case, trimmed) and `@Composable fun FallbackChapterScreen(topic: Topic, lang: Lang, onQuiz: ()->Unit, onRevision: ()->Unit, onGlossary: ()->Unit, onBack: ()->Unit)`.

- [ ] **Step 1: Failing tests:** `normaliseForSearch` of an NFD-composed Tamil string equals the NFC form; searching Tamil `ஒ` forms and English terms both hit the same entry.
- [ ] **Step 2:** Implement. The fallback screen shows the banner "Chapter text for this topic is coming in a later update" with its Tamil text, and three large buttons Quiz, Revision notes, Glossary; it appears for every topic whose chapter JSON is absent.
- [ ] **Step 3:** Run tests; push; green CI; **M6 done.**

## Milestone M7: remove the WebView (last)

### Task 12: Delete WebView path and HTML assets

**Files:** Modify `Screens.kt` (delete `ReaderScreen`), `MainActivity.kt`, `README.md`, `docs/privacy.html` if it mentions web content; Delete `app/src/main/assets/modules/`, `headings.json` if unused; Modify `app/build.gradle.kts` (`versionCode = 7`, `versionName = "2.0.0"`)

- [ ] **Step 1: Failing check:** add a JVM test `NoWebViewTest` that scans `app/src/main/java` for the string `android.webkit` and asserts none are found, and asserts `assets/modules` does not exist.
- [ ] **Step 2:** Remove `ReaderScreen` and every call site (all units already route to `LessonScreen`), the HTML assets and any `Topic.url`.
- [ ] **Step 3:** Fix the README (292 MCQs across 29 modules; native reader; build instructions) and update `docs/RELEASE.md` if it references WebView.
- [ ] **Step 4:** Run tests; push; green CI; download the APK artefact and confirm with `unzip -l` that `assets/modules` is absent and no `android.webkit` appears in `strings classes*.dex`.
- [ ] **Step 5:** Open a PR into `main` with the audit reports linked. **M7 done.**

---

## Findings recorded after M2 (read before starting M5)

- Diagram count is **32**, confirmed: 26 inline in the page plus 6 held in JavaScript template strings (the cDNA stepper in `dna_library_genomic_cdna`). All 32 are content diagrams (labelled, 13 to 71 shape elements each); none is chrome. Fundamentals and Enzymes have none.
- Hand-writing 32 Compose composables with that many elements is slow and error-prone. **Proposed for M5 (decide at its start):** convert each SVG into a compact JSON draw-spec by script and render all diagrams with one generic native Canvas renderer, with labels looked up per language by id. Still 100% native, no SVG library, audit log per diagram. This replaces the "one composable per diagram" wording in Task 9.
- The extractor also moved: buttons are now content unless a profile lists them as UI controls (reported under `dropped` in each chapter JSON and in the report), and prose hidden in JavaScript is checked (`script_orphans`).

## Self-review notes

- Spec coverage: criteria 1 to 6 map to Tasks 12, 3/8/9, 6/7, 11/12, 6/7, 1/12; sections 4.2 to 4.4 to Tasks 4, 9, 8/10; 6.3 to Task 11; 6.4 to Tasks 4, 8; M3b to Task 7; inventory to Tasks 4/5.
- Out of scope here by design: Units II to V chapter conversion, Play release signing.
- Known unverifiable locally: Gradle/Compose compilation (no SDK, Google Maven blocked); CI is the compile check, which is why each milestone gates on a green run.
