package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils

/** Queue stage: tap a row to play it, remove rows one by one, save or clear what is up next. */
@Composable
internal fun BentoPlayerQueue(context: ViewContext, modifier: Modifier = Modifier) {
    val colors = LocalBentoColors.current
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    val upcoming = queue.mapIndexedNotNull { index, songId ->
        if (index > queueIndex) index to songId else null
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(BentoRadii.PLAYER_COVER.dp))
            .background(colors.raised),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Up next",
                    style = BentoTypography.rowTitle,
                    color = colors.fg,
                )
                Text(
                    if (upcoming.isEmpty()) "End of queue"
                    else context.symphony.t.XSongs(upcoming.size.toString()),
                    style = BentoTypography.secondary,
                    color = colors.muted,
                )
            }
            QueueIconButton(
                icon = { Icon(Icons.Filled.Save, "Save queue") },
                onClick = { showSaveDialog = true },
            )
            if (upcoming.isNotEmpty()) {
                QueueIconButton(
                    icon = { Icon(Icons.Filled.ClearAll, "Clear up next") },
                    onClick = {
                        // Only the songs after the current one: radio.stop() would end playback too.
                        context.symphony.radio.queue.remove(upcoming.map { it.first })
                    },
                )
            }
        }

        if (upcoming.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "End of queue",
                    style = BentoTypography.body,
                    color = colors.muted,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                itemsIndexed(
                    items = upcoming,
                    key = { _, entry -> "${entry.first}-${entry.second}" },
                ) { _, entry ->
                    val (queuePosition, songId) = entry
                    context.symphony.groove.song.get(songId)?.let { song ->
                        BentoPlayerQueueRow(
                            context = context,
                            song = song,
                            onClick = { context.symphony.radio.jumpTo(queuePosition) },
                            onRemove = { context.symphony.radio.queue.remove(queuePosition) },
                        )
                    }
                }
            }
        }
    }

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

@Composable
private fun BentoPlayerQueueRow(
    context: ViewContext,
    song: Song,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.QUEUE_ROW.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .clickable(onClick = onClick)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            song.createArtworkImageRequest(context.symphony).build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(BentoRadii.QUEUE_ART_40.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = BentoTypography.rowTitle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artists.joinToString(),
                style = BentoTypography.secondary,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            DurationUtils.formatMs(song.duration),
            style = bentoTabularStyle(BentoTypography.secondary),
            color = colors.muted,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Close,
                "Remove ${song.title}",
                tint = colors.muted,
                modifier = Modifier.size(BentoSizes.ICON.dp),
            )
        }
    }
}

@Composable
private fun QueueIconButton(icon: @Composable () -> Unit, onClick: () -> Unit) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(BentoSizes.TOUCH_TARGET_MIN.dp)
            .clip(CircleShape)
            .combinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides colors.sub,
        ) {
            Box(modifier = Modifier.size(BentoSizes.ICON.dp)) {
                icon()
            }
        }
    }
}
