package com.example.perudo.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perudo.DiceViewModel
import com.example.perudo.ui.components.DiceSummaryView
import com.example.perudo.ui.components.DiceView

@Composable
fun DiceScreen(
    viewModel: DiceViewModel = viewModel()
) {
    val dice by viewModel.dice.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val summary by viewModel.diceSummary.collectAsState()

    // Infinite rotation for the refresh icon when rolling
    val infiniteTransition = rememberInfiniteTransition(label = "iconRollTransition")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotation"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!isRolling) viewModel.addDie() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Dado")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tocca un dado per rimuoverlo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "Totale dadi: ${dice.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid of dice
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (dice.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun dado. Premi '+' per aggiungerne uno!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 80.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(dice, key = { _, die -> die.id }) { index, die ->
                            Box(
                                modifier = Modifier.animateItem()
                            ) {
                                DiceView(
                                    die = die,
                                    isRolling = isRolling,
                                    staggerIndex = index,
                                    onClickToRemove = { viewModel.removeDie(die.id) }
                                )
                            }
                        }
                    }
                }
            }

            if (dice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                DiceSummaryView(summary = summary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Roll Button
            Button(
                onClick = { viewModel.rollDice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = dice.isNotEmpty() && !isRolling
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isRolling) iconRotation else 0f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRolling) "LANCIANDO..." else "LANCIA I DADI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
