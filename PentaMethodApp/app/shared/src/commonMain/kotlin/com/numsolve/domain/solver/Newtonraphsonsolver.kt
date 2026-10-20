package com.numsolve.domain.solver

import com.numsolve.domain.math.NumericDerivative
import com.numsolve.domain.model.IterationRow.PointIterationRow
import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.StopReason
import com.numsolve.domain.model.ValidationIssue
import com.numsolve.domain.model.ValidationReport
import kotlin.math.abs

/**
 * x_(i+1) = x_i − f(x_i) / f'(x_i)
 *
 * Si el usuario no ingresa f'(x) explícita, se deriva numéricamente por
 * diferencias centrales. Se valida f'(x0) ≠ 0 antes de iterar y se detiene
 * con un mensaje claro si la derivada se anula durante el proceso.
 */
class NewtonRaphsonSolver : Solver<NewtonParams> {

    private fun derivativeAt(params: NewtonParams, x: Double): Double =
        params.derivative?.evaluate(x) ?: NumericDerivative.centralDifference(params.f, x)

    override fun validate(params: NewtonParams): ValidationReport {
        val issues = mutableListOf<ValidationIssue>()

        if (params.tolerancePercent <= 0.0) {
            issues += ValidationIssue.Blocking("La tolerancia debe ser un valor positivo.")
        }
        if (params.maxIterations <= 0) {
            issues += ValidationIssue.Blocking("El número máximo de iteraciones debe ser mayor que 0.")
        }

        val f0 = try {
            params.f.evaluate(params.x0)
        } catch (e: Exception) {
            null
        }
        if (f0 == null) {
            issues += ValidationIssue.Blocking("f(x) no pudo evaluarse en x0 = ${params.x0} (dominio inválido).")
        } else {
            val d0 = try {
                derivativeAt(params, params.x0)
            } catch (e: Exception) {
                null
            }
            if (d0 == null) {
                issues += ValidationIssue.Blocking("f'(x) no pudo evaluarse en x0 = ${params.x0}.")
            } else if (abs(d0) < 1e-10) {
                issues += ValidationIssue.Blocking(
                    "f'(x0) ≈ 0: la tangente en x0 es horizontal y Newton-Raphson no puede avanzar. " +
                            "Elige otro punto inicial x0."
                )
            }
        }
        if (params.derivative == null) {
            issues += ValidationIssue.Warning("No se ingresó f'(x): se calculará por diferenciación numérica.")
        }
        return ValidationReport(issues)
    }

    override fun solve(params: NewtonParams): SolveResult {
        val validation = validate(params)
        if (!validation.canProceed) {
            return SolveResult(
                method = NumericMethod.NEWTON_RAPHSON,
                rows = emptyList(),
                root = null,
                finalError = null,
                iterationsUsed = 0,
                stopReason = StopReason.NUMERIC_FAILURE,
                failureMessage = validation.blockingIssues.joinToString(" ") { it.message }
            )
        }

        val rows = mutableListOf<PointIterationRow>()
        var xPrev = params.x0
        var fPrev = params.f.evaluate(xPrev)
        rows += PointIterationRow(i = 0, label = "x0", xi = xPrev, fxi = fPrev, errorPercent = null)

        var stopReason = StopReason.MAX_ITERATIONS_REACHED
        var lastError: Double? = null
        var root = xPrev

        for (i in 1..params.maxIterations) {
            val derivative = try {
                derivativeAt(params, xPrev)
            } catch (e: Exception) {
                null
            }
            if (derivative == null || abs(derivative) < 1e-10) {
                return SolveResult(
                    method = NumericMethod.NEWTON_RAPHSON,
                    rows = rows,
                    root = root,
                    finalError = lastError,
                    iterationsUsed = rows.size,
                    stopReason = StopReason.NUMERIC_FAILURE,
                    failureMessage = "f'(x$( i - 1)) ≈ 0 en la iteración $i: la tangente se volvió horizontal. " +
                            "El método no puede continuar; intenta con otro x0."
                )
            }

            val xNext = xPrev - fPrev / derivative
            val fNext = try {
                params.f.evaluate(xNext)
            } catch (e: Exception) {
                return SolveResult(
                    method = NumericMethod.NEWTON_RAPHSON,
                    rows = rows,
                    root = root,
                    finalError = lastError,
                    iterationsUsed = rows.size,
                    stopReason = StopReason.NUMERIC_FAILURE,
                    failureMessage = "f(x) no pudo evaluarse en x$i = ${xNext} (posible salida de dominio)."
                )
            }
            val errorPercent = relativePercentError(xNext, xPrev)

            rows += PointIterationRow(i = i, label = "x$i", xi = xNext, fxi = fNext, errorPercent = errorPercent)
            lastError = errorPercent
            root = xNext

            if (fNext == 0.0) {
                stopReason = StopReason.EXACT_ROOT_FOUND
                break
            }
            if (errorPercent != null && errorPercent <= params.tolerancePercent) {
                stopReason = StopReason.TOLERANCE_MET
                break
            }

            xPrev = xNext
            fPrev = fNext
        }

        return SolveResult(
            method = NumericMethod.NEWTON_RAPHSON,
            rows = rows,
            root = root,
            finalError = lastError,
            iterationsUsed = rows.size - 1,
            stopReason = stopReason
        )
    }
}