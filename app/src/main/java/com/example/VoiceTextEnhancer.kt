package com.example

import java.util.Locale

/**
 * VoiceTextEnhancer
 *
 * Smart speech recognition post-processor and text enhancer for SingBord.
 * Corrects spoken punctuation, cleans voice typos, fixes short/long word formatting,
 * and formats both Bangla (bn-BD) and English (en-US) spoken input.
 */
object VoiceTextEnhancer {

    /**
     * Enhances raw voice recognition text based on language and context.
     */
    fun enhanceText(rawText: String, isBangla: Boolean): String {
        if (rawText.isBlank()) return ""

        var text = rawText.trim()

        if (isBangla) {
            text = enhanceBanglaText(text)
        } else {
            text = enhanceEnglishText(text)
        }

        // Common cleanups for both languages
        text = cleanRepeatedWords(text)
        text = fixSpacingAroundPunctuation(text)

        return text
    }

    private fun enhanceBanglaText(input: String): String {
        var text = input

        // 1. Spoken Punctuation Commands in Bangla
        text = text.replace(Regex("(?i)\\b(দাঁড়ি|দাড়ি|ফুলস্টপ|পূর্ণচ্ছেদ)\\b"), "।")
        text = text.replace(Regex("(?i)\\b(কমা)\\b"), ",")
        text = text.replace(Regex("(?i)\\b(প্রশ্নবোধক চিহ্ন|প্রশ্নবোধক|প্রশ্নচিহ্ন)\\b"), "?")
        text = text.replace(Regex("(?i)\\b(আশ্চর্যবোধক চিহ্ন|বিস্ময়সূচক চিহ্ন|আশ্চর্যবোধক)\\b"), "!")
        text = text.replace(Regex("(?i)\\b(নতুন লাইন|নতুন প্যারা)\\b"), "\n")

        // 2. Normalize Bangla Digits / Numbers spoken in words
        // e.g. "এক দুই তিন" -> "১২৩" if in number mode or clean spacing
        text = text.replace(Regex("\\s+।"), "।")
        text = text.replace(Regex("\\s+,"), ",")
        text = text.replace(Regex("\\s+\\?"), "?")
        text = text.replace(Regex("\\s+!"), "!")

        // 3. Fix Bangla Dandi Spacing: "কথা। নতুন"
        text = text.replace(Regex("।(?=[^\\s\\n])"), "। ")

        return text
    }

    private fun enhanceEnglishText(input: String): String {
        var text = input

        // 1. Spoken Punctuation Commands in English
        text = text.replace(Regex("(?i)\\b(full stop|period)\\b"), ".")
        text = text.replace(Regex("(?i)\\b(comma)\\b"), ",")
        text = text.replace(Regex("(?i)\\b(question mark)\\b"), "?")
        text = text.replace(Regex("(?i)\\b(exclamation mark|exclamation point)\\b"), "!")
        text = text.replace(Regex("(?i)\\b(new line|next line)\\b"), "\n")
        text = text.replace(Regex("(?i)\\b(new paragraph)\\b"), "\n\n")

        // 2. Sentence Capitalization
        if (text.isNotEmpty() && text[0].isLowerCase()) {
            text = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        // Capitalize after period/question/exclamation
        text = text.replace(Regex("([.!?]\\s+)([a-z])")) { match ->
            match.groupValues[1] + match.groupValues[2].uppercase(Locale.getDefault())
        }

        return text
    }

    /**
     * Removes accidental duplicate consecutive words caused by voice stuttering
     * e.g. "Hello hello world" -> "Hello world", "আমি আমি যাব" -> "আমি যাব"
     */
    private fun cleanRepeatedWords(input: String): String {
        val words = input.split(Regex("\\s+"))
        if (words.size <= 1) return input

        val result = mutableListOf<String>()
        var prevWord = ""

        for (word in words) {
            val cleanWord = word.lowercase(Locale.getDefault())
            val cleanPrev = prevWord.lowercase(Locale.getDefault())

            // Don't deduplicate very short words like "na na", "ha ha", "no no", "bye bye"
            val isIntentionalRepeat = cleanWord in listOf("না", "হ্যাঁ", "হা", "bye", "ha", "go") && words.size <= 4

            if (cleanWord.isNotBlank() && (cleanWord != cleanPrev || isIntentionalRepeat)) {
                result.add(word)
                prevWord = word
            }
        }

        return result.joinToString(" ")
    }

    private fun fixSpacingAroundPunctuation(input: String): String {
        var text = input
        // Remove spaces before punctuation
        text = text.replace(Regex("\\s+([.,!?।])"), "$1")
        // Ensure single space after punctuation (except newline)
        text = text.replace(Regex("([.,!?।])(?=[^\\s\\n.,!?।])"), "$1 ")
        // Multiple spaces to single space
        text = text.replace(Regex("[ ]{2,}"), " ")
        return text.trim()
    }
}
