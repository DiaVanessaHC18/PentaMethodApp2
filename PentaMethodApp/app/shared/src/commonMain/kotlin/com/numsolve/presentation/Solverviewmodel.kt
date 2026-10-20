package com.numsolve.app.presentation

import com.numsolve.domain.math.ExpressionParser
import com.numsolve.domain.math.MathExpression
import com.numsolve.domain.math.ParseResult

import com.numsolve.app.domain.solver.SolverFacade
import com.numsolve.domain.math.NumericDerivative
import com.numsolve.domain.model.FormFieldState
import com.numsolve.domain.model.InputCategory
import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.SolverFormState
import com.numsolve.domain.model.StopReason
import com.numsolve.domain.model.ValidationIssue
import com.numsolve.domain.model.ValidationReport
import com.numsolve.domain.solver.FixedPointParams
import com.numsolve.domain.solver.IntervalParams
import com.numsolve.domain.solver.NewtonParams
import com.numsolve.domain.solver.SecantParams
import com.numsolve.presentation.FieldParse
import com.numsolve.presentation.parseNumericField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs


/**
 * Contenedor de estado y lógica de presentación para la pantalla de resolución.
 * No depende de ningún framework de UI: puede reutilizarse igual en
 * Compose Desktop, Android o iOS.
 */
