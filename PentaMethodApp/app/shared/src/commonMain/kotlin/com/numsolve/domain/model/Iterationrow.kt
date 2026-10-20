package com.numsolve.domain.model

/**
 * Filas de la tabla de iteraciones. Existen dos formas, replicando 1:1 las columnas
 * usadas en las hojas de cálculo de referencia:
 *
 *  - IntervalIterationRow  -> Bisección / Regula Falsi
 *        columnas: i | a_i | b_i | x_r (c_i) | f(a_i) | f(x_r) | f(b_i) | Ea (%)
 *
 *  - PointIterationRow     -> Punto Fijo / Secante / Newton-Raphson
 *        columnas: i | x_i | f(x_i) | Ea (%)
 */
sealed class IterationRow {

    data class IntervalIterationRow(
        val i: Int,
        val ai: Double,
        val bi: Double,
        val mi: Double,
        val fai: Double,
        val fmi: Double,
        val fbi: Double,
        val errorPercent: Double?
    ) : IterationRow()

    data class PointIterationRow(
        val i: Int,
        val label: String,     // "x0", "x1", "x2"... tal como en las plantillas de Excel
        val xi: Double,
        val fxi: Double?,       // función original evaluada en xi; nunca g(xi)
        val errorPercent: Double?
    ) : IterationRow()
}