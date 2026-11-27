package io.github.zyrouge.symphony.ui.adapters

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.Genre
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiAlbumArtist
import io.github.zyrouge.symphony.ui.model.UiArtist
import io.github.zyrouge.symphony.ui.model.UiGenre
import io.github.zyrouge.symphony.ui.model.UiPlaylist
import io.github.zyrouge.symphony.ui.model.UiSong

fun Song.toUiSong() = UiSong(
    id = id,
    title = title,
    album = album,
    artists = artists,
    albumArtists = albumArtists,
    genres = genres,
    durationMillis = duration,
    dateModifiedMillis = dateModified,
    trackNumber = trackNumber,
    discNumber = discNumber,
    path = path,
    coverKey = coverFile,
    encoder = encoder,
)

fun Album.toUiAlbum() = UiAlbum(
    id = id,
    name = name,
    artistNames = artists.toSet(),
    startYear = startYear,
    endYear = endYear,
    trackCount = numberOfTracks,
    durationMillis = duration.inWholeMilliseconds,
)

fun Artist.toUiArtist() = UiArtist(
    name = name,
    albumCount = numberOfAlbums,
    trackCount = numberOfTracks,
)

fun AlbumArtist.toUiAlbumArtist() = UiAlbumArtist(
    name = name,
    albumCount = numberOfAlbums,
    trackCount = numberOfTracks,
)

fun Genre.toUiGenre() = UiGenre(
    name = name,
    trackCount = numberOfTracks,
)

fun Playlist.toUiPlaylist(symphony: Symphony) = UiPlaylist(
    id = id,
    title = title,
    trackIds = getSongIds(symphony),
    isLocal = uri != null,
)
