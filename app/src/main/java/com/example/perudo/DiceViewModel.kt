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

data class DieItem(
    val id: Int,
    val value: Int = 1
)

data class CombinationInfo(
    val value: Int,
    val naturalCount: Int,
    val jollyCount: Int,
    val totalWithJolly: Int,
    val typeName: String,
    val description: String
)

data class ValueCountInfo(
    val value: Int,
    val naturalCount: Int,
    val totalWithJolly: Int
)

data class DiceSummary(
    val jollyCount: Int,
    val combinations: List<CombinationInfo>,
    val valueBreakdown: List<ValueCountInfo>
)

class DiceViewModel : ViewModel() {
    private var nextId = 1

    private val _dice = MutableStateFlow<List<DieItem>>(
        listOf(
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7))
        )
    )
    val dice: StateFlow<List<DieItem>> = _dice.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    val diceSummary: StateFlow<DiceSummary> = _dice.map { diceList ->
        calculateSummary(diceList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = calculateSummary(_dice.value)
    )

    private fun calculateSummary(diceList: List<DieItem>): DiceSummary {
        val jollyCount = diceList.count { it.value == 1 }
        val combinations = mutableListOf<CombinationInfo>()
        val valueBreakdown = mutableListOf<ValueCountInfo>()

        for (v in 2..6) {
            val natural = diceList.count { it.value == v }
            val total = natural + jollyCount

            valueBreakdown.add(
                ValueCountInfo(
                    value = v,
                    naturalCount = natural,
                    totalWithJolly = total
                )
            )

            // A pair/tris/poker/etc can be formed by natural dice + jollys (totalWithJolly >= 2 and natural >= 1)
            if (total >= 2 && natural >= 1) {
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
                        naturalCount = natural,                        jollyCount = jollyCount,
                        totalWithJolly = total,
                        typeName = label,
                        description = desc
                    )
                )
            }
        }

        return DiceSummary(
            jollyCount = jollyCount,
            combinations = combinations,
            valueBreakdown = valueBreakdown
        )
    }

    fun addDie() {
        _dice.update { current ->
            current + DieItem(id = nextId++, value = Random.nextInt(1, 7))
        }
    }

    fun removeDie(id: Int) {
        if (_isRolling.value) return
        _dice.update { current ->
            current.filter { it.id != id }
        }
    }

    fun rollDice() {
        if (_isRolling.value || _dice.value.isEmpty()) return
        viewModelScope.launch {
            _isRolling.value = true
            delay(400)
            _dice.update { current ->
                current.map { die ->
                    die.copy(value = Random.nextInt(1, 7))
                }
            }
            delay(350)
            _isRolling.value = false
        }
    }
}
