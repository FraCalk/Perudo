package com.example.perudo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.perudo.ui.screen.DiceScreen
import com.example.perudo.ui.theme.PerudoTheme

/** Punto di ingresso Android: inizializza Compose, il tema e la schermata dei dadi. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Consente al contenuto di estendersi sotto le barre di sistema.
        // DiceScreen usa il padding di Scaffold per posizionare il contenuto.
        enableEdgeToEdge()
        // Da qui la UI viene descritta con funzioni Compose, senza layout XML.
        setContent {
            // Ottiene il tema di sistema all'avvio in modo sicuro all'interno del contesto Composable
            val systemDark = isSystemInDarkTheme()
            // Utilizza rememberSaveable per preservare il tema selezionato anche dopo la rotazione dello schermo (orientation change)
            var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
            
            // Il tema parte dalla preferenza di sistema e poi segue la scelta locale.
            // Il callback risale dalla schermata e aggiorna lo stato dell'Activity.
            PerudoTheme(darkTheme = darkTheme) {
                DiceScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                )
            }
        }
    }
}
