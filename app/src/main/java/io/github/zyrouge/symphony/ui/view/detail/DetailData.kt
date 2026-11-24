package io.github.zyrouge.symphony.ui.view.detail

import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/** Genres of [songIds], the most common first. */
fun detailGenresOf(context: ViewContext, songIds: List<String>): List<String> =
    countedNames(songIds.flatMap { context.symphony.groove.song.get(it)?.genres ?: emptySet() })

/** Artists of [songIds], the most common first. */
fun detailArtistsOf(context: ViewContext, songIds: List<String>): List<String> =
    countedNames(songIds.flatMap { context.symphony.groove.song.get(it)?.artists ?: emptySet() })

/** Albums of [songIds], in the order they first appear. */
fun detailAlbumsOf(context: ViewContext, songIds: List<String>): List<Album> = songIds
    .mapNotNull { songId ->
        context.symphony.groove.song.get(songId)
            ?.let { context.symphony.groove.album.getIdFromSong(it) }
    }
    .distinct()
    .mapNotNull { context.symphony.groove.album.get(it) }

/**
 * Artists (or album artists) with songs in [genres], the most songs there
 * first, without [exclude].
 */
fun detailSimilarArtistsOf(
    context: ViewContext,
    genres: List<String>,
    exclude: String,
    albumArtists: Boolean,
): List<String> {
    val names = genres
        .flatMap { context.symphony.groove.genre.getSongIds(it) }
        .distinct()
        .flatMap { songId ->
            val song = context.symphony.groove.song.get(songId)
            when {
                song == null -> emptySet()
                albumArtists -> song.albumArtists
                else -> song.artists
            }
        }
        .filter { it != exclude }
    return countedNames(names)
}

private fun countedNames(names: List<String>): List<String> = names
    .groupingBy { it }
    .eachCount()
    .entries
    .sortedByDescending { it.value }
    .map { it.key }
