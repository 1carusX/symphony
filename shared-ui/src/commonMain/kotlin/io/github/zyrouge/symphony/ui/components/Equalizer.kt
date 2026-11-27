package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * An equalizer glyph (DESIGN.md 5.1): three bars that bounce while playing
 * and rest at fixed heights otherwise.
 */
@Composable
fun BentoEqualizerGlyph(color: Color, playing: Boolean) {
    val phase = when {
        playing -> {
            val transition = rememberInfiniteTransition(label = "bento-eq")
            val animated by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "bento-eq-phase",
            )
            animated
        }

        else -> 0f
    }
    Canvas(modifier = Modifier.size(13.dp, 14.dp)) {
        val barWidth = 3.dp.toPx()
        val gap = 2.dp.toPx()
        val restHeights = listOf(0.45f, 0.85f, 0.6f)
        val delays = listOf(0.35f, 0f, 0.6f)
        restHeights.forEachIndexed { i, restHeight ->
            val heightFraction = when {
                playing -> {
                    val t = ((phase + delays[i]) % 1f) * 2f
                    val triangle = when {
                        t < 1f -> t
                        else -> 2f - t
                    }
                    0.2f + 0.8f * triangle
                }

                else -> restHeight
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), size.height * (1f - heightFraction)),
                size = Size(barWidth, size.height * heightFraction),
                cornerRadius = CornerRadius(1.dp.toPx()),
            )
        }
    }
}
