package com.numsolve

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    val windowState = WindowState(width = 480.dp, height = 900.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "PentaMethodApp",
        state = windowState
    ) {
        App()
    }
}