package com.example.pentametod2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import com.example.pentametod2.catalogo.Catalogo
import com.example.pentametod2.ui.navegacion.MenuScreen
import com.example.pentametod2.ui.navegacion.Pantalla
import com.example.pentametod2.ui.navegacion.PantallaSaver
import com.example.pentametod2.ui.navegacion.anterior
import com.example.pentametod2.ui.resultados.MetodoScreen
import com.example.pentametod2.ui.tema.TemaMetodos

/**
 * Raíz de la app.
 *
 * [manejadorAtras] permite conectar el botón "atrás" del sistema desde la plataforma
 * (en commonMain no hay un BackHandler disponible en el classpath de compilación). Por ejemplo, en
 * Android: `AppMetodos { habilitado, alVolver -> BackHandler(habilitado, alVolver) }`.
 */
@Composable
fun AppMetodos(
    manejadorAtras: @Composable (habilitado: Boolean, alVolver: () -> Unit) -> Unit = { _, _ -> },
) {
    TemaMetodos {
        var pantalla by rememberSaveable(stateSaver = PantallaSaver) { mutableStateOf<Pantalla>(Pantalla.Menu) }
        // Conserva el estado (rememberSaveable) de cada método aunque se vuelva al menú.
        val estados = rememberSaveableStateHolder()
        val anterior = pantalla.anterior()
        manejadorAtras(anterior != null) { if (anterior != null) pantalla = anterior }

        when (val p = pantalla) {
            Pantalla.Menu -> MenuScreen(Catalogo.metodos()) { pantalla = Pantalla.Metodo(it) }
            is Pantalla.Metodo -> estados.SaveableStateProvider(p.id.name) {
                MetodoScreen(Catalogo.info(p.id), alVolver = { pantalla = Pantalla.Menu }, alAbrirMetodo = { pantalla = Pantalla.Metodo(it) })
            }
        }
    }
}
