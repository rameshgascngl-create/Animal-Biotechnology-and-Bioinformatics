package com.gasczoology.animalbiotech.content

import org.json.JSONArray

/** Links an atlas figure to the section (and the block within it) that its text explains. */
data class Anchor(val id: String, val unit: String, val chapter: String, val section: String, val after: String)

fun parseAnchors(json: String): List<Anchor> {
    val a = JSONArray(json)
    return List(a.length()) { i ->
        val o = a.getJSONObject(i)
        Anchor(o.getString("id"), o.getString("unit"), o.getString("chapter"), o.getString("section"), o.getString("after"))
    }
}

/** Text used to match [Anchor.after]: first 48 characters of a prose block's English text. */
fun anchorKey(b: Block): String? = when (b) {
    is Paragraph -> b.text.en
    is Bullets -> b.items.joinToString(" ") { it.en }
    is Callout -> b.text.en
    else -> null
}?.take(48)

/**
 * Returns [ch] with each atlas figure for which [lookup] finds a [Diagram] inserted directly after the
 * block its anchor names. If that block cannot be found the figure goes to the end of its section.
 */
fun applyAnchors(ch: Chapter, stem: String, anchors: List<Anchor>, lookup: (Anchor) -> Diagram?): Chapter {
    val mine = anchors.filter { it.chapter == stem }
    if (mine.isEmpty()) return ch
    return ch.copy(sections = ch.sections.map { s ->
        val here = mine.filter { it.section == s.id }
        if (here.isEmpty()) return@map s
        val blocks = s.blocks.toMutableList()
        for (a in here) {
            val d = lookup(a) ?: continue
            val at = blocks.indexOfFirst { anchorKey(it) == a.after }
            blocks.add(if (at >= 0) at + 1 else blocks.size, d)
        }
        s.copy(blocks = blocks)
    })
}
