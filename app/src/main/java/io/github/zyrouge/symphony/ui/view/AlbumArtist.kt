package io.github.zyrouge.symphony.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.components.AlbumArtistDropdownMenu
import io.github.zyrouge.symphony.ui.components.AlbumRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.serialization.Serializable

@Serializable
data class AlbumArtistViewRoute(val albumArtistName: String)

@Composable
fun AlbumArtistView(context: ViewContext, route: AlbumArtistViewRoute) {
    val allAlbumArtistNames by context.symphony.groove.albumArtist.all.collectAsState()
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val allAlbumIds = context.symphony.groove.album.all
    val albumArtist by remember(allAlbumArtistNames) {
        derivedStateOf { context.symphony.groove.albumArtist.get(route.albumArtistName) }
    }
    val songIds by remember(albumArtist, allSongIds) {
        derivedStateOf { albumArtist?.getSongIds(context.symphony) ?: listOf() }
    }
    val albumIds by remember(albumArtist, allAlbumIds) {
        derivedStateOf { albumArtist?.getAlbumIds(context.symphony) ?: listOf() }
    }

    BentoCollectionPage(
        context = context,
        title = albumArtist?.name ?: route.albumArtistName,
        subtitle = context.symphony.t.AlbumArtist,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = context.symphony.t.UnknownArtistX(route.albumArtistName),
        leading = when {
            albumIds.isEmpty() -> null
            else -> {
                { AlbumRow(context, albumIds) }
            }
        },
        trailingMenu = when {
            albumArtist == null -> null
            else -> {
                {
                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Filled.MoreVert, null)
                        AlbumArtistDropdownMenu(
                            context,
                            albumArtist!!,
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                        )
                    }
                }
            }
        },
    )
}
