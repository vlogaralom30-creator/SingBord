package com.example

import android.content.ClipboardManager
import android.content.Context
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.inputmethod.EditorInfo
import com.example.data.UserDictionaryRepository
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Palette
import com.example.data.CloudClipboardItem
import com.example.data.SupabaseBackendClient
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalKeyboardTheme = staticCompositionLocalOf { KeyboardThemes.CLEAN_LIGHT }

enum class ShiftState {
    OFF,
    ON,
    CAPS_LOCK
}

enum class KeyboardMode {
    ALPHA,
    NUMERIC_SYMBOLS,
    ALT_SYMBOLS,
    EMOJI
}

enum class ToolbarMode {
    SUGGESTIONS,
    TOOLS,
    VOICE_INLINE
}

enum class KeyboardSubPanel {
    NONE,
    SYMBOLS_STUDIO,
    TEXT_EDITOR,
    CLIPBOARD,
    NUMBER_DIALER,
    THEME_PICKER,
    FONT_PICKER,
    VOICE_INPUT
}

interface KeyboardActionListener {
    fun onTextEntered(text: String)
    fun onDelete()
    fun onEnter()
    fun onSpace()
    fun onDoubleSpacePeriod()
    fun onMoveCursor(direction: Int)
    fun onWordSelected(word: String, prefixLength: Int)
    fun onCut() {}
    fun onCopy() {}
    fun onPaste() {}
    fun onSelectAll() {}
    fun onMoveCursorVertical(direction: Int) {}
    fun onMoveToStart() {}
    fun onMoveToEnd() {}
    fun onSelectText(direction: Int) {}
    fun onVoiceInput(languageCode: String? = null) {}
    fun onOpenSettings() {}
}

