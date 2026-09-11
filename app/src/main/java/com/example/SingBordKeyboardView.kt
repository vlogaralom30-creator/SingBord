package com.example

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
    ALT_SYMBOLS
}

interface KeyboardActionListener {
    fun onTextEntered(text: String)
    fun onDelete()
    fun onEnter()
    fun onSpace()
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

    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    fun triggerFeedback() {
        if (settings.enableHaptics) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (settings.enableSound) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
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

        if (shiftState == ShiftState.ON) {
            shiftState = ShiftState.OFF
        }
    }

    val keyHeight = (46 * settings.keyboardSize.heightFactor).dp
    val lineThicknessDp = settings.lineThicknessDp.dp
    val gridBorderColor = Color(0xFFD1D5DB) // Crisp flat gray line
    val letterKeyBg = Color(0xFFFFFFFF)    // Pure 2D flat white
    val functionKeyBg = Color(0xFFF1F5F9)  // Flat light slate for modifier keys
    val keyPressedBg = Color(0xFFE2E8F0)   // Tap visual feedback
    val textColor = Color(0xFF0F172A)       // Crisp black text
    val functionTextColor = Color(0xFF334155)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE2E8F0)) // Background for grid separators
            .border(width = lineThicknessDp, color = gridBorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fixed-Height Top Character Preview Bar (eliminates layout height jumping during typing)
            if (settings.enableKeyPopup) {
                val isKeyPressed = activePopupKey != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(if (isKeyPressed) Color(0xFF2563EB) else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isKeyPressed) (activePopupKey?.uppercase() ?: "") else "SingBord",
                        color = if (isKeyPressed) Color.White else Color(0xFF94A3B8),
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
                    // Indent slightly for classic QWERTY look or stretch keys
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
                        ShiftState.ON -> Color(0xFF3B82F6) // Active blue
                        ShiftState.CAPS_LOCK -> Color(0xFF1D4ED8) // Deep blue
                    }
                    val shiftIconColor = if (shiftState == ShiftState.OFF) functionTextColor else Color.White

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
                }

                FlatKeyButton(
                    text = modeLabel,
                    backgroundColor = functionKeyBg,
                    textColor = functionTextColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
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
                if (keyboardMode != KeyboardMode.ALPHA) {
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

                // Comma
                FlatKeyButton(
                    text = ",",
                    backgroundColor = functionKeyBg,
                    textColor = functionTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { handleKeyPress(",") }
                )

                Spacer(
                    modifier = Modifier
                        .width(lineThicknessDp)
                        .fillMaxHeight()
                        .background(gridBorderColor)
                )

                // Spacebar
                Box(
                    modifier = Modifier
                        .weight(3.5f)
                        .fillMaxHeight()
                        .background(letterKeyBg)
                        .clickable {
                            triggerFeedback()
                            listener?.onSpace()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SingBord",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Normal
                    )
                }

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
                    modifier = Modifier.weight(1f),
                    onClick = { handleKeyPress(".") }
                )

                Spacer(
                    modifier = Modifier
                        .width(lineThicknessDp)
                        .fillMaxHeight()
                        .background(gridBorderColor)
                )

                // Enter Key
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

                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .background(Color(0xFF2563EB)) // Clean 2D flat blue action key
                        .clickable {
                            triggerFeedback()
                            listener?.onEnter()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = enterIcon,
                        contentDescription = enterDescription,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
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
            fontSize = 18.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
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
