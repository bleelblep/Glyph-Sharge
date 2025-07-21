package com.bleelblep.glyphsharge.utils

import android.graphics.Color

/**
 * Simple demo showing how to use WatermarkHelper
 * 
 * To enable watermark in your app, uncomment the lines in MainActivity.initializeWatermark()
 * or use these examples:
 */
object WatermarkDemo {
    
    /**
     * Enable basic watermark
     */
    fun enableBasicWatermark() {
        WatermarkHelper.configure {
            text = "PREVIEW"
            rotationAngle = 315f
            spacing = 45
            useThemeBasedColor = true
        }.enable()
    }
    
    /**
     * Enable custom watermark
     */
    fun enableCustomWatermark() {
        WatermarkHelper.configure {
            text = "GLYPH SHARGE"
            rotationAngle = 315f
            spacing = 40
            alpha = 0.2f
            textSize = 18f
            useThemeBasedColor = true
        }.enable()
    }
    
    /**
     * Disable watermark
     */
    fun disableWatermark() {
        WatermarkHelper.disable()
    }
}

/**
 * Usage Instructions:
 * 
 * 1. To enable watermark, add this to your MainActivity.onCreate():
 *    WatermarkDemo.enableCustomWatermark()
 * 
 * 2. Or uncomment the line in MainActivity.initializeWatermark():
 *    WatermarkHelper.enable()
 * 
 * 3. The watermark will automatically appear on all screens
 * 
 * 4. To disable, call:
 *    WatermarkDemo.disableWatermark()
 */ 