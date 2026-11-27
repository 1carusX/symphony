package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.model.UiSongSort

/** Sorts songs with the Bento list order; [reverse] flips the result. */
fun List<UiSong>.sortedBento(sort: UiSongSort, reverse: Boolean): List<UiSong> {
    val comparator = when (sort) {
        UiSongSort.ARTIST -> compareBy<UiSong> { it.artists.joinToString().lowercase() }
        UiSongSort.ALBUM -> compareBy { it.album.orEmpty().lowercase() }
        UiSongSort.DURATION -> compareBy { it.durationMillis }
        UiSongSort.DATE_MODIFIED -> compareBy { it.dateModifiedMillis }
        else -> compareBy { it.title.lowercase() }
    }
    val sorted = sortedWith(comparator)
    return if (reverse) sorted.asReversed() else sorted
}
