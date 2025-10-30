package com.example.sphereescape2125.sensors


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Zarządza czujnikiem światła i udostępnia jego odczyty jako strumień danych (Flow).
 *
 * Ta klasa opakowuje standardowy Android SensorEventListener w nowoczesne API
 * oparte na korutynach (callbackFlow), aby ułatwić konsumowanie danych
 * w ViewModelu lub warstwie UI.
 */

class LightSensor(private val context: Context) {

    // Pobranie menedżera sensorów z kontekstu aplikacji
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Pobranie domyślnego czujnika światła
    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    /**
     * Strumień (Flow), który emituje wartości natężenia światła (w luksach).
     *
     * - Rozpoczyna nasłuchiwanie, gdy strumień jest kolekcjonowany (obserwowany).
     * - Przestaje nasłuchiwać, gdy kolekcja jest anulowana (np. gdy ViewModel jest niszczony).
     */
    val sensorReadings: Flow<Float> = callbackFlow {
        // 1. Definicja listenera, który będzie reagował na zmiany czujnika
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                // Sprawdzamy, czy zdarzenie pochodzi od naszego czujnika światła
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    // event.values[0] zawiera wartość natężenia światła w luksach (lx)
                    val luxValue = event.values[0]

                    // "Wysyłamy" nową wartość do strumienia (Flow)
                    // trySend jest bezpieczne do użycia z różnych wątków
                    trySend(luxValue)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Zazwyczaj nie potrzebujemy tego implementować dla czujnika światła
            }
        }

        // 2. Rejestracja listenera w systemie
        if (lightSensor != null) {
            sensorManager.registerListener(
                listener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL // Częstotliwość odczytów
            )
        } else {
            // Jeśli czujnik nie jest dostępny, zamykamy strumień z błędem
            close(IllegalStateException("Czujnik światła nie jest dostępny w tym urządzeniu."))
        }


        // 3. Definicja "sprzątania" (cleanup)
        // Ten blok wykona się, gdy strumień zostanie anulowany (np. ViewModel przestanie go obserwować)
        awaitClose {
            // Wyrejestrowujemy listenera, aby oszczędzać baterię
            sensorManager.unregisterListener(listener)
        }
    }
}