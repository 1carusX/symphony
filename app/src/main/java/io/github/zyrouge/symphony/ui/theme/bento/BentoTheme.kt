package io.github.zyrouge.symphony.ui.theme.bento

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
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
 * (not per recomposition) and exposes them through [LocalBentoColors]. A
 * non-null [hueDegrees] (wallpaper colors) replaces [hue].
 */
@Composable
fun BentoTheme(
    mode: BentoThemeMode,
    scheme: BentoColorScheme,
    hue: BentoPrimaryHue,
    hueDegrees: Float? = null,
    content: @Composable () -> Unit,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val colorMode = remember(mode, isSystemInDarkTheme) {
        mode.toBentoColorMode(isSystemInDarkTheme)
    }
    val colors = remember(scheme, colorMode, hue, hueDegrees) {
        when (hueDegrees) {
            null -> BentoColorRoles.resolve(scheme, colorMode, hue)
            else -> BentoColorRoles.resolve(scheme, colorMode, hueDegrees)
        }
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

/**
 * The hue of the system's wallpaper-derived accent (Android 12+), or null on
 * older versions, where there are no wallpaper colors to follow.
 */
fun bentoWallpaperHue(context: Context): Float? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
        bentoHsl(Color(context.getColor(android.R.color.system_accent1_500)))[0]

    else -> null
}
