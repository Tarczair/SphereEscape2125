package com.example.sphereescape2125.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Klasa odpowiedzialna za wykrywanie gestu potrząśnięcia urządzeniem.
 *
 * Wykorzystuje akcelerometr do monitorowania sił działających na telefon.
 * Jeśli obliczona siła przeciążenia (g-force) przekroczy zdefiniowany próg,
 * wywoływana jest funkcja zwrotna [onShake]. Klasa zawiera mechanizm "debounce",
 * zapobiegający wielokrotnemu wywoływaniu zdarzenia w krótkim odstępie czasu.
 *
 * @param context Kontekst aplikacji potrzebny do dostępu do [SensorManager].
 * @param threshold Próg siły G wyzwalający zdarzenie (domyślnie 2.5f).
 * @param onShake Funkcja zwrotna (callback) typu `() -> Unit`, uruchamiana po wykryciu wstrząsu.
 */
class ShakeDetector(
    context: Context,
    private var threshold: Float = 2.5f,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val minTimeBetweenShakesMs = 1000
    private var lastShakeTime: Long = 0

    /**
     * Dynamicznie zmienia czułość wykrywania wstrząsów.
     *
     * Mapuje wartość czułości na próg siły G.
     * Przykład: wyższa czułość (np. 3.0) skutkuje niskim progiem (łatwiejsze wyzwolenie).
     *
     * @param sensitivity Wartość czułości (sugerowany zakres 0.5 - 3.0).
     */
    fun setSensitivity(sensitivity: Float) {
        // Mapujemy suwak na próg G-force: sens 3.0 -> próg 1.5, sens 0.5 -> próg 4.0
        threshold = 4.5f - sensitivity
    }

    /**
     * Rozpoczyna nasłuchiwanie danych z akcelerometru.
     *
     * Rejestruje listener z opóźnieniem [SensorManager.SENSOR_DELAY_UI],
     * co jest wystarczające dla wykrywania gestów i mniej obciążające dla baterii.
     */
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Zatrzymuje nasłuchiwanie danych z sensora.
     *
     * Należy wywołać tę metodę w cyklu życia Activity/Fragmentu (np. w onPause),
     * aby zwolnić zasoby sprzętowe.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Przetwarza surowe dane z akcelerometru w celu wykrycia wstrząsu.
     *
     * Algorytm:
     * 1. Normalizuje wartości osi X, Y, Z względem grawitacji ziemskiej.
     * 2. Oblicza wypadkową siłę g-force używając pierwiastka z sumy kwadratów.
     * 3. Sprawdza, czy siła przekracza aktualny próg [threshold].
     * 4. Weryfikuje czas od ostatniego wstrząsu (debounce), aby uniknąć duplikatów.
     *
     * @param event Obiekt zdarzenia sensora zawierający wartości przyspieszenia.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Obliczamy siłę przeciążenia (g-force) dla każdej osi
        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH

        // Pitagoras w 3D: pierwiastek z sumy kwadratów
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > threshold) {
            val now = System.currentTimeMillis()
            // Ignoruj wstrząsy, jeśli są zbyt blisko siebie (debounce)
            if (lastShakeTime + minTimeBetweenShakesMs > now) {
                return
            }

            lastShakeTime = now
            onShake()
        }
    }

    /**
     * Metoda wywoływana przy zmianie dokładności sensora.
     *
     * W obecnej implementacji nie jest wykorzystywana.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nieistotne w tym przypadku
    }
}