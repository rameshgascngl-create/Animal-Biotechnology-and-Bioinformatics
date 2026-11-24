# Decisions for RR

Decision brief for Animal Biotechnology and Bioinformatics. Expand the two tables for the individual decisions. All Tamil below is draft.

| Decision | Options | Recommendation | If you do nothing |
|---|---|---|---|
| Backup | **A** keeps study records local with the current Android backup and transfer exclusions. **B** allows Android to back up or transfer only the app’s `animal_biotech.xml` preferences, with matching policy updates. | Keep A for this release until supported cloud restore and real-phone migration are demonstrated for B. | A stays in effect. Students should plan for a fresh record after a device change or app data reset. |
| 28 legacy quiz headings | Leave the reader-only search route, or add chapter-scoped results that open the existing chapter quizzes directly. | Approve the mapped quiz targets below as a separate routing change after review. | Search keeps its current section behaviour. Chapter practice is still accessible through the Quiz tab and reader quiz action. |
| Merge `fix/rc2-syllabus-navigation` | Hold main, or approve the publishing change first and then merge the syllabus work. | Approve the publishing safeguard and land it before or atomically with the syllabus changes so the merge push uses the guarded workflow. | Main stays at the checked baseline until you authorise a change. Its current Build APK push can still replace the public `latest` release. |
| 25 Tamil wording conflicts | Approve individual proposals, revise them, or defer all decisions. | Review the evidence and approve terms row by row using the technique-chapter rule. | The app terms and draft ledger remain as they are. |
| Staged student release | Hold for complete Tamil review, or ship English and reviewed Tamil with a draft-status flag and English fallback where Tamil is withheld. | If an early student release is essential, prefer the staged option after the language gates and their tests are ready. | The current build remains a candidate with its existing draft Tamil behaviour. |

**Staged-release code cost.** Estimated three to five engineering days, excluding language review and phone testing. Use the reader’s existing Tamil status model and add stable per-item review metadata where it is missing in revision, quiz and figure assets. Gate visible text, search indexing and accessible labels consistently across the reader, figures, quizzes, Revise, Glossary and Flashcards. Show a draft-status flag alongside the English fallback. Preserve saved IDs, and test mixed-language navigation, accessibility and empty states. All present Tamil remains draft, so a strict reviewed-only gate starts with English content.

<details>
<summary>28 legacy quiz-heading destinations</summary>

**Source mapping.** On main `fb2a8e50cc9f90f386011ff55662f0d8527073fa`, tapping each legacy Search heading opened the first syllabus topic for its file in the reader. The existing search on the candidate `f5294a395fc8f7fdaa37580a9e1fa218a7794fe1` still has a reader callback only. Each proposed target is an existing Quiz tab bank entry on the candidate, with the chapter shown for repeated labels. These are source mappings. Student-device routing is not verified.

