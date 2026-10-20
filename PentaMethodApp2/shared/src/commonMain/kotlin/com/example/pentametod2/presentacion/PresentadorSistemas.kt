package com.example.pentametod2.presentacion

import com.example.pentametod2.methods.AlgebraLineal
import com.example.pentametod2.methods.PasoNewton
import com.example.pentametod2.methods.ResultadoSistema
import com.example.pentametod2.model.Bloque
import com.example.pentametod2.model.BloqueGrupo
import com.example.pentametod2.model.BloqueMatriz
import com.example.pentametod2.model.BloqueTabla
import com.example.pentametod2.model.BloqueTexto
import com.example.pentametod2.model.Celda
import com.example.pentametod2.model.Estado
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
import com.example.pentametod2.presentacion.Notacion.sub
import com.example.pentametod2.presentacion.Notacion.sup

object PresentadorSistemas {

    /** `expresiones[i]` es Gᵢ (xᵢ = Gᵢ(x)). */
    fun presentarPuntoFijo(r: ResultadoSistema, expresiones: List<String>, tolerancia: Double? = null): SalidaMetodo {
        val sustituida = r.variables.indices.map { i -> "${r.variables[i]} = ${expresiones.getOrElse(i) { "?" }}" } +
            reglaDeParada(tolerancia)
        val formula = Formula(
            titulo = "Punto fijo para sistemas",
            general = listOf("x⁽ᵏ⁺¹⁾ = G(x⁽ᵏ⁾)", "E = ‖x⁽ᵏ⁺¹⁾ − x⁽ᵏ⁾‖∞"),
            sustituida = sustituida,
        )
        val filas = r.iteraciones.map { iter ->
            listOf<Celda>(Celda.Entero(iter.k)) + iter.x.map { v -> Celda.Num(v) } + celdaError(iter.error)
        }
        val tabla = BloqueTabla("Iteraciones", listOf("k") + r.variables + "E", filas)
        return salidaSistema(MetodoId.PUNTO_FIJO, r, formula, listOf(tabla), "‖G(x*) − x*‖∞", tolerancia)
    }

    /** `expresiones[i]` es Fᵢ, con Fᵢ(x) = 0. */
    fun presentarNewtonSistemas(r: ResultadoSistema, expresiones: List<String>, tolerancia: Double? = null): SalidaMetodo {
        val sustituida = r.variables.indices.map { i -> "F${sub(i + 1)} = ${expresiones.getOrElse(i) { "?" }}" } +
            reglaDeParada(tolerancia)
        val formula = Formula(
            titulo = "Newton-Raphson para sistemas",
            general = listOf(
                "J(x⁽ᵏ⁾) · Δ = −F(x⁽ᵏ⁾)",
                "x⁽ᵏ⁺¹⁾ = x⁽ᵏ⁾ + Δ",
                "Jᵢⱼ ≈ ( Fᵢ(x + hⱼeⱼ) − Fᵢ(x − hⱼeⱼ) ) / (2hⱼ)   (Jacobiano numérico)",
                "E = ‖x⁽ᵏ⁺¹⁾ − x⁽ᵏ⁾‖∞",
            ),
            sustituida = sustituida,
        )
        val filas = r.iteraciones.map { iter ->
            listOf<Celda>(Celda.Entero(iter.k)) + iter.x.map { v -> Celda.Num(v) } +
                normaF(r, iter.k) + celdaError(iter.error)
        }
        val tabla = BloqueTabla("Iteraciones", listOf("k") + r.variables + listOf("‖F(x)‖∞", "E"), filas)
        val detalles = r.detalle.map { detalleNewton(it, r.variables) }
        return salidaSistema(MetodoId.NEWTON_SISTEMAS, r, formula, listOf<Bloque>(tabla) + detalles, "‖F(x*)‖∞", tolerancia)
    }

    // ── auxiliares ──

    private fun salidaSistema(
        metodo: MetodoId,
        r: ResultadoSistema,
        formula: Formula,
        bloques: List<Bloque>,
        etiquetaResidual: String,
        tolerancia: Double?,
    ): SalidaMetodo {
        val tipoMensaje = if (r.convergio) TipoTexto.CONCLUSION else TipoTexto.ADVERTENCIA
        return SalidaMetodo(
            metodo = metodo,
            estado = if (r.convergio) Estado.OK else Estado.ERROR,
            resumen = resumenSistema(r, etiquetaResidual),
            formulas = listOf(formula),
            grafica = graficaError(r, tolerancia),
            bloques = bloques + BloqueTexto(r.mensaje, tipoMensaje),
            advertencias = emptyList(),
            error = if (r.convergio) null else r.mensaje,
        )
    }

