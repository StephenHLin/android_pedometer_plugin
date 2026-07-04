package com.example.pedometer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.content.Context

class PedometerService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    companion object {
        var stepCount: Int = 0
        private const val PREFS_NAME = "pedometer_prefs"
        private const val KEY_STEP_COUNT = "step_count"
    }

    override fun onCreate() {
        super.onCreate()

        // TYPE_STEP_COUNTER only fires onSensorChanged when a new step is taken —
        // registering the listener never delivers the current value immediately.
        // If this process/service was killed and restarted, stepCount would
        // otherwise sit at 0 until the user's next physical step, which the
        // Godot-side delta logic can misread as a device reboot. Restoring the
        // last known value here means getStepCount() reports real data right away.
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        stepCount = prefs.getInt(KEY_STEP_COUNT, stepCount)

        val channelId = "pedometer_channel"
        val channel = NotificationChannel(
            channelId, "Pedometer", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Pedometer Running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(1, notification)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_STEP_COUNT, stepCount)
                .apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null
}