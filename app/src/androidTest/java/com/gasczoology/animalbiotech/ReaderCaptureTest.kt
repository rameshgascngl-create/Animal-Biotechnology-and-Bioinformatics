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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

/** Genuine native screens at 360 dp; fixture opens an actual chapter without scrolling through the syllabus. */
@RunWith(AndroidJUnit4::class)
class ReaderCaptureTest {
    private fun md5(device: UiDevice, name: String): String =
        device.executeShellCommand("md5sum /sdcard/Pictures/$name.png").trim().substringBefore(' ')

    private fun hasButton(device: UiDevice, label: String) = device.hasObject(By.text(label)) || device.hasObject(By.desc(label))
    private fun button(device: UiDevice, label: String) = device.findObject(By.text(label)) ?: device.findObject(By.desc(label))

    /**
     * Semantic checks for the figure viewer (each failure fails the build):
     * it opens (Close and Zoom controls exist), it differs from the inline frame, it is still open two seconds later
     * (the failure this guards against: it closed itself ~0.5 s after opening), Zoom changes the frame, the Close button
     * closes it, and system Back closes it.
     */
    private fun exerciseViewer(device: UiDevice, lang: Lang, figurePrefix: String, tag: String, capture: (String) -> Unit) {
        val close = if (lang == Lang.TA) "\u0BAE\u0BC2\u0B9F\u0BC1" else "Close"
        val zoomIn = if (lang == Lang.TA) "\u0BAA\u0BC6\u0BB0\u0BBF\u0BA4\u0BBE\u0B95\u0BCD\u0B95\u0BC1" else "Zoom in"
        fun open() {
            val fig = requireNotNull(device.findObject(By.descStartsWith(figurePrefix))) { "inline figure missing" }
            fig.click()
            assertTrue("viewer did not open", device.wait(Until.hasObject(By.text(close)), 8000) || device.wait(Until.hasObject(By.desc(close)), 2000))
            assertTrue("zoom control missing", hasButton(device, zoomIn))
        }
        capture("v-inline-$tag"); val inline = md5(device, "v-inline-$tag")
        open()
        capture("v-open-$tag"); val opened = md5(device, "v-open-$tag")
        assertNotEquals("enlarged capture is identical to the inline one", inline, opened)
        Thread.sleep(2000)
        assertTrue("viewer closed itself after opening", hasButton(device, close))
        requireNotNull(button(device, zoomIn)).click()
        capture("v-zoomed-$tag")
        assertNotEquals("Zoom did not change the frame", opened, md5(device, "v-zoomed-$tag"))
        requireNotNull(button(device, close)).click()
        assertTrue("Close button did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
        open()
        device.pressBack()
        assertTrue("Back did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
        assertTrue("inline figure missing after closing", device.wait(Until.hasObject(By.descStartsWith(figurePrefix)), 5000))
    }

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
                val prefix = if (lang == Lang.TA) "\u0BAA\u0B9F\u0BAE\u0BCD:" else "Diagram:"
                assertTrue("inline figure not found", device.wait(Until.hasObject(By.descStartsWith(prefix)), 10000))
                capture("02-lesson-" + lang.name)
                exerciseViewer(device, lang, prefix, "vectors-" + lang.name, ::capture)
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

    @Test fun captureBookThemes() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle(); Thread.sleep(600)
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (lang in Lang.entries) for (theme in com.gasczoology.animalbiotech.ui.theme.ReaderTheme.entries) {
                scenario.onActivity { activity ->
                    val ch = Repo.units(activity).flatMap { it.topics }
                        .firstNotNullOf { ChapterRepo.load(activity, it.file, it.unitRoman)?.chapter }
                    activity.setContent {
                        MaterialTheme { LessonScreen(ch, lang, {}, 115, false, {}, false, {}, {}, null,
                            theme = theme, eyebrow = "Unit I", onNext = {}) }
                    }
                }
                capture("09-book-" + theme.name + "-" + lang.name)
            }
        }
    }

