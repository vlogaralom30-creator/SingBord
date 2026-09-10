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
    val lineThicknessDp: Float = 1.0f,
    val keyboardSize: KeyboardSize = KeyboardSize.NORMAL,
    val showNumberRow: Boolean = true,
    val enableHaptics: Boolean = true,
    val enableSound: Boolean = false,
    val enableKeyPopup: Boolean = true,
    val autoCapitalization: Boolean = true
)

class SingBordPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("singbord_settings", Context.MODE_PRIVATE)

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

    fun getSettings(): KeyboardSettings {
        return KeyboardSettings(
            lineThicknessDp = lineThicknessDp,
            keyboardSize = keyboardSize,
            showNumberRow = showNumberRow,
            enableHaptics = enableHaptics,
            enableSound = enableSound,
            enableKeyPopup = enableKeyPopup,
            autoCapitalization = autoCapitalization
        )
    }

    companion object {
        private const val KEY_LINE_THICKNESS = "line_thickness_dp"
        private const val KEY_KEYBOARD_SIZE = "keyboard_size"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_ENABLE_HAPTICS = "enable_haptics"
        private const val KEY_ENABLE_SOUND = "enable_sound"
        private const val KEY_ENABLE_KEY_POPUP = "enable_key_popup"
        private const val KEY_AUTO_CAPITALIZATION = "auto_capitalization"
    }
}
