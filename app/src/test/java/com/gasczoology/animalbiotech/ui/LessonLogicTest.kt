package com.gasczoology.animalbiotech.ui

import com.gasczoology.animalbiotech.content.Bi
import com.gasczoology.animalbiotech.content.Chapter
import com.gasczoology.animalbiotech.content.Paragraph
import com.gasczoology.animalbiotech.content.Section
import org.junit.Assert.assertEquals
import org.junit.Test

class LessonLogicTest {
    private val h = listOf(Bi("A"), Bi("B"), Bi("C"))

    @Test fun short_row_is_padded_to_header_width() {
        val out = normaliseTable(h, listOf(listOf(Bi("x"))))
        assertEquals(3, out[0].size)
        assertEquals("x", out[0][0].en)
        assertEquals("", out[0][2].en)
    }

    @Test fun long_row_is_never_truncated() {
        val row = listOf(Bi("1"), Bi("2"), Bi("3"), Bi("4"), Bi("5"))
        val out = normaliseTable(h, listOf(row))
        assertEquals(5, out[0].size)
        assertEquals("5", out[0][4].en)
    }

    @Test fun all_rows_end_up_the_same_width() {
        val out = normaliseTable(h, listOf(listOf(Bi("a")), listOf(Bi("a"), Bi("b"), Bi("c"), Bi("d"))))
        assertEquals(listOf(4, 4), out.map { it.size })
    }

    @Test fun width_is_header_when_there_are_no_rows() {
        assertEquals(3, tableWidth(h, emptyList()))
        assertEquals(emptyList<List<Bi>>(), normaliseTable(h, emptyList()))
    }

    @Test fun tamil_text_survives_padding() {
        val out = normaliseTable(h, listOf(listOf(Bi("Vector", "தாங்கிக்கடத்தி"))))
        assertEquals("தாங்கிக்கடத்தி", out[0][0].ta)
    }

    private fun chap(vararg counts: Int) = Chapter("c", "I", Bi("T"),
        counts.mapIndexed { i, n -> Section("s$i", Bi("S$i"), List(n) { Paragraph(Bi("w ".repeat(10))) }) })

    @Test fun reading_time_is_at_least_one_minute() = assertEquals(1, readingMinutes(chap(1)))

    @Test fun reading_time_rounds_up_at_200_wpm() = assertEquals(2, readingMinutes(chap(21))) // 210 words

    @Test fun section_starts_skip_title_page_headers_and_blocks() =
        assertEquals(listOf(1, 4, 7), sectionStartIndices(chap(2, 2, 0)))
}
