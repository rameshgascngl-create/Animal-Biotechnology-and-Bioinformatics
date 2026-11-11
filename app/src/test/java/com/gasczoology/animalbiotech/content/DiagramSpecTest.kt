package com.gasczoology.animalbiotech.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiagramSpecTest {
    private val ok = """{"id":"d1","w":800,"h":400,"markers":[
        {"n":1,"x":0.25,"y":1.4,"en":"Anode","ta":"நேர்மின்முனை","ta_status":"draft"},
        {"n":2,"x":-0.2,"y":0.5,"en":"Wells"}]}"""

    @Test fun parsesAspectAndMarkers() {
        val s = parseDiagramSpec(ok)!!
        assertEquals(2f, s.aspect, 0.001f)
        assertEquals(2, s.markers.size)
        assertEquals("நேர்மின்முனை", s.markers[0].label.ta)
    }
    @Test fun clampsMarkersIntoImage() {
        val s = parseDiagramSpec(ok)!!
        assertEquals(1f, s.markers[0].y, 0f)
        assertEquals(0f, s.markers[1].x, 0f)
    }
    @Test fun missingTamilIsEmptyNotCrash() = assertEquals("", parseDiagramSpec(ok)!!.markers[1].label.ta)
    @Test fun malformedReturnsNull() { assertNull(parseDiagramSpec("{oops")); assertNull(parseDiagramSpec("""{"id":"x","w":0,"h":1,"markers":[]}""")) }
}
