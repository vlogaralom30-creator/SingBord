package com.example

import android.content.Context
import android.content.SharedPreferences

enum class KeyboardSize(val label: String, val heightFactor: Float) {
    COMPACT("Compact", 0.85f),
    NORMAL("Normal", 1.0f),
    TALL("Tall", 1.15f),
    EXTRA_TALL("Extra Tall", 1.3f)
}

data class KeyboardSettings(
    val themeId: String = "clean_light",
    val lineThicknessDp: Float = 1.0f,
    val keyboardSize: KeyboardSize = KeyboardSize.NORMAL,
    val showNumberRow: Boolean = true,
    val showSuggestions: Boolean = true,
    val enableWordLearning: Boolean = true,
    val enableBanglish: Boolean = true,
    val enableGestures: Boolean = true,
    val enableEmoji: Boolean = true,
    val enableHaptics: Boolean = true,
    val enableSound: Boolean = false,
    val enableKeyPopup: Boolean = true,
    val autoCapitalization: Boolean = true
)

class SingBordPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("singbord_settings", Context.MODE_PRIVATE)

    var themeId: String
        get() = prefs.getString(KEY_THEME_ID, "clean_light") ?: "clean_light"
        set(value) = prefs.edit().putString(KEY_THEME_ID, value).apply()

    var lineThicknessDp: Float
        get() = prefs.getFloat(KEY_LINE_THICKNESS, 1.5f)
        set(value) = prefs.edit().putFloat(KEY_LINE_THICKNESS, value).apply()

    var keyboardSize: KeyboardSize
        get() {
            val name = prefs.getString(KEY_KEYBOARD_SIZE, KeyboardSize.NORMAL.name)
            return try {
                KeyboardSize.valueOf(name ?: KeyboardSize.NORMAL.name)
            } catch (e: Exception) {
                KeyboardSize.NORMAL
            }
        }
        set(value) = prefs.edit().putString(KEY_KEYBOARD_SIZE, value.name).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()

    var showSuggestions: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SUGGESTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_SUGGESTIONS, value).apply()

    var enableWordLearning: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_WORD_LEARNING, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_WORD_LEARNING, value).apply()

    var enableBanglish: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_BANGLISH, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_BANGLISH, value).apply()

    var enableGestures: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_GESTURES, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_GESTURES, value).apply()

    var enableEmoji: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_EMOJI, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_EMOJI, value).apply()

    var enableHaptics: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_HAPTICS, value).apply()

    var enableSound: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_SOUND, value).apply()

    var enableKeyPopup: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_KEY_POPUP, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_KEY_POPUP, value).apply()

    var autoCapitalization: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CAPITALIZATION, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CAPITALIZATION, value).apply()

    fun getRecentEmojis(): List<String> {
        val raw = prefs.getString(KEY_RECENT_EMOJIS, null) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentEmoji(emoji: String) {
        val current = getRecentEmojis().toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        val trimmed = current.take(35)
        prefs.edit().putString(KEY_RECENT_EMOJIS, trimmed.joinToString(",")).apply()
    }

    fun getSettings(): KeyboardSettings {
        return KeyboardSettings(
            themeId = themeId,
            lineThicknessDp = lineThicknessDp,
            keyboardSize = keyboardSize,
            showNumberRow = showNumberRow,
            showSuggestions = showSuggestions,
            enableWordLearning = enableWordLearning,
            enableBanglish = enableBanglish,
            enableGestures = enableGestures,
            enableEmoji = enableEmoji,
            enableHaptics = enableHaptics,
            enableSound = enableSound,
            enableKeyPopup = enableKeyPopup,
            autoCapitalization = autoCapitalization
        )
    }

    companion object {
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_LINE_THICKNESS = "line_thickness_dp"
        private const val KEY_KEYBOARD_SIZE = "keyboard_size"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_SHOW_SUGGESTIONS = "show_suggestions"
        private const val KEY_ENABLE_WORD_LEARNING = "enable_word_learning"
        private const val KEY_ENABLE_BANGLISH = "enable_banglish"
        private const val KEY_ENABLE_GESTURES = "enable_gestures"
        private const val KEY_ENABLE_EMOJI = "enable_emoji"
        private const val KEY_ENABLE_HAPTICS = "enable_haptics"
        private const val KEY_ENABLE_SOUND = "enable_sound"
        private const val KEY_ENABLE_KEY_POPUP = "enable_key_popup"
        private const val KEY_AUTO_CAPITALIZATION = "auto_capitalization"
        private const val KEY_RECENT_EMOJIS = "recent_emojis"
    }
}
