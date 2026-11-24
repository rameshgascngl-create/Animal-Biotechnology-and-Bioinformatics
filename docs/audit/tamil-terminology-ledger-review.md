# Tamil terminology ledger review

## Quick guide for RR review

**Read the CSV.** Each row identifies an English headword and an app chapter. Compare `revision_json_tamil` with `reader_tamil` at `reader_section`. The `state_board_*` columns give the recorded textbook term, source and printed page. The search columns record the queries, candidate PDF pages, verdict and follow-up. The proposal columns record suggested canonical terms, glossary synonyms, descriptive options and review actions. `draft, needs RR review` means the row still awaits your approval.

**Read `textbook vs app`.** `matches revision` means the recorded textbook headword agrees with the revision entry only. `matches reader` means it agrees with the lesson reader only. `matches both` means both agree with it, and `matches neither` means both differ from it. A blank means the ledger has no printed term to compare. Check `difference_type` and `recheck_note` for spelling variants and contextual evidence.

| Recheck verdict | Rows | Decision for RR |
|---|---:|---|
| found term as printed | 14 | Review the cited form for adoption as the shared term. |
| found a different or related spelling | 12 | Check whether the citation supports the same term, a spelling variant or only a related expression before choosing the shared term. |
| not found | 38 | Leave the row unresolved if the evidence is insufficient. Use the descriptive rule below if you determine that no textbook term exists. |

**Resolve conflicts.** When the State Board gives two terms, use the term from the chapter that covers the technique and list the other as a glossary synonym. When no textbook term exists, keep the Latin symbol or abbreviation with a short descriptive Tamil phrase and mark it `descriptive, not State Board`.

<details>
<summary>41 rows marked “matches neither” with printed textbook pages</summary>

