package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.Box
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
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.components.BentoSnackbarHost
import io.github.zyrouge.symphony.ui.components.settings.BentoListAddRow
import io.github.zyrouge.symphony.ui.components.settings.BentoListTextAddRow
import io.github.zyrouge.symphony.ui.components.settings.BentoSystemFolderPicker
import io.github.zyrouge.symphony.ui.components.settings.GrooveFolderPickerDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt
import io.github.zyrouge.symphony.utils.ImageQuality
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoSheetChoiceRow
import io.github.zyrouge.symphony.ui.components.BentoSheetPages
import io.github.zyrouge.symphony.ui.components.BentoSheetSliderRow
import io.github.zyrouge.symphony.ui.components.BentoSheetTextField
import io.github.zyrouge.symphony.ui.components.BentoSheetValueRow
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import io.github.zyrouge.symphony.ui.components.BentoSheetChip
import io.github.zyrouge.symphony.ui.components.settings.bentoReadableFolderPath

private enum class GroovePage {
    MEDIA_FOLDERS,
    BLACKLIST_FOLDERS,
    WHITELIST_FOLDERS,
}

/**
 * The Library settings in a sheet over the settings page. Filters, tags and
 * switches are set in place; the three folder lists open as pages of the same
 * sheet rather than dialogs on top. Lists and the filter are saved when they
 * leave the screen, so the rescan they need runs once, not on every edit.
 */
