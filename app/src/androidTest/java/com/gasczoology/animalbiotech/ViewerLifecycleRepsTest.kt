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
 * Tier 3: viewer lifecycle and memory budget on a few representative figures (CI passes `reps` as id:LANG pairs: the tallest, the widest and a Tamil one).
 * The viewer's behaviour does not depend on the individual figure, so this is not run per figure.
 * For each: open the viewer, rotate left/right/natural, recreate the activity, press Back; PSS must stay within 150 MB inline and 200 MB with the viewer open.
 */
@RunWith(AndroidJUnit4::class)
class ViewerLifecycleRepsTest {
    private fun assertPssWithinBudget(what: String, limitMb: Int) {
        System.gc(); Thread.sleep(800); System.gc(); Thread.sleep(200)
        val out = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("dumpsys meminfo com.gasczoology.animalbiotech")
        val kb = Regex("TOTAL PSS:\\s+(\\d+)").find(out)?.groupValues?.get(1)?.toInt() ?: Regex("TOTAL\\s+(\\d+)").find(out)?.groupValues?.get(1)?.toInt() ?: error("no PSS in meminfo")
        android.util.Log.i("CITEST", "BUDGET $what PSS=${kb / 1024} MB (limit $limitMb)")
        assertTrue("PSS budget exceeded at $what: ${kb / 1024} MB > $limitMb MB", kb <= limitMb * 1024)
    }

    @Test fun viewerLifecycleAndMemoryOnRepresentativeFigures() {
        val reps = FigureNav.listArg("reps").map { val (id, l) = it.split(':'); id to Lang.valueOf(l) }
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        val dm = inst.targetContext.resources.displayMetrics
        val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        fun capture(name: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png") }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
                for ((id, lang) in reps) {
                    val close = if (lang == Lang.EN) "Close" else "மூடு"
                    fun viewerOpen() = device.wait(Until.hasObject(By.text(close)), 6000) || device.wait(Until.hasObject(By.desc(close)), 1500)
                    scenario.onActivity { FigureNav.show(it, id, lang) }
                    FigureNav.reach(device, id, tag, lang, ::capture)
                    FigureNav.alignTop(device, id)
                    assertPssWithinBudget("inline $id/${lang.name}", 150)
                    FigureNav.tap(device, id)
                    assertTrue("$id/${lang.name}: viewer did not open", viewerOpen())
                    Thread.sleep(1200)
                    assertPssWithinBudget("viewer $id/${lang.name}", 200)
                    device.setOrientationLeft(); device.waitForIdle()
                    assertTrue("$id/${lang.name}: viewer lost after rotating left", viewerOpen())
                    device.setOrientationRight(); device.waitForIdle()
                    assertTrue("$id/${lang.name}: viewer lost after rotating right", viewerOpen())
                    device.setOrientationNatural(); device.waitForIdle()
                    assertTrue("$id/${lang.name}: viewer lost after returning to natural orientation", viewerOpen())
                    scenario.recreate()
                    scenario.onActivity { FigureNav.show(it, id, lang) }
                    assertTrue("$id/${lang.name}: viewer not restored after activity recreation", viewerOpen())
                    capture("22-lifecycle-$tag-$id-${lang.name}")
                    device.pressBack()
                    assertTrue("$id/${lang.name}: Back did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
                }
            }
        } finally { device.setOrientationNatural(); device.unfreezeRotation() }
    }
}
