package io.github.zyrouge.symphony.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.desktop.library.DesktopLibrary
import io.github.zyrouge.symphony.desktop.library.DesktopPlaylistStore
import io.github.zyrouge.symphony.desktop.ui.adapters.DesktopUiApp
import io.github.zyrouge.symphony.desktop.ui.DesktopSettingsStore
import io.github.zyrouge.symphony.desktop.ui.adapters.toUiAlbums
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.i18n.rememberStrings
import io.github.zyrouge.symphony.services.locales.localeNativeName
import io.github.zyrouge.symphony.services.locales.localeTag
import io.github.zyrouge.symphony.ui.components.appearanceLanguageOptions
import io.github.zyrouge.symphony.ui.components.BentoAppearanceSettingsSheet
import io.github.zyrouge.symphony.ui.components.HomeNavItem
import io.github.zyrouge.symphony.ui.components.HomePageHeader
import io.github.zyrouge.symphony.ui.components.HomeRailLabeled
import io.github.zyrouge.symphony.ui.components.MiniPlayerActions
import io.github.zyrouge.symphony.ui.components.BentoMiniPlayerCapsule
import io.github.zyrouge.symphony.ui.components.BentoPlayerQueue
import io.github.zyrouge.symphony.ui.components.BentoPlayerTitle
import io.github.zyrouge.symphony.ui.components.BentoPlayerTransport
import io.github.zyrouge.symphony.ui.components.BentoSeekRow
import io.github.zyrouge.symphony.ui.components.MiniPlayerLabels
import io.github.zyrouge.symphony.ui.components.HomeRailWide
import io.github.zyrouge.symphony.ui.components.HomeTab
import io.github.zyrouge.symphony.ui.components.HomeTabsRow
import io.github.zyrouge.symphony.ui.components.ForYouMix
import io.github.zyrouge.symphony.ui.components.ForYouNowPlayingLabels
import io.github.zyrouge.symphony.ui.components.ForYouNowPlayingTile
import io.github.zyrouge.symphony.ui.components.ForYouPage
import io.github.zyrouge.symphony.ui.components.ForYouPageData
import io.github.zyrouge.symphony.ui.components.ForYouUpNextEntry
import io.github.zyrouge.symphony.ui.model.UiPlaybackPosition
import io.github.zyrouge.symphony.ui.components.BentoGroupedTileColumns
import io.github.zyrouge.symphony.ui.components.BentoMediaCard
import io.github.zyrouge.symphony.ui.components.BentoMenu
import io.github.zyrouge.symphony.ui.components.BentoNewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.ForYouLibraryStatTile
import io.github.zyrouge.symphony.ui.components.SettingsRootPage
import io.github.zyrouge.symphony.ui.components.SettingsShortcut
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetCheckRow
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoMenuItem
import io.github.zyrouge.symphony.ui.components.BentoSongsPage
import io.github.zyrouge.symphony.ui.components.PlayerMode
import io.github.zyrouge.symphony.ui.components.QueueEntry
import io.github.zyrouge.symphony.ui.components.isShuffle
import io.github.zyrouge.symphony.ui.components.nextPlayerMode
import io.github.zyrouge.symphony.ui.components.resolvePlayerMode
import io.github.zyrouge.symphony.ui.components.toQueueLoopMode
import io.github.zyrouge.symphony.ui.components.SearchPage
import io.github.zyrouge.symphony.utils.DurationUtils
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.sortedBento
import io.github.zyrouge.symphony.ui.components.bentoAlbumMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoArtistNameMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoSongMatchesQuery
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.matches
import io.github.zyrouge.symphony.ui.model.BentoSearchFilter
import io.github.zyrouge.symphony.ui.model.SearchHit
import io.github.zyrouge.symphony.ui.model.UiMusicFolder
import io.github.zyrouge.symphony.ui.model.UiPlaylist
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.model.UiSongSort
import io.github.zyrouge.symphony.ui.view.BentoCollectionPage
import io.github.zyrouge.symphony.ui.view.player.BentoPlayerLevel
import io.github.zyrouge.symphony.ui.view.player.PhonePlayerSheet
import io.github.zyrouge.symphony.ui.view.player.collapseVertically
import io.github.zyrouge.symphony.ui.view.player.growingSquare
import io.github.zyrouge.symphony.ui.view.player.BentoPlayerStage
import io.github.zyrouge.symphony.ui.view.player.PlayerCloseButton
import io.github.zyrouge.symphony.ui.view.player.PlayerControlsContent
import io.github.zyrouge.symphony.ui.view.player.playerTransportLabels
import io.github.zyrouge.symphony.ui.view.player.playerTransportPlayModeLabel
import io.github.zyrouge.symphony.ui.view.player.PlayerHeaderInfo
import io.github.zyrouge.symphony.ui.view.player.PlayerStage
import io.github.zyrouge.symphony.ui.view.DetailSideTile
import io.github.zyrouge.symphony.ui.view.detail.DetailChip
import io.github.zyrouge.symphony.ui.view.detail.DetailChipsTile
import io.github.zyrouge.symphony.ui.view.detail.DetailImageCoverTile
import io.github.zyrouge.symphony.ui.view.detail.DetailMosaicCoverTile
import io.github.zyrouge.symphony.ui.view.detail.DetailPerson
import io.github.zyrouge.symphony.ui.view.detail.DetailPeopleTile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Shuffle
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoTheme
import io.github.zyrouge.symphony.ui.theme.LocalBentoAppDensity
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import java.awt.Window
import java.nio.file.Path
import java.util.Locale
import javax.swing.JFileChooser

private enum class DesktopTab {
    FOR_YOU,
    SONGS,
    ALBUMS,
    ARTISTS,
    GENRES,
    PLAYLISTS,
    FOLDERS,
}

private sealed interface DesktopDetail {
    data class Album(val name: String) : DesktopDetail
    data class Artist(val name: String) : DesktopDetail
    data class Genre(val name: String) : DesktopDetail
    data class Folder(val path: String, val name: String) : DesktopDetail
    data class Playlist(val id: String, val title: String) : DesktopDetail
}

