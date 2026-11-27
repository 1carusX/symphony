package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** The three states of the player stage. */
enum class BentoPlayerStage {
    COVER,
    LYRICS,
    QUEUE,
}

/**
 * Cover, lyrics and queue share one fixed stage so the controls never move.
 * The platform supplies the three layers; tapping the cover opens the lyrics
 * and tapping the lyrics returns to the cover.
 */
@Composable
fun PlayerStage(
    stage: BentoPlayerStage,
    onStageChange: (BentoPlayerStage) -> Unit,
    cover: @Composable (Modifier) -> Unit,
    lyrics: @Composable (Modifier) -> Unit,
    queue: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    Box(modifier = modifier) {
        when (stage) {
            BentoPlayerStage.COVER -> BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val coverSize = minOf(maxWidth, maxHeight, 640.dp)
                cover(
                    Modifier
                        .size(coverSize)
                        .clip(RoundedCornerShape(BentoRadii.PLAYER_COVER.dp))
                        .clickable { onStageChange(BentoPlayerStage.LYRICS) },
                )
            }

            BentoPlayerStage.LYRICS -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(BentoRadii.PLAYER_COVER.dp))
                    .background(colors.raised)
                    .clickable { onStageChange(BentoPlayerStage.COVER) },
            ) {
                lyrics(Modifier.fillMaxSize())
            }

            BentoPlayerStage.QUEUE -> queue(Modifier.fillMaxSize())
        }
    }
}
