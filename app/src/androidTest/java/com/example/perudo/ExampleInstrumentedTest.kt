package com.example.perudo

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Test strumentato del template, eseguito su dispositivo o emulatore Android.
 * Controlla il package dell'app installata; non verifica schermata o regole del gioco.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Usa il contesto dell'app sotto test, distinto da quello del runner.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.perudo", appContext.packageName)
    }
}