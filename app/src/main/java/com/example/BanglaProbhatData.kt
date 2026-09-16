package com.example

/**
 * Supported Keyboard Language Modes
 */
enum class KeyboardLanguage(val label: String, val spacebarLabel: String) {
    BANGLA_PROBHAT("প্রভাত", "প্রভাত"),
    ENGLISH("English", "English"),
    AVRO("অভ্র", "অভ্র")
}

/**
 * Key data model representing primary key text, shifted text, sub-hint, and long-press alternates.
 */
data class ProbhatKey(
    val normal: String,
    val shifted: String,
    val hint: String = shifted,
    val alternates: List<String> = emptyList(),
    val isActionKey: Boolean = false
)

object BanglaProbhatLayout {

    // Row 0: Bangla Digits with English digits as hints & alternates
    val numberRow = listOf(
        ProbhatKey("১", "1", "1", listOf("১", "1", "!", "¹")),
        ProbhatKey("২", "2", "2", listOf("২", "2", "@", "²")),
        ProbhatKey("৩", "3", "3", listOf("৩", "3", "#", "³")),
        ProbhatKey("৪", "4", "4", listOf("৪", "4", "$", "৳")),
        ProbhatKey("৫", "5", "5", listOf("৫", "5", "%", "‰")),
        ProbhatKey("৬", "6", "6", listOf("৬", "6", "^")),
        ProbhatKey("৭", "7", "7", listOf("৭", "7", "&")),
        ProbhatKey("৮", "8", "8", listOf("৮", "8", "*")),
        ProbhatKey("৯", "9", "9", listOf("৯", "9", "(", "[")),
        ProbhatKey("০", "0", "0", listOf("০", "0", ")", "]"))
    )

    // Row 1: 12 keys exactly as in reference screenshots
    // Normal:  দ   ূ   ী   র   ট   এ   ু   ি   ও   প   ে   ো
    // Shifted: ধ   ঊ   ঈ   ড়   ঠ   ঐ   উ   ই   ঔ   ফ   ৈ   ৌ
    val row1 = listOf(
        ProbhatKey("দ", "ধ", "ধ", listOf("দ", "ধ", "দ্")),
        ProbhatKey("ূ", "ঊ", "ঊ", listOf("ূ", "ঊ", "ু")),
        ProbhatKey("ী", "ঈ", "ঈ", listOf("ী", "ঈ", "ি")),
        ProbhatKey("র", "ড়", "ড়", listOf("র", "ড়", "্র", "র্", "ঢ়")),
        ProbhatKey("ট", "ঠ", "ঠ", listOf("ট", "ঠ", "ট্")),
        ProbhatKey("এ", "ঐ", "ঐ", listOf("এ", "ঐ", "১")),
        ProbhatKey("ু", "উ", "উ", listOf("ু", "উ", "ূ")),
        ProbhatKey("ি", "ই", "ই", listOf("ি", "ই", "ী")),
        ProbhatKey("ও", "ঔ", "ঔ", listOf("ও", "ঔ")),
        ProbhatKey("প", "ফ", "ফ", listOf("প", "ফ", "প্")),
        ProbhatKey("ে", "ৈ", "ৈ", listOf("ে", "ৈ")),
        ProbhatKey("ো", "ৌ", "ৌ", listOf("ো", "ৌ"))
    )

    // Row 2: 9 keys exactly as in reference screenshots
    // Normal:  া   স   ড   ত   গ   হ   জ   ক   ল
    // Shifted: অ   ষ   ঢ   থ   ঘ   ঃ   ঝ   খ   ং
    val row2 = listOf(
        ProbhatKey("া", "অ", "অ", listOf("া", "অ", "আ")),
        ProbhatKey("স", "ষ", "ষ", listOf("স", "ষ", "স্")),
        ProbhatKey("ড", "ঢ", "ঢ", listOf("ড", "ঢ", "ড্ড")),
        ProbhatKey("ত", "থ", "থ", listOf("ত", "থ", "ৎ", "ত্ত")),
        ProbhatKey("গ", "ঘ", "ঘ", listOf("গ", "ঘ", "গ্ধ")),
        ProbhatKey("হ", "ঃ", "ঃ", listOf("হ", "ঃ", "হ্ম", "হ্ন")),
        ProbhatKey("জ", "ঝ", "ঝ", listOf("জ", "ঝ", "জ্ঞ", "জ্জ")),
        ProbhatKey("ক", "খ", "খ", listOf("ক", "খ", "ক্ষ", "ক্ক")),
        ProbhatKey("ল", "ং", "ং", listOf("ল", "ং", "ল্ল"))
    )

    // Row 3: 9 keys (between Shift and Backspace) exactly as in reference screenshots
    // Normal:  য   শ   চ   আ   ব   ন   ম   ৃ   ্
    // Shifted: য   ঢ়   ছ   ঋ   ভ   ণ   ঙ   <   ঁ
    val row3 = listOf(
        ProbhatKey("য", "য", "্য", listOf("য", "্য", "য়", "য্")),
        ProbhatKey("শ", "ঢ়", "ঢ়", listOf("শ", "ঢ়", "শ্")),
        ProbhatKey("চ", "ছ", "ছ", listOf("চ", "ছ", "চ্চ")),
        ProbhatKey("আ", "ঋ", "ঋ", listOf("আ", "ঋ", "অ")),
        ProbhatKey("ব", "ভ", "ভ", listOf("ব", "ভ", "ব্ব")),
        ProbhatKey("ন", "ণ", "ণ", listOf("ন", "ণ", "ন্")),
        ProbhatKey("ম", "ঙ", "ঙ", listOf("ম", "ঙ", "ম্ম")),
        ProbhatKey("ৃ", "<", "<", listOf("ৃ", "<", "ঋ")),
        ProbhatKey("্", "ঁ", "ঁ", listOf("্", "ঁ", "ঃ", "ং"))
    )

    // Bangla punctuation popups for Comma and Daari keys
    val commaAlternates = listOf(",", "!", "?", "\"", "'", "-", ";", ":")
    val dariAlternates = listOf("।", "॥", ".", "?", "!", ":", ";", "/", "@")
}
