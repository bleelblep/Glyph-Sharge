package com.bleelblep.glyphsharge.glyph

import androidx.annotation.DrawableRes
import com.bleelblep.glyphsharge.R

/**
 * Registry of Glow Gate glyph animations that a user can choose from.
 * Each entry maps an identifier (persisted in SharedPreferences) to a
 * user-friendly name and (optionally) an icon resource for the settings UI.
 * The identifier strings are also used by [GlyphAnimationManager.playPulseLockAnimation].
 */
object PulseLockAnimations {
    data class PulseAnim(
        val id: String,
        val displayName: String,
        @DrawableRes val iconRes: Int
    )

    val list = listOf(
        PulseAnim("C1", "C1 Sequential", R.drawable.su),
        PulseAnim("WAVE", "Wave", R.drawable._78),
        PulseAnim("BEEDAH", "Beedah", R.drawable._78),
        PulseAnim("PULSE", "Pulse", R.drawable._44),
        PulseAnim("LOCK", "Padlock Sweep", R.drawable._23_24px),
        PulseAnim("SPIRAL", "Spiral", R.drawable._78),
        PulseAnim("HEARTBEAT", "Heartbeat", R.drawable._44),
        PulseAnim("MATRIX", "Matrix Rain", R.drawable._78),
        PulseAnim("FIREWORKS", "Fireworks", R.drawable._44),
        PulseAnim("DNA", "DNA Helix", R.drawable._23_24px)
    )

    fun getById(id: String): PulseAnim =
        list.firstOrNull { it.id == id } ?: list.first()
} 