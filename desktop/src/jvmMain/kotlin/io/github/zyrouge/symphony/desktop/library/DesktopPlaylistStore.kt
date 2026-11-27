package io.github.zyrouge.symphony.desktop.library

import io.github.zyrouge.symphony.ui.model.UiPlaylist
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * Stores desktop playlists in the XDG config directory. Track ids are the
 * library's path-based ids, so playlists survive rescans as long as the files
 * keep their paths.
 */
class DesktopPlaylistStore(
    private val file: Path = defaultPlaylistFile(),
) {
    fun load(): List<UiPlaylist> = runCatching {
        if (!Files.isRegularFile(file)) return emptyList()
        val properties = Properties()
        Files.newInputStream(file).use(properties::load)
        properties.getProperty("playlist.ids")
            ?.split(',')
            ?.filter { it.isNotBlank() }
            .orEmpty()
            .mapNotNull { id ->
                val title = properties.getProperty("playlist.$id.title")
                    ?: return@mapNotNull null
                val trackIds = properties.getProperty("playlist.$id.tracks")
                    ?.split('\n')
                    ?.filter { it.isNotBlank() }
                    .orEmpty()
                UiPlaylist(
                    id = id,
                    title = title,
                    trackIds = trackIds,
                    isLocal = false,
                )
            }
    }.getOrElse { emptyList() }

    fun save(playlists: List<UiPlaylist>) {
        runCatching {
            val properties = Properties()
            properties.setProperty("playlist.ids", playlists.joinToString(",") { it.id })
            playlists.forEach { playlist ->
                properties.setProperty("playlist.${playlist.id}.title", playlist.title)
                properties.setProperty(
                    "playlist.${playlist.id}.tracks",
                    playlist.trackIds.joinToString("\n"),
                )
            }
            file.parent?.let { Files.createDirectories(it) }
            Files.newOutputStream(file).use { stream ->
                properties.store(stream, "Symphony desktop playlists")
            }
        }
    }

    companion object {
        fun defaultPlaylistFile(): Path {
            val configHome = System.getenv("XDG_CONFIG_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: (System.getProperty("user.home") + "/.config")
            return Path.of(configHome, "symphony", "playlists.properties")
        }
    }
}
