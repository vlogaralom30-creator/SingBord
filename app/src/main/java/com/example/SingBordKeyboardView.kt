package com.example

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SpaceBar
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

interface KeyboardActionListener {
    fun onTextEntered(text: String)
    fun onDelete()
    fun onEnter()
    fun onSpace()
    fun onDoubleSpacePeriod()
    fun onMoveCursor(direction: Int)
    fun onWordSelected(word: String, prefixLength: Int)
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
    var activePopupKey by remember { mutableStateOf<String?>(null) }
    var currentComposingWord by remember { mutableStateOf("") }

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
        if (settings.enableKeyPopup) {
            activePopupKey = text
            coroutineScope.launch {
                delay(120)
                if (activePopupKey == text) {
                    activePopupKey = null
                }
            }
        }

        val charToCommit = when (shiftState) {
            ShiftState.ON, ShiftState.CAPS_LOCK -> text.uppercase()
            ShiftState.OFF -> text.lowercase()
        }
        listener?.onTextEntered(charToCommit)

        if (charToCommit.all { it.isLetter() || it == '\'' }) {
            currentComposingWord += charToCommit
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

    val theme = remember(settings.themeId) {
        KeyboardThemes.getTheme(settings.themeId)
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.keyboardBg) // Theme background for grid separators
            .border(width = lineThicknessDp, color = gridBorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Candidate / Word Suggestion Bar or Preview Header
            if (keyboardMode == KeyboardMode.EMOJI) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(theme.functionKeyBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Emoji • ${selectedEmojiCategory.title}",
                        color = theme.functionTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )
            } else if (settings.showSuggestions) {
                val suggestions = remember(currentComposingWord, settings.enableBanglish, settings.enableWordLearning) {
                    WordSuggestionEngine.getSuggestions(
                        prefix = currentComposingWord,
                        userRepo = if (settings.enableWordLearning) userRepo else null,
                        enableBanglish = settings.enableBanglish,
                        maxCount = 4
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(theme.suggestionStripBg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                    listener?.onWordSelected(candidate, currentComposingWord.length)
                                    currentComposingWord = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = candidate,
                                fontSize = 14.sp,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isHighlighted) theme.accentColor else textColor,
                                maxLines = 1,
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
            } else if (settings.enableKeyPopup) {
                val isKeyPressed = activePopupKey != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(if (isKeyPressed) theme.accentColor else theme.functionKeyBg),
                    contentAlignment = Alignment.Center
                ) {
                    val previewText = if (isKeyPressed) activePopupKey?.uppercase() ?: "" else "SingBord"
                    Text(
                        text = previewText,
                        color = if (isKeyPressed) theme.accentTextColor else theme.functionTextColor.copy(alpha = 0.7f),
                        fontSize = if (isKeyPressed) 16.sp else 11.sp,
                        fontWeight = if (isKeyPressed) FontWeight.Bold else FontWeight.SemiBold,
                        letterSpacing = if (isKeyPressed) 0.sp else 1.2.sp
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )
            }

            if (keyboardMode == KeyboardMode.EMOJI) {
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

                // Emoji Grid Area
                val totalRowsHeight = if (settings.showNumberRow) {
                    (keyHeight * 0.85f) + (keyHeight * 3) + (lineThicknessDp * 3)
                } else {
                    (keyHeight * 3) + (lineThicknessDp * 2)
                }
                val gridHeight = (totalRowsHeight - 38.dp).coerceAtLeast(140.dp)

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
                // Top Dedicated Number Row (if enabled)
                if (settings.showNumberRow && keyboardMode == KeyboardMode.ALPHA) {
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
                            FlatKeyButton(
                                text = num,
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
                            val displayText = if (shiftState != ShiftState.OFF) letter.uppercase() else letter
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
                            val displayText = if (shiftState != ShiftState.OFF) letter.uppercase() else letter
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
                            val displayText = if (shiftState != ShiftState.OFF) letter.uppercase() else letter
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
                    // Symbol Modes
                    val symbolRows = if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS) {
                        listOf(
                            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
                            listOf("*", "\"", "'", ":", ";", "!", "?", "\\", "_", "=")
                        )
                    } else {
                        listOf(
                            listOf("~", "`", "|", "^", "<", ">", "{", "}", "[", "]"),
                            listOf("£", "€", "¥", "¢", "°", "©", "®", "™", "✓", "•"),
                            listOf("…", "§", "¶", "∆", "π", "÷", "×", "≠", "≈", "∞")
                        )
                    }

                    symbolRows.forEachIndexed { rowIndex, rowKeys ->
                        if (rowIndex > 0) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(lineThicknessDp)
                                    .background(gridBorderColor)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(keyHeight)
                        ) {
                            rowKeys.forEachIndexed { index, sym ->
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
                        FlatKeyButton(
                            text = "😊",
                            backgroundColor = functionKeyBg,
                            textColor = functionTextColor,
                            fontSize = 18.sp,
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
