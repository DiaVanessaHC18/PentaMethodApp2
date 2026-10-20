package com.example.pentametod2.ui.resultados

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pentametod2.catalogo.Catalogo
import com.example.pentametod2.catalogo.MetodoInfo
import com.example.pentametod2.model.MetodoId
import com.example.pentametod2.model.SalidaMetodo
import com.example.pentametod2.ui.componentes.BarraSuperior
import com.example.pentametod2.ui.componentes.BotonTexto
import com.example.pentametod2.ui.componentes.ContenidoCentrado
import com.example.pentametod2.ui.componentes.EtiquetaSeccion
import com.example.pentametod2.ui.componentes.TarjetaContorno
import com.example.pentametod2.ui.componentes.TituloTarjeta
import com.example.pentametod2.ui.formularios.EstadoFormulario
import com.example.pentametod2.ui.formularios.FormularioOpcionalSaver
import com.example.pentametod2.ui.formularios.FormularioSaver
import com.example.pentametod2.ui.formularios.LineaEntrada
import com.example.pentametod2.ui.formularios.TarjetasEdicion
import com.example.pentametod2.ui.formularios.estadoDesdeEntrada
import com.example.pentametod2.ui.formularios.estadoInicial
import com.example.pentametod2.ui.formularios.resumenEntrada
import com.example.pentametod2.ui.tema.Dimens
import com.example.pentametod2.ui.tema.Formas
import com.example.pentametod2.ui.tema.Iconos
import com.example.pentametod2.ui.tema.mono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val TEXTO_GUIA = "Escribe tus datos y pulsa Calcular, o carga un ejemplo."
const val TEXTO_GUIA_PIE = "Aquí verás la fórmula, el resumen, la gráfica y la tabla de pasos."

/**
 * Pantalla de un método con dos estados:
 *  1. EDICIÓN: tarjetas del formulario (y el estado vacío si aún no hay resultado).
 *  2. RESULTADO: la entrada comprimida (solo lectura, botón "Editar") y los resultados.
 *
 * Se guarda (rememberSaveable) el formulario, el formulario con el que se calculó y si se está editando;
 * la [SalidaMetodo] no se guarda: se recalcula al restaurar (rotación, volver del menú).
 */
@Composable
fun MetodoScreen(info: MetodoInfo, alVolver: () -> Unit, alAbrirMetodo: (MetodoId) -> Unit = {}) {
    val id = info.id
    var formulario by rememberSaveable(stateSaver = FormularioSaver) { mutableStateOf(estadoInicial(id)) }
    var calculado by rememberSaveable(stateSaver = FormularioOpcionalSaver) { mutableStateOf<EstadoFormulario?>(null) }
    var numCalculo by rememberSaveable { mutableIntStateOf(0) }
    var editando by rememberSaveable { mutableStateOf(true) }
    var ayuda by rememberSaveable { mutableStateOf(false) }
    var salida by remember { mutableStateOf<SalidaMetodo?>(null) }
    var calculando by remember { mutableStateOf(false) }

    LaunchedEffect(numCalculo) {
        val datos = calculado ?: return@LaunchedEffect
        calculando = true
        salida = withContext(Dispatchers.Default) { Catalogo.ejecutar(id, datos.aEntrada()) }
        calculando = false
    }

    Scaffold(
        topBar = { BarraSuperior(info.nombre, alVolver, alAyuda = { ayuda = true }, cargando = calculando) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            ContenidoCentrado(Modifier.padding(horizontal = Dimens.margenLateral, vertical = Dimens.e8)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.entreTarjetas)) {
                    ChipCategoria(info.seccion)
                    val datosCalculados = calculado
                    if (editando || datosCalculados == null) {
                        TarjetasEdicion(
                            id = id,
                            estado = formulario,
                            calculando = calculando,
                            alCambiar = { formulario = it },
                            alCargarEjemplo = { formulario = estadoDesdeEntrada(Catalogo.ejemplo(id)) },
                            alCalcular = {
                                calculado = formulario
                                editando = false
                                numCalculo++
                            },
                        )
                    } else {
                        TarjetaEntradaComprimida(resumenEntrada(id, datosCalculados)) { editando = true }
                    }

                    val s = salida
                    if (s == null) {
                        if (!calculando) EstadoVacio()
                    } else {
                        ResultadosMetodo(
                            salida = s,
                            alEditar = { editando = true },
                            alAbrirMetodo = alAbrirMetodo,
                            claveCalculo = numCalculo,
                        )
                    }
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars).heightIn(min = Dimens.e24))
        }
    }

    if (ayuda) DialogoAyuda(info) { ayuda = false }
}

