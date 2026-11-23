package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.locales.localeNativeName
import io.github.zyrouge.symphony.services.locales.localeTag
import io.github.zyrouge.symphony.services.locales.symphonyLocales
import io.github.zyrouge.symphony.ui.components.BentoOptionDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetActionRow
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSliderDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import kotlin.math.roundToInt

private enum class AppearancePopup {
    LANGUAGE,
    FONT,
    FONT_SCALE,
    CONTENT_SCALE,
}

private val SCALE_RANGE = 0.5f..2f

/**
 * The Appearance settings (language, font and scaling) in a sheet over the
 * settings page rather than a page of their own, so wide windows do not
 * stretch the rows. Each value opens its picker or slider dialog.
 */
@Composable
fun AppearanceSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val language by context.symphony.settings.language.flow.collectAsState()
    val fontFamily by context.symphony.settings.fontFamily.flow.collectAsState()
    val fontScale by context.symphony.settings.fontScale.flow.collectAsState()
    val contentScale by context.symphony.settings.contentScale.flow.collectAsState()
    var popup by remember { mutableStateOf<AppearancePopup?>(null) }

    val languages = remember {
        val defaultName = localeNativeName(java.util.Locale.getDefault().language)
        listOf("" to "System ($defaultName)") +
            symphonyLocales.map { it.code to it.nativeName }
    }
    val languageValue = language ?: ""

    BentoSheetDialog(
        title = context.symphony.t.Appearance,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        BentoSheetActionRow(
            title = context.symphony.t.Language_,
            subtitle = languages.firstOrNull { it.first == languageValue }?.second ?: languageValue,
            onClick = { popup = AppearancePopup.LANGUAGE },
        )
        BentoSheetActionRow(
            title = context.symphony.t.Font,
            subtitle = SymphonyTypography.resolveFont(fontFamily).fontName,
            onClick = { popup = AppearancePopup.FONT },
        )
        BentoSheetActionRow(
            title = context.symphony.t.FontScale,
            subtitle = "x$fontScale",
            onClick = { popup = AppearancePopup.FONT_SCALE },
        )
        BentoSheetActionRow(
            title = context.symphony.t.ContentScale,
            subtitle = "x$contentScale",
            onClick = { popup = AppearancePopup.CONTENT_SCALE },
        )
    }

    when (popup) {
        AppearancePopup.LANGUAGE -> BentoOptionDialog(
            title = context.symphony.t.Language_,
            options = languages.map { it.second to (it.first == languageValue) },
            onSelect = { index ->
                val key = languages[index].first
                context.symphony.settings.language.setValue(key.takeUnless { it == "" })
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
                popup = null
            },
            onDismiss = { popup = null },
        )

        AppearancePopup.FONT -> BentoOptionDialog(
            title = context.symphony.t.Font,
            options = SymphonyTypography.all.keys.map {
                it to (it == SymphonyTypography.resolveFont(fontFamily).fontName)
            },
            onSelect = { index ->
                context.symphony.settings.fontFamily.setValue(SymphonyTypography.all.keys.toList()[index])
                popup = null
            },
            onDismiss = { popup = null },
        )

        AppearancePopup.FONT_SCALE -> BentoSliderDialog(
            title = context.symphony.t.FontScale,
            value = fontScale,
            range = SCALE_RANGE,
            onValue = { (it * 10).roundToInt() / 10f },
            label = { "x${(it * 10).roundToInt() / 10f}" },
            onChange = { context.symphony.settings.fontScale.setValue((it * 10).roundToInt() / 10f) },
            onReset = {
                context.symphony.settings.fontScale.setValue(
                    context.symphony.settings.fontScale.defaultValue,
                )
            },
            onDismiss = { popup = null },
        )

        AppearancePopup.CONTENT_SCALE -> BentoSliderDialog(
            title = context.symphony.t.ContentScale,
            value = contentScale,
            range = SCALE_RANGE,
            onValue = { (it * 10).roundToInt() / 10f },
            label = { "x${(it * 10).roundToInt() / 10f}" },
            onChange = {
                context.symphony.settings.contentScale.setValue((it * 10).roundToInt() / 10f)
            },
            onReset = {
                context.symphony.settings.contentScale.setValue(
                    context.symphony.settings.contentScale.defaultValue,
                )
            },
            onDismiss = { popup = null },
        )

        null -> Unit
    }
}
