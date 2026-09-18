package com.example

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

@Composable
fun SingBordBottomActionRow(
    theme: KeyboardThemePalette,
    settings: KeyboardSettings,
    isFlatGrid: Boolean,
    lineThicknessDp: Dp,
    gridBorderColor: Color,
    letterKeyBg: Color,
    functionKeyBg: Color,
    keyPressedBg: Color,
    functionTextColor: Color,
    accentColor: Color,
    accentTextColor: Color,
    keyHeight: Dp,
    keyboardMode: KeyboardMode,
    currentLanguage: KeyboardLanguage,
    currentComposingWord: String,
    lastCommittedWord: String,
    editorInfo: EditorInfo?,
    listener: KeyboardActionListener?,
    onToggleMode: () -> Unit,
    onToggleAltSymbols: () -> Unit,
    onCycleLanguage: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onKeyPress: (String, Boolean) -> Unit,
    onSpace: () -> Unit,
    onDoubleSpacePeriod: () -> Unit,
    onMoveCursor: (Int) -> Unit,
    onEnter: () -> Unit,
    triggerFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Row 4: Bottom Action Row
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(keyHeight)
    ) {
        // Mode Toggle Key (?123 / ABC / =<)
        val modeLabel = when (keyboardMode) {
            KeyboardMode.ALPHA -> if (theme.isKawaiiDessert) "🍨 ?123" else "?123"
            KeyboardMode.NUMERIC_SYMBOLS -> "ABC"
            KeyboardMode.ALT_SYMBOLS -> "ABC"
            KeyboardMode.EMOJI -> "ABC"
        }

        FlatKeyButton(
            text = modeLabel,
            backgroundColor = functionKeyBg,
            textColor = functionTextColor,
            fontWeight = FontWeight.Bold,
            isFunctionKey = true,
            badgeColor = theme.modeBadgeColor,
            modifier = Modifier.weight(1.2f),
            onClick = {
                triggerFeedback()
                onToggleMode()
            }
        )

        if (isFlatGrid) {
            Spacer(
                modifier = Modifier
                    .width(lineThicknessDp)
                    .fillMaxHeight()
                    .background(gridBorderColor)
            )
        }

        // Secondary symbol switch key when in symbol mode
        if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS || keyboardMode == KeyboardMode.ALT_SYMBOLS) {
            val altLabel = if (keyboardMode == KeyboardMode.NUMERIC_SYMBOLS) "=<" else "123"
            FlatKeyButton(
                text = altLabel,
                backgroundColor = functionKeyBg,
                textColor = functionTextColor,
                fontWeight = FontWeight.Bold,
                isFunctionKey = true,
                badgeColor = theme.modeBadgeColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerFeedback()
                    onToggleAltSymbols()
                }
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

        // Language / Globe Key (dedicated language switch icon under the keyboard)
        val showLanguageSwitchKey = (settings.showLanguageSwitchKey || theme.showLanguageGlobeKey) && keyboardMode == KeyboardMode.ALPHA
        if (showLanguageSwitchKey) {
            IconKeyButton(
                icon = Icons.Default.Language,
                contentDescription = "Switch Language (${currentLanguage.label})",
                backgroundColor = functionKeyBg,
                iconColor = functionTextColor,
                isFunctionKey = true,
                modifier = Modifier.weight(0.9f),
                onLongClick = {
                    triggerFeedback()
                    onCycleLanguage()
                },
                onClick = {
                    triggerFeedback()
                    onCycleLanguage()
                }
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

        // Emoji Button (if enabled in settings)
        if (settings.enableEmoji) {
            IconKeyButton(
                icon = Icons.Default.SentimentSatisfiedAlt,
                contentDescription = "Emoji",
                backgroundColor = functionKeyBg,
                iconColor = functionTextColor,
                isFunctionKey = true,
                modifier = Modifier.weight(0.9f),
                onClick = {
                    triggerFeedback()
                    onOpenEmojiPicker()
                }
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

        // Comma (in Alpha mode)
        if (keyboardMode == KeyboardMode.ALPHA) {
            val isProbhat = currentLanguage == KeyboardLanguage.BANGLA_PROBHAT
            FlatKeyButton(
                text = ",",
                alternates = if (isProbhat) BanglaProbhatLayout.commaAlternates else emptyList(),
                backgroundColor = functionKeyBg,
                textColor = functionTextColor,
                isFunctionKey = true,
                modifier = Modifier.weight(0.8f),
                onAlternateSelected = { alt -> onKeyPress(alt, true) },
                onClick = { onKeyPress(",", isProbhat) }
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

        // Spacebar
        SpacebarKey(
            backgroundColor = letterKeyBg,
            currentLanguage = currentLanguage,
            pressedColor = keyPressedBg,
            dragColor = accentColor.copy(alpha = 0.35f),
            enableGestures = settings.enableSpacebarCursor && settings.enableGestures,
            modifier = Modifier.weight(
                when {
                    keyboardMode != KeyboardMode.ALPHA -> 4.2f
                    showLanguageSwitchKey && settings.enableEmoji -> 3.2f
                    showLanguageSwitchKey || settings.enableEmoji -> 3.8f
                    else -> 4.5f
                }
            ),
            onSpace = onSpace,
            onDoubleSpacePeriod = onDoubleSpacePeriod,
            onMoveCursor = onMoveCursor,
            onLanguageToggle = onCycleLanguage,
            triggerFeedback = triggerFeedback
        )

        if (isFlatGrid) {
            Spacer(
                modifier = Modifier
                    .width(lineThicknessDp)
                    .fillMaxHeight()
                    .background(gridBorderColor)
            )
        }

        // Period / Daari Key
        val isProbhatPeriod = keyboardMode == KeyboardMode.ALPHA && currentLanguage == KeyboardLanguage.BANGLA_PROBHAT
        FlatKeyButton(
            text = if (isProbhatPeriod) "।" else ".",
            subText = if (isProbhatPeriod) "." else null,
            alternates = if (isProbhatPeriod) BanglaProbhatLayout.dariAlternates else emptyList(),
            backgroundColor = functionKeyBg,
            textColor = functionTextColor,
            isFunctionKey = true,
            modifier = Modifier.weight(0.8f),
            onAlternateSelected = { alt -> onKeyPress(alt, true) },
            onClick = {
                if (isProbhatPeriod) {
                    onKeyPress("।", true)
                } else {
                    onKeyPress(".", false)
                }
            }
        )

        if (isFlatGrid) {
            Spacer(
                modifier = Modifier
                    .width(lineThicknessDp)
                    .fillMaxHeight()
                    .background(gridBorderColor)
            )
        }

        // Enter Key
        EnterKeyButton(
            editorInfo = editorInfo,
            accentColor = accentColor,
            accentTextColor = accentTextColor,
            badgeColor = theme.enterBadgeColor,
            modifier = Modifier.weight(1.3f),
            onClick = onEnter
        )
    }
}
