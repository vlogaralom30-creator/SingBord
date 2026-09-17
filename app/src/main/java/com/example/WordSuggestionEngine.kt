package com.example

import com.example.data.UserDictionaryRepository
import java.util.Locale

/**
 * Intelligent multilingual Word Suggestion Engine for SingBord.
 * - Supports Native Bangla dictionary
 * - Avro and Banglish-to-Bangla transliteration suggestions
 * - Banglish conversational vocabulary
 * - Standard English vocabulary
 * - Dynamic User-Learned Dictionary ranking
 * - Predictive Next-Word Prediction (Bigram learning & pre-seeded transitions)
 */
object WordSuggestionEngine {

    /**
     * Top high-frequency native Bangla words for predictive typing.
     */
    val BANGLA_VOCABULARY = listOf(
        // Pronouns & Addressing
        "আমি", "তুমি", "আপনি", "তুই", "আমরা", "তোমরা", "আপনারা", "তারা", "সে", "তিনি",
        "আমাকে", "তোমাকে", "আপনাকে", "আমাদের", "তোমাদের", "তাদের", "ওদের", "কাউকে",
        "তোমায়", "আমায়", "আমার", "তোমার", "আপনার",
        "ভাই", "আপু", "দাদা", "দিদি", "মামা", "কাকু", "বাবা", "মা", "বন্ধু", "বন্ধুরা",
        "সবাই", "সবাইকে", "মানুষ", "মানুষের",

        // Daily Conversations & Greetings
        "কেমন", "আছো", "আছেন", "আছি", "কোথায়", "কী", "কি", "কেন", "কবে", "কখন",
        "কিভাবে", "ভালো", "ধন্যবাদ", "স্বাগতম", "ঠিক", "আছে", "সত্যি", "অবশ্যই",
        "হয়তো", "কিন্তু", "এবং", "বা", "অথবা", "তাহলে", "কারণ", "তাই", "এখন", "পরে",
        "সময়", "অনেক", "একটু", "আরও", "শুধু", "নাকি", "হবে", "হয়", "না", "হ্যাঁ",
        "ভালোবাসি", "ভালোবাসা", "সুন্দর", "ইনশাআল্লাহ", "আলহামদুলিল্লাহ", "সুবহানাল্লাহ",

        // Common Verbs & Actions
        "করছি", "করব", "করলাম", "করেন", "করো", "করি", "করলে", "করবে",
        "যাচ্ছি", "যাব", "গেলাম", "গেল", "যান", "যাও", "যাই", "যাবে",
        "আসছি", "আসব", "এলাম", "আসুন", "আসো", "আসি", "আসবে",
        "দেখছি", "দেখব", "দেখলাম", "দেখুন", "দেখো", "দেখি",
        "শুনছি", "শুনব", "শুনলাম", "শুনুন", "শোনো", "শুনি",
        "বলছি", "বলব", "বললাম", "বলুন", "বলো", "বলি",
        "লিখছি", "লিখব", "পড়ছি", "পড়ব", "জানছি", "জানি", "জানলাম", "জানি না",
        "পেলাম", "পাব", "দিলাম", "দেব", "দাও", "নিন", "নিব",

        // Places, Things & Life
        "বাড়ি", "বাসা", "অফিস", "স্কুল", "কলেজ", "কাজ", "খাবার", "ভাত", "পানি", "চা",
        "টাকা", "মোবাইল", "ফোন", "ছবি", "গান", "বই", "গল্প", "শহর", "দেশ", "গ্রাম",
        "রাস্তা", "গাড়ি", "দিন", "রাত", "সকাল", "সন্ধ্যা", "খবর", "বাংলা", "বাংলাদেশ",
        "নতুন", "পুরনো", "শান্তি", "আনন্দ", "খুশি", "কষ্ট", "মন"
    )

