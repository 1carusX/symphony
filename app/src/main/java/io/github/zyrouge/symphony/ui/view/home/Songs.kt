package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoActionTile
import io.github.zyrouge.symphony.ui.components.BentoAzScrubber
import io.github.zyrouge.symphony.ui.components.BentoLetterHeader
import io.github.zyrouge.symphony.ui.components.BentoSongColumnHeader
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.BentoSortMenu
import io.github.zyrouge.symphony.ui.components.BentoSortOption
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.groupBentoEntriesByLetter
import io.github.zyrouge.symphony.ui.components.label
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtEnd
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtTop
import io.github.zyrouge.symphony.ui.view.SettingsViewRoute
import kotlinx.coroutines.launch

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
    var showSortMenu by remember { mutableStateOf(false) }

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

            else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val windowInfo = LocalWindowInfo.current
                val compactWindow = windowInfo.containerDpSize.width < 600.dp
                val gutter = when {
                    compactWindow -> BentoSpacing.GUTTER_COMPACT.dp
                    else -> BentoSpacing.GUTTER_OTHER.dp
                }
                val tileGap = when {
                    compactWindow -> BentoSpacing.GAP_COMPACT.dp
                    else -> BentoSpacing.GAP_OTHER.dp
                }
                val innerWidth = maxWidth - gutter * 2
                val lettered = isBentoLetteredSort(sortBy)
                val columnMode = innerWidth >= 720.dp
                val narrowActions = innerWidth < 520.dp
                val showScrubber = lettered &&
                    rememberBentoWindowClass() != BentoWindowClass.SHORT_HEIGHT

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = gutter),
                    verticalArrangement = Arrangement.spacedBy(tileGap),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tileGap),
                    ) {
                        Box(modifier = Modifier.weight(2f)) {
                            BentoActionTile(
                                icon = Icons.Filled.PlayArrow,
                                label = context.symphony.t.PlayAll,
                                sub = context.symphony.t.XSongs(songs.size.toString()) +
                                    when {
                                        narrowActions -> ""
                                        else -> " · " +
                                            formatBentoDurationMinutes(
                                                songs.sumOf { it.duration }, context.symphony.t)
                                    },
                                tone = BentoTileTone.ACCENT,
                                main = true,
                                narrow = narrowActions,
                                onClick = {
                                    context.symphony.radio.shorty.playQueue(sortedSongIds, source = context.symphony.t.AllSongs)
                                },
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BentoActionTile(
                                icon = Icons.Filled.Shuffle,
                                label = context.symphony.t.ShufflePlay,
                                sub = context.symphony.t.RandomOrder,
                                tone = BentoTileTone.TONAL,
                                narrow = narrowActions,
                                onClick = {
                                    context.symphony.radio.shorty.playQueue(
                                        sortedSongIds,
                                        shuffle = true,
                                        source = context.symphony.t.AllSongs,
                                    )
                                },
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BentoActionTile(
                                icon = Icons.AutoMirrored.Rounded.Sort,
                                label = sortBy.label(context),
                                sub = when {
                                    sortReverse -> context.symphony.t.Descending
                                    else -> context.symphony.t.Ascending
                                },
                                narrow = narrowActions,
                                onClick = { showSortMenu = true },
                            )
                            BentoSortMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                title = context.symphony.t.SortSongsBy,
                                options = SongRepository.SortBy.entries.map {
                                    BentoSortOption(it, it.label(context))
                                },
                                selected = sortBy,
                                onSelect = {
                                    context.symphony.settings.lastUsedSongsSortBy.setValue(it)
                                },
                                reverse = sortReverse,
                                reverseLabel = context.symphony.t.ReverseOrder,
                                onReverseChange = {
                                    context.symphony.settings.lastUsedSongsSortReverse.setValue(it)
                                },
                            )
                        }
                    }

                    SongsListTile(
                        context = context,
                        songs = songs,
                        sortedSongIds = sortedSongIds,
                        sortBy = sortBy,
                        playingSongId = playingSong.songId,
                        isPlaying = playingSong.isPlaying,
                        lettered = lettered,
                        columnMode = columnMode,
                        showScrubber = showScrubber,
                        favorites = favorites,
                        modifier = Modifier.weight(1f),
                    )
                }
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
    columnMode: Boolean,
    showScrubber: Boolean,
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
    val coroutineScope = rememberCoroutineScope()
    val groups by remember(songs, lettered, sortBy) {
        derivedStateOf {
            groupBentoEntriesByLetter(
                entries = songs,
                lettered = lettered,
                sortKeyOf = { bentoSongLetterKey(it, sortBy) },
            )
        }
    }
    val presentLetters = remember(groups) {
        groups.mapNotNull { it.letter }.toSet()
    }
    // The LazyColumn item index of each group's letter header: one header
    // item plus one item per song before it.
    val groupItemIndices = remember(groups) {
        var offset = 0
        groups.map { group ->
            offset.also { offset += 1 + group.entries.size }
        }
    }
    val songIndexById = remember(sortedSongIds) {
        sortedSongIds.withIndex().associate { (index, songId) -> songId to index }
    }
    // The tile runs under the floating capsule; the list and the scrubber stay clear of it.
    val bottomInset = bentoBottomInset()
    val colorScheme = LocalBentoColorScheme.current
    val tileShape = RoundedCornerShape(BentoRadii.TILE.dp)

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
        // The scrubber's room is kept beside the column header too, so the
        // header labels stay over their cells.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    when {
                        showScrubber -> Modifier.padding(end = 30.dp)
                        else -> Modifier
                    },
                ),
        ) {
            if (columnMode) {
                BentoSongColumnHeader(
                    title = context.symphony.t.Title,
                    artist = context.symphony.t.Artist,
                    album = context.symphony.t.Album,
                    duration = context.symphony.t.Duration,
                )
            }
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = bottomInset),
                modifier = Modifier.fillMaxSize(),
            ) {
                groups.forEachIndexed { groupIndex, group ->
                    if (lettered) {
                        stickyHeader(key = "group-$groupIndex-${group.letter}-header") {
                            BentoLetterHeader(group.letter!!)
                        }
                    }
                    itemsIndexed(
                        group.entries,
                        key = { _, song -> song.id },
                        contentType = { _, _ -> Groove.Kind.SONG },
                    ) { _, song ->
                        val playing = song.id == playingSongId
                        BentoSongRow(
                            context = context,
                            song = song,
                            playing = playing,
                            isPlaying = isPlaying,
                            columns = columnMode,
                            menu = { expanded, onDismissRequest ->
                                SongDropdownMenu(
                                    context = context,
                                    song = song,
                                    isFavorite = favorites.contains(song.id),
                                    expanded = expanded,
                                    onDismissRequest = onDismissRequest,
                                )
                            },
                            onClick = {
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
        }
        if (showScrubber) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, bottom = bottomInset),
            ) {
                BentoAzScrubber(
                    presentLetters = presentLetters,
                    onLetter = { letter ->
                        val groupIndex = groups.indexOfFirst { it.letter == letter }
                        if (groupIndex >= 0) {
                            coroutineScope.launch {
                                listState.scrollToItem(groupItemIndices[groupIndex])
                            }
                        }
                    },
                )
            }
        }
    }
}

/** Whether a sort shows letter headers and the scrubber (DESIGN.md 5.2). */
fun isBentoLetteredSort(sortBy: SongRepository.SortBy) = when (sortBy) {
    SongRepository.SortBy.TITLE,
    SongRepository.SortBy.ARTIST,
    SongRepository.SortBy.ALBUM,
    -> true

    else -> false
}

/**
 * The sort key a letter header derives from (DESIGN.md 5.2): the sorted
 * field's first character.
 */
fun bentoSongLetterKey(song: Song, sortBy: SongRepository.SortBy) = when (sortBy) {
    SongRepository.SortBy.ARTIST -> song.artists.joinToString()
    SongRepository.SortBy.ALBUM -> song.album ?: ""
    else -> song.title
}
