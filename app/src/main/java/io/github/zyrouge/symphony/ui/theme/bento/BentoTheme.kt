package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.zyrouge.symphony.ui.components.BentoTileContentColors
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors

/**
 * Resolved [BentoColors] of the enclosing [BentoTheme].
 */
val LocalBentoColors = staticCompositionLocalOf<BentoColors> {
    error("BentoColors is not provided; wrap content in BentoTheme")
}

/**
 * Color scheme of the enclosing [BentoTheme]. Tiles read it for the Contrast
 * ring; the Settings theme tile reads it for the preview row.
 */
val LocalBentoColorScheme = staticCompositionLocalOf<BentoColorScheme> {
    error("BentoColorScheme is not provided; wrap content in BentoTheme")
}

/**
 * Resolved color mode (light/dark/black; SYSTEM has become LIGHT or DARK) of
 * the enclosing [BentoTheme].
 */
val LocalBentoColorMode = staticCompositionLocalOf<BentoColorMode> {
    error("BentoColorMode is not provided; wrap content in BentoTheme")
}

/**
 * Applies the bento theme: resolves the color roles once per selection change
 * (not per recomposition) and exposes them through [LocalBentoColors]. Not yet
 * wired into any screen; later phases host it and connect the settings state.
 */
@Composable
fun BentoTheme(
    mode: BentoThemeMode,
    scheme: BentoColorScheme,
    hue: BentoPrimaryHue,
    content: @Composable () -> Unit,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val colorMode = remember(mode, isSystemInDarkTheme) {
        mode.toBentoColorMode(isSystemInDarkTheme)
    }
    val colors = remember(scheme, colorMode, hue) {
        BentoColorRoles.resolve(scheme, colorMode, hue)
    }
    CompositionLocalProvider(
        LocalBentoColors provides colors,
        LocalBentoColorScheme provides scheme,
        LocalBentoColorMode provides colorMode,
        LocalBentoTileContentColors provides BentoTileContentColors(colors.fg, colors.sub),
    ) {
        content()
    }
}

fun BentoThemeMode.toBentoColorMode(isSystemInDarkTheme: Boolean) = when (this) {
    BentoThemeMode.SYSTEM -> if (isSystemInDarkTheme) BentoColorMode.DARK else BentoColorMode.LIGHT
    BentoThemeMode.LIGHT -> BentoColorMode.LIGHT
    BentoThemeMode.DARK -> BentoColorMode.DARK
    BentoThemeMode.BLACK -> BentoColorMode.BLACK
}
