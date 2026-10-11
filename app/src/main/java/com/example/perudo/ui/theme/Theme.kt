package com.example.perudo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Palette di riserva quando i colori dinamici sono disattivati o non disponibili.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Personalizza i tre colori principali; gli altri ruoli mantengono i default Material.
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Fornisce colori e tipografia a tutti i composable contenuti.
 * Con dynamicColor attivo, Android 12+ usa la palette generata dal sistema:
 * i colori di Color.kt sono usati soltanto nel percorso di riserva.
 */
@Composable
fun PerudoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // I colori dinamici del sistema sono disponibili da Android 12.
    dynamicColor: Boolean = true,
    content: /**
 * Fornisce colori e tipografia a tutti i composable contenuti.
 * Con dynamicColor attivo, Android 12+ usa la palette generata dal sistema:
 * i colori di Color.kt sono usati soltanto nel percorso di riserva.
 */
@Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}