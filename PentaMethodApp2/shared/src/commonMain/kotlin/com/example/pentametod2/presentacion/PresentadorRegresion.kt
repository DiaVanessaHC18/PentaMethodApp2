package com.example.pentametod2.presentacion

import com.example.pentametod2.methods.Muestreo
import com.example.pentametod2.methods.ResultadoRegresion
import com.example.pentametod2.model.Bloque
import com.example.pentametod2.model.BloqueMatriz
import com.example.pentametod2.model.BloqueTabla
import com.example.pentametod2.model.BloqueTexto
import com.example.pentametod2.model.Celda
import com.example.pentametod2.model.Formato
import com.example.pentametod2.model.Formula
import com.example.pentametod2.model.Grafica
import com.example.pentametod2.model.ItemResumen
import com.example.pentametod2.model.MetodoId
import com.example.pentametod2.model.Punto
import com.example.pentametod2.model.SalidaMetodo
import com.example.pentametod2.model.Serie
import com.example.pentametod2.model.TipoSerie
import com.example.pentametod2.model.TipoTexto
import com.example.pentametod2.presentacion.Notacion.dato
import com.example.pentametod2.presentacion.Notacion.num
import com.example.pentametod2.presentacion.Notacion.potencia
import com.example.pentametod2.presentacion.Notacion.sub
import kotlin.math.pow

object PresentadorRegresion {

    fun presentarRegresionPolinomial(
        r: ResultadoRegresion,
        xs: DoubleArray,
        ys: DoubleArray,
        xPredecir: Double? = null,
    ): SalidaMetodo {
        val m = r.grado ?: throw IllegalArgumentException("El resultado no es de una regresión polinomial")
        val n = xs.size
        val ecuacion = "P${sub(m)}(x) = " + Notacion.polinomio(r.coeficientes, (0..m).map { k -> factorPotencia("x", k) })

        val formula = Formula(
            titulo = "Regresión polinomial por mínimos cuadrados",
            general = listOf(
                "Pₘ(x) = a₀ + a₁x + … + aₘxᵐ",
                "Sistema normal: C·a = b,  Cᵢⱼ = Σ xₖ^(i+j),  bᵢ = Σ xₖⁱ·yₖ",
                "a = C⁻¹·b",
                "Q = Σ (Pₘ(xₖ) − yₖ)²   ·   R² = (S_yy − Q) / S_yy   ·   S = √(Q / (n − m − 1))",
            ),
            sustituida = listOf(ecuacion) + lineasAjuste(r, "n − m − 1 = ${n - m - 1}"),
        )

        // Tabla de datos: i, xᵢ, yᵢ, xᵢ²…xᵢ^(2m), xᵢyᵢ…xᵢ^m·yᵢ, P(xᵢ), (P(xᵢ)−yᵢ)², (yᵢ−ȳ)²
        val columnas = mutableListOf("i", "xᵢ", "yᵢ")
        for (k in 2..2 * m) columnas += "xᵢ${Notacion.sup(k)}"
        for (k in 1..m) columnas += "xᵢ${potencia(k)}yᵢ"
        columnas += listOf("P(xᵢ)", "(P(xᵢ)−yᵢ)²", "(yᵢ−ȳ)²")
        val valores = (0 until n).map { i ->
            val fila = mutableListOf(xs[i], ys[i])
            for (k in 2..2 * m) fila += xs[i].pow(k)
            for (k in 1..m) fila += xs[i].pow(k) * ys[i]
            fila += finDeFila(r, ys[i], i)
            fila.toDoubleArray()
        }
        val tabla = BloqueTabla(
            titulo = "Datos y sumatorias",
            columnas = columnas,
            filas = Notacion.conFilaDeSumas((1..n).map { Celda.Entero(it) }, valores),
            filaTotal = true,
        )

        val resumen = mutableListOf(ItemResumen("Ecuación", ecuacion))
        resumen += resumenAjuste(r)
        resumen += ItemResumen("Grado", m.toString())

        val curvaDesde = minOf(xs.min(), xPredecir ?: xs.min())
        val curvaHasta = maxOf(xs.max(), xPredecir ?: xs.max())
        val (desde, hasta) = Muestreo.rangoConMargen(curvaDesde, curvaHasta)
        val series = mutableListOf(
            Serie("Datos", TipoSerie.PUNTOS, xs.indices.map { Punto(xs[it], ys[it]) }),
            Serie("P(x)", TipoSerie.LINEA, Muestreo.curva(r::predecir, desde, hasta)),
        )
        if (xPredecir != null) {
            val p = r.predecir(xPredecir)
            resumen += ItemResumen("P(${dato(xPredecir)})", num(p), destacado = true)
            series += Serie("Predicción", TipoSerie.PUNTOS, listOf(Punto(xPredecir, p)), destacada = true)
        }

        return SalidaMetodo(
            metodo = MetodoId.REGRESION_POLINOMIAL,
            estado = Notacion.estado(r.advertencias),
            resumen = resumen,
            formulas = listOf(formula),
            grafica = Grafica("Ajuste polinomial", "x", "y", series),
            bloques = listOf(tabla) + bloquesMatrices(r) + conclusion(r, "El polinomio"),
            advertencias = r.advertencias,
            error = null,
        )
    }

