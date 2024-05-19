package com.example.tp7

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.content.pm.ServiceInfo

import androidx.core.app.NotificationCompat

class TorchService : Service(), SensorEventListener {
    private val TAG = "TorchService"
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var flashlightOn: Boolean = false

    private val notificationId = 1001 // Define notification ID as a constant

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TorchService onCreate()")
        // Initialize sensor manager and proximity sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        // Initialize camera manager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        cameraId = cameraManager?.cameraIdList?.firstOrNull()

        // Create the notification
        val notification = createNotification()
        Log.d(TAG, "Notification created")

        // Start the service in the foreground
        startForeground(notificationId, notification)
        Log.d(TAG, "Service started in foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification
        val notification = createNotification()

        // Start the service in the foreground
        startForeground(notificationId, notification)

        // Register sensor listener
        proximitySensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister sensor listener
        sensorManager.unregisterListener(this)

        // Stop the service from foreground
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PROXIMITY) {
                val maximumRangeFloat = proximitySensor?.maximumRange?.toFloat() ?: 0f
                val sensorCovered = it.values[0] < maximumRangeFloat

                if (sensorCovered) {
                    if (!flashlightOn) {
                        // Proximity sensor covered and flashlight off, turn on flashlight
                        try {
                            cameraManager?.setTorchMode(cameraId!!, true)
                            flashlightOn = true // Update flashlight state
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                            // Handle error here
                        }
                    } else {
                        // Proximity sensor not covered and flashlight on, turn off flashlight
                        try {
                            cameraManager?.setTorchMode(cameraId!!, false)
                            flashlightOn = false // Update flashlight state
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                            // Handle error here
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "proximity_service_channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                "Proximity Service Channel",
                importance
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification= NotificationCompat.Builder(this, channelId)
            .setContentTitle("Proximity Service")
            .setContentText("Proximity Service is running...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // This makes the notification non-dismissable by the user
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(1001, notification)
        }



        return notification // Return the created notification
    }
}