/** Chip de categoría: alto 32, radio 8, borde 1dp outlineVariant, texto 14/500 onSurfaceVariant. */
@Composable
private fun ChipCategoria(texto: String) {
    Surface(
        shape = Formas.chip,
        color = Color.Transparent,
        border = BorderStroke(Dimens.borde, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.heightIn(min = Dimens.chipCategoria),
    ) {
        Box(Modifier.padding(horizontal = Dimens.e12, vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(
                texto,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Datos en modo RESULTADO: solo lectura, con "Editar". */
@Composable
private fun TarjetaEntradaComprimida(lineas: List<LineaEntrada>, alEditar: () -> Unit) {
    TarjetaContorno(espacio = Dimens.e4) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                EtiquetaSeccion("Entrada")
                TituloTarjeta("Datos")
            }
            BotonTexto("Editar", alEditar, icono = Iconos.editar)
        }
        Column(Modifier.padding(top = Dimens.e8), verticalArrangement = Arrangement.spacedBy(Dimens.e4)) {
            for (l in lineas) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.e12)) {
                    Text(
                        l.etiqueta,
                        style = MaterialTheme.mono.dato,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(88.dp),
                    )
                    Text(l.valor, style = MaterialTheme.mono.dato, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Estado vacío: borde punteado 1dp outlineVariant, radio 16, padding 28/24, ilustración de 56dp. */
@Composable
private fun EstadoVacio() {
    val borde = MaterialTheme.colorScheme.outlineVariant
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val r = 16.dp.toPx()
                drawRoundRect(
                    color = borde,
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(horizontal = Dimens.e24, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.e12)) {
            IlustracionVacia()
            val fuerte = MaterialTheme.colorScheme.onSurface
            Text(
                buildAnnotatedString {
                    append("Escribe tus datos y pulsa ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = fuerte)) { append("Calcular") }
                    append(", o carga un ejemplo.")
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                TEXTO_GUIA_PIE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val TRAZOS_ILUSTRACION = listOf("M8 46h40", "M10 46V8")
private const val CURVA_ILUSTRACION = "M12 12c8 10 18 22 32 26"
private val PUNTOS_ILUSTRACION = listOf(16f to 16f, 24f to 22f, 32f to 32f, 40f to 36f)

/** Ilustración del estado vacío (viewBox 56×56, trazo 1.5, outline; curva punteada en primary). */
@Composable
private fun IlustracionVacia() {
    val linea = MaterialTheme.colorScheme.outline
    val curva = MaterialTheme.colorScheme.primary
    val ejes = remember { TRAZOS_ILUSTRACION.map { PathParser().parsePathString(it).toPath() } }
    val trazoCurva = remember { PathParser().parsePathString(CURVA_ILUSTRACION).toPath() }
    Canvas(Modifier.size(56.dp)) {
        val f = size.width / 56f
        scale(f, f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            for (p in ejes) drawPath(p, linea, style = Stroke(1.5f, cap = StrokeCap.Round))
            drawPath(trazoCurva, curva, style = Stroke(1.5f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))))
            for ((x, y) in PUNTOS_ILUSTRACION) drawCircle(linea, 2f, androidx.compose.ui.geometry.Offset(x, y), style = Stroke(1.5f))
        }
    }
}

@Composable
private fun DialogoAyuda(info: MetodoInfo, alCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alCerrar,
        confirmButton = { BotonTexto("Entendido", alCerrar) },
        title = { Text(info.nombre, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.e8)) {
                Text(info.descripcion + ".", style = MaterialTheme.typography.bodyLarge)
                Text(TEXTO_GUIA, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(TEXTO_GUIA_PIE, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = Formas.tarjeta,
        tonalElevation = 0.dp,
    )
}
