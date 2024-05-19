package com.example.tp7

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel
        createNotificationChannel()

        MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger ?: return, "com.example.torch_service").setMethodCallHandler { call, result ->
            when (call.method) {
                "startService" -> {
                    startService()
                    result.success(null)
                }
                "stopService" -> {
                    stopService()
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "proximity_service_channel", // Change this to match the ID in TorchService.kt
                "Proximity Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startService() {
        // Start your service here
        startService(Intent(this, TorchService::class.java))
    }

    private fun stopService() {
        // Stop your service here
        stopService(Intent(this, TorchService::class.java))
    }
}
