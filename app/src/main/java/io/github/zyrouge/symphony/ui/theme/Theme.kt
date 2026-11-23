package io.github.zyrouge.symphony.ui.theme

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.i18n.rememberStrings
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorMode
import io.github.zyrouge.symphony.ui.theme.bento.toBentoColorMode

enum class ThemeMode {
    SYSTEM,
    SYSTEM_BLACK,
    LIGHT,
    DARK,
    BLACK,
}

enum class ColorSchemeMode {
    LIGHT,
    DARK,
    BLACK
}

@Composable
fun SymphonyTheme(
    context: ViewContext,
    content: @Composable () -> Unit,
) {
    // The Bento theme mode drives the remaining Material components and the system bar icons,
    // so they match the Bento surfaces instead of the old theme setting.
    val bentoThemeMode by context.symphony.settings.bentoThemeMode.flow.collectAsState()
    val primaryColorName by context.symphony.settings.primaryColor.flow.collectAsState()
    val fontName by context.symphony.settings.fontFamily.flow.collectAsState()
    val fontScale by context.symphony.settings.fontScale.flow.collectAsState()
    val contentScale by context.symphony.settings.contentScale.flow.collectAsState()

    val colorSchemeMode = when (bentoThemeMode.toBentoColorMode(isSystemInDarkTheme())) {
        BentoColorMode.LIGHT -> ColorSchemeMode.LIGHT
        BentoColorMode.DARK -> ColorSchemeMode.DARK
        BentoColorMode.BLACK -> ColorSchemeMode.BLACK
    }
    val primaryColor = ThemeColors.resolvePrimaryColor(primaryColorName)
    val colorScheme = when (colorSchemeMode) {
        ColorSchemeMode.LIGHT -> ThemeColorSchemes.createLightColorScheme(primaryColor)
        ColorSchemeMode.DARK -> ThemeColorSchemes.createDarkColorScheme(primaryColor)
        ColorSchemeMode.BLACK -> ThemeColorSchemes.createBlackColorScheme(primaryColor)
    }

    val strings = rememberStrings(
        language = context.symphony.settings.language.value ?: java.util.Locale.getDefault().language,
    )
    context.symphony.t = strings

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        SideEffect {
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = colorSchemeMode == ColorSchemeMode.LIGHT
                isAppearanceLightNavigationBars = colorSchemeMode == ColorSchemeMode.LIGHT
            }
        }
    }

    val selectedLanguage = context.symphony.settings.language.value
    val textDirection = when {
        // "System": the platform's layout direction already follows the system locale.
        selectedLanguage == null -> TextDirection.Unspecified
        io.github.zyrouge.symphony.services.locales.localeDirection(selectedLanguage) == "rtl" ->
            TextDirection.Rtl

        else -> TextDirection.Ltr
    }
    val typography = SymphonyTypography.toTypography(
        SymphonyTypography.resolveFont(fontName),
        textDirection,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = {
            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalDensity provides Density(
                    LocalDensity.current.density * contentScale,
                    LocalDensity.current.fontScale * fontScale,
                )
            ) {
                content()
            }
        }
    )
}

fun ThemeMode.toColorSchemeMode(symphony: Symphony): ColorSchemeMode {
    val isSystemInDarkTheme = symphony.applicationContext.resources.configuration.uiMode.let {
        (it and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    return toColorSchemeMode(isSystemInDarkTheme)
}

fun ThemeMode.toColorSchemeMode(isSystemInDarkTheme: Boolean) = when (this) {
    ThemeMode.SYSTEM -> if (isSystemInDarkTheme) ColorSchemeMode.DARK else ColorSchemeMode.LIGHT
    ThemeMode.SYSTEM_BLACK -> if (isSystemInDarkTheme) ColorSchemeMode.BLACK else ColorSchemeMode.LIGHT
    ThemeMode.LIGHT -> ColorSchemeMode.LIGHT
    ThemeMode.DARK -> ColorSchemeMode.DARK
    ThemeMode.BLACK -> ColorSchemeMode.BLACK
}

fun ColorSchemeMode.isLight() = this == ColorSchemeMode.LIGHT
