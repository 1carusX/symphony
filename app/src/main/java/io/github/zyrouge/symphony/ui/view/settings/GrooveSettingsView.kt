package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.repeatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.helpers.TransitionDurations
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoDialog
import io.github.zyrouge.symphony.ui.components.BentoSnackbarHost
import io.github.zyrouge.symphony.ui.components.settings.BentoListAddRow
import io.github.zyrouge.symphony.ui.components.settings.BentoListEditorDialog
import io.github.zyrouge.symphony.ui.components.settings.BentoListTextAddRow
import io.github.zyrouge.symphony.ui.components.settings.BentoSystemFolderPicker
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.components.BentoOptionDialog
import io.github.zyrouge.symphony.ui.components.BentoSettingGroup
import io.github.zyrouge.symphony.ui.components.BentoSettingsColumns
import io.github.zyrouge.symphony.ui.components.BentoSettingRow
import io.github.zyrouge.symphony.ui.components.BentoSliderDialog
import io.github.zyrouge.symphony.ui.components.BentoTextDialog
import io.github.zyrouge.symphony.ui.components.BentoToggleDialog
import io.github.zyrouge.symphony.ui.components.settings.GrooveFolderPickerDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.SettingsViewRoute
import kotlin.math.roundToInt
import io.github.zyrouge.symphony.utils.ImageQuality
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

@Serializable
data class GrooveSettingsViewRoute(
    val initialElement: String? = null,
)

private enum class GroovePopup {
    MEDIA_FOLDERS,
    MEDIA_FILTER,
    MIN_SONG_DURATION,
    BLACKLIST_FOLDERS,
    WHITELIST_FOLDERS,
    ARTIST_SEPARATORS,
    GENRE_SEPARATORS,
    ARTWORK_QUALITY,
    CASE_SENSITIVE,
    METAPHONY,
}