    @Test fun captureBookBodies() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle(); Thread.sleep(600)
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (lang in Lang.entries) for (theme in com.gasczoology.animalbiotech.ui.theme.ReaderTheme.entries) {
                scenario.onActivity { activity ->
                    var found: com.gasczoology.animalbiotech.content.Chapter? = null
                    for (un in Repo.units(activity)) for (t in un.topics) {
                        val ch = ChapterRepo.load(activity, t.file, t.unitRoman)?.chapter ?: continue
                        val all = ch.sections.flatMap { it.blocks }
                        val para = all.firstOrNull { it is com.gasczoology.animalbiotech.content.Paragraph }
                        val callouts = all.filterIsInstance<com.gasczoology.animalbiotech.content.Callout>().distinctBy { com.gasczoology.animalbiotech.ui.theme.calloutRole(it.kind, it.text.en) }.take(2)
                        val tbl = all.firstOrNull { it is com.gasczoology.animalbiotech.content.Table }
                        if (para != null && callouts.isNotEmpty() && tbl != null) {
                            found = ch.copy(sections = listOf(ch.sections.first().copy(blocks = listOf(para) + callouts + tbl)))
                            break
                        }
                    }
                    val fx = requireNotNull(found)
                    activity.setContent {
                        MaterialTheme { LessonScreen(fx, lang, {}, 115, false, {}, false, {}, {}, null, theme = theme, startIndex = 1) }
                    }
                }
                capture("10-body-" + theme.name + "-" + lang.name)
            }
        }
    }

    @Test fun captureInlineFigure() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle(); Thread.sleep(800)
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (lang in Lang.entries) {
                scenario.onActivity { activity ->
                    val t = Repo.units(activity).flatMap { it.topics }.first { it.file.startsWith("enzymes_genetic_engineering") }
                    val ch = requireNotNull(ChapterRepo.load(activity, t.file, t.unitRoman)?.chapter)
                    val starts = com.gasczoology.animalbiotech.ui.sectionStartIndices(ch)
                    var idx = 0
                    ch.sections.forEachIndexed { si, s -> val bi = s.blocks.indexOfFirst { it is com.gasczoology.animalbiotech.content.Diagram }
                        if (bi >= 0 && idx == 0) idx = starts[si] + 1 + bi }
                    require(idx > 0) { "no inlined figure in enzymes chapter" }
                    activity.setContent { MaterialTheme { LessonScreen(ch, lang, {}, 100, false, {}, false, {}, {}, null, startIndex = idx) } }
                }
                capture("11-inline-figure-" + lang.name)
            }
        }
    }

    /** Pilot figure at the current screen size: inline and viewer captures, pinch, rotation and memory; strict, no silent skips. */
    @Test fun capturePilotFigure() {
        val pilot = "screening_selection_recombinants-d02"
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        val dm = inst.targetContext.resources.displayMetrics
        val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        fun capture(name: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png") }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (lang in Lang.entries) {
                scenario.onActivity { activity ->
                    val t = Repo.units(activity).flatMap { it.topics }.first { it.file.startsWith("screening_selection_recombinants") }
                    val ch = requireNotNull(ChapterRepo.load(activity, t.file, t.unitRoman)?.chapter)
                    val starts = com.gasczoology.animalbiotech.ui.sectionStartIndices(ch)
                    var idx = -1
                    ch.sections.forEachIndexed { si, s -> val bi = s.blocks.indexOfFirst { it is Diagram && it.id == pilot }
                        if (bi >= 0 && idx < 0) idx = starts[si] + 1 + bi }
                    require(idx > 0) { "pilot figure not found in chapter" }
                    activity.setContent { MaterialTheme { LessonScreen(ch, lang, {}, 100, false, {}, false, {}, {}, null, startIndex = idx) } }
                }
                val prefix = if (lang == Lang.EN) "Diagram:" else "\u0BAA\u0B9F\u0BAE\u0BCD:"
                assertTrue("pilot figure not found", device.wait(Until.hasObject(By.descStartsWith(prefix)), 10000))
                capture("12-pilot-inline-$tag-${lang.name}")
                val close = if (lang == Lang.EN) "Close" else "\u0BAE\u0BC2\u0B9F\u0BC1"
                requireNotNull(device.findObject(By.descStartsWith(prefix))).click()
                assertTrue("pilot viewer did not open", device.wait(Until.hasObject(By.text(close)), 8000) || device.wait(Until.hasObject(By.desc(close)), 2000))
                Thread.sleep(1500)
                assertTrue("viewer closed itself", hasButton(device, close))
                capture("13-pilot-viewer-$tag-${lang.name}")
                assertTrue("viewer is not landscape", device.displayWidth > device.displayHeight)
                // real pinch on the figure inside the viewer (the last matching node is the top window's)
                val before = md5(device, "13-pilot-viewer-$tag-${lang.name}")
                val fig = device.findObjects(By.descStartsWith(prefix)).last()
                fig.pinchOpen(0.6f, 400)
                capture("14-pilot-pinched-$tag-${lang.name}")
                assertNotEquals("pinch did not change the frame", before, md5(device, "14-pilot-pinched-$tag-${lang.name}"))
                // memory with the viewer open and zoomed (logged; read from the CI log)
                device.executeShellCommand("dumpsys meminfo com.gasczoology.animalbiotech").lines().filter { it.isNotBlank() }
                    .forEach { android.util.Log.e("CITEST-MEM", "$tag ${lang.name} $it") }
                device.pressBack()
                assertTrue("Back did not close the pilot viewer", device.wait(Until.gone(By.text(close)), 5000))
            }
        }
    }
}
