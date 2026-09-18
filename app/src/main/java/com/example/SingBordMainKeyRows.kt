package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SingBordMainKeyRows(
    theme: KeyboardThemePalette,
    settings: KeyboardSettings,
    isFlatGrid: Boolean,
    lineThicknessDp: Dp,
    gridBorderColor: Color,
    letterKeyBg: Color,
    functionKeyBg: Color,
    textColor: Color,
    functionTextColor: Color,
    accentColor: Color,
    accentTextColor: Color,
    keyHeight: Dp,
    keyboardMode: KeyboardMode,
    currentLanguage: KeyboardLanguage,
    shiftState: ShiftState,
    activeFontStyle: StylishFontStyle,
    onToggleShift: () -> Unit,
    onKeyPress: (String, Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Top Dedicated Number Row (if enabled across all keyboard modes)
        if (settings.showNumberRow) {
            val isProbhat = currentLanguage == KeyboardLanguage.BANGLA_PROBHAT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight * 0.85f)
            ) {
                if (isProbhat) {
                    BanglaProbhatLayout.numberRow.forEachIndexed { index, pk ->
                        if (index > 0 && isFlatGrid) {
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }
                        FlatKeyButton(
                            text = pk.normal,
                            subText = pk.hint,
                            alternates = pk.alternates,
                            backgroundColor = functionKeyBg,
                            textColor = functionTextColor,
                            isFunctionKey = true,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f),
                            onAlternateSelected = { alt -> onKeyPress(alt, true) },
                            onClick = { onKeyPress(pk.normal, true) }
                        )
                    }
                } else {
                    val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                    numberRow.forEachIndexed { index, num ->
                        if (index > 0 && isFlatGrid) {
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
                            isFunctionKey = true,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f),
                            onClick = { onKeyPress(num, false) }
                        )
                    }
                }
            }
            if (isFlatGrid) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )
            }
        }

        if (keyboardMode == KeyboardMode.ALPHA) {
            val isProbhat = currentLanguage == KeyboardLanguage.BANGLA_PROBHAT

            if (isProbhat) {
                // Bangla Probhat Layout (Exact match to Reference Screenshots)
                // Row 1 (12 Keys)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    BanglaProbhatLayout.row1.forEachIndexed { index, k ->
                        if (index > 0 && isFlatGrid) {
                            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                        }
                        val keyText = if (shiftState != ShiftState.OFF) k.shifted else k.normal
                        val sub = if (shiftState == ShiftState.OFF) k.hint else null
                        FlatKeyButton(
                            text = keyText,
                            subText = sub,
                            alternates = k.alternates,
                            backgroundColor = letterKeyBg,
                            textColor = textColor,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            onAlternateSelected = { alt -> onKeyPress(alt, true) },
                            onClick = { onKeyPress(keyText, true) }
                        )
                    }
                }

                if (isFlatGrid) {
                    Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
                }

                // Row 2 (9 Keys)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    BanglaProbhatLayout.row2.forEachIndexed { index, k ->
                        if (index > 0 && isFlatGrid) {
                            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                        }
                        val keyText = if (shiftState != ShiftState.OFF) k.shifted else k.normal
                        val sub = if (shiftState == ShiftState.OFF) k.hint else null
                        FlatKeyButton(
                            text = keyText,
                            subText = sub,
                            alternates = k.alternates,
                            backgroundColor = letterKeyBg,
                            textColor = textColor,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            onAlternateSelected = { alt -> onKeyPress(alt, true) },
                            onClick = { onKeyPress(keyText, true) }
                        )
                    }
                }

                if (isFlatGrid) {
                    Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
                }

                // Row 3 (Shift [1.3f] + 7 Keys [1f each] + Backspace [1.5f])
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    // Shift Key
                    IconKeyButton(
                        icon = Icons.Default.ArrowUpward,
                        contentDescription = "Shift",
                        backgroundColor = if (shiftState != ShiftState.OFF) accentColor else functionKeyBg,
                        iconColor = if (shiftState != ShiftState.OFF) accentTextColor else functionTextColor,
                        badgeColor = theme.shiftBadgeColor,
                        isFunctionKey = true,
                        modifier = Modifier.weight(1.3f),
                        onClick = onToggleShift
                    )

                    if (isFlatGrid) {
                        Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                    }

                    // 7 Middle Keys
                    BanglaProbhatLayout.row3.forEach { k ->
                        val keyText = if (shiftState != ShiftState.OFF) k.shifted else k.normal
                        val sub = if (shiftState == ShiftState.OFF) k.hint else null
                        FlatKeyButton(
                            text = keyText,
                            subText = sub,
                            alternates = k.alternates,
                            backgroundColor = letterKeyBg,
                            textColor = textColor,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            onAlternateSelected = { alt -> onKeyPress(alt, true) },
                            onClick = { onKeyPress(keyText, true) }
                        )
                        if (isFlatGrid) {
                            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                        }
                    }

                    // Backspace Key
                    RepeatingBackspaceKey(
                        backgroundColor = functionKeyBg,
                        iconColor = functionTextColor,
                        badgeColor = theme.backspaceBadgeColor,
                        modifier = Modifier.weight(1.5f),
                        onDelete = onDelete
                    )
                }
            } else {
                // English QWERTY / Avro Phonetic Standard Layout
                val row1Keys = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                val row2Keys = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
                val row3Keys = listOf("z", "x", "c", "v", "b", "n", "m")

                val rainbowSpectrum = listOf(
                    Color(0xFF84CC16), // Lime Green
                    Color(0xFF22C55E), // Green
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFF0EA5E9), // Sky Blue
                    Color(0xFF3B82F6), // Blue
                    Color(0xFF6366F1), // Indigo
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFFA855F7), // Violet
                    Color(0xFFD946EF), // Magenta
                    Color(0xFFF43F5E)  // Pink / Rose
                )

                val kawaiiRow1Hints = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                val kawaiiRow2Hints = listOf("@", "#", "$", "%", "&", "*", "-", "+", "=")
                val kawaiiRow3Hints = listOf("!", "?", "\"", "'", "/", ":", ";")

                // Row 1 (10 Keys)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    row1Keys.forEachIndexed { index, letter ->
                        if (index > 0 && isFlatGrid) {
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }
                        val keyText = when (shiftState) {
                            ShiftState.ON, ShiftState.CAPS_LOCK -> letter.uppercase()
                            ShiftState.OFF -> letter.lowercase()
                        }
                        val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                            StylishFontEngine.transformText(keyText, activeFontStyle)
                        } else {
                            keyText
                        }
                        val resolvedTextColor = if (theme.isRainbowSpectrum) {
                            rainbowSpectrum[index % rainbowSpectrum.size]
                        } else textColor

                        val subHint = if (theme.isKawaiiDessert) kawaiiRow1Hints.getOrNull(index) else null

                        FlatKeyButton(
                            text = displayText,
                            subText = subHint,
                            backgroundColor = letterKeyBg,
                            textColor = resolvedTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onKeyPress(keyText, false) }
                        )
                    }
                }

                if (isFlatGrid) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )
                }

                // Row 2 (9 Keys - centered with 0.5f spacer or edge alignment)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    if (!isFlatGrid) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                    row2Keys.forEachIndexed { index, letter ->
                        if (index > 0 && isFlatGrid) {
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }
                        val keyText = when (shiftState) {
                            ShiftState.ON, ShiftState.CAPS_LOCK -> letter.uppercase()
                            ShiftState.OFF -> letter.lowercase()
                        }
                        val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                            StylishFontEngine.transformText(keyText, activeFontStyle)
                        } else {
                            keyText
                        }
                        val resolvedTextColor = if (theme.isRainbowSpectrum) {
                            rainbowSpectrum[(index + 1) % rainbowSpectrum.size]
                        } else textColor

                        val subHint = if (theme.isKawaiiDessert) kawaiiRow2Hints.getOrNull(index) else null

                        FlatKeyButton(
                            text = displayText,
                            subText = subHint,
                            backgroundColor = letterKeyBg,
                            textColor = resolvedTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onKeyPress(keyText, false) }
                        )
                    }
                    if (!isFlatGrid) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }

                if (isFlatGrid) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineThicknessDp)
                            .background(gridBorderColor)
                    )
                }

                // Row 3 (Shift [1.5f] + 7 Keys [1f each] + Backspace [1.5f])
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight)
                ) {
                    // Shift Key
                    if (theme.isKawaiiDessert) {
                        FlatKeyButton(
                            text = "🍓",
                            backgroundColor = if (shiftState != ShiftState.OFF) accentColor else functionKeyBg,
                            textColor = if (shiftState != ShiftState.OFF) accentTextColor else functionTextColor,
                            badgeColor = theme.shiftBadgeColor,
                            isFunctionKey = true,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1.5f),
                            onClick = onToggleShift
                        )
                    } else {
                        IconKeyButton(
                            icon = Icons.Default.ArrowUpward,
                            contentDescription = "Shift",
                            backgroundColor = if (shiftState != ShiftState.OFF) accentColor else functionKeyBg,
                            iconColor = if (shiftState != ShiftState.OFF) accentTextColor else functionTextColor,
                            badgeColor = theme.shiftBadgeColor,
                            isFunctionKey = true,
                            modifier = Modifier.weight(1.5f),
                            onClick = onToggleShift
                        )
                    }

                    if (isFlatGrid) {
                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )
                    }

                    // 7 Middle Keys
                    row3Keys.forEachIndexed { index, letter ->
                        val keyText = when (shiftState) {
                            ShiftState.ON, ShiftState.CAPS_LOCK -> letter.uppercase()
                            ShiftState.OFF -> letter.lowercase()
                        }
                        val displayText = if (settings.enableStylishFonts && activeFontStyle != StylishFontStyle.NORMAL) {
                            StylishFontEngine.transformText(keyText, activeFontStyle)
                        } else {
                            keyText
                        }
                        val resolvedTextColor = if (theme.isRainbowSpectrum) {
                            rainbowSpectrum[(index + 2) % rainbowSpectrum.size]
                        } else textColor

                        val subHint = if (theme.isKawaiiDessert) kawaiiRow3Hints.getOrNull(index) else null

                        FlatKeyButton(
                            text = displayText,
                            subText = subHint,
                            backgroundColor = letterKeyBg,
                            textColor = resolvedTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onKeyPress(keyText, false) }
                        )
                        if (isFlatGrid) {
                            Spacer(
                                modifier = Modifier
                                    .width(lineThicknessDp)
                                    .fillMaxHeight()
                                    .background(gridBorderColor)
                            )
                        }
                    }

                    // Backspace Key
                    if (theme.isKawaiiDessert) {
                        FlatKeyButton(
                            text = "🥤",
                            backgroundColor = functionKeyBg,
                            textColor = functionTextColor,
                            badgeColor = theme.backspaceBadgeColor,
                            isFunctionKey = true,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1.5f),
                            onClick = onDelete
                        )
                    } else {
                        RepeatingBackspaceKey(
                            backgroundColor = functionKeyBg,
                            iconColor = functionTextColor,
                            badgeColor = theme.backspaceBadgeColor,
                            modifier = Modifier.weight(1.5f),
                            onDelete = onDelete
                        )
                    }
                }
            }
        } else {
            // Symbols Keypad Rows (Numeric & Alt Symbols)
            val isAltSymbols = keyboardMode == KeyboardMode.ALT_SYMBOLS
            val row1Symbols = if (isAltSymbols) {
                listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
            } else {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            }
            val row2Symbols = if (isAltSymbols) {
                listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\")
            } else {
                listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
            }
            val row3LeadingKey = if (isAltSymbols) "123" else "=<"
            val row3MiddleSymbols = if (isAltSymbols) {
                listOf("৳", "%", "©", "®", "™", "✓", "[", "]")
            } else {
                listOf("*", "\"", "'", ":", ";", "!", "?")
            }

            // Symbol Row 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight)
            ) {
                row1Symbols.forEachIndexed { index, sym ->
                    if (index > 0 && isFlatGrid) {
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
                        onClick = { onKeyPress(sym, false) }
                    )
                }
            }

            if (isFlatGrid) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )
            }

            // Symbol Row 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight)
            ) {
                row2Symbols.forEachIndexed { index, sym ->
                    if (index > 0 && isFlatGrid) {
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
                        onClick = { onKeyPress(sym, false) }
                    )
                }
            }

            if (isFlatGrid) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineThicknessDp)
                        .background(gridBorderColor)
                )
            }

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
                    isFunctionKey = true,
                    modifier = Modifier.weight(1.5f),
                    onClick = { onKeyPress(row3LeadingKey, false) }
                )

                if (isFlatGrid) {
                    Spacer(
                        modifier = Modifier
                            .width(lineThicknessDp)
                            .fillMaxHeight()
                            .background(gridBorderColor)
                    )
                }

                row3MiddleSymbols.forEach { sym ->
                    FlatKeyButton(
                        text = sym,
                        backgroundColor = letterKeyBg,
                        textColor = textColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(sym, false) }
                    )
                    if (isFlatGrid) {
                        Spacer(
                            modifier = Modifier
                                .width(lineThicknessDp)
                                .fillMaxHeight()
                                .background(gridBorderColor)
                        )
                    }
                }

                RepeatingBackspaceKey(
                    backgroundColor = functionKeyBg,
                    iconColor = functionTextColor,
                    badgeColor = theme.backspaceBadgeColor,
                    modifier = Modifier.weight(1.5f),
                    onDelete = onDelete
                )
            }
        }
    }
}
