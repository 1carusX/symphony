package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.ui.components.BentoGenreChip
import io.github.zyrouge.symphony.ui.components.BentoSmartFilterTile
import io.github.zyrouge.symphony.ui.components.label
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The search field (DESIGN.md 4.3): a 52 dp pill with the search icon, a
 * 15 sp input, a clear button and the [trailing] buttons (the sort button)
 * inside it; a 2 dp accent ring while focused, else a 1 dp `line` ring in
 * the Contrast scheme.
 */
@Composable
internal fun SearchField(
    context: ViewContext,
    terms: String,
    onTermsChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    var focused by remember { mutableStateOf(false) }
    val fieldShape = RoundedCornerShape(BentoRadii.SEARCH_FIELD.dp)

    Row(
        modifier = modifier
            .height(BentoSizes.SEARCH_FIELD.dp)
            .clip(fieldShape)
            .background(colors.surface)
            .then(
                when {
                    focused -> Modifier.border(2.dp, colors.accent, fieldShape)
                    colorScheme == BentoColorScheme.CONTRAST ->
                        Modifier.border(1.dp, colors.line, fieldShape)
                    else -> Modifier
                },
            )
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Filled.Search,
            context.symphony.t.SearchYourMusic,
            tint = colors.sub,
        )
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = terms,
                onValueChange = onTermsChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = colors.fg,
                    // BasicTextField does not inherit the app font from the theme.
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                ),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focused = it.isFocused },
            )
            if (terms.isEmpty()) {
                Text(
                    context.symphony.t.SearchYourMusic,
                    style = BentoTypography.rowTitle,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (terms.isNotEmpty()) {
            IconButton(
                content = {
                    Icon(
                        Icons.Filled.Close,
                        null,
                        tint = colors.sub,
                    )
                },
                onClick = { onTermsChange("") },
            )
        }
        trailing()
    }
}

/**
 * The smart filter tiles (DESIGN.md 5.5): two per row, a last odd tile
 * taking the whole row, or stacked 56 dp rows in the wide side column.
 */
@Composable
internal fun BentoSearchFilterTiles(
    context: ViewContext,
    selectedFilter: BentoSearchFilter?,
    filterCounts: Map<BentoSearchFilter, Int>,
    onFilterChange: (BentoSearchFilter?) -> Unit,
    sideColumn: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BentoSearchFilter.entries.chunked(if (sideColumn) 1 else 2).forEach { rowFilters ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFilters.forEach { filter ->
                    Box(modifier = Modifier.weight(1f)) {
                        BentoSmartFilterTile(
                            icon = filter.icon,
                            label = filter.label,
                            count = context.symphony.t.XSongs(
                                (filterCounts[filter] ?: 0).toString(),
                            ),
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
internal fun BentoGenreChips(
    context: ViewContext,
    genreNames: List<String>,
    selectedGenre: String?,
    onGenreChange: (String?) -> Unit,
) {
    BentoGenreChip(
        label = context.symphony.t.AllGenres,
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