@Composable
fun GrooveSettingsView(context: ViewContext, route: GrooveSettingsViewRoute) {
    val colors = LocalBentoColors.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var popup by remember { mutableStateOf<GroovePopup?>(null) }

    val songsFilterPattern by context.symphony.settings.songsFilterPattern.flow.collectAsState()
    val minSongDuration by context.symphony.settings.minSongDuration.flow.collectAsState()
    val blacklistFolders by context.symphony.settings.blacklistFolders.flow.collectAsState()
    val whitelistFolders by context.symphony.settings.whitelistFolders.flow.collectAsState()
    val artistTagSeparators by context.symphony.settings.artistTagSeparators.flow.collectAsState()
    val genreTagSeparators by context.symphony.settings.genreTagSeparators.flow.collectAsState()
    val mediaFolders by context.symphony.settings.mediaFolders.flow.collectAsState()
    val artworkQuality by context.symphony.settings.artworkQuality.flow.collectAsState()
    val caseSensitiveSorting by context.symphony.settings.caseSensitiveSorting.flow.collectAsState()
    val useMetaphony by context.symphony.settings.useMetaphony.flow.collectAsState()

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
                    title = context.symphony.t.Groove,
                    windowClass = rememberBentoWindowClass(),
                    onBack = { context.navController.popBackStack() },
                )
                BentoSettingsColumns {
                    BentoSettingGroup {
                        BentoSettingRow(
                            label = context.symphony.t.MediaFolders,
                            value = context.symphony.t.XFolders(mediaFolders.size.toString()),
                            onClick = { popup = GroovePopup.MEDIA_FOLDERS },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.BlacklistFolders,
                            value = context.symphony.t.XFolders(blacklistFolders.size.toString()),
                            onClick = { popup = GroovePopup.BLACKLIST_FOLDERS },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.WhitelistFolders,
                            value = context.symphony.t.XFolders(whitelistFolders.size.toString()),
                            onClick = { popup = GroovePopup.WHITELIST_FOLDERS },
                        )
                    }
                    BentoSettingGroup {
                        BentoSettingRow(
                            label = context.symphony.t.SongsFilterPattern,
                            value = songsFilterPattern ?: ".*",
                            onClick = { popup = GroovePopup.MEDIA_FILTER },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.MinSongDurationFilter,
                            value = context.symphony.t.XSecs(minSongDuration.toString()),
                            onClick = { popup = GroovePopup.MIN_SONG_DURATION },
                        )
                    }
                    BentoSettingGroup {
                        BentoSettingRow(
                            label = context.symphony.t.ArtistTagValueSeparators,
                            value = artistTagSeparators.joinToString("  "),
                            onClick = { popup = GroovePopup.ARTIST_SEPARATORS },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.GenreTagValueSeparators,
                            value = genreTagSeparators.joinToString("  "),
                            onClick = { popup = GroovePopup.GENRE_SEPARATORS },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.ArtworkQuality,
                            value = artworkQuality.label(context),
                            onClick = { popup = GroovePopup.ARTWORK_QUALITY },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.CaseSensitiveSorting,
                            value = if (caseSensitiveSorting) "On" else "Off",
                            onClick = { popup = GroovePopup.CASE_SENSITIVE },
                        )
                        BentoSettingRow(
                            label = context.symphony.t.UseMetaphonyMetadataDecoder,
                            value = if (useMetaphony) "On" else "Off",
                            onClick = { popup = GroovePopup.METAPHONY },
                        )
                    }
                    BentoSettingGroup {
                        BentoSettingRow(
                            label = context.symphony.t.ClearSongCache,
                            value = null,
                            onClick = {
                                refreshMediaLibrary(context.symphony, true)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.symphony.t.SongCacheCleared,
                                        withDismissAction = true,
                                    )
                                }
                            },
                        )
                    }
                }
                Box(Modifier.padding(8.dp))
            }
            BentoSnackbarHost(snackbarHostState)
        }

    when (popup) {
        GroovePopup.MEDIA_FOLDERS -> BentoDialog(
            title = context.symphony.t.MediaFolders,
            onDismiss = { popup = null },
        ) {
            BentoSystemFolderPicker(
                context = context,
                values = mediaFolders,
                onChange = { values ->
                    context.symphony.settings.mediaFolders.setValue(values)
                    refreshMediaLibrary(context.symphony)
                },
            )
        }

        GroovePopup.MEDIA_FILTER -> BentoTextDialog(
            title = context.symphony.t.SongsFilterPattern,
            value = songsFilterPattern ?: ".*",
            placeholder = ".*",
            // An invalid pattern would make the next scan throw, so it is never saved.
            isValid = { runCatching { Regex(it) }.isSuccess },
            errorText = "Not a valid regular expression",
            onDone = {
                context.symphony.settings.songsFilterPattern.setValue(it)
                refreshMediaLibrary(context.symphony)
            },
            onDismiss = { popup = null },
        )

        GroovePopup.MIN_SONG_DURATION -> BentoSliderDialog(
            title = context.symphony.t.MinSongDurationFilter,
            value = minSongDuration.toFloat(),
            range = 0f..60f,
            onValue = { it.roundToInt().toFloat() },
            label = { context.symphony.t.XSecs(it.roundToInt().toString()) },
            onChange = { context.symphony.settings.minSongDuration.setValue(it.roundToInt()) },
            onDismiss = { popup = null },
        )

        GroovePopup.BLACKLIST_FOLDERS -> GrooveFolderListDialog(
            context = context,
            title = context.symphony.t.BlacklistFolders,
            values = blacklistFolders,
            onDone = { values ->
                context.symphony.settings.blacklistFolders.setValue(values)
                refreshMediaLibrary(context.symphony)
            },
            onDismiss = { popup = null },
        )

        GroovePopup.WHITELIST_FOLDERS -> GrooveFolderListDialog(
            context = context,
            title = context.symphony.t.WhitelistFolders,
            values = whitelistFolders,
            onDone = { values ->
                context.symphony.settings.whitelistFolders.setValue(values)
                refreshMediaLibrary(context.symphony)
            },
            onDismiss = { popup = null },
        )

        GroovePopup.ARTIST_SEPARATORS -> BentoListEditorDialog(
            context = context,
            title = context.symphony.t.ArtistTagValueSeparators,
            values = artistTagSeparators,
            // Edited as a list so any separator, the comma included, can be kept or added.
            onDone = { values ->
                context.symphony.settings.artistTagSeparators.setValue(values)
                refreshMediaLibrary(context.symphony)
            },
            onDismiss = { popup = null },
            addRow = { onAdd -> BentoListTextAddRow(placeholder = ";", onAdd = onAdd) },
        )

        GroovePopup.GENRE_SEPARATORS -> BentoListEditorDialog(
            context = context,
            title = context.symphony.t.GenreTagValueSeparators,
            values = genreTagSeparators,
            // Edited as a list so any separator, the comma included, can be kept or added.
            onDone = { values ->
                context.symphony.settings.genreTagSeparators.setValue(values)
                refreshMediaLibrary(context.symphony)
            },
            onDismiss = { popup = null },
            addRow = { onAdd -> BentoListTextAddRow(placeholder = ";", onAdd = onAdd) },
        )

        GroovePopup.ARTWORK_QUALITY -> BentoOptionDialog(
            title = context.symphony.t.ArtworkQuality,
            options = ImageQuality.entries.map { it.label(context) to (it == artworkQuality) },
            onSelect = { index ->
                context.symphony.settings.artworkQuality.setValue(ImageQuality.entries[index])
                popup = null
            },
            onDismiss = { popup = null },
        )

        GroovePopup.CASE_SENSITIVE -> BentoToggleDialog(
            title = context.symphony.t.CaseSensitiveSorting,
            checked = caseSensitiveSorting,
            onChange = { context.symphony.settings.caseSensitiveSorting.setValue(it) },
            onDismiss = { popup = null },
        )

        GroovePopup.METAPHONY -> BentoToggleDialog(
            title = context.symphony.t.UseMetaphonyMetadataDecoder,
            checked = useMetaphony,
            onChange = { context.symphony.settings.useMetaphony.setValue(it) },
            onDismiss = { popup = null },
        )

        null -> Unit
    }
}

