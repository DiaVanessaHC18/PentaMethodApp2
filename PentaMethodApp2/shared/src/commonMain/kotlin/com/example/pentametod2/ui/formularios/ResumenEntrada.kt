package com.example.pentametod2.ui.formularios

import com.example.pentametod2.catalogo.ModoNodos
import com.example.pentametod2.model.MetodoId

/** Una línea de la tarjeta de datos comprimida (estado RESULTADO). */
data class LineaEntrada(val etiqueta: String, val valor: String)

private val SEPARADORES = Regex("[,;\\s]+")

/** "1,2;3  4" → "1, 2, 3, 4" (tal como lo leerá el catálogo). */
fun listaLegible(texto: String): String = texto.trim().split(SEPARADORES).filter { it.isNotEmpty() }.joinToString(", ")

private fun oDefecto(texto: String, defecto: String) = texto.trim().ifEmpty { defecto }

/** Resumen de solo lectura de lo que escribió el alumno. */
fun resumenEntrada(id: MetodoId, estado: EstadoFormulario): List<LineaEntrada> = when (estado) {
    is EstadoSistema -> resumenSistema(id == MetodoId.PUNTO_FIJO, estado)
    is EstadoInterpolacion -> resumenInterpolacion(estado)
    is EstadoRegresionPolinomial -> resumenPolinomial(estado)
    is EstadoRegresionMultiple -> resumenMultiple(estado)
}

private fun resumenSistema(puntoFijo: Boolean, e: EstadoSistema): List<LineaEntrada> {
    val nombres = e.nombresVariables()
    val lineas = e.ecuaciones.mapIndexed { i, ec ->
        if (puntoFijo) {
            LineaEntrada("G${subindice(i + 1)}", "${nombres.getOrElse(i) { "x" }} = ${ec.trim()}")
        } else {
            LineaEntrada("F${subindice(i + 1)}", "${ec.trim()} = 0")
        }
    }
    return lineas + listOf(
        LineaEntrada("Variables", listaLegible(e.variables)),
        LineaEntrada("x₀", "(" + listaLegible(e.x0) + ")"),
        LineaEntrada("Criterio", "tol ${oDefecto(e.tolerancia, "1e-6")} · máx ${oDefecto(e.maxIter, "50")}"),
    )
}

private fun resumenInterpolacion(e: EstadoInterpolacion): List<LineaEntrada> {
    val nodos = if (e.modo == ModoNodos.MANUAL) "Manual" else "Automático · grado ${e.grado.trim()}"
    return listOf(
        LineaEntrada("x", listaLegible(e.xs)),
        LineaEntrada("y", listaLegible(e.ys)),
        LineaEntrada("x a evaluar", e.x.trim()),
        LineaEntrada("Nodos", nodos),
    )
}

private fun resumenPolinomial(e: EstadoRegresionPolinomial): List<LineaEntrada> {
    val grado = e.grado.trim() + if (e.xPredecir.isBlank()) "" else " · x a predecir ${e.xPredecir.trim()}"
    return listOf(
        LineaEntrada("x", listaLegible(e.xs)),
        LineaEntrada("y", listaLegible(e.ys)),
        LineaEntrada("Grado", grado),
    )
}

private fun resumenMultiple(e: EstadoRegresionMultiple): List<LineaEntrada> {
    val filas = e.filas.count { f -> f.any { it.isNotBlank() } }
    val lineas = mutableListOf(LineaEntrada("Datos", "$filas filas · k = ${e.numVariables}"))
    val encabezado = (1..e.numVariables).joinToString(", ") { "x${subindice(it)}" } + ", y"
    lineas += LineaEntrada("Columnas", encabezado)
    if (e.xPredecir.isNotBlank()) lineas += LineaEntrada("Predecir", "(" + listaLegible(e.xPredecir) + ")")
    return lineas
}
