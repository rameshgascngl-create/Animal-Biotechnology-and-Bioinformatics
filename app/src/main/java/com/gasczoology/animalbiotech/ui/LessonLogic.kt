package com.gasczoology.animalbiotech.ui

import com.gasczoology.animalbiotech.content.Bi

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
