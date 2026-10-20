package com.numsolve.domain.solver

import com.numsolve.domain.model.NumericMethod

/**
 * xr = b - f(b)·(a - b) / (f(a) - f(b))
 * Traza la secante entre (a, f(a)) y (b, f(b)) y toma su corte con el eje x
 * como siguiente aproximación; suele converger más rápido que Bisección.
 */
class RegulaFalsiSolver : IntervalSolverBase(NumericMethod.REGULA_FALSI) {
    override fun estimateRoot(a: Double, b: Double, fa: Double, fb: Double): Double =
        b - fb * (a - b) / (fa - fb)
}