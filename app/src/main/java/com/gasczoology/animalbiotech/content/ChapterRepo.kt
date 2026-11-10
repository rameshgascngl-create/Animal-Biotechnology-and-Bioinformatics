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
        return parseChapter(text).also { cache[path] = it }
    }
}
