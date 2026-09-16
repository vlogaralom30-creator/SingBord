package com.example

import androidx.compose.ui.graphics.Color

/**
 * Immutable palette specification for SingBord themes.
 * Zero-allocation, pre-resolved color values ensure 60/120fps lag-free typing.
 */
data class KeyboardThemePalette(
    val id: String,
    val name: String,
    val subtitle: String,
    val keyboardBg: Color,
    val keyBg: Color,
    val keyTextColor: Color,
    val functionKeyBg: Color,
    val functionTextColor: Color,
    val accentColor: Color,
    val accentTextColor: Color = Color.White,
    val gridBorderColor: Color,
    val suggestionStripBg: Color,
    val candidateBg: Color,
    val isDark: Boolean = false
)

object KeyboardThemes {
    val CLEAN_LIGHT = KeyboardThemePalette(
        id = "clean_light",
        name = "Clean Light",
        subtitle = "Crisp chalk white & slate",
        keyboardBg = Color(0xFFECEFF1),
        keyBg = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF263238),
        functionKeyBg = Color(0xFFCFD8DC),
        functionTextColor = Color(0xFF37474F),
        accentColor = Color(0xFF2563EB),
        accentTextColor = Color.White,
        gridBorderColor = Color(0xFFB0BEC5),
        suggestionStripBg = Color(0xFFE2E8F0),
        candidateBg = Color(0xFFF8FAFC),
        isDark = false
    )

    val OLED_BLACK = KeyboardThemePalette(
        id = "oled_black",
        name = "OLED Pure Dark",
        subtitle = "True black battery saver",
        keyboardBg = Color(0xFF000000),
        keyBg = Color(0xFF18181B),
        keyTextColor = Color(0xFFF4F4F5),
        functionKeyBg = Color(0xFF27272A),
        functionTextColor = Color(0xFFA1A1AA),
        accentColor = Color(0xFF3B82F6),
        accentTextColor = Color.White,
        gridBorderColor = Color(0xFF27272A),
        suggestionStripBg = Color(0xFF09090B),
        candidateBg = Color(0xFF18181B),
        isDark = true
    )

    val MIDNIGHT_NAVY = KeyboardThemePalette(
        id = "midnight_navy",
        name = "Midnight Navy",
        subtitle = "Deep oceanic blue & cyan",
        keyboardBg = Color(0xFF0B1120),
        keyBg = Color(0xFF1E293B),
        keyTextColor = Color(0xFFE2E8F0),
        functionKeyBg = Color(0xFF334155),
        functionTextColor = Color(0xFF94A3B8),
        accentColor = Color(0xFF06B6D4),
        accentTextColor = Color(0xFF0B1120),
        gridBorderColor = Color(0xFF1E293B),
        suggestionStripBg = Color(0xFF0F172A),
        candidateBg = Color(0xFF1E293B),
        isDark = true
    )

    val FOREST_MINT = KeyboardThemePalette(
        id = "forest_mint",
        name = "Forest Mint",
        subtitle = "Emerald green & fresh mint",
        keyboardBg = Color(0xFF064E3B),
        keyBg = Color(0xFF065F46),
        keyTextColor = Color(0xFFECFDF5),
        functionKeyBg = Color(0xFF047857),
        functionTextColor = Color(0xFFA7F3D0),
        accentColor = Color(0xFF10B981),
        accentTextColor = Color(0xFF064E3B),
        gridBorderColor = Color(0xFF065F46),
        suggestionStripBg = Color(0xFF022C22),
        candidateBg = Color(0xFF065F46),
        isDark = true
    )

    val SUNSET_AMBER = KeyboardThemePalette(
        id = "sunset_amber",
        name = "Sunset Amber",
        subtitle = "Warm dark stone & golden amber",
        keyboardBg = Color(0xFF1C1917),
        keyBg = Color(0xFF292524),
        keyTextColor = Color(0xFFFAFAF9),
        functionKeyBg = Color(0xFF44403C),
        functionTextColor = Color(0xFFD6D3D1),
        accentColor = Color(0xFFF59E0B),
        accentTextColor = Color(0xFF1C1917),
        gridBorderColor = Color(0xFF292524),
        suggestionStripBg = Color(0xFF0C0A09),
        candidateBg = Color(0xFF292524),
        isDark = true
    )

    val PASTEL_LAVENDER = KeyboardThemePalette(
        id = "pastel_lavender",
        name = "Pastel Lavender",
        subtitle = "Aesthetic lilac & purple accent",
        keyboardBg = Color(0xFFF3E8FF),
        keyBg = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF3B0764),
        functionKeyBg = Color(0xFFE9D5FF),
        functionTextColor = Color(0xFF6B21A8),
        accentColor = Color(0xFF9333EA),
        accentTextColor = Color.White,
        gridBorderColor = Color(0xFFD8B4FE),
        suggestionStripBg = Color(0xFFFAF5FF),
        candidateBg = Color(0xFFFFFFFF),
        isDark = false
    )

    val CYBER_CRIMSON = KeyboardThemePalette(
        id = "cyber_crimson",
        name = "Cyber Crimson",
        subtitle = "Dark ruby & vibrant rose",
        keyboardBg = Color(0xFF181115),
        keyBg = Color(0xFF2B1B24),
        keyTextColor = Color(0xFFFFF1F2),
        functionKeyBg = Color(0xFF4C1D35),
        functionTextColor = Color(0xFFFDA4AF),
        accentColor = Color(0xFFF43F5E),
        accentTextColor = Color.White,
        gridBorderColor = Color(0xFF3B1F30),
        suggestionStripBg = Color(0xFF120C10),
        candidateBg = Color(0xFF2B1B24),
        isDark = true
    )

    val ALL_THEMES = listOf(
        CLEAN_LIGHT,
        OLED_BLACK,
        MIDNIGHT_NAVY,
        FOREST_MINT,
        SUNSET_AMBER,
        PASTEL_LAVENDER,
        CYBER_CRIMSON
    )

    val LIGHT_THEMES = ALL_THEMES.filter { !it.isDark }
    val DARK_THEMES = ALL_THEMES.filter { it.isDark }

    fun getTheme(id: String): KeyboardThemePalette {
        return ALL_THEMES.find { it.id.equals(id, ignoreCase = true) } ?: CLEAN_LIGHT
    }

    fun resolveTheme(
        themeId: String,
        followSystemTheme: Boolean,
        lightThemeId: String,
        darkThemeId: String,
        isSystemDark: Boolean
    ): KeyboardThemePalette {
        if (followSystemTheme) {
            val targetId = if (isSystemDark) darkThemeId else lightThemeId
            return getTheme(targetId)
        }
        return getTheme(themeId)
    }

    fun resolveTheme(settings: KeyboardSettings, isSystemDark: Boolean): KeyboardThemePalette {
        return resolveTheme(
            themeId = settings.themeId,
            followSystemTheme = settings.followSystemTheme,
            lightThemeId = settings.lightThemeId,
            darkThemeId = settings.darkThemeId,
            isSystemDark = isSystemDark
        )
    }
}
