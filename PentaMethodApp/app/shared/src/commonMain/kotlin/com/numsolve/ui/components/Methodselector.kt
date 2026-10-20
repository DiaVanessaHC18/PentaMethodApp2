package com.numsolve.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.numsolve.domain.model.NumericMethod
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.Spacing

/**
 * Selector principal de método numérico. Usa un ExposedDropdownMenuBox
 * (patrón estándar Material 3) con una vista previa clara del método activo
 * y su categoría de entrada, para que el usuario sepa qué formulario esperar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodSelector(
    selected: NumericMethod,
    onMethodSelected: (NumericMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Método numérico",
            style = MaterialTheme.typography.labelLarge,
            color = AppColors.onSurfaceMuted
        )
        Spacer(Modifier.height(Spacing.xs))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.primary)
                    .border(BorderStroke(1.dp, AppColors.primary), RoundedCornerShape(14.dp))
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = selected.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Elegir método",
                    tint = Color.White
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                NumericMethod.all.forEach { method ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = method.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (method == selected) AppColors.primary else AppColors.onSurface
                                )
                            }
                        },
                        onClick = {
                            onMethodSelected(method)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (method == selected) AppColors.surface else Color.White
                        )
                    )
                    if (method != NumericMethod.all.last()) {
                        HorizontalDivider(color = AppColors.border, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}