package com.example.perudo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Menu ancorato al pulsante Multiplayer.
 * Chiude la tendina prima di inoltrare l'azione scelta alla schermata.
 */
@Composable
fun MultiplayerMenu(
    onCreateMatch: () -> Unit,
    onJoinMatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.semantics {
                stateDescription = if (expanded) "Menu aperto" else "Menu chiuso"
            },
        ) {
            Text(text = "Multiplayer")
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Crea una Partita") },
                onClick = {
                    expanded = false
                    onCreateMatch()
                },
            )
            DropdownMenuItem(
                text = { Text("Entra in una partita") },
                onClick = {
                    expanded = false
                    onJoinMatch()
                },
            )
        }
    }
}

