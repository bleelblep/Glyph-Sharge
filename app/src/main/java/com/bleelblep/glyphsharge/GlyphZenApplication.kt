package com.bleelblep.glyphsharge

import android.app.Application
import com.bleelblep.glyphsharge.glyph.GlyphManager
import com.bleelblep.glyphsharge.utils.LoggingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GlyphShargeApplication : Application() {

    @Inject
    lateinit var glyphManager: GlyphManager

    // Force instantiation of SettingsRepository at the earliest point in the app lifecycle so
    // its `init {}` block reliably writes first-run defaults before *any* component can read
    // preferences – this avoids race conditions seen in release builds.
    @Inject
    lateinit var settingsRepository: com.bleelblep.glyphsharge.ui.theme.SettingsRepository

    override fun onCreate() {
        super.onCreate()

        // Initialize Logging Manager
        LoggingManager.initialize(this)

        // Initialize Glyph Manager on Nothing phones
        if (glyphManager.isNothingPhone()) {
            glyphManager.initialize()
        }

        // Accessing the repository here guarantees that first-run defaults have executed.
        settingsRepository.dumpAllSettings()
    }

    override fun onTerminate() {
        super.onTerminate()

        // Clean up Glyph Manager resources
        if (glyphManager.isNothingPhone()) {
            glyphManager.cleanup()
        }
    }
} 