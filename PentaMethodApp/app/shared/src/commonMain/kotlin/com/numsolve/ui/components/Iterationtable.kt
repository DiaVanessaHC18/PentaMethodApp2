package com.numsolve.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.numsolve.domain.model.IterationRow
import com.numsolve.domain.model.NumericMethod
import com.numsolve.ui.theme.AppColors
import com.numsolve.ui.theme.NumericHeaderTextStyle
import com.numsolve.ui.theme.NumericTextStyle
import com.numsolve.ui.theme.SandMist
import com.numsolve.ui.components.formatFixed
import com.numsolve.ui.components.formatPercent

/**
 * Tabla de iteraciones "estilo Excel": encabezado fijo, filas alternas,
 * cifras en fuente monoespaciada alineadas por columna y las mismas
 * cabeceras que las plantillas de hoja de cálculo de referencia:
 *
 *  - Bisección / Regula Falsi -> i | a | b | xr | f(a) | f(xr) | f(b) | Ea (%)
 *  - Punto Fijo / Secante     -> i | xi | f(xi) | Ea (%)
 *  - Newton-Raphson           -> i | xi | f(xi) | Ea (%)
 */
@Composable
fun IterationTable(method: NumericMethod, rows: List<IterationRow>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AppColors.border, RoundedCornerShape(14.dp))
    ) {
        val scrollState = rememberScrollState()
        Column(Modifier.horizontalScroll(scrollState)) {
            TableHeader(method)
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(rows) { index, row ->
                    TableRow(method = method, row = row, zebra = index % 2 == 1)
                }
            }
        }
    }
}

private val ColInt = 56.dp
private val ColNum = 128.dp
private val ColErr = 108.dp
private val RowPadV = 10.dp
private val RowPadH = 14.dp

@Composable
private fun TableHeader(method: NumericMethod) {
    Row(
        modifier = Modifier
            .background(AppColors.primary)
            .padding(vertical = RowPadV),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (method) {
            NumericMethod.BISECTION, NumericMethod.REGULA_FALSI -> {
                HeaderCell("i", ColInt)
                HeaderCell("ai", ColNum)
                HeaderCell("bi", ColNum)
                HeaderCell("mi", ColNum)
                HeaderCell("f(ai)", ColNum)
                HeaderCell("f(mi)", ColNum)
                HeaderCell("f(bi)", ColNum)
                HeaderCell("Ei (%)", ColErr)
            }
            NumericMethod.NEWTON_RAPHSON, NumericMethod.FIXED_POINT, NumericMethod.SECANT -> {
                HeaderCell("i", ColInt)
                HeaderCell("xi", ColNum)
                HeaderCell("f(xi)", ColNum)
                HeaderCell("Ei (%)", ColErr)
            }

        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.width(width).padding(horizontal = RowPadH), contentAlignment = Alignment.CenterStart) {
        Text(text = text, style = NumericHeaderTextStyle, color = Color.White)
    }
}

@Composable
private fun TableRow(method: NumericMethod, row: IterationRow, zebra: Boolean) {
    val bg = if (zebra) SandMist else Color.White
    Row(
        modifier = Modifier
            .background(bg)
            .padding(vertical = RowPadV),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (row) {
            is IterationRow.IntervalIterationRow -> {
                NumCell(row.i.toString(), ColInt, bold = true)
                NumCell(formatFixed(row.ai), ColNum)
                NumCell(formatFixed(row.bi), ColNum)
                NumCell(formatFixed(row.mi), ColNum, bold = true)
                SignedNumCell(row.fai, ColNum)
                SignedNumCell(row.fmi, ColNum)
                SignedNumCell(row.fbi, ColNum)
                NumCell(formatPercent(row.errorPercent), ColErr, color = AppColors.secondary)
            }
            is IterationRow.PointIterationRow -> {
                NumCell(row.i.toString(), ColInt, bold = true)
                NumCell(formatFixed(row.xi), ColNum, bold = true)
                if (method in listOf(NumericMethod.NEWTON_RAPHSON, NumericMethod.FIXED_POINT, NumericMethod.SECANT)) {
                    if (row.fxi != null) SignedNumCell(row.fxi, ColNum) else NumCell("—", ColNum)
                }
                NumCell(formatPercent(row.errorPercent), ColErr, color = AppColors.secondary)
            }
        }
    }
}

@Composable
private fun NumCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false,
    color: Color = AppColors.onSurface
) {
    Box(modifier = Modifier.width(width).padding(horizontal = RowPadH), contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            style = NumericTextStyle.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal),
            color = color
        )
    }
}

@Composable
private fun SignedNumCell(value: Double, width: androidx.compose.ui.unit.Dp) {
    val color = when {
        value > 0 -> AppColors.success
        value < 0 -> AppColors.error
        else -> AppColors.onSurface
    }
    NumCell(formatFixed(value), width, color = color)
}