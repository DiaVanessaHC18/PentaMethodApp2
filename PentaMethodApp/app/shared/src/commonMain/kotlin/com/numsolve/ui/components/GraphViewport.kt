package com.numsolve.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

/** Rangos independientes: mover X no vuelve a ajustar Y automáticamente. */
internal data class GraphViewport(val xMin: Double, val xMax: Double, val yMin: Double, val yMax: Double) {
    fun transform(base: GraphViewport, anchorX: Double, anchorY: Double,
                  panX: Double, panY: Double, zoom: Double): GraphViewport {
        if (!listOf(anchorX, anchorY, panX, panY, zoom).all { it.isFinite() } || zoom <= 0) return this
        val width = xMax - xMin
        val height = yMax - yMin
        val baseWidth = base.xMax - base.xMin
        val baseHeight = base.yMax - base.yMin
        val scale = (width / zoom).coerceIn(baseWidth / 100, baseWidth * 20) / width
        val newWidth = width * scale
        val newHeight = height * scale
        val left = (xMin + anchorX * width - (anchorX + panX) * newWidth)
            .coerceIn(base.xMin - baseWidth * 20, base.xMax + baseWidth * 20 - newWidth)
        val bottom = (yMin + anchorY * height - (anchorY - panY) * newHeight)
            .coerceIn(base.yMin - baseHeight * 20, base.yMax + baseHeight * 20 - newHeight)
        val result = GraphViewport(left, left + newWidth, bottom, bottom + newHeight)
        return if (listOf(result.xMin, result.xMax, result.yMin, result.yMax).all { it.isFinite() } &&
            result.xMin < result.xMax && result.yMin < result.yMax) result else this
    }
}

/** Un dedo queda disponible para el scroll, salvo que se active Mover. */
internal fun Modifier.graphGestures(panEnabled: Boolean, onTransform: (Offset, Offset, Float) -> Unit) =
    pointerInput(panEnabled) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var dragging = false
            var totalPan = Offset.Zero
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.any { it.isConsumed }) break
                val multiple = event.changes.count { it.pressed } >= 2
                val pan = event.calculatePan()
                totalPan += pan
                if (multiple || (panEnabled && totalPan.getDistance() > viewConfiguration.touchSlop)) dragging = true
                if (dragging) {
                    val zoom = event.calculateZoom()
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (centroid != Offset.Unspecified && (pan != Offset.Zero || abs(zoom - 1f) > 0f)) {
                        onTransform(centroid, pan, zoom)
                    }
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            } while (event.changes.any { it.pressed })
        }
    }
