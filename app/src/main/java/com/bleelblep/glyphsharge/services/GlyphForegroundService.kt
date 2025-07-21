package com.bleelblep.glyphsharge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bleelblep.glyphsharge.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.bleelblep.glyphsharge.glyph.GlyphFeatureCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository

/**
 * Permanent foreground service that keeps the app listed in Quick Settings "Active apps".
 * It owns no LED logic itself; real animations are triggered by other components which
 * acquire the [GlyphFeatureCoordinator] mutex.
 */
@AndroidEntryPoint
class GlyphForegroundService : Service() {

    @Inject
    lateinit var coordinator: GlyphFeatureCoordinator

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If user has disabled Glyph Service, stop immediately.
        if (!settingsRepository.getGlyphServiceEnabled()) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        // Promote to foreground immediately
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Glyph Foreground Service",
                NotificationManager.IMPORTANCE_MIN // silent
            )
            channel.setShowBadge(false)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Glyph features ready")
            .setContentText("LED features will activate automatically")
            .setSmallIcon(R.drawable._44)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // Restart only if Glyph Service is still enabled
        if (settingsRepository.getGlyphServiceEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, GlyphForegroundService::class.java))
            } else {
                startService(Intent(this, GlyphForegroundService::class.java))
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 4242
        private const val NOTIF_CHANNEL_ID = "GlyphForegroundServiceChannel"
    }
} 