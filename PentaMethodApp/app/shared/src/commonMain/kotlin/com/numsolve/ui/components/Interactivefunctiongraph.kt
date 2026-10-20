package com.numsolve.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numsolve.domain.math.MathExpression
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.NumericFontFamily
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow


/** Un campo del formulario que la gráfica puede rellenar al tocar un punto. */
data class GraphTarget(
    val label: String,
    val currentValue: Double?,
    val onPick: (Double) -> Unit,
    val color: Color
)

/**
 * Gráfica de f(x) que se dibuja apenas la expresión es válida (sin esperar a
 * que existan a/b/x0/x1: si faltan, se elige un dominio automáticamente
 * buscando cambios de signo). Al tocar la gráfica, el punto más cercano de la
 * curva se asigna al primer [GraphTarget] vacío (o, si ya están todos
 * llenos, se reasigna el primero), para que el
 * usuario pueda "leer" la función y elegir sus valores iniciales visualmente.
 */
@Composable
fun InteractiveFunctionGraph(
    f: MathExpression?,
    targets: List<GraphTarget>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(AppColors.surface, RoundedCornerShape(14.dp))
            .border(1.dp, AppColors.border, RoundedCornerShape(14.dp))
    ) {
        if (f == null) {
            Text(
                text = "Escribe f(x) para ver la gráfica y elegir tus puntos aquí mismo.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.onSurfaceMuted,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
            return@Box
        }

        val explicitValues = targets.mapNotNull { it.currentValue }
        val domain = remember(f, explicitValues) {
            if (explicitValues.size >= 2) {
                expandedDomain(*explicitValues.toDoubleArray())
            } else if (explicitValues.size == 1) {
                expandedDomain(explicitValues[0], explicitValues[0] + 1.0)
            } else {
                autoDomainForFunction(f)
            }
        }
        val initialPoints = remember(f, domain) { sampleFunction(f, domain.first, domain.second) }

        if (initialPoints.isEmpty()) {
            Text(
                text = "f(x) no se pudo evaluar en este rango. Revisa el dominio (raíces, logaritmos, etc.).",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.error,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
            return@Box
        }

        var autoYMin = initialPoints.minOf { it.y }
        var autoYMax = initialPoints.maxOf { it.y }
        if (autoYMin == autoYMax) { autoYMin -= 1.0; autoYMax += 1.0 }
        val yPad = (autoYMax - autoYMin) * 0.12
        autoYMin -= yPad; autoYMax += yPad
        val original = GraphViewport(domain.first, domain.second, autoYMin, autoYMax)
        var viewport by remember(f, original) { mutableStateOf(original) }
        var panEnabled by remember(f) { mutableStateOf(false) }
        var canvasSize by remember { mutableStateOf(IntSize.Zero) }
        val xMin = viewport.xMin
        val xMax = viewport.xMax
        val yMin = viewport.yMin
        val yMax = viewport.yMax
        val points = remember(f, xMin, xMax) { sampleFunction(f, xMin, xMax, 400) }
        val transform by rememberUpdatedState<(Offset, Offset, Float) -> Unit>({ center, pan, zoom ->
            val width = (canvasSize.width - 56).coerceAtLeast(1).toDouble()
            val height = (canvasSize.height - 56).coerceAtLeast(1).toDouble()
            val candidate = viewport.transform(original,
                (center.x - 28) / width, 1 - (center.y - 28) / height,
                pan.x / width, pan.y / height, zoom.toDouble())
            // No aceptar un zoom que deje toda la curva fuera de la vista.
            val samples = sampleFunction(f, candidate.xMin, candidate.xMax, 400)
            if (zoom == 1f || (samples.isNotEmpty() &&
                    samples.minOf { it.y } <= candidate.yMax && samples.maxOf { it.y } >= candidate.yMin)) {
                viewport = candidate
            }
        })

        val xStep = niceAxisStep(xMax - xMin)
        val yStep = niceAxisStep(yMax - yMin)
        val textMeasurer = rememberTextMeasurer()
        val axisLabelStyle = TextStyle(
            fontFamily = NumericFontFamily,
            fontSize = 9.sp,
            color = AppColors.onSurfaceMuted
        )

        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = { panEnabled = !panEnabled }) {
                    Text(if (panEnabled) "Mover: activo" else "Mover")
                }
                TextButton(onClick = { viewport = original; panEnabled = false }) { Text("Restablecer") }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .onSizeChanged { canvasSize = it }
                    .graphGestures(panEnabled) { center, pan, zoom -> transform(center, pan, zoom) }
                    .pointerInput(f, viewport, targets) {
                        detectTapGestures { offset ->
                            val inverse = InverseMapper(size.width.toFloat(), xMin, xMax)
                            val tappedX = inverse.toX(offset.x)
                            val nearest = points.minByOrNull { kotlin.math.abs(it.x - tappedX) } ?: return@detectTapGestures
                            val slot = targets.firstOrNull { it.currentValue == null } ?: targets.firstOrNull()
                            slot?.onPick?.invoke(nearest.x)
                        }
                    }
            ) {
                val mapper = PlotMapper(size.width, size.height, xMin, xMax, yMin, yMax, clampY = false)
                val gridColor = AppColors.border.copy(alpha = 0.6f)

                // Cuadrícula vertical con números "redondos" de x, como referencia de lectura.
                var gx = ceil(xMin / xStep) * xStep
                while (gx <= xMax) {
                    val sx = mapper.toScreen(gx, yMin).x
                    drawLine(gridColor, Offset(sx, 0f), Offset(sx, size.height), strokeWidth = 1f)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatTick(gx, xStep),
                        topLeft = Offset(sx + 2f, size.height - textMeasurer.measure("0", axisLabelStyle).size.height - 2f),
                        style = axisLabelStyle
                    )
                    gx += xStep
                }

                // Cuadrícula horizontal con números "redondos" de y.
                var gy = ceil(yMin / yStep) * yStep
                while (gy <= yMax) {
                    val sy = mapper.toScreen(xMin, gy).y
                    drawLine(gridColor, Offset(0f, sy), Offset(size.width, sy), strokeWidth = 1f)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatTick(gy, yStep),
                        topLeft = Offset(4f, sy - 12f),
                        style = axisLabelStyle
                    )
                    gy += yStep
                }

                // Ejes principales (x = 0, y = 0), más marcados que la cuadrícula.
                if (0.0 in yMin..yMax) {
                    val yZero = mapper.toScreen(xMin, 0.0).y
                    drawLine(AppColors.secondary, Offset(0f, yZero), Offset(size.width, yZero), strokeWidth = 1.8f)
                }
                if (0.0 in xMin..xMax) {
                    val xZero = mapper.toScreen(0.0, yMin).x
                    drawLine(AppColors.secondary, Offset(xZero, 0f), Offset(xZero, size.height), strokeWidth = 1.8f)
                }

                var previous: Offset? = null
                for (p in points) {
                    val screen = mapper.toScreen(p.x, p.y)
                    previous?.let { prev ->
                        drawLine(AppColors.primary, prev, screen, strokeWidth = 4f, cap = StrokeCap.Round)
                    }
                    previous = screen
                }

                // Marcadores de los puntos ya asignados (a, b, x0, x1...)
                targets.forEach { target ->
                    val x = target.currentValue ?: return@forEach
                    val y = try { f.evaluate(x) } catch (e: Exception) { return@forEach }
                    if (!y.isFinite() || x !in xMin..xMax || y !in yMin..yMax) return@forEach
                    val p = mapper.toScreen(x, y)
                    drawCircle(color = target.color, radius = 7f, center = p)
                    drawCircle(color = Color.White, radius = 3f, center = p)
                }
            }

            Text(
                text = "Dos dedos: zoom · Mover: arrastrar · Toca: " + targets.joinToString(" → ") { it.label },
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.onSurfaceMuted,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Elige un paso de cuadrícula "redondo" (1, 2, 5, 10, 20, 50...) para el
 * rango dado, apuntando a mostrar entre 5 y 10 líneas guía — igual que hacen
 * las hojas de cálculo y librerías de gráficos al elegir sus ejes.
 */
private fun niceAxisStep(range: Double): Double {
    if (range <= 0.0 || range.isNaN() || range.isInfinite()) return 1.0
    val roughStep = range / 7.0
    val magnitude = 10.0.pow(floor(log10(roughStep)))
    val residual = roughStep / magnitude
    val niceResidual = when {
        residual < 1.5 -> 1.0
        residual < 3.5 -> 2.0
        residual < 7.5 -> 5.0
        else -> 10.0
    }
    return niceResidual * magnitude
}

/** Formatea el número de una marca de cuadrícula: entero si el paso es entero, con decimales si no. */
private fun formatTick(value: Double, step: Double): String {
    val decimals = when {
        step >= 1.0 -> 0
        step >= 0.1 -> 1
        else -> 2
    }
    return formatFixed(value, decimals)
}
