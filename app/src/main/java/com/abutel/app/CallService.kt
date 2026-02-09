package com.abutel.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CallService : Service() {

    companion object {
        const val ACTION_START_INCOMING = "com.abutel.app.START_INCOMING"
        const val ACTION_START_OUTGOING = "com.abutel.app.START_OUTGOING"
        const val CHANNEL_ID = "abutel_call_channel"
        const val NOTIFICATION_ID = 1
    }

    private val binder = LocalBinder()
    private var isCallActive = false

    inner class LocalBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_INCOMING -> {
                val callerName = intent.getStringExtra("CALLER_NAME") ?: "Desconocido"
                showIncomingCallNotification(callerName)
                launchIncomingActivity(callerName)
            }
            ACTION_START_OUTGOING -> {
                val contactName = intent.getStringExtra("CONTACT_NAME") ?: "Desconocido"
                startForegroundCallNotification(contactName)
                // Aquí iría la lógica de WebRTC real
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Llamadas Abutel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para llamadas activas"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundCallNotification(contactName: String): Notification {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Llamada activa con $contactName")
            .setContentText("Tocando para volver")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return notification
    }

    private fun showIncomingCallNotification(callerName: String) {
        createNotificationChannel()
        
        // Intent para abrir la actividad de llamada entrada en pantalla completa
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("CALLER_NAME", callerName)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Llamada entrante")
            .setContentText(callerName)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Clave para heads-up
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun launchIncomingActivity(callerName: String) {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("CALLER_NAME", callerName)
        }
        startActivity(intent)
    }

    fun answerCall() {
        isCallActive = true
        // Lógica WebRTC para aceptar stream
    }

    fun hangUp() {
        isCallActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
