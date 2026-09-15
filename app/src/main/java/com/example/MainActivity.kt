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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SingBordTheme

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

@Composable
fun MainSettingsScreen(prefs: SingBordPreferences) {
    val context = LocalContext.current

    // State for preferences
    var lineThicknessDp by remember { mutableStateOf(prefs.lineThicknessDp) }
    var keyboardSize by remember { mutableStateOf(prefs.keyboardSize) }
    var showNumberRow by remember { mutableStateOf(prefs.showNumberRow) }
    var enableHaptics by remember { mutableStateOf(prefs.enableHaptics) }
    var enableSound by remember { mutableStateOf(prefs.enableSound) }
    var enableKeyPopup by remember { mutableStateOf(prefs.enableKeyPopup) }
    var autoCapitalization by remember { mutableStateOf(prefs.autoCapitalization) }

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
        lineThicknessDp = lineThicknessDp,
        keyboardSize = keyboardSize,
        showNumberRow = showNumberRow,
        enableHaptics = enableHaptics,
        enableSound = enableSound,
        enableKeyPopup = enableKeyPopup,
        autoCapitalization = autoCapitalization
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFAFA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Modernized App Open Hero Section
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
                            // Unique 2D Flat App Icon Badge
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
                                            text = "v1.0.0",
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

                    // Quick Action Badges for Privacy & User Agreement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPrivacyDialog = true },
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
                            onClick = { showTermsDialog = true },
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

            // Keyboard Activation Card
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
                                isEnabled = isKeyboardEnabled(context)
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
                                isSelected = isKeyboardSelected(context)
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
                        onValueChange = { testTypingText = it },
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
                            TextButton(onClick = { testTypingText = "" }) {
                                Text("Clear Text", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Settings Section Header
            Text(
                text = "Keyboard Customization (All Options)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            // Line Thickness Adjustment
            FlatCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Line Thickness Adjustment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Width of thin lines separating buttons (${lineThicknessDp}dp)",
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
                            val isSelectedOption = lineThicknessDp == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        lineThicknessDp = value
                                        prefs.lineThicknessDp = value
                                    }
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

            // Keyboard Size Adjustment
            FlatCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Keyboard Size Adjustment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Key height multiplier (${keyboardSize.label})",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KeyboardSize.entries.forEach { sizeOption ->
                            val isSelectedOption = keyboardSize == sizeOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelectedOption) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        keyboardSize = sizeOption
                                        prefs.keyboardSize = sizeOption
                                    }
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

            // More Options (Toggles)
            FlatCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Keyboard Options",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // Number Row Toggle
                    ToggleOptionRow(
                        title = "Top Number Row",
                        description = "Show dedicated 1-0 keys above letter keys",
                        checked = showNumberRow,
                        onCheckedChange = {
                            showNumberRow = it
                            prefs.showNumberRow = it
                        }
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Haptics Toggle
                    ToggleOptionRow(
                        title = "Haptic Vibration Feedback",
                        description = "Vibrate lightly on keypress",
                        checked = enableHaptics,
                        onCheckedChange = {
                            enableHaptics = it
                            prefs.enableHaptics = it
                        }
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Sound Toggle
                    ToggleOptionRow(
                        title = "Key Click Sound",
                        description = "Play subtle audio click on tap",
                        checked = enableSound,
                        onCheckedChange = {
                            enableSound = it
                            prefs.enableSound = it
                        }
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Key Popup Toggle
                    ToggleOptionRow(
                        title = "Character Preview Popup",
                        description = "Show visual character badge when pressing keys",
                        checked = enableKeyPopup,
                        onCheckedChange = {
                            enableKeyPopup = it
                            prefs.enableKeyPopup = it
                        }
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Auto-Capitalization Toggle
                    ToggleOptionRow(
                        title = "Auto-Capitalization",
                        description = "Automatically capitalize first letter of sentences",
                        checked = autoCapitalization,
                        onCheckedChange = {
                            autoCapitalization = it
                            prefs.autoCapitalization = it
                        }
                    )
                }
            }

            // Live 2D Layout Preview
            FlatCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Real-Time 2D Keyboard Preview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
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

            // Privacy & Legal Trust Card
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
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privacy & Trust",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Privacy & Legal Trust",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = "SingBord does not request INTERNET permissions. It is architecturally impossible for your keystrokes, passwords, or personal chats to leave this phone.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPrivacyDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("View Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showTermsDialog = true },
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
                            text = "100% Secure • Free • No Ads • English Only",
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

        if (showPrivacyDialog) {
            PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
        }

        if (showTermsDialog) {
            TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
        }
    }
}

@Composable
fun FlatCard(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFE2E8F0), shape = RoundedCornerShape(6.dp))
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
                            "• Local Preferences: Your settings (height, border thickness, sound, number row) are saved strictly on this device.",
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
