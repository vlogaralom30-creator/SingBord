package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CloudClipboardItem
import com.example.data.SupabaseBackendClient
import kotlinx.coroutines.launch

enum class ClipTab {
    ALL, PINNED, CLOUD
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ClipboardSubPanel(
    theme: KeyboardThemePalette,
    prefs: SingBordPreferences,
    lineThicknessDp: Dp,
    gridBorderColor: Color,
    totalHeight: Dp,
    listener: KeyboardActionListener?,
    onClose: () -> Unit,
    triggerFeedback: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val supabase = remember { SupabaseBackendClient.getInstance(context) }

    var history by remember { mutableStateOf(prefs.getClipboardHistory()) }
    var pinnedClips by remember { mutableStateOf(prefs.getPinnedClips().toSet()) }
    var selectedTab by remember { mutableStateOf(ClipTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var cloudClips by remember { mutableStateOf<List<CloudClipboardItem>>(emptyList()) }
    var isCloudLoading by remember { mutableStateOf(false) }
    var cloudSyncSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Popup modal state for Long-Press full text detail view
    var activeDetailClip by remember { mutableStateOf<String?>(null) }
    var isDetailFromCloud by remember { mutableStateOf(false) }
    var activeCloudItemId by remember { mutableStateOf<String?>(null) }

    // Edit modal state
    var editTargetClip by remember { mutableStateOf<String?>(null) }
    var editTargetNewText by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    // Add new clip modal state
    var showAddDialog by remember { mutableStateOf(false) }
    var newClipInputText by remember { mutableStateOf("") }

    val isFlatGrid = theme.keyShapeStyle == KeyboardKeyShapeStyle.FLAT_GRID

    fun reloadLocalClips() {
        history = prefs.getClipboardHistory()
        pinnedClips = prefs.getPinnedClips().toSet()
    }

    fun loadCloudClips() {
        isCloudLoading = true
        scope.launch {
            val res = supabase.fetchCloudClipboard()
            cloudClips = res.getOrDefault(emptyList())
            isCloudLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadLocalClips()
        loadCloudClips()
    }

    // Filtered lists
    val filteredHistory = remember(history, pinnedClips, selectedTab, searchQuery) {
        val baseList = when (selectedTab) {
            ClipTab.ALL -> {
                // Put pinned items first, then others
                val pinnedList = history.filter { pinnedClips.contains(it) }
                val unpinnedList = history.filterNot { pinnedClips.contains(it) }
                pinnedList + unpinnedList
            }
            ClipTab.PINNED -> history.filter { pinnedClips.contains(it) }
            ClipTab.CLOUD -> emptyList()
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredCloudClips = remember(cloudClips, searchQuery) {
        if (searchQuery.isBlank()) {
            cloudClips
        } else {
            cloudClips.filter {
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(theme.keyboardBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(theme.functionKeyBg)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchActive) {
                    // Search Bar
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = theme.functionTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(theme.accentColor),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search in clips...",
                                    color = theme.functionTextColor.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = theme.functionTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentColor
                        )
                    }
                } else {
                    // Standard Title & Quick Actions
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copypad",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.functionTextColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tab Chips
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            TabChip(
                                label = "All (${history.size})",
                                isSelected = selectedTab == ClipTab.ALL,
                                theme = theme,
                                onClick = {
                                    triggerFeedback()
                                    selectedTab = ClipTab.ALL
                                }
                            )
                        }
                        item {
                            TabChip(
                                label = "📌 Pinned (${pinnedClips.size})",
                                isSelected = selectedTab == ClipTab.PINNED,
                                theme = theme,
                                onClick = {
                                    triggerFeedback()
                                    selectedTab = ClipTab.PINNED
                                }
                            )
                        }
                        item {
                            TabChip(
                                label = "☁️ Cloud (${cloudClips.size})",
                                isSelected = selectedTab == ClipTab.CLOUD,
                                theme = theme,
                                onClick = {
                                    triggerFeedback()
                                    selectedTab = ClipTab.CLOUD
                                    loadCloudClips()
                                }
                            )
                        }
                    }

                    // Action Icons: Search, Add, Clear, Close
                    IconButton(
                        onClick = {
                            triggerFeedback()
                            isSearchActive = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.functionTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            triggerFeedback()
                            newClipInputText = ""
                            showAddDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Clip",
                            tint = theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (selectedTab != ClipTab.CLOUD && history.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                triggerFeedback()
                                // Clears unpinned clips, preserves pinned clips!
                                prefs.clearClipboardHistory(includePinned = false)
                                reloadLocalClips()
                                Toast.makeText(context, "Unpinned clips cleared (Pinned preserved)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Unpinned",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            triggerFeedback()
                            onClose()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.functionTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isFlatGrid) {
                Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Notice / Success bar
            cloudSyncSuccessMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = msg, fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                }
            }

            // Main Content Area
            if (selectedTab == ClipTab.CLOUD) {
                // Cloud Tab List
                if (isCloudLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (isFlatGrid) theme.keyBg else theme.keyboardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = theme.accentColor,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Syncing Cloud Copypad...", fontSize = 11.sp, color = theme.functionTextColor)
                        }
                    }
                } else if (filteredCloudClips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (isFlatGrid) theme.keyBg else theme.keyboardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFF2563EB).copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Cloud Clips Found",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.keyTextColor
                            )
                            Text(
                                text = "Tap ☁️ on any clip or long-press to save permanently to Cloud Copypad",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = theme.functionTextColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (isFlatGrid) theme.keyBg else theme.keyboardBg),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCloudClips, key = { it.id }) { clipItem ->
                            CloudClipCard(
                                item = clipItem,
                                theme = theme,
                                isFlatGrid = isFlatGrid,
                                onClick = {
                                    triggerFeedback()
                                    listener?.onTextEntered(clipItem.content)
                                    prefs.addClipboardItem(clipItem.content)
                                    reloadLocalClips()
                                },
                                onLongClick = {
                                    triggerFeedback()
                                    activeDetailClip = clipItem.content
                                    isDetailFromCloud = true
                                    activeCloudItemId = clipItem.id
                                },
                                onDelete = {
                                    triggerFeedback()
                                    scope.launch {
                                        supabase.deleteCloudClipboardItem(clipItem.id)
                                        loadCloudClips()
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Local / Pinned Tab List
                if (filteredHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (isFlatGrid) theme.keyBg else theme.keyboardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedTab == ClipTab.PINNED) Icons.Default.PushPin else Icons.Default.Assignment,
                                contentDescription = null,
                                tint = theme.keyTextColor.copy(alpha = 0.35f),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = if (selectedTab == ClipTab.PINNED) "No Pinned Clips" else "Clipboard is Empty",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.keyTextColor
                            )
                            Text(
                                text = if (selectedTab == ClipTab.PINNED) "Tap 📌 on any clip to pin it permanently here" else "Copy text anywhere to paste in 1 tap, or tap + to add note",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = theme.functionTextColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (isFlatGrid) theme.keyBg else theme.keyboardBg),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredHistory, key = { it }) { clipText ->
                            val isPinned = pinnedClips.contains(clipText)
                            LocalClipCard(
                                text = clipText,
                                isPinned = isPinned,
                                theme = theme,
                                isFlatGrid = isFlatGrid,
                                onClick = {
                                    triggerFeedback()
                                    listener?.onTextEntered(clipText)
                                    prefs.addClipboardItem(clipText)
                                    reloadLocalClips()
                                },
                                onLongClick = {
                                    triggerFeedback()
                                    activeDetailClip = clipText
                                    isDetailFromCloud = false
                                    activeCloudItemId = null
                                },
                                onTogglePin = {
                                    triggerFeedback()
                                    prefs.togglePinClip(clipText)
                                    reloadLocalClips()
                                },
                                onSaveToCloud = {
                                    triggerFeedback()
                                    scope.launch {
                                        supabase.addCloudClipboardItem(clipText, isPinned = isPinned)
                                        cloudSyncSuccessMessage = "Saved to Cloud Copypad!"
                                        loadCloudClips()
                                    }
                                },
                                onDelete = {
                                    triggerFeedback()
                                    prefs.deleteClipboardItem(clipText)
                                    reloadLocalClips()
                                }
                            )
                        }
                    }
                }
            }

            if (isFlatGrid) {
                Spacer(modifier = Modifier.fillMaxWidth().height(lineThicknessDp).background(gridBorderColor))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Bottom Action Return Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(theme.keyboardBg)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Return to Keyboard Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(if (isFlatGrid) 0.dp else 6.dp))
                        .background(theme.accentColor)
                        .clickable {
                            triggerFeedback()
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardReturn,
                            contentDescription = null,
                            tint = theme.accentTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Return to Keyboard",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentTextColor
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // LONG-PRESS DETAIL POPUP MODAL (Full Text, Edit, Pin, Delete, Cloud)
        // -------------------------------------------------------------
        activeDetailClip?.let { fullText ->
            val isCurrentPinned = pinnedClips.contains(fullText)
            val charCount = fullText.length
            val wordCount = fullText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { activeDetailClip = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.88f)
                        .clickable(enabled = false) {}, // prevent click-through
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.candidateBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Modal Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDetailFromCloud) Icons.Default.Cloud else Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = theme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isDetailFromCloud) "Cloud Copypad Item" else "Clip Details",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.keyTextColor
                                )
                                Text(
                                    text = "$charCount chars • $wordCount words",
                                    fontSize = 10.sp,
                                    color = theme.functionTextColor.copy(alpha = 0.7f)
                                )
                            }
                            if (isCurrentPinned && !isDetailFromCloud) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "📌 Pinned",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            IconButton(
                                onClick = { activeDetailClip = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = theme.functionTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = theme.keyBorderColor.copy(alpha = 0.2f)
                        )

                        // Full Scrollable Text Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.keyboardBg.copy(alpha = 0.5f))
                                .border(0.8.dp, theme.keyBorderColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = fullText,
                                fontSize = 13.sp,
                                color = theme.keyTextColor,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons Grid / Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Primary: Paste Directly
                            Button(
                                onClick = {
                                    triggerFeedback()
                                    listener?.onTextEntered(fullText)
                                    prefs.addClipboardItem(fullText)
                                    reloadLocalClips()
                                    activeDetailClip = null
                                    onClose()
                                },
                                modifier = Modifier.weight(1.3f).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = theme.accentTextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Paste",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accentTextColor
                                )
                            }

                            // Copy to System Clipboard
                            OutlinedButton(
                                onClick = {
                                    triggerFeedback()
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Copypad", fullText))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = theme.keyTextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Copy", fontSize = 11.sp, color = theme.keyTextColor)
                            }

                            // Edit Button
                            OutlinedButton(
                                onClick = {
                                    triggerFeedback()
                                    editTargetClip = fullText
                                    editTargetNewText = fullText
                                    showEditDialog = true
                                    activeDetailClip = null
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = theme.keyTextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Edit", fontSize = 11.sp, color = theme.keyTextColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Secondary Row: Pin / Save Cloud / Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!isDetailFromCloud) {
                                // Pin / Unpin Toggle
                                OutlinedButton(
                                    onClick = {
                                        triggerFeedback()
                                        prefs.togglePinClip(fullText)
                                        reloadLocalClips()
                                    },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCurrentPinned) Color(0xFFFEF3C7) else Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (isCurrentPinned) Color(0xFFD97706) else theme.keyTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isCurrentPinned) "Unpin" else "Pin",
                                        fontSize = 11.sp,
                                        color = if (isCurrentPinned) Color(0xFFD97706) else theme.keyTextColor
                                    )
                                }

                                // Save to Cloud
                                OutlinedButton(
                                    onClick = {
                                        triggerFeedback()
                                        scope.launch {
                                            supabase.addCloudClipboardItem(fullText, isPinned = isCurrentPinned)
                                            loadCloudClips()
                                            Toast.makeText(context, "Saved to Cloud Copypad! ☁️", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1.1f).height(34.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Cloud",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "Save Cloud", fontSize = 11.sp, color = Color(0xFF2563EB))
                                }
                            }

                            // Delete Clip
                            OutlinedButton(
                                onClick = {
                                    triggerFeedback()
                                    if (isDetailFromCloud && activeCloudItemId != null) {
                                        scope.launch {
                                            supabase.deleteCloudClipboardItem(activeCloudItemId!!)
                                            loadCloudClips()
                                        }
                                    } else {
                                        prefs.deleteClipboardItem(fullText)
                                        reloadLocalClips()
                                    }
                                    activeDetailClip = null
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEE2E2).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Delete", fontSize = 11.sp, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // EDIT CLIP DIALOG
        // -------------------------------------------------------------
        if (showEditDialog && editTargetClip != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { showEditDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.candidateBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Edit Clip", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.keyTextColor)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { showEditDialog = false }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = theme.functionTextColor, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.keyboardBg.copy(alpha = 0.6f))
                                .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            BasicTextField(
                                value = editTargetNewText,
                                onValueChange = { editTargetNewText = it },
                                textStyle = TextStyle(color = theme.keyTextColor, fontSize = 13.sp, lineHeight = 18.sp),
                                cursorBrush = SolidColor(theme.accentColor),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditDialog = false },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Cancel", fontSize = 11.sp, color = theme.functionTextColor)
                            }

                            Button(
                                onClick = {
                                    triggerFeedback()
                                    prefs.editClipboardItem(editTargetClip!!, editTargetNewText)
                                    reloadLocalClips()
                                    showEditDialog = false
                                },
                                modifier = Modifier.weight(1.2f).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.accentTextColor)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // ADD NEW NOTE / CLIP DIALOG
        // -------------------------------------------------------------
        if (showAddDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { showAddDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.candidateBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Add New Clip / Note", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.keyTextColor)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAddDialog = false }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = theme.functionTextColor, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.keyboardBg.copy(alpha = 0.6f))
                                .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            BasicTextField(
                                value = newClipInputText,
                                onValueChange = { newClipInputText = it },
                                textStyle = TextStyle(color = theme.keyTextColor, fontSize = 13.sp, lineHeight = 18.sp),
                                cursorBrush = SolidColor(theme.accentColor),
                                decorationBox = { inner ->
                                    if (newClipInputText.isEmpty()) {
                                        Text(
                                            text = "Type or paste snippet here (e.g. email, phone, quick note)...",
                                            color = theme.functionTextColor.copy(alpha = 0.5f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddDialog = false },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Cancel", fontSize = 11.sp, color = theme.functionTextColor)
                            }

                            Button(
                                onClick = {
                                    triggerFeedback()
                                    if (newClipInputText.isNotBlank()) {
                                        prefs.addClipboardItem(newClipInputText.trim())
                                        reloadLocalClips()
                                    }
                                    showAddDialog = false
                                },
                                enabled = newClipInputText.isNotBlank(),
                                modifier = Modifier.weight(1.2f).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Save Clip", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.accentTextColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabChip(
    label: String,
    isSelected: Boolean,
    theme: KeyboardThemePalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) theme.candidateBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) theme.accentColor else theme.functionTextColor
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LocalClipCard(
    text: String,
    isPinned: Boolean,
    theme: KeyboardThemePalette,
    isFlatGrid: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTogglePin: () -> Unit,
    onSaveToCloud: () -> Unit,
    onDelete: () -> Unit
) {
    val clipShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(clipShape)
            .background(
                if (isPinned) theme.candidateBg.copy(alpha = 0.95f)
                else theme.candidateBg
            )
            .then(
                if (isPinned) Modifier.border(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), clipShape)
                else if (!isFlatGrid && theme.keyBorderWidthDp > 0f) Modifier.border(0.8.dp, theme.keyBorderColor.copy(alpha = 0.35f), clipShape)
                else if (isFlatGrid) Modifier.border(1.dp, theme.gridBorderColor.copy(alpha = 0.4f), clipShape)
                else Modifier
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pin Toggle Icon
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Unpin" else "Pin",
                    tint = if (isPinned) Color(0xFFF59E0B) else theme.functionTextColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Clip Text snippet
            Text(
                text = text,
                fontSize = 12.sp,
                color = theme.keyTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Actions: Cloud + Delete
            IconButton(
                onClick = onSaveToCloud,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Save to Cloud",
                    tint = Color(0xFF2563EB).copy(alpha = 0.75f),
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = theme.functionTextColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CloudClipCard(
    item: CloudClipboardItem,
    theme: KeyboardThemePalette,
    isFlatGrid: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val clipShape = RoundedCornerShape(if (isFlatGrid) 0.dp else 8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(clipShape)
            .background(theme.candidateBg)
            .then(
                if (!isFlatGrid && theme.keyBorderWidthDp > 0f) Modifier.border(0.8.dp, theme.keyBorderColor.copy(alpha = 0.4f), clipShape)
                else if (isFlatGrid) Modifier.border(1.dp, theme.gridBorderColor.copy(alpha = 0.5f), clipShape)
                else Modifier
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(15.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = item.content,
                fontSize = 12.sp,
                color = theme.keyTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFFDBEAFE), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "Cloud",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = theme.functionTextColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
