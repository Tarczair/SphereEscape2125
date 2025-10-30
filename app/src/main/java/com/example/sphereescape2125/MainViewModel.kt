package com.example.sphereescape2125


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sphereescape2125.sensors.LightSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Używamy AndroidViewModel, aby bezpiecznie uzyskać dostęp do Kontekstu (Application)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Instancja naszego czujnika
    // Przekazujemy kontekst aplikacji, którego wymaga LightSensor
    private val lightSensor: LightSensor = LightSensor(application)

    // 2. Prywatny, modyfikowalny stan (nasze "1 lub 0")
    // Domyślnie ustawiamy motyw jasny (false)
    private val _isDarkTheme = MutableStateFlow(false)

    // 3. Publiczny, niemodyfikowalny stan (StateFlow), którego będzie słuchać UI
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // 4. Próg czujnika - dostosuj tę wartość eksperymentalnie!
    // Poniżej tej wartości (w luksach) włączy się tryb ciemny.
    private val LIGHT_SENSOR_THRESHOLD = 100f

    // 5. Blok 'init' uruchamia się raz, gdy ViewModel jest tworzony
    init {
        // Uruchamiamy korutynę w 'viewModelScope',
        // która automatycznie anuluje się, gdy ViewModel zostanie zniszczony
        viewModelScope.launch {
            // Rozpoczynamy obserwowanie strumienia z czujnika
            lightSensor.sensorReadings.collect { luxValue ->
                // To jest nasza logika "1 lub 0"
                // Jeśli światło jest poniżej progu, ustawiamy _isDarkTheme.value na true
                val isDark = luxValue < LIGHT_SENSOR_THRESHOLD

                // Aktualizujemy stan, co automatycznie powiadomi UI (MainActivity)
                _isDarkTheme.value = isDark
            }
        }
    }
}