fun ImageQuality.label(context: ViewContext) = when (this) {
    ImageQuality.Low -> context.symphony.t.Low
    ImageQuality.Medium -> context.symphony.t.Medium
    ImageQuality.High -> context.symphony.t.High
    ImageQuality.Loseless -> context.symphony.t.Loseless
}

/** Blacklist and whitelist folders: a removable list plus a picker for new folders. */
@Composable
private fun GrooveFolderListDialog(
    context: ViewContext,
    title: String,
    values: Set<String>,
    onDone: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    BentoListEditorDialog(
        context = context,
        title = title,
        values = values,
        onDone = onDone,
        onDismiss = onDismiss,
        addRow = { onAdd ->
            BentoListAddRow(label = context.symphony.t.AddFolder, onClick = { picking = true })
            if (picking) {
                GrooveFolderPickerDialog(
                    context = context,
                    explorer = context.symphony.groove.song.explorer,
                    onSelect = { path ->
                        path?.let { onAdd(it.pathString) }
                        picking = false
                    },
                )
            }
        },
    )
}

private var refreshMediaLibraryJob: kotlinx.coroutines.Job? = null

private fun refreshMediaLibrary(symphony: Symphony, clearCache: Boolean = false) {
    symphony.radio.stop()
    refreshMediaLibraryJob?.cancel()
    refreshMediaLibraryJob = symphony.groove.coroutineScope.launch {
        val options = Groove.FetchOptions(
            resetInMemoryCache = true,
            resetPersistentCache = clearCache,
        )
        symphony.groove.fetch(options)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpotlightTile(isInSpotlight: Boolean, content: @Composable (() -> Unit)) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val highlightAlphaAnimated = remember { Animatable(0f) }
    val highlightColor = LocalBentoColors.current.accent

    LaunchedEffect(isInSpotlight) {
        if (isInSpotlight) {
            bringIntoViewRequester.bringIntoView()
            delay(100)
            highlightAlphaAnimated.animateTo(
                targetValue = 0.3f,
                animationSpec = repeatable(
                    2,
                    TransitionDurations.Fast.asTween(easing = LinearEasing)
                ),
            )
            highlightAlphaAnimated.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .drawWithContent {
                drawContent()
                drawRect(color = highlightColor, alpha = highlightAlphaAnimated.value)
            }
    ) {
        content()
    }
}
