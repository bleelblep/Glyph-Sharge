package com.bleelblep.glyphsharge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.bleelblep.glyphsharge.glyph.GlyphFeature
import javax.inject.Inject

/**
 * Foreground service that listens for the device being unlocked and plays the
 * user-chosen Glow Gate glyph animation (and optional sound).
 */
@AndroidEntryPoint
class PulseLockService : Service() {

    companion object {
        private const val TAG = "PulseLockService"
        private const val NOTIF_CHANNEL_ID = "PulseLockServiceChannel"
        private const val NOTIF_ID = 1010
        const val ACTION_START = "com.bleelblep.glyphsharge.PULSE_LOCK_START"
        const val ACTION_STOP = "com.bleelblep.glyphsharge.PULSE_LOCK_STOP"
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var glyphAnimationManager: GlyphAnimationManager

    @Inject
    lateinit var featureCoordinator: com.bleelblep.glyphsharge.glyph.GlyphFeatureCoordinator

    private var mediaPlayer: MediaPlayer? = null
    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                Log.d(TAG, "Received ACTION_USER_PRESENT – playing Glow Gate sequence")
                playPulseLockSequence()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PulseLockService created")

        // Register the broadcast receiver for unlock events
        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground immediately to satisfy Android's 10-second FGS requirement
        // (it is safe to call this multiple times; later calls simply update the notification).
        startForeground(NOTIF_ID, buildNotification())

        when (intent?.action) {
            ACTION_STOP -> {
                stopAudio() // Ensure audio is stopped when service is stopped
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Check if glow gate is enabled after starting foreground
        if (!settingsRepository.isPulseLockEnabled()) {
            stopAudio() // Ensure audio is stopped if glow gate is disabled
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        // Check if glyph service is enabled after starting foreground
        if (!settingsRepository.getGlyphServiceEnabled()) {
            stopAudio() // Ensure audio is stopped if glyph service is disabled
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        // Service waits for ACTION_USER_PRESENT; nothing foreground yet
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Glow Gate Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("✨ Glow Gate Active")
            .setContentText("Unlocking will play your chosen animation.")
            .setSmallIcon(R.drawable._44)
            .setOngoing(true)
            .build()
    }

    private fun playPulseLockSequence() {
        scope.launch {
            try {
                // Double check glow gate is still enabled before playing
                if (!settingsRepository.isPulseLockEnabled()) {
                    Log.d(TAG, "Glow Gate disabled, stopping sequence")
                    stopAudio()
                    return@launch
                }

                // Double check glyph service is still enabled before playing
                if (!settingsRepository.getGlyphServiceEnabled()) {
                    Log.d(TAG, "Glyph Service disabled, stopping sequence")
                    stopAudio()
                    return@launch
                }

                // Check if currently in quiet hours
                if (settingsRepository.isCurrentlyInQuietHours()) {
                    Log.d(TAG, "Glow Gate blocked by quiet hours")
                    return@launch
                }

                val animationId = settingsRepository.getPulseLockAnimationId()
                val audioEnabled = settingsRepository.isPulseLockAudioEnabled()
                val audioUriStr = settingsRepository.getPulseLockAudioUri()
                val audioOffset = settingsRepository.getPulseLockAudioOffset()
                val duration = settingsRepository.getPulseLockDuration()

                Log.d(TAG, "GlowGate sequence - Animation: $animationId, Audio enabled: $audioEnabled, URI: $audioUriStr, Offset: ${audioOffset}ms")

                // Promote to foreground for the duration of the sequence
                startForeground(NOTIF_ID, buildNotification())

                // Acquire LED lock before starting animation/audio
                if (!featureCoordinator.acquire(GlyphFeature.PULSE_LOCK)) {
                    Log.d(TAG, "Glow Gate skipped – LEDs busy by ${featureCoordinator.currentOwner.value}")
                    return@launch
                }

                // Launch animation in separate coroutine so we can overlap with sound
                val animJob = launch(Dispatchers.Default) {
                    glyphAnimationManager.playPulseLockAnimation(animationId)
                }

                // Launch duration timer that will stop the animation when time expires
                val durationJob = launch {
                    delay(duration)
                    Log.d(TAG, "Duration limit reached, stopping animation and audio")
                    glyphAnimationManager.stopAnimations()
                    stopAudio()
                }

                // Handle sound timing
                if (audioEnabled && !audioUriStr.isNullOrEmpty()) {
                    try {
                        // Verify URI is still accessible before attempting playback
                        applicationContext.contentResolver.openInputStream(Uri.parse(audioUriStr))?.use {
                            if (audioOffset < 0) {
                                // Play sound first, then wait for offset, then start animation
                                Log.d(TAG, "Playing audio ${-audioOffset}ms before animation")
                                playAudio(audioUriStr)
                                delay(-audioOffset)
                                animJob.join()
                            } else if (audioOffset > 0) {
                                // Start animation, wait for offset, then play sound
                                Log.d(TAG, "Playing audio ${audioOffset}ms after animation starts")
                                delay(audioOffset)
                                playAudio(audioUriStr)
                                animJob.join()
                            } else {
                                // Play simultaneously (offset = 0)
                                Log.d(TAG, "Playing audio simultaneously with animation")
                                playAudio(audioUriStr)
                                animJob.join()
                            }
                        } ?: run {
                            Log.w(TAG, "Audio URI is no longer accessible: $audioUriStr")
                            animJob.join()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error accessing audio URI: $audioUriStr", e)
                        animJob.join()
                    }
                } else {
                    Log.d(TAG, "Audio disabled or no URI - playing animation only")
                    animJob.join()
                }

                // Cancel the duration timer since animation completed naturally
                durationJob.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error running Glow Gate sequence", e)
            } finally {
                featureCoordinator.release(GlyphFeature.PULSE_LOCK)
                // Remove notification and demote service once done. Keep service running so it
                // continues to listen for future unlock events; just drop foreground status.
                stopForeground(true)
                stopAudio()
            }
        }
    }

    private fun playAudio(uriStr: String) {
        stopAudio()
        try {
            val uri = Uri.parse(uriStr)
            Log.d(TAG, "Attempting to play audio from URI: $uri")
            
            // Check if we have permission to access this URI
            try {
                applicationContext.contentResolver.openInputStream(uri)?.use { 
                    Log.d(TAG, "URI is accessible, proceeding with MediaPlayer")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "No permission to access URI: $uri", e)
                return
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioStreamType(android.media.AudioManager.STREAM_NOTIFICATION)
                setOnPreparedListener { 
                    Log.d(TAG, "Audio prepared, starting playback")
                    it.start() 
                }
                setOnCompletionListener { 
                    Log.d(TAG, "Audio playback completed")
                    stopAudio() 
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    stopAudio()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio from URI: $uriStr", e)
        }
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            try {
                it.stop()
                it.release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(unlockReceiver)
        stopAudio()
        // Ensure any lingering notification is removed
        stopForeground(true)
        serviceJob.cancel()
        Log.d(TAG, "PulseLockService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, PulseLockService::class.java).apply { action = ACTION_START })
        } else {
            startService(Intent(this, PulseLockService::class.java).apply { action = ACTION_START })
        }
    }
} 