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
    symphonyLocales.firstOrNull { it.code == code }?.nativeName ?: code

fun localeDirection(code: String?): String =
    symphonyLocales.firstOrNull { it.code == code }?.direction ?: "ltr"
