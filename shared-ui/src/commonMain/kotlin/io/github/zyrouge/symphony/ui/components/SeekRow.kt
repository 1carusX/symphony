package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiPlaybackPosition
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The player seek bar with the played and remaining times. While the thumb is
 * dragged the times follow it.
 */
@Composable
fun BentoSeekRow(
    position: UiPlaybackPosition,
    playing: Boolean,
    seekable: Boolean,
    timeText: (Long) -> String,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    var scrubRatio by remember { mutableStateOf<Float?>(null) }
    val played = scrubRatio?.let { (position.durationMillis * it).toLong() }
        ?: position.playedMillis
    Column(modifier = modifier) {
        BentoSeekWave(
            position = position,
            playing = playing,
            height = BentoSizes.SEEK_HEIGHT.dp,
            seekable = seekable,
            timeText = timeText,
            onSeek = onSeek,
            onScrub = { scrubRatio = it },
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                timeText(played),
                style = bentoTabularStyle(BentoTypography.secondary),
                color = colors.sub,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "-${timeText((position.durationMillis - played).coerceAtLeast(0L))}",
                style = bentoTabularStyle(BentoTypography.secondary),
                color = colors.sub,
            )
        }
    }
}