@Composable
fun SingBordKeyboardView(
    settings: KeyboardSettings,
    listener: KeyboardActionListener?,
    editorInfo: EditorInfo? = null,
    modifier: Modifier = Modifier
) {
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var keyboardMode by remember { mutableStateOf(KeyboardMode.ALPHA) }
    var currentLanguage by remember(settings.defaultLanguage) { mutableStateOf(settings.defaultLanguage) }
    var toolbarMode by remember { mutableStateOf(ToolbarMode.SUGGESTIONS) }
    var activeSubPanel by remember { mutableStateOf(KeyboardSubPanel.NONE) }
    var activePopupKey by remember { mutableStateOf<String?>(null) }
    var currentComposingWord by remember { mutableStateOf("") }
    var lastCommittedWord by remember { mutableStateOf("") }
    var activeFontStyle by remember(settings.activeStylishStyle) {
        mutableStateOf(StylishFontEngine.getStyle(settings.activeStylishStyle))
    }
    var activeThemeId by remember(settings.themeId) { mutableStateOf(settings.themeId) }

    val view = LocalView.current
    val context = LocalContext.current
    val prefs = remember { SingBordPreferences(context) }
    val userRepo = remember { UserDictionaryRepository.getInstance(context) }
    var selectedEmojiCategory by remember { mutableStateOf(EmojiCategory.SMILEYS) }
    var recentEmojis by remember { mutableStateOf(prefs.getRecentEmojis()) }
    val coroutineScope = rememberCoroutineScope()

    // Dedicated Voice Recognition State for Inline Voice Mode
    var voiceState by remember { mutableStateOf<SingBordVoiceInputManager.VoiceState>(SingBordVoiceInputManager.VoiceState.Idle) }
    var voiceLiveText by remember { mutableStateOf("") }
    var voiceRmsLevel by remember { mutableStateOf(0f) }
    var voiceLanguageCode by remember(currentLanguage, settings.voiceLanguage) {
        val initialLang = if (settings.voiceLanguage == "auto") {
            if (currentLanguage == KeyboardLanguage.ENGLISH) "en-US" else "bn-BD"
        } else {
            settings.voiceLanguage
        }
        mutableStateOf(initialLang)
    }

    val voiceManager = remember {
        SingBordVoiceInputManager(
            context = context,
            onPartialResult = { partial ->
                voiceLiveText = partial
            },
            onFinalResult = { final ->
                voiceLiveText = final
                if (final.isNotBlank()) {
                    listener?.onTextEntered(final + " ")
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    coroutineScope.launch {
                        delay(1200)
                        if (voiceLiveText == final) {
                            voiceLiveText = ""
                        }
                    }
                }
            },
            onStateChanged = { state ->
                voiceState = state
            },
            onRmsLevelChanged = { level ->
                voiceRmsLevel = level
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.cancel()
        }
    }

    // Auto-sync voice recognition language whenever user switches keyboard language
    LaunchedEffect(currentLanguage) {
        if (settings.voiceLanguage == "auto") {
            val newLang = if (currentLanguage == KeyboardLanguage.ENGLISH) "en-US" else "bn-BD"
            voiceLanguageCode = newLang
            if (toolbarMode == ToolbarMode.VOICE_INLINE) {
                voiceManager.startListening(newLang, continuous = true)
            }
        }
    }

    fun cycleLanguage() {
        currentLanguage = when (currentLanguage) {
            KeyboardLanguage.ENGLISH -> KeyboardLanguage.BANGLA_PROBHAT
            KeyboardLanguage.BANGLA_PROBHAT -> KeyboardLanguage.AVRO
            KeyboardLanguage.AVRO -> KeyboardLanguage.ENGLISH
        }
        currentComposingWord = ""
    }

    fun isSensitiveInput(): Boolean {
        if (editorInfo == null) return false
        val inputType = editorInfo.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isPassword = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                variation == 0x00000010 // TYPE_NUMBER_VARIATION_PASSWORD
        val isNoLearning = (editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
        return isPassword || isNoLearning
    }

    fun maybeLearnWord(word: String) {
        if (settings.enableWordLearning && !isSensitiveInput()) {
            userRepo.recordWord(word)
        }
    }

    fun maybeLearnBigram(prev: String, next: String) {
        if (settings.enableWordLearning && !isSensitiveInput() && prev.isNotBlank() && next.isNotBlank()) {
            userRepo.recordBigram(prev, next)
        }
    }

    fun triggerFeedback() {
        if (settings.enableHaptics) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (settings.enableSound) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun handleEmojiPress(emoji: String) {
        triggerFeedback()
        currentComposingWord = ""
        listener?.onTextEntered(emoji)
        prefs.addRecentEmoji(emoji)
        recentEmojis = prefs.getRecentEmojis()
    }

    fun handleKeyPress(text: String, isRawChar: Boolean = false) {
        triggerFeedback()
        if (toolbarMode == ToolbarMode.TOOLS) {
            toolbarMode = ToolbarMode.SUGGESTIONS
        }
        if (activeSubPanel != KeyboardSubPanel.NONE) {
            activeSubPanel = KeyboardSubPanel.NONE
        }
        if (settings.enableKeyPopup) {
            activePopupKey = text
            coroutineScope.launch {
                delay(120)
                if (activePopupKey == text) {
                    activePopupKey = null
                }
            }
        }

        val isBangla = text.any { it in '\u0980'..'\u09FF' } || currentLanguage == KeyboardLanguage.BANGLA_PROBHAT || isRawChar
        val rawCharToCommit = if (isBangla) {
            text
        } else {
            when (shiftState) {
                ShiftState.ON, ShiftState.CAPS_LOCK -> text.uppercase()
                ShiftState.OFF -> text.lowercase()
            }
        }

        val charToCommit = if (!isBangla && settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
            StylishFontEngine.transformText(rawCharToCommit, activeFontStyle)
        } else {
            rawCharToCommit
        }
        listener?.onTextEntered(charToCommit)

        val isWordConstituent = rawCharToCommit.all {
            it.isLetter() || it == '\'' || it in '\u0980'..'\u09FF'
        }
        if (isWordConstituent) {
            currentComposingWord += rawCharToCommit
        } else {
            if (currentComposingWord.isNotBlank()) {
                maybeLearnWord(currentComposingWord)
            }
            currentComposingWord = ""
        }

        if (shiftState == ShiftState.ON) {
            shiftState = ShiftState.OFF
        }
    }

    val isSystemDark = isSystemInDarkTheme()
    val theme = remember(
        activeThemeId,
        settings.followSystemTheme,
        settings.lightThemeId,
        settings.darkThemeId,
        isSystemDark
    ) {
        KeyboardThemes.resolveTheme(
            themeId = activeThemeId,
            followSystemTheme = settings.followSystemTheme,
            lightThemeId = settings.lightThemeId,
            darkThemeId = settings.darkThemeId,
            isSystemDark = isSystemDark
        )
    }

    val keyHeight = (46 * settings.keyboardSize.heightFactor).dp
    val lineThicknessDp = settings.lineThicknessDp.dp
    val gridBorderColor = theme.gridBorderColor
    val letterKeyBg = theme.keyBg
    val functionKeyBg = theme.functionKeyBg
    val keyPressedBg = if (theme.isDark) Color(0xFF3F3F46) else Color(0xFFCBD5E1)
    val textColor = theme.keyTextColor
    val functionTextColor = theme.functionTextColor
    val accentColor = theme.accentColor
    val accentTextColor = theme.accentTextColor

    val is5RowLayout = settings.showNumberRow
    val topNumberRowHeight = if (is5RowLayout) keyHeight * 0.85f else 0.dp
    val totalContentHeight = (keyHeight * 4) + topNumberRowHeight + (lineThicknessDp * if (is5RowLayout) 4 else 3)
    val totalSubPanelHeight = totalContentHeight

    val isFlatGrid = theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID

    CompositionLocalProvider(LocalKeyboardTheme provides theme) {
        val bgBrush = when {
            theme.bgMeshColors != null -> Brush.linearGradient(theme.bgMeshColors!!)
            theme.bgGradientTop != null && theme.bgGradientBottom != null -> Brush.verticalGradient(
                listOfNotNull(theme.bgGradientTop, theme.bgGradientCenter, theme.bgGradientBottom)
            )
            else -> null
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (bgBrush != null) Modifier.background(bgBrush)
                    else Modifier.background(theme.keyboardBg)
                )
                .then(
                    if (isFlatGrid) Modifier.border(width = lineThicknessDp, color = gridBorderColor)
                    else Modifier
                )
                .then(
                    if (!isFlatGrid) Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    else Modifier
                )
        ) {
            // Realistic Water Droplets & Condensation Canvas (Photos 3 & 4)
            if (theme.hasWaterDrops) {
                androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                    val dropSeeds = listOf(
                        Triple(0.06f, 0.12f, 7f),
                        Triple(0.18f, 0.38f, 13f),
                        Triple(0.12f, 0.74f, 10f),
                        Triple(0.28f, 0.20f, 6f),
                        Triple(0.35f, 0.82f, 15f),
                        Triple(0.48f, 0.10f, 9f),
                        Triple(0.55f, 0.62f, 16f),
                        Triple(0.68f, 0.32f, 11f),
                        Triple(0.78f, 0.78f, 8f),
                        Triple(0.85f, 0.16f, 14f),
                        Triple(0.92f, 0.52f, 10f),
                        Triple(0.05f, 0.88f, 6f),
                        Triple(0.42f, 0.46f, 12f),
                        Triple(0.62f, 0.90f, 9f),
                        Triple(0.72f, 0.08f, 13f),
                        Triple(0.88f, 0.72f, 7f),
                        Triple(0.22f, 0.58f, 8f),
                        Triple(0.96f, 0.30f, 11f)
                    )
                    dropSeeds.forEach { (nx, ny, r) ->
                        val cx = size.width * nx
                        val cy = size.height * ny
                        val radius = r.dp.toPx()
                        // Soft Shadow
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.28f),
                            radius = radius,
                            center = Offset(cx + 1.5f, cy + 2f)
                        )
                        // Water Drop Refraction
                        drawCircle(
                            color = Color.White.copy(alpha = 0.24f),
                            radius = radius,
                            center = Offset(cx, cy)
                        )
                        // Specular Light Highlight
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = radius * 0.35f,
                            center = Offset(cx - radius * 0.35f, cy - radius * 0.35f)
                        )
                        // Softer Bottom Glow
                        drawCircle(
                            color = Color.White.copy(alpha = 0.40f),
                            radius = radius * 0.22f,
                            center = Offset(cx + radius * 0.25f, cy + radius * 0.35f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
            // Ridmik-Style Dual-Mode Top Toolbar
            // Ridmik-Style Dual-Mode Top Toolbar
            SingBordTopToolbar(
                theme = theme,
                settings = settings,
                isFlatGrid = isFlatGrid,
                lineThicknessDp = lineThicknessDp,
                gridBorderColor = gridBorderColor,
                textColor = textColor,
                toolbarMode = toolbarMode,
                activeSubPanel = activeSubPanel,
                keyboardMode = keyboardMode,
                currentLanguage = currentLanguage,
                currentComposingWord = currentComposingWord,
                lastCommittedWord = lastCommittedWord,
                activeFontStyle = activeFontStyle,
                userRepo = userRepo,
                voiceState = voiceState,
                voiceRmsLevel = voiceRmsLevel,
                voiceLiveText = voiceLiveText,
                voiceLanguageCode = voiceLanguageCode,
                listener = listener,
                onToggleTools = {
                    if (toolbarMode == ToolbarMode.VOICE_INLINE) {
                        voiceManager.cancel()
                    }
                    if (activeSubPanel != KeyboardSubPanel.NONE) {
                        activeSubPanel = KeyboardSubPanel.NONE
                        toolbarMode = ToolbarMode.SUGGESTIONS
                    } else {
                        toolbarMode = if (toolbarMode == ToolbarMode.SUGGESTIONS) ToolbarMode.TOOLS else ToolbarMode.SUGGESTIONS
                    }
                },
                onSelectEmojiMode = {
                    if (toolbarMode == ToolbarMode.VOICE_INLINE) {
                        voiceManager.cancel()
                    }
                    activeSubPanel = KeyboardSubPanel.NONE
                    keyboardMode = KeyboardMode.EMOJI
                    toolbarMode = ToolbarMode.SUGGESTIONS
                },
                onOpenSubPanel = { panel ->
                    if (toolbarMode == ToolbarMode.VOICE_INLINE) {
                        voiceManager.cancel()
                        toolbarMode = ToolbarMode.SUGGESTIONS
                    }
                    keyboardMode = KeyboardMode.ALPHA
                    activeSubPanel = if (activeSubPanel == panel) KeyboardSubPanel.NONE else panel
                },
                onSwitchToTools = {
                    if (toolbarMode == ToolbarMode.VOICE_INLINE) {
                        voiceManager.cancel()
                    }
                    toolbarMode = ToolbarMode.TOOLS
                },
                onSelectCandidate = { candidate ->
                    maybeLearnWord(candidate)
                    if (lastCommittedWord.isNotBlank()) {
                        maybeLearnBigram(lastCommittedWord, candidate)
                    }
                    lastCommittedWord = candidate
                    val wordToCommit = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                        StylishFontEngine.transformText(candidate, activeFontStyle)
                    } else {
                        candidate
                    }
                    val prefixLen = if (currentComposingWord.isNotBlank()) currentComposingWord.length else 0
                    listener?.onWordSelected(wordToCommit, prefixLen)
                    currentComposingWord = ""
                },
                onOpenVoiceInput = {
                    // Inline voice mode: keeps keyboard completely visible underneath!
                    activeSubPanel = KeyboardSubPanel.NONE
                    keyboardMode = KeyboardMode.ALPHA
                    toolbarMode = ToolbarMode.VOICE_INLINE
                    voiceLiveText = ""
                    val lang = if (settings.voiceLanguage == "auto") {
                        if (currentLanguage == KeyboardLanguage.ENGLISH) "en-US" else "bn-BD"
                    } else {
                        settings.voiceLanguage
                    }
                    voiceLanguageCode = lang
                    voiceManager.startListening(lang, continuous = true)
                },
                onOpenVoiceSubPanel = {
                    voiceManager.cancel()
                    toolbarMode = ToolbarMode.SUGGESTIONS
                    keyboardMode = KeyboardMode.ALPHA
                    activeSubPanel = KeyboardSubPanel.VOICE_INPUT
                },
                onToggleVoiceMic = {
                    if (voiceManager.isListening) {
                        voiceManager.stopListening()
                    } else {
                        voiceManager.startListening(voiceLanguageCode, continuous = true)
                    }
                },
                onSwitchVoiceLanguage = {
                    val nextLang = if (voiceLanguageCode.startsWith("bn")) "en-US" else "bn-BD"
                    voiceLanguageCode = nextLang
                    voiceLiveText = ""
                    voiceManager.startListening(nextLang, continuous = true)
                },
                onCloseVoiceInline = {
                    voiceManager.cancel()
                    voiceLiveText = ""
                    toolbarMode = ToolbarMode.SUGGESTIONS
                },
                triggerFeedback = { triggerFeedback() }
            )

            if (isFlatGrid) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (activeSubPanel != KeyboardSubPanel.NONE) {
                when (activeSubPanel) {
                    KeyboardSubPanel.SYMBOLS_STUDIO -> {
                        SymbolsSubPanel(
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            listener = listener,
                            prefs = prefs,
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.TEXT_EDITOR -> {
                        TextEditorSubPanel(
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            listener = listener,
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.CLIPBOARD -> {
                        ClipboardSubPanel(
                            theme = theme,
                            prefs = prefs,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            listener = listener,
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.NUMBER_DIALER -> {
                        NumberDialerSubPanel(
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            listener = listener,
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.THEME_PICKER -> {
                        ThemePickerSubPanel(
                            currentThemeId = activeThemeId,
                            settings = settings,
                            prefs = prefs,
                            isSystemDark = isSystemDark,
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            onThemeSelect = { newTheme ->
                                activeThemeId = newTheme.id
                                prefs.themeId = newTheme.id
                                if (settings.followSystemTheme) {
                                    if (newTheme.isDark) {
                                        prefs.darkThemeId = newTheme.id
                                    } else {
                                        prefs.lightThemeId = newTheme.id
                                    }
                                }
                                activeSubPanel = KeyboardSubPanel.NONE
                            },
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.FONT_PICKER -> {
                        FontPickerSubPanel(
                            activeStyle = activeFontStyle,
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            onStyleSelect = { style ->
                                activeFontStyle = style
                                prefs.activeStylishStyle = style.id
                                activeSubPanel = KeyboardSubPanel.NONE
                            },
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.VOICE_INPUT -> {
                        val resolvedVoiceLang = if (settings.voiceLanguage == "auto") {
                            if (currentLanguage == KeyboardLanguage.ENGLISH) "en-US" else "bn-BD"
                        } else {
                            settings.voiceLanguage
                        }
                        VoiceInputSubPanel(
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            initialLanguageCode = resolvedVoiceLang,
                            listener = listener,
                            onClose = { activeSubPanel = KeyboardSubPanel.NONE },
                            triggerFeedback = { triggerFeedback() }
                        )
                    }
                    KeyboardSubPanel.NONE -> {}
                }
            } else if (keyboardMode == KeyboardMode.EMOJI) {
                EmojiPickerLayer(
                    theme = theme,
                    recents = recentEmojis,
                    keyHeight = keyHeight,
                    onEmojiSelected = { emoji ->
                        handleEmojiPress(emoji)
                    },
                    onBackToAlpha = {
                        keyboardMode = KeyboardMode.ALPHA
                    },
                    onSpacePress = {
                        listener?.onSpace()
                    },
                    onBackspacePress = {
                        listener?.onDelete()
                    },
                    onFeedback = {
                        triggerFeedback()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(totalContentHeight)
                )
            } else {
                SingBordMainKeyRows(
                    theme = theme,
                    settings = settings,
                    isFlatGrid = isFlatGrid,
                    lineThicknessDp = lineThicknessDp,
                    gridBorderColor = gridBorderColor,
                    letterKeyBg = letterKeyBg,
                    functionKeyBg = functionKeyBg,
                    textColor = textColor,
                    functionTextColor = functionTextColor,
                    accentColor = accentColor,
                    accentTextColor = accentTextColor,
                    keyHeight = keyHeight,
                    keyboardMode = keyboardMode,
                    currentLanguage = currentLanguage,
                    shiftState = shiftState,
                    activeFontStyle = activeFontStyle,
                    onToggleShift = {
                        triggerFeedback()
                        shiftState = when (shiftState) {
                            ShiftState.OFF -> ShiftState.ON
                            ShiftState.ON -> ShiftState.CAPS_LOCK
                            ShiftState.CAPS_LOCK -> ShiftState.OFF
                        }
                    },
                    onKeyPress = { k, isRaw -> handleKeyPress(k, isRaw) },
                    onDelete = {
                        triggerFeedback()
                        if (currentComposingWord.isNotEmpty()) {
                            currentComposingWord = currentComposingWord.dropLast(1)
                        } else {
                            lastCommittedWord = ""
                        }
                        listener?.onDelete()
                    }
                )

                if (isFlatGrid) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )
                }

                // Row 4: Bottom Action Row
                SingBordBottomActionRow(
                    theme = theme,
                    settings = settings,
                    isFlatGrid = isFlatGrid,
                    lineThicknessDp = lineThicknessDp,
                    gridBorderColor = gridBorderColor,
                    letterKeyBg = letterKeyBg,
                    functionKeyBg = functionKeyBg,
                    keyPressedBg = keyPressedBg,
                    functionTextColor = functionTextColor,
                    accentColor = accentColor,
                    accentTextColor = accentTextColor,
                    keyHeight = keyHeight,
                    keyboardMode = keyboardMode,
                    currentLanguage = currentLanguage,
                    currentComposingWord = currentComposingWord,
                    lastCommittedWord = lastCommittedWord,
                    editorInfo = editorInfo,
                    listener = listener,
                    onToggleMode = {
                        keyboardMode = if (keyboardMode == KeyboardMode.ALPHA) {
                            KeyboardMode.NUMERIC_SYMBOLS
                        } else {
                            KeyboardMode.ALPHA
                        }
                    },
                    onToggleAltSymbols = {
                        keyboardMode = if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS) {
                            KeyboardMode.ALT_SYMBOLS
                        } else {
                            KeyboardMode.NUMERIC_SYMBOLS
                        }
                    },
                    onCycleLanguage = { cycleLanguage() },
                    onOpenEmojiPicker = {
                        if (recentEmojis.isNotEmpty()) {
                            selectedEmojiCategory = EmojiCategory.RECENT
                        } else {
                            selectedEmojiCategory = EmojiCategory.SMILEYS
                        }
                        keyboardMode = KeyboardMode.EMOJI
                    },
                    onKeyPress = { k, isRaw -> handleKeyPress(k, isRaw) },
                    onSpace = {
                        if (currentComposingWord.isNotBlank()) {
                            maybeLearnWord(currentComposingWord)
                            if (lastCommittedWord.isNotBlank()) {
                                maybeLearnBigram(lastCommittedWord, currentComposingWord)
                            }
                            lastCommittedWord = currentComposingWord
                        }
                        currentComposingWord = ""
                        listener?.onSpace()
                    },
                    onDoubleSpacePeriod = {
                        if (currentComposingWord.isNotBlank()) {
                            maybeLearnWord(currentComposingWord)
                            if (lastCommittedWord.isNotBlank()) {
                                maybeLearnBigram(lastCommittedWord, currentComposingWord)
                            }
                            lastCommittedWord = currentComposingWord
                        }
                        currentComposingWord = ""
                        listener?.onDoubleSpacePeriod()
                    },
                    onMoveCursor = { dir ->
                        currentComposingWord = ""
                        listener?.onMoveCursor(dir)
                    },
                    onEnter = {
                        triggerFeedback()
                        if (currentComposingWord.isNotBlank()) {
                            maybeLearnWord(currentComposingWord)
                        }
                        currentComposingWord = ""
                        listener?.onEnter()
                    },
                    triggerFeedback = { triggerFeedback() }
                )
                }
            }
        }
    }
}

@Composable
fun KeyCapsule(
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    isFunctionKey: Boolean = false,
    customBg: Color? = null,
    badgeColor: Color? = null,
    badgeShape: Shape = CircleShape,
    badgeContent: (@Composable () -> Unit)? = null,
    theme: KeyboardThemePalette = LocalKeyboardTheme.current,
    content: @Composable BoxScope.() -> Unit
) {
    if (theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .background(customBg ?: if (isFunctionKey) theme.functionKeyBg else theme.keyBg),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    } else {
        val isGlass = theme.keyShapeStyle == KeyboardKeyShapeStyle.GLASSMORPHIC_3D
        val cornerRadius = theme.keyCornerRadiusDp.dp
        val shape = RoundedCornerShape(cornerRadius)
        val hGap = (theme.keyHorizontalGapDp / 2).dp
        val vGap = (theme.keyVerticalGapDp / 2).dp

        val baseColor = customBg ?: if (isFunctionKey) theme.functionKeyBg else theme.keyBg

        val keyBrush = if (isGlass) {
            val topColor = if (isFunctionKey) (theme.functionKeyGradientTop ?: baseColor) else (theme.keyGradientTop ?: baseColor)
            val btmColor = if (isFunctionKey) (theme.functionKeyGradientBottom ?: baseColor) else (theme.keyGradientBottom ?: baseColor)
            Brush.verticalGradient(listOf(topColor, btmColor))
        } else if (theme.keyGradientTop != null && theme.keyGradientBottom != null) {
            val topColor = if (isFunctionKey) (theme.functionKeyGradientTop ?: theme.keyGradientTop!!) else theme.keyGradientTop!!
            val btmColor = if (isFunctionKey) (theme.functionKeyGradientBottom ?: theme.keyGradientBottom!!) else theme.keyGradientBottom!!
            Brush.verticalGradient(listOf(topColor, btmColor))
        } else null

        val borderStroke = if (theme.keyBorderWidthDp > 0f) {
            if (isGlass) {
                BorderStroke(
                    width = theme.keyBorderWidthDp.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            theme.keyBorderColor,
                            theme.keyBorderColor.copy(alpha = (theme.keyBorderColor.alpha * 0.35f).coerceAtLeast(0.08f))
                        )
                    )
                )
            } else {
                BorderStroke(
                    width = theme.keyBorderWidthDp.dp,
                    color = theme.keyBorderColor
                )
            }
        } else null

        Box(
            modifier = modifier
                .fillMaxHeight()
                .padding(horizontal = hGap, vertical = vGap)
        ) {
            // Neon Glow / 3D Bottom Lip Depth Shadow
            if (theme.keyElevationDp > 0f) {
                val shadowOffsetY = if (isPressed) (theme.keyElevationDp * 0.35f).dp else theme.keyElevationDp.dp
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = shadowOffsetY)
                        .clip(shape)
                        .background(theme.keyShadowColor)
                )
            }

            // Keycap Body
            val pressOffsetY = if (isPressed && theme.keyElevationDp > 0f) (theme.keyElevationDp * 0.65f).dp else 0.dp
            val activeBrush = if (isPressed && isGlass) {
                Brush.verticalGradient(listOf(theme.accentColor, theme.accentColor.copy(alpha = 0.85f)))
            } else keyBrush

            val activeBaseColor = if (isPressed) theme.accentColor else baseColor

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = pressOffsetY)
                    .clip(shape)
                    .then(if (activeBrush != null) Modifier.background(activeBrush) else Modifier.background(activeBaseColor))
                    .then(if (borderStroke != null) Modifier.border(borderStroke, shape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                // 3D Glass / Ice Cube Top Highlight Reflection (Photos 3 & 4)
                if (theme.hasWaterDrops || isGlass) {
                    val topHighlightRadius = (theme.keyCornerRadiusDp - 2f).coerceAtLeast(2f).dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.46f)
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(topStart = topHighlightRadius, topEnd = topHighlightRadius))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = if (theme.hasWaterDrops) 0.50f else 0.28f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                    )
                }

                if (badgeColor != null && badgeContent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.78f)
                            .clip(badgeShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        badgeContent()
                    }
                } else {
                    content()
                }
            }
        }
    }
}

@Composable
fun FlatKeyButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    subText: String? = null,
    alternates: List<String> = emptyList(),
    pressedColor: Color? = null,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = 18.sp,
    enableAnimation: Boolean = true,
    isFunctionKey: Boolean = false,
    badgeColor: Color? = null,
    badgeShape: Shape = CircleShape,
    onAlternateSelected: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
    val theme = LocalKeyboardTheme.current
    var isPressed by remember { mutableStateOf(false) }
    var showAlternates by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var keySize by remember { mutableStateOf(IntSize.Zero) }

    val resolvedFontWeight = fontWeight ?: theme.keyTextFontWeight

    val scale by animateFloatAsState(
        targetValue = if (enableAnimation && isPressed) {
            if (theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) 0.88f else 0.94f
        } else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "flat_key_scale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 120),
        label = "flat_key_overlay"
    )

    val resolvedPressedColor = pressedColor ?: run {
        val luminance = (backgroundColor.red * 0.299f + backgroundColor.green * 0.587f + backgroundColor.blue * 0.114f)
        if (luminance < 0.5f) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.14f)
    }

    KeyCapsule(
        modifier = modifier
            .onSizeChanged { keySize = it }
            .pointerInput(alternates) {
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = {
                        if (alternates.isNotEmpty()) {
                            showAlternates = true
                        }
                    },
                    onTap = { onClick() }
                )
            },
        isPressed = isPressed,
        isFunctionKey = isFunctionKey,
        customBg = backgroundColor,
        badgeColor = badgeColor,
        badgeShape = badgeShape,
        badgeContent = if (badgeColor != null) {
            {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        } else null,
        theme = theme
    ) {
        if (overlayAlpha > 0.01f && theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) {
            val radius = maxOf(keySize.width.toFloat(), keySize.height.toFloat(), 100f) * 1.2f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = overlayAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                resolvedPressedColor,
                                resolvedPressedColor.copy(alpha = (resolvedPressedColor.alpha * 0.3f))
                            ),
                            center = touchOffset,
                            radius = radius
                        )
                    )
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!subText.isNullOrBlank()) {
                Text(
                    text = subText,
                    color = textColor.copy(alpha = 0.50f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = 2.dp)
                )
            }

            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = resolvedFontWeight,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
            )
        }

        if (showAlternates && alternates.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -115),
                onDismissRequest = { showAlternates = false }
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.keyboardBg)
                        .border(1.dp, theme.accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        alternates.forEach { alt ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (alt == text) theme.accentColor.copy(alpha = 0.25f) else theme.keyBg)
                                    .clickable {
                                        showAlternates = false
                                        if (onAlternateSelected != null) {
                                            onAlternateSelected(alt)
                                        } else {
                                            onClick()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = alt,
                                    fontSize = 17.sp,
                                    color = theme.keyTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconKeyButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    pressedColor: Color? = null,
    enableAnimation: Boolean = true,
    isFunctionKey: Boolean = true,
    badgeColor: Color? = null,
    badgeShape: Shape = CircleShape,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val theme = LocalKeyboardTheme.current
    var isPressed by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var keySize by remember { mutableStateOf(IntSize.Zero) }

    val scale by animateFloatAsState(
        targetValue = if (enableAnimation && isPressed) {
            if (theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) 0.88f else 0.92f
        } else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "icon_key_scale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 120),
        label = "icon_key_overlay"
    )

    val resolvedPressedColor = pressedColor ?: run {
        val luminance = (backgroundColor.red * 0.299f + backgroundColor.green * 0.587f + backgroundColor.blue * 0.114f)
        if (luminance < 0.5f) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.14f)
    }

    KeyCapsule(
        modifier = modifier
            .onSizeChanged { keySize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = {
                        if (onLongClick != null) {
                            onLongClick()
                        }
                    },
                    onTap = { onClick() }
                )
            },
        isPressed = isPressed,
        isFunctionKey = isFunctionKey,
        customBg = backgroundColor,
        badgeColor = badgeColor,
        badgeShape = badgeShape,
        badgeContent = if (badgeColor != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        } else null,
        theme = theme
    ) {
        if (overlayAlpha > 0.01f && theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) {
            val radius = maxOf(keySize.width.toFloat(), keySize.height.toFloat(), 100f) * 1.2f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = overlayAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                resolvedPressedColor,
                                resolvedPressedColor.copy(alpha = (resolvedPressedColor.alpha * 0.3f))
                            ),
                            center = touchOffset,
                            radius = radius
                        )
                    )
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )
    }
}

