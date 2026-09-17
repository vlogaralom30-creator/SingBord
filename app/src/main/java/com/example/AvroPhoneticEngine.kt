package com.example

/**
 * Phonetic Transliteration Engine for Avro Bangla typing mode.
 * Converts Romanized phonetic input into standard Bangla Unicode.
 */
object AvroPhoneticEngine {

    private val directPatterns = listOf(
        // Vowels standalone
        "aa" to "আ",
        "oi" to "ঐ",
        "ou" to "ঔ",
        "ii" to "ঈ",
        "uu" to "ঊ",
        "ee" to "ঈ",
        "oo" to "ঊ",
        "a" to "অ",
        "o" to "ও",
        "i" to "ই",
        "u" to "উ",
        "e" to "এ",

        // Common multi-letter English & Banglish clusters
        "tion" to "শন",
        "sion" to "শন",
        "que" to "কুয়ে",
        "qu" to "কু",
        "kkhn" to "ক্ষ্ণ",
        "kkh" to "ক্ষ",
        "ggy" to "জ্ঞ",
        "ngkh" to "ঙ্খ",
        "ngk" to "ঙ্ক",
        "nggh" to "ঙ্ঘ",
        "ngg" to "ঙ্গ",
        "cch" to "চ্ছ",
        "jjh" to "জ্ঝ",
        "nch" to "ঞ্চ",
        "njh" to "ঞ্জ",
        "ntth" to "ণ্ঠ",
        "nddh" to "ণ্ঢ",
        "tth" to "ত্থ",
        "ddh" to "দ্ধ",
        "nt" to "ন্ত",
        "nth" to "ন্থ",
        "nd" to "ন্দ",
        "ndh" to "ন্ধ",
        "mph" to "ম্ফ",
        "mbh" to "ম্ভ",
        "shch" to "শ্চ",
        "shthh" to "ষ্ফ",
        "shth" to "ষ্ঠ",
        "shkh" to "ষ্খ",
        "shk" to "ষ্ক",
        "sht" to "ষ্ট",
        "shph" to "স্ফ",
        "skh" to "স্খ",
        "sk" to "স্ক",
        "sth" to "স্থ",
        "st" to "স্ট",
        "sph" to "স্ফ",
        "hm" to "হ্ম",
        "hn" to "হ্ন",
        "hl" to "হ্ল",

        // Aspirated & specialized consonants
        "kh" to "খ",
        "gh" to "ঘ",
        "ng" to "ঙ",
        "ch" to "চ",
        "chh" to "ছ",
        "jh" to "ঝ",
        "Th" to "ঠ",
        "Dh" to "ঢ",
        "th" to "থ",
        "dh" to "ধ",
        "ph" to "ফ",
        "bh" to "ভ",
        "sh" to "শ",
        "Sh" to "ষ",
        "Rh" to "ঢ়",
        "rr" to "ড়",
        "wa" to "ওয়া",
        "we" to "ওয়ে",
        "wi" to "উই",
        "wo" to "ও",

        // Single consonants (including q, w, x, z)
        "k" to "ক",
        "q" to "ক",
        "g" to "গ",
        "c" to "চ",
        "j" to "জ",
        "T" to "ট",
        "D" to "ড",
        "N" to "ণ",
        "t" to "ত",
        "d" to "দ",
        "n" to "ন",
        "p" to "প",
        "f" to "ফ",
        "b" to "ব",
        "v" to "ভ",
        "m" to "ম",
        "z" to "জ",
        "Z" to "য",
        "x" to "ক্স",
        "w" to "ওয়",
        "y" to "য়",
        "r" to "র",
        "l" to "ল",
        "s" to "স",
        "S" to "ষ",
        "h" to "হ",
        "R" to "ড়"
    )

    private val karMap = mapOf(
        "aa" to "া",
        "a" to "া",
        "i" to "ি",
        "ee" to "ী",
        "ii" to "ী",
        "u" to "ু",
        "oo" to "ূ",
        "uu" to "ূ",
        "e" to "ে",
        "oi" to "ৈ",
        "o" to "ো",
        "ou" to "ৌ"
    )