@Composable
fun DesktopApp(
    window: Window,
    initialDirectory: Path? = null,
    onShortcutsAvailable: (((KeyEvent) -> Boolean)?) -> Unit = {},
) {
    val settingsStore = remember { DesktopSettingsStore() }
    val initialSettings = remember { settingsStore.load() }
    val systemLocale = remember { Locale.getDefault() }
    var language by remember { mutableStateOf(initialSettings.language) }
    val strings = rememberStrings(language ?: systemLocale.language)
    val scope = rememberCoroutineScope()
    val playlistStore = remember { DesktopPlaylistStore() }
    val uiApp = remember {
        DesktopUiApp(
            scope = scope,
            initialStrings = strings,
            initialPlaylists = playlistStore.load(),
            onPlaylistsChanged = playlistStore::save,
            initialSettings = initialSettings,
            onSettingsChanged = settingsStore::save,
        )
    }
    LaunchedEffect(strings) { uiApp.updateStrings(strings) }
    DisposableEffect(uiApp) {
        onDispose { uiApp.close() }
    }

    val songs by uiApp.library.songs.collectAsState()
    val albums by uiApp.library.albums.collectAsState()
    val artists by uiApp.library.artists.collectAsState()
    val genres by uiApp.library.genres.collectAsState()
    val folders by uiApp.library.musicFolders.collectAsState()
    val playlists by uiApp.library.playlists.collectAsState()
    val themeMode by uiApp.settings.themeMode.collectAsState()
    val colorScheme by uiApp.settings.colorScheme.collectAsState()
    val primaryHue by uiApp.settings.primaryHue.collectAsState()
    val fontScale by uiApp.settings.fontScale.collectAsState()
    val contentScale by uiApp.settings.contentScale.collectAsState()
    val queue by uiApp.playback.queue.collectAsState()
    val queueIndex by uiApp.playback.queueIndex.collectAsState()
    val isPlaying by uiApp.playback.isPlaying.collectAsState()
    val positionMs by uiApp.playback.positionMillis.collectAsState()
    val durationMs by uiApp.playback.durationMillis.collectAsState()
    val loopMode by uiApp.playback.loopMode.collectAsState()
    val shuffleEnabled by uiApp.playback.shuffleEnabled.collectAsState()

    val status = remember { mutableStateOf("Choose a folder to load your music") }
    var sortBy by remember { mutableStateOf(UiSongSort.TITLE) }
    var sortReverse by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<BentoSearchFilter?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var browseTab by remember { mutableStateOf(DesktopTab.SONGS) }
    var detail by remember { mutableStateOf<DesktopDetail?>(null) }
    var volume by remember { mutableStateOf(80) }
    var showNewPlaylist by remember { mutableStateOf(false) }
    var songPickerPlaylistId by remember { mutableStateOf<String?>(null) }
    var songPickerSelection by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSettings by remember { mutableStateOf(false) }
    var playerOpen by remember { mutableStateOf(false) }
    var playerStage by remember { mutableStateOf(BentoPlayerStage.COVER) }
    var playerLevel by remember { mutableStateOf(BentoPlayerLevel.SMALL) }
    var searchExpanded by remember { mutableStateOf(false) }
    var showSettingsPage by remember { mutableStateOf(false) }
    var forYouNowPlayingVisible by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val nowMs = remember { System.currentTimeMillis() }
    val currentSong = queue.getOrNull(queueIndex)?.let { id -> songs.firstOrNull { it.id == id } }
    val playingSongId = currentSong?.id
    val queueEntries = remember(queue, queueIndex, songs) {
        queue.mapIndexedNotNull { index, id ->
            if (index > queueIndex) {
                songs.firstOrNull { it.id == id }?.let { QueueEntry(index, it) }
            } else {
                null
            }
        }
    }

    LaunchedEffect(uiApp) {
        uiApp.playback.setVolume(volume)
        uiApp.playback.setShuffleEnabled(false)
    }

    fun load(directory: Path) {
        status.value = "Scanning $directory"
        uiApp.library.beginScan()
        Thread {
            val scan = DesktopLibrary.scan(directory)
            uiApp.library.updateTracks(scan.tracks)
            status.value = buildString {
                append(directory)
                append(" - ")
                append(scan.tracks.size)
                append(" tracks")
                if (scan.skippedCount > 0) {
                    append(", ")
                    append(scan.skippedCount)
                    append(" skipped")
                }
                scan.unavailableReason?.let {
                    append(" (")
                    append(it)
                    append(")")
                }
            }
        }.apply { isDaemon = true }.start()
    }

    fun chooseFolder() {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = strings.ChooseMusicFolder
        }
        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            detail = null
            load(chooser.selectedFile.toPath())
        }
    }

    LaunchedEffect(initialDirectory) {
        initialDirectory?.let { load(it) }
    }

    fun playQueue(entries: List<UiSong>, startIndex: Int = 0, shuffle: Boolean = false) {
        uiApp.playback.playQueue(entries.map { it.id }, startIndex = startIndex, shuffle = shuffle)
    }

    // Ctrl-based shortcuts so typing in the search field never loses a key.
    val shortcuts: (KeyEvent) -> Boolean = { event ->
        when {
            event.type != KeyEventType.KeyDown || !event.isCtrlPressed -> false
            event.key == Key.Spacebar -> {
                uiApp.playback.togglePlayPause()
                true
            }

            event.key == Key.DirectionLeft -> {
                uiApp.playback.seekTo((positionMs - 5_000L).coerceAtLeast(0L))
                true
            }

            event.key == Key.DirectionRight -> {
                uiApp.playback.seekTo(positionMs + 5_000L)
                true
            }

            event.key == Key.DirectionUp -> {
                volume = (volume + 5).coerceAtMost(100)
                uiApp.playback.setVolume(volume)
                true
            }

            event.key == Key.DirectionDown -> {
                volume = (volume - 5).coerceAtLeast(0)
                uiApp.playback.setVolume(volume)
                true
            }

            else -> false
        }
    }
    LaunchedEffect(shortcuts) { onShortcutsAvailable(shortcuts) }

    BentoTheme(
        mode = themeMode,
        scheme = colorScheme,
        hue = primaryHue,
    ) {
        val bentoColors = LocalBentoColors.current
        val baseDensity = LocalDensity.current
        val scaledDensity = remember(baseDensity, fontScale, contentScale) {
            Density(
                density = baseDensity.density * contentScale,
                fontScale = baseDensity.fontScale * fontScale,
            )
        }
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalBentoAppDensity provides scaledDensity,
        ) {
        // Material's own surfaces and controls follow the Bento palette, so dark
        // and contrast themes stay consistent with the shared text colors.
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                primary = bentoColors.accent,
                onPrimary = bentoColors.onAccent,
                secondary = bentoColors.tonal,
                onSecondary = bentoColors.onTonal,
                background = bentoColors.bg,
                onBackground = bentoColors.fg,
                surface = bentoColors.surface,
                onSurface = bentoColors.fg,
                surfaceVariant = bentoColors.raised,
                onSurfaceVariant = bentoColors.sub,
                outline = bentoColors.line,
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                val windowClass = rememberBentoWindowClass()
                val showRail = detail == null && windowClass != BentoWindowClass.COMPACT &&
                    windowClass != BentoWindowClass.SHORT_HEIGHT
                LaunchedEffect(searchExpanded) {
                    if (searchExpanded) searchFocusRequester.requestFocus()
                }
                val openPlayer: (BentoPlayerStage) -> Unit = { target ->
                    playerStage = target
                    playerLevel = if (
                        windowClass == BentoWindowClass.COMPACT ||
                        windowClass == BentoWindowClass.SHORT_HEIGHT
                    ) {
                        BentoPlayerLevel.SMALL
                    } else {
                        BentoPlayerLevel.MEDIUM
                    }
                    playerOpen = true
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (showRail) {
                        val railItems = DesktopTab.entries.map { tab ->
                            HomeNavItem(
                                key = tab.name,
                                label = tab.label(strings),
                                icon = tab.icon,
                            )
                        }
                        val onSelect: (String) -> Unit = { key ->
                            browseTab = DesktopTab.valueOf(key)
                            detail = null
                            searchExpanded = false
                        }
                        val onSearch = { searchExpanded = true }
                        when (windowClass) {
                            BentoWindowClass.EXTRA_LARGE -> HomeRailWide(
                                items = railItems,
                                selectedKey = browseTab.name,
                                onSelect = onSelect,
                                onSearch = onSearch,
                                onSettings = { showSettingsPage = true },
                                searchLabel = strings.SearchYourMusic,
                                settingsLabel = strings.Settings,
                                appName = strings.SymphonyDesktop,
                            )

                            else -> HomeRailLabeled(
                                items = railItems,
                                selectedKey = browseTab.name,
                                onSelect = onSelect,
                                onSearch = onSearch,
                                onSettings = { showSettingsPage = true },
                                searchLabel = strings.SearchYourMusic,
                                settingsLabel = strings.Settings,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                    if (detail == null && !searchExpanded) {
                        HomePageHeader(
                            title = browseTab.label(strings),
                            windowClass = windowClass,
                            showActions = !showRail,
                            onSearch = { searchExpanded = true },
                            onSettings = { showSettingsPage = true },
                            searchLabel = strings.SearchYourMusic,
                            settingsLabel = strings.Settings,
                            trailing = {
                                IconButton(onClick = { openPlayer(BentoPlayerStage.QUEUE) }) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.QueueMusic,
                                        strings.Queue,
                                    )
                                }
                                IconButton(onClick = { chooseFolder() }) {
                                    Icon(
                                        Icons.Rounded.Folder,
                                        strings.OpenFolder,
                                    )
                                }
                            },
                        )
                        if (!showRail) {
                            HomeTabsRow(
                                items = DesktopTab.entries.map { tab ->
                                    HomeTab(key = tab.name, label = tab.label(strings))
                                },
                                selectedKey = browseTab.name,
                                onSelect = { key ->
                                    browseTab = DesktopTab.valueOf(key)
                                    detail = null
                                },
                            )
                        }
                    } else {
                        Text(
                            text = status.value,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when {
                            detail != null -> DesktopCollectionDetail(
                                detail = detail!!,
                                allSongs = songs,
                                playlists = playlists,
                                strings = strings,
                                playingSongId = playingSongId,
                                isPlaying = isPlaying,
                                onBack = { detail = null },
                                onPlayAll = { entries -> playQueue(entries) },
                                onShuffleAll = { entries -> playQueue(entries, shuffle = true) },
                                onQueue = { entries -> uiApp.playback.enqueue(entries.map { it.id }) },
                                onSongClick = { song, entries ->
                                    playQueue(
                                        entries,
                                        startIndex = entries.indexOfFirst { it.id == song.id }
                                            .coerceAtLeast(0),
                                    )
                                },
                                onArtistClick = { name -> detail = DesktopDetail.Artist(name) },
                                onGenreClick = { name -> detail = DesktopDetail.Genre(name) },
                                onEditSongs = { playlist ->
                                    songPickerPlaylistId = playlist.id
                                    songPickerSelection = playlist.trackIds.toSet()
                                },
                                onDeletePlaylist = { playlistId ->
                                    uiApp.library.deletePlaylist(playlistId)
                                    detail = null
                                },
                                onRemoveSong = { playlist, song ->
                                    uiApp.library.updatePlaylistSongs(
                                        playlist.id,
                                        playlist.trackIds.filterNot { it == song.id },
                                    )
                                },
                            )

                            searchExpanded -> DesktopSearchResults(
                                strings = strings,
                                terms = terms,
                                onTermsChange = { terms = it },
                                focusRequester = searchFocusRequester,
                                sortBy = sortBy,
                                sortReverse = sortReverse,
                                onSortByChange = { sortBy = it },
                                onSortReverseChange = { sortReverse = it },
                                selectedFilter = selectedFilter,
                                onFilterChange = { selectedFilter = it },
                                selectedGenre = selectedGenre,
                                onGenreChange = { selectedGenre = it },
                                allSongs = songs,
                                nowMs = nowMs,
                                playingSongId = playingSongId,
                                isPlaying = isPlaying,
                                onBack = { searchExpanded = false },
                                onPlayAll = { entries -> playQueue(entries) },
                                onShuffleAll = { entries -> playQueue(entries, shuffle = true) },
                                onSongClick = { song, entries ->
                                    playQueue(
                                        entries,
                                        startIndex = entries.indexOfFirst { it.id == song.id }
                                            .coerceAtLeast(0),
                                    )
                                },
                                onHitClick = { hit ->
                                    terms = ""
                                    searchExpanded = false
                                    detail = when {
                                        hit.key.startsWith("artist:") ->
                                            DesktopDetail.Artist(hit.title)

                                        else -> DesktopDetail.Album(hit.title)
                                    }
                                },
                            )

                            else -> when (browseTab) {
                                DesktopTab.FOR_YOU -> ForYouPage(
                                    data = ForYouPageData(
                                        songCount = songs.size,
                                        albumCount = albums.size,
                                        genreNames = genres.map { it.name },
                                        playlistCount = playlists.size,
                                        folderCount = folders.size,
                                        totalDurationMs = songs.sumOf { it.durationMillis },
                                        recentlyAddedAlbums = songs
                                            .sortedByDescending { it.dateModifiedMillis }
                                            .mapNotNull { song ->
                                                song.album?.let { name ->
                                                    albums.firstOrNull { it.name == name }
                                                }
                                            }
                                            .distinctBy { it.id }
                                            .take(9),
                                        suggestedAlbums = albums.take(6),
                                        suggestedArtists = artists.map { it.name }.take(6),
                                        upNext = queue.drop(queueIndex + 1)
                                            .mapIndexedNotNull { index, id ->
                                                songs.firstOrNull { it.id == id }?.let { song ->
                                                    ForYouUpNextEntry(
                                                        position = queueIndex + 1 + index,
                                                        song = song,
                                                        durationText = DurationUtils.formatMs(
                                                            song.durationMillis,
                                                        ),
                                                    )
                                                }
                                            },
                                        mix = playlists.firstOrNull()?.let { playlist ->
                                            ForYouMix(
                                                title = playlist.title,
                                                sub = strings.Playlist + " · " + strings.XSongs(
                                                    playlist.trackCount.toString(),
                                                ),
                                            )
                                        },
                                        songsEnabled = true,
                                    ),
                                    strings = strings,
                                    appFontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                                    nowPlaying = { geometry ->
                                        ForYouNowPlayingTile(
                                            geometry = geometry,
                                            song = currentSong,
                                            isPlaying = isPlaying,
                                            position = UiPlaybackPosition(positionMs, durationMs),
                                            seekable = true,
                                            labels = ForYouNowPlayingLabels(
                                                nowPlaying = strings.NowPlaying,
                                                nothingPlaying =
                                                    strings.NothingIsBeingPlayedRightNow,
                                                previous = strings.Previous,
                                                play = strings.Play,
                                                pause = strings.Pause,
                                                next = strings.Next,
                                            ),
                                            artwork = { modifier ->
                                                currentSong?.let {
                                                    DesktopArtwork(Path.of(it.path), modifier)
                                                }
                                            },
                                            timeText = { DurationUtils.formatMs(it) },
                                            onOpenPlayer = { openPlayer(BentoPlayerStage.COVER) },
                                            onSeek = { uiApp.playback.seekTo(it) },
                                            onPrevious = { uiApp.playback.skipToPrevious() },
                                            onPlayPause = { uiApp.playback.togglePlayPause() },
                                            onNext = { uiApp.playback.skipToNext() },
                                            onVisibilityChanged = {
                                                forYouNowPlayingVisible = it
                                            },
                                        )
                                    },
                                    artworkAlbum = { album, modifier ->
                                        val path = songs
                                            .firstOrNull {
                                                it.album != null && "album:${it.album}" == album.id
                                            }
                                            ?.let { Path.of(it.path) }
                                        if (path != null) DesktopArtwork(path, modifier)
                                    },
                                    artworkArtist = { name, modifier ->
                                        val path = songs.firstOrNull { name in it.artists }
                                            ?.let { Path.of(it.path) }
                                        if (path != null) DesktopArtwork(path, modifier)
                                    },
                                    artworkSong = { song, modifier ->
                                        DesktopArtwork(Path.of(song.path), modifier)
                                    },
                                    onShuffle = { playQueue(songs, shuffle = true) },
                                    onRescan = { chooseFolder() },
                                    onOpenGenres = {
                                        browseTab = DesktopTab.GENRES
                                        detail = null
                                    },
                                    onOpenPlaylists = {
                                        browseTab = DesktopTab.PLAYLISTS
                                        detail = null
                                    },
                                    onOpenFolders = {
                                        browseTab = DesktopTab.FOLDERS
                                        detail = null
                                    },
                                    onOpenAlbums = {
                                        browseTab = DesktopTab.ALBUMS
                                        detail = null
                                    },
                                    onOpenArtists = {
                                        browseTab = DesktopTab.ARTISTS
                                        detail = null
                                    },
                                    onOpenSongs = {
                                        browseTab = DesktopTab.SONGS
                                        detail = null
                                    },
                                    onOpenSettings = { showSettings = true },
                                    onOpenAlbum = { album ->
                                        detail = DesktopDetail.Album(album.name)
                                    },
                                    onOpenArtist = { name ->
                                        detail = DesktopDetail.Artist(name)
                                    },
                                    onOpenMix = {
                                        playlists.firstOrNull()?.let {
                                            detail = DesktopDetail.Playlist(it.id, it.title)
                                        }
                                    },
                                    onJumpQueue = { position ->
                                        uiApp.playback.playAt(position)
                                    },
                                )

                                DesktopTab.SONGS -> BentoSongsPage(
                                    strings = strings,
                                    sort = sortBy,
                                    sortReverse = sortReverse,
                                    songCount = songs.size,
                                    totalDurationText = formatBentoDurationMinutes(
                                        songs.sumOf { it.durationMillis },
                                        strings,
                                    ),
                                    onSortChange = { sortBy = it },
                                    onReverseChange = { sortReverse = it },
                                    onPlayAll = {
                                        playQueue(songs.sortedBento(sortBy, sortReverse))
                                    },
                                    onShuffle = {
                                        playQueue(
                                            songs.sortedBento(sortBy, sortReverse),
                                            shuffle = true,
                                        )
                                    },
                                ) { listModifier ->
                                    Box(modifier = listModifier) {
                                        DesktopSongsList(
                                            songs = songs,
                                            sortBy = sortBy,
                                            sortReverse = sortReverse,
                                            playingSongId = playingSongId,
                                            isPlaying = isPlaying,
                                            strings = strings,
                                            onSongClick = { song, entries ->
                                                playQueue(
                                                    entries,
                                                    startIndex = entries
                                                        .indexOfFirst { it.id == song.id }
                                                        .coerceAtLeast(0),
                                                )
                                            },
                                        )
                                    }
                                }

                                DesktopTab.ALBUMS -> DesktopBrowseGrid(
                                    albums = albums,
                                    songs = songs,
                                    strings = strings,
                                    onOpen = { detail = DesktopDetail.Album(it) },
                                )

                                DesktopTab.ARTISTS -> DesktopArtistsGrid(
                                    artists = artists,
                                    songs = songs,
                                    strings = strings,
                                    onOpen = { detail = DesktopDetail.Artist(it) },
                                )

                                DesktopTab.GENRES -> DesktopGenresGrid(
                                    genres = genres,
                                    songs = songs,
                                    strings = strings,
                                    onOpen = { detail = DesktopDetail.Genre(it) },
                                )

                                DesktopTab.PLAYLISTS -> DesktopPlaylistsGrid(
                                    playlists = playlists,
                                    songs = songs,
                                    strings = strings,
                                    onCreate = { showNewPlaylist = true },
                                    onOpen = {
                                        detail = DesktopDetail.Playlist(it.id, it.title)
                                    },
                                )

                                DesktopTab.FOLDERS -> DesktopFoldersGrid(
                                    folders = folders,
                                    songs = songs,
                                    strings = strings,
                                    onOpen = {
                                        detail = DesktopDetail.Folder(it.path.orEmpty(), it.name)
                                    },
                                )
                            }
                        }
                    }
                    val capsuleHidden = browseTab == DesktopTab.FOR_YOU &&
                        forYouNowPlayingVisible
                    if (currentSong != null && !capsuleHidden) {
                        NowPlayingBar(
                        strings = strings,
                        song = currentSong,
                        isPlaying = isPlaying,
                        mode = resolvePlayerMode(loopMode, shuffleEnabled),
                        positionMs = positionMs,
                        durationMs = durationMs,
                        volume = volume,
                        artwork = { song, modifier ->
                            DesktopArtwork(Path.of(song.path), modifier)
                        },
                        onOpen = { openPlayer(BentoPlayerStage.COVER) },
                        onTogglePlay = { uiApp.playback.togglePlayPause() },
                        onCycleMode = {
                            val next = nextPlayerMode(resolvePlayerMode(loopMode, shuffleEnabled))
                            uiApp.playback.setShuffleEnabled(next.isShuffle())
                            uiApp.playback.setLoopMode(next.toQueueLoopMode())
                        },
                        onPrevious = { uiApp.playback.skipToPrevious() },
                        onNext = { uiApp.playback.skipToNext() },
                        onVolume = {
                            volume = it
                            uiApp.playback.setVolume(it)
                        },
                        )
                    }
                    }
                }
            }
            if (playerOpen && currentSong != null) {
                DesktopPlayerHost(
                    strings = strings,
                    song = currentSong,
                    isPlaying = isPlaying,
                    mode = resolvePlayerMode(loopMode, shuffleEnabled),
                    positionMs = positionMs,
                    durationMs = durationMs,
                    volume = volume,
                    level = playerLevel,
                    stage = playerStage,
                    queueEntries = queueEntries,
                    onLevelChange = { playerLevel = it },
                    onStageChange = { playerStage = it },
                    onClose = { playerOpen = false },
                    onCycleMode = {
                        val next = nextPlayerMode(resolvePlayerMode(loopMode, shuffleEnabled))
                        uiApp.playback.setShuffleEnabled(next.isShuffle())
                        uiApp.playback.setLoopMode(next.toQueueLoopMode())
                    },
                    onPrevious = { uiApp.playback.skipToPrevious() },
                    onTogglePlay = { uiApp.playback.togglePlayPause() },
                    onNext = { uiApp.playback.skipToNext() },
                    onSeek = { uiApp.playback.seekTo(it) },
                    onVolume = {
                        volume = it
                        uiApp.playback.setVolume(it)
                    },
                    onQueueEntryClick = { entry -> uiApp.playback.playAt(entry.position) },
                    onQueueEntryRemove = { entry ->
                        uiApp.playback.removeQueueItem(entry.position)
                    },
                    onClearUpNext = { uiApp.playback.clearUpNext() },
                )
            }
            }
            if (showSettingsPage) {
                DesktopSettingsPage(
                    strings = strings,
                    themeMode = themeMode,
                    songCount = songs.size,
                    albumCount = albums.size,
                    totalDurationMs = songs.sumOf { it.durationMillis },
                    onBack = { showSettingsPage = false },
                    onOpenAppearance = { showSettings = true },
                    onOpenFolders = { chooseFolder() },
                )
            }
            DesktopPlaylistDialogs(
                strings = strings,
                songs = songs,
                showNewPlaylist = showNewPlaylist,
                onCreatePlaylist = { title ->
                    uiApp.library.createPlaylist(title, emptyList())
                    showNewPlaylist = false
                },
                onDismissNewPlaylist = { showNewPlaylist = false },
                playlist = songPickerPlaylistId?.let { id ->
                    playlists.firstOrNull { it.id == id }
                },
                selection = songPickerSelection,
                onSelectionChange = { songPickerSelection = it },
                onApplySelection = { id, ids ->
                    uiApp.library.updatePlaylistSongs(id, ids)
                    songPickerPlaylistId = null
                },
            )
            if (showSettings) {
                DesktopSettingsDialog(
                    strings = strings,
                    themeMode = themeMode,
                    colorScheme = colorScheme,
                    primaryHue = primaryHue,
                    onThemeMode = { uiApp.settings.setThemeMode(it) },
                    onColorScheme = { uiApp.settings.setColorScheme(it) },
                    onPrimaryHue = { uiApp.settings.setPrimaryHue(it) },
                    fontScale = fontScale,
                    contentScale = contentScale,
                    onFontScale = { uiApp.settings.setFontScale(it) },
                    onContentScale = { uiApp.settings.setContentScale(it) },
                    language = language,
                    onLanguage = { code ->
                        language = code
                        uiApp.settings.setLanguage(code)
                        Locale.setDefault(
                            when {
                                code == null -> systemLocale
                                else -> Locale.forLanguageTag(localeTag(code))
                            },
                        )
                    },
                    onDismiss = { showSettings = false },
                )
            }
        }
    }
}
}

