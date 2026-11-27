package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.matches
import io.github.zyrouge.symphony.ui.components.bentoAlbumMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoArtistNameMatchesQuery
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.bentoSongMatchesQuery
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.model.BentoSearchFilter
import io.github.zyrouge.symphony.ui.model.UiSongSort
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.SearchPage
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import io.github.zyrouge.symphony.ui.model.BentoSearchResults
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SearchViewRoute(val initialChip: String?)

/**
 * The Search page (DESIGN.md 5.5): the search field with the sort button,
 * smart filter tiles, genre chips and one results tile. Artist and album
 * hits sit above the songs; filters, genre and query combine; the
 * "Recently added" filter orders the results newest first with "N d ago"
 * badges. On wide windows the filters move to a 320 dp sticky side column;
 * elsewhere the header, filters and results scroll as one list.
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
    // Search keeps its own sort, starting from the Songs tab's, so changing it here
    // does not re-sort the Songs tab and every detail page.
    var sortBy by rememberSaveable { mutableStateOf(context.symphony.settings.lastUsedSongsSortBy.value) }
    var sortReverse by rememberSaveable { mutableStateOf(context.symphony.settings.lastUsedSongsSortReverse.value) }

    var terms by rememberSaveable { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<BentoSearchFilter?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<BentoSearchResults?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val textFieldFocusRequester = remember { FocusRequester() }
    var currentTermsRoutine by remember { mutableStateOf<Job?>(null) }

    fun setTerms(nTerms: String) {
        terms = nTerms
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
            }
        }
    }

    LaunchedEffect(Unit) {
        setTerms(terms)
    }

    val nowMs = remember { System.currentTimeMillis() }
    val filterCounts by remember(allSongIds, favorites) {
        derivedStateOf {
            val songs = allSongIds.mapNotNull { context.symphony.groove.song.get(it) }
            BentoSearchFilter.entries.associateWith { filter ->
                songs.count { song ->
                    filter.matches(
                        songId = song.id,
                        dateModifiedMs = song.dateModified,
                        path = song.path,
                        encoder = song.encoder,
                        favorites = favorites,
                        nowMs = nowMs,
                    )
                }
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
                val matchesFilter = activeFilter == null || activeFilter.matches(
                    songId = song.id,
                    dateModifiedMs = song.dateModified,
                    path = song.path,
                    encoder = song.encoder,
                    favorites = favorites,
                    nowMs = nowMs,
                )
                matchesFilter &&
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

    val t = context.symphony.t
    val uiSongs = remember(resultSongs) { resultSongs.map { it.toUiSong() } }
    val songsById = remember(resultSongs) { resultSongs.associateBy { it.id } }
    val hits = remember(results?.artistNames, results?.albumIds) {
        searchHitsOf(
            context = context,
            t = t,
            artistNames = results?.artistNames ?: emptyList(),
            albumIds = results?.albumIds ?: emptyList(),
        )
    }
    val narrowed = terms.isNotBlank() ||
        selectedFilter != null ||
        selectedGenre != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
    SearchPage(
        strings = t,
        terms = terms,
        onTermsChange = { setTerms(it) },
        focusRequester = textFieldFocusRequester,
        sortBy = UiSongSort.valueOf(sortBy.name),
        sortReverse = sortReverse,
        onSortByChange = { sortBy = SongRepository.SortBy.valueOf(it.name) },
        onSortReverseChange = { sortReverse = it },
        filterCounts = filterCounts,
        selectedFilter = selectedFilter,
        onFilterChange = { selectedFilter = it },
        genreNames = genreNames,
        selectedGenre = selectedGenre,
        onGenreChange = { selectedGenre = it },
        songs = uiSongs,
        hits = hits,
        ready = results != null,
        narrowed = narrowed,
        playingSongId = playingSong.songId,
        isPlaying = playingSong.isPlaying,
        nowMs = nowMs,
        durationText = { DurationUtils.formatMs(it.durationMillis) },
        artwork = { song, artworkModifier ->
            songsById[song.id]?.let { original ->
                AsyncImage(
                    original.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = artworkModifier,
                )
            } ?: Box(artworkModifier.background(LocalBentoColors.current.raised))
        },
        hitArtwork = { hit, artworkModifier ->
            SearchHitArtwork(context, hit, artworkModifier)
        },
        menu = { song, expanded, onDismissRequest ->
            songsById[song.id]?.let { original ->
                SongDropdownMenu(
                    context = context,
                    song = original,
                    isFavorite = favorites.contains(song.id),
                    expanded = expanded,
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        onPlayAll = { list ->
            context.symphony.radio.shorty.playQueue(
                list.map { it.id },
                source = t.SearchResults,
            )
        },
        onShuffleAll = { list ->
            context.symphony.radio.shorty.playQueue(
                list.map { it.id },
                shuffle = true,
                source = t.SearchResults,
            )
        },
        onSongClick = { song ->
            context.symphony.radio.shorty.playQueue(
                uiSongs.map { it.id },
                Radio.PlayOptions(
                    index = uiSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0),
                ),
                source = t.SearchResults,
            )
        },
        onHitClick = { hit -> openSearchHit(context, hit) },
        onBack = { context.navController.popBackStack() },
        bottomInset = bentoBottomInset(),
    )
    }
}
