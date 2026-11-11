package com.gasczoology.animalbiotech.ui

import com.gasczoology.animalbiotech.content.Bi
import com.gasczoology.animalbiotech.content.Bullets
import com.gasczoology.animalbiotech.content.Chapter
import com.gasczoology.animalbiotech.content.Paragraph

/** Pads a row with empty cells up to [width]; never drops cells. */
fun padRow(row: List<Bi>, width: Int): List<Bi> =
    if (row.size >= width) row else row + List(width - row.size) { Bi("") }

/** Width of a table: the widest of the header and every row, so no cell is ever lost. */
fun tableWidth(header: List<Bi>, rows: List<List<Bi>>): Int =
    maxOf(header.size, rows.maxOfOrNull { it.size } ?: 0)

/** Pads ragged rows to the table width. Cells are only ever added, never removed. */
fun normaliseTable(header: List<Bi>, rows: List<List<Bi>>): List<List<Bi>> {
    val w = tableWidth(header, rows)
    return rows.map { padRow(it, w) }
}

/** Estimated reading time in whole minutes (>= 1), from English prose at 200 words per minute. */
fun readingMinutes(c: Chapter): Int {
    var words = 0
    for (sec in c.sections) for (b in sec.blocks) when (b) {
        is Paragraph -> words += b.text.en.wordCount()
        is Bullets -> b.items.forEach { words += it.en.wordCount() }
        else -> {}
    }
    return maxOf(1, (words + 199) / 200)
}

private fun String.wordCount() = split(Regex("\\s+")).count { it.isNotBlank() }

/** Flat reader entries so a contents list can jump to an item index. */
fun sectionStartIndices(c: Chapter): List<Int> {
    var i = 1 // index 0 is the title page
    return c.sections.map { s -> val at = i; i += 1 + s.blocks.size; at }
}
