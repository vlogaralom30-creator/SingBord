package com.example

import android.content.ClipboardManager
import android.content.Context
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.inputmethod.EditorInfo
import com.example.data.UserDictionaryRepository
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    TOOLS
}

enum class KeyboardSubPanel {
    NONE,
    TEXT_EDITOR,
    CLIPBOARD,
    NUMBER_DIALER,
    THEME_PICKER,
    FONT_PICKER
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
    fun onVoiceInput() {}
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
    var toolbarMode by remember { mutableStateOf(ToolbarMode.SUGGESTIONS) }
    var activeSubPanel by remember { mutableStateOf(KeyboardSubPanel.NONE) }
    var activePopupKey by remember { mutableStateOf<String?>(null) }
    var currentComposingWord by remember { mutableStateOf("") }
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

    fun handleKeyPress(text: String) {
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

        val rawCharToCommit = when (shiftState) {
            ShiftState.ON, ShiftState.CAPS_LOCK -> text.uppercase()
            ShiftState.OFF -> text.lowercase()
        }

        val charToCommit = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
            StylishFontEngine.transformText(rawCharToCommit, activeFontStyle)
        } else {
            rawCharToCommit
        }
        listener?.onTextEntered(charToCommit)

        if (rawCharToCommit.all { it.isLetter() || it == '\'' }) {
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

    val theme = remember(activeThemeId) {
        KeyboardThemes.getTheme(activeThemeId)
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.keyboardBg) // Theme background for grid separators
            .border(width = lineThicknessDp, color = gridBorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ridmik-Style Dual-Mode Top Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(theme.suggestionStripBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: T Toggle Icon Button (Circular / Round style)
                val isToolsActive = toolbarMode == ToolbarMode.TOOLS || activeSubPanel != KeyboardSubPanel.NONE
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isToolsActive) theme.accentColor else theme.functionKeyBg)
                        .clickable {
                            triggerFeedback()
                            if (activeSubPanel != KeyboardSubPanel.NONE) {
                                activeSubPanel = KeyboardSubPanel.NONE
                                toolbarMode = ToolbarMode.SUGGESTIONS
                            } else {
                                toolbarMode = if (toolbarMode == ToolbarMode.SUGGESTIONS) ToolbarMode.TOOLS else ToolbarMode.SUGGESTIONS
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isToolsActive) theme.accentTextColor else theme.accentColor
                    )
                }

                Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))

                if (toolbarMode == ToolbarMode.TOOLS) {
                    // Tools Panel Strip (Icon-only, no name text): Emoji, Theme, Clipboard, Edit Pad, Number Dialer, Stylish Fonts, Settings, Voice
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(theme.suggestionStripBg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ToolIconItem(
                            icon = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Emoji",
                            theme = theme,
                            isActive = keyboardMode == KeyboardMode.EMOJI,
                            onClick = {
                                triggerFeedback()
                                activeSubPanel = KeyboardSubPanel.NONE
                                keyboardMode = KeyboardMode.EMOJI
                                toolbarMode = ToolbarMode.SUGGESTIONS
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.Palette,
                            contentDescription = "Themes",
                            theme = theme,
                            isActive = activeSubPanel == KeyboardSubPanel.THEME_PICKER,
                            onClick = {
                                triggerFeedback()
                                keyboardMode = KeyboardMode.ALPHA
                                activeSubPanel = if (activeSubPanel == KeyboardSubPanel.THEME_PICKER) KeyboardSubPanel.NONE else KeyboardSubPanel.THEME_PICKER
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.ContentPaste,
                            contentDescription = "Clipboard",
                            theme = theme,
                            isActive = activeSubPanel == KeyboardSubPanel.CLIPBOARD,
                            onClick = {
                                triggerFeedback()
                                keyboardMode = KeyboardMode.ALPHA
                                activeSubPanel = if (activeSubPanel == KeyboardSubPanel.CLIPBOARD) KeyboardSubPanel.NONE else KeyboardSubPanel.CLIPBOARD
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.EditNote,
                            contentDescription = "Text Edit Pad",
                            theme = theme,
                            isActive = activeSubPanel == KeyboardSubPanel.TEXT_EDITOR,
                            onClick = {
                                triggerFeedback()
                                keyboardMode = KeyboardMode.ALPHA
                                activeSubPanel = if (activeSubPanel == KeyboardSubPanel.TEXT_EDITOR) KeyboardSubPanel.NONE else KeyboardSubPanel.TEXT_EDITOR
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.Dialpad,
                            contentDescription = "Number Dialer",
                            theme = theme,
                            isActive = activeSubPanel == KeyboardSubPanel.NUMBER_DIALER,
                            onClick = {
                                triggerFeedback()
                                keyboardMode = KeyboardMode.ALPHA
                                activeSubPanel = if (activeSubPanel == KeyboardSubPanel.NUMBER_DIALER) KeyboardSubPanel.NONE else KeyboardSubPanel.NUMBER_DIALER
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.TextFields,
                            contentDescription = "Stylish Fonts",
                            theme = theme,
                            isActive = activeSubPanel == KeyboardSubPanel.FONT_PICKER,
                            onClick = {
                                triggerFeedback()
                                keyboardMode = KeyboardMode.ALPHA
                                activeSubPanel = if (activeSubPanel == KeyboardSubPanel.FONT_PICKER) KeyboardSubPanel.NONE else KeyboardSubPanel.FONT_PICKER
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            theme = theme,
                            isActive = false,
                            onClick = {
                                triggerFeedback()
                                listener?.onOpenSettings()
                            }
                        )

                        ToolIconItem(
                            icon = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            theme = theme,
                            isActive = false,
                            onClick = {
                                triggerFeedback()
                                listener?.onVoiceInput()
                            }
                        )
                    }
                } else {
                    // Suggestion Mode Strip
                    val suggestions = remember(currentComposingWord, settings.enableBanglish, settings.enableWordLearning) {
                        if (settings.showSuggestions) {
                            WordSuggestionEngine.getSuggestions(
                                prefix = currentComposingWord,
                                userRepo = if (settings.enableWordLearning) userRepo else null,
                                enableBanglish = settings.enableBanglish,
                                maxCount = 4
                            )
                        } else {
                            emptyList()
                        }
                    }

                    val hasSuggestions = suggestions.isNotEmpty() && currentComposingWord.isNotBlank()

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (suggestions.isEmpty() && currentComposingWord.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(theme.candidateBg)
                                    .clickable {
                                        triggerFeedback()
                                        toolbarMode = ToolbarMode.TOOLS
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (keyboardMode == KeyboardMode.EMOJI) "Emoji • Tap T for Tools" else "SingBord • Tap T for Tools",
                                    fontSize = 12.sp,
                                    color = theme.functionTextColor.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            suggestions.forEachIndexed { index, candidate ->
                                if (index > 0) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(lineThicknessDp)
                                            .fillMaxHeight()
                                            .background(gridBorderColor)
                                    )
                                }
                                val isHighlighted = currentComposingWord.isNotBlank() && (index == 0 || (suggestions.size > 1 && index == 1))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isHighlighted) theme.accentColor.copy(alpha = 0.18f) else theme.candidateBg)
                                        .clickable {
                                            triggerFeedback()
                                            maybeLearnWord(candidate)
                                            val wordToCommit = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                                                StylishFontEngine.transformText(candidate, activeFontStyle)
                                            } else {
                                                candidate
                                            }
                                            listener?.onWordSelected(wordToCommit, currentComposingWord.length)
                                            currentComposingWord = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val displayCandidate = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                                        StylishFontEngine.transformText(candidate, activeFontStyle)
                                    } else {
                                        candidate
                                    }
                                    Text(
                                        text = displayCandidate,
                                        fontSize = 14.sp,
                                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isHighlighted) theme.accentColor else textColor,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Voice / Mic icon on the right side: ONLY shown when there are NO word suggestions active
                    if (!hasSuggestions) {
                        Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(theme.functionKeyBg)
                                .clickable {
                                    triggerFeedback()
                                    listener?.onVoiceInput()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = theme.keyTextColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lineThicknessDp)
                    .background(gridBorderColor)
            )

            if (activeSubPanel != KeyboardSubPanel.NONE) {
                when (activeSubPanel) {
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
                            context = context,
                            prefs = prefs,
                            theme = theme,
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
                            theme = theme,
                            lineThicknessDp = lineThicknessDp,
                            gridBorderColor = gridBorderColor,
                            totalHeight = totalSubPanelHeight,
                            onThemeSelect = { newTheme ->
                                activeThemeId = newTheme.id
                                prefs.themeId = newTheme.id
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
                    KeyboardSubPanel.NONE -> {}
                }
            } else if (keyboardMode == KeyboardMode.EMOJI) {
                // Emoji Category Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(theme.functionKeyBg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmojiCategory.values().forEachIndexed { index, category ->
                        if (index > 0) {
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }
                        val isSelected = selectedEmojiCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isSelected) theme.accentColor.copy(alpha = 0.25f) else theme.candidateBg)
                                .clickable {
                                    triggerFeedback()
                                    selectedEmojiCategory = category
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.icon,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )

                // Emoji Grid Area (Calculated so total emoji screen height exactly equals totalContentHeight)
                val gridHeight = (totalContentHeight - 38.dp - keyHeight - (lineThicknessDp * 2)).coerceAtLeast(140.dp)

                val currentEmojis = remember(selectedEmojiCategory, recentEmojis) {
                    EmojiData.getEmojis(selectedEmojiCategory, recentEmojis)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                        .background(theme.keyBg),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { handleEmojiPress(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )

                // Bottom Row in Emoji Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    FlatKeyButton(
                        text = "ABC",
                        backgroundColor = functionKeyBg,
                        textColor = functionTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f),
                        onClick = {
                            triggerFeedback()
                            keyboardMode = KeyboardMode.ALPHA
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )

                    SpacebarKey(
                        backgroundColor = letterKeyBg,
                        modifier = Modifier.weight(3.5f),
                        onSpace = { listener?.onSpace() },
                        onDoubleSpacePeriod = { listener?.onDoubleSpacePeriod() },
                        onMoveCursor = { dir -> listener?.onMoveCursor(dir) },
                        triggerFeedback = { triggerFeedback() }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )

                    RepeatingBackspaceKey(
                        backgroundColor = functionKeyBg,
                        iconColor = functionTextColor,
                        modifier = Modifier.weight(1.5f),
                        onDelete = {
                            triggerFeedback()
                            listener?.onDelete()
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )

                    EnterKeyButton(
                        editorInfo = editorInfo,
                        modifier = Modifier.weight(1.5f),
                        onClick = {
                            triggerFeedback()
                            listener?.onEnter()
                        }
                    )
                }
            } else {
                // Top Dedicated Number Row (if enabled across all keyboard modes)
                if (settings.showNumberRow) {
                    val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight * 0.85f)
                    ) {
                        numberRow.forEachIndexed { index, num ->
                            if (index > 0) {
                                Spacer(
                                    modifier = Modifier
                                        .width(lineThicknessDp)
                                        .fillMaxHeight()
                                        .background(gridBorderColor)
                                )
                            }
                            val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                                StylishFontEngine.transformText(num, activeFontStyle)
                            } else {
                                num
                            }
                            FlatKeyButton(
                                text = displayText,
                                backgroundColor = functionKeyBg,
                                textColor = functionTextColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(num) }
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )
                }

                if (keyboardMode == KeyboardMode.ALPHA) {
                    // Row 1
                    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight)
                    ) {
                        row1.forEachIndexed { index, letter ->
                            if (index > 0) {
                                Spacer(
                                    modifier = Modifier
                                        .width(lineThicknessDp)
                                        .fillMaxHeight()
                                        .background(gridBorderColor)
                                )
                            }
                            val rawText = if (shiftState != ShiftState.OFF) letter.uppercase() else letter
                            val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                                StylishFontEngine.transformText(rawText, activeFontStyle)
                            } else {
                                rawText
                            }
                            FlatKeyButton(
                                text = displayText,
                                backgroundColor = letterKeyBg,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(letter) }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )

                    // Row 2
                    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight)
                    ) {
                        row2.forEachIndexed { index, letter ->
                            if (index > 0) {
                                Spacer(
                                    modifier = Modifier
                                        .width(lineThicknessDp)
                                        .fillMaxHeight()
                                        .background(gridBorderColor)
                                )
                            }
                            val rawText = if (shiftState != ShiftState.OFF) letter.uppercase() else letter
                            val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                                StylishFontEngine.transformText(rawText, activeFontStyle)
                            } else {
                                rawText
                            }
                            FlatKeyButton(
                                text = displayText,
                                backgroundColor = letterKeyBg,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(letter) }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )

                    // Row 3
                    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight)
                    ) {
                        // Shift Key
                        val shiftBg = when (shiftState) {
                            ShiftState.OFF -> functionKeyBg
                            ShiftState.ON -> theme.accentColor
                            ShiftState.CAPS_LOCK -> theme.accentColor
                        }
                        val shiftIconColor = if (shiftState == ShiftState.OFF) functionTextColor else theme.accentTextColor

                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                                .background(shiftBg)
                                .clickable {
                                    triggerFeedback()
                                    shiftState = when (shiftState) {
                                        ShiftState.OFF -> ShiftState.ON
                                        ShiftState.ON -> ShiftState.CAPS_LOCK
                                        ShiftState.CAPS_LOCK -> ShiftState.OFF
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Shift",
                                tint = shiftIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )

                        row3.forEach { letter ->
                            val rawText = if (shiftState != ShiftState.OFF) letter.uppercase() else letter
                            val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                                StylishFontEngine.transformText(rawText, activeFontStyle)
                            } else {
                                rawText
                            }
                            FlatKeyButton(
                                text = displayText,
                                backgroundColor = letterKeyBg,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(letter) }
                            )
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }

                        // Backspace Key
                        RepeatingBackspaceKey(
                            backgroundColor = functionKeyBg,
                            iconColor = functionTextColor,
                            modifier = Modifier.weight(1.5f),
                            onDelete = {
                                triggerFeedback()
                                if (currentComposingWord.isNotEmpty()) {
                                    currentComposingWord = currentComposingWord.dropLast(1)
                                }
                                listener?.onDelete()
                            }
                        )
                    }

                } else {
                    // Symbol Modes: 3 Rows + Backspace
                    val row1Symbols = when {
                        keyboardMode == KeyboardMode.NUMERIC_SYMBOLS && settings.showNumberRow ->
                            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
                        keyboardMode == KeyboardMode.NUMERIC_SYMBOLS ->
                            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                        else ->
                            listOf("~", "`", "|", "^", "<", ">", "{", "}", "[", "]")
                    }

                    val row2Symbols = when {
                        keyboardMode == KeyboardMode.NUMERIC_SYMBOLS && settings.showNumberRow ->
                            listOf("*", "\"", "'", ":", ";", "!", "?", "\\", "_", "=")
                        keyboardMode == KeyboardMode.NUMERIC_SYMBOLS ->
                            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
                        else ->
                            listOf("£", "€", "¥", "¢", "°", "©", "®", "™", "✓", "•")
                    }

                    val (row3LeadingKey, row3MiddleSymbols) = when {
                        keyboardMode == KeyboardMode.NUMERIC_SYMBOLS && settings.showNumberRow ->
                            Pair("~", listOf("<", ">", "{", "}", "[", "]", "^"))
                        keyboardMode == KeyboardMode.NUMERIC_SYMBOLS ->
                            Pair("*", listOf("\"", "'", ":", ";", "!", "?", "_"))
                        else ->
                            Pair("…", listOf("§", "¶", "∆", "π", "÷", "×", "≠"))
                    }

                    // Symbol Row 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight)
                    ) {
                        row1Symbols.forEachIndexed { index, sym ->
                            if (index > 0) {
                                Spacer(
                                    modifier = Modifier
                                        .width(lineThicknessDp)
                                        .fillMaxHeight()
                                        .background(gridBorderColor)
                                )
                            }
                            FlatKeyButton(
                                text = sym,
                                backgroundColor = letterKeyBg,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(sym) }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )

                    // Symbol Row 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight)
                    ) {
                        row2Symbols.forEachIndexed { index, sym ->
                            if (index > 0) {
                                Spacer(
                                    modifier = Modifier
                                        .width(lineThicknessDp)
                                        .fillMaxHeight()
                                        .background(gridBorderColor)
                                )
                            }
                            FlatKeyButton(
                                text = sym,
                                backgroundColor = letterKeyBg,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(sym) }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )

                    // Symbol Row 3 (Leading symbol key [1.5f] + 7 symbols [1f each] + Backspace key [1.5f])
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyHeight)
                    ) {
                        FlatKeyButton(
                            text = row3LeadingKey,
                            backgroundColor = functionKeyBg,
                            textColor = functionTextColor,
                            modifier = Modifier.weight(1.5f),
                            onClick = { handleKeyPress(row3LeadingKey) }
                        )

                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )

                        row3MiddleSymbols.forEach { sym ->
                            FlatKeyButton(
                                text = sym,
                                backgroundColor = letterKeyBg,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                onClick = { handleKeyPress(sym) }
                            )
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }

                        RepeatingBackspaceKey(
                            backgroundColor = functionKeyBg,
                            iconColor = functionTextColor,
                            modifier = Modifier.weight(1.5f),
                            onDelete = {
                                triggerFeedback()
                                if (currentComposingWord.isNotEmpty()) {
                                    currentComposingWord = currentComposingWord.dropLast(1)
                                }
                                listener?.onDelete()
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )

                // Row 4: Bottom Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    // Mode Toggle Key (?123 / ABC / =<)
                    val modeLabel = when (keyboardMode) {
                        KeyboardMode.ALPHA -> "?123"
                        KeyboardMode.NUMERIC_SYMBOLS -> "ABC"
                        KeyboardMode.ALT_SYMBOLS -> "ABC"
                        KeyboardMode.EMOJI -> "ABC"
                    }

                    FlatKeyButton(
                        text = modeLabel,
                        backgroundColor = functionKeyBg,
                        textColor = functionTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.3f),
                        onClick = {
                            triggerFeedback()
                            keyboardMode = if (keyboardMode == KeyboardMode.ALPHA) {
                                KeyboardMode.NUMERIC_SYMBOLS
                            } else {
                                KeyboardMode.ALPHA
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )

                    // Secondary symbol switch key when in symbol mode
                    if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS || keyboardMode == KeyboardMode.ALT_SYMBOLS) {
                        val altLabel = if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS) "=<" else "123"
                        FlatKeyButton(
                            text = altLabel,
                            backgroundColor = functionKeyBg,
                            textColor = functionTextColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                triggerFeedback()
                                keyboardMode = if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS) {
                                    KeyboardMode.ALT_SYMBOLS
                                } else {
                                    KeyboardMode.NUMERIC_SYMBOLS
                                }
                            }
                        )
                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )
                    }

                    // Emoji Button (if enabled in settings)
                    if (settings.enableEmoji) {
                        IconKeyButton(
                            icon = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Emoji",
                            backgroundColor = functionKeyBg,
                            iconColor = functionTextColor,
                            modifier = Modifier.weight(1.0f),
                            onClick = {
                                triggerFeedback()
                                if (recentEmojis.isNotEmpty()) {
                                    selectedEmojiCategory = EmojiCategory.RECENT
                                } else {
                                    selectedEmojiCategory = EmojiCategory.SMILEYS
                                }
                                keyboardMode = KeyboardMode.EMOJI
                            }
                        )

                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )
                    }

                    // Comma (in Alpha mode)
                    if (keyboardMode == KeyboardMode.ALPHA) {
                        FlatKeyButton(
                            text = ",",
                            backgroundColor = functionKeyBg,
                            textColor = functionTextColor,
                            modifier = Modifier.weight(0.9f),
                            onClick = { handleKeyPress(",") }
                        )

                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )
                    }

                    // Spacebar
                    SpacebarKey(
                        backgroundColor = letterKeyBg,
                        pressedColor = keyPressedBg,
                        dragColor = accentColor.copy(alpha = 0.35f),
                        enableGestures = settings.enableGestures,
                        modifier = Modifier.weight(if (settings.enableEmoji) 3.5f else 4.5f),
                        onSpace = {
                            if (currentComposingWord.isNotBlank()) {
                                maybeLearnWord(currentComposingWord)
                            }
                            currentComposingWord = ""
                            listener?.onSpace()
                        },
                        onDoubleSpacePeriod = {
                            if (currentComposingWord.isNotBlank()) {
                                maybeLearnWord(currentComposingWord)
                            }
                            currentComposingWord = ""
                            listener?.onDoubleSpacePeriod()
                        },
                        onMoveCursor = { dir ->
                            currentComposingWord = ""
                            listener?.onMoveCursor(dir)
                        },
                        triggerFeedback = { triggerFeedback() }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )

                    // Period
                    FlatKeyButton(
                        text = ".",
                        backgroundColor = functionKeyBg,
                        textColor = functionTextColor,
                        modifier = Modifier.weight(0.9f),
                        onClick = {
                            if (currentComposingWord.isNotBlank()) {
                                maybeLearnWord(currentComposingWord)
                            }
                            currentComposingWord = ""
                            handleKeyPress(".")
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )

                    // Enter Key
                    EnterKeyButton(
                        editorInfo = editorInfo,
                        accentColor = accentColor,
                        accentTextColor = accentTextColor,
                        modifier = Modifier.weight(1.4f),
                        onClick = {
                            triggerFeedback()
                            if (currentComposingWord.isNotBlank()) {
                                maybeLearnWord(currentComposingWord)
                            }
                            currentComposingWord = ""
                            listener?.onEnter()
                        }
                    )
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
    fontWeight: FontWeight = FontWeight.SemiBold,
    fontSize: TextUnit = 18.sp,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val activeBg = if (isPressed) Color(0xFFCBD5E1) else backgroundColor

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(activeBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun IconKeyButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val activeBg = if (isPressed) Color(0xFFCBD5E1) else backgroundColor

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(activeBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EnterKeyButton(
    editorInfo: EditorInfo?,
    accentColor: Color = Color(0xFF2563EB),
    accentTextColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
    val activeBg = if (isPressed) accentColor.copy(alpha = 0.8f) else accentColor

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(activeBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = enterIcon,
            contentDescription = enterDescription,
            tint = accentTextColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun RepeatingBackspaceKey(
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val activeBg = if (isPressed) Color(0xFFCBD5E1) else backgroundColor

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(activeBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
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
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Delete",
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SpacebarKey(
    backgroundColor: Color,
    pressedColor: Color = Color(0xFFE2E8F0),
    dragColor: Color = Color(0xFFCBD5E1),
    enableGestures: Boolean = true,
    modifier: Modifier = Modifier,
    onSpace: () -> Unit,
    onDoubleSpacePeriod: () -> Unit,
    onMoveCursor: (Int) -> Unit,
    triggerFeedback: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableStateOf(0L) }
    val density = LocalDensity.current
    val stepThresholdPx = with(density) { 16.dp.toPx() }

    val currentBg = when {
        isDragging -> dragColor
        isPressed -> pressedColor
        else -> backgroundColor
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(currentBg)
            .pointerInput(enableGestures) {
                if (!enableGestures) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
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
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (enableGestures && isDragging) "‹ ── Slide cursor ── ›" else "SingBord",
            fontSize = if (isDragging) 11.sp else 12.sp,
            color = if (isDragging) Color(0xFF1E293B) else Color(0xFF94A3B8),
            fontWeight = if (isDragging) FontWeight.SemiBold else FontWeight.Normal
        )
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
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isActive) theme.accentColor else theme.candidateBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) theme.accentTextColor else theme.keyTextColor,
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
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onSelectAll()
                }
            )
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            FlatKeyButton(
                text = "Cut",
                fontSize = 12.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerFeedback()
                    listener?.onCut()
                }
            )
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            FlatKeyButton(
                text = "Copy",
                fontSize = 12.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerFeedback()
                    listener?.onCopy()
                }
            )
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            FlatKeyButton(
                text = "Paste",
                fontSize = 12.sp,
                backgroundColor = theme.accentColor,
                textColor = theme.accentTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.1f),
                onClick = {
                    triggerFeedback()
                    listener?.onPaste()
                }
            )
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

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
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onMoveToStart()
                }
            )
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
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
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            FlatKeyButton(
                text = "End ⇥",
                fontSize = 13.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    listener?.onMoveToEnd()
                }
            )
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

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
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            FlatKeyButton(
                text = if (selectMode) "Select: ON" else "Select: OFF",
                fontSize = 13.sp,
                backgroundColor = if (selectMode) theme.accentColor else theme.functionKeyBg,
                textColor = if (selectMode) theme.accentTextColor else theme.functionTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.6f),
                onClick = {
                    triggerFeedback()
                    selectMode = !selectMode
                }
            )
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
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

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        // D-Pad Row 3: Return to Keyboard, DOWN Arrow, Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
        ) {
            FlatKeyButton(
                text = "ABC",
                fontSize = 13.sp,
                backgroundColor = theme.accentColor,
                textColor = theme.accentTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerFeedback()
                    onClose()
                }
            )
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
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
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
            FlatKeyButton(
                text = "⌫",
                fontSize = 16.sp,
                backgroundColor = theme.functionKeyBg,
                textColor = theme.functionTextColor,
                fontWeight = FontWeight.Bold,
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
fun ClipboardSubPanel(
    context: Context,
    prefs: SingBordPreferences,
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    listener: KeyboardActionListener?,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    var history by remember {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val primary = clipboard?.primaryClip?.let { clip ->
            if (clip.itemCount > 0) clip.getItemAt(0)?.text?.toString() else null
        }
        if (!primary.isNullOrBlank()) {
            prefs.addClipboardItem(primary)
        }
        mutableStateOf(prefs.getClipboardHistory())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        // Top Toolbar inside Clipboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(theme.functionKeyBg)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = null,
                tint = theme.functionTextColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Clipboard History (${history.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.functionTextColor,
                modifier = Modifier.weight(1f)
            )

            if (history.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable {
                            triggerFeedback()
                            prefs.clearClipboardHistory()
                            history = emptyList()
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Clear",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))

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

        // Clips List
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(theme.keyBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = theme.keyTextColor.copy(alpha = 0.35f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Clipboard is empty",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.keyTextColor
                    )
                    Text(
                        text = "Copy text anywhere to paste it in 1 tap here",
                        fontSize = 11.sp,
                        color = theme.functionTextColor.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(theme.keyBg),
                contentPadding = PaddingValues(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(history) { clipText ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.candidateBg)
                            .border(1.dp, theme.gridBorderColor.copy(alpha = 0.5f))
                            .clickable {
                                triggerFeedback()
                                listener?.onTextEntered(clipText)
                                prefs.addClipboardItem(clipText)
                                history = prefs.getClipboardHistory()
                            }
                            .padding(10.dp)
                    ) {
                        Text(
                            text = clipText,
                            fontSize = 13.sp,
                            color = theme.keyTextColor,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))

        // Bottom Return Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
        ) {
            FlatKeyButton(
                text = "Return to Keyboard",
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        dialerRows.forEachIndexed { rowIndex, rowKeys ->
            if (rowIndex > 0) {
                Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                rowKeys.forEachIndexed { colIndex, key ->
                    if (colIndex > 0) {
                        Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                    }
                    val isAction = key in listOf("⌫", "⏎", "ABC", "␣", "+", "-", "*", "#", "/")
                    val isReturn = key == "ABC"
                    val isEnter = key == "⏎"
                    val isBackspace = key == "⌫"
                    val isSpace = key == "␣"

                    val bg = when {
                        isReturn -> theme.accentColor
                        isEnter -> theme.accentColor
                        isAction -> theme.functionKeyBg
                        else -> theme.keyBg
                    }
                    val txtColor = when {
                        isReturn || isEnter -> theme.accentTextColor
                        isAction -> theme.functionTextColor
                        else -> theme.keyTextColor
                    }

                    FlatKeyButton(
                        text = key,
                        fontSize = if (key.length > 2) 13.sp else 18.sp,
                        fontWeight = if (isAction) FontWeight.Bold else FontWeight.Medium,
                        backgroundColor = bg,
                        textColor = txtColor,
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
    theme: KeyboardThemePalette,
    lineThicknessDp: androidx.compose.ui.unit.Dp,
    gridBorderColor: Color,
    totalHeight: androidx.compose.ui.unit.Dp,
    onThemeSelect: (KeyboardThemePalette) -> Unit,
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
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = theme.functionTextColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Choose Keyboard Theme",
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
            items(KeyboardThemes.ALL_THEMES) { itemTheme ->
                val isSelected = itemTheme.id.equals(currentThemeId, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(itemTheme.keyboardBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) itemTheme.accentColor else itemTheme.gridBorderColor
                        )
                        .clickable {
                            triggerFeedback()
                            onThemeSelect(itemTheme)
                        }
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(itemTheme.keyBg)
                                .border(1.dp, itemTheme.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = itemTheme.keyTextColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = itemTheme.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = itemTheme.keyTextColor,
                                maxLines = 1
                            )
                            Text(
                                text = if (isSelected) "Active" else itemTheme.subtitle,
                                fontSize = 10.sp,
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
