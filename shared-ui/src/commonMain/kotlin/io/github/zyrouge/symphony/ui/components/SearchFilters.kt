package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.BentoSearchFilter
import io.github.zyrouge.symphony.ui.model.label
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing

/**
 * The smart filter tiles (DESIGN.md 5.5): two per row, a last odd tile
 * taking the whole row, or stacked 56 dp rows in the wide side column.
 */
@Composable
fun BentoSearchFilterTiles(
    selectedFilter: BentoSearchFilter?,
    filterCounts: Map<BentoSearchFilter, Int>,
    t: Strings,
    onFilterChange: (BentoSearchFilter?) -> Unit,
    sideColumn: Boolean,
) {
    // A large font or content scale leaves two tiles a row no room for their names.
    // The window width is converted with the app's density, so the content scale counts.
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp().value }
    val crowded = bentoTooNarrowForText(
        windowWidthDp - BentoSpacing.GUTTER_COMPACT * 2f,
        density.fontScale,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BentoSearchFilter.entries.chunked(if (sideColumn || crowded) 1 else 2).forEach { rowFilters ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFilters.forEach { filter ->
                    Box(modifier = Modifier.weight(1f)) {
                        BentoSmartFilterTile(
                            icon = filter.icon,
                            label = filter.label(t),
                            count = t.XSongs((filterCounts[filter] ?: 0).toString()),
                            active = selectedFilter == filter,
                            sideColumn = sideColumn,
                            onClick = {
                                onFilterChange(
                                    when {
                                        selectedFilter == filter -> null
                                        else -> filter
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * The genre chips (DESIGN.md 5.5): "All genres" plus each genre; the
 * selected chip is tonal.
 */
@Composable
fun BentoGenreChips(
    genreNames: List<String>,
    selectedGenre: String?,
    allGenresLabel: String,
    onGenreChange: (String?) -> Unit,
) {
    BentoGenreChip(
        label = allGenresLabel,
        selected = selectedGenre == null,
        onClick = { onGenreChange(null) },
    )
    genreNames.forEach { genreName ->
        BentoGenreChip(
            label = genreName,
            selected = selectedGenre == genreName,
            onClick = {
                onGenreChange(
                    when {
                        selectedGenre == genreName -> null
                        else -> genreName
                    },
                )
            },
        )
    }
}
