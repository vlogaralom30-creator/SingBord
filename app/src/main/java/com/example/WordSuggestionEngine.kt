package com.example

import com.example.data.UserDictionaryRepository
import java.util.Locale

/**
 * Intelligent multilingual Word Suggestion Engine for SingBord.
 * - Supports Native Bangla dictionary
 * - Avro transliteration suggestions
 * - Banglish conversational vocabulary
 * - Standard English vocabulary
 * - Dynamic User-Learned Dictionary ranking
 */
object WordSuggestionEngine {

    /**
     * Top high-frequency native Bangla words for predictive typing.
     */
    val BANGLA_VOCABULARY = listOf(
        // Pronouns & Addressing
        "আমি", "তুমি", "আপনি", "তুই", "আমরা", "তোমরা", "আপনারা", "তারা", "সে", "তিনি",
        "আমাকে", "তোমাকে", "আপনাকে", "আমাদের", "তোমাদের", "তাদের", "ওদের", "কাউকে",
        "ভাই", "আপু", "দাদা", "দিদি", "মামা", "কাকু", "বাবা", "মা", "বন্ধু", "বন্ধুরা",
        "সবাই", "সবাইকে", "মানুষ", "মানুষের",

        // Daily Conversations & Greetings
        "কেমন", "আছো", "আছেন", "আছি", "কোথায়", "কী", "কি", "কেন", "কবে", "কখন",
        "কিভাবে", "ভালো", "ধন্যবাদ", "স্বাগতম", "ঠিক", "আছে", "সত্যি", "অবশ্যই",
        "হয়তো", "কিন্তু", "এবং", "বা", "অথবা", "তাহলে", "কারণ", "তাই", "এখন", "পরে",
        "সময়", "অনেক", "একটু", "আরও", "শুধু", "নাকি", "হবে", "হয়", "না", "হ্যাঁ",

        // Common Verbs & Actions
        "করছি", "করব", "করলাম", "করেন", "করো", "করি", "করলে",
        "যাচ্ছি", "যাব", "গেলাম", "গেল", "যান", "যাও", "যাই",
        "আসছি", "আসব", "এলাম", "আসুন", "আসো", "আসি",
        "দেখছি", "দেখব", "দেখলাম", "দেখুন", "দেখো", "দেখি",
        "শুনছি", "শুনব", "শুনলাম", "শুনুন", "শোনো", "শুনি",
        "বলছি", "বলব", "বললাম", "বলুন", "বলো", "বলি",
        "লিখছি", "লিখব", "পড়ছি", "পড়ব", "জানছি", "জানি", "জানলাম",
        "পেলাম", "পাব", "দিলাম", "দেব", "দাও", "নিন",

        // Places, Things & Life
        "বাড়ি", "বাসা", "অফিস", "স্কুল", "কলেজ", "কাজ", "খাবার", "ভাত", "পানি", "চা",
        "টাকা", "মোবাইল", "ফোন", "ছবি", "গান", "বই", "গল্প", "শহর", "দেশ", "গ্রাম",
        "রাস্তা", "গাড়ি", "দিন", "রাত", "সকাল", "সন্ধ্যা", "খবর", "বাংলা", "বাংলাদেশ",
        "সুন্দর", "নতুন", "পুরনো", "শান্তি", "আনন্দ", "খুশি", "কষ্ট", "ভালোবাসা"
    )

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
     */
    fun getSuggestions(
        prefix: String,
        userRepo: UserDictionaryRepository? = null,
        language: KeyboardLanguage = KeyboardLanguage.BANGLA_PROBHAT,
        enableBanglish: Boolean = true,
        maxCount: Int = 4
    ): List<String> {
        val trimmed = prefix.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }

        val isBanglaPrefix = trimmed.any { it in '\u0980'..'\u09FF' }

        // If in Avro mode and prefix is Latin, generate phonetic Bangla candidate first
        val avroPhoneticCandidate = if (language == KeyboardLanguage.AVRO && !isBanglaPrefix) {
            AvroPhoneticEngine.parse(trimmed)
        } else null

        val lowerPrefix = trimmed.lowercase(Locale.ROOT)
        val isAllUpper = trimmed.length > 1 && trimmed.all { it.isUpperCase() }
        val isCapitalized = trimmed.first().isUpperCase() && !isAllUpper

        val candidateScores = mutableMapOf<String, Int>()

        // 0. Avro Transliteration candidate if available
        if (!avroPhoneticCandidate.isNullOrBlank() && avroPhoneticCandidate != trimmed) {
            candidateScores[avroPhoneticCandidate] = 2000
        }

        // 1. User-Learned Words (Highest Priority)
        if (userRepo != null) {
            val learnedWords = userRepo.getMemoryLearnedWords()
            for ((word, freq) in learnedWords) {
                if (word.startsWith(lowerPrefix) || (isBanglaPrefix && word.startsWith(trimmed))) {
                    val exactBonus = if (word == lowerPrefix || word == trimmed) 500 else 0
                    candidateScores[word] = 1000 + (freq * 50) + exactBonus
                }
            }
        }

        // 2. Native Bangla Vocabulary (if prefix is Bangla or in Bangla mode)
        if (isBanglaPrefix || language == KeyboardLanguage.BANGLA_PROBHAT) {
            for (word in BANGLA_VOCABULARY) {
                if (word.startsWith(trimmed) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == trimmed) 300 else 0
                    candidateScores[word] = 600 + exactBonus
                }
            }
        }

        // 3. Banglish Vocabulary
        if (enableBanglish && !isBanglaPrefix) {
            for (word in BANGLISH_VOCABULARY) {
                if (word.startsWith(lowerPrefix) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == lowerPrefix) 200 else 0
                    candidateScores[word] = 400 + exactBonus
                }
            }
        }

        // 4. Standard English Vocabulary
        if (!isBanglaPrefix) {
            for (word in ENGLISH_VOCABULARY) {
                if (word.startsWith(lowerPrefix) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == lowerPrefix) 150 else 0
                    candidateScores[word] = 200 + exactBonus
                }
            }
        }

        // 5. Ensure raw input is present if not matched
        if (!candidateScores.containsKey(trimmed) && !candidateScores.containsKey(lowerPrefix)) {
            candidateScores[trimmed] = 50
        }

        // Sort by score descending, then length ascending
        val ranked = candidateScores.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key.length }
            )
            .take(maxCount)
            .map { it.key }

        // Format casing for Latin letters
        return ranked.map { candidate ->
            val candidateIsBangla = candidate.any { it in '\u0980'..'\u09FF' }
            if (candidateIsBangla) {
                candidate
            } else {
                when {
                    isAllUpper -> candidate.uppercase(Locale.ROOT)
                    isCapitalized -> candidate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    else -> candidate
                }
            }
        }
    }
}
