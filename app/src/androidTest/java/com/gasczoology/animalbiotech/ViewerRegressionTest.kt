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

/**
 * Regression test for the enlarged-figure viewer: open a figure that is deep in a lesson (its list item
 * leaves composition when the list is scrolled), rotate, recreate the activity, press Back.
 * The same file runs unchanged on the buggy viewer (bisect/viewer-only) and on the fix; it must fail on the first and pass on the second.
 */
@RunWith(AndroidJUnit4::class)
class ViewerRegressionTest {
    /** Failure aid: writes every non-empty content description on screen to the CITEST log tag. */
    private fun logDescriptions(device: UiDevice) {
        val bo = java.io.ByteArrayOutputStream(); device.dumpWindowHierarchy(bo)
        Regex("content-desc=\"([^\"]+)\"").findAll(bo.toString()).forEach { android.util.Log.e("CITEST", "desc: " + it.groupValues[1].take(160)) }
    }

    @Test fun viewerSurvivesRotationAndRecreationOnDeepFigure() {
        val pilot = "screening_selection_recombinants-d02"
        val pilotAlt = "pBR322 plasmid"            // alt text of the rebuilt figure
        val pilotCaption = "Insertional inactivation pBR322" // caption used as alt text by the original figure
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        fun fixture(activity: MainActivity) {
            val t = Repo.units(activity).flatMap { it.topics }.first { it.file.startsWith("screening_selection_recombinants") }
            val ch = requireNotNull(ChapterRepo.load(activity, t.file, t.unitRoman)?.chapter)
            val starts = com.gasczoology.animalbiotech.ui.sectionStartIndices(ch)
            var idx = -1
            ch.sections.forEachIndexed { si, s -> val bi = s.blocks.indexOfFirst { it is Diagram && it.id == pilot }
                if (bi >= 0 && idx < 0) idx = starts[si] + 1 + bi }
            require(idx > 5) { "pilot figure is not deep in the lesson (index $idx)" }
            android.util.Log.e("CITEST", "deep figure item index = $idx")
            activity.setContent { MaterialTheme { LessonScreen(ch, Lang.EN, {}, 100, false, {}, false, {}, {}, null, startIndex = (idx - 10).coerceAtLeast(0)) } }
        }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
                scenario.onActivity { fixture(it) }
                var swipes = 0
                /** Centre of a clickable figure with a quarter screen visible, read eagerly (a node reference goes stale while the list moves). */
                fun candidate(): Pair<Int, Int>? = try {
                    device.findObjects(By.descContains(pilotAlt)).ifEmpty { device.findObjects(By.descContains(pilotCaption)) }.mapNotNull {
                        try { val r = it.visibleBounds
                            if (r.height() >= device.displayHeight / 4) Triple(r.centerX(), r.centerY(), it.contentDescription) else null
                        } catch (e: androidx.test.uiautomator.StaleObjectException) { null }
                    }.firstOrNull()?.also { android.util.Log.e("CITEST", "figure ${it.third?.take(80)} at ${it.first},${it.second}") }?.let { it.first to it.second }
                } catch (e: androidx.test.uiautomator.StaleObjectException) { null }
                var target: Pair<Int, Int>? = null
                while (target == null && swipes < 40) {
                    target = candidate()
                    if (target == null) { device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4, device.displayWidth / 2, device.displayHeight / 2, 25); swipes++; device.waitForIdle(); Thread.sleep(500) }
                }
                android.util.Log.e("CITEST", "scrolled $swipes swipes to reach a clickable figure with a quarter screen visible")
                val (cx, cy) = requireNotNull(target) { "no clickable figure reached by scrolling" }
                device.click(cx, cy)
                val close = "Close"
                fun viewerOpen() = device.wait(Until.hasObject(By.text(close)), 6000) || device.wait(Until.hasObject(By.desc(close)), 1500)
                if (!viewerOpen()) { logDescriptions(device); org.junit.Assert.fail("viewer did not open") }
                device.setOrientationLeft(); device.waitForIdle()
                assertTrue("viewer lost after rotating left", viewerOpen())
                device.setOrientationRight(); device.waitForIdle()
                assertTrue("viewer lost after rotating right", viewerOpen())
                device.setOrientationNatural(); device.waitForIdle()
                assertTrue("viewer lost after returning to natural orientation", viewerOpen())
                scenario.recreate()
                scenario.onActivity { fixture(it) }
                assertTrue("viewer not restored after activity recreation", viewerOpen())
                device.pressBack()
                assertTrue("Back did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
            }
        } finally { device.setOrientationNatural(); device.unfreezeRotation() }
    }
}
