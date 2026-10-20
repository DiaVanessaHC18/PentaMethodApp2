package com.example.pentametod2.ui.formularios

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pentametod2.catalogo.ModoNodos
import com.example.pentametod2.model.MetodoId
import com.example.pentametod2.ui.componentes.BotonPrimario
import com.example.pentametod2.ui.componentes.BotonSecundario
import com.example.pentametod2.ui.componentes.BotonTonal
import com.example.pentametod2.ui.componentes.CajaInfo
import com.example.pentametod2.ui.componentes.CampoTexto
import com.example.pentametod2.ui.componentes.EtiquetaSeccion
import com.example.pentametod2.ui.componentes.FilaDoble
import com.example.pentametod2.ui.componentes.Segmentado
import com.example.pentametod2.ui.componentes.TarjetaContorno
import com.example.pentametod2.ui.componentes.TecladoDatos
import com.example.pentametod2.ui.componentes.TituloTarjeta
import com.example.pentametod2.ui.tema.Dimens
import com.example.pentametod2.ui.tema.Formas
import com.example.pentametod2.ui.tema.Iconos
import com.example.pentametod2.ui.tema.Icono
import com.example.pentametod2.ui.tema.mono

const val SUGERENCIA_FORMATO = "Usa punto decimal y separa con comas"
const val INFO_NEWTON = "Escribe cada ecuación igualada a cero. Usa ^ para potencias y punto decimal. El jacobiano se calcula solo."
const val INFO_PUNTO_FIJO = "Escribe cada despeje x = G(x). Usa ^ para potencias y punto decimal."
const val INFO_NORMA = "Error: norma infinito ‖·‖∞"

private const val SUB = "₀₁₂₃₄₅₆₇₈₉"

/** 1 → "₁" */
fun subindice(n: Int): String = n.toString().map { SUB[it - '0'] }.joinToString("")

/** Etiqueta completa de la ecuación i: "x = G₁(x, y)" (punto fijo) o "F₁(x, y) = 0" (Newton). */
fun etiquetaEcuacion(puntoFijo: Boolean, i: Int, variables: List<String>): String {
    val args = variables.joinToString(", ")
    return if (puntoFijo) "${variables.getOrElse(i) { "x" }} = G${subindice(i + 1)}($args)" else "F${subindice(i + 1)}($args) = 0"
}

/** Label y prefijo del campo de la ecuación i: ("F₁(x, y)", "=") o ("G₁(x, y)", "x ="). */
fun campoEcuacion(puntoFijo: Boolean, i: Int, variables: List<String>): Pair<String, String> {
    val args = variables.joinToString(", ")
    return if (puntoFijo) {
        "G${subindice(i + 1)}($args)" to "${variables.getOrElse(i) { "x" }} ="
    } else {
        "F${subindice(i + 1)}($args)" to "="
    }
}

/** Contador "2 de 2": ecuaciones escritas frente a variables declaradas. */
fun contadorEcuaciones(estado: EstadoSistema): String {
    val variables = listaLegible(estado.variables).split(", ").count { it.isNotEmpty() }
    return "${estado.ecuaciones.size} de $variables"
}

/**
 * Formulario en modo EDICIÓN: las tarjetas de datos del método con los botones "Cargar ejemplo" y "Calcular".
 * Sistemas: tarjetas "Ecuaciones" y "Parámetros"; el resto: una tarjeta "Datos".
 */
@Composable
fun TarjetasEdicion(
    id: MetodoId,
    estado: EstadoFormulario,
    calculando: Boolean,
    alCambiar: (EstadoFormulario) -> Unit,
    alCargarEjemplo: () -> Unit,
    alCalcular: () -> Unit,
) {
    val botones = @Composable { BotonesDatos(calculando, alCargarEjemplo, alCalcular) }
    when (estado) {
        is EstadoSistema -> FormularioSistema(estado, id == MetodoId.PUNTO_FIJO, alCambiar, botones)
        is EstadoInterpolacion -> TarjetaDatos(botones) { FormularioInterpolacion(estado, alCambiar) }
        is EstadoRegresionPolinomial -> TarjetaDatos(botones) { FormularioRegresionPolinomial(estado, alCambiar) }
        is EstadoRegresionMultiple -> TarjetaDatos(botones) { FormularioRegresionMultiple(estado, alCambiar) }
    }
}