@Composable
fun EnterKeyButton(
    editorInfo: EditorInfo?,
    accentColor: Color = Color(0xFF2563EB),
    accentTextColor: Color = Color.White,
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true,
    badgeColor: Color? = null,
    onClick: () -> Unit
) {
    val theme = LocalKeyboardTheme.current
    val actionId = editorInfo?.let { info ->
        val id = info.actionId
        if (id != EditorInfo.IME_ACTION_UNSPECIFIED && id != EditorInfo.IME_ACTION_NONE) id
        else (info.imeOptions and EditorInfo.IME_MASK_ACTION)
    } ?: EditorInfo.IME_ACTION_NONE

    val (enterIcon, enterDescription) = when (actionId) {
        EditorInfo.IME_ACTION_SEARCH -> Icons.Default.Search to "Search"
        EditorInfo.IME_ACTION_SEND -> Icons.AutoMirrored.Filled.Send to "Send"
        EditorInfo.IME_ACTION_GO -> Icons.AutoMirrored.Filled.ArrowForward to "Go"
        EditorInfo.IME_ACTION_NEXT -> Icons.Default.NavigateNext to "Next"
        EditorInfo.IME_ACTION_DONE -> Icons.Default.Check to "Done"
        else -> Icons.Default.KeyboardReturn to "Enter"
    }

    var isPressed by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var keySize by remember { mutableStateOf(IntSize.Zero) }

    val scale by animateFloatAsState(
        targetValue = if (enableAnimation && isPressed) {
            if (theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) 0.88f else 0.92f
        } else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "enter_key_scale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 120),
        label = "enter_key_overlay"
    )

    val activeBadgeColor = badgeColor ?: theme.enterBadgeColor

    KeyCapsule(
        modifier = modifier
            .onSizeChanged { keySize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        isPressed = isPressed,
        isFunctionKey = true,
        customBg = accentColor,
        badgeColor = activeBadgeColor,
        badgeShape = RoundedCornerShape(12.dp),
        badgeContent = if (activeBadgeColor != null) {
            {
                Icon(
                    imageVector = enterIcon,
                    contentDescription = enterDescription,
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        } else null,
        theme = theme
    ) {
        if (overlayAlpha > 0.01f && theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) {
            val radius = maxOf(keySize.width.toFloat(), keySize.height.toFloat(), 100f) * 1.2f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = overlayAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.2f)
                            ),
                            center = touchOffset,
                            radius = radius
                        )
                    )
            )
        }

        if (theme.isKawaiiDessert) {
            Text(
                text = "🍰",
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
            )
        } else {
            Icon(
                imageVector = enterIcon,
                contentDescription = enterDescription,
                tint = accentTextColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
            )
        }
    }
}

