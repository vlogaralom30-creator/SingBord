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
    val followSystemTheme: Boolean = false,
    val lightThemeId: String = "clean_light",
    val darkThemeId: String = "oled_black",
    val lineThicknessDp: Float = 1.0f,
    val keyboardSize: KeyboardSize = KeyboardSize.NORMAL,
    val showNumberRow: Boolean = true,
    val showSuggestions: Boolean = true,
    val enableWordLearning: Boolean = true,
    val enableBanglish: Boolean = true,
    val enableGestures: Boolean = true,
    val enableSpacebarCursor: Boolean = true,
    val enableVoiceTyping: Boolean = true,
    val voiceLanguage: String = "auto",
    val showLanguageSwitchKey: Boolean = true,
    val enableEmoji: Boolean = true,
    val enableHaptics: Boolean = true,
    val enableSound: Boolean = false,
    val enableKeyPopup: Boolean = true,
    val autoCapitalization: Boolean = true,
    val enableStylishFonts: Boolean = true,
    val activeStylishStyle: String = "normal",
    val enableKeyAnimation: Boolean = true,
    val defaultLanguage: KeyboardLanguage = KeyboardLanguage.ENGLISH
)

class SingBordPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("singbord_settings", Context.MODE_PRIVATE)

    var themeId: String
        get() = prefs.getString(KEY_THEME_ID, "clean_light") ?: "clean_light"
        set(value) = prefs.edit().putString(KEY_THEME_ID, value).apply()

    var followSystemTheme: Boolean
        get() = prefs.getBoolean(KEY_FOLLOW_SYSTEM_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM_THEME, value).apply()

    var lightThemeId: String
        get() = prefs.getString(KEY_LIGHT_THEME_ID, "clean_light") ?: "clean_light"
        set(value) = prefs.edit().putString(KEY_LIGHT_THEME_ID, value).apply()

    var darkThemeId: String
        get() = prefs.getString(KEY_DARK_THEME_ID, "oled_black") ?: "oled_black"
        set(value) = prefs.edit().putString(KEY_DARK_THEME_ID, value).apply()

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

    var enableSpacebarCursor: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_SPACEBAR_CURSOR, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_SPACEBAR_CURSOR, value).apply()

    var enableVoiceTyping: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_VOICE_TYPING, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_VOICE_TYPING, value).apply()

    var voiceLanguage: String
        get() = prefs.getString(KEY_VOICE_LANGUAGE, "auto") ?: "auto"
        set(value) = prefs.edit().putString(KEY_VOICE_LANGUAGE, value).apply()

    var showLanguageSwitchKey: Boolean
        get() = prefs.getBoolean(KEY_SHOW_LANGUAGE_SWITCH_KEY, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_LANGUAGE_SWITCH_KEY, value).apply()

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

    var enableStylishFonts: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_STYLISH_FONTS, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_STYLISH_FONTS, value).apply()

    var activeStylishStyle: String
        get() = prefs.getString(KEY_ACTIVE_STYLISH_STYLE, "normal") ?: "normal"
        set(value) = prefs.edit().putString(KEY_ACTIVE_STYLISH_STYLE, value).apply()

    var enableKeyAnimation: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_KEY_ANIMATION, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_KEY_ANIMATION, value).apply()

    var defaultLanguage: KeyboardLanguage
        get() {
            val name = prefs.getString(KEY_DEFAULT_LANGUAGE, KeyboardLanguage.ENGLISH.name)
            return try {
                KeyboardLanguage.valueOf(name ?: KeyboardLanguage.ENGLISH.name)
            } catch (e: Exception) {
                KeyboardLanguage.ENGLISH
            }
        }
        set(value) = prefs.edit().putString(KEY_DEFAULT_LANGUAGE, value.name).apply()

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

    fun getRecentSymbols(): List<String> {
        val raw = prefs.getString(KEY_RECENT_SYMBOLS, null) ?: return emptyList()
        return raw.split(SYMBOL_SEPARATOR).filter { it.isNotBlank() }
    }

    fun addRecentSymbol(symbol: String) {
        if (symbol.isBlank()) return
        val current = getRecentSymbols().toMutableList()
        current.remove(symbol)
        current.add(0, symbol)
        val trimmed = current.take(35)
        prefs.edit().putString(KEY_RECENT_SYMBOLS, trimmed.joinToString(SYMBOL_SEPARATOR)).apply()
    }

    fun getClipboardHistory(): List<String> {
        val raw = prefs.getString(KEY_CLIPBOARD_HISTORY, null) ?: return emptyList()
        return raw.split(CLIPBOARD_SEPARATOR).filter { it.isNotBlank() }
    }

    fun addClipboardItem(text: String) {
        if (text.isBlank()) return
        val current = getClipboardHistory().toMutableList()
        current.remove(text)
        current.add(0, text)
        val trimmed = current.take(20)
        prefs.edit().putString(KEY_CLIPBOARD_HISTORY, trimmed.joinToString(CLIPBOARD_SEPARATOR)).apply()
    }

    fun clearClipboardHistory() {
        prefs.edit().remove(KEY_CLIPBOARD_HISTORY).apply()
    }

    fun getSettings(): KeyboardSettings {
        return KeyboardSettings(
            themeId = themeId,
            followSystemTheme = followSystemTheme,
            lightThemeId = lightThemeId,
            darkThemeId = darkThemeId,
            lineThicknessDp = lineThicknessDp,
            keyboardSize = keyboardSize,
            showNumberRow = showNumberRow,
            showSuggestions = showSuggestions,
            enableWordLearning = enableWordLearning,
            enableBanglish = enableBanglish,
            enableGestures = enableGestures,
            enableSpacebarCursor = enableSpacebarCursor,
            enableVoiceTyping = enableVoiceTyping,
            voiceLanguage = voiceLanguage,
            showLanguageSwitchKey = showLanguageSwitchKey,
            enableEmoji = enableEmoji,
            enableHaptics = enableHaptics,
            enableSound = enableSound,
            enableKeyPopup = enableKeyPopup,
            autoCapitalization = autoCapitalization,
            enableStylishFonts = enableStylishFonts,
            activeStylishStyle = activeStylishStyle,
            enableKeyAnimation = enableKeyAnimation,
            defaultLanguage = defaultLanguage
        )
    }

    var authUserId: String
        get() = prefs.getString(KEY_AUTH_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USER_ID, value).apply()

    var authUserEmail: String
        get() = prefs.getString(KEY_AUTH_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USER_EMAIL, value).apply()

    var authAccessToken: String
        get() = prefs.getString(KEY_AUTH_ACCESS_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_ACCESS_TOKEN, value).apply()

    var lastCloudSettingsJson: String
        get() = prefs.getString(KEY_LAST_CLOUD_SETTINGS_JSON, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_CLOUD_SETTINGS_JSON, value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, value).apply()

    var cloudClipboardJson: String
        get() = prefs.getString(KEY_CLOUD_CLIPBOARD_JSON, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CLOUD_CLIPBOARD_JSON, value).apply()

    var enableOnlineDictionary: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_ONLINE_DICTIONARY, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_ONLINE_DICTIONARY, value).apply()

    var autoCloudSync: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CLOUD_SYNC, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CLOUD_SYNC, value).apply()

    fun exportSettingsToJson(): String {
        val s = getSettings()
        val json = org.json.JSONObject()
        json.put("themeId", s.themeId)
        json.put("followSystemTheme", s.followSystemTheme)
        json.put("lightThemeId", s.lightThemeId)
        json.put("darkThemeId", s.darkThemeId)
        json.put("lineThicknessDp", s.lineThicknessDp.toDouble())
        json.put("keyboardSize", s.keyboardSize.name)
        json.put("showNumberRow", s.showNumberRow)
        json.put("showSuggestions", s.showSuggestions)
        json.put("enableWordLearning", s.enableWordLearning)
        json.put("enableBanglish", s.enableBanglish)
        json.put("enableGestures", s.enableGestures)
        json.put("enableSpacebarCursor", s.enableSpacebarCursor)
        json.put("enableVoiceTyping", s.enableVoiceTyping)
        json.put("voiceLanguage", s.voiceLanguage)
        json.put("showLanguageSwitchKey", s.showLanguageSwitchKey)
        json.put("enableEmoji", s.enableEmoji)
        json.put("enableHaptics", s.enableHaptics)
        json.put("enableSound", s.enableSound)
        json.put("enableKeyPopup", s.enableKeyPopup)
        json.put("autoCapitalization", s.autoCapitalization)
        json.put("enableStylishFonts", s.enableStylishFonts)
        json.put("activeStylishStyle", s.activeStylishStyle)
        json.put("enableKeyAnimation", s.enableKeyAnimation)
        json.put("defaultLanguage", s.defaultLanguage.name)
        return json.toString()
    }

    fun applySettingsFromJson(jsonStr: String): Boolean {
        return try {
            val json = org.json.JSONObject(jsonStr)
            if (json.has("themeId")) themeId = json.getString("themeId")
            if (json.has("followSystemTheme")) followSystemTheme = json.getBoolean("followSystemTheme")
            if (json.has("lightThemeId")) lightThemeId = json.getString("lightThemeId")
            if (json.has("darkThemeId")) darkThemeId = json.getString("darkThemeId")
            if (json.has("lineThicknessDp")) lineThicknessDp = json.getDouble("lineThicknessDp").toFloat()
            if (json.has("keyboardSize")) {
                try { keyboardSize = KeyboardSize.valueOf(json.getString("keyboardSize")) } catch (_: Exception) {}
            }
            if (json.has("showNumberRow")) showNumberRow = json.getBoolean("showNumberRow")
            if (json.has("showSuggestions")) showSuggestions = json.getBoolean("showSuggestions")
            if (json.has("enableWordLearning")) enableWordLearning = json.getBoolean("enableWordLearning")
            if (json.has("enableBanglish")) enableBanglish = json.getBoolean("enableBanglish")
            if (json.has("enableGestures")) enableGestures = json.getBoolean("enableGestures")
            if (json.has("enableSpacebarCursor")) enableSpacebarCursor = json.getBoolean("enableSpacebarCursor")
            if (json.has("enableVoiceTyping")) enableVoiceTyping = json.getBoolean("enableVoiceTyping")
            if (json.has("voiceLanguage")) voiceLanguage = json.getString("voiceLanguage")
            if (json.has("showLanguageSwitchKey")) showLanguageSwitchKey = json.getBoolean("showLanguageSwitchKey")
            if (json.has("enableEmoji")) enableEmoji = json.getBoolean("enableEmoji")
            if (json.has("enableHaptics")) enableHaptics = json.getBoolean("enableHaptics")
            if (json.has("enableSound")) enableSound = json.getBoolean("enableSound")
            if (json.has("enableKeyPopup")) enableKeyPopup = json.getBoolean("enableKeyPopup")
            if (json.has("autoCapitalization")) autoCapitalization = json.getBoolean("autoCapitalization")
            if (json.has("enableStylishFonts")) enableStylishFonts = json.getBoolean("enableStylishFonts")
            if (json.has("activeStylishStyle")) activeStylishStyle = json.getString("activeStylishStyle")
            if (json.has("enableKeyAnimation")) enableKeyAnimation = json.getBoolean("enableKeyAnimation")
            if (json.has("defaultLanguage")) {
                try { defaultLanguage = KeyboardLanguage.valueOf(json.getString("defaultLanguage")) } catch (_: Exception) {}
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_FOLLOW_SYSTEM_THEME = "follow_system_theme"
        private const val KEY_LIGHT_THEME_ID = "light_theme_id"
        private const val KEY_DARK_THEME_ID = "dark_theme_id"
        private const val KEY_LINE_THICKNESS = "line_thickness_dp"
        private const val KEY_KEYBOARD_SIZE = "keyboard_size"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_SHOW_SUGGESTIONS = "show_suggestions"
        private const val KEY_ENABLE_WORD_LEARNING = "enable_word_learning"
        private const val KEY_ENABLE_BANGLISH = "enable_banglish"
        private const val KEY_ENABLE_GESTURES = "enable_gestures"
        private const val KEY_ENABLE_SPACEBAR_CURSOR = "enable_spacebar_cursor"
        private const val KEY_ENABLE_VOICE_TYPING = "enable_voice_typing"
        private const val KEY_VOICE_LANGUAGE = "voice_language"
        private const val KEY_SHOW_LANGUAGE_SWITCH_KEY = "show_language_switch_key"
        private const val KEY_ENABLE_EMOJI = "enable_emoji"
        private const val KEY_ENABLE_HAPTICS = "enable_haptics"
        private const val KEY_ENABLE_SOUND = "enable_sound"
        private const val KEY_ENABLE_KEY_POPUP = "enable_key_popup"
        private const val KEY_AUTO_CAPITALIZATION = "auto_capitalization"
        private const val KEY_ENABLE_STYLISH_FONTS = "enable_stylish_fonts"
        private const val KEY_ACTIVE_STYLISH_STYLE = "active_stylish_style"
        private const val KEY_ENABLE_KEY_ANIMATION = "enable_key_animation"
        private const val KEY_DEFAULT_LANGUAGE = "default_language"
        private const val KEY_RECENT_EMOJIS = "recent_emojis"
        private const val KEY_RECENT_SYMBOLS = "recent_symbols"
        private const val KEY_CLIPBOARD_HISTORY = "clipboard_history"
        private const val KEY_AUTH_USER_ID = "auth_user_id"
        private const val KEY_AUTH_USER_EMAIL = "auth_user_email"
        private const val KEY_AUTH_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_LAST_CLOUD_SETTINGS_JSON = "last_cloud_settings_json"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_CLOUD_CLIPBOARD_JSON = "cloud_clipboard_json"
        private const val KEY_ENABLE_ONLINE_DICTIONARY = "enable_online_dictionary"
        private const val KEY_AUTO_CLOUD_SYNC = "auto_cloud_sync"
        private const val CLIPBOARD_SEPARATOR = "<!--SINGBORD_CLIP_SEP-->"
        private const val SYMBOL_SEPARATOR = "<!--SINGBORD_SYM_SEP-->"
    }
}
