package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** One upcoming song: its position in the queue and the song itself. */
@Immutable
data class QueueEntry(
    val position: Int,
    val song: UiSong,
)

/**
 * The player queue panel: "up next" header with save and clear actions, the
 * upcoming rows, or an end-of-queue message.
 */
@Composable
fun BentoPlayerQueue(
    entries: List<QueueEntry>,
    upNextLabel: String,
    countLabel: String,
    endOfQueueLabel: String,
    saveQueueLabel: String,
    clearUpNextLabel: String,
    durationText: (UiSong) -> String,
    removeContentDescription: (UiSong) -> String,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    onEntryClick: (QueueEntry) -> Unit,
    onEntryRemove: (QueueEntry) -> Unit,
    onClearUpNext: () -> Unit,
    onSaveQueue: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current

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
                    upNextLabel,
                    style = BentoTypography.rowTitle,
                    color = colors.fg,
                )
                Text(
                    countLabel,
                    style = BentoTypography.secondary,
                    color = colors.muted,
                )
            }
            if (onSaveQueue != null) {
                QueueIconButton(
                    icon = { Icon(Icons.Filled.Save, saveQueueLabel) },
                    onClick = onSaveQueue,
                )
            }
            if (entries.isNotEmpty()) {
                QueueIconButton(
                    icon = { Icon(Icons.Filled.ClearAll, clearUpNextLabel) },
                    onClick = onClearUpNext,
                )
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    endOfQueueLabel,
                    style = BentoTypography.body,
                    color = colors.muted,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                itemsIndexed(
                    items = entries,
                    key = { _, entry -> "${entry.position}-${entry.song.id}" },
                ) { _, entry ->
                    QueueRow(
                        song = entry.song,
                        durationText = durationText(entry.song),
                        removeContentDescription = removeContentDescription(entry.song),
                        artwork = { contentModifier -> artwork(entry.song, contentModifier) },
                        onClick = { onEntryClick(entry) },
                        onRemove = { onEntryRemove(entry) },
                    )
                }
            }
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
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides colors.sub,
        ) {
            Box(modifier = Modifier.size(BentoSizes.ICON.dp)) {
                icon()
            }
        }
    }
}
