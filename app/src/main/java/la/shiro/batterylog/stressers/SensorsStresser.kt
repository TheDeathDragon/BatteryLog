package la.shiro.batterylog.stressers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import la.shiro.batterylog.config.PI_50
import la.shiro.batterylog.config.TAG

class SensorsStresser(context: Context) : Stresser(context) {
    private val sensorManager : SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val sensorsListener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            impossibleUIUpdateOnMain(event.values[0].toDouble() == PI_50)
            // Log.d(TAG, "onSensorChanged: ${event.sensor.name} ${event.values[0]}")
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Log.d(TAG, "onAccuracyChanged: ${sensor.name}. New accuracy: $accuracy")
        }
    }

    override fun permissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ContextCompat.checkSelfPermission(context, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED)
        } else {
            super.permissionsGranted()
        }
    }

    override fun start() {
        super.start()
        sensorManager.getSensorList(Sensor.TYPE_ALL).forEach { sensor ->
            Log.d(TAG, "Stressing sensor: ${sensor.name}")
            sensorManager.registerListener(sensorsListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun stop() {
        super.stop()
        sensorManager.unregisterListener(sensorsListener)
    }

}