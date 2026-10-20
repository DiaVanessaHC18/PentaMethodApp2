package com.example.pentametod2.presentacion

import com.example.pentametod2.methods.Muestreo
import com.example.pentametod2.methods.ResultadoLagrange
import com.example.pentametod2.methods.ResultadoNewton
import com.example.pentametod2.model.BloqueTabla
import com.example.pentametod2.model.Celda
import com.example.pentametod2.model.Formula
import com.example.pentametod2.model.Grafica
import com.example.pentametod2.model.ItemResumen
import com.example.pentametod2.model.MetodoId
import com.example.pentametod2.model.Punto
import com.example.pentametod2.model.SalidaMetodo
import com.example.pentametod2.model.Serie
import com.example.pentametod2.model.TipoSerie
import com.example.pentametod2.presentacion.Notacion.dato
import com.example.pentametod2.presentacion.Notacion.entreParentesisSiNegativo
import com.example.pentametod2.presentacion.Notacion.num
import com.example.pentametod2.presentacion.Notacion.resta
import com.example.pentametod2.presentacion.Notacion.sub
import com.example.pentametod2.presentacion.Notacion.sup

object PresentadorInterpolacion {

    const val ADVERTENCIA_EXTRAPOLACION =
        "x está fuera del rango de los nodos (extrapolación): el resultado puede ser poco confiable"

    fun presentarLagrange(r: ResultadoLagrange): SalidaMetodo {
        val n = r.xs.size
        val xt = dato(r.x)
        val sustituida = mutableListOf<String>()
        for (i in 0 until n) {
            val otros = (0 until n).filter { it != i }
            val numerador = otros.joinToString("") { resta(xt, r.xs[it]) }
            val denominador = otros.joinToString("") { resta(dato(r.xs[i]), r.xs[it]) }
            sustituida += "L${sub(i)}($xt) = ${fraccion(numerador, denominador, otros.size)} = ${num(r.L[i])}"
        }
        val simbolos = (0 until n).joinToString(" + ") { "y${sub(it)}·L${sub(it)}" }
        val numeros = (0 until n).joinToString(" + ") { "${dato(r.ys[it])}·${entreParentesisSiNegativo(num(r.L[it]))}" }
        sustituida += "P${sub(r.grado)}($xt) = $simbolos = $numeros = ${num(r.valor)}"

        val formula = Formula(
            titulo = "Polinomio de Lagrange",
            general = listOf("Pₙ(x) = Σ yᵢ · Lᵢ(x)", "Lᵢ(x) = Π (x − xⱼ) / (xᵢ − xⱼ),  j ≠ i"),
            sustituida = sustituida,
        )

        val filas = (0 until n).map { i ->
            listOf(Celda.Entero(i), Celda.Num(r.xs[i]), Celda.Num(r.ys[i]), Celda.Num(r.L[i]), Celda.Num(r.terminos[i]))
        }
        val total = listOf(Celda.Txt("Σ"), Celda.Vacia, Celda.Vacia, Celda.Num(r.L.sum()), Celda.Num(r.valor))
        val tabla = BloqueTabla(
            titulo = "Nodos y polinomios de Lagrange",
            columnas = listOf("i", "xᵢ", "yᵢ", "Lᵢ($xt)", "yᵢ·Lᵢ($xt)"),
            filas = filas + listOf(total),
            filaTotal = true,
        )

        val advertencias = advertenciasInterpolacion(r.extrapolacion)
        return SalidaMetodo(
            metodo = MetodoId.LAGRANGE,
            estado = Notacion.estado(advertencias),
            resumen = resumenInterpolacion(r.grado, n, r.x, r.valor),
            formulas = listOf(formula),
            grafica = graficaInterpolacion(r.xs, r.ys, r.x, r.valor, r.evaluar),
            bloques = listOf(tabla),
            advertencias = advertencias,
            error = null,
        )
    }