@Composable
private fun DesktopSettingsDialog(
    strings: Strings,
    themeMode: BentoThemeMode,
    colorScheme: BentoColorScheme,
    primaryHue: BentoPrimaryHue,
    onThemeMode: (BentoThemeMode) -> Unit,
    onColorScheme: (BentoColorScheme) -> Unit,
    onPrimaryHue: (BentoPrimaryHue) -> Unit,
    fontScale: Float,
    contentScale: Float,
    onFontScale: (Float) -> Unit,
    onContentScale: (Float) -> Unit,
    language: String?,
    onLanguage: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val languages = appearanceLanguageOptions(
        systemLanguageName = strings.SystemX(
            localeNativeName(Locale.getDefault().language),
        ),
    ) { code ->
        Locale.forLanguageTag(localeTag(code)).getDisplayName(Locale.ENGLISH)
    }
    CompositionLocalProvider(LocalStrings provides strings) {
        BentoAppearanceSettingsSheet(
            strings = strings,
            onDismissRequest = onDismiss,
            languages = languages,
            selectedLanguage = language,
            onLanguageChange = onLanguage,
            fonts = emptyList(),
            selectedFont = null,
            onFontChange = {},
            fontScale = fontScale,
            onFontScaleChange = onFontScale,
            contentScale = contentScale,
            onContentScaleChange = onContentScale,
            themeMode = themeMode,
            onThemeModeChange = onThemeMode,
            colorScheme = colorScheme,
            onColorSchemeChange = onColorScheme,
            primaryHue = primaryHue,
            onPrimaryHueChange = onPrimaryHue,
        )
    }
}

