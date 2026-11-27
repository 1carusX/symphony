package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.BentoSearchFilter
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiSong

private val BENTO_LOSSLESS_ENCODER_HINTS =
    listOf("flac", "alac", "aiff", "ape", "wv", "pcm", "lossless")

private val BENTO_LOSSLESS_EXTENSIONS =
    listOf(".flac", ".wav", ".aiff", ".aif", ".ape", ".wv", ".alac")

/**
 * Whether a song is lossless for the search filter (DESIGN.md 5.5): the kit
 * does not define the check for the app's metadata, so a lossless encoder
 * name or a lossless file extension qualifies.
 */
fun isBentoLosslessSong(path: String, encoder: String?): Boolean {
    val hint = encoder?.lowercase() ?: ""
    if (BENTO_LOSSLESS_ENCODER_HINTS.any { it in hint }) {
        return true
    }
    val filename = path.lowercase().substringAfterLast('/')
    return BENTO_LOSSLESS_EXTENSIONS.any { filename.endsWith(it) }
}

/**
 * "N d ago" for the Recently added filter rows; "Today" within the first day.
 * With [t], in the app language; without it (tests), in English.
 */
fun formatBentoDaysAgo(dateModifiedMs: Long, nowMs: Long, t: Strings? = null): String {
    val days = ((nowMs - dateModifiedMs) / 86_400_000L).toInt()
    return when {
        days <= 0 -> t?.Today ?: "Today"
        else -> t?.XDaysAgo(days.toString()) ?: "$days d ago"
    }
}

/** The window of the "Recently added" search filter (prototype: 30 days). */
const val BENTO_RECENTLY_ADDED_DAYS = 30

/** Whether a song falls in the "Recently added" search filter. */
fun isBentoRecentlyAddedSong(dateModifiedMs: Long, nowMs: Long): Boolean =
    nowMs - dateModifiedMs <= BENTO_RECENTLY_ADDED_DAYS * 86_400_000L

/** Whether a song passes a smart filter. */
fun BentoSearchFilter.matches(
    songId: String,
    dateModifiedMs: Long,
    path: String,
    encoder: String?,
    favorites: Collection<String>,
    nowMs: Long,
): Boolean = when (this) {
    BentoSearchFilter.FAVORITES -> favorites.contains(songId)
    BentoSearchFilter.RECENTLY_ADDED -> isBentoRecentlyAddedSong(dateModifiedMs, nowMs)
    BentoSearchFilter.LOSSLESS -> isBentoLosslessSong(path, encoder)
}

/** Whether a song passes a smart filter. */
fun BentoSearchFilter.matches(
    song: UiSong,
    favorites: Collection<String>,
    nowMs: Long,
): Boolean = matches(
    songId = song.id,
    dateModifiedMs = song.dateModifiedMillis,
    path = song.path,
    encoder = song.encoder,
    favorites = favorites,
    nowMs = nowMs,
)

/**
 * Search matching (DESIGN.md 5.5): the query matches title, artist and
 * album, case-insensitively.
 */
fun bentoSongMatchesQuery(
    title: String,
    artists: Collection<String>,
    album: String?,
    query: String,
): Boolean {
    val q = query.lowercase()
    return title.lowercase().contains(q) ||
        artists.any { it.lowercase().contains(q) } ||
        album?.lowercase()?.contains(q) == true
}

/** Artist hits match the artist name, case-insensitively. */
fun bentoArtistNameMatchesQuery(artistName: String, query: String): Boolean =
    artistName.lowercase().contains(query.lowercase())

/** Album hits match the album title or its artists, case-insensitively. */
fun bentoAlbumMatchesQuery(album: UiAlbum, query: String): Boolean {
    val q = query.lowercase()
    return album.name.lowercase().contains(q) ||
        album.artistNames.any { it.lowercase().contains(q) }
}
