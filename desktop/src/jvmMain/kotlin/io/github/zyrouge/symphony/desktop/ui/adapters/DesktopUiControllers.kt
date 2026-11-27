package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.desktop.library.DesktopTrack
import io.github.zyrouge.symphony.desktop.player.AudioPlayer
import io.github.zyrouge.symphony.desktop.player.PlaybackState
import io.github.zyrouge.symphony.desktop.player.VlcjAudioPlayer
import io.github.zyrouge.symphony.ui.contracts.UiLibraryController
import io.github.zyrouge.symphony.ui.contracts.UiLoopMode
import io.github.zyrouge.symphony.ui.contracts.UiPlaybackController
import io.github.zyrouge.symphony.ui.model.UiArtwork
import io.github.zyrouge.symphony.ui.model.UiPlaylist
import io.github.zyrouge.symphony.ui.model.UiSong
import java.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DesktopUiLibraryController(
    private val scope: CoroutineScope,
    initialPlaylists: List<UiPlaylist> = emptyList(),
    private val onPlaylistsChanged: (List<UiPlaylist>) -> Unit = {},
) : UiLibraryController {
    private val _isScanning = MutableStateFlow(false)
    override val isScanning = _isScanning.asStateFlow()

    private val _songs = MutableStateFlow<List<UiSong>>(emptyList())
    override val songs = _songs.asStateFlow()

    override val albums = songs
        .map { it.toUiAlbums() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val artists = songs
        .map { it.toUiArtists() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val albumArtists = songs
        .map { it.toUiAlbumArtists() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val genres = songs
        .map { it.toUiGenres() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _playlists = MutableStateFlow(initialPlaylists)
    override val playlists = _playlists.asStateFlow()
    private var playlistIds = 0
    override val musicFolders = songs
        .map { it.toUiMusicFolders() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val favorites = MutableStateFlow<Set<String>>(emptySet())

    fun beginScan() {
        _isScanning.value = true
    }

    fun updateTracks(tracks: List<DesktopTrack>) {
        _songs.value = tracks.map { it.toUiSong() }
        _isScanning.value = false
    }

    fun clear() {
        _songs.value = emptyList()
        _isScanning.value = false
    }

    override fun createPlaylist(title: String, songIds: List<String>) {
        val playlist = UiPlaylist(
            id = nextPlaylistId(),
            title = title,
            trackIds = songIds,
            isLocal = false,
        )
        updatePlaylists(_playlists.value + playlist)
    }

    override fun deletePlaylist(playlistId: String) {
        updatePlaylists(_playlists.value.filterNot { it.id == playlistId })
    }

    override fun updatePlaylistSongs(playlistId: String, songIds: List<String>) {
        updatePlaylists(
            _playlists.value.map { playlist ->
                when {
                    playlist.id == playlistId -> playlist.copy(trackIds = songIds)
                    else -> playlist
                }
            },
        )
    }

    private fun nextPlaylistId(): String {
        while (true) {
            val id = "local-${playlistIds++}"
            if (_playlists.value.none { it.id == id }) return id
        }
    }

    private fun updatePlaylists(playlists: List<UiPlaylist>) {
        _playlists.value = playlists
        onPlaylistsChanged(playlists)
    }

    override fun songArtwork(songId: String) = UiArtwork.Song(songId)
    override fun albumArtwork(albumId: String) = UiArtwork.Album(albumId)
    override fun artistArtwork(name: String) = UiArtwork.Artist(name)
    override fun albumArtistArtwork(name: String) = UiArtwork.AlbumArtist(name)
    override fun playlistArtwork(playlistId: String) = UiArtwork.Playlist(playlistId)
    override fun genreArtwork(name: String) = UiArtwork.Genre(name)
}

class DesktopUiPlaybackController(
    private val library: UiLibraryController,
    private val scope: CoroutineScope,
    private val player: AudioPlayer = VlcjAudioPlayer(),
) : UiPlaybackController {
    private val _queue = MutableStateFlow<List<String>>(emptyList())
    override val queue = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    override val queueIndex = _queueIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying = _isPlaying.asStateFlow()

    private val _positionMillis = MutableStateFlow(0L)
    override val positionMillis = _positionMillis.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val _speed = MutableStateFlow(1f)
    override val speed = _speed.asStateFlow()

    private val _pitch = MutableStateFlow(1f)
    override val pitch = _pitch.asStateFlow()

    private val _loopMode = MutableStateFlow(UiLoopMode.NONE)
    override val loopMode = _loopMode.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    override val shuffleEnabled = _shuffleEnabled.asStateFlow()

    private var previousState: PlaybackState = PlaybackState.Idle

    init {
        scope.launch {
            while (isActive) {
                refresh()
                delay(250)
            }
        }
    }

    override fun playQueue(songIds: List<String>, startIndex: Int, shuffle: Boolean) {
        if (songIds.isEmpty()) {
            stop()
            return
        }
        val nextQueue = if (shuffle) songIds.shuffled() else songIds
        _queue.value = nextQueue
        _queueIndex.value = if (shuffle) 0 else startIndex.coerceIn(0, nextQueue.lastIndex)
        playCurrent()
    }

    override fun enqueue(songIds: List<String>) {
        if (songIds.isEmpty()) return
        _queue.value = _queue.value + songIds
    }

    override fun playAt(index: Int) {
        if (index !in _queue.value.indices) return
        _queueIndex.value = index
        playCurrent()
    }

    override fun pause() {
        player.pause()
        refresh()
    }

    override fun resume() {
        player.resume()
        refresh()
    }

    override fun togglePlayPause() {
        when (player.state) {
            is PlaybackState.Playing -> pause()
            is PlaybackState.Paused -> resume()
            else -> playCurrent()
        }
    }

    override fun stop() {
        player.stop()
        _queue.value = emptyList()
        _queueIndex.value = -1
        _isPlaying.value = false
        _positionMillis.value = 0
        _durationMillis.value = 0
        previousState = PlaybackState.Idle
    }

    override fun seekTo(positionMillis: Long) {
        player.seekTo(positionMillis)
        refresh()
    }

    override fun skipToNext() {
        val next = _queueIndex.value + 1
        if (next in _queue.value.indices) {
            playAt(next)
        }
    }

    override fun skipToPrevious() {
        val previous = _queueIndex.value - 1
        if (previous in _queue.value.indices) {
            playAt(previous)
        }
    }

    override fun removeQueueItem(index: Int) {
        if (index !in _queue.value.indices) return
        val wasCurrent = index == _queueIndex.value
        _queue.value = _queue.value.filterIndexed { i, _ -> i != index }
        when {
            wasCurrent -> {
                if (_queue.value.isEmpty()) {
                    stop()
                } else {
                    _queueIndex.value = index.coerceAtMost(_queue.value.lastIndex)
                    playCurrent()
                }
            }

            index < _queueIndex.value -> _queueIndex.value--
        }
    }

    override fun clearUpNext() {
        val index = _queueIndex.value
        if (index < 0 || index >= _queue.value.lastIndex) return
        _queue.value = _queue.value.take(index + 1)
    }

    override fun setLoopMode(mode: UiLoopMode) {
        _loopMode.value = mode
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        _shuffleEnabled.value = enabled
    }

    override fun setSpeed(value: Float) {
        _speed.value = value
    }

    override fun setPitch(value: Float) {
        _pitch.value = value
    }

    fun setVolume(value: Int) {
        player.setVolume(value)
    }

    fun close() {
        player.close()
    }

    private fun playCurrent() {
        val songId = _queue.value.getOrNull(_queueIndex.value) ?: return
        val song = library.songs.value.firstOrNull { it.id == songId }
        if (song == null) {
            _isPlaying.value = false
            return
        }
        player.play(Path.of(song.path))
        refresh()
    }

    private fun refresh() {
        val state = player.state
        _isPlaying.value = state is PlaybackState.Playing
        _positionMillis.value = player.positionMs()
        _durationMillis.value = player.durationMs()

        val endedNow = state is PlaybackState.Ended && previousState !is PlaybackState.Ended
        previousState = state
        if (endedNow) {
            handleEnded()
        }
    }

    private fun handleEnded() {
        when (_loopMode.value) {
            UiLoopMode.SONG -> playCurrent()
            UiLoopMode.QUEUE -> {
                _queueIndex.value = if (_queueIndex.value >= _queue.value.lastIndex) 0 else _queueIndex.value + 1
                playCurrent()
            }

            UiLoopMode.NONE -> skipToNext()
        }
    }
}
