package com.gasczoology.animalbiotech

import org.json.JSONObject
import com.gasczoology.animalbiotech.content.Bi
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.shown

/** Original option indices and question IDs are preserved across language switches. */
fun parseMcq(file: String, o: JSONObject): Mcq {
    val choices = o.getJSONArray("o")
    val options = (0 until choices.length()).map { choices.getString(it) }
    val translated = o.optJSONArray("o_ta")
    val ta = if (translated != null && translated.length() == options.size)
        (0 until translated.length()).map { translated.optString(it, "") } else emptyList()
    val answer = o.getInt("a")
    require(answer in options.indices) { "Quiz answer outside option range: $file" }
    val q = o.getString("q")
    return Mcq("$file#${q.hashCode()}", q, options, answer, o.optString("e", ""),
        o.optString("q_ta", ""), ta, o.optString("e_ta", ""))
}

fun pick(en: String, ta: String, lang: Lang): String = Bi(en, ta).shown(lang).joinToString("\n") {
    it.text + if (it.pending) " [தமிழ் விரைவில்]" else ""
}