@Composable
fun RepeatingBackspaceKey(
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    pressedColor: Color? = null,
    enableAnimation: Boolean = true,
    badgeColor: Color? = null,
    onDelete: () -> Unit
) {
    val theme = LocalKeyboardTheme.current
    var isPressed by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var keySize by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (enableAnimation && isPressed) {
            if (theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) 0.88f else 0.92f
        } else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "backspace_key_scale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 120),
        label = "backspace_key_overlay"
    )

    val resolvedPressedColor = pressedColor ?: run {
        val luminance = (backgroundColor.red * 0.299f + backgroundColor.green * 0.587f + backgroundColor.blue * 0.114f)
        if (luminance < 0.5f) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.14f)
    }

    val activeBadgeColor = badgeColor ?: theme.backspaceBadgeColor

    KeyCapsule(
        modifier = modifier
            .onSizeChanged { keySize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        isPressed = true
                        onDelete()
                        val job = coroutineScope.launch {
                            delay(400) // initial hold threshold
                            while (isPressed) {
                                onDelete()
                                delay(60) // repeat rate
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        job.cancel()
                    }
                )
            },
        isPressed = isPressed,
        isFunctionKey = true,
        customBg = backgroundColor,
        badgeColor = activeBadgeColor,
        badgeShape = CircleShape,
        badgeContent = if (activeBadgeColor != null) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier
                        .size(17.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        } else null,
        theme = theme
    ) {
        if (overlayAlpha > 0.01f && theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) {
            val radius = maxOf(keySize.width.toFloat(), keySize.height.toFloat(), 100f) * 1.2f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = overlayAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                resolvedPressedColor,
                                resolvedPressedColor.copy(alpha = (resolvedPressedColor.alpha * 0.3f))
                            ),
                            center = touchOffset,
                            radius = radius
                        )
                    )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Delete",
            tint = iconColor,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )
    }
}

