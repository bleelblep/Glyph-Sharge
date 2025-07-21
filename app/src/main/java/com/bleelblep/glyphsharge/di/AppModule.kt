package com.bleelblep.glyphsharge.di

import android.content.Context
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.glyph.GlyphController
import com.bleelblep.glyphsharge.glyph.GlyphManager
import com.bleelblep.glyphsharge.ui.theme.FontState
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.theme.ThemeState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGlyphManager(@ApplicationContext context: Context): GlyphManager {
        return GlyphManager(context)
    }

    @Provides
    @Singleton
    fun provideGlyphController(
        @ApplicationContext context: Context,
        glyphManager: GlyphManager
    ): GlyphController {
        return GlyphController(context, glyphManager)
    }

    @Provides
    @Singleton
    fun provideGlyphAnimationManager(
        glyphManager: GlyphManager,
        settingsRepository: SettingsRepository
    ): GlyphAnimationManager {
        return GlyphAnimationManager(glyphManager, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository = SettingsRepository(context)

    @Provides
    @Singleton
    fun provideFontState(
        settingsRepository: SettingsRepository
    ): FontState = FontState(settingsRepository)

    @Provides
    @Singleton
    fun provideThemeState(
        settingsRepository: SettingsRepository
    ): ThemeState = ThemeState(settingsRepository)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SettingsRepositoryEntryPoint {
        fun getSettingsRepository(): SettingsRepository
    }
} 