package com.example.sphereescape2125.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit // To jest callback - funkcja, która odpali się przy wstrząsie
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Parametry czułości - można dostosować
    private val shakeThresholdGravity = 1.2F // Siła wstrząsu (g-force)
    private val minTimeBetweenShakesMs = 1000 // Żeby nie spamowało eventami

    private var lastShakeTime: Long = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nieistotne w tym przypadku
    }
}