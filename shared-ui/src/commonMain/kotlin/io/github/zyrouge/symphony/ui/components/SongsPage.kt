package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.UiSongSort
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing

/**
 * The Songs page skeleton: the page gutter, the action tiles and the list
 * area. The list itself stays with the platform so it can add its own
 * surface, insets and scroll state.
 */
@Composable
fun BentoSongsPage(
    strings: Strings,
    sort: UiSongSort,
    sortReverse: Boolean,
    songCount: Int,
    onSortChange: (UiSongSort) -> Unit,
    onReverseChange: (Boolean) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    totalDurationText: String? = null,
    list: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compactWindow = maxWidth < 600.dp
        val gutter = when {
            compactWindow -> BentoSpacing.GUTTER_COMPACT.dp
            else -> BentoSpacing.GUTTER_OTHER.dp
        }
        val gap = when {
            compactWindow -> BentoSpacing.GAP_COMPACT.dp
            else -> BentoSpacing.GAP_OTHER.dp
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = gutter),
        ) {
            BentoSongsActions(
                strings = strings,
                sort = sort,
                sortReverse = sortReverse,
                songCount = songCount,
                totalDurationText = totalDurationText,
                onSortChange = onSortChange,
                onReverseChange = onReverseChange,
                onPlayAll = onPlayAll,
                onShuffle = onShuffle,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = gap),
            ) {
                list(Modifier.fillMaxSize())
            }
        }
    }
}