private fun desktopSettingsPlacements(gridClass: BentoGridClass) = when (gridClass) {
    BentoGridClass.C4 -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 2),
        BentoTilePlacement("lib", 1, 3, 4, 1),
        BentoTilePlacement("appearance", 1, 4, 2, 1),
        BentoTilePlacement("folders", 3, 4, 2, 1),
        BentoTilePlacement("about", 1, 5, 4, 1),
    )

    BentoGridClass.C6 -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 2),
        BentoTilePlacement("lib", 5, 1, 2, 2),
        BentoTilePlacement("appearance", 1, 3, 2, 1),
        BentoTilePlacement("folders", 3, 3, 2, 1),
        BentoTilePlacement("about", 5, 3, 2, 1),
    )

    BentoGridClass.C8, BentoGridClass.SHORT -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 2),
        BentoTilePlacement("lib", 5, 1, 4, 2),
        BentoTilePlacement("appearance", 1, 3, 2, 1),
        BentoTilePlacement("folders", 3, 3, 2, 1),
        BentoTilePlacement("about", 5, 3, 4, 1),
    )

    BentoGridClass.C12 -> listOf(
        BentoTilePlacement("theme", 1, 1, 5, 2),
        BentoTilePlacement("lib", 6, 1, 7, 2),
        BentoTilePlacement("appearance", 1, 3, 3, 1),
        BentoTilePlacement("folders", 4, 3, 3, 1),
        BentoTilePlacement("about", 7, 3, 6, 1),
    )
}

