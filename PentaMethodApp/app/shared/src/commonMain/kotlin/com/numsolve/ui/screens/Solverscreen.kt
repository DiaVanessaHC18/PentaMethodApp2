package com.numsolve.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.numsolve.app.presentation.SolverViewModel
import com.numsolve.ui.components.DynamicInputForm
import com.numsolve.app.ui.components.IterationTable
import com.numsolve.app.ui.components.MethodSelector
import com.numsolve.ui.components.MethodVisual
import com.numsolve.ui.components.ResultSummaryCard
import com.numsolve.ui.components.ValidationBanner
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.NumericFontFamily
import com.numsolve.ui.theme.Spacing


@Composable
fun SolverScreen(viewModel: SolverViewModel = remember { SolverViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.form

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PentaMethod", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            MethodSelector(
                selected = form.method,
                onMethodSelected = viewModel::onMethodSelected
            )

            SectionCard(title = "Datos de entrada") {
                Text(
                    text = form.method.formulaLatexLike,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = NumericFontFamily),
                    color = AppColors.secondary
                )
                Spacer(Modifier.height(Spacing.md))
                // f(x) -> gráfica interactiva (toca la curva para fijar a/b/x0/x1) -> resto de parámetros.
                DynamicInputForm(
                    form = form,
                    onFunctionChange = viewModel::onFunctionChange,
                    onDerivativeChange = viewModel::onDerivativeChange,
                    onLowerBoundChange = viewModel::onLowerBoundChange,
                    onUpperBoundChange = viewModel::onUpperBoundChange,
                    onInitialGuessChange = viewModel::onInitialGuessChange,
                    onSecondGuessChange = viewModel::onSecondGuessChange,
                    onToleranceChange = viewModel::onToleranceChange,
                    onMaxIterationsChange = viewModel::onMaxIterationsChange
                )
            }

            ValidationBanner(report = uiState.preValidation)

            Button(
                onClick = viewModel::calculate,
                enabled = !uiState.isCalculating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.secondary,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isCalculating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Calcular", style = MaterialTheme.typography.titleMedium)
                }
            }

            val result = uiState.result
            if (uiState.hasCalculated && result != null) {
                ResultSummaryCard(result = result)

                if (result.rows.isNotEmpty()) {
                    SectionCard(title = "Tabla de iteraciones") {
                        IterationTable(method = form.method, rows = result.rows)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, MaterialTheme.shapes.large)
            .border(1.dp, AppColors.border, MaterialTheme.shapes.large)
            .padding(Spacing.lg)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.primary)
        Spacer(Modifier.height(Spacing.md))
        content()
    }
}
