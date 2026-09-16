package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * VoiceInputSubPanel
 *
 * Premium in-keyboard voice recognition UI overlay for SingBord.
 * Provides real-time audio soundwave visualizer, fast language switching (Bangla/English),
 * continuous partial text preview, and 1-tap insertion for long & short words.
 */
@Composable
fun VoiceInputSubPanel(
    theme: KeyboardThemePalette,
    lineThicknessDp: Dp,
    gridBorderColor: Color,
    totalHeight: Dp,
    initialLanguageCode: String,
    listener: KeyboardActionListener?,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    val context = LocalContext.current
    var activeLang by remember { mutableStateOf(if (initialLanguageCode.startsWith("bn")) "bn-BD" else "en-US") }
    var voiceState by remember { mutableStateOf<SingBordVoiceInputManager.VoiceState>(SingBordVoiceInputManager.VoiceState.Idle) }
    var liveText by remember { mutableStateOf("") }
    var rmsLevel by remember { mutableStateOf(0f) }

    // Soundwave animation scale
    val animatedScale by animateFloatAsState(
        targetValue = 1f + (rmsLevel * 0.45f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "MicScale"
    )

    // Pulse animation for idle/listening state
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val voiceManager = remember(activeLang) {
        SingBordVoiceInputManager(
            context = context,
            onPartialResult = { partial ->
                liveText = partial
            },
            onFinalResult = { final ->
                liveText = final
                if (final.isNotBlank()) {
                    listener?.onTextEntered(final + " ")
                    triggerFeedback()
                }
            },
            onStateChanged = { state ->
                voiceState = state
            },
            onRmsLevelChanged = { level ->
                rmsLevel = level
            }
        )
    }

    DisposableEffect(activeLang) {
        voiceManager.startListening(activeLang)
        onDispose {
            voiceManager.cancel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header Bar: Language Switcher + Status + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Active Voice Language Selector
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            triggerFeedback()
                            activeLang = if (activeLang == "bn-BD") "en-US" else "bn-BD"
                        },
                    color = theme.accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (activeLang == "bn-BD") "🇧🇩 বাংলা (bn-BD)" else "🇺🇸 English (en-US)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentColor
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Voice Language",
                            tint = theme.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Voice Status Label
                val statusText = when (voiceState) {
                    is SingBordVoiceInputManager.VoiceState.Preparing -> if (activeLang == "bn-BD") "মাইক প্রস্তুত হচ্ছে..." else "Preparing Mic..."
                    is SingBordVoiceInputManager.VoiceState.Listening -> if (activeLang == "bn-BD") "শুনছি... বলুন" else "Listening... Speak"
                    is SingBordVoiceInputManager.VoiceState.Processing -> if (activeLang == "bn-BD") "প্রসেসিং হচ্ছে..." else "Processing..."
                    is SingBordVoiceInputManager.VoiceState.Error -> (voiceState as SingBordVoiceInputManager.VoiceState.Error).message
                    else -> if (activeLang == "bn-BD") "ভয়েস টাইপিং" else "Voice Typing"
                }

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (voiceState is SingBordVoiceInputManager.VoiceState.Error) Color(0xFFEF4444) else theme.keyTextColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Close Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(theme.functionKeyBg)
                        .clickable {
                            triggerFeedback()
                            voiceManager.cancel()
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Voice Typing",
                        tint = theme.functionTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 2. Middle Area: Mic Visualizer + Live Spoken Text Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pulsing Mic Button
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Ripple Circle
                    if (voiceState is SingBordVoiceInputManager.VoiceState.Listening) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(animatedScale)
                                .clip(CircleShape)
                                .background(theme.accentColor.copy(alpha = pulseAlpha * 0.3f))
                        )
                    }

                    // Main Mic Circle
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .clickable {
                                triggerFeedback()
                                if (voiceManager.isListening) {
                                    voiceManager.stopListening()
                                } else {
                                    voiceManager.startListening(activeLang)
                                }
                            },
                        color = if (voiceManager.isListening) theme.accentColor else theme.functionKeyBg,
                        shadowElevation = 4.dp,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (voiceManager.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Mic",
                                tint = if (voiceManager.isListening) theme.accentTextColor else theme.functionTextColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Live Spoken Text Preview Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = theme.keyBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, theme.keyBorderColor.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (liveText.isNotBlank()) {
                            Text(
                                text = liveText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.keyTextColor,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            val placeholder = when (voiceState) {
                                is SingBordVoiceInputManager.VoiceState.Listening ->
                                    if (activeLang == "bn-BD") "যেটা বলবেন তা এখানে সরাসরি টাইপ হবে..." else "Speak now, words will appear live..."
                                is SingBordVoiceInputManager.VoiceState.Error ->
                                    if (activeLang == "bn-BD") "মাইক রিট্রাই করতে চাপ দিন..." else "Tap mic button to try again..."
                                else ->
                                    if (activeLang == "bn-BD") "মাইক অন করে কথা বলুন..." else "Tap mic button to speak..."
                            }
                            Text(
                                text = placeholder,
                                fontSize = 13.sp,
                                color = theme.keyTextColor.copy(alpha = 0.45f),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            // 3. Quick Action Bar: Space, Delete, Manual Commit, Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear Preview
                OutlinedButton(
                    onClick = {
                        triggerFeedback()
                        liveText = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Clear", fontSize = 12.sp, color = theme.keyTextColor)
                }

                // Insert Space
                OutlinedButton(
                    onClick = {
                        triggerFeedback()
                        listener?.onSpace()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SpaceBar, contentDescription = null, modifier = Modifier.size(16.dp), tint = theme.keyTextColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Space", fontSize = 12.sp, color = theme.keyTextColor)
                }

                // Backspace
                OutlinedButton(
                    onClick = {
                        triggerFeedback()
                        listener?.onDelete()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, modifier = Modifier.size(16.dp), tint = theme.keyTextColor)
                }

                // Commit Spoken Text Manual Button
                Button(
                    onClick = {
                        triggerFeedback()
                        if (liveText.isNotBlank()) {
                            listener?.onTextEntered(liveText + " ")
                            liveText = ""
                        } else {
                            onClose()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentColor,
                        contentColor = theme.accentTextColor
                    ),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(if (liveText.isNotBlank()) "Insert" else "Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