    /**
     * Translates a phonetic romanized word into Bengali Unicode.
     */
    fun parse(input: String): String {
        if (input.isBlank()) return input

        val lower = input.lowercase()
        // Quick word dictionary for high accuracy common words & loanwords
        val directWord = commonPhoneticWords[lower]
        if (directWord != null) return directWord

        val sb = StringBuilder()
        var i = 0
        val len = input.length
        var lastWasConsonant = false

        while (i < len) {
            var matched = false

            // Try 4-char down to 1-char combinations
            for (matchLen in 4 downTo 1) {
                if (i + matchLen <= len) {
                    val sub = input.substring(i, i + matchLen)

                    // If previous token was consonant and current matches a vowel kar
                    if (lastWasConsonant && karMap.containsKey(sub.lowercase())) {
                        sb.append(karMap[sub.lowercase()])
                        i += matchLen
                        matched = true
                        lastWasConsonant = false
                        break
                    }

                    // Otherwise check direct patterns
                    val target = directPatterns.firstOrNull { it.first.equals(sub, ignoreCase = (sub.length > 1)) }
                        ?: directPatterns.firstOrNull { it.first == sub }
                    if (target != null) {
                        sb.append(target.second)
                        i += matchLen
                        matched = true
                        lastWasConsonant = isConsonantBangla(target.second)
                        break
                    }
                }
            }

            if (!matched) {
                val c = input[i]
                // Fallback character mapping to avoid leaking raw Latin chars
                when (c.lowercaseChar()) {
                    'q' -> sb.append("ক")
                    'w' -> sb.append("ও")
                    'x' -> sb.append("ক্স")
                    'z' -> sb.append("জ")
                    'c' -> sb.append("ক")
                    else -> sb.append(c)
                }
                lastWasConsonant = false
                i++
            }
        }

        return sb.toString()
    }

    private fun isConsonantBangla(str: String): Boolean {
        if (str.isEmpty()) return false
        val c = str.last()
        return c in '\u0995'..'\u09B9' || c == 'ড়' || c == 'ঢ়' || c == 'য়'
    }

