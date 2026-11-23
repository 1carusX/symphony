package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import io.github.zyrouge.symphony.Symphony
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample

/**
 * Changes whenever songs or playlists change. Groups (albums, artists, genres,
 * playlists, folders) are mutated in place by the repositories, so screens add
 * this to their remember keys to pick up new tracks, renames and edits.
 */
val LocalGrooveVersion = compositionLocalOf { 0L }

/**
 * A version number that follows the song and playlist repositories, at most
 * twice a second: a scan updates the songs once per file, and recomputing every
 * group on each of those would stall the UI.
 */
@OptIn(FlowPreview::class)
@Composable
fun rememberGrooveVersion(symphony: Symphony): State<Long> = produceState(0L, symphony) {
    combine(symphony.groove.song.id, symphony.groove.playlist.updateId) { songs, playlists ->
        songs * 31 + playlists
    }
        .sample(500)
        .collect { value = it }
}
