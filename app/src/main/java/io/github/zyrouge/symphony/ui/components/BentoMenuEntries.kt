package io.github.zyrouge.symphony.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumArtistViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute

/**
 * The playback group of a group menu (prototype MENUS.grp): Shuffle play,
 * Play next, Add to queue and Add to playlist. The song lists are read on
 * click, so opening the menu stays cheap.
 */
@Composable
internal fun BentoQueueMenuItems(
    context: ViewContext,
    orderedSongIds: () -> List<String>,
    onDismissRequest: () -> Unit,
    onAddToPlaylist: () -> Unit,
    queueSongIds: () -> List<String> = orderedSongIds,
) {
    BentoMenuItem(
        label = context.symphony.t.ShufflePlay,
        icon = Icons.Rounded.Shuffle,
        onClick = {
            onDismissRequest()
            context.symphony.radio.shorty.playQueue(orderedSongIds(), shuffle = true)
        },
    )
    BentoMenuItem(
        label = context.symphony.t.PlayNext,
        icon = Icons.Rounded.SkipNext,
        onClick = {
            onDismissRequest()
            context.symphony.radio.queue.add(
                orderedSongIds(),
                context.symphony.radio.queue.currentSongIndex + 1,
            )
        },
    )
    BentoMenuItem(
        label = context.symphony.t.AddToQueue,
        icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
        onClick = {
            onDismissRequest()
            context.symphony.radio.queue.add(queueSongIds())
        },
    )
    BentoMenuItem(
        label = context.symphony.t.AddToPlaylist,
        icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
        onClick = {
            onDismissRequest()
            onAddToPlaylist()
        },
    )
}

/** An artist a menu can open: an artist page, or an album artist page. */
@Immutable
internal data class BentoArtistLink(val name: String, val albumArtist: Boolean)

/**
 * A song's artists, then its album artists that are not also artists. Album
 * artists open the album artist page, since the artist page only knows the
 * song artists.
 */
internal fun bentoSongArtistLinks(song: Song): List<BentoArtistLink> {
    val links = song.artists.distinct().map { BentoArtistLink(it, albumArtist = false) }
    val extra = song.albumArtists
        .filter { it !in song.artists }
        .distinct()
        .map { BentoArtistLink(it, albumArtist = true) }
    return links + extra
}

/**
 * One "Go to artist" entry. With one artist it opens that artist. With
 * several it expands in place into one row per name (aligned under the
 * label, album artists tagged), so the menu stays short until asked.
 */
@Composable
internal fun BentoGoToArtistItem(
    context: ViewContext,
    links: List<BentoArtistLink>,
    onDismissRequest: () -> Unit,
) {
    if (links.isEmpty()) {
        return
    }

    fun open(link: BentoArtistLink) {
        onDismissRequest()
        context.navController.navigate(
            when {
                link.albumArtist -> AlbumArtistViewRoute(link.name)
                else -> ArtistViewRoute(link.name)
            },
        )
    }

    if (links.size == 1) {
        BentoMenuItem(
            label = context.symphony.t.ViewArtist,
            icon = Icons.Outlined.Person,
            onClick = { open(links.first()) },
        )
        return
    }

    var showAll by remember(links) { mutableStateOf(false) }
    BentoMenuItem(
        label = context.symphony.t.ViewArtist,
        icon = Icons.Outlined.Person,
        trailingIcon = when {
            showAll -> Icons.Rounded.ExpandLess
            else -> Icons.Rounded.ExpandMore
        },
        onClick = { showAll = !showAll },
    )
    if (showAll) {
        links.forEach { link ->
            BentoMenuItem(
                label = link.name,
                icon = null,
                value = when {
                    link.albumArtist -> context.symphony.t.AlbumArtist
                    else -> null
                },
                onClick = { open(link) },
            )
        }
    }
}
