package io.github.zyrouge.symphony.ui.theme.bento

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color

/**
 * The hue of the system's wallpaper-derived accent (Android 12+), or null on
 * older versions, where there are no wallpaper colors to follow.
 */
fun bentoWallpaperHue(context: Context): Float? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
        bentoHsl(Color(context.getColor(android.R.color.system_accent1_500)))[0]

    else -> null
}
