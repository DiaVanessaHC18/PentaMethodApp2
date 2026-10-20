package com.numsolve.domain.solver

import com.numsolve.domain.math.MathExpression
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.ValidationReport
import kotlin.math.abs

/** Contrato común que implementa cada uno de los 5 métodos numéricos. */
interface Solver<Params> {
    fun validate(params: Params): ValidationReport
    fun solve(params: Params): SolveResult
}

/** Parámetros comunes a cualquier método iterativo. */
interface IterativeParams {
    val tolerancePercent: Double
    val maxIterations: Int
}

data class IntervalParams(
    val f: MathExpression,
    val a: Double,
    val b: Double,
    override val tolerancePercent: Double,
    override val maxIterations: Int
) : IterativeParams

data class FixedPointParams(
    val g: MathExpression,
    val f: MathExpression, // función original, independiente de g(x)
    val x0: Double,
    override val tolerancePercent: Double,
    override val maxIterations: Int
) : IterativeParams

data class NewtonParams(
    val f: MathExpression,
    val derivative: MathExpression?, // null -> se deriva numéricamente
    val x0: Double,
    override val tolerancePercent: Double,
    override val maxIterations: Int
) : IterativeParams

data class SecantParams(
    val f: MathExpression,
    val x0: Double,
    val x1: Double,
    override val tolerancePercent: Double,
    override val maxIterations: Int
) : IterativeParams

/**
 * Error relativo porcentual estándar usado en todas las tablas de iteración
 * de punto único: Ea% = |x_actual - x_anterior| / |x_actual| * 100
 */
internal fun relativePercentError(current: Double, previous: Double): Double? {
    if (current == 0.0) return null
    return abs((current - previous) / current) * 100.0
}