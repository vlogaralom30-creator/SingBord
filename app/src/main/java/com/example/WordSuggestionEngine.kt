package com.example

import com.example.data.UserDictionaryRepository
import java.util.Locale

/**
 * Intelligent dual-language Word Suggestion Engine for SingBord.
 * - Supports high-frequency English vocabulary
 * - Rich built-in Banglish vocabulary (everyday conversational phonetic words)
 * - Dynamic User-Learned Dictionary ranking: Prioritizes words the user types frequently
 */
object WordSuggestionEngine {

    /**
     * Top high-frequency Banglish words used in daily messaging, chats, and social media.
     */
    val BANGLISH_VOCABULARY = listOf(
        // Pronouns & Addressing
        "ami", "tumi", "tui", "apni", "amra", "tomra", "apnara", "tara", "she", "tini",
        "amake", "tomake", "apnake", "amader", "tomader", "oder", "tader",
        "vai", "bhai", "apu", "dada", "didi", "mama", "mami", "kaku", "chacha", "baba", "ma",
        "bondhu", "bondhura", "dost", "shobai", "shobaike",

        // Greetings & Questions
        "kemon", "acho", "achho", "achen", "kothay", "kothao", "ki", "keno", "kobe",
        "kivabe", "kirokom", "shokal", "shubho", "shondha", "raat", "khobor",
        "ki khobor", "shob", "shobthik",

        // Responses & Expressions
        "bhalo", "valo", "bhalobashi", "dhonnobad", "shagotom", "thik", "ache", "achhe",
        "shotti", "oboshoy", "nischoy", "hoyto", "kintu", "ebong", "ar", "othoba",
        "tahole", "tobe", "karon", "tai", "tai to", "ekhon", "pore", "shomoy",
        "kichu", "onek", "ektu", "aro", "matro", "shudhu", "naki", "hobe", "hoy",

        // Common Verbs (Present, Past, Future, Continuous)
        "korchi", "korbo", "korlam", "koren", "koro", "kori", "korle",
        "jachhi", "jabo", "gelam", "gelo", "jan", "jao", "jai",
        "ashbo", "ashchi", "elam", "asho", "ashen", "ashi",
        "dekhi", "dekhlam", "dekhbo", "dekhte", "dekhun",
        "shunlam", "shunbo", "shunte", "shunchee", "shuno",
        "bolchi", "bolbo", "bollam", "bolen", "bolo", "boli",
        "likhchi", "likhbo", "likhechi", "porchi", "porbo",
        "khelchi", "ghumachhi", "ghumabo", "bujhlam", "bujhte", "bujhi",
        "jani", "janlam", "janbo", "pelam", "pabo", "pawa", "dilam", "debo",

        // Daily Life, Work & Places
        "bari", "basay", "office", "school", "college", "varsity", "class",
        "khabar", "bhat", "pani", "cha", "coffee", "nasta", "ranna",
        "taka", "poisa", "mobile", "phone", "computer", "laptop",
        "chobi", "gaan", "natok", "cinema", "boi", "golpo",
        "chuti", "kaj", "kam", "shohor", "desh", "gram", "rasta",
        "gari", "rickshaw", "train", "bus", "bazar", "dokan",

        // Feelings, States & Qualities
        "shundor", "kharap", "notun", "purono", "shanto", "bortoman",
        "anondo", "khushi", "dukkho", "koshto", "shanti", "prem", "maya",
        "asha", "bhorosha", "bishwas", "shobdo", "bhasha", "manush"
    )

    /**
     * Top high-frequency English words.
     */
    val ENGLISH_VOCABULARY = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "great", "here", "why", "need", "feel", "try", "leave", "call", "find", "again",
        "hello", "thanks", "please", "welcome", "yes", "today", "tomorrow", "tonight",
        "always", "never", "sometimes", "happy", "love", "friend", "family", "message",
        "number", "water", "food", "place", "home", "right", "left", "high", "long",
        "important", "together", "already", "beautiful", "wonderful", "awesome", "perfect"
    )

    /**
     * Generates a ranked list of word suggestions for the prefix.
     *
     * @param prefix The word fragment currently typed by the user
     * @param userRepo Optional reference to the UserDictionaryRepository
     * @param enableBanglish Whether to include Banglish vocabulary
     * @param maxCount Number of candidates to return (default: 4)
     */
    fun getSuggestions(
        prefix: String,
        userRepo: UserDictionaryRepository? = null,
        enableBanglish: Boolean = true,
        maxCount: Int = 4
    ): List<String> {
        val trimmed = prefix.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }

        val lowerPrefix = trimmed.lowercase(Locale.ROOT)
        val isAllUpper = trimmed.length > 1 && trimmed.all { it.isUpperCase() }
        val isCapitalized = trimmed.first().isUpperCase() && !isAllUpper

        // Gather candidates from all relevant sources
        val candidateScores = mutableMapOf<String, Int>()

        // 1. User-Learned Words (Highest Priority)
        if (userRepo != null) {
            val learnedWords = userRepo.getMemoryLearnedWords()
            for ((word, freq) in learnedWords) {
                if (word.startsWith(lowerPrefix)) {
                    // Score = Base 1000 + (frequency * 50) + exact match bonus
                    val exactBonus = if (word == lowerPrefix) 500 else 0
                    candidateScores[word] = 1000 + (freq * 50) + exactBonus
                }
            }
        }

        // 2. Banglish Vocabulary
        if (enableBanglish) {
            for (word in BANGLISH_VOCABULARY) {
                if (word.startsWith(lowerPrefix) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == lowerPrefix) 200 else 0
                    candidateScores[word] = 400 + exactBonus
                }
            }
        }

        // 3. Standard English Vocabulary
        for (word in ENGLISH_VOCABULARY) {
            if (word.startsWith(lowerPrefix) && !candidateScores.containsKey(word)) {
                val exactBonus = if (word == lowerPrefix) 150 else 0
                candidateScores[word] = 200 + exactBonus
            }
        }

        // 4. If current prefix is not in suggestions yet, ensure user's raw word is present
        if (!candidateScores.containsKey(lowerPrefix)) {
            candidateScores[lowerPrefix] = 50 // available to confirm new word
        }

        // Sort by score descending, then by length ascending
        val ranked = candidateScores.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key.length }
            )
            .take(maxCount)
            .map { it.key }

        // Format casing based on user's input style
        return ranked.map { candidate ->
            when {
                isAllUpper -> candidate.uppercase(Locale.ROOT)
                isCapitalized -> candidate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                else -> candidate
            }
        }
    }
}
