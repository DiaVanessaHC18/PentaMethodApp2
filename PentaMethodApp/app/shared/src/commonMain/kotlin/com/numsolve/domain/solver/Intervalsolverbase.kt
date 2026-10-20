package com.numsolve.domain.solver

import com.numsolve.domain.solver.Solver
import com.numsolve.domain.solver.IntervalParams
import com.numsolve.domain.model.IterationRow.IntervalIterationRow
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.StopReason
import com.numsolve.domain.model.ValidationIssue
import com.numsolve.domain.model.ValidationReport
import com.numsolve.domain.model.NumericMethod
import kotlin.math.abs

/**
 * Base común para Bisección y Regula Falsi: ambos parten de un intervalo [a, b]
 * donde f cambia de signo y generan una tabla con las mismas columnas
 * (i, a, b, xr, f(a), f(xr), f(b), Ea%); solo difiere la fórmula para calcular xr
 * y el criterio de cuál sub-intervalo se conserva (ambos igual: por cambio de signo).
 */
abstract class IntervalSolverBase(private val method: NumericMethod) : Solver<IntervalParams> {

    /** Fórmula específica del método para estimar la raíz dentro de [a, b]. */
    protected abstract fun estimateRoot(a: Double, b: Double, fa: Double, fb: Double): Double

    override fun validate(params: IntervalParams): ValidationReport {
        val issues = mutableListOf<ValidationIssue>()

        if (params.a >= params.b) {
            issues += ValidationIssue.Blocking("El límite inferior 'a' debe ser menor que el límite superior 'b'.")
        }
        if (params.tolerancePercent <= 0.0) {
            issues += ValidationIssue.Blocking("La tolerancia debe ser un valor positivo.")
        }
        if (params.maxIterations <= 0) {
            issues += ValidationIssue.Blocking("El número máximo de iteraciones debe ser mayor que 0.")
        }

        if (issues.none { it is ValidationIssue.Blocking }) {
            val fa = safeEval(params, params.a)
            val fb = safeEval(params, params.b)
            if (fa == null || fb == null) {
                issues += ValidationIssue.Blocking("f(x) no pudo evaluarse en los extremos del intervalo (dominio inválido).")
            } else {
                if (fa == 0.0) issues += ValidationIssue.Warning("a = ${params.a} ya es raíz exacta: f(a) = 0.")
                if (fb == 0.0) issues += ValidationIssue.Warning("b = ${params.b} ya es raíz exacta: f(b) = 0.")
                if (fa != 0.0 && fb != 0.0 && fa * fb > 0.0) {
                    issues += ValidationIssue.Blocking(
                        "No hay cambio de signo en [a, b]: f(a) = ${format(fa)} y f(b) = ${format(fb)} " +
                                "tienen el mismo signo. ${method.displayName} requiere f(a)·f(b) < 0. " +
                                "Prueba con otro intervalo."
                    )
                }
            }
        }
        return ValidationReport(issues)
    }

    override fun solve(params: IntervalParams): SolveResult {
        val validation = validate(params)
        if (!validation.canProceed) {
            return SolveResult(
                method = method,
                rows = emptyList(),
                root = null,
                finalError = null,
                iterationsUsed = 0,
                stopReason = StopReason.NUMERIC_FAILURE,
                failureMessage = validation.blockingIssues.joinToString(" ") { it.message }
            )
        }

        val rows = mutableListOf<IntervalIterationRow>()
        var a = params.a
        var b = params.b
        var fa = params.f.evaluate(a)
        var fb = params.f.evaluate(b)
        var previousXr: Double? = null
        var stopReason = StopReason.MAX_ITERATIONS_REACHED
        var lastError: Double? = null
        var root: Double? = null

        for (i in 0 until params.maxIterations) {
            val xr = estimateRoot(a, b, fa, fb)
            val fxr = params.f.evaluate(xr)

            val errorPercent = previousXr?.let { prev ->
                if (xr == 0.0) null else abs((xr - prev) / xr) * 100.0
            }

            rows += IntervalIterationRow(
                i = i, ai = a, bi = b, mi = xr, fai = fa, fmi = fxr, fbi = fb, errorPercent = errorPercent
            )
            lastError = errorPercent
            root = xr

            if (fxr == 0.0) {
                stopReason = StopReason.EXACT_ROOT_FOUND
                break
            }
            if (errorPercent != null && errorPercent <= params.tolerancePercent) {
                stopReason = StopReason.TOLERANCE_MET
                break
            }

            // Conserva el subintervalo donde persiste el cambio de signo.
            if (fa * fxr < 0.0) {
                b = xr
                fb = fxr
            } else {
                a = xr
                fa = fxr
            }
            previousXr = xr
        }

        return SolveResult(
            method = method,
            rows = rows,
            root = root,
            finalError = lastError,
            iterationsUsed = rows.size,
            stopReason = stopReason
        )
    }

    private fun safeEval(params: IntervalParams, x: Double): Double? = try {
        params.f.evaluate(x)
    } catch (e: Exception) {
        null
    }

    private fun format(value: Double): String {
        val rounded = kotlin.math.round(value * 1_000_000) / 1_000_000
        return rounded.toString()
    }
}