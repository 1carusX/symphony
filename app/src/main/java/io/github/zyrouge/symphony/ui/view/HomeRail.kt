package io.github.zyrouge.symphony.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.github.zyrouge.symphony.ui.components.HomeNavItem
import io.github.zyrouge.symphony.ui.components.HomeRailLabeled
import io.github.zyrouge.symphony.ui.components.HomeRailShort
import io.github.zyrouge.symphony.ui.components.HomeRailWide
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount

@Composable
internal fun HomeRailColumnLabeled(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    HomeRailLabeled(
        items = sections.map { it.toNavItem(context) },
        selectedKey = currentTab.name,
        onSelect = { key -> sections.firstOrNull { it.name == key }?.let(onSelectSection) },
        onSearch = { context.navController.navigate(SearchViewRoute(currentTab.kind?.name)) },
        onSettings = { context.navController.navigate(SettingsViewRoute()) },
        searchLabel = context.symphony.t.SearchYourMusic,
        settingsLabel = context.symphony.t.Settings,
    )
}

@Composable
internal fun HomeRailColumnShort(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    HomeRailShort(
        items = sections.map { it.toNavItem(context) },
        selectedKey = currentTab.name,
        onSelect = { key -> sections.firstOrNull { it.name == key }?.let(onSelectSection) },
        onSearch = { context.navController.navigate(SearchViewRoute(currentTab.kind?.name)) },
        onSettings = { context.navController.navigate(SettingsViewRoute()) },
        searchLabel = context.symphony.t.SearchYourMusic,
        settingsLabel = context.symphony.t.Settings,
    )
}

@Composable
internal fun HomeRailColumnWide(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    HomeRailWide(
        items = sections.map { section ->
            section.toNavItem(context).copy(count = rememberSectionCount(context, section))
        },
        selectedKey = currentTab.name,
        onSelect = { key -> sections.firstOrNull { it.name == key }?.let(onSelectSection) },
        onSearch = { context.navController.navigate(SearchViewRoute(currentTab.kind?.name)) },
        onSettings = { context.navController.navigate(SettingsViewRoute()) },
        searchLabel = context.symphony.t.SearchYourMusic,
        settingsLabel = context.symphony.t.Settings,
        appName = "Symphony",
    )
}

private fun HomePage.toNavItem(context: ViewContext) = HomeNavItem(
    key = name,
    label = label(context),
    icon = icon,
)

/**
 * Section count for the wide rail rows. The design kit covers Songs,
 * Albums, Artists, Genres, Playlists and Folders; ForYou has no count.
 */
@Composable
private fun rememberSectionCount(context: ViewContext, section: HomePage): Int? {
    return when (section) {
        HomePage.Songs -> context.symphony.groove.song.count.collectAsState().value
        HomePage.Albums -> context.symphony.groove.album.count.collectAsState().value
        HomePage.Artists -> context.symphony.groove.artist.count.collectAsState().value
        HomePage.AlbumArtists -> context.symphony.groove.albumArtist.count.collectAsState().value
        HomePage.Genres -> context.symphony.groove.genre.count.collectAsState().value
        HomePage.Playlists -> context.symphony.groove.playlist.count.collectAsState().value
        HomePage.Folders -> rememberGrooveFolderCount(context)
        else -> null
    }
}
