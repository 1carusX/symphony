package io.github.zyrouge.symphony.ui.view.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import io.github.zyrouge.symphony.services.locales.localeNativeName
import io.github.zyrouge.symphony.services.locales.localeTag
import io.github.zyrouge.symphony.services.locales.symphonyLocales
import io.github.zyrouge.symphony.ui.components.AppearanceFontOption
import io.github.zyrouge.symphony.ui.components.AppearanceLanguageOption
import io.github.zyrouge.symphony.ui.components.BentoAppearanceSettingsSheet
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography

/**
 * The Appearance settings in a sheet over the settings page. The sheet is
 * shared; this wrapper supplies the Android font registry and applies the
 * language through the per-app locale (or an activity recreate on older
 * versions).
 */
@Composable
fun AppearanceSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val language by settings.language.flow.collectAsState()
    val fontFamily by settings.fontFamily.flow.collectAsState()
    val fontScale by settings.fontScale.flow.collectAsState()
    val contentScale by settings.contentScale.flow.collectAsState()
    val strings = LocalStrings.current

    val systemLanguage = LocalConfiguration.current.locales[0].language
    val systemLanguageName = strings.SystemX(localeNativeName(systemLanguage))
    val languages = listOf(
        AppearanceLanguageOption(
            code = "",
            nativeName = systemLanguageName,
            englishName = null,
        ),
    ) + symphonyLocales.map { info ->
        AppearanceLanguageOption(
            code = info.code,
            nativeName = info.nativeName,
            englishName = java.util.Locale.forLanguageTag(localeTag(info.code))
                .getDisplayName(java.util.Locale.ENGLISH)
                .takeIf { it.isNotBlank() && !it.equals(info.nativeName, ignoreCase = true) },
        )
    }
    val fonts = SymphonyTypography.all.map { (id, font) ->
        AppearanceFontOption(id = id, label = font.fontName)
    }

    BentoAppearanceSettingsSheet(
        strings = strings,
        onDismissRequest = onDismissRequest,
        languages = languages,
        selectedLanguage = language,
        onLanguageChange = { code ->
            settings.language.setValue(code)
            val activity = context.activity
            if (Build.VERSION.SDK_INT >= 33) {
                // "System" clears the per-app locale instead of forcing English.
                activity.getSystemService(android.app.LocaleManager::class.java)
                    .applicationLocales = when {
                    code == null -> android.os.LocaleList.getEmptyLocaleList()
                    else -> android.os.LocaleList.forLanguageTags(localeTag(code))
                }
            } else {
                // MainActivity.attachBaseContext applies the saved language on every start.
                activity.recreate()
            }
        },
        fonts = fonts,
        selectedFont = SymphonyTypography.resolveFont(fontFamily).fontName,
        onFontChange = { settings.fontFamily.setValue(it) },
        fontFamilyOf = { id -> SymphonyTypography.all[id]?.fontFamily() },
        fontScale = fontScale,
        onFontScaleChange = { settings.fontScale.setValue(it) },
        contentScale = contentScale,
        onContentScaleChange = { settings.contentScale.setValue(it) },
    )
}
