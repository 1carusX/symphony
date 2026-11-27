package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * One row of the player queue: artwork, title, artists, duration and a remove
 * button. The platform supplies the artwork.
 */
@Composable
fun QueueRow(
    song: UiSong,
    durationText: String,
    removeContentDescription: String,
    artwork: @Composable (Modifier) -> Unit,
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
        artwork(
            Modifier
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
            durationText,
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
                removeContentDescription,
                tint = colors.muted,
                modifier = Modifier.size(BentoSizes.ICON.dp),
            )
        }
    }
}
