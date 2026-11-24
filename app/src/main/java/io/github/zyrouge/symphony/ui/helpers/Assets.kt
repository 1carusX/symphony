package io.github.zyrouge.symphony.ui.helpers

import android.content.ContentResolver
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorMode
import io.github.zyrouge.symphony.ui.theme.bento.toBentoColorMode

object Assets {
    val placeholderDarkId = R.raw.placeholder_dark
    val placeholderLightId = R.raw.placeholder_light

    private fun getPlaceholderId(isLight: Boolean = false) = when {
        isLight -> placeholderLightId
        else -> placeholderDarkId
    }

    fun getPlaceholderId(symphony: Symphony): Int {
        val systemDark = symphony.applicationContext.resources.configuration.uiMode.let {
            (it and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        val mode = symphony.settings.bentoThemeMode.value.toBentoColorMode(systemDark)
        return getPlaceholderId(isLight = mode == BentoColorMode.LIGHT)
    }

    fun getPlaceholderUri(symphony: Symphony) = buildUriOfResource(
        symphony.applicationContext.resources,
        getPlaceholderId(symphony),
    )

    fun createPlaceholderImageRequest(symphony: Symphony) =
        ImageRequest.Builder(symphony.applicationContext)
            .data(getPlaceholderUri(symphony))

    private fun buildUriOfResource(resources: Resources, resourceId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
    }
}
