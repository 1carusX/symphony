package io.github.zyrouge.symphony.desktop.library

import io.github.zyrouge.symphony.services.groove.AudioMetadataSource
import io.github.zyrouge.symphony.services.groove.FileInfo
import io.github.zyrouge.symphony.services.groove.FileSystemScanner
import io.github.zyrouge.symphony.services.groove.JaudiotaggerMetadataSource
import io.github.zyrouge.symphony.services.groove.ResolutionOptions
import io.github.zyrouge.symphony.services.groove.SongFields
import io.github.zyrouge.symphony.services.groove.buildSongFields
import io.github.zyrouge.symphony.services.groove.makeSeparatorsRegex
import java.nio.file.Files
import java.nio.file.Path

data class DesktopTrack(
    val path: Path,
    val fields: SongFields,
)

data class SkippedTrack(
    val path: Path,
    val reason: String,
)

data class LibraryScan(
    val root: Path,
    val tracks: List<DesktopTrack>,
    val skipped: List<SkippedTrack>,
    val unavailableReason: String? = null,
) {
    val skippedCount: Int
        get() = skipped.size
}

object DesktopLibrary {
    val defaultSeparators: Set<String> = setOf(";", "/", ",", "+")

    fun scan(
        root: Path,
        separators: Set<String> = defaultSeparators,
        source: AudioMetadataSource = JaudiotaggerMetadataSource,
    ): LibraryScan {
        val normalized = root.toAbsolutePath().normalize()
        val notADirectory = when {
            !Files.exists(normalized) -> "does not exist"
            !Files.isDirectory(normalized) -> "is not a directory"
            !Files.isReadable(normalized) -> "is not readable"
            else -> null
        }
        if (notADirectory != null) {
            return LibraryScan(normalized, emptyList(), emptyList(), notADirectory)
        }

        val files = try {
            FileSystemScanner.scan(normalized)
        } catch (error: Exception) {
            return LibraryScan(
                normalized,
                emptyList(),
                emptyList(),
                error.message ?: error.javaClass.simpleName,
            )
        }

        val options = ResolutionOptions(
            artistSeparatorRegex = makeSeparatorsRegex(separators),
            genreSeparatorRegex = makeSeparatorsRegex(separators),
        )
        val tracks = mutableListOf<DesktopTrack>()
        val skipped = mutableListOf<SkippedTrack>()
        for (file in files) {
            if (!Files.isReadable(file.path)) {
                skipped.add(SkippedTrack(file.path, "not readable"))
                continue
            }
            try {
                val raw = source.read(file)
                if (raw == null) {
                    skipped.add(SkippedTrack(file.path, "no metadata"))
                    continue
                }
                val resolution = buildSongFields(
                    fileInfo = FileInfo(
                        name = file.name,
                        lastModified = file.lastModified,
                        size = file.size,
                        pathName = file.pathName,
                    ),
                    options = options,
                    id = file.pathName,
                    source = source,
                    raw = raw,
                    artworkQualityMaxSide = null,
                )
                tracks.add(DesktopTrack(file.path, resolution.fields))
            } catch (error: Exception) {
                skipped.add(
                    SkippedTrack(file.path, error.message ?: error.javaClass.simpleName),
                )
            }
        }
        return LibraryScan(normalized, tracks, skipped)
    }
}
