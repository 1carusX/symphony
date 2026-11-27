package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.desktop.library.DesktopTrack
import io.github.zyrouge.symphony.services.groove.SongFields
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiAlbumArtist
import io.github.zyrouge.symphony.ui.model.UiArtist
import io.github.zyrouge.symphony.ui.model.UiGenre
import io.github.zyrouge.symphony.ui.model.UiMusicFolder
import io.github.zyrouge.symphony.ui.model.UiSong
import java.nio.file.Path

fun SongFields.toUiSong() = UiSong(
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

fun DesktopTrack.toUiSong() = fields.toUiSong().copy(path = path.toString())

fun List<UiSong>.toUiAlbums(): List<UiAlbum> = groupBy { it.album }
    .filterKeys { it.isNullOrBlank().not() }
    .map { (name, songs) ->
        UiAlbum(
            id = "album:${name.orEmpty()}",
            name = name.orEmpty(),
            artistNames = songs.flatMap { it.albumArtists.ifEmpty { it.artists } }.toSet(),
            startYear = null,
            endYear = null,
            trackCount = songs.size,
            durationMillis = songs.sumOf { it.durationMillis },
        )
    }
    .sortedBy { it.name.lowercase() }

fun List<UiSong>.toUiArtists(): List<UiArtist> = groupByArtist { it.artists }
    .map { (name, songs) ->
        UiArtist(
            name = name,
            albumCount = songs.mapNotNull { it.album }.toSet().size,
            trackCount = songs.size,
        )
    }
    .sortedBy { it.name.lowercase() }

fun List<UiSong>.toUiAlbumArtists(): List<UiAlbumArtist> = groupByArtist { it.albumArtists }
    .map { (name, songs) ->
        UiAlbumArtist(
            name = name,
            albumCount = songs.mapNotNull { it.album }.toSet().size,
            trackCount = songs.size,
        )
    }
    .sortedBy { it.name.lowercase() }

fun List<UiSong>.toUiGenres(): List<UiGenre> = groupByArtist { it.genres }
    .map { (name, songs) -> UiGenre(name = name, trackCount = songs.size) }
    .sortedBy { it.name.lowercase() }

fun List<UiSong>.toUiMusicFolders(): List<UiMusicFolder> = mapNotNull { song ->
    runCatching { Path.of(song.path).parent }.getOrNull()
}
    .distinct()
    .map { folder ->
        UiMusicFolder(
            id = "folder:$folder",
            name = folder.fileName?.toString() ?: folder.toString(),
            uri = null,
            path = folder.toString(),
        )
    }
    .sortedBy { it.path }

private fun List<UiSong>.groupByArtist(namesOf: (UiSong) -> Set<String>) =
    flatMap { song -> namesOf(song).map { it to song } }
        .groupBy({ it.first }, { it.second })