@Composable
fun SpacebarKey(
    backgroundColor: Color,
    currentLanguage: KeyboardLanguage = KeyboardLanguage.BANGLA_PROBHAT,
    pressedColor: Color = Color(0xFFE2E8F0),
    dragColor: Color = Color(0xFFCBD5E1),
    enableGestures: Boolean = true,
    enableAnimation: Boolean = true,
    modifier: Modifier = Modifier,
    onSpace: () -> Unit,
    onDoubleSpacePeriod: () -> Unit,
    onMoveCursor: (Int) -> Unit,
    onLanguageToggle: (() -> Unit)? = null,
    triggerFeedback: () -> Unit
) {
    val theme = LocalKeyboardTheme.current
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var keySize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val stepThresholdPx = with(density) { 16.dp.toPx() }

    val scale by animateFloatAsState(
        targetValue = if (enableAnimation && isPressed && !isDragging) {
            if (theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) 0.94f else 0.98f
        } else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "spacebar_scale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed || isDragging) 1f else 0f,
        animationSpec = tween(durationMillis = 60),
        label = "spacebar_overlay_alpha"
    )

    val currentBg = when {
        isDragging -> dragColor
        isPressed -> pressedColor
        else -> backgroundColor
    }

    KeyCapsule(
        modifier = modifier
            .onSizeChanged { keySize = it }
            .pointerInput(enableGestures) {
                if (!enableGestures) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchOffset = offset
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onLongPress = {
                            triggerFeedback()
                            onLanguageToggle?.invoke()
                        },
                        onTap = {
                            triggerFeedback()
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 450L) {
                                lastTapTime = 0L
                                onDoubleSpacePeriod()
                            } else {
                                lastTapTime = now
                                onSpace()
                            }
                        }
                    )
                } else {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        touchOffset = down.position
                        isPressed = true
                        var accumulatedDx = 0f
                        var hasDragged = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                change.consume()
                                break
                            }

                            val dx = change.position.x - change.previousPosition.x
                            if (kotlin.math.abs(change.position.x - down.position.x) > 12f || hasDragged) {
                                accumulatedDx += dx
                                if (kotlin.math.abs(accumulatedDx) >= stepThresholdPx) {
                                    hasDragged = true
                                    isDragging = true
                                    val steps = (accumulatedDx / stepThresholdPx).toInt()
                                    if (steps != 0) {
                                        triggerFeedback()
                                        onMoveCursor(if (steps > 0) 1 else -1)
                                        accumulatedDx -= steps * stepThresholdPx
                                    }
                                }
                                change.consume()
                            }
                        }

                        isPressed = false
                        val wasDragging = isDragging
                        isDragging = false

                        if (!hasDragged && !wasDragging) {
                            triggerFeedback()
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 450L) {
                                lastTapTime = 0L
                                onDoubleSpacePeriod()
                            } else {
                                lastTapTime = now
                                onSpace()
                            }
                        }
                    }
                }
            },
        isPressed = isPressed,
        isFunctionKey = false,
        customBg = currentBg,
        theme = theme
    ) {
        if (overlayAlpha > 0.01f && theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID) {
            val radius = maxOf(keySize.width.toFloat(), keySize.height.toFloat(), 100f) * 1.2f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = overlayAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = touchOffset,
                            radius = radius
                        )
                    )
            )
        }

        val spaceDisplayText = when {
            enableGestures && isDragging -> "‹ ── Slide cursor ── ›"
            theme.spaceBarLabel.isNotBlank() -> theme.spaceBarLabel
            else -> "◀  ${currentLanguage.spacebarLabel}  ▶"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
        ) {
            if (theme.showSpaceVoiceGlyph && !isDragging) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = theme.functionTextColor.copy(alpha = 0.55f),
                    modifier = Modifier
                        .size(15.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = spaceDisplayText,
                fontSize = if (isDragging) 11.sp else 13.sp,
                color = if (isDragging) theme.accentColor else theme.functionTextColor.copy(alpha = 0.75f),
                fontWeight = if (isDragging) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ToolIconItem(
    icon: ImageVector,
    contentDescription: String,
    theme: KeyboardThemePalette,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val isGlass = theme.keyShapeStyle == KeyboardKeyShapeStyle.GLASSMORPHIC_3D
    val isElevated = theme.keyShapeStyle == KeyboardKeyShapeStyle.ROUNDED_ELEVATED
    val isModern = isGlass || isElevated
    val itemShape = RoundedCornerShape(if (isModern) 8.dp else 16.dp)

    Box(
        modifier = Modifier
            .size(33.dp)
            .clip(itemShape)
            .then(
                when {
                    isActive -> Modifier.background(theme.accentColor)
                    isModern -> Modifier
                        .background(theme.functionKeyBg.copy(alpha = if (theme.isDark) 0.65f else 0.85f))
                        .then(
                            if (theme.keyBorderWidthDp > 0f) Modifier.border(0.7.dp, theme.keyBorderColor.copy(alpha = 0.5f), itemShape)
                            else Modifier
                        )
                    else -> Modifier.background(theme.candidateBg)
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) theme.accentTextColor else theme.functionTextColor,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
fun TextEditorSubPanel(
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    listener: KeyboardActionListener?,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    var selectMode by remember { mutableStateOf(false) }
    val isFlatGrid = theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        // Quick Action Row: Select All, Cut, Copy, Paste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            FlatKeyButton(
                text = "Select All",
                fontSize = 12.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                isFunctionKey = true,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onSelectAll()
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "Cut",
                fontSize = 12.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                isFunctionKey = true,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerFeedback()
                    listener?.onCut()
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "Copy",
                fontSize = 12.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                isFunctionKey = true,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerFeedback()
                    listener?.onCopy()
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "Paste",
                fontSize = 12.sp,
                backgroundColor = theme.accentColor,
                textColor = theme.accentTextColor,
                fontWeight = FontWeight.Bold,
                isFunctionKey = true,
                badgeColor = theme.enterBadgeColor,
                modifier = Modifier.weight(1.1f),
                onClick = {
                    triggerFeedback()
                    listener?.onPaste()
                }
            )
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // D-Pad Row 1: Home, UP Arrow, End
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
        ) {
            FlatKeyButton(
                text = "⇤ Home",
                fontSize = 13.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                isFunctionKey = true,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onMoveToStart()
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "▲",
                fontSize = 18.sp,
                backgroundColor = theme.keyBg,
                textColor = theme.keyTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.6f),
                onClick = {
                    triggerFeedback()
                    if (selectMode) {
                        listener?.onSelectText(-2)
                    } else {
                        listener?.onMoveCursorVertical(-1)
                    }
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "End ⇥",
                fontSize = 13.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                isFunctionKey = true,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onMoveToEnd()
                }
            )
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // D-Pad Row 2: LEFT Arrow, SELECT TOGGLE, RIGHT Arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
        ) {
            FlatKeyButton(
                text = "◀",
                fontSize = 18.sp,
                backgroundColor = theme.keyBg,
                textColor = theme.keyTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    if (selectMode) {
                        listener?.onSelectText(-1)
                    } else {
                        listener?.onMoveCursor(-1)
                    }
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = if (selectMode) "Select: ON" else "Select: OFF",
                fontSize = 13.sp,
                backgroundColor = if (selectMode) theme.accentColor else theme.functionKeyBg,
                textColor = if (selectMode) theme.accentTextColor else theme.functionTextColor,
                fontWeight = FontWeight.Bold,
                isFunctionKey = true,
                modifier = Modifier.weight(1.6f),
                onClick = {
                    triggerFeedback()
                    selectMode = !selectMode
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "▶",
                fontSize = 18.sp,
                backgroundColor = theme.keyBg,
                textColor = theme.keyTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    if (selectMode) {
                        listener?.onSelectText(1)
                    } else {
                        listener?.onMoveCursor(1)
                    }
                }
            )
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // D-Pad Row 3: Return to Keyboard, DOWN Arrow, Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
        ) {
            FlatKeyButton(
                text = "ABC",
                fontSize = 13.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.Bold,
                isFunctionKey = true,
                badgeColor = theme.modeBadgeColor,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    onClose()
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "▼",
                fontSize = 18.sp,
                backgroundColor = theme.keyBg,
                textColor = theme.keyTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.6f),
                onClick = {
                    triggerFeedback()
                    if (selectMode) {
                        listener?.onSelectText(2)
                    } else {
                        listener?.onMoveCursorVertical(1)
                    }
                }
            )
            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }
            FlatKeyButton(
                text = "⌫",
                fontSize = 16.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.Bold,
                isFunctionKey = true,
                badgeColor = theme.backspaceBadgeColor,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onDelete()
                }
            )
        }
    }
}

