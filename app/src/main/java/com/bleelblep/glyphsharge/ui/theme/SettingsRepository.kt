package com.bleelblep.glyphsharge.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.bleelblep.glyphsharge.ui.components.GlyphGuardMode

/**
 * Repository for persisting app settings using SharedPreferences
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "glyphzen_settings",
        Context.MODE_PRIVATE
    )

    // Automatically apply baseline defaults on repository creation.
    init {
        applyFirstRunDefaults()

        // Apply data migrations for users coming from older builds where incorrect
        // defaults might already have been backed-up or written.
        applyVersionMigrations()
    }

    companion object {
        private const val TAG = "SettingsRepository"
        private const val KEY_FONT_VARIANT = "font_variant"
        private const val KEY_USE_CUSTOM_FONTS = "use_custom_fonts"
        private const val KEY_FONT_SIZE_DISPLAY_SCALE = "font_size_display_scale"
        private const val KEY_FONT_SIZE_TITLE_SCALE = "font_size_title_scale"
        private const val KEY_FONT_SIZE_BODY_SCALE = "font_size_body_scale"
        private const val KEY_FONT_SIZE_LABEL_SCALE = "font_size_label_scale"
        private const val KEY_FONT_SIZE_CUSTOMIZED = "font_size_customized"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_THEME_STYLE = "theme_style"
        private const val KEY_GLYPH_SERVICE_ENABLED = "glyph_service_enabled"
        private const val KEY_POWER_PEEK_ENABLED = "power_peek_enabled"
        private const val KEY_SHAKE_THRESHOLD = "shake_threshold"
        private const val KEY_DISPLAY_DURATION = "display_duration"
        private const val KEY_VIBRATION_INTENSITY = "vibration_intensity"
        private const val DEFAULT_VIBRATION_INTENSITY = 0.66f // Medium intensity (66% = ~170/255)
        private const val KEY_GLYPH_GUARD_DURATION = "glyph_guard_duration"
        private const val DEFAULT_GLYPH_GUARD_DURATION = 30000L // 30 seconds
        private const val KEY_GLYPH_GUARD_SOUND_TYPE = "glyph_guard_sound_type"
        private const val DEFAULT_GLYPH_GUARD_SOUND_TYPE = "ALARM" // ALARM, NOTIFICATION, RINGTONE
        private const val KEY_GLYPH_GUARD_CUSTOM_RINGTONE_URI = "glyph_guard_custom_ringtone_uri"
        private const val KEY_GLYPH_GUARD_SOUND_ENABLED = "glyph_guard_sound_enabled"
        private const val DEFAULT_GLYPH_GUARD_SOUND_ENABLED = true
        private const val KEY_GLYPH_GUARD_ENABLED = "glyph_guard_enabled" // Persist Glyph Guard on/off state
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        // Battery Story enable/disable
        private const val KEY_BATTERY_STORY_ENABLED = "battery_story_enabled"
        // Glow Gate settings keys
        private const val KEY_PULSE_LOCK_ENABLED = "pulse_lock_enabled"
        private const val KEY_PULSE_LOCK_ANIMATION_ID = "pulse_lock_animation_id"
        private const val KEY_PULSE_LOCK_AUDIO_URI = "pulse_lock_audio_uri"
        private const val KEY_PULSE_LOCK_AUDIO_ENABLED = "pulse_lock_audio_enabled"
        private const val KEY_PULSE_LOCK_AUDIO_OFFSET = "pulse_lock_audio_offset"
        private const val KEY_PULSE_LOCK_DURATION = "pulse_lock_duration"
        private const val DEFAULT_PULSE_LOCK_DURATION = 5000L // 5 seconds

        // Low-Battery Alert settings keys
        private const val KEY_LOW_BATTERY_ENABLED = "low_battery_enabled"
        private const val KEY_LOW_BATTERY_THRESHOLD = "low_battery_threshold" // Int percentage
        private const val KEY_LOW_BATTERY_ANIMATION_ID = "low_battery_animation_id"
        private const val KEY_LOW_BATTERY_AUDIO_URI = "low_battery_audio_uri"
        private const val KEY_LOW_BATTERY_AUDIO_ENABLED = "low_battery_audio_enabled"
        private const val KEY_LOW_BATTERY_AUDIO_OFFSET = "low_battery_audio_offset"
        private const val KEY_LOW_BATTERY_DURATION = "low_battery_duration"
        private const val DEFAULT_LOW_BATTERY_DURATION = 10000L // 10 seconds

        private const val DEFAULT_LOW_BATTERY_THRESHOLD = 20 // 20%

        // Quiet Hours settings keys
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START_HOUR = "quiet_hours_start_hour"
        private const val KEY_QUIET_HOURS_START_MINUTE = "quiet_hours_start_minute"
        private const val KEY_QUIET_HOURS_END_HOUR = "quiet_hours_end_hour"
        private const val KEY_QUIET_HOURS_END_MINUTE = "quiet_hours_end_minute"
        private const val DEFAULT_QUIET_HOURS_START_HOUR = 22 // 10 PM
        private const val DEFAULT_QUIET_HOURS_START_MINUTE = 0
        private const val DEFAULT_QUIET_HOURS_END_HOUR = 7 // 7 AM
        private const val DEFAULT_QUIET_HOURS_END_MINUTE = 0

        // Predefined shake intensity levels (in m/s²)
        const val SHAKE_SOFT = 12.0f
        const val SHAKE_EASY = 15.0f
        const val SHAKE_MEDIUM = 18.0f
        const val SHAKE_HARD = 22.0f
        const val SHAKE_HARDEST = 28.0f

        // Glyph Guard Settings
        private const val PREF_GLYPH_GUARD_ENABLED = "glyph_guard_enabled"
        private const val PREF_GLYPH_GUARD_DURATION = "glyph_guard_duration"
        private const val PREF_GLYPH_GUARD_SOUND_ENABLED = "glyph_guard_sound_enabled"
        private const val PREF_GLYPH_GUARD_SOUND_TYPE = "glyph_guard_sound_type"
        private const val PREF_GLYPH_GUARD_CUSTOM_RINGTONE_URI = "glyph_guard_custom_ringtone_uri"
        private const val PREF_GLYPH_GUARD_ALERT_MODE = "glyph_guard_alert_mode"

        // Flag indicating that the app has already applied its first-run default settings.
        private const val KEY_FIRST_RUN_COMPLETED = "first_run_completed"

        // One-time migration flag – increment when we need to patch existing installs
        private const val KEY_LAST_MIGRATED_VERSION = "last_migrated_version"
    }

    /**
     * Apply default preference values on the very first launch of the app. This is a safeguard
     * to guarantee that features which should be *opt-in* (like Power Peek) start in the disabled
     * state and that the headline font is the initial typeface. Historically some build variants
     * shipped with incorrect `SharedPreferences` defaults which could leave these values in an
     * unexpected state. By committing an explicit baseline on first run we guarantee a clean
     * experience for every fresh install while leaving existing user choices untouched.
     */
    private fun applyFirstRunDefaults() {
        // Exit early if the defaults have already been written.
        if (prefs.getBoolean(KEY_FIRST_RUN_COMPLETED, false)) return

        prefs.edit().apply {
            putBoolean(KEY_POWER_PEEK_ENABLED, false)
            putBoolean(KEY_GLYPH_SERVICE_ENABLED, false)
            putBoolean(KEY_GLYPH_GUARD_ENABLED, false)
            putBoolean(KEY_BATTERY_STORY_ENABLED, false)
            putBoolean(KEY_PULSE_LOCK_ENABLED, false)
            putBoolean(KEY_LOW_BATTERY_ENABLED, false)
            putBoolean(KEY_GLYPH_GUARD_SOUND_ENABLED, false)
            putString(KEY_FONT_VARIANT, FontVariant.HEADLINE.name)
            putBoolean(KEY_USE_CUSTOM_FONTS, true)
            putFloat(KEY_FONT_SIZE_DISPLAY_SCALE, 1.0f)
            putFloat(KEY_FONT_SIZE_TITLE_SCALE, 1.0f)
            putFloat(KEY_FONT_SIZE_BODY_SCALE, 1.0f)
            putFloat(KEY_FONT_SIZE_LABEL_SCALE, 1.0f)
            apply()
        }

        // Mark baseline setup as complete so we don't overwrite user preferences on subsequent launches.
        prefs.edit().putBoolean(KEY_FIRST_RUN_COMPLETED, true).apply()

        Log.d(TAG, "First-run defaults applied (PowerPeek=disabled, Font=HEADLINE)")
    }

    /**
     * Migration routine that applies fixes once per app version. We only touch the keys that
     * were historically wrong so we don't wipe the user's entire configuration.
     */
    private fun applyVersionMigrations() {
        val lastMigrated = prefs.getInt(KEY_LAST_MIGRATED_VERSION, 0)

        // Migration to version 109: force-reset all feature toggles to OFF and reset font to HEADLINE.
        if (lastMigrated < 109) {
            prefs.edit()
                .putBoolean(KEY_POWER_PEEK_ENABLED, false)
                .putBoolean(KEY_GLYPH_SERVICE_ENABLED, false)
                .putBoolean(KEY_GLYPH_GUARD_ENABLED, false)
                .putBoolean(KEY_BATTERY_STORY_ENABLED, false)
                .putBoolean(KEY_PULSE_LOCK_ENABLED, false)
                .putBoolean(KEY_LOW_BATTERY_ENABLED, false)
                .putBoolean(KEY_GLYPH_GUARD_SOUND_ENABLED, false)
                .putString(KEY_FONT_VARIANT, FontVariant.HEADLINE.name)
                .putBoolean(KEY_USE_CUSTOM_FONTS, true)
                .putFloat(KEY_FONT_SIZE_DISPLAY_SCALE, 1.0f)
                .putFloat(KEY_FONT_SIZE_TITLE_SCALE, 1.0f)
                .putFloat(KEY_FONT_SIZE_BODY_SCALE, 1.0f)
                .putFloat(KEY_FONT_SIZE_LABEL_SCALE, 1.0f)
                .putInt(KEY_LAST_MIGRATED_VERSION, 109)
                .apply()

            Log.d(TAG, "Migration to 109 applied – all feature toggles reset to OFF & font reset to HEADLINE")
        }
    }

    // Font settings
    fun saveFontVariant(variant: FontVariant) {
        Log.d(TAG, "Saving font variant: ${variant.name}")
        val success = prefs.edit()
            .putString(KEY_FONT_VARIANT, variant.name)
            .commit() // Use commit() for immediate persistence
        Log.d(TAG, "Font variant save success: $success")
    }

    fun getFontVariant(): FontVariant {
        val variantName = prefs.getString(KEY_FONT_VARIANT, FontVariant.HEADLINE.name)
        Log.d(TAG, "Retrieved font variant: $variantName")
        return try {
            FontVariant.valueOf(variantName ?: FontVariant.HEADLINE.name)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid font variant: $variantName, using default")
            FontVariant.HEADLINE
        }
    }

    // Custom fonts settings
    fun saveUseCustomFonts(useCustom: Boolean) {
        Log.d(TAG, "Saving use custom fonts: $useCustom")
        val success = prefs.edit()
            .putBoolean(KEY_USE_CUSTOM_FONTS, useCustom)
            .commit()
        Log.d(TAG, "Use custom fonts save success: $success")
    }

    fun getUseCustomFonts(): Boolean {
        val useCustom = prefs.getBoolean(KEY_USE_CUSTOM_FONTS, true)
        Log.d(TAG, "Retrieved use custom fonts: $useCustom")
        return useCustom
    }

    // Font size settings
    fun saveFontSizeSettings(settings: FontSizeSettings) {
        Log.d(TAG, "Saving font size settings: $settings")
        val success = prefs.edit()
            .putFloat(KEY_FONT_SIZE_DISPLAY_SCALE, settings.displayScale)
            .putFloat(KEY_FONT_SIZE_TITLE_SCALE, settings.titleScale)
            .putFloat(KEY_FONT_SIZE_BODY_SCALE, settings.bodyScale)
            .putFloat(KEY_FONT_SIZE_LABEL_SCALE, settings.labelScale)
            .putBoolean(KEY_FONT_SIZE_CUSTOMIZED, true)
            .commit()
        Log.d(TAG, "Font size settings save success: $success")
    }

    fun getFontSizeSettings(): FontSizeSettings {
        val displayScale = prefs.getFloat(KEY_FONT_SIZE_DISPLAY_SCALE, 1.0f)
        val titleScale = prefs.getFloat(KEY_FONT_SIZE_TITLE_SCALE, 1.0f)
        val bodyScale = prefs.getFloat(KEY_FONT_SIZE_BODY_SCALE, 1.0f)
        val labelScale = prefs.getFloat(KEY_FONT_SIZE_LABEL_SCALE, 1.0f)
        
        val settings = FontSizeSettings(displayScale, titleScale, bodyScale, labelScale)
        Log.d(TAG, "Retrieved font size settings: $settings")
        return settings
    }

    fun clearFontSizeCustomization() {
        Log.d(TAG, "Clearing font size customization flag")
        prefs.edit()
            .remove(KEY_FONT_SIZE_DISPLAY_SCALE)
            .remove(KEY_FONT_SIZE_TITLE_SCALE)
            .remove(KEY_FONT_SIZE_BODY_SCALE)
            .remove(KEY_FONT_SIZE_LABEL_SCALE)
            .putBoolean(KEY_FONT_SIZE_CUSTOMIZED, false)
            .apply()
    }

    fun getFontSizeSettingsForFont(fontVariant: FontVariant): FontSizeSettings {
        // Check if user has manually customized font sizes
        val isCustomized = prefs.getBoolean(KEY_FONT_SIZE_CUSTOMIZED, false)
        
        val settings = if (isCustomized) {
            // User has manually customized settings, return them
            getFontSizeSettings()
        } else {
            // User hasn't customized, return font-specific defaults
            FontSizeSettings.getDefaultForFont(fontVariant)
        }
        
        Log.d(TAG, "Font size settings for ${fontVariant.name}: customized=$isCustomized, settings=$settings")
        return settings
    }

    // Theme settings
    fun saveTheme(isDarkTheme: Boolean) {
        Log.d(TAG, "Saving theme: isDarkTheme=$isDarkTheme")
        val success = prefs.edit()
            .putBoolean(KEY_IS_DARK_THEME, isDarkTheme)
            .commit() // Use commit() for immediate persistence
        Log.d(TAG, "Theme save success: $success")
    }

    fun getTheme(): Boolean {
        val theme = prefs.getBoolean(KEY_IS_DARK_THEME, false)
        Log.d(TAG, "Retrieved theme: isDarkTheme=$theme")
        return theme
    }

    // Theme style settings
    fun saveThemeStyle(themeStyle: AppThemeStyle) {
        Log.d(TAG, "Saving theme style: ${themeStyle.name}")
        val success = prefs.edit()
            .putString(KEY_THEME_STYLE, themeStyle.name)
            .commit() // Use commit() for immediate persistence
        Log.d(TAG, "Theme style save success: $success")
    }

    fun getThemeStyle(): AppThemeStyle {
        val styleName = prefs.getString(KEY_THEME_STYLE, AppThemeStyle.CLASSIC.name)
        Log.d(TAG, "Retrieved theme style: $styleName")
        return try {
            AppThemeStyle.valueOf(styleName ?: AppThemeStyle.CLASSIC.name)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid theme style: $styleName, using default")
            AppThemeStyle.CLASSIC
        }
    }

    // Glyph service settings
    fun saveGlyphServiceEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving glyph service enabled: $enabled")
        val success = prefs.edit()
            .putBoolean(KEY_GLYPH_SERVICE_ENABLED, enabled)
            .commit() // Use commit() for immediate persistence
        Log.d(TAG, "Glyph service save success: $success")
    }

    fun getGlyphServiceEnabled(): Boolean {
        val enabled = prefs.getBoolean(KEY_GLYPH_SERVICE_ENABLED, false)
        Log.d(TAG, "Retrieved glyph service enabled: $enabled")
        return enabled
    }

    // Power Peek service settings
    fun savePowerPeekEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving PowerPeek enabled: $enabled")
        val success = prefs.edit()
            .putBoolean(KEY_POWER_PEEK_ENABLED, enabled)
            .commit() // Use commit() for immediate persistence and reliability
        Log.d(TAG, "PowerPeek save success: $success")
        
        // Verify the save worked
        val verified = prefs.getBoolean(KEY_POWER_PEEK_ENABLED, false)
        Log.d(TAG, "PowerPeek save verification: expected=$enabled, actual=$verified")
    }

    fun isPowerPeekEnabled(): Boolean {
        val enabled = prefs.getBoolean(KEY_POWER_PEEK_ENABLED, false)
        Log.d(TAG, "Retrieved PowerPeek enabled: $enabled")
        return enabled
    }

    // PowerPeek Shake Threshold
    fun saveShakeThreshold(threshold: Float) {
        Log.d(TAG, "Saving shake threshold: $threshold")
        val success = prefs.edit().putFloat(KEY_SHAKE_THRESHOLD, threshold).commit()
        Log.d(TAG, "Shake threshold save success: $success")
    }

    fun getShakeThreshold(): Float {
        val threshold = prefs.getFloat(KEY_SHAKE_THRESHOLD, SHAKE_MEDIUM)
        Log.d(TAG, "Retrieved shake threshold: $threshold")
        return threshold
    }

    fun getShakeIntensityLevel(threshold: Float): String {
        return when (threshold) {
            SHAKE_SOFT -> "Soft"
            SHAKE_EASY -> "Easy"
            SHAKE_MEDIUM -> "Medium"
            SHAKE_HARD -> "Hard"
            SHAKE_HARDEST -> "Hardest"
            else -> "Medium"
        }
    }

    // PowerPeek Display Duration
    fun saveDisplayDuration(duration: Long) {
        Log.d(TAG, "Saving display duration: $duration")
        val success = prefs.edit().putLong(KEY_DISPLAY_DURATION, duration).commit()
        Log.d(TAG, "Display duration save success: $success")
    }

    fun getDisplayDuration(): Long {
        val duration = prefs.getLong(KEY_DISPLAY_DURATION, 3000L)
        Log.d(TAG, "Retrieved display duration: $duration")
        return duration
    }

    fun saveVibrationIntensity(intensity: Float) {
        Log.d(TAG, "Saving vibration intensity: $intensity")
        prefs.edit().putFloat(KEY_VIBRATION_INTENSITY, intensity).apply()
        _vibrationIntensityFlow.value = intensity
    }

    private val _vibrationIntensityFlow = androidx.compose.runtime.mutableStateOf(
        prefs.getFloat(KEY_VIBRATION_INTENSITY, DEFAULT_VIBRATION_INTENSITY)
    )
    
    val vibrationIntensityFlow: androidx.compose.runtime.State<Float> = _vibrationIntensityFlow

    fun getVibrationIntensity(): Float {
        val storedValue = prefs.getFloat(KEY_VIBRATION_INTENSITY, DEFAULT_VIBRATION_INTENSITY)
        
        // Migration: If the stored value is in the old range (1-255), convert it to new range (0.0-1.0)
        // This migration only happens once per value, then the converted value is saved
        return if (storedValue > 1.0f) {
            // Old format (Android amplitude 1-255) - convert to 0.0-1.0
            val converted = when {
                storedValue <= 1f -> 0.1f // Minimum non-zero intensity
                storedValue >= 255f -> 1.0f // Maximum intensity
                else -> (storedValue - 1f) / 254f // Scale 1-255 to 0.0-1.0
            }.coerceIn(0.0f, 1.0f)
            
            Log.d(TAG, "Converting old vibration intensity $storedValue to new format: $converted")
            // Save the converted value for future use - this ensures migration only happens once
            saveVibrationIntensity(converted)
            converted
        } else {
            // New format (0.0-1.0 range) - this is correct
            storedValue
        }
    }

    // Glyph Guard Duration settings
    fun saveGlyphGuardDuration(duration: Long) {
        Log.d(TAG, "Saving Glyph Guard duration: $duration")
        prefs.edit().putLong(KEY_GLYPH_GUARD_DURATION, duration).apply()
    }

    fun getGlyphGuardDuration(): Long {
        return prefs.getLong(KEY_GLYPH_GUARD_DURATION, DEFAULT_GLYPH_GUARD_DURATION)
    }

    // Glyph Guard Sound Type settings
    fun saveGlyphGuardSoundType(soundType: String) {
        prefs.edit().putString(KEY_GLYPH_GUARD_SOUND_TYPE, soundType).apply()
    }

    fun getGlyphGuardSoundType(): String {
        return prefs.getString(KEY_GLYPH_GUARD_SOUND_TYPE, DEFAULT_GLYPH_GUARD_SOUND_TYPE) ?: DEFAULT_GLYPH_GUARD_SOUND_TYPE
    }

    // Glyph Guard Custom Ringtone URI
    fun saveGlyphGuardCustomRingtoneUri(uri: String?) {
        prefs.edit().putString(KEY_GLYPH_GUARD_CUSTOM_RINGTONE_URI, uri).apply()
    }

    fun getGlyphGuardCustomRingtoneUri(): String? {
        return prefs.getString(KEY_GLYPH_GUARD_CUSTOM_RINGTONE_URI, null)
    }

    // Glyph Guard Sound Enabled/Disabled
    fun saveGlyphGuardSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLYPH_GUARD_SOUND_ENABLED, enabled).apply()
    }

    fun isGlyphGuardSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_GLYPH_GUARD_SOUND_ENABLED, false)
    }

    // Glyph Guard enabled/disabled
    fun saveGlyphGuardEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving Glyph Guard enabled: $enabled")
        val success = prefs.edit().putBoolean(KEY_GLYPH_GUARD_ENABLED, enabled).commit()
        Log.d(TAG, "Glyph Guard enabled save success: $success")
    }

    fun isGlyphGuardEnabled(): Boolean {
        val enabled = prefs.getBoolean(KEY_GLYPH_GUARD_ENABLED, false)
        Log.d(TAG, "Retrieved Glyph Guard enabled: $enabled")
        return enabled
    }

    // Onboarding state
    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun setOnboardingComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    // Battery Story enabled/disabled
    fun saveBatteryStoryEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving Battery Story enabled: $enabled")
        val success = prefs.edit().putBoolean(KEY_BATTERY_STORY_ENABLED, enabled).commit()
        Log.d(TAG, "Battery Story enabled save success: $success")
    }

    fun isBatteryStoryEnabled(): Boolean {
        val enabled = prefs.getBoolean(KEY_BATTERY_STORY_ENABLED, false)
        Log.d(TAG, "Retrieved Battery Story enabled: $enabled")
        return enabled
    }

    // Glyph Guard Alert Mode
    fun saveGlyphGuardAlertMode(mode: GlyphGuardMode) {
        prefs.edit().putString(PREF_GLYPH_GUARD_ALERT_MODE, mode.name).apply()
    }

    fun getGlyphGuardAlertMode(): GlyphGuardMode {
        val modeName = prefs.getString(PREF_GLYPH_GUARD_ALERT_MODE, GlyphGuardMode.Standard.name)
        return when (modeName) {
            GlyphGuardMode.Stealth.name -> GlyphGuardMode.Stealth
            GlyphGuardMode.Intense.name -> GlyphGuardMode.Intense
            else -> GlyphGuardMode.Standard
        }
    }

    // Glow Gate – enabled/disabled
    fun savePulseLockEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving Glow Gate enabled: $enabled")
        val success = prefs.edit()
            .putBoolean(KEY_PULSE_LOCK_ENABLED, enabled)
            .commit() // Use commit() for immediate persistence to disk
        Log.d(TAG, "Glow Gate enabled save success: $success")
    }

    fun isPulseLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_PULSE_LOCK_ENABLED, false)
    }

    // Glow Gate – selected animation id
    fun savePulseLockAnimationId(id: String) {
        prefs.edit().putString(KEY_PULSE_LOCK_ANIMATION_ID, id).apply()
    }

    fun getPulseLockAnimationId(): String {
        return prefs.getString(KEY_PULSE_LOCK_ANIMATION_ID, "C1") ?: "C1"
    }

    // Glow Gate – custom audio URI (nullable)
    fun savePulseLockAudioUri(uri: String?) {
        prefs.edit().putString(KEY_PULSE_LOCK_AUDIO_URI, uri).apply()
    }

    fun getPulseLockAudioUri(): String? {
        return prefs.getString(KEY_PULSE_LOCK_AUDIO_URI, null)
    }

    // Glow Gate – audio enabled/disabled
    fun savePulseLockAudioEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PULSE_LOCK_AUDIO_ENABLED, enabled).apply()
    }

    fun isPulseLockAudioEnabled(): Boolean {
        return prefs.getBoolean(KEY_PULSE_LOCK_AUDIO_ENABLED, false)
    }

    // Glow Gate – audio offset in ms (positive = after animation start, negative = before)
    fun savePulseLockAudioOffset(offsetMs: Long) {
        prefs.edit().putLong(KEY_PULSE_LOCK_AUDIO_OFFSET, offsetMs).apply()
    }

    fun getPulseLockAudioOffset(): Long {
        return prefs.getLong(KEY_PULSE_LOCK_AUDIO_OFFSET, 0L)
    }

    // Glow Gate – animation duration in ms
    fun savePulseLockDuration(durationMs: Long) {
        prefs.edit().putLong(KEY_PULSE_LOCK_DURATION, durationMs).apply()
    }

    fun getPulseLockDuration(): Long {
        return prefs.getLong(KEY_PULSE_LOCK_DURATION, DEFAULT_PULSE_LOCK_DURATION)
    }

    // ----------------- Low-Battery Alert -----------------
    fun saveLowBatteryEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving Low-Battery Alert enabled: $enabled")
        val success = prefs.edit()
            .putBoolean(KEY_LOW_BATTERY_ENABLED, enabled)
            .commit()
        Log.d(TAG, "Low-Battery Alert enabled save success: $success")
    }

    fun isLowBatteryEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOW_BATTERY_ENABLED, false)
    }

    fun saveLowBatteryThreshold(pct: Int) {
        prefs.edit().putInt(KEY_LOW_BATTERY_THRESHOLD, pct).apply()
    }

    fun getLowBatteryThreshold(): Int {
        return prefs.getInt(KEY_LOW_BATTERY_THRESHOLD, DEFAULT_LOW_BATTERY_THRESHOLD)
    }

    fun saveLowBatteryAnimationId(id: String) {
        prefs.edit().putString(KEY_LOW_BATTERY_ANIMATION_ID, id).apply()
    }

    fun getLowBatteryAnimationId(): String {
        return prefs.getString(KEY_LOW_BATTERY_ANIMATION_ID, "C1") ?: "C1"
    }

    fun saveLowBatteryAudioUri(uri: String?) {
        prefs.edit().putString(KEY_LOW_BATTERY_AUDIO_URI, uri).apply()
    }

    fun getLowBatteryAudioUri(): String? {
        return prefs.getString(KEY_LOW_BATTERY_AUDIO_URI, null)
    }

    fun saveLowBatteryAudioEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_BATTERY_AUDIO_ENABLED, enabled).apply()
    }

    fun isLowBatteryAudioEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOW_BATTERY_AUDIO_ENABLED, false)
    }

    fun saveLowBatteryAudioOffset(offsetMs: Long) {
        prefs.edit().putLong(KEY_LOW_BATTERY_AUDIO_OFFSET, offsetMs).apply()
    }

    fun getLowBatteryAudioOffset(): Long {
        return prefs.getLong(KEY_LOW_BATTERY_AUDIO_OFFSET, 0L)
    }

    // Low-Battery flash duration (in ms)
    fun saveLowBatteryDuration(durationMs: Long) {
        prefs.edit().putLong(KEY_LOW_BATTERY_DURATION, durationMs).apply()
    }

    fun getLowBatteryDuration(): Long {
        return prefs.getLong(KEY_LOW_BATTERY_DURATION, DEFAULT_LOW_BATTERY_DURATION)
    }

    // Quiet Hours settings
    fun saveQuietHoursEnabled(enabled: Boolean) {
        Log.d(TAG, "Saving quiet hours enabled: $enabled")
        val success = prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).commit()
        Log.d(TAG, "Quiet hours enabled save success: $success")
    }

    fun isQuietHoursEnabled(): Boolean {
        val enabled = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false)
        Log.d(TAG, "Retrieved quiet hours enabled: $enabled")
        return enabled
    }

    fun saveQuietHoursStartHour(hour: Int) {
        prefs.edit().putInt(KEY_QUIET_HOURS_START_HOUR, hour).apply()
    }

    fun getQuietHoursStartHour(): Int {
        return prefs.getInt(KEY_QUIET_HOURS_START_HOUR, DEFAULT_QUIET_HOURS_START_HOUR)
    }

    fun saveQuietHoursStartMinute(minute: Int) {
        prefs.edit().putInt(KEY_QUIET_HOURS_START_MINUTE, minute).apply()
    }

    fun getQuietHoursStartMinute(): Int {
        return prefs.getInt(KEY_QUIET_HOURS_START_MINUTE, DEFAULT_QUIET_HOURS_START_MINUTE)
    }

    fun saveQuietHoursEndHour(hour: Int) {
        prefs.edit().putInt(KEY_QUIET_HOURS_END_HOUR, hour).apply()
    }

    fun getQuietHoursEndHour(): Int {
        return prefs.getInt(KEY_QUIET_HOURS_END_HOUR, DEFAULT_QUIET_HOURS_END_HOUR)
    }

    fun saveQuietHoursEndMinute(minute: Int) {
        prefs.edit().putInt(KEY_QUIET_HOURS_END_MINUTE, minute).apply()
    }

    fun getQuietHoursEndMinute(): Int {
        return prefs.getInt(KEY_QUIET_HOURS_END_MINUTE, DEFAULT_QUIET_HOURS_END_MINUTE)
    }

    /**
     * Check if current time is within quiet hours
     */
    fun isCurrentlyInQuietHours(): Boolean {
        if (!isQuietHoursEnabled()) return false
        
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        
        val startTimeInMinutes = getQuietHoursStartHour() * 60 + getQuietHoursStartMinute()
        val endTimeInMinutes = getQuietHoursEndHour() * 60 + getQuietHoursEndMinute()
        
        return if (startTimeInMinutes <= endTimeInMinutes) {
            // Same day range (e.g., 10 PM to 7 AM)
            currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes
        } else {
            // Overnight range (e.g., 10 PM to 7 AM next day)
            currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes
        }
    }

    /**
     * Debug method to dump all settings
     */
    fun dumpAllSettings() {
        Log.d(TAG, "=== All Settings ===")
        Log.d(TAG, "Font Variant: ${getFontVariant()}")
        Log.d(TAG, "Dark Theme: ${getTheme()}")
        Log.d(TAG, "Theme Style: ${getThemeStyle()}")
        Log.d(TAG, "Glyph Service: ${getGlyphServiceEnabled()}")
        Log.d(TAG, "PowerPeek: ${isPowerPeekEnabled()}")
        Log.d(TAG, "Shake Threshold: ${getShakeThreshold()}")
        Log.d(TAG, "Display Duration: ${getDisplayDuration()}")
        Log.d(TAG, "Vibration Intensity: ${getVibrationIntensity()}")
        Log.d(TAG, "Glyph Guard Duration: ${getGlyphGuardDuration()}")
        Log.d(TAG, "Glyph Guard Sound: ${isGlyphGuardSoundEnabled()}")
        Log.d(TAG, "Glyph Guard Enabled: ${isGlyphGuardEnabled()}")
        Log.d(TAG, "Battery Story: ${isBatteryStoryEnabled()}")
        Log.d(TAG, "Glyph Guard Alert Mode: ${getGlyphGuardAlertMode()}")
        Log.d(TAG, "Glow Gate enabled: ${isPulseLockEnabled()}")
        Log.d(TAG, "Low-Battery Alert enabled: ${isLowBatteryEnabled()}")
        Log.d(TAG, "Quiet Hours enabled: ${isQuietHoursEnabled()}")
        Log.d(TAG, "Quiet Hours start: ${getQuietHoursStartHour()}:${getQuietHoursStartMinute()}")
        Log.d(TAG, "Quiet Hours end: ${getQuietHoursEndHour()}:${getQuietHoursEndMinute()}")
        Log.d(TAG, "===================")
    }

} 