    fun presentarNewton(r: ResultadoNewton): SalidaMetodo {
        val n = r.xs.size
        val xt = dato(r.x)
        val factoresX = (0 until n).map { k -> if (k == 0) "" else Notacion.factorNewton("x", r.xs, k) }
        val sustituida = listOf(
            "P${sub(r.grado)}(x) = ${Notacion.polinomio(r.coeficientes, factoresX)}",
            "P${sub(r.grado)}($xt) = ${r.terminos.joinToString(" + ") { entreParentesisSiNegativo(num(it)) }} = ${num(r.valor)}",
        )
        val formula = Formula(
            titulo = "Polinomio de Newton (diferencias divididas)",
            general = listOf(
                "Pₙ(x) = f[x₀] + f[x₀,x₁](x−x₀) + f[x₀,x₁,x₂](x−x₀)(x−x₁) + …",
                "f[xᵢ,…,xᵢ₊ₖ] = ( f[xᵢ₊₁,…,xᵢ₊ₖ] − f[xᵢ,…,xᵢ₊ₖ₋₁] ) / ( xᵢ₊ₖ − xᵢ )",
            ),
            sustituida = sustituida,
        )

        val columnas = listOf("k", "xₖ", "f[xₖ]") + (1 until n).map { "Δ${sup(it)}" }
        val filasDif = (0 until n).map { k ->
            listOf(Celda.Entero(k), Celda.Num(r.xs[k])) + r.tabla[k].map { Notacion.celda(it) }
        }
        val tablaDif = BloqueTabla("Tabla de diferencias divididas", columnas, filasDif)

        val filasTerm = (0 until n).map { k ->
            listOf(
                Celda.Entero(k), Celda.Num(r.coeficientes[k]),
                Celda.Txt(Notacion.factorNewton(xt, r.xs, k)), Celda.Num(producto(r.xs, r.x, k)),
                Celda.Num(r.terminos[k]),
            )
        }
        val totalTerm = listOf(Celda.Txt("Σ"), Celda.Vacia, Celda.Vacia, Celda.Vacia, Celda.Num(r.valor))
        val tablaTerm = BloqueTabla(
            titulo = "Términos de Newton",
            columnas = listOf("k", "f[x₀,…,xₖ]", "Factor", "Producto", "Término"),
            filas = filasTerm + listOf(totalTerm),
            filaTotal = true,
        )

        val advertencias = advertenciasInterpolacion(r.extrapolacion)
        return SalidaMetodo(
            metodo = MetodoId.NEWTON_INTERPOLACION,
            estado = Notacion.estado(advertencias),
            resumen = resumenInterpolacion(r.grado, n, r.x, r.valor),
            formulas = listOf(formula),
            grafica = graficaInterpolacion(r.xs, r.ys, r.x, r.valor, r.evaluar),
            bloques = listOf(tablaDif, tablaTerm),
            advertencias = advertencias,
            error = null,
        )
    }

    // ── auxiliares ──

    private fun fraccion(numerador: String, denominador: String, factores: Int): String =
        if (factores == 1) "$numerador/$denominador" else "$numerador / [$denominador]"

    /** (x − x₀)(x − x₁)…(x − xₖ₋₁) */
    private fun producto(xs: DoubleArray, x: Double, k: Int): Double {
        var p = 1.0
        for (j in 0 until k) p *= x - xs[j]
        return p
    }

    private fun advertenciasInterpolacion(extrapolacion: Boolean): List<String> =
        if (extrapolacion) listOf(ADVERTENCIA_EXTRAPOLACION) else emptyList()

    private fun resumenInterpolacion(grado: Int, nodos: Int, x: Double, valor: Double) = listOf(
        ItemResumen("Grado", grado.toString()),
        ItemResumen("Nodos usados", nodos.toString()),
        ItemResumen("x", dato(x)),
        ItemResumen("P(${dato(x)})", num(valor), destacado = true),
    )

    private fun graficaInterpolacion(
        xs: DoubleArray, ys: DoubleArray, x: Double, valor: Double, evaluar: (Double) -> Double,
    ): Grafica {
        val (desde, hasta) = Muestreo.rangoConMargen(minOf(xs.min(), x), maxOf(xs.max(), x))
        return Grafica(
            titulo = "Polinomio interpolante",
            ejeX = "x",
            ejeY = "y",
            series = listOf(
                Serie("Nodos", TipoSerie.PUNTOS, xs.indices.map { Punto(xs[it], ys[it]) }),
                Serie("P(x)", TipoSerie.LINEA, Muestreo.curva(evaluar, desde, hasta)),
                Serie("x evaluado", TipoSerie.PUNTOS, listOf(Punto(x, valor)), destacada = true),
            ),
        )
    }
}
