package com.gasczoology.animalbiotech.ui.blocks

import java.io.File
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FigureBitmapTest {
    @Test fun inlineDecodeStaysAtLeastDisplayWidth() {
        assertEquals(1, sampleSizeFor(1440, 3232, 788))   // 300 dp at 420 dpi: halving would give 720 < 788
        assertEquals(2, sampleSizeFor(1440, 3232, 600))   // 300 dp at 320 dpi: 720 >= 600
        assertEquals(4, sampleSizeFor(2000, 744, 400))
        assertEquals(1, sampleSizeFor(1440, 3232, null))
    }
    @Test fun sampleSizeKeepsEverySideUnderTextureLimit() {
        assertEquals(2, sampleSizeFor(5000, 3000, null))
        assertEquals(4, sampleSizeFor(9000, 3000, null))
        assertTrue(maxOf(8191, 100) / sampleSizeFor(8191, 100, null) <= 4096)
    }
    @Test fun zoomIsCappedAtCrispFactorAndNeverBelowOne() {
        assertEquals(1.333f, crispMaxZoom(1440, 1080f), 0.001f)   // 411 dp phone, portrait
        assertEquals(1f, crispMaxZoom(1440, 2000f), 0f)           // landscape: fit-to-width is already above native pixels
        assertEquals(3f, crispMaxZoom(2880, 960f), 0f)
    }
    /** Every shipped figure keeps both sides at or below 4096 px, so none needs tiling on a 4096-texture GPU (minSdk 26). */
    @Test fun everyShippedFigureIsWithinTextureLimit() {
        val dir = listOf(File("src/main/assets/diagrams"), File("app/src/main/assets/diagrams")).first { it.isDirectory }
        val pngs = dir.listFiles { f -> f.name.endsWith(".png") }!!
        assertTrue("no figures found", pngs.size > 100)
        for (f in pngs) {
            val head = f.inputStream().use { it.readNBytes(24) }
            val bb = ByteBuffer.wrap(head); val w = bb.getInt(16); val h = bb.getInt(20)
            assertTrue("${f.name} is ${w}x$h", w in 1..4096 && h in 1..4096)
        }
    }

    @Test fun scaleToWidthOnlyWhenClearlyOversizedAndInline() {
        assertEquals(940, scaleToWidthFor(1271, 940))
        assertNull(scaleToWidthFor(1000, 940))
        assertNull(scaleToWidthFor(1271, null))
    }
}
