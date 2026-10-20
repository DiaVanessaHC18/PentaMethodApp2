package com.numsolve.domain.solver
import com.numsolve.domain.solver.IntervalSolverBase
import com.numsolve.domain.model.NumericMethod

/**
 * xr = (a + b) / 2
 * El método más simple y robusto: siempre converge si f(a)·f(b) < 0,
 * aunque de forma lineal (más lenta que Newton o Secante).
 */
class BisectionSolver : IntervalSolverBase(NumericMethod.BISECTION) {
    override fun estimateRoot(a: Double, b: Double, fa: Double, fb: Double): Double = (a + b) / 2.0
}