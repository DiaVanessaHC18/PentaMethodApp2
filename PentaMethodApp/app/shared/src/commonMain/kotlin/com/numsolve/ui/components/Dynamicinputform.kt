package com.numsolve.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.numsolve.domain.math.ExpressionParser
import com.numsolve.domain.math.MathExpression
import com.numsolve.domain.math.ParseResult
import com.numsolve.domain.model.FormFieldState
import com.numsolve.domain.model.InputCategory
import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.model.SolverFormState
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.NumericFontFamily
import com.numsolve.ui.theme.Spacing


/**
 * Formulario que se reconstruye según [SolverFormState.category]. En todos
 * los casos el orden es: f(x) -> gráfica interactiva (para elegir a/b/x0/x1
 * tocando la curva) -> el resto de los parámetros.
 *  - INTERVAL      -> f(x), [gráfica], a, b, tolerancia, iteraciones máx.
 *  - INITIAL_POINT -> f(x), [gráfica], x0 [+ f'(x) opcional en Newton], tolerancia, iteraciones máx.
 *  - TWO_POINTS    -> f(x), [gráfica], x0, x1, tolerancia, iteraciones máx.
 *
 * En Punto Fijo no se pide g(x): la app arma g(x) = x − f(x)/f'(x0) sola en
 * cuanto tiene f(x) y x0 (ver [com.numsolve.app.presentation.SolverViewModel]).
 */
@Composable
fun DynamicInputForm(
    form: SolverFormState,
    onFunctionChange: (String) -> Unit,
    onDerivativeChange: (String) -> Unit,
    onLowerBoundChange: (String) -> Unit,
    onUpperBoundChange: (String) -> Unit,
    onInitialGuessChange: (String) -> Unit,
    onSecondGuessChange: (String) -> Unit,
    onToleranceChange: (String) -> Unit,
    onMaxIterationsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {

        LabeledField(
            label = form.functionLabel,
            helper = "Ej: x^3 + 2*x^2 + 10*x - 20",
            field = form.functionExpression,
            onValueChange = onFunctionChange,
            monospace = true
        )

        val parsedFx: MathExpression? = remember(form.functionExpression.text) {
            (ExpressionParser.parse(form.functionExpression.text) as? ParseResult.Success)?.expression
        }

        fun textToDouble(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
        fun doubleToText(value: Double): String {
            val rounded = kotlin.math.round(value * 10000) / 10000
            return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
        }

        val graphTargets = when (form.category) {
            InputCategory.INTERVAL -> listOf(
                GraphTarget("a", textToDouble(form.lowerBound.text), { onLowerBoundChange(doubleToText(it)) }, AppColors.secondary),
                GraphTarget("b", textToDouble(form.upperBound.text), { onUpperBoundChange(doubleToText(it)) }, AppColors.warning)
            )
            InputCategory.INITIAL_POINT -> listOf(
                GraphTarget("x0", textToDouble(form.initialGuess.text), { onInitialGuessChange(doubleToText(it)) }, AppColors.secondary)
            )
            InputCategory.TWO_POINTS -> listOf(
                GraphTarget("x0", textToDouble(form.initialGuess.text), { onInitialGuessChange(doubleToText(it)) }, AppColors.secondary),
                GraphTarget("x1", textToDouble(form.secondGuess.text), { onSecondGuessChange(doubleToText(it)) }, AppColors.warning)
            )
        }

        InteractiveFunctionGraph(f = parsedFx, targets = graphTargets)

        if (form.method == NumericMethod.FIXED_POINT) {
        }

        when (form.category) {
            InputCategory.INTERVAL -> {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    LabeledField(
                        label = "Límite inferior (a)",
                        field = form.lowerBound,
                        onValueChange = onLowerBoundChange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        monospace = true
                    )
                    LabeledField(
                        label = "Límite superior (b)",
                        field = form.upperBound,
                        onValueChange = onUpperBoundChange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        monospace = true
                    )
                }
            }

            InputCategory.INITIAL_POINT -> {
                LabeledField(
                    label = "Valor inicial (x0)",
                    field = form.initialGuess,
                    onValueChange = onInitialGuessChange,
                    keyboardType = KeyboardType.Number,
                    monospace = true
                )
                if (form.method == NumericMethod.NEWTON_RAPHSON) {
                    LabeledField(
                        label = "f'(x) — opcional",
                        helper = "Si la dejas vacía, se calcula f'(x) por diferenciación numérica.",
                        field = form.derivativeExpression,
                        onValueChange = onDerivativeChange,
                        monospace = true
                    )
                }
            }

            InputCategory.TWO_POINTS -> {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    LabeledField(
                        label = "x0",
                        field = form.initialGuess,
                        onValueChange = onInitialGuessChange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        monospace = true
                    )
                    LabeledField(
                        label = "x1",
                        field = form.secondGuess,
                        onValueChange = onSecondGuessChange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        monospace = true
                    )
                }
                Text(
                    text = "La Secante necesita dos aproximaciones iniciales distintas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.onSurfaceMuted
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            LabeledField(
                label = "Tolerancia (%)",
                field = form.tolerance,
                onValueChange = onToleranceChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                monospace = true
            )
            LabeledField(
                label = "Máx. iteraciones",
                field = form.maxIterations,
                onValueChange = onMaxIterationsChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                monospace = true
            )
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    field: FormFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    helper: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    monospace: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = field.text,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = field.hasError,
            textStyle = if (monospace) LocalTextStyle.current.copy(fontFamily = NumericFontFamily) else LocalTextStyle.current,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            supportingText = {
                val message = field.errorMessage ?: helper
                if (message != null) {
                    Text(
                        text = message,
                        color = if (field.hasError) AppColors.error else AppColors.onSurfaceMuted
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.secondary,
                unfocusedBorderColor = AppColors.border,
                errorBorderColor = AppColors.error,
                focusedLabelColor = AppColors.secondary,
                cursorColor = AppColors.secondary
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
    }
}