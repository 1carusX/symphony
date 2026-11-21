package io.github.zyrouge.symphony.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.ui.components.AlbumRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.serialization.Serializable

@Serializable
data class ArtistViewRoute(val artistName: String)

@Composable
fun ArtistView(context: ViewContext, route: ArtistViewRoute) {
    val allArtistNames by context.symphony.groove.artist.all.collectAsState()
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val artist by remember(allArtistNames) {
        derivedStateOf { context.symphony.groove.artist.get(route.artistName) }
    }
    val songIds by remember(artist, allSongIds) {
        derivedStateOf { artist?.getSongIds(context.symphony) ?: listOf() }
    }
    val albumIds by remember(artist) {
        derivedStateOf { artist?.getAlbumIds(context.symphony) ?: listOf() }
    }

    BentoCollectionPage(
        context = context,
        title = artist?.name ?: route.artistName,
        subtitle = context.symphony.t.Artist,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = context.symphony.t.UnknownArtistX(route.artistName),
        leading = when {
            albumIds.isEmpty() -> null
            else -> {
                { AlbumRow(context, albumIds) }
            }
        },
    )
}
