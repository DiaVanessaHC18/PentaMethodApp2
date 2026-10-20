package com.example.pentametod2.model

/**
 * Salida genérica de un método numérico. La interfaz la dibuja sin saber qué método la generó,
 * en tres zonas: fórmulas (arriba); resumen + gráfica + bloques (abajo).
 */
data class SalidaMetodo(
    val metodo: MetodoId,
    val estado: Estado,
    val resumen: List<ItemResumen>,
    val formulas: List<Formula>,
    val grafica: Grafica?,
    val bloques: List<Bloque>,
    val advertencias: List<String>,
    val error: String?,
) {
    companion object {
        fun conError(metodo: MetodoId, mensaje: String) = SalidaMetodo(
            metodo = metodo, estado = Estado.ERROR, resumen = emptyList(), formulas = emptyList(),
            grafica = null, bloques = emptyList(), advertencias = emptyList(), error = mensaje,
        )
    }
}

enum class MetodoId { PUNTO_FIJO, NEWTON_SISTEMAS, REGRESION_POLINOMIAL, REGRESION_MULTIPLE, LAGRANGE, NEWTON_INTERPOLACION }

enum class Estado { OK, ADVERTENCIA, ERROR }

/** `destacado`: el valor clave del ejercicio (predicción, P(x) evaluado); la interfaz lo resalta. */
data class ItemResumen(val etiqueta: String, val valor: String, val destacado: Boolean = false)

/** `general`: la fórmula con símbolos; `sustituida`: la misma con los números del ejercicio. */
data class Formula(val titulo: String, val general: List<String>, val sustituida: List<String>)

// ── Bloques ──

sealed interface Celda {
    /** Número real; la interfaz lo formatea con los `decimales` de la tabla. */
    data class Num(val v: Double) : Celda

    /** Entero (índices i, k): se muestra sin decimales. */
    data class Entero(val v: Int) : Celda

    data class Txt(val s: String) : Celda

    data object Vacia : Celda
}

sealed interface Bloque

enum class TipoTexto { NORMAL, CONCLUSION, ADVERTENCIA }

/** `progreso` (0..1, opcional): p. ej. R² en la conclusión de una regresión, para dibujar una barra. */
data class BloqueTexto(val texto: String, val tipo: TipoTexto = TipoTexto.NORMAL, val progreso: Double? = null) : Bloque

/** Si `filaTotal` es true, la última fila de `filas` es la de totales (Σ). */
data class BloqueTabla(
    val titulo: String,
    val columnas: List<String>,
    val filas: List<List<Celda>>,
    val filaTotal: Boolean = false,
    val decimales: Int = 6,
) : Bloque

/** Matriz o vector (un vector columna es una matriz de n×1). */
data class BloqueMatriz(
    val titulo: String,
    val nombre: String,
    val datos: List<List<Double>>,
    val decimales: Int = 6,
) : Bloque

data class BloqueGrupo(val titulo: String, val bloques: List<Bloque>, val colapsado: Boolean = true) : Bloque

// ── Gráfica ──

enum class TipoSerie { PUNTOS, LINEA, LINEA_CON_PUNTOS }

data class Punto(val x: Double, val y: Double)

/** `discontinua`: se dibuja con trazo discontinuo (p. ej. la línea de tolerancia). */
data class Serie(
    val nombre: String,
    val tipo: TipoSerie,
    val puntos: List<Punto>,
    val destacada: Boolean = false,
    val discontinua: Boolean = false,
)

data class Grafica(
    val titulo: String,
    val ejeX: String,
    val ejeY: String,
    val series: List<Serie>,
    val escalaLogY: Boolean = false,
)
