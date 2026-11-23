package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoGenreChip
import io.github.zyrouge.symphony.ui.components.BentoMiniButton
import io.github.zyrouge.symphony.ui.components.BentoSearchHitChip
import io.github.zyrouge.symphony.ui.components.BentoSmartFilterTile
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.BentoSortMenu
import io.github.zyrouge.symphony.ui.components.BentoSortOption
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.bentoAlbumMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoArtistNameMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoSongMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.components.formatBentoDaysAgo
import io.github.zyrouge.symphony.ui.components.isBentoLosslessSong
import io.github.zyrouge.symphony.ui.components.isBentoRecentlyAddedSong
import io.github.zyrouge.symphony.ui.components.label
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * The smart filters of the search page (DESIGN.md 5.5). "Most played" has no
 * data source in the app (the service layer does not track play counts and
 * stays untouched), so it is omitted.
 */
enum class BentoSearchFilter(
    val label: String,
    val icon: ImageVector,
) {
    FAVORITES("Favorites", Icons.Filled.Favorite),
    RECENTLY_ADDED("Recently added", Icons.Filled.Schedule),
    LOSSLESS("Lossless", Icons.Filled.GraphicEq),
}

/** The query-dependent half of the results; filters apply on top of it. */
internal data class BentoSearchResults(
    val songIds: List<String>,
    val artistNames: List<String>,
    val albumIds: List<String>,
)

/** Whether a song passes a smart filter. */
private fun BentoSearchFilter.test(song: Song, favorites: List<String>, nowMs: Long) =
    when (this) {
        BentoSearchFilter.FAVORITES -> favorites.contains(song.id)
        BentoSearchFilter.RECENTLY_ADDED -> isBentoRecentlyAddedSong(song.dateModified, nowMs)
        BentoSearchFilter.LOSSLESS -> isBentoLosslessSong(song.path, song.encoder)
    }

@Serializable
data class SearchViewRoute(val initialChip: String?)