@Composable
fun GrooveSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val settings = context.symphony.settings
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var page by remember { mutableStateOf<GroovePage?>(null) }

    val songsFilterPattern by settings.songsFilterPattern.flow.collectAsState()
    val minSongDuration by settings.minSongDuration.flow.collectAsState()
    val blacklistFolders by settings.blacklistFolders.flow.collectAsState()
    val whitelistFolders by settings.whitelistFolders.flow.collectAsState()
    val artistTagSeparators by settings.artistTagSeparators.flow.collectAsState()
    val genreTagSeparators by settings.genreTagSeparators.flow.collectAsState()
    val mediaFolders by settings.mediaFolders.flow.collectAsState()
    val artworkQuality by settings.artworkQuality.flow.collectAsState()
    val caseSensitiveSorting by settings.caseSensitiveSorting.flow.collectAsState()
    val useMetaphony by settings.useMetaphony.flow.collectAsState()
    val songIds by context.symphony.groove.song.all.collectAsState()
    val songPaths = remember(songIds) { songIds.mapNotNull { context.symphony.groove.song.get(it)?.path } }

    BentoSheetDialog(
        title = when (page) {
            GroovePage.MEDIA_FOLDERS -> context.symphony.t.MediaFolders
            GroovePage.BLACKLIST_FOLDERS -> context.symphony.t.BlacklistFolders
            GroovePage.WHITELIST_FOLDERS -> context.symphony.t.WhitelistFolders
            null -> context.symphony.t.LibraryFolders
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
        overlay = { BentoSnackbarHost(snackbarHostState) },
    ) {
        BentoSheetPages(page) { current ->
            when (current) {
                null -> {
                    SettingsSheetSectionLabel("Folders")
                    BentoSheetValueRow(
                        title = context.symphony.t.MediaFolders,
                        value = context.symphony.t.XFolders(mediaFolders.size.toString()),
                        onClick = { page = GroovePage.MEDIA_FOLDERS },
                    )
                    BentoSheetValueRow(
                        title = context.symphony.t.BlacklistFolders,
                        subtitle = EXCLUDED_FOLDERS_NOTE,
                        value = context.symphony.t.XFolders(blacklistFolders.size.toString()),
                        onClick = { page = GroovePage.BLACKLIST_FOLDERS },
                    )
                    BentoSheetValueRow(
                        title = context.symphony.t.WhitelistFolders,
                        subtitle = EXCEPTION_FOLDERS_NOTE,
                        value = context.symphony.t.XFolders(whitelistFolders.size.toString()),
                        onClick = { page = GroovePage.WHITELIST_FOLDERS },
                    )
                    SettingsSheetSectionLabel("Filters")
                    GrooveFilterPatternField(
                        context = context,
                        songPaths = songPaths,
                        saved = songsFilterPattern ?: ".*",
                        onSave = {
                            settings.songsFilterPattern.setValue(it)
                            refreshMediaLibrary(context.symphony)
                        },
                    )
                    BentoSheetSliderRow(
                        title = context.symphony.t.MinSongDurationFilter,
                        value = minSongDuration.toFloat(),
                        range = 0f..60f,
                        step = 1f,
                        stops = listOf(0f, 10f, 30f, 60f),
                        label = { context.symphony.t.XSecs(it.roundToInt().toString()) },
                        onChange = { settings.minSongDuration.setValue(it.roundToInt()) },
                    )
                    SettingsSheetSectionLabel("Tags and sorting")
                    // Edited as a list so any separator, the comma included, can be kept or added.
                    GrooveSeparatorEditor(
                        context = context,
                        title = context.symphony.t.ArtistTagValueSeparators,
                        values = artistTagSeparators,
                        onSave = {
                            settings.artistTagSeparators.setValue(it)
                            refreshMediaLibrary(context.symphony)
                        },
                    )
                    GrooveSeparatorEditor(
                        context = context,
                        title = context.symphony.t.GenreTagValueSeparators,
                        values = genreTagSeparators,
                        onSave = {
                            settings.genreTagSeparators.setValue(it)
                            refreshMediaLibrary(context.symphony)
                        },
                    )
                    BentoSheetChoiceRow(
                        title = context.symphony.t.ArtworkQuality,
                        options = ImageQuality.entries.map { it.label(context) },
                        selectedIndex = ImageQuality.entries.indexOf(artworkQuality),
                        onSelect = { settings.artworkQuality.setValue(ImageQuality.entries[it]) },
                    )
                    BentoSheetSwitchRow(
                        title = context.symphony.t.CaseSensitiveSorting,
                        checked = caseSensitiveSorting,
                        onCheckedChange = { settings.caseSensitiveSorting.setValue(it) },
                    )
                    BentoSheetSwitchRow(
                        title = context.symphony.t.UseMetaphonyMetadataDecoder,
                        checked = useMetaphony,
                        onCheckedChange = { settings.useMetaphony.setValue(it) },
                    )
                    SettingsSheetSectionLabel("Cache")
                    GrooveClearCacheRow(
                        context = context,
                        onConfirm = {
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

                GroovePage.MEDIA_FOLDERS -> {
                    GroovePageNote(
                        "The folders that hold your music. Every song inside them, " +
                            "subfolders included, is added to the library.",
                    )
                    BentoSystemFolderPicker(
                        context = context,
                        values = mediaFolders,
                        onChange = { values ->
                            settings.mediaFolders.setValue(values)
                            refreshMediaLibrary(context.symphony)
                        },
                    )
                }

                GroovePage.BLACKLIST_FOLDERS -> GrooveFolderListPage(
                    context = context,
                    note = "$EXCLUDED_FOLDERS_NOTE, subfolders included.",
                    values = blacklistFolders,
                    onSave = { values ->
                        settings.blacklistFolders.setValue(values)
                        refreshMediaLibrary(context.symphony)
                    },
                )

                GroovePage.WHITELIST_FOLDERS -> GrooveFolderListPage(
                    context = context,
                    note = "Pick folders inside an excluded folder that should still be scanned. " +
                        "They do nothing outside excluded folders.",
                    values = whitelistFolders,
                    onSave = { values ->
                        settings.whitelistFolders.setValue(values)
                        refreshMediaLibrary(context.symphony)
                    },
                )
            }
        }
    }
}

fun ImageQuality.label(context: ViewContext) = when (this) {
    ImageQuality.Low -> context.symphony.t.Low
    ImageQuality.Medium -> context.symphony.t.Medium
    ImageQuality.High -> context.symphony.t.High
    ImageQuality.Loseless -> context.symphony.t.Loseless
}

private const val EXCLUDED_FOLDERS_NOTE = "Not scanned"
private const val EXCEPTION_FOLDERS_NOTE = "Scanned even inside an excluded folder"

/** Ready-made patterns; each keeps every folder that passes, so none can empty the library. */
private val FILTER_EXAMPLES = listOf(
    "Everything" to ".*",
    "Skip Recordings" to "^(?!.*Recordings)",
    "Skip chat apps" to "^(?!.*(WhatsApp|Telegram))",
)

/**
 * Whether a scan keeps the song at [path] under [regex]: the scan checks the
 * pattern against every folder on the way down (not the file), case
 * insensitively, and skips a folder that does not match with all inside it.
 */
internal fun bentoFilterKeeps(path: String, regex: Regex): Boolean {
    val parts = path.split('/')
    return (1 until parts.size).all { depth ->
        regex.containsMatchIn(parts.subList(0, depth).joinToString("/"))
    }
}

/**
 * The songs filter pattern, edited in place in a monospace field, with a
 * few ready-made patterns and how many library songs the typed one keeps.
 * An invalid pattern would make the next scan throw, so it is never saved; a
 * valid change is saved on Done or when the field leaves the screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrooveFilterPatternField(
    context: ViewContext,
    songPaths: List<String>,
    saved: String,
    onSave: (String) -> Unit,
) {
    val colors = LocalBentoColors.current
    val focusManager = LocalFocusManager.current
    var text by remember(saved) { mutableStateOf(TextFieldValue(saved)) }
    val regex = remember(text.text) {
        runCatching { Regex(text.text, RegexOption.IGNORE_CASE) }.getOrNull()
    }
    val kept = remember(regex, songPaths) {
        regex?.let { r -> songPaths.count { bentoFilterKeeps(it, r) } }
    }
    val latestText by rememberUpdatedState(text.text)
    val latestSaved by rememberUpdatedState(saved)
    val latestOnSave by rememberUpdatedState(onSave)
    val save = {
        val value = latestText
        if (value != latestSaved && runCatching { Regex(value) }.isSuccess) latestOnSave(value)
    }
    DisposableEffect(Unit) { onDispose { save() } }

    // 8 dp plus the field label's own 4 dp lines the label up with the row titles.
    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BentoSheetTextField(
            value = text,
            onValueChange = { text = it },
            label = context.symphony.t.SongsFilterPattern,
            placeholder = ".*",
            fontFamily = FontFamily.Monospace,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                save()
                focusManager.clearFocus()
            }),
        )
        Text(
            when (kept) {
                null -> "Not a valid regular expression"
                else -> "Keeps $kept of the ${songPaths.size} songs now in the library. " +
                    "Checked against each folder's path, not file names."
            },
            style = BentoTypography.secondary,
            color = if (kept == null) ThemeColors.Red else colors.sub,
            modifier = Modifier.padding(start = 4.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FILTER_EXAMPLES.forEach { (label, pattern) ->
                BentoSheetChip(
                    label = label,
                    selected = text.text == pattern,
                    onClick = { text = TextFieldValue(pattern, TextRange(pattern.length)) },
                )
            }
        }
    }
}

/** A short explanation at the top of a sheet page. */
@Composable
private fun GroovePageNote(text: String) {
    Text(
        text,
        style = BentoTypography.secondary,
        color = LocalBentoColors.current.sub,
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
    )
}

/**
 * Clear song cache, in red. The first tap asks in place, under the row, so
 * the rescan it starts is never a slip of the finger.
 */
@Composable
private fun GrooveClearCacheRow(context: ViewContext, onConfirm: () -> Unit) {
    val colors = LocalBentoColors.current
    var asking by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .clickable(role = Role.Button) { asking = !asking }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(Icons.Outlined.Delete, null, tint = ThemeColors.Red)
        Text(
            context.symphony.t.ClearSongCache,
            style = BentoTypography.rowTitle,
            color = ThemeColors.Red,
        )
    }
    AnimatedVisibility(visible = asking) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BentoRadii.ROW.dp))
                .background(colors.raised)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Forgets the saved song details and scans every folder again. " +
                    "Playback stops, and a large library can take a few minutes.",
                style = BentoTypography.secondary,
                color = colors.sub,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                BentoSheetButton(label = context.symphony.t.Cancel, onClick = { asking = false })
                BentoSheetButton(
                    label = "Clear and rescan",
                    primary = true,
                    onClick = {
                        asking = false
                        onConfirm()
                    },
                )
            }
        }
    }
}

