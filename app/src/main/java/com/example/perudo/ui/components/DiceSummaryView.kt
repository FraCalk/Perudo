package com.example.perudo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perudo.DiceSummary
import com.example.perudo.ValueCountInfo

/**
 * Componente visivo che mostra il riepilogo statistico e strategico dei dadi per il Perudo.
 * Include il conteggio dei Jolly (1), le combinazioni/coppie e il breakdown per ciascun valore.
 */
@Composable
fun DiceSummaryView(
    summary: DiceSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Sezione Jolly (Valore 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Jolly",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Jolly (Dado da 1)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Valgono per qualsiasi numero",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = summary.jollyCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Sezione Combinazioni e Coppie (incluse combinazioni con i Jolly)
            Text(
                text = "Coppie e Combinazioni (inclusi Jolly)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            if (summary.combinations.isEmpty()) {
                val emptyMessage = if (summary.jollyCount >= 2) {
                    "Hai ${summary.jollyCount} Jolly: puoi formare una Coppia con qualsiasi numero!"
                } else {
                    "Nessuna coppia o combinazione presente"
                }
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    summary.combinations.forEach { combo ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${combo.typeName} da ${combo.value}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${combo.description})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Sezione Breakdown del Conteggio per ciascun valore (da 2 a 6)
            Text(
                text = "Conteggio Totale (Naturali)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                summary.valueBreakdown.forEach { info ->
                    ValueCountTile(
                        info = info,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Singolo riquadro per mostrare il conteggio di un determinato valore di dado.
 */
@Composable
private fun ValueCountTile(
    info: ValueCountInfo,
    modifier: Modifier = Modifier
) {
    val isHighlighted = info.naturalCount > 0

    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(36.dp),
            border = BorderStroke(
                1.dp,
                if (isHighlighted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                MiniDicePips(
                    value = info.value,
                    color = if (isHighlighted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${info.naturalCount}",
            fontSize = 12.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * Disegna i pallini miniaturizzati per i riquadri di riepilogo.
 */
@Composable
private fun MiniDicePips(
    value: Int,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp).padding(3.dp)) {
        val radius = size.minDimension / 10f
        val width = size.width
        val height = size.height

        val left = width * 0.25f
        val centerX = width * 0.5f
        val right = width * 0.75f

        val top = height * 0.25f
        val centerY = height * 0.5f
        val bottom = height * 0.75f

        fun drawPip(x: Float, y: Float) {
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x, y)
            )
        }

        when (value) {
            2 -> {
                drawPip(left, top)
                drawPip(right, bottom)
            }
            3 -> {
                drawPip(left, top)
                drawPip(centerX, centerY)
                drawPip(right, bottom)
            }
            4 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            5 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(centerX, centerY)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            6 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, centerY)
                drawPip(right, centerY)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
        }
    }
}
