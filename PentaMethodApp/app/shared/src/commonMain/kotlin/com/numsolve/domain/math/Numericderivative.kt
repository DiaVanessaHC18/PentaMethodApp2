package com.numsolve.domain.math

import kotlin.math.abs
import kotlin.math.max

/**
 * Deriva numéricamente una [MathExpression] usando diferencias centrales,
 * con un paso h adaptado a la magnitud de x para mantener estabilidad numérica.
 * Se usa cuando el usuario no provee f'(x) de forma explícita (Newton-Raphson)
 * o para validar la convergencia de g(x) en Punto Fijo (|g'(x0)| < 1).
 */
object NumericDerivative {

    fun centralDifference(f: MathExpression, x: Double, stepHint: Double? = null): Double {
        val h = stepHint ?: (1e-6 * max(1.0, abs(x)))
        val forward = f.evaluate(x + h)
        val backward = f.evaluate(x - h)
        return (forward - backward) / (2 * h)
    }
}