private fun themeModeLabel(strings: Strings, mode: BentoThemeMode) = when (mode) {
    BentoThemeMode.SYSTEM -> strings.System
    BentoThemeMode.LIGHT -> strings.Light
    BentoThemeMode.DARK -> strings.Dark
    BentoThemeMode.BLACK -> strings.Black
}

@Composable
private fun DesktopSettingsPage(
    strings: Strings,
    themeMode: BentoThemeMode,
    songCount: Int,
    albumCount: Int,
    totalDurationMs: Long,
    onBack: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenFolders: () -> Unit,
) {
    val colors = LocalBentoColors.current
    CompositionLocalProvider(LocalStrings provides strings) {
        SettingsRootPage(
            windowClass = rememberBentoWindowClass(),
            strings = strings,
            shortcuts = listOf(
                SettingsShortcut(
                    key = "appearance",
                    icon = Icons.Rounded.Palette,
                    label = strings.Appearance,
                    summary = strings.AppearanceSettingsSummary,
                    onClick = onOpenAppearance,
                ),
                SettingsShortcut(
                    key = "folders",
                    icon = Icons.Rounded.Folder,
                    label = strings.LibraryFolders,
                    summary = strings.OpenFolder,
                    onClick = onOpenFolders,
                ),
            ),
            onBack = onBack,
            themeTile = { geometry ->
                BentoTile(
                    tone = BentoTileTone.PLAIN,
                    sizeClass = geometry.sizeClass,
                    onClick = onOpenAppearance,
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            strings.Theme,
                            style = BentoTypography.secondary,
                            color = colors.sub,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            themeModeLabel(strings, themeMode),
                            style = BentoTypography.title,
                            color = colors.fg,
                        )
                        Text(
                            strings.Appearance,
                            style = BentoTypography.secondary,
                            color = colors.muted,
                        )
                    }
                }
            },
            libraryTile = { geometry ->
                ForYouLibraryStatTile(
                    geometry = geometry,
                    appFontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    totalDurationMs = totalDurationMs,
                    songCount = songCount,
                    albumCount = albumCount,
                    strings = strings,
                    onRescan = onOpenFolders,
                )
            },
            aboutTile = { geometry ->
                BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            strings.SymphonyDesktop,
                            style = BentoTypography.title,
                            color = colors.fg,
                        )
                        Text(
                            "Compose Desktop · VLCJ",
                            style = BentoTypography.secondary,
                            color = colors.sub,
                        )
                    }
                }
            },
            contributing = {},
            links = {},
            placements = ::desktopSettingsPlacements,
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg),
        )
    }
}

private val DesktopTab.icon: ImageVector
    get() = when (this) {
        DesktopTab.FOR_YOU -> Icons.Rounded.AutoAwesome
        DesktopTab.SONGS -> Icons.Rounded.MusicNote
        DesktopTab.ALBUMS -> Icons.Rounded.Album
        DesktopTab.ARTISTS -> Icons.Rounded.Person
        DesktopTab.GENRES -> Icons.Rounded.Sell
        DesktopTab.PLAYLISTS -> Icons.AutoMirrored.Rounded.QueueMusic
        DesktopTab.FOLDERS -> Icons.Rounded.Folder
    }

private fun DesktopTab.label(strings: Strings) = when (this) {
    DesktopTab.FOR_YOU -> strings.ForYou
    DesktopTab.SONGS -> strings.Songs
    DesktopTab.ALBUMS -> strings.Albums
    DesktopTab.ARTISTS -> strings.Artists
    DesktopTab.GENRES -> strings.Genres
    DesktopTab.PLAYLISTS -> strings.Playlists
    DesktopTab.FOLDERS -> strings.Folders
}

@Composable
private fun DesktopSongsList(
    songs: List<UiSong>,
    sortBy: UiSongSort,
    sortReverse: Boolean,
    playingSongId: String?,
    isPlaying: Boolean,
    strings: Strings,
    onSongClick: (UiSong, List<UiSong>) -> Unit,
) {
    val sorted = remember(songs, sortBy, sortReverse) { songs.sortedBento(sortBy, sortReverse) }
    val lettered = sortBy == UiSongSort.TITLE ||
        sortBy == UiSongSort.ARTIST ||
        sortBy == UiSongSort.ALBUM
    SongList(
        songs = sorted,
        playingSongId = playingSongId,
        isPlaying = isPlaying,
        durationText = { DurationUtils.formatMs(it.durationMillis) },
        moreContentDescription = strings.More,
        artwork = { song, modifier -> DesktopArtwork(Path.of(song.path), modifier) },
        lettered = lettered,
        showScrubber = lettered,
        sortKeyOf = { song ->
            when (sortBy) {
                UiSongSort.ARTIST -> song.artists.joinToString()
                UiSongSort.ALBUM -> song.album ?: ""
                else -> song.title
            }
        },
        onSongClick = { song -> onSongClick(song, sorted) },
    )
}

@Composable
private fun DesktopBrowseGrid(
    albums: List<io.github.zyrouge.symphony.ui.model.UiAlbum>,
    songs: List<UiSong>,
    strings: Strings,
    onOpen: (String) -> Unit,
) {
    val sorted = remember(albums) { albums.sortedBy { it.name.lowercase() } }
    BentoGroupedTileColumns(
        entries = sorted,
        entryHeightDp = { 260f },
        modifier = Modifier.fillMaxSize(),
    ) { album, _ ->
        DesktopBrowseCard(
            title = album.name,
            subtitle = listOf(
                strings.XSongs(album.trackCount.toString()),
                formatBentoDurationMinutes(album.durationMillis, strings),
            ).joinToString(" · "),
            round = false,
            artPath = remember(album.name, songs) {
                songs.firstOrNull { it.album == album.name }?.path?.let(Path::of)
            },
            onClick = { onOpen(album.name) },
        )
    }
}

@Composable
private fun DesktopArtistsGrid(
    artists: List<io.github.zyrouge.symphony.ui.model.UiArtist>,
    songs: List<UiSong>,
    strings: Strings,
    onOpen: (String) -> Unit,
) {
    val sorted = remember(artists) { artists.sortedBy { it.name.lowercase() } }
    BentoGroupedTileColumns(
        entries = sorted,
        entryHeightDp = { 260f },
        modifier = Modifier.fillMaxSize(),
    ) { artist, _ ->
        DesktopBrowseCard(
            title = artist.name,
            subtitle = listOf(
                strings.XAlbums(artist.albumCount.toString()),
                strings.XSongs(artist.trackCount.toString()),
            ).joinToString(" · "),
            round = true,
            artPath = remember(artist.name, songs) {
                songs.firstOrNull { it.artists.contains(artist.name) }?.path?.let(Path::of)
            },
            onClick = { onOpen(artist.name) },
        )
    }
}