| Legacy search heading | Old destination on main — reader topic | Proposed new destination — chapter quiz (bank key, questions) |
|---|---|---|
| Test Your Knowledge — MCQ Quiz | Cosmid, YAC, SV40 and transposons as vectors (`t4`) | Advanced Vectors – Cosmid, YAC, SV40 & Transposons quiz (`advanced_vectors_cosmid_yac_sv40_transposons.html`, 10) |
| Atlas I: Fundamentals & Vectors — MCQ | Diagram Atlas I: biotech colour code, enzyme toolkit, vector maps, library flow charts (`atlasI`) | Diagram Atlas I: Fundamentals & Vectors quiz (`atlas1_fundamentals_vectors.html`, 10) |
| Atlas II: Techniques — MCQ | Diagram Atlas II: cloning, gene transfer, selection, Sanger, PCR and gel diagrams (`atlasII`) | Diagram Atlas II: Cloning & Techniques quiz (`atlas2_techniques.html`, 10) |
| Atlas III: Transgenic & Medical — MCQ | Diagram Atlas III: transgenic, knockout, Dolly, hybridoma, gene therapy, CRISPR, markers (`atlasIII`) | Diagram Atlas III: Transgenic & Medical quiz (`atlas3_transgenic_medical.html`, 10) |
| Atlas IV: Culture, HGP & Ethics — MCQ | Diagram Atlas IV: culture, stem cells, bioreactor, ethanol, HGP, ethics and patents (`atlasIV`) | Diagram Atlas IV: Culture, HGP & Ethics quiz (`atlas4_culture_society.html`, 11) |
| Atlas V: Bioinformatics — MCQ | Diagram Atlas V: databases, alignment matrix, BLAST, MSA, structure, NGS pipeline (`atlasV`) | Diagram Atlas V: Bioinformatics quiz (`atlas5_bioinformatics.html`, 11) |
| Self-Assessment Quiz | Definition; nucleotide databases (GenBank, EMBL, DDBJ) (`t24`) | Bioinformatics: Nucleotide Sequence Databases quiz (`bioinformatics_databases_app.html`, 10) |
| Alignment, BLAST & Structure Tools — MCQ | Sequence alignment: pairwise and multiple (`t26`) | Alignment, BLAST & Structure Tools quiz (`bioinformatics_tools_alignment_blast.html`, 10) |
| Fundamentals of Biotechnology — MCQ | Definition, Scope and Importance of Biotechnology (`t1`) | Fundamentals of Biotechnology — Definition, Scope & Importance quiz (`biotechnology_fundamentals.html`, 10) |
| Blotting Techniques — MCQ | Blotting techniques: Southern, Northern, Western (`t9`) | Blotting Techniques quiz (`blotting_techniques.html`, 10) |
| Self-Assessment Quiz | CRISPR gene editing (`t17`) | CRISPR Gene Editing quiz (`crispr_gene_editing_app.html`, 10) |
| Test Your Knowledge — MCQ Quiz | DNA library: genomic and cDNA (`t5`) | DNA Library – Genomic & cDNA Library quiz (`dna_library_genomic_cdna.html`, 10) |
| Test Your Knowledge — MCQ Quiz | Enzymes: restriction enzymes and enzymes useful for genetic engineering (`t2`) | Enzymes in Genetic Engineering – Interactive Learning App quiz (`enzymes_genetic_engineering.html`, 10) |
| Test Your Knowledge — MCQ Quiz | Gel electrophoresis (`t12`) | Gel Electrophoresis quiz (`gel_electrophoresis.html`, 10) |
| Test Your Knowledge — MCQ Quiz | Gene cloning: steps and selection of clones (`t6`) | Gene Cloning – Steps & Selection of Clones quiz (`gene_cloning_steps_selection.html`, 10) |
| Self-Assessment Quiz | Gene therapy: ex vivo and in vivo; role in cancer treatment (`t16`) | Gene Therapy – Ex Vivo & In Vivo quiz (`gene_therapy_app.html`, 10) |
| Test Your Knowledge — MCQ Quiz | Introducing rDNA into host cells (transformation, transfection, transduction, microinjection, electroporation, liposomes) (`t7`) | Introduction of Recombinant DNA into Host Cells quiz (`gene_transfer_methods_rdna_host_cells.html`, 10) |
| Human Genome Project, Ethics & IPR — MCQ | Human Genome Project: mapping and applications (`t21`) | Human Genome Project, Ethics & IPR quiz (`hgp_ethics_ipr.html`, 10) |
| Hybridoma & Monoclonal Antibodies — MCQ | Hybridoma technology and monoclonal antibodies (`t15`) | Hybridoma & Monoclonal Antibodies quiz (`hybridoma_monoclonal_antibodies.html`, 10) |
| Fermentation & Ethanol Production — MCQ | Industrial biotechnology: fermentation and ethanol production (`t20`) | Industrial Biotechnology — Fermentation & Ethanol Production quiz (`industrial_biotech_fermentation.html`, 10) |
| Self-Assessment Quiz | Molecular markers: RFLP, RAPD, DNA fingerprinting (`t18`) | Molecular Markers: RFLP, RAPD & DNA Fingerprinting quiz (`molecular_markers_app.html`, 10) |
| Test Your Knowledge — MCQ Quiz | PCR: principle, types and applications (`t11`) | PCR: Principle, Types & Applications quiz (`pcr_principle_types_applications.html`, 10) |
| Self-Assessment Quiz | Protein databases: SWISS-PROT, UniProt; structural database PDB (`t25`) | Protein Databases: SWISS-PROT, UniProt & PDB quiz (`protein_databases_app.html`, 10) |
| Test Your Knowledge — MCQ Quiz | DNA sequencing: Sanger method (`t10`) | DNA Sequencing: Sanger Method quiz (`sanger_dna_sequencing.html`, 10) |
| Test Your Knowledge — MCQ Quiz | Screening and selection of recombinants (direct selection, insertional inactivation, blue-white, colony hybridisation) (`t8`) | Screening & Selection of Recombinants quiz (`screening_selection_recombinants.html`, 10) |
| Superbug, Bioremediation & Bioweapons — MCQ | Superbug, bioremediation and bioweapons (`t14`) | Superbug, Bioremediation & Bioweapons quiz (`superbug_bioremediation_bioweapons.html`, 10) |
| Transgenic Animal Technology — MCQ | Transgenesis: transgenes, knock-out mice, Dolly, animals as bioreactors (`t13`) | Transgenic Animal Technology quiz (`transgenic_animal_technology.html`, 10) |
| Test Your Knowledge — MCQ Quiz | Vectors: plasmids, pBR322, Ti plasmid, lambda, M13 (`t3`) | Vectors in Genetic Engineering – Interactive Learning App quiz (`vectors_genetic_engineering.html`, 10) |

