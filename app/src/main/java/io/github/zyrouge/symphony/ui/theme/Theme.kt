package io.github.zyrouge.symphony.ui.theme

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.i18n.rememberStrings
import io.github.zyrouge.symphony.ui.components.BentoAppDialog
import io.github.zyrouge.symphony.ui.components.BentoPlatformMetrics
import io.github.zyrouge.symphony.ui.components.LocalBentoBackHandler
import io.github.zyrouge.symphony.ui.components.LocalBentoDialogHost
import io.github.zyrouge.symphony.ui.components.LocalBentoPlatformMetrics
import io.github.zyrouge.symphony.ui.components.bentoSystemBottomInset
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
        language = context.symphony.settings.language.value
            ?: LocalConfiguration.current.locales[0].language,
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
            val appDensity = Density(
                LocalDensity.current.density * contentScale,
                LocalDensity.current.fontScale * fontScale,
            )
            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalDensity provides appDensity,
                LocalBentoAppDensity provides appDensity,
                LocalBentoPlatformMetrics provides BentoPlatformMetrics(
                    bottomInset = bentoSystemBottomInset(),
                    topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    windowScale = bentoAppScale(),
                ),
                LocalBentoBackHandler provides { enabled, onBack ->
                    BackHandler(enabled = enabled, onBack = onBack)
                },
                LocalBentoDialogHost provides { window, onDismissRequest, content ->
                    BentoAppDialog(
                        onDismissRequest = onDismissRequest,
                        properties = DialogProperties(
                            dismissOnBackPress = window.dismissOnBackPress,
                            dismissOnClickOutside = window.dismissOnClickOutside,
                            usePlatformDefaultWidth = window.usePlatformDefaultWidth,
                            decorFitsSystemWindows = !window.edgeToEdge,
                        ),
                    ) {
                        if (window.edgeToEdge) {
                            val view = LocalView.current
                            LaunchedEffect(view) {
                                (view as? DialogWindowProvider)?.window?.setDimAmount(0f)
                            }
                        }
                        content()
                    }
                },
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