@Composable
fun NumberDialerSubPanel(
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    listener: KeyboardActionListener?,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    val dialerRows = listOf(
        listOf("1", "2", "3", "⌫"),
        listOf("4", "5", "6", "+"),
        listOf("7", "8", "9", "-"),
        listOf("*", "0", "#", "⏎"),
        listOf(".", ",", "/", "␣", "ABC")
    )
    val isFlatGrid = theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        dialerRows.forEachIndexed { rowIndex, rowKeys ->
            if (rowIndex > 0) {
                if (isFlatGrid) {
                    Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                rowKeys.forEachIndexed { colIndex, key ->
                    if (colIndex > 0) {
                        if (isFlatGrid) {
                            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                        }
                    }
                    val isAction = key in listOf("⌫", "⏎", "ABC", "␣", "+", "-", "*", "#", "/")
                    val isReturn = key == "ABC"
                    val isEnter = key == "⏎"
                    val isBackspace = key == "⌫"
                    val isSpace = key == "␣"

                    val bg = when {
                        isReturn -> theme.functionKeyBg
                        isEnter -> theme.accentColor
                        isAction -> theme.functionKeyBg
                        else -> theme.keyBg
                    }
                    val txtColor = when {
                        isReturn -> theme.functionTextColor
                        isEnter -> theme.accentTextColor
                        isAction -> theme.functionTextColor
                        else -> theme.keyTextColor
                    }

                    FlatKeyButton(
                        text = key,
                        fontSize = if (key.length > 2) 13.sp else 18.sp,
                        fontWeight = if (isAction) FontWeight.Bold else FontWeight.Medium,
                        backgroundColor = bg,
                        textColor = txtColor,
                        isFunctionKey = isAction,
                        badgeColor = when {
                            isBackspace -> theme.backspaceBadgeColor
                            isEnter -> theme.enterBadgeColor
                            isReturn -> theme.modeBadgeColor
                            else -> null
                        },
                        modifier = Modifier.weight(if (isSpace) 1.5f else 1f),
                        onClick = {
                            triggerFeedback()
                            when {
                                isReturn -> onClose()
                                isBackspace -> listener?.onDelete()
                                isEnter -> listener?.onEnter()
                                isSpace -> listener?.onSpace()
                                else -> listener?.onTextEntered(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemePickerSubPanel(
    currentThemeId: String,
    settings: KeyboardSettings,
    prefs: SingBordPreferences,
    isSystemDark: Boolean,
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    onThemeSelect: (KeyboardThemePalette) -> Unit,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    var isAutoSystem by remember { mutableStateOf(prefs.followSystemTheme) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(theme.functionKeyBg)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = theme.accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Themes & Appearance",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.functionTextColor,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .clickable {
                        triggerFeedback()
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = theme.functionTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        // Auto System Dark/Light Toggle Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(if (isAutoSystem) theme.accentColor.copy(alpha = 0.15f) else theme.candidateBg)
                .clickable {
                    triggerFeedback()
                    val newVal = !isAutoSystem
                    isAutoSystem = newVal
                    prefs.followSystemTheme = newVal
                }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isAutoSystem) theme.accentColor else theme.functionTextColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Auto Switch (System Dark/Light)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAutoSystem) theme.accentColor else theme.keyTextColor
                    )
                    Text(
                        text = if (isAutoSystem) "Syncing with OS (${if (isSystemDark) "Dark Mode" else "Light Mode"})" else "Manual static theme mode",
                        fontSize = 9.sp,
                        color = theme.functionTextColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAutoSystem) theme.accentColor else theme.functionKeyBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isAutoSystem) "AUTO ON ✓" else "OFF",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAutoSystem) theme.accentTextColor else theme.functionTextColor
                )
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        // Grid of Themes
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(theme.keyBg),
            contentPadding = PaddingValues(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(KeyboardThemes.ALL_THEMES) { itemTheme ->
                val isSelected = itemTheme.id.equals(currentThemeId, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(itemTheme.keyboardBg)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) itemTheme.accentColor else itemTheme.gridBorderColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            triggerFeedback()
                            onThemeSelect(itemTheme)
                        }
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isGlass = itemTheme.keyShapeStyle == KeyboardKeyShapeStyle.GLASSMORPHIC_3D
                        val previewBrush = if (itemTheme.keyGradientTop != null && itemTheme.keyGradientBottom != null) {
                            Brush.verticalGradient(listOf(itemTheme.keyGradientTop, itemTheme.keyGradientBottom))
                        } else null
                        val previewShape = RoundedCornerShape(if (isGlass) 5.dp else 3.dp)

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(previewShape)
                                .then(
                                    if (previewBrush != null) Modifier.background(previewBrush)
                                    else Modifier.background(itemTheme.keyBg)
                                )
                                .border(
                                    1.dp,
                                    itemTheme.keyBorderColor ?: itemTheme.accentColor,
                                    previewShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = itemTheme.keyTextColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = itemTheme.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = itemTheme.keyTextColor,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (itemTheme.isCustomTheme) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "💎",
                                        fontSize = 8.sp
                                    )
                                } else if (itemTheme.isDark) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "🌙",
                                        fontSize = 8.sp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "☀️",
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            Text(
                                text = if (isSelected) "Active ✓" else itemTheme.subtitle,
                                fontSize = 9.sp,
                                color = if (isSelected) itemTheme.accentColor else itemTheme.functionTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            FlatKeyButton(
                text = "Back to Keyboard",
                fontSize = 12.sp,
                backgroundColor = theme.accentColor,
                textColor = theme.accentTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    triggerFeedback()
                    onClose()
                }
            )
        }
    }
}

@Composable
fun FontPickerSubPanel(
    activeStyle: StylishFontStyle,
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    onStyleSelect: (StylishFontStyle) -> Unit,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(theme.functionKeyBg)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = theme.functionTextColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Choose Stylish Font",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.functionTextColor,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .clickable {
                        triggerFeedback()
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = theme.functionTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(theme.keyBg),
            contentPadding = PaddingValues(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(StylishFontEngine.ALL_STYLES) { style ->
                val isSelected = activeStyle == style
                val sampleText = StylishFontEngine.transformText("Sample Text", style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) theme.accentColor.copy(alpha = 0.15f) else theme.candidateBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) theme.accentColor else theme.gridBorderColor
                        )
                        .clickable {
                            triggerFeedback()
                            onStyleSelect(style)
                        }
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = style.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) theme.accentColor else theme.keyTextColor
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accentColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sampleText,
                            fontSize = 12.sp,
                            color = theme.functionTextColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
        ) {
            FlatKeyButton(
                text = "Back to Keyboard",
                fontSize = 13.sp,
                backgroundColor = theme.accentColor,
                textColor = theme.accentTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    triggerFeedback()
                    onClose()
                }
            )
        }
    }
}

