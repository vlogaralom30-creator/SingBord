package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlatLightColorScheme = lightColorScheme(
    primary = SingBordPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = SingBordPrimaryBlue,
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    background = SingBordCanvasBg,
    onBackground = SingBordPrimaryText,
    surface = SingBordCardBg,
    onSurface = SingBordPrimaryText,
    outline = SingBordBorderGray
)

@Composable
fun SingBordTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FlatLightColorScheme,
        typography = Typography,
        content = content
    )
}

