package com.example.sphereescape2125.sensors

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
 * Zarządza czujnikiem grawitacji (przechyłu) urządzenia.
 *
 * Klasa ta implementuje interfejs [SensorEventListener], aby odbierać surowe dane
 * z sensora sprzętowego (typu [Sensor.TYPE_GRAVITY]) i udostępniać je
 * w reaktywny sposób za pomocą strumienia [StateFlow].
 *
 * Służy do sterowania elementami gry poprzez fizyczne przechylanie urządzenia.
 *
 * @param context Kontekst aplikacji wymagany do uzyskania dostępu do usługi [SensorManager].
 */
class TiltSensor(
    context: Context
) : SensorEventListener {


    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager


    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)


    private val _gravityData = MutableStateFlow(Offset(0f, 0f))

    /**
     * Publiczny strumień danych o aktualnym wychyleniu urządzenia.
     *
     * Wartość emitowana to [Offset], gdzie:
     * - `x` odpowiada wychyleniu w osi poziomej.
     * - `y` odpowiada wychyleniu w osi pionowej.
     */

    val gravityData: StateFlow<Offset> = _gravityData.asStateFlow()

    /**
     * Rejestruje nasłuchiwanie zdarzeń z czujnika grawitacji.
     *
     * Metodę należy wywołać w momencie startu gry lub wznowienia widoku,
     * aby rozpocząć odbieranie danych. Używa flagi [SensorManager.SENSOR_DELAY_GAME]
     * dla zapewnienia płynności sterowania.
     *
     * Jeśli urządzenie nie posiada odpowiedniego sensora, metoda kończy działanie bez błędu.
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
     * Wyrejestrowuje nasłuchiwanie zdarzeń z czujnika.
     *
     * Metodę należy bezwzględnie wywołać przy wstrzymaniu gry lub niszczeniu widoku,
     * aby zapobiec zbędnemu zużyciu baterii przez sensor działający w tle.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Wywoływana przez system, gdy zmienią się wskazania sensora.
     *
     * Aktualizuje stan [_gravityData] nowymi współrzędnymi pobranymi z [event].
     *
     * @param event Obiekt zawierający nowe dane sensora (wartości x i y).
     */

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            // Aktualizujemy nasz StateFlow nowymi danymi
            _gravityData.update {
                Offset(event.values[0], event.values[1])
            }
        }
    }
    /**
     * Wywoływana przez system, gdy zmieni się dokładność sensora.
     *
     * Obecnie implementacja jest pusta, gdyż gra nie wymaga dynamicznej reakcji na zmiany dokładności.
     *
     * @param sensor Sensor, którego dokładność uległa zmianie.
     * @param accuracy Nowy poziom dokładności.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}