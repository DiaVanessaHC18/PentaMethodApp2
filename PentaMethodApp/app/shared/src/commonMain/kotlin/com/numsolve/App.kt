package com.numsolve

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.numsolve.ui.screens.SolverScreen
import com.numsolve.ui.theme.NumSolveTheme

@Composable
@Preview
fun App() {
    NumSolveTheme {
        SolverScreen()
    }
}