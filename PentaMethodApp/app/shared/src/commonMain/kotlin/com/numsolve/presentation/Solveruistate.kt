package com.numsolve.app.presentation

import com.numsolve.domain.model.NumericMethod
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.SolverFormState
import com.numsolve.domain.model.ValidationReport

data class SolverUiState(
    val form: SolverFormState = SolverFormState(),
    val preValidation: ValidationReport = ValidationReport(),
    val result: SolveResult? = null,
    val isCalculating: Boolean = false,
    val hasCalculated: Boolean = false
) {
    val selectedMethod: NumericMethod get() = form.method
}