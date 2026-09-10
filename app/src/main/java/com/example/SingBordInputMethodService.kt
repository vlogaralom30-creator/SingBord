package com.example

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
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

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        prefs = SingBordPreferences(this)
        settingsState.value = prefs.getSettings()
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
                                ic.deleteSurroundingText(1, 0)
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

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
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
}

