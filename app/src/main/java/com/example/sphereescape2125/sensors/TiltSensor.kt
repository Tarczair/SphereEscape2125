package com.example.sphereescape2125.sensors // Upewnij się, że pakiet jest dobry

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Zarządza czujnikiem grawitacji (przechyłu) i udostępnia dane jako StateFlow.
 */
class TiltSensor(
    context: Context
) : SensorEventListener {

    // Pobieramy systemowy SensorManager
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Znajdujemy domyślny czujnik grawitacji
    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    // Prywatny, mutowalny stan
    private val _gravityData = MutableStateFlow(Offset(0f, 0f))
    // Publiczny, niemutowalny stan, który Composable będzie czytać
    val gravityData: StateFlow<Offset> = _gravityData.asStateFlow()

    /**
     * Rejestruje słuchacza. Należy wywołać, gdy ekran staje się widoczny.
     */
    fun startListening() {
        if (gravitySensor == null) {
            // Brak sensora na urządzeniu
            return
        }
        sensorManager.registerListener(
            this,
            gravitySensor,
            SensorManager.SENSOR_DELAY_GAME // Najszybsze odczyty
        )
    }

    /**
     * Wyrejestrowuje słuchacza. Kluczowe dla oszczędzania baterii.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    // --- Metody z interfejsu SensorEventListener ---

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            // Aktualizujemy nasz StateFlow nowymi danymi
            _gravityData.update {
                Offset(event.values[0], event.values[1])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Zazwyczaj nie musimy nic tu robić
    }
}