package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun LoaderScaffold(
    context: ViewContext,
    isLoading: Boolean,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var height by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(
                    bottom = with(density) {
                        if (isLoading) height.toDp() else 0.dp
                    }
                )
        ) {
            content()
        }
        AnimatedVisibility(
            visible = isLoading,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onGloballyPositioned {
                    height = it.size.height
                },
        ) {
            val colors = LocalBentoColors.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.float,
                        RoundedCornerShape(
                            topStart = BentoRadii.TILE.dp,
                            topEnd = BentoRadii.TILE.dp,
                        ),
                    )
                    .padding(16.dp, 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = colors.accent,
                        trackColor = colors.track,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        context.symphony.t.Loading,
                        style = BentoTypography.secondary,
                        color = colors.sub,
                    )
                }
            }
        }
    }
}
