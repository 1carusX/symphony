package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.UiSongSort

/** The name of a song sort in the app language. */
fun UiSongSort.label(t: Strings) = when (this) {
    UiSongSort.CUSTOM -> t.Custom
    UiSongSort.TITLE -> t.Title
    UiSongSort.ARTIST -> t.Artist
    UiSongSort.ALBUM -> t.Album
    UiSongSort.DURATION -> t.Duration
    UiSongSort.DATE_MODIFIED -> t.LastModified
    UiSongSort.COMPOSER -> t.Composer
    UiSongSort.ALBUM_ARTIST -> t.AlbumArtist
    UiSongSort.YEAR -> t.Year
    UiSongSort.FILENAME -> t.Filename
    UiSongSort.TRACK_NUMBER -> t.TrackNumber
}

/**
 * The Songs page action tiles: Play all, Shuffle and the current sort, with
 * the sort menu. Below 520 dp the Play all tile gets a row of its own.
 */
@Composable
fun BentoSongsActions(
    strings: Strings,
    sort: UiSongSort,
    sortReverse: Boolean,
    songCount: Int,
    onSortChange: (UiSongSort) -> Unit,
    onReverseChange: (Boolean) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    totalDurationText: String? = null,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val narrow = maxWidth < 520.dp
        val playAll: @Composable () -> Unit = {
            BentoActionTile(
                icon = Icons.Filled.PlayArrow,
                label = strings.PlayAll,
                sub = strings.XSongs(songCount.toString()) + when {
                    narrow || totalDurationText == null -> ""
                    else -> " · $totalDurationText"
                },
                tone = BentoTileTone.ACCENT,
                main = true,
                narrow = narrow,
                onClick = onPlayAll,
            )
        }
        val shuffle: @Composable () -> Unit = {
            BentoActionTile(
                icon = Icons.Filled.Shuffle,
                label = strings.ShufflePlay,
                sub = strings.RandomOrder,
                tone = BentoTileTone.TONAL,
                narrow = narrow,
                onClick = onShuffle,
            )
        }
        val sortTile: @Composable () -> Unit = {
            BentoActionTile(
                icon = Icons.AutoMirrored.Rounded.Sort,
                label = sort.label(strings),
                sub = when {
                    sortReverse -> strings.Descending
                    else -> strings.Ascending
                },
                narrow = narrow,
                onClick = { showSortMenu = true },
            )
            BentoSortMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                title = strings.SortSongsBy,
                options = UiSongSort.entries.map { BentoSortOption(it, it.label(strings)) },
                selected = sort,
                onSelect = onSortChange,
                reverse = sortReverse,
                reverseLabel = strings.ReverseOrder,
                onReverseChange = onReverseChange,
                onValue = strings.On,
                offValue = strings.Off,
            )
        }
        when {
            narrow -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                playAll()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) { shuffle() }
                    Box(modifier = Modifier.weight(1f)) { sortTile() }
                }
            }

            else -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.weight(2f)) { playAll() }
                Box(modifier = Modifier.weight(1f)) { shuffle() }
                Box(modifier = Modifier.weight(1f)) { sortTile() }
            }
        }
    }
}
