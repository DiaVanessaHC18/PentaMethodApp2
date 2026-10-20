package com.numsolve.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta base solicitada
val InkNavy = Color(0xFF20214F)     // texto principal, headers, botones primarios
val SteelBlue = Color(0xFF204F63)   // acentos, elementos interactivos secundarios
val SandMist = Color(0xFFF5EFEB)    // fondo de superficies/cards, filas alternas de tabla
val PureWhite = Color(0xFFFFFFFF)   // fondo base, texto sobre color oscuro
val PowderBlue = Color(0xFFC8D9E6)  // bordes, separadores, chips inactivos, fondo de inputs

// Roles semánticos derivados (mismo mundo cromático, no colores nuevos "de sistema")
val SuccessGreen = Color(0xFF3E7A4C)
val WarningAmber = Color(0xFFB07A1E)
val ErrorRed = Color(0xFFB3401F)

object AppColors {
    val primary = InkNavy
    val onPrimary = PureWhite
    val secondary = SteelBlue
    val onSecondary = PureWhite
    val background = PureWhite
    val surface = SandMist
    val surfaceVariant = PowderBlue
    val onSurface = InkNavy
    val onSurfaceMuted = SteelBlue
    val border = PowderBlue
    val success = SuccessGreen
    val warning = WarningAmber
    val error = ErrorRed
}