</details>

<details>
<summary>25 wording conflicts and draft proposals</summary>

Use the State Board term from the chapter that covers the technique and list another attested form as a glossary synonym. Where no textbook term exists, retain the Latin symbol or abbreviation with a short descriptive Tamil phrase marked `descriptive, not State Board`. Contextual evidence can support a draft label with an explicit note about editorial normalisation. Textbook pages are not verified against the official edition. [Ledger and citations](tamil-terminology-ledger.csv)

| English headword and chapter where repeated | App term (revision / reader) | Textbook wording | Draft proposal for RR |
|---|---|---|---|
| cos Site — Advanced Vectors – Cosmid, YAC, SV40 & Transposons | cos இடம் / cos களம் | ஒட்டிணைவுக் களம் | ஒட்டிணைவுக் களம் |
| cos site — Diagram Atlas I: Fundamentals & Vectors | cos இடம் / cos களம் (cos site) | ஒட்டிணைவுக் களம் | ஒட்டிணைவுக் களம் |
| Genomic DNA | மரபணுத்தொகை DNA / மரபணு தொகைய DNA | மரபணுத் தொகையின் (Genomic DNA) DNA-விலிருந்து | மரபணுத் தொகையின் DNA — normalised from the contextual phrase. Approve the label explicitly. |
| Cathode | எதிர்மின்முனை (கேத்தோடு) / கேதோடு | எதிர்மின் | எதிர்மின் — use the explicit textbook label. Retain the chapter context when defining the electrode. |
| Blue-White Screening | நீல–வெள்ளைச் சலிப்பு / நீலம்-வெள்ளை சலிக்கை | நீலம் – வெண்மை காலனி தேர்வு முறை | நீலம் – வெண்மை காலனி தேர்வு முறை |
| Inoculum | உட்செலுத்து வளர்ப்பு / இனோக்குலம் | உட்புகட்டலின் | உட்புகட்டல் — remove the case ending from the cited form. |
| Recombinant | மறுசேர்க்கை / மறுகூட்டிணைவு | மறுகூட்டிணைவு அடைந்த செல்கள் | மறுகூட்டிணைவு அடைந்த செல்கள் — use in the clone-screening context. |
| Bioaugmentation | உயிரி மிகைப்பெருக்கம் / உயிர்ப்பெருக்கம் | உயிரி வழி பெருக்குதல் | உயிரி வழி பெருக்குதல் |
| Origin of Replication (ori) | பெருக்கத் தொடக்கப் புள்ளி (ori) / பெருக்கமடைதலின் தோற்றம் (ori) | பெருக்கமடைதலுக்கான ஒரு தோற்றுவியை | பெருக்கமடைதலுக்கான தோற்றுவி (ori) — remove the case ending and retain the abbreviation. |
| cos Site — Vectors in Genetic Engineering – Interactive Learning App | cos இடம் / cos களம் | ஒட்டிணைவுக் களம் | ஒட்டிணைவுக் களம் |
| Insert — Advanced Vectors – Cosmid, YAC, SV40 & Transposons | செருகல் / செருகி | செருகல் DNA | செருகல் DNA — use the technique text. Glossary synonym செருகி DNA from the Botany glossary. |
| Replica plating — Diagram Atlas II: Cloning & Techniques | நகல் தட்டுப்படுத்தல் / படிநகல் தட்டு | நகல் தட்டிடுதல் தொழில்நுட்பமுறை | நகல் தட்டிடுதல் தொழில்நுட்பமுறை |
| Sparger | காற்றுப் பாய்ச்சி / ஸ்பார்ஜர் | தூவுகலம் | தூவுகலம் |
| Bioremediation | உயிரிவழி சீரமைப்பு / உயிரிவழிச் சீரமைப்பு | உயிரியத்தீர்வு | உயிரியத்தீர்வு — use the dedicated Bio-Zoology section. Glossary synonym உயிரி வழித்திருத்தம். |
| Monoclonal Antibodies (MAbs) | ஒற்றைக்குளோன் எதிர்ப்பொருட்கள் (MAbs) / ஒற்றைப்படி எதிர்ப்பொருள்கள் (MAbs) | ஒற்றை படியாக்க எதிர்பொருட்கள் | ஒற்றை படியாக்க எதிர்பொருட்கள் (MAbs) |
| Blotting | புளாட்டிங் / பிளாட்டிங் | ஒற்றியெடுப்பு | ஒற்றியெடுப்பு |
| Selection Marker | தேர்வுக் குறிப்பான் / தெரிவு அடையாளக்குறி | தேர்ந்தெடுக்கும் அடையாளக்குறி | தேர்ந்தெடுக்கும் அடையாளக்குறி |
| Microinjection | நுண்ணூசி செலுத்தல் / நுண்செலுத்தல் &#124;&#124; நுண்செலுத்தல் | நுண் உட்செலுத்துதல் | நுண் உட்செலுத்துதல் — use the technique heading. Glossary synonym உட்செலுத்துதல் in the microinjection context. |
| Annealing | பிணைதல் / அனீலிங் | முதன்மை இணைப்பு இழை பதப்படுத்தல் | முதன்மை இணைப்பு இழை பதப்படுத்தல் |
| Denaturation | இயல்பிழத்தல் / டினேச்சுரேஷன் | இயல்பு திரிபு | இயல்பு திரிபு — use the PCR chapter. Glossary synonym இயல்பிழத்தல் from the blotting table. |
| Template DNA — PCR: Principle, Types & Applications | வார்ப்புரு DNA / டெம்ப்ளேட் DNA | அச்சு வார்ப்பு டி.என்.ஏ | அச்சு வார்ப்பு டி.என்.ஏ |
| Template DNA — DNA Sequencing: Sanger Method | வார்ப்புரு DNA / டெம்ப்ளேட் DNA | அச்சு வார்ப்பு டி.என்.ஏ | அச்சு வார்ப்பு டி.என்.ஏ |
| Replica Plating — Screening & Selection of Recombinants | பிரதியெடுப்புத் தட்டுமுறை / ரெப்ளிகா பிளேட்டிங் | நகல் தட்டிடுதல் தொழில்நுட்பமுறை | நகல் தட்டிடுதல் தொழில்நுட்பமுறை |
| Selectable Marker | தேர்வுக் குறிப்பான் / தெரிவு குறிப்பான் | தேர்ந்தெடுக்கும் அடையாளக்குறி | தேர்ந்தெடுக்கும் அடையாளக்குறி |
| Insert — Vectors in Genetic Engineering – Interactive Learning App | உட்செருகு / செருகி | செருகல் DNA | செருகல் DNA — use the technique text. Glossary synonym செருகி DNA from the Botany glossary. |

</details>
