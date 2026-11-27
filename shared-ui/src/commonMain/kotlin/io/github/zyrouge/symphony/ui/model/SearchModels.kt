package io.github.zyrouge.symphony.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.zyrouge.symphony.i18n.Strings

/**
 * The smart filters of the search page (DESIGN.md 5.5). "Most played" has no
 * data source in the app (the service layer does not track play counts and
 * stays untouched), so it is omitted.
 */
enum class BentoSearchFilter(
    val icon: ImageVector,
) {
    FAVORITES(Icons.Filled.Favorite),
    RECENTLY_ADDED(Icons.Filled.Schedule),
    LOSSLESS(Icons.Filled.GraphicEq),
}

/** The filter's name in the app language. */
fun BentoSearchFilter.label(t: Strings) = when (this) {
    BentoSearchFilter.FAVORITES -> t.Favorites
    BentoSearchFilter.RECENTLY_ADDED -> t.RecentlyAdded
    BentoSearchFilter.LOSSLESS -> t.Lossless
}

/** The query-dependent half of the results; filters apply on top of it. */
@Immutable
data class BentoSearchResults(
    val songIds: List<String>,
    val artistNames: List<String>,
    val albumIds: List<String>,
)

/** An artist or album hit above the song results. */
@Immutable
data class SearchHit(
    val key: String,
    val title: String,
    val subtitle: String,
    val round: Boolean,
)