    /**
     * Top high-frequency Banglish words used in daily messaging, chats, and social media.
     */
    val BANGLISH_VOCABULARY = listOf(
        // Pronouns & Addressing
        "ami", "tumi", "tui", "apni", "amra", "tomra", "apnara", "tara", "she", "tini",
        "amake", "tomake", "apnake", "amader", "tomader", "oder", "tader",
        "amar", "tomar", "apnar", "tomay", "amay",
        "vai", "bhai", "apu", "dada", "didi", "mama", "mami", "kaku", "chacha", "baba", "ma",
        "bondhu", "bondhura", "dost", "shobai", "shobaike", "sobai",

        // Greetings & Questions
        "kemon", "acho", "achho", "achen", "asen", "achi", "kothay", "kothao", "ki", "keno", "kobe",
        "kivabe", "kirokom", "shokal", "shubho", "shondha", "raat", "khobor",
        "shob", "sob", "shobthik",

        // Responses & Expressions
        "bhalo", "valo", "bhalobashi", "valobasi", "bhalobasha", "dhonnobad", "shagotom", "thik", "ache", "achhe",
        "shotti", "sotti", "oboshoy", "nischoy", "hoyto", "kintu", "ebong", "ar", "othoba",
        "tahole", "tobe", "karon", "tai", "ekhon", "pore", "shomoy", "somoy",
        "kichu", "onek", "ektu", "aro", "matro", "shudhu", "naki", "hobe", "hoy",
        "jani", "janina", "inshaallah", "alhamdulillah",

        // Common Verbs
        "korchi", "korbo", "korlam", "koren", "koro", "kori", "korle", "korbe",
        "jachhi", "jabo", "gelam", "gelo", "jan", "jao", "jai", "jabe",
        "ashbo", "ashchi", "elam", "asho", "ashen", "ashi", "ashbe", "asbo",
        "dekhi", "dekhlam", "dekhbo", "dekhte", "dekhun",
        "shunlam", "shunbo", "shunte", "shuno", "shuncho",
        "bolchi", "bolbo", "bollam", "bolen", "bolo", "boli",
        "likhchi", "likhbo", "likhechi", "porchi", "porbo",
        "bujhlam", "bujhte", "bujhi", "bujhechi",
        "pelam", "pabo", "dilam", "debo", "dibo", "nibo",

        // Daily Life, Work & Places
        "bari", "basay", "basha", "office", "school", "college", "class",
        "khabar", "bhat", "pani", "cha", "coffee", "nasta",
        "taka", "mobile", "phone", "chobi", "gaan", "desh", "rasta",
        "shundor", "sundor", "notun", "purono", "khushi", "koshto", "shanti", "mon"
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
        "important", "together", "already", "beautiful", "wonderful", "question", "questions"
    )

