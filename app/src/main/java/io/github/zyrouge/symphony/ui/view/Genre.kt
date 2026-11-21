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
import io.github.zyrouge.symphony.ui.components.GenericSongListDropdown
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.serialization.Serializable

@Serializable
data class GenreViewRoute(val genreName: String)

@Composable
fun GenreView(context: ViewContext, route: GenreViewRoute) {
    val allGenreNames by context.symphony.groove.genre.all.collectAsState()
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val genre by remember(allGenreNames) {
        derivedStateOf { context.symphony.groove.genre.get(route.genreName) }
    }
    val songIds by remember(genre, allSongIds) {
        derivedStateOf { genre?.getSongIds(context.symphony) ?: listOf() }
    }

    BentoCollectionPage(
        context = context,
        title = genre?.name ?: route.genreName,
        subtitle = context.symphony.t.Genre,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = context.symphony.t.UnknownGenreX(route.genreName),
        trailingMenu = {
            var showOptionsMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                Icon(Icons.Filled.MoreVert, null)
                GenericSongListDropdown(
                    context,
                    songIds = songIds,
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                )
            }
        },
    )
}
