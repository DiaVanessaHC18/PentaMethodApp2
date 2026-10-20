package com.numsolve.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Contenedor de error muy sutil, dentro de la misma paleta (tono cálido claro).
private val Color_ErrorContainer = Color(0xFFF7E7E2)
private val AppColorScheme = lightColorScheme(
    primary = AppColors.primary,
    onPrimary = AppColors.onPrimary,
    primaryContainer = PowderBlue,
    onPrimaryContainer = AppColors.primary,
    secondary = AppColors.secondary,
    onSecondary = AppColors.onSecondary,
    secondaryContainer = SandMist,
    onSecondaryContainer = AppColors.primary,
    background = AppColors.background,
    onBackground = AppColors.onSurface,
    surface = AppColors.background,
    onSurface = AppColors.onSurface,
    surfaceVariant = AppColors.surface,
    onSurfaceVariant = AppColors.onSurfaceMuted,
    outline = AppColors.border,
    outlineVariant = PowderBlue,
    error = AppColors.error,
    onError = PureWhite,
    errorContainer = Color_ErrorContainer,
    onErrorContainer = AppColors.error
)


@Composable
fun NumSolveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

/** Espaciado consistente en toda la app (escala de 4dp). */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}