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
 * Each entry is (figure id, text its content description must contain in English, same in Tamil).
 */
@RunWith(AndroidJUnit4::class)
class BatchFigureCaptureTest {
    private val batch = listOf(
        Triple("screening_selection_recombinants-d01", "Direct selection on ampicillin", "ஆம்பிசிலினில் நேரடி"),
        Triple("sanger_dna_sequencing-d01", "Comparison of a normal deoxynucleotide", "சாதாரண டிஆக்ஸி"),
        Triple("vectors_genetic_engineering-d04", "M13 phage", "M13 பேஜ்"),
    )

    @Test fun captureBatchFigures() {
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        val dm = inst.targetContext.resources.displayMetrics
        val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        // Phase 2 policy: figures are judged at the real reader widths 360x800 and 411x914 dp. The 360x640 base-suite screen is not part of it.
        org.junit.Assume.assumeTrue("batch capture runs at 360x800 and 411x914 only", tag != "360x640")
        fun capture(name: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png") }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for ((id, en, ta) in batch) for (lang in Lang.entries) {
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
                val needle = if (lang == Lang.EN) en else ta
                var shown = device.wait(Until.hasObject(By.descContains(needle)), 10000)
                // Blocks can span several list items, so the arithmetic index is only approximate: start 8 items early and scroll forward until the figure shows.
                var tries = 0
                while (!shown && tries < 14) {
                    device.swipe(dm.widthPixels / 2, (dm.heightPixels * 0.7).toInt(), dm.widthPixels / 2, (dm.heightPixels * 0.3).toInt(), 20)
                    shown = device.wait(Until.hasObject(By.descContains(needle)), 1200); tries++
                }
                tries = 0
                while (!shown && tries < 30) {
                    device.swipe(dm.widthPixels / 2, (dm.heightPixels * 0.3).toInt(), dm.widthPixels / 2, (dm.heightPixels * 0.7).toInt(), 20)
                    shown = device.wait(Until.hasObject(By.descContains(needle)), 1200); tries++
                }
                if (!shown) {
                    capture("29-fail-$tag-$id-${lang.name}")
                    val out = java.io.ByteArrayOutputStream(); device.dumpWindowHierarchy(out)
                    android.util.Log.e("CITEST", "hierarchy for $id/${lang.name}: " + out.toString("UTF-8").replace("\n", " ").take(6000))
                }
                assertTrue("$id/${lang.name}: figure with description containing '$needle' not shown", shown)
                capture("20-batch-$tag-$id-${lang.name}-inline")
                val close = if (lang == Lang.EN) "Close" else "மூடு"
                requireNotNull(device.findObject(By.descContains(needle))).click()
                assertTrue("$id/${lang.name}: viewer did not open", device.wait(Until.hasObject(By.text(close)), 8000) || device.wait(Until.hasObject(By.desc(close)), 2000))
                Thread.sleep(1200)
                assertTrue("$id/${lang.name}: viewer closed itself", device.hasObject(By.text(close)) || device.hasObject(By.desc(close)))
                capture("21-batch-$tag-$id-${lang.name}-viewer")
                device.pressBack()
                assertTrue("$id/${lang.name}: Back did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
            }
        }
    }
}
