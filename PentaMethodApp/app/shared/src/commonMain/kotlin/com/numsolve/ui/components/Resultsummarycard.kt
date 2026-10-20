package com.numsolve.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.numsolve.domain.model.SolveResult
import com.numsolve.domain.model.StopReason
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.NumericFontFamily
import com.numsolve.ui.theme.Spacing

@Composable
fun ResultSummaryCard(result: SolveResult, modifier: Modifier = Modifier) {
    if (result.stopReason == StopReason.NUMERIC_FAILURE) {
        FailureCard(result.failureMessage ?: "No se pudo completar el cálculo.", modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.primary, RoundedCornerShape(16.dp))
            .padding(Spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = if (result.converged) "Convergencia alcanzada" else "Límite de iteraciones alcanzado",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
        Spacer(Modifier.height(Spacing.md))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryStat("Raíz aproximada", result.root?.let { formatFixed(it, 8) } ?: "—")
            SummaryStat("Iteraciones", result.iterationsUsed.toString())
            SummaryStat("Error final", formatPercent(result.finalError))
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFD8E3EA))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = NumericFontFamily),
            color = Color.White
        )
    }
}

@Composable
private fun FailureCard(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7E7E2), RoundedCornerShape(16.dp))
            .padding(Spacing.lg),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = AppColors.error)
        Spacer(Modifier.width(Spacing.sm))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = AppColors.error)
    }
}