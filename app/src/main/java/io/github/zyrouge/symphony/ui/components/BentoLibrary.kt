package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * Shared building blocks of the bento library pages (DESIGN.md 4.4 rows,
 * 5.2 Songs, 5.3 grouped tiles, 5.5 search filters) plus the pure layout
 * logic those pages need: letter grouping, masonry distribution, group
 * ordering and the album placement helpers.
 */

// ---------------------------------------------------------------------------
// Pure logic
// ---------------------------------------------------------------------------

/**
 * The number of rows a grouped tile shows (DESIGN.md 5.3): up to six, with a
 * "Show all" row replacing the sixth song when the group is longer.
 */
const val BENTO_GROUP_MAX_ROWS = 6

@Composable
fun rememberBentoPlayingSong(context: ViewContext): BentoPlayingSong {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    return remember(queue, queueIndex, isPlaying) {
        BentoPlayingSong(songId = queue.getOrNull(queueIndex), isPlaying = isPlaying)
    }
}
