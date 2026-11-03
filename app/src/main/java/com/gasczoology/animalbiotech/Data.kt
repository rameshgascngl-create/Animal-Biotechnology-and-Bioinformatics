package com.gasczoology.animalbiotech

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Topic(
    val id: String,
    val title: String,
    val note: String,
    val file: String,
    val hash: String,
    val unitRoman: String,
) {
    val url: String get() = "file:///android_asset/modules/$file$hash"
}

data class UnitInfo(val roman: String, val title: String, val topics: List<Topic>)

data class Mcq(val q: String, val options: List<String>, val answer: Int, val explanation: String)

data class QuizSet(val file: String, val title: String, val unitRoman: String, val items: List<Mcq>)

object Repo {
    private var unitCache: List<UnitInfo>? = null
    private var quizCache: List<QuizSet>? = null

    private fun read(ctx: Context, name: String): String =
        ctx.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    fun units(ctx: Context): List<UnitInfo> {
        unitCache?.let { return it }
        val root = JSONObject(read(ctx, "syllabus.json"))
        val arr = root.getJSONArray("units")
        val out = ArrayList<UnitInfo>()
        for (i in 0 until arr.length()) {
            val u = arr.getJSONObject(i)
            val roman = u.getString("roman")
            val ta = u.getJSONArray("topics")
            val topics = ArrayList<Topic>()
            for (j in 0 until ta.length()) {
                val t = ta.getJSONObject(j)
                topics.add(
                    Topic(
                        id = t.getString("id"),
                        title = t.getString("title"),
                        note = t.optString("note", ""),
                        file = t.getString("file"),
                        hash = t.optString("hash", ""),
                        unitRoman = roman,
                    )
                )
            }
            out.add(UnitInfo(roman, u.getString("title"), topics))
        }
        unitCache = out
        return out
    }

    fun quizzes(ctx: Context): List<QuizSet> {
        quizCache?.let { return it }
        val mods = JSONObject(read(ctx, "syllabus.json")).getJSONObject("modules")
        val bank = JSONObject(read(ctx, "quizbank.json"))
        val out = ArrayList<QuizSet>()
        val keys = bank.keys()
        while (keys.hasNext()) {
            val file = keys.next()
            val arr: JSONArray = bank.getJSONArray(file)
            val items = ArrayList<Mcq>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val oa = o.getJSONArray("o")
                val opts = ArrayList<String>()
                for (k in 0 until oa.length()) opts.add(oa.getString(k))
                items.add(Mcq(o.getString("q"), opts, o.getInt("a"), o.optString("e", "")))
            }
            val m = mods.optJSONObject(file)
            out.add(
                QuizSet(
                    file = file,
                    title = m?.optString("title", file) ?: file,
                    unitRoman = m?.optString("unit", "") ?: "",
                    items = items,
                )
            )
        }
        val order = listOf("I", "II", "III", "IV", "V")
        val sorted = out.sortedWith(compareBy({ order.indexOf(it.unitRoman) }, { it.title }))
        quizCache = sorted
        return sorted
    }
}

/** Small wrapper around SharedPreferences for progress, bookmarks and best quiz scores. */
class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("animal_biotech", Context.MODE_PRIVATE)

    fun getSet(key: String): Set<String> = HashSet(sp.getStringSet(key, emptySet()) ?: emptySet())

    fun toggle(key: String, id: String): Set<String> {
        val s = getSet(key).toMutableSet()
        if (!s.add(id)) s.remove(id)
        sp.edit().putStringSet(key, s).apply()
        return s
    }

    fun best(file: String): Int = sp.getInt("best_$file", -1)

    fun saveBest(file: String, score: Int) {
        if (score > best(file)) sp.edit().putInt("best_$file", score).apply()
    }
}