class SolverViewModel(
    private val facade: SolverFacade = SolverFacade()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(SolverUiState())
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    fun onMethodSelected(method: NumericMethod) {
        _uiState.update {
            it.copy(
                form = it.form.copy(method = method),
                result = null,
                hasCalculated = false
            )
        }
        refreshPreValidation()
    }

    fun onFunctionChange(text: String) = updateForm { it.copy(functionExpression = FormFieldState(text)) }
    fun onDerivativeChange(text: String) = updateForm { it.copy(derivativeExpression = FormFieldState(text)) }
    fun onLowerBoundChange(text: String) = updateForm { it.copy(lowerBound = FormFieldState(text)) }
    fun onUpperBoundChange(text: String) = updateForm { it.copy(upperBound = FormFieldState(text)) }
    fun onInitialGuessChange(text: String) = updateForm { it.copy(initialGuess = FormFieldState(text)) }
    fun onSecondGuessChange(text: String) = updateForm { it.copy(secondGuess = FormFieldState(text)) }
    fun onToleranceChange(text: String) = updateForm { it.copy(tolerance = FormFieldState(text)) }
    fun onMaxIterationsChange(text: String) = updateForm { it.copy(maxIterations = FormFieldState(text)) }

    private fun updateForm(mutate: (SolverFormState) -> SolverFormState) {
        _uiState.update { it.copy(form = mutate(it.form), result = null, hasCalculated = false) }
        refreshPreValidation()
    }

    /**
     * Ejecuta una validación "en vivo" (convergencia, cambio de signo, derivada
     * no nula, etc.) apenas los campos necesarios sean parseables, sin esperar
     * a que el usuario presione "Calcular". Los errores de formato en sí se
     * muestran por campo mediante [validateFieldsAndBuildParams].
     */
    private fun refreshPreValidation() {
        scope.launch {
            val form = _uiState.value.form
            val report = buildPreValidationReport(form)
            _uiState.update { it.copy(preValidation = report) }
        }
    }

    private fun buildPreValidationReport(form: SolverFormState): ValidationReport {
        val fx = (ExpressionParser.parse(form.functionExpression.text) as? ParseResult.Success)?.expression
            ?: return ValidationReport()

        return try {
            when (form.method) {
                NumericMethod.BISECTION, NumericMethod.REGULA_FALSI -> {
                    val a = form.lowerBound.text.trim().replace(',', '.').toDoubleOrNull()
                    val b = form.upperBound.text.trim().replace(',', '.').toDoubleOrNull()
                    val tol = form.tolerance.text.trim().replace(',', '.').toDoubleOrNull()
                    val maxIt = form.maxIterations.text.trim().toIntOrNull()
                    if (a == null || b == null || tol == null || maxIt == null) return ValidationReport()
                    val params = IntervalParams(fx, a, b, tol, maxIt)
                    facade.validateInterval(form.method, params)
                }
                NumericMethod.FIXED_POINT -> {
                    val x0 = form.initialGuess.text.trim().replace(',', '.').toDoubleOrNull()
                    val tol = form.tolerance.text.trim().replace(',', '.').toDoubleOrNull()
                    val maxIt = form.maxIterations.text.trim().toIntOrNull()
                    if (x0 == null || tol == null || maxIt == null) return ValidationReport()
                    val slope = safeSlopeAt(fx, x0)
                    if (slope == null || abs(slope) < 1e-10) {
                        return ValidationReport(
                            listOf(
                                ValidationIssue.Blocking(
                                    "f'(x0) ≈ 0 en x0 = $x0: la app no puede construir un punto fijo " +
                                            "automático ahí. Elige otro x0 tocando la gráfica."
                                )
                            )
                        )
                    }
                    val g = buildAutoFixedPointG(fx, slope)
                    facade.validateFixedPoint(FixedPointParams(g, fx, x0, tol, maxIt))
                }
                NumericMethod.NEWTON_RAPHSON -> {
                    val x0 = form.initialGuess.text.trim().replace(',', '.').toDoubleOrNull()
                    val tol = form.tolerance.text.trim().replace(',', '.').toDoubleOrNull()
                    val maxIt = form.maxIterations.text.trim().toIntOrNull()
                    if (x0 == null || tol == null || maxIt == null) return ValidationReport()
                    val derivativeText = form.derivativeExpression.text
                    val derivative: MathExpression? = if (derivativeText.isBlank()) null else
                        (ExpressionParser.parse(derivativeText) as? ParseResult.Success)?.expression
                    facade.validateNewton(NewtonParams(fx, derivative, x0, tol, maxIt))
                }
                NumericMethod.SECANT -> {
                    val x0 = form.initialGuess.text.trim().replace(',', '.').toDoubleOrNull()
                    val x1 = form.secondGuess.text.trim().replace(',', '.').toDoubleOrNull()
                    val tol = form.tolerance.text.trim().replace(',', '.').toDoubleOrNull()
                    val maxIt = form.maxIterations.text.trim().toIntOrNull()
                    if (x0 == null || x1 == null || tol == null || maxIt == null) return ValidationReport()
                    facade.validateSecant(SecantParams(fx, x0, x1, tol, maxIt))
                }
            }
        } catch (e: Exception) {
            ValidationReport()
        }
    }

    fun calculate() {
        val form = _uiState.value.form
        _uiState.update { it.copy(isCalculating = true) }

        scope.launch {
            val validatedForm = validateFields(form)
            val hasFieldErrors = listOf(
                validatedForm.functionExpression, validatedForm.derivativeExpression,
                validatedForm.lowerBound, validatedForm.upperBound,
                validatedForm.initialGuess, validatedForm.secondGuess,
                validatedForm.tolerance, validatedForm.maxIterations
            ).any { it.hasError }

            if (hasFieldErrors) {
                _uiState.update { it.copy(form = validatedForm, isCalculating = false) }
                return@launch
            }

            val result = runCatching { executeSolve(validatedForm) }.getOrNull()
            val preValidation = buildPreValidationReport(validatedForm)

            _uiState.update {
                it.copy(
                    form = validatedForm,
                    preValidation = preValidation,
                    result = result,
                    isCalculating = false,
                    hasCalculated = true
                )
            }
        }
    }

    /** Revalida cada campo relevante a la categoría actual y anota mensajes de error inline. */
    private fun validateFields(form: SolverFormState): SolverFormState {
        fun parseExprOrError(text: String, label: String): String? {
            if (text.isBlank()) return "$label es obligatorio."
            return when (val r = ExpressionParser.parse(text)) {
                is ParseResult.Failure -> r.message
                is ParseResult.Success -> null
            }
        }

        val functionError = parseExprOrError(form.functionExpression.text, form.functionLabel)
        val toleranceError = (parseNumericField("La tolerancia", form.tolerance.text, mustBePositive = true) as? FieldParse.Error)?.message
        val maxIterError = (parseNumericField("El máximo de iteraciones", form.maxIterations.text, mustBePositive = true, mustBeInteger = true) as? FieldParse.Error)?.message

        var lowerError: String? = null
        var upperError: String? = null
        var initialError: String? = null
        var secondError: String? = null
        var derivativeError: String? = null

        when (form.category) {
            InputCategory.INTERVAL -> {
                lowerError = (parseNumericField("El límite inferior (a)", form.lowerBound.text) as? FieldParse.Error)?.message
                upperError = (parseNumericField("El límite superior (b)", form.upperBound.text) as? FieldParse.Error)?.message
            }
            InputCategory.INITIAL_POINT -> {
                initialError = (parseNumericField("El valor inicial (x0)", form.initialGuess.text) as? FieldParse.Error)?.message
                if (form.method == NumericMethod.NEWTON_RAPHSON && form.derivativeExpression.text.isNotBlank()) {
                    derivativeError = parseExprOrError(form.derivativeExpression.text, "f'(x)")
                }
            }
            InputCategory.TWO_POINTS -> {
                initialError = (parseNumericField("x0", form.initialGuess.text) as? FieldParse.Error)?.message
                secondError = (parseNumericField("x1", form.secondGuess.text) as? FieldParse.Error)?.message
            }
        }

        return form.copy(
            functionExpression = form.functionExpression.copy(errorMessage = functionError),
            derivativeExpression = form.derivativeExpression.copy(errorMessage = derivativeError),
            lowerBound = form.lowerBound.copy(errorMessage = lowerError),
            upperBound = form.upperBound.copy(errorMessage = upperError),
            initialGuess = form.initialGuess.copy(errorMessage = initialError),
            secondGuess = form.secondGuess.copy(errorMessage = secondError),
            tolerance = form.tolerance.copy(errorMessage = toleranceError),
            maxIterations = form.maxIterations.copy(errorMessage = maxIterError)
        )
    }

    private fun executeSolve(form: SolverFormState) = with(form) {
        val fx = (ExpressionParser.parse(functionExpression.text) as ParseResult.Success).expression
        val tol = tolerance.text.trim().replace(',', '.').toDouble()
        val maxIt = maxIterations.text.trim().toInt()

        when (method) {
            NumericMethod.BISECTION, NumericMethod.REGULA_FALSI -> {
                val a = lowerBound.text.trim().replace(',', '.').toDouble()
                val b = upperBound.text.trim().replace(',', '.').toDouble()
                facade.solveInterval(method, IntervalParams(fx, a, b, tol, maxIt))
            }
            NumericMethod.FIXED_POINT -> {
                val x0 = initialGuess.text.trim().replace(',', '.').toDouble()
                val slope = safeSlopeAt(fx, x0)
                if (slope == null || abs(slope) < 1e-10) {
                    SolveResult(
                        method = NumericMethod.FIXED_POINT,
                        rows = emptyList(),
                        root = null,
                        finalError = null,
                        iterationsUsed = 0,
                        stopReason = StopReason.NUMERIC_FAILURE,
                        failureMessage = "f'(x0) ≈ 0 en x0 = $x0: la app no puede construir un punto fijo " +
                                "automático ahí. Elige otro x0 tocando la gráfica."
                    )
                } else {
                    val g = buildAutoFixedPointG(fx, slope)
                    facade.solveFixedPoint(FixedPointParams(g, fx, x0, tol, maxIt))
                }
            }
            NumericMethod.NEWTON_RAPHSON -> {
                val x0 = initialGuess.text.trim().replace(',', '.').toDouble()
                val derivative = if (derivativeExpression.text.isBlank()) null else
                    (ExpressionParser.parse(derivativeExpression.text) as? ParseResult.Success)?.expression
                facade.solveNewton(NewtonParams(fx, derivative, x0, tol, maxIt))
            }
            NumericMethod.SECANT -> {
                val x0 = initialGuess.text.trim().replace(',', '.').toDouble()
                val x1 = secondGuess.text.trim().replace(',', '.').toDouble()
                facade.solveSecant(SecantParams(fx, x0, x1, tol, maxIt))
            }
        }
    }

    /** f'(x0) numérica, o null si f no pudo evaluarse alrededor de x0. */
    private fun safeSlopeAt(f: MathExpression, x0: Double): Double? = try {
        NumericDerivative.centralDifference(f, x0)
    } catch (e: Exception) {
        null
    }

    /**
     * Construye g(x) = x − f(x)/f'(x0) automáticamente: como g'(x0) = 1 − f'(x0)/f'(x0) = 0,
     * la convergencia local en x0 queda garantizada sin que el usuario tenga que despejar x a mano.
     */
    private fun buildAutoFixedPointG(f: MathExpression, slopeAtX0: Double): MathExpression =
        MathExpression { x -> x - f.evaluate(x) / slopeAtX0 }

    fun dispose() {
        scope.launch { }.let { /* no-op: scope se cancela desde el owner de la pantalla si aplica */ }
    }
}