    /** `x[i]` = [x₁ᵢ, …, xₖᵢ]. */
    fun presentarRegresionMultiple(
        r: ResultadoRegresion,
        x: Array<DoubleArray>,
        y: DoubleArray,
        xPredecir: DoubleArray? = null,
    ): SalidaMetodo {
        val n = y.size
        val k = r.coeficientes.size - 1
        val ecuacion = "y = " + Notacion.polinomio(r.coeficientes, (0..k).map { j -> if (j == 0) "" else "x${sub(j)}" })

        val formula = Formula(
            titulo = "Regresión lineal múltiple por mínimos cuadrados",
            general = listOf(
                "y = a₀ + a₁x₁ + … + aₖxₖ",
                "Sistema normal: C·a = b,  C = XᵀX,  b = Xᵀy  (X tiene una columna de unos y una por variable)",
                "a = C⁻¹·b",
                "Q = Σ (ŷᵢ − yᵢ)²   ·   R² = (S_yy − Q) / S_yy   ·   S = √(Q / (n − k − 1))",
            ),
            sustituida = listOf(ecuacion) + lineasAjuste(r, "n − k − 1 = ${n - k - 1}"),
        )

        val columnas = listOf("i") + (1..k).map { "x${sub(it)}ᵢ" } + listOf("yᵢ", "ŷᵢ", "(ŷᵢ−yᵢ)²", "(yᵢ−ȳ)²")
        val valores = (0 until n).map { i -> (x[i].toList() + y[i] + finDeFila(r, y[i], i)).toDoubleArray() }
        val tabla = BloqueTabla(
            titulo = "Datos y residuos",
            columnas = columnas,
            filas = Notacion.conFilaDeSumas((1..n).map { Celda.Entero(it) }, valores),
            filaTotal = true,
        )

        val resumen = mutableListOf(ItemResumen("Ecuación", ecuacion))
        resumen += resumenAjuste(r)
        resumen += ItemResumen("Variables independientes (k)", k.toString())
        if (xPredecir != null) {
            val etiqueta = "Predicción " + xPredecir.joinToString(", ", "(", ")") { dato(it) }
            resumen += ItemResumen(etiqueta, num(r.predecir(xPredecir)), destacado = true)
        }

        val realVsPredicho = (0 until n).map { Punto(r.yAjustada[it], y[it]) }
        val minimo = minOf(y.min(), r.yAjustada.min())
        val maximo = maxOf(y.max(), r.yAjustada.max())
        val grafica = Grafica(
            titulo = "Real vs predicho",
            ejeX = "ŷ (predicho)",
            ejeY = "y (real)",
            series = listOf(
                Serie("Real vs predicho", TipoSerie.PUNTOS, realVsPredicho),
                Serie("y = x", TipoSerie.LINEA, listOf(Punto(minimo, minimo), Punto(maximo, maximo))),
            ),
        )

        return SalidaMetodo(
            metodo = MetodoId.REGRESION_MULTIPLE,
            estado = Notacion.estado(r.advertencias),
            resumen = resumen,
            formulas = listOf(formula),
            grafica = grafica,
            bloques = listOf(tabla) + bloquesMatrices(r) + conclusion(r, "El modelo"),
            advertencias = r.advertencias,
            error = null,
        )
    }

    // ── auxiliares ──

    private fun factorPotencia(variable: String, k: Int): String = if (k == 0) "" else variable + potencia(k)

    /** ŷᵢ, (ŷᵢ−yᵢ)², (yᵢ−ȳ)² */
    private fun finDeFila(r: ResultadoRegresion, yi: Double, i: Int): List<Double> {
        val d = yi - r.mediaY
        return listOf(r.yAjustada[i], r.residuos[i] * r.residuos[i], d * d)
    }

    private fun lineasAjuste(r: ResultadoRegresion, gradosLibertad: String): List<String> {
        val lineas = mutableListOf(
            "Q = ${num(r.Q)}",
            "ȳ = ${num(r.mediaY)},  S_yy = ${num(r.Syy)}",
            "R² = (${num(r.Syy)} − ${num(r.Q)}) / ${num(r.Syy)} = ${num(r.r2)}",
        )
        val s = r.errorEstandar
        if (s != null) lineas += "S = √(${num(r.Q)} / ${r.n - r.coeficientes.size}) = ${num(s)}   ($gradosLibertad)"
        return lineas
    }

    private fun resumenAjuste(r: ResultadoRegresion): List<ItemResumen> {
        val items = mutableListOf(
            ItemResumen("Q", num(r.Q)),
            ItemResumen("R²", num(r.r2)),
        )
        val s = r.errorEstandar
        if (s != null) items += ItemResumen("S", num(s))
        items += ItemResumen("n", r.n.toString())
        return items
    }

    private fun bloquesMatrices(r: ResultadoRegresion): List<Bloque> = listOf(
        BloqueMatriz("Matriz del sistema normal", "C", Notacion.matriz(r.matrizC)),
        BloqueMatriz("Vector independiente", "b", Notacion.columna(r.vectorB)),
        BloqueMatriz("Inversa de C", "C⁻¹", Notacion.matriz(r.inversaC)),
        BloqueMatriz("Coeficientes", "a", Notacion.columna(r.coeficientes)),
    )

    private fun conclusion(r: ResultadoRegresion, sujeto: String): BloqueTexto {
        val calidad = when {
            r.r2 >= 0.95 -> "Ajuste muy bueno."
            r.r2 >= 0.8 -> "Ajuste bueno."
            else -> "Ajuste débil."
        }
        val texto = "$sujeto explica el ${Formato.porcentaje(r.r2, 2)} de la variabilidad de y (R² = ${Formato.fijo(r.r2, 6)}). $calidad"
        return BloqueTexto(texto, TipoTexto.CONCLUSION, progreso = r.r2.coerceIn(0.0, 1.0))
    }
}