@Composable
fun SymbolsSubPanel(
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    listener: KeyboardActionListener?,
    prefs: SingBordPreferences,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SymbolCategory.STARS) }
    var recentSymbols by remember { mutableStateOf(prefs.getRecentSymbols()) }
    val isFlatGrid = theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        // Top Header: Title & Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(theme.functionKeyBg)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = theme.accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Stylish Symbols Studio",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.functionTextColor,
                modifier = Modifier.weight(1f)
            )
            val closeBtnShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 8.dp)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(closeBtnShape)
                    .background(if (isFlatGrid) Color.Transparent else theme.keyBg.copy(alpha = 0.5f))
                    .clickable {
                        triggerFeedback()
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = theme.functionTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Horizontal Category Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(theme.functionKeyBg)
                .then(if (!isFlatGrid) Modifier.padding(horizontal = 4.dp, vertical = 2.dp) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (!isFlatGrid) Arrangement.spacedBy(4.dp) else Arrangement.Start
        ) {
            items(SymbolCategory.values()) { category ->
                val isSelected = selectedCategory == category
                val chipShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 10.dp)
                Box(
                    modifier = Modifier
                        .fillMaxHeight(if (isFlatGrid) 1f else 0.88f)
                        .clip(chipShape)
                        .background(
                            if (isSelected) theme.accentColor.copy(alpha = if (theme.isDark) 0.32f else 0.22f)
                            else if (isFlatGrid) Color.Transparent
                            else theme.keyBg.copy(alpha = 0.55f)
                        )
                        .then(
                            if (isSelected && !isFlatGrid) Modifier.border(1.dp, theme.accentColor.copy(alpha = 0.75f), chipShape)
                            else if (!isFlatGrid && theme.keyBorderWidthDp > 0f) Modifier.border(0.6.dp, theme.keyBorderColor.copy(alpha = 0.35f), chipShape)
                            else Modifier
                        )
                        .clickable {
                            triggerFeedback()
                            selectedCategory = category
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.icon,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) theme.accentColor else theme.functionTextColor
                        )
                    }
                }
                if (isFlatGrid) {
                    Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                }
            }
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Symbols Grid Area
        val currentSymbols = remember(selectedCategory, recentSymbols) {
            SymbolData.getSymbols(selectedCategory, recentSymbols)
        }

        val isWideItem = selectedCategory == SymbolCategory.KAOMOJI ||
                selectedCategory == SymbolCategory.COMBOS ||
                selectedCategory == SymbolCategory.LINES

        LazyVerticalGrid(
            columns = if (isWideItem) GridCells.Adaptive(minSize = 78.dp) else GridCells.Adaptive(minSize = 38.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (isFlatGrid) theme.candidateBg else theme.keyboardBg),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(if (isFlatGrid) 3.dp else 4.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isFlatGrid) 3.dp else 4.dp)
        ) {
            items(currentSymbols) { sym ->
                val symRadius = if (isFlatGrid) 4.dp else (theme.keyCornerRadiusDp * 0.75f).coerceAtLeast(6f).dp
                val symShape = RoundedCornerShape(symRadius)
                val isGlass = theme.keyShapeStyle == KeyboardKeyShapeStyle.GLASSMORPHIC_3D
                val keyBrush = if (isGlass && theme.keyGradientTop != null && theme.keyGradientBottom != null) {
                    Brush.verticalGradient(listOf(theme.keyGradientTop, theme.keyGradientBottom))
                } else null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    if (!isFlatGrid && theme.keyElevationDp > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = (theme.keyElevationDp * 0.7f).dp)
                                .clip(symShape)
                                .background(theme.keyShadowColor)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(symShape)
                            .then(if (keyBrush != null) Modifier.background(keyBrush) else Modifier.background(theme.keyBg))
                            .then(
                                if (!isFlatGrid && theme.keyBorderWidthDp > 0f) {
                                    Modifier.border(
                                        width = theme.keyBorderWidthDp.dp,
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                theme.keyBorderColor,
                                                theme.keyBorderColor.copy(alpha = 0.2f)
                                            )
                                        ),
                                        shape = symShape
                                    )
                                } else if (isFlatGrid) {
                                    Modifier.border(0.5.dp, theme.gridBorderColor.copy(alpha = 0.5f), symShape)
                                } else Modifier
                            )
                            .clickable {
                                triggerFeedback()
                                listener?.onTextEntered(sym)
                                prefs.addRecentSymbol(sym)
                                recentSymbols = prefs.getRecentSymbols()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sym,
                            fontSize = if (isWideItem) 12.sp else 16.sp,
                            color = theme.keyTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Bottom Action Row (ABC, Space, Backspace)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            FlatKeyButton(
                text = "ABC",
                fontSize = 13.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.Bold,
                isFunctionKey = true,
                badgeColor = theme.modeBadgeColor,
                modifier = Modifier.weight(1.3f),
                onClick = {
                    triggerFeedback()
                    onClose()
                }
            )

            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }

            SpacebarKey(
                backgroundColor = theme.keyBg,
                modifier = Modifier.weight(3.5f),
                onSpace = { listener?.onSpace() },
                onDoubleSpacePeriod = { listener?.onDoubleSpacePeriod() },
                onMoveCursor = { dir -> listener?.onMoveCursor(dir) },
                triggerFeedback = { triggerFeedback() }
            )

            if (isFlatGrid) {
                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            }

            RepeatingBackspaceKey(
                backgroundColor = theme.functionKeyBg,
                iconColor = theme.functionTextColor,
                badgeColor = theme.backspaceBadgeColor,
                modifier = Modifier.weight(1.3f),
                onDelete = {
                    triggerFeedback()
                    listener?.onDelete()
                }
            )
        }
    }
}
