package com.numsolve.app.domain.solver

import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.solver.BisectionSolver
import com.numsolve.domain.solver.FixedPointParams
import com.numsolve.domain.solver.FixedPointSolver
import com.numsolve.domain.solver.IntervalParams
import com.numsolve.domain.solver.NewtonParams
import com.numsolve.domain.solver.NewtonRaphsonSolver
import com.numsolve.domain.solver.RegulaFalsiSolver
import com.numsolve.domain.solver.SecantParams
import com.numsolve.domain.solver.SecantSolver

/**
 * Punto único de acceso a los 5 solvers. Mantiene el módulo de dominio
 * desacoplado de la capa de presentación: la UI solo necesita conocer
 * [NumericMethod] y los parámetros tipados correspondientes.
 */
class SolverFacade(
    private val bisection: BisectionSolver = BisectionSolver(),
    private val regulaFalsi: RegulaFalsiSolver = RegulaFalsiSolver(),
    private val fixedPoint: FixedPointSolver = FixedPointSolver(),
    private val secant: SecantSolver = SecantSolver(),
    private val newtonRaphson: NewtonRaphsonSolver = NewtonRaphsonSolver()
) {
    fun solveInterval(method: NumericMethod, params: IntervalParams) =
        when (method) {
            NumericMethod.BISECTION -> bisection.solve(params)
            NumericMethod.REGULA_FALSI -> regulaFalsi.solve(params)
            else -> error("$method no usa parámetros de intervalo.")
        }

    fun validateInterval(method: NumericMethod, params: IntervalParams) =
        when (method) {
            NumericMethod.BISECTION -> bisection.validate(params)
            NumericMethod.REGULA_FALSI -> regulaFalsi.validate(params)
            else -> error("$method no usa parámetros de intervalo.")
        }

    fun solveFixedPoint(params: FixedPointParams) = fixedPoint.solve(params)
    fun validateFixedPoint(params: FixedPointParams) = fixedPoint.validate(params)

    fun solveSecant(params: SecantParams) = secant.solve(params)
    fun validateSecant(params: SecantParams) = secant.validate(params)

    fun solveNewton(params: NewtonParams) = newtonRaphson.solve(params)
    fun validateNewton(params: NewtonParams) = newtonRaphson.validate(params)
}