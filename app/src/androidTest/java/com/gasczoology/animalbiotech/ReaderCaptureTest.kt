package com.gasczoology.animalbiotech

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.content.ChapterRepo
import com.gasczoology.animalbiotech.content.Diagram
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.ui.LessonScreen
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

/** Genuine native screens at 360 dp; fixture opens an actual chapter without scrolling through the syllabus. */
@RunWith(AndroidJUnit4::class)
class ReaderCaptureTest {
    @Test fun captureReaderLanguagesAndEnlargement() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle()
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            capture("01-home")
            for (lang in Lang.entries) {
                scenario.onActivity { activity ->
                    val chapter = requireNotNull(ChapterRepo.load(activity, "vectors_genetic_engineering.html", "I")?.chapter)
                    // Keep the original section and diagram, removing preceding sections only to bring the figure into view.
                    val section = chapter.sections.first { it.blocks.any { b -> b is Diagram } }
                    val start = section.blocks.indexOfFirst { it is Diagram }
                    val fixture = chapter.copy(sections = listOf(section.copy(blocks = section.blocks.drop(start))))
                    activity.setContent {
                        MaterialTheme { LessonScreen(fixture, lang, {}, 100, false, {}, false, {}, {}, null) }
                    }
                }
                device.waitForIdle()
                assertTrue(device.wait(Until.hasObject(By.descStartsWith("Diagram:")), 10000) ||
                    device.wait(Until.hasObject(By.clazz("android.widget.ImageView")), 10000))
                capture("02-lesson-" + lang.name)
                if (lang == Lang.EN) {
                    val image = device.findObject(By.descStartsWith("Diagram:"))
                        ?: device.findObject(By.clazz("android.widget.ImageView"))
                    requireNotNull(image).click()
                    assertTrue(device.wait(Until.hasObject(By.text("Close")), 5000))
                    capture("03-enlarged")
                    device.findObject(By.text("Close")).click()
                }
            }
        }
    }

    @Test fun captureBoldTablesAndLabels() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle()
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (lang in Lang.entries) {
                fun show(file: String, pick: (com.gasczoology.animalbiotech.content.Chapter) -> com.gasczoology.animalbiotech.content.Chapter, shot: String) {
                    scenario.onActivity { activity ->
                        val chapter = pick(requireNotNull(ChapterRepo.load(activity, file, "I")?.chapter))
                        activity.setContent { MaterialTheme { LessonScreen(chapter, lang, {}, 100, false, {}, false, {}, {}, null) } }
                    }
                    device.waitForIdle(); Thread.sleep(800)
                    capture(shot + "-" + lang.name)
                }
                show("biotechnology_fundamentals.html", { it }, "04-chapter-start")
                show("atlas1_fundamentals_vectors.html", { ch ->
                    val sec = ch.sections.first { sc -> sc.blocks.any { it is com.gasczoology.animalbiotech.content.Table && it.header.size >= 3 } }
                    val at = sec.blocks.indexOfFirst { it is com.gasczoology.animalbiotech.content.Table && it.header.size >= 3 }
                    ch.copy(sections = listOf(sec.copy(blocks = sec.blocks.drop(at))))
                }, "05-wide-table")
            }
        }
    }

    @Test fun captureHomeAndShadedBlocks() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle(); Thread.sleep(600)
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            capture("06-home-EN")
            device.findObject(By.text("\u0BA4\u0BAE\u0BBF\u0BB4\u0BCD")).click()
            device.waitForIdle()
            capture("06-home-TA")
            device.findObject(By.text("English")).click()
            for (lang in Lang.entries) {
                scenario.onActivity { activity ->
                    var found: com.gasczoology.animalbiotech.content.Chapter? = null
                    for (un in Repo.units(activity)) for (t in un.topics) {
                        val ch = ChapterRepo.load(activity, t.file, t.unitRoman)?.chapter ?: continue
                        val all = ch.sections.flatMap { it.blocks }
                        val reveal = all.firstOrNull { it is com.gasczoology.animalbiotech.content.Reveal }
                        val term = all.firstOrNull { it is com.gasczoology.animalbiotech.content.KeyTerm || it is com.gasczoology.animalbiotech.content.GlossaryEntry }
                        val callouts = all.filterIsInstance<com.gasczoology.animalbiotech.content.Callout>().distinctBy { com.gasczoology.animalbiotech.ui.theme.calloutRole(it.kind, it.text.en) }.take(3)
                        if (reveal != null && callouts.isNotEmpty()) {
                            found = ch.copy(sections = listOf(ch.sections.first().copy(blocks = listOfNotNull(reveal, term) + callouts)))
                            break
                        }
                    }
                    val fixture = requireNotNull(found) { "no chapter with a question and a definition/callout" }
                    activity.setContent { MaterialTheme { LessonScreen(fixture, lang, {}, 100, false, {}, false, {}, {}, null) } }
                }
                capture("07-shaded-" + lang.name)
                device.findObject(By.textStartsWith(if (lang == Lang.TA) "\u0BB5\u0BBF\u0B9F\u0BC8\u0BAF\u0BC8" else "Tap to reveal"))?.click()
                capture("08-answer-" + lang.name)
            }
        }
    }
}
