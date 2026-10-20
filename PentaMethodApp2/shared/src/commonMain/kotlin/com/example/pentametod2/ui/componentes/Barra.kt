package com.example.pentametod2.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pentametod2.ui.tema.Dimens
import com.example.pentametod2.ui.tema.Iconos
import com.example.pentametod2.ui.tema.Icono

/**
 * Barra superior de 64dp: botón atrás (48, circular), título titleLarge y botón de ayuda (48) opcional.
 * Respeta la barra de estado. Si [cargando], muestra un LinearProgressIndicator de 4dp debajo.
 */
@Composable
fun BarraSuperior(
    titulo: String,
    alVolver: (() -> Unit)? = null,
    alAyuda: (() -> Unit)? = null,
    cargando: Boolean = false,
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
            Row(
                Modifier.fillMaxWidth().height(Dimens.barraSuperior).padding(horizontal = Dimens.e4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (alVolver != null) {
                    IconButton(onClick = alVolver) { Icono(Iconos.atras, "Volver") }
                } else {
                    Spacer(Modifier.width(Dimens.e12))
                }
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = Dimens.e4).semantics { heading() },
                )
                if (alAyuda != null) {
                    IconButton(onClick = alAyuda) { Icono(Iconos.ayuda, "Ayuda sobre $titulo") }
                }
            }
            if (cargando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            } else {
                Spacer(Modifier.fillMaxWidth().height(4.dp))
            }
        }
    }
}

/** Para pantallas sin barra (el menú): solo el espacio de la barra de estado. */
@Composable
fun EspacioBarraEstado(color: Color = Color.Transparent) {
    Spacer(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)))
}
