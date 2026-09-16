package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * SingBordVoiceInputManager
 *
 * Dedicated speech recognition controller for SingBord Input Method.
 * Uses Android SpeechRecognizer API directly inside the keyboard with live audio level
 * visualizer, partial recognition streaming, Bangla/English enhancement, and robust error handling.
 */
class SingBordVoiceInputManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onStateChanged: (VoiceState) -> Unit,
    private val onRmsLevelChanged: (Float) -> Unit
) {
    sealed class VoiceState {
        object Idle : VoiceState()
        object Preparing : VoiceState()
        object Listening : VoiceState()
        object Processing : VoiceState()
        data class Error(val message: String) : VoiceState()
    }

    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    var currentLanguageCode: String = "bn-BD"
        private set

    fun startListening(languageCode: String = "bn-BD") {
        currentLanguageCode = languageCode

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onStateChanged(VoiceState.Error("Speech recognition is not available on this device"))
            return
        }

        try {
            stopListening()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val isBangla = languageCode.startsWith("bn")
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(languageCode, if (isBangla) "en-US" else "bn-BD"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
            }

            onStateChanged(VoiceState.Preparing)
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("SingBordVoice", "Failed to start speech recognizer", e)
            isListening = false
            onStateChanged(VoiceState.Error("Could not start microphone: ${e.localizedMessage}"))
        }
    }

    fun stopListening() {
        try {
            if (isListening) {
                isListening = false
                speechRecognizer?.stopListening()
            }
        } catch (e: Exception) {
            Log.e("SingBordVoice", "Error stopping speech recognizer", e)
        }
    }

    fun cancel() {
        try {
            isListening = false
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            onStateChanged(VoiceState.Idle)
        } catch (e: Exception) {
            Log.e("SingBordVoice", "Error cancelling speech recognizer", e)
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onStateChanged(VoiceState.Listening)
            }

            override fun onBeginningOfSpeech() {
                onStateChanged(VoiceState.Listening)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize rmsdB (-2 to 10 dB typically) to 0.0 - 1.0
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onRmsLevelChanged(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onStateChanged(VoiceState.Processing)
            }

            override fun onError(error: Int) {
                isListening = false
                val isBangla = currentLanguageCode.startsWith("bn")
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection required"
                    SpeechRecognizer.ERROR_NO_MATCH -> if (isBangla) "স্পষ্ট বোঝা যায়নি, আবার চেষ্টা করুন" else "No speech match. Tap mic to retry."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Mic engine busy, reset..."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> if (isBangla) "কোনো কথা শোনা যায়নি, আবার মাইকে চাপ দিন" else "No speech heard. Tap mic to speak."
                    else -> "Voice input stopped"
                }

                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    onStateChanged(VoiceState.Error(msg))
                } else if (error != SpeechRecognizer.ERROR_CLIENT) {
                    onStateChanged(VoiceState.Error(msg))
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val rawText = matches[0]
                    val isBangla = currentLanguageCode.startsWith("bn")
                    val enhanced = VoiceTextEnhancer.enhanceText(rawText, isBangla)
                    if (enhanced.isNotBlank()) {
                        onFinalResult(enhanced)
                    }
                }
                onStateChanged(VoiceState.Idle)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val rawText = matches[0]
                    val isBangla = currentLanguageCode.startsWith("bn")
                    val enhanced = VoiceTextEnhancer.enhanceText(rawText, isBangla)
                    if (enhanced.isNotBlank()) {
                        onPartialResult(enhanced)
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
