package com.numsolve.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Sistema tipográfico "Developer Mono": una familia sans-serif (IBM Plex Sans /
 * equivalente de sistema) para texto de interfaz, y una familia monoespaciada
 * (JetBrains Mono / equivalente de sistema) para todo valor numérico -campos,
 * tabla de iteraciones y resultado final- de forma que las cifras siempre
 * queden alineadas en columnas, como en una hoja de cálculo.
 *
 * Nota: para producción se recomienda empaquetar las fuentes reales como
 * recursos de Compose (composeResources/font) usando Font(Res.font.xxx) y
 * FontFamily(...). Aquí se referencian mediante FontFamily.Default /
 * FontFamily.Monospace como fallback multiplataforma sin dependencias extra,
 * y quedan aisladas en dos constantes para reemplazarlas en un solo lugar.
 */
val UiFontFamily: FontFamily = FontFamily.Default
val NumericFontFamily: FontFamily = FontFamily.Monospace

val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelSmall = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.3.sp)
)

/** Estilo específico para cifras (tabla de iteraciones, resultado final, badges de error %). */
val NumericTextStyle = TextStyle(
    fontFamily = NumericFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
)

val NumericHeaderTextStyle = TextStyle(
    fontFamily = UiFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp
)