package com.numsolve

import com.numsolve.app.domain.solver.SolverFacade
import com.numsolve.domain.math.ExpressionParser
import com.numsolve.domain.math.MathExpression
import com.numsolve.domain.math.ParseResult
import com.numsolve.domain.model.IterationRow.PointIterationRow
import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.solver.*
import com.numsolve.ui.components.formatFixed
import kotlin.test.*

class SolverResidualTest {
    private val f = (ExpressionParser.parse("x^3 - x - 1") as ParseResult.Success).expression
    private val facade = SolverFacade()

    @Test fun fixedPointUsesOriginalFunctionForEveryRow() {
        val g = MathExpression { x -> x - f.evaluate(x) / 3.32 }
        val result = facade.solveFixedPoint(FixedPointParams(g, f, 1.2, 0.0001, 50))
        assertTrue(result.converged)
        val rows = result.rows.map { it as PointIterationRow }
        assertEquals(-0.472, assertNotNull(rows.first().fxi), 1e-12)
        assertNull(rows.first().errorPercent)
        rows.forEach { assertEquals(f.evaluate(it.xi), assertNotNull(it.fxi), 1e-12) }
        rows.zipWithNext().forEach { (a, b) -> assertEquals(g.evaluate(a.xi), b.xi, 1e-12) }
    }

    @Test fun secantStoresResidualIncludingInitialPoints() {
        val result = facade.solveSecant(SecantParams(f, 1.0, 1.5, 0.0001, 50))
        assertTrue(result.converged)
        val rows = result.rows.map { it as PointIterationRow }
        assertEquals(-1.0, rows[0].fxi)
        assertEquals(0.875, rows[1].fxi)
        rows.forEach { assertEquals(f.evaluate(it.xi), assertNotNull(it.fxi), 1e-12) }
        rows.windowed(3).forEach { (a, b, c) ->
            assertEquals(b.xi - f.evaluate(b.xi) * (b.xi - a.xi) /
                (f.evaluate(b.xi) - f.evaluate(a.xi)), c.xi, 1e-12)
        }
        assertTrue(kotlin.math.abs(assertNotNull(rows.last().fxi)) < 1e-6)
    }

    @Test fun residualOutsideDomainDoesNotReplaceGOrCrash() {
        val original = MathExpression { x -> if (x > 1.3) error("domain") else x - 1 }
        val result = facade.solveFixedPoint(FixedPointParams(MathExpression { 1.5 }, original, 1.2, 0.001, 3))
        assertEquals(1.5, result.root)
        assertNull((result.rows.last() as PointIterationRow).fxi)
    }

    @Test fun otherSolversStillConverge() {
        for (method in listOf(NumericMethod.BISECTION, NumericMethod.REGULA_FALSI)) {
            val result = facade.solveInterval(method, IntervalParams(f, 1.0, 2.0, 0.0001, 100))
            assertTrue(result.converged)
            assertEquals(1.324717957, assertNotNull(result.root), 1e-5)
        }
        val newton = facade.solveNewton(NewtonParams(f, null, 1.2, 0.0001, 50))
        assertTrue(newton.converged)
        newton.rows.map { it as PointIterationRow }.forEach {
            assertEquals(f.evaluate(it.xi), assertNotNull(it.fxi), 1e-12)
        }
    }

    @Test fun residualFormattingKeepsSixDecimalsAndAvoidsNegativeZero() {
        assertEquals("-0.472000", formatFixed(-0.472))
        assertEquals("0.075636", formatFixed(0.075636))
        assertEquals("0.000000", formatFixed(-1e-9))
    }
}