    private fun reglaDeParada(tol: Double?): List<String> =
        if (tol == null) emptyList() else listOf("Se detiene cuando E < ${dato(tol)}")

    private fun celdaError(e: Double?): Celda = if (e == null) Celda.Vacia else Celda.Num(e)

    /** ‖F(x⁽ᵏ⁾)‖∞: sale del paso k+1 (que evaluó F en x⁽ᵏ⁾) o, en la última fila, del residual. */
    private fun normaF(r: ResultadoSistema, k: Int): Celda {
        val paso = r.detalle.firstOrNull { it.k == k + 1 }
        if (paso != null) return Celda.Num(AlgebraLineal.normaInf(paso.fx))
        val residual = r.residual
        if (r.convergio && residual != null && k == r.iteraciones.last().k) return Celda.Num(residual)
        return Celda.Vacia
    }

    private fun detalleNewton(p: PasoNewton, variables: List<String>): BloqueGrupo {
        val anterior = "x⁽${sup(p.k - 1)}⁾"
        val bloques = mutableListOf<Bloque>(
            BloqueTexto("Se evalúa en $anterior: ${Notacion.asignaciones(variables, p.x)}"),
            BloqueMatriz("Jacobiano en $anterior", "J", Notacion.matriz(p.jacobiano)),
            BloqueMatriz("F en $anterior", "F(x)", Notacion.columna(p.fx)),
        )
        bloques += bloqueDelta(p)
        return BloqueGrupo("Detalle de la iteración ${p.k}", bloques, colapsado = true)
    }

    private fun bloqueDelta(p: PasoNewton): Bloque {
        val delta = p.delta ?: return BloqueTexto("El Jacobiano es singular: no se puede calcular Δ.", TipoTexto.ADVERTENCIA)
        return BloqueMatriz("Solución de J·Δ = −F", "Δ", Notacion.columna(delta))
    }

    private fun resumenSistema(r: ResultadoSistema, etiquetaResidual: String): List<ItemResumen> {
        val items = mutableListOf<ItemResumen>()
        val sol = r.solucion
        if (sol != null) {
            items += ItemResumen("Solución", Notacion.asignaciones(r.variables, sol))
        } else {
            items += ItemResumen("Último valor", Notacion.asignaciones(r.variables, r.iteraciones.last().x))
        }
        items += ItemResumen("Iteraciones", r.iteraciones.last().k.toString())
        val e = r.iteraciones.last().error
        items += ItemResumen("Error final", if (e == null) "—" else num(e))
        val residual = r.residual
        if (residual != null) items += ItemResumen("Residual $etiquetaResidual", num(residual))
        items += ItemResumen("Convergió", if (r.convergio) "Sí" else "No")
        return items
    }

    private fun graficaError(r: ResultadoSistema, tolerancia: Double?): Grafica {
        val puntos = mutableListOf<Punto>()
        for (iter in r.iteraciones) {
            val e = iter.error ?: continue
            if (e > 0 && e.isFinite()) puntos += Punto(iter.k.toDouble(), e)
        }
        val series = mutableListOf(Serie("E(k)", TipoSerie.LINEA_CON_PUNTOS, puntos))
        val lineaTolerancia = serieTolerancia(puntos, tolerancia)
        if (lineaTolerancia != null) series += lineaTolerancia
        return Grafica(
            titulo = "Error por iteración",
            ejeX = "k",
            ejeY = "E",
            series = series,
            escalaLogY = true,
        )
    }

    const val NOMBRE_TOLERANCIA = "Tolerancia"

    /** Línea horizontal y = tolerancia sobre el rango de k de la serie de error (discontinua). */
    private fun serieTolerancia(puntos: List<Punto>, tolerancia: Double?): Serie? {
        if (tolerancia == null || !(tolerancia > 0) || puntos.isEmpty()) return null
        val desde = puntos.minOf { it.x }
        val hasta = puntos.maxOf { it.x }
        return Serie(
            NOMBRE_TOLERANCIA,
            TipoSerie.LINEA,
            listOf(Punto(desde, tolerancia), Punto(hasta, tolerancia)),
            discontinua = true,
        )
    }
}
