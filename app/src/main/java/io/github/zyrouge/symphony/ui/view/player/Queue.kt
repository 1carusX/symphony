package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/** Queue stage with the existing jump, multi-delete, save and clear actions. */
@Composable
internal fun BentoPlayerQueue(context: ViewContext, modifier: Modifier = Modifier) {
    val colors = LocalBentoColors.current
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val selectedSongIndices = remember { mutableStateListOf<Int>() }
    var showSaveDialog by remember { mutableStateOf(false) }
    val upcoming = queue.mapIndexedNotNull { index, songId ->
        if (index > queueIndex) index to songId else null
    }

    LaunchedEffect(queue) {
        selectedSongIndices.clear()
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
            if (selectedSongIndices.isNotEmpty()) {
                QueueIconButton(
                    icon = { Icon(Icons.Filled.Delete, "Remove selected") },
                    onClick = {
                        context.symphony.radio.queue.remove(selectedSongIndices.toList())
                        selectedSongIndices.clear()
                    },
                )
            } else {
                QueueIconButton(
                    icon = { Icon(Icons.Filled.Save, "Save queue") },
                    onClick = { showSaveDialog = true },
                )
            }
            QueueIconButton(
                icon = { Icon(Icons.Filled.ClearAll, "Clear queue") },
                onClick = {
                    context.symphony.radio.stop()
                    selectedSongIndices.clear()
                },
            )
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
                            selected = queuePosition in selectedSongIndices,
                            selecting = selectedSongIndices.isNotEmpty(),
                            onSelect = {
                                if (queuePosition in selectedSongIndices) {
                                    selectedSongIndices.remove(queuePosition)
                                } else {
                                    selectedSongIndices.add(queuePosition)
                                }
                            },
                            onClick = {
                                if (selectedSongIndices.isNotEmpty()) {
                                    if (queuePosition in selectedSongIndices) {
                                        selectedSongIndices.remove(queuePosition)
                                    } else {
                                        selectedSongIndices.add(queuePosition)
                                    }
                                } else {
                                    context.symphony.radio.jumpTo(queuePosition)
                                }
                            },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BentoPlayerQueueRow(
    context: ViewContext,
    song: Song,
    selected: Boolean,
    selecting: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.QUEUE_ROW.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(if (selected) colors.tonal else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onSelect)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (selecting) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.accent else colors.raised)
                    .clickable(onClick = onSelect),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        Icons.Filled.Check,
                        null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
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
                color = if (selected) colors.onTonal else colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artists.joinToString(),
                style = BentoTypography.secondary,
                color = if (selected) colors.tonalSub else colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            DurationUtils.formatMs(song.duration),
            style = bentoTabularStyle(BentoTypography.secondary),
            color = if (selected) colors.tonalSub else colors.muted,
            maxLines = 1,
        )
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
