package com.bleelblep.glyphsharge.data.model

import androidx.room.*
import java.util.concurrent.TimeUnit

/**
 * Model representing a single charging session.
 */
@Entity(tableName = "charging_sessions")
data class ChargingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,           // epoch millis when charging started
    val endTimestamp: Long,             // epoch millis when charging stopped (0 if ongoing)
    val startPercentage: Int,           // battery % at start
    val endPercentage: Int,             // battery % at end (same as start if ongoing)
    val avgTemperatureC: Float,          // average battery temperature
    val sampleCount: Int = 1            // number of temperature samples aggregated
) {
    @Ignore
    val durationMillis: Long = if (endTimestamp == 0L) 0 else endTimestamp - startTimestamp
    @Ignore
    val chargeDelta: Int = endPercentage - startPercentage

    /** Composite health score (0-100) and tier */
    @Ignore
    val healthScore: Int

    @Ignore
    val health: HealthStatus

    init {
        // --- Temperature penalty ---
        val tempPenalty = if (avgTemperatureC > 35) ((avgTemperatureC - 35) * 2).toInt() else 0

        // --- Charging speed penalty (based on % gained per minute) ---
        val durationMinutes = if (durationMillis > 0) durationMillis / 60000.0 else 0.0
        val pctPerMin = if (durationMinutes > 0) chargeDelta / durationMinutes else 0.0
        val speedPenalty = if (pctPerMin < 0.7) (((0.7 - pctPerMin) / 0.1).coerceAtLeast(0.0)).toInt() else 0

        // For historical sessions we skip battery-age penalty (not available)
        val batteryAgePenalty = 0

        var score = 100 - tempPenalty - speedPenalty - batteryAgePenalty
        if (score < 0) score = 0
        if (score > 100) score = 100

        healthScore = score

        health = when (score) {
            in 80..100 -> HealthStatus.EXCELLENT
            in 60..79 -> HealthStatus.GOOD
            in 40..59 -> HealthStatus.FAIR
            in 20..39 -> HealthStatus.POOR
            else -> HealthStatus.CRITICAL
        }
    }

    fun formattedDuration(): String {
        val hrs = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val mins = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60

        return when {
            hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
            hrs > 0 -> "${hrs}h"
            else -> "${mins}m"
        }
    }
}

enum class HealthStatus(val emoji: String) {
    EXCELLENT("🟢"),
    GOOD("😊"),
    FAIR("😐"),
    POOR("😟"),
    CRITICAL("🔴")
} 