package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.KeepScreenAwake
import io.github.zyrouge.symphony.ui.components.LyricsText
import io.github.zyrouge.symphony.ui.components.TimedContentTextStyle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** Cover, lyrics and queue share one fixed stage so the controls never move. */
@Composable
internal fun BentoPlayerStageView(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    onStageChange: (BentoPlayerStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    PlayerStage(
        stage = stage,
        onStageChange = onStageChange,
        modifier = modifier,
        cover = { coverModifier ->
            AsyncImage(
                data.song.createArtworkImageRequest(context.symphony).build(),
                data.song.title,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.High,
                modifier = coverModifier,
            )
        },
        lyrics = { lyricsModifier ->
            val keepScreenAwake by context.symphony.settings.lyricsKeepScreenAwake.flow
                .collectAsState()
            if (keepScreenAwake) {
                KeepScreenAwake()
            }
            CompositionLocalProvider(
                LocalTextStyle provides BentoTypography.body.copy(color = colors.muted),
            ) {
                Box(
                    modifier = lyricsModifier
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.08f to Color.Black,
                                    0.92f to Color.Black,
                                    1f to Color.Transparent,
                                ),
                                blendMode = BlendMode.DstIn,
                            )
                        },
                ) {
                    LyricsText(
                        context = context,
                        padding = PaddingValues(horizontal = 22.dp, vertical = 42.dp),
                        style = TimedContentTextStyle(
                            highlighted = BentoTypography.title.copy(color = colors.muted),
                            active = BentoTypography.title.copy(color = colors.fg),
                            inactive = BentoTypography.title.copy(color = colors.muted),
                            spacing = 8.dp,
                        ),
                    )
                }
            }
        },
        queue = { queueModifier ->
            BentoPlayerQueue(
                context = context,
                modifier = queueModifier,
            )
        },
    )
}