/** Con fuente grande (> 1.15×) los botones no caben lado a lado sin recortar el texto: se apilan. */
fun apilarBotones(escalaFuente: Float): Boolean = escalaFuente > 1.15f

@Composable
private fun BotonesDatos(calculando: Boolean, alCargarEjemplo: () -> Unit, alCalcular: () -> Unit) {
    val textoCalcular = if (calculando) "Calculando…" else "Calcular"
    if (apilarBotones(LocalDensity.current.fontScale)) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.e12)) {
            BotonPrimario(textoCalcular, alCalcular, Modifier.fillMaxWidth(), icono = Iconos.calculadora, habilitado = !calculando)
            BotonSecundario("Cargar ejemplo", alCargarEjemplo, Modifier.fillMaxWidth(), icono = Iconos.ejemplo)
        }
    } else {
        FilaDoble(Modifier.fillMaxWidth()) {
            BotonSecundario("Cargar ejemplo", alCargarEjemplo, Modifier.weight(1f), icono = Iconos.ejemplo)
            BotonPrimario(textoCalcular, alCalcular, Modifier.weight(1f), icono = Iconos.calculadora, habilitado = !calculando)
        }
    }
}

@Composable
private fun TarjetaDatos(botones: @Composable () -> Unit, campos: @Composable () -> Unit) {
    TarjetaContorno(espacio = Dimens.e4) {
        EtiquetaSeccion("Entrada")
        TituloTarjeta("Datos")
        Column(Modifier.fillMaxWidth().padding(top = Dimens.e12), verticalArrangement = Arrangement.spacedBy(Dimens.entreCampos)) {
            campos()
            botones()
        }
    }
}

// ── Sistemas ──

