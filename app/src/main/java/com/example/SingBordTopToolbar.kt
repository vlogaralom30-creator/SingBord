package com.example

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserDictionaryRepository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingBordTopToolbar(
    theme: KeyboardThemePalette,
    settings: KeyboardSettings,
    isFlatGrid: Boolean,
    lineThicknessDp: Dp,
    gridBorderColor: Color,
    textColor: Color,
    toolbarMode: ToolbarMode,
    activeSubPanel: KeyboardSubPanel,
    keyboardMode: KeyboardMode,
    currentLanguage: KeyboardLanguage,
    currentComposingWord: String,
    lastCommittedWord: String,
    activeFontStyle: StylishFontStyle,
    userRepo: UserDictionaryRepository,
    voiceState: SingBordVoiceInputManager.VoiceState,
    voiceRmsLevel: Float,
    voiceLiveText: String,
    voiceLanguageCode: String,
    listener: KeyboardActionListener?,
    onToggleTools: () -> Unit,
    onSelectEmojiMode: () -> Unit,
    onOpenSubPanel: (KeyboardSubPanel) -> Unit,
    onSwitchToTools: () -> Unit,
    onSelectCandidate: (String) -> Unit,
    onOpenVoiceInput: () -> Unit,
    onOpenVoiceSubPanel: () -> Unit,
    onToggleVoiceMic: () -> Unit,
    onSwitchVoiceLanguage: () -> Unit,
    onCloseVoiceInline: () -> Unit,
    triggerFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (toolbarMode == ToolbarMode.VOICE_INLINE) {
        SingBordInlineVoiceBar(
            theme = theme,
            isFlatGrid = isFlatGrid,
            lineThicknessDp = lineThicknessDp,
            gridBorderColor = gridBorderColor,
            voiceState = voiceState,
            rmsLevel = voiceRmsLevel,
            liveText = voiceLiveText,
            currentVoiceLang = voiceLanguageCode,
            onToggleMic = onToggleVoiceMic,
            onSwitchLanguage = onSwitchVoiceLanguage,
            onExpandFullPad = onOpenVoiceSubPanel,
            onClose = onCloseVoiceInline,
            triggerFeedback = triggerFeedback,
            modifier = modifier
        )
        return
    }

    // Ridmik-Style Dual-Mode Top Toolbar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(theme.suggestionStripBg)
            .then(if (!isFlatGrid) Modifier.padding(horizontal = 4.dp, vertical = 2.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: T Toggle Icon Button
        val isToolsActive = toolbarMode == ToolbarMode.TOOLS || activeSubPanel != KeyboardSubPanel.NONE
        val tBtnShape = RoundedCornerShape(if (isFlatGrid) 15.dp else 8.dp)
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(32.dp)
                .clip(tBtnShape)
                .then(
                    if (isToolsActive) {
                        Modifier.background(theme.accentColor)
                    } else if (!isFlatGrid) {
                        Modifier
                            .background(theme.functionKeyBg.copy(alpha = if (theme.isDark) 0.65f else 0.85f))
                            .then(
                                if (theme.keyBorderWidthDp > 0f) Modifier.border(0.7.dp, theme.keyBorderColor.copy(alpha = 0.5f), tBtnShape)
                                else Modifier
                            )
                    } else {
                        Modifier.background(theme.functionKeyBg)
                    }
                )
                .clickable {
                    triggerFeedback()
                    onToggleTools()
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

        if (isFlatGrid) {
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }

        if (toolbarMode == ToolbarMode.TOOLS) {
            // Tools Panel Strip (Icon-only, no name text): Emoji, Theme, Clipboard, Edit Pad, Number Dialer, Stylish Fonts, Settings, Voice
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
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
                        onSelectEmojiMode()
                    }
                )

                ToolIconItem(
                    icon = Icons.Default.AutoAwesome,
                    contentDescription = "Symbols Studio",
                    theme = theme,
                    isActive = activeSubPanel == KeyboardSubPanel.SYMBOLS_STUDIO,
                    onClick = {
                        triggerFeedback()
                        onOpenSubPanel(KeyboardSubPanel.SYMBOLS_STUDIO)
                    }
                )

                ToolIconItem(
                    icon = Icons.Default.Palette,
                    contentDescription = "Themes",
                    theme = theme,
                    isActive = activeSubPanel == KeyboardSubPanel.THEME_PICKER,
                    onClick = {
                        triggerFeedback()
                        onOpenSubPanel(KeyboardSubPanel.THEME_PICKER)
                    }
                )

                ToolIconItem(
                    icon = Icons.Default.ContentPaste,
                    contentDescription = "Clipboard",
                    theme = theme,
                    isActive = activeSubPanel == KeyboardSubPanel.CLIPBOARD,
                    onClick = {
                        triggerFeedback()
                        onOpenSubPanel(KeyboardSubPanel.CLIPBOARD)
                    }
                )

                ToolIconItem(
                    icon = Icons.Default.EditNote,
                    contentDescription = "Text Edit Pad",
                    theme = theme,
                    isActive = activeSubPanel == KeyboardSubPanel.TEXT_EDITOR,
                    onClick = {
                        triggerFeedback()
                        onOpenSubPanel(KeyboardSubPanel.TEXT_EDITOR)
                    }
                )

                ToolIconItem(
                    icon = Icons.Default.Dialpad,
                    contentDescription = "Number Dialer",
                    theme = theme,
                    isActive = activeSubPanel == KeyboardSubPanel.NUMBER_DIALER,
                    onClick = {
                        triggerFeedback()
                        onOpenSubPanel(KeyboardSubPanel.NUMBER_DIALER)
                    }
                )

                ToolIconItem(
                    icon = Icons.Default.TextFields,
                    contentDescription = "Stylish Fonts",
                    theme = theme,
                    isActive = activeSubPanel == KeyboardSubPanel.FONT_PICKER,
                    onClick = {
                        triggerFeedback()
                        onOpenSubPanel(KeyboardSubPanel.FONT_PICKER)
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
                    isActive = toolbarMode == ToolbarMode.VOICE_INLINE || activeSubPanel == KeyboardSubPanel.VOICE_INPUT,
                    onClick = {
                        triggerFeedback()
                        onOpenVoiceInput()
                    }
                )
            }
        } else {
            // Suggestion & Predictive Typing Strip
            val isComposing = currentComposingWord.isNotBlank()
            val activeCandidates = remember(
                currentComposingWord,
                lastCommittedWord,
                currentLanguage,
                settings.enableBanglish,
                settings.enableWordLearning,
                settings.showSuggestions
            ) {
                if (!settings.showSuggestions) {
                    emptyList()
                } else if (isComposing) {
                    WordSuggestionEngine.getSuggestions(
                        prefix = currentComposingWord,
                        userRepo = if (settings.enableWordLearning) userRepo else null,
                        language = currentLanguage,
                        enableBanglish = settings.enableBanglish,
                        maxCount = 4
                    )
                } else if (lastCommittedWord.isNotBlank()) {
                    WordSuggestionEngine.getNextWordPredictions(
                        prevWord = lastCommittedWord,
                        userRepo = if (settings.enableWordLearning) userRepo else null,
                        language = currentLanguage,
                        maxCount = 4
                    )
                } else {
                    emptyList()
                }
            }

            val hasSuggestions = activeCandidates.isNotEmpty()

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeCandidates.isEmpty()) {
                    val emptyShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 8.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(if (isFlatGrid) 1f else 0.88f)
                            .clip(emptyShape)
                            .background(if (isFlatGrid) theme.candidateBg else theme.functionKeyBg.copy(alpha = 0.35f))
                            .clickable {
                                triggerFeedback()
                                onSwitchToTools()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (keyboardMode == KeyboardMode.EMOJI) "Emoji • Tap T for Tools" else "SingBord • Tap T for Tools",
                            fontSize = 12.sp,
                            color = theme.functionTextColor.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    activeCandidates.forEachIndexed { index, candidate ->
                        if (index > 0) {
                            if (isFlatGrid) {
                                Spacer(
                                    modifier = Modifier
                                        .width(lineThicknessDp)
                                        .fillMaxHeight()
                                        .background(gridBorderColor)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                        val isHighlighted = isComposing && (index == 0 || (activeCandidates.size > 1 && index == 1))
                        val candShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 8.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(if (isFlatGrid) 1f else 0.88f)
                            .clip(candShape)
                            .background(
                                if (isHighlighted) theme.accentColor.copy(alpha = if (theme.isDark) 0.28f else 0.18f)
                                else if (isFlatGrid) theme.candidateBg
                                else theme.keyBg.copy(alpha = 0.85f)
                            )
                            .then(
                                if (isHighlighted && !isFlatGrid) Modifier.border(1.dp, theme.accentColor.copy(alpha = 0.65f), candShape)
                                else if (!isFlatGrid && theme.keyBorderWidthDp > 0f) Modifier.border(0.6.dp, theme.keyBorderColor.copy(alpha = 0.4f), candShape)
                                else Modifier
                            )
                            .clickable {
                                triggerFeedback()
                                onSelectCandidate(candidate)
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

            // Voice / Mic icon on the right side: ONLY shown when enabled in settings and NO word suggestions active
            if (settings.enableVoiceTyping && !hasSuggestions) {
                if (isFlatGrid) {
                    Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                val micShape = RoundedCornerShape(if (isFlatGrid) 15.dp else 8.dp)
                val resolvedVoiceLang = if (settings.voiceLanguage == "auto") {
                    if (currentLanguage == KeyboardLanguage.ENGLISH) "en-US" else "bn-BD"
                } else {
                    settings.voiceLanguage
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(32.dp)
                        .clip(micShape)
                        .then(
                            if (!isFlatGrid) {
                                Modifier
                                    .background(theme.functionKeyBg.copy(alpha = if (theme.isDark) 0.65f else 0.85f))
                                    .then(
                                        if (theme.keyBorderWidthDp > 0f) Modifier.border(0.7.dp, theme.keyBorderColor.copy(alpha = 0.5f), micShape)
                                        else Modifier
                                    )
                            } else {
                                Modifier.background(theme.functionKeyBg)
                            }
                        )
                        .combinedClickable(
                            onClick = {
                                triggerFeedback()
                                onOpenVoiceInput()
                            },
                            onLongClick = {
                                triggerFeedback()
                                onOpenVoiceSubPanel()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input ($resolvedVoiceLang) - Tap for inline, Long-press for studio",
                        tint = theme.functionTextColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

