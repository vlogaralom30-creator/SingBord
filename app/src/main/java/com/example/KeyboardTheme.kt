package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Defines the physical visual geometry and render style of keycaps.
 */
enum class KeyboardKeyShapeStyle {
    FLAT_GRID,          // Default 2D zero-gap connected minimalist grid
    ROUNDED_ELEVATED,   // iOS style rounded floating keys with bottom 3D drop-shadow lip
    GLASSMORPHIC_3D     // Facemoji 3D liquid frosted glass with specular rims and glowing badges
}

/**
 * Immutable palette and visual specification for SingBord themes.
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
    val isDark: Boolean = false,
    // Custom Visual Theme Parameters (Extensible & 100% Backward-Compatible)
    val isCustomTheme: Boolean = false,
    val keyShapeStyle: KeyboardKeyShapeStyle = KeyboardKeyShapeStyle.FLAT_GRID,
    val keyCornerRadiusDp: Float = 0f,
    val keyHorizontalGapDp: Float = 0f,
    val keyVerticalGapDp: Float = 0f,
    val keyElevationDp: Float = 0f,
    val keyShadowColor: Color = Color.Transparent,
    val keyBorderColor: Color = Color.Transparent,
    val keyBorderWidthDp: Float = 0f,
    val keyGradientTop: Color? = null,
    val keyGradientBottom: Color? = null,
    val functionKeyGradientTop: Color? = null,
    val functionKeyGradientBottom: Color? = null,
    val shiftBadgeColor: Color? = null,
    val backspaceBadgeColor: Color? = null,
    val modeBadgeColor: Color? = null,
    val enterBadgeColor: Color? = null,
    val keyTextFontWeight: FontWeight = FontWeight.SemiBold,
    val spaceBarLabel: String = "",
    val showSpaceVoiceGlyph: Boolean = false,
    val showLanguageGlobeKey: Boolean = false,
    val showBottomUtilityRow: Boolean = false,
    val frostedGlassOverlay: Boolean = false,
    // Dynamic Gradient Background Mesh & Liquid Water Ripple Animation
    val bgGradientTop: Color? = null,
    val bgGradientCenter: Color? = null,
    val bgGradientBottom: Color? = null,
    val bgMeshColors: List<Color>? = null,
    val enableWaterRipple: Boolean = true,
    val waterRippleColor: Color? = null
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

    // =========================================================================
    // CUSTOM REFERENCE THEMES (High-Fidelity Recreated & User Image Inspired)
    // =========================================================================

    /**
     * Aurora Sunset Glass: Direct 100% replica of the user's uploaded reference image!
     * Vibrant sunset multi-color gradient canvas (Yellow/Orange/Magenta/Purple),
     * translucent white pill keycaps with specular white stroke borders, and glowing hot-magenta key press fill.
     */
    val AURORA_SUNSET_GLASS = KeyboardThemePalette(
        id = "aurora_sunset_glass",
        name = "Aurora Sunset Glass",
        subtitle = "Vibrant Sunset Mesh • Translucent Pills • Hot Pink Glow",
        keyboardBg = Color(0xFFC026D3),
        keyBg = Color(0xFFFFFFFF).copy(alpha = 0.24f),
        keyTextColor = Color.White,
        functionKeyBg = Color(0xFFFFFFFF).copy(alpha = 0.32f),
        functionTextColor = Color.White,
        accentColor = Color(0xFFEC4899),
        accentTextColor = Color.White,
        gridBorderColor = Color.Transparent,
        suggestionStripBg = Color(0xFF0F172A).copy(alpha = 0.25f),
        candidateBg = Color.White.copy(alpha = 0.25f),
        isDark = true,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 14f,
        keyHorizontalGapDp = 4.0f,
        keyVerticalGapDp = 8.0f,
        keyElevationDp = 1.8f,
        keyShadowColor = Color(0xFF4C0519).copy(alpha = 0.35f),
        keyBorderColor = Color.White.copy(alpha = 0.70f),
        keyBorderWidthDp = 1.0f,
        keyGradientTop = Color.White.copy(alpha = 0.32f),
        keyGradientBottom = Color.White.copy(alpha = 0.16f),
        functionKeyGradientTop = Color.White.copy(alpha = 0.40f),
        functionKeyGradientBottom = Color.White.copy(alpha = 0.22f),
        shiftBadgeColor = Color(0xFFF59E0B),
        backspaceBadgeColor = Color(0xFFE11D48),
        modeBadgeColor = Color(0xFF10B981),
        enterBadgeColor = Color(0xFF6366F1),
        keyTextFontWeight = FontWeight.Bold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true,
        bgGradientTop = Color(0xFFF59E0B),
        bgGradientCenter = Color(0xFFEC4899),
        bgGradientBottom = Color(0xFF4F46E5),
        bgMeshColors = listOf(Color(0xFFFBBF24), Color(0xFFF43F5E), Color(0xFFC026D3), Color(0xFF4338CA)),
        enableWaterRipple = true,
        waterRippleColor = Color(0xFFF43F5E)
    )

    /**
     * Ocean Water Wave: Dynamic liquid water ripple animation theme with oceanic aqua-blue
     * gradient mesh background, frosted ice keycaps, and electric cyan water ripples on key press.
     */
    val OCEAN_WATER_WAVE = KeyboardThemePalette(
        id = "ocean_water_wave",
        name = "Ocean Water Wave",
        subtitle = "Liquid Aqua Gradient • Fluid Water Ripple FX",
        keyboardBg = Color(0xFF0F172A),
        keyBg = Color(0xFF0284C7).copy(alpha = 0.30f),
        keyTextColor = Color(0xFFF0F9FF),
        functionKeyBg = Color(0xFF0369A1).copy(alpha = 0.40f),
        functionTextColor = Color(0xFFE0F2FE),
        accentColor = Color(0xFF00F0FF),
        accentTextColor = Color(0xFF0C4A6E),
        gridBorderColor = Color.Transparent,
        suggestionStripBg = Color(0xFF082F49).copy(alpha = 0.55f),
        candidateBg = Color(0xFF0284C7).copy(alpha = 0.35f),
        isDark = true,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 12f,
        keyHorizontalGapDp = 3.8f,
        keyVerticalGapDp = 7.5f,
        keyElevationDp = 2.0f,
        keyShadowColor = Color(0xFF082F49),
        keyBorderColor = Color(0xFF38BDF8).copy(alpha = 0.65f),
        keyBorderWidthDp = 0.9f,
        keyGradientTop = Color(0xFF38BDF8).copy(alpha = 0.38f),
        keyGradientBottom = Color(0xFF0284C7).copy(alpha = 0.22f),
        functionKeyGradientTop = Color(0xFF7DD3FC).copy(alpha = 0.45f),
        functionKeyGradientBottom = Color(0xFF0369A1).copy(alpha = 0.30f),
        shiftBadgeColor = Color(0xFF38BDF8),
        backspaceBadgeColor = Color(0xFFF43F5E),
        modeBadgeColor = Color(0xFF34D399),
        enterBadgeColor = Color(0xFF00F0FF),
        keyTextFontWeight = FontWeight.SemiBold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true,
        bgGradientTop = Color(0xFF0EA5E9),
        bgGradientCenter = Color(0xFF0284C7),
        bgGradientBottom = Color(0xFF0F172A),
        bgMeshColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF0F172A)),
        enableWaterRipple = true,
        waterRippleColor = Color(0xFF00F0FF)
    )

    /**
     * Cyber Neon Pulse: Dark midnight violet canvas with electric cyan and hot magenta neon glow.
     */
    val CYBERPUNK_NEON_PULSE = KeyboardThemePalette(
        id = "cyberpunk_neon_pulse",
        name = "Cyber Neon Pulse",
        subtitle = "Midnight Violet • Electric Cyan & Magenta Rims",
        keyboardBg = Color(0xFF090514),
        keyBg = Color(0xFF581C87).copy(alpha = 0.35f),
        keyTextColor = Color(0xFFF5F3FF),
        functionKeyBg = Color(0xFF6B21A8).copy(alpha = 0.45f),
        functionTextColor = Color(0xFFE9D5FF),
        accentColor = Color(0xFF00F0FF),
        accentTextColor = Color(0xFF090514),
        gridBorderColor = Color.Transparent,
        suggestionStripBg = Color(0xFF2E1065).copy(alpha = 0.5f),
        candidateBg = Color(0xFF581C87).copy(alpha = 0.35f),
        isDark = true,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 11f,
        keyHorizontalGapDp = 4.0f,
        keyVerticalGapDp = 7.5f,
        keyElevationDp = 2.2f,
        keyShadowColor = Color(0xFF000000),
        keyBorderColor = Color(0xFF00F0FF).copy(alpha = 0.75f),
        keyBorderWidthDp = 1.0f,
        keyGradientTop = Color(0xFFA855F7).copy(alpha = 0.45f),
        keyGradientBottom = Color(0xFF3B0764).copy(alpha = 0.30f),
        functionKeyGradientTop = Color(0xFFC026D3).copy(alpha = 0.50f),
        functionKeyGradientBottom = Color(0xFF581C87).copy(alpha = 0.35f),
        shiftBadgeColor = Color(0xFFF59E0B),
        backspaceBadgeColor = Color(0xFFF43F5E),
        modeBadgeColor = Color(0xFF10B981),
        enterBadgeColor = Color(0xFF00F0FF),
        keyTextFontWeight = FontWeight.Bold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true,
        bgGradientTop = Color(0xFFC026D3),
        bgGradientCenter = Color(0xFF7E22CE),
        bgGradientBottom = Color(0xFF090514),
        bgMeshColors = listOf(Color(0xFFE0E7FF), Color(0xFFC026D3), Color(0xFF7E22CE), Color(0xFF090514)),
        enableWaterRipple = true,
        waterRippleColor = Color(0xFF00F0FF)
    )

    /**
     * Pastel Candy Glass: Dreamy aesthetic peach, blush pink & lilac gradient mesh with soft white translucent pills.
     */
    val PASTEL_CANDY_GLASS = KeyboardThemePalette(
        id = "pastel_candy_glass",
        name = "Pastel Candy Glass",
        subtitle = "Dreamy Peach & Lilac • Soft Translucent Pills",
        keyboardBg = Color(0xFFFDF2F8),
        keyBg = Color(0xFFFFFFFF).copy(alpha = 0.65f),
        keyTextColor = Color(0xFF831843),
        functionKeyBg = Color(0xFFFCE7F3).copy(alpha = 0.85f),
        functionTextColor = Color(0xFF9D174D),
        accentColor = Color(0xFFF43F5E),
        accentTextColor = Color.White,
        gridBorderColor = Color.Transparent,
        suggestionStripBg = Color(0xFFFFF1F2).copy(alpha = 0.85f),
        candidateBg = Color.White,
        isDark = false,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 14f,
        keyHorizontalGapDp = 4.0f,
        keyVerticalGapDp = 8.0f,
        keyElevationDp = 1.5f,
        keyShadowColor = Color(0xFFF472B6).copy(alpha = 0.30f),
        keyBorderColor = Color.White,
        keyBorderWidthDp = 1.2f,
        keyGradientTop = Color.White,
        keyGradientBottom = Color(0xFFFCE7F3).copy(alpha = 0.80f),
        functionKeyGradientTop = Color.White,
        functionKeyGradientBottom = Color(0xFFFBCFE8).copy(alpha = 0.85f),
        shiftBadgeColor = Color(0xFFF59E0B),
        backspaceBadgeColor = Color(0xFFF43F5E),
        modeBadgeColor = Color(0xFF10B981),
        enterBadgeColor = Color(0xFFEC4899),
        keyTextFontWeight = FontWeight.SemiBold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true,
        bgGradientTop = Color(0xFFFED7AA),
        bgGradientCenter = Color(0xFFFBCFE8),
        bgGradientBottom = Color(0xFFDDD6FE),
        bgMeshColors = listOf(Color(0xFFFED7AA), Color(0xFFFBCFE8), Color(0xFFF472B6), Color(0xFFDDD6FE)),
        enableWaterRipple = true,
        waterRippleColor = Color(0xFFF43F5E)
    )

    /**
     * Emerald Gold Glass: Deep royal emerald green canvas with metallic gold specular rim.
     */
    val ROYAL_EMERALD_GOLD = KeyboardThemePalette(
        id = "emerald_gold_aurora",
        name = "Emerald Gold Glass",
        subtitle = "Royal Deep Emerald • Metallic Gold Specular Rim",
        keyboardBg = Color(0xFF022C22),
        keyBg = Color(0xFF065F46).copy(alpha = 0.40f),
        keyTextColor = Color(0xFFECFDF5),
        functionKeyBg = Color(0xFF047857).copy(alpha = 0.50f),
        functionTextColor = Color(0xFFA7F3D0),
        accentColor = Color(0xFFF59E0B),
        accentTextColor = Color(0xFF022C22),
        gridBorderColor = Color.Transparent,
        suggestionStripBg = Color(0xFF064E3B).copy(alpha = 0.60f),
        candidateBg = Color(0xFF065F46).copy(alpha = 0.40f),
        isDark = true,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 10f,
        keyHorizontalGapDp = 3.5f,
        keyVerticalGapDp = 7.0f,
        keyElevationDp = 2.0f,
        keyShadowColor = Color(0xFF022C22),
        keyBorderColor = Color(0xFFF59E0B).copy(alpha = 0.70f),
        keyBorderWidthDp = 0.9f,
        keyGradientTop = Color(0xFF10B981).copy(alpha = 0.40f),
        keyGradientBottom = Color(0xFF064E3B).copy(alpha = 0.30f),
        functionKeyGradientTop = Color(0xFF34D399).copy(alpha = 0.45f),
        functionKeyGradientBottom = Color(0xFF047857).copy(alpha = 0.35f),
        shiftBadgeColor = Color(0xFFF59E0B),
        backspaceBadgeColor = Color(0xFFEF4444),
        modeBadgeColor = Color(0xFF10B981),
        enterBadgeColor = Color(0xFFF59E0B),
        keyTextFontWeight = FontWeight.SemiBold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true,
        bgGradientTop = Color(0xFF047857),
        bgGradientCenter = Color(0xFF065F46),
        bgGradientBottom = Color(0xFF022C22),
        bgMeshColors = listOf(Color(0xFFD97706), Color(0xFF047857), Color(0xFF065F46), Color(0xFF022C22)),
        enableWaterRipple = true,
        waterRippleColor = Color(0xFFF59E0B)
    )

    /**
     * Facemoji Obsidian Glass: 3D Frosted Smoke Acrylic Glass with specular rim highlight.
     */
    val FACEMOJI_OBSIDIAN_GLASS = KeyboardThemePalette(
        id = "facemoji_obsidian_glass",
        name = "Facemoji Obsidian Glass",
        subtitle = "3D Liquid Smoke Glass • Glowing Badges",
        keyboardBg = Color(0xFF0F1218),
        keyBg = Color(0xFF1E222D),
        keyTextColor = Color(0xFFF8FAFC),
        functionKeyBg = Color(0xFF252A36),
        functionTextColor = Color(0xFFE2E8F0),
        accentColor = Color(0xFF2563EB),
        accentTextColor = Color.White,
        gridBorderColor = Color(0xFF252A38),
        suggestionStripBg = Color(0xFF0C0E14),
        candidateBg = Color(0xFF1E222D),
        isDark = true,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 9f,
        keyHorizontalGapDp = 3.5f,
        keyVerticalGapDp = 7f,
        keyElevationDp = 2.2f,
        keyShadowColor = Color(0xFF000000),
        keyBorderColor = Color.White.copy(alpha = 0.32f),
        keyBorderWidthDp = 0.8f,
        keyGradientTop = Color(0xFF343A4A).copy(alpha = 0.78f),
        keyGradientBottom = Color(0xFF191C25).copy(alpha = 0.90f),
        functionKeyGradientTop = Color(0xFF2B313F).copy(alpha = 0.82f),
        functionKeyGradientBottom = Color(0xFF141720).copy(alpha = 0.92f),
        shiftBadgeColor = Color(0xFFF59E0B),
        backspaceBadgeColor = Color(0xFFEF4444),
        modeBadgeColor = Color(0xFF10B981),
        enterBadgeColor = Color(0xFF2563EB),
        keyTextFontWeight = FontWeight.SemiBold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true
    )

    /**
     * Facemoji Frosted Glass: 3D Liquid White Frosted Glass with glossy highlights.
     */
    val FACEMOJI_FROSTED_GLASS = KeyboardThemePalette(
        id = "facemoji_frosted_glass",
        name = "Facemoji Frosted Glass",
        subtitle = "3D Liquid White Glass • Soft Glow Badges",
        keyboardBg = Color(0xFFE2E6EE),
        keyBg = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF0F172A),
        functionKeyBg = Color(0xFFD6DCE6),
        functionTextColor = Color(0xFF1E293B),
        accentColor = Color(0xFF3B82F6),
        accentTextColor = Color.White,
        gridBorderColor = Color(0xFFCBD5E1),
        suggestionStripBg = Color(0xFFECEFF5),
        candidateBg = Color(0xFFFFFFFF),
        isDark = false,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.GLASSMORPHIC_3D,
        keyCornerRadiusDp = 9f,
        keyHorizontalGapDp = 3.5f,
        keyVerticalGapDp = 7f,
        keyElevationDp = 2.0f,
        keyShadowColor = Color(0xFF94A3B8),
        keyBorderColor = Color.White.copy(alpha = 0.90f),
        keyBorderWidthDp = 0.9f,
        keyGradientTop = Color.White.copy(alpha = 0.95f),
        keyGradientBottom = Color(0xFFF1F5F9).copy(alpha = 0.82f),
        functionKeyGradientTop = Color(0xFFF8FAFC).copy(alpha = 0.90f),
        functionKeyGradientBottom = Color(0xFFDDE3EC).copy(alpha = 0.85f),
        shiftBadgeColor = Color(0xFFF59E0B),
        backspaceBadgeColor = Color(0xFFF43F5E),
        modeBadgeColor = Color(0xFF22C55E),
        enterBadgeColor = Color(0xFF3B82F6),
        keyTextFontWeight = FontWeight.SemiBold,
        spaceBarLabel = "",
        showSpaceVoiceGlyph = true,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = false,
        frostedGlassOverlay = true
    )

    /**
     * iOS Cupertino Glass: Clean minimalist light keyboard with floating rounded keys.
     */
    val IOS_CLEAN_LIGHT = KeyboardThemePalette(
        id = "ios_clean_light",
        name = "iOS Cupertino Glass",
        subtitle = "Minimalist Light • Floating Keycaps & 3D Shadow",
        keyboardBg = Color(0xFFD2D5DC),
        keyBg = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF111827),
        functionKeyBg = Color(0xFFB1B6C2),
        functionTextColor = Color(0xFF1F2937),
        accentColor = Color(0xFFB1B6C2),
        accentTextColor = Color(0xFF4B5563),
        gridBorderColor = Color(0xFFB1B6C2),
        suggestionStripBg = Color(0xFFDCE0E8),
        candidateBg = Color(0xFFFFFFFF),
        isDark = false,
        isCustomTheme = true,
        keyShapeStyle = KeyboardKeyShapeStyle.ROUNDED_ELEVATED,
        keyCornerRadiusDp = 5.5f,
        keyHorizontalGapDp = 4.0f,
        keyVerticalGapDp = 8.0f,
        keyElevationDp = 1.6f,
        keyShadowColor = Color(0xFF888D96),
        keyBorderColor = Color.Transparent,
        keyBorderWidthDp = 0f,
        keyTextFontWeight = FontWeight.Normal,
        spaceBarLabel = "space",
        showSpaceVoiceGlyph = false,
        showLanguageGlobeKey = true,
        showBottomUtilityRow = true,
        frostedGlassOverlay = false
    )

    val CLASSIC_THEMES = listOf(
        CLEAN_LIGHT,
        OLED_BLACK,
        MIDNIGHT_NAVY,
        FOREST_MINT,
        SUNSET_AMBER,
        PASTEL_LAVENDER,
        CYBER_CRIMSON
    )

    val CUSTOM_THEMES = listOf(
        AURORA_SUNSET_GLASS,
        OCEAN_WATER_WAVE,
        CYBERPUNK_NEON_PULSE,
        PASTEL_CANDY_GLASS,
        ROYAL_EMERALD_GOLD,
        FACEMOJI_OBSIDIAN_GLASS,
        FACEMOJI_FROSTED_GLASS,
        IOS_CLEAN_LIGHT
    )

    val ALL_THEMES = CLASSIC_THEMES + CUSTOM_THEMES

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