    /**
     * Built-in Next-Word Prediction pairs (both Bangla and Banglish/English).
     * Used when the user presses space after a word.
     */
    private val BUILTIN_BIGRAM_PREDICTIONS: Map<String, List<String>> = mapOf(
        // Bangla
        "আমি" to listOf("তোমাকে", "জানি না", "তোমায়", "ভালো আছি", "আছি", "যাব", "করছি"),
        "তুমি" to listOf("কেমন আছো", "কোথায়", "কি করছো", "কবে আসবে", "ভালো থেকো", "আসো"),
        "আপনি" to listOf("কেমন আছেন", "কোথায় আছেন", "কি করছেন", "ভালো আছেন", "আসেন"),
        "কেমন" to listOf("আছো", "আছেন", "হলো", "আছো তুমি"),
        "কি" to listOf("খবর", "করছো", "হয়েছে", "বলবে", "করছেন"),
        "কী" to listOf("খবর", "করছো", "হয়েছে", "অবস্থা"),
        "ভালো" to listOf("আছি", "থেকো", "লাগে", "বাসি", "হবে", "থাকবেন"),
        "ধন্যবাদ" to listOf("অনেক", "ভাই", "আপু", "তোমাকে", "সবাইকে"),
        "ইনশাআল্লাহ" to listOf("হবে", "যাব", "দেখা হবে", "ভালো হবে", "আসব"),
        "আলহামদুলিল্লাহ" to listOf("ভালো আছি", "সব ঠিক", "অনেক ভালো"),
        "সুবহানাল্লাহ" to listOf("আল্লাহ", "সুন্দর"),
        "অনেক" to listOf("ধন্যবাদ", "সুন্দর", "ভালোবাসা", "ভালো", "কষ্ট"),
        "সব" to listOf("ঠিক আছে", "কিছু", "সময়", "ভালো"),
        "বন্ধু" to listOf("কেমন আছো", "আমার", "কোথায়"),
        "ভাই" to listOf("কেমন আছেন", "কোথায় আছেন", "শুনুন"),
        "কোথায়" to listOf("আছো", "আছেন", "যাবে", "যাবেন"),
        "দেখা" to listOf("হবে", "করব", "হয়েছে"),
        "কথা" to listOf("বলব", "বলছি", "হবে"),
        "ভালোবাসি" to listOf("তোমাকে", "অনেক", "তোমায়"),

        // Banglish (translates into both Bangla & Banglish predictions)
        "ami" to listOf("তোমাকে", "জানি না", "তোমায়", "valo achi", "tomake", "achi"),
        "tumi" to listOf("কেমন আছো", "kemon acho", "kothay", "ki korcho", "asho"),
        "apni" to listOf("কেমন আছেন", "kemon achen", "kothay achen", "ki korchen"),
        "kemon" to listOf("acho", "আছো", "achen", "আছেন", "holo"),
        "ki" to listOf("khobor", "খবর", "korcho", "hoyeche", "hobe"),
        "valo" to listOf("achi", "আছি", "theko", "লাগে", "hobe"),
        "bhalo" to listOf("achi", "আছি", "theko", "বাসি", "hobe"),
        "dhonnobad" to listOf("onek", "অনেক", "bhai", "vai", "apu"),
        "inshaallah" to listOf("hobe", "হবে", "jabo", "dekha hobe"),
        "alhamdulillah" to listOf("valo achi", "ভালো আছি", "shob thik"),
        "onek" to listOf("dhonnobad", "ধন্যবাদ", "shundor", "valo"),
        "shob" to listOf("thik ache", "ঠিক আছে", "kichu"),
        "bondhu" to listOf("kemon acho", "কেমন আছো", "amar"),

        // English
        "how" to listOf("are you", "is it", "about you", "can i"),
        "thank" to listOf("you", "you so much", "god"),
        "thanks" to listOf("a lot", "for your help", "bro"),
        "good" to listOf("morning", "night", "luck", "afternoon"),
        "i" to listOf("am", "will", "have", "love you", "think", "know"),
        "you" to listOf("are", "can", "have", "know", "want"),
        "we" to listOf("are", "will", "can", "have"),
        "what" to listOf("is", "are you", "happened", "about"),
        "where" to listOf("are you", "is", "were you"),
        "see" to listOf("you", "you soon", "later"),
        "please" to listOf("help", "let me know", "call me")
    )

