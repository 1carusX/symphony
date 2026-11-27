package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoPlayerQueue as SharedBentoPlayerQueue
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.QueueEntry
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.DurationUtils

/** Queue stage: tap a row to play it, remove rows one by one, save or clear what is up next. */
@Composable
internal fun BentoPlayerQueue(context: ViewContext, modifier: Modifier = Modifier) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    val t = context.symphony.t
    val entries = queue.mapIndexedNotNull { index, songId ->
        if (index > queueIndex) {
            context.symphony.groove.song.get(songId)?.let { song ->
                QueueEntry(position = index, song = song.toUiSong())
            }
        } else {
            null
        }
    }

    SharedBentoPlayerQueue(
        entries = entries,
        upNextLabel = t.UpNext,
        countLabel = if (entries.isEmpty()) t.EndOfQueue else t.XSongs(entries.size.toString()),
        endOfQueueLabel = t.EndOfQueue,
        saveQueueLabel = t.SaveQueue,
        clearUpNextLabel = t.ClearUpNext,
        durationText = { DurationUtils.formatMs(it.durationMillis) },
        removeContentDescription = { t.RemoveX(it.title) },
        artwork = { song, artworkModifier ->
            context.symphony.groove.song.get(song.id)?.let { original ->
                AsyncImage(
                    original.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = artworkModifier,
                )
            }
        },
        onEntryClick = { entry -> context.symphony.radio.jumpTo(entry.position) },
        onEntryRemove = { entry -> context.symphony.radio.queue.remove(entry.position) },
        onClearUpNext = {
            // Only the songs after the current one: radio.stop() would end playback too.
            context.symphony.radio.queue.remove(entries.map { it.position })
        },
        onSaveQueue = { showSaveDialog = true },
        modifier = modifier,
    )

    if (showSaveDialog) {
        NewPlaylistDialog(
            context = context,
            initialSongIds = queue.toList(),
            onDone = { playlist ->
                showSaveDialog = false
                context.symphony.groove.playlist.add(playlist)
            },
            onDismissRequest = { showSaveDialog = false },
        )
    }
}
