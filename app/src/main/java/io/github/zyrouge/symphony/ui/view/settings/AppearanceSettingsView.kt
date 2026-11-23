package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.locales.localeNativeName
import io.github.zyrouge.symphony.services.locales.symphonyLocales
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.BentoOptionDialog
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.components.BentoSettingGroup
import io.github.zyrouge.symphony.ui.components.BentoSettingRow
import io.github.zyrouge.symphony.ui.components.BentoSliderDialog
import io.github.zyrouge.symphony.ui.components.BentoToggleDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.SymphonyTypography
import io.github.zyrouge.symphony.ui.theme.ThemeMode
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.theme.PrimaryThemeColor
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
object AppearanceSettingsViewRoute

private enum class AppearancePopup {
    LANGUAGE,
    FONT,
    FONT_SCALE,
    CONTENT_SCALE,
    THEME,
    MATERIAL_YOU,
    PRIMARY_COLOR,
}

private val SCALE_RANGE = 0.5f..2f

@Composable
fun AppearanceSettingsView(context: ViewContext) {
    val scrollState = rememberScrollState()
    val windowClass = rememberBentoWindowClass()
    val colors = LocalBentoColors.current
    val language by context.symphony.settings.language.flow.collectAsState()
    val fontFamily by context.symphony.settings.fontFamily.flow.collectAsState()
    val themeMode by context.symphony.settings.themeMode.flow.collectAsState()
    val useMaterialYou by context.symphony.settings.useMaterialYou.flow.collectAsState()
    val primaryColor by context.symphony.settings.primaryColor.flow.collectAsState()
    val fontScale by context.symphony.settings.fontScale.flow.collectAsState()
    val contentScale by context.symphony.settings.contentScale.flow.collectAsState()
    var popup by remember { mutableStateOf<AppearancePopup?>(null) }

    val languages = remember {
        val defaultName = localeNativeName(java.util.Locale.getDefault().language)
        listOf("" to "System ($defaultName)") +
            symphonyLocales.map { it.code to it.nativeName }
    }
    val languageValue = language ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState),
        ) {
            BentoPageHeader(
                title = context.symphony.t.Appearance,
                windowClass = windowClass,
                onBack = { context.navController.popBackStack() },
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BentoSettingGroup {
                    BentoSettingRow(
                        label = context.symphony.t.Language_,
                        value = languages.firstOrNull { it.first == languageValue }?.second
                            ?: languageValue,
                        onClick = { popup = AppearancePopup.LANGUAGE },
                    )
                }
                BentoSettingGroup {
                    BentoSettingRow(
                        label = context.symphony.t.Font,
                        value = SymphonyTypography.resolveFont(fontFamily).fontName,
                        onClick = { popup = AppearancePopup.FONT },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.FontScale,
                        value = "x$fontScale",
                        onClick = { popup = AppearancePopup.FONT_SCALE },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.ContentScale,
                        value = "x$contentScale",
                        onClick = { popup = AppearancePopup.CONTENT_SCALE },
                    )
                }
                BentoSettingGroup {
                    BentoSettingRow(
                        label = context.symphony.t.Theme,
                        value = themeMode.label(context),
                        onClick = { popup = AppearancePopup.THEME },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.MaterialYou,
                        value = if (useMaterialYou) "On" else "Off",
                        onClick = { popup = AppearancePopup.MATERIAL_YOU },
                    )
                    BentoSettingRow(
                        label = context.symphony.t.PrimaryColor,
                        value = PrimaryThemeColor.entries
                            .firstOrNull { it.name == primaryColor }
                            ?.label(context)
                            ?: "", 
                        onClick = { popup = AppearancePopup.PRIMARY_COLOR },
                    )
                }
                Box(Modifier.bentoGestureBarPadding())
            }
        }
    }

    when (popup) {
        AppearancePopup.LANGUAGE -> BentoOptionDialog(
            title = context.symphony.t.Language_,
            options = languages.map { it.second to (it.first == languageValue) },
            onSelect = { index ->
                val key = languages[index].first
                context.symphony.settings.language.setValue(key.takeUnless { it == "" })
                @Suppress("DEPRECATION")
                run {
                    val activity = context.activity
                    val tag = key.ifEmpty { "en" }
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        activity.getSystemService(android.app.LocaleManager::class.java)
                            .applicationLocales = android.os.LocaleList.forLanguageTags(tag)
                    } else {
                        val locale = java.util.Locale.forLanguageTag(tag)
                        val configuration =
                            android.content.res.Configuration(activity.resources.configuration)
                        configuration.setLocale(locale)
                        activity.resources.updateConfiguration(
                            configuration,
                            activity.resources.displayMetrics,
                        )
                        activity.recreate()
                    }
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

        AppearancePopup.THEME -> BentoOptionDialog(
            title = context.symphony.t.Theme,
            options = themeOptions(context).map { it.first to (it.second == themeMode) },
            onSelect = { index ->
                context.symphony.settings.themeMode.setValue(themeOptions(context)[index].second)
                popup = null
            },
            onDismiss = { popup = null },
        )

        AppearancePopup.MATERIAL_YOU -> BentoToggleDialog(
            title = context.symphony.t.MaterialYou,
            checked = useMaterialYou,
            onChange = { context.symphony.settings.useMaterialYou.setValue(it) },
            onDismiss = { popup = null },
        )

        AppearancePopup.PRIMARY_COLOR -> BentoOptionDialog(
            title = context.symphony.t.PrimaryColor,
            options = PrimaryThemeColor.entries.map {
                it.label(context) to (it.name == primaryColor)
            },
            onSelect = { index ->
                context.symphony.settings.primaryColor.setValue(
                    PrimaryThemeColor.entries[index].name,
                )
                popup = null
            },
            onDismiss = { popup = null },
        )

        null -> Unit
    }
}

private fun themeOptions(context: ViewContext) = listOf(
    context.symphony.t.SystemLightDark to ThemeMode.SYSTEM,
    context.symphony.t.SystemLightBlack to ThemeMode.SYSTEM_BLACK,
    context.symphony.t.Light to ThemeMode.LIGHT,
    context.symphony.t.Dark to ThemeMode.DARK,
    context.symphony.t.Black to ThemeMode.BLACK,
)

private fun ThemeMode.label(context: ViewContext) = when (this) {
    ThemeMode.SYSTEM -> context.symphony.t.SystemLightDark
    ThemeMode.SYSTEM_BLACK -> context.symphony.t.SystemLightBlack
    ThemeMode.LIGHT -> context.symphony.t.Light
    ThemeMode.DARK -> context.symphony.t.Dark
    ThemeMode.BLACK -> context.symphony.t.Black
}

fun PrimaryThemeColor.label(context: ViewContext) = when (this) {
    PrimaryThemeColor.Red -> context.symphony.t.Red
    PrimaryThemeColor.Orange -> context.symphony.t.Orange
    PrimaryThemeColor.Amber -> context.symphony.t.Amber
    PrimaryThemeColor.Yellow -> context.symphony.t.Yellow
    PrimaryThemeColor.Lime -> context.symphony.t.Lime
    PrimaryThemeColor.Green -> context.symphony.t.Green
    PrimaryThemeColor.Emerald -> context.symphony.t.Emerald
    PrimaryThemeColor.Teal -> context.symphony.t.Teal
    PrimaryThemeColor.Cyan -> context.symphony.t.Cyan
    PrimaryThemeColor.Sky -> context.symphony.t.Sky
    PrimaryThemeColor.Blue -> context.symphony.t.Blue
    PrimaryThemeColor.Indigo -> context.symphony.t.Indigo
    PrimaryThemeColor.Violet -> context.symphony.t.Violet
    PrimaryThemeColor.Purple -> context.symphony.t.Purple
    PrimaryThemeColor.Fuchsia -> context.symphony.t.Fuchsia
    PrimaryThemeColor.Pink -> context.symphony.t.Pink
    PrimaryThemeColor.Rose -> context.symphony.t.Rose
}
