package com.numsolve.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.numsolve.domain.model.ValidationIssue
import com.numsolve.domain.model.ValidationReport
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.Spacing

/**
 * Muestra las validaciones previas de convergencia (cambio de signo,
 * f'(x0) ≠ 0, |g'(x0)| < 1, etc.). Los errores bloqueantes se marcan en rojo
 * y evitan el cálculo; las advertencias en ámbar permiten continuar.
 */
@Composable
fun ValidationBanner(report: ValidationReport, modifier: Modifier = Modifier) {
    if (report.isClean) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        report.issues.forEach { issue -> IssueRow(issue) }
    }
}

@Composable
private fun IssueRow(issue: ValidationIssue) {
    val isBlocking = issue is ValidationIssue.Blocking
    val isPositive = issue.message.contains("✓")
    val (bg, fg, icon) = when {
        isBlocking -> Triple(Color(0xFFF7E7E2), AppColors.error, Icons.Filled.Error)
        isPositive -> Triple(Color(0xFFE7F0E9), AppColors.success, Icons.Filled.CheckCircle)
        else -> Triple(Color(0xFFF6EEDD), AppColors.warning, Icons.Filled.WarningAmber)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(
            text = issue.message,
            style = MaterialTheme.typography.bodySmall,
            color = fg
        )
    }
}