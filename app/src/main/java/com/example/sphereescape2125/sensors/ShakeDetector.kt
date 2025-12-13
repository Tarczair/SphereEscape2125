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
 * @param onShake Funkcja zwrotna (callback) typu `() -> Unit`, uruchamiana po wykryciu wstrząsu.
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


    private val shakeThresholdGravity = 1.2F
    private val minTimeBetweenShakesMs = 1000

    private var lastShakeTime: Long = 0

    /**
     * Rozpoczyna nasłuchiwanie danych z akcelerometru.
     *
     * Rejestruje listener z opóźnieniem [SensorManager.SENSOR_DELAY_UI],
     * co jest wystarczające dla wykrywania gestów i mniej obciążające dla baterii niż tryb GAME.
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
     * 3. Sprawdza, czy siła przekracza próg [shakeThresholdGravity].
     * 4. Weryfikuje czas od ostatniego wstrząsu (debounce), aby uniknąć duplikatów.
     *
     * @param event Obiekt zdarzenia sensora zawierający wartości przyspieszenia.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Obliczamy siłę przeciążenia (g-force)
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Pitagoras w 3D: pierwiastek z sumy kwadratów
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > shakeThresholdGravity) {
            val now = System.currentTimeMillis()
            // Ignoruj wstrząsy, jeśli są zbyt blisko siebie (debounce)
            if (lastShakeTime + minTimeBetweenShakesMs > now) {
                return
            }

            lastShakeTime = now
            onShake() // <--- TU ODPALAMY AKCJĘ
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