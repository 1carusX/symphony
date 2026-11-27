package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import kotlinx.coroutines.launch

@Immutable
data class SongColumnLabels(
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
)

/**
 * A cross-platform song list: letter grouping, sticky headers, an A-Z scrubber
 * and the shared [SongRow]. Artwork and menus stay platform-specific slots.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongList(
    songs: List<UiSong>,
    playingSongId: String?,
    isPlaying: Boolean,
    durationText: (UiSong) -> String,
    moreContentDescription: String,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    modifier: Modifier = Modifier,
    lettered: Boolean = true,
    columnMode: Boolean = false,
    showScrubber: Boolean = true,
    columnLabels: SongColumnLabels? = null,
    contentPadding: PaddingValues = PaddingValues(),
    scrubberPadding: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    sortKeyOf: (UiSong) -> String = { it.title },
    trailing: (@Composable RowScope.(UiSong) -> Unit)? = null,
    menu: (@Composable (UiSong, Boolean, () -> Unit) -> Unit)? = null,
    onSongClick: (UiSong) -> Unit,
) {
    val groups = remember(songs, lettered, sortKeyOf) {
        groupBentoEntriesByLetter(songs, lettered, sortKeyOf)
    }
    val presentLetters = remember(groups) {
        groups.mapNotNull { it.letter }.toSet()
    }
    val groupItemIndices = remember(groups) {
        var offset = 0
        groups.map { group ->
            offset.also { offset += 1 + group.entries.size }
        }
    }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = if (showScrubber && lettered) 30.dp else 0.dp),
        ) {
            if (columnMode && columnLabels != null) {
                BentoSongColumnHeader(
                    title = columnLabels.title,
                    artist = columnLabels.artist,
                    album = columnLabels.album,
                    duration = columnLabels.duration,
                )
            }
            LazyColumn(
                state = listState,
                contentPadding = contentPadding,
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
                    ) { _, song ->
                        SongRow(
                            song = song,
                            playing = song.id == playingSongId,
                            isPlaying = isPlaying,
                            durationText = durationText(song),
                            moreContentDescription = moreContentDescription,
                            artwork = { contentModifier -> artwork(song, contentModifier) },
                            columns = columnMode,
                            trailing = trailing?.let { slot -> { slot(song) } },
                            menu = menu?.let { slot ->
                                { expanded, dismiss -> slot(song, expanded, dismiss) }
                            },
                            onClick = { onSongClick(song) },
                        )
                    }
                }
            }
        }
        if (showScrubber && lettered) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(scrubberPadding),
            ) {
                BentoAzScrubber(
                    presentLetters = presentLetters,
                    onLetter = { letter ->
                        val groupIndex = groups.indexOfFirst { it.letter == letter }
                        if (groupIndex >= 0) {
                            scope.launch {
                                listState.scrollToItem(groupItemIndices[groupIndex])
                            }
                        }
                    },
                )
            }
        }
    }
}
