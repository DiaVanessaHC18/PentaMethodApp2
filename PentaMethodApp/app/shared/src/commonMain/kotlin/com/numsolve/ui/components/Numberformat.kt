package com.numsolve.ui.components

import kotlin.math.abs
import kotlin.math.round

/** Formatea un Double con [decimals] decimales fijos, útil para columnas alineadas tipo Excel. */
internal fun formatFixed(value: Double, decimals: Int = 6): String {
    if (value.isNaN()) return "—"
    if (value.isInfinite()) return if (value > 0) "+∞" else "-∞"
    val factor = generateSequence(1.0) { it * 10 }.take(decimals + 1).last()
    val rounded = round(value * factor) / factor
    // Evita el "-0.000000"
    val normalized = if (rounded == 0.0) 0.0 else rounded
    return formatWithDecimals(normalized, decimals)
}

internal fun formatPercent(value: Double?, decimals: Int = 4): String =
    if (value == null) "—" else "${formatFixed(value, decimals)} %"

/** Implementación manual (sin String.format, no disponible igual en todos los targets KMP). */
private fun formatWithDecimals(value: Double, decimals: Int): String {
    val negative = value < 0
    val absValue = abs(value)
    val factor = generateSequence(1L) { it * 10 }.take(decimals + 1).last()
    val scaled = round(absValue * factor).toLong()
    val intPart = scaled / factor
    val fracPart = scaled % factor
    val fracStr = fracPart.toString().padStart(decimals, '0')
    val sign = if (negative && (intPart != 0L || fracPart != 0L)) "-" else ""
    return if (decimals == 0) "$sign$intPart" else "$sign$intPart.$fracStr"
}