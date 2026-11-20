package com.gasczoology.animalbiotech

import com.gasczoology.animalbiotech.content.chapterAssetPath
import com.gasczoology.animalbiotech.content.parseChapter
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.ui.topicStartIndex
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyllabusNavigationTest {
    @Test fun shared_chapter_topics_link_to_distinct_native_sections() {
        val assets = File("src/main/assets")
        val units = JSONObject(File(assets, "syllabus.json").readText()).getJSONArray("units")
        val sectionsByFile = mutableMapOf<String, MutableSet<String>>()
        for (i in 0 until units.length()) {
            val unit = units.getJSONObject(i)
            val topics = unit.getJSONArray("topics")
            for (j in 0 until topics.length()) {
                val topic = topics.getJSONObject(j)
                if (topic.optString("hash").isBlank()) continue
                val file = topic.getString("file")
                val path = chapterAssetPath(file, unit.getString("roman"))
                val chapter = requireNotNull(parseChapter(File(assets, path).readText()).chapter)
                val section = topic.getString("section")
                assertTrue("Missing section for ${topic.getString("id")}", topicStartIndex(chapter, section, -1) > 0)
                assertTrue("Duplicate section for $file", sectionsByFile.getOrPut(file) { mutableSetOf() }.add(section))
            }
        }
        assertEquals(7, sectionsByFile.values.sumOf { it.size })
    }

    @Test fun tamil_search_index_retains_the_specific_topic_for_shared_chapters() {
        val first = Topic("first", "Alignment", "", "shared.html", "#p1", "V")
        val second = Topic(
            "second", "Similarity search", "", "shared.html", "#p3", "V",
            titleTa = "ஒப்புமைத் தேடல்", sectionId = "s05",
        )
        val hits = buildIndex(listOf(UnitInfo("V", "Bioinformatics", listOf(first, second))), emptyList(), emptyMap(), Lang.TA)
        assertEquals(listOf(first, second), hits.map { it.topic })
        assertEquals(listOf("Alignment", "ஒப்புமைத் தேடல்"), hits.map { it.title })
    }
}