| English headword | App chapter | TN SCERT Class XII source and printed page |
|---|---|---|
| Telomere | Advanced Vectors – Cosmid, YAC, SV40 & Transposons | [Botany, Ch. 3, p. 85](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=93) |
| cos Site | Advanced Vectors – Cosmid, YAC, SV40 & Transposons | [Botany, Ch. 4, p. 130](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=138) |
| cos site | Diagram Atlas I: Fundamentals & Vectors | [Botany, Ch. 4, p. 130](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=138) |
| Ligation | Diagram Atlas II: Cloning & Techniques | [Botany, Ch. 4, p. 108](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=116) |
| Transgenic Organism | Fundamentals of Biotechnology — Definition, Scope & Importance | [Botany, Ch. 4, p. 120](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=128) |
| Genomic DNA | DNA Library – Genomic & cDNA Library | [Botany, Ch. 4, p. 130](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=138) |
| Recognition Sequence | Enzymes in Genetic Engineering – Interactive Learning App | [Botany, Ch. 4, p. 107](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=115) |
| Cathode | Gel Electrophoresis | [Microbiology, Ch. 2, p. 18](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Microbiology_TM_optimised.pdf#page=22) |
| Blue-White Screening | Gene Cloning – Steps & Selection of Clones | [Botany, Ch. 4, p. 114](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=122) |
| Ligation | Gene Cloning – Steps & Selection of Clones | [Botany, Ch. 4, p. 108](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=116) |
| Glycolysis (EMP Pathway) | Industrial Biotechnology — Fermentation & Ethanol Production | [Microbiology, Ch. 4, p. 48](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Microbiology_TM_optimised.pdf#page=52) |
| Inoculum | Industrial Biotechnology — Fermentation & Ethanol Production | [Botany, Ch. 4, p. 103](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=111) |
| dNTP (Deoxynucleotide) | DNA Sequencing: Sanger Method | [Bio-Zoology, Ch. 5, p. 79](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=87) |
| ddNTP (Dideoxynucleotide) | DNA Sequencing: Sanger Method | [Microbiology, Ch. 1, p. 8](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Microbiology_TM_optimised.pdf#page=12) |
| Recombinant | Screening & Selection of Recombinants | [Botany, Ch. 4, p. 114](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=122) |
| Bioaugmentation | Superbug, Bioremediation & Bioweapons | [Botany, Ch. 4, p. 124](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=132) |
| cos Site | Vectors in Genetic Engineering – Interactive Learning App | [Botany, Ch. 4, p. 130](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=138) |
| Insert | Advanced Vectors – Cosmid, YAC, SV40 & Transposons | [Botany, Ch. 4, p. 117](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=125) |
| Blunt end | Diagram Atlas I: Fundamentals & Vectors | [Botany, Ch. 4, p. 107](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=115) |
| Sticky end | Diagram Atlas I: Fundamentals & Vectors | [Botany, Ch. 4, p. 107](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=115) |
| Transposon | Diagram Atlas I: Fundamentals & Vectors | [Botany, Ch. 4, p. 127](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=135) |
| Replica plating | Diagram Atlas II: Cloning & Techniques | [Botany, Ch. 4, p. 114](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=122) |
| Sparger | Diagram Atlas IV: Culture, HGP & Ethics | [Microbiology, Ch. 6, p. 95](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Microbiology_TM_optimised.pdf#page=99) |
| Bioreactor | Fundamentals of Biotechnology — Definition, Scope & Importance | [Botany, Ch. 4, p. 103](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=111) |
| Bioremediation | Fundamentals of Biotechnology — Definition, Scope & Importance | [Bio-Zoology, Ch. 8, p. 170](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=178) |
| Biotechnology | Fundamentals of Biotechnology — Definition, Scope & Importance | [Botany, Ch. 4, p. 108](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=116) |
| Monoclonal Antibodies (MAbs) | Fundamentals of Biotechnology — Definition, Scope & Importance | [Bio-Zoology, Ch. 9, p. 186](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=194) |
| Restriction Endonuclease | Fundamentals of Biotechnology — Definition, Scope & Importance | [Botany, Ch. 4, p. 106](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=114) |
| Blotting | Blotting Techniques | [Botany, Ch. 4, p. 116](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=124) |
| Restriction Endonuclease | DNA Library – Genomic & cDNA Library | [Botany, Ch. 4, p. 106](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=114) |
| Selection Marker | Gene Cloning – Steps & Selection of Clones | [Botany, Ch. 4, p. 109](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=117) |
| Microinjection | Introduction of Recombinant DNA into Host Cells | [Botany, Ch. 4, p. 112](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=120) |
| Genome | Human Genome Project, Ethics & IPR | [Botany, Ch. 4, p. 130](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=138) |
| Annealing | PCR: Principle, Types & Applications | [Bio-Zoology, Ch. 9, p. 183](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=191) |
| Denaturation | PCR: Principle, Types & Applications | [Bio-Zoology, Ch. 9, p. 183](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=191) |
| Template DNA | PCR: Principle, Types & Applications | [Bio-Zoology, Ch. 9, p. 183](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=191) |
| Template DNA | DNA Sequencing: Sanger Method | [Bio-Zoology, Ch. 9, p. 183](https://www.winmeen.com/wp-content/uploads/2020/09/12th_Std_Biology_Zoology_TM_optimised.pdf#page=191) |
| Replica Plating | Screening & Selection of Recombinants | [Botany, Ch. 4, p. 114](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=122) |
| Selectable Marker | Screening & Selection of Recombinants | [Botany, Ch. 4, p. 109](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=117) |
| Insert | Vectors in Genetic Engineering – Interactive Learning App | [Botany, Ch. 4, p. 117](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=125) |
| Multiple Cloning Site (MCS) | Vectors in Genetic Engineering – Interactive Learning App | [Botany, Ch. 4, p. 109](https://athiyamanteam.com/wp-content/uploads/2019/11/New-TN-Samacheer-Book-12th-Std-Botany-TM-Athiyaman-Team-Part-1.pdf#page=117) |

</details>

The [terminology ledger](tamil-terminology-ledger.csv) retains 122 chapter–term pairs and 127 reader locations. Every row remains **draft, needs RR review**.

The 64 rows previously unresolved were searched again against the locally retained Tamil-medium TN SCERT Class XII Botany, Bio-Zoology and Microbiology PDFs. The exact English headword, any parenthetical forms, and the recorded Tamil revision and reader forms were searched case-insensitively after Unicode, whitespace and dash normalisation.

| Recheck verdict | Pairs |
|---|---:|
| found term as printed | 14 |
| found a different or related spelling | 12 |
| not found | 38 |

A targeted follow-up on five of the 43 not-found rows checked a plural glossary entry and alternate spellings against the rendered textbook pages. The counts above include that follow-up.

Candidate columns use one-based PDF page numbers, including front matter. The evidence column uses the printed textbook page, chapter and a PDF page link. Candidate hits can be unrelated. The follow-up column records additional contextual checks and search phrases. A not-found verdict applies only to this search, including its PDF text extraction limitations.

| Textbook vs app | Pairs with printed evidence |
|---|---:|
| matches revision | 7 |
| matches reader | 31 |
| matches both | 5 |
| matches neither | 41 |

The comparison uses headwords. Parenthetical annotations and acronym wrappers around a Tamil term are excluded. For Anode and Inoculum, an explicitly documented case suffix is removed only for comparison. For Phage / Bacteriophage, the comparison joins the two spellings printed separately in the Botany chapter. A reader match can occur at any recorded section for that pair. Related contextual evidence is flagged separately from spelling variants and exact headword matches.

The seven descriptive options, canonical suggestions and existing draft review status are preserved. All proposed review actions are suggestions for RR review.
