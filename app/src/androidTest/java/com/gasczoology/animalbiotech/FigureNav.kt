package com.gasczoology.animalbiotech

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.content.ChapterRepo
import com.gasczoology.animalbiotech.content.Diagram
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.ui.LessonScreen
import org.junit.Assert.assertTrue

/** Shared navigation for the figure tests: put a figure's lesson on screen, scroll to it by its semantics tag figure-<id>, align and tap it. */
object FigureNav {
    /** Instrumentation argument as a list; a missing or blank argument fails loudly (a test that silently ran nothing would look green). */
    fun listArg(name: String): List<String> {
        val v = InstrumentationRegistry.getArguments().getString(name)
        require(!v.isNullOrBlank()) { "instrumentation argument '$name' is required (comma-separated); nothing would be tested" }
        return v.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun show(activity: MainActivity, id: String, lang: Lang) {
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

    /** Scroll until a node tagged figure-<id> is on screen; on a miss, capture and log what was on screen, then fail with the ID. */
    fun reach(device: UiDevice, id: String, tag: String, lang: Lang, capture: (String) -> Unit) {
        val sel = By.res("figure-$id")
        var shown = device.wait(Until.hasObject(sel), 6000)
        val w = device.displayWidth / 2; val h = device.displayHeight
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
    }

    /** Bring the figure's top into the band under the top bar (a clipped tall figure reports a top at the bar, so it only passes once truly aligned). */
    fun alignTop(device: UiDevice, id: String) {
        val sel = By.res("figure-$id"); val h = device.displayHeight; val w = device.displayWidth / 2
        for (round in 1..8) {
            try {
                val top = device.findObject(sel)?.visibleBounds?.top ?: break
                if (top >= h * 0.185 && top <= h * 0.30) break
                val delta = (top - h * 0.22).toInt()
                val dy = delta.coerceIn(-(h * 0.5).toInt(), (h * 0.5).toInt()).let { if (it == 0) 1 else it }
                val y0 = (h * 0.55).toInt()
                device.swipe(w, y0, w, (y0 - dy).coerceIn(10, h - 10), 25)
                device.waitForIdle()
            } catch (e: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(400) }
        }
        device.waitForIdle()
    }

    /** Tap the figure by coordinates read from a fresh lookup each attempt (a node click can hit a stale reference while the lazy list settles). */
    fun tap(device: UiDevice, id: String) {
        val sel = By.res("figure-$id"); var clicked = false
        for (attempt in 1..8) {
            try {
                device.waitForIdle(); Thread.sleep(500)
                val b = requireNotNull(device.findObject(sel)) { "figure-$id vanished before click" }.visibleBounds
                device.click(b.centerX(), b.centerY()); clicked = true; break
            } catch (e: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(600) }
        }
        assertTrue("figure-$id could not be clicked (stale node 8 times)", clicked)
    }
}
