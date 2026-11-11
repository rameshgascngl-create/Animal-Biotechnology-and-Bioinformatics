package com.gasczoology.animalbiotech.content

import org.json.JSONArray
import org.json.JSONObject

/** Asset path for a chapter, e.g. ("enzymes_genetic_engineering.html", "I") -> "content/u1/enzymes_genetic_engineering.json". */
fun chapterAssetPath(file: String, unitRoman: String): String {
    val unit = when (unitRoman.uppercase()) {
        "I" -> "1"; "II" -> "2"; "III" -> "3"; "IV" -> "4"; "V" -> "5"
        else -> unitRoman.lowercase()
    }
    return "content/u$unit/${file.removeSuffix(".html")}.json"
}

/**
 * Parses chapter JSON. Never throws: malformed input gives a null chapter and a warning;
 * unknown or broken blocks become visible [Unparsed] blocks plus a warning (spec 5 and 6.4).
 */
fun parseChapter(json: String): ParseResult {
    val warnings = ArrayList<String>()
    val root = try {
        JSONObject(json)
    } catch (e: Exception) {
        return ParseResult(null, listOf("Malformed chapter JSON: ${e.message}"))
    }
    val chapterId = root.optString("id", "")
    val sections = ArrayList<Section>()
    val arr = root.optJSONArray("sections") ?: JSONArray()
    for (i in 0 until arr.length()) {
        val s = arr.optJSONObject(i) ?: continue
        val blocks = ArrayList<Block>()
        val ba = s.optJSONArray("blocks") ?: JSONArray()
        for (j in 0 until ba.length()) {
            val b = ba.optJSONObject(j) ?: continue
            blocks.add(parseBlock(b, chapterId, warnings))
        }
        sections.add(Section(s.optString("id", "s$i"), bi(s.optJSONObject("title")), blocks))
    }
    return ParseResult(Chapter(chapterId, root.optString("unit", ""), bi(root.optJSONObject("title")), sections), warnings)
}

private fun bi(o: JSONObject?): Bi {
    if (o == null) return Bi("")
    val status = when (o.optString("ta_status", "draft").lowercase()) {
        "reviewed" -> TaStatus.REVIEWED
        "bracketed" -> TaStatus.BRACKETED
        else -> TaStatus.DRAFT
    }
    return Bi(o.optString("en", ""), o.optString("ta", ""), status)
}

private fun biList(a: JSONArray?): List<Bi> =
    if (a == null) emptyList() else (0 until a.length()).map { bi(a.optJSONObject(it)) }

private fun parseBlock(o: JSONObject, source: String, warnings: MutableList<String>): Block {
    val type = o.optString("type", "")
    return try {
        when (type) {
            "p" -> Paragraph(bi(o.optJSONObject("text")))
            "bullets" -> Bullets(biList(o.optJSONArray("items")), o.optBoolean("ordered", false))
            "table" -> {
                val header = biList(o.optJSONArray("header"))
                val ra = o.optJSONArray("rows") ?: JSONArray()
                val rows = (0 until ra.length()).map { r ->
                    val cells = biList(ra.optJSONArray(r))
                    if (cells.size < header.size) cells + List(header.size - cells.size) { Bi("") } else cells
                }
                Table(header, rows)
            }
            "callout" -> Callout(
                CalloutKind.entries.firstOrNull { it.name == o.optString("kind", "info").uppercase() } ?: CalloutKind.INFO,
                bi(o.optJSONObject("text")),
            )
            "keyterm" -> KeyTerm(bi(o.optJSONObject("term")), bi(o.optJSONObject("definition")))
            "diagram" -> Diagram(o.optString("id", ""), bi(o.optJSONObject("caption")))
            "steps" -> Steps(biList(o.optJSONArray("items")))
            "reveal" -> Reveal(bi(o.optJSONObject("question")), bi(o.optJSONObject("answer")))
            "summary" -> Summary(biList(o.optJSONArray("items")))
            "glossary" -> GlossaryEntry(bi(o.optJSONObject("term")), bi(o.optJSONObject("definition")))
            "unparsed" -> Unparsed(o.optString("text", ""), o.optString("source", source), o.optString("selector", ""))
            else -> {
                warnings.add("Unknown block type '$type' in chapter '$source'")
                Unparsed(o.toString(), source, "type:$type")
            }
        }
    } catch (e: Exception) {
        warnings.add("Broken '$type' block in chapter '$source': ${e.message}")
        Unparsed(o.toString(), source, "type:$type")
    }
}
