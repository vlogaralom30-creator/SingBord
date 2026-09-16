package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppNotice
import com.example.data.CloudClipboardItem
import com.example.data.SupabaseBackendClient
import com.example.data.UserDictionaryRepository
import com.example.data.UserWord
import com.example.ui.theme.SingBordTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = SingBordPreferences(this)

        setContent {
            SingBordTheme {
                MainSettingsScreen(prefs = prefs)
            }
        }
    }
}

enum class MainNavTab(val title: String, val icon: ImageVector) {
    KEYBOARD("Keyboard", Icons.Default.Keyboard),
    THEMES("Themes", Icons.Default.Palette),
    SETTINGS("Settings", Icons.Default.Tune),
    ACCOUNT("Account & Sync", Icons.Default.CloudSync)
}

@Composable
fun MainSettingsScreen(prefs: SingBordPreferences) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MainNavTab.KEYBOARD) }

    // State for all preferences
    var themeId by remember { mutableStateOf(prefs.themeId) }
    var followSystemTheme by remember { mutableStateOf(prefs.followSystemTheme) }
    var lightThemeId by remember { mutableStateOf(prefs.lightThemeId) }
    var darkThemeId by remember { mutableStateOf(prefs.darkThemeId) }
    var lineThicknessDp by remember { mutableStateOf(prefs.lineThicknessDp) }
    var keyboardSize by remember { mutableStateOf(prefs.keyboardSize) }
    var showNumberRow by remember { mutableStateOf(prefs.showNumberRow) }
    var showSuggestions by remember { mutableStateOf(prefs.showSuggestions) }
    var enableWordLearning by remember { mutableStateOf(prefs.enableWordLearning) }
    var enableBanglish by remember { mutableStateOf(prefs.enableBanglish) }
    var enableGestures by remember { mutableStateOf(prefs.enableGestures) }
    var enableSpacebarCursor by remember { mutableStateOf(prefs.enableSpacebarCursor) }
    var enableVoiceTyping by remember { mutableStateOf(prefs.enableVoiceTyping) }
    var voiceLanguage by remember { mutableStateOf(prefs.voiceLanguage) }
    var showLanguageSwitchKey by remember { mutableStateOf(prefs.showLanguageSwitchKey) }
    var enableEmoji by remember { mutableStateOf(prefs.enableEmoji) }
    var enableHaptics by remember { mutableStateOf(prefs.enableHaptics) }
    var enableSound by remember { mutableStateOf(prefs.enableSound) }
    var enableKeyPopup by remember { mutableStateOf(prefs.enableKeyPopup) }
    var autoCapitalization by remember { mutableStateOf(prefs.autoCapitalization) }
    var enableKeyAnimation by remember { mutableStateOf(prefs.enableKeyAnimation) }
    var enableStylishFonts by remember { mutableStateOf(prefs.enableStylishFonts) }
    var activeStylishStyle by remember { mutableStateOf(prefs.activeStylishStyle) }
    var defaultLanguage by remember { mutableStateOf(prefs.defaultLanguage) }

    // User Dictionary Repository and Live Words
    val userRepo = remember { UserDictionaryRepository.getInstance(context) }
    val learnedWords by userRepo.getAllWordsFlow().collectAsState(initial = emptyList())

    // Privacy & Terms dialog states
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // State for test typing text
    var testTypingText by remember { mutableStateOf("") }

    // Activation states
    var isEnabled by remember { mutableStateOf(isKeyboardEnabled(context)) }
    var isSelected by remember { mutableStateOf(isKeyboardSelected(context)) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isEnabled = isKeyboardEnabled(context)
                isSelected = isKeyboardSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentSettings = KeyboardSettings(
        themeId = themeId,
        followSystemTheme = followSystemTheme,
        lightThemeId = lightThemeId,
        darkThemeId = darkThemeId,
        lineThicknessDp = lineThicknessDp,
        keyboardSize = keyboardSize,
        showNumberRow = showNumberRow,
        showSuggestions = showSuggestions,
        enableWordLearning = enableWordLearning,
        enableBanglish = enableBanglish,
        enableGestures = enableGestures,
        enableSpacebarCursor = enableSpacebarCursor,
        enableVoiceTyping = enableVoiceTyping,
        voiceLanguage = voiceLanguage,
        showLanguageSwitchKey = showLanguageSwitchKey,
        enableEmoji = enableEmoji,
        enableHaptics = enableHaptics,
        enableSound = enableSound,
        enableKeyPopup = enableKeyPopup,
        enableKeyAnimation = enableKeyAnimation,
        autoCapitalization = autoCapitalization,
        enableStylishFonts = enableStylishFonts,
        activeStylishStyle = activeStylishStyle,
        defaultLanguage = defaultLanguage
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.White,
                    contentColor = Color(0xFF2563EB),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = Color(0xFF2563EB),
                            height = 3.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                    }
                ) {
                    MainNavTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainNavTab.KEYBOARD -> {
                    KeyboardHomeTab(
                        context = context,
                        isEnabled = isEnabled,
                        isSelected = isSelected,
                        onUpdateActivation = {
                            isEnabled = isKeyboardEnabled(context)
                            isSelected = isKeyboardSelected(context)
                        },
                        testTypingText = testTypingText,
                        onTestTypingChange = { testTypingText = it },
                        currentSettings = currentSettings,
                        onSelectTab = { selectedTab = it },
                        onShowPrivacy = { showPrivacyDialog = true },
                        onShowTerms = { showTermsDialog = true }
                    )
                }
                MainNavTab.THEMES -> {
                    KeyboardThemesTab(
                        currentThemeId = themeId,
                        currentSettings = currentSettings,
                        onThemeSelected = { newThemeId ->
                            themeId = newThemeId
                            prefs.themeId = newThemeId
                        },
                        onFollowSystemThemeChange = {
                            followSystemTheme = it
                            prefs.followSystemTheme = it
                        },
                        onLightThemeSelected = {
                            lightThemeId = it
                            prefs.lightThemeId = it
                        },
                        onDarkThemeSelected = {
                            darkThemeId = it
                            prefs.darkThemeId = it
                        }
                    )
                }
                MainNavTab.SETTINGS -> {
                    KeyboardSettingsTab(
                        currentSettings = currentSettings,
                        prefs = prefs,
                        onFollowSystemThemeChange = {
                            followSystemTheme = it
                            prefs.followSystemTheme = it
                        },
                        onBanglishChange = {
                            enableBanglish = it
                            prefs.enableBanglish = it
                        },
                        onWordLearningChange = {
                            enableWordLearning = it
                            prefs.enableWordLearning = it
                        },
                        onGesturesChange = {
                            enableGestures = it
                            prefs.enableGestures = it
                        },
                        onSpacebarCursorChange = {
                            enableSpacebarCursor = it
                            prefs.enableSpacebarCursor = it
                        },
                        onVoiceTypingChange = {
                            enableVoiceTyping = it
                            prefs.enableVoiceTyping = it
                        },
                        onVoiceLanguageChange = {
                            voiceLanguage = it
                            prefs.voiceLanguage = it
                        },
                        onLanguageSwitchKeyChange = {
                            showLanguageSwitchKey = it
                            prefs.showLanguageSwitchKey = it
                        },
                        onEmojiChange = {
                            enableEmoji = it
                            prefs.enableEmoji = it
                        },
                        onKeyboardSizeChange = {
                            keyboardSize = it
                            prefs.keyboardSize = it
                        },
                        onLineThicknessChange = {
                            lineThicknessDp = it
                            prefs.lineThicknessDp = it
                        },
                        onNumberRowChange = {
                            showNumberRow = it
                            prefs.showNumberRow = it
                        },
                        onSuggestionsChange = {
                            showSuggestions = it
                            prefs.showSuggestions = it
                        },
                        onKeyPopupChange = {
                            enableKeyPopup = it
                            prefs.enableKeyPopup = it
                        },
                        onKeyAnimationChange = {
                            enableKeyAnimation = it
                            prefs.enableKeyAnimation = it
                        },
                        onAutoCapChange = {
                            autoCapitalization = it
                            prefs.autoCapitalization = it
                        },
                        onHapticsChange = {
                            enableHaptics = it
                            prefs.enableHaptics = it
                        },
                        onSoundChange = {
                            enableSound = it
                            prefs.enableSound = it
                        },
                        onStylishFontsChange = {
                            enableStylishFonts = it
                            prefs.enableStylishFonts = it
                        },
                        onActiveStyleChange = {
                            activeStylishStyle = it
                            prefs.activeStylishStyle = it
                        },
                        onDefaultLanguageChange = {
                            defaultLanguage = it
                            prefs.defaultLanguage = it
                        },
                        userRepo = userRepo,
                        learnedWords = learnedWords,
                        onShowPrivacy = { showPrivacyDialog = true },
                        onShowTerms = { showTermsDialog = true }
                    )
                }
                MainNavTab.ACCOUNT -> {
                    AccountHubTab(
                        context = context,
                        prefs = prefs,
                        userRepo = userRepo,
                        learnedWords = learnedWords,
                        onSettingsRestored = {
                            themeId = prefs.themeId
                            followSystemTheme = prefs.followSystemTheme
                            lightThemeId = prefs.lightThemeId
                            darkThemeId = prefs.darkThemeId
                            lineThicknessDp = prefs.lineThicknessDp
                            keyboardSize = prefs.keyboardSize
                            showNumberRow = prefs.showNumberRow
                            showSuggestions = prefs.showSuggestions
                            enableWordLearning = prefs.enableWordLearning
                            enableBanglish = prefs.enableBanglish
                            enableGestures = prefs.enableGestures
                            enableSpacebarCursor = prefs.enableSpacebarCursor
                            enableVoiceTyping = prefs.enableVoiceTyping
                            voiceLanguage = prefs.voiceLanguage
                            showLanguageSwitchKey = prefs.showLanguageSwitchKey
                            enableEmoji = prefs.enableEmoji
                            enableHaptics = prefs.enableHaptics
                            enableSound = prefs.enableSound
                            enableKeyPopup = prefs.enableKeyPopup
                            autoCapitalization = prefs.autoCapitalization
                            enableStylishFonts = prefs.enableStylishFonts
                            activeStylishStyle = prefs.activeStylishStyle
                            enableKeyAnimation = prefs.enableKeyAnimation
                            defaultLanguage = prefs.defaultLanguage
                        }
                    )
                }
            }
        }

        if (showPrivacyDialog) {
            PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
        }

        if (showTermsDialog) {
            TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
        }
    }
}

