package com.gasczoology.animalbiotech

import com.gasczoology.animalbiotech.content.chapterAssetPath
import com.gasczoology.animalbiotech.content.parseChapter
import com.gasczoology.animalbiotech.i18n.Lang
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchCorpusTest {
    private val assets = File("src/main/assets")
    private fun asset(name: String) = JSONObject(File(assets, name).readText())
    private fun <T> JSONArray.readEach(read: (Int) -> T) = List(length(), read)

    private val syllabus = asset("syllabus.json")
    private val legacy = asset("headings.json")
    private val units = syllabus.getJSONArray("units").let { all ->
        all.readEach { i ->
            val unit = all.getJSONObject(i)
            val roman = unit.getString("roman")
            val topics = unit.getJSONArray("topics")
            UnitInfo(roman, unit.getString("title"), topics.readEach { j ->
                val t = topics.getJSONObject(j)
                val file = t.getString("file")
                Topic(t.getString("id"), t.getString("title"), t.optString("note"), file,
                    t.optString("hash"), roman, t.optString("title_ta").ifBlank {
                        syllabus.getJSONObject("modules").optJSONObject(file)?.optString("title_ta").orEmpty()
                    }, t.optString("note_ta"), t.optString("section"))
            })
        }
    }
    private val topicsByFile = units.flatMap { it.topics }.groupBy { it.file }
    private val chapters = topicsByFile.mapValues { (file, topics) ->
        val json = File(assets, chapterAssetPath(file, topics.first().unitRoman)).readText()
        requireNotNull(parseChapter(json).chapter)
    }
    private val revisionSets = asset("revision.json").let { root ->
        root.keys().asSequence().map { file ->
            val item = root.getJSONObject(file)
            val title = item.getString("title")
            val kp = item.getJSONArray("kp")
            val ta = item.getJSONArray("kp_ta")
            val sa = item.getJSONArray("sa")
            val gl = item.getJSONArray("gl")
            RevisionSet(file, title, item.getString("unit"), kp.readEach(kp::getString),
                sa.readEach { i ->
                    val q = sa.getJSONObject(i)
                    Saq(q.getString("q"), q.getString("a"), q.optString("q_ta"), q.optString("a_ta"))
                },
                gl.readEach { i ->
                    val term = gl.getJSONObject(i)
                    Term(term.getString("t"), term.getString("d"), title,
                        term.optString("t_ta"), term.optString("d_ta"))
                }, titleTa = item.optString("title_ta"), keyPointsTa = ta.readEach(ta::getString))
        }.sortedWith(compareBy({ listOf("I", "II", "III", "IV", "V").indexOf(it.unitRoman) }, { it.title })).toList()
    }
    private val indexes = Lang.entries.associateWith { lang ->
        val headings = chapters.flatMap { (file, chapter) ->
            val old = legacy.optJSONArray(file)
            chapterSearchHeadings(chapter, topicsByFile.getValue(file), old?.let { it.readEach(it::getString) }.orEmpty(), lang)
        }
        buildIndex(units, revisionSets, headings, lang)
    }

    @Test fun every_native_section_title_returns_its_section_in_its_own_language() {
        for (lang in Lang.entries) {
            val index = indexes.getValue(lang)
            val failures = chapters.flatMap { (file, chapter) ->
                chapter.sections.mapNotNull { section ->
                    val title = if (lang == Lang.EN) section.title.en else section.title.ta
                    if (searchHits(index, title).any { it.file == file && it.sectionId == section.id }) null
                    else "$lang $file/${section.id}: $title"
                }
            }
            assertTrue(failures.joinToString("\n"), failures.isEmpty())
        }
    }

    @Test fun one_distinctive_keyword_in_each_language_finds_every_chapter() {
        assertEquals(chapters.keys, chapterKeywords.map { it.first }.toSet())
        val misses = chapterKeywords.flatMap { (file, en, ta) ->
            listOf(Lang.EN to en, Lang.TA to ta).mapNotNull { (lang, query) ->
                if (searchHits(indexes.getValue(lang), query).any { it.file == file }) null else "$lang $file: $query"
            }
        }
        assertTrue(misses.joinToString("\n"), misses.isEmpty())
    }

    @Test fun aliases_and_decorated_queries_show_the_native_title_at_the_correct_destination() {
        for ((topic, section, queries) in aliases) {
            for ((lang, query) in listOf(Lang.EN to queries.first, Lang.TA to queries.second)) {
                val hit = searchHits(indexes.getValue(lang), "🔬 • $query")
                    .firstOrNull { it.topic?.id == topic && it.sectionId == section }
                assertTrue("$lang $topic/$section: $query", hit != null)
                val nativeTitle = chapters.getValue(requireNotNull(hit).file).sections.single { it.id == section }.title
                assertEquals(if (lang == Lang.EN) nativeTitle.en else nativeTitle.ta, hit.title)
            }
        }
        val recovered = searchHits(indexes.getValue(Lang.EN), "❄️ Cryopreservation — Cell Banking Flow")
        assertTrue(recovered.any { it.topic?.id == "t19" && it.sectionId == "s06" && it.title == "Cell Lines" })
    }

    @Test fun legacy_quiz_heading_does_not_open_a_reader_section() {
        val english = indexes.getValue(Lang.EN)
        for (query in listOf("Fundamentals of Biotechnology — MCQ", "Blotting Techniques — MCQ")) {
            assertTrue(query, searchHits(english, query).none { it.sectionId != null })
        }
    }

    private val aliases = listOf(
        Triple("t19", "s02", "Annotated Animal Cell — Key Organelles" to "விலங்கு செல்லின் முக்கிய நுண்ணுறுப்புகள்"),
        Triple("t19", "s03", "Sterilization Methods in Cell Culture" to "செல் வளர்ப்பில் கிருமிநீக்க முறைகள்"),
        Triple("t19", "s06", "Primary Culture → Cell Line → Continuous Line" to "முதல்நிலை வளர்ப்பு செல் வரிசை தொடர் செல் வரிசை"),
        Triple("t19", "s07", "Stem Cell Potency Hierarchy" to "தண்டு செல்களின் வேறுபாடடையும் திறன் நிலைகள்"),
        Triple("t19", "s08", "Grid Method — Organ Culture Setup Diagram" to "உறுப்பு வளர்ப்பின் வலைத்தட்டு முறை"),
        Triple("t20", "s08", "Industrial Ethanol Production — Process Flow" to "தொழிற்சாலை எத்தனால் உற்பத்திப் படிகள்"),
        Triple("t24", "s02", "Common Confusions" to "பொதுவான கருத்துக் குழப்பங்கள்"),
        Triple("t1", "s08", "Branches of Biotechnology — Concept Map" to "உயிரித் தொழில்நுட்பத்தின் கிளைகள் கருத்து வரைபடம்"),
        Triple("t10", "s10", "Sanger Sequencing Limitations" to "சாங்கர் வரிசையாக்கத்தின் வரம்புகளும்"),
    )

    private val chapterKeywords = listOf(
        Triple("advanced_vectors_cosmid_yac_sv40_transposons.html", "cosmid", "காஸ்மிட்"),
        Triple("atlas1_fundamentals_vectors.html", "pBR322", "பிளாஸ்மிட்"),
        Triple("biotechnology_fundamentals.html", "scope", "வரம்பு"),
        Triple("dna_library_genomic_cdna.html", "genomic", "நூலகம்"),
        Triple("enzymes_genetic_engineering.html", "restriction", "ரெஸ்ட்ரிக்ஷன்"),
        Triple("vectors_genetic_engineering.html", "plasmids", "பிளாஸ்மிட்கள்"),
        Triple("atlas2_techniques.html", "electrophoresis", "மின்னாற்பிரித்தல்"),
        Triple("blotting_techniques.html", "northern", "நார்தர்ன்"),
        Triple("gel_electrophoresis.html", "agarose", "அகரோஸ்"),
        Triple("gene_cloning_steps_selection.html", "cloning", "குளோனாக்கம்"),
        Triple("gene_transfer_methods_rdna_host_cells.html", "transfection", "செலுத்தல்"),
        Triple("pcr_principle_types_applications.html", "PCR", "பெருக்கம்"),
        Triple("sanger_dna_sequencing.html", "Sanger", "சேங்கர்"),
        Triple("screening_selection_recombinants.html", "inactivation", "செயலிழப்பு"),
        Triple("atlas3_transgenic_medical.html", "Dolly", "டாலி"),
        Triple("crispr_gene_editing_app.html", "CRISPR", "திருத்தம்"),
        Triple("gene_therapy_app.html", "therapy", "சிகிச்சை"),
        Triple("hybridoma_monoclonal_antibodies.html", "hybridoma", "ஹைப்ரிடோமா"),
        Triple("molecular_markers_app.html", "polymorphism", "பல்லுருவமைப்பு"),
        Triple("superbug_bioremediation_bioweapons.html", "bioremediation", "சீரமைப்பு"),
        Triple("transgenic_animal_technology.html", "transgenesis", "மரபணுமாற்றம்"),
        Triple("animal_cell_culture.html", "cryopreservation", "கிரையோபிரிசர்வேஷன்"),
        Triple("atlas4_culture_society.html", "organ culture", "உறுப்பு வளர்ப்பு"),
        Triple("hgp_ethics_ipr.html", "mapping", "வரைபடமாக்கல்"),
        Triple("industrial_biotech_fermentation.html", "ethanol", "எத்தனால்"),
        Triple("atlas5_bioinformatics.html", "dot plots", "புள்ளி வரைபடங்களும்"),
        Triple("bioinformatics_databases_app.html", "DDBJ", "தரவுத்தளங்கள்"),
        Triple("bioinformatics_tools_alignment_blast.html", "FASTA", "ஒப்புமை"),
        Triple("protein_databases_app.html", "UniProt", "புரத வளம்"),
    )
}
