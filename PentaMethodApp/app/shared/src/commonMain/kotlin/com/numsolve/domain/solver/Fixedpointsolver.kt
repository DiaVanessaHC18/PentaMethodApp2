package com.numsolve.domain.solver

import com.numsolve.domain.math.NumericDerivative
import com.numsolve.domain.solver.FixedPointParams
import com.numsolve.domain.solver.Solver
import com.numsolve.domain.solver.relativePercentError
import com.numsolve.domain.model.IterationRow
import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.StopReason
import com.numsolve.domain.model.ValidationIssue
import com.numsolve.domain.model.ValidationReport
import kotlin.math.abs
import kotlin.math.round

/**
 * x_(i+1) = g(x_i)
 *
 * Antes de iterar, replica la verificación que se hace manualmente en la hoja de
 * cálculo de referencia: "VERIFICAR SI EL ENUNCIADO EQUIVALENTE ES CONVERGENTE",
 * evaluando |g'(x0)| numéricamente. Si |g'(x0)| >= 1 la convergencia no está
 * garantizada; se muestra como advertencia (no bloqueante) para que el usuario
 * decida -por ejemplo, si el ejercicio pide demostrar la NO convergencia- o
 * pruebe con otra reescritura de g(x)/otro punto inicial.
 */
class FixedPointSolver : Solver<FixedPointParams> {

    override fun validate(params: FixedPointParams): ValidationReport {
        val issues = mutableListOf<ValidationIssue>()

        if (params.tolerancePercent <= 0.0) {
            issues += ValidationIssue.Blocking("La tolerancia debe ser un valor positivo.")
        }
        if (params.maxIterations <= 0) {
            issues += ValidationIssue.Blocking("El número máximo de iteraciones debe ser mayor que 0.")
        }

        val g0 = try {
            params.g.evaluate(params.x0)
        } catch (e: Exception) {
            null
        }
        if (g0 == null) {
            issues += ValidationIssue.Blocking("g(x) no pudo evaluarse en x0 = ${params.x0} (dominio inválido).")
        } else {
            val derivative = try {
                NumericDerivative.centralDifference(params.g, params.x0)
            } catch (e: Exception) {
                null
            }
            if (derivative == null) {
                issues += ValidationIssue.Warning("No se pudo estimar g'(x0); no se puede verificar convergencia automáticamente.")
            } else if (abs(derivative) >= 1.0) {
                issues += ValidationIssue.Warning(
                    "|g'(x0)| ≈ ${round6(abs(derivative))} ≥ 1: la convergencia NO está garantizada con esta " +
                            "reescritura de g(x). Puedes continuar, pero considera despejar x de otra forma."
                )
            } else {
                issues += ValidationIssue.Warning(
                    "|g'(x0)| ≈ ${round6(abs(derivative))} < 1: convergencia local garantizada. ✓"
                )
            }
        }
        return ValidationReport(issues)
    }

    override fun solve(params: FixedPointParams): SolveResult {
        val validation = validate(params)
        if (!validation.canProceed) {
            return SolveResult(
                method = NumericMethod.FIXED_POINT,
                rows = emptyList(),
                root = null,
                finalError = null,
                iterationsUsed = 0,
                stopReason = StopReason.NUMERIC_FAILURE,
                failureMessage = validation.blockingIssues.joinToString(" ") { it.message }
            )
        }

        val rows = mutableListOf<IterationRow.PointIterationRow>()
        var xPrev = params.x0
        rows += IterationRow.PointIterationRow(
            i = 0,
            label = "x0",
            xi = xPrev,
            fxi = evaluateOriginal(params, xPrev),
            errorPercent = null
        )

        var stopReason = StopReason.MAX_ITERATIONS_REACHED
        var lastError: Double? = null
        var root = xPrev

        for (i in 1..params.maxIterations) {
            val xNext = try {
                params.g.evaluate(xPrev)
            } catch (e: Exception) {
                return SolveResult(
                    method = NumericMethod.FIXED_POINT,
                    rows = rows,
                    root = null,
                    finalError = lastError,
                    iterationsUsed = rows.size,
                    stopReason = StopReason.NUMERIC_FAILURE,
                    failureMessage = "g(x) no pudo evaluarse en x${i - 1} = ${round6(xPrev)}: ${e.message}"
                )
            }

            val errorPercent = relativePercentError(xNext, xPrev)
            rows += IterationRow.PointIterationRow(
                i = i,
                label = "x$i",
                xi = xNext,
                fxi = evaluateOriginal(params, xNext),
                errorPercent = errorPercent
            )
            lastError = errorPercent
            root = xNext

            if (xNext == xPrev) {
                stopReason = StopReason.EXACT_ROOT_FOUND
                break
            }
            if (errorPercent != null && errorPercent <= params.tolerancePercent) {
                stopReason = StopReason.TOLERANCE_MET
                break
            }
            xPrev = xNext
        }

        return SolveResult(
            method = NumericMethod.FIXED_POINT,
            rows = rows,
            root = root,
            finalError = lastError,
            iterationsUsed = rows.size - 1,
            stopReason = stopReason
        )
    }

    // Un residual fuera de dominio se muestra como ausente sin cambiar la iteración de g.
    private fun evaluateOriginal(params: FixedPointParams, x: Double): Double? =
        runCatching { params.f.evaluate(x) }.getOrNull()?.takeIf { it.isFinite() }

    private fun round6(value: Double): Double = round(value * 1_000_000) / 1_000_000
}