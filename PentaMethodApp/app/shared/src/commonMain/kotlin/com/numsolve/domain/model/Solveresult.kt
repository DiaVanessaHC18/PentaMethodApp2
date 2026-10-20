package com.numsolve.domain.model

/**
 * Resultado de una validación previa (pre-cálculo). Distingue entre errores que
 * bloquean el cálculo (BLOCKING) y advertencias que permiten continuar bajo
 * responsabilidad del usuario (WARNING), como el caso de Punto Fijo con
 * |g'(x0)| >= 1, donde el enunciado puede pedir explícitamente intentarlo igual.
 */
sealed class ValidationIssue {
    abstract val message: String

    data class Blocking(override val message: String) : ValidationIssue()
    data class Warning(override val message: String) : ValidationIssue()
}

data class ValidationReport(
    val issues: List<ValidationIssue> = emptyList()
) {
    val blockingIssues: List<ValidationIssue.Blocking> = issues.filterIsInstance<ValidationIssue.Blocking>()
    val warnings: List<ValidationIssue.Warning> = issues.filterIsInstance<ValidationIssue.Warning>()
    val canProceed: Boolean get() = blockingIssues.isEmpty()
    val isClean: Boolean get() = issues.isEmpty()
}

enum class StopReason {
    TOLERANCE_MET,
    MAX_ITERATIONS_REACHED,
    EXACT_ROOT_FOUND,
    NUMERIC_FAILURE
}

data class SolveResult(
    val method: NumericMethod,
    val rows: List<IterationRow>,
    val root: Double?,
    val finalError: Double?,
    val iterationsUsed: Int,
    val stopReason: StopReason,
    val failureMessage: String? = null
) {
    val converged: Boolean get() = stopReason == StopReason.TOLERANCE_MET || stopReason == StopReason.EXACT_ROOT_FOUND
}

/** Envoltorio de todo lo que produce un cálculo: validación + (opcional) resultado. */
data class SolveOutcome(
    val validation: ValidationReport,
    val result: SolveResult?
)