package com.numsolve.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.numsolve.domain.math.MathExpression
import com.numsolve.ui.theme.AppColors

import kotlin.math.max
import kotlin.math.min

/** Un punto (x, f(x)) ya evaluado y válido (sin NaN/Infinity), listo para graficar. */
internal data class SamplePoint(val x: Double, val y: Double)

/** Muestrea f de forma segura en [domainMin, domainMax], ignorando puntos fuera de dominio. */
internal fun sampleFunction(f: MathExpression, domainMin: Double, domainMax: Double, steps: Int = 160): List<SamplePoint> {
    if (domainMin >= domainMax) return emptyList()
    val points = mutableListOf<SamplePoint>()
    val step = (domainMax - domainMin) / steps
    for (i in 0..steps) {
        val x = domainMin + i * step
        val y = try { f.evaluate(x) } catch (e: Exception) { Double.NaN }
        if (y.isFinite()) points += SamplePoint(x, y)
    }
    return points
}

/**
 * Convierte coordenadas matemáticas a coordenadas de pantalla dentro de un
 * DrawScope, dejando un margen y recortando valores extremos de Y (asíntotas)
 * para que la curva siga siendo legible.
 */
internal class PlotMapper(
    private val width: Float,
    private val height: Float,
    private val xMin: Double,
    private val xMax: Double,
    private val yMin: Double,
    private val yMax: Double,
    private val margin: Float = 28f,
    private val clampY: Boolean = true
) {
    private val plotW = width - 2 * margin
    private val plotH = height - 2 * margin

    fun toScreen(x: Double, y: Double): Offset {
        val clampedY = if (clampY) y.coerceIn(yMin, yMax) else y
        val nx = if (xMax == xMin) 0.5 else (x - xMin) / (xMax - xMin)
        val ny = if (yMax == yMin) 0.5 else (clampedY - yMin) / (yMax - yMin)
        val sx = margin + nx.toFloat() * plotW
        val sy = margin + (1f - ny.toFloat()) * plotH
        return Offset(sx, sy)
    }
}

/**
 * Dibuja la curva de f(x) sobre un dominio dado, con ejes de referencia.
 * [overlay] permite a cada método superponer sus propios elementos (marcas de
 * intervalo, tangente, secante, escalera de punto fijo) usando el mismo mapper.
 */
@Composable
internal fun FunctionCurveCanvas(
    points: List<SamplePoint>,
    modifier: Modifier = Modifier,
    overlay: (DrawScope.(PlotMapper) -> Unit)? = null
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (points.isEmpty()) {
            return@Canvas
        }

        val xMin = points.minOf { it.x }
        val xMax = points.maxOf { it.x }
        var yMin = points.minOf { it.y }
        var yMax = points.maxOf { it.y }
        if (yMin == yMax) {
            yMin -= 1.0; yMax += 1.0
        }
        // Un poco de aire arriba/abajo para que la curva no toque el borde.
        val yPad = (yMax - yMin) * 0.12
        yMin -= yPad; yMax += yPad

        val mapper = PlotMapper(w, h, xMin, xMax, yMin, yMax)

        // Eje X (y = 0) si está dentro de rango visible
        if (0.0 in yMin..yMax) {
            val yZero = mapper.toScreen(xMin, 0.0).y
            drawLine(
                color = AppColors.border,
                start = Offset(0f, yZero),
                end = Offset(w, yZero),
                strokeWidth = 1.5f
            )
        }
        // Eje Y (x = 0) si está dentro de rango visible
        if (0.0 in xMin..xMax) {
            val xZero = mapper.toScreen(0.0, yMin).x
            drawLine(
                color = AppColors.border,
                start = Offset(xZero, 0f),
                end = Offset(xZero, h),
                strokeWidth = 1.5f
            )
        }

        // Curva f(x)
        var previous: Offset? = null
        for (p in points) {
            val screen = mapper.toScreen(p.x, p.y)
            val prev = previous
            if (prev != null) {
                drawLine(
                    color = AppColors.primary,
                    start = prev,
                    end = screen,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            previous = screen
        }

        overlay?.invoke(this, mapper)
    }
}

internal fun expandedDomain(vararg values: Double, minSpan: Double = 2.0): Pair<Double, Double> {
    val lo = values.min()
    val hi = values.max()
    val span = max(hi - lo, minSpan)
    val pad = span * 0.35
    return (lo - pad) to (hi + pad)
}

/**
 * Cuando el usuario todavía no definió a/b/x0/x1, se busca un dominio
 * "interesante" automáticamente: se escanea una ventana amplia buscando
 * cambios de signo de f y se centra el dominio ahí (con margen). Si no se
 * encuentra ninguno, se cae a un rango genérico [-10, 10].
 */
internal fun autoDomainForFunction(f: MathExpression, wideScan: Double = 25.0, steps: Int = 250): Pair<Double, Double> {
    var prevX: Double? = null
    var prevY: Double? = null
    var minCross = Double.POSITIVE_INFINITY
    var maxCross = Double.NEGATIVE_INFINITY
    var found = false

    val step = (2 * wideScan) / steps
    for (i in 0..steps) {
        val x = -wideScan + i * step
        val y = try { f.evaluate(x) } catch (e: Exception) { null }
        if (y != null && y.isFinite()) {
            if (prevY != null && prevX != null && (prevY!! * y < 0.0 || y == 0.0)) {
                minCross = min(minCross, min(prevX!!, x))
                maxCross = max(maxCross, max(prevX!!, x))
                found = true
            }
            prevX = x; prevY = y
        }
    }

    return if (found) {
        val span = max(maxCross - minCross, 1.0)
        val pad = max(span * 1.2, 3.0)
        (minCross - pad) to (maxCross + pad)
    } else {
        -10.0 to 10.0
    }
}

/** Convierte una coordenada de pantalla (Offset) de vuelta a coordenada x del dominio graficado. */
internal class InverseMapper(
    private val width: Float,
    private val xMin: Double,
    private val xMax: Double,
    private val margin: Float = 28f
) {
    fun toX(screenX: Float): Double {
        val plotW = width - 2 * margin
        val nx = ((screenX - margin) / plotW).coerceIn(0f, 1f)
        return xMin + nx * (xMax - xMin)
    }
}
