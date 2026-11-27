package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import kotlinx.coroutines.delay

/** One language choice; the empty code is the system language. */
@Immutable
data class AppearanceLanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String?,
)

/** One font choice: its setting id and the name shown on the chip. */
@Immutable
data class AppearanceFontOption(
    val id: String,
    val label: String,
)

private enum class AppearancePage {
    LANGUAGE,
}

/** The text and content sizes offered as chips. */
private val SCALE_STOPS = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f)

private fun formatScale(value: Float): String =
    when {
        value % 1f == 0f -> "${value.toInt()}×"
        else -> "$value×"
    }

/**
 * The Appearance settings in a sheet: language, font, and the font/content
 * scale chips. The language list opens as a page of the same sheet.
 */
@Composable
fun BentoAppearanceSettingsSheet(
    strings: Strings,
    onDismissRequest: () -> Unit,
    languages: List<AppearanceLanguageOption>,
    selectedLanguage: String?,
    onLanguageChange: (String?) -> Unit,
    fonts: List<AppearanceFontOption>,
    selectedFont: String?,
    onFontChange: (String) -> Unit,
    fontFamilyOf: (@Composable (String) -> FontFamily?)? = null,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    contentScale: Float,
    onContentScaleChange: (Float) -> Unit,
    themeMode: BentoThemeMode? = null,
    onThemeModeChange: ((BentoThemeMode) -> Unit)? = null,
    colorScheme: BentoColorScheme? = null,
    onColorSchemeChange: ((BentoColorScheme) -> Unit)? = null,
    primaryHue: BentoPrimaryHue? = null,
    onPrimaryHueChange: ((BentoPrimaryHue) -> Unit)? = null,
) {
    var page by remember { mutableStateOf<AppearancePage?>(null) }
    val languageValue = selectedLanguage ?: ""
    val selectedLanguageLabel = languages.firstOrNull { it.code == languageValue }?.nativeName
        ?: languageValue

    BentoSheetDialog(
        title = when (page) {
            AppearancePage.LANGUAGE -> strings.Language_
            null -> strings.Appearance
        },
        onDismissRequest = onDismissRequest,
        onBack = page?.let { { page = null } },
        actions = {
            BentoSheetButton(
                label = strings.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        BentoSheetPages(page) { current ->
            when (current) {
                null -> {
                    if (themeMode != null && onThemeModeChange != null) {
                        BentoSheetChoiceRow(
                            title = strings.Theme,
                            options = BentoThemeMode.entries.map { themeModeLabel(it, strings) },
                            selectedIndex = BentoThemeMode.entries.indexOf(themeMode),
                            onSelect = { onThemeModeChange(BentoThemeMode.entries[it]) },
                        )
                    }
                    if (colorScheme != null && onColorSchemeChange != null) {
                        BentoSheetChoiceRow(
                            title = strings.ColorScheme,
                            options = BentoColorScheme.entries.map {
                                colorSchemeLabel(it, strings)
                            },
                            selectedIndex = BentoColorScheme.entries.indexOf(colorScheme),
                            onSelect = { onColorSchemeChange(BentoColorScheme.entries[it]) },
                        )
                    }
                    if (primaryHue != null && onPrimaryHueChange != null) {
                        BentoSheetChoiceRow(
                            title = strings.PrimaryColor,
                            options = BentoPrimaryHue.entries.map {
                                primaryHueLabel(it, strings)
                            },
                            selectedIndex = BentoPrimaryHue.entries.indexOf(primaryHue),
                            onSelect = { onPrimaryHueChange(BentoPrimaryHue.entries[it]) },
                        )
                    }
                    BentoSheetValueRow(
                        title = strings.Language_,
                        value = selectedLanguageLabel,
                        onClick = { page = AppearancePage.LANGUAGE },
                    )
                    if (fonts.isNotEmpty()) {
                        BentoSheetChoiceRow(
                            title = strings.Font,
                            options = fonts.map { it.label },
                            selectedIndex = fonts.indexOfFirst { it.id == selectedFont },
                            onSelect = { onFontChange(fonts[it].id) },
                            optionFontFamily = fontFamilyOf?.let { provider ->
                                { index -> provider(fonts[index].id) }
                            },
                        )
                    }
                    BentoSheetChoiceRow(
                        title = strings.FontScale,
                        options = SCALE_STOPS.map(::formatScale),
                        selectedIndex = bentoNearestStop(SCALE_STOPS, fontScale),
                        onSelect = { onFontScaleChange(SCALE_STOPS[it]) },
                    )
                    BentoSheetChoiceRow(
                        title = strings.ContentScale,
                        options = SCALE_STOPS.map(::formatScale),
                        selectedIndex = bentoNearestStop(SCALE_STOPS, contentScale),
                        onSelect = { onContentScaleChange(SCALE_STOPS[it]) },
                    )
                }

                AppearancePage.LANGUAGE -> AppearanceLanguagePage(
                    languages = languages,
                    selected = languageValue,
                    onSelect = { code ->
                        onLanguageChange(code.takeUnless { it.isEmpty() })
                        page = null
                    },
                )
            }
        }
    }
}

/**
 * The language page: a search field, then each language by its own name with
 * its English name under it. The current one is scrolled into view on open.
 */
@Composable
private fun AppearanceLanguagePage(
    languages: List<AppearanceLanguageOption>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val shown = languages.filter { language ->
        val q = query.text.trim()
        q.isEmpty() ||
            language.nativeName.contains(q, ignoreCase = true) ||
            language.englishName?.contains(q, ignoreCase = true) == true
    }
    val selectedRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(Unit) {
        // After the page has slid in, so the sheet knows its height.
        delay(260)
        selectedRequester.bringIntoView()
    }

    BentoSheetTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = LocalStrings.current.SearchLanguages,
        leadingIcon = Icons.Rounded.Search,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
    shown.forEach { language ->
        BentoSheetOptionRow(
            title = language.nativeName,
            subtitle = language.englishName,
            selected = language.code == selected,
            onClick = { onSelect(language.code) },
            modifier = when {
                language.code == selected -> Modifier.bringIntoViewRequester(selectedRequester)
                else -> Modifier
            },
        )
    }
}

private fun themeModeLabel(value: BentoThemeMode, strings: Strings) = when (value) {
    BentoThemeMode.SYSTEM -> strings.System
    BentoThemeMode.LIGHT -> strings.Light
    BentoThemeMode.DARK -> strings.Dark
    BentoThemeMode.BLACK -> strings.Black
}

private fun colorSchemeLabel(value: BentoColorScheme, strings: Strings) = when (value) {
    BentoColorScheme.TINTED -> strings.Tinted
    BentoColorScheme.NEUTRAL -> strings.Neutral
    BentoColorScheme.VIVID -> strings.Vivid
    BentoColorScheme.CONTRAST -> strings.Contrast
}

private fun primaryHueLabel(value: BentoPrimaryHue, strings: Strings) = when (value) {
    BentoPrimaryHue.ROSE -> strings.Rose
    BentoPrimaryHue.BLUE -> strings.Blue
    BentoPrimaryHue.GREEN -> strings.Green
    BentoPrimaryHue.AMBER -> strings.Amber
    BentoPrimaryHue.VIOLET -> strings.Violet
}
