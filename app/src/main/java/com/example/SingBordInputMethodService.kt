package com.example

import android.content.Context
import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * SingBordInputMethodService
 *
 * Android InputMethodService implementation integrating SingBord keyboard
 * as a full system input method using Jetpack Compose.
 */
class SingBordInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var prefs: SingBordPreferences
    private val settingsState = mutableStateOf(KeyboardSettings())
    private val editorInfoState = mutableStateOf<EditorInfo?>(null)

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        settingsState.value = prefs.getSettings()
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        prefs = SingBordPreferences(this)
        settingsState.value = prefs.getSettings()

        val sharedPrefs = getSharedPreferences("singbord_settings", Context.MODE_PRIVATE)
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        val composeView = ComposeView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@SingBordInputMethodService))

            setViewTreeLifecycleOwner(this@SingBordInputMethodService)
            setViewTreeViewModelStoreOwner(this@SingBordInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@SingBordInputMethodService)

            setContent {
                SingBordKeyboardView(
                    settings = settingsState.value,
                    editorInfo = editorInfoState.value,
                    listener = object : KeyboardActionListener {
                        override fun onTextEntered(text: String) {
                            val ic = currentInputConnection ?: return
                            ic.commitText(text, 1)
                        }

                        override fun onDelete() {
                            val ic = currentInputConnection ?: return
                            val selectedText = ic.getSelectedText(0)
                            if (!selectedText.isNullOrEmpty()) {
                                ic.commitText("", 1)
                            } else {
                                deleteGraphemeClusterAware(ic)
                            }
                        }

                        override fun onEnter() {
                            val ic = currentInputConnection ?: return
                            val info = currentInputEditorInfo
                            val actionId = getEffectiveImeAction(info)

                            if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                                ic.performEditorAction(actionId)
                            } else {
                                ic.commitText("\n", 1)
                            }
                        }

                        override fun onSpace() {
                            val ic = currentInputConnection ?: return
                            ic.commitText(" ", 1)
                        }

                        override fun onDoubleSpacePeriod() {
                            val ic = currentInputConnection ?: return
                            ic.deleteSurroundingText(1, 0)
                            ic.commitText(". ", 1)
                        }

                        override fun onMoveCursor(direction: Int) {
                            if (direction < 0) {
                                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
                            } else if (direction > 0) {
                                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
                            }
                        }

                        override fun onWordSelected(word: String, prefixLength: Int) {
                            val ic = currentInputConnection ?: return
                            if (prefixLength > 0) {
                                ic.deleteSurroundingText(prefixLength, 0)
                            }
                            ic.commitText("$word ", 1)
                        }

                        override fun onCut() {
                            val ic = currentInputConnection ?: return
                            ic.performContextMenuAction(android.R.id.cut)
                        }

                        override fun onCopy() {
                            val ic = currentInputConnection ?: return
                            ic.performContextMenuAction(android.R.id.copy)
                        }

                        override fun onPaste() {
                            val ic = currentInputConnection ?: return
                            ic.performContextMenuAction(android.R.id.paste)
                        }

                        override fun onSelectAll() {
                            val ic = currentInputConnection ?: return
                            ic.performContextMenuAction(android.R.id.selectAll)
                        }

                        override fun onMoveCursorVertical(direction: Int) {
                            if (direction < 0) {
                                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP)
                            } else if (direction > 0) {
                                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
                            }
                        }

                        override fun onMoveToStart() {
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME)
                        }

                        override fun onMoveToEnd() {
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_END)
                        }

                        override fun onSelectText(direction: Int) {
                            val keyCode = when (direction) {
                                -1 -> KeyEvent.KEYCODE_DPAD_LEFT
                                1 -> KeyEvent.KEYCODE_DPAD_RIGHT
                                -2 -> KeyEvent.KEYCODE_DPAD_UP
                                2 -> KeyEvent.KEYCODE_DPAD_DOWN
                                else -> KeyEvent.KEYCODE_DPAD_LEFT
                            }
                            val ic = currentInputConnection ?: return
                            val now = System.currentTimeMillis()
                            val downShift = KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, KeyEvent.META_SHIFT_ON)
                            val downKey = KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, KeyEvent.META_SHIFT_ON)
                            val upKey = KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, KeyEvent.META_SHIFT_ON)
                            val upShift = KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0)
                            ic.sendKeyEvent(downShift)
                            ic.sendKeyEvent(downKey)
                            ic.sendKeyEvent(upKey)
                            ic.sendKeyEvent(upShift)
                        }

                        override fun onVoiceInput(languageCode: String?) {
                            try {
                                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    if (!languageCode.isNullOrBlank()) {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                                        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(languageCode))
                                    }
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore if no speech recognizer available
                            }
                        }

                        override fun onOpenSettings() {
                            try {
                                val intent = android.content.Intent(this@SingBordInputMethodService, MainActivity::class.java).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    }
                )
            }
        }
        return composeView
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Reload latest preferences and input metadata when keyboard is displayed
        settingsState.value = prefs.getSettings()
        editorInfoState.value = info
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onFinishInputView(finishingInput)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        settingsState.value = prefs.getSettings()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        try {
            val sharedPrefs = getSharedPreferences("singbord_settings", Context.MODE_PRIVATE)
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefChangeListener)
        } catch (e: Exception) {
            // Ignore
        }
        store.clear()
        super.onDestroy()
    }

    /**
     * Determines the effective IME action ID from EditorInfo actionId or imeOptions mask.
     */
    private fun getEffectiveImeAction(info: EditorInfo?): Int {
        if (info == null) return EditorInfo.IME_ACTION_NONE
        val actionId = info.actionId
        if (actionId != EditorInfo.IME_ACTION_UNSPECIFIED && actionId != EditorInfo.IME_ACTION_NONE) {
            return actionId
        }
        val optionsAction = info.imeOptions and EditorInfo.IME_MASK_ACTION
        if (optionsAction != EditorInfo.IME_ACTION_UNSPECIFIED && optionsAction != EditorInfo.IME_ACTION_NONE) {
            return optionsAction
        }
        return EditorInfo.IME_ACTION_NONE
    }

    /**
     * Intelligently removes a complete Bangla grapheme cluster / combining character sequence
     * (e.g. virama, vowel signs, phalas, candrabindu) instead of blindly deleting 1 code unit.
     */
    private fun deleteGraphemeClusterAware(ic: InputConnection) {
        val textBefore = ic.getTextBeforeCursor(16, 0)
        if (textBefore.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
            return
        }

        val str = textBefore.toString()
        val len = str.length
        val lastChar = str[len - 1]

        // Check if last character is a Bangla combining mark:
        // Combining vowel signs (া to ৌ): 0x09BE - 0x09CC
        // Virama (্): 0x09CD
        // Candrabindu (ঁ): 0x0981, Anusvara (ং): 0x0982, Visarga (ঃ): 0x0983
        // Hasanta/virama + preceding consonant
        val isCombiningMark = lastChar in '\u09BE'..'\u09CD' || lastChar in '\u0981'..'\u0983' || lastChar == '\u09D7'

        if (isCombiningMark) {
            // Delete the combining mark first
            ic.deleteSurroundingText(1, 0)
            return
        }

        // If the character before this one was a virama (্), e.g., ক + ্ + ত (conjunct)
        // deleting the current consonant should leave the virama or clean the sequence
        if (len >= 2 && str[len - 2] == '\u09CD') {
            // It's a conjunct ending. Delete 1 character
            ic.deleteSurroundingText(1, 0)
            return
        }

        // Standard 1-character deletion (handles surrogate pairs if needed)
        if (Character.isSurrogate(lastChar) && len >= 2 && Character.isSurrogatePair(str[len - 2], lastChar)) {
            ic.deleteSurroundingText(2, 0)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }
}

