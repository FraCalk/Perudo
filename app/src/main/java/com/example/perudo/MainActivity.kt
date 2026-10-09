package com.example.perudo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.perudo.ui.screen.DiceScreen
import com.example.perudo.ui.theme.PerudoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerudoTheme {
                DiceScreen()
            }
        }
    }
}
