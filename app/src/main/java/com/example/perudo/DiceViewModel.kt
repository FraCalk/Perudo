package com.example.perudo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Modello di dati che rappresenta un singolo dado all'interno dell'applicazione.
 * @property id Identificativo univoco del dado.
 * @property value Valore facciale attuale del dado (da 1 a 6).
 */
data class DieItem(
    val id: Int,
    val value: Int = 1,
)

/**
 * Informazioni su una combinazione o coppia rilevata tra i dadi del giocatore.
 * @property value Faccia naturale considerata, da 2 a 6.
 * @property naturalCount Dadi che mostrano esattamente questa faccia.
 * @property jollyCount Dadi da 1 utilizzabili per completare la combinazione.
 * @property totalWithJolly Somma dei dadi naturali e dei Jolly.
 * @property typeName Etichetta della combinazione, per esempio Coppia o Tris.
 * @property description Testo che distingue dadi naturali e Jolly nel riepilogo.
 */
data class CombinationInfo(
    val value: Int,
    val naturalCount: Int,
    val jollyCount: Int,
    val totalWithJolly: Int,
    val typeName: String,
    val description: String,
)

/**
 * Conteggio dei dadi del giocatore per un determinato valore facciale, da 2 a 6.
 * @property value Faccia di cui si calcola il conteggio.
 * @property naturalCount Numero di occorrenze esatte, mostrato nei riquadri della UI.
 * @property totalWithJolly Conteggio comprensivo degli 1, disponibile nel modello.
 */
data class ValueCountInfo(
    val value: Int,
    val naturalCount: Int,
    val totalWithJolly: Int,
)

/**
 * Riepilogo dei soli dadi del giocatore; il totale di partita non entra nel calcolo.
 * @property jollyCount Numero di dadi con valore 1.
 * @property combinations Combinazioni con almeno un naturale e almeno due dadi totali.
 * @property valueBreakdown Conteggi per tutte le facce da 2 a 6, comprese quelle assenti.
 */
data class DiceSummary(
    val jollyCount: Int,
    val combinations: List<CombinationInfo>,
    val valueBreakdown: List<ValueCountInfo>,
)

/**
 * Gestisce dadi, lancio e contatore di partita, separando i dati dalla UI Compose.
 * Sopravvive alle ricreazioni dell'Activity, ma non salva lo stato su disco:
 * dopo la terminazione del processo vengono ricreati i valori iniziali.
 * Turni, dichiarazioni e Palafico non sono gestiti da questo modello.
 */
class DiceViewModel : ViewModel() {
    // Contatore progressivo per assegnare ID unici ai nuovi dadi creati
    private var nextId = 1

    // Stato interno mutabile contenente la lista dei dadi correnti (inizializzata con 6 dadi casuali)
    private val _dice = MutableStateFlow(
        listOf(
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
        ),
    )
    // Stato pubblico immutabile esposto alla UI
    val dice: StateFlow<List<DieItem>> = _dice.asStateFlow()

