package io.github.zyrouge.symphony.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.serialization.Serializable

@Serializable
data class AlbumArtistViewRoute(val albumArtistName: String)

@Composable
fun AlbumArtistView(context: ViewContext, route: AlbumArtistViewRoute) {
    val allAlbumArtistNames by context.symphony.groove.albumArtist.all.collectAsState()
    val grooveVersion = LocalGrooveVersion.current
    val albumArtist by remember(allAlbumArtistNames, grooveVersion) {
        derivedStateOf { context.symphony.groove.albumArtist.get(route.albumArtistName) }
    }
    val songIds by remember(albumArtist, grooveVersion) {
        derivedStateOf { albumArtist?.getSongIds(context.symphony) ?: listOf() }
    }
    val albumIds by remember(albumArtist, grooveVersion) {
        derivedStateOf { albumArtist?.getAlbumIds(context.symphony) ?: listOf() }
    }

    when (val value = albumArtist) {
        null -> BentoCollectionUnknownPage(
            label = context.symphony.t.AlbumArtist,
            message = context.symphony.t.UnknownArtistX(route.albumArtistName),
            icon = Icons.Filled.Person,
            onBack = { context.navController.popBackStack() },
        )

        else -> ArtistCollectionPage(
            context = context,
            label = context.symphony.t.AlbumArtist,
            name = value.name,
            songIds = songIds,
            albumIds = albumIds,
            albumArtist = true,
            trailingMenu = {
                var showOptionsMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(Icons.Filled.MoreVert, null)
                    AlbumArtistDropdownMenu(
                        context,
                        value,
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                    )
                }
            },
        )
    }
}
