package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.services.locales.symphonyLocales

/**
 * The language list of the appearance sheet: "system" first, then every
 * bundled locale with its native name and, when it differs, its English name.
 * English names need a platform Locale lookup, so the caller supplies
 * [englishNameOf].
 */
fun appearanceLanguageOptions(
    systemLanguageName: String,
    englishNameOf: (code: String) -> String?,
): List<AppearanceLanguageOption> = listOf(
    AppearanceLanguageOption(
        code = "",
        nativeName = systemLanguageName,
        englishName = null,
    ),
) + symphonyLocales.map { info ->
    AppearanceLanguageOption(
        code = info.code,
        nativeName = info.nativeName,
        englishName = englishNameOf(info.code)
            ?.takeIf { it.isNotBlank() && !it.equals(info.nativeName, ignoreCase = true) },
    )
}