@Composable
private fun DesktopGenresGrid(
    genres: List<io.github.zyrouge.symphony.ui.model.UiGenre>,
    songs: List<UiSong>,
    strings: Strings,
    onOpen: (String) -> Unit,
) {
    val sorted = remember(genres) { genres.sortedBy { it.name.lowercase() } }
    BentoGroupedTileColumns(
        entries = sorted,
        entryHeightDp = { 260f },
        modifier = Modifier.fillMaxSize(),
    ) { genre, _ ->
        DesktopBrowseCard(
            title = genre.name,
            subtitle = strings.XSongs(genre.trackCount.toString()),
            round = false,
            artPath = remember(genre.name, songs) {
                songs.firstOrNull { it.genres.contains(genre.name) }?.path?.let(Path::of)
            },
            onClick = { onOpen(genre.name) },
        )
    }
}

@Composable
private fun DesktopPlaylistsGrid(
    playlists: List<UiPlaylist>,
    songs: List<UiSong>,
    strings: Strings,
    onCreate: () -> Unit,
    onOpen: (UiPlaylist) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            BentoSheetButton(label = strings.NewPlaylist, onClick = onCreate)
        }
        val sorted = remember(playlists) { playlists.sortedBy { it.title.lowercase() } }
        BentoGroupedTileColumns(
            entries = sorted,
            entryHeightDp = { 180f },
            modifier = Modifier.fillMaxSize(),
        ) { playlist, _ ->
            val firstTrack = playlist.trackIds.firstOrNull()
            DesktopBrowseCard(
                title = playlist.title,
                subtitle = strings.XSongs(playlist.trackCount.toString()),
                round = false,
                height = 180.dp,
                artPath = remember(firstTrack, songs) {
                    songs.firstOrNull { it.id == firstTrack }?.path?.let(Path::of)
                },
                onClick = { onOpen(playlist) },
            )
        }
    }
}

