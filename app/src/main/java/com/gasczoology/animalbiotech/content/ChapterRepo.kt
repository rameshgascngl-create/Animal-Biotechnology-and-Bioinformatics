package com.gasczoology.animalbiotech.content

import android.content.Context
import java.io.IOException

/** Loads bilingual chapter JSON from assets. Returns null when a topic has no native chapter yet. */
object ChapterRepo {
    private val cache = HashMap<String, ParseResult>()

    fun load(ctx: Context, file: String, unitRoman: String): ParseResult? {
        val path = chapterAssetPath(file, unitRoman)
        cache[path]?.let { return it }
        val text = try {
            ctx.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: IOException) {
            return null
        }
        val parsed = parseChapter(text)
        val ch = parsed.chapter ?: return parsed.also { cache[path] = it }
        val withFigures = applyAnchors(ch, file.removeSuffix(".html"), anchors(ctx)) { a -> atlasFigure(ctx, a) }
        return parsed.copy(chapter = withFigures).also { cache[path] = it }
    }

    private var anchorList: List<Anchor>? = null
    private fun anchors(ctx: Context): List<Anchor> = anchorList ?: try {
        parseAnchors(ctx.assets.open("figure_anchors.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
    } catch (e: Exception) { emptyList() }.also { anchorList = it }

    private fun atlasFigure(ctx: Context, a: Anchor): Diagram? = try {
        val stem = a.id.substringBeforeLast("-d")
        val text = ctx.assets.open("content/${a.unit}/$stem.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        parseChapter(text).chapter?.sections?.flatMap { it.blocks }?.filterIsInstance<Diagram>()?.firstOrNull { it.id == a.id }
    } catch (e: Exception) { null }
}
