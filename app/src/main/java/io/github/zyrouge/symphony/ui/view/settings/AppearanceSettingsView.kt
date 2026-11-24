package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.locales.localeNativeName
import io.github.zyrouge.symphony.services.locales.localeTag
import io.github.zyrouge.symphony.services.locales.symphonyLocales
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetChoiceRow
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetOptionRow
import io.github.zyrouge.symphony.ui.components.BentoSheetPages
import io.github.zyrouge.symphony.ui.components.BentoSheetTextField
import io.github.zyrouge.symphony.ui.components.BentoSheetValueRow
import io.github.zyrouge.symphony.ui.components.bentoNearestStop
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.delay

private enum class AppearancePage {
    LANGUAGE,
}

/** The text and content sizes offered as chips. */
private val SCALE_STOPS = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f)

/** "1×", "1.15×": a scale with at most two decimals. */
private fun formatScale(value: Float) =
    BigDecimal(value.toDouble()).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "×"

/**
 * The Appearance settings in a sheet over the settings page. Font and the two
 * scales are set in place; the language list, too long for that, opens as a
 * page of the same sheet rather than a dialog on top.
 */
@Composable
fun AppearanceSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val language by settings.language.flow.collectAsState()
    val fontFamily by settings.fontFamily.flow.collectAsState()
    val fontScale by settings.fontScale.flow.collectAsState()
    val contentScale by settings.contentScale.flow.collectAsState()
    var page by remember { mutableStateOf<AppearancePage?>(null) }

    val languages = remember {
        val defaultName = localeNativeName(java.util.Locale.getDefault().language)
        listOf("" to context.symphony.t.SystemX(defaultName)) +
            symphonyLocales.map { it.code to it.nativeName }
    }
    val languageValue = language ?: ""
    val fontNames = SymphonyTypography.all.keys.toList()

    BentoSheetDialog(
        title = when (page) {
            AppearancePage.LANGUAGE -> context.symphony.t.Language_
            null -> context.symphony.t.Appearance
        },
        onDismissRequest = onDismissRequest,
        onBack = page?.let { { page = null } },
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        BentoSheetPages(page) { current ->
            when (current) {
                null -> {
                    BentoSheetValueRow(
                        title = context.symphony.t.Language_,
                        value = languages.firstOrNull { it.first == languageValue }?.second
                            ?: languageValue,
                        onClick = { page = AppearancePage.LANGUAGE },
                    )
                    BentoSheetChoiceRow(
                        title = context.symphony.t.Font,
                        options = fontNames,
                        selectedIndex = fontNames.indexOf(SymphonyTypography.resolveFont(fontFamily).fontName),
                        onSelect = { settings.fontFamily.setValue(fontNames[it]) },
                        // Each font's name is set in that font, so the choice previews itself.
                        optionFontFamily = { SymphonyTypography.all[fontNames[it]]?.fontFamily?.invoke() },
                    )
                    // A few fixed sizes, so chips: the whole app resizes on a tap, which is the preview.
                    BentoSheetChoiceRow(
                        title = context.symphony.t.FontScale,
                        options = SCALE_STOPS.map(::formatScale),
                        selectedIndex = bentoNearestStop(SCALE_STOPS, fontScale),
                        onSelect = { settings.fontScale.setValue(SCALE_STOPS[it]) },
                    )
                    BentoSheetChoiceRow(
                        title = context.symphony.t.ContentScale,
                        options = SCALE_STOPS.map(::formatScale),
                        selectedIndex = bentoNearestStop(SCALE_STOPS, contentScale),
                        onSelect = { settings.contentScale.setValue(SCALE_STOPS[it]) },
                    )
                }

                AppearancePage.LANGUAGE -> LanguagePage(
                    languages = languages,
                    selected = languageValue,
                    onSelect = { key ->
                        settings.language.setValue(key.takeUnless { it == "" })
                        val activity = context.activity
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            // "System" clears the per-app locale instead of forcing English.
                            activity.getSystemService(android.app.LocaleManager::class.java)
                                .applicationLocales = when {
                                key.isEmpty() -> android.os.LocaleList.getEmptyLocaleList()
                                else -> android.os.LocaleList.forLanguageTags(localeTag(key))
                            }
                        } else {
                            // MainActivity.attachBaseContext applies the saved language on every start.
                            activity.recreate()
                        }
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
private fun LanguagePage(
    languages: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val englishNames = remember(languages) {
        languages.associate { (key, name) ->
            key to when (key) {
                "" -> null
                else -> java.util.Locale.forLanguageTag(localeTag(key))
                    .getDisplayName(java.util.Locale.ENGLISH)
                    .takeIf { it.isNotEmpty() && !it.equals(name, ignoreCase = true) }
            }
        }
    }
    val shown = languages.filter { (key, name) ->
        val q = query.text.trim()
        q.isEmpty() || name.contains(q, ignoreCase = true) ||
            englishNames[key]?.contains(q, ignoreCase = true) == true
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
    shown.forEach { (key, name) ->
        BentoSheetOptionRow(
            title = name,
            subtitle = englishNames[key],
            selected = key == selected,
            onClick = { onSelect(key) },
            modifier = if (key == selected) Modifier.bringIntoViewRequester(selectedRequester) else Modifier,
        )
    }
}
