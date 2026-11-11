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

    @Test fun italic_gene_name() = assertEquals(listOf(Span("The ", false), Span("tk", false, true), Span(" gene", false)), parseInline("The _tk_ gene"))
    @Test fun snake_case_is_not_italic() = assertEquals(listOf(Span("lac_Z_gene", false)), parseInline("lac_Z_gene"))
    @Test fun unpaired_underscore_stays() = assertEquals(listOf(Span("a _ b", false)), parseInline("a _ b"))
    @Test fun tamil_italic_with_tamil_suffix() =
        assertEquals(listOf(Span("\u0BAE\u0BC6", false, true), Span("\u0B90", false)), parseInline("_\u0BAE\u0BC6_\u0B90"))
    @Test fun bold_with_italic_inside() = assertEquals(listOf(Span("see ", true), Span("tk", true, true)), parseInline("**see _tk_**"))
    @Test fun no_text_lost_with_italics() = assertEquals("The tk gene", parseInline("The _tk_ gene").joinToString("") { it.text })
    @Test fun strips_trailing_reveal_prompt() = assertEquals("\uD83E\uDDEB\nFermentation", stripRevealPrompt("\uD83E\uDDEB\nFermentation\nClick to reveal"))
    @Test fun keeps_single_line_question() = assertEquals("Click to reveal", stripRevealPrompt("Click to reveal"))
}
