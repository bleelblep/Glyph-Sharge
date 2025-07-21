package com.bleelblep.glyphsharge.di

import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GlyphComponent {
    fun glyphAnimationManager(): GlyphAnimationManager
} 