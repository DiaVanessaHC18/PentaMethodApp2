package com.numsolve.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.numsolve.domain.math.MathExpression
import com.numsolve.domain.math.NumericDerivative
import com.numsolve.domain.model.NumericMethod
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.NumericFontFamily
import com.numsolve.ui.theme.Spacing
import kotlin.math.abs

/**
 * Sección visual obligatoria de cada método: fórmula principal + gráfica
 * conceptual. La curva se traza con la f(x)/g(x) real ingresada por el
 * usuario (si es válida); si aún no hay una expresión válida, se muestra
 * solo la tarjeta de fórmula con una nota.
 */
@Composable
fun MethodVisual(
    method: NumericMethod,
    functionExpression: MathExpression?,
    a: Double?,
    b: Double?,
    x0: Double?,
    x1: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
            .padding(Spacing.lg)
    ) {
        Text("Fórmula principal", style = MaterialTheme.typography.labelLarge, color = AppColors.onSurfaceMuted)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = method.formulaLatexLike,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = NumericFontFamily),
            color = AppColors.primary
        )

        Spacer(Modifier.height(Spacing.md))
        HorizontalDivider(color = AppColors.border)
        Spacer(Modifier.height(Spacing.md))

        Text("Vista conceptual", style = MaterialTheme.typography.labelLarge, color = AppColors.onSurfaceMuted)
        Spacer(Modifier.height(Spacing.sm))

        val domain = resolveDomain(method, a, b, x0, x1)
        if (functionExpression == null || domain == null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(AppColors.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ingresa ${if (method == NumericMethod.FIXED_POINT) "g(x)" else "f(x)"} y los parámetros " +
                            "para previsualizar la curva.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.onSurfaceMuted,
                    modifier = Modifier.padding(Spacing.md)
                )
            }
        } else {
            val (domMin, domMax) = domain
            val points = sampleFunction(functionExpression, domMin, domMax)
            FunctionCurveCanvas(
                points = points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(AppColors.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) { mapper ->
                drawMethodOverlay(method, functionExpression, mapper, a, b, x0, x1)
            }
        }

        Spacer(Modifier.height(Spacing.sm))

    }
}

private fun resolveDomain(method: NumericMethod, a: Double?, b: Double?, x0: Double?, x1: Double?): Pair<Double, Double>? =
    when (method) {
        NumericMethod.BISECTION, NumericMethod.REGULA_FALSI ->
            if (a != null && b != null && a < b) expandedDomain(a, b) else null
        NumericMethod.FIXED_POINT, NumericMethod.NEWTON_RAPHSON ->
            if (x0 != null) expandedDomain(x0, x0 + 1.0) else null
        NumericMethod.SECANT ->
            if (x0 != null && x1 != null) expandedDomain(x0, x1) else null
    }

private fun DrawScope.drawMethodOverlay(
    method: NumericMethod,
    f: MathExpression,
    mapper: PlotMapper,
    a: Double?,
    b: Double?,
    x0: Double?,
    x1: Double?
) {
    val dashed = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

    fun markPoint(x: Double, color: Color) {
        val fx = try { f.evaluate(x) } catch (e: Exception) { return }
        val p = mapper.toScreen(x, fx)
        drawCircle(color = color, radius = 6f, center = p)
    }

    fun verticalGuide(x: Double, color: Color) {
        val fx = try { f.evaluate(x) } catch (e: Exception) { return }
        val top = mapper.toScreen(x, fx)
        val bottom = mapper.toScreen(x, 0.0)
        drawLine(color = color, start = top, end = bottom, strokeWidth = 2.5f, pathEffect = dashed, cap = StrokeCap.Round)
    }

    when (method) {
        NumericMethod.BISECTION, NumericMethod.REGULA_FALSI -> {
            if (a != null && b != null) {
                markPoint(a, AppColors.secondary)
                markPoint(b, AppColors.secondary)
                verticalGuide(a, AppColors.secondary)
                verticalGuide(b, AppColors.secondary)

                val fa = try { f.evaluate(a) } catch (e: Exception) { null }
                val fb = try { f.evaluate(b) } catch (e: Exception) { null }
                if (fa != null && fb != null) {
                    val pA = mapper.toScreen(a, fa)
                    val pB = mapper.toScreen(b, fb)
                    // Secante entre (a,f(a)) y (b,f(b)); en Bisección solo referencia visual del intervalo.
                    drawLine(color = AppColors.warning, start = pA, end = pB, strokeWidth = 2.5f, cap = StrokeCap.Round)
                }
            }
        }

        NumericMethod.SECANT -> {
            if (x0 != null && x1 != null) {
                markPoint(x0, AppColors.secondary)
                markPoint(x1, AppColors.secondary)
                verticalGuide(x0, AppColors.secondary)
                verticalGuide(x1, AppColors.secondary)
                val f0 = try { f.evaluate(x0) } catch (e: Exception) { null }
                val f1 = try { f.evaluate(x1) } catch (e: Exception) { null }
                if (f0 != null && f1 != null) {
                    val p0 = mapper.toScreen(x0, f0)
                    val p1 = mapper.toScreen(x1, f1)
                    drawLine(color = AppColors.warning, start = p0, end = p1, strokeWidth = 2.5f, cap = StrokeCap.Round)
                }
            }
        }

        NumericMethod.NEWTON_RAPHSON -> {
            if (x0 != null) {
                markPoint(x0, AppColors.secondary)
                verticalGuide(x0, AppColors.secondary)
                val fx0 = try { f.evaluate(x0) } catch (e: Exception) { null }
                val slope = try { NumericDerivative.centralDifference(f, x0) } catch (e: Exception) { null }
                if (fx0 != null && slope != null && abs(slope) > 1e-9) {
                    // Recta tangente y = fx0 + slope*(x - x0), dibujada en un rango amplio alrededor de x0.
                    val span = 1.5
                    val xLeft = x0 - span
                    val xRight = x0 + span
                    val yLeft = fx0 + slope * (xLeft - x0)
                    val yRight = fx0 + slope * (xRight - x0)
                    drawLine(
                        color = AppColors.warning,
                        start = mapper.toScreen(xLeft, yLeft),
                        end = mapper.toScreen(xRight, yRight),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        NumericMethod.FIXED_POINT -> {
            if (x0 != null) {
                markPoint(x0, AppColors.secondary)
                verticalGuide(x0, AppColors.secondary)
                // y = x (referencia para la intersección x = g(x))
                val xMinLine = x0 - 2.0
                val xMaxLine = x0 + 2.0
                drawLine(
                    color = AppColors.warning,
                    start = mapper.toScreen(xMinLine, xMinLine),
                    end = mapper.toScreen(xMaxLine, xMaxLine),
                    strokeWidth = 2.5f,
                    pathEffect = dashed,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}