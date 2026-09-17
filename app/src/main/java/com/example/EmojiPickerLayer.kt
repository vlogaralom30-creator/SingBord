package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Integrated Emoji picker UI component that appears as a full keyboard layer.
 * Includes:
 * - Search bar with instant real-time filtering across English and Banglish keywords
 * - Category bar with tab indicators
 * - 8-column responsive grid with haptic feedback
 * - Bottom action row with ABC switch, Spacebar, and Backspace
 */
@Composable
fun EmojiPickerLayer(
    theme: KeyboardThemePalette,
    recents: List<String>,
    keyHeight: Dp,
    onEmojiSelected: (String) -> Unit,
    onBackToAlpha: () -> Unit,
    onSpacePress: () -> Unit,
    onBackspacePress: () -> Unit,
    onFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EmojiCategory.SMILEYS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val displayedEmojis = remember(selectedCategory, recents, searchQuery, isSearchActive) {
        if (isSearchActive && searchQuery.isNotBlank()) {
            val results = EmojiData.searchEmojis(searchQuery)
            if (results.isNotEmpty()) results else EmojiData.smileys
        } else {
            EmojiData.getEmojis(selectedCategory, recents)
        }
    }

    val isFlatGrid = theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID
    val borderColor = if (theme.isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.keyboardBg)
            .testTag("emoji_picker_layer")
    ) {
        // Top Toolbar: Categories & Search Toggle
        Surface(
            color = if (theme.isDark) Color(0x1AFFFFFF) else Color(0x0A000000),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchActive) {
                    // Active Search Input
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(if (theme.isDark) Color(0x33FFFFFF) else Color(0x15000000))
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.keyTextColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search emojis...",
                                    fontSize = 13.sp,
                                    color = theme.keyTextColor.copy(alpha = 0.5f)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = theme.keyTextColor,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(theme.accentColor),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { /* Done */ }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(searchFocusRequester)
                                    .testTag("emoji_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = theme.keyTextColor.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cancel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                isSearchActive = false
                                searchQuery = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                } else {
                    // Category Icons
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EmojiCategory.values().forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) theme.accentColor.copy(alpha = 0.22f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onFeedback()
                                        selectedCategory = cat
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.icon,
                                    fontSize = if (isSelected) 18.sp else 16.sp
                                )
                            }
                        }
                    }

                    // Search Trigger Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (theme.isDark) Color(0x22FFFFFF) else Color(0x10000000))
                            .clickable {
                                onFeedback()
                                isSearchActive = true
                                coroutineScope.launch {
                                    delay(100)
                                    try {
                                        searchFocusRequester.requestFocus()
                                    } catch (e: Exception) {
                                        // Ignore focus errors
                                    }
                                }
                            }
                            .testTag("emoji_search_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Emojis",
                            tint = theme.keyTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Search Results Indicator
        if (isSearchActive && searchQuery.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Results for \"$searchQuery\"",
                    fontSize = 11.sp,
                    color = theme.keyTextColor.copy(alpha = 0.6f)
                )
                Text(
                    text = "${displayedEmojis.size} found",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.accentColor
                )
            }
        }

        // Emoji Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (isFlatGrid) theme.keyBg else theme.keyboardBg)
                .testTag("emoji_grid"),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(displayedEmojis) { emoji ->
                val emojiShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 8.dp)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(emojiShape)
                        .clickable {
                            onFeedback()
                            onEmojiSelected(emoji)
                        },
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

        // Bottom Row Controls (ABC, Space, Backspace)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ABC button to return to typing
            Surface(
                color = theme.functionKeyBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clickable {
                        onFeedback()
                        onBackToAlpha()
                    }
                    .testTag("emoji_picker_abc_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "ABC",
                        color = theme.functionTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Quick Space Bar
            Surface(
                color = theme.keyBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(3.5f)
                    .fillMaxHeight()
                    .clickable {
                        onFeedback()
                        onSpacePress()
                    }
                    .testTag("emoji_picker_space_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Space",
                        color = theme.keyTextColor.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Backspace
            Surface(
                color = theme.functionKeyBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clickable {
                        onFeedback()
                        onBackspacePress()
                    }
                    .testTag("emoji_picker_backspace_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Backspace,
                        contentDescription = "Backspace",
                        tint = theme.functionTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
