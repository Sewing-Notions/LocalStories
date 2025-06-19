package com.localstories

import android.hardware.*
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import android.widget.ImageButton
class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private lateinit var compassImage: ImageView
    private lateinit var compassLabel: TextView

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var currentAzimuth = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        val closeBtn = findViewById<ImageButton>(R.id.closeCompassBtn)
        closeBtn.setOnClickListener {
            finish()
        }
        compassImage = findViewById(R.id.compassImage)
        compassLabel = findViewById(R.id.compassDirectionLabel)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values.clone()
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values.clone()

        val R = FloatArray(9)
        val I = FloatArray(9)

        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)

            val azimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
            updateCompass(azimuth)
        }
    }

    private fun updateCompass(degree: Float) {
        val rotate = RotateAnimation(
            currentAzimuth,
            -degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotate.duration = 210
        rotate.fillAfter = true
        compassImage.startAnimation(rotate)

        currentAzimuth = degree
        updateDirectionText(degree)
    }

    private fun updateDirectionText(degree: Float) {
        val direction = when (degree.toInt()) {
            in 0..22 -> "North"
            in 23..67 -> "Northeast"
            in 68..112 -> "East"
            in 113..157 -> "Southeast"
            in 158..202 -> "South"
            in 203..247 -> "Southwest"
            in 248..292 -> "West"
            in 293..337 -> "Northwest"
            else -> "North"
        }
        compassLabel.text = "Facing $direction"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