/**
 * Tag separators as chips with a remove button, plus a field to add one.
 * The set is saved once, when the editor leaves the screen, and only if it
 * changed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrooveSeparatorEditor(
    context: ViewContext,
    title: String,
    values: Set<String>,
    onSave: (Set<String>) -> Unit,
) {
    val colors = LocalBentoColors.current
    val entries = remember(values) { mutableStateListOf<String>().apply { addAll(values) } }
    val latestEntries by rememberUpdatedState(entries)
    val latestValues by rememberUpdatedState(values)
    val latestOnSave by rememberUpdatedState(onSave)
    DisposableEffect(Unit) {
        onDispose {
            val edited = latestEntries.toSet()
            if (edited != latestValues) latestOnSave(edited)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = BentoTypography.rowTitle, color = colors.fg)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            entries.toList().forEach { entry ->
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.raised)
                        .padding(start = 14.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        entry,
                        style = BentoTypography.body.copy(fontFamily = FontFamily.Monospace),
                        color = colors.fg,
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { entries.remove(entry) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            "${context.symphony.t.Delete} $entry",
                            tint = colors.sub,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
        BentoListTextAddRow(placeholder = ";") { value ->
            if (value.isNotEmpty() && value !in entries) entries.add(value)
        }
    }
}

/**
 * A blacklist or whitelist page: each folder with a remove button, then a
 * picker for new ones. The set is saved once, when the page closes.
 */
@Composable
private fun GrooveFolderListPage(
    context: ViewContext,
    note: String,
    values: Set<String>,
    onSave: (Set<String>) -> Unit,
) {
    val colors = LocalBentoColors.current
    val entries = remember { mutableStateListOf<String>().apply { addAll(values) } }
    val latestValues by rememberUpdatedState(values)
    val latestOnSave by rememberUpdatedState(onSave)
    var picking by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            val edited = entries.toSet()
            if (edited != latestValues) latestOnSave(edited)
        }
    }

    GroovePageNote(note)
    entries.toList().forEach { entry ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BentoRadii.ROW.dp))
                .background(colors.raised)
                .padding(start = 14.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                bentoReadableFolderPath(entry),
                style = BentoTypography.secondary,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(BentoSizes.ICON_BUTTON.dp)
                    .clip(CircleShape)
                    .clickable { entries.remove(entry) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Close,
                    "${context.symphony.t.Delete} $entry",
                    tint = colors.sub,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
    BentoListAddRow(label = context.symphony.t.AddFolder, onClick = { picking = true })
    if (picking) {
        GrooveFolderPickerDialog(
            context = context,
            explorer = context.symphony.groove.song.explorer,
            onSelect = { path ->
                path?.let { if (it.pathString !in entries) entries.add(it.pathString) }
                picking = false
            },
        )
    }
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