    /**
     * Generates a ranked list of word suggestions for the prefix.
     * When user types Banglish/English, intelligently generates and surfaces Bangla suggestions!
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
        val lowerPrefix = trimmed.lowercase(Locale.ROOT)
        val isAllUpper = trimmed.length > 1 && trimmed.all { it.isUpperCase() }
        val isCapitalized = trimmed.first().isUpperCase() && !isAllUpper

        val candidateScores = mutableMapOf<String, Int>()

        // 0. Avro & Phonetic Transliteration Candidate:
        // When typing Latin letters, parse to Bangla Unicode!
        // In Avro mode, or in English mode with enableBanglish turned on,
        // typing "ami" directly suggests "আমি"!
        val avroPhoneticCandidate = if (!isBanglaPrefix && (language == KeyboardLanguage.AVRO || enableBanglish)) {
            AvroPhoneticEngine.parse(trimmed)
        } else null

        if (!avroPhoneticCandidate.isNullOrBlank() && avroPhoneticCandidate != trimmed) {
            candidateScores[avroPhoneticCandidate] = 2500
        }

        // Also check if prefix is a known Banglish word in phonetic dictionary
        if (!isBanglaPrefix && enableBanglish) {
            val banglaDirect = AvroPhoneticEngine.commonPhoneticWords[lowerPrefix]
            if (banglaDirect != null) {
                candidateScores[banglaDirect] = 3000
            }
        }

        // 1. User-Learned Words (Highest Priority)
        if (userRepo != null) {
            val learnedWords = userRepo.getMemoryLearnedWords()
            for ((word, freq) in learnedWords) {
                if (word.startsWith(lowerPrefix) || (isBanglaPrefix && word.startsWith(trimmed))) {
                    val exactBonus = if (word == lowerPrefix || word == trimmed) 600 else 0
                    candidateScores[word] = 1500 + (freq * 50) + exactBonus
                }
            }
        }

        // 2. Native Bangla Vocabulary
        if (isBanglaPrefix || language == KeyboardLanguage.BANGLA_PROBHAT) {
            for (word in BANGLA_VOCABULARY) {
                if (word.startsWith(trimmed) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == trimmed) 300 else 0
                    candidateScores[word] = 800 + exactBonus
                }
            }
        } else if (!isBanglaPrefix && avroPhoneticCandidate != null) {
            // Also suggest Bangla words that start with the phonetic transliteration!
            for (word in BANGLA_VOCABULARY) {
                if (word.startsWith(avroPhoneticCandidate) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == avroPhoneticCandidate) 400 else 0
                    candidateScores[word] = 900 + exactBonus
                }
            }
        }

        // 3. Banglish Vocabulary
        if (enableBanglish && !isBanglaPrefix) {
            for (word in BANGLISH_VOCABULARY) {
                if (word.startsWith(lowerPrefix) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == lowerPrefix) 200 else 0
                    candidateScores[word] = 500 + exactBonus
                }
            }
        }

        // 4. Standard English Vocabulary
        if (!isBanglaPrefix) {
            for (word in ENGLISH_VOCABULARY) {
                if (word.startsWith(lowerPrefix) && !candidateScores.containsKey(word)) {
                    val exactBonus = if (word == lowerPrefix) 150 else 0
                    candidateScores[word] = 300 + exactBonus
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

    /**
     * Context-aware Next-Word Prediction.
     * Given the previously typed word, predicts the most likely next words.
     * Combines user-learned transitions with built-in high frequency n-grams.
     * e.g., "ami" / "আমি" -> ["তোমাকে", "জানি না", "তোমায়"]
     */
    fun getNextWordPredictions(
        prevWord: String,
        userRepo: UserDictionaryRepository? = null,
        language: KeyboardLanguage = KeyboardLanguage.BANGLA_PROBHAT,
        maxCount: Int = 4
    ): List<String> {
        val cleanPrev = prevWord.trim().lowercase(Locale.ROOT)
        if (cleanPrev.isBlank()) return emptyList()

        val results = mutableListOf<String>()

        // 1. User-Learned bigrams from database / memory (highest personal priority)
        if (userRepo != null) {
            val userPredictions = userRepo.getPredictedNextWords(cleanPrev, limit = maxCount)
            for (word in userPredictions) {
                if (!results.contains(word)) {
                    results.add(word)
                }
            }

            // Also check Bangla translation of cleanPrev if it was Latin
            val banglaEquiv = AvroPhoneticEngine.commonPhoneticWords[cleanPrev]
            if (banglaEquiv != null) {
                val banglaUserPredictions = userRepo.getPredictedNextWords(banglaEquiv, limit = maxCount)
                for (word in banglaUserPredictions) {
                    if (!results.contains(word)) {
                        results.add(word)
                    }
                }
            }
        }

        // 2. Built-in high frequency bigrams
        val builtin = BUILTIN_BIGRAM_PREDICTIONS[cleanPrev]
            ?: AvroPhoneticEngine.commonPhoneticWords[cleanPrev]?.let { BUILTIN_BIGRAM_PREDICTIONS[it] }
            ?: emptyList()

        for (word in builtin) {
            if (!results.contains(word)) {
                results.add(word)
            }
            if (results.size >= maxCount) break
        }

        return results.take(maxCount)
    }
}
