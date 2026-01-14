package com.example.sphereescape2125

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sphereescape2125.sensors.LightSensor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Główny ViewModel aplikacji, zarządzający stanem globalnym i logiką gry.
 *
 * Odpowiada za:
 * - Automatyczne przełączanie motywu (Ciemny/Jasny) na podstawie odczytów z czujnika światła.
 * - Obsługę gestu potrząśnięcia urządzeniem i komunikację tego zdarzenia do warstwy UI.
 *
 * @param application Kontekst aplikacji.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val lightSensor: LightSensor = LightSensor(application)
    private val prefs = application.getSharedPreferences("SphereEscapePrefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(false)

    /**
     * Publiczny strumień określający, czy aplikacja powinna używać ciemnego motywu.
     *
     * Wartość jest aktualizowana dynamicznie na podstawie [LightSensor] oraz preferencji użytkownika.
     */
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            lightSensor.sensorReadings.collect { luxValue ->
                // ODCZYTUJEMY WARTOŚĆ Z SUWAKA (0.0 - 1.0) z SharedPreferences
                val savedSens = prefs.getFloat("LightSens", 0f)

                // PRZELICZAMY NA DYNAMICZNY PRÓG (np. od 100 do 500 luksów)
                val dynamicThreshold = 100f + (savedSens * 400f)

                // Aktualizacja stanu motywu
                _isDarkTheme.value = luxValue < dynamicThreshold
            }
        }
    }

    // --- SHAKE LOGIC ---
    private val _shakeEvent = Channel<Unit>(Channel.BUFFERED)

    /**
     * Strumień zdarzeń informujący UI o wystąpieniu wstrząsu.
     */
    val shakeEvent = _shakeEvent.receiveAsFlow()

    /**
     * Metoda wywoływana, gdy zewnętrzny detektor (ShakeDetector) wykryje wstrząs.
     *
     * Inicjuje logikę gry (zmianę układu ścian) oraz emituje zdarzenie do [shakeEvent].
     */
    fun onShakeDetected() {
        viewModelScope.launch {
            randomizeWalls()
            _shakeEvent.send(Unit)
        }
    }

    /**
     * Logika odpowiedzialna za losową zmianę konfiguracji przeszkód w grze.
     */
    private fun randomizeWalls() {
        println("SHAKE: Logika zmiany ścian (ViewModel)")
    }
}