package com.example.perudo.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perudo.DiceViewModel
import com.example.perudo.ui.components.DiceSummaryView
import com.example.perudo.ui.components.DiceView
import com.example.perudo.ui.components.MultiplayerMenu

/**
 * Schermata principale dell'applicazione Perudo che ospita la griglia dei dadi,
 * il pannello di riepilogo, il pulsante di lancio animato e il dialogo con le regole del gioco.
 * @param darkTheme Tema attualmente scelto nell'Activity.
 * @param onToggleTheme Callback che richiede il cambio di tema.
 * @param viewModel Stato e operazioni sui dadi, ottenuti dal ViewModelStoreOwner corrente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: DiceViewModel = viewModel(),
) {
    // collectAsState converte i flussi in stato Compose: ogni emissione aggiorna
    // le parti della schermata che leggono quei valori.
    // Osserva gli stati reattivi provenienti dal ViewModel
    val dice by viewModel.dice.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val summary by viewModel.diceSummary.collectAsState()
    val totalMatchDice by viewModel.totalMatchDice.collectAsState()

    // remember conserva questi flag nelle ricomposizioni, ma non alla ricreazione dell'Activity.
    // Stato per gestire la visibilit? del dialogo con le regole del Perudo
    var showRulesDialog by remember { mutableStateOf(value = false) }
    // Stato per gestire la visibilità del dialogo di inserimento dadi totali
    var showMatchDialog by remember { mutableStateOf(value = false) }
    // La voce selezionata apre un avviso finche non sono presenti i flussi di rete.
    var selectedMultiplayerAction by remember { mutableStateOf<String?>(null) }

    // La transizione resta attiva mentre la schermata e composta; il suo angolo
    // viene applicato all'icona solo quando isRolling ? true.
    // Transizione infinita per animare la rotazione dell'icona di ricarica durante il lancio
    val infiniteTransition = rememberInfiniteTransition(label = "iconRollTransition")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "iconRotation",
    )

    // Struttura Material con barra superiore, contenuto e pulsante fluttuante.
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        MultiplayerMenu(
                            onCreateMatch = { selectedMultiplayerAction = "Crea una Partita" },
                            onJoinMatch = { selectedMultiplayerAction = "Entra in una partita" },
                        )
                    },
                    actions = {
                        // Il cambio tema resta disponibile accanto al regolamento.
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                                contentDescription = "Cambia Modalità Tema",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { showRulesDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "Regole del Gioco",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                // Seconda riga per il contatore: lascia spazio al menu anche su schermi piccoli.
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Pulsantiera centrale in alto con - [ Dadi Totali ] +
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Tasto di decremento in alto
                                IconButton(
                                    onClick = { viewModel.decrementTotalMatchDice() },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Diminuisci dadi",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                // Numero centrale cliccabile per aprire il pop-up di modifica testuale
                                Box(
                                    modifier = Modifier
                                        .clickable { showMatchDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Casino,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "Tot: $totalMatchDice",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }

                                // Tasto di incremento in alto
                                IconButton(
                                    onClick = { viewModel.incrementTotalMatchDice() },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumenta dadi",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // Pulsante fluttuante "+" per aggiungere un nuovo dado
            FloatingActionButton(
                onClick = { if (!isRolling) viewModel.addDie() },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Dado")
            }
        },
    ) { innerPadding ->
        // Il padding di Scaffold tiene il contenuto fuori dalla barra superiore.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Tocca un dado per rimuoverlo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            
            Text(
                text = "Totale dadi miei: ${dice.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Griglia dinamica dei dadi che si adatta allo spazio disponibile
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (dice.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Nessun dado. Premi '+' per aggiungerne uno!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    // Determina il numero ottimale di colonne in base alla quantità di dadi
                    val numColumns = when {
                        dice.size <= 1 -> 1
                        dice.size == 2 -> 2
                        dice.size == 3 -> 3
                        dice.size == 4 -> if (maxWidth > 500.dp) 4 else 2
                        (dice.size <= 6) -> if (maxWidth > 500.dp) 4 else 3
                        else -> 4
                    }

                    // La larghezza decide solo alcuni casi di colonne; le dimensioni
                    // dei dadi sono valori prefissati, non un adattamento continuo.
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
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        // La chiave stabile conserva lo stato di ciascun dado quando
                        // cambia la lista; animateItem anima inserimenti, rimozioni e spostamenti.
                        itemsIndexed(dice, key = { _, (dieId, _) -> dieId }) { index, dieItem ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                contentAlignment = Alignment.Center,
                            ) {
                                DiceView(
                                    die = dieItem,
                                    isRolling = isRolling,
                                    staggerIndex = index,
                                    onClickToRemove = { viewModel.removeDie(dieItem.id) },
                                    size = calculatedSize,
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
                enabled = dice.isNotEmpty() && !isRolling,
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isRolling) iconRotation else 0f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRolling) "LANCIANDO..." else "LANCIA I DADI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    // Riscontro esplicito: le voci del menu non creano ancora sessioni multiplayer.
    selectedMultiplayerAction?.let { action ->
        AlertDialog(
            onDismissRequest = { selectedMultiplayerAction = null },
            title = { Text(action) },
            text = {
                Text("Le partite multiplayer non sono ancora disponibili.")
            },
            confirmButton = {
                TextButton(onClick = { selectedMultiplayerAction = null }) {
                    Text("Ho capito")
                }
            },
        )
    }

    // Dialogo per la modifica testuale dei dadi totali quando si clicca sul numero in alto
    if (showMatchDialog) {
        // Lo stato nasce all'apertura del dialogo e viene eliminato quando questo si chiude.
        var textInput by remember { mutableStateOf(totalMatchDice.toString()) }

        AlertDialog(
            onDismissRequest = { showMatchDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = {
                Text(
                    text = "Modifica Dadi Totali",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Inserisci il numero effettivo dei dadi totali in gioco:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { newValue ->
                            textInput = newValue
                            // Ogni intero valido aggiorna subito il ViewModel; testo vuoto
                            // o non convertibile lascia il totale precedente. Conferma chiude
                            // soltanto il dialogo e la chiusura non annulla le modifiche.
                            val parsed = newValue.toIntOrNull()
                            if (parsed != null) {
                                viewModel.setTotalMatchDice(parsed)
                            }
                        },
                        label = { Text("Dadi totali") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMatchDialog = false }) {
                    Text("Conferma", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // Regolamento informativo: queste descrizioni non implementano turni, Dubito,
    // Calza o Palafico. Il riepilogo continua a trattare gli 1 come Jolly.
    // Dialogo pop-up con le regole del Perudo
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = {
                Text(
                    text = "Regole del Perudo",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "🎲 Il Dado 1 (Jolly)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Gli 1 valgono come Jolly e contano come qualsiasi numero! Quando scommetti su un valore (es. i 5), sommi sia i 5 naturali sia tutti i Jolly (1).\n• Non si può iniziare un round sugli 1",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🗣️ Le Dichiarazioni e Rilanci",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "I giocatori scommettono a turno sul numero minimo di dadi di un certo valore presenti complessivamente sul tavolo tra tutti i giocatori. A ogni turno puoi rilanciare aumentando la quantità o il valore del dado.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "⚡ Dubito!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Se ritieni che la scommessa precedente sia falsa, dici \"Dubito!\". Si rivelano tutti i dadi: se la quantità reale (inclusi i Jolly) è inferiore alla chiamata, lo scommettitore perde un dado; altrimenti perde un dado chi ha dubitato.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🧦 La Calza (Chiamata Esatta)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Se non sei di turno (non stai dichiarando né dubitando), puoi interrompere dicendo \"Calzo!\" se credi che la scommessa corrente sia ESATTAMENTE pari al numero reale di dadi sul tavolo.\n• Se indovini vinci un dado e si conclude la manche, la successiva riparte da te.\n• Se sbagli: perdi 1 dado e si conclude la manche, la successiva riparte da te.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🏛️ Il Palafico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Quando un giocatore rimane per la prima volta con un SOLO dado, la manche successiva è una manche di Palafico:\n• Gli 1 NON sono Jolly (valgono soltanto come 1).\n• Il giocatore in Palafico decide il valore del dado scommesso; gli altri giocatori possono solo aumentare la quantità mantenendo lo stesso dado.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("Ho capito", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}
