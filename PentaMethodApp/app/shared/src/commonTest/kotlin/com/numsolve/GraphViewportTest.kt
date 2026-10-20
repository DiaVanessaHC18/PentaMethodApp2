package com.numsolve

import com.numsolve.ui.components.GraphViewport
import kotlin.test.*

class GraphViewportTest {
    private val base = GraphViewport(-10.0, 10.0, -20.0, 20.0)

    @Test fun zoomPreservesOffCenterAnchor() {
        val zoomed = base.transform(base, 0.25, 0.75, 0.0, 0.0, 2.0)
        assertEquals(10.0, zoomed.xMax - zoomed.xMin, 1e-12)
        assertEquals(-5.0, zoomed.xMin + 0.25 * (zoomed.xMax - zoomed.xMin), 1e-12)
        assertEquals(10.0, zoomed.yMin + 0.75 * (zoomed.yMax - zoomed.yMin), 1e-12)
        assertEquals(base, zoomed.transform(base, 0.25, 0.75, 0.0, 0.0, 0.5))
    }

    @Test fun dragMovesImageRightAndDownWithoutRescaling() {
        val moved = base.transform(base, 0.5, 0.5, 0.1, 0.1, 1.0)
        assertEquals(-12.0, moved.xMin, 1e-12)
        assertEquals(-16.0, moved.yMin, 1e-12)
        assertEquals(20.0, moved.xMax - moved.xMin, 1e-12)
    }

    @Test fun extremeZoomAndInvalidInputRemainBounded() {
        val close = base.transform(base, 0.5, 0.5, 0.0, 0.0, 1e100)
        val far = base.transform(base, 0.5, 0.5, 0.0, 0.0, 1e-100)
        assertEquals(0.2, close.xMax - close.xMin, 1e-12)
        assertEquals(400.0, far.xMax - far.xMin, 1e-12)
        assertEquals(base, base.transform(base, 0.5, 0.5, 0.0, 0.0, Double.NaN))
    }
}
