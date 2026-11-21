# RC3 release work

Estimates refer to engineering and editorial time. Reviewer availability and device access can extend calendar time. The unpacked RC2 audit provides asset observations. Behaviour and accessibility require testing in the running app.

| Order | Work | Estimate | Evidence to record |
| --- | --- | --- | --- |
| 1 | Tamil revision, flashcards, glossary and search | 1–2 days including UI verification | Check Tamil and English on all four screens, chapter titles, missing-translation behaviour and flashcard progress after a language switch. This branch wires the bundled translations into these screens. |
| 2 | Remove web-only instructions and fix extraction defects | 1–2 days | Review the 138 reported matches in context. Keep valid instructions for native controls. Regenerate chapters from source and validate figure anchors. This branch fixes seven joined answer labels. |
| 3 | Review Tamil and subject content | 8–15 editorial days plus independent review | Review definitions, bracketed terms and repetitions against the source and State Board texts. Record approved terms and separate intentional acronyms from English prose. Obtain independent subject and Tamil review. |
| 4 | Improve chapter structure and active recall | 4–7 days | Replace metadata-only openings with chapter-specific learning objectives. Merge sentence fragments and group related points. Use exam and warning callouts where justified. Add model answers or marking points before converting exam prompts to reveal cards. Check saved positions and anchors after structural edits. |
| 5 | Correct and ship prioritised figures | 4–8 days | Inspect the right edge of the reported Atlas IV plate and the navy cell-culture figure in the actual reader. Verify the 19 prepared plates against shipped assets. Check anchor placement, legibility, useful margins and dark-mode contrast. Add content-specific descriptions for meaningful diagrams. |
| 6 | Accessibility and release gates | 2–4 days with a device and signing access | Test TalkBack order and figure controls, text at 200%, portrait and landscape, and dark mode. Build a signed AAB and verify its certificate, privacy statement and install or update path. Benchmark any WebP conversion on representative plates before adopting it. |
| 7 | Practice and feedback enhancements | 2–4 days after release blockers | Verify the existing per-chapter best score in the running quiz UI. Use actual attempts to identify weak topics. Add marking points and feedback routes when their educational scope and destination are established. |

## Current source checks

- The bilingual study assets already included Tamil for the revision sets. The app previously ignored those fields. Their language review remains part of item 3.
- The native reader already has a contents sheet and a reading-time estimate. Verify both on a phone. Collapsible headings and chapter-specific opening summaries are separate changes.
- The release build already configures R8 and resource shrinking. The repository includes a privacy page and release signing documentation. Store signing, privacy disclosure and an installable update still require release verification.
- The earlier figure-size and alt-text counts describe the unpacked release. Recount on the RC3 build after figure work. Treat the proposed WebP savings as a hypothesis until measured.
