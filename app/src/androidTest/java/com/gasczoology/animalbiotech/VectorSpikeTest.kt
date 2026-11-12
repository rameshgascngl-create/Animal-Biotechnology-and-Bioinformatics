package com.gasczoology.animalbiotech

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.ui.blocks.VectorFigure
import org.junit.Test
import org.junit.runner.RunWith

/** SPIKE: native vector pilot. Captures (width 300 dp as inline), 3x zoom crop, font scale 1.5, and memory. Not a product test. */
@RunWith(AndroidJUnit4::class)
class VectorSpikeTest {
    @Test fun captureVectorPilot() {
        val inst = InstrumentationRegistry.getInstrumentation(); val device = UiDevice.getInstance(inst)
        fun capture(n: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$n.png") }
        fun mem(m: String) { val o = device.executeShellCommand("dumpsys meminfo com.gasczoology.animalbiotech")
            for (key in listOf("Java Heap:", "Native Heap:", "TOTAL PSS:")) o.lines().firstOrNull { it.trim().startsWith(key) }?.let { android.util.Log.e("CITEST-MEM", "VECTOR $m | ${it.trim().replace(Regex("\\\\s+"), " ")}") } }
        val dm = inst.targetContext.resources.displayMetrics; val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            for (lang in Lang.entries) {
                for ((variant, fontScale, zoom) in listOf(Triple("1x", 1f, 1f), Triple("fs15", 1.5f, 1f), Triple("zoom3", 1f, 3f))) {
                    scenario.onActivity { a ->
                        a.setContent { MaterialTheme {
                            val d = LocalDensity.current
                            CompositionLocalProvider(LocalDensity provides Density(d.density, fontScale)) {
                                Box(Modifier.fillMaxSize().clipToBounds().verticalScroll(rememberScrollState())) {
                                    Box(Modifier.padding(start = 30.dp).width(300.dp).graphicsLayer(scaleX = zoom, scaleY = zoom, transformOrigin = TransformOrigin(0.5f, 0.12f))) {
                                        VectorFigure("screening_selection_recombinants-d02", lang)
                                    } } } } }
                    }
                    capture("v-spike-$tag-${lang.name}-$variant"); if (variant == "1x") mem("$tag ${lang.name} vector-inline")
                }
            }
        }
    }
    private fun assertTrue(b: Boolean) = org.junit.Assert.assertTrue(b)
}
