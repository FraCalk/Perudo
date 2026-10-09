package com.example.perudo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class DieItem(
    val id: Int,
    val value: Int = 1
)

class DiceViewModel : ViewModel() {
    private var nextId = 1

    private val _dice = MutableStateFlow<List<DieItem>>(
        listOf(
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7)),
            DieItem(id = nextId++, value = Random.nextInt(1, 7))
        ) // Start with 5 dice by default
    )
    val dice: StateFlow<List<DieItem>> = _dice.asStateFlow()

    fun addDie() {
        _dice.update { current ->
            current + DieItem(id = nextId++, value = Random.nextInt(1, 7))
        }
    }

    fun removeDie(id: Int) {
        _dice.update { current ->
            current.filter { it.id != id }
        }
    }

    fun rollDice() {
        _dice.update { current ->
            current.map { die ->
                die.copy(value = Random.nextInt(1, 7))
            }
        }
    }
}
