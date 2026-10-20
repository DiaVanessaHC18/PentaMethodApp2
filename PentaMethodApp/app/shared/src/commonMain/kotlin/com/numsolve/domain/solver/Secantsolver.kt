package com.numsolve.domain.solver

import com.numsolve.domain.model.IterationRow
import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.StopReason
import com.numsolve.domain.model.ValidationIssue
import com.numsolve.domain.model.ValidationReport

/**
 * x_(i+1) = x_i − f(x_i)·(x_i − x_(i-1)) / (f(x_i) − f(x_(i-1)))
 *
 * Requiere dos aproximaciones iniciales x0 ≠ x1 provistas manualmente por el
 * usuario (no se calculan automáticamente), tal como exige el enunciado del
 * caso de estudio: "se necesitan dos puntos existentes x0 y x1".
 */
class SecantSolver : Solver<SecantParams> {

    override fun validate(params: SecantParams): ValidationReport {
        val issues = mutableListOf<ValidationIssue>()

        if (params.x0 == params.x1) {
            issues += ValidationIssue.Blocking("x0 y x1 deben ser distintos para trazar la secante inicial.")
        }
        if (params.tolerancePercent <= 0.0) {
            issues += ValidationIssue.Blocking("La tolerancia debe ser un valor positivo.")
        }
        if (params.maxIterations <= 0) {
            issues += ValidationIssue.Blocking("El número máximo de iteraciones debe ser mayor que 0.")
        }

        if (issues.none { it is ValidationIssue.Blocking }) {
            val f0 = safeEval(params, params.x0)
            val f1 = safeEval(params, params.x1)
            if (f0 == null || f1 == null) {
                issues += ValidationIssue.Blocking("f(x) no pudo evaluarse en x0 o x1 (dominio inválido).")
            } else if (f0 == f1) {
                issues += ValidationIssue.Blocking("f(x0) = f(x1): la secante inicial es horizontal y no corta el eje x.")
            }
        }
        return ValidationReport(issues)
    }

    override fun solve(params: SecantParams): SolveResult {
        val validation = validate(params)
        if (!validation.canProceed) {
            return SolveResult(
                method = NumericMethod.SECANT,
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
        var xCurr = params.x1
        var fPrev = params.f.evaluate(xPrev)
        var fCurr = params.f.evaluate(xCurr)
        rows += IterationRow.PointIterationRow(
            i = 0,
            label = "x0",
            xi = xPrev,
            fxi = fPrev,
            errorPercent = null
        )
        rows += IterationRow.PointIterationRow(
            i = 1, label = "x1", xi = xCurr, fxi = fCurr,
            errorPercent = relativePercentError(xCurr, xPrev)
        )

        var stopReason = StopReason.MAX_ITERATIONS_REACHED
        var lastError = rows.last().errorPercent
        var root = xCurr


        if (lastError != null && lastError <= params.tolerancePercent) {
            stopReason = StopReason.TOLERANCE_MET
        } else {
            for (i in 2..params.maxIterations) {
                if (fCurr == fPrev) {
                    return SolveResult(
                        method = NumericMethod.SECANT,
                        rows = rows,
                        root = root,
                        finalError = lastError,
                        iterationsUsed = rows.size,
                        stopReason = StopReason.NUMERIC_FAILURE,
                        failureMessage = "f(x_i) = f(x_(i-1)) en la iteración $i: la secante se volvió horizontal " +
                                "(división por cero). Prueba con otros puntos iniciales."
                    )
                }
                val xNext = xCurr - fCurr * (xCurr - xPrev) / (fCurr - fPrev)
                val fNext = params.f.evaluate(xNext)
                val errorPercent = relativePercentError(xNext, xCurr)

                rows += IterationRow.PointIterationRow(
                    i = i,
                    label = "x$i",
                    xi = xNext,
                    fxi = fNext,
                    errorPercent = errorPercent
                )
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

                xPrev = xCurr; fPrev = fCurr
                xCurr = xNext; fCurr = fNext
            }
        }

        return SolveResult(
            method = NumericMethod.SECANT,
            rows = rows,
            root = root,
            finalError = lastError,
            iterationsUsed = rows.size - 1,
            stopReason = stopReason
        )
    }

    private fun safeEval(params: SecantParams, x: Double): Double? = try {
        params.f.evaluate(x)
    } catch (e: Exception) {
        null
    }
}