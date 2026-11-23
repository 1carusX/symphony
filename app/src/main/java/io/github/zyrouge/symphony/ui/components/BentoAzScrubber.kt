package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

/** The A-Z letters of the scrubber, in order (DESIGN.md 4.6). */
val BENTO_AZ_LETTERS = ('A'..'Z').map { it.toString() } + "#"

/**
 * Resolves the scrub target while dragging (prototype scrubAt): a letter
 * without songs is skipped forward, then backward; null when no letter has
 * songs at all.
 */
fun resolveBentoScrubLetter(presentLetters: Set<String>, pressedLetter: String): String? {
    if (pressedLetter in presentLetters) {
        return pressedLetter
    }
    val pressedIndex = BENTO_AZ_LETTERS.indexOf(pressedLetter).coerceAtLeast(0)
    for (i in pressedIndex until BENTO_AZ_LETTERS.size) {
        if (BENTO_AZ_LETTERS[i] in presentLetters) return BENTO_AZ_LETTERS[i]
    }
    for (i in pressedIndex downTo 0) {
        if (BENTO_AZ_LETTERS[i] in presentLetters) return BENTO_AZ_LETTERS[i]
    }
    return null
}

// ---------------------------------------------------------------------------
// Playing-song state
// ---------------------------------------------------------------------------

/**
 * The song the radio is on, with its playing state, for row highlighting.
 */
@Immutable
data class BentoPlayingSong(val songId: String?, val isPlaying: Boolean)

/**
 * A letter header of the Songs page (DESIGN.md 5.2): 34 dp, 13/700 accent,
 * on `surface` so rows do not show through it while it sticks.
 */
@Composable
fun BentoLetterHeader(letter: String) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.LETTER_HEADER.dp)
            .background(colors.surface)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            letter,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.Bold),
            color = colors.accent,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// A-Z scrubber (DESIGN.md 4.6)
// ---------------------------------------------------------------------------

/**
 * The A-Z scrubber (DESIGN.md 4.6): a 24 dp column of letters at the right
 * edge, vertically centered on screen, min(520, window height - 260) tall.
 * Letters with songs use `accent`, the others render dimmed and are skipped
 * while scrubbing. A 56 dp accent bubble shows the current letter while
 * dragging and the caller jumps the list to that letter's header.
 */
@Composable
fun BentoAzScrubber(
    presentLetters: Set<String>,
    onLetter: (String) -> Unit,
) {
    val colors = LocalBentoColors.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val height = minOf(
        BentoSizes.AZ_SCRUBBER_MAX_HEIGHT.dp,
        windowInfo.containerDpSize.height - 260.dp,
    ).coerceAtLeast(120.dp)
    var dragY by remember { mutableStateOf<Float?>(null) }
    var bubbleLetter by remember { mutableStateOf<String?>(null) }
    // The gesture blocks below outlive recompositions; without this they would
    // keep the first callback and jump to stale indices after a sort change.
    val currentOnLetter by rememberUpdatedState(onLetter)

    fun scrubAt(yPx: Float, boxHeightPx: Float) {
        with(density) {
            val usable = boxHeightPx - 20.dp.toPx()
            val fraction = ((yPx - 10.dp.toPx()) / usable).coerceIn(0f, 0.9999f)
            val index = (fraction * BENTO_AZ_LETTERS.size).toInt().coerceIn(0, 26)
            val letter = resolveBentoScrubLetter(presentLetters, BENTO_AZ_LETTERS[index])
            if (letter != null) {
                dragY = yPx
                if (bubbleLetter != letter) {
                    bubbleLetter = letter
                    currentOnLetter(letter)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .width(30.dp)
            .height(height)
            .pointerInput(presentLetters, height) {
                detectVerticalDragGestures(
                    onDragStart = { offset -> scrubAt(offset.y, size.height.toFloat()) },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        scrubAt(change.position.y, size.height.toFloat())
                    },
                    onDragEnd = {
                        dragY = null
                        bubbleLetter = null
                    },
                    onDragCancel = {
                        dragY = null
                        bubbleLetter = null
                    },
                )
            }
            .pointerInput(presentLetters, height) {
                detectTapGestures { offset ->
                    scrubAt(offset.y, size.height.toFloat())
                    dragY = null
                    bubbleLetter = null
                }
            },
    ) {
        Column(
            modifier = Modifier
                .width(BentoSizes.AZ_SCRUBBER_WIDTH.dp)
                .fillMaxHeight()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BENTO_AZ_LETTERS.forEach { letter ->
                val present = letter in presentLetters
                Text(
                    letter,
                    style = BentoTypography.index,
                    color = when {
                        present -> colors.accent
                        else -> colors.muted.copy(alpha = 0.45f)
                    },
                    maxLines = 1,
                )
            }
        }
        val y = dragY
        val letter = bubbleLetter
        if (y != null && letter != null) {
            with(density) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset((-60).dp.roundToPx(), (y - 28.dp.toPx()).roundToInt()) }
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp, 28.dp, 6.dp, 28.dp))
                        .background(colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        letter,
                        style = BentoTypography.display,
                        color = colors.onAccent,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Grouped tiles (DESIGN.md 5.3)
// ---------------------------------------------------------------------------