    /**
     * Comprehensive dictionary mapping high-frequency Banglish & common English words
     * to accurate Bengali translations.
     */
    val commonPhoneticWords = mapOf(
        // High frequency Pronouns & Addresses
        "ami" to "আমি",
        "tumi" to "তুমি",
        "apni" to "আপনি",
        "tui" to "তুই",
        "amra" to "আমরা",
        "tomra" to "তোমরা",
        "apnara" to "আপনারা",
        "amake" to "আমাকে",
        "tomake" to "তোমাকে",
        "apnake" to "আপনাকে",
        "tomay" to "তোমায়",
        "amay" to "আমায়",
        "amar" to "আমার",
        "tomar" to "তোমার",
        "apnar" to "আপনার",
        "amader" to "আমাদের",
        "tomader" to "তোমাদের",
        "apnader" to "আপনাদের",
        "tader" to "তাদের",
        "tar" to "তার",
        "she" to "সে",
        "tini" to "তিনি",
        "bhai" to "ভাই",
        "vai" to "ভাই",
        "apu" to "আপু",
        "dada" to "দাদা",
        "didi" to "দিদি",
        "mama" to "মামা",
        "baba" to "বাবা",
        "ma" to "মা",
        "bondhu" to "বন্ধু",
        "shobai" to "সবাই",
        "sobai" to "সবাই",
        "shobaike" to "সবাইকে",
        "manush" to "মানুষ",

        // Common Conversational & Greetings
        "kemon" to "কেমন",
        "acho" to "আছো",
        "achho" to "আছো",
        "asen" to "আছেন",
        "achen" to "আছেন",
        "achi" to "আছি",
        "valo" to "ভালো",
        "bhalo" to "ভালো",
        "bhalobashi" to "ভালোবাসি",
        "valobasi" to "ভালোবাসি",
        "bhalobasha" to "ভালোবাসা",
        "valobasha" to "ভালোবাসা",
        "khobor" to "খবর",
        "ki" to "কি",
        "kee" to "কি",
        "dhonnobad" to "ধন্যবাদ",
        "dhonnobaad" to "ধন্যবাদ",
        "bangla" to "বাংলা",
        "bangladesh" to "বাংলাদেশ",
        "shundor" to "সুন্দর",
        "sundor" to "সুন্দর",
        "kokhon" to "কখন",
        "kothay" to "কোথায়",
        "keno" to "কেন",
        "kivabe" to "কিভাবে",
        "kibhabe" to "কিভাবে",
        "ha" to "হ্যাঁ",
        "haa" to "হ্যাঁ",
        "na" to "না",
        "naa" to "না",
        "onek" to "অনেক",
        "ektu" to "একটু",
        "kichu" to "কিছু",
        "shob" to "সব",
        "sob" to "সব",
        "shotti" to "সত্যি",
        "sotti" to "সত্যি",
        "thik" to "ঠিক",
        "thikache" to "ঠিক আছে",
        "thik ache" to "ঠিক আছে",
        "oboshoy" to "অবশ্যই",
        "kintu" to "কিন্তু",
        "ebong" to "এবং",
        "ar" to "আর",
        "aar" to "আর",
        "tai" to "তাই",
        "ekhon" to "এখন",
        "pore" to "পরে",
        "age" to "আগে",
        "shomoy" to "সময়",
        "somoy" to "সময়",
        "din" to "দিন",
        "rat" to "রাত",
        "raat" to "রাত",
        "shokal" to "সকাল",
        "sokal" to "সকাল",
        "dupur" to "দুপুর",
        "shondha" to "সন্ধ্যা",
        "ajke" to "আজকে" ,
        "aaj" to "আজ",
        "kalke" to "কালকে",
        "kaal" to "কাল",

        // Common Verbs
        "jani" to "জানি",
        "janina" to "জানি না",
        "korcho" to "করছো",
        "korchen" to "করছেন",
        "korchi" to "করছি",
        "koro" to "করো",
        "koren" to "করেন",
        "kori" to "করি",
        "korbo" to "করবো",
        "korbe" to "করবে",
        "korlam" to "করলাম",
        "ashbo" to "আসবো",
        "asbo" to "আসবো",
        "ashbe" to "আসবে",
        "asbe" to "আসবে",
        "asho" to "আসো",
        "aso" to "আসো",
        "ashen" to "আসেন",
        "asen" to "আছেন",
        "jabo" to "যাব",
        "jabe" to "যাবে",
        "jao" to "যাও",
        "jan" to "যান",
        "jai" to "যাই",
        "dekha" to "দেখা",
        "hobe" to "হবে",
        "hoy" to "হয়",
        "hoyeche" to "হয়েছে",
        "hoise" to "হয়েছে",
        "kotha" to "কথা",
        "bolbo" to "বলবো",
        "bolchi" to "বলছি",
        "bolo" to "বলো",
        "bolen" to "বলেন",
        "boli" to "বলি",
        "bollam" to "বললাম",
        "shuncho" to "শুনছো",
        "shunlam" to "শুনলাম",
        "shunte" to "শুনতে",
        "shuno" to "শোনো",
        "bujhechi" to "বুঝেছি",
        "bujhlam" to "বুঝেছি",
        "dekhi" to "দেখি",
        "dekhbo" to "দেখবো",
        "dekhlam" to "দেখলাম",
        "dekho" to "দেখো",
        "dekhun" to "দেখুন",
        "dao" to "দাও",
        "den" to "দেন",
        "dibo" to "দিব",
        "debo" to "দেব",
        "nibo" to "নিব",
        "nebo" to "নেব",
        "pelam" to "পেলাম",
        "pabo" to "পাব",
        "kaj" to "কাজ",
        "bari" to "বাড়ি",
        "basha" to "বাসা",
        "khabar" to "খাবার",
        "pani" to "পানি",
        "cha" to "চা",
        "bhat" to "ভাত",
        "bhaat" to "ভাত",
        "taka" to "টাকা",
        "rasta" to "রাস্তা",
        "desh" to "দেশ",
        "gaan" to "গান",
        "chobi" to "ছবি",
        "notun" to "নতুন",
        "purono" to "পুরনো",
        "khushi" to "খুশি",
        "koshto" to "কষ্ট",
        "shanti" to "শান্তি",
        "mon" to "মন",

        // Islamic & Cultural Greetings
        "salam" to "সালাম",
        "assalamu" to "আসসালামু",
        "alaikum" to "আলাইকুম",
        "inshaallah" to "ইনশাআল্লাহ",
        "inshallah" to "ইনশাআল্লাহ",
        "alhamdulillah" to "আলহামদুলিল্লাহ",
        "subhanallah" to "সুবহানাল্লাহ",
        "mashallah" to "মাশাআল্লাহ",
        "bismillah" to "বিসমিল্লাহ",
        "allah" to "আল্লাহ",
        "hafez" to "হাফেজ",
        "shukriya" to "শুকরিয়া",

        // English loanwords commonly used in daily text
        "question" to "কোশ্চেন",
        "questions" to "কোশ্চেন",
        "school" to "স্কুল",
        "college" to "কলেজ",
        "office" to "অফিস",
        "class" to "ক্লাস",
        "mobile" to "মোবাইল",
        "phone" to "ফোন",
        "number" to "নম্বর",
        "problem" to "সমস্যা",
        "help" to "সাহায্য",
        "time" to "টাইম",
        "message" to "মেসেজ",
        "photo" to "ফটো",
        "picture" to "ছবি",
        "video" to "ভিডিও",
        "audio" to "অডিও",
        "link" to "লিংক",
        "call" to "কল",
        "same" to "সেম",
        "okay" to "ঠিক আছে",
        "ok" to "ঠিক আছে",
        "yes" to "হ্যাঁ",
        "no" to "না",
        "please" to "প্লিজ",
        "sorry" to "দুঃখিত",
        "welcome" to "স্বাগতম",
        "hello" to "হ্যালো",
        "hi" to "হাই",
        "bye" to "বিদায়",
        "thanks" to "ধন্যবাদ",
        "thank" to "ধন্যবাদ",
        "love" to "ভালোবাসা",
        "like" to "লাইক",
        "good" to "ভালো",
        "happy" to "হ্যাপি",
        "birthday" to "জন্মদিন"
    )
}
