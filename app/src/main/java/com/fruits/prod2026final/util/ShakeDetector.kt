package com.fruits.prod2026final.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime = 0L
    private val shakeThreshold = 40f
    private val shakeCooldown = 1000L

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val (x, y, z) = event.values
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

            if (acceleration > shakeThreshold) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > shakeCooldown) {
                    lastShakeTime = now
                    onShake()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() { sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI) }
    fun stop() { sensorManager.unregisterListener(listener) }
}
