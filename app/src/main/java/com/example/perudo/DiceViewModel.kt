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
 * Informazioni su una combinazione o coppia rilevata tra i dadi.
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
 * Conteggio dei dadi per un determinato valore facciale.
 */
data class ValueCountInfo(
    val value: Int,
    val naturalCount: Int,
    val totalWithJolly: Int,
)

/**
 * Riepilogo generale dello stato dei dadi per il gioco del Perudo.
 */
data class DiceSummary(
    val jollyCount: Int,
    val combinations: List<CombinationInfo>,
    val valueBreakdown: List<ValueCountInfo>,
)

/**
 * ViewModel principale che gestisce la logica di business e lo stato reattivo dei dadi.
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

    // Flusso reattivo che calcola automaticamente il riepilogo del Perudo ogni volta che la lista dei dadi cambia
    val diceSummary: StateFlow<DiceSummary> = _dice.map { diceList ->
        calculateSummary(diceList)
    }.stateIn(
        scope = viewModelScope,
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
     * Aggiunge un nuovo dado alla collezione con un valore casuale iniziale.
     */
    fun addDie() {
        _dice.update { current ->
            current + DieItem(id = nextId++, value = Random.nextInt(1, 7))
        }
    }

    /**
     * Rimuove un dado specifico tramite il suo ID (disabilitato durante il lancio).
     */
    fun removeDie(id: Int) {
        if (_isRolling.value) return
        _dice.update { current ->
            current.filter { (idItem, _) -> idItem != id }
        }
    }

    /**
     * Simula il lancio dei dadi con animazione e ritardo temporale, assegnando nuovi valori casuali.
     */
    fun rollDice() {
        if (_isRolling.value || _dice.value.isEmpty()) return
        viewModelScope.launch {
            _isRolling.value = true
            delay(400.milliseconds) // Ritardo iniziale per la fase di sollevamento
            _dice.update { current ->
                current.map { die ->
                    die.copy(value = Random.nextInt(1, 7))
                }
            }
            delay(350.milliseconds) // Ritardo di assestamento
            _isRolling.value = false
        }
    }
}
