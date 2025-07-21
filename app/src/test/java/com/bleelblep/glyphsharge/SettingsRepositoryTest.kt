package com.bleelblep.glyphsharge

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bleelblep.glyphsharge.ui.theme.FontVariant
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies that a freshly-constructed [SettingsRepository] writes the expected baseline
 * preference values (Power Peek disabled, HEADLINE font) on the first app launch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsRepositoryTest {

    @Test
    fun `first run defaults are applied`() {
        // Use a clean context to guarantee empty SharedPreferences
        val context: Context = ApplicationProvider.getApplicationContext()
        context.deleteSharedPreferences("glyphzen_settings")

        val repo = SettingsRepository(context)

        // Baseline expectations
        assertFalse("Power Peek should be disabled by default", repo.isPowerPeekEnabled())
        assertEquals("Headline font should be the default", FontVariant.HEADLINE, repo.getFontVariant())
    }

    @Test
    fun `migration resets incorrect values`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // preload prefs with incorrect values and old migration flag
        val prefs = context.getSharedPreferences("glyphzen_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("power_peek_enabled", true)
            .putString("font_variant", FontVariant.NDOT.name)
            .putInt("last_migrated_version", 0) // simulate pre-109 version
            .apply()

        val repo = SettingsRepository(context)

        assertFalse("Migration should disable Power Peek", repo.isPowerPeekEnabled())
        assertEquals("Migration should set font to HEADLINE", FontVariant.HEADLINE, repo.getFontVariant())
    }

    @Test
    fun `vibration intensity defaults to medium`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        context.deleteSharedPreferences("glyphzen_settings")

        val repo = SettingsRepository(context)
        assertEquals("Default vibration intensity should be 0.66f (66%)", 0.66f, repo.getVibrationIntensity(), 0.001f)
    }

    @Test
    fun `vibration intensity migration converts old format`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val prefs = context.getSharedPreferences("glyphzen_settings", Context.MODE_PRIVATE)
        
        // Test migration from old 1-255 range to new 0.0-1.0 range
        prefs.edit()
            .putFloat("vibration_intensity", 170f) // Old format: 170/255 = 66%
            .apply()

        val repo = SettingsRepository(context)
        val converted = repo.getVibrationIntensity()
        
        // Should convert 170 to approximately 0.66 (170/255 ≈ 0.667)
        assertEquals("170 should convert to ~0.667", 0.667f, converted, 0.01f)
        
        // Verify the converted value was saved
        val savedValue = prefs.getFloat("vibration_intensity", -1f)
        assertEquals("Converted value should be saved", converted, savedValue, 0.001f)
    }

    @Test
    fun `vibration intensity saves and retrieves correctly`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        context.deleteSharedPreferences("glyphzen_settings")

        val repo = SettingsRepository(context)
        
        // Test saving different values
        repo.saveVibrationIntensity(0.5f)
        assertEquals("Should retrieve saved value 0.5f", 0.5f, repo.getVibrationIntensity(), 0.001f)
        
        repo.saveVibrationIntensity(1.0f)
        assertEquals("Should retrieve saved value 1.0f", 1.0f, repo.getVibrationIntensity(), 0.001f)
        
        repo.saveVibrationIntensity(0.0f)
        assertEquals("Should retrieve saved value 0.0f", 0.0f, repo.getVibrationIntensity(), 0.001f)
    }
} 