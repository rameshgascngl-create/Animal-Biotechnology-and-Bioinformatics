package com.gasczoology.animalbiotech.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FigureAnchorsTest {
    private val assets = File("src/main/assets")
    private fun chapter(unit: String, stem: String) =
        requireNotNull(parseChapter(File(assets, "content/$unit/$stem.json").readText()).chapter) { "$unit/$stem" }

    private val anchors by lazy { parseAnchors(File(assets, "figure_anchors.json").readText()) }

    private fun diagrams(c: Chapter) = c.sections.flatMap { it.blocks }.filterIsInstance<Diagram>().map { it.id }

    @Test fun inserts_after_named_block_and_not_elsewhere() {
        val p1 = Paragraph(Bi("First paragraph about plasmids"))
        val p2 = Paragraph(Bi("Second paragraph"))
        val ch = Chapter("c", "I", Bi("T"), listOf(Section("s1", Bi("S"), listOf(p1, p2))))
        val d = Diagram("atlas-d1", Bi("Fig"))
        val a = Anchor("atlas-d1", "u1", "c", "s1", "First paragraph about plasmids")
        val out = applyAnchors(ch, "c", listOf(a)) { d }
        assertEquals(listOf<Block>(p1, d, p2), out.sections[0].blocks)
        assertEquals(ch, applyAnchors(ch, "other", listOf(a)) { d })
    }

    @Test fun unmatched_block_falls_back_to_end_of_section() {
        val p1 = Paragraph(Bi("Alpha"))
        val ch = Chapter("c", "I", Bi("T"), listOf(Section("s1", Bi("S"), listOf(p1))))
        val d = Diagram("x-d1", Bi("Fig"))
        val out = applyAnchors(ch, "c", listOf(Anchor("x-d1", "u1", "c", "s1", "nope"))) { d }
        assertEquals(listOf<Block>(p1, d), out.sections[0].blocks)
    }

    @Test fun every_real_anchor_resolves_to_its_section_and_block() {
        assertTrue(anchors.isNotEmpty())
        for (a in anchors) {
            val atlas = chapter(a.unit, a.id.substringBeforeLast("-d"))
            val fig = atlas.sections.flatMap { it.blocks }.filterIsInstance<Diagram>().single { it.id == a.id }
            val target = chapter(a.unit, a.chapter)
            val sec = requireNotNull(target.sections.firstOrNull { it.id == a.section }) { "no section for ${a.id}" }
            assertTrue("anchor block not found for ${a.id}", sec.blocks.any { anchorKey(it) == a.after })
            val out = applyAnchors(target, a.chapter, anchors) { x -> if (x.id == fig.id) fig else null }
            val outSec = out.sections.first { it.id == a.section }
            val at = outSec.blocks.indexOf(fig)
            assertTrue("${a.id} not inserted", at > 0)
            assertEquals(a.after, anchorKey(outSec.blocks[at - 1]))
        }
    }

    @Test fun anchored_figure_ids_are_unique_and_never_duplicate_a_chapters_own_figure() {
        assertEquals(anchors.size, anchors.map { it.id }.toSet().size)
        for (a in anchors) assertTrue(a.id !in diagrams(chapter(a.unit, a.chapter)))
    }
}
