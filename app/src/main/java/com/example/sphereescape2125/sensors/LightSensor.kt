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
 * Zarządza czujnikiem natężenia światła w urządzeniu.
 *
 * Klasa wykorzystuje mechanizm Kotlin Coroutines [Flow], aby przekształcić
 * oparte na wywołaniach zwrotnych API sensorów Androida w reaktywny strumień danych.
 * Pozwala to na wygodne i bezpieczne dla pamięci obserwowanie zmian oświetlenia.
 *
 * @property context Kontekst aplikacji wymagany do uzyskania dostępu do usług systemowych.
 */
class LightSensor(private val context: Context) {


    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager


    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    /**
     * Strumień emitujący wartości natężenia światła wyrażone w luksach (lx).
     *
     * Implementacja wykorzystuje [callbackFlow], co oznacza, że rejestracja sensora
     * następuje automatycznie w momencie rozpoczęcia pobierania danych (collect),
     * a wyrejestrowanie następuje przy anulowaniu subskrypcji (np. zamknięcie ekranu).
     *
     * @return [Flow] emitujący wartości typu Float reprezentujące natężenie światła.
     * @throws IllegalStateException Jeśli urządzenie nie posiada fizycznego czujnika światła.
     */
    val sensorReadings: Flow<Float> = callbackFlow {

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {

                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {

                    val luxValue = event.values[0]


                    trySend(luxValue)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }


        if (lightSensor != null) {
            sensorManager.registerListener(
                listener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {

            close(IllegalStateException("Czujnik światła nie jest dostępny w tym urządzeniu."))
        }



        awaitClose {

            sensorManager.unregisterListener(listener)
        }
    }
}