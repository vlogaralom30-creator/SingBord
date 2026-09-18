package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SingBordInlineVoiceBar
 *
 * Modern Gboard-style inline speech recognition strip.
 * Sits in the top suggestion toolbar while keeping the entire keyboard 100% visible and interactive.
 * Features live audio equalizer soundwaves, real-time speech-to-text streaming,
 * auto-synced Bangla/English switching, and quick studio expand / close controls.
 */
@Composable
fun SingBordInlineVoiceBar(
    theme: KeyboardThemePalette,
    isFlatGrid: Boolean,
    lineThicknessDp: Dp,
    gridBorderColor: Color,
    voiceState: SingBordVoiceInputManager.VoiceState,
    rmsLevel: Float,
    liveText: String,
    currentVoiceLang: String,
    onToggleMic: () -> Unit,
    onSwitchLanguage: () -> Unit,
    onExpandFullPad: () -> Unit,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = voiceState is SingBordVoiceInputManager.VoiceState.Listening
    val isPreparing = voiceState is SingBordVoiceInputManager.VoiceState.Preparing
    val isError = voiceState is SingBordVoiceInputManager.VoiceState.Error
    val isBangla = currentVoiceLang.startsWith("bn")

    // Pulsing animations for mic and soundwaves
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceBarTransitions")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val waveScale1 by animateFloatAsState(
        targetValue = if (isListening) (0.3f + rmsLevel * 0.9f).coerceIn(0.2f, 1.2f) else 0.25f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Wave1"
    )
    val waveScale2 by animateFloatAsState(
        targetValue = if (isListening) (0.4f + rmsLevel * 1.1f).coerceIn(0.25f, 1.4f) else 0.35f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "Wave2"
    )
    val waveScale3 by animateFloatAsState(
        targetValue = if (isListening) (0.25f + rmsLevel * 0.85f).coerceIn(0.2f, 1.1f) else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Wave3"
    )
    val waveScale4 by animateFloatAsState(
        targetValue = if (isListening) (0.35f + rmsLevel * 1.0f).coerceIn(0.2f, 1.3f) else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "Wave4"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(theme.suggestionStripBg)
            .then(if (!isFlatGrid) Modifier.padding(horizontal = 4.dp, vertical = 2.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Left: Pulsing Mic Button with live ring
        val micShape = RoundedCornerShape(if (isFlatGrid) 15.dp else 8.dp)
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(34.dp)
                .clip(micShape)
                .background(
                    if (isListening) theme.accentColor
                    else if (isError) Color(0xFFEF4444)
                    else theme.functionKeyBg
                )
                .clickable {
                    triggerFeedback()
                    onToggleMic()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isListening) {
                // Pulsing glow background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1f + rmsLevel * 0.25f)
                        .background(theme.accentColor.copy(alpha = pulseAlpha * 0.3f))
                )
            }
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else if (isError) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Toggle Microphone",
                tint = if (isListening || isError) Color.White else theme.functionTextColor,
                modifier = Modifier.size(18.dp)
            )
        }

        if (isFlatGrid) {
            Spacer(modifier = Modifier.width(lineThicknessDp).fillMaxHeight().background(gridBorderColor))
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }

        // 2. Soundwave Equalizer (4 live dancing vertical bars)
        Row(
            modifier = Modifier
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.keyBg.copy(alpha = 0.5f))
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            val barColor = if (isListening) theme.accentColor else theme.functionTextColor.copy(alpha = 0.4f)
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((8.dp * (1f + waveScale1)).coerceIn(4.dp, 20.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((11.dp * (1f + waveScale2)).coerceIn(5.dp, 24.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((7.dp * (1f + waveScale3)).coerceIn(4.dp, 18.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((10.dp * (1f + waveScale4)).coerceIn(5.dp, 22.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 3. Middle: Live Speech Text / Status Prompt
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(8.dp))
                .background(theme.candidateBg)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (liveText.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = liveText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.keyTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Blinking live typing cursor
                    Text(
                        text = " ▌",
                        fontSize = 11.sp,
                        color = theme.accentColor.copy(alpha = pulseAlpha),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val promptText = when {
                    isPreparing -> if (isBangla) "মাইক তৈরি হচ্ছে..." else "Starting mic..."
                    isListening -> if (isBangla) "🎙️ শুনছি... বলুন" else "🎙️ Speak now..."
                    isError -> (voiceState as SingBordVoiceInputManager.VoiceState.Error).message
                    else -> if (isBangla) "মাইকে চাপ দিয়ে বলুন" else "Tap mic to speak"
                }
                Text(
                    text = promptText,
                    fontSize = 12.sp,
                    fontWeight = if (isListening) FontWeight.Bold else FontWeight.Medium,
                    color = if (isError) Color(0xFFEF4444) else if (isListening) theme.accentColor else theme.keyTextColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 4. Right Controls: Language Switcher Chip Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(theme.accentColor.copy(alpha = 0.15f))
                .border(0.8.dp, theme.accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .clickable {
                    triggerFeedback()
                    onSwitchLanguage()
                }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (isBangla) "🇧🇩 BN" else "🇺🇸 EN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.accentColor
                )
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch language",
                    tint = theme.accentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(3.dp))

        // 5. Expand to Studio Mode (Full sub-panel)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(theme.functionKeyBg.copy(alpha = 0.5f))
                .clickable {
                    triggerFeedback()
                    onExpandFullPad()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.OpenInFull,
                contentDescription = "Full Voice Pad",
                tint = theme.functionTextColor,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // 6. Close Inline Voice
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(theme.functionKeyBg.copy(alpha = 0.5f))
                .clickable {
                    triggerFeedback()
                    onClose()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Voice Typing",
                tint = theme.functionTextColor,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