@Composable
private fun DesktopPlaylistDialogs(
    strings: Strings,
    songs: List<UiSong>,
    showNewPlaylist: Boolean,
    onCreatePlaylist: (String) -> Unit,
    onDismissNewPlaylist: () -> Unit,
    playlist: UiPlaylist?,
    selection: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onApplySelection: (String, List<String>) -> Unit,
) {
    CompositionLocalProvider(LocalStrings provides strings) {
        if (showNewPlaylist) {
            BentoNewPlaylistDialog(
                strings = strings,
                onCreate = onCreatePlaylist,
                onDismissRequest = onDismissNewPlaylist,
            )
        }
        if (playlist != null) {
            BentoSheetDialog(
                title = strings.AddSongs,
                onDismissRequest = {
                    onApplySelection(playlist.id, selection.toList())
                },
            ) {
                LazyColumn(modifier = Modifier.height(420.dp)) {
                    items(songs, key = { it.id }) { song ->
                        BentoSheetCheckRow(
                            title = song.title,
                            subtitle = song.artists.joinToString(),
                            checked = song.id in selection,
                            onCheckedChange = { checked ->
                                onSelectionChange(
                                    when {
                                        checked -> selection + song.id
                                        else -> selection - song.id
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopFoldersGrid(
    folders: List<UiMusicFolder>,
    songs: List<UiSong>,
    strings: Strings,
    onOpen: (UiMusicFolder) -> Unit,
) {
    val sorted = remember(folders) { folders.sortedBy { it.name.lowercase() } }
    BentoGroupedTileColumns(
        entries = sorted,
        entryHeightDp = { 260f },
        modifier = Modifier.fillMaxSize(),
    ) { folder, _ ->
        val prefix = folder.path.orEmpty().trimEnd('/') + "/"
        DesktopBrowseCard(
            title = folder.name,
            subtitle = strings.XSongs(
                songs.count { it.path.startsWith(prefix) }.toString(),
            ),
            round = false,
            artPath = remember(prefix, songs) {
                songs.firstOrNull { it.path.startsWith(prefix) }?.path?.let(Path::of)
            },
            onClick = { onOpen(folder) },
        )
    }
}

@Composable
private fun DesktopBrowseCard(
    title: String,
    subtitle: String,
    round: Boolean,
    artPath: Path?,
    onClick: () -> Unit,
    height: Dp = 240.dp,
) {
    val colors = LocalBentoColors.current
    BentoMediaCard(
        title = title,
        subtitle = subtitle,
        artwork = { modifier ->
            if (artPath != null) {
                DesktopArtwork(artPath, modifier)
            } else {
                Box(modifier.background(colors.raised))
            }
        },
        onClick = onClick,
        round = round,
        height = height,
    )
}

@Composable
private fun DesktopCollectionDetail(
    detail: DesktopDetail,
    allSongs: List<UiSong>,
    playlists: List<UiPlaylist>,
    strings: Strings,
    playingSongId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: (List<UiSong>) -> Unit,
    onShuffleAll: (List<UiSong>) -> Unit,
    onQueue: (List<UiSong>) -> Unit,
    onSongClick: (UiSong, List<UiSong>) -> Unit,
    onArtistClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onEditSongs: (UiPlaylist) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onRemoveSong: (UiPlaylist, UiSong) -> Unit,
) {
    val playlist = (detail as? DesktopDetail.Playlist)?.let { value ->
        playlists.firstOrNull { it.id == value.id }
    }
    val songs = remember(detail, allSongs, playlists) {
        detailSongsOf(detail, allSongs, playlists)
    }
    val coverPath = remember(detail, songs) {
        songs.firstOrNull()?.path?.let(Path::of)
    }
    val title = when (detail) {
        is DesktopDetail.Album -> detail.name
        is DesktopDetail.Artist -> detail.name
        is DesktopDetail.Genre -> detail.name
        is DesktopDetail.Folder -> detail.name
        is DesktopDetail.Playlist -> detail.title
    }
    val label = when (detail) {
        is DesktopDetail.Album -> strings.Album
        is DesktopDetail.Artist -> strings.Artist
        is DesktopDetail.Genre -> strings.Genre
        is DesktopDetail.Folder -> strings.Folder
        is DesktopDetail.Playlist -> strings.Playlist
    }
    val genreNames = remember(songs) {
        songs.flatMap { it.genres }.distinct().sortedBy { it.lowercase() }
    }
    val artistNames = remember(songs) {
        songs.flatMap { it.artists }.distinct().sortedBy { it.lowercase() }
    }
    val sideTiles = buildList {
        when (detail) {
            is DesktopDetail.Album -> Unit

            is DesktopDetail.Folder -> Unit

            is DesktopDetail.Playlist -> Unit

            is DesktopDetail.Artist -> {
                if (genreNames.isNotEmpty()) {
                    add(
                        DetailSideTile(id = "genres", rowSpan = { 1 }) { geometry ->
                            DetailChipsTile(
                                label = strings.Genres,
                                chips = genreNames.map { genre ->
                                    DetailChip(genre) { onGenreClick(genre) }
                                },
                                geometry = geometry,
                            )
                        },
                    )
                }
            }

            is DesktopDetail.Genre -> {
                if (artistNames.isNotEmpty()) {
                    add(
                        DetailSideTile(id = "artists", leading = true, rowSpan = { 1 }) { geometry ->
                            DetailPeopleTile(
                                label = strings.Artists,
                                people = artistNames.map { name ->
                                    DetailPerson(
                                        name = name,
                                        artwork = { modifier ->
                                            val path = allSongs
                                                .firstOrNull { it.artists.contains(name) }
                                                ?.path
                                                ?.let(Path::of)
                                            if (path != null) {
                                                DesktopArtwork(path, modifier)
                                            } else {
                                                Box(
                                                    modifier.background(
                                                        MaterialTheme.colorScheme
                                                            .secondaryContainer,
                                                    ),
                                                )
                                            }
                                        },
                                        onClick = { onArtistClick(name) },
                                    )
                                },
                                geometry = geometry,
                            )
                        },
                    )
                }
            }
        }
    }

    BentoCollectionPage(
        label = label,
        title = title,
        songs = songs,
        t = strings,
        onBack = onBack,
        emptyMessage = strings.DamnThisIsSoEmpty,
        cover = { geometry ->
            when {
                detail is DesktopDetail.Playlist && songs.isNotEmpty() -> {
                    val artworks = List<@Composable (Modifier) -> Unit>(4) { index ->
                        val song = songs.getOrNull(index)
                        val slot: @Composable (Modifier) -> Unit = { modifier ->
                            if (song != null) {
                                DesktopArtwork(Path.of(song.path), modifier)
                            } else {
                                Box(
                                    modifier.background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                )
                            }
                        }
                        slot
                    }
                    DetailMosaicCoverTile(geometry, artworks)
                }

                else -> DetailImageCoverTile(geometry) { modifier ->
                    if (coverPath != null) {
                        DesktopArtwork(coverPath, modifier)
                    } else {
                        Box(
                            modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                        )
                    }
                }
            }
        },
        playingSongId = playingSongId,
        isPlaying = isPlaying,
        onPlayAll = { onPlayAll(songs) },
        onShuffle = { onShuffleAll(songs) },
        onQueue = { onQueue(songs) },
        onSongClick = { song -> onSongClick(song, songs) },
        rowDurationText = { DurationUtils.formatMs(it.durationMillis) },
        rowArtwork = { song, modifier -> DesktopArtwork(Path.of(song.path), modifier) },
        rowMenu = playlist?.let { value ->
            { song, expanded, dismiss ->
                BentoMenu(expanded = expanded, onDismissRequest = dismiss) {
                    BentoMenuItem(
                        label = strings.RemoveFromPlaylist,
                        icon = null,
                        onClick = {
                            onRemoveSong(value, song)
                            dismiss()
                        },
                    )
                }
            }
        },
        sideTiles = sideTiles,
        onEditSongs = playlist?.let { value -> { onEditSongs(value) } },
        trailingMenu = playlist?.let { value ->
            {
                TextButton(onClick = { onDeletePlaylist(value.id) }) {
                    Text(strings.DeletePlaylist)
                }
            }
        },
    )
}

private fun detailSongsOf(
    detail: DesktopDetail,
    songs: List<UiSong>,
    playlists: List<UiPlaylist>,
): List<UiSong> = when (detail) {
    is DesktopDetail.Album -> songs
        .filter { it.album == detail.name }
        .sortedWith(
            compareBy(
                { it.discNumber ?: 1 },
                { it.trackNumber ?: 0 },
                { it.title.lowercase() },
            ),
        )

    is DesktopDetail.Artist -> songs
        .filter { it.artists.contains(detail.name) }
        .sortedWith(
            compareBy(
                { it.album.orEmpty().lowercase() },
                { it.trackNumber ?: 0 },
                { it.title.lowercase() },
            ),
        )

    is DesktopDetail.Genre -> songs
        .filter { it.genres.contains(detail.name) }
        .sortedBy { it.title.lowercase() }

    is DesktopDetail.Folder -> songs
        .filter { it.path.startsWith(detail.path.trimEnd('/') + "/") }
        .sortedBy { it.title.lowercase() }

    is DesktopDetail.Playlist -> {
        val byId = songs.associateBy { it.id }
        playlists.firstOrNull { it.id == detail.id }
            ?.trackIds
            .orEmpty()
            .mapNotNull { byId[it] }
    }
}

@Composable
private fun NowPlayingBar(
    strings: Strings,
    song: UiSong,
    isPlaying: Boolean,
    mode: PlayerMode,
    positionMs: Long,
    durationMs: Long,
    volume: Int,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    onOpen: () -> Unit,
    onTogglePlay: () -> Unit,
    onCycleMode: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onVolume: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        val narrow = maxWidth < 720.dp
        Box(modifier = Modifier.align(Alignment.Center).widthIn(max = 560.dp)) {
            BentoMiniPlayerCapsule(
                song = song,
                isPlaying = isPlaying,
                progressRatio = when {
                    durationMs > 0 -> (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                    else -> 0f
                },
                artwork = { modifier -> artwork(song, modifier) },
                short = false,
                showPrevious = true,
                trackControls = true,
                seekControls = false,
                textMarquee = true,
                seekBackSeconds = 15,
                seekForwardSeconds = 30,
                labels = MiniPlayerLabels(
                    play = strings.Play,
                    pause = strings.Pause,
                    previous = strings.Previous,
                    next = strings.Next,
                    seekBack = strings.BackXSeconds("15"),
                    seekForward = strings.ForwardXSeconds("30"),
                ),
                actions = MiniPlayerActions(
                    onOpen = onOpen,
                    onPlayPause = onTogglePlay,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onSeekBack = {},
                    onSeekForward = {},
                ),
            )
        }
        if (!narrow) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCycleMode) {
                Icon(
                    when (mode) {
                        PlayerMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
                        PlayerMode.SHUFFLE -> Icons.Rounded.Shuffle
                        else -> Icons.Rounded.Repeat
                    },
                    contentDescription = playerTransportPlayModeLabel(strings, mode),
                    tint = LocalBentoColors.current.sub,
                )
            }
            Icon(
                Icons.Rounded.VolumeUp,
                null,
                tint = LocalBentoColors.current.sub,
                modifier = Modifier.padding(start = 4.dp),
            )
                Slider(
                    value = volume.toFloat(),
                    onValueChange = { onVolume(it.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.width(120.dp).padding(start = 8.dp),
                )
            }
        }
    }
}

private const val DESKTOP_PLAYER_FIXED_CONTENT_DP = 349f

/**
 * The shared player host: a bottom sheet on compact windows and a floating
 * window on wider ones, both from `PhonePlayerSheet`. The stage uses the
 * shared [PlayerStage] and [BentoPlayerQueue].
 */
@Composable
private fun DesktopPlayerHost(
    strings: Strings,
    song: UiSong,
    isPlaying: Boolean,
    mode: PlayerMode,
    positionMs: Long,
    durationMs: Long,
    volume: Int,
    level: BentoPlayerLevel,
    stage: BentoPlayerStage,
    queueEntries: List<QueueEntry>,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onStageChange: (BentoPlayerStage) -> Unit,
    onClose: () -> Unit,
    onCycleMode: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolume: (Int) -> Unit,
    onQueueEntryClick: (QueueEntry) -> Unit,
    onQueueEntryRemove: (QueueEntry) -> Unit,
    onClearUpNext: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val windowClass = rememberBentoWindowClass()
    val compact = windowClass == BentoWindowClass.COMPACT
    val floatingInset = BentoSizes.FLOATING_PLAYER_INSET.dp
    val position = UiPlaybackPosition(positionMs, durationMs)

    Box(modifier = Modifier.fillMaxSize()) {
        if (compact) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.scrim)
                    .clickable(onClick = onClose),
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val contentWidth = if (compact) {
                (maxWidth - 32.dp).coerceAtLeast(0.dp)
            } else {
                BentoSizes.FLOATING_PLAYER_WIDTH.dp - 32.dp
            }
            val availableHeight = if (compact) {
                (maxHeight - 24.dp).coerceAtLeast(0.dp)
            } else {
                minOf(
                    (maxHeight - floatingInset * 2).coerceAtLeast(0.dp),
                    BentoSizes.FLOATING_PLAYER_MEDIUM_MAX_HEIGHT.dp,
                )
            }
            val coverSize = minOf(
                contentWidth,
                (availableHeight - DESKTOP_PLAYER_FIXED_CONTENT_DP.dp).coerceAtLeast(160.dp),
            )
            val smallHeight = minOf(300.dp, availableHeight)
            val mediumHeight = minOf(availableHeight, coverSize + DESKTOP_PLAYER_FIXED_CONTENT_DP.dp)
            val sheetModifier = if (compact) {
                Modifier.align(Alignment.BottomCenter)
            } else {
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(floatingInset)
                    .width(BentoSizes.FLOATING_PLAYER_WIDTH.dp)
            }
            PhonePlayerSheet(
                level = level,
                containerHeight = if (compact) maxHeight else maxHeight - floatingInset * 2,
                smallHeight = smallHeight,
                mediumHeight = mediumHeight,
                coverSize = coverSize,
                canResize = compact,
                navigationBottomInset = 0.dp,
                onLevelChange = onLevelChange,
                onClose = onClose,
                onScrimAlphaChange = {},
                floating = !compact,
                modifier = sheetModifier,
                header = { _, headerModifier ->
                    Row(
                        modifier = headerModifier.padding(start = 20.dp, end = 8.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerHeaderInfo(
                            song = song,
                            artwork = { artworkModifier ->
                                DesktopArtwork(Path.of(song.path), artworkModifier)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        PlayerCloseButton(
                            contentDescription = strings.ClosePlayer,
                            onClose = onClose,
                        )
                    }
                },
                stage = { progress, stageModifier ->
                    PlayerStage(
                        stage = stage,
                        onStageChange = onStageChange,
                        modifier = stageModifier.growingSquare(coverSize, progress),
                        cover = { modifier -> DesktopArtwork(Path.of(song.path), modifier) },
                        lyrics = { modifier ->
                            Box(
                                modifier = modifier.background(colors.raised),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    strings.NoLyrics,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.muted,
                                )
                            }
                        },
                        queue = { modifier ->
                            BentoPlayerQueue(
                                entries = queueEntries,
                                upNextLabel = strings.UpNext,
                                countLabel = when {
                                    queueEntries.isEmpty() -> strings.EndOfQueue
                                    else -> strings.XSongs(queueEntries.size.toString())
                                },
                                endOfQueueLabel = strings.EndOfQueue,
                                saveQueueLabel = strings.SaveQueue,
                                clearUpNextLabel = strings.ClearUpNext,
                                durationText = { DurationUtils.formatMs(it.durationMillis) },
                                removeContentDescription = { strings.RemoveX(it.title) },
                                artwork = { entrySong, artworkModifier ->
                                    DesktopArtwork(Path.of(entrySong.path), artworkModifier)
                                },
                                onEntryClick = onQueueEntryClick,
                                onEntryRemove = onQueueEntryRemove,
                                onClearUpNext = onClearUpNext,
                                modifier = modifier,
                            )
                        },
                    )
                },
                title = { progress ->
                    Box(
                        modifier = Modifier
                            .collapseVertically(progress)
                            .padding(horizontal = 20.dp),
                    ) {
                        Box(modifier = Modifier.padding(top = 14.dp)) {
                            BentoPlayerTitle(
                                song = song,
                                isFavorite = false,
                                favoriteLabel = "",
                                unfavoriteLabel = "",
                                formatLine = null,
                                onToggleFavorite = {},
                                showFavorite = false,
                            )
                        }
                    }
                },
                controls = {
                    PlayerControlsContent(
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 12.dp,
                            bottom = 16.dp,
                        ),
                        seek = {
                            BentoSeekRow(
                                position = position,
                                playing = isPlaying,
                                seekable = durationMs > 0,
                                timeText = { DurationUtils.formatMs(it) },
                                onSeek = onSeek,
                            )
                        },
                        transport = {
                            BentoPlayerTransport(
                                mode = mode,
                                isPlaying = isPlaying,
                                short = compact,
                                labels = playerTransportLabels(strings, mode, 0, 0, null),
                                seekControls = false,
                                seekBackSeconds = 0,
                                seekForwardSeconds = 0,
                                sleepTimerBadge = null,
                                sleepTimerActive = false,
                                onCycleMode = onCycleMode,
                                onPrevious = onPrevious,
                                onSeekBack = {},
                                onPlayPause = onTogglePlay,
                                onSeekForward = {},
                                onNext = onNext,
                                onSleepTimerClick = {},
                                onSleepTimerLongClick = {},
                                showSleepTimer = false,
                            )
                        },
                        extras = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    onStageChange(
                                        if (stage == BentoPlayerStage.QUEUE) {
                                            BentoPlayerStage.COVER
                                        } else {
                                            BentoPlayerStage.QUEUE
                                        },
                                    )
                                },
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.QueueMusic,
                                    strings.Queue,
                                    tint = LocalBentoColors.current.sub,
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                Icons.Rounded.VolumeUp,
                                null,
                                tint = LocalBentoColors.current.sub,
                            )
                            Slider(
                                value = volume.toFloat(),
                                onValueChange = { onVolume(it.toInt()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.width(140.dp).padding(start = 8.dp),
                            )
                        }
                        },
                    )
                },
            )
        }
    }
}

/**
 * The shared search page for desktop: filter tiles in a side column on wide
 * windows (stacked compactly otherwise), then the shared results tile.
 */
@Composable
private fun DesktopSearchResults(
    strings: Strings,
    terms: String,
    onTermsChange: (String) -> Unit,
    focusRequester: androidx.compose.ui.focus.FocusRequester,
    sortBy: UiSongSort,
    sortReverse: Boolean,
    onSortByChange: (UiSongSort) -> Unit,
    onSortReverseChange: (Boolean) -> Unit,
    selectedFilter: BentoSearchFilter?,
    onFilterChange: (BentoSearchFilter?) -> Unit,
    selectedGenre: String?,
    onGenreChange: (String?) -> Unit,
    allSongs: List<UiSong>,
    playingSongId: String?,
    isPlaying: Boolean,
    nowMs: Long,
    onBack: () -> Unit,
    onPlayAll: (List<UiSong>) -> Unit,
    onShuffleAll: (List<UiSong>) -> Unit,
    onSongClick: (UiSong, List<UiSong>) -> Unit,
    onHitClick: (SearchHit) -> Unit,
) {
    val favorites = remember { emptyList<String>() }
    val filterCounts = remember(allSongs, nowMs) {
        BentoSearchFilter.entries.associateWith { filter ->
            allSongs.count { filter.matches(it, favorites, nowMs) }
        }
    }
    val genres = remember(allSongs) {
        allSongs.flatMap { it.genres }.distinct().sortedBy { it.lowercase() }
    }
    val query = terms.trim()
    val results = remember(allSongs, query, selectedFilter, selectedGenre, sortBy, sortReverse) {
        val filtered = allSongs.filter { song ->
            val matchesQuery = query.isEmpty() ||
                bentoSongMatchesQuery(song.title, song.artists, song.album, query)
            val matchesFilter = selectedFilter == null ||
                selectedFilter.matches(song, favorites, nowMs)
            val matchesGenre = selectedGenre == null || song.genres.contains(selectedGenre)
            matchesQuery && matchesFilter && matchesGenre
        }
        when (selectedFilter) {
            BentoSearchFilter.RECENTLY_ADDED ->
                filtered.sortedByDescending { it.dateModifiedMillis }

            else -> filtered.sortedBento(sortBy, sortReverse)
        }
    }
    val hits = if (query.isEmpty()) {
        emptyList()
    } else {
        buildList {
            allSongs.flatMap { it.artists }
                .distinct()
                .filter { bentoArtistNameMatchesQuery(it, query) }
                .sortedBy { it.lowercase() }
                .forEach { add(SearchHit("artist:$it", it, strings.Artist, true)) }
            allSongs.toUiAlbums()
                .filter { bentoAlbumMatchesQuery(it, query) }
                .forEach { add(SearchHit("album:${it.name}", it.name, strings.Album, false)) }
        }
    }

    fun pathOf(hit: SearchHit): Path? = when {
        hit.round -> allSongs.firstOrNull { it.artists.contains(hit.title) }
            ?.path
            ?.let(Path::of)

        else -> allSongs.firstOrNull { it.album == hit.title }
            ?.path
            ?.let(Path::of)
    }

    SearchPage(
        strings = strings,
        terms = terms,
        onTermsChange = onTermsChange,
        focusRequester = focusRequester,
        sortBy = sortBy,
        sortReverse = sortReverse,
        onSortByChange = onSortByChange,
        onSortReverseChange = onSortReverseChange,
        filterCounts = filterCounts,
        selectedFilter = selectedFilter,
        onFilterChange = onFilterChange,
        genreNames = genres,
        selectedGenre = selectedGenre,
        onGenreChange = onGenreChange,
        songs = results,
        hits = hits,
        ready = true,
        narrowed = query.isNotEmpty() || selectedFilter != null || selectedGenre != null,
        playingSongId = playingSongId,
        isPlaying = isPlaying,
        nowMs = nowMs,
        durationText = { DurationUtils.formatMs(it.durationMillis) },
        artwork = { song, modifier -> DesktopArtwork(Path.of(song.path), modifier) },
        hitArtwork = { hit, modifier ->
            val path = pathOf(hit)
            if (path != null) {
                DesktopArtwork(path, modifier)
            } else {
                Box(modifier.background(LocalBentoColors.current.raised))
            }
        },
        onPlayAll = onPlayAll,
        onShuffleAll = onShuffleAll,
        onSongClick = { song -> onSongClick(song, results) },
        onHitClick = onHitClick,
        onBack = onBack,
    )
}
