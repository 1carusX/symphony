package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoSongsPage
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongColumnLabels
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.model.UiSongSort
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtEnd
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtTop
import io.github.zyrouge.symphony.ui.view.SettingsViewRoute
import io.github.zyrouge.symphony.utils.DurationUtils

/**
 * The Songs page (DESIGN.md 5.2): Play all / Shuffle / Sort action tiles
 * above one surface tile holding every song. Title, Artist and Album sorts
 * group the list under sticky letter headers with an A-Z scrubber; the other
 * sorts show a plain list. The existing sort, reverse and shuffle behavior
 * (the persisted last-used sort settings and queue playback from the tapped
 * song) is unchanged.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsView(context: ViewContext) {
    val colors = LocalBentoColors.current
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val songIds by context.symphony.groove.song.all.collectAsState()
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val sortedSongIds by remember(songIds, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.song.sort(songIds, sortBy, sortReverse)
        }
    }
    val songs by remember(sortedSongIds) {
        derivedStateOf {
            sortedSongIds.mapNotNull { context.symphony.groove.song.get(it) }
        }
    }
    val playingSong = rememberBentoPlayingSong(context)
    val favorites by context.symphony.groove.playlist.favorites.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        when {
            songIds.isEmpty() && isUpdating -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            songIds.isEmpty() -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Filled.MusicNote,
                        null,
                        modifier = modifier,
                    )
                },
                content = {
                    Text(context.symphony.t.DamnThisIsSoEmpty)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        context.symphony.t.HintAddMediaFolders,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable {
                                context.navController.navigate(
                                    SettingsViewRoute(SettingsViewRoute.ELEMENT_MEDIA_FOLDERS),
                                )
                            }
                            .padding(2.dp),
                    )
                },
            )

            else -> BentoSongsPage(
                strings = context.symphony.t,
                sort = sortBy.toUiSongSort(),
                sortReverse = sortReverse,
                songCount = songs.size,
                totalDurationText = formatBentoDurationMinutes(
                    songs.sumOf { it.duration },
                    context.symphony.t,
                ),
                onSortChange = {
                    context.symphony.settings.lastUsedSongsSortBy.setValue(it.toSortBy())
                },
                onReverseChange = {
                    context.symphony.settings.lastUsedSongsSortReverse.setValue(it)
                },
                onPlayAll = {
                    context.symphony.radio.shorty.playQueue(
                        sortedSongIds,
                        source = context.symphony.t.AllSongs,
                    )
                },
                onShuffle = {
                    context.symphony.radio.shorty.playQueue(
                        sortedSongIds,
                        shuffle = true,
                        source = context.symphony.t.AllSongs,
                    )
                },
            ) { listModifier ->
                SongsListTile(
                    context = context,
                    songs = songs,
                    sortedSongIds = sortedSongIds,
                    sortBy = sortBy,
                    playingSongId = playingSong.songId,
                    isPlaying = playingSong.isPlaying,
                    lettered = isBentoLetteredSort(sortBy),
                    favorites = favorites,
                    modifier = listModifier,
                )
            }
        }
    }
}

/**
 * The song list tile (DESIGN.md 5.2): one surface tile holding every song,
 * with sticky letter headers (lettered sorts) and the A-Z scrubber on the
 * right when it applies.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsListTile(
    context: ViewContext,
    songs: List<Song>,
    sortedSongIds: List<String>,
    sortBy: SongRepository.SortBy,
    playingSongId: String?,
    isPlaying: Boolean,
    lettered: Boolean,
    favorites: List<String>,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val listState = rememberLazyListState()
    val contentAtEnd = LocalHomeContentAtEnd.current
    androidx.compose.runtime.LaunchedEffect(listState) {
        androidx.compose.runtime.snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last != null &&
                last.index >= info.totalItemsCount - 1 &&
                last.offset + last.size <= info.viewportEndOffset + 4
        }.collect { contentAtEnd.value = it }
    }
    val contentAtTop = LocalHomeContentAtTop.current
    androidx.compose.runtime.LaunchedEffect(listState) {
        androidx.compose.runtime.snapshotFlow {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 4
        }.collect { contentAtTop.value = it }
    }
    val songIndexById = remember(sortedSongIds) {
        sortedSongIds.withIndex().associate { (index, songId) -> songId to index }
    }
    val uiSongs = remember(songs) { songs.map { it.toUiSong() } }
    val songsById = remember(songs) { songs.associateBy { it.id } }
    // The tile runs under the floating capsule while rows continue below it;
    // the list and the scrubber stay clear of it. Once the last row scrolls
    // in, the tile's bottom edge follows it up, so at the end of the list the
    // edge sits above the capsule instead of staying hidden under it.
    val bottomInset = bentoBottomInset()
    val bottomInsetPx = with(LocalDensity.current) { bottomInset.toPx() }
    val tileTrim by remember(listState, bottomInsetPx) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            when {
                last == null || last.index < info.totalItemsCount - 1 -> 0f
                else -> (info.viewportSize.height - last.offset - last.size).toFloat()
                    .coerceIn(0f, bottomInsetPx)
            }
        }
    }
    val colorScheme = LocalBentoColorScheme.current
    val tileShape = BottomTrimmedShape(RoundedCornerShape(BentoRadii.TILE.dp), tileTrim)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(tileShape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, tileShape)
                    else -> Modifier
                },
            )
            .background(colors.surface),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // The list area is already inside the page gutter, so its width is
            // the inner width the column mode is based on.
            val columnMode = maxWidth >= 720.dp
            val showScrubber = lettered &&
                rememberBentoWindowClass() != BentoWindowClass.SHORT_HEIGHT
        SongList(
            songs = uiSongs,
            playingSongId = playingSongId,
            isPlaying = isPlaying,
            durationText = { DurationUtils.formatMs(it.durationMillis) },
            moreContentDescription = context.symphony.t.More,
            artwork = { song, artworkModifier ->
                songsById[song.id]?.let { original ->
                    AsyncImage(
                        original.createArtworkImageRequest(context.symphony).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = artworkModifier,
                    )
                } ?: Box(modifier = artworkModifier.background(colors.raised))
            },
            lettered = lettered,
            columnMode = columnMode,
            showScrubber = showScrubber,
            columnLabels = SongColumnLabels(
                title = context.symphony.t.Title,
                artist = context.symphony.t.Artist,
                album = context.symphony.t.Album,
                duration = context.symphony.t.Duration,
            ),
            contentPadding = PaddingValues(bottom = bottomInset),
            scrubberPadding = PaddingValues(end = 6.dp, bottom = bottomInset),
            listState = listState,
            sortKeyOf = { song ->
                when (sortBy) {
                    SongRepository.SortBy.ARTIST -> song.artists.joinToString()
                    SongRepository.SortBy.ALBUM -> song.album ?: ""
                    else -> song.title
                }
            },
            menu = { song, expanded, onDismissRequest ->
                songsById[song.id]?.let { original ->
                    SongDropdownMenu(
                        context = context,
                        song = original,
                        isFavorite = favorites.contains(song.id),
                        expanded = expanded,
                        onDismissRequest = onDismissRequest,
                    )
                }
            },
            onSongClick = { song ->
                context.symphony.radio.shorty.playQueue(
                    sortedSongIds,
                    Radio.PlayOptions(
                        index = songIndexById[song.id] ?: 0,
                    ),
                    source = context.symphony.t.AllSongs,
                )
            },
        )
        }
    }
}

/** [shape] with its bottom [trim] pixels cut off, so a tile's edge can rise with its content. */
private data class BottomTrimmedShape(val shape: Shape, val trim: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        shape.createOutline(
            size.copy(height = (size.height - trim).coerceAtLeast(0f)),
            layoutDirection,
            density,
        )
}

/** Whether a sort shows letter headers and the scrubber (DESIGN.md 5.2). */
fun isBentoLetteredSort(sortBy: SongRepository.SortBy) = when (sortBy) {
    SongRepository.SortBy.TITLE,
    SongRepository.SortBy.ARTIST,
    SongRepository.SortBy.ALBUM,
    -> true

    else -> false
}

/** The shared UI sort that matches this repository sort. */
fun SongRepository.SortBy.toUiSongSort(): UiSongSort = UiSongSort.valueOf(name)

/** The repository sort that matches this shared UI sort. */
fun UiSongSort.toSortBy(): SongRepository.SortBy = SongRepository.SortBy.valueOf(name)
