package com.bleelblep.glyphsharge.glyph

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates exclusive access to the Glyph LEDs across independent features (services).
 * Only one [GlyphFeature] may hold the lock at any given time.  When the first feature
 * acquires the lock we guarantee a Glyph session is active via [GlyphManager].  When the
 * last feature releases the lock we optionally turn the LEDs off.
 */
@Singleton
class GlyphFeatureCoordinator @Inject constructor(
    private val glyphManager: GlyphManager
) {
    private val lock = Mutex()

    private val _currentOwner = MutableStateFlow<GlyphFeature?>(null)
    val currentOwner: StateFlow<GlyphFeature?> = _currentOwner.asStateFlow()

    /**
     * Attempts to acquire the LED lock for [owner].  Waits up to [timeoutMs].
     * Returns true on success, false if the LEDs are busy.
     */
    suspend fun acquire(owner: GlyphFeature, timeoutMs: Long = 500L): Boolean {
        val success = withTimeoutOrNull(timeoutMs) {
            lock.lock() // suspends until available or timeout
            true
        } ?: false

        if (success) {
            _currentOwner.value = owner
            if (!glyphManager.isSessionActive) {
                glyphManager.forceEnsureSession()
            }
        }
        return success
    }

    /**
     * Releases the lock if [owner] currently owns it.
     */
    fun release(owner: GlyphFeature) {
        if (_currentOwner.value == owner && lock.isLocked) {
            _currentOwner.value = null
            lock.unlock()
            // Turn LEDs off to leave clean slate for next feature
            runCatching { glyphManager.turnOffAll() }
        }
    }
}

/** All high level app features that can drive Glyph LEDs. */
enum class GlyphFeature {
    PULSE_LOCK,
    POWER_PEEK,
    GLYPH_GUARD,
    BATTERY_STORY,
    MANUAL_DEMO,
    LOW_BATTERY,
} 