@Composable
fun KeyboardHomeTab(
    context: Context,
    isEnabled: Boolean,
    isSelected: Boolean,
    onUpdateActivation: () -> Unit,
    testTypingText: String,
    onTestTypingChange: (String) -> Unit,
    currentSettings: KeyboardSettings,
    onSelectTab: (MainNavTab) -> Unit,
    onShowPrivacy: () -> Unit,
    onShowTerms: () -> Unit
) {
    val activeTheme = remember(currentSettings.themeId) {
        KeyboardThemes.getTheme(currentSettings.themeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF2563EB), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "SingBord Logo",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "SingBord",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "v1.5.0",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Text(
                                text = "Zero-Gap 2D Minimalist Keyboard",
                                fontSize = 12.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "100% OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                Text(
                    text = "Pure speed, zero typing latency, and total privacy. No ads, no cloud sync, and zero keystroke logging.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShowPrivacy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Policy",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF16A34A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Privacy Policy", fontSize = 12.sp, color = Color(0xFF0F172A))
                    }

                    OutlinedButton(
                        onClick = onShowTerms,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terms",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("User Terms", fontSize = 12.sp, color = Color(0xFF0F172A))
                    }
                }
            }
        }

        // Setup & Activation Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Setup & Activation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                // Step 1: Enable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Enable SingBord",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isEnabled) "Enabled in System Settings" else "Turn on in System Settings",
                            fontSize = 12.sp,
                            color = if (isEnabled) Color(0xFF16A34A) else Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = {
                            openImeSettings(context)
                            onUpdateActivation()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEnabled) Color(0xFFDCFCE7) else Color(0xFF2563EB),
                            contentColor = if (isEnabled) Color(0xFF15803D) else Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = if (isEnabled) "Active ✓" else "Enable")
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Step 2: Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. Select Active Keyboard",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isSelected) "SingBord is default" else "Set SingBord as current input method",
                            fontSize = 12.sp,
                            color = if (isSelected) Color(0xFF16A34A) else Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = {
                            showImePicker(context)
                            onUpdateActivation()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFFDCFCE7) else Color(0xFF2563EB),
                            contentColor = if (isSelected) Color(0xFF15803D) else Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = if (isSelected) "Selected ✓" else "Select")
                    }
                }
            }
        }

        // Live Test Typing Box
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Interactive Test Typing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = testTypingText,
                    onValueChange = onTestTypingChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tap here to test SingBord typing...") },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                if (testTypingText.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onTestTypingChange("") }) {
                            Text("Clear Text", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Overview Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Quick Badge
            FlatCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTab(MainNavTab.THEMES) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(activeTheme.accentColor)
                                .border(1.dp, activeTheme.gridBorderColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = "Active Theme",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = activeTheme.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                    Text(
                        text = "Change ›",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }

        // Live 2D Keyboard Preview
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time 2D Keyboard Preview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = activeTheme.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Live preview of your zero-gap line-separated keyboard:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                SingBordKeyboardView(
                    settings = currentSettings,
                    listener = null
                )
            }
        }

        // Branding & Credits Footer
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "100% Secure • Free • No Ads • Offline",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Text(
                    text = "SingBord is designed specifically for Android devices, ensuring ultimate privacy and effortless, fast typing.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Brand: Naxxivo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Creator: Rony Ahammad",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WebsiteChip(label = "naxxivo.xyz", url = "https://naxxivo.xyz", context = context)
                        WebsiteChip(label = "naxxivo.online", url = "https://naxxivo.online", context = context)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun KeyboardThemesTab(
    currentThemeId: String,
    currentSettings: KeyboardSettings,
    onThemeSelected: (String) -> Unit,
    onFollowSystemThemeChange: (Boolean) -> Unit,
    onLightThemeSelected: (String) -> Unit,
    onDarkThemeSelected: (String) -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val activeTheme = remember(
        currentSettings.followSystemTheme,
        currentSettings.themeId,
        currentSettings.lightThemeId,
        currentSettings.darkThemeId,
        isSystemDark
    ) {
        KeyboardThemes.resolveTheme(currentSettings, isSystemDark)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Theme Templates",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${KeyboardThemes.ALL_THEMES.size} Handcrafted Styles • 60fps Zero-Lag",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Text(
                            text = activeTheme.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Themes use static immutable color palettes to prevent memory allocations, guaranteeing ultra-fast render speed without any typing lag.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp
                )
            }
        }

        // Auto Dark/Light Theme Switching Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Auto Switch with System Theme",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Switch light/dark themes according to OS system theme",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Switch(
                        checked = currentSettings.followSystemTheme,
                        onCheckedChange = onFollowSystemThemeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB)
                        )
                    )
                }

                // System status badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSystemDark) Color(0xFF0F172A) else Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSystemDark) "🌙 Android System is currently Dark" else "☀️ Android System is currently Light",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemDark) Color(0xFFE2E8F0) else Color(0xFF92400E)
                    )
                    Text(
                        text = "Active: ${activeTheme.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemDark) Color(0xFF93C5FD) else Color(0xFFB45309)
                    )
                }

                if (currentSettings.followSystemTheme) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Light theme target
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "☀️ Preferred Light Theme (Day / Light Mode)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KeyboardThemes.LIGHT_THEMES.forEach { lightT ->
                                val isTarget = lightT.id == currentSettings.lightThemeId
                                FilterChip(
                                    selected = isTarget,
                                    onClick = { onLightThemeSelected(lightT.id) },
                                    label = {
                                        Text(
                                            text = lightT.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (isTarget) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Dark theme target
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🌙 Preferred Dark Theme (Night / Dark Mode)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(KeyboardThemes.DARK_THEMES) { darkT ->
                                val isTarget = darkT.id == currentSettings.darkThemeId
                                FilterChip(
                                    selected = isTarget,
                                    onClick = { onDarkThemeSelected(darkT.id) },
                                    label = {
                                        Text(
                                            text = darkT.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (isTarget) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Keyboard Preview under Selected Theme
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Theme Preview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = activeTheme.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }

                SingBordKeyboardView(
                    settings = currentSettings.copy(
                        themeId = activeTheme.id,
                        followSystemTheme = false
                    ),
                    listener = null
                )
            }
        }

        // Section Title
        Text(
            text = "All Theme Templates (${KeyboardThemes.ALL_THEMES.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        // List of all theme cards
        KeyboardThemes.ALL_THEMES.forEach { theme ->
            val isSelected = if (currentSettings.followSystemTheme) {
                if (theme.isDark) theme.id == currentSettings.darkThemeId else theme.id == currentSettings.lightThemeId
            } else {
                theme.id == currentThemeId
            }
            ThemeTemplateCard(
                theme = theme,
                isSelected = isSelected,
                isAutoMode = currentSettings.followSystemTheme,
                onSelect = {
                    if (currentSettings.followSystemTheme) {
                        if (theme.isDark) {
                            onDarkThemeSelected(theme.id)
                        } else {
                            onLightThemeSelected(theme.id)
                        }
                    } else {
                        onThemeSelected(theme.id)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ThemeTemplateCard(
    theme: KeyboardThemePalette,
    isSelected: Boolean,
    isAutoMode: Boolean = false,
    onSelect: () -> Unit
) {
    FlatCard {
        Column(
            modifier = Modifier
                .clickable { onSelect() }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mini Keyboard Swatch preview
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.keyboardBg)
                            .border(1.5.dp, theme.gridBorderColor, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(theme.keyBg)
                                .border(1.dp, theme.accentColor, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.keyTextColor
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = theme.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (theme.isDark) Color(0xFF1E293B) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (theme.isDark) "🌙 DARK" else "☀️ LIGHT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (theme.isDark) Color.White else Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = theme.subtitle,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFDCFCE7) else Color(0xFF2563EB),
                        contentColor = if (isSelected) Color(0xFF15803D) else Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSelected) {
                            if (isAutoMode) "Selected Target ✓" else "Applied ✓"
                        } else {
                            if (isAutoMode) "Set as ${if (theme.isDark) "Dark" else "Light"}" else "Apply"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Swatch dots row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Palette:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                ColorDot(label = "Base", color = theme.keyboardBg)
                ColorDot(label = "Key", color = theme.keyBg)
                ColorDot(label = "Function", color = theme.functionKeyBg)
                ColorDot(label = "Accent", color = theme.accentColor)
            }
        }
    }
}

@Composable
fun ColorDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
        )
        Text(text = label, fontSize = 10.sp, color = Color(0xFF475569))
    }
}

@Composable
fun KeyboardSettingsTab(
    currentSettings: KeyboardSettings,
    prefs: SingBordPreferences,
    onFollowSystemThemeChange: (Boolean) -> Unit,
    onBanglishChange: (Boolean) -> Unit,
    onWordLearningChange: (Boolean) -> Unit,
    onGesturesChange: (Boolean) -> Unit,
    onSpacebarCursorChange: (Boolean) -> Unit,
    onVoiceTypingChange: (Boolean) -> Unit,
    onVoiceLanguageChange: (String) -> Unit,
    onLanguageSwitchKeyChange: (Boolean) -> Unit,
    onEmojiChange: (Boolean) -> Unit,
    onKeyboardSizeChange: (KeyboardSize) -> Unit,
    onLineThicknessChange: (Float) -> Unit,
    onNumberRowChange: (Boolean) -> Unit,
    onSuggestionsChange: (Boolean) -> Unit,
    onKeyPopupChange: (Boolean) -> Unit,
    onKeyAnimationChange: (Boolean) -> Unit,
    onAutoCapChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onStylishFontsChange: (Boolean) -> Unit,
    onActiveStyleChange: (String) -> Unit,
    onDefaultLanguageChange: (KeyboardLanguage) -> Unit,
    userRepo: UserDictionaryRepository,
    learnedWords: List<UserWord>,
    onShowPrivacy: () -> Unit,
    onShowTerms: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 0: Default Keyboard Language Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Default Keyboard Layout (ডিফল্ট কীবোর্ড ভাষা)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Set primary startup typing script when keyboard opens",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                val availableLanguages = listOf(
                    Triple(KeyboardLanguage.ENGLISH, "English (QWERTY)", "Standard English layout with word suggestions"),
                    Triple(KeyboardLanguage.BANGLA_PROBHAT, "বাংলা প্রভাত (Bangla Probhat)", "Complete Probhat layout with Bengali letters & alternates"),
                    Triple(KeyboardLanguage.AVRO, "বাংলা অভ্রো (Bangla Avro)", "English phonetic typing converting to Bangla in real-time")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableLanguages.forEach { (lang, title, desc) ->
                        val isSelected = currentSettings.defaultLanguage == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDefaultLanguageChange(lang) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onDefaultLanguageChange(lang) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF2563EB)
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF0F172A)
                                )
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Core Features On/Off Hub
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Core Features Control (কোর ফিচার নিয়ন্ত্রণ)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Toggle all major features on or off in a single click",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 1: Stylish Fonts
                ToggleOptionRow(
                    title = "1. Stylish Fonts (স্টাইলিশ ইউনিকোড ফন্টস)",
                    description = "Write in Cursive, Bold Script, Double Struck, Slashed (q̷w̷e̷) anywhere",
                    checked = currentSettings.enableStylishFonts,
                    onCheckedChange = onStylishFontsChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 2: Banglish
                ToggleOptionRow(
                    title = "2. Banglish Vocabulary (বাংলিশ শব্দকোষ)",
                    description = "Phonetic Banglish dictionary suggestions (ami, tumi, kemon, etc.)",
                    checked = currentSettings.enableBanglish,
                    onCheckedChange = onBanglishChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 3: Smart Word Learning
                ToggleOptionRow(
                    title = "3. Smart Word Learning (স্মার্ট ডিকশনারি লার্নিং)",
                    description = "Memorize your frequently typed words offline and rank higher",
                    checked = currentSettings.enableWordLearning,
                    onCheckedChange = onWordLearningChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 4: Spacebar Cursor Gestures
                ToggleOptionRow(
                    title = "4. Spacebar Cursor Slide (স্পেসবার স্লাইড কার্সার অন/অফ)",
                    description = "Slide finger left or right on spacebar to accurately move cursor",
                    checked = currentSettings.enableSpacebarCursor,
                    onCheckedChange = onSpacebarCursorChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 5: Voice Typing / Speech Button
                ToggleOptionRow(
                    title = "5. Speech / Voice Typing (ভয়েস টাইপিং ও স্পিচ বাটন অন/অফ)",
                    description = "Microphone button to speak and convert Bangla or English speech into text",
                    checked = currentSettings.enableVoiceTyping,
                    onCheckedChange = onVoiceTypingChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 6: Bottom Language Switch Key
                ToggleOptionRow(
                    title = "6. Bottom Language Key (কীবোর্ডের নিচে ভাষা পরিবর্তন বাটন)",
                    description = "Dedicated 🌐 language icon at bottom row for fast switching",
                    checked = currentSettings.showLanguageSwitchKey,
                    onCheckedChange = onLanguageSwitchKeyChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Feature 7: Emoji Keyboard
                ToggleOptionRow(
                    title = "7. Emoji Keyboard & Key (ইমোজি কীবোর্ড ও বাটন)",
                    description = "Dedicated 😊 emoji key and 8 offline emoji category grids",
                    checked = currentSettings.enableEmoji,
                    onCheckedChange = onEmojiChange
                )
            }
        }

        // Section: Speech / Voice Typing Language Selection Hub
        if (currentSettings.enableVoiceTyping) {
            FlatCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Speech Input Language (স্পিচ বাটন ভাষা পরিবর্তন)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Select default recognition language for microphone input",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    val voiceLanguages = listOf(
                        Triple("auto", "Auto (অটো সিঙ্ক)", "Syncs automatically with keyboard language (Bangla / English)"),
                        Triple("bn-BD", "বাংলা (Bangladesh)", "বাংলা ভাষায় ভয়েস ইনপুট ও টাইপিং"),
                        Triple("en-US", "English (United States)", "English language speech-to-text recognition")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        voiceLanguages.forEach { (langCode, title, desc) ->
                            val isSelected = currentSettings.voiceLanguage == langCode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onVoiceLanguageChange(langCode) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onVoiceLanguageChange(langCode) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF2563EB)
                                    )
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Stylish Font Styles Gallery & Active Picker
        if (currentSettings.enableStylishFonts) {
            FlatCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "𝓐",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                        Column {
                            Text(
                                text = "Active Stylish Font (ডিফল্ট ফন্ট নির্বাচন)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Keyboard-এ [ 𝓐 ] বাটনে ট্যাপ করেও সরাসরি ফন্ট বদলানো যাবে",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StylishFontEngine.ALL_STYLES.forEach { style ->
                            val isSelected = currentSettings.activeStylishStyle.equals(style.id, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onActiveStyleChange(style.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = style.label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = style.preview,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Keyboard Sizing Adjustment
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Keyboard Height Adjustment (সাইজ নির্ধারণ)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Text(
                    text = "Key height multiplier (${currentSettings.keyboardSize.label})",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KeyboardSize.entries.forEach { sizeOption ->
                        val isSelectedOption = currentSettings.keyboardSize == sizeOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onKeyboardSizeChange(sizeOption) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sizeOption.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectedOption) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Line Thickness Adjustment
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Line Thickness Adjustment (গ্রিড লাইনের পুরুত্ব)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Width of thin lines separating buttons (${currentSettings.lineThicknessDp}dp)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1.0f to "1px",
                        1.5f to "1.5px",
                        2.0f to "2px",
                        3.0f to "3px"
                    ).forEach { (value, label) ->
                        val isSelectedOption = currentSettings.lineThicknessDp == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onLineThicknessChange(value) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectedOption) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Layout & Visual Toggles
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Layout & Visual Preferences",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                // Auto System Dark/Light Theme Switching
                ToggleOptionRow(
                    title = "Auto-Switch Theme with System",
                    description = "Follow Android system global Dark and Light mode automatically",
                    checked = currentSettings.followSystemTheme,
                    onCheckedChange = onFollowSystemThemeChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Number Row
                ToggleOptionRow(
                    title = "Top Number Row",
                    description = "Show dedicated 1-0 keys above letter keys",
                    checked = currentSettings.showNumberRow,
                    onCheckedChange = onNumberRowChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Word Suggestion Bar
                ToggleOptionRow(
                    title = "Word Suggestion Strip",
                    description = "Display real-time offline candidate word bar",
                    checked = currentSettings.showSuggestions,
                    onCheckedChange = onSuggestionsChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Character Popup Preview
                ToggleOptionRow(
                    title = "Character Preview Popup",
                    description = "Show visual character badge when pressing keys",
                    checked = currentSettings.enableKeyPopup,
                    onCheckedChange = onKeyPopupChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Key Press Animations
                ToggleOptionRow(
                    title = "Key Animation Effects",
                    description = "Tactile spring scale & radial ripple press animation on key touch",
                    checked = currentSettings.enableKeyAnimation,
                    onCheckedChange = onKeyAnimationChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Auto-Capitalization
                ToggleOptionRow(
                    title = "Auto-Capitalization",
                    description = "Automatically capitalize first letter of sentences",
                    checked = currentSettings.autoCapitalization,
                    onCheckedChange = onAutoCapChange
                )
            }
        }

        // Section 5: Haptic & Audio Feedback
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Haptic & Audio Feedback",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                ToggleOptionRow(
                    title = "Haptic Vibration Feedback",
                    description = "Vibrate lightly on keypress",
                    checked = currentSettings.enableHaptics,
                    onCheckedChange = onHapticsChange
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                ToggleOptionRow(
                    title = "Key Click Sound",
                    description = "Play subtle audio click on tap",
                    checked = currentSettings.enableSound,
                    onCheckedChange = onSoundChange
                )
            }
        }

        // Section 6: Banglish & Custom Learned Dictionary Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Learned Words & Dictionary Manager",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = "${learnedWords.size} words",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "SingBord remembers words you type frequently and prioritizes them in suggestions. Data is securely stored only on this device.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )

                // Add Custom Word Row
                var newWordInput by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newWordInput,
                        onValueChange = { input ->
                            newWordInput = input.filter { it.isLetter() }
                        },
                        placeholder = { Text("Add custom word (e.g. kemon, valo)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Button(
                        onClick = {
                            if (newWordInput.trim().length >= 2) {
                                userRepo.addCustomWord(newWordInput.trim(), frequency = 3)
                                newWordInput = ""
                            }
                        },
                        enabled = newWordInput.trim().length >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Learned words chips
                if (learnedWords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No custom words memorized yet. Type with SingBord or add your favorite words above!",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Top Memorized Words (by usage frequency):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(learnedWords.take(40)) { userWord ->
                                LearnedWordChip(
                                    word = userWord.word,
                                    frequency = userWord.frequency,
                                    onDelete = { userRepo.deleteWord(userWord.word) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { userRepo.clearAll() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Clear All Words", fontSize = 11.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Legal Dialog Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShowPrivacy,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onShowTerms,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("User Agreement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFE2E8F0),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.5.dp
    ) {
        content()
    }
}

@Composable
fun ToggleOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB),
                uncheckedThumbColor = Color(0xFF64748B),
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
fun WebsiteChip(
    label: String,
    url: String,
    context: Context
) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(4.dp))
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Ignore if no web browser available
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = label,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D4ED8)
            )
        }
    }
}

fun isKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val enabledMethods = imm.enabledInputMethodList
    return enabledMethods.any { it.packageName == context.packageName }
}

fun isKeyboardSelected(context: Context): Boolean {
    val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    return currentIme?.contains(context.packageName) == true
}

fun openImeSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    } catch (e: Exception) {
        // Fallback
    }
}

fun showImePicker(context: Context) {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    } catch (e: Exception) {
        // Fallback
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF16A34A)
                )
                Text("Privacy Policy", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "100% Offline & Private Commitment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "• Zero Internet Access: SingBord does not declare INTERNET permission. It is physically impossible to send data anywhere.\n\n" +
                            "• Zero Data Collection: We do not log, record, store, or transmit your keystrokes, passwords, or personal messages.\n\n" +
                            "• No Trackers or Ads: No analytics SDKs or advertising networks are bundled.\n\n" +
                            "• Local Preferences: Your settings (height, border thickness, sound, number row, themes) are saved strictly on this device.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
        }
    )
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF2563EB)
                )
                Text("User Agreement", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Open Source License & Terms",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "• Open Source: SingBord is open-source software provided under the Apache 2.0 license.\n\n" +
                            "• Safe Usage: You are free to use SingBord on any compatible Android device for personal, work, or educational needs.\n\n" +
                            "• As-Is Software: Provided without warranties of any kind. Developers assume no liability for third-party device compatibility.\n\n" +
                            "• Creator: Developed by Rony Ahammad (Naxxivo Tech Lab).",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("I Agree", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
        }
    )
}

@Composable
fun LearnedWordChip(
    word: String,
    frequency: Int,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = word,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF2563EB)
            ) {
                Text(
                    text = "${frequency}x",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete word",
                tint = Color(0xFF94A3B8),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun AccountHubTab(
    context: Context,
    prefs: SingBordPreferences,
    userRepo: UserDictionaryRepository,
    learnedWords: List<UserWord>,
    onSettingsRestored: () -> Unit
) {
    val supabase = remember { SupabaseBackendClient.getInstance(context) }
    val scope = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(supabase.isLoggedIn) }
    var userEmail by remember { mutableStateOf(supabase.currentUserEmail) }
    var userId by remember { mutableStateOf(supabase.currentUserId) }

    // Auth Form State
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var authLoading by remember { mutableStateOf(false) }
    var authMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // isSuccess to text

    // Sync States
    var syncLoading by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("") }

    // Quick Notes & Copypad State
    var cloudClips by remember { mutableStateOf<List<CloudClipboardItem>>(emptyList()) }
    var cloudClipsLoading by remember { mutableStateOf(false) }
    var newClipText by remember { mutableStateOf("") }

    // Online Dict State
    var onlineDictEnabled by remember { mutableStateOf(prefs.enableOnlineDictionary) }
    var testQuery by remember { mutableStateOf("") }
    var testResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var testSearching by remember { mutableStateOf(false) }

    // Notices State
    var notices by remember { mutableStateOf<List<AppNotice>>(emptyList()) }
    var noticesLoading by remember { mutableStateOf(false) }

    // Load initial data
    LaunchedEffect(Unit) {
        cloudClipsLoading = true
        val clipsResult = supabase.fetchCloudClipboard()
        cloudClips = clipsResult.getOrDefault(emptyList())
        cloudClipsLoading = false

        noticesLoading = true
        val noticesResult = supabase.fetchNotices()
        notices = noticesResult.getOrDefault(emptyList())
        noticesLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Account & Authentication Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFEDE9FE), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SingBord Account & Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Backup your preferences, quick notes & words",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    if (isLoggedIn) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Connected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                }

                if (isLoggedIn) {
                    // Logged In Profile Box
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF7C3AED), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (userEmail.firstOrNull() ?: 'U').uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userEmail,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Account Active • Encrypted Sync",
                                        fontSize = 11.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            supabase.signOut()
                            isLoggedIn = false
                            userEmail = ""
                            userId = ""
                            authMessage = Pair(true, "Successfully signed out")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Segmented Switch Tab for Sign In / Sign Up
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isRegisterMode = false
                                        authMessage = null
                                    },
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isRegisterMode) Color.White else Color.Transparent,
                                shadowElevation = if (!isRegisterMode) 1.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sign In",
                                        fontSize = 13.sp,
                                        fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isRegisterMode) Color(0xFF0F172A) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isRegisterMode = true
                                        authMessage = null
                                    },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isRegisterMode) Color.White else Color.Transparent,
                                shadowElevation = if (isRegisterMode) 1.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Create Account",
                                        fontSize = 13.sp,
                                        fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isRegisterMode) Color(0xFF0F172A) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("user@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (authMessage != null) {
                        Surface(
                            color = if (authMessage!!.first) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (authMessage!!.first) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authMessage!!.second,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (authMessage!!.first) Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                authMessage = Pair(false, "Please enter both email and password")
                                return@Button
                            }
                            authLoading = true
                            authMessage = null
                            scope.launch {
                                val res = if (isRegisterMode) {
                                    supabase.signUp(emailInput, passwordInput)
                                } else {
                                    supabase.signIn(emailInput, passwordInput)
                                }
                                authLoading = false
                                res.onSuccess { user ->
                                    isLoggedIn = true
                                    userEmail = user.email
                                    userId = user.id
                                    authMessage = Pair(true, if (isRegisterMode) "Account created & ready!" else "Welcome back! Signed in.")
                                    
                                    // Auto restore settings on login if available
                                    val restoreRes = supabase.fetchSettingsFromCloud()
                                    restoreRes.onSuccess { jsonStr ->
                                        if (prefs.applySettingsFromJson(jsonStr)) {
                                            onSettingsRestored()
                                        }
                                    }
                                }.onFailure { err ->
                                    authMessage = Pair(false, err.message ?: "Authentication failed")
                                }
                            }
                        },
                        enabled = !authLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (isRegisterMode) "Create Account" else "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Settings Cloud Backup & Auto Restore Card
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Safely backup themes, layout & preferences",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                if (syncStatusText.isNotBlank()) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = syncStatusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            syncLoading = true
                            syncStatusText = "Backing up settings..."
                            scope.launch {
                                val jsonStr = prefs.exportSettingsToJson()
                                val res = supabase.backupSettingsToCloud(jsonStr)
                                syncLoading = false
                                res.onSuccess {
                                    syncStatusText = "✓ Settings successfully backed up!"
                                }.onFailure { err ->
                                    syncStatusText = "Backup notice: ${err.message}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            syncLoading = true
                            syncStatusText = "Restoring settings..."
                            scope.launch {
                                val res = supabase.fetchSettingsFromCloud()
                                syncLoading = false
                                res.onSuccess { jsonStr ->
                                    if (prefs.applySettingsFromJson(jsonStr)) {
                                        onSettingsRestored()
                                        syncStatusText = "✓ Settings successfully restored & applied!"
                                    } else {
                                        syncStatusText = "Failed to apply restored settings"
                                    }
                                }.onFailure { err ->
                                    syncStatusText = "Restore notice: ${err.message}"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Quick Notes & Copypad
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quick Notes & Copypad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Save permanent snippets & notes for instant pasting",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                cloudClipsLoading = true
                                val res = supabase.fetchCloudClipboard()
                                cloudClips = res.getOrDefault(emptyList())
                                cloudClipsLoading = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh clips",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Add New Clip Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newClipText,
                        onValueChange = { newClipText = it },
                        placeholder = { Text("Add snippet or note...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = {
                            if (newClipText.isNotBlank()) {
                                val txt = newClipText.trim()
                                newClipText = ""
                                scope.launch {
                                    supabase.addCloudClipboardItem(txt)
                                    val res = supabase.fetchCloudClipboard()
                                    cloudClips = res.getOrDefault(emptyList())
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD97706),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }

                if (cloudClipsLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (cloudClips.isEmpty()) {
                    Text(
                        text = "No saved notes yet. Add your favorite snippets above to access them anytime.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cloudClips.take(8).forEach { item ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.content,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("SingBord", item.content))
                                            prefs.addClipboardItem(item.content)
                                            syncStatusText = "✓ Copied to clipboard!"
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                supabase.deleteCloudClipboardItem(item.id)
                                                val res = supabase.fetchCloudClipboard()
                                                cloudClips = res.getOrDefault(emptyList())
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Personal Vocabulary Sync
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFECFDF5), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Personal Vocabulary Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Sync custom words to keep predictions fast on all devices",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Learned Words: ${learnedWords.size} custom words saved locally",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                syncLoading = true
                                val wordsList = learnedWords.map { it.word }
                                val res = supabase.syncUserWordsToCloud(wordsList)
                                syncLoading = false
                                res.onSuccess { count ->
                                    syncStatusText = "✓ Synced $count vocabulary words!"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Words", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                syncLoading = true
                                val res = supabase.fetchCloudUserWords()
                                syncLoading = false
                                res.onSuccess { cloudWords ->
                                    cloudWords.forEach { word ->
                                        userRepo.addCustomWord(word, 5)
                                    }
                                    syncStatusText = "✓ Imported ${cloudWords.size} words!"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Words", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Smart Live Word Predictions
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Live Word Predictions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Intelligent vocabulary suggestions while typing",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = onlineDictEnabled,
                        onCheckedChange = {
                            onlineDictEnabled = it
                            prefs.enableOnlineDictionary = it
                        }
                    )
                }

                // Test live suggestion input
                OutlinedTextField(
                    value = testQuery,
                    onValueChange = { q ->
                        testQuery = q
                        if (q.length >= 2 && onlineDictEnabled) {
                            testSearching = true
                            scope.launch {
                                val results = supabase.fetchOnlineSuggestions(q)
                                testResults = results
                                testSearching = false
                            }
                        } else {
                            testResults = emptyList()
                        }
                    },
                    placeholder = { Text("Test word prediction (e.g. 'বাংলা', 'prog')...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (testResults.isNotEmpty()) {
                    Text(
                        text = "Suggestions for '$testQuery':",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        testResults.forEach { sug ->
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                            ) {
                                Text(
                                    text = sug,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Official Announcements & Updates
        FlatCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFFEDD5), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Updates & Announcements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Latest features, updates & official notices",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                noticesLoading = true
                                val res = supabase.fetchNotices()
                                notices = res.getOrDefault(emptyList())
                                noticesLoading = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh notices",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (noticesLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        notices.forEach { notice ->
                            val tagBg = when (notice.tag) {
                                "New Feature" -> Color(0xFFEDE9FE)
                                "Bug Fix" -> Color(0xFFDCFCE7)
                                else -> Color(0xFFDBEAFE)
                            }
                            val tagColor = when (notice.tag) {
                                "New Feature" -> Color(0xFF7C3AED)
                                "Bug Fix" -> Color(0xFF16A34A)
                                else -> Color(0xFF2563EB)
                            }

                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = tagBg,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = notice.tag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tagColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = notice.version,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = notice.date,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Text(
                                        text = notice.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )

                                    Text(
                                        text = notice.content,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

