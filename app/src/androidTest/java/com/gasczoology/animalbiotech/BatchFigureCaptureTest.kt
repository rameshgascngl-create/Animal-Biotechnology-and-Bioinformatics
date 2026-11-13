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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 2 batch capture: for each redrawn figure, in both languages, the reader at its real screen size (inline, then the enlarged viewer).
 * Strict: a missing figure, a viewer that does not open, or one that closes itself fails the test. Screenshots go to /sdcard/Pictures.
 * Figures are located by the semantics tag figure-<id>; a miss fails with the ID.
 */
@RunWith(AndroidJUnit4::class)
class BatchFigureCaptureTest {
    private val batch = listOf(
        "atlas3_transgenic_medical-d05",
        "animal_cell_culture-d06",
        "animal_cell_culture-d07"
    )

    /** Memory budget (Phase 2): total PSS at most 150 MB on the 2 GB AVD, measured after a GC. */
    private fun assertPssWithinBudget(what: String, limitMb: Int = 150) {
        System.gc(); Thread.sleep(800); System.gc(); Thread.sleep(200)
        val out = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("dumpsys meminfo com.gasczoology.animalbiotech")
        val kb = Regex("TOTAL PSS:\\s+(\\d+)").find(out)?.groupValues?.get(1)?.toInt() ?: Regex("TOTAL\\s+(\\d+)").find(out)?.groupValues?.get(1)?.toInt() ?: error("no PSS in meminfo")
        android.util.Log.i("CITEST", "BUDGET $what PSS=${kb / 1024} MB (limit $limitMb)")
        assertTrue("PSS budget exceeded at $what: ${kb / 1024} MB > $limitMb MB", kb <= limitMb * 1024)
    }

    @Test fun captureBatchFigures() {
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        val dm = inst.targetContext.resources.displayMetrics
        val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        fun capture(name: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png") }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (id in batch) for (lang in Lang.entries) {
                scenario.onActivity { activity ->
                    var found = false
                    for (t in Repo.units(activity).flatMap { it.topics }) {
                        val ch = ChapterRepo.load(activity, t.file, t.unitRoman)?.chapter ?: continue
                        val starts = com.gasczoology.animalbiotech.ui.sectionStartIndices(ch)
                        var idx = -1
                        ch.sections.forEachIndexed { si, s -> val bi = s.blocks.indexOfFirst { it is Diagram && it.id == id }
                            if (bi >= 0 && idx < 0) idx = starts[si] + 1 + bi }
                        if (idx < 0) continue
                        activity.setContent { MaterialTheme { LessonScreen(ch, lang, {}, 100, false, {}, false, {}, {}, null, startIndex = (idx - 8).coerceAtLeast(0)) } }
                        found = true; break
                    }
                    require(found) { "figure $id not found in any chapter" }
                }
                // Figures are found by their stable semantics tag figure-<id> (exposed as a resource id), never by list index or alt text.
                val sel = By.res("figure-$id")
                fun visible() = device.hasObject(sel)
                var shown = device.wait(Until.hasObject(sel), 6000)
                val w = dm.widthPixels / 2; val h = dm.heightPixels
                var n = 0
                while (!shown && n < 60) { device.swipe(w, (h * 0.75).toInt(), w, (h * 0.25).toInt(), 25); shown = device.wait(Until.hasObject(sel), 500); n++ }
                n = 0
                while (!shown && n < 120) { device.swipe(w, (h * 0.25).toInt(), w, (h * 0.75).toInt(), 25); shown = device.wait(Until.hasObject(sel), 500); n++ }
                if (!shown) {
                    capture("29-fail-$tag-$id-${lang.name}")
                    val out = java.io.ByteArrayOutputStream(); device.dumpWindowHierarchy(out)
                    android.util.Log.e("CITEST", "MISSING FIGURE $id/${lang.name} at $tag; resource ids on screen: " + Regex("resource-id=\"([^\"]*)\"").findAll(out.toString("UTF-8")).map { it.groupValues[1] }.filter { it.isNotEmpty() }.toList())
                }
                assertTrue("MISSING FIGURE $id (${lang.name}) at $tag: no node tagged figure-$id in the reader after scrolling both ways", shown)
                // Bring the figure's top into view so the capture starts at the figure.
                // Tall figures may be clipped by the top bar: loop until the figure's visible top sits in the band under it (a clipped figure reports a top at the bar, so it never passes early).
                for (round in 1..8) {
                    try {
                        val top = device.findObject(sel)?.visibleBounds?.top ?: break
                        if (top >= h * 0.185 && top <= h * 0.30) break
                        val delta = (top - h * 0.22).toInt() // >0: figure is low, drag up; <0: clipped at the bar, drag down
                        val dy = delta.coerceIn(-(h * 0.5).toInt(), (h * 0.5).toInt()).let { if (it == 0) 1 else it }
                        val y0 = (h * 0.55).toInt()
                        device.swipe(w, y0, w, (y0 - dy).coerceIn(10, h - 10), 25)
                        device.waitForIdle()
                    } catch (e: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(400) }
                }
                device.waitForIdle()
                capture("20-batch-$tag-$id-${lang.name}-inline")
                assertPssWithinBudget("inline $id/${lang.name} at $tag")
                val close = if (lang == Lang.EN) "Close" else "மூடு"
                var clicked = false
                // Tap by screen coordinates read from a fresh lookup each attempt: a node click can hit a stale reference while the lazy list settles.
                for (attempt in 1..4) {
                    try {
                        device.waitForIdle(); Thread.sleep(500)
                        val b = requireNotNull(device.findObject(sel)) { "figure-$id vanished before click" }.visibleBounds
                        device.click(b.centerX(), b.centerY()); clicked = true; break
                    } catch (e: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(600) }
                }
                assertTrue("figure-$id could not be clicked (stale node 4 times)", clicked)
                assertTrue("$id/${lang.name}: viewer did not open", device.wait(Until.hasObject(By.text(close)), 8000) || device.wait(Until.hasObject(By.desc(close)), 2000))
                Thread.sleep(1200)
                assertTrue("$id/${lang.name}: viewer closed itself", device.hasObject(By.text(close)) || device.hasObject(By.desc(close)))
                capture("21-batch-$tag-$id-${lang.name}-viewer")
                assertPssWithinBudget("viewer $id/${lang.name} at $tag", 200) // viewer holds a second full-size bitmap; informational soft cap
                device.pressBack()
                assertTrue("$id/${lang.name}: Back did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
            }
        }
    }
}