@Composable
fun FormularioSistema(
    estado: EstadoSistema,
    puntoFijo: Boolean,
    alCambiar: (EstadoFormulario) -> Unit,
    botones: @Composable () -> Unit,
) {
    val nombres = estado.nombresVariables()
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.entreTarjetas)) {
        TarjetaContorno(espacio = Dimens.e4) {
            EtiquetaSeccion("Entrada")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    TituloTarjeta("Ecuaciones")
                    Text(
                        if (puntoFijo) "  G(x)" else "  F(x) = 0",
                        style = MaterialTheme.mono.formula,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(contadorEcuaciones(estado), style = MaterialTheme.mono.pequeno, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.padding(top = Dimens.e12), verticalArrangement = Arrangement.spacedBy(Dimens.entreEcuaciones)) {
                estado.ecuaciones.forEachIndexed { i, texto ->
                    val (etiqueta, prefijo) = campoEcuacion(puntoFijo, i, nombres)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.e4)) {
                        CampoTexto(texto, { alCambiar(estado.conEcuacion(i, it)) }, etiqueta, Modifier.weight(1f), prefijo = prefijo)
                        IconButton(onClick = { alCambiar(estado.quitarEcuacion(i)) }, enabled = estado.ecuaciones.size > MIN_ECUACIONES) {
                            Icono(Iconos.quitar, "Quitar ecuación ${i + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                BotonTonal(
                    "Agregar ecuación",
                    { alCambiar(estado.agregarEcuacion()) },
                    icono = Iconos.agregar,
                    habilitado = estado.ecuaciones.size < MAX_ECUACIONES,
                )
            }
        }
        TarjetaContorno(espacio = Dimens.entreCampos) {
            TituloTarjeta("Parámetros")
            FilaDoble {
                CampoTexto(estado.variables, { alCambiar(estado.copy(variables = it)) }, "Variables", Modifier.weight(1f))
                CampoTexto(estado.x0, { alCambiar(estado.copy(x0 = it)) }, "Valores iniciales x₀", Modifier.weight(1f))
            }
            FilaDoble {
                CampoTexto(estado.tolerancia, { alCambiar(estado.copy(tolerancia = it)) }, "Tolerancia", Modifier.weight(1f))
                CampoTexto(estado.maxIter, { alCambiar(estado.copy(maxIter = it)) }, "Máx. iteraciones", Modifier.weight(1f))
            }
            Text(INFO_NORMA, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            CajaInfo(if (puntoFijo) INFO_PUNTO_FIJO else INFO_NEWTON)
            botones()
        }
    }
}

// ── Interpolación ──

@Composable
fun FormularioInterpolacion(estado: EstadoInterpolacion, alCambiar: (EstadoFormulario) -> Unit) {
    CampoTexto(estado.xs, { alCambiar(estado.copy(xs = it)) }, "Nodos x", multilinea = true)
    CampoTexto(estado.ys, { alCambiar(estado.copy(ys = it)) }, "Valores y", multilinea = true, ayuda = SUGERENCIA_FORMATO)
    CampoTexto(estado.x, { alCambiar(estado.copy(x = it)) }, "x a evaluar")
    Segmentado(
        opciones = listOf("Manual", "Automático"),
        seleccionado = if (estado.modo == ModoNodos.MANUAL) 0 else 1,
        alElegir = { alCambiar(estado.copy(modo = if (it == 0) ModoNodos.MANUAL else ModoNodos.AUTOMATICO)) },
    )
    if (estado.modo == ModoNodos.AUTOMATICO) {
        CampoTexto(estado.grado, { alCambiar(estado.copy(grado = it)) }, "Grado", ayuda = "Se usan los grado + 1 nodos más cercanos a x")
    }
}

// ── Regresión polinomial ──

@Composable
fun FormularioRegresionPolinomial(estado: EstadoRegresionPolinomial, alCambiar: (EstadoFormulario) -> Unit) {
    CampoTexto(estado.xs, { alCambiar(estado.copy(xs = it)) }, "Valores x", multilinea = true)
    CampoTexto(estado.ys, { alCambiar(estado.copy(ys = it)) }, "Valores y", multilinea = true, ayuda = SUGERENCIA_FORMATO)
    FilaDoble {
        CampoTexto(estado.grado, { alCambiar(estado.copy(grado = it)) }, "Grado", Modifier.width(96.dp))
        CampoTexto(estado.xPredecir, { alCambiar(estado.copy(xPredecir = it)) }, "x a predecir (opcional)", Modifier.weight(1f))
    }
}

// ── Regresión múltiple ──

private val ANCHO_CELDA = 96.dp

@Composable
fun FormularioRegresionMultiple(estado: EstadoRegresionMultiple, alCambiar: (EstadoFormulario) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.e8)) {
        Text("Variables independientes (k)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Segmentado(
            opciones = (MIN_VARIABLES..MAX_VARIABLES).map { it.toString() },
            seleccionado = estado.numVariables - MIN_VARIABLES,
            alElegir = { alCambiar(estado.conNumVariables(it + MIN_VARIABLES)) },
        )
    }
    val k = estado.numVariables
    val encabezados = (1..k).map { "x${subindice(it)}" } + "y"
    Column(Modifier.horizontalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Dimens.e8)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.e8)) {
            Box(Modifier.width(28.dp))
            for (h in encabezados) {
                Text(h, Modifier.width(ANCHO_CELDA), textAlign = TextAlign.Center, style = MaterialTheme.mono.tablaFuerte, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        estado.filas.forEachIndexed { i, fila ->
            FilaCuadricula(i, fila, estado.filas.size > MIN_FILAS, { j, t -> alCambiar(estado.conCelda(i, j, t)) }) {
                alCambiar(estado.quitarFila(i))
            }
        }
    }
    BotonTonal("Agregar fila", { alCambiar(estado.agregarFila()) }, icono = Iconos.agregar)
    CampoTexto(
        estado.xPredecir, { alCambiar(estado.copy(xPredecir = it)) }, "Valores a predecir (opcional)",
        ayuda = "Un valor por cada x, p. ej.: 1, 1",
    )
}

@Composable
private fun FilaCuadricula(
    i: Int,
    fila: List<String>,
    sePuedeQuitar: Boolean,
    alCambiarCelda: (Int, String) -> Unit,
    alQuitar: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.e8)) {
        Text("${i + 1}", Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.mono.pequeno, color = MaterialTheme.colorScheme.onSurfaceVariant)
        fila.forEachIndexed { j, celda ->
            OutlinedTextField(
                value = celda,
                onValueChange = { alCambiarCelda(j, it) },
                singleLine = true,
                keyboardOptions = TecladoDatos,
                textStyle = MaterialTheme.mono.campo.copy(textAlign = TextAlign.End),
                shape = Formas.campo,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.width(ANCHO_CELDA),
            )
        }
        IconButton(onClick = alQuitar, enabled = sePuedeQuitar) {
            Icono(Iconos.quitar, "Quitar fila ${i + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
