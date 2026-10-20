package com.example.pentametod2.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pentametod2.ui.tema.DefIcono
import com.example.pentametod2.ui.tema.Dimens
import com.example.pentametod2.ui.tema.Formas
import com.example.pentametod2.ui.tema.Iconos
import com.example.pentametod2.ui.tema.Icono
import com.example.pentametod2.ui.tema.mono

// ── Campos ──

/** Teclado de texto (las listas necesitan coma, punto, espacio y signo menos), sin autocorrección. */
val TecladoDatos = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false, imeAction = ImeAction.Next)

/**
 * Campo del diseño: OutlinedTextField con label flotante, texto mono 16, borde 1dp outline (2dp primary con foco),
 * radio 4, alto 56. `prefijo` (p. ej. "=") en mono para ecuaciones.
 */
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    prefijo: String? = null,
    multilinea: Boolean = false,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingText = textoAyuda(ayuda),
        prefix = textoPrefijo(prefijo),
        singleLine = !multilinea,
        maxLines = if (multilinea) 4 else 1,
        keyboardOptions = TecladoDatos,
        textStyle = MaterialTheme.mono.campo,
        shape = Formas.campo,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.campo),
    )
}

private fun textoAyuda(ayuda: String?): (@Composable () -> Unit)? {
    if (ayuda == null) return null
    return { Text(ayuda, style = MaterialTheme.typography.bodySmall) }
}

private fun textoPrefijo(prefijo: String?): (@Composable () -> Unit)? {
    if (prefijo == null) return null
    return { Text("$prefijo ", style = MaterialTheme.mono.campo, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

// ── Botones (48dp, radio 24, ícono 18dp + labelLarge) ──

private val RellenoBoton = PaddingValues(horizontal = 20.dp, vertical = 10.dp)

@Composable
private fun RowScope.ContenidoBoton(texto: String, icono: DefIcono?) {
    if (icono != null) {
        Icono(icono, null, tamano = Dimens.iconoBoton, enBoton = true)
        Spacer(Modifier.width(Dimens.e8))
    }
    Text(texto, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
fun BotonPrimario(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier, icono: DefIcono? = null, habilitado: Boolean = true) {
    Button(
        onClick = alPulsar,
        enabled = habilitado,
        shape = Formas.boton,
        contentPadding = RellenoBoton,
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        modifier = modifier.heightIn(min = Dimens.boton),
    ) { ContenidoBoton(texto, icono) }
}

@Composable
fun BotonSecundario(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier, icono: DefIcono? = null, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        shape = Formas.boton,
        contentPadding = RellenoBoton,
        border = BorderStroke(Dimens.borde, if (habilitado) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = modifier.heightIn(min = Dimens.boton),
    ) { ContenidoBoton(texto, icono) }
}

@Composable
fun BotonTexto(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: DefIcono? = null,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = alPulsar,
        shape = Formas.boton,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        modifier = modifier.heightIn(min = Dimens.boton),
    ) { ContenidoBoton(texto, icono) }
}

@Composable
fun BotonTonal(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier, icono: DefIcono? = null, habilitado: Boolean = true) {
    Button(
        onClick = alPulsar,
        enabled = habilitado,
        shape = Formas.boton,
        contentPadding = RellenoBoton,
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = modifier.heightIn(min = Dimens.boton),
    ) { ContenidoBoton(texto, icono) }
}

/** Segmentado (Material 3): alto 48, borde 1dp outline, radio 24, seleccionado en primaryContainer con check 16dp. */
@Composable
fun Segmentado(opciones: List<String>, seleccionado: Int, alElegir: (Int) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        opciones.forEachIndexed { i, texto ->
            SegmentedButton(
                selected = i == seleccionado,
                onClick = { alElegir(i) },
                shape = SegmentedButtonDefaults.itemShape(i, opciones.size, Formas.boton),
                border = SegmentedButtonDefaults.borderStroke(MaterialTheme.colorScheme.outline, Dimens.borde),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = Color.Transparent,
                ),
                icon = { IconoSegmento(i == seleccionado) },
                modifier = Modifier.heightIn(min = Dimens.boton),
            ) { Text(texto, style = MaterialTheme.typography.labelLarge, maxLines = 1) }
        }
    }
}

@Composable
private fun IconoSegmento(activo: Boolean) {
    if (activo) Icono(Iconos.check, null, tamano = Dimens.iconoInfo)
}

/** Fila de dos controles con flex 1 y separación 12. */
@Composable
fun FilaDoble(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.e12), verticalAlignment = Alignment.Top, content = content)
}