    // Stato che indica se è in corso l'animazione di lancio dei dadi
    private val _isRolling = MutableStateFlow(value = false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    // Numero totale di dadi nella partita (miei + avversari)
    private val _totalMatchDice = MutableStateFlow(value = 30)
    val totalMatchDice: StateFlow<Int> = _totalMatchDice.asStateFlow()

    /** Imposta il contatore manuale di partita, con un minimo di 1 anche senza dadi propri. */
    fun setTotalMatchDice(count: Int) {
        _totalMatchDice.value = count.coerceAtLeast(1)
    }

    /** Aumenta il totale di partita senza aggiungere un dado alla mano del giocatore. */
    fun incrementTotalMatchDice() {
        _totalMatchDice.update { it + 1 }
    }

    /** Riduce il totale di partita senza modificare la mano, fermandosi a 1. */
    fun decrementTotalMatchDice() {
        _totalMatchDice.update { (it - 1).coerceAtLeast(1) }
    }

    // Flusso reattivo che calcola automaticamente il riepilogo del Perudo ogni volta che la lista dei dadi cambia
    val diceSummary: StateFlow<DiceSummary> = _dice.map { diceList ->
        calculateSummary(diceList)
    }.stateIn(
        scope = viewModelScope,
        // Mantiene il calcolo attivo per 5 secondi dopo la disconnessione dell'ultimo osservatore.
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = calculateSummary(_dice.value),
    )

    /**
     * Calcola le statistiche e le combinazioni possibili (inclusi i Jolly che valgono 1) per il Perudo.
     */
    private fun calculateSummary(diceList: List<DieItem>): DiceSummary {
        // Conta quanti dadi hanno valore 1 (Jolly)
        val jollyCount = diceList.count { (_, value) -> value == 1 }
        val combinations = mutableListOf<CombinationInfo>()
        val valueBreakdown = mutableListOf<ValueCountInfo>()

        // Gli 1 sono sempre Jolly in questo calcolo: non esiste una modalit? Palafico.
        // Analizza i valori da 2 a 6
        for (v in 2..6) {
            val natural = diceList.count { (_, value) -> value == v }
            val total = natural + jollyCount

            valueBreakdown.add(
                ValueCountInfo(
                    value = v,
                    naturalCount = natural,
                    totalWithJolly = total,
                ),
            )

            // Il riepilogo include anche facce senza dadi naturali, ma la lista delle
            // combinazioni richiede almeno un naturale: i soli Jolly non generano etichette.
            // Se ci sono abbastanza dadi (naturali + jolly) per formare almeno una coppia
            if ((total >= 2) && (natural >= 1)) {
                val label = when (total) {
                    2 -> "Coppia"
                    3 -> "Tris"
                    4 -> "Poker"
                    5 -> "5 Dadi"
                    6 -> "6 Dadi"
                    7 -> "7 Dadi"
                    else -> "$total Dadi"
                }

                val desc = if (jollyCount > 0) {
                    val dadoText = if (natural == 1) "1 dado" else "$natural dadi"
                    val jollyText = if (jollyCount == 1) "1 Jolly" else "$jollyCount Jolly"
                    "$dadoText + $jollyText"
                } else {
                    "$natural dadi naturali"
                }

                combinations.add(
                    CombinationInfo(
                        value = v,
                        naturalCount = natural,
                        jollyCount = jollyCount,
                        totalWithJolly = total,
                        typeName = label,
                        description = desc,
                    ),
                )
            }
        }

        return DiceSummary(
            jollyCount = jollyCount,
            combinations = combinations,
            valueBreakdown = valueBreakdown,
        )
    }

    /**
     * Aggiunge un nuovo dado casuale e incrementa il totale di partita.
     * La schermata impedisce l'aggiunta durante il lancio; questa funzione non la blocca.
     */
    fun addDie() {
        _dice.update { current ->
            current + DieItem(id = nextId++, value = Random.nextInt(1, 7))
        }
        _totalMatchDice.update { it + 1 }
    }

    /**
     * Rimuove un dado tramite il suo ID; durante il lancio non modifica lo stato.
     * Decrementa il totale di partita anche se l'ID non esiste: la UI passa ID presenti.
     */
    fun removeDie(id: Int) {
        if (_isRolling.value) return
        _dice.update { current ->
            current.filter { (idItem, _) -> idItem != id }
        }
        _totalMatchDice.update { (it - 1).coerceAtLeast(1) }
    }

    /**
     * Genera immediatamente i nuovi valori dei dadi all'avvio del lancio, affinché l'animazione 3D
     * utilizzi fin dal primo frame del volo il valore di destinazione e atterri già con quello.
     */
    fun rollDice() {
        if (_isRolling.value || _dice.value.isEmpty()) return
        viewModelScope.launch {
            _isRolling.value = true
            // Genera i nuovi valori IMMEDIATAMENTE a t = 0ms prima del lancio e atterraggio
            _dice.update { current ->
                current.map { die ->
                    die.copy(value = Random.nextInt(1, 7))
                }
            }
            // Mantiene lo stato di lancio attivo per la durata dell'animazione 3D
            // Il blocco dura 480 ms fissi; non attende la conclusione delle singole molle.
            delay(480.milliseconds)
            _isRolling.value = false
        }
    }
}
