package io.github.zyrouge.symphony.ui.contracts

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiAlbumArtist
import io.github.zyrouge.symphony.ui.model.UiArtist
import io.github.zyrouge.symphony.ui.model.UiArtwork
import io.github.zyrouge.symphony.ui.model.UiGenre
import io.github.zyrouge.symphony.ui.model.UiMusicFolder
import io.github.zyrouge.symphony.ui.model.UiPlaylist
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import kotlinx.coroutines.flow.StateFlow

enum class UiHomePage {
    FOR_YOU,
    SONGS,
    ALBUMS,
    ARTISTS,
    ALBUM_ARTISTS,
    GENRES,
    PLAYLISTS,
    FOLDERS,
    BROWSER,
    TREE,
}

enum class UiLoopMode {
    NONE,
    QUEUE,
    SONG,
}

@Immutable
data class UiCapabilities(
    val canPickMusicFolder: Boolean,
    val hasSafFolders: Boolean,
    val hasAudioFocus: Boolean,
    val hasMediaSession: Boolean,
    val hasNotificationPlayer: Boolean,
    val hasSystemWallpaperColors: Boolean,
)

sealed interface UiRoute {
    data object Home : UiRoute
    data object Search : UiRoute
    data object Settings : UiRoute

    @Immutable
    data class Album(val albumId: String) : UiRoute

    @Immutable
    data class Artist(val name: String) : UiRoute

    @Immutable
    data class AlbumArtist(val name: String) : UiRoute

    @Immutable
    data class Genre(val name: String) : UiRoute

    @Immutable
    data class Playlist(val playlistId: String) : UiRoute
}

interface UiLibraryController {
    val isScanning: StateFlow<Boolean>
    val songs: StateFlow<List<UiSong>>
    val albums: StateFlow<List<UiAlbum>>
    val artists: StateFlow<List<UiArtist>>
    val albumArtists: StateFlow<List<UiAlbumArtist>>
    val genres: StateFlow<List<UiGenre>>
    val playlists: StateFlow<List<UiPlaylist>>
    val musicFolders: StateFlow<List<UiMusicFolder>>
    val favorites: StateFlow<Set<String>>

    fun songArtwork(songId: String): UiArtwork
    fun albumArtwork(albumId: String): UiArtwork
    fun artistArtwork(name: String): UiArtwork
    fun albumArtistArtwork(name: String): UiArtwork
    fun playlistArtwork(playlistId: String): UiArtwork
    fun genreArtwork(name: String): UiArtwork

    fun createPlaylist(title: String, songIds: List<String> = emptyList())
    fun deletePlaylist(playlistId: String)
    fun updatePlaylistSongs(playlistId: String, songIds: List<String>)
}

interface UiPlaybackController {
    val queue: StateFlow<List<String>>
    val queueIndex: StateFlow<Int>
    val isPlaying: StateFlow<Boolean>
    val positionMillis: StateFlow<Long>
    val durationMillis: StateFlow<Long>
    val speed: StateFlow<Float>
    val pitch: StateFlow<Float>
    val loopMode: StateFlow<UiLoopMode>
    val shuffleEnabled: StateFlow<Boolean>

    fun playQueue(songIds: List<String>, startIndex: Int = 0, shuffle: Boolean = false)
    fun enqueue(songIds: List<String>)
    fun playAt(index: Int)
    fun pause()
    fun resume()
    fun togglePlayPause()
    fun stop()
    fun seekTo(positionMillis: Long)
    fun skipToNext()
    fun skipToPrevious()
    fun removeQueueItem(index: Int)
    fun clearUpNext()
    fun setLoopMode(mode: UiLoopMode)
    fun setShuffleEnabled(enabled: Boolean)
    fun setSpeed(value: Float)
    fun setPitch(value: Float)
}

interface UiSettingsController {
    val themeMode: StateFlow<BentoThemeMode>
    val colorScheme: StateFlow<BentoColorScheme>
    val primaryHue: StateFlow<BentoPrimaryHue>
    val fontScale: StateFlow<Float>
    val contentScale: StateFlow<Float>
    val language: StateFlow<String?>
    val homeTabs: StateFlow<List<UiHomePage>>
}

interface UiNavigation {
    val currentRoute: StateFlow<UiRoute?>
    fun navigate(route: UiRoute)
    fun back(): Boolean
}

interface UiPlatformServices {
    val capabilities: UiCapabilities

    fun showMessage(text: String)
    fun openUrl(url: String)
    fun setKeepScreenAwake(enabled: Boolean)
}

interface UiApp {
    val library: UiLibraryController
    val playback: UiPlaybackController
    val settings: UiSettingsController
    val navigation: UiNavigation
    val platform: UiPlatformServices
    val strings: StateFlow<Strings>
}
