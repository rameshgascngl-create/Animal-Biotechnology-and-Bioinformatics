package com.gasczoology.animalbiotech.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChapterParserTest {
    private fun chapter(blocks: String) = """
        {"id":"c1","unit":"I","title":{"en":"Enzymes","ta":"நொதிகள்"},
         "sections":[{"id":"s1","title":{"en":"Intro"},"blocks":[$blocks]}]}
    """.trimIndent()

    @Test fun minimal_chapter_parses_with_paragraph() {
        val r = parseChapter(chapter("""{"type":"p","text":{"en":"Hello","ta":"வணக்கம்"}}"""))
        val ch = r.chapter!!
        assertEquals("c1", ch.id)
        assertEquals("நொதிகள்", ch.title.ta)
        assertEquals(1, ch.sections.size)
        val p = ch.sections[0].blocks[0] as Paragraph
        assertEquals(Bi("Hello", "வணக்கம்", TaStatus.DRAFT), p.text)
        assertTrue(r.warnings.isEmpty())
    }

    @Test fun ta_status_is_read_and_defaults_to_draft() {
        val r = parseChapter(chapter("""{"type":"p","text":{"en":"A","ta":"அ","ta_status":"reviewed"}},{"type":"p","text":{"en":"B"}}"""))
        val b = r.chapter!!.sections[0].blocks
        assertEquals(TaStatus.REVIEWED, (b[0] as Paragraph).text.taStatus)
        assertEquals(TaStatus.DRAFT, (b[1] as Paragraph).text.taStatus)
    }

    @Test fun ragged_table_row_is_padded_never_dropped() {
        val t = """{"type":"table","header":[{"en":"Enzyme"},{"en":"Site"}],
                     "rows":[[{"en":"EcoRI"},{"en":"GAATTC"}],[{"en":"HindIII"}]]}"""
        val tbl = parseChapter(chapter(t)).chapter!!.sections[0].blocks[0] as Table
        assertEquals(2, tbl.rows.size)
        assertEquals(2, tbl.rows[1].size)
        assertEquals(Bi(""), tbl.rows[1][1])
        assertEquals("HindIII", tbl.rows[1][0].en)
    }

    @Test fun unknown_block_type_becomes_visible_unparsed_block_with_warning() {
        val r = parseChapter(chapter("""{"type":"hologram","text":{"en":"Lost?"}}"""))
        val u = r.chapter!!.sections[0].blocks[0] as Unparsed
        assertEquals("type:hologram", u.selector)
        assertTrue(u.text.contains("Lost?"))
        assertEquals(1, r.warnings.size)
    }

    @Test fun malformed_json_returns_null_chapter_and_one_warning() {
        val r = parseChapter("{")
        assertNull(r.chapter)
        assertEquals(1, r.warnings.size)
    }

    @Test fun other_block_types_parse() {
        val r = parseChapter(chapter("""
            {"type":"bullets","ordered":true,"items":[{"en":"x"},{"en":"y"}]},
            {"type":"callout","kind":"exam","text":{"en":"Remember"}},
            {"type":"keyterm","term":{"en":"Ligase"},"definition":{"en":"Joins DNA"}},
            {"type":"diagram","id":"vec-d01","caption":{"en":"pBR322"}},
            {"type":"steps","items":[{"en":"Cut"},{"en":"Ligate"}]},
            {"type":"reveal","question":{"en":"Q?"},"answer":{"en":"A"}},
            {"type":"summary","items":[{"en":"s"}]},
            {"type":"glossary","term":{"en":"Vector"},"definition":{"en":"Carrier"}}
        """))
        assertTrue(r.warnings.isEmpty())
        val b = r.chapter!!.sections[0].blocks
        assertEquals(8, b.size)
        assertEquals(true, (b[0] as Bullets).ordered)
        assertEquals(CalloutKind.EXAM, (b[1] as Callout).kind)
        assertEquals("vec-d01", (b[3] as Diagram).id)
        assertNotNull(b[7] as GlossaryEntry)
    }

    @Test fun asset_path_follows_unit_and_stem() {
        assertEquals("content/u1/atlas1_fundamentals_vectors.json", chapterAssetPath("atlas1_fundamentals_vectors.html", "I"))
        assertEquals("content/u4/hgp_ethics_ipr.json", chapterAssetPath("hgp_ethics_ipr.html", "IV"))
        assertEquals("content/u5/x.json", chapterAssetPath("x.html", "V"))
    }

    @Test fun unit1_generated_chapters_parse_cleanly_and_hold_all_32_diagrams() {
        val dir = listOf("src/main/assets/content/u1", "app/src/main/assets/content/u1")
            .map(::File).first { it.isDirectory }
        val files = dir.listFiles { f -> f.extension == "json" }!!.sortedBy { it.name }
        assertEquals(6, files.size)
        var diagrams = 0
        for (f in files) {
            val r = parseChapter(f.readText())
            assertTrue("${f.name}: ${r.warnings}", r.warnings.isEmpty())
            val blocks = r.chapter!!.sections.flatMap { it.blocks }
            assertTrue("${f.name} has no content", blocks.isNotEmpty())
            assertTrue("${f.name} contains Unparsed blocks", blocks.none { it is Unparsed })
            diagrams += blocks.count { it is Diagram }
        }
        assertEquals(32, diagrams)
    }
}
