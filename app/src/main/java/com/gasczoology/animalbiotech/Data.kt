package com.gasczoology.animalbiotech

import android.content.Context
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.parseLang
import org.json.JSONArray
import org.json.JSONObject

data class Topic(
    val id: String,
    val title: String,
    val note: String,
    val file: String,
    val hash: String,
    val unitRoman: String,
    val titleTa: String = "",
) {
}

data class UnitInfo(val roman: String, val title: String, val topics: List<Topic>, val titleTa: String = "")

data class Mcq(val id: String, val q: String, val options: List<String>, val answer: Int, val explanation: String, val qTa: String = "", val optionsTa: List<String> = emptyList(), val explanationTa: String = "")

data class Saq(val q: String, val a: String)
data class Term(val term: String, val def: String, val module: String)
data class RevisionSet(
    val file: String, val title: String, val unitRoman: String,
    val keyPoints: List<String>, val saqs: List<Saq>, val glossary: List<Term>,
)

data class QuizSet(val file: String, val title: String, val unitRoman: String, val items: List<Mcq>)

object Repo {
    private var revCache: List<RevisionSet>? = null
    private var unitCache: List<UnitInfo>? = null
    private var quizCache: List<QuizSet>? = null

    private fun read(ctx: Context, name: String): String =
        ctx.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    fun units(ctx: Context): List<UnitInfo> {
        unitCache?.let { return it }
        val root = JSONObject(read(ctx, "syllabus.json"))
        val arr = root.getJSONArray("units")
        val modules = root.getJSONObject("modules")
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
                        titleTa = modules.optJSONObject(t.getString("file"))?.optString("title_ta", "") ?: "",
                    )
                )
            }
            out.add(UnitInfo(roman, u.getString("title"), topics, u.optString("title_ta", "")))
        }
        unitCache = out
        return out
    }

    fun revisions(ctx: Context): List<RevisionSet> {
        revCache?.let { return it }
        val root = JSONObject(read(ctx, "revision.json"))
        val out = ArrayList<RevisionSet>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val file = keys.next()
            val o = root.getJSONObject(file)
            val title = o.getString("title")
            val kp = ArrayList<String>()
            val a = o.getJSONArray("kp"); for (i in 0 until a.length()) kp.add(a.getString(i))
            val sq = ArrayList<Saq>()
            val b = o.getJSONArray("sa"); for (i in 0 until b.length()) { val x = b.getJSONObject(i); sq.add(Saq(x.getString("q"), x.getString("a"))) }
            val gl = ArrayList<Term>()
            val c = o.getJSONArray("gl"); for (i in 0 until c.length()) { val x = c.getJSONObject(i); gl.add(Term(x.getString("t"), x.getString("d"), title)) }
            out.add(RevisionSet(file, title, o.optString("unit", ""), kp, sq, gl))
        }
        val order = listOf("I", "II", "III", "IV", "V")
        val sorted = out.sortedWith(compareBy({ order.indexOf(it.unitRoman) }, { it.title }))
        revCache = sorted
        return sorted
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
                items.add(parseMcq(file, o))
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

    fun add(key: String, id: String): Set<String> {
        val s = getSet(key).toMutableSet(); s.add(id)
        sp.edit().putStringSet(key, s).apply(); return s
    }

    fun remove(key: String, id: String): Set<String> {
        val s = getSet(key).toMutableSet(); s.remove(id)
        sp.edit().putStringSet(key, s).apply(); return s
    }

    fun getInt(key: String, def: Int): Int = sp.getInt(key, def)
    fun putInt(key: String, v: Int) { sp.edit().putInt(key, v).apply() }
    fun getLong(key: String, def: Long): Long = sp.getLong(key, def)
    fun putLong(key: String, v: Long) { sp.edit().putLong(key, v).apply() }
    fun getString(key: String): String = sp.getString(key, "") ?: ""
    fun putString(key: String, v: String) { sp.edit().putString(key, v).apply() }

    fun best(file: String): Int = sp.getInt("best_$file", -1)

    fun saveBest(file: String, score: Int) {
        if (score > best(file)) sp.edit().putInt("best_$file", score).apply()
    }
}

/** Reader language, persisted. Unknown or missing values give English. */
fun Store.getLang(): Lang = parseLang(getString("lang"))
fun Store.putLang(l: Lang) = putString("lang", l.name)