/**
 * The Search page (DESIGN.md 5.5): the search field with the sort button,
 * smart filter tiles, genre chips and one results tile. Artist and album
 * hits sit above the songs; filters, genre and query combine; the
 * "Recently added" filter orders the results newest first with "N d ago"
 * badges. On wide windows the filters move to a 320 dp sticky side column.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchView(context: ViewContext, route: SearchViewRoute) {
    val colors = LocalBentoColors.current
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val allArtistNames by context.symphony.groove.artist.all.collectAsState()
    val allAlbumIds by context.symphony.groove.album.all.collectAsState()
    val genreNames by context.symphony.groove.genre.all.collectAsState()
    val favorites by context.symphony.groove.playlist.favorites.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()

    var terms by rememberSaveable { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<BentoSearchFilter?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<BentoSearchResults?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val textFieldFocusRequester = remember { FocusRequester() }
    var currentTermsRoutine by remember { mutableStateOf<Job?>(null) }

    fun setTerms(nTerms: String) {
        terms = nTerms
        isSearching = true
        currentTermsRoutine?.cancel()
        currentTermsRoutine = coroutineScope.launch {
            withContext(Dispatchers.Default) {
                delay(250)
                val query = nTerms.trim()
                val songIds = when {
                    query.isEmpty() -> allSongIds
                    else -> allSongIds.filter { songId ->
                        context.symphony.groove.song.get(songId)
                            ?.let { song ->
                                bentoSongMatchesQuery(
                                    title = song.title,
                                    artists = song.artists,
                                    album = song.album,
                                    query = query,
                                )
                            }
                            ?: false
                    }
                }
                val artistNames = when {
                    query.isEmpty() -> emptyList()
                    else -> allArtistNames.filter {
                        bentoArtistNameMatchesQuery(it, query)
                    }
                }
                val albumIds = when {
                    query.isEmpty() -> emptyList()
                    else -> allAlbumIds.filter { albumId ->
                        context.symphony.groove.album.get(albumId)
                            ?.let { bentoAlbumMatchesQuery(it, query) }
                            ?: false
                    }
                }
                results = BentoSearchResults(
                    songIds = songIds,
                    artistNames = artistNames,
                    albumIds = albumIds,
                )
                isSearching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        textFieldFocusRequester.requestFocus()
        setTerms(terms)
    }

    val nowMs = remember { System.currentTimeMillis() }
    val filterCounts by remember(allSongIds, favorites) {
        derivedStateOf {
            val songs = allSongIds.mapNotNull { context.symphony.groove.song.get(it) }
            BentoSearchFilter.entries.associateWith { filter ->
                songs.count { filter.test(it, favorites, nowMs) }
            }
        }
    }
    val resultSongs by remember(results, selectedFilter, selectedGenre, favorites, sortBy, sortReverse) {
        derivedStateOf {
            val value = results ?: return@derivedStateOf emptyList()
            val songs = value.songIds.mapNotNull { context.symphony.groove.song.get(it) }
            val activeFilter = selectedFilter
            val activeGenre = selectedGenre
            val filtered = songs.filter { song ->
                (activeFilter == null || activeFilter.test(song, favorites, nowMs)) &&
                    (activeGenre == null || song.genres.contains(activeGenre))
            }
            when (activeFilter) {
                BentoSearchFilter.RECENTLY_ADDED -> filtered.sortedByDescending {
                    it.dateModified
                }

                else -> {
                    val ids = filtered.map { it.id }
                    context.symphony.groove.song
                        .sort(ids, sortBy, sortReverse)
                        .mapNotNull { context.symphony.groove.song.get(it) }
                }
            }
        }
    }

    val windowInfo = LocalWindowInfo.current
    val compactWindow = windowInfo.containerDpSize.width < 600.dp
    val gutter = when {
        compactWindow -> BentoSpacing.GUTTER_COMPACT.dp
        else -> BentoSpacing.GUTTER_OTHER.dp
    }
    val tileGap = when {
        compactWindow -> BentoSpacing.GAP_COMPACT.dp
        else -> BentoSpacing.GAP_OTHER.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = gutter, end = gutter, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(
                    content = {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            context.symphony.t.SearchYourMusic,
                            tint = colors.fg,
                        )
                    },
                    onClick = { context.navController.popBackStack() },
                )
                SearchField(
                    context = context,
                    terms = terms,
                    onTermsChange = { setTerms(it) },
                    focusRequester = textFieldFocusRequester,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(
                        content = {
                            Icon(
                                Icons.AutoMirrored.Rounded.Sort,
                                "Sort songs by",
                                tint = colors.fg,
                            )
                        },
                        onClick = { showSortMenu = !showSortMenu },
                    )
                    BentoSortMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        title = context.symphony.t.SortSongsBy,
                        options = SongRepository.SortBy.entries.map {
                            BentoSortOption(it, it.label(context))
                        },
                        selected = sortBy,
                        onSelect = {
                            context.symphony.settings.lastUsedSongsSortBy.setValue(it)
                        },
                        reverse = sortReverse,
                        reverseLabel = "Reverse order",
                        onReverseChange = {
                            context.symphony.settings.lastUsedSongsSortReverse.setValue(it)
                        },
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val innerWidth = maxWidth - gutter * 2
                val wide = innerWidth >= 720.dp &&
                    rememberBentoWindowClass() != BentoWindowClass.SHORT_HEIGHT

                when {
                    // Wide layout (DESIGN.md 5.5): the filters move to a 320 dp
                    // sticky left column and the genre chips wrap.
                    wide -> Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = gutter),
                        horizontalArrangement = Arrangement.spacedBy(tileGap),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(BentoSizes.SIDE_COLUMN.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BentoSearchFilterTiles(
                                context = context,
                                selectedFilter = selectedFilter,
                                filterCounts = filterCounts,
                                onFilterChange = { selectedFilter = it },
                                sideColumn = true,
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BentoGenreChips(
                                    context = context,
                                    genreNames = genreNames,
                                    selectedGenre = selectedGenre,
                                    onGenreChange = { selectedGenre = it },
                                )
                            }
                        }
                        SearchResultsTile(
                            context = context,
                            results = results,
                            resultSongs = resultSongs,
                            artistNames = results?.artistNames ?: emptyList(),
                            albumIds = results?.albumIds ?: emptyList(),
                            selectedFilter = selectedFilter,
                            narrowed = terms.isNotBlank() ||
                                selectedFilter != null ||
                                selectedGenre != null,
                            playingSongId = playingSong.songId,
                            playing = playingSong.isPlaying,
                            nowMs = nowMs,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    else -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = gutter),
                        verticalArrangement = Arrangement.spacedBy(tileGap),
                    ) {
                        BentoSearchFilterTiles(
                            context = context,
                            selectedFilter = selectedFilter,
                            filterCounts = filterCounts,
                            onFilterChange = { selectedFilter = it },
                            sideColumn = false,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BentoGenreChips(
                                context = context,
                                genreNames = genreNames,
                                selectedGenre = selectedGenre,
                                onGenreChange = { selectedGenre = it },
                            )
                        }
                        SearchResultsTile(
                            context = context,
                            results = results,
                            resultSongs = resultSongs,
                            artistNames = results?.artistNames ?: emptyList(),
                            albumIds = results?.albumIds ?: emptyList(),
                            selectedFilter = selectedFilter,
                            narrowed = terms.isNotBlank() ||
                                selectedFilter != null ||
                                selectedGenre != null,
                            playingSongId = playingSong.songId,
                            playing = playingSong.isPlaying,
                            nowMs = nowMs,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
