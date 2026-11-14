package com.gasczoology.animalbiotech

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.i18n.Lang
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tier 2: inline capture of a batch's NEW figures only, in both languages, at the screen size the job set.
 * The ids come from the instrumentation argument `figures` (comma-separated; CI derives them from the changed assets), never from a list in this file,
 * so a batch cannot re-run an earlier batch. A missing argument, a missing figure or a figure that is not on screen fails the test.
 * The viewer lifecycle and the memory budget are viewer/app properties, not per-figure ones: they are Tier 3 (ViewerLifecycleRepsTest).
 */
@RunWith(AndroidJUnit4::class)
class BatchFigureCaptureTest {
    @Test fun captureBatchFigures() {
        val batch = FigureNav.listArg("figures")
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        val dm = inst.targetContext.resources.displayMetrics
        val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        fun capture(name: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png") }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (id in batch) for (lang in Lang.entries) {
                scenario.onActivity { FigureNav.show(it, id, lang) }
                FigureNav.reach(device, id, tag, lang, ::capture)
                FigureNav.alignTop(device, id)
                capture("20-batch-$tag-$id-${lang.name}-inline")
            }
        }
    }
}
