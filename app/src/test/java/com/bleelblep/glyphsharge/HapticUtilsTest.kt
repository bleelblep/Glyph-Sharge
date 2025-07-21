package com.bleelblep.glyphsharge

import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for HapticUtils vibration intensity conversion and consistency
 */
class HapticUtilsTest {

    @Test
    fun `convertUserIntensityToAndroidAmplitude converts correctly`() {
        // Test edge cases
        assertEquals("0.0f should convert to OFF (0)", 0, HapticUtils.convertUserIntensityToAndroidAmplitude(0.0f))
        assertEquals("1.0f should convert to STRONG (255)", 255, HapticUtils.convertUserIntensityToAndroidAmplitude(1.0f))
        
        // Test middle values
        assertEquals("0.5f should convert to ~128", 128, HapticUtils.convertUserIntensityToAndroidAmplitude(0.5f))
        assertEquals("0.66f should convert to ~168", 168, HapticUtils.convertUserIntensityToAndroidAmplitude(0.66f))
        assertEquals("0.33f should convert to ~84", 84, HapticUtils.convertUserIntensityToAndroidAmplitude(0.33f))
        
        // Test boundary conditions
        assertEquals("0.001f should convert to 1", 1, HapticUtils.convertUserIntensityToAndroidAmplitude(0.001f))
        assertEquals("0.999f should convert to 254", 254, HapticUtils.convertUserIntensityToAndroidAmplitude(0.999f))
    }

    @Test
    fun `convertUserIntensityToAndroidAmplitude handles out of range values`() {
        // Test values outside 0.0-1.0 range
        assertEquals("Negative values should convert to OFF", 0, HapticUtils.convertUserIntensityToAndroidAmplitude(-1.0f))
        assertEquals("Values > 1.0 should convert to STRONG", 255, HapticUtils.convertUserIntensityToAndroidAmplitude(2.0f))
    }

    @Test
    fun `vibration intensity consistency test`() {
        // Test that the same user intensity produces consistent results
        val testIntensities = listOf(0.0f, 0.33f, 0.66f, 1.0f)
        
        testIntensities.forEach { intensity ->
            val result1 = HapticUtils.convertUserIntensityToAndroidAmplitude(intensity)
            val result2 = HapticUtils.convertUserIntensityToAndroidAmplitude(intensity)
            assertEquals("Conversion should be consistent for intensity $intensity", result1, result2)
        }
    }

    @Test
    fun `vibration intensity scaling is linear`() {
        // Test that the conversion maintains reasonable linearity
        val intensity1 = 0.25f
        val intensity2 = 0.5f
        val intensity3 = 0.75f
        
        val amplitude1 = HapticUtils.convertUserIntensityToAndroidAmplitude(intensity1)
        val amplitude2 = HapticUtils.convertUserIntensityToAndroidAmplitude(intensity2)
        val amplitude3 = HapticUtils.convertUserIntensityToAndroidAmplitude(intensity3)
        
        // amplitude2 should be roughly double amplitude1
        assertEquals("0.5f should be roughly double 0.25f", amplitude1 * 2, amplitude2, 2)
        
        // amplitude3 should be roughly triple amplitude1
        assertEquals("0.75f should be roughly triple 0.25f", amplitude1 * 3, amplitude3, 2)
    }
} 