package com.example.perudo.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * Schermata principale dell'applicazione Perudo che ospita la griglia dei dadi,
 * il pannello di riepilogo, il pulsante di lancio animato e il dialogo con le regole del gioco.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: DiceViewModel = viewModel()
) {
    // Osserva gli stati reattivi provenienti dal ViewModel
    val dice by viewModel.dice.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val summary by viewModel.diceSummary.collectAsState()

    // Stato per gestire la visibilità del dialogo con le regole del Perudo
    var showRulesDialog by remember { mutableStateOf(false) }

    // Transizione infinita per animare la rotazione dell'icona di ricarica durante il lancio
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perudo",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    // Pulsante in alto a sinistra per alternare tra Light Mode e Dark Mode
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = "Cambia Modalità Tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Pulsante per aprire il regolamento del gioco
                    IconButton(onClick = { showRulesDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Regole del Gioco",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Pulsante fluttuante "+" per aggiungere un nuovo dado
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

            // Griglia dinamica dei dadi che si adatta allo spazio disponibile
            BoxWithConstraints(
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
                    // Determina il numero ottimale di colonne in base alla quantità di dadi
                    val numColumns = when {
                        dice.size <= 1 -> 1
                        dice.size == 2 -> 2
                        dice.size == 3 -> 3
                        dice.size == 4 -> if (maxWidth > 500.dp) 4 else 2
                        (dice.size in 5..6) -> if (maxWidth > 500.dp) 4 else 3
                        else -> 4
                    }

                    // Scala la dimensione dei dadi dinamicamente in base alle colonne
                    val calculatedSize = when (numColumns) {
                        1 -> 130.dp
                        2 -> 108.dp
                        3 -> 92.dp
                        else -> 76.dp
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(numColumns),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                    ) {
                        itemsIndexed(dice, key = { _, die -> die.id }) { index, die ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                contentAlignment = Alignment.Center
                            ) {
                                DiceView(
                                    die = die,
                                    isRolling = isRolling,
                                    staggerIndex = index,
                                    onClickToRemove = { viewModel.removeDie(die.id) },
                                    size = calculatedSize
                                )
                            }
                        }
                    }
                }
            }

            // Mostra il riepilogo del Perudo se ci sono dadi
            if (dice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                DiceSummaryView(summary = summary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante principale per avviare il lancio dei dadi
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

    // Dialogo pop-up con le regole complete del Perudo
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Regole del Perudo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎲 Il Dado 1 (Jolly)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Gli 1 valgono come Jolly e contano come qualsiasi numero! Quando scommetti su un valore (es. i 5), sommi sia i 5 naturali sia tutti i Jolly (1).\n• Non si può iniziare un round sugli 1",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🗣️ Le Dichiarazioni e Rilanci",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "I giocatori scommettono a turno sul numero minimo di dadi di un certo valore presenti complessivamente sul tavolo tra tutti i giocatori. A ogni turno puoi rilanciare aumentando la quantità o il valore del dado.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "⚡ Dubito!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Se ritieni che la scommessa precedente sia falsa, dici \"Dubito!\". Si rivelano tutti i dadi: se la quantità reale (inclusi i Jolly) è inferiore alla chiamata, lo scommettitore perde un dado; altrimenti perde un dado chi ha dubitato.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🧦 La Calza (Chiamata Esatta)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Se non sei di turno (non stai dichiarando né dubitando), puoi interrompere dicendo \"Calzo!\" se credi che la scommessa corrente sia ESATTAMENTE pari al numero reale di dadi sul tavolo.\n• Se indovini vinci un dado e si conclude la manche, la successiva riparte da te.\n• Se sbagli: perdi 1 dado e si conclude la manche, la successiva riparte da te.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🏛️ Il Palafico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quando un giocatore rimane per la prima φορά con un SOLO dado, la manche successiva è una manche di Palafico:\n• Gli 1 NON sono Jolly (valgono soltanto come 1).\n• Il giocatore in Palafico decide il valore del dado scommesso; gli altri giocatori possono solo aumentare la quantità mantenendo lo stesso dado.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("Ho capito", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
