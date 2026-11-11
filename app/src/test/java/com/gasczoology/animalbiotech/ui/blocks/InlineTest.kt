package com.gasczoology.animalbiotech.ui.blocks

import org.junit.Assert.assertEquals
import org.junit.Test

class InlineTest {
    @Test fun plain_text_is_one_span() = assertEquals(listOf(Span("abc", false)), parseInline("abc"))
    @Test fun bold_markers_are_removed() =
        assertEquals(listOf(Span("Biotech is the ", false), Span("science of life", true), Span(".", false)), parseInline("Biotech is the **science of life**."))
    @Test fun several_pairs() =
        assertEquals(listOf(Span("A", true), Span(" and ", false), Span("B", true)), parseInline("**A** and **B**"))
    @Test fun whole_line_bold_with_emoji() = assertEquals(listOf(Span("🎯 Objectives", true)), parseInline("**🎯 Objectives**"))
    @Test fun unpaired_marker_stays_literal() = assertEquals(listOf(Span("a ** b", false)), parseInline("a ** b"))
    @Test fun empty_pair_stays_literal() = assertEquals(listOf(Span("x****y", false)), parseInline("x****y"))
    @Test fun newlines_inside_bold_are_kept() = assertEquals(listOf(Span("a\nb", true)), parseInline("**a\nb**"))
    @Test fun no_text_is_lost() {
        val s = "**Bios** (βίος) = Life · **Technē** = Skill"
        assertEquals("Bios (βίος) = Life · Technē = Skill", parseInline(s).joinToString("") { it.text })
    }
}
