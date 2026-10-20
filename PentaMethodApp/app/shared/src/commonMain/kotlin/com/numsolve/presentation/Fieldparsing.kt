package com.numsolve.presentation

import kotlin.math.floor

/** Resultado de intentar convertir el texto de un campo a un Double válido. */
internal sealed class FieldParse {
    data class Value(val value: Double) : FieldParse()
    data class Error(val message: String) : FieldParse()
}

/**
 * Convierte el texto de un campo numérico a Double, aceptando coma o punto
 * decimal (útil para usuarios hispanohablantes) y validando límites básicos.
 */
internal fun parseNumericField(
    label: String,
    text: String,
    mustBePositive: Boolean = false,
    mustBeInteger: Boolean = false
): FieldParse {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return FieldParse.Error("$label es obligatorio.")

    val normalized = trimmed.replace(',', '.')
    val value = normalized.toDoubleOrNull()
        ?: return FieldParse.Error("$label debe ser un número válido.")

    if (mustBeInteger && value != floor(value)) {
        return FieldParse.Error("$label debe ser un número entero.")
    }
    if (mustBePositive && value <= 0.0) {
        return FieldParse.Error("$label debe ser mayor que 0.")
    }
    return FieldParse.Value(value)
}