package io.github.zyrouge.symphony.services.locales

data class LocaleInfo(
    val code: String,
    val nativeName: String,
    val direction: String,
    val qualifier: String = code,
)

val symphonyLocales = listOf(
    LocaleInfo("be", "беларуская", "ltr"),
    LocaleInfo("de", "Deutsch", "ltr"),
    LocaleInfo("en", "English", "ltr"),
    LocaleInfo("es", "español", "ltr"),
    LocaleInfo("fa", "فارسی", "rtl"),
    LocaleInfo("fi", "suomi", "ltr"),
    LocaleInfo("fr", "français", "ltr"),
    LocaleInfo("it", "italiano", "ltr"),
    LocaleInfo("ja", "日本語", "ltr"),
    LocaleInfo("pl", "polski", "ltr"),
    LocaleInfo("pt", "português", "ltr"),
    LocaleInfo("ro", "română", "ltr"),
    LocaleInfo("ru", "русский", "ltr"),
    LocaleInfo("ryu", "うちなーぐち", "ltr"),
    LocaleInfo("tr", "Türkçe", "ltr"),
    LocaleInfo("uk", "українська", "ltr"),
    LocaleInfo("vi", "Tiếng Việt", "ltr"),
    LocaleInfo("zh-Hans", "中文 (简体)", "ltr", qualifier = "zh-rCN"),
)

fun localeNativeName(code: String): String =
    (symphonyLocales.firstOrNull { it.code == code }
        // The system reports a bare language ("zh") for codes declared with a script ("zh-Hans").
        ?: symphonyLocales.firstOrNull { it.code.substringBefore('-') == code })
        ?.nativeName ?: code

/**
 * BCP 47 tag for a locale code. Codes whose resources live under a region
 * qualifier use that region (zh-Hans -> zh-CN), so the values-zh-rCN chunk
 * matches; a tag without a region would fall back to English.
 */
fun localeTag(code: String): String =
    symphonyLocales.firstOrNull { it.code == code }?.qualifier?.replace("-r", "-") ?: code

fun localeDirection(code: String?): String =
    symphonyLocales.firstOrNull { it.code == code }?.direction ?: "ltr"
