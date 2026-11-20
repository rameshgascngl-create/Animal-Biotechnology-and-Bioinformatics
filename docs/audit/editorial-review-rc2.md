# RC2 chapter content and Tamil editorial review

The baseline is the Android reader at commit `9fd9ab1`. The inventory reads every bilingual chapter JSON file. It counts text fields, including both table cells and prose, and looks for common references to web controls such as hovering and click-to-reveal. The inventory describes coverage and review status. Scientific accuracy and natural Tamil require reading the text in context.

| Unit | Chapter files | Sections | Tamil draft fields | Tamil bracketed fields | Empty table cells | Web-control references found |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| I | 6 | 75 | 1796 | 62 | 0 | 7 |
| II | 8 | 108 | 2314 | 40 | 9 | 7 |
| III | 7 | 80 | 1763 | 44 | 9 | 3 |
| IV | 4 | 39 | 1231 | 39 | 8 | 2 |
| V | 4 | 47 | 1241 | 23 | 6 | 2 |
| Total | 29 | 349 | 8,345 | 208 | 32 | 21 |

All populated English fields have Tamil text. The 32 empty fields have blank English cells as well. The scan found zero unparsed blocks and zero fields marked reviewed. Bracketed fields need a terminology decision in context. Counts cover the lesson JSON, including chapter atlases. Figures, quizbank and revision data require their own review.

## Editorial changes in this batch

- **Unit I, Fundamentals of Biotechnology.** The opening now uses yeast in bread-making and cultured cells producing therapeutic proteins to distinguish biological activity from its intended use. The comparison table separates observation-based selection from sequence-targeted methods, explains the need to verify outcomes, dates the double-helix model and treats contamination as a risk in traditional production. The environmental example distinguishes degradation of organic pollutants from changes in the form or mobility of metals. It distinguishes methanogenic archaea from the other organisms involved in digestion and gives treatment performance in context. The insulin case distinguishes the 1982 approval and scalable production from affordability and access. [CBD Article 2](https://www.cbd.int/convention/articles/default.shtml?a=cbd-02), [US EPA on bioremediation](https://www.epa.gov/sites/default/files/2015-04/documents/introductiontoinsitubioremediationofgroundwater_dec2013.pdf), [FDA clinical review and approval chronology](https://www.fda.gov/media/71387/download)
- **Unit V, Sequence Alignment, BLAST and Structure Viewers.** The comparison table defines orthology by speciation and paralogy by duplication. The multiple alignment explanation uses the size of the exact score grid to explain the need for heuristics. The BLAST lesson gives separate meanings for E-value, query coverage and percent identity, with a 20-of-200 residue example. The structure section explains that the B-factor field can encode confidence in AlphaFold files. [NCBI on orthologues and paralogues](https://www.ncbi.nlm.nih.gov/books/NBK20255/), [NCBI BLAST Glossary](https://www.ncbi.nlm.nih.gov/books/NBK62051/), [AlphaFold DB FAQ](https://alphafold.ebi.ac.uk/faq)

Tamil changes cover the introductory definitions and learning objectives, selected tables and case studies, and the sequence-analysis explanations. The edits favour short textbook sentences, explanations of how terms relate to observations, and stable scientific symbols. Existing draft status remains pending faculty verification. Two obsolete web instructions were replaced with descriptions of the actual lesson content.

Further editorial review should compare the other chapters and the separate revision and quiz assets against their figures, verify terminology in context, and assess remaining web instructions against the Android reader.
