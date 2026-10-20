package com.numsolve.domain.model

/**
 * Estado crudo (texto) del formulario dinámico. Se guarda como String para permitir
 * edición fluida en los TextField de Compose; se parsea/valida al momento de calcular
 * y también de forma incremental para mostrar errores en línea.
 */
data class FormFieldState(
    val text: String = "",
    val errorMessage: String? = null
) {
    val hasError: Boolean get() = errorMessage != null
}

/**
 * Agrupa todos los campos posibles del formulario. Solo se muestran/exigen los campos
 * relevantes a la categoría de entrada (InputCategory) del método seleccionado.
 */
data class SolverFormState(
    val method: NumericMethod = NumericMethod.BISECTION,

    // Función original f(x) para todos los métodos
    val functionExpression: FormFieldState = FormFieldState(),

    // Derivada opcional para Newton-Raphson; si se deja vacía se deriva numéricamente
    val derivativeExpression: FormFieldState = FormFieldState(),

    // Métodos de intervalo: Bisección, Regula Falsi
    val lowerBound: FormFieldState = FormFieldState(),   // a
    val upperBound: FormFieldState = FormFieldState(),   // b

    // Métodos de punto inicial: Newton-Raphson, Punto Fijo
    val initialGuess: FormFieldState = FormFieldState(), // x0

    // Secante: segundo punto inicial
    val secondGuess: FormFieldState = FormFieldState(),  // x1

    val tolerance: FormFieldState = FormFieldState(text = "0.0001"),
    val maxIterations: FormFieldState = FormFieldState(text = "50")
) {
    val category: InputCategory get() = method.inputCategory

    /**
     * Siempre f(x): incluso en Punto Fijo el usuario solo ingresa f(x); la app
     * construye g(x) automáticamente a partir de f(x) y x0 (ver [SolverViewModel]).
     */
    val functionLabel: String get() = "f(x)"
}