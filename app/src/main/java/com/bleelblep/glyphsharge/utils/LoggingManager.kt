package com.bleelblep.glyphsharge.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object LoggingManager {
    private var isLoggingEnabled = false
    private val logMutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    private lateinit var context: Context
    private lateinit var logFile: File
    private lateinit var c15LogFile: File
    
    // Cache for channel names to avoid repeated calculations
    private val channelNameCache = ConcurrentHashMap<Int, String>()
    
    // Pre-compiled regex patterns for C15-related checks
    private val c15RelatedPatterns = setOf(
        "c15", "c14", "channel 17", "channel 16",
        "hardware channel 17", "hardware channel 16",
        "final state", "breathing", "exhale", "glyph",
        "frame", "session", "toggle", "animate",
        "builder", "service", "register", "turnoff",
        "brightness", "channel", "isolation", "excluded",
        "dim state", "hold phase", "sdk"
    )
    
    fun initialize(context: Context) {
        this.context = context
        createLogFiles()
    }
    
    private fun createLogFiles() {
        val logsDir = File(context.getExternalFilesDir(null), "logs").apply {
            if (!exists()) mkdirs()
        }
        
        val timestamp = fileDateFormat.format(Date())
        logFile = File(logsDir, "glyphzen_debug_$timestamp.log")
        c15LogFile = File(logsDir, "glyphzen_c15_debug_$timestamp.log")
        
        // Create files if they don't exist
        if (!logFile.exists()) {
            logFile.createNewFile()
            writeToFile(logFile, "=== GlyphZen Debug Log Started at ${dateFormat.format(Date())} ===\n")
        }
        if (!c15LogFile.exists()) {
            c15LogFile.createNewFile()
            writeToFile(c15LogFile, "=== GlyphZen C15 Debug Log Started at ${dateFormat.format(Date())} ===\n")
        }
    }
    
    fun setLoggingEnabled(enabled: Boolean) {
        isLoggingEnabled = enabled
        if (enabled) {
            log("SYSTEM", "Logging enabled")
        } else {
            log("SYSTEM", "Logging disabled")
        }
    }
    
    fun isLoggingEnabled(): Boolean = isLoggingEnabled
    
    fun log(tag: String, message: String) {
        if (!isLoggingEnabled) return
        
        CoroutineScope(Dispatchers.IO).launch {
            logMutex.withLock {
                val timestamp = dateFormat.format(Date())
                val logEntry = "[$timestamp] [$tag] $message\n"
                
                // Write to main log
                writeToFile(logFile, logEntry)
                
                // Write to C15 log if relevant
                if (isC15Related(tag, message)) {
                    writeToFile(c15LogFile, logEntry)
                }
                
                // Also log to Android logcat
                android.util.Log.d("GlyphZen_$tag", message)
            }
        }
    }
    
    private fun isC15Related(tag: String, message: String): Boolean {
        val lowerMessage = message.lowercase()
        val lowerTag = tag.lowercase()
        
        return c15RelatedPatterns.any { pattern ->
            lowerMessage.contains(pattern) || lowerTag.contains(pattern)
        }
    }
    
    fun logSDKOperation(operation: String, details: String) {
        log("SDK", "$operation: $details")
    }
    
    fun logSessionState(state: String, details: String = "") {
        log("SESSION", "$state${if (details.isNotEmpty()) " - $details" else ""}")
    }
    
    fun logFrameOperation(operation: String, channels: List<Int>, brightness: Int? = null) {
        val brightnessInfo = brightness?.let { " brightness=$it" } ?: ""
        log("FRAME", "$operation: channels=$channels$brightnessInfo")
    }
    
    fun logChannelOperation(channel: Int, brightness: Int, operation: String) {
        val channelName = getChannelName(channel)
        log("CHANNEL", "$operation: $channelName (channel $channel) brightness=$brightness")
    }
    
    fun logHardwareResponse(operation: String, success: Boolean, error: String? = null) {
        val status = if (success) "SUCCESS" else "FAILED"
        val errorInfo = error?.let { " - Error: $it" } ?: ""
        log("HARDWARE", "$operation: $status$errorInfo")
    }
    
    // ===== SPECIALIZED C14/C15 DEBUGGING FUNCTIONS =====
    
    fun logC15Investigation(phase: String, details: String) {
        log("C15_INVESTIGATION", "$phase: $details")
    }
    
    fun logIsolationTest(testType: String, channel: Int, brightness: Int, expected: String) {
        val channelName = getChannelName(channel)
        log("ISOLATION_TEST", "$testType: $channelName (ch$channel) brightness=$brightness - Expected: $expected")
    }
    
    fun logFrameExclusion(excludedChannels: List<Int>, reason: String) {
        val channelNames = excludedChannels.map { "${getChannelName(it)}(ch$it)" }
        log("FRAME_EXCLUSION", "Excluded channels: $channelNames - Reason: $reason")
    }
    
    fun logBreathingPhase(phase: String, step: Int, totalSteps: Int, brightness: Int, affectedChannels: List<Int>) {
        val channelNames = affectedChannels.map { "${getChannelName(it)}(ch$it)" }
        log("BREATHING_PHASE", "$phase step $step/$totalSteps brightness=$brightness channels=$channelNames")
    }
    
    fun logFinalStateAnalysis(analysis: String, c14State: String, c15State: String) {
        log("FINAL_STATE_ANALYSIS", "$analysis - C14: $c14State, C15: $c15State")
    }
    
    fun logHardwareChannelMapping(logicalName: String, hardwareChannel: Int, expectedBehavior: String) {
        log("CHANNEL_MAPPING", "$logicalName -> Hardware Channel $hardwareChannel - Expected: $expectedBehavior")
    }
    
    fun logTestSequence(testName: String, step: String, details: String) {
        log("TEST_SEQUENCE", "[$testName] $step: $details")
    }
    
    fun logBugReproduction(scenario: String, observed: String, expected: String) {
        log("BUG_REPRODUCTION", "Scenario: $scenario | Observed: $observed | Expected: $expected")
    }
    
    fun logSDKFrameDetails(frameChannels: List<Int>, excludedChannels: List<Int>, brightness: Int) {
        val includedNames = frameChannels.map { "${getChannelName(it)}(ch$it)" }
        val excludedNames = excludedChannels.map { "${getChannelName(it)}(ch$it)" }
        log("SDK_FRAME_DETAILS", "Frame built - Included: $includedNames, Excluded: $excludedNames, Brightness: $brightness")
    }
    
    fun logAnimationCycle(animationType: String, cycle: Int, totalCycles: Int, phase: String, duration: Long) {
        log("ANIMATION_CYCLE", "$animationType cycle $cycle/$totalCycles - $phase phase (${duration}ms)")
    }
    
    fun logHoldPhaseAnalysis(duration: Long, maintainedChannels: List<Int>, brightness: Int) {
        val channelNames = maintainedChannels.map { "${getChannelName(it)}(ch$it)" }
        log("HOLD_PHASE", "Duration: ${duration}ms, Maintained channels: $channelNames at brightness $brightness")
    }
    
    fun logExhalePhaseAnalysis(step: Int, totalSteps: Int, brightness: Int, c14Brightness: Int, c15Brightness: Int) {
        log("EXHALE_ANALYSIS", "Step $step/$totalSteps - General brightness: $brightness, C14: $c14Brightness, C15: $c15Brightness")
    }
    
    fun logDeviceSpecificBehavior(deviceModel: String, behavior: String, channels: List<Int>) {
        val channelNames = channels.map { "${getChannelName(it)}(ch$it)" }
        log("DEVICE_BEHAVIOR", "$deviceModel: $behavior - Affected channels: $channelNames")
    }
    
    fun logProgressiveIsolation(isolationLevel: String, activeChannels: List<Int>, excludedChannels: List<Int>) {
        val activeNames = activeChannels.map { "${getChannelName(it)}(ch$it)" }
        val excludedNames = excludedChannels.map { "${getChannelName(it)}(ch$it)" }
        log("PROGRESSIVE_ISOLATION", "$isolationLevel - Active: $activeNames, Excluded: $excludedNames")
    }
    
    fun logCleanupOperation(operation: String, affectedChannels: List<Int>, success: Boolean) {
        val channelNames = affectedChannels.map { "${getChannelName(it)}(ch$it)" }
        val status = if (success) "SUCCESS" else "FAILED"
        log("CLEANUP", "$operation: $channelNames - $status")
    }
    
    fun logTimingAnalysis(operation: String, expectedDuration: Long, actualDuration: Long, variance: Long) {
        log("TIMING_ANALYSIS", "$operation - Expected: ${expectedDuration}ms, Actual: ${actualDuration}ms, Variance: ${variance}ms")
    }
    
    private fun getChannelName(channel: Int): String {
        return channelNameCache.getOrPut(channel) {
            when (channel) {
                0 -> "A1"
                1 -> "A2"
                2 -> "B1"
                in 3..18 -> "C1_${channel - 2}" // C1_1 to C1_16
                in 19..23 -> "C${channel - 17}" // C2 to C6
                24 -> "E1"
                in 25..32 -> "D1_${channel - 24}" // D1_1 to D1_8
                else -> "Unknown"
            }
        }
    }
    
    private fun writeToFile(file: File, content: String) {
        try {
            FileWriter(file, true).use { writer ->
                writer.write(content)
                writer.flush()
            }
        } catch (e: Exception) {
            android.util.Log.e("LoggingManager", "Error writing to log file: ${e.message}")
        }
    }
    
    fun exportLogs(): String {
        return try {
            val logContent = StringBuilder()
            
            // Read main log file
            if (logFile.exists()) {
                logContent.append("=== Main Debug Log ===\n")
                logContent.append(logFile.readText())
                logContent.append("\n")
            }
            
            // Read C15 log file
            if (c15LogFile.exists()) {
                logContent.append("=== C15 Debug Log ===\n")
                logContent.append(c15LogFile.readText())
            }
            
            logContent.toString()
        } catch (e: Exception) {
            "Error exporting logs: ${e.message}"
        }
    }
    
    fun shareLogs() {
        try {
            val logContent = exportLogs()
            val tempFile = File(context.cacheDir, "glyphzen_logs.txt")
            tempFile.writeText(logContent)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "GlyphZen Debug Logs")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Logs"))
        } catch (e: Exception) {
            android.util.Log.e("LoggingManager", "Error sharing logs: ${e.message}")
        }
    }
} 