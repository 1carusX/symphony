package io.github.zyrouge.symphony.i18n

internal enum class PluralCategory {
    ONE,
    FEW,
    MANY,
    OTHER,
}

internal fun pluralCategory(language: String, count: Int): PluralCategory {
    val value = count.toLong()
    return when (language.substringBefore("-")) {
        "ru", "uk", "be" -> when {
            value % 10 == 1L && value % 100 != 11L -> PluralCategory.ONE
            value % 10 in 2L..4L && value % 100 !in 12L..14L -> PluralCategory.FEW
            else -> PluralCategory.MANY
        }

        "pl" -> when {
            value == 1L -> PluralCategory.ONE
            value % 10 in 2L..4L && value % 100 !in 12L..14L -> PluralCategory.FEW
            else -> PluralCategory.MANY
        }

        "zh", "ja", "vi", "ryu", "tr", "fa" -> PluralCategory.OTHER

        else -> if (value == 1L) PluralCategory.ONE else PluralCategory.OTHER
    }
}

internal fun pluralTemplate(
    language: String,
    count: String,
    one: String?,
    few: String? = null,
    many: String? = null,
    other: String,
): String {
    val value = count.toIntOrNull() ?: 0
    return when (pluralCategory(language, value)) {
        PluralCategory.ONE -> one ?: other
        PluralCategory.FEW -> few ?: other
        PluralCategory.MANY -> many ?: other
        PluralCategory.OTHER -> other
    }
}
