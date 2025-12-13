package com.example.sphereescape2125


import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
 * Klasa dziedziczy po [AndroidViewModel], aby mieć dostęp do kontekstu aplikacji
 * wymaganego przez sensory.
 *
 * @param application Kontekst aplikacji.
 */

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private val lightSensor: LightSensor = LightSensor(application)


    private val _isDarkTheme = MutableStateFlow(false)

    /**
     * Publiczny strumień określający, czy aplikacja powinna używać ciemnego motywu.
     *
     * Wartość jest aktualizowana w czasie rzeczywistym na podstawie danych z [LightSensor].
     * - `true`: Otoczenie jest ciemne (poniżej [LIGHT_SENSOR_THRESHOLD]).
     * - `false`: Otoczenie jest jasne.
     */
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    /**
     * Próg natężenia światła w luksach (lx).
     * Poniżej tej wartości aktywowany jest tryb ciemny.
     */
    private val LIGHT_SENSOR_THRESHOLD = 100f


    init {

        viewModelScope.launch {

            lightSensor.sensorReadings.collect { luxValue ->

                val isDark = luxValue < LIGHT_SENSOR_THRESHOLD


                _isDarkTheme.value = isDark
            }
        }
    }


    // --- SHAKE LOGIC ---
    private val _shakeEvent = Channel<Unit>(Channel.BUFFERED)

    /**
     * Strumień zdarzeń typu "fire-and-forget" informujący UI o wystąpieniu wstrząsu.
     *
     * Wykorzystuje [Channel], ponieważ zdarzenie wstrząsu jest jednorazowe
     * i nie stanowi trwałego stanu (w przeciwieństwie do StateFlow).
     * Służy do wyzwalania animacji lub efektów dźwiękowych w warstwie widoku.
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
     *
     * (Metoda wewnętrzna - implementacja logiki gry).
     */
    private fun randomizeWalls() {
        println("SHAKE: Logika zmiany ścian (ViewModel)")

    }
    }


